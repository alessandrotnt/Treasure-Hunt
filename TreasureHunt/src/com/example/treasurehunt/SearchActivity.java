package com.example.treasurehunt;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
* <p>MMNET Team 04</p>
* <p>Project Title: Treasure Hunt</p>
* <p>Class Description: Activity called by the user that seek the treasure. Make use of the OpenCV camera
* preview. It uses Android Camera in order to take focused pictures.
* @authors Alessandro Tontini & Martina Valente
*/

public class SearchActivity extends Activity implements CvCameraViewListener2, OnTouchListener, SensorEventListener {
    
	private static final String TAG = "OCVSample::Activity";
	
	//variables
    public  static boolean finish      = false;
    public  static boolean zero        = false;
    public  static boolean novanta 	   = false;
    public  static boolean centottanta = false;
	private static boolean pari        = true;
	private static boolean dispari     = false;
	private static boolean isFailed    = false;
	private static Mat LoadedMat;
	private static Mat trInnerHist     = new Mat();
	Mat rgba = new Mat();
	private static MatOfKeyPoint TreasureKp;
	private Scalar green = new Scalar(0, 255, 0); 
	private Scalar red = new Scalar (255,0,0);
	private Scalar blue = new Scalar (0,0,255);
	double innerSim;
	private int errore;    
	private long mLastUpdate;
	private static int nrOfErrors = 0; 
	static final int MIN_DISTANCE = 150;
	private float x1,x2,y1,y2;
	private int failCont=0;
	private static final int UPDATE_THRESHOLD = 500;
	public static Bitmap bmp;
	
	//layout
    private CameraLib mOpenCvCameraView;
    private static ProgressBar progress;
    private int mProgressStatus = 0;
    private Handler mHandler = new Handler();
   
    //sensors
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	public static Context myContext;
	
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(SearchActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public SearchActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_search);
   
        progress = (ProgressBar) findViewById(R.id.progressbar);
        progress.setVisibility(View.GONE);
        
        mOpenCvCameraView = (CameraLib) findViewById(R.id.camera_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        myContext=this;       
        // Get reference to SensorManager
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// Get reference to Accelerometer
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
        mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_UI);
		mLastUpdate = System.currentTimeMillis();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    /**
     * This method is used to start camera preview and to compute the keypoints of the received
     * treasure image. It is called just once in a match.
     */
    public void onCameraViewStarted(int width, int height) {
	    	if(finish){
	        	finish=false;
	        	finish();
	        }
    		LoadedMat = new Mat(); 
    		LoadedMat=decodeBmp(BluetoothActivity.TreasureBmp);
    		//--Compute treasure features:
    		FeatureDetector Orbdetector = FeatureDetector.create(FeatureDetector.ORB);
    		TreasureKp = new MatOfKeyPoint();
    		Orbdetector.detect(LoadedMat,TreasureKp);
    		trInnerHist=calcInnerHist(LoadedMat);
    }
 
    public void onCameraViewStopped() {
    }
    
    /**
     * Method that computes the histogram of the inner part of the input Mat object
     * @param Img input Mat that could be both treasure Mat or online preview Mat
     * @return hist Mat object containing the histogram
     */
    public Mat calcInnerHist(Mat Img){
    	
   	  	 int mHistSizeNum=15;
   	  	 
    	 Mat InnerWindow = new Mat();
    	 Mat mIntermediateMat= new Mat();
    	 Mat hist = new Mat();
	     int rows = (int) Img.size().height;
	     int cols = (int) Img.size().width;
	     int left = cols / 8;
	     int top = rows / 8;

	     int width = cols * 3 / 4;
	     int height = rows * 3 / 4;
	     
	     MatOfInt[] mChannels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
	     MatOfInt mHistSize = new MatOfInt(mHistSizeNum);
	     MatOfFloat mRanges = new MatOfFloat(0f, 256f);
	     
	     InnerWindow = Img.submat(top, top+height, left, left+width);
	     org.opencv.core.Size dime = InnerWindow.size();
    	 Mat mMat0 = new Mat();
    	 
    	 Imgproc.cvtColor(InnerWindow, mIntermediateMat, Imgproc.COLOR_RGB2HSV_FULL);
         Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[0], mMat0, hist, mHistSize, mRanges);
    	
