package com.demo.acepilot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;

public class Star {
	private double star_positionX;	
	private double star_positionY;	
	private double star_velocity;	
	private float[] star_colorArray=new float[4];

	
	 private float vertices[] = { -1.0f, 1.0f, 0.0f, 
	   -1.0f, -1.0f, 0.0f, 
	   1.0f, -1.0f, 0.0f, 
	   1.0f, 1.0f, 0.0f, 
	 };

	
	 private short[] indices = { 0, 1, 2, 0, 2, 3 };

	 
	 private FloatBuffer vertexBuffer;
	
	 private ShortBuffer indexBuffer;

	 public Star(float pxl_w, float pxl_h) {
	  // adjust shape
	  for (int i=0; i<=3; i++) {
		vertices[i*3] = vertices[i*3]*(pxl_w/2);
		vertices[i*3+1] = vertices[i*3+1]*(pxl_h/2);
	  }	
		 
	 
	  ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
	  vbb.order(ByteOrder.nativeOrder());
	  vertexBuffer = vbb.asFloatBuffer();
	  vertexBuffer.put(vertices);
	  vertexBuffer.position(0);

	 
	  ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
	  ibb.order(ByteOrder.nativeOrder());
	  indexBuffer = ibb.asShortBuffer();
	  indexBuffer.put(indices);
	  indexBuffer.position(0);
	 }

	 
	public void draw(GL10 gl) {
		
		gl.glFrontFace(GL10.GL_CCW);
		
		gl.glEnable(GL10.GL_CULL_FACE);
		
		gl.glCullFace(GL10.GL_BACK);

		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);	
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

		
//		gl.glColor4f((float)Math.random(), (float)Math.random(), (float)Math.random(), 0.5f);

		
		gl.glDrawElements(GL10.GL_TRIANGLES, indexBuffer.remaining(),
			  GL10.GL_UNSIGNED_SHORT, indexBuffer);

		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		
		gl.glDisable(GL10.GL_CULL_FACE);
		
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