package com.talkweb.wishFilling.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.TableOrder;

/**
 * jfreechart-自定义画蛛网图
 * @version 1.0
 * @author zhanghuihui
 *
 */
public class CalibrationSpiderWebPlot extends SpiderWebPlot {
	private static final long serialVersionUID = -11L;
	private NumberFormat format = NumberFormat.getInstance();
	private static final double PERPENDICULAR = 90;
	private static final double TICK_SCALE = 0.045; // 刻度线长度
	private int valueLabelGap = DEFAULT_GAP;
	private static final int DEFAULT_GAP = 10;
	private static final double THRESHOLD = 15;
	public static final int DEFAULT_TICKS = 2; // 刻度圆圈数
	public static final double DEFAULT_MAX_VALUE = 100; // 坐标最大值
	public static final boolean DEFAULT_DRAW_RING = true; // 画换默认值

	private List<List<double[]>> ticksPointList = new ArrayList<List<double[]>>();
	/**
	 * 刻度数/环数
	 */
	private int ticks = DEFAULT_TICKS;

	/**
	 * 画环
	 */
	private boolean drawRing = false;

	/**
	 * 刻度前缀
	 */
	private String lablePrefix = "";

	/**
	 * 刻度后缀
	 */
	private String lableSuffix = "";

	/**
	 * 默认坐标最大值和画环
	 * 
	 * @param createCategoryDataset
	 */
	public CalibrationSpiderWebPlot(CategoryDataset createCategoryDataset) {
		this(createCategoryDataset, DEFAULT_MAX_VALUE); // 设置坐标最大值为默认的值
	}

	/**
	 * 默认画环,与刻度数
	 * 
	 * @param createCategoryDataset
	 * @param maxValue
	 */
	public CalibrationSpiderWebPlot(CategoryDataset createCategoryDataset, double maxValue) {
		this(createCategoryDataset, maxValue, DEFAULT_DRAW_RING); // 设置画换默认值
	}

	/**
	 * 自定义坐标最大值和画环
	 * 
	 * @param createCategoryDataset
	 * @param maxValue
	 * @param drawRing
	 */
	public CalibrationSpiderWebPlot(CategoryDataset createCategoryDataset, double maxValue, boolean drawRing) {
		this(createCategoryDataset, maxValue, drawRing, DEFAULT_TICKS);// 设置刻度数默认值
	}

	/**
	 * 自定义坐标最大值和画环、刻度数
	 * 
	 * @param createCategoryDataset
	 * @param maxValue
	 * @param drawRing
	 */
	public CalibrationSpiderWebPlot(CategoryDataset createCategoryDataset, double maxValue, boolean drawRing,
			int ticks) {
		this(createCategoryDataset, maxValue, drawRing, ticks, "");// 设置刻度前缀默认值
	}

	/**
	 * 自定义坐标最大值和画环以及刻度前缀、刻度数
	 * 
	 * @param createCategoryDataset
	 * @param maxValue
	 * @param drawRing
	 */
	public CalibrationSpiderWebPlot(CategoryDataset createCategoryDataset, double maxValue, boolean drawRing, int ticks,
			String lablePrefix) {
		this(createCategoryDataset, maxValue, drawRing, ticks, lablePrefix, "");
		;// 设置刻度后缀默认值
	}

	/**
	 * 自定义坐标最大值和画环以及刻度前/后缀、刻度数
	 * 
	 * @param createCategoryDataset
	 * @param maxValue
	 * @param drawRing
	 */
	public CalibrationSpiderWebPlot(CategoryDataset createCategoryDataset, double maxValue, boolean drawRing, int ticks,
			String lablePrefix, String lableSuffix) {
		super(createCategoryDataset);
		this.setMaxValue(maxValue);// 设置坐标最大值
		this.setDrawRing(drawRing);// 设置画换
		this.setTicks(ticks);// 设置刻度数
		this.setLablePrefix(lablePrefix);// 刻度前缀
		this.setLableSuffix(lableSuffix);// 刻度后缀
	}