         Core.normalize(hist, hist, dime.height/2, 0, Core.NORM_INF);
         InnerWindow.release();
         mIntermediateMat.release();
         mMat0.release();
         return hist;
    }
    /**
     * Method that is used in order to draw the histogram similarity bar 
     * (used to understand whether the current scene colors are similar to the treasure colors or not).
     * and the blinking animation that occurs when the user selects a wrong object.
     * @param inputFrame the current camera preview frame on which the drawings will be made and displayed
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	
		 rgba = inputFrame.rgba();
		 
	     org.opencv.core.Size sizeRgba = rgba.size();
	     
	     Mat rgbaInnerWindow = new Mat();
	     
	     int rows = (int) sizeRgba.height;
	     int cols = (int) sizeRgba.width;
	     int left = cols / 8;
	     int top = rows / 8;
	     int width = cols * 3 / 4;
	     int height = rows * 3 / 4;
	     rgbaInnerWindow = rgba.submat(top, top+height, left, left+width);

	     final Mat rgbaconv = new Mat();
	     final Mat rgbaconv2 = new Mat();
	     
	     final org.opencv.core.Size dime = rgbaInnerWindow.size();
       	 final Point mP1= new Point();
       	 final Point mP2= new Point();
       	 final Point p3 = new Point();
       	 final Point p4 = new Point();
	     mP1.x=0; 
	     mP1.y=0;
	     mP2.x=(int) (dime.width-1);
	     mP2.y=(int) (dime.height-1);
	     
	     Core.rectangle(rgbaInnerWindow, mP2, mP1, green);
	     
	     if(isFailed && failCont<=25 && dispari){
	    	 Core.rectangle(rgbaInnerWindow, mP2, mP1, red,20);
	    	 failCont=failCont+1;
	    	 pari=!pari;
	    	 if(failCont==25){
	    		 failCont=0;
	    		 isFailed=false;
	    	 }
	     }
	     
	     if(isFailed && failCont<=25 && pari){
	    	 Core.rectangle(rgbaInnerWindow, mP2, mP1, blue,20);
	    	 failCont=failCont+1;
	    	 dispari=!dispari;
	    	 if(failCont==25){
	    		 failCont=0;
	    		 isFailed=false;
	    	 }
	     }
	     
	     Imgproc.cvtColor(rgba, rgbaconv2, Imgproc.COLOR_RGBA2BGR,3);
	     Mat innerHist =  new Mat();
	     innerHist = calcInnerHist(rgbaconv2); 
	     innerSim = Imgproc.compareHist(innerHist, trInnerHist, Imgproc.CV_COMP_CORREL);
	     
	     float[] valueTreasure = new float[(int) trInnerHist.size().height];
	     trInnerHist.get(0,0,valueTreasure);
	     float[] value = new float[(int) innerHist.size().height];
	     innerHist.get(0,0,value);
	      
		 errore=0;
		 int val1;
		 int val2;
		 for(int i=0; i<innerHist.size().height; i++){
			 val1=(int)valueTreasure[i];
			 val2=(int)value[i];
			 errore=errore+(val1-val2)*(val1-val2);
		 } 
		 errore= (int) (errore/innerHist.size().height);
		 final org.opencv.core.Size rgbaDim = rgba.size();	 
	  	 if(novanta){
	  		 p3.y = rgbaDim.height-(rgbaDim.height-dime.height)/4;
	  		 p3.x= rgbaDim.width- (rgbaDim.width-dime.width)/2;
	  		 p4.y=p3.y;
	  		 if(innerSim>0){
	  			 p4.x=p3.x-(dime.width)*innerSim;
	  		 }else{
	  			 p4.x=p3.x;
	  	 	}
	  	 	Core.line(rgba,p3,p4,red,10); 	
	  	 }
	  	 else if(centottanta){
	  		 p3.y = (rgbaDim.height-dime.height)/2;
	  		 p3.x= rgbaDim.width-(rgbaDim.width-dime.width)/4;
	  		 p4.x=p3.x;
	  		 if(innerSim>0){
	  			 p4.y=p3.y+innerSim*dime.height;
	  		 }else{
	  			 p4.y=p3.y;
	  		 }
	  		 Core.line(rgba,p3,p4,red,10); 
	  	 }
	  	 else if(zero){
	  		 p3.y = rgbaDim.height-(rgbaDim.height-dime.height)/2;
	  		 p3.x= (rgbaDim.width-dime.width)/4;
	  		 p4.x=p3.x;
	  		 if(innerSim>0){
	  			 p4.y=p3.y-innerSim*dime.height;
	  		 }else{
	  			 p4.y=p3.y;
	  		 }
	  		 Core.line(rgba,p3,p4,red,10); 
	  	 }
		  	 
	     innerHist.release();
	     rgbaInnerWindow.release();
	     rgbaconv.release();
	     rgbaconv2.release();
		 return rgba;
    }
    
    /**
     * Method that convert an input bmp file into a Mat object
     * @param bmp the input bitmap to be converted
     * @return ConvMat the output converted Mat
     */
	public static Mat decodeBmp(Bitmap bmp){
		
		Mat ConvMat = new Mat();
		Utils.bitmapToMat(bmp, ConvMat);
		Log.e("dfedfe","ConvMat DEPTH = "+ConvMat.depth()+" TIPO = "+ConvMat.type());
		Imgproc.cvtColor(ConvMat, ConvMat, Imgproc.COLOR_RGB2BGR, 3); 
		ConvMat.convertTo(ConvMat, CvType.CV_8U);
		return ConvMat;
	}
	
	 /**
     * Method that calls the takePicture method of the standard Android camera.
     */
    public void takePhoto(){
  	  File mediaStorageDir = new File("/sdcard/", "FeatureCam     //progress.setIndeterminate(true);");
	  if (!mediaStorageDir.exists()) { mediaStorageDir.mkdirs(); }
      File mediaFile= new File(mediaStorageDir.getPath() + File.separator + "pic.jpg");
      String filename = mediaFile.toString();
      mOpenCvCameraView.search=true;
      mOpenCvCameraView.takePicture(filename);
      return;
    }
    
    /**
     * Method that senses for a motion event in order to distinguish between
     * a swipe (give up action) and a touch (capture action)
     * @param event an event that gives info about what is measured on the touch panel
     */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{     
	    switch(event.getAction())
	    {
	      case MotionEvent.ACTION_DOWN:
	          x1 = event.getX();      
	          y1 = event.getY();
	      break; 
	      
	      case MotionEvent.ACTION_UP:
	          x2 = event.getX();
	          y2 = event.getY();
	          float deltaX = x2 - x1;
	          float deltaY = y2 - y1; 
	          if(novanta){
	        	  if (Math.abs(deltaX) > MIN_DISTANCE){
		              // Left to Right swipe action
		              if (x2 > x1){
		            	  Intent previewIntent = new Intent(myContext, TreasureActivity.class);
		  		    	  myContext.startActivity(previewIntent);	
		              }
		              // Right to left swipe action               
		              else{
		            	  Intent previewIntent = new Intent(myContext, TreasureActivity.class);
		  		    	  myContext.startActivity(previewIntent);	
		              }
		          }
		          else{
		        	  progress.setVisibility(View.VISIBLE);
		        	  takePhoto();
		          }             
	          }else{
	        	  if (Math.abs(deltaY) > MIN_DISTANCE){
		              // Left to Right swipe action
		              if (y2 > y1){
		            	  Intent previewIntent = new Intent(myContext, TreasureActivity.class);
		  		    	  myContext.startActivity(previewIntent);
		              }
		              // Right to left swipe action               
		              else{
		            	  Intent previewIntent = new Intent(myContext, TreasureActivity.class);
		  		    	  myContext.startActivity(previewIntent);
		              }
		          }
		          else{
		        	  progress.setVisibility(View.VISIBLE);
		        	  takePhoto(); ;
		          } 
	          }
	      break;   
	    }           
	    return super.onTouchEvent(event);       
	}
    
	/**
	 * Method that performs the comparison between the captured picture and the treasure received image.
	 * In case of match the user will be notified about that and the activity finishes.
	 * In case the match fails the user must come back and try with another picture
	 * 
	 * @param myContext
	 */
    public static void findTreasure(final Context myContext){
        
    	Mat OnlineMat = new Mat();
        OnlineMat=decodeBmp(bmp); 
		MatOfKeyPoint TempKp = new MatOfKeyPoint();
		FeatureDetector Orbdetector = FeatureDetector.create(FeatureDetector.ORB);
		Orbdetector.detect(OnlineMat,TempKp);
		if ((int)TempKp.size().height == 0){
			nrOfErrors=nrOfErrors+1;
			progress.setVisibility(View.INVISIBLE);
			isFailed=true;
		}else{ //do all this activities only if enough keypoints are available, otherwise take another pic
			DescriptorExtractor orbDE = DescriptorExtractor.create(DescriptorExtractor.ORB);
			
			Mat trainDescriptor = new Mat();
			Mat queryDescriptor = new Mat();
			
			orbDE.compute(OnlineMat, TempKp, trainDescriptor);
			orbDE.compute(LoadedMat,TreasureKp,queryDescriptor);
			
			DescriptorMatcher BFmatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
			MatOfDMatch matches = new MatOfDMatch();	
			BFmatcher.match(trainDescriptor,queryDescriptor,matches);

			List<DMatch> matchesList = matches.toList();
			
			double max_dist=40;
			int c=0;
			double total=0;
			for(int i=0; i<matchesList.size(); i++){
				if(matchesList.get(i).distance<=max_dist){	
					c=c+1;
					total=total+matchesList.get(i).distance;	
				}		
			}			
			if(c>20){
				String DataAck = "FOUND";
				BluetoothActivity.mBluetoothConnection.write(DataAck.getBytes());
				BluetoothActivity.hoTrovatoTesoro=true;
				
				new CountDownTimer(2000, 1000) {
					public void onTick(long l){
						
					}
					public void onFinish(){
						progress.setVisibility(View.INVISIBLE);
						((SearchActivity) myContext).finish();
					}
				}.start();
				
			}else{
				progress.setVisibility(View.INVISIBLE);
				isFailed=true;
			}
	 	}
	
		OnlineMat.release();
		TempKp.release();
	
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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}		
	
}
