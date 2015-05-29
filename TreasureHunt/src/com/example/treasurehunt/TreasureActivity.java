package com.example.treasurehunt;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;

/**
* <p>Title: TreasureActivity</p>
* <p>Description: Preview of the treasure in "give up" case
</p>
* <p>Copyright: Copyright (c) 2015</p>
* <p>Company: University of Trento, DISI</p>
* <p>Class description: the treasure is revealed if the user give up.
* @author Alessandro Tontini & Martina Valente
* @version 1.0
*/

public class TreasureActivity extends Activity {
	
	static ImageView mImageView;
	public static Context myContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_treasure);
		myContext=this;
		mImageView=(ImageView) findViewById(R.id.imageView1);
		mImageView.setImageBitmap(BluetoothActivity.TreasureBmp);
		new CountDownTimer(5000, 1000) {
			public void onTick(long l){
				
			}
			public void onFinish(){
				String DataAck = "FAIL";
				BluetoothActivity.mBluetoothConnection.write(DataAck.getBytes());
				SearchActivity.finish=true;
				BluetoothActivity.hoFallito=true;
				finish();
			}
		}.start();

	}

	protected void onResume() {
		super.onResume();
		
	}

	protected void onPause() {
		
		super.onPause();
	}
}
