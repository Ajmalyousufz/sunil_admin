package com.sunilproject.suniladmin;

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
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.airbnb.lottie.utils.Utils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jsibbold.zoomage.ZoomageView;
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter;
import com.sunilproject.suniladmin.utils.Callback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choose_from_file = findViewById(R.id.choose_from_file);
        camera_iv = findViewById(R.id.camera_iv);
        upload_btn= findViewById(R.id.upload_btn);
        scan_image = findViewById(R.id.scan_image);
        remove_shadow = findViewById(R.id.remove_shadow);

        view_image_ll = findViewById(R.id.view_image_ll);
        view_image = findViewById(R.id.view_image);
        view_image.setVisibility(View.GONE);
        myZoomageView = findViewById(R.id.myZoomageView);
        view_image_ll.setVisibility(View.GONE);
        progressbar = findViewById(R.id.progressbar);
        progressbar.setVisibility(View.GONE);

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

        myZoomageView.setOnClickListener(v -> {
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
                    documentFilter.getShadowRemoval(photo, new DocumentFilter.CallBack<Bitmap>() {
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
                                            try {Log.d(TAG,"---------------file path : "+filePath+"  filepath.getpath : "+filePath.getPath()+" filepaths  : "+filePaths);

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
               // uploadImage();
                if(photo!=null){

                    uploadFile(photo);
                }else {
                    Toast.makeText(MainActivity.this, "Select image first", Toast.LENGTH_SHORT).show();
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

    @SuppressLint("NewApi")
    public static String getPathAPI19(Context context, Uri uri, Callback callback) {
        String filePath = "";
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

    private void uploadFile(Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        //StorageReference storageRef = storage.getReferenceFromUrl("Your url for storage");
        StorageReference mountainImagesRef = storageReference.child("images/" + UUID.randomUUID().toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = mountainImagesRef.putBytes(data);

        ProgressDialog progressDialog
                = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads
                // Error, Image not uploaded
                progressDialog.dismiss();
                Toast
                        .makeText(MainActivity.this,
                                "Failed " + e.getMessage(),
                                Toast.LENGTH_SHORT)
                        .show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                //sendMsg("" + downloadUrl, 2);
                Log.d("downloadUrl-->", "" + downloadUrl);
                // Image uploaded successfully
                // Dismiss dialog
                progressDialog.dismiss();
                Toast
                        .makeText(MainActivity.this,
                                "Image Uploaded!!",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                {
                    double progress
                            = (100.0
                            * taskSnapshot.getBytesTransferred()
                            / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage(
                            "Uploaded "
                                    + (int)progress + "%");
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


    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
                ,Manifest.permission.CAMERA
        },REQUEST_PERMISSION_CODE);
    }

    private boolean checkPermissionFromDevice() {

        int write_external_storage_result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED;
        //return write_external_storage_result == PackageManager.PERMISSION_GRANTED;
    }
}