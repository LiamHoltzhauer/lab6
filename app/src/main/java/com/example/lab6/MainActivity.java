package com.example.lab6;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.catImageView);
        progressBar = findViewById(R.id.progressBar);

        // Start AsyncTask to fetch dog images
        new DogImages().execute();
    }

    // Inner AsyncTask class for fetching dog images
    public class DogImages extends AsyncTask<Void, Integer, Bitmap> {
        private Bitmap dogImage;
        private boolean running = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE); // Show the progress bar
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            while (running) {
                try {
                    // Get random dog image JSON from the API
                    String apiUrl = "https://dog.ceo/api/breeds/image/random";
                    String jsonResponse = fetchJson(apiUrl);

                    // Parse JSON to get the image URL
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    String dogUrl = jsonObject.getString("message"); // The URL is in the "message" field

                    // Check if the image already exists locally
                    String dogId = dogUrl.substring(dogUrl.lastIndexOf('/') + 1); // Extract image name from URL
                    File dogFile = new File(getCacheDir(), dogId); // Save it without extension
                    if (dogFile.exists()) {
                        dogImage = BitmapFactory.decodeFile(dogFile.getAbsolutePath());
                    } else {
                        // Download image and save to local storage
                        dogImage = downloadImage(dogUrl);
                        saveImageToFile(dogImage, dogFile);
                    }

                    // Simulate progress
                    for (int i = 0; i < 100; i++) {
                        publishProgress(i);
                        Thread.sleep(30); // Simulate some processing time
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    running = false; // Stop the loop if there's an error
                }
            }
            return dogImage; // Return the last downloaded dog image
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update the progress bar
            progressBar.setProgress(progress[0]);

            // Update the ImageView with the new image when progress completes a cycle
            if (progress[0] == 99 && dogImage != null) {
                imageView.setImageBitmap(dogImage);
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            progressBar.setVisibility(View.GONE); // Hide progress bar when done
        }

        @Override
        protected void onCancelled() {
            running = false; // Stop the loop when cancelled
        }

        // Method to fetch JSON response from API
        private String fetchJson(String urlString) throws Exception {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder jsonResult = new StringBuilder();
                int data = inputStream.read();
                while (data != -1) {
                    jsonResult.append((char) data);
                    data = inputStream.read();
                }
                return jsonResult.toString();
            } finally {
                urlConnection.disconnect();
            }
        }

        // Method to download the image from URL
        private Bitmap downloadImage(String urlString) throws Exception {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        }

        // Method to save the image to a local file
        private void saveImageToFile(Bitmap image, File file) throws Exception {
            FileOutputStream fos = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        }
    }
}