	/**
	 * 画图，支持添加圆环
	 * 
	 * @param g2
	 *            the graphics device.
	 * @param area
	 *            the area within which the plot should be drawn.
	 * @param anchor
	 *            the anchor point (<code>null</code> permitted).
	 * @param parentState
	 *            the state from the parent plot, if there is one.
	 * @param info
	 *            collects info about the drawing.
	 */
	public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {

		// adjust for insets...
		RectangleInsets insets = getInsets();
		insets.trim(area);

		if (info != null) {
			info.setPlotArea(area);
			info.setDataArea(area);
		}

		drawBackground(g2, area);
		drawOutline(g2, area);

		Shape savedClip = g2.getClip();

		g2.clip(area);
		Composite originalComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getForegroundAlpha()));

		if (!DatasetUtilities.isEmptyOrNull(this.getDataset())) {
			int seriesCount = 0, catCount = 0;

			if (this.getDataExtractOrder() == TableOrder.BY_ROW) {
				seriesCount = this.getDataset().getRowCount();
				catCount = this.getDataset().getColumnCount();
			} else {
				seriesCount = this.getDataset().getColumnCount();
				catCount = this.getDataset().getRowCount();
			}

			// ensure we have a maximum value to use on the axes
			if (this.getMaxValue() == DEFAULT_MAX_VALUE)
				calculateMaxValue(seriesCount, catCount);

			// Next, setup the plot area

			// adjust the plot area by the interior spacing value

			double gapHorizontal = area.getWidth() * getInteriorGap();
			double gapVertical = area.getHeight() * getInteriorGap();

			double X = area.getX() + gapHorizontal / 2;
			double Y = area.getY() + gapVertical / 2;
			double W = area.getWidth() - gapHorizontal;
			double H = area.getHeight() - gapVertical;

			double headW = area.getWidth() * this.headPercent;
			double headH = area.getHeight() * this.headPercent;

			// make the chart area a square
			double min = Math.min(W, H) / 2;
			X = (X + X + W) / 2 - min;
			Y = (Y + Y + H) / 2 - min;
			W = 2 * min;
			H = 2 * min;

			Point2D centre = new Point2D.Double(X + W / 2, Y + H / 2);
			Rectangle2D radarArea = new Rectangle2D.Double(X, Y, W, H);

			// draw the axis and category label
			//画背景线1
			for (int cat = 0; cat < catCount; cat++) {
				double angle = getStartAngle() + (getDirection().getFactor() * cat * 360 / catCount);

				// 如果只有两个分类，设置固定角度
				if (catCount == 2 && cat == 1) {
					angle = 0;
				}

				Point2D endPoint = getWebPoint(radarArea, angle, 1);
				// 1 = end of axis
				Line2D line = new Line2D.Double(centre, endPoint);
				g2.setPaint(this.getAxisLinePaint());
				g2.setStroke(this.getAxisLineStroke());
				g2.setPaint(new Color(192,192,192)); //浅灰色
				g2.draw(line);
				drawLabel(g2, radarArea, 0.0, cat, angle, 360.0 / catCount);
			}
			//画背景线2
			if (this.isDrawRing()) {
				for(int i = 0; i < ticksPointList.size(); i ++) {
					List<double[]> prePointList = null;
					if(i == 0) {
						prePointList = ticksPointList.get(ticksPointList.size() - 1);
					} else {
						prePointList = ticksPointList.get(i - 1);
					}
					
					List<double[]> pointList = ticksPointList.get(i);
					
					for(int j = 0; j < pointList.size(); j ++ ) {
						double[] point = pointList.get(j);
						double[] prePoint = prePointList.get(j);
						g2.setPaint(new Color(192,192,192)); //浅灰色
						g2.draw(new Line2D.Double(point[0], point[1], prePoint[0], prePoint[1]));
					}
				}
			}
			
//			// 画环
//			if (this.isDrawRing()) {
//				Point2D topPoint = getWebPoint(radarArea, 90, 1); // 以90度为轴心，计算各个圆环的x、y坐标
//				double topPointR = centre.getY() - topPoint.getY(); // 轴心顶点圆的半径
//				double step = topPointR / this.getTicks(); // 每个刻度的半径长
//
//				for (int p = this.getTicks(); p >= 1; p--) {
//					double r = p * step;
//					double upperLeftX = centre.getX() - r;
//					double upperLeftY = centre.getY() - r;
//					double d = 2 * r;
//					Ellipse2D ring = new Ellipse2D.Double(upperLeftX, upperLeftY, d, d);
//					g2.setPaint(Color.cyan); // 设置圆环的颜色，设置圆环的别的属性直接g2（Graphics2D）
//					g2.draw(ring);
//				}
//			}

			// Now actually plot each of the series polygons..
			for (int series = 0; series < seriesCount; series++) {
				drawRadarPoly(g2, radarArea, centre, info, series, catCount, headH, headW);
			}
		} else {
			drawNoDataMessage(g2, area);
		}
		g2.setClip(savedClip);
		g2.setComposite(originalComposite);
		drawOutline(g2, area);
	}

	/**
	 * 增加刻度
	 */
	@Override
	protected void drawLabel(final Graphics2D g2, final Rectangle2D plotArea, final double value, final int cat,
			final double startAngle, final double extent) {
		//画顶点
		super.drawLabel(g2, plotArea, value, cat, startAngle, extent);
		final FontRenderContext frc = g2.getFontRenderContext();
		final double[] transformed = new double[2];
		final double[] transformer = new double[2];
		final Arc2D arc1 = new Arc2D.Double(plotArea, startAngle, 0, Arc2D.OPEN);

		/**
		 * 设置默认值
		 */
		if (ticks <= 0) {
			ticks = DEFAULT_TICKS;
		}

		List<double[]> pointList = new ArrayList<double[]>();
		for (int i = 1; i <= ticks; i++) {

			final Point2D point1 = arc1.getEndPoint();

			final double deltaX = plotArea.getCenterX();
			final double deltaY = plotArea.getCenterY();
			double labelX = point1.getX() - deltaX;
			double labelY = point1.getY() - deltaY;

			final double scale = ((double) i / (double) ticks);
			final AffineTransform tx = AffineTransform.getScaleInstance(scale, scale);
			final AffineTransform pointTrans = AffineTransform.getScaleInstance(scale, scale);
			transformer[0] = labelX;
			transformer[1] = labelY;
			pointTrans.transform(transformer, 0, transformed, 0, 1);
			final double pointX = transformed[0] + deltaX;
			final double pointY = transformed[1] + deltaY;
			tx.transform(transformer, 0, transformed, 0, 1);
			labelX = transformed[0] + deltaX;
			labelY = transformed[1] + deltaY;

			final Composite saveComposite = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

			pointList.add(new double[]{pointX, pointY});
//			if(startAngle == 90) {
//				this.ticksPointList.add();
//			} else {
		    	//画刻度线
//				g2.draw(new Line2D.Double(pointX, pointY, ticksPointList.get(i - 1)[0], ticksPointList.get(i - 1)[1]));
//				double[] prePoint = this.ticksPointList.get(i - 1);
//				prePoint[0] = pointX;
//				prePoint[1] = pointY;
//			}
				
			

			if (startAngle == this.getStartAngle()) {
				String label = format.format(((double) i / (double) ticks) * this.getMaxValue());
				final LineMetrics lm = getLabelFont().getLineMetrics(label, frc);
				final double ascent = lm.getAscent();
				if (Math.abs(labelX - plotArea.getCenterX()) < THRESHOLD) {
					labelX += valueLabelGap;
					labelY += ascent / (float) 2;
				} else if (Math.abs(labelY - plotArea.getCenterY()) < THRESHOLD) {
					labelY += valueLabelGap;
				} else if (labelX >= plotArea.getCenterX()) {
					if (labelY < plotArea.getCenterY()) {
						labelX += valueLabelGap;
						labelY += valueLabelGap;
					} else {
						labelX -= valueLabelGap;
						labelY += valueLabelGap;
					}
				} else {
					if (labelY > plotArea.getCenterY()) {
						labelX -= valueLabelGap;
						labelY -= valueLabelGap;
					} else {
						labelX += valueLabelGap;
						labelY -= valueLabelGap;
					}
				}
				g2.setPaint(getLabelPaint());
				g2.setFont(getLabelFont());

				// 添加刻度的前缀和后缀
				if (null != this.getLablePrefix() && !"".equals(this.getLablePrefix())) {
					label = this.getLablePrefix() + label;
				}
				if (null != this.getLableSuffix() && !"".equals(this.getLableSuffix())) {
					label = label + this.getLableSuffix();
				}
				//画刻度
				//g2.drawString(label, (float) labelX, (float) labelY);
			}
			g2.setComposite(saveComposite);
		}
		this.ticksPointList.add(pointList);
	}

	/**
	 * Draws a radar plot polygon. 如果只有两个分类，设置固定角度
	 * 
	 * @param g2
	 *            the graphics device.
	 * @param plotArea
	 *            the area we are plotting in (already adjusted).
	 * @param centre
	 *            the centre point of the radar axes
	 * @param info
	 *            chart rendering info.
	 * @param series
	 *            the series within the dataset we are plotting
	 * @param catCount
	 *            the number of categories per radar plot
	 * @param headH
	 *            the data point height
	 * @param headW
	 *            the data point width
	 */
	protected void drawRadarPoly(Graphics2D g2, Rectangle2D plotArea, Point2D centre, PlotRenderingInfo info,
			int series, int catCount, double headH, double headW) {

		Polygon polygon = new Polygon();

		EntityCollection entities = null;
		if (info != null) {
			entities = info.getOwner().getEntityCollection();
		}

		// plot the data...
		for (int cat = 0; cat < catCount; cat++) {

			Number dataValue = getPlotValue(series, cat);

			if (dataValue != null) {
				double value = dataValue.doubleValue();

				if (value >= 0) { // draw the polygon series...

					// Finds our starting angle from the centre for this axis

					double angle = getStartAngle() + (getDirection().getFactor() * cat * 360 / catCount);

					// 如果只有两个分类，设置固定角度
					if (catCount == 2 && cat == 1) {
						angle = 0;
					}

					// The following angle calc will ensure there isn't a top
					// vertical axis - this may be useful if you don't want any
					// given criteria to 'appear' move important than the
					// others..
					// + (getDirection().getFactor()
					// * (cat + 0.5) * 360 / catCount);

					// find the point at the appropriate distance end point
					// along the axis/angle identified above and add it to the
					// polygon

					Point2D point = getWebPoint(plotArea, angle, value / this.getMaxValue());
					polygon.addPoint((int) point.getX(), (int) point.getY());

					// put an elipse at the point being plotted..

					Paint paint = getSeriesPaint(series);
					Paint outlinePaint = getSeriesOutlinePaint(series);
					Stroke outlineStroke = getSeriesOutlineStroke(series);

					Ellipse2D head = new Ellipse2D.Double(point.getX() - headW / 2, point.getY() - headH / 2, headW,
							headH);
					//画顶点
					g2.setPaint(new Color(247,151,204));
					//g2.setPaint(paint);
					g2.fill(head);
					g2.setStroke(outlineStroke);
					//g2.setPaint(outlinePaint);
					//画顶点值
					/*double labelX = point.getX()+20;
					double labelY = point.getY();
					g2.drawString(value+"", (float) labelX, (float) labelY);*/
					g2.draw(head);

					if (entities != null) {
						int row = 0;
						int col = 0;
						if (this.getDataExtractOrder() == TableOrder.BY_ROW) {
							row = series;
							col = cat;
						} else {
							row = cat;
							col = series;
						}
						String tip = null;
						if (this.getToolTipGenerator() != null) {
							tip = this.getToolTipGenerator().generateToolTip(this.getDataset(), row, col);
						}

						String url = null;
						if (this.getURLGenerator() != null) {
							url = this.getURLGenerator().generateURL(this.getDataset(), row, col);
						}

						Shape area = new Rectangle((int) (point.getX() - headW), (int) (point.getY() - headH),
								(int) (headW * 2), (int) (headH * 2));
						CategoryItemEntity entity = new CategoryItemEntity(area, tip, url, this.getDataset(),
								this.getDataset().getRowKey(row), this.getDataset().getColumnKey(col));
						entities.add(entity);
					}

				}
			}
		}
		// Plot the polygon
        //画图形
		Paint paint = getSeriesPaint(series);
		g2.setPaint(paint);
		//加粗边线
		g2.setStroke(new BasicStroke(2.0F));
		//g2.setStroke(getSeriesOutlineStroke(series));
		g2.setColor(new Color(247,151,204));
		g2.draw(polygon);

		// Lastly, fill the web polygon if this is required
		//填充颜色
		if (this.isWebFilled()) {
			g2.setColor(new Color(247,151,204));
			//设置透明度
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
			g2.fill(polygon);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getForegroundAlpha()));
		}
	}

	/**
	 * 获取分类的最大值
	 * 
	 * @param seriesCount
	 *            the number of series
	 * @param catCount
	 *            the number of categories
	 */
	private void calculateMaxValue(int seriesCount, int catCount) {
		double v = 0;
		Number nV = null;

		for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
			for (int catIndex = 0; catIndex < catCount; catIndex++) {
				nV = getPlotValue(seriesIndex, catIndex);
				if (nV != null) {
					v = nV.doubleValue();
					if (v > this.getMaxValue()) {
						this.setMaxValue(v);
					}
				}
			}
		}
	}

	public String getLablePrefix() {
		return lablePrefix;
	}

	public void setLablePrefix(String lablePrefix) {
		this.lablePrefix = lablePrefix;
	}

	public String getLableSuffix() {
		return lableSuffix;
	}

	public void setLableSuffix(String lableSuffix) {
		this.lableSuffix = lableSuffix;
	}

	public boolean isDrawRing() {
		return drawRing;
	}

	public void setDrawRing(boolean drawRing) {
		this.drawRing = drawRing;
	}

	public int getTicks() {
		return ticks;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
	}
	
	public static void main(String[] args) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String group1 = "菲菲的成绩";
		
		dataset.addValue(90, "", "政治(90)");
		dataset.addValue(75, "", "历史(75)");
		dataset.addValue(40, "", "地理(40)");
