package com.example.treasurehunt;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
* <p>MMNET Team 04</p>
* <p>Project Title: Treasure Hunt</p>
* <p>Class Description: Main activity that manages Bluetooth and from which all the other 
* activities are instantiated.  
* @authors Alessandro Tontini & Martina Valente
*/

public class BluetoothActivity extends Activity {
	
	private static final String TAG = "DataTransferActivity";
	
	//Bluetooth tools
	private static final int REQUEST_ENABLE_BT = 0;
	private static final int SELECT_SERVER = 1;
	public  static final int DATA_RECEIVED = 3;
	public  static final int SOCKET_CONNECTED = 4;
	public  static final int REQUEST_DISCOVERABLE=5;
	public  static final UUID APP_UUID = UUID.fromString("aeb9f938-a1a3-4947-ace2-9ebd0c67adf1");
	private BluetoothAdapter mBluetoothAdapter = null;
	public  static ConnectionThread mBluetoothConnection = null;
	public  static ConnectThread mConnectBluetooth=null;
	public  static AcceptThread mAcceptBluetooth=null;
	
	//layouts
	static  Button searchButton;
	static  Button hideButton;
	public  static Button joinButton;
	public  static TextView tv = null; 
	public  static ImageView iv = null;
	public  static ImageView iv2 = null;
	
	//variables
	private byte[] totalArray = new byte[0];
	public  static Bitmap TreasureBmp;
	private boolean mServerMode= true;
	public  static boolean control=false;
	public  static boolean hoTrovatoTesoro=false;
	public  static boolean hoNascostoTesoro=false;
	public  static boolean hoFallito=false;
	
	private static Context  myContext;
	private static ProgressBar progress;
		
	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {

                } break;
                default:
                {
                	super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    /**
     * This method is responsible for the instantiation of all layout elements (buttons, imageviews, 
     * textviews). It checks whether Bluetooth is active or not and launches an intent 
     * to ask the user to enable it if needed.
     */    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.e("aa","OnCreate");
		super.onCreate(savedInstanceState);
				
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Log.i(TAG, "Bluetooth not supported");
			finish();
		}
		
		setContentView(R.layout.activity_bluetooth);
		
		searchButton = (Button)     findViewById(R.id.button1);
		hideButton   = (Button)     findViewById(R.id.button2);
		joinButton   = (Button)     findViewById(R.id.button3);
		iv           = (ImageView)  findViewById(R.id.imageView3); //color chest
		iv2          = (ImageView)  findViewById(R.id.imageView2); //black chest
		tv           = (TextView)   findViewById(R.id.text_window);
        progress     = (ProgressBar)findViewById(R.id.progressbar);
        
		myContext=this;
		
		Typeface pirateFont = Typeface.createFromAsset(myContext.getAssets(), "pirata.ttf");

		searchButton.setTypeface(pirateFont);
		searchButton.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				tv.setText("");
				v.clearAnimation();
				String DataAck = "SEARCHING";
				mBluetoothConnection.write(DataAck.getBytes());
			    
				final String PREFS_NAME = "MyPrefsFile1";
				final CheckBox dontShowAgain;
				 
