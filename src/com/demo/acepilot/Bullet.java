package com.demo.acepilot;

public class Bullet extends Circle{
	private double bullet_positionX;	//子彈x方向位移
	private double bullet_positionY;	//子彈y方向位移
	private double bullet_fly;			//子彈x方向的飛行位移	
	private double bullet_totalFly;		//子彈x方向飛行總位移	
	private double bulletAngle;			//子彈的圓心角
	
	public  Bullet(){
		super();
		setBullet_positionX(0);
		setBullet_positionY(0);
		setBullet_fly(0);
		setBullet_totalFly(0);		
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
	
	
}
