package com.demo.acepilot;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

	private Button btnUp,btnDown,btnLeft,btnRight;
	private MyGlSurfaceView myGlSurfaceView; 
	private MyRender myRender=new MyRender(); 
	private FrameLayout gl_layout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);	//設定全螢幕
		
		setContentView(R.layout.activity_main);
		findView();
		
		gl_layout=(FrameLayout)findViewById(R.id.gl_layout);	//find出frameLayout
		myGlSurfaceView=new MyGlSurfaceView(MainActivity.this);	//建立MyGlSurfaceView的物件
		myGlSurfaceView.setRenderer(myRender);	//設定render
		gl_layout.addView(myGlSurfaceView);		//將MyGlSurfaceView的物件加入gl_layout
		
		
	}
	
	//find出元件
	private void findView(){
		btnUp=(Button)findViewById(R.id.button1);
		btnDown=(Button)findViewById(R.id.button2);
		btnLeft=(Button)findViewById(R.id.button3);
		btnRight=(Button)findViewById(R.id.button4);
		
		
		btnUp.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	myRender.motion="UP";
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	myRender.motion="";
		        }
		        return false;   
		    }    
		});
		
		btnDown.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	myRender.motion="DOWN";
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	myRender.motion="";
		        }
		        return false;   
		    }    
		});
		
		btnLeft.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	myRender.motion="LEFT";
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	myRender.motion="";
		        }
		        return false;   
		    }    
		});
		
		btnRight.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	myRender.motion="RIGHT";
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	myRender.motion="";
		        }
		        return false;   
		    }    
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
