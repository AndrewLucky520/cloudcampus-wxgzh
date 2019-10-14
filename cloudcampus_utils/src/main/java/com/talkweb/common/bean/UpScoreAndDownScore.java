package com.talkweb.common.bean;

public class UpScoreAndDownScore{
	
	private int upScore;
	private int downScore;
	
	
	public UpScoreAndDownScore() {
		super();
	}
	
	public UpScoreAndDownScore(int upScore, int downScore) {
		super();
		this.upScore = upScore;
		this.downScore = downScore;
	}

	public double getUpScore() {
		return upScore;
	}

	

	public int getDownScore() {
		return downScore;
	}

	public void setDownScore(int downScore) {
		this.downScore = downScore;
	}

	public void setUpScore(int upScore) {
		this.upScore = upScore;
	}

	@Override
	public String toString() {
		return "UpScoreAndDownScore [upScore=" + upScore + ", downScore="
				+ downScore + "]";
	}

	

}