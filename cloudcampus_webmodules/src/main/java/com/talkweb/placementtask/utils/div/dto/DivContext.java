package com.talkweb.placementtask.utils.div.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.talkweb.placementtask.utils.div.Utils;
import com.talkweb.placementtask.utils.div.dezy.dto.AdClassResult;
import com.talkweb.placementtask.utils.div.dto.Subject.UP_OR_DOWN;


/**
 * 一次分班生命周期中的上下文对象
 * @author hushow
 *
 */
public class DivContext {
	
	
	/**
	 * 开班数
	 */
	private Integer globalClassCount;
	
	/**
	 * 平均班额
	 */
	private Double globalAgvClassSize;
	
	/**
	 * 最大班额
	 */
	private int maxClassSize;
	
	
	/**
	 * 总学生数
	 */
	private int totalStudent;
	
	/**
	 * 科目id和对象映射关系
	 */
	private Map<String, Subject> subjectId2SubjectMap = new HashMap<String, Subject>();
	
	
	/**
	 * 志愿信息集合
	 */
	private List<SubjectGroup> wishSubjectGroupList;
	
	
	/**
	 * 定二组合情况
	 */
	private List<SubjectGroup> fixTwoSubjectGroupList;
	
	
	/**
	 * 志愿id和对象映射关系
	 */
	private Map<String, SubjectGroup> wishId2SubjectGroupMap = new HashMap<String, SubjectGroup>();
	
	
	/**
	 * 根据id索引行班
	 */
	private Map<String, AdClassResult> adClassId2AdClassMap = new HashMap<String, AdClassResult>();
	
