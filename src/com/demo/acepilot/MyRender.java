package com.demo.acepilot;

import java.util.ArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;

public class MyRender implements Renderer{	
	public static float player_positionX,player_positionY;	//	
	public static float x_screen=0,y_screen=0;				//�������e�P��	
	public static float ratio_pixToDist;					//��ȡA�y�ФW�C���۷��h�ֹ���
	private Square square;
	private ArrayList<Bullet> bulletList;					//�ŧi�w�Ƥl�u��list	
	private double radius; 									//�G�p�l�u���ꤧ�b�|(���:����)	
	public static boolean isDie;							//�O�_�Q����
	private long timePrevious,timeNow,timeBetweenFrame;
	private int count;
	
	 public MyRender() {
		 Log.d("ABC","MyRender()...");
		 square = new Square();	
		 count=0;		 
		 isDie=false;			//��l�B���m��false�A�]��MainActivity��onCreate()��onResume()���I�s�A
	 }							//�ҥHonResume()�̧�쪺isDie�N�O���m�L��

	
	@Override
	public void onDrawFrame(GL10 gl) {		
		timeNow=System.currentTimeMillis();		
		if(count == 0){			  
			gl.glLoadIdentity();			//�N���I���ܿù�����		
			gl.glTranslatef(0, 0,-10);		//���驹�ù�������10���A�N�����P���I����
			player_positionX=0;				//ø�s�Ĥ@��frame�ɡA���a�n�X�{�b�ù�����
			player_positionY=0;
		}
		if(count > 0){
			timeBetweenFrame=timeNow-timePrevious;	//��X�X�L��ø�s�@��Frame
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	//�M���ù��M�`�׽w�İ�			  		  						 
			gl.glPushMatrix();													//�x�s�ثegl���A		 
			gl.glTranslatef(player_positionX, player_positionY, 0);
			gl.glScalef(0.25f, 0.25f, 0.25f);									//�վ��Ϊ��j�p
			square.draw(gl);		  											//�e�X���
			gl.glPopMatrix();													//�^��W�@��gl�x�s�I�����A
			  		  		  		  			
			if((count % (Math.ceil(MyConstant.BULLET_TIME_INTERVAL*1000/timeBetweenFrame))) == 1 &&
					bulletList.size() < MyConstant.BULLET_NUM){	//�C�j�X��Frame�ǳƤ@���l�u
				
				prepareBullet();
			}
						
			for(int i=0;i<bulletList.size();i++){														  						  
					  Bullet b=bulletList.get(i);
					  deleteBullet(b,i);	//�ˬd�O�_�ݭn�����l�u
					  gl.glPushMatrix();			  
					  gl.glTranslatef((float)(b.getBullet_positionX()), (float)(b.getBullet_positionY()), 0);	//����
					  gl.glScalef(0.0400f, 0.0400f, 0.0400f);			  
					  b.draw(gl);			  					  
					  gl.glPopMatrix();			  			  					  
					  checkDie(b);										  
					  b.setBullet_positionX(b.getBullet_positionX() - 
							  b.getBullet_fly()*Math.cos(b.getBulletFlyAngle()*Math.PI/180)*timeBetweenFrame/1000);	 	//�]�w��e�l�u�U�@��frame��x��V���첾
					  b.setBullet_positionY(b.getBullet_positionY() - 
							  b.getBullet_fly()*Math.sin(b.getBulletFlyAngle()*Math.PI/180)*timeBetweenFrame/1000);		//�]�w��e�l�u�U�@��frame��y��V���첾
					  b.setBullet_totalFly(b.getBullet_totalFly() + b.getBullet_fly()*timeBetweenFrame/1000);			//�]�w��e�l�u�����`�첾
			}			 		
		}
		timePrevious=timeNow;
		count++;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		Log.d("ABC","onSurfaceChanged(GL10 gl, int width, int height)...");
		// �]�w�s����������j�p
		  gl.glViewport(0, 0, width, height);
		  // ��ܧ�g���}�C�Ҧ�
		  gl.glMatrixMode(GL10.GL_PROJECTION);
		  // ���]��g�}
		  gl.glLoadIdentity();
		  // �p��������e����v
		  GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
		    100.0f);
		  