//		dataset.addValue(20, "", "物理");
//		dataset.addValue(50, "", "化学");
//		dataset.addValue(50, "", "生物");
		
		Font font = new Font("宋体",Font.PLAIN,16);
		CalibrationSpiderWebPlot webPlot = new CalibrationSpiderWebPlot(dataset);
		//设置无边线
		webPlot.setOutlinePaint(null);
		webPlot.setLabelFont(font);//设置字体大小
		webPlot.setLabelPaint(Color.BLACK) ; // 字体颜色  new Color(192,192,192)
		JFreeChart jfreechart = new JFreeChart(webPlot);
		// 设置外层图片 无边框 无背景色 背景图片透明     
		jfreechart.setBorderVisible(false);  
		jfreechart.setBackgroundPaint(null);  
		jfreechart.setBackgroundImageAlpha(0.0f);
		//去掉底部title
		jfreechart.setSubtitles(new ArrayList());
		/*LegendTitle legendtitle = new LegendTitle(webPlot);
		legendtitle.setPosition(RectangleEdge.BOTTOM);*/
		//jfreechart.addSubtitle(legendtitle);
		try {
			OutputStream os = new FileOutputStream("radar.PNG");// 图片是文件格式的，故要用到FileOutputStream用来输出。
			ChartUtilities.writeChartAsPNG(os, jfreechart, 800, 600);
			// 使用一个面向application的工具类，将chart转换成JPEG格式的图片。第3个参数是宽度，第4个参数是高度。
			os.close();// 关闭输出流
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
