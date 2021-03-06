package com.demo.acepilot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;

public class Square {

	 
	 private float vertices[] = { -1.0f, 1.0f, 0.0f, 
	   -1.0f, -1.0f, 0.0f, 
	   1.0f, -1.0f, 0.0f, 
	   1.0f, 1.0f, 0.0f, 
	 };

	 
	 private short[] indices = { 0, 1, 2, 0, 2, 3 };
	 
	
	 private float[] textureCor = { 0.0f, 0.0f, 
			   0.0f, 1.0f, 
			   1.0f, 1.0f, 
			   1.0f, 0.0f, 
			 };

	 
	 private FloatBuffer vertexBuffer;
	
	 private ShortBuffer indexBuffer;
	
	 private FloatBuffer textureBuffer;
	 
	 private static Bitmap bitmap;
	 private int texture;

	 public Square(float pxl_w, float pxl_h) {
	
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
	  
	  ByteBuffer byteBuf = ByteBuffer.allocateDirect(textureCor.length * 4);
	  byteBuf.order(ByteOrder.nativeOrder());
	  textureBuffer = byteBuf.asFloatBuffer();
	  textureBuffer.put(textureCor);
	  textureBuffer.position(0);
	 }
	 
	
	public void draw(GL10 gl) {
		
	    gl.glEnable(GL10.GL_TEXTURE_2D);	
		
		gl.glFrontFace(GL10.GL_CCW);
		
		gl.glEnable(GL10.GL_CULL_FACE);
		
		gl.glCullFace(GL10.GL_BACK);
		
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);	
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
		
		gl.glDrawElements(GL10.GL_TRIANGLES, indexBuffer.remaining(),
			  GL10.GL_UNSIGNED_SHORT, indexBuffer);

		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		
		gl.glDisable(GL10.GL_CULL_FACE);
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		gl.glDisable(GL10.GL_TEXTURE_2D);
	 }

	 public void setBitmap(Bitmap bitmap) {	  
		 this.bitmap = bitmap;
	 }
	 
	 public void loadTexture(GL10 gl)
	    {
	        Bitmap tmpBitmap = null;
	        try
	        {
	           
	            tmpBitmap=this.bitmap;
	            int[] textures = new int[1];
	           
	            gl.glGenTextures(1, textures, 0);
	            
	            texture = textures[0];
	            
	            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
	           
	            gl.glTexParameterf(GL10.GL_TEXTURE_2D,
	                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
	            
	            gl.glTexParameterf(GL10.GL_TEXTURE_2D,
	                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	           
	            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
	                GL10.GL_REPEAT);
	            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
	                GL10.GL_REPEAT);
	            
	            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, tmpBitmap, 0);       
	        }catch(Exception e){
	        	Log.d("ABC",e.getStackTrace()+" "+e.getMessage());
	        }finally{
	            
//	            if (tmpBitmap != null){
//	                tmpBitmap.recycle();
//	                System.gc();
//	            }    
	        }
	    }

}
