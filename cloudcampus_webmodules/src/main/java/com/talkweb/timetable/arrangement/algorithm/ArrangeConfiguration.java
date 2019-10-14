package com.talkweb.timetable.arrangement.algorithm;

import org.jgap.Configuration;
import org.jgap.DefaultFitnessEvaluator;
import org.jgap.InvalidConfigurationException;
import org.jgap.event.EventManager;
import org.jgap.impl.BestChromosomesSelector;
import org.jgap.impl.ChromosomePool;
import org.jgap.impl.GABreeder;
import org.jgap.impl.StockRandomGenerator;
import org.jgap.util.ICloneable;

/**
 * 排课算法的参数配置类
 *
 */
public class ArrangeConfiguration extends Configuration implements ICloneable {
	
	public ArrangeConfiguration() {
		super("", "");
		
		try {
		      setBreeder(new GABreeder());
		      setRandomGenerator(new StockRandomGenerator());
		      //setRandomGenerator(new GaussianRandomGenerator());
		      setEventManager(new EventManager());
		      BestChromosomesSelector bestChromsSelector = new BestChromosomesSelector(
		          this, 0.90d);
		      bestChromsSelector.setDoubletteChromosomesAllowed(true);
		      addNaturalSelector(bestChromsSelector, false);
		      setMinimumPopSizePercent(0);
		      //
		      setSelectFromPrevGen(1.0d);
		      setKeepPopulationSizeConstant(true);
		      setFitnessEvaluator(new DefaultFitnessEvaluator());
		      setChromosomePool(new ChromosomePool());
		      addGeneticOperator(new NormalOperator(this));
		      //addGeneticOperator(new CrossoverOperator(this, 0.35d));
		      //addGeneticOperator(new MutationOperator(this, 12));
		      
		    }
		    catch (InvalidConfigurationException e) {
		      throw new RuntimeException(
		          "Fatal error: DefaultConfiguration class could not use its "
		          + "own stock configuration values. This should never happen. "
		          + "Please report this as a bug to the JGAP team.");
		    }
		
	}
	
	  public Object clone() {
		    return super.clone();
		  }
	
	
}
