package com.sunilproject.suniladmin;

import static com.google.common.net.MediaType.PDF;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfRenderer;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.asksira.webviewsuite.WebViewSuite;
import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jsibbold.zoomage.ZoomageView;
//import com.labters.documentscanner.DocumentScannerView;
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter;
//import com.scanlibrary.ScanActivity;
//import com.pdfview.PDFView;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.sunilproject.suniladmin.utils.Callback;
import com.sunilproject.suniladmin.utils.ImageFilePath;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    LinearLayout choose_from_file,view_image_ll;
    ImageView camera_iv,view_image;
    final int REQUEST_PERMISSION_CODE = 1000;
    public static final String TAG = "MainActivity1";
    Uri filePath;
    Button upload_btn;
    Uri imageUri = null;
    String imageurl="";
    ZoomageView myZoomageView;
    // instance for firebase storage and StorageReference
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference storageReference;
    boolean isCamera = false;
    Button scan_image,remove_shadow;
    Bitmap photo,originalbitmap;
    DocumentFilter documentFilter = new DocumentFilter();
    SharedPreferences sharedPreferences;
    ProgressBar progressbar;
    boolean isScanImageClicked = false;
    boolean isRemoveShadowClicked = false;
    LinearLayout keywordview_ll,btSelect;
    TextView keywordview_tv;
    String keywordtext="";
    TextInputEditText textInputEdittext_article_title,textInputEdittext_article_description
            ,textInputEdittext_podcast_keyword,textInputEdittext_article_newspaper_name;
    Button news_date_btn;

    Intent mData=null;

    // Initialize variable
    //Button btSelect;
    TextView tvUri, tvPath;
    ActivityResultLauncher<Intent> resultLauncher;
//    ImageView imgpdf;
    WebViewSuite webviewid;
    PDFView pdfView;
    String uri = "";
    PdfRenderer renderer;


    private DatePicker datePicker;
    private Calendar calendar;
    //private TextView dateView;
    private int year, month, day;
    private String pickedDate="";
    private long pickedDate_timestamp=0l;

   private static InputStream intentStream;
    String pageurl="https://www.google.com";
    LinearLayout webviewlayoutid;
    ImageView imgview;
    String realPth="",realuri="";
    //PDFView pdfview;

    //DocumentScannerView documentScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choose_from_file = findViewById(R.id.choose_from_file);
        camera_iv = findViewById(R.id.camera_iv);
        upload_btn= findViewById(R.id.upload_btn);
        scan_image = findViewById(R.id.scan_image);
        remove_shadow = findViewById(R.id.remove_shadow);
        webviewlayoutid = findViewById(R.id.webviewlayoutid);
        imgview =   findViewById(R.id.img_pdf);
        imgview.setVisibility(View.GONE);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        //webviewlayoutid.setVisibility(View.GONE);
        webviewlayoutid.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),MainActivity2.class);
            intent.putExtra("linkuri",pageurl);
            intent.putExtra("realpath",realPth);
            intent.putExtra("realuri",realuri);
            //intent.putExtra("intentStream", (Parcelable) intentStream[0]);
            startActivity(intent);
        });


        webviewid = findViewById(R.id.webViewSuitee);
