package com.gg.busStation.function.internet;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpClientHelper {
    private HttpClientHelper() {
    }

    public static void getDataAsync(String url, Callback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static ResponseBody getBody(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        if (response.body() == null) {
            throw new IOException("Response body is null");
        }

        return response.body();
    }

    public static String getData(String url) throws IOException {
        ResponseBody body = getBody(url);

        return body.string();
    }

    public static InputStream getDataStream(String url) throws IOException {
        ResponseBody body = getBody(url);

        body.contentLength();

        return body.byteStream();
    }
}
