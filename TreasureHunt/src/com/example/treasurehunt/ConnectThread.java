package com.example.treasurehunt;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

/**
* <p>MMNET Team 04 </p>
* <p>Project Title: Treasure Hunt </p>
* <p>Thread Description: Bluetooth Client management, here a connection request is sent to the
* selected Server.
* @author Alessandro Tontini & Martina Valente
*/

public class ConnectThread extends Thread {
	private BluetoothSocket mBluetoothSocket;
	private final BluetoothDevice mDevice;
	private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private final Handler mHandler;

	public ConnectThread(String deviceID, Handler handler) {
		mDevice = mBluetoothAdapter.getRemoteDevice(deviceID);
		mHandler = handler;
		try {
			mBluetoothSocket = mDevice.createRfcommSocketToServiceRecord(BluetoothActivity.APP_UUID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This is the core method of the thread.
	 * It connects the device through the socket. If an error occurs, the current socket is closed.
	 */
    public void run() {
        mBluetoothAdapter.cancelDiscovery();
 
        try {
            // This will block until it succeeds or throws an exception
            mBluetoothSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
        	Log.e("edfe","UNABLE TO CONNECT!!!!");
            try {
                mBluetoothSocket.close();
            } catch (IOException closeException) { }
            return;
        }
 
        manageConnectedSocket(mBluetoothSocket);
    }
    
    /**
     * This method starts a new connection thread in order to make the device
     * able to send/receive data via Bluetooth.
     * @param mBluetoothSocket the connected BluetoothSocket
     */
	private void manageConnectedSocket(BluetoothSocket mBluetoothSocket) {
		
		ConnectionThread conn = new ConnectionThread(mBluetoothSocket, mHandler);
		mHandler.obtainMessage(BluetoothActivity.SOCKET_CONNECTED, conn).sendToTarget();
		BluetoothActivity.control=true;
		conn.start();
		
	}
	
	public void cancel() {
		try {
			mBluetoothSocket.close();
			
		} catch (IOException e) {
		}
	}

}