	/**
	 * 根据id索引定二
	 */
	private Map<String, SubjectGroup> fixTwoId2fixTwoSubjectGroup = new HashMap<>();
	
	
	/**
	 * 最优开班数分布图
	 */
	Table<Integer, String, SeqCell> classCountLayoutTable=HashBasedTable.create();
	
	
	public DivContext(Map<String, Subject> subjectId2SubjectMap, List<SubjectGroup> wishSubjectGroupList, Integer globalClassCount, int maxClassSize) {
		
		this.globalClassCount = globalClassCount;
		this.maxClassSize = maxClassSize;
		this.subjectId2SubjectMap = subjectId2SubjectMap;
		this.wishSubjectGroupList = wishSubjectGroupList;
		
		//统计科目被选人数
		for (SubjectGroup wishSubjectGroup : wishSubjectGroupList) {
			System.out.println(wishSubjectGroup.getName()+"["+wishSubjectGroup.getId()+"]人数："+wishSubjectGroup.getStudentCount());
			totalStudent+=wishSubjectGroup.getStudentCount();
			wishId2SubjectGroupMap.put(wishSubjectGroup.getId(), wishSubjectGroup);
			for (Subject subject : wishSubjectGroup.getSubjects()) {
				Subject s = this.subjectId2SubjectMap.get(subject.getId());
				s.setTotalStudentCount(s.getTotalStudentCount()+wishSubjectGroup.getStudentCount());
			}
		}
		
		this.globalAgvClassSize = (totalStudent)/globalClassCount.doubleValue();
		
		//统计科目平均班额和开班数
//		for (Subject subject : this.subjectId2SubjectMap.values()) {
//			
//			subject.setGlobalAgvClassSize(this.globalAgvClassSize);
//			
//			SubjectAvgSize sas = new SubjectAvgSize();
//			sas.setClassCount(((Integer)subject.getTotalStudentCount())/globalAgvClassSize);
//			sas.setClassCountMod(sas.getClassCount()%1);
//			sas.setSubjectId(subject.getId());
//			sas.setTotalStudentCount(subject.getTotalStudentCount());
//			sAvgList.add(sas);
//			//平均班额  
//			//向下取整开班数
////			Double count = Math.floor(((Integer)subject.getTotalStudentCount()).doubleValue()/globalAgvClassSize);
////			if(((Integer)subject.getTotalStudentCount()).doubleValue()/count > maxClassSize) {
////				count = Math.ceil(((Integer)subject.getTotalStudentCount()).doubleValue()/globalAgvClassSize);
////			}
//			if(subject.getId().equals("4")) {
//				subject.setClassTotalCount(2);
//				subject.setUpOrDown(UP_OR_DOWN.UP);
//			}
//			if(subject.getId().equals("5")) {
//				subject.setClassTotalCount(4);
//				subject.setUpOrDown(UP_OR_DOWN.UP);
//			}
//			if(subject.getId().equals("6")) {
//				subject.setClassTotalCount(5);
//				subject.setUpOrDown(UP_OR_DOWN.DOWN);
//			}
//			if(subject.getId().equals("7")) {
//				subject.setClassTotalCount(6);
//				subject.setUpOrDown(UP_OR_DOWN.DOWN);
//			}
//			if(subject.getId().equals("8")) {
//				subject.setClassTotalCount(6);
//				subject.setUpOrDown(UP_OR_DOWN.UP);
//			}
//			if(subject.getId().equals("9")) {
//				subject.setClassTotalCount(7);
//				subject.setUpOrDown(UP_OR_DOWN.DOWN);
//			}
//			subject.setAvgClassSize(new BigDecimal(((Integer)subject.getTotalStudentCount()).doubleValue()/subject.getClassTotalCount()).setScale(2, BigDecimal.ROUND_HALF_UP));
//		}
		//根据向上向下取整计算最优平均班额
		this.calculateAvgSize(this.subjectId2SubjectMap);
		
		//根据整体情况重新计算科目最优班额
//		int halfCount = sAvgList.size()/2;
//		//从小到大排序
//		Collections.sort(sAvgList, new Comparator<SubjectAvgSize>() {
//			public int compare(SubjectAvgSize o1, SubjectAvgSize o2) {
//				return o1.getClassCountMod().compareTo(o2.getClassCountMod());
//			};
//		});
//		
//		for (SubjectAvgSize subjectAvgSize : sAvgList) {
//			System.out.println("==科目:"+subjectAvgSize.getSubjectId()+" 总人数"+ subjectAvgSize.getTotalStudentCount());
//			System.out.println("  "+subjectAvgSize.getClassCount());
//			System.out.println("  "+subjectAvgSize.getClassCountMod());
//		}
//		
//		//一半向上取整，一半向下取整
//		for (SubjectAvgSize subjectAvgSize : sAvgList) {
//			Subject subject = this.subjectId2SubjectMap.get(subjectAvgSize.getSubjectId());
//			if(halfCount>0) {
//				Double classCount = Math.floor(subjectAvgSize.getClassCount());
//				if(((Integer)subjectAvgSize.getTotalStudentCount()).doubleValue()/classCount > maxClassSize) {
//					subjectAvgSize.setClassCount(Math.ceil(subjectAvgSize.getClassCount()));
//					subject.setUpOrDown(UP_OR_DOWN.UP);
//					
//				}else {
//					halfCount--;
//					subjectAvgSize.setClassCount(classCount);
//					subject.setUpOrDown(UP_OR_DOWN.DOWN);
//				}
//			}else {
//				subjectAvgSize.setClassCount(Math.ceil(subjectAvgSize.getClassCount()));
//				subject.setUpOrDown(UP_OR_DOWN.UP);
//			}
//			
//			//subject.setClassTotalCount(subjectAvgSize.getClassCount().intValue());
//			if(subject.getId().equals("4")) {
//				subject.setClassTotalCount(2);
//			}
//			if(subject.getId().equals("5")) {
//				subject.setClassTotalCount(4);
//			}
//			if(subject.getId().equals("6")) {
//				subject.setClassTotalCount(6);
//			}
//			if(subject.getId().equals("7")) {
//				subject.setClassTotalCount(6);
//			}
//			if(subject.getId().equals("8")) {
//				subject.setClassTotalCount(5);
//			}
//			if(subject.getId().equals("9")) {
//				subject.setClassTotalCount(7);
//			}
//			subject.setAvgClassSize(new BigDecimal(((Integer)subject.getTotalStudentCount()).doubleValue()/subject.getClassTotalCount()).setScale(2, BigDecimal.ROUND_HALF_UP));
//		}
//		
//		for (SubjectAvgSize subjectAvgSize : sAvgList) {
//			System.out.println("==科目:"+subjectAvgSize.getSubjectId()+" 总人数"+ subjectAvgSize.getTotalStudentCount());
//			System.out.println("  "+subjectAvgSize.getClassCount());
//			System.out.println("  "+subjectAvgSize.getClassCountMod());
//		}
		
		//计算定二组合数据
		this.fixTwoSubjectGroupList = Utils.calculateFixTwoSubjectGroup(wishSubjectGroupList, this.subjectId2SubjectMap);
		
		//索引定二数据
		for (SubjectGroup fixTwoSubjectGroup : this.fixTwoSubjectGroupList) {
			fixTwoId2fixTwoSubjectGroup.put(fixTwoSubjectGroup.getId(), fixTwoSubjectGroup);
		}
		
	}
	
	
	/**
	 * 计算平均班额
	 * @param subjectMap
	 */
	public void calculateAvgSize(Map<String, Subject> subjectMap) {
		
		List<SubjectAvgSize> bestList = new ArrayList<>();
		
		List<SubjectAvgSize> sAvgList = new ArrayList<>();
		for (Subject subject : subjectMap.values()) {
			
			if(subject.getTotalStudentCount()<=0) continue;
			
			SubjectAvgSize sas = new SubjectAvgSize();
			Double classCount = subject.getTotalStudentCount()/this.getGlobalAgvClassSize();
			sas.setClassCountMod(classCount%1);
			classCount = Math.ceil(classCount);
			sas.setClassCount(classCount);
			sas.setUpOrDown(UP_OR_DOWN.UP);
			sas.setAvgSize(subject.getTotalStudentCount()/classCount);
			sas.setSubjectId(subject.getId());
			sas.setTotalStudentCount(subject.getTotalStudentCount());
			sAvgList.add(sas);
		}
		
		Double minOffset = Double.MAX_VALUE;
		List<LinkedList<SubjectAvgSize>> upCombineList = Utils.generateCombine(sAvgList.toArray(new SubjectAvgSize[sAvgList.size()]), 0, new LinkedList<>(), 3);
		for (LinkedList<SubjectAvgSize> upList : upCombineList) {
			
			Double maxAvgSize = Double.MIN_VALUE;
			Double minAvgSize = Double.MAX_VALUE;
			
			for (SubjectAvgSize subjectAvgSize : upList) {
				Double upClassCount = Math.ceil(subjectAvgSize.getTotalStudentCount()/this.getGlobalAgvClassSize());
				subjectAvgSize.setClassCount(upClassCount);
				subjectAvgSize.setAvgSize(subjectAvgSize.getTotalStudentCount()/upClassCount);
				subjectAvgSize.setUpOrDown(UP_OR_DOWN.UP);
				
				if(subjectAvgSize.getAvgSize()>maxAvgSize) maxAvgSize=subjectAvgSize.getAvgSize();
				if(subjectAvgSize.getAvgSize()<minAvgSize) minAvgSize=subjectAvgSize.getAvgSize();
			}
			
			List<SubjectAvgSize> downList = new ArrayList<>(sAvgList);
			downList.removeAll(upList);
			for (SubjectAvgSize subjectAvgSize : downList) {
				Double downClassCount = Math.floor(subjectAvgSize.getTotalStudentCount()/this.getGlobalAgvClassSize());
				subjectAvgSize.setClassCount(downClassCount);
				subjectAvgSize.setAvgSize(subjectAvgSize.getTotalStudentCount()/downClassCount);
				subjectAvgSize.setUpOrDown(UP_OR_DOWN.DOWN);
				
				if(subjectAvgSize.getAvgSize()>maxAvgSize) maxAvgSize=subjectAvgSize.getAvgSize();
				if(subjectAvgSize.getAvgSize()<minAvgSize) minAvgSize=subjectAvgSize.getAvgSize();
			}
			
			//记录最小数据
			if(maxAvgSize-minAvgSize<minOffset) {
				bestList.clear();
				minOffset = maxAvgSize-minAvgSize;
				for (SubjectAvgSize subjectAvgSize : upList) {
					bestList.add(new SubjectAvgSize().copyFrom(subjectAvgSize));
				}
				for (SubjectAvgSize subjectAvgSize : downList) {
					bestList.add(new SubjectAvgSize().copyFrom(subjectAvgSize));
				}
			}
			
		}
		
		
		for (SubjectAvgSize subjectAvgSize : bestList) {
			Subject subject = subjectMap.get(subjectAvgSize.getSubjectId());
			if(subject.getTotalStudentCount()<=0) continue;
			
			subject.setClassTotalCount(subjectAvgSize.getClassCount().intValue());
			subject.setUpOrDown(subjectAvgSize.getUpOrDown());
			subject.setAvgClassSize(new BigDecimal(subjectAvgSize.getAvgSize()).setScale(2, BigDecimal.ROUND_HALF_UP));
		}
	}
	
	
	/**
	 * 初始化最优班级数分布情况
	 */
	public void initClassCountLayout() {
		//初始化矩阵图
		for(int i=1;i<=3;i++) {
			for (Map.Entry<String, Subject> entry : this.getSubjectId2SubjectMap().entrySet()) {
				classCountLayoutTable.put(i, entry.getKey(), new SeqCell(i, entry.getValue()));
			}
		}
		
		//根据科目总人数计算最优班级分布
		calculateHourTable();
		
		printSeqHour();
	}
	
	
	public void  calculateHourTable() {
		//初始化矩阵科目开班分布
		int preIndex = 0;
		//循环科目
		for (Subject subject : this.subjectId2SubjectMap.values()) {
			int classNumber = subject.getClassTotalCount();
			Map<Integer, SeqCell> seqMap =  classCountLayoutTable.column(subject.getId());
			Integer[] seqArray = seqMap.keySet().toArray(new Integer[seqMap.keySet().size()]);
			int i=preIndex>=seqMap.keySet().size()?0:preIndex;
			//循环序列
			while(i<seqArray.length && classNumber > 0) {
				SeqCell seqCell = seqMap.get(seqArray[i]);
				seqCell.setClassCount(seqCell.getClassCount()+1);
				classNumber--;
				if(i==seqArray.length-1 && classNumber>0) {
					i=0;
				}else {
					i++;
				}
			}
			preIndex = i;
		}
	}
	
	
	/**
	 * 打印科目序列课时分布图
	 */
	public void  printSeqHour() {
		
		StringBuffer title = new StringBuffer("      ");
		StringBuffer avgClassSizeStr = new StringBuffer("平均班额  ");
		StringBuffer classTotalStr  = new StringBuffer("科目班数  ");
		for (Subject subject : this.subjectId2SubjectMap.values()) {
			title.append("   "+subject.getName()+"("+subject.getTotalStudentCount()+")");
			avgClassSizeStr.append("  "+subject.getAvgClassSize());
			classTotalStr.append("  "+subject.getClassTotalCount());
		}
		System.out.println(title.toString());
		
		for (Map.Entry<Integer, Map<String, SeqCell>> rowEntry : this.classCountLayoutTable.rowMap().entrySet()) {
			StringBuffer sb = new StringBuffer(rowEntry.getKey()+"序列       ");
			for (Subject subject : this.subjectId2SubjectMap.values()) {
				SeqCell cell = rowEntry.getValue().get(subject.getId());
				sb.append("  "+cell.getClassCount());
			}
			System.out.println(sb.toString());
		}
		
		System.out.println(classTotalStr);
		System.out.println(avgClassSizeStr);
	}
	
	
	
