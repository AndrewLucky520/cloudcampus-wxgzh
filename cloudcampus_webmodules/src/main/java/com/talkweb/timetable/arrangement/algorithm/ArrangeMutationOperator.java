package com.talkweb.timetable.arrangement.algorithm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.RandomGenerator;
import org.jgap.impl.MutationOperator;

import com.talkweb.timetable.arrangement.domain.ArrangeClass;
import com.talkweb.timetable.arrangement.domain.ArrangeGrid;

/**
 * 变异算子
 * 
 * @author Li xi yuan
 *
 */
public class ArrangeMutationOperator extends MutationOperator {

	private ArrangeGrid arrangeGrid;
	
	private RuleConflict ruleConflict;
	
	public ArrangeMutationOperator(Configuration a_config,
			int a_desiredMutationRate, ArrangeGrid arrangeGrid, RuleConflict ruleConflict) throws InvalidConfigurationException {
		super(a_config, a_desiredMutationRate);
		this.arrangeGrid = arrangeGrid;
		this.ruleConflict = ruleConflict;
	}

	/**
	 * 在这里将产生新的种群
	 */
	public void operate(final Population a_population, final List a_candidateChromosomes) {
		
		boolean mutate = false;
		
		int size = Math.min(getConfiguration().getPopulationSize(), a_population.size());
		RandomGenerator generator = getConfiguration().getRandomGenerator();
		
		for (int i = 0; i < size; i++) {
			//染色体
		      IChromosome chrom = a_population.getChromosome(i);
		      //单个染色体的基因组
		      Gene[] genes1 = chrom.getGenes();
		      IChromosome copyOfChromosome = null;
		      Gene[] genes = null;
		      
		      for (int j = 0; j < genes1.length; j++) {
		    	  //随机阀值
		          if (this.getMutationRateCalc() != null) {

		            mutate = this.getMutationRateCalc().toBePermutated(chrom, j);
		          }
		          else {

		            mutate = (generator.nextInt(this.getMutationRate()) == 0);
		          }
		          if (mutate) {

		            if (copyOfChromosome == null) {
		            	//变异产生新的染色体
		              copyOfChromosome = (IChromosome) chrom.clone();
		              //加到候选染色体种群中
		              a_candidateChromosomes.add(copyOfChromosome);

		              genes = copyOfChromosome.getGenes();

		              if (m_monitorActive) {
		                copyOfChromosome.setUniqueIDTemplate(chrom.getUniqueID(), 1);
		              }
		            }
		            
		            //这里进行优调结果
		            
		            
		            //随机调课
		            //this.randomArrangeCourse(genes);
		            //
		            //this.growUpForGene(genes);

		            
		          }
		        }
		      
		}
		
		
	}
	
	private void randomArrangeCourse(Gene[] genes){
		RandomGenerator generator = getConfiguration().getRandomGenerator();		
		int index = generator.nextInt(genes.length);
		//一个课眼
		CourseGene fromCourse = (CourseGene) genes[index];
		String classId = fromCourse.getClassId();
		CourseGene[][] timetable = this.loadTimetableByClassId(genes, classId);
		for (int g = 0; g < genes.length; g++) { 			
			
			if (g != index && !fromCourse.isFixed()) {				
				
            	ArrangeClass arrangeClass = this.arrangeGrid.getArrangeClass(classId);
            	int x1 = fromCourse.getDay();
            	int y1 = fromCourse.getLesson();
            	
            	//随机坐标，种群数量很大时，这里随机很费时
            	//int x2 = generator.nextInt(arrangeClass.getTotalMaxDays());
            	//int y2 = generator.nextInt(arrangeClass.getTotalMaxLesson());
            	
            	CourseGene toCourse = (CourseGene) genes[g];
            	            	
            	if(toCourse==null || !classId.equals(toCourse.getClassId())){
            		continue;
            	}
				//CourseGene toCourse = (CourseGene) timetable[x2][y2];
            	int x2 = toCourse.getDay();
            	int y2 = toCourse.getLesson();
				
				if(!fromCourse.isFixed() && !toCourse.isFixed() && !fromCourse.isMerge() && !toCourse.isMerge() && fromCourse.getLessonSize()!=0.5 && toCourse.getLessonSize()!=0.5 
						&& fromCourse.getNearNum()<=1 && toCourse.getNearNum()<=1 && fromCourse.getCourseLevel()==2 && toCourse.getCourseLevel()==0){
					
					if(this.canArrangePosition(genes, timetable, arrangeClass, fromCourse, x2, y2) &&
							this.canArrangePosition(genes, timetable, arrangeClass, toCourse, x1, y1)) {
						swap(fromCourse, toCourse);
					}
					
					
				}
				
        	}
            
            
		}
	}
	/**
	 * 基因进化
	 * @param genes
	 */
	private void growUpForGene(Gene[] genes){
        Set<String> classIds = new HashSet<String>();
        for (int g = 0; g < genes.length; g++) {
        	CourseGene gene = (CourseGene) genes[g];
        	String classId = gene.getClassId();
        	classIds.add(classId);
        }
        
        for (String classId : classIds) {
        	//调一个班的课表
        	CourseGene[][] timetable = this.loadTimetableByClassId(genes, classId);
        	ArrangeClass arrangeClass = this.arrangeGrid.getArrangeClass(classId);
        	//主要检查上午的最后两节 和 下午的最后两节
        	int totalMaxDays = arrangeClass.getTotalMaxDays();
        	int totalMaxLesson = arrangeClass.getTotalMaxLesson();
        	int amLessonNum = arrangeClass.getAmLessonNum();
        	int pmLessonNum = arrangeClass.getPmLessonNum();
    		for (int d = 0; d < totalMaxDays; d++) {
    			
    			for(int l = 0; l < totalMaxLesson; l++) {

    				if((l== amLessonNum-2 || l==totalMaxLesson-2) && timetable[d][l]!=null && timetable[d][l+1]!=null) {
    					CourseGene fromCourse = timetable[d][l];
    					CourseGene toCourse = timetable[d][l+1];
    					
    					//
    					if(!fromCourse.isFixed() && !toCourse.isFixed() && !fromCourse.isMerge() && !toCourse.isMerge() && fromCourse.getLessonSize()!=0.5 && toCourse.getLessonSize()!=0.5 
    							&& !fromCourse.getCourseId().equals(toCourse.getCourseId()) && fromCourse.getNearNum()<=1 && toCourse.getNearNum()<=1 && fromCourse.getCourseLevel()==2 && toCourse.getCourseLevel()==0){

    						if(this.canSwapPosition(genes, timetable, arrangeClass, fromCourse, d, l+1) &&
    								this.canSwapPosition(genes, timetable, arrangeClass, toCourse, d, l)) {

    							swap(fromCourse, toCourse);
    						}
    						
    						
    					}
    					
    					//

    					
    				}
    				
    			}

    		}

            
            
		}
	}
	
