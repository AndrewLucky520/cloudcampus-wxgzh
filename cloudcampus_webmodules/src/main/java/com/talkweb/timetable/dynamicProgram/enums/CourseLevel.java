package com.talkweb.timetable.dynamicProgram.enums;
/**
 * 课程排上午还是下午
 * @author talkweb
 *
 */
public enum CourseLevel {

		// 尽量上午
		AmFirst (0),
		// 尽量下午
		PmFirst(1),
		// 都可以
		AllCan(2) ,
		// 尽量上午,但也分配下午
		AmButNotAll(3);

		private int level;
		
		private CourseLevel(int level){
			this.level = level;
		}
		
		public int getValue( ){
			return this.level;
		}
		
}
