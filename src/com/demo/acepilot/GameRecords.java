package com.demo.acepilot;

//a class used to generate record objs.
public class GameRecords {
	private int id;
	private String name;	//player name.
	private double score;	//player score.
	private String level;	//player level.
		
	public GameRecords(){
		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}	
}