//        webviewid.customizeClient(new WebViewSuite.WebViewSuiteCallback() {
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                Log.d("urlk","------------------- url ++--- :"+url);
//                //Do your own stuffs. These will be executed after default onPageStarted().
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                if(view.getTitle().equals("")){
//                    view.loadUrl(url);
//
//                }
//                Log.d("urlk","------------------- url ++ ,  :"+pageurl);
//                view.loadUrl(pageurl);
//                webviewid.refresh();
//                //Do your own stuffs. These will be executed after default onPageFinished().
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                //Override those URLs you need and return true.
//                //Return false if you don't need to override that URL.
//                Log.d("urlk","------------------- url ++ :"+url);
//                view.loadUrl(url);
//                webviewid.refresh();
//                return true;
//            }
//        });
//        webviewid.getSettings().setJavaScriptEnabled(true);
//        webviewid.setWebViewClient(new WebViewClient());


//        webviewid.getSettings().setJavaScriptEnabled(true);
//        webviewid.getSettings().setLoadWithOverviewMode(true);
//        webviewid.getSettings().setDomStorageEnabled(true);
//        webviewid.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//        webviewid.zoomOut();
//        webviewid.zoomIn();
//        webviewid.getSettings().setBuiltInZoomControls(true);
//        webviewid.getSettings().setLoadWithOverviewMode(true);
//        webviewid.getSettings().setUseWideViewPort(true);
////        webviewid.getSettings().setLoadWithOverviewMode(true);
////        webviewid.getSettings().setUseWideViewPort(true);
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


        //String url = "https://google.com";
//        String url = "https://firebasestorage.googleapis.com/v0/b/sunil-admin.appspot.com/o/1659526508060.pdf?alt=media&token=a4cb67aa-54d7-434b-aa3d-95188e6e9183";
//        try {
//            //url=URLEncoder.encode(url,"UTF-8");
//            webviewid.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url="+url);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
        //webviewid.loadUrl("https://docs.google.com/viewer?url="+"https://firebasestorage.googleapis.com/v0/b/sunil-admin.appspot.com/o/1659526508060.pdf?alt=media&token=a4cb67aa-54d7-434b-aa3d-95188e6e9183");



        //webviewid.loadUrl("https://docs.google.com/gview?embedded=true&url="+"https://firebasestorage.googleapis.com/v0/b/sunil-admin.appspot.com/o/1659526508060.pdf?alt=media&token=a4cb67aa-54d7-434b-aa3d-95188e6e9183");
        //webviewid.loadUrl("https://firebasestorage.googleapis.com/v0/b/sunil-admin.appspot.com/o/1659526508060.pdf?alt=media&token=a4cb67aa-54d7-434b-aa3d-95188e6e9183");
        //webviewid.loadUrl("https://facebook.com");
        //pdfview = findViewById(R.id.pdfview);
        //documentScannerView = findViewById(R.id.document_scanner);

        view_image_ll = findViewById(R.id.view_image_ll);
        view_image = findViewById(R.id.view_image);
        view_image.setVisibility(View.GONE);
        myZoomageView = findViewById(R.id.myZoomageView);
        view_image_ll.setVisibility(View.GONE);
        progressbar = findViewById(R.id.progressbar);
        progressbar.setVisibility(View.GONE);
        textInputEdittext_podcast_keyword = findViewById(R.id.textInputEdittext_podcast_keyword);
        textInputEdittext_article_title = findViewById(R.id.textInputEdittext_article_title);
        textInputEdittext_article_newspaper_name = findViewById(R.id.textInputEdittext_article_newspaper_name);
        news_date_btn = findViewById(R.id.news_date_btn);
        news_date_btn.setOnClickListener(v -> {
            showDialog(999);
        });
        textInputEdittext_article_description = findViewById(R.id.textInputEdittext_article_description);
        keywordview_ll= findViewById(R.id.keywordview_ll);
        keywordview_tv = findViewById(R.id.keywordview_tv);
        // assign variable
        btSelect = findViewById(R.id.choose_pdf_file);
        tvUri = findViewById(R.id.tv_uri);
        tvPath = findViewById(R.id.tv_path);
        pdfView = findViewById(R.id.pdfView);

        //webviewid.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        //webviewid.loadUrl("https://www.ajmal.com");
        webviewid.startLoading("https://docs.google.com/gview?embedded=true&url="+"https://firebasestorage.googleapis.com/v0/b/sunil-admin.appspot.com/o/1661178567165.pdf?alt=media&token=c4df17cb-7002-42c7-97bf-6a36f569aeee");

//        startActivity(new Intent(getApplicationContext(),MainActivity2.class));
//        finish();
        //webviewid.loadUrl("https://github.github.com/training-kit/downloads/github-git-cheat-sheet.pdf");

        //webviewid.loadUrl("https://www.docs.google.com/gview?embedded=true&url="+"https://www.adobe.com/support/products/enterprise/knowledgecenter/media/c4611_sample_explain.pdf");
        // Initialize result launcher
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result)
                    {

                        // Initialize result data
                        Intent data = result.getData();
                        // check condition
                        if (data != null) {
                            // When data is not equal to empty
                            // Get PDf uri
                            findViewById(R.id.progressbar1).setVisibility(View.VISIBLE);
                            Uri sUri = data.getData();
                            // set Uri on text view

                            // Get PDF path
                            String sPath = sUri.getPath();
                            // Set path on text view
//                            tvPath.setText(Html.fromHtml(
//                                    "<big><b>PDF Path</b></big><br>"
//                                            + sPath));
                            tvPath.setText("");

                            //

//                            try {
//                                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(sUri,"r");
//                                renderer = new PdfRenderer(parcelFileDescriptor);
//                                _display();
//                            } catch (FileNotFoundException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }

                            //this func occure must be in upload button
                            mData = data;
                            String realPath = "";
                            realPath = ImageFilePath.getPath(MainActivity.this, data.getData());
//                realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());

                            Log.d("jdf","---------  real path ; "+realPath
                            );
                            Log.d("jdf","---------  sUri  ; "+sUri
                            );

                            long timestmp = System.currentTimeMillis();

                            File f = new File(realPath);
                            String finalRealPath = realPath;
                            convertPdfToBitmap(f, new Callback() {
                                @Override
                                public void onSuccess() {
                                    pdfBitmap = TrimBitmap(pdfBitmap, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                            Log.d("jdf","convertPdfto image2  ");
                                            imgview.setVisibility(View.VISIBLE);
                                            imgview.setImageBitmap(pdfBitmap);

                                            ZoomageView imgvieww =   findViewById(R.id.myZoomageView);
                                            //view_image_ll.setVisibility(View.VISIBLE);
                                            imgvieww.setVisibility(View.VISIBLE);
                                            imgvieww.setImageBitmap(pdfBitmap);
                                            realPth= finalRealPath;
                                            realuri = String.valueOf(sUri);

                                            tvPath.setText(Html.fromHtml(
                                                    "<big><b>PDF Uri</b></big><br>"
                                                            + realPth));
                                            //progressDialog.dismiss();

//                                            uploadFile(pdfBitmap, timestmp, new Callback() {
//                                                @Override
//                                                public void onSuccess() {
//                                                    //progressDialog.dismiss();
//                                                }
//
//                                                @Override
//                                                public void onFailure(String error) {
//
//                                                }
//                                            });
                                        }

                                        @Override
                                        public void onFailure(String error) {

                                        }
                                    });


                                }

                                @Override
                                public void onFailure(String error) {

                                }
                            });


