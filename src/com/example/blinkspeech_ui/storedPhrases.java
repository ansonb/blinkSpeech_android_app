package com.example.blinkspeech_ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;


import android.R.color;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class storedPhrases extends ActionBarActivity{
	ListView lv;
	DatabaseOperations DOP;
	ArrayList<String> list = new ArrayList<String>();;
	ArrayAdapter<String> lvAdapter;
	TextToSpeech tts;
	
	boolean endOfActivity = false;
	boolean leftActivity = false;
	
	int mode = MainActivity.mode;
	int totalModes = MainActivity.totalModes;
	
	int scrollCursorPos=0;
	
	BluetoothSocket mSocket = MainActivity.mSocket;
	
	Thread mHandler = new Thread(new Runnable(){
		   @Override	
	 	   public void run(){
	 	       readMessage_async();
	 	   }
	    });
	
	public storedPhrases(){
		//Default constructor
	}
	
	@SuppressLint("NewApi") @Override 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.storedphrase);
		
		Log.d("storedPhrases", "set layout");
		lv = (ListView)findViewById(R.id.listView1);
		lv.setLongClickable(true);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		DOP = new DatabaseOperations(storedPhrases.this);
		displayList();
		
        tts = new TextToSpeech(getApplicationContext(),new TextToSpeech.OnInitListener(){
        	@Override
        	public void onInit(int status){
        		if(status!=TextToSpeech.ERROR){
        			tts.setLanguage(Locale.ENGLISH);
        		}
        	}
        });		
        
        if(mSocket!=null && mSocket.isConnected()){
     	   mHandler.start();
        }
        
        Thread check_time = new Thread(new Runnable(){
           	public void run(){
           		long currTime = System.currentTimeMillis();
           		long prevTime = currTime;
           		long delta;
           		int count = 0;
           		while(true && !endOfActivity){
           			if(mode==1 || leftActivity) continue;
           			
           			currTime = System.currentTimeMillis();
           			delta = currTime - prevTime;
           			if(delta>1000){
           				count++;
           				final int fcount = count;
           				storedPhrases.this.runOnUiThread(new Runnable(){
           					public void run(){
           						scrollToNext();
           					}
           				});
           				delta=0;
           				prevTime = currTime;
           			}
           		}
           	}
           });
           check_time.start();
        
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
        	@Override
        	public void onItemClick(AdapterView<?> parent, final View view,
  		          int position, long id){
        		String s = list.get(position).toString();
        		if(s.equals("Return")){
        			finish();
        		}
        		tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
        	}
        });
       
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
        	@Override
        	public boolean onItemLongClick(AdapterView<?> parent, final View view,
  		          int position, long id){
        		
        		final int pos = position;
        		
        		new AlertDialog.Builder(storedPhrases.this)
                .setMessage("Delete the Text?")
                .setCancelable(false)
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @SuppressLint("NewApi")
                    public void onClick(DialogInterface dialog, int id) {
            	    	return;
                    }
                })
                .setNegativeButton("Yes",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						DOP.delete(DOP, list.get(pos).toString());
						displayList();
						scrollCursorPos = 0;
					}
                	
                }).show();
        		return true;
        	}
        });
	}
	
	private void displayList() {
		list = new ArrayList<String>();
		
		list.add("Return");
		Cursor CR = DOP.getInfo(DOP);
		Log.d("phrase", "Got CR");
		if(CR.moveToLast()){
			do{
				list.add(CR.getString(0));
				Log.d("phrase", "added "+CR.getString(0));
			}while(CR.moveToPrevious());
		}
		
		Log.d("phrase", "added return");
		lvAdapter = new ArrayAdapter<String>(this,
    	        android.R.layout.simple_list_item_1, list);
		lv.setAdapter(lvAdapter);
		Log.d("phrase", "returning");
	}

	@Override
	public void onResume(){
		leftActivity = false;
		super.onResume();
	}
	
	@Override
	public void onPause(){
        leftActivity = true;
		super.onPause();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		endOfActivity = true;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.others, menu);
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
	private void showToast(String message){
    	Toast.makeText(storedPhrases.this, message, Toast.LENGTH_SHORT).show();
    }
	
	@SuppressLint("NewApi") private void readMessage_async(){
		try {
			while(mSocket!=null && mSocket.isConnected() && !endOfActivity){
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
					storedPhrases.this.runOnUiThread(new Runnable(){
						public void run(){
							scrollHandler(fs);
						}
					});
					
				}
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			storedPhrases.this.runOnUiThread(new Runnable(){
				public void run(){
					showToast(e.getLocalizedMessage()) ;
				}
			});
		}
		
	}
	
	private void scrollHandler(String s) {
		// TODO Auto-generated method stub
		if(s.equals("b")&&mode==1){
			scrollToNext();
			
		}else if(s.equals("B")||(s.equals("b")&&mode==0)){
			selCursor();
		}
	}
	
	private void scrollToNext(){
		//scrollCursorPos = (scrollCursorPos+1)%(list.size());
		scrollCursorPos = (scrollCursorPos+1)%(lv.getLastVisiblePosition()+1);
		highlightPos(scrollCursorPos);
	}

	private void highlightPos(int scrollCursorPos2) {
		// TODO Auto-generated method stub
		/*for(int i=0;i<list.size();i++){
			//lv.setSelection(i);
			//lv.getChildAt(i).setBackgroundColor(color.transparent);
		}*/
		int prev_pos = (lv.getLastVisiblePosition() + scrollCursorPos2)%(lv.getLastVisiblePosition()+1);
		while(lv.getLastVisiblePosition()<prev_pos){
			lv.setSelection(prev_pos);
		}
		lv.getChildAt(prev_pos).setBackgroundColor(Color.TRANSPARENT);
		
		while(lv.getLastVisiblePosition()<scrollCursorPos2){
			lv.setSelection(scrollCursorPos2);
		}
		lv.getChildAt(scrollCursorPos2).setBackgroundColor(Color.CYAN);
	}
	
	private void selCursor(){
		String s = list.get(scrollCursorPos).toString();
		if(s.equals("Return")){
			finish();
		}
		tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);		
	}
}
