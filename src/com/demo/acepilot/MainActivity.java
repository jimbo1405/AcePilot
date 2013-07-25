package com.demo.acepilot;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

//the MainActivity of this proj.
public class MainActivity extends Activity {
	private AudioManager audioManager;
	private boolean SoundEnabled;
	private int currVolumeIndex;
	public static SoundPool sp;
	private int clickSound1,explosionSound,banSound;
	public static int coinSound;
	private MediaPlayer mpMainMenu;
	private MediaPlayer mpPlaying;
	
	private Button btnStart,btnQuit,btnHighScore,btnRestart;
	private ToggleButton btnPauseResume;
	private TextView tvShowCoin,tvStartCD;					//tvShowCoin, a textView to show how many coins did you get.
															//tvStartCD, a textView to make the count down-effection when the game starts.  
	private ImageView ivCoin,ivSound;
	private MyGlSurfaceView myGlSurfaceView;
	private MyRender myRender; 
	private RelativeLayout gl_layout;
	private long timeStart;									//records the time start playing anytime.
	private double timeScore;								//records the score which is the whole time of playing.
	private String timeScoreInSec;							//
	private DecimalFormat df;								//use to format timeScore.
	
	public static MyGameHandler myGameHandler;				//a handler to handle message,include game mode,...etc.
	public static int gameStatus;							//records the status of the game.
	
	public static int coinGet;								//the number of coins which is gotten during one game.
	private static int initCoinNeed;						//coin need at first revive.
	public static int totalCoin;							//the total number of coins user owns.
	
	private static int reviveCount;							//the number of the revive count.
	
	public final static int TV_SHOWCOIN=2000;
	private final static int PROGRESSDIALOG_REVIVE=1000;	//give PROGRESSDIALOG_REVIVE a final value,then we can easily identify it.
	private Dialog reviveDialog;							//obj's reference of Dialog class.
	private Button btnOkOnDialog, btnNoOnDialog;
	private ProgressBar proBarOnDialog;
	private int progressCount;								//use to store the count value of progressbar at the moment.
	
