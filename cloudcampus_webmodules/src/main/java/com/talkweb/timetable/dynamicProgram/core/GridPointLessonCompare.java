package com.talkweb.timetable.dynamicProgram.core;

import java.io.Serializable;
import java.util.Comparator;

import com.talkweb.timetable.dynamicProgram.entity.GridPoint;

public class GridPointLessonCompare  implements Comparator<GridPoint>,Serializable {

	@Override
	public int compare(GridPoint o1, GridPoint o2) {
		// TODO Auto-generated method stub
		return o1.getLesson()-o2.getLesson();
	}

}
