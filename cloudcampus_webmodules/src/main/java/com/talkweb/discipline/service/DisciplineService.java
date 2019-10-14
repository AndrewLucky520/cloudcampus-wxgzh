package com.talkweb.discipline.service;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.discipline.domain.DisciplineDetail;
import com.talkweb.discipline.domain.DisciplineExcel;

public interface DisciplineService {

	public int addDiscipline(JSONObject param);

	public int updateDiscipline(JSONObject param);

	public int deleteDiscipline(JSONObject param);

	public int updateDisciplinePublished(JSONObject param);

	public List<JSONObject> getDisciplineList(JSONObject param);

	public JSONObject getDisciplines(JSONObject param);

	public JSONObject getPersonalDiscipline(JSONObject param);

	int addImportDiscipline(List<DisciplineDetail> successInfos, List<DisciplineExcel> l_disciExcel);

	public List<JSONObject> getAPPClassList(JSONObject param);

	public void sendKafka(JSONObject obj, Boolean isTeacher, JSONArray msgCenterReceiversArray);

	public JSONObject getDiscipline(JSONObject param);

	public List<JSONObject> queryParentInfos(JSONObject param);

	public List<JSONObject> queryTeacherInfos(JSONObject param);
	
	public List<JSONObject> getClassList(JSONObject param);
	
	public JSONObject getRecordInfo(JSONObject param);
	
	public int addRecordInfo(JSONObject param);
	
	public int updateRecordInfo(JSONObject param);
}
