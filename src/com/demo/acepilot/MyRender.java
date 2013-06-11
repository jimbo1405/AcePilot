package com.demo.acepilot;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;
import android.webkit.WebView.FindListener;

public class MyRender implements Renderer{
	
	public String motion="";
	
	private float x_screen=0,y_screen=0;	//視窗的寬與高
	private float x_move,y_move;	//x,y方向的像素移動量
	private float ratio_pixToDist;	//比值，座標上每單位相當於多少像素
	private boolean isStart=false;	//isStart為false，則會進行首次繪圖設定
	private Square square;
//	private float angle = 0;

	 public MyRender() {
	  // 初始化
	  square = new Square();
	 }

	
	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		// 清除螢幕和深度緩衝區
		  gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); 
		  
		  if(isStart==false){
			  x_move=0;
			  y_move=0;
			  gl.glLoadIdentity();	//將原點移至螢幕中心		
			  gl.glTranslatef(0, 0,-10);	//物體往螢幕內移動10單位，將景物與視點分離
			  gl.glScalef(0.25f, 0.25f, 0.25f);	//調整方形的大小
			  isStart=true;	//isStart設為true，接下來的繪圖將略過此步驟，避免畫面重置
		  }
		 
		  if(motion.equals("UP")){
			  if(y_move <= (y_screen/2)){
				  gl.glTranslatef(0, 1, 0);
				  y_move+=0.01*ratio_pixToDist*0.25;	//model位在負Z軸10單位處，而near平面位在0.1單位處，由於model的scale變更過，
			  }											//因此於model處1單位的移動量，於近平面的移動量要乘上(0.1/10)及0.25，並乘上比例轉成像素
			  Log.d("Wang","y_move="+y_move);
		  }else if(motion.equals("DOWN")){
			  if(y_move >= (-y_screen/2)){
				  gl.glTranslatef(0, -1, 0);
				  y_move-=0.01*ratio_pixToDist*0.25;
			  }
			  Log.d("Wang","y_move="+y_move);
		  }else if(motion.equals("LEFT")){
			  if(x_move >= (-x_screen/2)){
				  gl.glTranslatef(-1, 0, 0);
				  x_move-=0.01*ratio_pixToDist*0.25;
			  }
			  Log.d("Wang","x_move="+x_move);
		  }else if(motion.equals("RIGHT")){
			  if(x_move <= (x_screen/2)){
				  gl.glTranslatef(1, 0, 0);
				  x_move+=0.01*ratio_pixToDist*0.25;
			  }
			  Log.d("Wang","x_move="+x_move);
		  }
		  
		  

		  // 第一個方形
		  // 存儲目前陣列
		  gl.glPushMatrix();
		  // 反時鐘旋轉
//		  gl.glRotatef(angle, 0, 0, 1);
		  // 畫出第一個方形
		  square.draw(gl);
		  // 復原成最後的矩陣
//		  gl.glPopMatrix();

//		  // 第二個方形
//		  // 存儲目前陣列
//		  gl.glPushMatrix();
//		  // 在移動前先旋轉, 讓第二個方形圍繞著第一個方形旋轉
//		  gl.glRotatef(-angle, 0, 0, 1);
//		  // 移動第二個方形
//		  gl.glTranslatef(2, 0, 0);
//		  // 調整其大小為第一個方形的一半
//		  gl.glScalef(.5f, .5f, .5f);
//		  // 畫出第二個方形
//		  square.draw(gl);
//
//		  // 第三個方形
//		  // 存儲目前陣列
//		  gl.glPushMatrix();
//		  // 讓第三個方形圍繞著第二個方形旋轉
//		  gl.glRotatef(-angle, 0, 0, 1);
//		  // 移動第三個方形
//		  gl.glTranslatef(2, 0, 0);
//		  // 調整其大小為第二個方形的一半
//		  gl.glScalef(.5f, .5f, .5f);
//		  // 以自己為中心旋轉
//		  gl.glRotatef(angle * 10, 0, 0, 1);
//		  // 畫出第三個方形.
//		  square.draw(gl);
//
//		  // 復原成第三個方形前的矩陣
//		  gl.glPopMatrix();
//		  // 復原成第二個方形前的矩陣.
//		  gl.glPopMatrix();
//
//		  // 增加角度
//		  angle++;
		  
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
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
		  Log.d("Wang"," ratio_pixToDist="+ ratio_pixToDist);
		  Log.d("Wang","x_screen="+x_screen+" y_screen="+y_screen);
		  		  
		  // 選擇MODELVIEW陣列
		  gl.glMatrixMode(GL10.GL_MODELVIEW);
		  // 重設MODELVIEW陣列
		  gl.glLoadIdentity(); 
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		// 設定背景顏色為黑色, 格式是RGBA
		  gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
		  // 設定流暢的陰影模式
		  gl.glShadeModel(GL10.GL_SMOOTH);
		  // 深度緩區的設定
		  gl.glClearDepthf(1.0f);
		  // 啟動深度的測試
		  gl.glEnable(GL10.GL_DEPTH_TEST);
		  // GL_LEQUAL深度函式測試
		  gl.glDepthFunc(GL10.GL_LEQUAL);
		  // 設定很好的角度計算模式
		  gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
		  
	}

}
