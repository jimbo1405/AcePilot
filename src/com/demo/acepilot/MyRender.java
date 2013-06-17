package com.demo.acepilot;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;
import android.widget.Toast;

public class MyRender implements Renderer{
	
	public static float player_positionX,player_positionY;
	public String motion="";					//用來識別動作的字串
	
	public static float x_screen=0,y_screen=0;		//視窗的寬與高
	private float x_movePix,y_movePix;			//飛機x,y方向的像素位移
	private int x_move,y_move;					//飛機x,y方向的位移
	public static float ratio_pixToDist;				//比值，座標上每單位相當於多少像素
	private boolean isStart=false;				//isStart為false，則會進行首次繪圖設定
	private Square square;
	private ArrayList<Bullet> bulletList;		//宣告預備子彈的list
	private final int num_bullet=30;			//設定預備子彈數目
	private double radius; 						//佈署子彈的圓之半徑(單位:像素)
	private float tmpPlayer_positionX=0,tmpPlayer_positionY=0;
	public static boolean isDie;						//是否被擊中	
//	private float angle = 0;

	 public MyRender() {
		 Log.d("ABC","MyRender()...");
		 square = new Square();	
		 isDie=false;			//初始、重置為false，因為MainActivity的onCreate()比onResume()早呼叫，
	 }							//所以onResume()裡抓到的isDie就是重置過的

	
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
//		  if(motion.equals("UP")){
//			  if(y_movePix <= (y_screen/2)){
//				  y_move++;												//y方向位移量+1
//				  y_movePix=(float)(y_move*0.01*ratio_pixToDist*0.25);	//model位在負Z軸10單位處，而near平面位在0.1單位處，由於model的scale變更過，
//			  }															//因此於model處1單位的移動量，於近平面的移動量要乘上(0.1/10)及0.25，並乘上比例轉成像素
//			  Log.d("Wang","y_move="+y_move);			 
//		  }else if(motion.equals("DOWN")){
//			  if(y_movePix >= (-y_screen/2)){
//				  y_move--;
//				  y_movePix=(float)(y_move*0.01*ratio_pixToDist*0.25);
//			  }
//			  Log.d("Wang","y_move="+y_move);
//		  }else if(motion.equals("LEFT")){
//			  if(x_movePix >= (-x_screen/2)){
//				  --x_move;
//				  x_movePix=(float)(x_move*0.01*ratio_pixToDist*0.25);
//			  }
//			  Log.d("Wang","x_move="+x_move);
//		  }else if(motion.equals("RIGHT")){
//			  if(x_movePix <= (x_screen/2)){
//				  ++x_move;
//				  x_movePix=(float)(x_move*0.01*ratio_pixToDist*0.25);
//			  }
//			  Log.d("Wang","x_move="+x_move);
//		  }		  
//		  gl.glTranslatef(x_move, y_move, 0);							//決定x_move,y_move後進行位移
		  gl.glTranslatef(player_positionX, player_positionY, 0);
		  square.draw(gl);		  										//畫出方形
		  gl.glPopMatrix();												//回到上一個gl儲存點的狀態
		  		  		  		  
		  if(bulletList.size() == 0){
			  tmpPlayer_positionX = player_positionX;
			  tmpPlayer_positionY = player_positionY;
		  }		  
		  prepareBullet();		 
		//畫出子彈起始位置的迴圈
		  for(int i=0;i<bulletList.size();i++){														  						  
			  Bullet b=bulletList.get(i);
			  gl.glPushMatrix();			  
			  gl.glTranslatef((float)(b.getBullet_positionX()), (float)(b.getBullet_positionY()), 0);		//平移
			  gl.glScalef(0.25f, 0.25f, 0.25f);			  
			  b.draw(gl);			  
			  deleteBullet(b,i);			  			  								//檢查是否需要消除子彈
			  gl.glPopMatrix();			  			  
			  
			  checkDie(b);
			  
			  b.setBullet_positionX(b.getBullet_positionX() - 
					  b.getBullet_fly()*Math.cos(b.getBulletAngle()*Math.PI/180));	 	//設定當前子彈下一個frame時x方向的位移
			  b.setBullet_positionY(b.getBullet_positionY() - 
					  b.getBullet_fly()*Math.sin(b.getBulletAngle()*Math.PI/180));		//設定當前子彈下一個frame時y方向的位移
			  b.setBullet_totalFly(b.getBullet_totalFly() + b.getBullet_fly());			//設定當前子彈飛行總位移
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
		double tmpAngle=0;																	//子彈圓心角初始為0
		if(bulletList.size() == 0){															//當bulletList的長度=0時
			bulletList=new ArrayList<Bullet>();												//bulletList就再指向一個新的ArrayList
			while(bulletList.size() <= num_bullet){											//當bulletList的長度<=指定的子彈樹目時，進入迴圈				
				  Bullet tmpBullet=new Bullet();			  
				  
				  double tmpRadius=(radius/ratio_pixToDist)/(0.25*0.01);					//將半徑從像素轉換成座標上的單位				  
				  tmpBullet.setBullet_positionX(tmpRadius*Math.cos(tmpAngle*Math.PI/180) + tmpPlayer_positionX);	//設定當前子彈初始x方向的位移
				  tmpBullet.setBullet_positionY(tmpRadius*Math.sin(tmpAngle*Math.PI/180) + tmpPlayer_positionY);	//設定當前子彈初始y方向的位移
				  /*
				  tmpBullet.setBullet_flyX(((2*Math.random()-1)));							//設定當前子彈x方向的飛行位移，-1~1，可乘上係數調整飛行速度
				  tmpBullet.setBullet_flyY(((2*Math.random()-1)));							//設定當前子彈y方向的飛行位移，-1~1，可乘上係數調整飛行速度
				  */
				  tmpBullet.setBullet_fly((Math.random()+1)/10);							//設定飛行距離，0.1~0.1999...				 
				  
				  tmpBullet.setBulletAngle(tmpAngle);										//設定子彈圓心角
				  
				  bulletList.add(tmpBullet);												
				  tmpAngle=tmpAngle + 360/num_bullet;
			  }		  		  
		}		
	}
	
	//消除飛出圓周之外的子彈，傳入整數i表示當前bulletList的index值
	private void deleteBullet(Bullet b,int i){
		double tmpFly=b.getBullet_totalFly();													//當前子彈的飛行總位移
		double tmpRadius=(radius/ratio_pixToDist)/(0.25*0.01);									//將半徑從像素轉換成座標上的單位
		
		//if判斷子彈必須在螢幕外 且 至少飛了半徑長度的距離才能從list刪掉
		if(Math.abs(b.getBullet_positionX()) > (0.5*x_screen/ratio_pixToDist)/(0.25*0.01) || 
				Math.abs(b.getBullet_positionY()) > (0.5*y_screen/ratio_pixToDist)/(0.25*0.01)){
			if(tmpFly > tmpRadius)
				bulletList.remove(i);
		}

	}
	
	//測試是否被子彈擊中
	private void checkDie(Bullet b){
		if((b.getBullet_positionX() >= MyRender.player_positionX - 1 && b.getBullet_positionX() <= MyRender.player_positionX + 1) &&
				(b.getBullet_positionY() >= MyRender.player_positionY - 1 && b.getBullet_positionY() <= MyRender.player_positionY + 1)){
			isDie=true;
		}
	}		

	
}
