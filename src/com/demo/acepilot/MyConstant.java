package com.demo.acepilot;

public class MyConstant {
	// GUI object size (in pixels)
	public final static float BULLET_W=50;
	public final static float BULLET_H=50;
	public final static float AIRPLANE_W=100;
	public final static float AIRPLANE_H=100;
	public final static float BGSTAR_W=5;
	public final static float BGSTAR_H=5;
	public final static float COIN_W=50;
	public final static float COIN_H=50;
	
	public final static int logoDisplaySec = 3;	//開頭logo顯示秒數
	
	public final static double BULLET_VELOCITY=200;	//子彈移動速度，每秒飛多少像素
	public final static double BULLET_TIME_INTERVAL=0.1;	//每隔幾秒懺生一個子彈，不能設<0.05，會造成錯誤
	public final static double COIN_TIME_INTERVAL=10;		//每隔幾秒懺生一個coin
	public final static double BULLET_NUM=50;	//圓周上會佈署幾顆子彈，相當於子彈排列的密度
	public final static double COIN_NUM=3;		//螢幕上最多同時有幾個coin
	public final static double PLAYER_VELOCITY_COFF=0.80;	//玩家移動速度之係數
	public final static int STAR_NUM=60;	//背景星星的數量

	// shrink all coordinates to locate from -0.5 to +0.5 pixels in x and y
	// shrink factor for x: 17.25 = (8.75+0.6)+(4.75+2.15)
	// shrink factor for y: 11.5 = (4.75+1)+(4.75+1)
	public final static double[][] AIRPLANE_PROBE_PT ={ 
		{           0, -4.75/11.5, 2.15/17.5},
		{           0, -1   /11.5, 3.2 /17.5},
		{           0,  1.6 /11.5, 2.5 /17.5},
		{           0,  4.5 /11.5, 1.0 /17.5},
		{           0,  5.55/11.5, 1.0 /17.5},
		{           0,  6.5 /11.5, 0.8 /17.5},
		{           0,  7.25/11.5, 0.8 /17.5},
		{           0,  8   /11.5, 0.8 /17.5},
		{           0,  8.75/11.5, 0.6 /17.5},
		{-3.125/17.25, -2   /11.5, 1.7 /17.5},
		{ 3.125/17.25, -2   /11.5, 1.7 /17.5},
		{-4.75 /17.25, -3   /11.5, 1   /17.5},
		{ 4.75 /17.25, -3   /11.5, 1   /17.5},
		{-2.4  /17.25, -5.1 /11.5, 1.2 /17.5},
		{ 2.4  /17.25, -5.1 /11.5, 1.2 /17.5}
	};

	public final static int calCurrCoinNeed(int initCoinNeed ,int reviveCount){
		return initCoinNeed += 5*reviveCount;
	}
	
	//計算總秒數(成績)的公式
	public static double calTotalSec(double originSec,int reviveCount){
		return originSec + reviveCount * 1;
	}
	
	public final static int[] COMMENT_SEC_RANGE = {5,10,15,20,25,30,35,40,45,50,
												55,60,65,70,75,80,85,90,95,100,
												105,110,115,120,125,130,135,140,145,150,
												155};	//range of rank's comment.
	
	//徽章圖的id陣列
	public final static int[] DrawableIdArray = {R.drawable.rank012, R.drawable.rank011,
												R.drawable.rank022, R.drawable.rank021,
												R.drawable.rank032, R.drawable.rank031,
												R.drawable.rank042, R.drawable.rank041,
												R.drawable.rank052, R.drawable.rank051,
												R.drawable.rank062, R.drawable.rank061,
												R.drawable.rank072, R.drawable.rank071,
												R.drawable.rank082, R.drawable.rank081,
												R.drawable.rank092, R.drawable.rank091,
												R.drawable.rank102, R.drawable.rank101,
												R.drawable.rank112, R.drawable.rank111,
												R.drawable.rank122, R.drawable.rank121,
												R.drawable.rank132, R.drawable.rank131,
												R.drawable.rank142, R.drawable.rank141,
												R.drawable.rank152, R.drawable.rank151,
												R.drawable.rank162, R.drawable.rank161	};
	

}
