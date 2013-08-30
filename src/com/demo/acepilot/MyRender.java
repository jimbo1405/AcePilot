package com.demo.acepilot;

import java.util.ArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.demo.acepilot.engine.Audio.SFX;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;

public class MyRender implements Renderer{	
	public static float player_positionX,player_positionY;	//	
	public static float x_screen=0,y_screen=0;				//window w & h	

	public static boolean isDie;							//hit or not
	public static long timePrevious;						//
	public static boolean drawControlFlag=true;				//used for revive flashing

	private Square square;
	private Bullet bullet;
	private Coin coin;
	private ArrayList<Bullet> bulletList;					//list for bullets	
	private ArrayList<Star> starList;						//list for background stars
	private ArrayList<Coin> coinList;						//list for coins
	private double radius; 									//bullets deployment radius in pixels	
	private long timeNow,timeBetweenFrame;
	private int count;										//the order of frame
	private ProbeCircle[] pCircleArray = new ProbeCircle[MyConstant.AIRPLANE_PROBE_PT.length];

	 public MyRender() {
		 Log.d("ABC","MyRender()...");
		 square = new Square(MyConstant.AIRPLANE_W, MyConstant.AIRPLANE_H);
		 bullet = new Bullet(MyConstant.BULLET_W, MyConstant.BULLET_H);
		 coin = new Coin(MyConstant.COIN_W, MyConstant.COIN_H);
		 count=0;		 
		 isDie=false;			//reset it as false. Because in MainActivity, onCreate() is called earlier than onResume().
	 }							//Therefore, the "isDie" in onResume() will have been reset.


	@Override
	public void onDrawFrame(GL10 gl) {						
		timeNow=System.currentTimeMillis();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	//clear screen & depth buffer
		if(count == 0){			  
			Log.d("ABC","count == 0...");
			player_positionX=0;				//in 1st frame, put player in the center
			player_positionY=0;
			bulletList=new ArrayList<Bullet>();
			starList=new ArrayList<Star>();
			coinList=new ArrayList<Coin>();
			initBGStar();					//init background star			
		}
		if(count > 0){
			timeBetweenFrame=timeNow-timePrevious;	//calculate time span between two consecutive frames		  		  						 
			//Log.d("frametime", "timeBetweenFrame="+timeBetweenFrame);
			//prepare(refill)stars
			if(starList.size() < MyConstant.STAR_NUM){
				prepareBGStar();
			}

			//draw background stars
			for(int i =0 ; i < starList.size() ; i++){
				Star tmpStar = starList.get(i);
				//remove stars out of screen
				delBGStarOutOfScreen(tmpStar, i);
				gl.glPushMatrix();
				gl.glTranslatef((float)tmpStar.getStar_positionX(), (float)tmpStar.getStar_positionY(), 0);
				float[] tmpColorArray=tmpStar.getColorArray();
				gl.glColor4f(tmpColorArray[0], tmpColorArray[1], tmpColorArray[2], tmpColorArray[3]);
				tmpStar.draw(gl);				
				gl.glPopMatrix();
				tmpStar.setStar_positionY(tmpStar.getStar_positionY() - tmpStar.getStar_velocity()*timeBetweenFrame/1000);
			}

			gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);	//set pen as white

			//prepare one coin per several frames. Sometimes 2~3 coins might be prepared due to fluctuation of timeBetweenFrame, so that remainder might be 1.
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
				c.draw(gl);
				gl.glPopMatrix();
			}

			//畫飛機
			gl.glPushMatrix();													//save current gl state		 
			gl.glTranslatef(player_positionX, player_positionY, 0);
			gl.glRotatef(-45, 0, 0, 1);
			if(drawControlFlag)													//******
				square.draw(gl);		  										//draw square
			gl.glPopMatrix();													//restore gl state

			if((count % (Math.ceil(MyConstant.BULLET_TIME_INTERVAL*1000/timeBetweenFrame))) == 1 &&	//prepare a bullet per several frames
					bulletList.size() < MyConstant.BULLET_NUM){															
				prepareBullet(gl);
			}

