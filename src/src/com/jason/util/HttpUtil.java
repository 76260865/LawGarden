package com.jason.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class HttpUtil {
    private static final String TAG = "HttpUtil";

    public static String doGet(String url, List<BasicNameValuePair> params) {
        String param = URLEncodedUtils.format(params, "UTF-8");
        HttpGet httpGet = new HttpGet(url + "?" + param);
        HttpClient httpClient = new DefaultHttpClient();
        String result = null;

        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return result;
    }

    public static String doPost(String url, JSONObject obj) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("charset", HTTP.UTF_8);
        String result = null;

        try {
            httpPost.setEntity(new StringEntity(obj.toString(), HTTP.UTF_8));
            HttpClient httpClient = new DefaultHttpClient();
            result = null;

            HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                Log.d("HttpUtil", "result:" + result);
            } else {
                Log.d("HttpUtil", "httpResponse.getStatusCode:"
                        + httpResponse.getStatusLine().getStatusCode());
                result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                Log.d("HttpUtil", "failure result:" + result);
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return result;
    }
}