//                            uploadPdfFile(data,timestmp, new Callback() {
//
//                                @Override
//                                public void onSuccess() {
//
//                                    try {
//
//                                        tvPath.setText(Html.fromHtml("<big><b>PDF url Path</b></big><br>" + pdfUrl));
//                                        tvPath.setSelectAllOnFocus(true);
//
//                                        webviewid.setDrawingCacheEnabled(true);
//                                        //webviewid.loadUrl("https://www.docs.google.com/gview?embedded=true&url="+pdfUrl);
//                                        webviewid.startLoading("https://docs.google.com/gview?embedded=true&url="+pdfUrl);
//                                        //"https://docs.google.com/gview?embedded=true&url="+
//                                        pageurl = String.valueOf(pdfUrl);
//
//                                        findViewById(R.id.progressbar1).setVisibility(View.GONE);
//                                        webviewlayoutid.setVisibility(View.VISIBLE);
//                                        Picture picture = webviewid.getWebView().capturePicture();
//
//
//                                        Log.d("picwidth","------ pic : width : "+picture.getWidth());
//
////                                        Picture picture = webviewid.getWebView().capturePicture();
////                                        Log.d("picwidth","------ pic : width : "+picture.getWidth());
////                                        Bitmap bitmap = Bitmap.createBitmap(picture.getWidth(),picture.getHeight(),Bitmap.Config.RGB_565);
////
////                                        Canvas canvas = new Canvas(bitmap);
////                                        canvas.drawColor(Color.RED);
////                                        picture.draw(canvas);
////                                        String pathofBmp = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "data", null);
////                                        Uri bmpUri = Uri.parse(pathofBmp);
////
////                                        Bitmap bm = webviewid.getDrawingCache();
////                                        BitmapDrawable bitmapDrawable = new BitmapDrawable(bm);
//                                       // image.setBackgroundDrawable(bitmapDrawable);
//
//                                        //ImageView imageView = findViewById(R.id.img_pdf);
//
//                                        //imageView.setImageBitmap(bitmap);
//                                        //Glide.with(imageView).load(bitmap);
//                                        //imageView.setImageBitmap(bm);
//                                       // imageView.setImageURI(bmpUri);
//
//                                        //new RetrieveFeedTask().execute(String.valueOf(pdfUrl));
//
//                                        new RetrivePDFfromUrl(new AsyncResponse() {
//                                            @Override
//                                            public void processFinish(InputStream output) {
//                                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//
//                                                    @Override
//                                                    public void run() {
//
//                                                        //pdfView.fromStream(output).load();
//
//                                                    }
//                                                }, 2000);
//                                                intentStream = output;
//                                                Log.d("picwidth","------ processFinish : "+output);
//                                            }
//                                        })
//                                                .execute(String.valueOf(pdfUrl))
//                                        ;
//
//                                        webviewid.refresh();
//                                        //webviewid.loadUrl("https://www.google.com");
//                                        //webviewid.loadUrl("https://docs.google.com/gview?embedded=true&url="+"https://github.github.com/training-kit/downloads/github-git-cheat-sheet.pdf");
//                                        //webviewid.loadUrl("https://github.github.com/training-kit/downloads/github-git-cheat-sheet.pdf");
//
//                                        Log.d("urlk","--------------------pdfurl---- tvPath.getText() : "+tvPath.getText());
//
//                                        Log.d("urlk","--------------------pdfurl---- pdfUrl : "+pdfUrl);
//                                    }catch (Exception e){
//                                        Log.d("urlk","--------------------pdfurl---- Exception : "+e.getMessage());
//                                    }
////                                    try {
////
////                                    webviewid.loadUrl(String.valueOf(pdfUrl));
////                                    Log.d("urlk","------------------------ pdfurl : "+pdfUrl);
////                                    String url="";
////                                    String pdf = "https://firebasestorage.googleapis.com/v0/b/mindlabz-6f809.appspot.com/o/course_content%2FCracking%20the%20Coding%20Interview.pdf?alt=media&token=12a7fb3d-92d8-49f6-bd13-f88ec634c06c";
////                                    try {
////                                        url= URLEncoder.encode(pdfUrl.toString(),"UTF-8");
////                                    } catch (UnsupportedEncodingException e) {
////
////                                        Log.d("urlk","------------------------ UnsupportedEncodingException : "+e.getMessage());
////                                        e.printStackTrace();
////                                    }
////                                    webviewid.loadUrl(url);
////                                }catch (Exception e){
////
////                                        Log.d("urlk","------------------------ Exception : "+e.getMessage());
////                                }
//                                }
//
//                                @Override
//                                public void onFailure(String error) {
//                                    Toast.makeText(MainActivity.this, "Error --- : "+error, Toast.LENGTH_SHORT).show();
//                                }
//                            });

                            //pdfView.fromFile(new File(sPath)).load();
                        }
                    }
                });


        // Set click listener on button
        btSelect.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v)
                    {
                        if(checkPermissionFromDevice()){
                            // check condition
                            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                // When permission is not granted
                                // Result permission
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE }, 1);
                            }
                            else {
                                // When permission is granted
                                // Create method
                                selectPDF();
                            }
                        }
                        else {
                            requestPermission();
                        }

                    }
                });

        textInputEdittext_podcast_keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s != null) {

                String s1 = textInputEdittext_podcast_keyword.getText().toString();
                // if(s1.contains(",")){

                //textInputEdittext_podcast_keyword.setText("");
                //  }
                keywordtext = String.valueOf(s);
                // if(keywordtext.contains(",")){

//                    keywordview_ll.setVisibility(View.VISIBLE);
//                    String[] splittedkeywords = keywordtext.split(",");
//                    StringBuilder keyword_str= new StringBuilder();
//                    for(int i = 0 ;i<splittedkeywords.length;i++){
//                        keyword_str.append(splittedkeywords[i]+"   ");

                // }
                Log.d(TAG, "--------------- keywordtext : " + keywordtext);
//                if(String.valueOf(s).contains(",")){
//                    String.valueOf(s).replace(",","\n");
//                }
                keywordview_tv.setText(String.valueOf(s).replace(",", "\n"));
                //  }
                //else {
                //keywordview_ll.setVisibility(View.GONE);
                //}
            }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        firestore = FirebaseFirestore.getInstance();

        sharedPreferences = getSharedPreferences("suni_app",MODE_PRIVATE);


        ActivityResultLauncher<Intent> intentreultlaucher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        progressbar.setVisibility(View.GONE);
                    }
                });

        view_image.setOnClickListener(v -> {

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    //doubleBackToExitPressedOnce=false;
                    progressbar.setVisibility(View.VISIBLE);
                }
            }, 10);

            Intent intent = new Intent(getApplicationContext(),ViewFullImageActivity.class);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();

            SharedPreferences.Editor shEditor = sharedPreferences.edit();
            String saveThis = Base64.encodeToString(bytes, Base64.DEFAULT);
            shEditor.putString("bitmapbytes", saveThis);
            shEditor.apply();
            shEditor.commit();

            //startActivity(intent);
            intentreultlaucher.launch(intent);
            //progressbar.setVisibility(View.GONE);
        });

