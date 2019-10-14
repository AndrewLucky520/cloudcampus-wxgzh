package com.talkweb.jasperReport.util;

import java.awt.Color;
import java.awt.geom.Ellipse2D.Double;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;

import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;

public class JFreeChartClass implements JRChartCustomizer {

	@Override
	public void customize(JFreeChart chart, JRChart jasperChart) {
		CategoryPlot plot = (CategoryPlot)chart.getCategoryPlot();		
        
        // 设置线条颜色及点的形状
        LineAndShapeRenderer lineRender = (LineAndShapeRenderer)plot.getRenderer();
        lineRender.setSeriesPaint(0,Color.decode("#00bc46"));
        lineRender.setSeriesPaint(1,Color.decode("#999999"));
        lineRender.setSeriesShape(0, new Double(-2D, -2D, 4D, 4D));
        lineRender.setSeriesShapesVisible(1, false);    
	}
}