	private final static String PREF="PrefSettings";
	private final static String PREF_TOTAL_COIN="ToatalCoin";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("jimbo","onCreate()...");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);	//設定全螢幕
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);		//設定螢幕為垂直
				
		initSoundResource();	//init sound source.
		setContentView(R.layout.activity_main);	
		
		restorePerf();		//get SharedPreferences.
		findView();
				
		prepareGlSurfaceView();
		createMyGameHandler();	//initialize myGameHandler 
		setAllBtn();

	}
	
	//find view.
	private void findView(){
		gl_layout=(RelativeLayout)findViewById(R.id.gl_layout);	//find出frameLayout
		btnStart=(Button)findViewById(R.id.button1);
		btnQuit=(Button)findViewById(R.id.button3);
		btnHighScore=(Button)findViewById(R.id.button4);
		btnRestart=(Button)findViewById(R.id.button2);
		btnPauseResume=(ToggleButton)findViewById(R.id.toggleButton1);
		ivCoin=(ImageView)findViewById(R.id.imageView1);
		ivSound=(ImageView)findViewById(R.id.imageView2);
		
        if (SoundEnabled) {
        	ivSound.setImageResource(R.drawable.soundopen40x40);        	
        } else {
        	ivSound.setImageResource(R.drawable.soundclose40x40);       	
        }
		
		tvShowCoin=(TextView)findViewById(R.id.textView1);
		tvShowCoin.setText("X"+totalCoin);
		tvStartCD=(TextView)findViewById(R.id.textView2);
		tvStartCD.setText("");	//let tvStartCD become blank at first
		
		df=new DecimalFormat("0.000");	//initial df and set pattern
		prepareReviveDialog();
		
		initCoinNeed = 5;
		reviveCount = 0;
		coinGet = 0;
	}
	
	//init sound source.
	private void initSoundResource(){
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		
		if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0)
			SoundEnabled = true;
		else
			SoundEnabled = false;
			
		sp = new SoundPool(100, AudioManager.STREAM_MUSIC, 0);
		clickSound1 = sp.load(MainActivity.this, R.raw.cameraflash, 1);
		explosionSound = sp.load(MainActivity.this, R.raw.explode3, 1);
		coinSound = sp.load(MainActivity.this, R.raw.money, 1);
		banSound = sp.load(MainActivity.this, R.raw.nono, 1);
		
		mpMainMenu = MediaPlayer.create(MainActivity.this, R.raw.superheros1);					
		mpMainMenu.setLooping(true);
		mpPlaying =  MediaPlayer.create(MainActivity.this, R.raw.superheros2);	
		mpPlaying.setLooping(true);
	}
	
	//set all btn's onClickListener.
	private void setAllBtn(){				
		MyButtonListener myButtonListener=new MyButtonListener(); 
		btnStart.setOnClickListener(myButtonListener);
		btnQuit.setOnClickListener(myButtonListener);
		btnHighScore.setOnClickListener(myButtonListener);
		btnRestart.setOnClickListener(myButtonListener);
		btnPauseResume.setOnCheckedChangeListener(myButtonListener);
		ivSound.setOnClickListener(myButtonListener);
	}
	
	//prepare myGlSurfaceView
	private void prepareGlSurfaceView(){		
		myRender=new MyRender();		
		myRender.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.su_30_flanker),
				BitmapFactory.decodeResource(getResources(), R.drawable.normal_bullet),
				BitmapFactory.decodeResource(getResources(), R.drawable.coin32x32));
		myGlSurfaceView=new MyGlSurfaceView(MainActivity.this);	//建立MyGlSurfaceView的物件					
		myGlSurfaceView.setRenderer(myRender);	//設定render		
		gl_layout.addView(myGlSurfaceView);		//將MyGlSurfaceView的物件加入gl_layout
		myGlSurfaceView.onPause();			
		//let views to be invisible.
		setViewInVisible(btnStart,btnQuit,btnHighScore,btnPauseResume,tvShowCoin,ivCoin,ivSound);	
		//let views bring to front.		
		setViewsToFront(btnStart,btnQuit,btnHighScore,tvStartCD,ivSound);
		//set view's showing animation.
		setShowAnimation(btnStart,btnQuit,btnHighScore,btnPauseResume,tvShowCoin,ivCoin,ivSound);
		//set views to be visible.
		setViewVisible(btnStart,btnQuit,btnHighScore,ivSound);
				
