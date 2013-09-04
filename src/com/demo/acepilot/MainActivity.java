package com.demo.acepilot;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.demo.acepilot.engine.Audio;
import com.demo.acepilot.engine.Audio.Music;
import com.demo.acepilot.engine.Audio.SFX;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

//the MainActivity of this proj.
public class MainActivity extends Activity {
	//-------------------------activity_main-------------------------------------
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
	private double timeScore;								//records the score which is the whole time of playing.(in millisecond)
	private String timeScoreInSec="0.000";				    //
	private DecimalFormat df=new DecimalFormat("0.000");	//use to format timeScore.

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
	
	private ViewFlipper vf;
	//--------------------------------------------------------------------------------
	
	//-------------------------game_result_layout-------------------------------------
	private Button btnContinue;
	private ImageView ivLevel;
	private TextView tvCoin,tvScore,tvComment,tvReviveCount;
	private int imageId;
	private String comment;
	private Handler resultHandler;	
	private String level;
	//--------------------------------------------------------------------------------
	
	//-------------------------high_score_layout--------------------------------------
	private Button btnBackMain, btnShowOnFB;
	private ListView myListView;
	private ImageView ivBG;
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
	private boolean updateFlag = false; 				//check if need to execute updateHighScore(currGameRecords).	
	private String userName;							//user name gets from FB.
	//--------------------------------------------------------------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("jimbo","onCreate()...");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);	//full screen
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);											//vertical
																					
		setContentView(R.layout.blank_layout);	

		restorePerf();																									//get SharedPreferences.
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
		vf = (ViewFlipper)findViewById(R.id.viewFlipper1); 
		View viewMain = LayoutInflater.from(this).inflate(R.layout.activity_main, (ViewGroup)findViewById(R.id.gl_layout));
		View viewGameResult = LayoutInflater.from(this).inflate(R.layout.game_result_layout, (ViewGroup)findViewById(R.id.game_result_layout));
		View viewHighScore = LayoutInflater.from(this).inflate(R.layout.high_score_layout, (ViewGroup)findViewById(R.id.high_score_layout));
		
		vf.addView(viewMain, 0);
		vf.addView(viewGameResult, 1);
		vf.addView(viewHighScore, 2);
		//-------------------------activity_main-------------------------------------
		gl_layout=(RelativeLayout)viewMain.findViewById(R.id.gl_layout);
		btnStart=(Button)viewMain.findViewById(R.id.button1);
		btnQuit=(Button)viewMain.findViewById(R.id.button3);
		btnHighScore=(Button)viewMain.findViewById(R.id.button4);
		btnRestart=(Button)viewMain.findViewById(R.id.button2);
		btnPauseResume=(ToggleButton)viewMain.findViewById(R.id.toggleButton1);
		/*ivCoin=(ImageView)viewMain.findViewById(R.id.imageView1);*/
		ivSound=(ImageView)viewMain.findViewById(R.id.imageView2);
		ivLogo=(ImageView)viewMain.findViewById(R.id.iv_logo);

        if (SoundEnabled) {
        	ivSound.setImageResource(R.drawable.soundopen40x40);        	
        } else {
        	ivSound.setImageResource(R.drawable.soundclose40x40);       	
        }

        showCoinLayout = (LinearLayout)viewMain.findViewById(R.id.showCoin_layout);
		tvShowCoin=(TextView)viewMain.findViewById(R.id.textView1);
		tvShowCoin.setText("X"+totalCoin);
		tvStartCD=(TextView)viewMain.findViewById(R.id.textView2);
		tvStartCD.setText("");													//let tvStartCD become blank at first
											
		prepareReviveDialog();

		gameStatus = GameStatus.GAME_NOT_READY.ordinal();

		initCoinNeed = 5;
		reviveCount = 0;
		coinGet = 0;		
		//---------------------------------------------------------------------------
		
		//-------------------------game_result_layout--------------------------------
		btnContinue=(Button)viewGameResult.findViewById(R.id.btn_continue);
		ivLevel=(ImageView)viewGameResult.findViewById(R.id.iv_level);
		tvCoin=(TextView)viewGameResult.findViewById(R.id.tv_coin);
		tvScore=(TextView)viewGameResult.findViewById(R.id.tv_score);
		tvComment=(TextView)viewGameResult.findViewById(R.id.tv_comment);
		tvReviveCount=(TextView)viewGameResult.findViewById(R.id.tv_revive);
		clearGameResult();
		//---------------------------------------------------------------------------
		
		//-------------------------high_score_layout---------------------------------
		btnBackMain = (Button)viewHighScore.findViewById(R.id.btn_back_main);
		btnShowOnFB = (Button)viewHighScore.findViewById(R.id.btn_showOnFB);
		myListView = (ListView)viewHighScore.findViewById(R.id.list_high_score);		
		ivBG = (ImageView)viewHighScore.findViewById(R.id.iv_bg_high_scoe);
		//---------------------------------------------------------------------------		
	}
	
	private void clearGameResult() {
		ivLevel.setImageBitmap(null);
		tvCoin.setText("");
		tvScore.setText("");
		tvReviveCount.setText("");
		tvComment.setText("");
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
		//-------------------------activity_main-------------------------------------
		btnStart.setOnClickListener(myButtonListener);
		btnQuit.setOnClickListener(myButtonListener);
		btnHighScore.setOnClickListener(myButtonListener);
		btnRestart.setOnClickListener(myButtonListener);
		btnPauseResume.setOnCheckedChangeListener(myButtonListener);
		ivSound.setOnClickListener(myButtonListener);
		//---------------------------------------------------------------------------
		
		//-------------------------game_result_layout--------------------------------		
		btnContinue.setOnClickListener(myButtonListener);
		//---------------------------------------------------------------------------
		
		//-------------------------game_high_score-----------------------------------
		btnBackMain.setOnClickListener(myButtonListener);
		btnShowOnFB.setOnClickListener(myButtonListener);
	    //---------------------------------------------------------------------------		
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
					activity.vf.setDisplayedChild(0);
					if(msg.arg1 == SET_LOGO_GONE){
						activity.setHideAnimation(activity.ivLogo);			
						activity.setViewGone(activity.ivLogo);
					}else{						
						activity.setViewsToFront(activity.btnStart,activity.btnQuit,activity.btnHighScore,activity.tvStartCD,activity.ivSound,activity.adLayout);
						activity.setViewVisible(activity.btnStart,activity.btnQuit,activity.btnHighScore,activity.ivSound,activity.adLayout);	
						gameStatus = GameStatus.GAME_READY.ordinal();	
					}					
					break;

				//GAME_START
				case 1:		
					activity.handleStartNewGame(msg);				
					break;

				//GAME_PAUSE
				case 2:				
					audio.pauseMusic();
					if(gameStatus == GameStatus.GAME_START.ordinal() || gameStatus == GameStatus.GAME_RESUME.ordinal()
							|| gameStatus == GameStatus.GAME_REVIVE.ordinal()){

						activity.timeScore += System.currentTimeMillis() - activity.timeStart;									
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
					activity.myGlSurfaceView.onPause();	
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
					activity.setViewGone(activity.btnPauseResume);
					activity.vf.setDisplayedChild(1);
					activity.workAtGameResult();
					/*Intent myIntent=new Intent();
					myIntent.setClass(activity, GameResultActivity.class);
					myIntent.putExtra("timeScoreInSec",activity.timeScoreInSec);
					myIntent.putExtra("coinGet",coinGet);
					myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //Note! we set special flag here
					activity.startActivity(myIntent);*/					
					break;

				//GAME_HIGHSCORE
				case 7:					
					gameStatus=GameStatus.GAME_HIGHSCORE.ordinal();
					activity.vf.setDisplayedChild(2);
					activity.workAtHighScore();
					/*Intent intent = new Intent(activity, HighScoreActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					HighScoreActivity.updateFlag = false;	//updateFlag must be false when go to highscore page from main page directly.
					activity.startActivity(intent);
					if(gameStatus != GameStatus.GAME_READY.ordinal())
						gameStatus=GameStatus.GAME_HIGHSCORE.ordinal();*/
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

	class MyButtonListener implements OnClickListener,CompoundButton.OnCheckedChangeListener{	
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			//-------------------------activity_main-------------------------------------			
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
				firstRunFlag = true;
				MainActivity.this.finish();	//end of the activity.
				break;

			//btnHighScore
			case R.id.button4:
				audio.play(SFX.CLICK);
				updateFlag = false;	//updateFlag must be false when go to highscore page from main page directly.
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
			//---------------------------------------------------------------------------

			//-------------------------game_result_layout--------------------------------
			case R.id.btn_continue:
				audio.play(SFX.CLICK);
				updateFlag = true;	//updateFlag must be false when go to highscore page from GameResult page directly.
				myGameHandler.sendEmptyMessage(GameStatus.GAME_HIGHSCORE.ordinal());
				break;
			//---------------------------------------------------------------------------
				
			//-------------------------game_high_score-----------------------------------
			case R.id.btn_back_main:
				audio.play(SFX.CLICK);
				if(beatRecordFlag == true && etOnItemReadyFlag == true){	
					GameRecords tmpGR = myGameRecordsList.get(getCurrPosition());	//get current GameRecords obj. 
					int n = myDataBaseHelper.updateData(etOnItem, tmpGR);			//update HighScore set name='xxx' where id=?;
					if(n > 0){
						Toast.makeText(MainActivity.this, "name saved...", Toast.LENGTH_LONG).show();
					}						
				}
				
				audio.pauseMusic();
				
				if(myDataBaseHelper != null)
					myDataBaseHelper.close();
				
				firstRunFlag = false;	//set firstRunFlag to be false.
				updateFlag = false;
				beatRecordFlag = false;
				btnStart.setEnabled(true);
				setViewInVisible(btnShowOnFB);
				timeScore = 0.0;
				clearGameResult();				
				gl_layout.removeView(myGlSurfaceView);
				prepareGlSurfaceView();
				
				myGameHandler.sendEmptyMessage(GameStatus.GAME_READY.ordinal());
				break;
				
			case R.id.iv_bg_high_scoe:
				Log.d("jimbo","imageView onClick...");
				etOnItem.clearFocus();
				break;
				
			case R.id.btn_showOnFB:
				Session.openActiveSession(MainActivity.this, true, new Session.StatusCallback() {			    	   
			    	@Override
			    	public void call(Session session, SessionState state,Exception exception) {
			    		if (session.isOpened()) {
			    			Log.d("fbt",session.getAccessToken()); // get token	    			
			    			 Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
			    		            // callback after Graph API response with user object
			    		            @Override
			    		            public void onCompleted(GraphUser user, Response response) {
			    		              if (user != null) {
			    		            	  userName = user.getName();
			    		            	  publishFeedDialog();
			    		              }
			    		            }
			    		     });			    						    			
			    	    }else
			    	    	Log.d("fbt",""+session.isOpened());
			    	}			    	
			    });
				break;
			//---------------------------------------------------------------------------				
			}
		}

		//pause,resume's event.
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if(isChecked){
				myGameHandler.sendEmptyMessage(GameStatus.GAME_PAUSE.ordinal());
			}	
			else{
				myGameHandler.sendEmptyMessage(GameStatus.GAME_RESUME.ordinal());			
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
					reviveDialog.cancel();
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
	
	
	
	
//-------------------------------work or method used in game_result----------------------------------------------
	private void workAtGameResult(){
		createGameResultHandler();
		makeComment(timeScore/1000);
		sendShowResultMsg();
	}
	
	private void createGameResultHandler(){
		resultHandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {				
				super.handleMessage(msg);
				switch(msg.what){
				case 100:
					tvCoin.setText(getString(R.string.multiply) + "  " + coinGet + "     ");
					break;

				case 200:
					tvReviveCount.setText(getString(R.string.multiply) + "  " + MainActivity.reviveCount + "     ");
					break;	

				case 300:
					tvScore.setText(timeScoreInSec + "  sec" + "     ");
					break;

				case 400:
					tvComment.setText(comment);					
					break;

				case 500:
					ivLevel.setImageResource(imageId);
					break;
				}
			}
		};		
	}
	
	//Comment generator...generate comment & bitmap to show as lvl by the score.
	private void makeComment(double score){
		String[] tmpCommentArray = getResources().getStringArray(R.array.comment_array);

		if(score < MyConstant.COMMENT_SEC_RANGE[0]){
			comment=tmpCommentArray[0];
			imageId = MyConstant.DrawableIdArray[0];
			level="1";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[1]){
			comment=tmpCommentArray[1];
			imageId = MyConstant.DrawableIdArray[1];
			level="2";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[2]){
			comment=tmpCommentArray[2];
			imageId = MyConstant.DrawableIdArray[2];
			level="3";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[3]){
			comment=tmpCommentArray[3];
			imageId = MyConstant.DrawableIdArray[3];
			level="4";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[4]){
			comment=tmpCommentArray[4];
			imageId = MyConstant.DrawableIdArray[4];
			level="5";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[5]){
			comment=tmpCommentArray[5];
			imageId = MyConstant.DrawableIdArray[5];
			level="6";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[6]){
			comment=tmpCommentArray[6];
			imageId = MyConstant.DrawableIdArray[6];
			level="7";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[7]){
			comment=tmpCommentArray[7];
			imageId = MyConstant.DrawableIdArray[7];
			level="8";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[8]){
			comment=tmpCommentArray[8];
			imageId = MyConstant.DrawableIdArray[8];
			level="9";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[9]){
			comment=tmpCommentArray[9];
			imageId = MyConstant.DrawableIdArray[9];
			level="10";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[10]){
			comment=tmpCommentArray[10];
			imageId = MyConstant.DrawableIdArray[10];
			level="11";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[11]){
			comment=tmpCommentArray[11];
			imageId = MyConstant.DrawableIdArray[11];
			level="12";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[12]){
			comment=tmpCommentArray[12];
			imageId = MyConstant.DrawableIdArray[12];
			level="13";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[13]){
			comment=tmpCommentArray[13];
			imageId = MyConstant.DrawableIdArray[13];
			level="14";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[14]){
			comment=tmpCommentArray[14];
			imageId = MyConstant.DrawableIdArray[14];
			level="15";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[15]){
			comment=tmpCommentArray[15];
			imageId = MyConstant.DrawableIdArray[15];
			level="16";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[16]){
			comment=tmpCommentArray[16];
			imageId = MyConstant.DrawableIdArray[16];
			level="17";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[17]){
			comment=tmpCommentArray[17];
			imageId = MyConstant.DrawableIdArray[17];
			level="18";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[18]){
			comment=tmpCommentArray[18];
			imageId = MyConstant.DrawableIdArray[18];
			level="19";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[19]){
			comment=tmpCommentArray[19];
			imageId = MyConstant.DrawableIdArray[19];
			level="20";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[20]){
			comment=tmpCommentArray[20];
			imageId = MyConstant.DrawableIdArray[20];
			level="21";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[21]){
			comment=tmpCommentArray[21];
			imageId = MyConstant.DrawableIdArray[21];
			level="22";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[22]){
			comment=tmpCommentArray[22];
			imageId = MyConstant.DrawableIdArray[22];
			level="23";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[23]){
			comment=tmpCommentArray[23];
			imageId = MyConstant.DrawableIdArray[23];
			level="24";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[24]){
			comment=tmpCommentArray[24];
			imageId = MyConstant.DrawableIdArray[24];
			level="25";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[25]){
			comment=tmpCommentArray[25];
			imageId = MyConstant.DrawableIdArray[25];
			level="26";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[26]){
			comment=tmpCommentArray[26];
			imageId = MyConstant.DrawableIdArray[26];
			level="27";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[27]){
			comment=tmpCommentArray[27];
			imageId = MyConstant.DrawableIdArray[27];
			level="28";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[28]){
			comment=tmpCommentArray[28];
			imageId = MyConstant.DrawableIdArray[28];
			level="29";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[29]){
			comment=tmpCommentArray[29];
			imageId = MyConstant.DrawableIdArray[29];
			level="30";
		}else if(score < MyConstant.COMMENT_SEC_RANGE[30]){
			comment=tmpCommentArray[30];
			imageId = MyConstant.DrawableIdArray[30];
			level="31";
		}else{
			comment=tmpCommentArray[30];
			imageId = MyConstant.DrawableIdArray[30];
			level="32";
		}
	}
	
	private void sendShowResultMsg(){
		new Thread(new Runnable() {
			@Override
			public void run() { 
				try {
					Thread.sleep(1000);
					resultHandler.sendEmptyMessage(100);					
					Thread.sleep(1000);
					resultHandler.sendEmptyMessage(200);					
					Thread.sleep(1000);
					resultHandler.sendEmptyMessage(300);
					Thread.sleep(1000);
					resultHandler.sendEmptyMessage(400);
					Thread.sleep(1000);
					resultHandler.sendEmptyMessage(500);				
				} catch (InterruptedException e){ 
					e.printStackTrace();
				}
			}
		}).start();
	}