	public void addAdClassResult(List<ClassResult> adClassList) {
		for (ClassResult adClass : adClassList) {
			this.adClassId2AdClassMap.put(adClass.getId(), (AdClassResult)adClass);
		}
	}

	public Double getGlobalAgvClassSize() {
		return globalAgvClassSize;
	}



	public void setGlobalAgvClassSize(Double globalAgvClassSize) {
		this.globalAgvClassSize = globalAgvClassSize;
	}



	public int getMaxClassSize() {
		return maxClassSize;
	}



	public void setMaxClassSize(int maxClassSize) {
		this.maxClassSize = maxClassSize;
	}



	public Map<String, Subject> getSubjectId2SubjectMap() {
		return subjectId2SubjectMap;
	}



	public void setSubjectId2SubjectMap(Map<String, Subject> subjectId2SubjectMap) {
		this.subjectId2SubjectMap = subjectId2SubjectMap;
	}



	public List<SubjectGroup> getWishSubjectGroupList() {
		return wishSubjectGroupList;
	}



	public void setWishSubjectGroupList(List<SubjectGroup> wishSubjectGroupList) {
		this.wishSubjectGroupList = wishSubjectGroupList;
	}



	public List<SubjectGroup> getFixTwoSubjectGroupList() {
		return fixTwoSubjectGroupList;
	}



