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

	 // 點的陣列
	 private float vertices[] = { -1.0f, 1.0f, 0.0f, // 0, 左上角
	   -1.0f, -1.0f, 0.0f, // 1, 左下角
	   1.0f, -1.0f, 0.0f, // 2, 右下角
	   1.0f, 1.0f, 0.0f, // 3, 右上角
	 };

	 // 連接點的次序
	 private short[] indices = { 0, 1, 2, 0, 2, 3 };
	 
	// 質地坐標
	 private float[] textureCor = { 0.0f, 0.0f, //
			   0.0f, 1.0f, //
			   1.0f, 1.0f, //
			   1.0f, 0.0f, //
			 };

	 // 點的緩衝區
	 private FloatBuffer vertexBuffer;
	 // 索引值緩衝區
	 private ShortBuffer indexBuffer;
	// 質地緩衝區
	 private FloatBuffer textureBuffer;
	 
	 private static Bitmap bitmap;
	 private int texture;

	 public Square() {
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
	  
	  ByteBuffer byteBuf = ByteBuffer.allocateDirect(textureCor.length * 4);
	  byteBuf.order(ByteOrder.nativeOrder());
	  textureBuffer = byteBuf.asFloatBuffer();
	  textureBuffer.put(textureCor);
	  textureBuffer.position(0);
	 }
	 
	 /**
	  * 畫圖函式
	  * 
	  * @param gl
	  */
	public void draw(GL10 gl) {
		// 启用2D纹理贴图
	    gl.glEnable(GL10.GL_TEXTURE_2D);	
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
		// 指定質地緩衝區
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		// 执行纹理贴图
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
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
		// 停用2D纹理贴图
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
	            // 加载位图
	            tmpBitmap=this.bitmap;
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

}
