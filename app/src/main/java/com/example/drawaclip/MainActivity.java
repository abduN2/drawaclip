package com.example.drawaclip;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
//import android.support.v4.content.ContextCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;



import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.kyanogen.signatureview.SignatureView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import yuku.ambilwarna.AmbilWarnaDialog;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends AppCompatActivity {
    int currentFrame = 1; //note: currentFrame variable counts from 1, decrement to use as index
    int defaultColor;
    private SignatureView frameView;
    private ImageButton imgEraser, imgColor, imgSave, imgAdd, imgNext, imgPrevious, imgPlay, imgSaveVid;
    private SeekBar seekBar;
    private TextView txtPenSize;
    private ArrayList<Bitmap> frames = new ArrayList();
    private int layoutLeft, layoutTop, layoutRight, layoutBottom;

    String projectDir = "ASDF_CHANGE_ME";


    private static String fileName;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/myPaintings" + projectDir);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameView = findViewById(R.id.signature_view);
        frames.add(frameView.getSignatureBitmap());
        try {
            if (frames.get(0) == null) {
                System.out.println("wtf bro 1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        seekBar = findViewById(R.id.penSize);
        txtPenSize = findViewById(R.id.txtPenSize);
        imgColor = findViewById(R.id.btnColor);
        imgEraser = findViewById(R.id.btnEraser);
        imgSave = findViewById(R.id.btnSave);
        imgAdd = findViewById(R.id.btnAdd);
        imgNext = findViewById(R.id.btnNext);
        imgPrevious = findViewById(R.id.btnPrevious);
        imgPlay = findViewById(R.id.btnVid);
        imgSaveVid = findViewById(R.id.btnVidSave);


        askPermission();

        //SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        //String date = format.format(new Date());
        fileName = path + "/" + "frame_" + currentFrame + ".png";

        if(!path.exists()){
            path.mkdirs();
        }

        defaultColor = ContextCompat.getColor(MainActivity.this, R.color.black);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtPenSize.setText(progress + "dp");
                frameView.setPenSize(progress);
                seekBar.setMax(100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        imgColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();
            }
        });

        imgSaveVid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Abdul works on this");
            }
        });

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("John works on this");
            }
        });

        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    if (frames.get(0) == null) {
                        frames.set(0, frameView.getSignatureBitmap());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                frameView.clearCanvas();
                frames.add(frameView.getSignatureBitmap());
                currentFrame++;
                previousFrame();
                nextFrame();
                System.out.println("cooooool" + currentFrame);
                System.out.println(frames.get(currentFrame-1));
                try {
                    if (frames.get(0) == null) {
                        System.out.println("wtf bro 2");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        imgNext.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               nextFrame();
           }
        });

        imgPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousFrame();
            }
        });

        imgEraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameView.clearCanvas();
            }
        });

        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!frameView.isBitmapEmpty()){
                    try{
                        saveImage();
                    } catch (IOException e){
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Couldn't save painting!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    private void nextFrame() {

        if (frames.size() > currentFrame) {
            System.out.println(currentFrame+" 1");
            frameView.setBitmap(frames.get(currentFrame));
            System.out.println(currentFrame+" 2");
            currentFrame++;
            System.out.println(currentFrame+" 3");
            if (frames.get(currentFrame-1) != null) {
                System.out.println("wow" + currentFrame);
                System.out.println(frames.get(currentFrame-1));
            }

        }

    }

    private void previousFrame() {
        if (currentFrame != 1) {
            System.out.println(currentFrame+" 4");
            frameView.setBitmap(frames.get(currentFrame-2));
            System.out.println(currentFrame+" 5");
            currentFrame--;
            System.out.println(currentFrame+" 6");
            if (frames.get(currentFrame-1) != null) {
                System.out.println("cool" + currentFrame);
                System.out.println(frames.get(currentFrame-1));
            }

        }

    }

    private void saveImage() throws IOException {
        for (Bitmap bitmap:frames) {
            fileName = path + "/" + "frame_" + (frames.lastIndexOf(bitmap)+1) + ".png";
            File file = new File(fileName);
            //System.out.println(fileName);

            //Bitmap bitmap = frameView.getSignatureBitmap();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapData = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            Toast.makeText(this, "Painting Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private void openColorPicker(){
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {

                defaultColor = color;
                frameView.setPenColor(color);
            }
        });
        ambilWarnaDialog.show();
    }

private void askPermission() {
    if (SDK_INT >= Build.VERSION_CODES.R) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
            startActivityForResult(intent, 2296);
        } catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, 2296);
        }
    } else {
        //below android 11
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 2296);
    }
}


}
