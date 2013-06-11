package com.demo.acepilot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Square {

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
	  // 指定位置和資料格式
	  gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

	  // 以三點劃出三角形
	  gl.glDrawElements(GL10.GL_TRIANGLES, indices.length,
	    GL10.GL_UNSIGNED_SHORT, indexBuffer);

	  // 除能點的緩衝區
	  gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	  // 除能CULL_FACE
	  gl.glDisable(GL10.GL_CULL_FACE);
	 }
}
