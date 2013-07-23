package com.demo.acepilot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

//this activity binds with high_score_layout.
public class HighScoreActivity extends Activity{
	
	private Button btnBackMain;
	private ListView myListView;
	private ImageView iv;
	private MyOnClickListener myOnClickListener;
	private DataBaseHelper myDataBaseHelper;
	private List<GameRecords> myGameRecordsList;
	private GameRecords currGameRecords;
	private SimpleAdapter mySimpleAdapter;
	/*why to use waitLViewHandler? 
	 * because we can only call getChildAt(int pos) method after the myListView is ready,
	 * so we try to wait myListView for 500ms,or will return null and get NullPointerException.*/ 	  
	private Handler waitLVHandler;						//a handler handles the msg which is sended from calling requestInputName().	
	private final static int WAIT_FOR_LISTVIEW = 100;	//use for waitLViewHandler's case. 
	private EditText etOnItem;							//an editText which is at the specified position of item of myListView.
	private boolean beatRecordFlag = false;				//a flag represents whether get top 5.		
	private InputMethodManager imManager;				//an obj to  control the soft keyboard.	
	private boolean openSoftKeyboardFlag = true;
	private boolean etOnItemReadyFlag = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);	//set full screen.
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);											//set protrait screen.
		setContentView(R.layout.high_score_layout);
		findView();
		
		imManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		initWaitLViewHandler();							//initial handler.
		
		if(myDataBaseHelper == null)					//initial myDataBaseHelper.
			myDataBaseHelper=new DataBaseHelper(HighScoreActivity.this);
		
		currGameRecords = getCurrGameRecords();			//get current GameRecords obj. 
		updateHighScore(currGameRecords);				//update database.
		showListView();									//show the listView.
		if(beatRecordFlag == true){						//if it gets top 5, request the focus of editText at the specified item of myListView. 
			requestInputName();						
		}
		
		btnBackMain.setOnClickListener(myOnClickListener);
	}
	
	private void findView(){
		btnBackMain=(Button)findViewById(R.id.button1);
		myListView=(ListView)findViewById(R.id.listView1);		
		iv=(ImageView)findViewById(R.id.imageView1);
		myOnClickListener = new MyOnClickListener();
	}
	
	private class MyOnClickListener implements View.OnClickListener{
		@Override
		public void onClick(View v) {			
			switch(v.getId()){
			case R.id.imageView1:
				Log.d("jimbo","imageView onClick...");
				etOnItem.clearFocus();
				break;
				
			case R.id.button1:
				if(beatRecordFlag == true && etOnItemReadyFlag == true){	
					GameRecords tmpGR = myGameRecordsList.get(getCurrPosition());	//get current GameRecords obj. 
					int n = myDataBaseHelper.updateData(etOnItem, tmpGR);			//update HighScore set name='xxx' where id=?;
					if(n > 0){
						Toast.makeText(HighScoreActivity.this, "name saved...", Toast.LENGTH_LONG).show();
					}						
				}
				Intent myIntent = new Intent();
				myIntent.setClass(HighScoreActivity.this, MainActivity.class);
//				myIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //注意本行的FLAG设置
				startActivity(myIntent);				
				break;
			}			
		}		
	}
	
	private class MyOnFocusChangeListener implements View.OnFocusChangeListener{
		@Override
		public void onFocusChange(View v, boolean hasFocus) {			
			switch(v.getId()){
			case R.id.imageView1:
				if(hasFocus){
					imManager.hideSoftInputFromWindow(etOnItem.getWindowToken(), 0);
					GameRecords tmpGR = myGameRecordsList.get(getCurrPosition());	//get current GameRecords obj. 
					int n = myDataBaseHelper.updateData(etOnItem, tmpGR);			//update HighScore set name='xxx' where id=?;
					if(n > 0){
//						Toast.makeText(HighScoreActivity.this, "name saved...", Toast.LENGTH_LONG).show();
					}	
				}
				break;
				
			case R.id.editText1:
				if(hasFocus && openSoftKeyboardFlag){								
					imManager.showSoftInput(etOnItem, InputMethodManager.SHOW_IMPLICIT);
					openSoftKeyboardFlag = false;
				}
				break;
			}			
		}				
	}
	
	//generate a current GameRecords obj.
	private GameRecords getCurrGameRecords(){
		double score=this.getIntent().getDoubleExtra("score", 0);
		String level=this.getIntent().getStringExtra("level");
		currGameRecords = new GameRecords();
		currGameRecords.setScore(score);
		currGameRecords.setLevel(level);
		return currGameRecords;
	}
	
	//update the table "HighScore" in database.
	private void updateHighScore(GameRecords currentGR){		
		myGameRecordsList=myDataBaseHelper.genGameRecordsList();
		int size = myGameRecordsList.size(); 
		
		if(size < 5){										//if myGameRecordsList.size() < 5, then insert directly. 
			myDataBaseHelper.insertData(currentGR);
			beatRecordFlag = true;							//set beatRecordFlag be true.
		}else{												//if myGameRecordsList.size() == 5, and also if current score > 5th score in database,then delete before insert.
			if(currentGR.getScore() >= myGameRecordsList.get(4).getScore()){
				myDataBaseHelper.deleteData(myGameRecordsList.get(4), myGameRecordsList.get(4).getId());
				myDataBaseHelper.insertData(currentGR);
				beatRecordFlag = true;						//set beatRecordFlag be true. 
			}
		}
	}
	
	//display the highscore content
	private void showListView(){
		List<Map<String,Object>> myList=new ArrayList<Map<String,Object>>();
		myGameRecordsList = myDataBaseHelper.genGameRecordsList();
		String[] fromArray = {"key1","key2","key3","key4"};
		int[] toArray = {R.id.textView1, R.id.editText1 , R.id.textView2, R.id.imageView1};
		String[] rankArray=getResources().getStringArray(R.array.rank);
		DecimalFormat df = new DecimalFormat("0.000");
		
		for(int i=0;i<myGameRecordsList.size();i++){			
			Map<String,Object> myMap=new HashMap<String,Object>();
			GameRecords tmpGR=myGameRecordsList.get(i);
			myMap.put("key1", rankArray[i]);
			myMap.put("key2", tmpGR.getName());
			myMap.put("key3", df.format(tmpGR.getScore()));
			myMap.put("key4", getLevelDrawID(tmpGR));
			myList.add(myMap);
		}
		mySimpleAdapter=new SimpleAdapter(HighScoreActivity.this, myList, R.layout.my_listview_layout, fromArray, toArray);
		myListView.setAdapter(mySimpleAdapter);
	}
	
	//a method to decide which *.png of lvl to show.
	private int getLevelDrawID(GameRecords currentGR){
		String lvl=currentGR.getLevel();
		
		if(lvl.equals("1")){
			return R.drawable.lvl_01;
		}else if(lvl.equals("2")){
			return R.drawable.lvl_02;
		}else if(lvl.equals("3")){
			return R.drawable.lvl_03;
		}else if(lvl.equals("4")){
			return R.drawable.lvl_04;
		}else if(lvl.equals("5")){
			return R.drawable.lvl_05;
		}else if(lvl.equals("6")){
			return R.drawable.lvl_06;
		}else if(lvl.equals("7")){
			return R.drawable.lvl_07;
		}else if(lvl.equals("8")){
			return R.drawable.lvl_08;
		}else if(lvl.equals("9")){
			return R.drawable.lvl_09;
		}else if(lvl.equals("10")){
			return R.drawable.lvl_10;
		}else if(lvl.equals("11")){
			return R.drawable.lvl_11;
		}else
			return R.drawable.lvl_12;
	}
	
	//get current position of item of myListView.
	private int getCurrPosition(){
		//find max id, because it means it's the last inserted.
		int idMax = 0;
		for(int i=0;i<myGameRecordsList.size();i++){
			GameRecords tmpGR = myGameRecordsList.get(i);			
			if(tmpGR.getId() > idMax)
				idMax = tmpGR.getId();
		}
		
		int position = 0;
		for(int i=0;i<myGameRecordsList.size();i++){
			if(myGameRecordsList.get(i).getId() == idMax)
				position = i;
		}		
		return position;
	}
	
	//send msg to handler for setting editText to requestFocus() to input name.
	private void requestInputName(){		
		new Thread(new Runnable() {			
			@Override
			public void run() { 
				try {
					Thread.sleep(500);					
					waitLVHandler.sendEmptyMessage(WAIT_FOR_LISTVIEW);
				} catch (InterruptedException e) { 
					e.printStackTrace();
				}				
			}
		}).start();
	}
	
	//initial a handler to handle the msg for setting editText to requestFocus() to input name.
	private void initWaitLViewHandler(){
		waitLVHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch(msg.what){
				case WAIT_FOR_LISTVIEW:
					View view = myListView.getChildAt(getCurrPosition());					
					etOnItem = (EditText)view.findViewById(R.id.editText1);										
					etOnItem.setEnabled(true);
					
					etOnItem.clearFocus();
					iv.clearFocus();
					
					MyOnFocusChangeListener myOnFocusChangeListener = new MyOnFocusChangeListener();					
					etOnItem.setOnFocusChangeListener(myOnFocusChangeListener);
					iv.setOnFocusChangeListener(myOnFocusChangeListener);
					
					iv.setOnClickListener(myOnClickListener);
					etOnItem.requestFocus();
					
					etOnItemReadyFlag = true;
					break;
				}
			}			
		};
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//lock key_BACK.
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		//lock key_MENU.
		}else if(keyCode == KeyEvent.KEYCODE_MENU){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(myDataBaseHelper != null)
			myDataBaseHelper.close();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
}
