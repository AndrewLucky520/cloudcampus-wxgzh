package com.talkweb.placementtask.utils.div.dezy.dto;

import java.util.ArrayList;
import java.util.List;

import com.talkweb.placementtask.utils.div.dto.Cell;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.ScoreResult;
import com.talkweb.placementtask.utils.div.dto.TeachClassResult;

public class AdScoreResult extends ScoreResult{
	
	/**
	 * 选三教学班集合
	 */
	private List<ClassResult> opThreeDivClassList = new ArrayList<>();
	
	
	private int adLastClassStudentCount;
	
	
	/**
	 * 打印打分结果详细
	 * @param scoreResult
	 */
	public void printScoreInfo(DivContext divContext) {
		
		int adTotalStudentCount = 0;
		int optTotalStudentCount = 0;
		
		int adMaxClassSize = Integer.MIN_VALUE;
		int adMinClassSize = Integer.MAX_VALUE;
		
		int optMaxClassSize = Integer.MIN_VALUE;
		int optMinClassSize = Integer.MAX_VALUE;
		
		System.out.println("===================行班班");
		for (ClassResult divClass : this.getClassResultList()) {
			
			AdClassResult adClassResult = (AdClassResult)divClass;
			System.out.println(adClassResult.getFixTwoSubjectGroup().getId()+"行政班 "+adClassResult.getId()+",总人数:"+adClassResult.getTotalStudent());
			for (Cell cell : adClassResult.getCells()) {
				System.out.println(" "+cell.getRowKey()+"人数:"+cell.getStudentCount());
			}
			if(divClass.getTotalStudent()>adMaxClassSize) adMaxClassSize = divClass.getTotalStudent();
			if(divClass.getTotalStudent()<adMinClassSize) adMinClassSize = divClass.getTotalStudent();
			
			adTotalStudentCount+=divClass.getTotalStudent();
		}
		
		System.out.println("===================选三班");
		for (ClassResult divClass : this.getOpThreeDivClassList()) {
			TeachClassResult teachClass = (TeachClassResult)divClass;
			System.out.println(teachClass.getSubjectId()+"选三班"+teachClass.getId()+",总人数:"+divClass.getTotalStudent());
			for (Cell cell : divClass.getCells()) {
				System.out.println(" "+cell.getRowKey()+"人数:"+cell.getStudentCount());
			}
			if(divClass.getTotalStudent()>optMaxClassSize) optMaxClassSize = divClass.getTotalStudent();
			if(divClass.getTotalStudent()<optMinClassSize) optMinClassSize = divClass.getTotalStudent();
			optTotalStudentCount+=divClass.getTotalStudent();
		}
		System.out.println("行政班最大班级:"+adMaxClassSize+"行政班最小班级:"+adMinClassSize);
		System.out.println("选三最大班级:"+optMaxClassSize+"选三最小班级:"+optMinClassSize);
		System.out.println("行政班总人数："+adTotalStudentCount);
		System.out.println("选三班总人数："+optTotalStudentCount);
		System.out.println("打分:"+this.getScore()+" 行政班开班数："+this.getClassResultList().size()+" 选三开班数"+this.getOpThreeDivClassList().size());
	}



	public List<ClassResult> getOpThreeDivClassList() {
		return opThreeDivClassList;
	}



	public void setOpThreeDivClassList(List<ClassResult> opThreeDivClassList) {
		this.opThreeDivClassList = opThreeDivClassList;
	}



	public int getAdLastClassStudentCount() {
		return adLastClassStudentCount;
	}



	public void setAdLastClassStudentCount(int adLastClassStudentCount) {
		this.adLastClassStudentCount = adLastClassStudentCount;
	}
	
}