	public void setFixTwoSubjectGroupList(List<SubjectGroup> fixTwoSubjectGroupList) {
		this.fixTwoSubjectGroupList = fixTwoSubjectGroupList;
	}



	public Map<String, SubjectGroup> getWishId2SubjectGroupMap() {
		return wishId2SubjectGroupMap;
	}



	public void setWishId2SubjectGroupMap(Map<String, SubjectGroup> wishId2SubjectGroupMap) {
		this.wishId2SubjectGroupMap = wishId2SubjectGroupMap;
	}



	public int getTotalStudent() {
		return totalStudent;
	}



	public void setTotalStudent(int totalStudent) {
		this.totalStudent = totalStudent;
	}



	public Map<String, AdClassResult> getAdClassId2AdClassMap() {
		return adClassId2AdClassMap;
	}



	public void setAdClassId2AdClassMap(Map<String, AdClassResult> adClassId2AdClassMap) {
		this.adClassId2AdClassMap = adClassId2AdClassMap;
	}

	public Map<String, SubjectGroup> getFixTwoId2fixTwoSubjectGroup() {
		return fixTwoId2fixTwoSubjectGroup;
	}

	public void setFixTwoId2fixTwoSubjectGroup(Map<String, SubjectGroup> fixTwoId2fixTwoSubjectGroup) {
		this.fixTwoId2fixTwoSubjectGroup = fixTwoId2fixTwoSubjectGroup;
	}
	
	
	
	
	
	
}
