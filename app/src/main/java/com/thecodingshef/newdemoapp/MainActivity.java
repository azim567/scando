package com.thecodingshef.newdemoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button googleSearch;
    TextView resulttxt;
    ImageView ImageClic;
    private static final int CAMERA_REQUEST_CODE=200;
    private static final int STORAGE_REQUEST_CODE=400;
    private static final int IMAGE_PICK_GALLERY_CODE=1000;
    private static final int IMAGE_PICK_CAMERA_CODE=1001;


    String cameraPermission[];
    String storagePermission[];

    Uri image_uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Scando");


        resulttxt=findViewById(R.id.textResult);
        ImageClic=findViewById(R.id.ivImge);
        googleSearch=findViewById(R.id.googlebtn);

        //camera Permission
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //Storage Permission
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


      googleSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/#q=" + resulttxt.getText())));
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id=item.getItemId();
        if(id==R.id.addImage){
              ShowImageImportDialog();
        }
        if(id==R.id.exportPdf){

            exportPdf();
        }

        if(id==R.id.exportWord){

        }
        if(id==R.id.txtShare){

            ShareText();

        }
        /*if(id==R.id.setting){
            Toast.makeText(this, "Setting", Toast.LENGTH_SHORT).show();
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void ShareText() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, resulttxt.getText().toString());
        intent.setType("text/plain");
        startActivity(intent);
    }

    private void exportPdf() {
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){

            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){

                String[] permission={Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission,1000);
            }
            else{
                savePDf();
            }
        }
        else {
            savePDf();
        }
    }

    private void savePDf() {
        Document document = new com.itextpdf.text.Document();
        String mfile = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String filepath = Environment.getExternalStorageDirectory()+"/"+mfile+".pdf";
        com.itextpdf.text.Font smallBold = new Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 12, com.itextpdf.text.Font.BOLD);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filepath));
            document.open();
            String txt = resulttxt.getText().toString();
            document.addAuthor("TheCodingShef");
            document.add(new Paragraph(txt, smallBold));
            document.close();
            Toast.makeText(this, "" + mfile + ".pdf" + " is saved to " + filepath, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "This is error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }




    private void ShowImageImportDialog() {

        String[] items={"Camera","Gallery"};
        AlertDialog.Builder dialogue=new AlertDialog.Builder(this);
        dialogue.setTitle("Select Image");
        dialogue.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==0){
                    //camera option clicked
                    if(!checkCameraPermission()){
                        //camera permision not allowed, request it
                        requestCameraPermission();
                    }else{
                        pickCamera();
                    }
                }
                if(which==1){

                    if(!checkStoragePermission()){
                        //camera permision not allowed, request it
                        requestStoragePermission();
                    }else{
                        pickGallery();
                    }
                }
            }

        });
        dialogue.create().show();
    }

    private void pickGallery() {

        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {

        // intent to take image from camera
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"NewPic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Image to text");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result;

    }

    private void requestCameraPermission() {

        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }


    //handle permission result


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){

                        pickCamera();
                    }else{
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0){

                    boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){

                        pickGallery();
                    }else{
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case 1000:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    savePDf();
                }
                else {
                    Toast.makeText(this, "permission denied...", Toast.LENGTH_SHORT).show();
                }

        }
    }


    //handle image result


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){

            if(requestCode==IMAGE_PICK_GALLERY_CODE){

                //got image from gallery now crop it
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);

            }
            if(requestCode==IMAGE_PICK_CAMERA_CODE){

                //get image from camera now crop it
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);

            }


        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                Uri resulturi=result.getUri();

                //set image to imageview
                ImageClic.setImageURI(resulturi);

                //get Drawable bitmap for text recognition
                BitmapDrawable bitmapDrawable=(BitmapDrawable)ImageClic.getDrawable();
                Bitmap bitmap=bitmapDrawable.getBitmap();

                TextRecognizer textRecognizer=new TextRecognizer.Builder(getApplicationContext()).build();

                if(!textRecognizer.isOperational()){
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Frame frame=new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items=textRecognizer.detect(frame);
                    StringBuilder sb=new StringBuilder();
                    //get value from sb until is no more existed

                    for(int i=0;i<items.size();i++){
                        TextBlock myItem=items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");

                    }
                    resulttxt.setText(sb.toString());

                  //  startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/#q=" + sb.toString())));
                }
            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

                Exception error=result.getError();
                Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
