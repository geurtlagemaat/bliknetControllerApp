package com.oriana.bliknetController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by geurt on 28-1-2016.
 */
public class cameraAct extends Activity {
    private CameraSettings CamSettings = null;

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view,
                                              HttpAuthHandler handler, String host, String realm) {
            handler.proceed(CamSettings.getCameraUser(), CamSettings.getCameraPW());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_act);

        BliknetApp applBliknet = (BliknetApp) getApplicationContext();

        Intent intent = getIntent();
        CamSettings = applBliknet.getAppPersistence().getCameraSettings(intent.getStringExtra("cam"));

        WebView myBrowser = (WebView) findViewById(R.id.webViewCamera);
        myBrowser.getSettings().setJavaScriptEnabled(true);
        /* myBrowser.getSettings().setLoadWithOverviewMode(true);
        myBrowser.getSettings().setUseWideViewPort(true); */
        myBrowser.setWebViewClient(new MyWebViewClient());

        if (CamSettings != null){
            setTitle(CamSettings.getCameraLabel());
            myBrowser.loadUrl(CamSettings.getCameraURL());
        }
        myBrowser.requestFocus();
    }
}
