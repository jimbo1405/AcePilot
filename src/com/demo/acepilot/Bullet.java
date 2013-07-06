package com.demo.acepilot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

public class Bullet extends Circle{
	private double bullet_positionX;	//子彈X位置
	private double bullet_positionY;	//子彈Y位置
	private double bullet_fly;			//子彈朝玩家飛行的速度(景物單位/秒)	
	private double bullet_totalFly;		//子彈飛行總位移	
	private double bulletAngle;			//子彈的圓心角
	private double bulletFlyAngle;		//子彈位置對玩家之向量與(1,0)向量之夾角(0~360)
	
	private FloatBuffer textureBuffer;
	private static Bitmap bitmap;
	private int texture;
	
	// 質地坐標
	 private float[] textureCor = { 0.0f, 0.0f, //
			   0.0f,1.0f, //
			  1.0f, 1.0f, //
			   1.0f, 0.0f, //
			 };
	
	public Bullet(){
		super();
		setBullet_positionX(0);
		setBullet_positionY(0);
		setBullet_fly(0);
		setBullet_totalFly(0);
		
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(textureCor.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(textureCor);
		textureBuffer.position(0);
	}
	
	@Override
	public void draw(GL10 gl) {
		// 启用2D纹理贴图
	    gl.glEnable(GL10.GL_TEXTURE_2D);
	    
		gl.glFrontFace(GL10.GL_CCW);
		// 啟動CULL_FACE
		gl.glEnable(GL10.GL_CULL_FACE);
		// 刪除多邀形的背景
		gl.glCullFace(GL10.GL_BACK);
	    
	    // 使用UV坐標
	 	gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		// 指定質地緩衝區
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		//設定當前顏色為透明
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		// 执行纹理贴图
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);

		super.draw(gl);
		
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
	
	 public void setBitmap(Bitmap bitmap) {	  
		 Bullet.bitmap = bitmap;
	 }

	 public void loadTexture(GL10 gl)
	    {
	        Bitmap tmpBitmap = null;
	        try
	        {
	            // 加载位图
	            tmpBitmap= Bullet.bitmap;
	            int[] textures = new int[1];
	            // 指定生成N个纹理（第一个参数指定生成1个纹理），
	            // textures数组将负责存储所有纹理的代号。
	            gl.glGenTextures(1, textures, 0);
	            // 获取textures纹理数组中的第一个纹理
	            texture = textures[0];
	            // 通知OpenGL将texture纹理绑定到GL10.GL_TEXTURE_2D目标中
	            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
	            // 设置纹理被缩小（距离视点很远时被缩小）时候的滤波方式
	            gl.glTexParameterf(GL10.GL_TEXTURE_2D,
	                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
	            // 设置纹理被放大（距离视点很近时被方法）时候的滤波方式
	            gl.glTexParameterf(GL10.GL_TEXTURE_2D,
	                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	            // 设置在横向、纵向上都是平铺纹理
	            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
	                GL10.GL_REPEAT);
	            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
	                GL10.GL_REPEAT);
	            // 加载位图生成纹理
	            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, tmpBitmap, 0);       
	        }catch(Exception e){
	        	Log.d("ABC",e.getStackTrace()+" "+e.getMessage());
	        }finally{
	            // 生成纹理之后，回收位图
//	            if (tmpBitmap != null){
//	                tmpBitmap.recycle();
//	                System.gc();
//	            }    
	        }
	    }

	public double getBullet_positionX() {
		return bullet_positionX;
	}



	public void setBullet_positionX(double bullet_positionX) {
		this.bullet_positionX = bullet_positionX;
	}



	public double getBullet_positionY() {
		return bullet_positionY;
	}



	public void setBullet_positionY(double bullet_positionY) {
		this.bullet_positionY = bullet_positionY;
	}

			

	public double getBullet_fly() {
		return bullet_fly;
	}



	public void setBullet_fly(double bullet_fly) {
		this.bullet_fly = bullet_fly;
	}



	public double getBullet_totalFly() {
		return bullet_totalFly;
	}



	public void setBullet_totalFly(double bullet_totalFly) {
		this.bullet_totalFly = bullet_totalFly;
	}



	public double getBulletAngle() {
		return bulletAngle;
	}

	public void setBulletAngle(double bulletAngle) {
		this.bulletAngle = bulletAngle;
	}



	public double getBulletFlyAngle() {
		return bulletFlyAngle;
	}



	public void setBulletFlyAngle(double bulletFlyAngle) {
		this.bulletFlyAngle = bulletFlyAngle;
	}
	
	
}
