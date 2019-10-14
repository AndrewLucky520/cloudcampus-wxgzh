package com.talkweb.common.tools;



import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * @ClassName GeneratorRandImage
 * @author Homer
 * @version 1.0
 * @Description 生成验证码图片
 * @date 2015年3月4日
 */
public class GeneratorRandImage {

	    //指定图片的宽度和高度
		private int width  = 120;
		private int height = 30;
		private OutputStream out = null;
		//定义随机生成器，产生随机数
		private Random rand = new Random();
		
		/**
		 * 构造方法，接收指定的流
		 * @param out
		 */
		public GeneratorRandImage(OutputStream out){
			this.out = out;
		}
		
		
		/**
		 * 随机数产生方法
		 * @return
		 */
		public String getRandString(int num){
			String str = "";
			//定义一个字符串的种子，随机字符串从该种子里面拿
			final String SEED = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
			
			for (int i = 0; i < num; i++) {
				char s = SEED.charAt(rand.nextInt(SEED.length()));
				str += s;
			}
			
			return str;
		}
		
		/*
		 * 设置随机颜色
		 */
		public Color getRandColor(){
			int red   = rand.nextInt(256);   
			int green = rand.nextInt(256);
			int blue  = rand.nextInt(256);
			return new Color(red,green,blue);
		}
		
		
		/**
		 * 获得随机图片
		 */
		public String getRandImage(){
			
			//设置缓存，用来保存图片
			BufferedImage bufImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			
			//获得画笔
			Graphics g = bufImg.getGraphics();
			
			//设置背景为白色
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, width, height);
			
			//设置边框
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, width-1, height-1);
			

			//设置字体
			g.setFont(new Font("宋体",Font.BOLD,22));
			//画字符串
			String strCode = getRandString(4);
			for (int i = 0; i < strCode.length(); i++) {
				//设置字体的颜色
				g.setColor(getRandColor());
				g.drawString(String.valueOf(strCode.charAt(i)), 10 + 23 * i, 20);
			}
			
			//画干扰线
			for (int i = 0; i < 5; i++) {
				//设置直线的两点的坐标
				//设置干扰线的颜色
				g.setColor(getRandColor());
				int x1 = rand.nextInt(width);
				int y1 = rand.nextInt(height);
				int x2 = rand.nextInt(width);
				int y2 = rand.nextInt(height);
				g.drawLine(x1, y1, x2, y2);
			}
			
			
			//画干扰点
			for (int i = 0; i < 20; i++) {
				//设置干扰点的颜色
				g.setColor(getRandColor());
				
				//设置干扰点的x，y坐标
				int x = rand.nextInt(width);
				int y = rand.nextInt(height);
				g.drawOval(x, y, 2, 2);
			}
			
			//关闭画笔，显示效果
			g.dispose();
			
			
			//把图片写入到流中
			try {
				ImageIO.write(bufImg, "jpeg", out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return strCode;
			
		}
}
