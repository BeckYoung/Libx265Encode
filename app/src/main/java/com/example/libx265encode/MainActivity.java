package com.example.libx265encode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button btnStart;
    private Camera camera;
    // 图片旋转角度
    private int degree;
    private int width = 640;
    private int height = 480;
    private int fps = 20;
    private int bitrate = 90000;
    // 时间戳
    private int timespan = bitrate / fps;
    // 总时间
    private long time;
    private H265Encode h265Encode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        h265Encode = H265Encode.getInstance();
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(h265Encode.stringFromJNI());
        surfaceView = findViewById(R.id.surfaceview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "requestCode = " + requestCode);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        getBackCamera();
        startCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    private void getBackCamera(){
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    private void setCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        this.degree = result;
        Log.d(TAG, "result = " + result);
        camera.setDisplayOrientation(result);
    }

    private void startCamera() {
        try {
            Camera.Parameters parameters = camera.getParameters();
            camera.setPreviewCallback(this);
            setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK);
//                mCamera.setDisplayOrientation(90);
            parameters.setPreviewFormat(ImageFormat.NV21);
            List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
            for (Camera.Size size : sizeList) {
                Log.d(TAG, "support width = " + size.width + ",height = " + size.height);
            }
            parameters.setPreviewSize(width, height);
            camera.setParameters(parameters);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
//            Log.d(TAG, "onPreviewFrame is mainThread");
//        }
        // 摄像头预览数据回调,在主线程
        if (btnStart.isSelected()) {
            // 正在编码中
            time += timespan;
            byte[] yuv420 = new byte[width * height * 3 / 2];
            YUV420SP2YUV420(data, yuv420, width, height);
        }
    }

    // nv21 to yuv420
    private void YUV420SP2YUV420(byte[] yuv420sp, byte[] yuv420, int width, int height) {
        if (yuv420sp == null || yuv420 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        //copy y
        for (i = 0; i < framesize; i++) {
            yuv420[i] = yuv420sp[i];
        }
        i = 0;
        for (j = 0; j < framesize / 2; j += 2) {
            yuv420[i + framesize * 5 / 4] = yuv420sp[j + framesize];
            i++;
        }
        i = 0;
        for (j = 1; j < framesize / 2; j += 2) {
            yuv420[i + framesize] = yuv420sp[j + framesize];
            i++;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                btnStart.setSelected(!btnStart.isSelected());
                if (btnStart.isSelected()) {
                    // 停止
                    btnStart.setText("stop encode");
                } else {
                    // 开始
                    btnStart.setText("start encode");
                }
                break;
            default:
                break;
        }
    }
}
