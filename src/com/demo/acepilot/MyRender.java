package com.demo.acepilot;

import java.util.ArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;

public class MyRender implements Renderer{	
	public static float player_positionX,player_positionY;	//	
	public static float x_screen=0,y_screen=0;				//視窗的寬與高	
	public static float ratio_pixToDist;					//比值，座標上每單位相當於多少像素
	private Square square;
	private ArrayList<Bullet> bulletList;					//宣告預備子彈的list	
	private double radius; 									//佈署子彈的圓之半徑(單位:像素)	
	public static boolean isDie;							//是否被擊中
	private long timePrevious,timeNow,timeBetweenFrame;
	private int count;
	
	 public MyRender() {
		 Log.d("ABC","MyRender()...");
		 square = new Square();	
		 count=0;		 
		 isDie=false;			//初始、重置為false，因為MainActivity的onCreate()比onResume()早呼叫，
	 }							//所以onResume()裡抓到的isDie就是重置過的

	
	@Override
	public void onDrawFrame(GL10 gl) {				
		timeNow=System.currentTimeMillis();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	//清除螢幕和深度緩衝區
		if(count == 0){			  
			gl.glLoadIdentity();			//將原點移至螢幕中心		
			gl.glTranslatef(0, 0,-10);		//物體往螢幕內移動10單位，將景物與視點分離
			player_positionX=0;				//繪製第一個frame時，玩家要出現在螢幕中央
			player_positionY=0;
		}
		if(count > 0){
			timeBetweenFrame=timeNow-timePrevious;	//算出幾微秒繪製一個Frame			  		  						 
			gl.glPushMatrix();													//儲存目前gl狀態		 
			gl.glTranslatef(player_positionX, player_positionY, 0);
			gl.glScalef(0.25f, 0.25f, 0.25f);									//調整方形的大小
			gl.glRotatef(-45, 0, 0, 1);
			square.draw(gl);		  											//畫出方形
			gl.glPopMatrix();													//回到上一個gl儲存點的狀態
			  		  		  		  			
			if((count % (Math.ceil(MyConstant.BULLET_TIME_INTERVAL*1000/timeBetweenFrame))) == 1 &&	//每隔幾個Frame準備一顆子彈
					bulletList.size() < MyConstant.BULLET_NUM){															
				prepareBullet();
			}
						
			for(int i=0;i<bulletList.size();i++){														  						  
					  Bullet b=bulletList.get(i);
					  deleteBullet(b,i);	//檢查是否需要消除子彈
					  gl.glPushMatrix();			  
					  gl.glTranslatef((float)(b.getBullet_positionX()), (float)(b.getBullet_positionY()), 0);	//平移
					  gl.glScalef(0.0400f, 0.0400f, 0.0400f);			  
					  b.draw(gl);			  					  
					  gl.glPopMatrix();			  			  					  
					  checkDie(b);										  
					  b.setBullet_positionX(b.getBullet_positionX() - 
							  b.getBullet_fly()*Math.cos(b.getBulletFlyAngle()*Math.PI/180)*timeBetweenFrame/1000);	 	//設定當前子彈下一個frame時x方向的位移
					  b.setBullet_positionY(b.getBullet_positionY() - 
							  b.getBullet_fly()*Math.sin(b.getBulletFlyAngle()*Math.PI/180)*timeBetweenFrame/1000);		//設定當前子彈下一個frame時y方向的位移
					  b.setBullet_totalFly(b.getBullet_totalFly() + b.getBullet_fly()*timeBetweenFrame/1000);			//設定當前子彈飛行總位移
			}			 		
		}
		timePrevious=timeNow;
		count++;
		gl.glFinish();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		Log.d("ABC","onSurfaceChanged(GL10 gl, int width, int height)...");
		// 設定新視域視窗的大小
		  gl.glViewport(0, 0, width, height);
		  // 選擇投射的陣列模式
		  gl.glMatrixMode(GL10.GL_PROJECTION);
		  // 重設投射陣
		  gl.glLoadIdentity();
		  // 計算視窗的寬高比率
		  GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
		    100.0f);		  
		  x_screen=width;
		  y_screen=height;
		  ratio_pixToDist=(float)(height/(2*Math.tan(22.5*Math.PI/180)*0.1));	//螢幕上的1單位相當於多少像素
		  radius=Math.sqrt(width*width + height*height);					//設定佈署子彈的圓之半徑=畫面的斜邊長
		  Log.d("Wang","ratio_pixToDist="+ ratio_pixToDist);
		  Log.d("Wang","x_screen="+x_screen+" y_screen="+y_screen);
		  Log.d("Wang","radius="+ radius);		  
		  // 選擇MODELVIEW陣列
		  gl.glMatrixMode(GL10.GL_MODELVIEW);
		  // 重設MODELVIEW陣列
		  gl.glLoadIdentity(); 
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		Log.d("ABC","onSurfaceCreated(GL10 gl, EGLConfig config)...");
        // 关闭抗抖动
        gl.glDisable(GL10.GL_DITHER);
        // 设置系统对透视进行修正
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		// 設定背景顏色為黑色, 格式是RGBA
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
		// 設定流暢的陰影模式
		gl.glShadeModel(GL10.GL_SMOOTH);
		// 啟動深度的測試
		gl.glEnable(GL10.GL_DEPTH_TEST);
		// GL_LEQUAL深度函式測試
		gl.glDepthFunc(GL10.GL_LEQUAL);
		//方形載入材質
		square.loadTexture(gl);
		bulletList=new ArrayList<Bullet>();
	}
	
	 public void setBitmap(Bitmap bitmap) {
		 square.setBitmap(bitmap);
	 }
	
	//準備子彈
	private void prepareBullet(){															
		Bullet b=new Bullet();			  				  
		double tmpRadius=(radius/ratio_pixToDist)/0.01;								//將半徑從像素轉換成座標上的單位				  
		double tmpAngle=Math.ceil(Math.random()*360);								//子彈圓心角
		b.setBullet_positionX(tmpRadius*Math.cos(tmpAngle*Math.PI/180));	//設定當前子彈初始x方向的位移
		b.setBullet_positionY(tmpRadius*Math.sin(tmpAngle*Math.PI/180));	//設定當前子彈初始y方向的位移				  
		b.setBullet_fly(MyConstant.BULLET_VELOCITY/(ratio_pixToDist*0.01));						 				  
		b.setBulletAngle(tmpAngle);											//設定子彈圓心角				  																
		double vectorX=b.getBullet_positionX()-player_positionX;
		double vectorY=b.getBullet_positionY()-player_positionY;
		double tmpCos=(vectorX)/(Math.sqrt(vectorX*vectorX + vectorY*vectorY));			  		
		double tmpFlyAngle=Math.acos(tmpCos)*(180/Math.PI);
		if((vectorX < 0 && vectorY < 0) || (vectorX > 0 && vectorY < 0))
			tmpFlyAngle = 360 - tmpFlyAngle;			
		b.setBulletFlyAngle(tmpFlyAngle);
		bulletList.add(b);
	}		
	
	//消除飛出圓周之外的子彈，傳入整數i表示當前bulletList的index值
	private void deleteBullet(Bullet b,int i){
		double tmpFly=b.getBullet_totalFly();			//當前子彈的飛行總位移
		double tmpRadius=(radius/ratio_pixToDist)/0.01;	//將半徑從像素轉換成座標上的單位
		
		//if判斷子彈必須在螢幕外 且 至少飛了半徑長度的距離才能從list刪掉
		if(Math.abs(b.getBullet_positionX()) > (0.5*x_screen/ratio_pixToDist)/0.01 || 
				Math.abs(b.getBullet_positionY()) > (0.5*y_screen/ratio_pixToDist)/0.01){
			if(tmpFly > tmpRadius)
				bulletList.remove(i);
		}

	}
	
	//測試是否被子彈擊中
	private void checkDie(Bullet b){
//		if((b.getBullet_positionX() >= MyRender.player_positionX - 0.25 && b.getBullet_positionX() <= MyRender.player_positionX + 0.25) &&
//				(b.getBullet_positionY() >= MyRender.player_positionY - 0.25 && b.getBullet_positionY() <= MyRender.player_positionY + 0.25)){
//			isDie=true;
//		}
		ProbeCircle[] pCircleArray={new ProbeCircle(player_positionX + 0.25, player_positionY + 0.25, 0.177),	//偵測碰撞圓陣列
				new ProbeCircle(player_positionX - 0.25, player_positionY + 0.25, 0.177),
				new ProbeCircle(player_positionX - 0.25, player_positionY - 0.25, 0.177),
				new ProbeCircle(player_positionX + 0.25, player_positionY - 0.25, 0.177)};	
		
		//偵測碰撞迴圈
		for(int i=0;i<pCircleArray.length;i++){			
				ProbeCircle tmpPC=pCircleArray[i];
				double tmp_dx=b.getBullet_positionX() - tmpPC.getpCircle_positionX();	//子彈X-圓心X
				double tmp_dy=b.getBullet_positionY() - tmpPC.getpCircle_positionY();	//子彈Y-圓心Y
				double tmpDistance=Math.sqrt(tmp_dx*tmp_dx + tmp_dy*tmp_dy);	//子彈與圓心的距離
				if(tmpDistance <= tmpPC.getR()){	//若距離小於半徑則isDie為true並跳出迴圈
					isDie=true;
					break;
				}					
		}
	}	
		
}
