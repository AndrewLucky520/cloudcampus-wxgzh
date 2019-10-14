package com.talkweb.timetable.arrangement.algorithm;

import org.jgap.BaseGene;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.jgap.RandomGenerator;
import org.jgap.UnsupportedRepresentationException;
import org.jgap.util.ICloneable;

import com.talkweb.timetable.arrangement.domain.ArrangeCourse;

/**
 * 单个课眼
 * @author talkweb
 *
 */
public class CourseGene extends BaseGene implements ICloneable {
	/**
	 * 保存正常课（非单双周）
	 */
	private ArrangeCourse arrangeCourse;
	
	private String classId;
	
	private int day;
	
	private int lesson;
	
	private int nearNum;
	
	/**
	 * 粒度，支持 1.0  或 0.5（单双周课）
	 */
	private double lessonSize = 1;
	
	/**
	 * 固排课[位置固定，不能改动]
	 */
	private boolean fixed = false;
	
	
	/**
	 * 单双周课
	 */
	
	private CourseGene dwCourseGene;
	
	

	public CourseGene(Configuration config) throws InvalidConfigurationException {
		super(config);

	}
	
	public CourseGene(Configuration config, ArrangeCourse arrangeCourse) throws InvalidConfigurationException {
		super(config);
		this.arrangeCourse = arrangeCourse;
		this.classId = this.arrangeCourse.getClassId();
	}

	
	public CourseGene newGene() {
		CourseGene gene = (CourseGene) super.newGene();
		gene.setClassId(this.classId);
		gene.setDay(this.day);
		gene.setLesson(this.lesson);
		gene.setLessonSize(this.lessonSize);
		gene.setFixed(this.fixed);
		gene.setArrangeCourse(this.arrangeCourse);
		gene.setDwCourseGene(this.dwCourseGene);
		return gene;
	}
	
	public int getUnitSize(){
		return this.arrangeCourse.getUnitSize();
	}
	
	public int getCourseLevel(){
		return this.arrangeCourse.getCourseLevel();
	}
	
	public boolean isFixed() {
		return fixed;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}
	
	public boolean isMerge() {
		return this.arrangeCourse.isMerge();
	}

	public void setMerge(boolean merge) {
		this.arrangeCourse.setMerge(merge);
	}


	public int size() {
		    return 1;
	  }
	
	public boolean isOddEven(){
		return this.lessonSize == 0.5 ? true : false;
	}
	/**
	 * 判断是否单双周课
	 * @return
	 */
//	public boolean hasEvenCourse(){
//		return this.oddEvenCourse!=null;
//	}
//
//	public String getOddEvenCourseName(){
//		if(this.oddEvenCourse==null){
//			return "";
//		}else{
//			return this.oddEvenCourse.getCourseName();
//		}
//	}

	public String getClassId() {
		return classId;
	}
	
	public String getCourseId(){
		return this.arrangeCourse.getCourseId();
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public ArrangeCourse getArrangeCourse() {
		return arrangeCourse;
	}

	public void setArrangeCourse(ArrangeCourse arrangeCourse) {
		this.arrangeCourse = arrangeCourse;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getLesson() {
		return lesson;
	}

	public void setLesson(int lesson) {
		this.lesson = lesson;
	}

	public int getNearNum() {
		return nearNum;
	}

	public void setNearNum(int nearNum) {
		this.nearNum = nearNum;
	}
	
	public boolean isNeared(){
		return nearNum>=2;
	}

	public double getLessonSize() {
		return lessonSize;
	}

	public void setLessonSize(double lessonSize) {
		this.lessonSize = lessonSize;
	}
	
	

//	public ArrangeCourse getOddEvenCourse() {
//		return oddEvenCourse;
//	}
//
//	public void setOddEvenCourse(ArrangeCourse oddEvenCourse) {
//		this.oddEvenCourse = oddEvenCourse;
//	}

	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public void setAllele(Object a_newValue) {
		if(a_newValue instanceof ArrangeCourse)
		this.arrangeCourse = (ArrangeCourse) a_newValue;
		
	}

	@Override
	public String getPersistentRepresentation()
			throws UnsupportedOperationException {
		//
		return null;
	}

	@Override
	public void setValueFromPersistentRepresentation(String a_representation)
			throws UnsupportedOperationException,
			UnsupportedRepresentationException {
		//
		
	}

	@Override
	public void setToRandomValue(RandomGenerator numberGenerator) {
		//随机变化值

		
	}

	@Override
	public void applyMutation(int index, double a_percentage) {
		//变异
		
	}

	@Override
	public int compareTo(Object o) {
		//
		return 0;
	}

	@Override
	protected Object getInternalValue() {
		//
		return this.arrangeCourse;
	}

	@Override
	protected Gene newGeneInternal() {
		try {
			Gene gene = new CourseGene(this.getConfiguration());
			return gene;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取年级id
	 */
//	private String getGradeId(){
//		String rs = this.arrangeCourse.getGradeId();
//		
//		return rs;
//	}

	public CourseGene getDwCourseGene() {
		return dwCourseGene;
	}

	public void setDwCourseGene(CourseGene dwCourseGene) {
		this.dwCourseGene = dwCourseGene;
	}

	/**
	 * 是否有双周课
	 * @return
	 */
	public boolean hasEvenCourse() {
		// TODO Auto-generated method stub
		return this.dwCourseGene==null?false:true;
	}

	public String getMcGroupId() {
		return this.arrangeCourse.getMcGroupId() ;
	}

	public void setMcGroupId(String mcGroupId) {
		this.arrangeCourse.setMcGroupId(mcGroupId);
	}
	
}
