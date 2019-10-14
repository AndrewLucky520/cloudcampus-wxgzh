package com.talkweb.timetable.arrangement.algorithm;

import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;

/**
 * 适应度函数
 * 
 * @author Li xi yuan
 *
 */
public class ArrangeFitnessFunction extends FitnessFunction {

	private Configuration config;
	
	private RuleConflict ruleConflict;
	
	public ArrangeFitnessFunction(Configuration config, RuleConflict ruleConflict){
		this.config = config;
		this.ruleConflict = ruleConflict;
	}
	/* *
	 * 计算排课的适应度
	 * 
	 */
	@Override
	protected double evaluate(IChromosome chromosome) {
		//默认为适应值越大为最优的
		Gene[] genes = chromosome.getGenes();

		return chromosomeEvaluate(genes);
		
	}
	
	private double chromosomeEvaluate(Gene[] genes){
		int globalEval = 0;
		//计算染色体的适应度
		for (Gene gene : genes) {
			CourseGene courseGene = (CourseGene) gene;
//			if(courseGene.getCourseLevel()==2 && courseGene.getLesson()<3){
//				globalEval -= courseGene.getCourseLevel() * 4;
//			}
			globalEval += courseGene.getLesson() ;
		}
		
		return globalEval < 0 ? 0 : globalEval;
		
	}
	
	

}