//        imgpdf.setOnClickListener(v -> {
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//                    //doubleBackToExitPressedOnce=false;
//                    progressbar.setVisibility(View.VISIBLE);
//                }
//            }, 10);
//
//            Intent intent = new Intent(getApplicationContext(),ViewFullImageActivity.class);
//
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//            byte[] bytes = stream.toByteArray();
//
//            SharedPreferences.Editor shEditor = sharedPreferences.edit();
//            String saveThis = Base64.encodeToString(bytes, Base64.DEFAULT);
//            shEditor.putString("bitmapbytes", saveThis);
//            shEditor.apply();
//            shEditor.commit();
//
//            //startActivity(intent);
//            intentreultlaucher.launch(intent);
//        });

        //documentScannerView.setImage(photo);

        myZoomageView.setOnClickListener(v -> {


//            Intent intent = new Intent(this, ScanActivity.class);
//            intent.putExtra(ScanActivity.EXTRA_BRAND_IMG_RES, com.scanlibrary.R.drawable.ic_crop_white_24dp); // Set image for title icon - optional
//            intent.putExtra(ScanActivity.EXTRA_TITLE, "Crop Document"); // Set title in action Bar - optional
//            intent.putExtra(ScanActivity.EXTRA_ACTION_BAR_COLOR, R.color.purple_500); // Set title color - optional
//            intent.putExtra(ScanActivity.EXTRA_LANGUAGE, "en"); // Set language - optional
//            startActivityForResult(intent, REQUEST_CODE_SCAN);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    //doubleBackToExitPressedOnce=false;
                    progressbar.setVisibility(View.VISIBLE);
                }
            }, 10);

            Intent intent = new Intent(getApplicationContext(),ViewFullImageActivity.class);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();

            SharedPreferences.Editor shEditor = sharedPreferences.edit();
            String saveThis = Base64.encodeToString(bytes, Base64.DEFAULT);
            shEditor.putString("bitmapbytes", saveThis);
            shEditor.apply();
            shEditor.commit();

            //startActivity(intent);
            intentreultlaucher.launch(intent);
            //progressbar.setVisibility(View.GONE);
        });

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        remove_shadow.setOnClickListener(v -> {
            if(isRemoveShadowClicked){
                isRemoveShadowClicked = false;
                remove_shadow.setText("Add Shadow Filter");
                photo = originalbitmap;
                view_image.setImageBitmap(photo);
                myZoomageView.setImageBitmap(photo);
                view_image_ll.setVisibility(View.VISIBLE);
            }else {
                isRemoveShadowClicked = true;
                remove_shadow.setText("Remove shadow Filter");
                if(photo!=null){
                    ProgressDialog progressDialog
                            = new ProgressDialog(this);
                    progressDialog.setTitle("Wait...");
                    progressDialog.show();
                    // replace getFilter_Name with the filter you want to use
                    documentFilter.getShadowRemoval(photo, new DocumentFilter.CallBack<Bitmap>()
                    {
                        @Override
                        public void onCompleted(Bitmap bitmap) {
                            // Do your tasks here with the returned bitmap
                            photo = bitmap;
                            view_image.setImageBitmap(bitmap);
                            myZoomageView.setImageBitmap(bitmap);

                            view_image_ll.setVisibility(View.VISIBLE);
                            progressDialog.dismiss();
                        }
                    });

                }else {
                    Toast.makeText(this, "Pick valid image first", Toast.LENGTH_SHORT).show();
                }

            }
        });

        scan_image.setOnClickListener(v -> {

            if(isScanImageClicked){
                isScanImageClicked = false;
                scan_image.setText("Scan Image");
                photo = originalbitmap;
                view_image.setImageBitmap(photo);
                myZoomageView.setImageBitmap(photo);
                view_image_ll.setVisibility(View.VISIBLE);

            }else {
                isScanImageClicked = true;
                scan_image.setText("Remove Scan Image");
                if(photo!=null){
                    ProgressDialog progressDialog
                            = new ProgressDialog(this);
                    progressDialog.setTitle("Wait...");
                    progressDialog.show();
                    // replace getFilter_Name with the filter you want to use
                    documentFilter.getMagicFilter(photo, new DocumentFilter.CallBack<Bitmap>() {
                        @Override
                        public void onCompleted(Bitmap bitmap) {
                            // Do your tasks here with the returned bitmap
                            photo = bitmap;
                            view_image.setImageBitmap(bitmap);
                            myZoomageView.setImageBitmap(bitmap);
                            view_image_ll.setVisibility(View.VISIBLE);
                            progressDialog.dismiss();
                        }
                    });

                }else {
                    Toast.makeText(this, "Pick valid image first", Toast.LENGTH_SHORT).show();
                }
            }
//
//            if(photo!=null){
//                ProgressDialog progressDialog
//                        = new ProgressDialog(this);
//                progressDialog.setTitle("Wait...");
//                progressDialog.show();
//                // replace getFilter_Name with the filter you want to use
//                documentFilter.getMagicFilter(photo, new DocumentFilter.CallBack<Bitmap>() {
//                    @Override
//                    public void onCompleted(Bitmap bitmap) {
//                        // Do your tasks here with the returned bitmap
//                        photo = bitmap;
//                        view_image.setImageBitmap(bitmap);
//                        view_image_ll.setVisibility(View.VISIBLE);
//                        progressDialog.dismiss();
//                    }
//                });
//
//            }else {
//                Toast.makeText(this, "Pick valid image first", Toast.LENGTH_SHORT).show();
//            }
        });



        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getResultCode() == Activity.RESULT_OK) {

                            // There are no request codes
                            Intent data = result.getData();
//                            String requestCode = (String) data.getExtras().get("requestCode");
                            //Log.d(TAG,"------------------------ requestCode ; "+data+" -------------photo : ");
//                            assert data != null;
                            if(result.getData() == null){
//                                photo = (Bitmap)data.getExtras()
//                                        .get("data");
//                                filePath = (Uri) data.getExtras()
//                                        .get("data");
                                //uploadFile(photo);

                                try {
                                    //Uri imageUri = null;
                                    photo = MediaStore.Images.Media.getBitmap(
                                            getContentResolver(), imageUri);

                                    Log.d(TAG,"---------------file path : "+imageUri+"  filepath.getpath : "+imageUri.getPath()+" photo bitmap : "+photo);
                                    ExifInterface ei = new ExifInterface(getRealPathFromURI(imageUri));
                                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                            ExifInterface.ORIENTATION_UNDEFINED);

                                    Bitmap rotatedBitmap = null;
                                    switch(orientation) {

                                        case ExifInterface.ORIENTATION_ROTATE_90:
                                            rotatedBitmap = rotateImage(photo, 90);
                                            break;

                                        case ExifInterface.ORIENTATION_ROTATE_180:
                                            rotatedBitmap = rotateImage(photo, 180);
                                            break;

                                        case ExifInterface.ORIENTATION_ROTATE_270:
                                            rotatedBitmap = rotateImage(photo, 270);
                                            break;

                                        case ExifInterface.ORIENTATION_NORMAL:
                                        default:
                                            rotatedBitmap = photo;
                                    }
                                    photo = rotatedBitmap;
                                    originalbitmap = photo;
                                    view_image.setImageBitmap(photo);
                                    myZoomageView.setImageBitmap(photo);
                                    view_image_ll.setVisibility(View.VISIBLE);
                                    imageurl = getRealPathFromURI(imageUri);
                                    //view_image.setRotationY(90);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(MainActivity.this, "Exception ; "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                isCamera= true;
                                Toast.makeText(MainActivity.this, "success : "+result, Toast.LENGTH_SHORT).show();
                                Log.d(TAG,"------------------------ result ; "+result.getData()+" -------------photo : "+photo.getAllocationByteCount());
                            }
                            else {
                                filePath = data.getData();
                                try {

                                    view_image_ll.setVisibility(View.VISIBLE);
                                    // Setting image on image view using Bitmap
                                    photo = MediaStore
                                            .Images
                                            .Media
                                            .getBitmap(
                                                    getContentResolver(),
                                                    filePath);

                                    String tempcheck = String.valueOf(filePath);
                                    if(tempcheck.contains("content://media/external/images/")){
                                        Log.d(TAG,"-------@@--------file path : "+filePath+"  filepath.getpath : "+filePath.getPath()+" filepaths  :'' ");
                                        ExifInterface ei = null;
                                        try {
                                            Log.d(TAG,"---------------file path : "+filePath+"  filepath.getpath : "+filePath.getPath()+" filepaths  : 000");

                                            ei = new ExifInterface(getRealPathFromURI(filePath));

                                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                                    ExifInterface.ORIENTATION_UNDEFINED);

                                            Bitmap rotatedBitmap = null;
                                            switch(orientation) {

                                                case ExifInterface.ORIENTATION_ROTATE_90:
                                                    rotatedBitmap = rotateImage(photo, 90);
                                                    break;

                                                case ExifInterface.ORIENTATION_ROTATE_180:
                                                    rotatedBitmap = rotateImage(photo, 180);
                                                    break;

                                                case ExifInterface.ORIENTATION_ROTATE_270:
                                                    rotatedBitmap = rotateImage(photo, 270);
                                                    break;

                                                case ExifInterface.ORIENTATION_NORMAL:
                                                default:
                                                    rotatedBitmap = photo;
                                            }
                                            photo = rotatedBitmap;
                                            originalbitmap = photo;

                                            //Glide.with(getApplicationContext()).load(filePath.getPath()).into(view_image);
                                            view_image.setImageBitmap(photo);
                                            myZoomageView.setImageBitmap(photo);
                                            //imageurl = getRealPathFromURI(filePath);
                                            isCamera = false;
                                            //imageView.setImageBitmap(bitmap);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            originalbitmap = photo;
                                            view_image.setImageBitmap(photo);
                                            myZoomageView.setImageBitmap(photo);
                                            Log.d(TAG,"---------------exception : "+e.getMessage());
                                        }
                                    }
                                    else {

                                        String filePaths = getPathAPI19(getApplicationContext(), filePath, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onFailure(String error) {

                                            }
                                        });
                                        getPathAPI19(getApplicationContext(), filePath, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                ExifInterface ei = null;
                                                try {
                                                    Log.d(TAG,"---------------file path : "+filePath+"  filepath.getpath : "+filePath.getPath()+" filepaths  : "+filePaths);

                                                    ei = new ExifInterface(filePaths);

                                                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                                            ExifInterface.ORIENTATION_UNDEFINED);

                                                    Bitmap rotatedBitmap = null;
                                                    switch(orientation) {

                                                        case ExifInterface.ORIENTATION_ROTATE_90:
                                                            rotatedBitmap = rotateImage(photo, 90);
                                                            break;

                                                        case ExifInterface.ORIENTATION_ROTATE_180:
                                                            rotatedBitmap = rotateImage(photo, 180);
                                                            break;

                                                        case ExifInterface.ORIENTATION_ROTATE_270:
                                                            rotatedBitmap = rotateImage(photo, 270);
                                                            break;

                                                        case ExifInterface.ORIENTATION_NORMAL:
                                                        default:
                                                            rotatedBitmap = photo;
                                                    }
                                                    photo = rotatedBitmap;
                                                    originalbitmap = photo;

                                                    //Glide.with(getApplicationContext()).load(filePath.getPath()).into(view_image);
                                                    view_image.setImageBitmap(photo);
                                                    myZoomageView.setImageBitmap(photo);
                                                    //imageurl = getRealPathFromURI(filePath);
                                                    isCamera = false;
                                                    //imageView.setImageBitmap(bitmap);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                    originalbitmap = photo;
                                                    view_image.setImageBitmap(photo);
                                                    myZoomageView.setImageBitmap(photo);
                                                    Log.d(TAG,"---------------exception : "+e.getMessage());
                                                }
                                            }

                                            @Override
                                            public void onFailure(String error) {

                                            }
                                        });
