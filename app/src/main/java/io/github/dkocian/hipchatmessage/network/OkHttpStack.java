package io.github.dkocian.hipchatmessage.network;

import com.android.volley.toolbox.HurlStack;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dkocian on 8/4/2015.
 */
public class OkHttpStack extends HurlStack {
    private final OkUrlFactory okUrlFactory;
    public OkHttpStack() {
        okUrlFactory = new OkUrlFactory(new OkHttpClient());
    }
    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {
        return okUrlFactory.open(url);
    }
}
