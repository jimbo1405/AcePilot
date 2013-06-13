package com.demo.acepilot;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;

public class MyRender implements Renderer{
	
	public String motion="";					//用來識別動作的字串
	
	private float x_screen=0,y_screen=0;		//視窗的寬與高
	private float x_movePix,y_movePix;			//飛機x,y方向的像素位移
	private int x_move,y_move;					//飛機x,y方向的位移
	private float ratio_pixToDist;				//比值，座標上每單位相當於多少像素
	private boolean isStart=false;				//isStart為false，則會進行首次繪圖設定
	private Square square;
	private ArrayList<Bullet> bulletList;		//宣告預備子彈的list
	private final int num_bullet=50;			//設定預備子彈數目
	private double radius; 						//佈署子彈的圓之半徑(單位:像素)
	private double bullteAngle;					//佈署子彈的圓心角
		
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
			  x_movePix=0;
			  y_movePix=0;
			  gl.glLoadIdentity();										//將原點移至螢幕中心		
			  gl.glTranslatef(0, 0,-10);								//物體往螢幕內移動10單位，將景物與視點分離
			  gl.glScalef(0.25f, 0.25f, 0.25f);							//調整方形的大小
			  isStart=true;												//isStart設為true，接下來的繪圖將略過此步驟，避免畫面重置
		  }
		 
		  gl.glPushMatrix();											//儲存目前gl狀態		 
		  if(motion.equals("UP")){
			  if(y_movePix <= (y_screen/2)){
				  y_move++;												//y方向位移量+1
				  y_movePix=(float)(y_move*0.01*ratio_pixToDist*0.25);	//model位在負Z軸10單位處，而near平面位在0.1單位處，由於model的scale變更過，
			  }															//因此於model處1單位的移動量，於近平面的移動量要乘上(0.1/10)及0.25，並乘上比例轉成像素
			  Log.d("Wang","y_move="+y_move);			 
		  }else if(motion.equals("DOWN")){
			  if(y_movePix >= (-y_screen/2)){
				  y_move--;
				  y_movePix=(float)(y_move*0.01*ratio_pixToDist*0.25);
			  }
			  Log.d("Wang","y_move="+y_move);
		  }else if(motion.equals("LEFT")){
			  if(x_movePix >= (-x_screen/2)){
				  --x_move;
				  x_movePix=(float)(x_move*0.01*ratio_pixToDist*0.25);
			  }
			  Log.d("Wang","x_move="+x_move);
		  }else if(motion.equals("RIGHT")){
			  if(x_movePix <= (x_screen/2)){
				  ++x_move;
				  x_movePix=(float)(x_move*0.01*ratio_pixToDist*0.25);
			  }
			  Log.d("Wang","x_move="+x_move);
		  }		  
		  gl.glTranslatef(x_move, y_move, 0);							//決定x_move,y_move後進行位移
		  square.draw(gl);		  										//畫出方形
		  gl.glPopMatrix();												//回到上一個gl儲存點的狀態
		  		  		  		  
		  prepareBullet();		  
		  for(int i=0;i<bulletList.size();i++){														//畫出子彈起始位置的迴圈			  						  
			  Bullet b=bulletList.get(i);
			  gl.glPushMatrix();
			  gl.glTranslatef((float)(b.getBullet_moveX()), (float)(b.getBullet_moveY()), 0);		//平移
			  gl.glScalef(0.25f, 0.25f, 0.25f);			  
			  b.draw(gl);			  
			  deleteBullet(b,i);			  			  											//檢查是否需要消除子彈
			  gl.glPopMatrix();			  			  
			  
			  b.setBullet_moveX(b.getBullet_moveX()+b.getBullet_flyX());							//設定當前子彈下一個frame時x方向的位移
			  b.setBullet_moveY(b.getBullet_moveY()+b.getBullet_flyY());							//設定當前子彈下一個frame時y方向的位移
			  b.setFly_moveX(b.getFly_moveX()+b.getBullet_flyX());									//設定當前子彈x方向飛行總位移
			  b.setFly_moveY(b.getFly_moveY()+b.getBullet_flyY());			  						//設定當前子彈y方向飛行總位移
		  }
		  
//		  gl.glPushMatrix();	
//		  gl.glTranslatef(-4, 0, 0);
//		  gl.glScalef(0.25f, 0.25f, 0.25f);
//		  new Bullet().draw(gl);		  
//		  gl.glPopMatrix();

		  // 第一個方形
		  // 存儲目前陣列
//		  gl.glPushMatrix();
		  // 反時鐘旋轉
//		  gl.glRotatef(angle, 0, 0, 1);
		  // 畫出第一個方形
//		  square.draw(gl);
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
		  radius=0.5*Math.sqrt(width*width + height*height);					//設定佈署子彈的圓之半徑=畫面的斜邊長
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
		  
		  bulletList=new ArrayList<Bullet>();
	}
	
	//準備子彈
	private void prepareBullet(){															
		bullteAngle=0;																		//子彈圓心角初始為0
		if(bulletList.size() == 0){															//當bulletList的長度=0時
			bulletList=new ArrayList<Bullet>();												//bulletList就再指向一個新的ArrayList
			while(bulletList.size() <= num_bullet){											//當bulletList的長度<=指定的子彈樹目時，進入迴圈				
				  Bullet tmpBullet=new Bullet();			  
				  
				  double tmpRadius=(radius/ratio_pixToDist)/(0.25*0.01);					//將半徑從像素轉換成座標上的單位
				  tmpBullet.setBullet_moveX(tmpRadius*Math.cos(bullteAngle*Math.PI/180));	//設定當前子彈初始x方向的位移
				  tmpBullet.setBullet_moveY(tmpRadius*Math.sin(bullteAngle*Math.PI/180));	//設定當前子彈初始y方向的位移
				  tmpBullet.setBullet_flyX(((2*Math.random()-1)));							//設定當前子彈x方向的飛行位移，-1~1，可乘上係數調整飛行速度
				  tmpBullet.setBullet_flyY(((2*Math.random()-1)));							//設定當前子彈y方向的飛行位移，-1~1，可乘上係數調整飛行速度
				  
				  bulletList.add(tmpBullet);												
				  bullteAngle=bullteAngle + 360/num_bullet;
			  }		  		  
		}		
	}
	
	//消除飛出圓周之外的子彈，傳入整數i表示當前bulletList的index值
	private void deleteBullet(Bullet b,int i){
//		Log.d("Wang","tmpFly="+Math.sqrt(b.getFly_moveX()*b.getFly_moveX() + b.getFly_moveY()*b.getFly_moveY()));
		double tmpFly=Math.sqrt(b.getFly_moveX()*b.getFly_moveX() + b.getFly_moveY()*b.getFly_moveY());			//當前子彈的飛行總位移
		double tmpRadius=(radius/ratio_pixToDist)/(0.25*0.01);													//將半徑從像素轉換成座標上的單位
		if(tmpFly > 2*tmpRadius)																				//飛行總位移>直徑
			bulletList.remove(i);																				//則從bulletList刪除當前子彈
	}
	

	
}
