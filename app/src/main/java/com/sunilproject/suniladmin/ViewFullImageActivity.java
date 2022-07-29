package com.sunilproject.suniladmin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;

import com.jsibbold.zoomage.ZoomageView;

public class ViewFullImageActivity extends AppCompatActivity {

    ZoomageView zoomageView ;
    Bitmap bitmap=null ;
    String imageUri = "";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_full_image);

        //bitmap = getIntent().getParcelableExtra("bitmap_image");
        //imageUri = getIntent().getStringExtra("bitmap_image");
        //(Bitmap) intent. getParcelableExtra("BitmapImage");

        setResult(RESULT_OK);
        zoomageView = findViewById(R.id.myZoomageView);


        sharedPreferences = getSharedPreferences("suni_app", MODE_PRIVATE);

        if(!sharedPreferences.getString("bitmapbytes", "").equals("")){
            String bitmap_byte_str = sharedPreferences.getString("bitmapbytes", "");
            byte[] array = Base64.decode(bitmap_byte_str, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(array, 0, array.length);
            zoomageView.setImageBitmap(bmp);
        }

//        if(imageUri!=null){
//            //zoomageView.setImageBitmap(bitmap);
//            zoomageView.setImageURI(Uri.parse(imageUri));
//        }

//        if(getIntent().hasExtra("bitmapbytes")) {
//            //ImageView previewThumbnail = new ImageView(this);
////            Bitmap b = BitmapFactory.decodeByteArray(
////                    getIntent().getByteArrayExtra("byteArray"),0,getIntent().getByteArrayExtra("byteArray").length);
//
//            byte[] bytes = getIntent().getByteArrayExtra("bitmapbytes");
//            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//
//            zoomageView.setImageBitmap(bmp);
//        }

    }
}