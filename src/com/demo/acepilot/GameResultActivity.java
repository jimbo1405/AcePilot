package com.demo.acepilot;

import java.text.DecimalFormat;

import com.demo.acepilot.engine.Audio.SFX;

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
	private Button btnContinue;
	private ImageView ivLevel;
	private TextView tvCoin,tvScore,tvComment,tvReviveCount;
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
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//full screen
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);		//set it as vertical
		setContentView(R.layout.game_result_layout);
	
		findView();

		createGameResultHandler();

		coinGet=this.getIntent().getIntExtra("coinGet", 0);
		score = MyConstant.calTotalSec(Double.parseDouble(this.getIntent().getStringExtra("timeScoreInSec")), 
				MainActivity.reviveCount);		

		makeComment();

		sendShowResultMsg();
		registerBtnContinue();
	}

	private void findView(){
		btnContinue=(Button)findViewById(R.id.button1);
		ivLevel=(ImageView)findViewById(R.id.imageView4);
		tvCoin=(TextView)findViewById(R.id.textView1);
		tvScore=(TextView)findViewById(R.id.textView2);
		tvComment=(TextView)findViewById(R.id.textView3);
		tvReviveCount=(TextView)findViewById(R.id.tv_revive);

		ivLevel.setImageBitmap(null);
		tvCoin.setText("");
		tvScore.setText("");
		tvReviveCount.setText("");
		tvComment.setText("");
	}

	private void registerBtnContinue(){
		btnContinue.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity.audio.play(SFX.CLICK);
				Intent myIntent=new Intent();
				myIntent.setClass(GameResultActivity.this, HighScoreActivity.class);
				myIntent.putExtra("score", score);
				myIntent.putExtra("level", level);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //Note! we set special FLAG here
				HighScoreActivity.updateFlag = true;	//updateFlag must be false when go to highscore page from GameResult page directly.
				startActivity(myIntent);
			}
		});
	}

	//Comment generator..generate comment & bitmap to show as lvl by the score.
	private void makeComment(){

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
					tvScore.setText(df.format(score)+"  sec" + "     ");
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
		MainActivity.audio.resumeMusic();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MainActivity.audio.pauseMusic();
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
		//MainActivity.audio.destroy();
	}
}