			//this for-loop is used to draw bullet.
			for(int i=0;i<bulletList.size();i++){														  						  
					  Bullet b=bulletList.get(i);
					  deleteBullet(b,i);	//check if bullets need be removed or not
					  gl.glPushMatrix();			  
					  gl.glTranslatef((float)(b.getBullet_positionX()), (float)(b.getBullet_positionY()), 0);	//translate
					  b.draw(gl);
					  gl.glPopMatrix();					 
					  checkDie(b);										  
					  b.setBullet_positionX(b.getBullet_positionX() - 
							  b.getBullet_fly()*Math.cos(b.getBulletFlyAngle()*Math.PI/180)*timeBetweenFrame/1000);	 	//set x position in next frame
					  b.setBullet_positionY(b.getBullet_positionY() - 
							  b.getBullet_fly()*Math.sin(b.getBulletFlyAngle()*Math.PI/180)*timeBetweenFrame/1000);		//set y position in next frame
					  b.setBullet_totalFly(b.getBullet_totalFly() + b.getBullet_fly()*timeBetweenFrame/1000);			//set total amount of movement
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
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		// calc window w/h ratio
		float properTranslateDist = (float) ((height/2)/Math.tan(22.5*Math.PI/180));
		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, properTranslateDist+10);		  
		x_screen=width;
		y_screen=height;
		radius=Math.sqrt(width*width + height*height);	//set radius of bullet deployment as diagonal length of screen
//		Log.d("Wang","x_screen="+x_screen+" y_screen="+y_screen);
//		Log.d("Wang","radius="+ radius);		  
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0, 0, -properTranslateDist); //move drawing plan to -properTranslateDist such that 1pxl on drawing plan can be projected as 1 pxl on projected plan
		if(bulletList != null)			
			reLoadBulletTexture(gl);	//provide texture for existing bullets in bulletList
		if(coinList != null)			
			reLoadCoinTexture(gl);		//provide texture for existing coins in coinList
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		Log.d("ABC","onSurfaceCreated(GL10 gl, EGLConfig config)...");
        gl.glDisable(GL10.GL_DITHER);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		// set background as black (RGBA)
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
//		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.5f);	//*****for testing
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
	    gl.glEnable(GL10.GL_ALPHA_TEST);  // *****Enable Alpha Testing (To Make BlackTansparent)  	    
	    gl.glAlphaFunc(GL10.GL_GREATER,0.1f);  // *****Set Alpha Testing (To Make Black Transparent)  

		square.loadTexture(gl);
