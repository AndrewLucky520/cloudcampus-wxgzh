package com.talkweb.csbasedata.util;

import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_StageType;

/**
 * 年级工具类
 * @author zhh
 *
 */
public class GradeUtil {
	/**
	 * 判断某个年级某个历史时刻是否毕业
	 * @param createLevel
	 * @param currentLevel
	 * @return 布尔 
	 * @author zhh
	 */
	public static boolean  isGraduate(int createLevel,int currentLevel){
		T_StageType st1=getStageType(currentLevel);
		T_StageType st2=getStageType(createLevel);
		return !(st1==st2);
	}
	/**
	 * 根据年级level获取对应的教学阶段
	 * @param gradeLevel
	 * @return T_StageType 枚举
	 * @author zhh
	 */
	public static T_StageType  getStageType(int gradeLevel){
		if(gradeLevel<T_GradeLevel.T_PrimaryOne.getValue()){
			return T_StageType.Kindergarten;
		}else if(gradeLevel<=T_GradeLevel.T_PrimarySix.getValue() && gradeLevel>=T_GradeLevel.T_PrimaryOne.getValue()){
			return T_StageType.Primary;
		}else if(gradeLevel<=T_GradeLevel.T_JuniorThree.getValue() && gradeLevel>=T_GradeLevel.T_JuniorOne.getValue()){
			return T_StageType.Junior;
		}else if(gradeLevel<=T_GradeLevel.T_HighThree.getValue()&& gradeLevel>=T_GradeLevel.T_HighOne.getValue()){
			return T_StageType.High;
		}
		return T_StageType.Invalid;
	}
}
