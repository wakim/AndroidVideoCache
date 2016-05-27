package com.danikula.videocache.sample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

/**
 * Created on 5/27/16.
 */
@EActivity(R.layout.download_file_activity)
public class DownloadFileActivity extends Activity implements CacheListener {
    private final static String TAG = DownloadFileActivity.class.getName();

    private final static String DOWNLOAD_URL = "http://y-sf.smule.com/y6/sing/performance/renvideo/cf/4a/f70993a0-907f-4dd8-a5e1-a5329edaaa6e.mp4";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onCacheAvailable(final File cacheFile, final String url, final int percentsAvailable) {

    }

    @Click(R.id.download_button)
    protected void downloadButtonClicked() {
        HttpProxyCacheServer proxy = App.getProxy(this);
        proxy.registerCacheListener(this, DOWNLOAD_URL);

        String url = proxy.getProxyUrl(DOWNLOAD_URL);

        DownloadTask downloadTask = new DownloadTask(url);
        downloadTask.execute();
    }

    private class DownloadTask extends AsyncTask<Void, Void, Void> {
        String mUrl;
        public DownloadTask(String url) {
            super();

            mUrl = url;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            InputStream is = null;
            HttpURLConnection connection = null;

            try {
                //Don't try to use a proxy to access our proxy. Proxyception.
                connection = (HttpURLConnection) new URL(mUrl).openConnection(Proxy.NO_PROXY);

                is = new BufferedInputStream(connection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                char buffer[] = new char[1024];

                //noinspection StatementWithEmptyBody
                while (br.read(buffer) != -1) {

                }
            }
            catch (Exception e) {
                Log.e(TAG, "Error reading InputStream", e);
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (IOException e) {
                        Log.e(TAG, "Error closing InputStream", e);
                    }
                }

                if (connection != null) {
                    connection.disconnect();
                }
            }

            return null;
        }
    }

}

