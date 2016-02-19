package com.example.blinkspeech_ui;

import com.example.blinkspeech_ui.TextTableInfo.ttInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.SyncStateContract.Columns;
import android.util.Log;



public class DatabaseOperations extends SQLiteOpenHelper{

	public static final int databaseVersion=1;
	public String QUERRY="CREATE TABLE "+ttInfo.TABLE_NAME+"("
			+ttInfo.SAVEDPHRASE+" TEXT);";
	
	public DatabaseOperations(Context context) {
		super(context, ttInfo.DATABASE_NAME, null, databaseVersion);
		Log.d("Database Operations ", "Database Created");
	}
	
	@Override
	public void onCreate(SQLiteDatabase sdb)
	{
		Log.d("Database OPerations ", "Creating Table");
		sdb.execSQL(QUERRY);
		Log.d("Database OPerations ", "Table Created");
	}
	
	public void putInfo(DatabaseOperations dob, String text)
	{
		Log.d("Database OPerations ", "Putting info");
		SQLiteDatabase SQ=dob.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(ttInfo.SAVEDPHRASE, text);
		SQ.insert(ttInfo.TABLE_NAME, null, cv);
		Log.d("Database Operator", "Row Inserted");
	}
	
	public Cursor getInfo(DatabaseOperations dob)
	{
		Log.d("Database OPerations ", "Getting info");
		SQLiteDatabase SQ = dob.getReadableDatabase();
		Log.d("DbOps", "Got Readable db");
		String Columns[]={ttInfo.SAVEDPHRASE};
		Cursor CR = SQ.query(ttInfo.TABLE_NAME, Columns,
				null, null, null, null, null, null);
		Log.d("DbOps", "returning CR");
		return CR;
	}

	 public void delete(DatabaseOperations DOP, String text)
	 {
	  String selection = ttInfo.SAVEDPHRASE+ " LIKE ?";
	  String args[] = {text};
	  SQLiteDatabase SQ = DOP.getWritableDatabase();
	  SQ.delete(ttInfo.TABLE_NAME, selection, args);
	  
	 }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
