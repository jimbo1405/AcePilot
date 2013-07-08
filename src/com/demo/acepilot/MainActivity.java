package com.demo.acepilot;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Button btnStart,btnRestart;
	private TextView tv1,tvStartCD;	//textView for start count down
	private MyGlSurfaceView myGlSurfaceView;
	private MyRender myRender; 
	private RelativeLayout gl_layout;
	private long timeStart,timeDie;	//開始的時間,結束的時間,活著的總時間
	private double timeLive;
	private DecimalFormat df;	//用來格式化飛行秒數
	
	public Handler myGameHandler;	//a handler to handle msg
	public static int gameStatus; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ABC","onCreate(Bundle savedInstanceState)...");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);	//設定全螢幕
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);		//設定螢幕為垂直

		setContentView(R.layout.activity_main);		
		findView();
		
		prepareGlSurfaceView();
		createMyGameHandler();	//initialize myGameHandler 
		setAllBtn();

	}
	
	//find出元件
	private void findView(){
		gl_layout=(RelativeLayout)findViewById(R.id.gl_layout);	//find出frameLayout
		btnStart=(Button)findViewById(R.id.button1);
		btnRestart=(Button)findViewById(R.id.button2);
		tv1=(TextView)findViewById(R.id.textView1);
		tvStartCD=(TextView)findViewById(R.id.textView2);
		tvStartCD.setText("");	//let tvStartCD become blank at first
		df=new DecimalFormat("0.00");	//initial df and set pattern
	}
	
	//set all btn
	private void setAllBtn(){				
		MyOnClickListener myOnClickListener=new MyOnClickListener(); 
		btnStart.setOnClickListener(myOnClickListener);
		btnRestart.setOnClickListener(myOnClickListener);
	}
	
	//prepare myGlSurfaceView
	private void prepareGlSurfaceView(){		
		myRender=new MyRender();		
		myRender.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.su_30_flanker),
				BitmapFactory.decodeResource(getResources(), R.drawable.normal_bullet));
		myGlSurfaceView=new MyGlSurfaceView(MainActivity.this);	//建立MyGlSurfaceView的物件					
		myGlSurfaceView.setRenderer(myRender);	//設定render		
		gl_layout.addView(myGlSurfaceView);		//將MyGlSurfaceView的物件加入gl_layout
		myGlSurfaceView.onPause();			
		btnStart.bringToFront();	//將按鈕移到最上層
		btnRestart.bringToFront();
		tv1.bringToFront();
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
				
				//GAME_START (按下start後的狀態)
				case 1:		
					gameStatus=GameStatus.GAME_START.ordinal();	//refresh gameStatus					
					handleStrtNewGame(msg);		//call startGame()
					break;
					
				//GAME_PAUSE
				case 2:	
					break;
				
				//GAME_RESUME
				case 3:	
					break;	
				
				//GAME_HIT
				case 4:	
					gameStatus=GameStatus.GAME_HIT.ordinal();
					myGlSurfaceView.onPause();	// set myGlSurfaceView pause
					openDialog();
					break;		
				}
			}			
		};
	}
	
	class MyOnClickListener implements OnClickListener{
		
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			//start game
			case R.id.button1:					
				sendStrNewGame();	//call sendStrNewGame()
				break;
			
			//重新開始
			case R.id.button2:	
				myGlSurfaceView.onPause();
				gl_layout.removeView(myGlSurfaceView);					
				prepareGlSurfaceView();
				myGlSurfaceView.onResume();
				break;
			}
		}
		
	}
		
	private void openDialog(){
		timeLive=(timeDie-timeStart)/1000.0;
		new AlertDialog.Builder(MainActivity.this)
		.setTitle("哈哈哈")
		.setMessage(df.format(timeLive)+"秒被擊中了")
		.setPositiveButton("結束", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				MainActivity.this.finish();
			}
			
		})
		.setNegativeButton("繼續", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
//				Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("http://android.gasolin.idv.tw/"));
//				startActivity(intent);			
				MyRender.isDie=false;
				createTestDieThread();
				myGlSurfaceView.onResume();				
			}
			
		})
		.show();
	}

	
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
//				myGlSurfaceView.onResume();		//原本想要讓飛機先出現，GO!開始閃爍時才能移動，但這樣寫不可行
//				myGlSurfaceView.onPause();
			}							
			tvStartCD.setText(msg.arg1-6 + "");
		}
		else{						
			if(msg.arg1 == 6){
				myGlSurfaceView.onResume();
				createTestDieThread();	//createTestDieThread while gmae starts running
			}
			tvStartCD.setText("GO!");						//GO!閃爍3次
			if(msg.arg1 % 2 == 1)
				tvStartCD.setVisibility(View.INVISIBLE);
			else
				tvStartCD.setVisibility(View.VISIBLE);						
									
			if(msg.arg1 == 0){
				tvStartCD.setVisibility(View.GONE);
				timeStart=System.currentTimeMillis();	//get timeStart value
			}	
		}					
	}

	//create testDie's thread
	private void createTestDieThread(){
		Thread tmpT = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){							//直到isDie為true則開啟對話方塊並跳出迴圈
					if(MyRender.isDie == true){
						timeDie=System.currentTimeMillis();	//get timeDie value 						
						myGameHandler.sendEmptyMessage(GameStatus.GAME_HIT.ordinal());						
						break;
					}
				}
			}
		});
		tmpT.start();
		tmpT=null;
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
