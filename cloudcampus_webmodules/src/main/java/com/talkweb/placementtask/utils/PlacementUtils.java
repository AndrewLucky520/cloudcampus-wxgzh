package com.talkweb.placementtask.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.util.Assert;

/**
 * 分班共用类
 * 
 * @author hushowly@foxmail.com
 *
 */
public class PlacementUtils {

	/**
	 * 验证大走班课时
	 * 
	 * @param openClassNum
	 * @param maxStuInClass
	 * @param subjectLessonList
	 * @return
	 */
	public static void validateGigGoClassSetting(int totalWeekLessons, int fixedSumLesson,
			List<SubjectLesson> subjectLessonList) {

		Assert.isTrue(subjectLessonList.size() >= 6, "科目数据不正确");

		int diffSum = 0;
		List<Integer> diffList = new ArrayList<>();
		for (SubjectLesson subjectLesson : subjectLessonList) {
			int diff = subjectLesson.getOptLesson() - subjectLesson.getProLesson();
			diffSum += diff;
			diffList.add(diff);
		}
	
		Collections.sort(diffList,new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return (o1 < o2) ? 1 : ((o1.equals(o2)) ? 0 : -1);
			}
		});
		
		int minLessonValue = diffSum + diffList.get(0) + diffList.get(1) + diffList.get(2);
		
		if(totalWeekLessons - fixedSumLesson < minLessonValue) {
			throw new IllegalArgumentException("走班课时不足，请增加年级周总课时或减少非走班科目周总课时");
		}

	}
	
	
	/**
	 * 验证定二走一课时
	 * 
	 * @return
	 */
	public static void validateFixedTwoGoOneSetting(int totalWeekLessons, int fixedSumLesson,
			List<SubjectGroupLesson> subjectGroupLessonList, boolean hasTech) {

		Assert.isTrue(subjectGroupLessonList.size() ==2, "科目组数据不正确");
		
		//是否通过学选分离
		boolean isPassOptAndProWay = false;
		//是否通过科目组分离
		boolean isPassGroupWay = false;
		
		/**
		 * 验证学选分离
		 */
		// 选考科目数
		int optSubjectNum = 3;
		// 选考中最大的的学时
		int optMaxLessionNum = 0;
		// 学考科目数
		int proSubjectNum = hasTech ? 4 : 3;
		// 学考中最大的学时
		int proMaxLessionNum = 0;
		
		for (SubjectGroupLesson subjectGroupLesson : subjectGroupLessonList) {
			optMaxLessionNum = subjectGroupLesson.getOptLesson() > optMaxLessionNum ? subjectGroupLesson.getOptLesson()
					: optMaxLessionNum;
			proMaxLessionNum = subjectGroupLesson.getProLesson() > proMaxLessionNum ? subjectGroupLesson.getProLesson()
					: proMaxLessionNum;
		}
		
		int minOptAndProWayValue = optSubjectNum * optMaxLessionNum + proSubjectNum * proMaxLessionNum;
		
		isPassOptAndProWay = totalWeekLessons - fixedSumLesson >= minOptAndProWayValue;
		
		/**
		 * 验证科目组分离
		 */
		SubjectGroupLesson sglOne = subjectGroupLessonList.get(0);
		SubjectGroupLesson sglTwo = subjectGroupLessonList.get(1);
		
		// 科目组一的科目数
		int groupOneSubjectNum = 3;
		// 科目组一的最大的学时
		int groupOneMaxLessionNum = sglOne.optLesson>sglOne.proLesson?sglOne.optLesson:sglOne.proLesson;
		
		// 科目组二的科目数
		int groupTwoSubjectNum = 3;
		// 科目组二的最大的学时
		int groupTwoMaxLessionNum = sglTwo.optLesson>sglTwo.proLesson?sglTwo.optLesson:sglTwo.proLesson;
		
		//技术放到学时大的里面，如果学时相同随便放一个就行
		if(hasTech) {
			if(groupOneMaxLessionNum > groupTwoMaxLessionNum) {
				groupOneSubjectNum ++;
			}else {
				groupTwoSubjectNum ++;
			}
		}

		int minGroupWayValue= groupOneSubjectNum * groupOneMaxLessionNum + groupTwoSubjectNum * groupTwoMaxLessionNum;
		
		isPassGroupWay = totalWeekLessons - fixedSumLesson >= minGroupWayValue;
		
		if(!isPassOptAndProWay && !isPassGroupWay) {
			throw new IllegalArgumentException("走班课时不足，请增加年级周总课时或减少非走班科目周总课时");
		}
	}

	/**
	 * 科目课时
	 * 
	 * @author hushowly@foxmail.com
	 *
	 */
	public static class SubjectLesson {
		private String subjectId;
		private int optLesson;
		private int proLesson;

		public String getSubjectId() {
			return subjectId;
		}

		public void setSubjectId(String subjectId) {
			this.subjectId = subjectId;
		}

		public int getOptLesson() {
			return optLesson;
		}

		public void setOptLesson(int optLesson) {
			this.optLesson = optLesson;
		}

		public int getProLesson() {
			return proLesson;
		}

		public void setProLesson(int proLesson) {
			this.proLesson = proLesson;
		}
	}

	/**
	 * 科目组课时
	 * 
	 * @author Administrator
	 *
	 */
	public static class SubjectGroupLesson {
		private String subjectIds;
		private String groupName;
		private int optLesson;
		private int proLesson;

		public String getSubjectIds() {
			return subjectIds;
		}

		public void setSubjectIds(String subjectIds) {
			this.subjectIds = subjectIds;
		}

		public String getGroupName() {
			return groupName;
		}

		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}

		public int getOptLesson() {
			return optLesson;
		}

		public void setOptLesson(int optLesson) {
			this.optLesson = optLesson;
		}

		public int getProLesson() {
			return proLesson;
		}

		public void setProLesson(int proLesson) {
			this.proLesson = proLesson;
		}

	}
}