//		bullet.loadTexture(gl);
//		bulletList=new ArrayList<Bullet>();	//***move to onDrawFrame()
//		starList=new ArrayList<Star>();
	}

	 public void setBitmap(Bitmap bitmap1,Bitmap bitmap2,Bitmap bitmap3) {
		 square.setBitmap(bitmap1);
		 bullet.setBitmap(bitmap2);
		 coin.setBitmap(bitmap3);
	 }

	// after pause/resume, reload texture is required, because gl object in on Resume is a different new one
	private void reLoadBulletTexture(GL10 gl){
		for(int i=0;i<bulletList.size();i++){
			Bullet b=bulletList.get(i);
			b.loadTexture(gl);
		}
	}

	// after pause/resume, reload texture is required, because gl object in on Resume is a different new one
	private void reLoadCoinTexture(GL10 gl){
		for(int i=0;i<coinList.size();i++){
			Coin c=coinList.get(i);
			c.loadTexture(gl);
		}
	}

	//prepare bullet
	private void prepareBullet(GL10 gl){															
		Bullet b=new Bullet(MyConstant.BULLET_W, MyConstant.BULLET_H);
		b.loadTexture(gl);
		double tmpRadius=radius;                                             			  
		double tmpAngle=Math.ceil(Math.random()*360);                        
		b.setBullet_positionX(tmpRadius*Math.cos(tmpAngle*Math.PI/180));     //set init x movement for current bullet
		b.setBullet_positionY(tmpRadius*Math.sin(tmpAngle*Math.PI/180));     //set init y movement for current bullet			  
		b.setBullet_fly(MyConstant.BULLET_VELOCITY);						 				  
		b.setBulletAngle(tmpAngle);				  																
		double vectorX=b.getBullet_positionX()-player_positionX;
		double vectorY=b.getBullet_positionY()-player_positionY;
		double tmpCos=(vectorX)/(Math.sqrt(vectorX*vectorX + vectorY*vectorY));			  		
		double tmpFlyAngle=Math.acos(tmpCos)*(180/Math.PI);
		if((vectorX < 0 && vectorY < 0) || (vectorX > 0 && vectorY < 0))
			tmpFlyAngle = 360 - tmpFlyAngle;			
		b.setBulletFlyAngle(tmpFlyAngle);
		bulletList.add(b);
	}		

	//remove bullets out of screen, here "int i" is the index in bulletlist
	private void deleteBullet(Bullet b,int i){
		double tmpFly=b.getBullet_totalFly();        //total flying  amount of current bullet
		double tmpRadius=radius;

		//remove bullet only if "out of screen" and "flying over deployment radius"
		if(Math.abs(b.getBullet_positionX()) > (0.5*x_screen) || 
				Math.abs(b.getBullet_positionY()) > (0.5*y_screen)){
			if(tmpFly > tmpRadius)
				bulletList.remove(i);
		}
	}

	//get pCircleArray.
	private void getProbeCircleArray(){
		for(int i=0;i<MyConstant.AIRPLANE_PROBE_PT.length;i++){
			pCircleArray[i] = new ProbeCircle(player_positionX + MyConstant.AIRPLANE_PROBE_PT[i][0] * MyConstant.AIRPLANE_W, 
					player_positionY + MyConstant.AIRPLANE_PROBE_PT[i][1] * MyConstant.AIRPLANE_H, 
					MyConstant.AIRPLANE_PROBE_PT[i][2] * MyConstant.AIRPLANE_W);
		}
	}

	//test if hit by bullet or not
	private void checkDie(Bullet b){
		getProbeCircleArray();

		//collision detection
		for(int i=0;i<pCircleArray.length;i++){			
				ProbeCircle tmpPC=pCircleArray[i];
				double tmp_dx=b.getBullet_positionX() - tmpPC.getpCircle_positionX();	//(center) bullet x - circle x
				double tmp_dy=b.getBullet_positionY() - tmpPC.getpCircle_positionY();	//(center) bullet y - circle y
				double tmpDistance=Math.sqrt(tmp_dx*tmp_dx + tmp_dy*tmp_dy);	//dist between (center) bullet & circle
				if(tmpDistance <= tmpPC.getR()){	//if dist < radius then isDie
					isDie=true;
					break;
				}					
		}
	}

	//init background stars
	private void initBGStar(){
		for(int i=0 ; i < MyConstant.STAR_NUM ; i++){
			Star tmpSatr = new Star(MyConstant.BGSTAR_W, MyConstant.BGSTAR_H);
			//(-width/2 ~ +width/2)
			tmpSatr.setStar_positionX(((Math.random()*x_screen)+(-x_screen/2)));
			tmpSatr.setStar_positionY(((Math.random()*y_screen)+(-y_screen/2)));
			//set moving down speed as 5~25(pxl/sec)
			tmpSatr.setStar_velocity(((int)(Math.random()*21)+5));							
			tmpSatr.setColorArray((float)Math.random(), (float)Math.random(), (float)Math.random(), 0.5f);			
			starList.add(tmpSatr);
		}		
	}

	//prepare(refill) stars
	private void prepareBGStar(){
		Star tmpSatr = new Star(MyConstant.BGSTAR_W, MyConstant.BGSTAR_H);
		tmpSatr.setStar_positionX(((Math.random()*x_screen)+(-x_screen/2)));
		tmpSatr.setStar_positionY((y_screen/2));
		//set moving down speed as 5~25(pxl/sec)
		tmpSatr.setStar_velocity(((int)(Math.random()*21)+5));							
		tmpSatr.setColorArray((float)Math.random(), (float)Math.random(), (float)Math.random(), 0.5f);			
		starList.add(tmpSatr);
	}

	//remove stars out of screen
	private void delBGStarOutOfScreen(Star tmpStar, int i){
		if(tmpStar.getStar_positionY() < (-y_screen/2)){
			starList.remove(i);
		}
	}

	//prepare(refill) coin
	private void prepareCoin(GL10 gl){
		Coin tmpCoin = new Coin(MyConstant.COIN_W, MyConstant.COIN_H);
		//let available placement for coins be a 20pxl shrinked screen
		tmpCoin.setCoin_positionX(((Math.random()*(x_screen - 40))+(-x_screen/2 + 20)));
		tmpCoin.setCoin_positionY(((Math.random()*(y_screen - 40))+(-y_screen/2 + 20)));
		tmpCoin.loadTexture(gl);
		coinList.add(tmpCoin);
	}

	//remove coins collected by player
	private void delEatenCoin(Coin c,int index){
		getProbeCircleArray();		
		//collision detection
		for(int i=0;i<pCircleArray.length;i++){			
				ProbeCircle tmpPC=pCircleArray[i];
				double tmp_dx=c.getCoin_positionX() - tmpPC.getpCircle_positionX();	//(center) coin x - circle x
				double tmp_dy=c.getCoin_positionY() - tmpPC.getpCircle_positionY();	//(center) coin y - circle y
				double tmpDistance=Math.sqrt(tmp_dx*tmp_dx + tmp_dy*tmp_dy);	//dist between (center) bullet & circle
				if(tmpDistance <= tmpPC.getR()){	
					//MainActivity.sp.play(MainActivity.coinSound, 1, 1, 0, 0, 1);
					MainActivity.audio.play(SFX.COIN);
					coinList.remove(index);
					MainActivity.coinGet++;
					MainActivity.totalCoin++;
					MainActivity.myGameHandler.sendEmptyMessage(MainActivity.TV_SHOWCOIN);
					break;
				}					
		}
	}
}