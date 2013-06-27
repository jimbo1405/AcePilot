package com.demo.acepilot;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

	private Button btnUp,btnDown,btnLeft,btnRight;
	private MyGlSurfaceView myGlSurfaceView;
	private MyRender myRender; 
	private FrameLayout gl_layout;
	private Handler thread_Handler;	//�ŧi�޲z�������handler
	private HandlerThread ht1;		//�ŧi�����
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ABC","onCreate(Bundle savedInstanceState)...");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);	//�]�w���ù�
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setContentView(R.layout.activity_main);
		findView();
		
		gl_layout=(FrameLayout)findViewById(R.id.gl_layout);	//find�XframeLayout
		myGlSurfaceView=new MyGlSurfaceView(MainActivity.this);	//�إ�MyGlSurfaceView������
		myRender=new MyRender();
		Bitmap bitmap=BitmapFactory.decodeResource(this.getResources(), R.drawable.su_30_flanker);
		myRender.setBitmap(bitmap);
		myGlSurfaceView.setRenderer(myRender);	//�]�wrender
		gl_layout.addView(myGlSurfaceView);		//�NMyGlSurfaceView������[�Jgl_layout		
	}
	
	//find�X����
	private void findView(){

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private void openDialog(){
		new AlertDialog.Builder(MainActivity.this)
		.setTitle("������")
		.setMessage("�Q�����F")
		.setPositiveButton("����", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				MainActivity.this.finish();
			}
			
		})
		.setNegativeButton("�~��", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
//				Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("http://android.gasolin.idv.tw/"));
//				startActivity(intent);			
				MyRender.isDie=false;
				MainActivity.this.onResume();
			}
			
		})
		.show();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d("ABC","onResume()...");
		super.onResume();
		ht1=new HandlerThread("ht1");					//����ư����ht1
		ht1.start();									//�Ұ�ht1
		thread_Handler=new Handler(ht1.getLooper());	//�����handler�A��ht1�浹�L��
		thread_Handler.post(new Runnable() {			//handler��N�u�@��ht1
			
			@Override
			public void run() {							
				// TODO Auto-generated method stub			
				while(true){							//����isDie��true�h�}�ҹ�ܤ���ø��X�j��
					if(MyRender.isDie == true){
						openDialog();
						break;
					}
				}	
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d("ABC","onDestroy()...");
		super.onDestroy();						
		if(thread_Handler != null)				//�M��handler,thread����
			thread_Handler=null;
		if(ht1 != null)
			ht1=null;
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
	
	
}
