package com.example.blinkspeech_ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import com.example.blinkspeech_ui.MainActivity.Status;

import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;

public class BluetoothClass extends Activity{

	enum Status{
		DISCONNECTED,
		PAIRED,
		CONNECTED
	};
	
	private Set<BluetoothDevice> paired;
	
	private ProgressDialog mProgressDlg;
		
	private BluetoothAdapter mBluetoothAdapter;
	
	public BluetoothDevice mDevice;
	String mDeviceName;
	
	public Status mStatus = Status.DISCONNECTED;
	
	public BluetoothSocket mSocket;
	
	public UUID mUUID;
	
	Context context;
	
	public BluetoothClass(String s, UUID uuid, Context context){
		this.context = context; 
		showToast("In BluetoothClass");
		mDeviceName = s;
		mUUID = uuid;
		//this.context = context;
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if(mBluetoothAdapter == null){
		    showToast("Bluetooth not supported");
		    //TODO: check if works
		    return;
		}
		
		if(!mBluetoothAdapter.isEnabled()){
			try{
				enableBluetooth();
			}catch(Exception e){
				showToast("Please enable Bluetooth");
			}
		}
		
		
		mProgressDlg = new ProgressDialog(getApplicationContext());
		mProgressDlg.setMessage("Scanning...");
		mProgressDlg.setCancelable(false);
		mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.dismiss();
		        
		        mBluetoothAdapter.cancelDiscovery();
		    }
		});
		
        IntentFilter filter = new IntentFilter();
		
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
		
		registerReceiver(mReceiver, filter);
		
	}
	
	 public void initialiseBT(){
	        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			
			if(mBluetoothAdapter == null){
			    showToast("Bluetooth not supported");
			    return;
			}
			
			if(!mBluetoothAdapter.isEnabled()){
				enableBluetooth();
			}
			
	    }
	
	
	public void showToast(String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	
	//TODO: This will not work here
	private void enableBluetooth(){
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		
		startActivityForResult(intent, 1000);
	}
	
	public boolean foundDevice(String device){
		paired = mBluetoothAdapter.getBondedDevices();
		BluetoothDevice btDeviceDefault = null; 
		if (paired == null || paired.size() == 0) { 
			showToast("No Paired Devices Found");
		} else {
			for(BluetoothDevice btDevice : paired){
				if(btDevice.getName() == null){
					continue;
				}
				if(btDevice.getName().compareTo(device)==0){
					return true;
				}					
			}
		}
		return false;
	}
	
	public BluetoothDevice findDevice(String device){
		paired = mBluetoothAdapter.getBondedDevices();
		
		if (paired == null || paired.size() == 0) { 
			showToast("No Paired Devices Found");
			return null;
		} else {
			for(BluetoothDevice btDevice : paired){
				if(btDevice.getName().equals(device)){
					return btDevice;
				}							
			}
		}
		return null;
	}
	
	public void pairDevice(BluetoothDevice device){
		try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            mStatus = Status.PAIRED;
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void unpairDevice(BluetoothDevice device){
		 try {
	            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
	            method.invoke(device, (Object[]) null);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}
	
	public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    @SuppressLint("NewApi") public void onReceive(Context context, Intent intent) {	    	
	        String action = intent.getAction();
	        final BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	        
	        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
	        	final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
	        	 
	        	if (state == BluetoothAdapter.STATE_ON) {
	        		 
	        	 }
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {				
				mProgressDlg.show();
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
	        	mProgressDlg.dismiss();
	        	
	        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	        	BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		        	        	
	        	showToast("Found device " + device.getName());
	        	
	        	if(device.getName().equals(mDeviceName)){
	        		mDevice = device;
	        		
	        		mBluetoothAdapter.cancelDiscovery();
		        	if(mStatus!=Status.PAIRED){
						pairDevice(mDevice);
				    }
		        	
	        	}
	        	
	        	
	        }  else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
	            //Device is now connected
	        	showToast("In Broadcast acl_conn ");
	        	final BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				//Connect only if paired
				if(foundDevice(device.getName())){
					try {
						//if socket is already connected reconnection will cause disconnection
	        			if(!mSocket.isConnected()){
	        				mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mUUID);
		        			mSocket.connect();
	        			}
	        			
						new Thread(new Runnable(){
							public void run(){
								readMessage_async();
							}
						}).start();
						
						mStatus = Status.CONNECTED;
						showToast("Device Connected");
		            } catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						showToast(e.getLocalizedMessage());
					}	
				}
	        }
	        else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
	           //Device is about to disconnect
	        }
	        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
	           //Device has disconnected
	        	try{
	        		mSocket.close();
	        		showToast("Device Disconnected");
	        	}catch(Exception e){
	        		e.printStackTrace();
	        		showToast(e.getLocalizedMessage());
	        	}
	        	mStatus = Status.DISCONNECTED;
	        	showToast("Disconnected");
	        } 
	        
	    }
	};
	
	@SuppressLint("NewApi") public void connectBt(){
		try {
			mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mUUID);
		    mSocket.connect();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@SuppressLint("NewApi") public void writeMessage(String message){	    
		try {
		    OutputStream mmOutput = mSocket.getOutputStream();
		    mmOutput.write(message.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showToast(e.getLocalizedMessage());
		}
	}
	
	private void readMessage_async(){
		try {
			while(true){
				InputStream mmInput = mSocket.getInputStream();
				int bytesAvailable=0;
				//Check if data is available
				if((bytesAvailable=mmInput.available())!=0){
					byte[] buffer = new byte[bytesAvailable];
					mmInput.read(buffer);
					char tmp;
					String s="";
					for(int i=0;i<bytesAvailable;i++){
						tmp = (char)buffer[i];
						s += tmp;
					}
					
					final String fs = s;
					BluetoothClass.this.runOnUiThread(new Runnable(){
						public void run(){
							//TODO
						}
					});
					
				}
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			BluetoothClass.this.runOnUiThread(new Runnable(){
				public void run(){
					showToast(e.getLocalizedMessage()) ;
				}
			});
		}
		
	}
	
	public void scanDevice(){
		mBluetoothAdapter.startDiscovery();
	}
}
