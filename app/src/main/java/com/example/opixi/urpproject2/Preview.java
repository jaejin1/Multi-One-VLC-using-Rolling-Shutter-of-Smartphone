package com.example.opixi.urpproject2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.IOError;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;


public class Preview implements SurfaceHolder.Callback, Camera.PreviewCallback {
    static final String TAG = "error";

    public SurfaceHolder mHolder;
    private Camera mCamera;
    private ImageView MyCameraPreview = null;
    private int MY_PERMISSION_REQUEST_CODE = 100;

    private int[] pixels = null;
    private Bitmap bitmap = null;
    private byte[] FrameData = null;
    public int previewWidth = 640;
    public int previewHeight = 480;
    public int centerP = 0;

    public Queue<int[]> queue = new LinkedList<int[]>();
    public boolean isStart = false;

    private CountDownTimer timer;
    Handler mHandler = new Handler(Looper.getMainLooper());

    public int[] createPixel(){
        int[] pixels = null;
        pixels = new int[previewHeight*previewWidth];

        bitmap.getPixels(pixels, 0, previewWidth, 0, 0, previewWidth, previewHeight);

        return pixels;
    }


    public Preview(ImageView myCameraPreview, Context context, MainActivity mainActivity) {
        Log.d(TAG, "preview 생성자 시작");
        mHolder = mainActivity.mHolder;
        mHolder.addCallback(this);
        MyCameraPreview = myCameraPreview;
        pixels = new int[previewWidth*previewHeight];

        bitmap = Bitmap.createBitmap(previewWidth,previewHeight,Bitmap.Config.ARGB_8888);
    }

    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1) {
        FrameData = arg0;
        mHandler.post(DoImageProcessing);
    }/*
    private boolean checkCAMERAPermission(){
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
            return result == PackageManager.PERMISSION_GRANTED;
    }*/

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "camera.open 시작");

        mCamera = Camera.open();
        Log.d(TAG, "camera.open 끝");



        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(previewWidth, previewHeight);
        // parameters.setAutoWhiteBalanceLock(false);
        // parameters.setAutoExposureLock(false);
        parameters.setPictureSize(640, 480);
        // 짧은 exposure time은 band구별 능력 향상
        int minexpore = parameters.getMinExposureCompensation();
        parameters.setExposureCompensation(-4);
        parameters.setAutoWhiteBalanceLock(false);
        parameters.setAntibanding(parameters.ANTIBANDING_OFF);
        parameters.setAutoExposureLock(false);
        parameters.setPreviewFrameRate(24);
        System.out.println("setAutoExposureLock -----------");
        System.out.println(parameters.isAutoExposureLockSupported());
        parameters.setAutoExposureLock(false);
        //parameters.setFocusMode(parameters.FOCUS_MODE_FIXED);
        Log.i("TAG", "Surrpoted Exposere Mode: " + minexpore);
        Log.i("TAG", "Surrpoted Exposere Mode: " + parameters.get("exposure"));
        Log.i("TAG", "Surrpoted Exposere Mode: " + parameters.get("whitebalance"));
        Log.i("anti banding", "Surrpoted Exposere Mode: " + parameters.getAntibanding());
        Log.i("vidio", "Surrpoted Exposere Mode: " + String.valueOf(parameters.getVideoStabilization()));
        Log.i("white", "Surrpoted Exposere Mode: " + parameters.getWhiteBalance());
        Log.i("auto", "Surrpoted Exposere Mode: " + parameters.getFocusMode());
        Log.i("auto", "Surrpoted Exposere Mode: " + parameters.getPreviewFrameRate());
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(null != mCamera){
            try {
                mCamera.setPreviewDisplay(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;

        }
    }

    private Runnable DoImageProcessing = new Runnable() {

        @SuppressLint("NewApi")
        public void run() {
            pixels = createPixel();


            Blur(previewWidth, previewHeight, FrameData, pixels);


            int temp = ImageProcessing(previewWidth, previewHeight, FrameData, pixels);
            System.out.println(" temp = " + temp);   // 중심에서 좀 떨어진 곳에서의 값.
            if(temp !=0)
                centerP = temp;   //imageProcessing 한 값


            //Hough(previewWidth, previewHeight, FrameData, pixels);
            // ImageProcessing(previewWidth, previewHeight, FrameData, pixels);
            //Log.i("cetnerX", String.valueOf(centerP));

            if (isStart) {
                queue.offer(pixels);   // 뒤로 값을 추가함.

                // Log.i("queueSize1:", String.valueOf(queue.size()));
            }


            //-16777216 이 블랙  -1 이 화이트
            bitmap.setPixels(pixels, 0, previewWidth, 0, 0, previewWidth, previewHeight);

            // if(queue.size()==10) isStart = false;
            MyCameraPreview.setImageBitmap(bitmap);
            //  System.out.println(MyCameraPreview.getWidth() + " " + MyCameraPreview.getHeight());
            MyCameraPreview.setRotation(90);
        }
    };


    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }


    public native int ImageProcessing(int width, int heigh, byte[] NV21FrameData, int[] pixels);

    public native boolean Blur(int width, int heigh, byte[] NV21FrameData, int[] pixels);

    public native void Hough(int width, int heigh, byte[] NV21FrameData, int[] pixels);

}
