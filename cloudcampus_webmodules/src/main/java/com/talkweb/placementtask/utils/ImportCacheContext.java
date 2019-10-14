package com.talkweb.placementtask.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.talkweb.placementtask.domain.TPlDezyClass;
import com.talkweb.placementtask.domain.TPlDezySubjectcomp;
import com.talkweb.placementtask.domain.TPlDezySubjectcompStudent;
import com.talkweb.placementtask.domain.TPlDezyTclassSubcomp;
import com.talkweb.placementtask.domain.TPlDezyTclassfrom;

/**
 * 一次导入动作缓存上下文
 * @author Administrator
 *
 */
public class ImportCacheContext {
	
	//缓存去重的行政班信息
	private Map<String, TPlDezyClass> adminClassName2InfoMap;
	
	//缓存去重教学班信息
	private Map<String, TPlDezyClass> teachClassName2InfoMap;
	
	//缓存去重场地信息
	private Map<String, String> siteName2IdMap;
	
	//行政班对应志愿组缓存
	private Map<Integer, TPlDezySubjectcomp> adminWishId2InfoMap;
	
	//教学班对应志愿组缓存
	private List<TPlDezyTclassSubcomp> teachWishList;
	
	//行政班和教学班所属二二对应关系
	private Map<Integer, TPlDezyTclassfrom> teachAndAdminMappingMap;
	
	//学生对应班级
	private List<TPlDezySubjectcompStudent> studentClassList;
	
	//科目id与所属科目组id对应关系
	private Map<String, String> subjectId2SubjectGroupId;
	
	//当前科目组合id（仅支持一个班级目组情况）
	private String  classGroupId;
	
	//班级组对应的新班级ids（仅支持一个班级目组情况）
	private List<String>  classIdForclassGroupId;
	
	/**
	 * 一个学校所有学生名和班级名hash后对应的学生id索引
	 */
	private Map<Integer, Long> classNameAndStudentName2StudentId;
	
	
	public ImportCacheContext() {
		adminClassName2InfoMap = new HashMap<>();
		teachClassName2InfoMap = new HashMap<>();
		siteName2IdMap = new HashMap<>();
		adminWishId2InfoMap = new HashMap<>();
		teachWishList = new ArrayList<>();
		teachAndAdminMappingMap = new HashMap<>();
		studentClassList = new ArrayList<>();
		classNameAndStudentName2StudentId = new HashMap<>();
		subjectId2SubjectGroupId = new HashMap<>();
		classIdForclassGroupId = new ArrayList<>();
	}


	public Map<String, TPlDezyClass> getAdminClassName2InfoMap() {
		return adminClassName2InfoMap;
	}


	public Map<String, TPlDezyClass> getTeachClassName2InfoMap() {
		return teachClassName2InfoMap;
	}


	public Map<String, String> getSiteName2IdMap() {
		return siteName2IdMap;
	}


	public Map<Integer, TPlDezySubjectcomp> getAdminWishId2InfoMap() {
		return adminWishId2InfoMap;
	}

	

	public List<TPlDezyTclassSubcomp> getTeachWishList() {
		return teachWishList;
	}


	public Map<Integer, TPlDezyTclassfrom> getTeachAndAdminMappingMap() {
		return teachAndAdminMappingMap;
	}


	public List<TPlDezySubjectcompStudent> getStudentClassList() {
		return studentClassList;
	}
	

	public Map<Integer, Long> getClassNameAndStudentName2StudentId() {
		return classNameAndStudentName2StudentId;
	}


	public Map<String, String> getSubjectId2SubjectGroupId() {
		return subjectId2SubjectGroupId;
	}


	public String getClassGroupId() {
		return classGroupId;
	}


	public void setClassGroupId(String classGroupId) {
		this.classGroupId = classGroupId;
	}


	public List<String> getClassIdForclassGroupId() {
		return classIdForclassGroupId;
	}



	
	
	
}