		  x_screen=width;
		  y_screen=height;
		  ratio_pixToDist=(float)(height/(2*Math.tan(22.5*Math.PI/180)*0.1));	//�ù��W��1���۷��h�ֹ���
		  radius=Math.sqrt(width*width + height*height);					//�]�w�G�p�l�u���ꤧ�b�|=�e���������
		  Log.d("Wang","ratio_pixToDist="+ ratio_pixToDist);
		  Log.d("Wang","x_screen="+x_screen+" y_screen="+y_screen);
		  Log.d("Wang","radius="+ radius);		  
		  // ���MODELVIEW�}�C
		  gl.glMatrixMode(GL10.GL_MODELVIEW);
		  // ���]MODELVIEW�}�C
		  gl.glLoadIdentity(); 
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		Log.d("ABC","onSurfaceCreated(GL10 gl, EGLConfig config)...");
		// �]�w�I���C�⬰�¦�, �榡�ORGBA
		  gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
		  // �]�w�y�Z�����v�Ҧ�
		  gl.glShadeModel(GL10.GL_SMOOTH);
		  // �`�׽w�Ϫ��]�w
		  gl.glClearDepthf(1.0f);
		  // �Ұʲ`�ת�����
		  gl.glEnable(GL10.GL_DEPTH_TEST);
		  // GL_LEQUAL�`�ר禡����
		  gl.glDepthFunc(GL10.GL_LEQUAL);
		  // �]�w�ܦn�����׭p��Ҧ�
		  gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		  		  
		  bulletList=new ArrayList<Bullet>();
	}
	
	//�ǳƤl�u
	private void prepareBullet(){															
		Bullet b=new Bullet();			  				  
		double tmpRadius=(radius/ratio_pixToDist)/0.01;								//�N�b�|�q�����ഫ���y�ФW�����				  
		double tmpAngle=Math.ceil(Math.random()*360);								//�l�u��ߨ�
		b.setBullet_positionX(tmpRadius*Math.cos(tmpAngle*Math.PI/180));	//�]�w��e�l�u��lx��V���첾
		b.setBullet_positionY(tmpRadius*Math.sin(tmpAngle*Math.PI/180));	//�]�w��e�l�u��ly��V���첾				  
		b.setBullet_fly(MyConstant.BULLET_VELOCITY/(ratio_pixToDist*0.01));						 				  
		b.setBulletAngle(tmpAngle);											//�]�w�l�u��ߨ�				  																
		double vectorX=b.getBullet_positionX()-player_positionX;
		double vectorY=b.getBullet_positionY()-player_positionY;
		double tmpCos=(vectorX)/(Math.sqrt(vectorX*vectorX + vectorY*vectorY));			  		
		double tmpFlyAngle=Math.acos(tmpCos)*(180/Math.PI);
		if((vectorX < 0 && vectorY < 0) || (vectorX > 0 && vectorY < 0))
			tmpFlyAngle = 360 - tmpFlyAngle;			
		b.setBulletFlyAngle(tmpFlyAngle);
		bulletList.add(b);
	}		
	
	//�������X��P���~���l�u�A�ǤJ���i��ܷ�ebulletList��index��
	private void deleteBullet(Bullet b,int i){
		double tmpFly=b.getBullet_totalFly();			//��e�l�u�������`�첾
		double tmpRadius=(radius/ratio_pixToDist)/0.01;	//�N�b�|�q�����ഫ���y�ФW�����
		
		//if�P�_�l�u�����b�ù��~ �B �ܤ֭��F�b�|���ת��Z���~��qlist�R��
		if(Math.abs(b.getBullet_positionX()) > (0.5*x_screen/ratio_pixToDist)/0.01 || 
				Math.abs(b.getBullet_positionY()) > (0.5*y_screen/ratio_pixToDist)/0.01){
			if(tmpFly > tmpRadius)
				bulletList.remove(i);
		}

	}
	
	//���լO�_�Q�l�u����
	private void checkDie(Bullet b){
//		if((b.getBullet_positionX() >= MyRender.player_positionX - 0.25 && b.getBullet_positionX() <= MyRender.player_positionX + 0.25) &&
//				(b.getBullet_positionY() >= MyRender.player_positionY - 0.25 && b.getBullet_positionY() <= MyRender.player_positionY + 0.25)){
//			isDie=true;
//		}
		ProbeCircle[] pCircleArray={new ProbeCircle(player_positionX + 0.25, player_positionY + 0.25, 0.177),	//�����I����}�C
				new ProbeCircle(player_positionX - 0.25, player_positionY + 0.25, 0.177),
				new ProbeCircle(player_positionX - 0.25, player_positionY - 0.25, 0.177),
				new ProbeCircle(player_positionX + 0.25, player_positionY - 0.25, 0.177)};	
		
		//�����I���j��
		for(int i=0;i<pCircleArray.length;i++){			
				ProbeCircle tmpPC=pCircleArray[i];
				double tmp_dx=b.getBullet_positionX() - tmpPC.getpCircle_positionX();	//�l�uX-���X
				double tmp_dy=b.getBullet_positionY() - tmpPC.getpCircle_positionY();	//�l�uY-���Y
				double tmpDistance=Math.sqrt(tmp_dx*tmp_dx + tmp_dy*tmp_dy);	//�l�u�P��ߪ��Z��
				if(tmpDistance <= tmpPC.getR()){	//�Y�Z���p��b�|�hisDie��true�ø��X�j��
					isDie=true;
					break;
				}					
		}
	}	
		
}