//                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//
//                                        @Override
//                                        public void run() {
//                                            //doubleBackToExitPressedOnce=false;
//                                            Log.d(TAG,"---------------file path : "+filePath+"  filepath.getpath : "+filePath.getPath()+" filepaths  : "+filePaths);
//                                            ExifInterface ei = null;
//                                            try {
//                                                ei = new ExifInterface(filePaths);
//
//                                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                                                    ExifInterface.ORIENTATION_UNDEFINED);
//
//                                            Bitmap rotatedBitmap = null;
//                                            switch(orientation) {
//
//                                                case ExifInterface.ORIENTATION_ROTATE_90:
//                                                    rotatedBitmap = rotateImage(photo, 90);
//                                                    break;
//
//                                                case ExifInterface.ORIENTATION_ROTATE_180:
//                                                    rotatedBitmap = rotateImage(photo, 180);
//                                                    break;
//
//                                                case ExifInterface.ORIENTATION_ROTATE_270:
//                                                    rotatedBitmap = rotateImage(photo, 270);
//                                                    break;
//
//                                                case ExifInterface.ORIENTATION_NORMAL:
//                                                default:
//                                                    rotatedBitmap = photo;
//                                            }
//                                            photo = rotatedBitmap;
//
//                                            //Glide.with(getApplicationContext()).load(filePath.getPath()).into(view_image);
//                                            view_image.setImageBitmap(photo);
//                                            //imageurl = getRealPathFromURI(filePath);
//                                            isCamera = false;
//                                            //imageView.setImageBitmap(bitmap);
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                                view_image.setImageBitmap(photo);
//                                                Log.d(TAG,"---------------exception : "+e.getMessage());
//                                            }
//                                        }
//                                    }, 1000);
                                        Log.d(TAG,"---------------file path : "+filePath+"  filepath.getpath : "+filePath.getPath()+" filepaths  : "+filePaths);

                                    }

                                }
                                catch (IOException e) {
                                    // Log the exception
                                    e.printStackTrace();
                                    Toast.makeText(MainActivity.this, "Exception ; "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                Toast.makeText(MainActivity.this, "success : "+data.getData().getPath(), Toast.LENGTH_SHORT).show();
                                Log.d(TAG,"------------------------ result ; "+result+" -------------picked : ");
                            }
                            //Toast.makeText(MainActivity.this, "success : "+data.getData(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });



        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Map<String,Object> contentMap = new HashMap<>();
               // uploadImage();
                if(mData!=null){
                    ArrayList<String> keyword_arrlist = new ArrayList<>();

                    if(!keywordtext.equals("")){
                        if(keywordtext.contains(",")){
                            String[] splittedkeywords = keywordtext.split(",");
                            StringBuilder keyword_str= new StringBuilder();

                            for (String splittedkeyword : splittedkeywords) {
                                keyword_str.append(splittedkeyword);
                                keyword_arrlist.add(splittedkeyword.trim());
                            }
                        }else {
                            keyword_arrlist.add(keywordtext);
                        }
                        contentMap.put("content_keywords",keyword_arrlist);


                        if(!textInputEdittext_article_newspaper_name.getText().toString().equals(""))
                        {
                            if(!pickedDate.equals("")){

                                if(!textInputEdittext_article_title.getText().toString().equals("")){


                                    contentMap.put("views",0);
                                    contentMap.put("comment_count",0);
                                    contentMap.put("newspaperDate_timestamp",pickedDate_timestamp);
                                    contentMap.put("newspaper_date",pickedDate);
                                    contentMap.put("newspaper_name",textInputEdittext_article_newspaper_name.getText().toString());
                                    contentMap.put("content_topic",textInputEdittext_article_title.getText().toString());
                                    if(!textInputEdittext_article_description.getText().toString().equals("")){

                                        contentMap.put("content_decription",textInputEdittext_article_description.getText().toString());
                                    }else {

                                        contentMap.put("content_decription","");
                                    }

//                            contentMap.put("podcastName",imageName);
//                            contentMap.put("podcasttime",imageTime);
//                            contentMap.put("adminEmail", FirebaseAuth.getInstance().getCurrentUser().getEmail());

                                    //Upload pdf

                                    if(mData!=null){

                                        long timestmp = System.currentTimeMillis();

                                        uploadFile(pdfBitmap, timestmp, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                //progressDialog.dismiss();
                                                uploadPdfFile(mData,timestmp, new Callback() {

                                                    @Override
                                                    public void onSuccess() {

                                                        try {

                                                            long time= System.currentTimeMillis();
                                                            time = timestmp;
                                                            imageName = "pdf"+time;
                                                            imageTime = String.valueOf(time);

                                                            tvPath.setText(Html.fromHtml("<big><b>PDF url Path</b></big><br>" + pdfUrl));
                                                            tvPath.setSelectAllOnFocus(true);
                                                            //webviewid.loadUrl("https://www.docs.google.com/gview?embedded=true&url="+pdfUrl);
                                                            //webviewid.startLoading("https://docs.google.com/gview?embedded=true&url="+pdfUrl);
                                                            //"https://docs.google.com/gview?embedded=true&url="+
                                                            pageurl = String.valueOf(pdfUrl);

                                                            findViewById(R.id.progressbar1).setVisibility(View.GONE);
                                                            webviewlayoutid.setVisibility(View.VISIBLE);

                                                            //String image_url = task.getResult().toString();

                                                            //                            Map<String,Object> contentMap = new HashMap<>();
//                            contentMap.put("content_title",);
//                            contentMap.put("content_decription",description_str);
                                                            //contentMap.put("adminEmail", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                                            contentMap.put("image_name",imageName);
                                                            contentMap.put("uploaded_time",imageTime);
//                            contentMap.put("content_keywords",contentKeywords);
                                                            contentMap.put("pdf_url",pageurl);
                                                            contentMap.put("image_url",imageUrl);

                                                            Log.d(TAG,"-------------contentmap ; "+contentMap);
//
                                                            firestore.collection("content_db").document(imageTime).set(contentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful()){
                                                                        //progressDialog.dismiss();
                                                                        Toast.makeText(MainActivity.this, "Data Successfully stored..", Toast.LENGTH_SHORT).show();
                                                                        photo = null;
                                                                        view_image.setImageBitmap(null);
                                                                        view_image_ll.setVisibility(View.GONE);
                                                                    }

                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(MainActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });


//
//                                            generateImageFromPdf(pdfUrl, new Callback() {
//                                                @Override
//                                                public void onSuccess() {
//                                                    Toast.makeText(MainActivity.this, "onsuc", Toast.LENGTH_SHORT).show();
//                                                    ImageView imageView = findViewById(R.id.img_pdf);
//                                                    imageView.setImageBitmap(bitmap);
//                                                    Log.d("bitmap1","---- : "+bitmap.getByteCount());
//
//                                                }
//
//                                                @Override
//                                                public void onFailure(String error) {
//
//                                                }
//                                            });

                                                            new RetrivePDFfromUrl(new AsyncResponse() {
                                                                @Override
                                                                public void processFinish(InputStream output) {
                                                                    //pdfView.fromStream(output).load();
                                                                    Log.d("picwidth","------ processFinish : "+output);
                                                                }
                                                            })
                                                            //        .execute(String.valueOf(pdfUrl))
                                                            ;

                                                            //webviewid.refresh();
                                                            //webviewid.loadUrl("https://www.google.com");
                                                            //webviewid.loadUrl("https://docs.google.com/gview?embedded=true&url="+"https://github.github.com/training-kit/downloads/github-git-cheat-sheet.pdf");
                                                            //webviewid.loadUrl("https://github.github.com/training-kit/downloads/github-git-cheat-sheet.pdf");

                                                            Log.d("urlk","--------------------pdfurl---- tvPath.getText() : "+tvPath.getText());

                                                            Log.d("urlk","--------------------pdfurl---- pdfUrl : "+pdfUrl);
                                                        }catch (Exception e){
                                                            Log.d("urlk","--------------------pdfurl---- Exception : "+e.getMessage());
                                                        }
//                                    try {
//
//                                    webviewid.loadUrl(String.valueOf(pdfUrl));
//                                    Log.d("urlk","------------------------ pdfurl : "+pdfUrl);
//                                    String url="";
//                                    String pdf = "https://firebasestorage.googleapis.com/v0/b/mindlabz-6f809.appspot.com/o/course_content%2FCracking%20the%20Coding%20Interview.pdf?alt=media&token=12a7fb3d-92d8-49f6-bd13-f88ec634c06c";
//                                    try {
//                                        url= URLEncoder.encode(pdfUrl.toString(),"UTF-8");
//                                    } catch (UnsupportedEncodingException e) {
//
//                                        Log.d("urlk","------------------------ UnsupportedEncodingException : "+e.getMessage());
//                                        e.printStackTrace();
//                                    }
//                                    webviewid.loadUrl(url);
//                                }catch (Exception e){
//
//                                        Log.d("urlk","------------------------ Exception : "+e.getMessage());
//                                }
                                                    }

                                                    @Override
                                                    public void onFailure(String error) {
                                                        Toast.makeText(MainActivity.this, "Error --- : "+error, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(String error) {

                                            }
                                        });

                                    }else {
                                        Toast.makeText(MainActivity.this, "Select pdf file...", Toast.LENGTH_SHORT).show();
                                    }

                                    //Uploading photo
//                            uploadFile(photo, new Callback() {
//                                @Override
//                                public void onSuccess() {
//
//                                    StorageReference starsRef = storageReference.child("images/img" + imageTime);
//                                    starsRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Uri> task) {
//
//                                            if(task.isSuccessful())
//                                            {
//                                                String image_url = task.getResult().toString();
//
//                                                //                            Map<String,Object> contentMap = new HashMap<>();
////                            contentMap.put("content_title",);
////                            contentMap.put("content_decription",description_str);
//                                                //contentMap.put("adminEmail", FirebaseAuth.getInstance().getCurrentUser().getEmail());
//                                                contentMap.put("image_name",imageName);
//                                                contentMap.put("uploaded_time",imageTime);
////                            contentMap.put("content_keywords",contentKeywords);
//                                                contentMap.put("image_url",image_url);
//
//                                                Log.d(TAG,"-------------contentmap ; "+contentMap);
////
//                                                firestore.collection("content_db").document(imageTime).set(contentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<Void> task) {
//
//                                                        if(task.isSuccessful()){
//                                                            progressDialog.dismiss();
//                                                            Toast.makeText(MainActivity.this, "Data Successfully stored..", Toast.LENGTH_SHORT).show();
//                                                            photo = null;
//                                                            view_image.setImageBitmap(null);
//                                                            view_image_ll.setVisibility(View.GONE);
//                                                        }
//
//                                                    }
//                                                }).addOnFailureListener(new OnFailureListener() {
//                                                    @Override
//                                                    public void onFailure(@NonNull Exception e) {
//                                                        Toast.makeText(MainActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                                                    }
//                                                });
//
//                                            }
//
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Toast.makeText(MainActivity.this, "Failure : "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//
//
//                                }
//
//                                @Override
//                                public void onFailure(String error) {
//                                    Toast.makeText(MainActivity.this, "Error : "+error, Toast.LENGTH_SHORT).show();
//                                }
//                            });
                                    //////////////////////////////
                                }else {
                                    Toast.makeText(MainActivity.this, "Enter Article Topic", Toast.LENGTH_SHORT).show();
                                }

                            }else {

                                Toast.makeText(MainActivity.this, "Pick News Date.", Toast.LENGTH_SHORT).show();
                            }

                        }else {
                            Toast.makeText(MainActivity.this, "Enter News paper Name...", Toast.LENGTH_SHORT).show();
                        }



                    }else {
                        Toast.makeText(MainActivity.this, "Enter at least one keyword", Toast.LENGTH_SHORT).show();
                    }





                }else {
                    Toast.makeText(MainActivity.this, "Select pdf file first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        camera_iv.setOnClickListener(v -> {

            if(checkPermissionFromDevice()){

//                Intent camera_intent
//                        = new Intent(MediaStore
//                        .ACTION_IMAGE_CAPTURE);

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                //startActivityForResult(intent, PICTURE_RESULT);
                intent.putExtra("requestCode", "cameraIntent");
                someActivityResultLauncher.launch(intent);
            }else{

                requestPermission();
            }
        });

        choose_from_file.setOnClickListener(v -> {


            if(checkPermissionFromDevice()){

                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");
                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                chooserIntent.putExtra("requestCode", "imagePickIntent");
                someActivityResultLauncher.launch(chooserIntent);
            }else {
                requestPermission();
            }
        });


    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                }
            };

    private void showDate(int year, int month, int day) {
        StringBuilder dDate = new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year);
        news_date_btn.setText(dDate);
        pickedDate = dDate.toString();

        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = (Date)formatter.parse(dDate.toString());

            System.out.println("Today is " +date.getTime());
            pickedDate_timestamp = date.getTime();
            Log.d("timestamp","------- timestamp : "+pickedDate_timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

        public static Bitmap TrimBitmap(Bitmap bmp,Callback callback) {
        int imgHeight = bmp.getHeight();
        int imgWidth  = bmp.getWidth();


        //TRIM WIDTH - LEFT
        int startWidth = 0;
        for(int x = 0; x < imgWidth; x++) {
            if (startWidth == 0) {
                for (int y = 0; y < imgHeight; y++) {
                    if (bmp.getPixel(x, y) != Color.TRANSPARENT) {
                        startWidth = x;
                        break;
                    }
                }
            } else break;
        }


        //TRIM WIDTH - RIGHT
        int endWidth  = 0;
        for(int x = imgWidth - 1; x >= 0; x--) {
            if (endWidth == 0) {
                for (int y = 0; y < imgHeight; y++) {
                    if (bmp.getPixel(x, y) != Color.TRANSPARENT) {
                        endWidth = x;
                        break;
                    }
                }
            } else break;
        }



        //TRIM HEIGHT - TOP
        int startHeight = 0;
        for(int y = 0; y < imgHeight; y++) {
            if (startHeight == 0) {
                for (int x = 0; x < imgWidth; x++) {
                    if (bmp.getPixel(x, y) != Color.TRANSPARENT) {
                        startHeight = y;
                        break;
                    }
                }
            } else break;
        }



        //TRIM HEIGHT - BOTTOM
        int endHeight = 0;
        for(int y = imgHeight - 1; y >= 0; y--) {
            if (endHeight == 0 ) {
                for (int x = 0; x < imgWidth; x++) {
                    if (bmp.getPixel(x, y) != Color.TRANSPARENT) {
                        endHeight = y;
                        break;
                    }
                }
            } else break;
        }

        callback.onSuccess();
        return Bitmap.createBitmap(
                bmp,
                startWidth,
                startHeight,
                endWidth - startWidth,
                endHeight - startHeight
        );

    }

    Bitmap pdfBitmap;
    private void convertPdfToBitmap(File documentFile,Callback callback) {

        Log.d("jdf","convertPdfto image1");

            //Convert pdf to bitmap
            // Create the page renderer for the PDF document.
        ParcelFileDescriptor fileDescriptor = null;
        try {
            Log.d("jdf","convertPdfto image1 suc31113");
            fileDescriptor = ParcelFileDescriptor.open(documentFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
            Log.d("jdf","convertPdfto image1 suc33");

            // Open the page to be rendered.
            PdfRenderer.Page page = pdfRenderer.openPage(0);
            Log.d("jdf","convertPdfto image1 suc11");
            // Render the page to the bitmap.
            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            Log.d("jdf","convertPdfto image1 suc@@");
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            Log.d("jdf","convertPdfto image1 suc##");
            pdfBitmap = bitmap;
            Log.d("jdf","convertPdfto image1 suc111$");
            callback.onSuccess();
            Log.d("jdf","convertPdfto image1 suc");
            // Use the rendered bitmap.
            // Close the page when you are done with it.
            page.close();
            // Close the `PdfRenderer` when you are done with it.
            pdfRenderer.close();

        } catch (FileNotFoundException e) {

            Log.d("jdf","convertPdfto image1 fail "+e.getMessage());
            callback.onFailure(e.getMessage());
            e.printStackTrace();

        } catch (IOException e) {

            Log.d("jdf","convertPdfto image1 fail2 "+e.getMessage());
            callback.onFailure(e.getMessage());
            e.printStackTrace();

        }



    }

    public static InputStream getInStreams() {
        return intentStream;
    }



    private void _display() {
        if(renderer!=null){
            PdfRenderer.Page page = renderer.openPage(0);
            Bitmap mBitmap = Bitmap.createBitmap(page.getWidth(),page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(mBitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            //imgpdf.setImageBitmap(mBitmap);
            photo = mBitmap;
            page.close();

        }
    }

    private void selectPDF()
    {
        // Initialize intent
        Intent intent
                = new Intent(Intent.ACTION_GET_CONTENT);
        // set type
        intent.setType("application/pdf");
        // Launch intent
        resultLauncher.launch(intent);
    }

    @SuppressLint("NewApi")
    public static String getPathAPI19(Context context, Uri uri, Callback callback) {
        String filePath = "";
        Log.d(TAG,"----------uri getPathAPI19  :  "+uri);
        String fileId = DocumentsContract.getDocumentId(uri);
        // Split at colon, use second item in the array
        String id = fileId.split(":")[1];
        String[] column = {MediaStore.Images.Media.DATA};
        String selector = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, selector, new String[]{id}, null);
        int columnIndex = cursor.getColumnIndex(column[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        callback.onSuccess();
        return filePath;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    String imageName="";
    String imageTime = "";
    ProgressDialog progressDialog;
    ProgressDialog dialog;
    Uri imageuri = null;
    Uri pdfUrl = null;
    Uri imageUrl = null;

    private void uploadPdfFile(Intent data,long timeStamp,Callback callback){
        // Here we are initialising the progress dialog box
//        dialog = new ProgressDialog(this);
//        dialog.setMessage("Uploading");
//
//        // this will show message uploading
//        // while pdf is uploading
//        dialog.show();
        imageuri = data.getData();
        final String timestamp = ""+timeStamp;
                //"" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final String messagePushID = timestamp;
        //Toast.makeText(MainActivity.this, imageuri.toString(), Toast.LENGTH_SHORT).show();

        // Here we are uploading the pdf in firebase storage with the name of current time
        final StorageReference filepath = storageReference.child("pdfs/pdf"+messagePushID
                //+ "." + "pdf"
        );
        //Toast.makeText(MainActivity.this, filepath.getName(), Toast.LENGTH_SHORT).show();
        filepath.putFile(imageuri).continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filepath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    // After uploading is done it progress
                    // dialog box will be dismissed
                    dialog.dismiss();
                    Uri uri = task.getResult();
                    String myurl;
                    myurl = uri.toString();
                    Toast.makeText(MainActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    pdfUrl = task.getResult();

                    //pdfUrl = Uri.parse(task.getResult().getPath());
                    //webviewid.loadUrl(String.valueOf(task.getResult()));

                    callback.onSuccess();
                } else {
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "UploadedFailed", Toast.LENGTH_SHORT).show();
                    callback.onFailure(task.getException().toString());
                }
            }
        });
    }

    private void uploadFile(Bitmap bitmap,long timeStamp,Callback callback) {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading");

        // this will show message uploading
        // while pdf is uploading
        dialog.show();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        //StorageReference storageRef = storage.getReferenceFromUrl("Your url for storage");
//        StorageReference mountainImagesRef = storageReference.child("images/" + UUID.randomUUID().toString());
        long time= timeStamp;
                //System.currentTimeMillis();
        imageName = "img"+time;
        imageTime = String.valueOf(time);
        StorageReference mountainImagesRef = storageReference.child("images/img" + time);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = mountainImagesRef.putBytes(data);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads
                // Error, Image not uploaded
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                callback.onFailure("e0 : error : "+e.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                //sendMsg("" + downloadUrl, 2);
                Log.d("downloadUrl-->", "" + downloadUrl);

                mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUrl = uri;
                        progressDialog.dismiss();
                    }
                });

                //imageUrl = downloadUrl;

                Log.d("picwidth", "image url : " + imageUrl);
                // Image uploaded successfully
                // Dismiss dialog
//                progressDialog.dismiss();
                //Toast.makeText(MainActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                callback.onSuccess();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                    progressDialog.setMessage("Uploaded " + (int)progress + "%");
                }
            }
        });

    }
    // UploadImage method
    private void uploadImage()
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(MainActivity.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(MainActivity.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int)progress + "%");
                                }
                            });
        }
        else {
            Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
        }
    }

