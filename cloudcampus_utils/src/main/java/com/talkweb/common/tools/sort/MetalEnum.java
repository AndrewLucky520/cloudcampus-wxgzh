package com.talkweb.common.tools.sort;
/**
 * @ClassName MetalEnum.java
 * @author liboqi
 * @version 1.0
 * @Description 天地枚举
 * @date 2015年12月18日 下午5:42:23
 */
public enum MetalEnum {
		//"甲、乙、丙、丁、戊、己、庚、辛、壬、癸";
		//"子、丑、寅、卯、辰、巳、午、未、申、酉、戌、亥"
		TGJIA("甲",1),TGYI("乙",2),TGBING("丙",3),
		TGDING("丁",4),TGWU("戊",5),TGJI("己",6),
		TGGENG("庚",7),TGXING("辛",8),TGREN("壬",9),
		TGGUI("癸",10),
		TGZI("子",11),TGCHOU("丑",12),TGYIN("寅",13),
		TGMAO("卯",14),TGCHEN("辰",15),TGSI("巳",16),
		TGWU1("午",17),TGWEI("未",18),TGSHEN("申",19),
		TGSHOU("酉",20),TGXU("戌",21),TGHAI("亥",22);
		private String name;
		private int value;
		
		 public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		// 构造方法
	    private MetalEnum(String name, int value) {
	        this.name = name;
	        this.value = value;
	    }
	    
	    public static Integer getValue(String name){
		   for (MetalEnum c : MetalEnum.values()) {
	           if (name.equals(c.getName())) {
	               return c.value;
	           }
	       }
		   return null;
	    }
}
