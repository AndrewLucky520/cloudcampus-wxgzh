package com.talkweb.timetable.dynamicProgram.core;

import java.util.Comparator;

import com.talkweb.timetable.dynamicProgram.entity.ScheduleTaskGroup;

public class CourseCompare implements Comparator<ScheduleTaskGroup> {


	@Override
	public int compare(ScheduleTaskGroup o1, ScheduleTaskGroup o2) {
		// TODO Auto-generated method stub
		int rs = (int) (o1.getAvgTaskNum() - o2.getAvgTaskNum());
		if(rs<0){
			rs = -1;
		}else if(rs>0){
			rs = 1;
		}
		System.out.println("+++++++compare:"+rs);
		return rs;
	}

}
