package com.example.treasurehunt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

/**
* <p>MMNET Team 04 </p>
* <p>Project Title: Treasure Hunt </p>
* <p>Thread Description: Bluetooth thread that implements the write/read functions on BT interface.
* @author Alessandro Tontini & Martina Valente
*/

public class ConnectionThread extends Thread {
	
	BluetoothSocket mBluetoothSocket;
	private final Handler mHandler;
	private InputStream mInStream;
	private OutputStream mOutStream;

	ConnectionThread(BluetoothSocket socket, Handler handler){
		super();
		mBluetoothSocket = socket; // server or client depending on the arrival path
		mHandler = handler;
		try {
			mInStream = mBluetoothSocket.getInputStream();
			mOutStream = mBluetoothSocket.getOutputStream();
		} catch (IOException e) {
		}
	}
	
	/**
	 * This method keeps listening on the BT input stream.
	 * If something is received, it is stored in a byte array and sent (via Handler message)
	 * to Bluetooth Activity.
	 */
	@Override
	public void run() {//read 
			int bytes;
		while (true) {
			try {
				byte[] buffer = new byte[1024];
				bytes = mInStream.read(buffer);
				byte[] bufferRead = new byte[bytes];
				bufferRead = Arrays.copyOf(buffer, bytes);
				mHandler.obtainMessage(BluetoothActivity.DATA_RECEIVED,bufferRead).sendToTarget();	
			} catch (IOException e) {
				break;
			}
		}
	}
	
	/**
	 * This method is used to write binary data through BT output interface.
	 * @param bytes the data to be sent
	 */
	public void write(byte[] bytes) {//write
		try {
			mOutStream.write(bytes);
		} catch (IOException e) {
		}
	}
}
