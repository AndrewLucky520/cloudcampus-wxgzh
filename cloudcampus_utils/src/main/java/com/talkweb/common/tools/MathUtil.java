package com.talkweb.common.tools;


import java.util.List;

public class MathUtil {
	
	/****
	 * 根据x轴集合数据以及y轴集合数据计算出趋势线的起点和终点坐标
	 * @param x :点的x轴的集合
	 * @param y :点的y轴的集合
	 * @return 第一行表示起点的【x，y】坐标，第二行表示终点的【x，y】坐标
	 */
	public static float [][]  trendLineCoordinate(List<Float>  x,List<Float> y)
	{
		//趋势线的起点和终点坐标存放变量
		float startEnd[][]=new float[2][2];
		 
		if(x==null||y==null)  return startEnd;
		
		if(x.size()==0||y.size()==0) return startEnd;
		
		if(x.size()!=y.size()) return startEnd;
		
		
		float averX=0,averY=0;//x的平均值，y的平均值
		float sumXY=0,sumXX=0;//xy的乘积之和，xx的乘积之和
		float n=x.size();//元素个数
		
		
		float b=0;//斜率
		float a=0;//偏移
		
        for(int i=0;i<n;i++)
        {
        	averX=averX+x.get(i)/n;
        	averY=averY+y.get(i)/n;
        			
        	sumXY=sumXY+x.get(i)*y.get(i);
        	sumXX=sumXX+x.get(i)*x.get(i);
        }
        
        b=(sumXY-n*averX*averY)/(sumXX-n*averX*averX);
        a=averY-b*averX;
        
        float startX=0,endX=0,startY=0,endY=0;//起点和终点的[x,y]坐标
        
        startX=x.get(0);
        startY=a+b*startX;
        
        endX=x.get((int)n-1);
        endY=a+b*endX;
        
        //把起点终点的坐标存放到数组中
        startEnd[0][0]=startX;startEnd[0][1]=startY;
        startEnd[1][0]=endX;startEnd[1][1]=endY;
		
		return startEnd;
	}

}
