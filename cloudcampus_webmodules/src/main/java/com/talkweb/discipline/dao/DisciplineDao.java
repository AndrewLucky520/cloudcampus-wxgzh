package com.talkweb.discipline.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.discipline.domain.DisciplineExcel;

public interface DisciplineDao {

	public int addDiscipline(JSONObject param); 
	public JSONObject getDiscipline(JSONObject param); 
	public int updateDiscipline(JSONObject param); 
	public int deleteDiscipline(JSONObject param); 
	public int updateDisciplinePublished(JSONObject param); 
	
	public List<JSONObject> getDisciplineList(JSONObject param); 
	
 
	public int insertDisciExcel(List<DisciplineExcel> l_estiExcel);
	public int insertDisciDetail(List<JSONObject> l_params);
 

	public List<JSONObject> getAllDisciplineExcel(JSONObject param);
	public List<JSONObject> getAllDisciplineDetail(JSONObject param);
	
	public int updateDisciplineImported(JSONObject param);
	public int deleteDisciplineDetail(JSONObject param);
	public int deleteDisciplineExcel(JSONObject param);
	
	public List<JSONObject> getAPPClassList(JSONObject param);
	
	public List<JSONObject> queryParentInfos(JSONObject param);
	
	public List<JSONObject> queryTeacherInfos(JSONObject param);
	
	public List<JSONObject> getClassList(JSONObject param);
	
	public JSONObject getRecordInfo(JSONObject param);
	
	public int addRecordInfo(JSONObject param);
	
	public int updateRecordInfo(JSONObject param);
}
