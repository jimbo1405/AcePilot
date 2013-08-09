package com.demo.acepilot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;

public class Star {
	private double star_positionX;	//starX位置
	private double star_positionY;	//starY位置 
	private double star_velocity;	//statr速度(景物座標)
	private float[] star_colorArray=new float[4];

	// 點的陣列
	 private float vertices[] = { -1.0f, 1.0f, 0.0f, // 0, 左上角
	   -1.0f, -1.0f, 0.0f, // 1, 左下角
	   1.0f, -1.0f, 0.0f, // 2, 右下角
	   1.0f, 1.0f, 0.0f, // 3, 右上角
	 };

	 // 連接點的次序
	 private short[] indices = { 0, 1, 2, 0, 2, 3 };

	 // 點的緩衝區
	 private FloatBuffer vertexBuffer;
	 // 索引值緩衝區
	 private ShortBuffer indexBuffer;

	 public Star(float pxl_w, float pxl_h) {
	  // adjust shape
	  for (int i=0; i<=3; i++) {
		vertices[i*3] = vertices[i*3]*(pxl_w/2);
		vertices[i*3+1] = vertices[i*3+1]*(pxl_h/2);
	  }	
		 
	  // 浮點數是4位元組因此需要把點陣列長度乘以4
	  ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
	  vbb.order(ByteOrder.nativeOrder());
	  vertexBuffer = vbb.asFloatBuffer();
	  vertexBuffer.put(vertices);
	  vertexBuffer.position(0);

	  // 短整數是2位元組因此需要把點陣列長度乘以2
	  ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
	  ibb.order(ByteOrder.nativeOrder());
	  indexBuffer = ibb.asShortBuffer();
	  indexBuffer.put(indices);
	  indexBuffer.position(0);
	 }

	 /**
	  * 畫圖函式
	  * 
	  * @param gl
	  */
	public void draw(GL10 gl) {
		// 逆時鐘
		gl.glFrontFace(GL10.GL_CCW);
		// 啟動CULL_FACE
		gl.glEnable(GL10.GL_CULL_FACE);
		// 刪除多邀形的背景
		gl.glCullFace(GL10.GL_BACK);

		// 啟動點的緩衝區
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		// 使用UV坐標
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);	
		// 指定位置和資料格式
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

		//顏色以亂數指定
//		gl.glColor4f((float)Math.random(), (float)Math.random(), (float)Math.random(), 0.5f);

		// 以三點劃出三角形
		gl.glDrawElements(GL10.GL_TRIANGLES, indexBuffer.remaining(),
			  GL10.GL_UNSIGNED_SHORT, indexBuffer);

		// 除能點的緩衝區
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		// 除能CULL_FACE
		gl.glDisable(GL10.GL_CULL_FACE);
		// 禁用顶点、纹理座标数组
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	 }

	public double getStar_positionX() {
		return star_positionX;
	}

	public void setStar_positionX(double star_positionX) {
		this.star_positionX = star_positionX;
	}

	public double getStar_positionY() {
		return star_positionY;
	}

	public void setStar_positionY(double star_positionY) {
		this.star_positionY = star_positionY;
	}

	public double getStar_velocity() {
		return star_velocity;
	}

	public void setStar_velocity(double star_velocity) {
		this.star_velocity = star_velocity;
	}

	public float[] getColorArray(){
		return star_colorArray;
	}

	public void setColorArray(float r, float g ,float b, float alpha){
		star_colorArray[0]=r;
		star_colorArray[1]=g;
		star_colorArray[2]=b;
		star_colorArray[3]=alpha;
	}
}