				AlertDialog searchDialog = new AlertDialog.Builder(BluetoothActivity.this).create();
				        LayoutInflater adbInflater = LayoutInflater.from(BluetoothActivity.this);
				        View eulaLayout = adbInflater.inflate(R.layout.checkbox, null);
				        dontShowAgain = (CheckBox)eulaLayout.findViewById(R.id.skip);
				        searchDialog.setView(eulaLayout);
				        searchDialog.setTitle("Info - How To");
				        searchDialog.setIcon(R.drawable.ic_launcher);
				        searchDialog.setCanceledOnTouchOutside(false);
				        searchDialog.setCancelable(false);
				        searchDialog.setMessage(
					        "- Tap on the screen to capture a photo."+"\n"+
					        "- Remember to fit objects INTO the green rectangle." +"\n"+
					        "- A red sidebar shows you the color similarity between scene and Treasure. Don't take it too seriously!" +"\n"+
					        "- A blinking rectangle appears if you are wrong." +"\n"+
					        "- Swipe up or down if you want to give up."+"\n"+
					        "..Enjoy!"
				        );
				        searchDialog.setButton("Ok", new DialogInterface.OnClickListener() {
				              public void onClick(DialogInterface dialog, int which) {
				                  String checkBoxResult = "NOT checked";
				                  
				                  Intent searchIntent = new Intent(BluetoothActivity.this, SearchActivity.class);
							      startActivity(searchIntent);
							      
				                  if (dontShowAgain.isChecked())  checkBoxResult = "searchChecked";
				                    SharedPreferences searchSettings = getSharedPreferences(PREFS_NAME, 0);
				                    SharedPreferences.Editor searchEditor = searchSettings.edit();
				                    searchEditor.putString("searchSkipMessage", checkBoxResult);
				                    searchEditor.commit();
				                  return;
				              } });
				 
				        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				        String skipMessage = settings.getString("searchSkipMessage", "NOT checked");
				        if (skipMessage != "searchChecked" ) {
				        	searchDialog.show();	
				        }else{
			                  Intent searchIntent = new Intent(BluetoothActivity.this, SearchActivity.class);
						      startActivity(searchIntent);
				        }  
			}	
		});
		
		hideButton.setTypeface(pirateFont);
		hideButton.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				tv.setText("");
				v.clearAnimation();
				String DataAck = "HIDING";
				mBluetoothConnection.write(DataAck.getBytes());

				final String PREFS_NAME = "MyPrefsFile1";
				final CheckBox dontShowAgain;

				AlertDialog hideDialog = new AlertDialog.Builder(BluetoothActivity.this).create();
		        LayoutInflater adbInflater = LayoutInflater.from(BluetoothActivity.this);
		        View eulaLayout = adbInflater.inflate(R.layout.checkbox, null);
		        dontShowAgain = (CheckBox)eulaLayout.findViewById(R.id.skip);
		        hideDialog.setView(eulaLayout);
		        hideDialog.setTitle("Info");
		        hideDialog.setIcon(R.drawable.ic_launcher);
		        hideDialog.setCanceledOnTouchOutside(false);
		        hideDialog.setCancelable(false);
		        hideDialog.setMessage(
			        "- Tap on the screen to capture a photo."+"\n"+
			        "- Remember to fit objects INTO the green rectangle."+"\n"+
			        "..Enjoy!"
			    );
		        hideDialog.setButton("Ok", new DialogInterface.OnClickListener() {
		              public void onClick(DialogInterface dialog, int which) {
		                  String checkBoxResult = "NOT checked";
		                  
		                  Intent searchIntent = new Intent(BluetoothActivity.this, CameraActivity.class);
					      startActivity(searchIntent);
					      
		                  if (dontShowAgain.isChecked())  checkBoxResult = "hideChecked";
		                    SharedPreferences hideSettings = getSharedPreferences(PREFS_NAME, 0);
		                    SharedPreferences.Editor hideEditor = hideSettings.edit();
		                    hideEditor.putString("hideSkipMessage", checkBoxResult);
		                    hideEditor.commit();
		                  return;
		              } });
		 
		        SharedPreferences hideSettings = getSharedPreferences(PREFS_NAME, 0);
		        String skipMessage = hideSettings.getString("hideSkipMessage", "NOT checked");
		        if (skipMessage != "hideChecked" ) {
		        	hideDialog.show();	
		        }else{
	                  Intent hideIntent = new Intent(BluetoothActivity.this, CameraActivity.class);
				      startActivity(hideIntent);
		        } 
			}
		});
		
		joinButton.setTypeface(pirateFont);
		joinButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mServerMode=false;
				selectServer();
			}
		});
		
		//Set visibility
		hideButton.setVisibility(View.INVISIBLE);
		searchButton.setVisibility(View.INVISIBLE);
		joinButton.setVisibility(View.VISIBLE);
		tv.setText("Just ONE PLAYER has to click on \nNew Game button and select \nthe other one.");
		iv.setVisibility(View.INVISIBLE);
		iv2.setVisibility(View.INVISIBLE);
		progress.setVisibility(View.GONE);

		if (!mBluetoothAdapter.isEnabled()) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			startActivityForResult(discoverableIntent, REQUEST_ENABLE_BT);
		} else {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
		}	
				
	}
	
	/**
	 * This method is called when the user comes back to Bluetooth Activity after having finished 
	 * another activity. It behaves differently wrt some paramenters which contain the information 
	 * regarding the previously running activity
	 * 
	 * @param hoTrovatoTesoro
	 * @param hoNascostoTesoro
	 * @param hoFallito
	 */
	 
	 @Override
	 public void onResume()
	 {
		 	Log.e("aa","OnResume");
	        super.onResume();
	        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	        
	        if(hoTrovatoTesoro){

	    		tv.setText("");
	        	iv2.setVisibility(View.INVISIBLE); //black chest
				iv.setVisibility(View.VISIBLE);  //color chest
				startAnimColor();
				searchButton.setVisibility(View.INVISIBLE);
				hideButton.setVisibility(View.INVISIBLE);
				Toast.makeText(myContext,"Congratulations! You have found the Treasure! You are rich now.. enjoy!", Toast.LENGTH_LONG).show();
				if(mServerMode){
					mAcceptBluetooth.cancel();
					mServerMode=true;
					startAsServer();
				}else{
					mConnectBluetooth.cancel();
					mServerMode=true;
					startAsServer();
				}  
				hoTrovatoTesoro=false;
	        }
	        if(hoNascostoTesoro){

	        	Log.e("aa","hoNascostoTesoro");
	        	joinButton.setVisibility(View.INVISIBLE);
	        	searchButton.setVisibility(View.VISIBLE);
	        	Log.e("oo","punto 2");
	        	hideButton.setVisibility(View.VISIBLE);
	        	setButtonsEnabled(false);
	        	iv2.setVisibility(View.VISIBLE);
	        	tv.setText("");
	        	
	        	hoNascostoTesoro=false;
	        }
	        if(hoFallito){

	        	iv2.setVisibility(View.INVISIBLE); //black chest
				iv.setVisibility(View.INVISIBLE);  //color chest
				searchButton.setVisibility(View.INVISIBLE);
				hideButton.setVisibility(View.INVISIBLE);
				joinButton.setVisibility(View.VISIBLE);
				tv.setText("YOU LOSE");
				if(mServerMode){
					mAcceptBluetooth.cancel();
					mServerMode=true;
					startAsServer();
				}else{
					mConnectBluetooth.cancel();
					mServerMode=true;
					startAsServer();
				} 
				hoFallito=false;
	        }      
	}
	
	public void onBackPressed(){
		if(mBluetoothAdapter.isDiscovering()){
			mBluetoothAdapter.cancelDiscovery();
		}
		
		if(mBluetoothAdapter.isEnabled()){
			new AlertDialog.Builder(this)
			.setTitle("Bluetooth interface is enabled!")
		    .setMessage("Do you want to disable Bluetooth before exiting?")
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int which) { 
		    		mBluetoothAdapter.disable();
		            BluetoothActivity.this.finish();
		        }
		     })
		    .setNegativeButton("No", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // do nothing
		        	BluetoothActivity.this.finish();
		        }
		     })
		    .setIcon(android.R.drawable.ic_dialog_alert)
		    .show();
		} 
	}
	 
	@Override
	protected void onDestroy(){
		
		if(mBluetoothAdapter.isDiscovering()){
			mBluetoothAdapter.cancelDiscovery();
		}	
		super.onDestroy();
	}
	 
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode != RESULT_CANCELED) {
			startAsServer();
		} else if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED){
			Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
		} else if (requestCode == SELECT_SERVER && resultCode == RESULT_OK) {
			progress.setVisibility(View.VISIBLE);
			BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			mBluetoothAdapter.cancelDiscovery();
			connectToBluetoothServer(device.getAddress());
		} else if (requestCode == REQUEST_DISCOVERABLE && resultCode == RESULT_CANCELED){
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
		} else if(requestCode == REQUEST_DISCOVERABLE && resultCode != RESULT_CANCELED){
			startAsServer();
		}
	}

   /**
    * This method is run on both devices after the BT connection has been established.
    * It sets the device in Server mode, listening on RF for an incoming connection request 
    */
	private void startAsServer() {
		Log.e("xx","### START AS SERVER ###");
		mAcceptBluetooth = new AcceptThread(mHandler);
		mAcceptBluetooth.start();
	}
	
	/**
	 * This method is called when the New Game button (join Button) is pressed. It switches the device
	 * to Client mode and starts the Show Devices Activity.
	*/
	private void selectServer() {
		setButtonsEnabled(false);
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		ArrayList<String> pairedDeviceStrings = new ArrayList<String>();
		mBluetoothAdapter.startDiscovery();
		Intent showDevicesIntent = new Intent(this, ShowDevices.class);
		showDevicesIntent.putStringArrayListExtra("devices", pairedDeviceStrings);
		startActivityForResult(showDevicesIntent, SELECT_SERVER);
	}

	/**
	 * This method is called after the user has selected the device to connect to. It starts a Connect 
	 * Thread, that is responsible for the connection establishment at Client Side
	 * @param id string that contains the MAC address of the BT device to connect to
	 */
	private void connectToBluetoothServer(String id) {
		
		Log.e("xx","!!! *** ConnectToBluetoothServer *** !!!");
		mConnectBluetooth = null; 
		mConnectBluetooth = new ConnectThread(id, mHandler);
		mConnectBluetooth.start();
		new CountDownTimer(10000, 1000) {
			public void onTick(long l){	
			}
			public void onFinish(){
				if(!control){ 
					mConnectBluetooth.cancel();
					progress.setVisibility(View.INVISIBLE);
					Toast.makeText(myContext,"Connection Timeout", Toast.LENGTH_SHORT).show();
				}
			}
		}.start();
	}
	
	public void startAnimBlack(){
		Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
		iv2.startAnimation(fadeInAnimation);
	}
	
	public void startAnimColor(){
		Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
		iv.startAnimation(fadeInAnimation);	
		fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
		    @Override
		    public void onAnimationStart(Animation animation) {

		    }
		    @Override
		    public void onAnimationEnd(Animation animation) {
				final Animation foundAnimation = new AlphaAnimation(0, 1); 
				foundAnimation.setDuration(500); 
			    foundAnimation.setInterpolator(new LinearInterpolator());
			    foundAnimation.setRepeatCount(0);  
			    joinButton.setVisibility(View.VISIBLE);
				joinButton.setEnabled(true);
				joinButton.startAnimation(foundAnimation);
		    }
		    @Override
		    public void onAnimationRepeat(Animation animation) {
		    }
		});		
	}
	

	public Handler mHandler = new Handler() {
		@Override
		/**
		 * This method handles all messages coming from the Threads that manages Bluetooth
		 * 
		 * @param msg Message whose description can be of two different types: SOCKET_CONNECTED or
		 * DATA_RECEIVED. In the first case, the object is a Connection Thread started from Server or
		 * Client indifferently (the communication is full-duplex). In the second case, the object is 
		 * a byte array containing the information coming from the Bluetooth Input Stream
		 */
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SOCKET_CONNECTED: {
				
				mBluetoothConnection = (ConnectionThread) msg.obj; 
				
				joinButton.setVisibility(View.INVISIBLE);
				hideButton.setVisibility(View.VISIBLE);
				searchButton.setVisibility(View.VISIBLE);
				iv.setVisibility(View.INVISIBLE);
				iv2.setVisibility(View.VISIBLE);
				progress.setVisibility(View.INVISIBLE);

			    final Animation searchAnimation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
			    searchAnimation.setDuration(350); // duration 
			    searchAnimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
			    searchAnimation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
			    searchAnimation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in

				searchButton.setEnabled(false);
				hideButton.setEnabled(true);				
				hideButton.startAnimation(searchAnimation);
				
				startAnimBlack(); //black chest animation
				
				tv.setText("Click on \"Hide\" \nto hide The Treasure!");
				tv.bringToFront();
				
				break;
			}
			case DATA_RECEIVED: {

				byte[] receivedArray =  (byte[]) msg.obj; //could be string or image
				String data = new String(receivedArray, 0, receivedArray.length);
				
				if(data.equals("HIDING")){
					
						tv.setText("Wait! Mr.Sparrow\nis hiding the treasure!");
						hideButton.clearAnimation();
						setButtonsEnabled(false);					
					
				}else if(data.equals("SEARCHING")){
					
						tv.setText("Davy Jones \nis searching for the treasure!");
						setButtonsEnabled(false);
						
				}else if(data.equals("FOUND")){
					
						tv.setText("");
						Toast.makeText(myContext,"Your friend has found the Treasure!", Toast.LENGTH_SHORT).show();

						searchButton.clearAnimation();	
						hideButton.setVisibility(View.INVISIBLE);		
						searchButton.setVisibility(View.INVISIBLE);
						iv.setVisibility(View.VISIBLE); //color chest
						iv2.setVisibility(View.INVISIBLE); //black chest
						if(mServerMode){
							mAcceptBluetooth.cancel();
							mServerMode=true;
							startAsServer();
						}else{
							mConnectBluetooth.cancel();
							mServerMode=true;
							startAsServer();
						}
						startAnimColor();
					
				}else if(data.equals("FAIL")){
					
						tv.setText("You friend GAVE UP!");
						hideButton.setVisibility(View.INVISIBLE);	
						searchButton.setVisibility(View.INVISIBLE);	
						joinButton.setVisibility(View.VISIBLE);
						joinButton.setEnabled(true);
						iv2.setVisibility(View.INVISIBLE); 
						iv.setVisibility(View.INVISIBLE);  
						if(mServerMode){
							mAcceptBluetooth.cancel();
							mServerMode=true;
							startAsServer();
						}else{
							mConnectBluetooth.cancel();
							mServerMode=true;
							startAsServer();
						}
				    
				}else{ //IMAGE RECEIVED
					
					  	Bitmap bmp=composeBmp(receivedArray);
						if (bmp!=null){ //the image has been received completely
							tv.setText("Hurry up! You have to \nfind a new Treasure!");
							TreasureBmp=bmp;
							totalArray=null;
							totalArray=new byte[0];
							searchButton.setEnabled(true);
	
						    final Animation searchAnimation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
						    searchAnimation.setDuration(350); // duration - half a second
						    searchAnimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
						    searchAnimation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
						    searchAnimation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
	
							searchButton.startAnimation(searchAnimation);
							hideButton.clearAnimation();
		
						}
					}				
				
			}
			default: break;
			}
		}
	};
	/**
	 * This method simply decodes the concatenated input byte array into a bmp file.
	 * @param b input byte array
	 * @return bmp bitmap file or NULL if the decoding has failed.
	 */
	public Bitmap composeBmp(byte[] b){
		//byte concatenation
		byte[] temp = new byte[totalArray.length+b.length];
		System.arraycopy(totalArray, 0, temp, 0, totalArray.length);
		System.arraycopy(b, 0, temp, totalArray.length, b.length);
		totalArray=Arrays.copyOf(temp, temp.length);				
		//conversion from byte to bmp
		Bitmap bmp = BitmapFactory.decodeByteArray(totalArray, 0, totalArray.length);	
		return bmp;
	}
	/**
	 * This methods performs the conversion from Bitmap to Mat
	 * @param bmp input bitmap file
	 * @return ConvMat output Mat
	 */
	public Mat decodeBmp(Bitmap bmp){
		Mat ConvMat = new Mat();
		Utils.bitmapToMat(bmp, ConvMat);  
		Imgproc.cvtColor(ConvMat, ConvMat, Imgproc.COLOR_RGB2BGR, 3); 
		ConvMat.convertTo(ConvMat,CvType.CV_32F);
		return ConvMat;
	}
	
	/**
	 * This method converts a bitmap file into a byte array and calls the write() method of the
	 * input Connection Thread
	 * @param conny input Connection Thread
	 */
	public static void writeFileOnBT(ConnectionThread conny){ //non-blocking call
		tv.setText("");
		Bitmap bmp = CameraActivity.biemmepi;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG,90, stream);
		byte[] byteArray = stream.toByteArray();
		conny.write(byteArray);	
	} 
	 
	private void setButtonsEnabled(boolean state) {
		Log.e("a"," Sabilito i botonio");
		searchButton.setEnabled(state);
		hideButton.setEnabled(state);
	}
	
}