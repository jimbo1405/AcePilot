package com.demo.acepilot;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.EditText;

public class DataBaseHelper extends SQLiteOpenHelper{
	
	private final static String DATABASE_NAME="AcePilot_DataBase";
	private final static int DATABASE_VERSION=1;
	private final static String TABLE_NAME="HighScore";
	private final static String TABLE_CREATE=
			"CREATE TABLE "+TABLE_NAME+"("+
			"id INTEGER PRIMARY KEY,"+
			"name TEXT,"+
			"score DOUBLE NOT NULL,"+
			"level TEXT NOT NULL);";
	
	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}
	
	//insert method.
	public long insertData(GameRecords gr){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues myContentValues = new ContentValues();
		myContentValues.put("name", gr.getName());
		myContentValues.put("score",gr.getScore());
		myContentValues.put("level",gr.getLevel());
		long num = db.insert(TABLE_NAME, null, myContentValues);
		db.close();
		return num;
	}
	
	//update method.
	public int updateData(EditText et, GameRecords gr){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues myContentValues = new ContentValues();
		myContentValues.put("name", et.getText().toString().replace(" ", ""));
		myContentValues.put("score",gr.getScore());
		myContentValues.put("level",gr.getLevel());
		String whereClause = "id=?";
		String[] whereArgs = {Integer.toString(gr.getId())};
		int num = db.update(TABLE_NAME, myContentValues, whereClause, whereArgs);
		db.close();
		return num;
	}
	
	//delete method.
	public int deleteData(GameRecords gr, int id){		
		SQLiteDatabase db=getWritableDatabase();
		String whereClause="id=?";
		String[] whereArgs={Integer.toString(id)};
		int num=db.delete(TABLE_NAME, whereClause, whereArgs);		
		db.close();
		return num;
	}
	
	//generate this table's cursor
	public Cursor getCursor(SQLiteDatabase db){
		String sql="SELECT * FROM "+TABLE_NAME+" ORDER BY score DESC;";
		Cursor c=db.rawQuery(sql, null);
		return c;
	}
	
	//generate GameRecordsList.
	public List<GameRecords> genGameRecordsList(){
		List<GameRecords> gameRecordsList = new ArrayList<GameRecords>();
		SQLiteDatabase db=this.getReadableDatabase();
		Cursor myCursor=getCursor(db);
		while(myCursor.moveToNext()){			
			GameRecords tmpGR=new GameRecords();
			tmpGR.setId(myCursor.getInt(0));
			tmpGR.setName(myCursor.getString(1));
			tmpGR.setScore(myCursor.getDouble(2));
			tmpGR.setLevel(myCursor.getString(3));
			gameRecordsList.add(tmpGR);
		}
		return gameRecordsList;
	}
		
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
