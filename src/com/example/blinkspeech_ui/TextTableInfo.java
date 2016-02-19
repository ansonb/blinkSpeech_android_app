package com.example.blinkspeech_ui;

import android.provider.BaseColumns;

public class TextTableInfo {
	public TextTableInfo(){
		
	}
	
	public static abstract class ttInfo implements BaseColumns{
		public static final String SAVEDPHRASE = "savedPhrase";
		public static final String TABLE_NAME = "TextTable";
		public static final String DATABASE_NAME = "TextDB";
	}
}