//		btnRestart.bringToFront();
	}
	
	public static class MyGameHandler extends Handler {
	    private final WeakReference<MainActivity> mActivity;
	 
	    public MyGameHandler(MainActivity activity) {
	      mActivity = new WeakReference<MainActivity>(activity);
	    }
	 
	    @Override
	    public void handleMessage(Message msg) {
	      MainActivity activity = mActivity.get();
	      if (activity != null) {
				switch(msg.what){
				
				//GAME_READY
				case 0:
					break;
				
				//GAME_START
				case 1:		
					gameStatus=GameStatus.GAME_START.ordinal();				//refresh gameStatus.					
					activity.handleStrtNewGame(msg);						//call startGame().					
					break;
					
				//GAME_PAUSE
				case 2:	
					gameStatus=GameStatus.GAME_PAUSE.ordinal();
					activity.myGlSurfaceView.onPause();
					break;
				
				//GAME_RESUME
				case 3:	
					gameStatus=GameStatus.GAME_RESUME.ordinal();
					MyRender.timePrevious=System.currentTimeMillis();
					activity.myGlSurfaceView.onResume();
					break;	
				
				//GAME_HIT
				case 4:	
					gameStatus=GameStatus.GAME_HIT.ordinal();
					activity.myGlSurfaceView.onPause();	//make myGlSurfaceView pause.
					//setMessage() must writes here because onCreateDialog(int id) is only called once.
					activity.timeScoreInSec=activity.df.format(activity.timeScore/1000);
					TextView tvSec= (TextView)activity.reviveDialog.findViewById(R.id.text_sec);					
					tvSec.setText(activity.timeScoreInSec+" "+activity.getResources().getString(R.string.sec));
					TextView tvCoinNeed= (TextView)activity.reviveDialog.findViewById(R.id.textView3);
					tvCoinNeed.setText(MyConstant.calCurrCoinNeed(initCoinNeed, reviveCount)+"");
					activity.reviveDialog.show();
					activity.reviveDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);	//must be called after show();				
					activity.controlReviveDialog();					
					break;
				
				//GAME_REVIVE
				case 5:
					gameStatus=GameStatus.GAME_REVIVE.ordinal();
					totalCoin -= MyConstant.calCurrCoinNeed(initCoinNeed, reviveCount);	//coinGet-coinNeed.
					this.sendEmptyMessage(TV_SHOWCOIN);		//update the textView which is showing coinGet.
					reviveCount++;							//reviveCount+1.
					MyRender.timePrevious=System.currentTimeMillis();
					activity.myGlSurfaceView.onResume();				
					activity.twinklePlane();
					break;
				
				//GAME_OVER
				case 6:
					gameStatus=GameStatus.GAME_OVER.ordinal();					
					Intent myIntent=new Intent();
					myIntent.setClass(activity, GameResultActivity.class);
					myIntent.putExtra("timeScoreInSec",activity.timeScoreInSec);
					myIntent.putExtra("coinGet",coinGet);
					myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //注意本行的FLAG设置
					activity.startActivity(myIntent);					
					break;
					
				//GAME_HIGHSCORE
				case 7:
					gameStatus=GameStatus.GAME_HIGHSCORE.ordinal();
					Intent intent = new Intent(activity, HighScoreActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					HighScoreActivity.updateFlag = false;	//updateFlag must be false when go to highscore page from main page directly.
					activity.startActivity(intent);
					break;	
				
				//handle for progressDialog.
				case PROGRESSDIALOG_REVIVE:
					activity.proBarOnDialog.setProgress(activity.progressCount);
					if(activity.progressCount == 0){
						activity.setHideAnimation(activity.btnOkOnDialog);
						activity.setViewInVisible(activity.btnOkOnDialog);
					}	
					break;
					
				//handle for tvShowCoin.
				case TV_SHOWCOIN:
					activity.tvShowCoin.setText("X"+totalCoin);
					break;
					
					
				}	
	      }
	    }
	  }
	
	//create myGameHandler
	 private void createMyGameHandler(){
		myGameHandler=new MyGameHandler(MainActivity.this);
	}
	
	//
	class MyButtonListener implements OnClickListener,CompoundButton.OnCheckedChangeListener{		
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			//btnStart
			case R.id.button1:					
				sp.play(clickSound1, 1, 1, 0, 0, 1);
				sendStrNewGame();									//call sendStrNewGame().
				setHideAnimation(btnStart,btnQuit,btnHighScore);	//set view's hidding animation.
				setViewGone(btnStart,btnQuit,btnHighScore);			//set views to be gone.
				
				if(mpMainMenu != null){
					if(mpMainMenu.isPlaying())
						mpMainMenu.stop();
					mpMainMenu.release();
					mpMainMenu = null;
				}
				
				mpPlaying.start();
				break;
			
			//btnRestart
			case R.id.button2:	
				myGlSurfaceView.onPause();
				gl_layout.removeView(myGlSurfaceView);					
				prepareGlSurfaceView();
				myGlSurfaceView.onResume();
				break;
			
			//btnQuit
			case R.id.button3:
				sp.play(clickSound1, 1, 1, 0, 0, 1);
				MainActivity.this.finish();	//end of the activity.
				break;
			
			//btnHighScore
			case R.id.button4:
				sp.play(clickSound1, 1, 1, 0, 0, 1);
				myGameHandler.sendEmptyMessage(GameStatus.GAME_HIGHSCORE.ordinal());
				break;
				
			//ivSound
			case R.id.imageView2:								
				if (SoundEnabled) {             
					currVolumeIndex = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					ivSound.setImageResource(R.drawable.soundclose40x40);
                	audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);                   
                } else {
                	ivSound.setImageResource(R.drawable.soundopen40x40);
                	audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currVolumeIndex, 0);                	
                }
				SoundEnabled = !SoundEnabled;
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
			
	//send the msg by starting new game
	private void sendStrNewGame(){
		new Thread(new Runnable() {
			Message msg;
			@Override
			public void run() {							
				for(int i=12;i>=0;i--){								
					try {
						if(i > 6)
							Thread.sleep(500);	//show 3.2.1...delay 1s.
						else
							Thread.sleep(350);	//show GO!delay 0.5s.
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
			if(msg.arg1 == 12){
//				myGlSurfaceView.onResume();		//原本想要讓飛機先出現，GO!開始閃爍時才能移動，但這樣寫不可行
//				myGlSurfaceView.onPause();
			}
			
			if(msg.arg1 % 2 == 1){
				setHideAnimation(tvStartCD);
				setViewInVisible(tvStartCD);
			}else{				
				tvStartCD.setText((msg.arg1-6)/2 + "");				
				setShowAnimation(tvStartCD);
				setViewVisible(tvStartCD);					
			}																				
		}else{						
			if(msg.arg1 == 6){				
				myGlSurfaceView.onResume();
				createTestDieThread();			//createTestDieThread while gmae starts running
			}
			
			if(msg.arg1 % 2 == 1){
				setHideAnimation(tvStartCD);
				setViewInVisible(tvStartCD);
			}else if(msg.arg1 != 0){				
				tvStartCD.setText("GO!");
				setShowAnimation(tvStartCD);
				setViewVisible(tvStartCD);			
			}else{
				setViewGone(tvStartCD);
				setViewsToFront(btnPauseResume,ivCoin,tvShowCoin);	//let views bring to front.
				setViewVisible(btnPauseResume,ivCoin,tvShowCoin);	//set views to be visible.			
				timeStart=System.currentTimeMillis();				//get timeStart value
			}	
		}					
	}

	//create testDie's thread
	private void createTestDieThread(){
		MyRender.isDie = false;		//MyRender.isDie must be false before testDie.
		new Thread(new Runnable() {			
			@Override
			public void run() {
				while(true){							//直到isDie為true則開啟對話方塊並跳出迴圈
					if(MyRender.isDie == true){						 						
						sp.play(explosionSound, 1, 1, 0, 0, 1);
						myGameHandler.sendEmptyMessage(GameStatus.GAME_HIT.ordinal());						
						break;
					}
				}
				timeScore += System.currentTimeMillis() - timeStart;
			}
		}).start();
	}
	
	//設計被擊中時彈出的對話框
	private void prepareReviveDialog(){
		View.OnClickListener myListener=new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				//btnOk
				case R.id.button1:
					if(totalCoin >= MyConstant.calCurrCoinNeed(initCoinNeed, reviveCount)){
						sp.play(clickSound1, 1, 1, 0, 0, 1);
						myGameHandler.sendEmptyMessage(GameStatus.GAME_REVIVE.ordinal());
						reviveDialog.cancel();	//close dialog.
						timeStart = System.currentTimeMillis();
					}else{
						sp.play(banSound, 1, 1, 0, 0, 1);
					}					
					break;
				//btnNo
				case R.id.button2:
					sp.play(clickSound1, 1, 1, 0, 0, 1);
					myGameHandler.sendEmptyMessage(GameStatus.GAME_OVER.ordinal());
					break;	
				}				
			}			
		};
		reviveDialog = new Dialog(MainActivity.this, R.style.TANCStyle);
		reviveDialog.setContentView(R.layout.my_progressdialog_layout);		
		
		proBarOnDialog = (ProgressBar)reviveDialog.findViewById(R.id.progressBar1);
		proBarOnDialog.setMax(100);	//設最大值為100
			
		btnOkOnDialog = (Button)reviveDialog.findViewById(R.id.button1);
		btnNoOnDialog = (Button)reviveDialog.findViewById(R.id.button2);
		btnOkOnDialog.setOnClickListener(myListener);
		btnNoOnDialog.setOnClickListener(myListener);
				
		reviveDialog.setCancelable(false);	//set dialog not to dismiss by touching other where on the screen.				
	}
		
	//對被擊中時彈出的對話框的時程控制	
	private void controlReviveDialog() {		
		new Thread(new Runnable() {				
			@Override
			public void run() {
				proBarOnDialog.setProgress(100);
				for(int i=100;i>=0;i--){
					//break this for loop when the gameStatus is GAME_REVIVE or GAME_OVER.
					if(gameStatus == GameStatus.GAME_REVIVE.ordinal() || gameStatus == GameStatus.GAME_OVER.ordinal())	
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
	}
	
	//revive時飛機閃爍效果5sec
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
	
	//get SharedPreferences.
	private void restorePerf(){
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		totalCoin = settings.getInt(PREF_TOTAL_COIN,0);
		currVolumeIndex = settings.getInt("currVolumeIndex",0);
	}
	
	//set views to bring to Front.
	private void setViewsToFront(View... view){
		for(View v:view)
			v.bringToFront();
	}
	
	//set views to be gone.
	private void setViewGone(View... view){
		for(View v:view)
			v.setVisibility(View.GONE);
	}
	
	//set views to be invisible.
	private void setViewInVisible(View... view){
		for(View v:view)
			v.setVisibility(View.INVISIBLE);
	}
	
	//set views to be visible.
	private void setViewVisible(View... view){
		for(View v:view)
			v.setVisibility(View.VISIBLE);
	}
	
	//set view's showing animation.
	private void setShowAnimation(View... view){
		for(View v:view){
			v.clearAnimation();
			v.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.show));
		}	
	}
	
	//set view's hidding animation.
	private void setHideAnimation(View... view){
		for(View v:view){
			v.clearAnimation();
			v.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.hidden));
		}	
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub		
		super.onResume();
		Log.d("jimbo","onResume()...");		
		mpMainMenu.start();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub		
		super.onDestroy();
		Log.d("jimbo","onDestroy()...");
//		android.os.Process.killProcess(android.os.Process.myPid());
		if(mpPlaying != null){
			Log.d("jimbo","xxxxx");
			if(mpPlaying.isPlaying()){
				Log.d("jimbo","xxxxx");
				mpPlaying.stop();
			}				
			mpPlaying.release();
			mpPlaying = null;
		}
		
		if(mpMainMenu != null){
			if(mpMainMenu.isPlaying())
				mpMainMenu.stop();
			mpMainMenu.release();
			mpMainMenu = null;
		}
		
		if(sp != null){
			sp.release();
			sp = null;
		}		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("jimbo","onPause()...");
		//store SharedPreferences.
		SharedPreferences sp = getSharedPreferences(PREF, 0);
		sp.edit()
		.putInt(PREF_TOTAL_COIN, totalCoin)
		.putInt("currVolumeIndex", currVolumeIndex)
		.commit();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("jimbo","onStart()...");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d("jimbo","onStop()...");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
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
		
		//adjust volume key
		if(keyCode == event.KEYCODE_VOLUME_DOWN){
			if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 1)	//why is 1?so tricky...
				ivSound.setImageResource(R.drawable.soundclose40x40);
		}else if(keyCode == event.KEYCODE_VOLUME_UP){
				ivSound.setImageResource(R.drawable.soundopen40x40);
		}
		return super.onKeyDown(keyCode, event);
	}	
}
