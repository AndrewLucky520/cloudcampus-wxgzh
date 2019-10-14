package com.talkweb.placementtask.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;

public class DezyDisTclassRunParams implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -45087534879670077L;
	
	/**
	 * 行政班组列表
	 */
	private List<TPlDezyClassgroup> classGroupList = new ArrayList<TPlDezyClassgroup>(); 
	/**
	 * 所有班级列表
	 */
	private List<TPlDezyClass> dezyTclassList = new ArrayList<TPlDezyClass>(); 
	/**
	 * 同组同层级同科目同学选等级的教学班列表
	 */
	private HashMap<String,List<TPlDezyClass>> optionTclassMap = new HashMap<String, List<TPlDezyClass>>();
	/**
	 * 教学班所含的志愿组合
	 */
	private Map<String,List<TPlDezyTclassSubcomp>> tclassSubMap = new HashMap<String, List<TPlDezyTclassSubcomp>>();

	private List<TPlDezyTclassfrom> tclassFromList = new ArrayList<TPlDezyTclassfrom>();

	/**
	 * 行政班所含的志愿组合
	 */
	private HashMap<String,List<TPlDezySubjectcomp>> classSubcompMap = new HashMap<String, List<TPlDezySubjectcomp>>();

	/**
	 * 科目所在科目组id
	 */
	private HashMap<String,TPlDezySubjectgroup> subject2GroupMap = new HashMap<String, TPlDezySubjectgroup>();

	/**
	 * 行政班id-行政班映射
	 */
	private HashMap<String,TPlDezyClass> xzclassMap = new HashMap<String, TPlDezyClass>();
	
	/**
	 * 场地空闲map	subgroup_tclassId_classseq--using
	 */
	ConcurrentHashMap<String,String> xzgroudMap = new ConcurrentHashMap<String, String>();
	/**
	 * 辅助教室占用map
	 */
	ConcurrentHashMap<String,List<Integer>> fzgroudMap = new ConcurrentHashMap<String, List<Integer>>();
	/**
	 * 辅助教室列表 json：groundId,groundName
	 */
	Vector<JSONObject> fzgroudList = new Vector<JSONObject>();
	
	public HashMap<String, List<TPlDezySubjectcomp>> getClassSubcompMap() {
		return classSubcompMap;
	}

	public List<TPlDezyClass> getDezyTclassList() {
		return dezyTclassList;
	}

	public void setDezyTclassList(List<TPlDezyClass> dezyTclassList) {
		this.dezyTclassList = dezyTclassList;
	}

	public HashMap<String, List<TPlDezyClass>> getOptionTclassMap() {
		return optionTclassMap;
	}

	public void setOptionTclassMap(
			HashMap<String, List<TPlDezyClass>> optionTclassMap) {
		this.optionTclassMap = optionTclassMap;
	}

	public void setClassSubcompMap(
			HashMap<String, List<TPlDezySubjectcomp>> classSubcompMap) {
		this.classSubcompMap = classSubcompMap;
	}

	public HashMap<String, TPlDezySubjectgroup> getSubject2GroupMap() {
		return subject2GroupMap;
	}
	public List<TPlDezyClassgroup> getClassGroupList() {
		return classGroupList;
	}

	public void setClassGroupList(List<TPlDezyClassgroup> classGroupList) {
		this.classGroupList = classGroupList;
	}

	public Map<String, List<TPlDezyTclassSubcomp>> getTclassSubMap() {
		return tclassSubMap;
	}

	public void setTclassSubMap(Map<String, List<TPlDezyTclassSubcomp>> tclassSubMap) {
		this.tclassSubMap = tclassSubMap;
	}

	public List<TPlDezyTclassfrom> getTclassFromList() {
		return tclassFromList;
	}

	public void setTclassFromList(List<TPlDezyTclassfrom> tclassFromList) {
		this.tclassFromList = tclassFromList;
	}
	public void setSubject2GroupMap( List<TPlDezySubjectgroup> subjectGroupList) {
		for(TPlDezySubjectgroup sub:subjectGroupList){
			String[] subIds = sub.getSubjectIds().split(",");
			for(int i=0;i<subIds.length;i++){
				subject2GroupMap.put(subIds[i], sub);
			}
		}
	}
	
	public String getSubjectGroupIdBySubId(String subId){
		String rs = "-999";
		if(subject2GroupMap.containsKey(subId)){
			rs = subject2GroupMap.get(subId).getSubjectGroupId();
		}
		
		return rs;
	}

	public HashMap<String, TPlDezyClass> getXzclassMap() {
		return xzclassMap;
	}

	public void setXzclassMap(List<TPlDezyClass> xzclassList) {
		for(TPlDezyClass xzclass:xzclassList ){
			xzclassMap.put(xzclass.getTclassId(), xzclass);
		}
		
		setDezyTclassList(xzclassList);
	}
	
	public TPlDezyClass getXzclassById(String xzclassId){
		return xzclassMap.get(xzclassId);
	}

	public void setClassSubcompMap(
			List<TPlDezyTclassSubcomp> tclassSubcompList,
			DezyDisTclassParams disTclassParams) {
		// TODO Auto-generated method stub
		//行政班组合构成map
		for(TPlDezyTclassSubcomp tclassSubcomp:tclassSubcompList){
			String classId = tclassSubcomp.getTclassId();
			String subjectCompId = tclassSubcomp.getSubjectCompId();
			TPlDezySubjectcomp subcomp = disTclassParams.getSubjectcompById(subjectCompId);
			List<TPlDezySubjectcomp> subplist = null;
			if(classSubcompMap.containsKey(classId)){
				subplist = classSubcompMap.get(classId);
			}else{
				subplist = new ArrayList<TPlDezySubjectcomp>();
			}
			subplist.add(subcomp);
			classSubcompMap.put(classId, subplist);
		}
	}
}
