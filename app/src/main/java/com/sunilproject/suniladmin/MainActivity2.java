package com.sunilproject.suniladmin;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.asksira.webviewsuite.WebViewSuite;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity2 extends AppCompatActivity {

    //WebViewSuite webviewid;
    PDFView pdfView;
    String uri = "";
    ProgressBar progressbar;
    InputStream inputStream;
    String realuri="",realpath="" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        String pdfurl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";
        uri = "https://docs.google.com/gview?embedded=true&url="+"https://firebasestorage.googleapis.com/v0/b/sunil-admin.appspot.com/o/1661178567165.pdf?alt=media&token=c4df17cb-7002-42c7-97bf-6a36f569aeee";
        String pdfuri = "https://firebasestorage.googleapis.com/v0/b/sunil-admin.appspot.com/o/1661178567165.pdf?alt=media&token=c4df17cb-7002-42c7-97bf-6a36f569aeee";

        progressbar = findViewById(R.id.progressbar);
        //progressbar.setVisibility(View.GONE);
        pdfuri = getIntent().getStringExtra("linkuri");
        realuri = getIntent().getStringExtra("realuri");
        realpath = getIntent().getStringExtra("realpath");
        //inputStream = getIntent().getParcelableExtra("intentStream");
        inputStream = MainActivity.getInStreams();
        //webviewid = findViewById(R.id.webViewSuite);
        pdfView = findViewById(R.id.pdfView);
        //pdfView.fromUri(Uri.parse(uri)).load();
        //new RetrivePDFfromUrl().execute(pdfuri);
        //pdfView.fromFile( new File(realpath)).load();
        new RetrivePDFfromFile().execute(realpath);
        Log.d("picwidth","---inp stream : inte ; "+inputStream);
        //pdfView.fromStream(inputStream).load();
        Log.d("picwidth","---inp stream : inte ; "+inputStream);
        progressbar.setVisibility(View.GONE);
        //webviewid.startLoading(uri);

//        webviewid.customizeClient(new WebViewSuite.WebViewSuiteCallback() {
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                //Do your own stuffs. These will be executed after default onPageStarted().
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                if(view.getTitle().equals("")){
//                    view.loadUrl(url);
//                }
//                //Do your own stuffs. These will be executed after default onPageFinished().
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                //Override those URLs you need and return true.
//                //Return false if you don't need to override that URL.
//                return true;
//            }
//        });
//        webviewid.getSettings().setJavaScriptEnabled(true);
//        webviewid.getSettings().setLoadWithOverviewMode(true);
//        webviewid.getSettings().setDomStorageEnabled(true);
//        webviewid.zoomOut();
//        webviewid.zoomIn();
//        webviewid.getSettings().setBuiltInZoomControls(true);
//        webviewid.getSettings().setLoadWithOverviewMode(true);
//        webviewid.getSettings().setUseWideViewPort(true);
//        webviewid.getSettings().setBlockNetworkLoads(false);
//        webviewid.getSettings().setUseWideViewPort(true);
//        webviewid.loadUrl("https://www.brototypecrossroads.com");
//        webviewid.setWebViewClient(new WebViewClient(){
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                //progDailog.show();
//                view.loadUrl(url);
//                Log.d("urlk","----------------- loadUrl : "+url);
//
////                Intent intent = new Intent(Intent.ACTION_VIEW);
////                intent.setDataAndType(Uri.parse(url), "application/pdf");
////                try{
////                    view.getContext().startActivity(intent);
////                } catch (ActivityNotFoundException e) {
////                    //user does not have a pdf viewer installed
////                }
//
//                return true;
//            }
//            @Override
//            public void onPageFinished(WebView view, final String url) {
//                Log.d("urlk","----------------- url : "+url+"\nview : "+view.getTitle()+"\nori url "+view.getOriginalUrl());
//                //progDailog.dismiss();
//                if (view.getTitle().equals("")) {
//                    view.reload();
//                    view.loadUrl(url);
//                }
//            }
//
////            @Override
////            public boolean shouldOverrideUrlLoading(WebView view, String url) {
////                if ( urlIsPDF(url)){
////                    Intent intent = new Intent(Intent.ACTION_VIEW);
////                    intent.setDataAndType(Uri.parse(url), "application/pdf");
////                    try{
////                        view.getContext().startActivity(intent);
////                    } catch (ActivityNotFoundException e) {
////                        //user does not have a pdf viewer installed
////                    }
////                } else {
////                    webview.loadUrl(url);
////                }
////                return true;
////            }
//
//        });

    }

    // create an async task class for loading pdf file from URL.
    class RetrivePDFfromUrl extends AsyncTask<String, Void, InputStream> {
        @Override
        protected InputStream doInBackground(String... strings) {
            // we are using inputstream
            // for getting out PDF.
            InputStream inputStream = null;
            try {
                URL url = new URL(strings[0]);
                // below is the step where we are
                // creating our connection.
                HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    // response is success.
                    // we are getting input stream from url
                    // and storing it in our variable.
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }

            } catch (IOException e) {
                // this is the method
                // to handle errors.
                e.printStackTrace();
                return null;
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            // after the execution of our async
            // task we are loading our pdf in our pdf view.
            pdfView.fromStream(inputStream).load();
        }
    }

    class RetrivePDFfromFile extends AsyncTask<String, Void, File> {
        @Override
        protected File doInBackground(String... strings) {
            // we are using inputstream
            // for getting out PDF.
            File inputStream = null;
            try {
                //URL url = new URL(strings[0]);
                inputStream = new File(strings[0]);
                // below is the step where we are
                // creating our connection.
//                HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
//                if (urlConnection.getResponseCode() == 200) {
//                    // response is success.
//                    // we are getting input stream from url
//                    // and storing it in our variable.
//                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
//                }

            } catch (Exception e) {
                // this is the method
                // to handle errors.
                e.printStackTrace();
                return null;
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(File inputStream) {
            // after the execution of our async
            // task we are loading our pdf in our pdf view.
            pdfView.fromFile(inputStream).load();
        }
    }
}