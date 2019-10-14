package com.talkweb.weekWork.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.weekWork.dao.WeekWorkDao;
import com.talkweb.weekWork.pojo.ReportingPersonnel;
import com.talkweb.weekWork.pojo.WeeklyRecord;

@Repository
public class WeekWorkDaoImpl extends MyBatisBaseDaoImpl implements WeekWorkDao {

	@Override
	public boolean getFillManInfo(Map<String, Object> map) {
		Integer res = selectOne("getFillManInfo", map);
		if(res != null && res == 1)	{
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<ReportingPersonnel> queryReportingPersonnel(Map<String, Object> map) {
		List<ReportingPersonnel> list = selectList("queryReportingPersonnel", map);
		if(list == null) {
			list = new ArrayList<ReportingPersonnel>();
		}
		return list;
	}
	
	@Override
	public List<WeeklyRecord> queryWeeklyRecordList(Map<String, Object> map) {
		List<WeeklyRecord> list = selectList("queryWeeklyRecordList", map);
		if(list == null) {
			list = new ArrayList<WeeklyRecord>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getDepartmentInfoByDataBase(JSONObject param) {
		List<JSONObject> list = selectList("getDepartmentInfoByDataBase",param);
		if(list == null) {
			list = new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public WeeklyRecord getWeeklyRecordListByDepartment(Map<String, Object> map) {
		return selectOne("getWeeklyRecordListByDepartment", map);
	}
	
	@Override
	public int insertWeeklyRecord(WeeklyRecord oneRecord) {
		return insert("insertWeeklyRecord", oneRecord);
	}
	
	
	
	
	
	
	
	
	
	@Override
	public String getCurrentTermWeek(JSONObject param) {
		// TODO Auto-generated method stub
		return selectOne("getCurrentTermWeek",param);
	}

	@Override
	public List<JSONObject> getWeeklyRecordDetail(JSONObject param) {
		// TODO Auto-generated method stub
		return selectList("getWeeklyRecordDetail",param);
	}

	@Override
	public List<JSONObject> getDistinctRecordDetail(JSONObject param) {
		// TODO Auto-generated method stub
		return selectList("getDistinctRecordDetail", param);
	}

	@Override
	public int deleteWeeklyContent(JSONObject del) {
		// TODO Auto-generated method stub
		return delete("deleteWeeklyContent", del);
	}

	@Override
	public int updateWeeklyContent(List<JSONObject> lj) {
		// TODO Auto-generated method stub
		return update("updateWeeklyContent", lj);
	}

	@Override
	public int insertTerminfoAndStartWeek(JSONObject param) {
		// TODO Auto-generated method stub
		return insert("insertTerminfoAndStartWeek", param);
	}

	@Override
	public List<JSONObject> getReportingPersonList(JSONObject param) {
		List<JSONObject> list = selectList("getReportingPersonList", param);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public void insertWeekWorkSortBatch(List<JSONObject> list) {
		update("insertWeekWorkSortBatchWW",list);
	}

	@Override
	public void insertWeekWorkTableHeadBatch(List<JSONObject> list) {
		update("insertWeekWorkTableHeadBatchWW",list);
	}

	@Override
	public void deleteWeekWorkSortById(JSONObject param) {
		update("deleteWeekWorkSortByIdWW",param);
	}

	@Override
	public void deleteWeekWorkTableHeadById(JSONObject param) {
		update("deleteWeekWorkTableHeadByIdWW",param);
	}

	@Override
	public String getWeekWorkSortNames(JSONObject param) {
		return selectOne("getWeekWorkSortNamesWW",param);
	}

	@Override
	public List<JSONObject> getWeekWorkTableHeadNames(JSONObject param) {
		return selectList("getWeekWorkTableHeadNamesWW",param);
	}

	@Override
	public boolean isExsitedSameDepartmentName(JSONObject param) {
		Integer result = selectOne("isExsitedSameDepartmentName",param);
		if(result != null && result == 1) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void insertDepartment(JSONObject param) {
		insert("insertDepartmentWW",param);
	}

	@Override
	public void insertTeacherBatch(Map<String, Object> params) {
		insert("insertTeacherBatchWW", params);
	}

	@Override
	public void deleteDepartment(JSONObject param) {
		delete("deleteDepartmentWW",param);
	}

	@Override
	public void deleteTeacher(JSONObject param) {
		delete("deleteTeacherWW",param);
	}

	@Override
	public Date getTerminfoAndStartWeek(JSONObject param) {
		// TODO Auto-generated method stub
		return selectOne("getTerminfoAndStartWeek", param);
	}

	@Override
	public int getFillRecord(JSONObject param) {
		// TODO Auto-generated method stub
		return selectOne("getFillRecord", param);
	}
 
	@Override
	public int getFillRecord2(JSONObject param) {
		 return selectOne("getFillRecord2", param);
	}

	@Override
	public List<JSONObject> getBaseRecordDetail(JSONObject param) {
		// TODO Auto-generated method stub
		return selectList("getBaseRecordDetail", param);
	}

	@Override
	public int deleteWeeklyRecord(JSONObject oneRecord) {
		// TODO Auto-generated method stub
		return delete("deleteWeeklyRecord", oneRecord);
	}

	@Override
	public int deleteTerminfoAndStartWeek(JSONObject param) {
		// TODO Auto-generated method stub
		return delete("deleteTerminfoAndStartWeek",param);
	}

	@Override
	public int deleteWeekWorkSortBySchoolId(JSONObject delParam) {
		// TODO Auto-generated method stub
		return delete("deleteWeekWorkSortBySchoolId", delParam);
	}

	@Override
	public int deleteWeekWorkTableHeadBySchoolId(JSONObject delParam) {
		// TODO Auto-generated method stub
		return delete("deleteWeekWorkTableHeadBySchoolId", delParam);
	}

	@Override
	public List<Long> getTeachersBydepartmentId(JSONObject param) {
		// TODO Auto-generated method stub
		return selectList("getTeachersBydepartmentId", param);
	}

	@Override
	public List<JSONObject> getDepartmentList(JSONObject param) {
		// TODO Auto-generated method stub
		return selectList("getWeekWorkDepartmentList", param);
	}

	@Override
	public List<JSONObject> getMaxWeekFromRecord(JSONObject param) {
		// TODO Auto-generated method stub
		return selectList("getMaxWeekFromRecord", param);
	}
 
	@Override
	public List<JSONObject> getMaxWeekFromRecord2(JSONObject param) {
		// TODO Auto-generated method stub
		return selectList("getMaxWeekFromRecord2", param);
	}

	@Override
	public int delWeeklyRecordDetail1(JSONObject param) {
		// TODO Auto-generated method stub
		return delete("delWeeklyRecordDetail1", param);
	}

	@Override
	public int delWeeklyRecordDetail2(JSONObject param) {
		// TODO Auto-generated method stub
		return delete("delWeeklyRecordDetail2", param);
	}

	@Override
	public List<JSONObject> getDistinctRecordDetail1(JSONObject param) {
		return selectList("getDistinctRecordDetail1",param);
	}

 
	@Override
	public List<JSONObject> getDistinctRecordDetail2(JSONObject param) {
		 
		return selectList("getDistinctRecordDetail2",param);
	}

	@Override
	public JSONObject getDepartmentById(JSONObject param) {
		return selectOne("getDepartmentByIdWW",param);
		 
	}

	@Override
	public List<JSONObject> getFilledList(JSONObject param) {
		return selectList("getFilledList",param);
	}

}
