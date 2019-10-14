package com.talkweb.placementtask.dao;

import java.util.List;

import com.talkweb.placementtask.domain.TPlConfIndexSubs;
import com.talkweb.placementtask.domain.TPlDezyClass;
import com.talkweb.placementtask.domain.TPlDezyClassgroup;
import com.talkweb.placementtask.domain.TPlDezySubjectcomp;
import com.talkweb.placementtask.domain.TPlDezySubjectcompStudent;
import com.talkweb.placementtask.domain.TPlDezyTclassSubcomp;
import com.talkweb.placementtask.domain.TPlDezyTclassfrom;
import com.talkweb.placementtask.vo.SubjectGroupIdInfo;

/**
 * 导入持久接口
 * @author hushowly@foxmail.com
 *
 */
public interface PlacementImportDao {
	
	//班级组信息表(t_pl_dezy_classgroup)
	int deleteTPlDezyClassgroup(String termInfo, String placementId, String schoolId);
	int batchInsertTPlDezyClassgroupList(String termInfo, List<TPlDezyClassgroup> list);
	
	//查询科目组和班级组信息
	List<SubjectGroupIdInfo> selectDistinctSubjectGroupIdByPlacementId(String termInfo, String placementId, String schoolId);
	List<String> selectDistinctClassGroupIdByPlacementId(String termInfo, String placementId, String schoolId);
	
	//更新科目组下班级ids
	int updateClassGroupClassIds(String termInfo, String placementId, String schoolId, List<String> classIds);
	
	
	//班级信息表(t_pl_dezy_class)
	int deleteTPlDezyClass(String termInfo, String placementId, String schoolId);
	int batchInsertTPlDezyClassList(String termInfo, List<TPlDezyClass> list);
	
	//行政班对应志愿组(t_pl_dezy_subjectcomp)
	int deleteTPlDezySubjectcomp(String termInfo, String placementId, String schoolId);
	int batchInsertTPlDezySubjectcompList(String termInfo, List<TPlDezySubjectcomp> list);
	
	//教学班对应志愿组(t_pl_dezy_tclass_subcomp)
	int deleteTPlDezyTclassSubcomp(String termInfo, String placementId, String schoolId);
	int batchInsertTPlDezyTclassSubcompList(String termInfo, List<TPlDezyTclassSubcomp> list);
	
	//教学班所属的行政班(t_pl_dezy_tclassfrom)
	int deleteTPlDezyTclassfrom(String termInfo, String placementId, String schoolId);
	int batchInsertTPlDezyTclassfromList(String termInfo, List<TPlDezyTclassfrom> list);
	
	//学生对应班级(t_pl_dezy_subjectcomp_student)
	int deleteTPlDezySubjectcompStudent(String termInfo, String placementId, String schoolId);
	int batchInsertTPlDezySubjectcompStudentList(String termInfo, List<TPlDezySubjectcompStudent> list);
	
	//学选交叉情况(t_pl_conf_index_subs)
	int deleteTPlConfIndexSubs(String termInfo, String placementId, String schoolId);
	int batchInsertTPlConfIndexSubsList(String termInfo, List<TPlConfIndexSubs> list);
	
	
}
