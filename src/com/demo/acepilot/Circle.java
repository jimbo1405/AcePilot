package com.demo.acepilot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Circle
{  
    private float[] vertices = new float[720];  
    private FloatBuffer verBuffer;  
    private float yAngle;  
    private float zAngle;  
    public Circle(float pxl_w, float pxl_h)  
    {  
        
        for (int i = 0; i < 720; i += 2) {  
            
            vertices[i]   =  (float) (Math.cos(DegToRad(i)) * (pxl_w/2));  
            
            vertices[i+1] =  (float) (Math.sin(DegToRad(i)) * (pxl_h/2));  
        }     
        
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
        
//      gl.glRotatef(yAngle, 0.0f, 1.0f, 0.0f);  
//      gl.glRotatef(zAngle, 1.0f, 0.0f, 0.0f);            
        
//      gl.glColor4f(1.0f, 0.1f, 0.1f, 1f);          
        
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);  
       
        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, verBuffer);          
       
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 360);  
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);        
        
//      gl.glFinish();  							
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