package com.example.treasurehunt;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

/**
* <p>MMNET Team 04 </p>
* <p>Project Title: Treasure Hunt </p>
* <p>Thread Description: Bluetooth server management, here a connection is accepted.
* @author Alessandro Tontini & Martina Valente
*/

public class AcceptThread extends Thread {
	private BluetoothServerSocket mServerSocket=null;
	private BluetoothSocket mBluetoothSocket = null;
	private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private final Handler mHandler;

	public AcceptThread(Handler handler) {
		mHandler = handler;
		try {
			Log.e("a","STABILISCO RF CONNECTION");
			mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Bluetooth Demo", BluetoothActivity.APP_UUID);
		} catch (IOException e) {
		}
	}
	
	/**
	 * This is the core method of the thread.
	 * It keeps listening until exception occurs or a socket is returned
	 * If a connection has been accepted the socket will be closed in order to free resources.
	 */
    public void run() {
        BluetoothSocket socket = null;

        while (true) {
            try {
                socket = mServerSocket.accept();
            } catch (IOException e) {
            	Log.e("EXC","ECCEZIONE DI ACCEPT :( :( :( :(");
                break;
            }
           
            if (socket != null) {
                
                manageConnectedSocket(socket);
                try {
					mServerSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
                break;
            }
        }
    }
    
    /**
     * This method starts a new connection thread in order to make the device
     * able to send/receive data via Bluetooth.
     * @param mBluetoothSocket the connected BluetoothSocket
     */
	private void manageConnectedSocket(BluetoothSocket mBluetoothSocket) {
		
		ConnectionThread conn = new ConnectionThread(mBluetoothSocket, mHandler);
		mHandler.obtainMessage(BluetoothActivity.SOCKET_CONNECTED, conn).sendToTarget();
		conn.start();
	}
	
	public void cancel() {
		try {
			if (null != mServerSocket)
				mServerSocket.close();
		} catch (IOException e) {
		}
	}
}
