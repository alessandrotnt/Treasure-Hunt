package com.example.treasurehunt;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ProgressBar;

/**
* <p>MMNET Team 04</p>
* <p>Project Title: Treasure Hunt</p>
* <p>Class Description: Activity called by the user that hides the treasure. Make use of the OpenCV camera
* preview. It uses Android Camera in order to take focused pictures.
* @authors Alessandro Tontini & Martina Valente
*/

public class CameraActivity extends Activity implements CvCameraViewListener2, OnTouchListener, SensorEventListener {
    
	private static final String TAG = "OCVSample::Activity";
	
	//camera class
    private CameraLib mOpenCvCameraView;
    
    //variables
    public static boolean finish=false;  
    public static boolean zero=false;
    public static boolean novanta = false;
    public static boolean centottanta = false;
    Scalar green = new Scalar(0, 255, 0);	
	private Mat rgba = new Mat();
	private long mLastUpdate;
	private static final int UPDATE_THRESHOLD = 500;
	public static Bitmap biemmepi;

	//sensors
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	public static Context myContext;
    public static Intent previewIntent;
    private static ProgressBar progress;
    
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(CameraActivity.this);
                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
     
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_camera);
        mOpenCvCameraView = (CameraLib) findViewById(R.id.camera_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        progress = (ProgressBar) findViewById(R.id.progressbar);
        progress.setVisibility(View.GONE);
        
        myContext=this;       

 		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		if (null == (mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))){
 			finish();
 		}
 		
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mSensorManager.unregisterListener(this);
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        progress.setVisibility(View.GONE);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_UI);
		mLastUpdate = System.currentTimeMillis();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    	if(finish){
        	finish=false;
        	finish();
        }   	
    }

    public void onCameraViewStopped() {
    }
    
    /**
     *This method is used in order to compute and show in real time the features of the scene.
     *It is useful to the user in order to understand whether the treasure has a sufficient number
     *of features or not. 
     *
     *@param inputFrame it is the current displayed camera preview
     *@return rgba Mat containing the current preview + the computed features 
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	
    	rgba = inputFrame.rgba();
	    final org.opencv.core.Size sizeRgba = rgba.size();

        Mat rgbaInnerWindow = new Mat();

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        int left = cols / 8;
        int top = rows / 8;

        int width = cols * 3 / 4;
        int height = rows * 3 / 4;
        
        rgbaInnerWindow = rgba.submat(top, top+height, left, left+width);
	    org.opencv.core.Size dime = rgbaInnerWindow.size();
       	Point mP1=new Point();  
       	Point mP2=new Point();
	    mP1.x=0; mP1.y=0;
	    mP2.x=(int) (dime.width-1);
	    mP2.y=(int) (dime.height-1);
	     
	    Core.rectangle(rgbaInnerWindow, mP2, mP1, green);
        
        FeatureDetector Orbdetector = FeatureDetector.create(FeatureDetector.ORB);
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        
        Mat rgbaconv = new Mat();
        Imgproc.cvtColor(rgbaInnerWindow, rgbaconv, Imgproc.COLOR_RGBA2RGB);
      
        Orbdetector.detect(rgbaconv,keypoints);
        
        if(!Orbdetector.empty()){
        	Features2d.drawKeypoints(rgbaconv,keypoints,rgbaconv);
        	Imgproc.cvtColor(rgbaconv, rgbaInnerWindow, Imgproc.COLOR_RGB2RGBA); 	
        }
        
        rgbaInnerWindow.release();
        rgbaconv.release();
        keypoints.release();
		return rgba;
    }
     
    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
    	progress.setVisibility(View.VISIBLE);
    	savePhoto();
        return false;
    }
    /**
     * Method that calls the takePicture method of the standard Android camera.
     */
    public void savePhoto(){
    	
		File mediaStorageDir = new File("/sdcard/", "FeatureCam");
		if (!mediaStorageDir.exists()) { mediaStorageDir.mkdirs(); }
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "pic.jpg");
		String filename = mediaFile.toString();
		mOpenCvCameraView.search=false;	
        mOpenCvCameraView.takePicture(filename);

    }
    
    /**
     * This method is called when the user has taken a photo in order to have a look at it
     * @param myContext 
     */
    public static void showPreview(Context myContext){
    	progress.setVisibility(View.INVISIBLE);
    	previewIntent = new Intent(myContext, PreviewActivity.class);
    	myContext.startActivity(previewIntent);	 
    }
    
    /**
     * This method is used in order to understand the current device rotation
     * @param event an event on the accelerometer sensor
     */
 	@Override
 	public void onSensorChanged(SensorEvent event) {

 		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 		
 			long actualTime = System.currentTimeMillis();
 			if (actualTime - mLastUpdate > UPDATE_THRESHOLD) {

 				mLastUpdate = actualTime;
 				float x = event.values[0], y = event.values[1], z = event.values[2];
 				if (Math.abs((int) x*100)>Math.abs((int) y*100)){
 					if (x>0){
 						zero = true;
 						novanta = false;
 						centottanta=false;
 					}else{
 						centottanta = true;
 						novanta=false;
 						zero=false;
 					}
 				}else{
 					novanta = true;
 					centottanta=false;
 					zero=false;
 				}
 			}
 		}
 	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub	
	}
   
}
