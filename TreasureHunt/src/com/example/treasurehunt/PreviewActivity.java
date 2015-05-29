package com.example.treasurehunt;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
* <p>MMNET Team 04</p>
* <p>Project Title: Treasure Hunt</p>
* <p>Class Description: Activity called after the user has taken a picture in the "HIDE" phase.
* It shows the captured image. In case that the number of keypoints is below a threshold the user must 
* come back and try to take another picture.
* @authors Alessandro Tontini & Martina Valente
*/
public class PreviewActivity extends Activity {
	
	static ImageView mImageView;
	private Button yesButton,noButton;
	private TextView tv = null;
	private static Context myContext;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_preview);
		tv = (TextView) findViewById(R.id.textwin);
		myContext = this;		
		mImageView=(ImageView) findViewById(R.id.imageView1);

		if(CameraActivity.zero){
			
		}
		if(CameraActivity.novanta){
			mImageView.setRotation(90);
		}
		if(CameraActivity.centottanta){
			mImageView.setRotation(180);
		}
		
		mImageView.setImageBitmap(CameraActivity.biemmepi);
			
		Mat rgba=decodeBmp(CameraActivity.biemmepi);
		final org.opencv.core.Size sizeRgba = rgba.size();

		Mat rgbaInnerWindow;
		
		int rows = (int) sizeRgba.height;
		int cols = (int) sizeRgba.width;
	
		int left = cols / 8;
		int top = rows / 8;
		
		int width = cols * 3 / 4;
		int height = rows * 3 / 4;
		
		rgbaInnerWindow = rgba.submat(top, top+height, left, left+width);
		
		FeatureDetector Orbdetector = FeatureDetector.create(FeatureDetector.ORB);
		MatOfKeyPoint keypoints = new MatOfKeyPoint();
		
		final Mat rgbaconv = new Mat();
		Imgproc.cvtColor(rgbaInnerWindow, rgbaconv, Imgproc.COLOR_RGBA2RGB);

		Orbdetector.detect(rgbaconv,keypoints);	
		
		tv.setText("Are you happy with your Treasure?");
		tv.setTextSize(20);
		tv.setTextColor(Color.parseColor("#000000"));
		tv.setBackgroundColor(Color.parseColor("#AAFFFFFF"));
		
		if(keypoints.size().height<300){
            AlertDialog myAlertDialog = new AlertDialog.Builder(PreviewActivity.this).create();
            myAlertDialog.setTitle("Mmmh, there's something wrong with your treasure..");
            myAlertDialog.setMessage("You should find much more keypoints :)");
            myAlertDialog.setIcon(R.drawable.ic_launcher);
            myAlertDialog.setCanceledOnTouchOutside(false);
            myAlertDialog.setCancelable(false);
            myAlertDialog.setButton("Ok, i'll come back :)", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ((Activity) myContext).finish();
                }
            });
            myAlertDialog.show();
		}
		
		yesButton = (Button) findViewById(R.id.button1);
		yesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BluetoothActivity.writeFileOnBT(BluetoothActivity.mBluetoothConnection);
				BluetoothActivity.hoNascostoTesoro=true;
				CameraActivity.finish=true;			
				finish();		
			}	
		});
		
		noButton = (Button) findViewById(R.id.button2);
		noButton.setOnClickListener(new OnClickListener() {
		@Override
			public void onClick(View v) {
				finish();
			}	
		});	
	}
	
	public static Mat decodeBmp(Bitmap bmp){
		Mat ConvMat = new Mat();
		Utils.bitmapToMat(bmp, ConvMat); 
		Imgproc.cvtColor(ConvMat, ConvMat, Imgproc.COLOR_RGB2BGR, 3); 
		ConvMat.convertTo(ConvMat,CvType.CV_32F);
		ConvMat.convertTo(ConvMat, CvType.CV_8U);
		return ConvMat;
	}
	
	protected void onResume() {
		super.onResume();	
	}
	
	protected void onPause() {
		super.onPause();
	}
}