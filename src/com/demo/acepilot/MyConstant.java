package com.demo.acepilot;

public class MyConstant {
	public final static double BULLET_VELOCITY=200;	//子彈移動速度，每秒飛多少像素
	public final static double BULLET_TIME_INTERVAL=0.1;	//每隔幾秒懺生一個子彈，不能設<0.05，會造成錯誤
	public final static double COIN_TIME_INTERVAL=10;		//每隔幾秒懺生一個coin
	public final static double BULLET_NUM=50;	//圓周上會佈署幾顆子彈，相當於子彈排列的密度
	public final static double COIN_NUM=3;		//螢幕上最多同時有幾個coin
	public final static double PLAYER_VELOCITY_COFF=0.80;	//玩家移動速度之係數
	public final static int STAR_NUM=60;	//背景星星的數量
		
	public final static double[][] AIRPLANE_PROBE_PT ={
		{0, -4.75, 2.15},
		{0, -1, 3.2},
		{0, 1.6, 2.5},
		{0, 4.5, 1.0},	//半徑略小，14~19行半徑+0.2
		{0, 5.55, 1.0},
		{0, 6.5, 0.8},
		{0, 7.25, 0.8},
		{0, 8, 0.8},
		{0, 8.75, 0.6},
		{-3.125, -2, 1.7},
		{3.125, -2, 1.7},
		{-4.75, -3, 1},
		{4.75, -3, 1},
		{-2.4, -5.1, 1.2},
		{2.4, -5.1, 1.2}
	};
	
	public final static double COR_SCALE = (Math.sqrt(0.5*0.5/2))/9.5;
	
	public final static int calCurrCoinNeed(int initCoinNeed ,int reviveCount){
		return initCoinNeed += 5*reviveCount;
	}
	
	public final static int[] COMMENT_SEC_RANGE = {5,10,15,20,25,30,35,40,45,50,55};	//range of rank's comment.
}
