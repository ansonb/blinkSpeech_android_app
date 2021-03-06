package com.example.blinkspeech_ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import com.example.blinkspeech_ui.MainActivity.cursorPos;
import com.example.blinkspeech_ui.MainActivity.scrollOption;

import android.R.string;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Selection;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class subGroup extends ActionBarActivity {

	boolean endOfActivity = false;
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
	
	BluetoothSocket mSocket = MainActivity.mSocket;
	
	Thread mHandler = new Thread(new Runnable(){
	   @Override	
 	   public void run(){
 			   readMessage_async();
 			   /*if(subGroup.messageReceived){
 				   subGroup.this.runOnUiThread(new Runnable(){
 					   public void run(){
 						  showToast(mReceived + "Message Received");
 		     			  subGroup.messageReceived = false;
 		     			   //scrollHandler(mReceived); 
 					   }
 				   });
 				   
     		   }*/
 	   }
    });
	
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
	
	public static boolean messageReceived = false;
	public static String mReceived = "x";
	
	int width, height;
	
	Thread check_time;
	Thread check_click;
	
	TextToSpeech tts;
	
	private char keyBoard[][] = 
		{
			{'.','A',',','B','<','C',' ','1','$'},
			{'.','D',',','E','<','F',' ','2','$'},
			{'.','G',',','H','<','I',' ','3','$'},
			{'.','J',',','K','<','L',' ','4','$'},
			{'.','M',',','N','<','O',' ','5','$'},
			{'.','P',',','Q','<','R',' ','6','$'},
			{'.','S',',','T','<','U',' ','7','$'},
			{'.','V',',','W','<','X',' ','8','$'},
			{'.','Y',',','Z','<','0',' ','9','$'},
		};
	
	int group=0;
	int subgroup=0;
	
	public subGroup(){
		
	}
	
	@SuppressLint("NewApi") @Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Bundle b = getIntent().getExtras();
        group = b.getInt("Group");
        
        rowPos = cursorPos.FIRST;
        colPos = cursorPos.FIRST;
        
        
        mTV = (TextView) findViewById(R.id.textView1);
        mButton = (Button) findViewById(R.id.button1);
        
        mTV.setText(MainActivity.mString);
        
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
        
        mIV1.setWidth(width/3);
        mIV2.setWidth(width/3);
        mIV3.setWidth(width/3);
        mIV4.setWidth(width/3);
        mIV5.setWidth(width/3);
        mIV6.setWidth(width/3);
        mIV7.setWidth(width/3);
        mIV8.setWidth(width/3);
        mIV9.setWidth(width/3);
        
        mIV1.setHeight(height/3);
        mIV2.setHeight(height/3);
        mIV3.setHeight(height/3);
        mIV4.setHeight(height/3);
        mIV5.setHeight(height/3);
        mIV6.setHeight(height/3);
        mIV7.setHeight(height/3);
        mIV8.setHeight(height/3);
        mIV9.setHeight(height/3);
        
        mTV.setWidth(width-90);
        
        setSubGroupText();
        
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
				// TODO Auto-generated method stub
			    	String mString = mTV.getText().toString();
			    	tts.speak(mString, TextToSpeech.QUEUE_FLUSH, null);
			}
		});
       
       if(mSocket.isConnected()){
    	   mHandler.start();
       }
       
      /*check_click = new Thread(new Runnable(){
       	@Override
       	public void run(){
       	    while(true){
       	    	if(subgroup!=0){
       	    		insertChar(group, subgroup);
       	    		subgroup = 0;
       	    	}
       	    }
       	}
       });
       
       check_click.start();
       
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
       				subGroup.this.runOnUiThread(new Runnable(){
       					public void run(){

       					}
       				});
       				delta=0;
       				prevTime = currTime;
       			}
       		}
       	}
       });
       check_time.start();*/
        
       showCursor();
	}
	
	@SuppressLint("NewApi") private void setSubGroupText(){
		/*Bitmap back = BitmapFactory.decodeResource(getResources(), R.drawable.back);
		back = Bitmap.createScaledBitmap(back, 25, 15, false);
		
		Bitmap spacebar = BitmapFactory.decodeResource(getResources(), R.drawable.space);
		spacebar = Bitmap.createScaledBitmap(spacebar, 25, 15, false);
		
		Bitmap delete = BitmapFactory.decodeResource(getResources(), R.drawable.delete);
		delete = Bitmap.createScaledBitmap(delete, 25, 15, false);*/
		
		mIV1.setText(Character.toString(keyBoard[group-1][0]));
		mIV2.setText(Character.toString(keyBoard[group-1][1]));
		mIV3.setText(Character.toString(keyBoard[group-1][2]));
		mIV4.setText(Character.toString(keyBoard[group-1][3]));
		mIV5.setText("BACK");
		mIV6.setText(Character.toString(keyBoard[group-1][5]));
		mIV7.setText("SPACE");
		mIV8.setText(Character.toString(keyBoard[group-1][7]));
		mIV9.setText("BACKSPACE");
		
		/*mIV5.setBackground(new BitmapDrawable(getResources(), back));
		mIV7.setBackground(new BitmapDrawable(getResources(), spacebar));
		mIV9.setBackground(new BitmapDrawable(getResources(), delete));*/
		
		/*mIV5.setBackgroundResource(R.drawable.back);
		mIV7.setBackgroundResource(R.drawable.space);
		mIV9.setBackgroundResource(R.drawable.delete);*/
	}
	
	private void insertChar(int gr, int subGr){
		MainActivity.mString = mTV.getText().toString();
		if(subGr==5){
			finish();
		}
		else if(subGr==9){
			String sTmp = MainActivity.mString;
			String s = "";
			if(sTmp.length()>0){
				s = String.copyValueOf(sTmp.toCharArray(), 0, sTmp.length()-1);
			}
			
			MainActivity.mString = s;
			setText(mTV, MainActivity.mString);
		}
		else{
			MainActivity.mString += keyBoard[gr-1][subGr-1];
			setText(mTV, MainActivity.mString);
		}
	}
	
	private void setText(View v, final String s){
		subGroup.this.runOnUiThread(new Runnable(){
			public void run(){
				mTV.setText(s);
				Editable etext = (Editable) mTV.getText();
				Selection.setSelection(etext, s.length());
				
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	@Override
    public void onPause(){
        /*if(tts !=null){
           tts.stop();
           tts.shutdown();
        }*/
       
        super.onPause();
     }
	
	@Override
    public void onRestart(){
    	super.onRestart();
    }
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		endOfActivity = true;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
    
    private void handleClick(){
    	insertChar(group, subgroup);
   		subgroup = 0;
    }
    
    public void gr1_click(View v){
    	subgroup = 1;
   		handleClick();
    }
    
    public void gr2_click(View v){
    	subgroup = 2;
   		handleClick();
    }
    
    public void gr3_click(View v){
    	subgroup = 3;
   		handleClick();
    }
    
    public void gr4_click(View v){
    	subgroup = 4;
   		handleClick();
    }
    
    public void gr5_click(View v){
    	subgroup = 5;
   		handleClick();
    }
    
    public void gr6_click(View v){
    	subgroup = 6;
   		handleClick();
    }
    
    public void gr7_click(View v){
    	subgroup = 7;
   		handleClick();
    }
    
    public void gr8_click(View v){
    	subgroup = 8;
   		handleClick();
    }
    
    public void gr9_click(View v){
    	subgroup = 9;
   		handleClick();
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
			subgroup = findSubGroup();
			if(subgroup!=0){
				handleClick();
			}
		}else if(s.equals("a")){
			
		}
	}
	
	private int findSubGroup(){
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
	
	private void showToast(String message){
    	Toast.makeText(subGroup.this, message, Toast.LENGTH_SHORT).show();
    }
	
	@SuppressLint("NewApi") private void readMessage_async(){
		try {
			while(mSocket!=null && mSocket.isConnected() && !endOfActivity){
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
					subGroup.this.runOnUiThread(new Runnable(){
						public void run(){
							//TODO
							scrollHandler(fs);
						}
					});
					
				}
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			subGroup.this.runOnUiThread(new Runnable(){
				public void run(){
					showToast(e.getLocalizedMessage()) ;
				}
			});
		}
		
	}
}
