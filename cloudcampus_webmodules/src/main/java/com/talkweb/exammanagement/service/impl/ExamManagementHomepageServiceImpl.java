package com.talkweb.exammanagement.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.exammanagement.dao.ExamManagementDao;
import com.talkweb.exammanagement.domain.ExamManagement;
import com.talkweb.exammanagement.service.ExamManagementHomepageService;

@Service
public class ExamManagementHomepageServiceImpl implements ExamManagementHomepageService {
	Logger logger = LoggerFactory.getLogger(ExamManagementHomepageServiceImpl.class);

	@Autowired
	private ExamManagementDao examManagementDao;

	public List<ExamManagement> getExamManagementList(JSONObject request) {
		return examManagementDao.getExamManagementList(request, request.getString("termInfo"));
	}

	public void insertOrupdateExamManagement(JSONObject request) {
		String name = request.getString("name");
		if (StringUtils.isBlank(name)) {
			throw new CommonRunException(-1, "新建或修改考试名称不能为空！");
		}
		
		ExamManagement em = new ExamManagement();
		em.setExamManagementId(request.getString("examManagementId"));
		em.setSchoolId(request.getLong("schoolId"));
		em.setName(name);
		em.setCreateDateTime(new Date());
		em.setAccountId(request.getLong("accountId"));
		em.setStatus(0);
		String termInfo = request.getString("termInfo");
		em.setTermInfo(termInfo);
		
		if(examManagementDao.ifExsitsSameNameInExamManagement(em, termInfo)) {
			throw new CommonRunException(-1, "考试名称重复，请重新更换考试名称！");
		}
		
		if (em.getExamManagementId() == null) {
			em.setExamManagementId(UUIDUtil.getUUID());
			examManagementDao.insertExamManagement(em, termInfo);
		} else {
			examManagementDao.updateExamManagement(em, termInfo);
		}
	}
	
	public void deleteExamManagement(JSONObject request) {
		String termInfo = request.getString("termInfo");
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if(em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考评信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		
		// 删除考试计划
		examManagementDao.deleteExamPlan(request, termInfo, autoIncr);
		// 删除考试科目
		examManagementDao.deleteExamSubject(request, termInfo, autoIncr);
		// 删除考场信息
		examManagementDao.deleteExamPlaceInfo(request, termInfo, autoIncr);
		// 删除不参考的学生
		examManagementDao.deleteStudsNotTakingExam(request, termInfo, autoIncr);
		// 删除等待排考的学生
		examManagementDao.deleteStudsWaiting(request, termInfo, autoIncr);
		// 删除安排考场信息
		examManagementDao.deleteArrangeExamPlaceInfo(request, termInfo, autoIncr);
		// 删除安排考试规则信息
		examManagementDao.deleteArrangeExamRule(request, termInfo, autoIncr);
		// 删除安排考试班级信息
		examManagementDao.deleteArrangeExamClassInfo(request, termInfo, autoIncr);
		// 删除安排考试科目信息
		examManagementDao.deleteArrangeExamSubjectInfo(request, termInfo, autoIncr);
		// 删除考试安排结果信息
		examManagementDao.deleteArrExamResult(request, termInfo, autoIncr);
		// 删除考号表信息
		examManagementDao.deleteTestNumberInfo(request, termInfo, autoIncr);
		// 删除主表信息
		examManagementDao.deleteExamManagement(request, termInfo);
	}
}
