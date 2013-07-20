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
	
	public static boolean isDie;							//是否被擊中
	public static long timePrevious;						//
	public static boolean drawControlFlag=true;				//藉由控制這個屬性來達到revive時飛機閃爍
	
	private Square square;
	private Bullet bullet;
	private Coin coin;
	private ArrayList<Bullet> bulletList;					//宣告預備子彈的list	
	private ArrayList<Star> starList;						//宣告背景星星的list
	private ArrayList<Coin> coinList;						//宣告coin的list
	private double radius; 									//佈署子彈的圓之半徑(單位:像素)	
	private long timeNow,timeBetweenFrame;
	private int count;										//第幾個frame
	private ProbeCircle[] pCircleArray = new ProbeCircle[MyConstant.AIRPLANE_PROBE_PT.length];
	
	 public MyRender() {
		 Log.d("ABC","MyRender()...");
		 square = new Square();
		 bullet = new Bullet();
		 coin = new Coin();
		 count=0;		 
		 isDie=false;			//初始、重置為false，因為MainActivity的onCreate()比onResume()早呼叫，
	 }							//所以onResume()裡抓到的isDie就是重置過的

	
	@Override
	public void onDrawFrame(GL10 gl) {						
		timeNow=System.currentTimeMillis();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	//清除螢幕和深度緩衝區
		if(count == 0){			  
			Log.d("ABC","count == 0...");
//			gl.glLoadIdentity();			//將原點移至螢幕中心						後來發現這兩行寫在onSurfaceChanged比較好，因為myGlSurfaceView每呼叫
//			gl.glTranslatef(0, 0,-10);		//物體往螢幕內移動10單位，將景物與視點分離                    onResume()，會呼叫onSurfaceCreated、onSurfaceChanged
			player_positionX=0;				//繪製第一個frame時，玩家要出現在螢幕中央
			player_positionY=0;
			bulletList=new ArrayList<Bullet>();
			starList=new ArrayList<Star>();
			coinList=new ArrayList<Coin>();
			initBGStar();					//init background star			
		}
		if(count > 0){
			timeBetweenFrame=timeNow-timePrevious;	//算出幾微秒繪製一個Frame			  		  						 
			
			//準備(補充)星星
			if(starList.size() < MyConstant.STAR_NUM){
				prepareBGStar();
			}
			
			//畫背景星星
			for(int i =0 ; i < starList.size() ; i++){
				Star tmpStar = starList.get(i);
				//刪除跑到螢幕外的星星
				delBGStarOutOfScreen(tmpStar, i);
				gl.glPushMatrix();
				gl.glTranslatef((float)tmpStar.getStar_positionX(), (float)tmpStar.getStar_positionY(), 0);
				gl.glScalef(0.01f, 0.01f, 0.01f);
				float[] tmpColorArray=tmpStar.getColorArray();
				gl.glColor4f(tmpColorArray[0], tmpColorArray[1], tmpColorArray[2], tmpColorArray[3]);
				tmpStar.draw(gl);				
				gl.glPopMatrix();
				tmpStar.setStar_positionY(tmpStar.getStar_positionY() - tmpStar.getStar_velocity()*timeBetweenFrame/1000);
			}
			
			gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);	//還原畫筆為白色
			
			//每隔幾個Frame準備1個coin，有時會有連續產生2個甚至3個的狀況，因為timeBetweenFrame會變動，可能造成接近的frame餘數為1的情形
			if((count % (Math.ceil(MyConstant.COIN_TIME_INTERVAL*1000/timeBetweenFrame))) == 1 &&	
					coinList.size() < MyConstant.COIN_NUM){															
				prepareCoin(gl);
			}
			
			//this for-loop is used to draw coin.
			for(int i=0;i<coinList.size();i++){
				Coin c=coinList.get(i);
				delEatenCoin(c, i);
				gl.glPushMatrix();
				gl.glTranslatef((float)c.getCoin_positionX(), (float)c.getCoin_positionY(), 0);
				gl.glScalef(0.15f, 0.15f, 0.15f);
				c.draw(gl);
				gl.glFlush();	//*******
				gl.glPopMatrix();
			}
			
			//畫飛機
			gl.glPushMatrix();													//儲存目前gl狀態		 
			gl.glTranslatef(player_positionX, player_positionY, 0);
			gl.glScalef(0.25f, 0.25f, 0.25f);									//調整方形的大小
			gl.glRotatef(-45, 0, 0, 1);
			if(drawControlFlag)													//******
				square.draw(gl);		  										//畫出方形
			gl.glFlush();	//*********
			gl.glPopMatrix();													//回到上一個gl儲存點的狀態
			  		  		  		  			
			if((count % (Math.ceil(MyConstant.BULLET_TIME_INTERVAL*1000/timeBetweenFrame))) == 1 &&	//每隔幾個Frame準備1顆子彈
					bulletList.size() < MyConstant.BULLET_NUM){															
				prepareBullet(gl);
			}
						
			//this for-loop is used to draw bullet.
			for(int i=0;i<bulletList.size();i++){														  						  
					  Bullet b=bulletList.get(i);
					  deleteBullet(b,i);	//檢查是否需要消除子彈
					  gl.glPushMatrix();			  
					  gl.glTranslatef((float)(b.getBullet_positionX()), (float)(b.getBullet_positionY()), 0);	//平移
					  gl.glScalef(0.125f, 0.125f, 0.125f);
					  b.draw(gl);
					  gl.glFlush();	//*********
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
//		Log.d("Wang","ratio_pixToDist="+ ratio_pixToDist);
//		Log.d("Wang","x_screen="+x_screen+" y_screen="+y_screen);
//		Log.d("Wang","radius="+ radius);		  
		// 選擇MODELVIEW陣列
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		// 重設MODELVIEW陣列
		gl.glLoadIdentity();
		gl.glTranslatef(0, 0,-10);		//物體往螢幕內移動10單位，將景物與視點分離
		if(bulletList != null)			
			reLoadBulletTexture(gl);	//給原本就存在bulletList中的子彈材質
		if(coinList != null)			
			reLoadCoinTexture(gl);		//給原本就存在coinList中的coin材質
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
//		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.5f);	//*****方便測試用
		// 設定流暢的陰影模式
		gl.glShadeModel(GL10.GL_SMOOTH);
		// 啟動深度的測試
		gl.glEnable(GL10.GL_DEPTH_TEST);
		// GL_LEQUAL深度函式測試
		gl.glDepthFunc(GL10.GL_LEQUAL);
		
	    gl.glEnable(GL10.GL_ALPHA_TEST);  // *****Enable Alpha Testing (To Make BlackTansparent)  	    
	    gl.glAlphaFunc(GL10.GL_GREATER,0.1f);  // *****Set Alpha Testing (To Make Black Transparent)  
		
		//方形載入材質
		square.loadTexture(gl);
//		bullet.loadTexture(gl);
//		bulletList=new ArrayList<Bullet>();	//***搬到onDrawFrame()
//		starList=new ArrayList<Star>();
	}
	
	 public void setBitmap(Bitmap bitmap1,Bitmap bitmap2,Bitmap bitmap3) {
		 square.setBitmap(bitmap1);
		 bullet.setBitmap(bitmap2);
		 coin.setBitmap(bitmap3);
	 }
	
	//暫停後、繼續遊戲時必須為當下已存在bulletList內的子彈重新給他材質，因為呼叫onResume()事實上GL10的物件gl已經是新的
	private void reLoadBulletTexture(GL10 gl){
		for(int i=0;i<bulletList.size();i++){
			Bullet b=bulletList.get(i);
			b.loadTexture(gl);
		}
	}
	
	//暫停後、繼續遊戲時必須為當下已存在coinList內的coin重新給他材質，因為呼叫onResume()事實上GL10的物件gl已經是新的
	private void reLoadCoinTexture(GL10 gl){
		for(int i=0;i<coinList.size();i++){
			Coin c=coinList.get(i);
			c.loadTexture(gl);
		}
	}
	 
	//準備子彈
	private void prepareBullet(GL10 gl){															
		Bullet b=new Bullet();
		b.loadTexture(gl);
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
	
	//get pCircleArray.
	private void getProbeCircleArray(){
		for(int i=0;i<MyConstant.AIRPLANE_PROBE_PT.length;i++){
			pCircleArray[i] = new ProbeCircle(player_positionX + MyConstant.AIRPLANE_PROBE_PT[i][0] * MyConstant.COR_SCALE, 
					player_positionY + MyConstant.AIRPLANE_PROBE_PT[i][1] * MyConstant.COR_SCALE, 
					MyConstant.AIRPLANE_PROBE_PT[i][2] * MyConstant.COR_SCALE);
		}
	}
	
	//測試是否被子彈擊中
	private void checkDie(Bullet b){
		getProbeCircleArray();
//		ProbeCircle[] pCircleArray = new ProbeCircle[MyConstant.AIRPLANE_PROBE_PT.length];
//		for(int i=0;i<MyConstant.AIRPLANE_PROBE_PT.length;i++){
//			pCircleArray[i] = new ProbeCircle(player_positionX + MyConstant.AIRPLANE_PROBE_PT[i][0] * MyConstant.COR_SCALE, 
//					player_positionY + MyConstant.AIRPLANE_PROBE_PT[i][1] * MyConstant.COR_SCALE, 
//					MyConstant.AIRPLANE_PROBE_PT[i][2] * MyConstant.COR_SCALE);
//		}

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
	
	//初始背景星星
	private void initBGStar(){
		for(int i=0 ; i < MyConstant.STAR_NUM ; i++){
			Star tmpSatr = new Star();
			//(-width/2 ~ +width/2)/(ratio_pixToDist*0.01)
			tmpSatr.setStar_positionX(((Math.random()*x_screen)+(-x_screen/2)) / (ratio_pixToDist*0.01));
			tmpSatr.setStar_positionY(((Math.random()*y_screen)+(-y_screen/2)) / (ratio_pixToDist*0.01));
			//設定星星每秒往下移動的速度為5~25(像素/秒)
			tmpSatr.setStar_velocity(((int)(Math.random()*21)+5) / (ratio_pixToDist*0.01));							
			tmpSatr.setColorArray((float)Math.random(), (float)Math.random(), (float)Math.random(), 0.5f);			
			starList.add(tmpSatr);
		}		
	}
	
	//準備(補充)星星
	private void prepareBGStar(){
		Star tmpSatr = new Star();
		tmpSatr.setStar_positionX(((Math.random()*x_screen)+(-x_screen/2)) / (ratio_pixToDist*0.01));
		tmpSatr.setStar_positionY((y_screen/2) / (ratio_pixToDist*0.01));
		//設定星星每秒往下移動的速度為5~25(像素/秒)
		tmpSatr.setStar_velocity(((int)(Math.random()*21)+5) / (ratio_pixToDist*0.01));							
		tmpSatr.setColorArray((float)Math.random(), (float)Math.random(), (float)Math.random(), 0.5f);			
		starList.add(tmpSatr);
	}
	
	//刪除跑到螢幕外的星星
	private void delBGStarOutOfScreen(Star tmpStar, int i){
		if(tmpStar.getStar_positionY() < (-y_screen/2)/(ratio_pixToDist*0.01)){
			starList.remove(i);
		}
	}
	
	//準備(補充)coin
	private void prepareCoin(GL10 gl){
		Coin tmpCoin = new Coin();
		//假設coin的位置為螢幕大小往內縮20個像素
		tmpCoin.setCoin_positionX(((Math.random()*(x_screen - 40))+(-x_screen/2 + 20)) / (ratio_pixToDist*0.01));
		tmpCoin.setCoin_positionY(((Math.random()*(y_screen - 40))+(-y_screen/2 + 20)) / (ratio_pixToDist*0.01));
		tmpCoin.loadTexture(gl);
		coinList.add(tmpCoin);
	}
	
	//刪除吃掉的coin
	private void delEatenCoin(Coin c,int index){
		getProbeCircleArray();		
		//偵測碰撞迴圈
		for(int i=0;i<pCircleArray.length;i++){			
				ProbeCircle tmpPC=pCircleArray[i];
				double tmp_dx=c.getCoin_positionX() - tmpPC.getpCircle_positionX();	//coinX-圓心X
				double tmp_dy=c.getCoin_positionY() - tmpPC.getpCircle_positionY();	//coinY-圓心Y
				double tmpDistance=Math.sqrt(tmp_dx*tmp_dx + tmp_dy*tmp_dy);	//coin與圓心的距離
				if(tmpDistance <= tmpPC.getR()){	
					coinList.remove(index);
					MainActivity.coinGet++;					
					MainActivity.myGameHandler.sendEmptyMessage(MainActivity.TV_SHOWCOIN);
					break;
				}					
		}
	}
}
