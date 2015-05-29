package com.example.treasurehunt;

import java.util.List;

import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;
import java.lang.Math;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;

/**
* <p>MMNET Team 04</p>
* <p>Project Title: Treasure Hunt</p>
* <p>Class Description: Class used as a toolbox in order to capture focused pictures.
* It implements standard Android Camera
* @authors Alessandro Tontini & Martina Valente
*/

public class CameraLib extends JavaCameraView implements PictureCallback {

    private static final String TAG = "Sample::Tutorial3View";
    
    private String mPictureFileName;
    private int wPicture = 640;
    private int hPicture = 480;
    public static Mat savedMat;
    public static Boolean search;
    
    public Intent previewIntent=CameraActivity.previewIntent;
    private Context myContext;
    
    public CameraLib (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    @SuppressWarnings("deprecation")
	public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }
    
	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){
		public void onAutoFocus(boolean arg0, Camera arg1) {
		//TODO Auto-generated method stub
		}
    };	
    
    public void takePicture(final String fileName) {
        
        this.mPictureFileName = fileName;
        mCamera.setPreviewCallback(null);
        Camera.Parameters params = mCamera.getParameters();
        List<Size> lista = params.getSupportedPictureSizes();
        double[] diff = new double[lista.size()];  
        Size temp;

        for(int i=0; i<lista.size(); i++){
        	temp=lista.get(i);
        	diff[i]=Math.sqrt((wPicture-temp.width)*(wPicture-temp.width)+(hPicture-temp.height)*(hPicture-temp.height));
        	Log.e("<zz","WIDTH = "+temp.width + " HEIGHT = "+temp.height+"DIFFERENZA= "+diff[i]);
        }
        
        double min = diff[0];
        int minIdx=0;
        for(int j=1; j<diff.length; j++){
        	if(diff[j]<min){
        		min=diff[j];
        		minIdx=j;
        	}
        }  
        params.setPictureSize(lista.get(minIdx).width,lista.get(minIdx).height);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(params);
        mCamera.autoFocus(myAutoFocusCallback);
        mCamera.takePicture(null, null, this);
    }
 
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        
        if(search){
        	SearchActivity.bmp = BitmapFactory.decodeByteArray(data, 0, data.length);;
        	SearchActivity.findTreasure(SearchActivity.myContext);
        }else{
        	CameraActivity.biemmepi = BitmapFactory.decodeByteArray(data, 0, data.length);
        	CameraActivity.showPreview(CameraActivity.myContext);
        }
    }
}
