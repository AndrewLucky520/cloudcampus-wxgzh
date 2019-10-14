package com.talkweb.scheduleJasperReport.util;

import java.awt.Color;
import java.awt.geom.Ellipse2D.Double;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;

import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;

public class JFreeChartTrend implements JRChartCustomizer {

	@Override
	public void customize(JFreeChart chart, JRChart jasperChart) {
		CategoryPlot plot = (CategoryPlot)chart.getCategoryPlot();		
        CategoryAxis domainAxis = plot.getDomainAxis();//x轴
        ValueAxis rangeAxis = plot.getRangeAxis(); //y轴
        
        domainAxis.setVisible(false);
        rangeAxis.setAutoTickUnitSelection(false);
        
        double value = rangeAxis.getUpperBound();
        double size = 0;
        boolean axis = false;
        if (value >= 100){
        	size = 10;
        }else if(value < 20){
        	size = 1;
        }else{
        	size = 2;
        	axis = true;
        }
        // 设置y轴的间隔为2个刻度
        NumberTickUnit unit = new NumberTickUnit(size);
        ((NumberAxis)rangeAxis).setTickUnit(unit);
        // 数据轴方向
        rangeAxis.setInverted(axis);
        
        // 设置线条颜色及点的形状
        LineAndShapeRenderer lineRender = (LineAndShapeRenderer)plot.getRenderer();
        lineRender.setSeriesPaint(0,Color.decode("#00bc46"));
        lineRender.setSeriesPaint(1,Color.decode("#999999"));
        lineRender.setSeriesShape(0, new Double(-2D, -2D, 4D, 4D));
        lineRender.setSeriesShapesVisible(1, false);    
	}
}
