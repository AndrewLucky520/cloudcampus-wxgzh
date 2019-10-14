package com.talkweb.base.common;

/**
 * 走班常量类
 * @author hushow@foxmail.com
 *
 */
public class PlacementConstant {
	
	
	/**
	 * 走班类型枚举
	 * @author hushowly@foxmail.com
	 */
	public enum PlacementType {
		
		/**
		 * 微走班
		 */
		MICRO_GO_CLASS(1,"微走班"),
		
		/**
		 * 中走班
		 */
		MEDIUM_GO_CLASS(2, "中走班"),
		
		/**
		 * 大走班
		 */
		BIG_GO_CLASS(3, "大走班"),
		
		/**
		 * 定二走一
		 */
		FIXED_TWO_GO_ONE(4, "定二走一"),
		
		/**
		 * 三加一加二
		 */
		FIXED_THREE_TWO_ONE(5, "三加一加二");
		
		
		private Integer code;
		private String label;
		
		
		PlacementType(Integer code, String label) {
			this.code = code;
			this.label = label;
		}

		
		public Integer getCode() {
			return code;
		}
		public void setCode(Integer code) {
			this.code = code;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		
		
		public static PlacementType getEnumByCode(Integer code) {
			
			if (code == null) {
				return null;
			}
			for (PlacementType obj : PlacementType.values()) {
				if (obj.getCode().equals(code)) {
					return obj;
				}
			}
			
			throw new IllegalArgumentException("不支持的类型："+code);
		}
	}
	
	/**
	 * 走班班级类型
	 * @author hushowly@foxmail.com
	 *
	 */
	public enum ClassType {
		
		ADMIN_CLASS(6, "行政班"),
		TEACH_CLASS(7,"教学班");
		
		private Integer code;
		private String label;
		
		ClassType(Integer code, String label) {
			this.code = code;
			this.label = label;
		}

		
		public Integer getCode() {
			return code;
		}
		public void setCode(Integer code) {
			this.code = code;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		
		
		public static ClassType getEnumByCode(Integer code) {
			
			if (code == null) {
				return null;
			}
			for (ClassType obj : ClassType.values()) {
				if (obj.getCode().equals(code)) {
					return obj;
				}
			}
			
			throw new IllegalArgumentException("不支持的类型："+code);
		}
		
		
		public static ClassType getEnumByLabel(String label) {
			
			if (label == null) {
				return null;
			}
			for (ClassType obj : ClassType.values()) {
				if (obj.getLabel().equals(label)) {
					return obj;
				}
			}
			
			throw new IllegalArgumentException("不支持的类型："+label);
		}
	}
	
	
	/**
	 * 教学班级级别类型
	 * @author hushowly@foxmail.com
	 *
	 */
	public enum ClassLevel {
		
		ADMIN_CLASS(0, "行政班"),
		CHOOSE_EXAM(1,"选考"),
		STUDY_EXAM(2,"学考");
		
		private Integer code;
		private String label;
		
		ClassLevel(Integer code, String label) {
			this.code = code;
			this.label = label;
		}

		
		public Integer getCode() {
			return code;
		}
		public void setCode(Integer code) {
			this.code = code;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		
		public static ClassLevel getEnumByCode(Integer code) {
			
			if (code == null) {
				return null;
			}
			for (ClassLevel obj : ClassLevel.values()) {
				if (obj.getCode().equals(code)) {
					return obj;
				}
			}
			
			throw new IllegalArgumentException("不支持的类型："+code);
		}
		
		
		public static ClassLevel getEnumByLabel(String label) {
			
			if (label == null) {
				return null;
			}
			for (ClassLevel obj : ClassLevel.values()) {
				if (obj.getLabel().equals(label)) {
					return obj;
				}
			}
			
			throw new IllegalArgumentException("不支持的类型："+label);
		}

	}
	
	
	/**
	 * 科目类型
	 * @author hushowly@foxmail.com
	 *
	 */
	public enum SubjectType {
		
		POLITICS("4", "政治"),
		HISTORY("5","历史"),
		GEOGRAPHY("6","地理"),
		PHYSICS("7","物理"),
		CHEMISTRY("8","化学"),
		BIOLOGY("9","生物"),
		TECHNICAL("19","技术");
		
		private String code;
		private String label;
		
		SubjectType(String code, String label) {
			this.code = code;
			this.label = label;
		}

		
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		
		public static SubjectType getEnumByCode(Integer code) {
			
			if (code == null) {
				return null;
			}
			for (SubjectType obj : SubjectType.values()) {
				if (obj.getCode().equals(code)) {
					return obj;
				}
			}
			
			throw new IllegalArgumentException("不支持的类型："+code);
		}
		
		
		public static SubjectType getEnumByLabel(String label) {
			
			if (label == null) {
				return null;
			}
			for (SubjectType obj : SubjectType.values()) {
				if (obj.getLabel().equals(label)) {
					return obj;
				}
			}
			
			throw new IllegalArgumentException("不支持的类型："+label);
		}

	}
	
}
