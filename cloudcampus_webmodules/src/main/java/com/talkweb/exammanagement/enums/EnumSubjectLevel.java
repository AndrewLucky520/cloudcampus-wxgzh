package com.talkweb.exammanagement.enums;

public enum EnumSubjectLevel {
	OTHER(0, "", ""), // 空
	COLLEGE_ENTRANCE_EXAMINATION(31, "选考", "选"), // 选考（高考）
	ACHIEVEMENT_EXAMINATION(32, "学考", "学"), // 学考
	FIRST_LEVEL(41, "A层", "A"), // 第一层次
	SECOND_LEVEL(42, "B层", "B"), // 第二层次
	THIRD_LEVEL(43, "C层", "C"); // 第三层次

	private final int value;

	private final String name;
	
	private final String simpleName;

	private EnumSubjectLevel(int value, String name, String simpleName) {
		this.value = value;
		this.name = name;
		this.simpleName = simpleName;
	}

	public int getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public static EnumSubjectLevel findByValue(int value) {
		switch (value) {
		case 0:
			return OTHER;
		case 31:
			return COLLEGE_ENTRANCE_EXAMINATION;
		case 32:
			return ACHIEVEMENT_EXAMINATION;
		case 41:
			return FIRST_LEVEL;
		case 42:
			return SECOND_LEVEL;
		case 43:
			return THIRD_LEVEL;
		case 71://31和32是以前版本的数据库，后面改成了71和72(不确定是否还有历史数据会用到31和32,所以不直接修改)
			return COLLEGE_ENTRANCE_EXAMINATION;
		case 72:
			return ACHIEVEMENT_EXAMINATION;
		default:
			return null;
		}
	}
	
	public static String findNameByValue(int value) {
		return findByValue(value).getName();
	}
	
	public static String findSimpleNameByValue(int value) {
		return findByValue(value).getSimpleName();
	}
	
	public static String findNameByValueWithBrackets(int value) {
		EnumSubjectLevel obj = findByValue(value);
		if(obj == null || obj.equals(OTHER)) {
			return obj.getName();
		}
		return "(" + obj.getName() + ")";
	}
	
	public static String findSimpleNameByValueWithBrackets(int value) {
		EnumSubjectLevel obj = findByValue(value);
		if(obj == null || obj.equals(OTHER)) {
			return obj.getSimpleName();
		}
		return "(" + obj.getSimpleName() + ")";
	}
}
