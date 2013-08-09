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
	
	public final static double BULLET_VELOCITY=200;	//�l�u���ʳt�סA�C���h�ֹ���
	public final static double BULLET_TIME_INTERVAL=0.1;	//�C�j�X���b�ͤ@�Ӥl�u�A����]<0.05�A�|�y�����~
	public final static double COIN_TIME_INTERVAL=10;		//�C�j�X���b�ͤ@��coin
	public final static double BULLET_NUM=50;	//��P�W�|�G�p�X���l�u�A�۷��l�u�ƦC���K��
	public final static double COIN_NUM=3;		//�ù��W�̦h�P�ɦ��X��coin
	public final static double PLAYER_VELOCITY_COFF=0.80;	//���a���ʳt�פ��Y��
	public final static int STAR_NUM=60;	//�I���P�P���ƶq

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
	
	public final static int[] COMMENT_SEC_RANGE = {5,10,15,20,25,30,35,40,45,50,55};	//range of rank's comment.
}
