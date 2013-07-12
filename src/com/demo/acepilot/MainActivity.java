package com.demo.acepilot;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

//the MainActivity of this proj.
public class MainActivity extends Activity {
	private Button btnStart,btnRestart;
	private ToggleButton btnPauseResume;
	private TextView tvShowCoin,tvStartCD;					//tvShowCoin, a textView to show how many coins did you get.
															//tvStartCD, a textView to make the count down-effection when the game starts.  
	private ImageView ivCoin;
	private MyGlSurfaceView myGlSurfaceView;
	private MyRender myRender; 
	private RelativeLayout gl_layout;
	private long timeStart;									//records the time start playing anytime.
	private double timeScore;								//records the score which is the whole time of playing.
	private DecimalFormat df;								//use to format timeScore.
	
	public static Handler myGameHandler;					//a handler to handle message,include game mode,...etc.
	public static int gameStatus;							//records the status of the game.
	public static int coinGet;								//the number of coins which is gotten during one game.
	public final static int TV_SHOWCOIN=2000;
	
	private final static int PROGRESSDIALOG_REVIVE=1000;	//give PROGRESSDIALOG_REVIVE a final value,then we can easily identify it.
	private ProgressDialog reviveProgressDialog;			//obj's reference of ProgressDialog class(����Ѧ�).
	private int progressCount;								//use to store the count value of progressbar at the moment. 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ABC","onCreate(Bundle savedInstanceState)...");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);	//�]�w���ù�
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);		//�]�w�ù�������

		setContentView(R.layout.activity_main);		
		findView();
		
		prepareGlSurfaceView();
		createMyGameHandler();	//initialize myGameHandler 
		setAllBtn();

	}
	
	//find�X����
	private void findView(){
		gl_layout=(RelativeLayout)findViewById(R.id.gl_layout);	//find�XframeLayout
		btnStart=(Button)findViewById(R.id.button1);
		btnRestart=(Button)findViewById(R.id.button2);
		btnPauseResume=(ToggleButton)findViewById(R.id.toggleButton1);
		ivCoin=(ImageView)findViewById(R.id.imageView1);
		tvShowCoin=(TextView)findViewById(R.id.textView1);
		tvShowCoin.setText("X"+coinGet);
		tvStartCD=(TextView)findViewById(R.id.textView2);
		tvStartCD.setText("");	//let tvStartCD become blank at first
		df=new DecimalFormat("0.000");	//initial df and set pattern
		reviveProgressDialog=new ProgressDialog(this);
	}
	
	//set all btn
	private void setAllBtn(){				
		MyOnClickListener myOnClickListener=new MyOnClickListener(); 
		btnStart.setOnClickListener(myOnClickListener);
		btnRestart.setOnClickListener(myOnClickListener);
		btnPauseResume.setOnCheckedChangeListener(myOnClickListener);
	}
	
	//prepare myGlSurfaceView
	private void prepareGlSurfaceView(){		
		myRender=new MyRender();		
		myRender.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.su_30_flanker),
				BitmapFactory.decodeResource(getResources(), R.drawable.normal_bullet),
				BitmapFactory.decodeResource(getResources(), R.drawable.star_coin));
		myGlSurfaceView=new MyGlSurfaceView(MainActivity.this);	//�إ�MyGlSurfaceView������					
		myGlSurfaceView.setRenderer(myRender);	//�]�wrender		
		gl_layout.addView(myGlSurfaceView);		//�NMyGlSurfaceView������[�Jgl_layout
		myGlSurfaceView.onPause();			
		btnStart.bringToFront();	//�N���s����̤W�h
		btnRestart.bringToFront();
		
		btnPauseResume.setVisibility(View.INVISIBLE);
		btnPauseResume.bringToFront();
		
		tvShowCoin.setVisibility(View.INVISIBLE);
		tvShowCoin.bringToFront();
		ivCoin.setVisibility(View.INVISIBLE);
		ivCoin.bringToFront();
		
		tvStartCD.bringToFront();
	}
	
	//create myGameHandler
	 private void createMyGameHandler(){
		myGameHandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch(msg.what){
				
				//GAME_READY
				case 0:
					break;
				
				//GAME_START (���Ustart��)
				case 1:		
					gameStatus=GameStatus.GAME_START.ordinal();	//refresh gameStatus					
					handleStrtNewGame(msg);						//call startGame()
					btnStart.setVisibility(View.INVISIBLE);		//hide btnStart 
					break;
					
				//GAME_PAUSE
				case 2:	
					gameStatus=GameStatus.GAME_PAUSE.ordinal();
					myGlSurfaceView.onPause();
					break;
				
				//GAME_RESUME (�~��C��)
				case 3:	
					gameStatus=GameStatus.GAME_RESUME.ordinal();
					MyRender.timePrevious=System.currentTimeMillis();
					myGlSurfaceView.onResume();
					break;	
				
				//GAME_HIT
				case 4:	
					gameStatus=GameStatus.GAME_HIT.ordinal();
					myGlSurfaceView.onPause();	//make myGlSurfaceView pause.
					//setMessage() must writes here because onCreateDialog(int id) is only called once.
					reviveProgressDialog.setMessage("�`�@�s��"+df.format(timeScore/1000)+"��");
					showDialog(PROGRESSDIALOG_REVIVE);					
					break;
				
				//GAME_REVIVE
				case 5:
					gameStatus=GameStatus.GAME_REVIVE.ordinal();
					MyRender.timePrevious=System.currentTimeMillis();
					myGlSurfaceView.onResume();				
					twinklePlane();
					break;
				
				//GAME_OVER
				case 6:
					gameStatus=GameStatus.GAME_OVER.ordinal();
					MainActivity.this.finish();	//*******
					break;
					
				//GAME_HIGHSCORE
				case 7:
					gameStatus=GameStatus.GAME_HIGHSCORE.ordinal();
					break;	
				
				//�w��progressDialog�B�z
				case PROGRESSDIALOG_REVIVE:
					reviveProgressDialog.setProgress(progressCount);
					if(progressCount == 0)
						reviveProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
					break;
					
				//�w��tvShowCoin�B�z
				case TV_SHOWCOIN:
					tvShowCoin.setText("X"+coinGet);
					break;
				}				
			}			
		};
	}
	
	class MyOnClickListener implements OnClickListener,CompoundButton.OnCheckedChangeListener{
		
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			//start game
			case R.id.button1:					
				sendStrNewGame();	//call sendStrNewGame()
				break;
			
			//���s�}�l
			case R.id.button2:	
				myGlSurfaceView.onPause();
				gl_layout.removeView(myGlSurfaceView);					
				prepareGlSurfaceView();
				myGlSurfaceView.onResume();
				break;
			}
		}
		
		//pause,resume's event.
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if(isChecked){
				myGameHandler.sendEmptyMessage(GameStatus.GAME_PAUSE.ordinal());
				timeScore += System.currentTimeMillis() - timeStart;
			}	
			else{
				myGameHandler.sendEmptyMessage(GameStatus.GAME_RESUME.ordinal());
				timeStart = System.currentTimeMillis();
			}
			isChecked = !isChecked;
		}
		
	}
		
