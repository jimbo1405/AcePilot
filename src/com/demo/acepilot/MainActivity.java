package com.demo.acepilot;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

import com.demo.acepilot.engine.Audio;
import com.demo.acepilot.engine.Audio.Music;
import com.demo.acepilot.engine.Audio.SFX;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.ads.ac;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Dialog;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

//the MainActivity of this proj.
public class MainActivity extends Activity {

	private static boolean SoundEnabled;
	private Button btnStart,btnQuit,btnHighScore,btnRestart;
	private ToggleButton btnPauseResume;
	private LinearLayout showCoinLayout;
	private TextView tvShowCoin,tvStartCD;					//tvShowCoin, a textView to show how many coins did you get.
															//tvStartCD, a textView to make the count down-effection when the game starts.  
	private ImageView ivSound,ivLogo;
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

	public static int reviveCount;							//the number of the revive count.

	public final static int TV_SHOWCOIN=2000;
	private final static int PROGRESSDIALOG_REVIVE=1000;	//give PROGRESSDIALOG_REVIVE a final value,then we can easily identify it.
	private Dialog reviveDialog;							//obj's reference of Dialog class.
	private Button btnOkOnDialog, btnNoOnDialog;
	private ProgressBar proBarOnDialog;
	private int progressCount;								//use to store the count value of progressbar at the moment.

	private final static String PREF="PrefSettings";
	private final static String PREF_TOTAL_COIN="ToatalCoin";
	private final static String PREF_MUSIC_IS_ON="MusicIsOn";
	private final static String PREF_SOUND_IS_ON="SoundIsOn";

	private Thread countDownThread;							//a thread which task is performing 3..2..1.
	private Message message;								//3..2..1's delivering msg,also be used as thread's obj lock.
	private boolean isPaused;								//the condition which let countDownThread execute wait(). 
	private boolean isFinished;								//the condition which means the end of countDownThread's task.

	private final static int SET_LOGO_GONE = 3000;
	public static boolean firstRunFlag = true;				//the flag implies whether is first running.It must be false from HighScoreActivity.

