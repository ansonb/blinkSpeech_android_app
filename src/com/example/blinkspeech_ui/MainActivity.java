package com.example.blinkspeech_ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.*;

import com.example.blinkspeech_ui.BluetoothClass.Status;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

	TextView mIV1;
	TextView mIV2;
	TextView mIV3;
	TextView mIV4;
	TextView mIV5;
	TextView mIV6;
	TextView mIV7;
	TextView mIV8;
	TextView mIV9;
	
	TextView mTV;
	Button mButton;
	
	TextToSpeech tts;
	
	boolean leftActivity = false;
	
	int group=0;
	
	int width, height;
	
	Thread check_time;
	Thread check_click;
	
	public static String mString;
	
	private String deviceName = "HC-05";
	private UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private BluetoothClass btClass;
	
	enum Status{
		DISCONNECTED,
		PAIRED,
		CONNECTED
	};
	
	enum cursorPos{
		FIRST,
		SECOND,
		THIRD,
		SPEAKER,
		ALL
	};
	
	enum scrollOption{
		AUTO,
		MANUAL,
		HORAUTO,
		VERTAUTO	
	};
	
	cursorPos rowPos, colPos;
	
	scrollOption currScroll;
	
	private ProgressDialog mProgressDlg;
		
	private Set<BluetoothDevice> paired;
	private BluetoothAdapter mBluetoothAdapter;
	public static BluetoothSocket mSocket;
	public BluetoothDevice mDevice;
	public String mDeviceName = "HC-05";
	
	public Status mStatus = Status.DISCONNECTED;
	
	public Menu menu;
	
	
	
	
    @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        currScroll = scrollOption.MANUAL;
        rowPos = cursorPos.FIRST;
        colPos = cursorPos.FIRST;
        
        mTV = (TextView) findViewById(R.id.textView1);
        mButton = (Button) findViewById(R.id.button1);
        
        mIV1 = (TextView) findViewById(R.id.abc1);
        mIV2 = (TextView) findViewById(R.id.def2);
        mIV3 = (TextView) findViewById(R.id.ghi3);
        mIV4 = (TextView) findViewById(R.id.jkl4);
        mIV5 = (TextView) findViewById(R.id.mno5);
        mIV6 = (TextView) findViewById(R.id.pqr6);
        mIV7 = (TextView) findViewById(R.id.stu7);
        mIV8 = (TextView) findViewById(R.id.vwx8);
        mIV9 = (TextView) findViewById(R.id.yz90); 
        
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x-48;
        height = size.y-200;
        
        mIV1.setHeight(height/3);
        mIV2.setHeight(height/3);
        mIV3.setHeight(height/3);
        mIV4.setHeight(height/3);
        mIV5.setHeight(height/3);
        mIV6.setHeight(height/3);
        mIV7.setHeight(height/3);
        mIV8.setHeight(height/3);
        mIV9.setHeight(height/3);
        
        mIV1.setWidth(width/3);
        mIV2.setWidth(width/3);
        mIV3.setWidth(width/3);
        mIV4.setWidth(width/3);
        mIV5.setWidth(width/3);
        mIV6.setWidth(width/3);
        mIV7.setWidth(width/3);
        mIV8.setWidth(width/3);
        mIV9.setWidth(width/3);
        
        mIV1.getBackground().setAlpha(25);
        mIV2.getBackground().setAlpha(25);
        mIV3.getBackground().setAlpha(25);
        mIV4.getBackground().setAlpha(25);
        mIV5.getBackground().setAlpha(25);
        mIV6.getBackground().setAlpha(25);
        mIV7.getBackground().setAlpha(25);
        mIV8.getBackground().setAlpha(25);
        mIV9.getBackground().setAlpha(25);
        
        mTV.setWidth(width-90);

        tts = new TextToSpeech(getApplicationContext(),new TextToSpeech.OnInitListener(){
        	@Override
        	public void onInit(int status){
        		if(status!=TextToSpeech.ERROR){
        			tts.setLanguage(Locale.ENGLISH);
        		}
        	}
        });
        
        mButton.getBackground().setAlpha(255);
        mButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    String mString = mTV.getText().toString();
			    tts.speak(mString, TextToSpeech.QUEUE_FLUSH, null);
			}
		});
        
        mProgressDlg = new ProgressDialog(this);
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
        
        check_time = new Thread(new Runnable(){
        	public void run(){
        		long currTime = System.currentTimeMillis();
        		long prevTime = currTime;
        		long delta;
        		int count = 0;
        		while(true){
        			currTime = System.currentTimeMillis();
        			delta = currTime - prevTime;
        			if(delta>1000){
        				count++;
        				final int fcount = count;
        				MainActivity.this.runOnUiThread(new Runnable(){
        					public void run(){
        						
        						
        					}
        				});
        				delta=0;
        				prevTime = currTime;
        			}
        		}
        	}
        });
        check_time.start();
                
        
        showCursor();
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public void onPause(){
        super.onPause();
        showToast("Leaving Activity");
        leftActivity = true;
     }
    
    @Override
    public void onRestart(){
    	super.onRestart();
    	showToast("Restarting activity");
    	leftActivity = false;
    	mTV.setText(mString);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        return true;
    }

    @SuppressLint("NewApi") @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_BT){
        	//On first Entry
        	if(mBluetoothAdapter == null){
        		initialiseBT();
        		return true;
        	}else{
        		if(!mBluetoothAdapter.isEnabled()){
        			enableBluetooth();
            		return true;
        		}
        	}
        
        	//If already connected disconnect
        	if(mSocket!=null && mSocket.isConnected()){
        		try {
					mSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        		return true;
        	}
        	
        	//If not connected connect
        	mDevice = findDevice(deviceName); //Find device in list of paired devices
        	if(mDevice==null){  //If not paired
        		scanDevice();
        		return true;
        	}else{              //If paired
        		connectBt();
        	}
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showToast(String message){
    	Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    private void openSubGroup(){
    	final Intent intent = new Intent(MainActivity.this, subGroup.class);
		intent.putExtra("Group", group);
		group = 0;
		startActivity(intent);
    }
    
    public void gr1_click(View v){
    	group = 1;
    	openSubGroup();
    }
    
    public void gr2_click(View v){
    	group = 2;
    	openSubGroup();
    }
    
    public void gr3_click(View v){
    	group = 3;
    	openSubGroup();
    }
    
    public void gr4_click(View v){
    	group = 4;
    	openSubGroup();

    }
    
    public void gr5_click(View v){
    	group = 5;
    	openSubGroup();

    }
    
    public void gr6_click(View v){
    	group = 6;
    	openSubGroup();

    }
    
    public void gr7_click(View v){
    	group = 7;
    	openSubGroup();

    }
    
    public void gr8_click(View v){
    	group = 8;
    	openSubGroup();

    }
    
    public void gr9_click(View v){
    	group = 9;
    	openSubGroup();

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
    
    private void enableBluetooth(){
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		
	    startActivityForResult(intent, 1000);
	}
	
	public BluetoothDevice findDevice(String device){
		paired = mBluetoothAdapter.getBondedDevices();
		BluetoothDevice btDeviceDefault = null; 
		if (paired == null || paired.size() == 0) { 
			
		} else {
			for(BluetoothDevice btDevice : paired){
				if(btDevice.getName() == null){
					continue;
				}
				if(btDevice.getName().compareTo(device)==0){
					return btDevice;
				}					
			}
		}
		return null;
		//return btDeviceDefault;
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
		//return btDeviceDefault;
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
	        	//showToast("In Broadcast acl_conn ");
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
	        		mStatus = Status.DISCONNECTED;
	        		showToast("Device Disconnected");
	        	}catch(Exception e){
	        		e.printStackTrace();
	        		showToast(e.getLocalizedMessage());
	        	}
	        	
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
	
	@SuppressLint("NewApi") private void readMessage_async(){
		try {
			while(mSocket!=null && mSocket.isConnected()){
				if(leftActivity){
					continue;
				}
				
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
					if(!leftActivity){
						MainActivity.this.runOnUiThread(new Runnable(){
							public void run(){
								//TODO
								scrollHandler(fs);
							}
						});
					}else{
						subGroup.messageReceived = true;
						subGroup.mReceived = fs;
					}
					
					
				}
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MainActivity.this.runOnUiThread(new Runnable(){
				public void run(){
					showToast(e.getLocalizedMessage()) ;
				}
			});
		}
		
	}
	
	private void showCursor(){
		 mIV1.getBackground().setAlpha(25);
	     mIV2.getBackground().setAlpha(25);
	     mIV3.getBackground().setAlpha(25);
	     mIV4.getBackground().setAlpha(25);
	     mIV5.getBackground().setAlpha(25);
	     mIV6.getBackground().setAlpha(25);
	     mIV7.getBackground().setAlpha(25);
	     mIV8.getBackground().setAlpha(25);
	     mIV9.getBackground().setAlpha(25);
	     
	     mButton.getBackground().setAlpha(255);

	     switch(rowPos){
	     case FIRST:
	    	 switch(colPos){
		     case FIRST:
		    	 mIV1.getBackground().setAlpha(50);
		    	 break;
		     case SECOND:
		    	 mIV2.getBackground().setAlpha(50);
		    	 break;
		     case THIRD:
		    	 mIV3.getBackground().setAlpha(50);
		    	 break;
		     case ALL:
		    	 break;
		     default:
		    	 break;
		     }
	    	 break;
	     case SECOND:
	    	 switch(colPos){
		     case FIRST:
		    	 mIV4.getBackground().setAlpha(50);
		    	 break;
		     case SECOND:
		    	 mIV5.getBackground().setAlpha(50);
		    	 break;
		     case THIRD:
		    	 mIV6.getBackground().setAlpha(50);
		    	 break;
		     case ALL:
		    	 break;
		     default:
		    	 break;
		     }
	    	 break;
	     case THIRD:
	    	 switch(colPos){
		     case FIRST:
		    	 mIV7.getBackground().setAlpha(50);
		    	 break;
		     case SECOND:
		    	 mIV8.getBackground().setAlpha(50);
		    	 break;
		     case THIRD:
		    	 mIV9.getBackground().setAlpha(50);
		    	 break;
		     case ALL:
		    	 break;
		     default:
		    	 break;
		     }
	    	 break;
	     case SPEAKER:
	    	 mButton.getBackground().setAlpha(150);
	    	 break;
	     case ALL:
	    	 break;
	     default:
	    	 break;
	     }
	}
	
	private void scrollHandler(String s) {
		// TODO Auto-generated method stub
		if(s.equals("b")){
			if(colPos.equals(cursorPos.FIRST)){
				colPos = cursorPos.SECOND;
			}else if(colPos.equals(cursorPos.SECOND)){
				colPos = cursorPos.THIRD;
			}else if(colPos.equals(cursorPos.THIRD)){
				if(rowPos.equals(cursorPos.FIRST)){
					rowPos = cursorPos.SECOND;
					colPos = cursorPos.FIRST;
				}else if(rowPos.equals(cursorPos.SECOND)){
					rowPos = cursorPos.THIRD;
					colPos = cursorPos.FIRST;
				}else if(rowPos.equals(cursorPos.THIRD)){
					rowPos = cursorPos.SPEAKER;
					colPos = cursorPos.SPEAKER;
				}
				
			}else if(colPos.equals(cursorPos.SPEAKER)){
				rowPos = cursorPos.FIRST;
				colPos = cursorPos.FIRST;
			}
			
			showCursor();
		}else if(s.equals("B")){
			if(colPos.equals(cursorPos.SPEAKER)){
				String mString = mTV.getText().toString();
			    tts.speak(mString, TextToSpeech.QUEUE_FLUSH, null);
			    return;
			}
			group = findGroup();
			if(group!=0){
				openSubGroup();
			}
		}else if(s.equals("a")){
			
		}
	}
	
	private int findGroup(){
		int pos=0;
		switch(rowPos){
	     case FIRST:
	    	 switch(colPos){
		     case FIRST:
		    	 return 1;
		     case SECOND:
		    	 return 2;
		     case THIRD:
		    	 return 3;
		     case ALL:
		    	 break;
		     default:
		    	 break;
		     }
	    	 break;
	     case SECOND:
	    	 switch(colPos){
		     case FIRST:
		    	 return 4;
		     case SECOND:
		    	 return 5;
		     case THIRD:
		    	 return 6;
		     case ALL:
		    	 break;
		     default:
		    	 break;
		     }
	    	 break;
	     case THIRD:
	    	 switch(colPos){
		     case FIRST:
		    	 return 7;
		     case SECOND:
		    	 return 8;
		     case THIRD:
		    	 return 9;
		     case ALL:
		    	 break;
		     default:
		    	 break;
		     }
	    	 break;
	     case SPEAKER:
	    	 break;
	     case ALL:
	    	 break;
	     default:
	    	 break;
	     }
		return pos;
	}
	
	public void scanDevice(){
		mBluetoothAdapter.startDiscovery();
	}
}
