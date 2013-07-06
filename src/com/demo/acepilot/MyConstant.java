package com.demo.acepilot;



public class MyConstant {
	public final static double[][] AIRPLANE_PROBE_PT = {{0.25, 0.25, 0.177}, {-0.25, 0.25, 0.177}, {-0.25, -0.25, 0.177}, {0.25, -0.25, 0.177}};
	public final static double BULLET_VELOCITY=200;	//子彈移動速度，每秒飛多少像素
	public final static double BULLET_TIME_INTERVAL=0.1;	//每隔幾秒懺生一個子彈，不能設<0.05，會造成錯誤
	public final static double BULLET_NUM=50;	//圓周上會佈署幾顆子彈，相當於子彈排列的密度
	public final static double PLAYER_VELOCITY_COFF=0.80;	//玩家移動速度之係數
}
