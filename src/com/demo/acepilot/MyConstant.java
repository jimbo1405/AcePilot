package com.demo.acepilot;



public class MyConstant {
//	public final static double[][] AIRPLANE_PROBE_PT = {{0.125, 0.125, 0.177}, {-0.125, 0.125, 0.177}, {-0.125, -0.125, 0.177}, {0.125, -0.125, 0.177}};
	public final static double BULLET_VELOCITY=200;	//�l�u���ʳt�סA�C���h�ֹ���
	public final static double BULLET_TIME_INTERVAL=0.1;	//�C�j�X���b�ͤ@�Ӥl�u�A����]<0.05�A�|�y�����~
	public final static double BULLET_NUM=50;	//��P�W�|�G�p�X���l�u�A�۷��l�u�ƦC���K��
	public final static double PLAYER_VELOCITY_COFF=0.80;	//���a���ʳt�פ��Y��
	
	public final static double[][] AIRPLANE_PROBE_PT ={
		{0, -4.75, 2.15},
		{0, -1, 3.2},
		{0, 1.6, 2.5},
		{0, 4.5, 0.8},
		{0, 5.55, 0.8},
		{0, 6.5, 0.6},
		{0, 7.25, 0.6},
		{0, 8, 0.6},
		{0, 8.75, 0.4},
		{-3.125, -2, 1.7},
		{3.125, -2, 1.7},
		{-4.75, -3, 1},
		{4.75, -3, 1},
		{-2.4, -5.1, 1.2},
		{2.4, -5.1, 1.2}
	};
	
	public final static double COR_SCALE = (Math.sqrt(0.5*0.5/2))/9.5;
}