	private LinearLayout adLayout;							//advertisement.
	public static Audio audio;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("jimbo","onCreate()...");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);	//full screen
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);											//vertical
																					//init sound source.
		setContentView(R.layout.activity_main);	

		restorePerf();																								//get SharedPreferences.
		initSoundResource();
		findViewAndInit();

		prepareGlSurfaceView();
		createMyGameHandler();																							//initialize myGameHandler 
		setAllBtn();

		//if it's first running,show the logo,or don't.
		if(firstRunFlag){	
			setGameReady();			
		}else{
			myGameHandler.sendEmptyMessage(GameStatus.GAME_READY.ordinal());
			firstRunFlag = true;
		}

		addAdMob();																							//add advertisement.
	}

	//find views and init some vlaues.
	private void findViewAndInit(){
		gl_layout=(RelativeLayout)findViewById(R.id.gl_layout);
		btnStart=(Button)findViewById(R.id.button1);
		btnQuit=(Button)findViewById(R.id.button3);
		btnHighScore=(Button)findViewById(R.id.button4);
		btnRestart=(Button)findViewById(R.id.button2);
		btnPauseResume=(ToggleButton)findViewById(R.id.toggleButton1);
//		ivCoin=(ImageView)findViewById(R.id.imageView1);
		ivSound=(ImageView)findViewById(R.id.imageView2);
		ivLogo=(ImageView)findViewById(R.id.iv_logo);

        if (SoundEnabled) {
        	ivSound.setImageResource(R.drawable.soundopen40x40);        	
        } else {
        	ivSound.setImageResource(R.drawable.soundclose40x40);       	
        }

        showCoinLayout = (LinearLayout)findViewById(R.id.showCoin_layout);
		tvShowCoin=(TextView)findViewById(R.id.textView1);
		tvShowCoin.setText("X"+totalCoin);
		tvStartCD=(TextView)findViewById(R.id.textView2);
		tvStartCD.setText("");	//let tvStartCD become blank at first

		df=new DecimalFormat("0.000");	//initial df and set pattern
		prepareReviveDialog();

		gameStatus = GameStatus.GAME_NOT_READY.ordinal();

		initCoinNeed = 5;
		reviveCount = 0;
		coinGet = 0;

	}

	//init sound source.
	private void initSoundResource(){
		
		audio = Audio.getInstance(MainActivity.this);
		
		// TODO (1) separate Music/SFX volumes
		// TODO (2) separate Music/SFX on/off
		audio.setMusicVolume(0.5f);
		audio.setSFXVolume(0.5f);

		if (SoundEnabled)
			audio.enable();
		else
			audio.disable();
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
		myGlSurfaceView=new MyGlSurfaceView(MainActivity.this);			
		myGlSurfaceView.setRenderer(myRender);
		gl_layout.addView(myGlSurfaceView);
		myGlSurfaceView.onPause();			
		//let views to be invisible.
		setViewInVisible(btnStart,btnQuit,btnHighScore,btnPauseResume,showCoinLayout,ivSound,ivLogo);	
		//set view's showing animation.
		setShowAnimation(btnStart,btnQuit,btnHighScore,btnPauseResume,showCoinLayout,ivSound,ivLogo);

		if(firstRunFlag){
			//let ivLogo bring to front.		
			setViewsToFront(ivLogo);	
			//set ivLogo to be visible.
			setViewVisible(ivLogo);
		}				

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
					if(msg.arg1 == SET_LOGO_GONE){
						activity.setHideAnimation(activity.ivLogo);			//set ivLogo hide animation.
						activity.setViewGone(activity.ivLogo);
					}else{						
						activity.setViewsToFront(activity.btnStart,activity.btnQuit,activity.btnHighScore,activity.tvStartCD,activity.ivSound);
						activity.setViewVisible(activity.btnStart,activity.btnQuit,activity.btnHighScore,activity.ivSound);	//set views to be visible.
						gameStatus = GameStatus.GAME_READY.ordinal();	//********************
//						mpMainMenu.start();								//***************************
					}					
					break;

				//GAME_START
				case 1:		
					activity.handleStartNewGame(msg);				//call startGame().		
					break;

				//GAME_PAUSE
				case 2:				
					audio.pauseMusic();
					if(gameStatus == GameStatus.GAME_START.ordinal() || gameStatus == GameStatus.GAME_RESUME.ordinal()
							|| gameStatus == GameStatus.GAME_REVIVE.ordinal()){

						activity.timeScore += System.currentTimeMillis() - activity.timeStart;			//********************************						
						gameStatus=GameStatus.GAME_PAUSE.ordinal();

					}else if(activity.countDownThread != null && activity.countDownThread.isAlive()){	//countDownThread wait if is alive.

						synchronized (activity.message) {
							activity.isPaused = true;
						}

					}				
					activity.myGlSurfaceView.onPause();										
					break;

				//GAME_RESUME
				case 3:	
					Log.d("checkState","gameStatus="+gameStatus);
					if(gameStatus == GameStatus.GAME_PAUSE.ordinal()){
						audio.resumeMusic();
						activity.myGlSurfaceView.onResume();
						activity.timeStart = System.currentTimeMillis();	//reset timeStart's value.
						MyRender.timePrevious=System.currentTimeMillis();	//reset MyRender.timePrevious's value.										
						gameStatus=GameStatus.GAME_RESUME.ordinal();

					}else if(gameStatus == GameStatus.GAME_READY.ordinal() || gameStatus == GameStatus.GAME_NOT_READY.ordinal()){						

						if(activity.countDownThread != null && activity.countDownThread.isAlive()){

							synchronized (activity.message) {
								activity.isPaused = false;
								activity.message.notify();
							}
							audio.play(Music.PLAYING);
						}else{			
							audio.play(Music.MAIN_MENU);
						}

					}else if(gameStatus == GameStatus.GAME_HIT.ordinal()){
						audio.play(Music.PLAYING);
						activity.myGlSurfaceView.onResume();
					}
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
					this.sendEmptyMessage(TV_SHOWCOIN);									//update the textView which is showing coinGet.
					reviveCount++;														//reviveCount+1.
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
					myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //Note! we set special flag here
					activity.startActivity(myIntent);					
					break;

				//GAME_HIGHSCORE
				case 7:					
					Intent intent = new Intent(activity, HighScoreActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					HighScoreActivity.updateFlag = false;	//updateFlag must be false when go to highscore page from main page directly.
					activity.startActivity(intent);
					if(gameStatus != GameStatus.GAME_READY.ordinal())
						gameStatus=GameStatus.GAME_HIGHSCORE.ordinal();
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
				btnStart.setEnabled(false);
				audio.play(SFX.CLICK);
				sendStartNewGame();											//call sendStrNewGame().
				setHideAnimation(btnStart,btnQuit,btnHighScore,adLayout);	//set view's hidding animation.
				setViewGone(btnStart,btnQuit,btnHighScore,adLayout);		//set views to be gone.				
				audio.play(Music.PLAYING);
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
				audio.play(SFX.CLICK);
				MainActivity.this.finish();	//end of the activity.
				break;

			//btnHighScore
			case R.id.button4:
				audio.play(SFX.CLICK);
				myGameHandler.sendEmptyMessage(GameStatus.GAME_HIGHSCORE.ordinal());
				break;

			//ivSound
			case R.id.imageView2:								
				if (SoundEnabled) {             
					ivSound.setImageResource(R.drawable.soundclose40x40);
                	audio.disable();
                } else {
                	ivSound.setImageResource(R.drawable.soundopen40x40);
                	audio.enable();
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
//				timeScore += System.currentTimeMillis() - timeStart;	//*************************************
			}	
			else{
				myGameHandler.sendEmptyMessage(GameStatus.GAME_RESUME.ordinal());
//				timeStart = System.currentTimeMillis();					//**************************************************
			}
			isChecked = !isChecked;
		}

	}

	//set game become ready.
	private void setGameReady(){
		new Thread(new Runnable() {			
			@Override
			public void run() {
				for(int i = 0 ; i < MyConstant.logoDisplaySec + 1 ; i++){
					try {
						if(i == 3){
							Message msg = new Message();
							msg.arg1 = SET_LOGO_GONE;
							msg.what = GameStatus.GAME_READY.ordinal();
							myGameHandler.sendMessage(msg);							
							Thread.sleep(800);							
						}else							
							Thread.sleep(1000);
					} catch (InterruptedException e) {	
						e.printStackTrace();
					}
				}
				myGameHandler.sendEmptyMessage(GameStatus.GAME_READY.ordinal());
			}
		}).start();
	}

	//send the msg to perform 3..2..1.
	private void sendStartNewGame(){
		//MyRunnable class used by countDownThread.
		class MyRunnable implements Runnable{

			//constructor.
			public MyRunnable(){
				isPaused = false;
				isFinished = false;
				message = new Message();
			}

			@Override
			public void run() {
				while(!isFinished){
					for(int i=12;i>=0;i--){								
						try {
							if(i > 6)
								Thread.sleep(500);	//show 3.2.1...delay 1s.
							else
								Thread.sleep(350);	//show GO!delay 0.5s.
						} catch (InterruptedException e) {									
							e.printStackTrace();
						}					
						message = new Message();
						message.what=GameStatus.GAME_START.ordinal();
						message.arg1=i;
						myGameHandler.sendMessage(message);
						if(i == 0)
							isFinished = true;

						while(isPaused){
							synchronized (message) {
								try {
									message.wait();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}	
				}
			}			
		}
		countDownThread = new Thread(new MyRunnable());		
		countDownThread.start();
	}

	//handle the msg from sendStartNewGame().
	private void handleStartNewGame(Message msg){
		if(msg.arg1 > 6){				
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
				createTestDieThread();			//createTestDieThread while game starts running
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
				setViewsToFront(btnPauseResume,showCoinLayout);	//let views bring to front.
				setViewVisible(btnPauseResume,showCoinLayout);	//set views to be visible.
				gameStatus=GameStatus.GAME_START.ordinal();			//change gameStatus.
				timeStart=System.currentTimeMillis();				//get timeStart value.
			}	
		}					
	}

	//create testDie's thread
	private void createTestDieThread(){
		MyRender.isDie = false;		//MyRender.isDie must be false before testDie.
		new Thread(new Runnable() {		
			@Override
			public void run() {
				while(true){							//run until isDie==true, then open dialog & leave loop
					if(MyRender.isDie == true){						 						
						MainActivity.audio.play(SFX.EXPLOSION);
						myGameHandler.sendEmptyMessage(GameStatus.GAME_HIT.ordinal());						
						break;
					}
				}
				timeScore += System.currentTimeMillis() - timeStart;
			}
		}).start();
	}

	//design the dialog when hitting
	private void prepareReviveDialog(){
		View.OnClickListener myListener=new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				//btnOk
				case R.id.button1:
					if(totalCoin >= MyConstant.calCurrCoinNeed(initCoinNeed, reviveCount)){
						audio.play(SFX.CLICK);
						myGameHandler.sendEmptyMessage(GameStatus.GAME_REVIVE.ordinal());
						reviveDialog.cancel();	//close dialog.
						timeStart = System.currentTimeMillis();
					}else{
						audio.play(SFX.BAN);
					}					
					break;
				//btnNo
				case R.id.button2:
					audio.play(SFX.CLICK);
					myGameHandler.sendEmptyMessage(GameStatus.GAME_OVER.ordinal());
					break;	
				}				
			}			
		};
		reviveDialog = new Dialog(MainActivity.this, R.style.TANCStyle);
		reviveDialog.setContentView(R.layout.my_progressdialog_layout);		

		proBarOnDialog = (ProgressBar)reviveDialog.findViewById(R.id.progressBar1);
		proBarOnDialog.setMax(100);

		btnOkOnDialog = (Button)reviveDialog.findViewById(R.id.button1);
		btnNoOnDialog = (Button)reviveDialog.findViewById(R.id.button2);
		btnOkOnDialog.setOnClickListener(myListener);
		btnNoOnDialog.setOnClickListener(myListener);

		reviveDialog.setCancelable(false);	//set dialog not to dismiss by touching other where on the screen.				
	}

	//control the timing of dialog after hitting
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

	// flash the airplan 5sec right after revive
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
		SoundEnabled = settings.getBoolean(PREF_MUSIC_IS_ON,true);
		SoundEnabled = settings.getBoolean(PREF_SOUND_IS_ON,true);
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

	//add advertisement.
	private void addAdMob(){
		adLayout = (LinearLayout)findViewById(R.id.admob_layout);
		AdView adView = new AdView(MainActivity.this, AdSize.BANNER, "a15204860267283");
		adLayout.addView(adView);
		AdRequest request = new AdRequest();
		adView.loadAd(request);
		adLayout.bringToFront();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("jimbo","onStart()...");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub		
		super.onResume();
		Log.d("jimbo","onResume()...");

		myGameHandler.sendEmptyMessage(GameStatus.GAME_RESUME.ordinal());	//do something in GAME_RESUME case.
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
		.putBoolean(PREF_MUSIC_IS_ON, SoundEnabled)
		.putBoolean(PREF_SOUND_IS_ON, SoundEnabled)
		.commit();

		myGameHandler.sendEmptyMessage(GameStatus.GAME_PAUSE.ordinal());	//do something in GAME_PAUSE case.
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d("jimbo","onStop()...");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub		
		super.onDestroy();
		Log.d("jimbo","onDestroy()...");
		//android.os.Process.killProcess(android.os.Process.myPid());
		//audio.destroy();
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
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			if(((AudioManager)getSystemService(AUDIO_SERVICE)).getStreamVolume(AudioManager.STREAM_MUSIC) == 1)
				ivSound.setImageResource(R.drawable.soundclose40x40);
		}else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
				ivSound.setImageResource(R.drawable.soundopen40x40);
		}

		return super.onKeyDown(keyCode, event);
	}	
}