//	private void openDialog(){
//		timeLive=(timeDie-timeStart)/1000.0;
//		new AlertDialog.Builder(MainActivity.this)
//		.setTitle("������")
//		.setMessage(df.format(timeLive)+"��Q�����F")
//		.setPositiveButton("����", new DialogInterface.OnClickListener(){
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				MainActivity.this.finish();
//			}
//			
//		})
//		.setNegativeButton("�~��", new DialogInterface.OnClickListener(){
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
////				Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("http://android.gasolin.idv.tw/"));
////				startActivity(intent);											
//				MyRender.timePrevious=System.currentTimeMillis();
//				myGlSurfaceView.onResume();				
////				createTestDieThread();								
//			}
//			
//		})
//		.show();
//	}

	
	//send the msg by starting new game
	private void sendStrNewGame(){
		new Thread(new Runnable() {
			Message msg;
			@Override
			public void run() {							
				for(int i=9;i>=0;i--){								
					try {
						if(i > 6)
							Thread.sleep(1000);	//show 3.2.1...wait 1s
						else
							Thread.sleep(500);	//show GO! wait 0.5s
					} catch (InterruptedException e) {									
						e.printStackTrace();
					}
					msg=new Message();
					msg.what=GameStatus.GAME_START.ordinal();
					msg.arg1=i;
					myGameHandler.sendMessage(msg);
				}
			}
		}).start();
	}
	
	//handle the msg by starting new game
	private void handleStrtNewGame(Message msg){
		if(msg.arg1 > 6){	
			if(msg.arg1 == 9){
//				myGlSurfaceView.onResume();		//�쥻�Q�n���������X�{�AGO!�}�l�{�{�ɤ~�ಾ�ʡA���o�˼g���i��
//				myGlSurfaceView.onPause();
			}							
			tvStartCD.setText(msg.arg1-6 + "");
		}
		else{						
			if(msg.arg1 == 6){
				myGlSurfaceView.onResume();
				createTestDieThread();	//createTestDieThread while gmae starts running
			}
			tvStartCD.setText("GO!");						//GO!�{�{3��
			if(msg.arg1 % 2 == 1)
				tvStartCD.setVisibility(View.INVISIBLE);
			else
				tvStartCD.setVisibility(View.VISIBLE);						
									
			if(msg.arg1 == 0){
				tvStartCD.setVisibility(View.GONE);
				btnPauseResume.setVisibility(View.VISIBLE);	//btnPauseResume appears
				ivCoin.setVisibility(View.VISIBLE);
				tvShowCoin.setVisibility(View.VISIBLE);
				timeStart=System.currentTimeMillis();	//get timeStart value
			}	
		}					
	}

	//create testDie's thread
	private void createTestDieThread(){
		MyRender.isDie = false;		//MyRender.isDie must be false before testDie.
		new Thread(new Runnable() {			
			@Override
			public void run() {
				while(true){							//����isDie��true�h�}�ҹ�ܤ���ø��X�j��
					if(MyRender.isDie == true){						 						
						myGameHandler.sendEmptyMessage(GameStatus.GAME_HIT.ordinal());						
						break;
					}
				}
				timeScore += System.currentTimeMillis() - timeStart;
			}
		}).start();
	}
	
	//�]�p�Q�����ɼu�X����ܮ�
	@Override
	protected Dialog onCreateDialog(int id) {
		DialogInterface.OnClickListener myListener=new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					myGameHandler.sendEmptyMessage(GameStatus.GAME_REVIVE.ordinal());
					timeStart = System.currentTimeMillis();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					myGameHandler.sendEmptyMessage(GameStatus.GAME_OVER.ordinal());
					break;	
				}				
			}			
		};
		
		switch (id) {
		//332~337 is only called once.
		case PROGRESSDIALOG_REVIVE:
			reviveProgressDialog.setMax(100);	//�]�̤j�Ȭ�100
			reviveProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);	//�]�w�˦�	
			reviveProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.revive), myListener);
			reviveProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.end), myListener);
			reviveProgressDialog.setCancelable(false);	//set dialog not to dismiss by touching other where on the screen. 
//			Log.d("ABC", "timeLive="+timeLive);
			break;
		}		
		return reviveProgressDialog;
	}
	
	//��Q�����ɼu�X����ܮت��ɵ{����
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case PROGRESSDIALOG_REVIVE:
			new Thread(new Runnable() {				
				@Override
				public void run() {
					reviveProgressDialog.setProgress(100);
					for(int i=100;i>=0;i--){
						if(gameStatus == GameStatus.GAME_REVIVE.ordinal())	//break this for loop when the gameStatus is GAME_REVIVE.
							break;
						progressCount=i;
						myGameHandler.sendEmptyMessage(PROGRESSDIALOG_REVIVE);
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}										
					}					
				}
			}).start();
			break;
		}		
	}
	
	//revive�ɭ����{�{�ĪG5sec
	private void twinklePlane(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=0;i<20;i++){
					MyRender.drawControlFlag = !MyRender.drawControlFlag;
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(i==9)
						MyRender.drawControlFlag=true;
				}
				createTestDieThread();	//TestDieThread() starts after 5secs.
			}
		}).start();		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d("ABC","onResume()...");
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d("ABC","onDestroy()...");
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("ABC","onPause()...");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("ABC","onStart()...");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d("ABC","onStop()...");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