//-------------------------------------------------------------------------------------------------------
	
	
//-------------------------------work or method used in high_score---------------------------------------	
	private void workAtHighScore(){
		imManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		initWaitLViewHandler();							//initial handler.
		
		if(myDataBaseHelper == null)					//initial myDataBaseHelper.
			myDataBaseHelper = new DataBaseHelper(MainActivity.this);
		
		currGameRecords = getCurrGameRecords();			//get current GameRecords obj. 
		if(updateFlag)
			updateHighScore(currGameRecords);			//update database.
		showListView();									//show the listView.
		if(beatRecordFlag == true){						//if it gets top 5, request the focus of editText at the specified item of myListView. 
			requestInputName();
			btnShowOnFB.setVisibility(View.VISIBLE);
		}
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
					ivBG.clearFocus();
					
					MyOnFocusChangeListener myOnFocusChangeListener = new MyOnFocusChangeListener();					
					etOnItem.setOnFocusChangeListener(myOnFocusChangeListener);
					ivBG.setOnFocusChangeListener(myOnFocusChangeListener);
										
					ivBG.setOnClickListener(new MyButtonListener());
					etOnItem.requestFocus();
					
					etOnItemReadyFlag = true;
					break;
				}
			}			
		};
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
		currGameRecords = new GameRecords();
		currGameRecords.setScore(Double.parseDouble(timeScoreInSec));
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
		mySimpleAdapter=new SimpleAdapter(MainActivity.this, myList, R.layout.my_listview_layout, fromArray, toArray);
		myListView.setAdapter(mySimpleAdapter);
	}
	
	//a method to decide which *.png of lvl to show.
	private int getLevelDrawID(GameRecords currentGR){
		String lvl = currentGR.getLevel();
		int tempLvl = Integer.parseInt(lvl);		
		return MyConstant.DrawableIdArray[tempLvl - 1];
	}	
	
	//the method to create FB post dialog.
	private void publishFeedDialog() {
	    Bundle params = new Bundle();
	    params.putString("name", "Exciting Game AcePilot for Android !");
	    params.putString("caption", "How long can you survive ?");
	    params.putString("description", userName + " survived "+ timeScoreInSec +" seconds ! How about you ?!");
	    params.putString("link", "https://developers.facebook.com/android");
	    params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
	    
	    WebDialog feedDialog = (
	        new WebDialog.FeedDialogBuilder(MainActivity.this,
	        		Session.getActiveSession(),
	            params))
	        .setOnCompleteListener(new WebDialog.OnCompleteListener() {

	            @Override
	            public void onComplete(Bundle values,
	                FacebookException error) {
	                if (error == null) {
	                    // When the story is posted, echo the success
	                    // and the post Id.
	                    final String postId = values.getString("post_id");
	                    if (postId != null) {
	                        Toast.makeText(MainActivity.this,
	                            "Posted story, id: "+postId,
	                            Toast.LENGTH_SHORT).show();
	                    } else {
	                        // User clicked the Cancel button
	                        Toast.makeText(MainActivity.this, 
	                            "Publish cancelled", 
	                            Toast.LENGTH_SHORT).show();
	                    }
	                } else if (error instanceof FacebookOperationCanceledException) {
	                    // User clicked the "x" button
	                    Toast.makeText(MainActivity.this, 
	                        "Publish cancelled", 
	                        Toast.LENGTH_SHORT).show();
	                } else {
	                    // Generic, ex: network error
	                    Toast.makeText(MainActivity.this, 
	                        "Error posting story", 
	                        Toast.LENGTH_SHORT).show();
	                }
	            }

	        })
	        .build();
	    feedDialog.show();
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
//-------------------------------------------------------------------------------------------------------	
}