package de.hfu.ashiqmoh.cardiaccustodian;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hfu.ashiqmoh.cardiaccustodian.enums.HttpMethod;
import de.hfu.ashiqmoh.cardiaccustodian.enums.HttpOperation;

public class HttpTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "HttpTask";

    private Response response;
    private String url;
    private HttpMethod httpMethod;
    private HttpOperation httpOperation;

    public HttpTask(Response response, String url, HttpMethod httpMethod, HttpOperation httpOperation) {
        this.response = response;
        this.url = url;
        this.httpMethod = httpMethod;
        this.httpOperation = httpOperation;
    }

    @Override
    protected String doInBackground(String... json) {

        // params comes from the execute() call: params[0] is the url
        String jsonString = json[0];

        try {
            return downloadUrl(jsonString);
        } catch (IOException e) {
            return "Unable to retrieve web page. URL may be invalid";
        }
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        if (response != null) {
            response.onPostExecute(this.httpOperation, result);
        }
    }

    private String downloadUrl(String jsonString) throws IOException {

        InputStream is = null;
        HttpURLConnection conn = null;
        Log.v(TAG, "url:" + this.url);
        try {
            URL url = new URL(this.url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000); // milliseconds
            conn.setConnectTimeout(15000); // milliseconds
            conn.setRequestMethod(httpMethod.toString());
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            // Start query
            conn.connect();

            // Create json string String using Gson
            // Gson gson = new Gson();
            // String jsonString = gson.toJson(user);

            Log.v(TAG, jsonString);

            // Send POST output with json string as byte array
            if (jsonString.length() > 0) {
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.write(jsonString.getBytes());
                out.flush();
                out.close();
            }

            // get connection response code
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "The response code is: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // retrieve JSON response as InputStream
                is = conn.getInputStream();

                // Convert the InputStream into a string and return it
                return readIt(is);
            }
            return "Connection error: " + responseCode;

        } finally {
            // Make sure that the InputStream is closed after the app is finished using it.
            if (is != null) {
                is.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public String readIt(InputStream inputStream) throws IOException {
        BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
        StringBuilder sBuilder = new StringBuilder();

        String line;
        while ((line = bReader.readLine()) != null) {
            sBuilder.append(line);
            sBuilder.append("\n");
        }

        return sBuilder.toString();
    }

    public interface Response {
        void onPostExecute(HttpOperation HttpOperation, String result);
    }
}
