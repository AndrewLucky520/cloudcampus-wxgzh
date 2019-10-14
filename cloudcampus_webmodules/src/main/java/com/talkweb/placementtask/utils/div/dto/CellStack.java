package com.talkweb.placementtask.utils.div.dto;

import java.util.LinkedList;

/**
 * 一种待拆情况
 * @author hushowly@foxmail.com
 *
 */
public class CellStack {
	
	private int avgClassSize;
	
	private int maxValue;
	
	private LinkedList<Cell> cells;
	
	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public LinkedList<Cell> getCells() {
		return cells;
	}

	public void setCells(LinkedList<Cell> cells) {
		this.cells = cells;
	}
	
	

	public int getAvgClassSize() {
		return avgClassSize;
	}

	public void setAvgClassSize(int avgClassSize) {
		this.avgClassSize = avgClassSize;
	}

	@Override
	public String toString() {
		return "CellStack [avgClassSize=" + avgClassSize + ", maxValue=" + maxValue + ", cells=" + cells + "]";
	}


	
	
	
}
