package com.example.android.newsup;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * News Up created by JCoupier on 19/06/2017.
 *
 * Helper methods related to requesting and receiving news data from the guardian API.
 */
public final class NewsUtils {

    /** Tag for the log messages */
    private static final String LOG_TAG = NewsUtils.class.getSimpleName();

    // Constant keys
    private static final String KEY_RESPONSE = "response";
    private static final String KEY_RESULTS = "results";
    private static final String KEY_WEB_TITLE = "webTitle";
    private static final String KEY_SECTION_NAME = "sectionName";
    private static final String KEY_WEB_URL = "webUrl";
    private static final String KEY_FIELDS = "fields";
    private static final String KEY_THUMBNAIL = "thumbnail";

    /**
     * Create a private constructor because no one should ever create a {@link NewsUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name NewsUtils (and an object instance of NewsUtils is not needed).
     */
    private NewsUtils() {
    }

    /**
     * Return a list of {@link News} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<News> extractFeatureFromJson(String newsJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding news to
        List<News> newsList = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            if (baseJsonResponse.has(KEY_RESPONSE)) {
                JSONObject responseObject = baseJsonResponse.getJSONObject(KEY_RESPONSE);
                if (responseObject.has(KEY_RESULTS)) {
                    // Extract the JSONArray associated with the key called "results",
                    // which represents a list of results (or news).
                    JSONArray newsArray = responseObject.getJSONArray(KEY_RESULTS);

                    // For each news in the newsArray, create an {@link News} object
                    for (int i = 0; i < newsArray.length(); i++) {

                        // Get a single news at position i within the list of news
                        JSONObject currentNews = newsArray.getJSONObject(i);

                        // Extract the value for the key called "webTitle"
                        String title;
                        if (currentNews.has(KEY_WEB_TITLE)) {
                            title = currentNews.getString(KEY_WEB_TITLE);
                        } else {
                            // Handle the case if there is no title
                            title = ("No title found");
                        }

                        // Extract the value for the key called "sectionName"
                        String sectionName;
                        if (currentNews.has(KEY_SECTION_NAME)) {
                            sectionName = currentNews.getString(KEY_SECTION_NAME);
                        } else {
                            // Handle the case if there is no section name
                            sectionName = ("No section name found");
                        }

                        // Extract the value for the key called "webUrl"
                        String webUrl;
                        if (currentNews.has(KEY_WEB_URL)) {
                            webUrl = currentNews.getString(KEY_WEB_URL);
                        } else {
                            // Handle the case if there is no webUrl
                            webUrl = ("No website link found");
                        }

                        // Extract the value for the key called "thumbnail" in the JSONObject "fields"
                        JSONObject imageLinks;
                        String imageUrl;
                        if (currentNews.has(KEY_FIELDS)) {
                            imageLinks = currentNews.getJSONObject(KEY_FIELDS);
                            if (imageLinks.has(KEY_THUMBNAIL)) {
                                imageUrl = imageLinks.getString(KEY_THUMBNAIL);
                            } else {
                                // Handle the case if there is no image available
                                imageUrl = ("No image found");
                            }
                        } else {
                            // Handle the case if there is no field as there is no image result
                            imageUrl = ("No image found");
                        }

                        // Create a new {@link News} object with the title, sectionName, imageUrl
                        // and webUrl from the JSON response. Add it to the list of news.
                        newsList.add(new News(title, sectionName, imageUrl, webUrl));
                    }
                }
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the news JSON results", e);
        }
        // Return the list of news
        return newsList;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        int readTimeOut = 10000;
        int connectTimeOut = 15000;
        int okResponseCode = 200;

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(readTimeOut /* milliseconds */);
            urlConnection.setConnectTimeout(connectTimeOut /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == okResponseCode) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Query the Guardian API and return a list of {@link News} objects.
     */
    public static List<News> fetchNewsData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        return extractFeatureFromJson(jsonResponse);
    }
}

