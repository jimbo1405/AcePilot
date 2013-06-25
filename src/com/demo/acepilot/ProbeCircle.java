package com.demo.acepilot;

//°»´ú¸I¼²ªº¶êªºclass
public class ProbeCircle {
	private double pCircle_positionX;	//x®y¼Ð
	private double pCircle_positionY;	//y®y¼Ð
	private double r;	//¥b®|
	
	public ProbeCircle(double x,double y,double r){
		setpCircle_positionX(x);
		setpCircle_positionY(y);
		setR(r);
	}
	
	public double getpCircle_positionX() {
		return pCircle_positionX;
	}
	public void setpCircle_positionX(double pCircle_positionX) {
		this.pCircle_positionX = pCircle_positionX;
	}
	public double getpCircle_positionY() {
		return pCircle_positionY;
	}
	public void setpCircle_positionY(double pCircle_positionY) {
		this.pCircle_positionY = pCircle_positionY;
	}
	public double getR() {
		return r;
	}
	public void setR(double r) {
		this.r = r;
	}
	
	
}
