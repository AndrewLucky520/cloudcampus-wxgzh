package com.talkweb.scoreManage.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.scoreManage.po.ce.ClassExamInfo;
import com.talkweb.scoreManage.po.ce.ClassExamSubjectScore;
import com.talkweb.scoreManage.proc.ClassExamExcelDetail;
import com.talkweb.scoreManage.proc.ClassExamExcelTitle;
import com.talkweb.student.domain.page.StartImportTaskParam;

public interface ClassScoreCrudService {
	List<JSONObject> getExamList(JSONObject params);

	int createExam(JSONObject params);

	int updateExam(JSONObject param);

	void deleteExam(JSONObject params);

	int updateReleaseExam(JSONObject params);

	void addImport(List<ClassExamExcelDetail> successInfos, List<ClassExamExcelTitle> l_salExcel);

	void insertClassExamScoreBatch(List<ClassExamSubjectScore> scoreList, StartImportTaskParam taskParam, int type);
	
	ClassExamInfo getClassExamInfoById(String termInfoId, Map<String, Object> map);
	
	int updateClassExam(String termInfoId, Map<String, Object> map);
}
