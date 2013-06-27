package com.demo.acepilot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Circle  
{  
    private float[] vertices = new float[720];  
    private FloatBuffer verBuffer ;  
    private float yAngle;  
    private float zAngle;  
    public Circle()  
    {  
        //初始化圖形數據  
        for (int i = 0; i < 720; i += 2) {  
            // x 座標 
            vertices[i]   =  (float) (Math.cos(DegToRad(i)) * 1);  
            // y 座標  
            vertices[i+1] =  (float) (Math.sin(DegToRad(i)) * 1);  
        }     
        //設置圖形頂點數據  
        ByteBuffer qbb = ByteBuffer.allocateDirect(vertices.length * 4);  
        qbb.order(ByteOrder.nativeOrder());  
        verBuffer = qbb.asFloatBuffer();  
        verBuffer.put(vertices);  
        verBuffer.position(0);   
    }  
      
    public float DegToRad(float deg)  
    {  
        return (float) (3.14159265358979323846 * deg / 180.0);  
    }  
      
    public void draw(GL10 gl)  
    {            
        //旋轉, angle, x, y , z  
        gl.glRotatef(yAngle, 0.0f, 1.0f, 0.0f);  
        gl.glRotatef(zAngle, 1.0f, 0.0f, 0.0f);  
          
        // 設置當前色為紅色, R, G, B, Alpha  
        gl.glColor4f(1.0f, 0.1f, 0.1f, 1f);  
          
        //設置頂點類型為浮點座標    
        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, verBuffer);  
  
        //打開頂點數組  
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);  
          
        //向OGL發送實際畫圖指令  
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 360);  
        //畫圖結束  
//        gl.glFinish();  							//不加這行，因為會造成cpu block住
    }  
  
    public void setyAngle(float yAngle)  
    {  
        this.yAngle = yAngle;  
    }  
  
    public void setzAngle(float zAngle)  
    {  
        this.zAngle = zAngle;  
    }  
}  