//    public void SetWebView(WebView webview,String externalUrl){
//        webview.Tag = "webview";
//        //webview.JavaScriptEnabled = true;
//        webview.getSettings().setJavaScriptEnabled(true);
//        webview.getSettings().supportZoom();
//        //webview.getSettings().supportZoom ();
//        //webview.Settings.SetAppCacheEnabled(true);
//        webview.getSettings().setAppCacheEnabled(true);
//        webview.Settings.DomStorageEnabled = true;
//        webview.getSettings().setDomStorageEnabled(true);
//        webview.zoomOut();
//        webview.zoomIn();
//        webview.getSettings().setBuiltInZoomControls(true);
//        webview.getSettings().setLoadWithOverviewMode(true);
//        webview.getSettings().setUseWideViewPort(true);
//        webview.getSettings().setPluginState(WebSettings.PluginState.ON);
//        webview.getSettings().getPluginState();
//        webview.setWebViewClient(new WebViewClient());

//
//        webview.ZoomOut ();
//        webview.ZoomIn ();
//        webview.Settings.BuiltInZoomControls = true;
//        webview.Settings.LoadWithOverviewMode = true;
//        webview.Settings.UseWideViewPort = true;
//        //webview.Settings.SetSupportZoom (true);
//        webview.Settings.SetPluginState (WebSettings.PluginState.On);
//        webview.Settings.GetPluginState ();
//        if (externalUrl.StartsWith("http://") || externalUrl.StartsWith("https://"))
//            webview.LoadUrl (externalUrl);
//        webview.SetWebViewClient (new MonkeyWebViewClient (imgViewBack, imgViewForward, imgRefresh));
//        webview.SetWebChromeClient (new WebChromeClient());
//    }

public interface AsyncResponse{
        void processFinish(InputStream output);
}
    // create an async task class for loading pdf file from URL.
    class RetrivePDFfromUrl extends AsyncTask<String, Void, InputStream> {


        public AsyncResponse delegate = null;

        public RetrivePDFfromUrl(AsyncResponse asyncResponse){
            delegate = asyncResponse;
        }

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
            //pdfView.fromStream(inputStream).load();
            delegate.processFinish(inputStream);
        }
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
                ,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
                ,Manifest.permission.CAMERA
        },REQUEST_PERMISSION_CODE);
    }

    private boolean checkPermissionFromDevice() {

        int write_external_storage_result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int manage_external_storage_result = ContextCompat.checkSelfPermission(this,Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        int read_external_storage_result = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED
                && read_external_storage_result == PackageManager.PERMISSION_GRANTED
                //&& manage_external_storage_result == PackageManager.PERMISSION_GRANTED
                ;
        //return write_external_storage_result == PackageManager.PERMISSION_GRANTED;
    }


}