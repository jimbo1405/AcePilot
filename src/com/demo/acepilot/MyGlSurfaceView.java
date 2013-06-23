package com.demo.acepilot;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

public class MyGlSurfaceView extends GLSurfaceView{
	 
	private float mPreviousX,mPreviousY;
	
	public MyGlSurfaceView(Context context) {
		super(context);		
		Log.d("Wang","glSurfaceView constructor...");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.d("ABC","TouchEvent x="+event.getX()+" y="+event.getY());
		
		float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE:        	
        	float dx = (float)((x - mPreviousX)*MyConstant.PLAYER_VELOCITY_COFF);
            float dy = (float)((-1)*(y - mPreviousY)*MyConstant.PLAYER_VELOCITY_COFF);
            
            //兩個if判斷，positionX,positionY加上dx,dy後的絕對值要分別小於螢幕寬、長的一半，也就是在螢幕範圍內position才做增減
            if(Math.abs(MyRender.player_positionY+dy / (MyRender.ratio_pixToDist*0.01)) <
        			(0.5*MyRender.y_screen/MyRender.ratio_pixToDist)/0.01){
            	MyRender.player_positionY += dy / (MyRender.ratio_pixToDist*0.01);
            }	
            if(Math.abs(MyRender.player_positionX+dx / (MyRender.ratio_pixToDist*0.01)) <
            		(0.5*MyRender.x_screen/MyRender.ratio_pixToDist)/0.01){
            	MyRender.player_positionX += dx / (MyRender.ratio_pixToDist*0.01);
            }

            requestRender();
            Log.d("Wang","moveX="+MyRender.player_positionX+" moveY="+MyRender.player_positionY);
        }
        mPreviousX = x;
        mPreviousY = y;
//		return super.onTouchEvent(event);
        return true;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.d("Wang","TrackballEvent x="+event.getX()+" y="+event.getY());
		return super.onTrackballEvent(event);		
	}

	
}
