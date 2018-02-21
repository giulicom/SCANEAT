package com.app.project.scaneatapp;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.support.annotation.NonNull;
import android.util.Log;

import javax.net.ssl.HttpsURLConnection;



public class JSONParser {

    static JSONObject jObj;
    static JSONArray jArr;

    // constructor
    public JSONParser() {

    }

    public static JSONObject serverRequest(String _url, HashMap<String, String> params) {
        String result = null;

        try {
            URL url = new URL(_url);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(15000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            Log.d("Json server request: " , ""+params);
            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            Log.d("Json server request: " , ""+params);
            try {
                writer.write(getPostDataString(params));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (java.net.SocketTimeoutException e){
                Log.d("Error timeout","");
                e.printStackTrace();
                final String timeout = "{\"error\":\"timeout\"}";
                try {
                    jObj = new JSONObject(timeout);
                } catch (JSONException d) {
                    Log.e("JSON Parser", "Error parsing data timeout" + d.toString());
                }

                return jObj;

            }

            writer.flush();
            writer.close();
            os.close();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            int responseCode = urlConnection.getResponseCode();
            //Log.d("Json server request: " , ""+params);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                result = readStream(in);
                Log.d("Json Parser: ","risposta server -> "+result);


            }

            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            jObj = new JSONObject(result);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        return jObj;
    }

    public static JSONArray serverRequestArray(String _url, HashMap<String, String> params) {
        String result = null;

        try {
            URL url = new URL(_url);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(15000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));


            try {
                writer.write(getPostDataString(params));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            writer.flush();
            writer.close();
            os.close();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                result = readStream(in);

            }

            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            jArr = new JSONArray(result);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        return jArr;
    }



    private static String readStream(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        String result = "";

        while ((line = br.readLine()) != null) {
            result += line;
        }

        in.close();

        return result;
    }

    @NonNull
    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }


}