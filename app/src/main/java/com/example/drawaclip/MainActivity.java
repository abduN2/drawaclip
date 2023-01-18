package com.example.drawaclip;
//imports yay

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.kyanogen.signatureview.SignatureView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {
    int currentFrame = 1; //note: currentFrame variable counts from 1, decrement to use as index
    int defaultColor; //color of pen
    private SignatureView frameView; //drawing panel
    private ImageButton imgEraser, imgColor, imgSave, imgAdd, imgNext, imgPrevious, imgPlay, imgSaveVid, imgDelete; //all the different buttons
    private SeekBar seekBar, fpsBar; //progress bars, seekBar is for size of pen, fpsBar is for user's choice of fps
    private TextView txtPenSize, txtFPS; //actual values of seekbar and fpsBar, txtPenSize is for seekBar and txtFPS is for fpsBar
    private int framesPerSecond = 12; //integer value of txtFPS used for playing the animation and saving it
    private TextView txtFrame; //text indicator of the frame user is editing
    private ArrayList<Bitmap> frames = new ArrayList(0); //array list of frames
    private FileInputStream fileGetter;


    String projectDir = "ASDF_CHANGE_ME"; //project file, change name = make new project!


    private static String fileName; //file name variable
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DrawAClip/" + projectDir); //get directory for where frames are saved

    @Override
    protected void onCreate(Bundle savedInstanceState) { //on create, where all the magic happens
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        frameView = findViewById(R.id.signature_view); //draw panel
        frames.add(frameView.getSignatureBitmap());
        try {
            if (frames.get(0) == null) {
                System.out.println("wtf bro 1");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //define all the variables used
        seekBar = findViewById(R.id.penSize);
        fpsBar = findViewById(R.id.frameSize);
        txtPenSize = findViewById(R.id.txtPenSize);
        txtFPS = findViewById(R.id.txtFPS);
        txtFrame = findViewById(R.id.txtframe);
        imgColor = findViewById(R.id.btnColor);
        imgEraser = findViewById(R.id.btnEraser);
        imgSave = findViewById(R.id.btnSave);
        imgAdd = findViewById(R.id.btnAdd);
        imgNext = findViewById(R.id.btnNext);
        imgPrevious = findViewById(R.id.btnPrevious);
        imgPlay = findViewById(R.id.btnVid);
        imgSaveVid = findViewById(R.id.btnVidSave);
        imgDelete = findViewById(R.id.btnDelete);




        askPermission(); //ask for all permissions used to fetch images and saving stuff

        fileName = path + "/" + "frame_" + String.format("%04d", currentFrame) + ".png"; //set file name for each frame

        if(!path.exists()){ //check that path exists yk
            System.out.println("piojwfapojwafojpwafjopwfajopwfa did it");
            path.mkdirs();
        }

        try { // fills an array with all pngs in project folder
            File[] savedFrames = Objects.requireNonNull(path.listFiles(new FilenameFilter() {
                @Override //filters file name to select only pngs, ignores the video output file
                public boolean accept(File file, String s) {
                    return s.toLowerCase().endsWith(".png");
                }
            }));

            int noOfFrames = savedFrames.length;
            for (int i = 0; i < noOfFrames; i++) { //creates a fileinputstream for each png to pass to bitmapfactory, adds the generated bitmap to frames array
                fileGetter = new FileInputStream(savedFrames[i]);
                if (frames.get(0) == null) {
                    frames.set(0, BitmapFactory.decodeStream(fileGetter).copy(Bitmap.Config.ARGB_8888, true));
                } else {
                    frames.add(BitmapFactory.decodeStream(fileGetter).copy(Bitmap.Config.ARGB_8888, true));
                }
                fileGetter.close();
            }
            frameView.setBitmap(frames.get(0)); //sets view to first frame

        } catch (Exception e) {
            e.printStackTrace();
        }

        defaultColor = ContextCompat.getColor(MainActivity.this, R.color.black); //default pen color

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { //seek bar, method to change the size of the drawing pen
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtPenSize.setText(progress + "dp");
                frameView.setPenSize(progress);
                seekBar.setMax(100);
                seekBar.setMin(1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        fpsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { //fpsBar, method to change the fps of the animation
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtFPS.setText(progress + "fps"); //update visual display
                framesPerSecond = progress; //actually update the int variable keeping track
                fpsBar.setMax(60); //max fps allowed is 60
                fpsBar.setMin(12); //min fps allowed is 12
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        imgColor.setOnClickListener(new View.OnClickListener() { //method to choose drawing color of the pen
            @Override
            public void onClick(View v) {
                openColorPicker();
            }
        });

        imgSaveVid.setOnClickListener(new View.OnClickListener() { //method to save animation as a video
            @Override
            public void onClick(View v) {
                try {
                    saveImage(); //make sure stuff is saved as images first
                    FFmpegSession session = FFmpegKit.execute("-y -framerate "+framesPerSecond+ " -i " + path + "/frame_%04d.png -vcodec libx264 -pix_fmt yuv420p -crf 20 -vf pad=ceil(iw/2)*2:ceil(ih/2)*2:color=white " + path + "/output.mp4"); //use ffmpeg to call line of command
                    if(ReturnCode.isSuccess(session.getReturnCode())){ //success!
                        Toast.makeText(MainActivity.this, "Successfully created animation!", Toast.LENGTH_SHORT).show();
                    } else if(ReturnCode.isCancel(session.getReturnCode())){ //cancelled..?
                        Toast.makeText(MainActivity.this, "Export Cancelled", Toast.LENGTH_SHORT).show();
                    } else{ //failed
                        Toast.makeText(MainActivity.this, "Couldn't create animation!", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) { //couldn't save frames as images
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Couldn't create animation!", Toast.LENGTH_SHORT).show();
                }



            }
        });

        imgPlay.setOnClickListener(new View.OnClickListener() { //play button to go through animation
            @Override
            public void onClick(View v) {
                int frameSize = frames.size(); //get frame array list size, used for a for loop
                int oldFrame = currentFrame;
                final int delay = 1000 / framesPerSecond; //get delay for chosen user fps
                try {
                    frameView.setBitmap(frames.get(0)); //go back to frame 1 so user can watch animation from start to end

                    currentFrame = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for(int i = 0; i < frameSize; i++){ //for loop to change the UI display to next frame every however much delay is depending on chosen fps
                    final int index = i;
                    frameView.postDelayed(new Runnable() {
                        public void run() {
                            nextFrame(); //move to next frame
                            txtFrame.setText(String.valueOf(currentFrame)); //change through frame numbers!

                        }
                    }, delay*i); //delay stuff to control speed of the for loop to make sure its at the correct FPS
                }


            }
        });

        imgAdd.setOnClickListener(new View.OnClickListener() { //method to make a new frame!
            @Override
            public void onClick(View v) {


                try {
                    if (frames.get(0) == null) { //if index 0 of frame array is null, sets it to the first frame
                        frames.set(0, frameView.getSignatureBitmap());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                frameView.clearCanvas(); //clearCanvas() generates a new bitmap
                if (currentFrame == frames.size()) { // if new frame is new last frame then shove it at the end
                    frames.add(frameView.getSignatureBitmap());
                } else { // else insert new frame in correct location
                    frames.add(currentFrame, frameView.getSignatureBitmap());
                }

                currentFrame++;
                previousFrame(); //program breaks without these two lines please ignore
                nextFrame();
                txtFrame.setText(String.valueOf(currentFrame)); //updates frame counter




            }
        });

        imgNext.setOnClickListener(new View.OnClickListener() { //method to move to next frame
           @Override
           public void onClick(View v) {
               nextFrame(); //call nextFrame method
               txtFrame.setText(String.valueOf(currentFrame)); //change visual display to new frame number
           }
        });

        imgPrevious.setOnClickListener(new View.OnClickListener() { //method to move to previous frame
            @Override
            public void onClick(View v) {
                previousFrame(); //call previousFrame method
                txtFrame.setText(String.valueOf(currentFrame)); //change visual display to new frame number
            }
        });

        imgEraser.setOnClickListener(new View.OnClickListener() { //erase frame button
            @Override
            public void onClick(View v) {
                frameView.clearCanvas(); //clear current frame
                frames.set(currentFrame-1, frameView.getSignatureBitmap());
                frameView.setBitmap(frames.get(currentFrame-1));
            }
        });

        imgDelete.setOnClickListener(new View.OnClickListener() { //delete frame button
            @Override
            public void onClick(View v) {
                if(frames.size() > 1){
                    File findFile = new File(path + "/frame_" + String.format("%04d", currentFrame) + ".png");
                    findFile.delete();
                    frames.remove(currentFrame-1);
                    if (currentFrame-1 != frames.size()) {
                        frameView.setBitmap(frames.get(currentFrame-1));
                        txtFrame.setText(String.valueOf(currentFrame));
                    } else {
                        frameView.setBitmap(frames.get(currentFrame-2));
                        currentFrame--;
                        txtFrame.setText(String.valueOf(currentFrame));
                    }
                }
            }
        });

        imgSave.setOnClickListener(new View.OnClickListener() { //method to save all work: main function is simply to call saveFrame() while handling errors
            @Override
            public void onClick(View v) {
                if(!frameView.isBitmapEmpty()){
                    try{
                        try {
                            if (frames.get(0) == null) {
                                frames.set(0, frameView.getSignatureBitmap());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        saveImage(); //save the stuff
                    } catch (IOException e){
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Couldn't save painting!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }




    private void nextFrame() { //method to move to next frame

        if (frames.size() > currentFrame) {

            frameView.setBitmap(frames.get(currentFrame)); //sets view to next frame in arrayList

            currentFrame++;


        }

    }



    private void previousFrame() { //method to move to previous frame
        if (currentFrame != 1) {

            frameView.setBitmap(frames.get(currentFrame-2)); //sets view to previous frame in arrayList

            currentFrame--;



        }

    }

    private void saveImage() throws IOException { //method to actually save each frame for wanting to use your work for later
        for (Bitmap bitmap:frames) {
            fileName = path + "/" + "frame_" + String.format("%04d", (frames.lastIndexOf(bitmap)+1)) + ".png"; //save file under a name of frame_(frame number), so if saving frame 1, its saved as frame_1
            File file = new File(fileName); //make the file
            //actually save it into files under images into the specific path
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapData = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

        }
        Toast.makeText(this, "Project Saved!", Toast.LENGTH_SHORT).show();
    }

    private void openColorPicker(){ //method for changing the color of the drawing pen
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {

                defaultColor = color;
                frameView.setPenColor(color); //set chosen color to actual pen color
            }
        });
        ambilWarnaDialog.show();
    }

private void askPermission() { //ask permission method to give app proper permissions to save and pull frame information
    if (SDK_INT >= Build.VERSION_CODES.R) {
        try { //make a pop up when launch app, actual part that asks for permission
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
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 2296);
    }
}


}
