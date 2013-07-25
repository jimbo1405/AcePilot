package com.demo.acepilot;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GameResultActivity extends Activity{
	private SoundPool sp;
	private int clickSound1;
	
	private Button btnContinue;
	private ImageView ivLevel;
	private TextView tvCoin,tvScore,tvComment;
	private Bitmap lvlBitmap;
	private int imageId;
	private String comment;
	private Handler resultHandler;
	private int coinGet;
	private double score;
	private String level;
	private DecimalFormat df = new DecimalFormat("0.000");
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);	//設定全螢幕
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);		//設定螢幕為垂直
		setContentView(R.layout.game_result_layout);
		
		initSoundResource();		
		findView();
		
		createGameResultHandler();
		
		coinGet=this.getIntent().getIntExtra("coinGet", 0);
		score=Double.parseDouble(this.getIntent().getStringExtra("timeScoreInSec"));		
		makeComment(score);
		
		sendShowResultMsg();
		registerBtnContinue();
	}
	
	//init sound source.
	private void initSoundResource(){
		sp = new SoundPool(100, AudioManager.STREAM_MUSIC, 0);
		clickSound1 = sp.load(GameResultActivity.this, R.raw.cameraflash, 1);
	}
	
	private void findView(){
		btnContinue=(Button)findViewById(R.id.button1);
		ivLevel=(ImageView)findViewById(R.id.imageView4);
		tvCoin=(TextView)findViewById(R.id.textView1);
		tvScore=(TextView)findViewById(R.id.textView2);
		tvComment=(TextView)findViewById(R.id.textView3);
		
		ivLevel.setImageBitmap(null);
		tvCoin.setText("");
		tvScore.setText("");
		tvComment.setText("");
	}
		
	private void registerBtnContinue(){
		btnContinue.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sp.play(clickSound1, 1, 1, 0, 0, 1);
				Intent myIntent=new Intent();
				myIntent.setClass(GameResultActivity.this, HighScoreActivity.class);
				myIntent.putExtra("score", score);
				myIntent.putExtra("level", level);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //注意本行的FLAG设置
				HighScoreActivity.updateFlag = true;	//updateFlag must be false when go to highscore page from GameResult page directly.
				startActivity(myIntent);
			}
		});
	}
	
	private void createGameResultHandler(){
		resultHandler=new Handler(){

			@Override
			public void handleMessage(Message msg) {				
				super.handleMessage(msg);
				switch(msg.what){
				case 100:
					tvCoin.setText("X"+coinGet);
					break;
				
				case 200:
					tvScore.setText(df.format(score)+"sec");
					break;
				
				case 300:
					tvComment.setText(comment);					
					break;
				case 400:
//					ivLevel.setImageBitmap(lvlBitmap);
					ivLevel.setImageResource(imageId);
					break;
				}
			}
			
		};		
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
				} catch (InterruptedException e){ 
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	//generate comment & bitmap to show as lvl by the score.
	private void makeComment(double d){
		String[] tmpCommentArray = getResources().getStringArray(R.array.comment_array);
		if(d < MyConstant.COMMENT_SEC_RANGE[0]){
			comment=tmpCommentArray[0];
//			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_01);	//sys resource wastes more...
			imageId = R.drawable.lvl_01;
			level="1";
		}else if(d < MyConstant.COMMENT_SEC_RANGE[1]){
			comment=tmpCommentArray[1];
//			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_02);
			imageId = R.drawable.lvl_02;
			level="2";
		}else if(d < MyConstant.COMMENT_SEC_RANGE[2]){
			comment=tmpCommentArray[2];
//			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_03);
			imageId = R.drawable.lvl_03;
			level="3";
		}else if(d < MyConstant.COMMENT_SEC_RANGE[3]){
			comment=tmpCommentArray[3];
//			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_04);
			imageId = R.drawable.lvl_04;
			level="4";
		}else if(d < MyConstant.COMMENT_SEC_RANGE[4]){
			comment=tmpCommentArray[4];
//			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_05);
			imageId = R.drawable.lvl_05;
			level="5";
		}else if(d < MyConstant.COMMENT_SEC_RANGE[5]){
			comment=tmpCommentArray[5];
//			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_06);
			imageId = R.drawable.lvl_06;
			level="6";
		}else if(d < MyConstant.COMMENT_SEC_RANGE[6]){
			comment=tmpCommentArray[6];
//			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_07);
			imageId = R.drawable.lvl_07;
			level="7";
		}else if(d < MyConstant.COMMENT_SEC_RANGE[7]){
			comment=tmpCommentArray[7];
//			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_08);
			imageId = R.drawable.lvl_08;
			level="8";
		}else if(d < MyConstant.COMMENT_SEC_RANGE[8]){
			comment=tmpCommentArray[8];
//			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_09);
			imageId = R.drawable.lvl_09;
			level="9";
		}else if(d < MyConstant.COMMENT_SEC_RANGE[9]){
			comment=tmpCommentArray[9];
//			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_10);
			imageId = R.drawable.lvl_10;
			level="10";
		}else if(d < MyConstant.COMMENT_SEC_RANGE[10]){
			comment=tmpCommentArray[10];
//			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_11);
			imageId = R.drawable.lvl_11;
			level="11";
		}else{
			comment=tmpCommentArray[11];
//			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_12);
			imageId = R.drawable.lvl_12;
			level="12";
		}		
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
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(sp != null){
			sp.release();
			sp = null;
		}
	}
}
