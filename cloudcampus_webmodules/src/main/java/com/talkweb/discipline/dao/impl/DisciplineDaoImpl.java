package com.talkweb.discipline.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.discipline.dao.DisciplineDao;
import com.talkweb.discipline.domain.DisciplineExcel;

@Repository
public class DisciplineDaoImpl extends MyBatisBaseDaoImpl  implements DisciplineDao{

	@Override 
	public int addDiscipline(JSONObject param) {
	 
		return insert("addDiscipline", param);
	}

	@Override
	public int updateDiscipline(JSONObject param) {
		 
		return update("updateDiscipline" , param);
	}

	@Override
	public int deleteDiscipline(JSONObject param) {
	 
		return delete("deleteDiscipline" , param);
	}

	@Override
	public int updateDisciplinePublished(JSONObject param) {
		 
		return update("updateDisciplinePublished" , param);
	}

	@Override
	public List<JSONObject> getDisciplineList(JSONObject param) {
		 
		return selectList("getDisciplineList" , param);
	}

	@Override
	public int insertDisciExcel(List<DisciplineExcel> l_disciExcel) {
		 
		return insert("insertDisciExcel" , l_disciExcel);
	}

	@Override
	public int insertDisciDetail(List<JSONObject> l_params) {
		 
		return insert("insertDisciDetail" , l_params);
	}

 

 
	@Override
	public List<JSONObject> getAllDisciplineExcel(JSONObject param) {
	 
		return selectList("getAllDisciplineExcel" , param );
	}

	@Override
	public List<JSONObject> getAllDisciplineDetail(JSONObject param) {
		 
		return selectList("getAllDisciplineDetail" , param);
	}

	@Override
	public int updateDisciplineImported(JSONObject param) {
		 
		return  update("updateDisciplineImported" , param );
	}

 
	@Override
	public int deleteDisciplineDetail(JSONObject param) {
		 
		return delete("deleteDisciplineDetail" ,param );
	}

	@Override
	public int deleteDisciplineExcel(JSONObject param) {
		 
		return delete("deleteDisciplineExcel" , param );
	}

	@Override
	public JSONObject getDiscipline(JSONObject param) {
		 
		return selectOne("getDiscipline", param);
	}

	@Override
	public List<JSONObject> getAPPClassList(JSONObject param) {
		return selectList("getAPPClassList", param);
	}

	@Override
	public List<JSONObject> queryParentInfos(JSONObject param) {
		return selectList("queryParentInfos", param);
	}

	@Override
	public List<JSONObject> queryTeacherInfos(JSONObject param) {
		return selectList("queryTeacherInfos", param);
	}

	@Override
	public List<JSONObject> getClassList(JSONObject param) {
		return selectList("getClassList", param);
	}

	@Override
	public JSONObject getRecordInfo(JSONObject param) {
		return selectOne("getRecordInfo", param);
	}

	@Override
	public int addRecordInfo(JSONObject param) {
		return insert("addRecordInfo", param);
	}

	@Override
	public int updateRecordInfo(JSONObject param) {
		return update("updateRecordInfo" , param);
	}

}
