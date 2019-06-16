package com.reactlibrary;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RNEpubParserDownload extends AsyncTask<Void, Integer, Void> {
    private File file;
    private String url;
    private Listener listener;

    RNEpubParserDownload(String url, File file, Listener listener){
        this.url = url;
        this.file = file;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onStarted();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream input = new BufferedInputStream(connection.getInputStream(), 8192);
            OutputStream output = new FileOutputStream(file);

            int count;
            byte data[] = new byte[8192];

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
        } catch (Exception e) {}

        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        listener.onComplete(file);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        listener.onProgress(values[0]);
    }

    public interface Listener {
        void onStarted();
        void onComplete(File file);
        void onProgress(Integer progress);
    }
}
