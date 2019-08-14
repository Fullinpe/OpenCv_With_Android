package com.example.myapplication;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this)
    {

        @Override
        public void onManagerConnected(int status)
        {
            switch(status)
            {
                case BaseLoaderCallback.SUCCESS:{
                    surfaceView.enableView();
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };
    private Handler mHandler;


    static{
        if(OpenCVLoader.initDebug())
        {
            Log.d(BATTERY_SERVICE, "Opencv successfully loaded");
        }
        else{
            Log.d(BATTERY_SERVICE, "Opencv not loaded");
        }
    }
    static {

        System.loadLibrary("native-lib");
    }



    Bitmap b;
    ImageView image;
    TextView tv;
    int x=0,y=0;
    boolean touch=false;

    private JavaCameraView surfaceView;
    private Mat mRgba;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image=findViewById(R.id.imageView);
        tv=findViewById(R.id.textView);
        surfaceView=findViewById(R.id.surfaceView);
        surfaceView.setVisibility(CameraBridgeViewBase.VISIBLE);
        surfaceView.setCvCameraViewListener(this);

        surfaceView.setCameraIndex(0); //摄像头索引        -1/0：后置双摄     1：前置
        //surfaceView.enableFpsMeter(); //显示FPS

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                tv.setText("("+msg.arg2+","+msg.arg1+")");

            }
        };


        requestPermissions(new String[]{Manifest.permission.CAMERA},0);
//        SurfaceHolder holder = surfaceView.getHolder();
//        holder.setKeepScreenOn(true);  //保持屏幕常亮
//        holder.addCallback(this);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        this.y=(int)(480-event.getX()*0.4445);
        this.x=(int)((event.getY()-60)*0.4448);
        tv.setText(x+"==="+y);
        if(event.getAction()==MotionEvent.ACTION_DOWN||event.getAction()==MotionEvent.ACTION_MOVE)
            touch=true;
        else
            touch=false;
        return true;
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void getRect(int[] intA,int x,int y);
    public native void nativeRgba(long addr,int[] x);


    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        long addr = mRgba.getNativeObjAddr();
        //对一帧图像进行处理
        int[] xy=new int[2];
        if(!touch)
            nativeRgba(addr,xy);//480*864
        Message msg=Message.obtain();
        msg.arg1=432-xy[0];
        msg.arg2=240-xy[1];
        mHandler.sendMessage(msg);

        return mRgba;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(surfaceView != null)
            surfaceView.disableView();

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(surfaceView != null)
            surfaceView.disableView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(OpenCVLoader.initDebug())
        {
            Log.i(BATTERY_SERVICE, "Opencv successfully loaded");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            Log.d(BATTERY_SERVICE, "Opencv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallBack);
        }

    }

}