	public boolean canArrangePosition(Gene[] genes, CourseGene[][] timetable, ArrangeClass arrangeClass, CourseGene fromCourse, int toDay, int toLesson){
		int totalMaxLesson = arrangeClass.getTotalMaxLesson();
		String courseId = fromCourse.getCourseId();
		boolean hasCourseByTeacher = this.ruleConflict.hasCourseByTeacher(genes, fromCourse.getArrangeCourse().getArrangeTeachers().keySet(), toDay, toLesson);
//		int canArrangeCourse = this.ruleConflict.canArrangeCourse(fromCourse.getCourseId(), toDay, toLesson,fromCourse.getArrangeCourse().getGradeLev());
//		int canArrangeTeacher = this.ruleConflict.canArrangeTeacher(fromCourse.getArrangeCourse().getArrangeTeachers(), toDay, toLesson,-1,null);
//		boolean hasCourseOnDay = this.ruleConflict.hasCourseOnDay(timetable, totalMaxLesson, toDay, courseId);
//		
//		if(!hasCourseByTeacher && canArrangeCourse>0 && canArrangeTeacher >0 && !hasCourseOnDay) {
//			return true;
//		}else{
//		}
		return false;
		
	}
	
	public boolean canSwapPosition(Gene[] genes, CourseGene[][] timetable, ArrangeClass arrangeClass, CourseGene fromCourse, int toDay, int toLesson){

		boolean hasCourseByTeacher = this.ruleConflict.hasCourseByTeacher(genes, fromCourse.getArrangeCourse().getArrangeTeachers().keySet(), toDay, toLesson);
//		int canArrangeCourse = this.ruleConflict.canArrangeCourse(fromCourse.getCourseId(), toDay, toLesson, fromCourse.getArrangeCourse().getGradeLev());
//		int canArrangeTeacher = this.ruleConflict.canArrangeTeacher(fromCourse.getArrangeCourse().getArrangeTeachers(), toDay, toLesson,fromCourse.getDay(),null);
		
//		if(!hasCourseByTeacher && canArrangeCourse>0 && canArrangeTeacher >0) {
//			return true;
//		}else{
//		}
		return false;
		
	}
	
	/**
	 * 将班级课程安排转换为二维表格（课表）
	 * @param genes 有哪些课眼
	 * @param classId 班级主键
	 * @return
	 */
	public CourseGene[][] loadTimetableByClassId(Gene[] genes, String classId) {
		//获取班级教学安排
		ArrangeClass arrangeClass = this.arrangeGrid.getArrangeClass(classId);
		//生成新的课表 空
		CourseGene[][] timetable = new CourseGene[arrangeClass.getTotalMaxDays()][arrangeClass.getTotalMaxLesson()];
		//根据班级课程安排-安排已定义、预排的课表
		for (int g = 0; g < genes.length; g++) {
			CourseGene course = (CourseGene) genes[g];
			if(classId.equals(course.getClassId())) {
				timetable[course.getDay()][course.getLesson()] = course;
			}
        	
		}
		
		return timetable;
		
		
	}

	
	public void swap(CourseGene fromCourse, CourseGene toCourse) {

		CourseGene temp = (CourseGene) fromCourse.clone();
		fromCourse.setDay(toCourse.getDay());
		fromCourse.setLesson(toCourse.getLesson());
		fromCourse.setNearNum(toCourse.getNearNum());
		
		toCourse.setDay(temp.getDay());
		toCourse.setLesson(temp.getLesson());
		toCourse.setNearNum(temp.getNearNum());
		
		
	}
	
	public void move(CourseGene fromCourse, int day, int lesson) {

		fromCourse.setDay(day);
		fromCourse.setLesson(lesson);
		
	}
	

}
