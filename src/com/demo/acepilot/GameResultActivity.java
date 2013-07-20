package com.demo.acepilot;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GameResultActivity extends Activity{

	private Button btnContinue;
	private ImageView ivLevel;
	private TextView tvCoin,tvScore,tvComment;
	private Bitmap lvlBitmap;
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
		findView();
		createGameResultHandler();
		
		coinGet=this.getIntent().getIntExtra("coinGet", 0);
		score=Double.parseDouble(this.getIntent().getStringExtra("timeScoreInSec"));		
		makeComment(score);
		
		sendShowResultMsg();
		registerBtnContinue();
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
				Intent myIntent=new Intent();
				myIntent.setClass(GameResultActivity.this, HighScoreActivity.class);
				myIntent.putExtra("score", score);
				myIntent.putExtra("level", level);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //注意本行的FLAG设置
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
					ivLevel.setImageBitmap(lvlBitmap);
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
		if(d < 5){
			comment=tmpCommentArray[0];
			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_01);
			level="1";
		}else if(d < 10){
			comment=tmpCommentArray[1];
			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_02);
			level="2";
		}else if(d < 15){
			comment=tmpCommentArray[2];
			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_03);
			level="3";
		}else if(d < 20){
			comment=tmpCommentArray[3];
			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_04);
			level="4";
		}else if(d < 25){
			comment=tmpCommentArray[4];
			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_05);
			level="5";
		}else if(d < 27.5){
			comment=tmpCommentArray[5];
			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_06);
			level="6";
		}else if(d < 30){
			comment=tmpCommentArray[6];
			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_07);
			level="7";
		}else if(d < 32.5){
			comment=tmpCommentArray[7];
			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_08);
			level="8";
		}else if(d < 35){
			comment=tmpCommentArray[8];
			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_09);
			level="9";
		}else if(d < 37.5){
			comment=tmpCommentArray[9];
			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_10);
			level="10";
		}else if(d < 40){
			comment=tmpCommentArray[10];
			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_11);
			level="11";
		}else{
			comment=tmpCommentArray[11];
			lvlBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.lvl_12);
			level="12";
		}		
	}
}
