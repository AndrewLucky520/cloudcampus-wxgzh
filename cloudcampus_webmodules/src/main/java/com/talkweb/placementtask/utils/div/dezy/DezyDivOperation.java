package com.talkweb.placementtask.utils.div.dezy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.talkweb.placementtask.domain.ClassInfo;
import com.talkweb.placementtask.domain.StudentbaseInfo;
import com.talkweb.placementtask.utils.div.dezy.dto.AdClassResult;
import com.talkweb.placementtask.utils.div.dezy.dto.AdScoreResult;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.SignalParam.ProcessType;
import com.talkweb.placementtask.utils.div.dto.SignalParam.ResponseType;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.dto.Subject;
import com.talkweb.placementtask.utils.div.dto.SubjectGroup;
import com.talkweb.placementtask.utils.div.dto.TeachClassResult;
import com.talkweb.placementtask.utils.div.medium.dto.CheckStudent;

public class DezyDivOperation {
	
	
	
	public static List<ClassInfo> excuteDiv(Integer maxClassSize, Integer globalClassCount,
			List<Student> wishStudList) {
		
		System.out.println(JSONObject.toJSONString(wishStudList));
		
		//科目信息
		Map<String, Subject> subjectId2SubjectMap = new HashMap<String, Subject>();
		Subject subject4 = new Subject("4", "政");
		subjectId2SubjectMap.put("4", subject4);
		
		Subject subject5 = new Subject("5", "史");
		subjectId2SubjectMap.put("5", subject5);
		
		Subject subject6 = new Subject("6", "地");
		subjectId2SubjectMap.put("6", subject6);
		
		Subject subject7 = new Subject("7", "物");
		subjectId2SubjectMap.put("7", subject7);
		
		Subject subject8 = new Subject("8", "化");
		subjectId2SubjectMap.put("8", subject8);
		
		Subject subject9 = new Subject("9", "生");
		subjectId2SubjectMap.put("9", subject9);
		
//		Subject subject2 = new Subject("2", "技");
//		subjectId2SubjectMap.put("2", subject2);
		
		//准备志愿数据
		List<SubjectGroup> wishSubjectGroupList = new ArrayList<SubjectGroup>();
		HashMultimap<String, Student> wishMulList = HashMultimap.create();
		
		for (Student stdWish : wishStudList) {
			String[] ids = stdWish.getSubjectIds().split(",");
			TreeSet<String> subjectIdSet = new TreeSet<>(Arrays.asList(ids));
			String swishId = Joiner.on(",").skipNulls().join(subjectIdSet);
			stdWish.setSubjectIds(swishId);
			wishMulList.put(swishId, stdWish);
		}
		
		for (Entry<String, Collection<Student>> wishEntiry : wishMulList.asMap().entrySet()) {
			String wishId = wishEntiry.getKey();
			List<Subject> subjectlist = new LinkedList<>();
			String sids[] = wishId.split(",");
			for (String sid : sids) {
				Subject s = subjectId2SubjectMap.get(sid);
				if(null == s) throw new RuntimeException("志愿信息不正确:"+wishId);
				subjectlist.add(s);
			}
			wishSubjectGroupList.add(new SubjectGroup(subjectlist, new ArrayList<>(wishEntiry.getValue())));
		}
		
		DivContext divContext = new DivContext(subjectId2SubjectMap, wishSubjectGroupList, globalClassCount, maxClassSize);
		divContext.initClassCountLayout();
		
		//分行政班
		AdScoreResult adScoreResult = (AdScoreResult)new AdClassDiv(divContext, ProcessType.BEST_AVG, ResponseType.TIMEOUT).excuteDiv();
		if(adScoreResult.getClassResultList().size()<=0) {
			adScoreResult = (AdScoreResult)new AdClassDiv(divContext, ProcessType.BAD_AVG, ResponseType.TIMEOUT).excuteDiv();
		}
		
		if(adScoreResult.getClassResultList().size() <= 0) throw new RuntimeException("分行政班失败，没有结果");
		
		//分选考
		List<ClassResult> optClassList = new OptClassDiv(divContext, adScoreResult.getClassResultList(), adScoreResult.getOpThreeDivClassList()).excuteDiv();
		
		//分学考
		List<ClassResult> proClassList = new ProClassDiv(divContext, adScoreResult.getClassResultList()).excuteDiv();
		
		
		checkStudentClass(divContext.getTotalStudent(), divContext, adScoreResult.getClassResultList(), optClassList, proClassList);
		
		//结果
		List<ClassInfo> classInfoList = new ArrayList<ClassInfo>();
		
		//转换行政班
		for (ClassResult classResult : adScoreResult.getClassResultList()) {
			AdClassResult adClass = (AdClassResult)classResult;
			ClassInfo c = new ClassInfo();
			List<String> ids = adClass.getFixTwoSubjectGroup().getIds();
			c.setFixedSubjectIds(ids.toArray(new String[ids.size()]));
			//序列
			c.setClassSeq(0);
			//教学班
			c.setClassInfo(6);
			c.setSubjectId("-999");
			//行政班
			c.setTclassLevel(0);
			c.setTclassNum(adClass.getWishId2StudentList().size());
			c.setTclassId(adClass.getId());
			c.setTclassName("");
			c.setGroundId("");
			c.setGroundName("");
			ArrayList<StudentbaseInfo> sList = new ArrayList<StudentbaseInfo>();
			for (Student student : adClass.getWishId2StudentList().values()) {
				StudentbaseInfo si = new StudentbaseInfo(student.getId(), student.getName());
				si.setWishId(student.getSubjectIds());
				sList.add(si);
			}
			c.setStudentLists(sList);
			
			classInfoList.add(c);
		}
		
		
		//转换选一选二选三教学班
		for (ClassResult classResult : optClassList) {
			
			TeachClassResult adTeachClass = (TeachClassResult)classResult;
			
			ClassInfo c = new ClassInfo();
			//序列
			c.setClassSeq(adTeachClass.getSeqId());
			//教学班
			c.setClassInfo(7);
			c.setSubjectId(String.valueOf(adTeachClass.getSubjectId()));
			//选考
			c.setTclassLevel(1);
			c.setTclassNum(adTeachClass.getWishId2StudentList().size());
			c.setTclassId(adTeachClass.getId());
			c.setTclassName("");
			c.setGroundId("");
			c.setGroundName("");
			ArrayList<StudentbaseInfo> sList = new ArrayList<StudentbaseInfo>();
			for (Student student : adTeachClass.getWishId2StudentList().values()) {
				StudentbaseInfo si = new StudentbaseInfo(student.getId(), student.getName());
				si.setWishId(student.getSubjectIds());
				sList.add(si);
			}
			c.setStudentLists(sList);
			classInfoList.add(c);
		}
		
		
		//转换学一学二学三教学班
		for (ClassResult classResult : proClassList) {
			
			TeachClassResult teachClass = (TeachClassResult)classResult;
			
			ClassInfo c = new ClassInfo();
			//序列
			c.setClassSeq(teachClass.getSeqId());
			//教学班
			c.setClassInfo(7);
			c.setSubjectId(String.valueOf(teachClass.getSubjectId()));
			//学考
			c.setTclassLevel(2);
			c.setTclassNum(teachClass.getWishId2StudentList().size());
			c.setTclassId(teachClass.getId());
			c.setTclassName("");
			c.setGroundId("");
			c.setGroundName("");
			ArrayList<StudentbaseInfo> sList = new ArrayList<StudentbaseInfo>();
			for (Student student : teachClass.getWishId2StudentList().values()) {
				StudentbaseInfo si = new StudentbaseInfo(student.getId(), student.getName());
				si.setWishId(student.getSubjectIds());
				sList.add(si);
			}
			c.setStudentLists(sList);
			classInfoList.add(c);
		}
		
		
		System.out.println("总班数："+ classInfoList.size());
		System.out.println("行政班数:"+adScoreResult.getClassResultList().size()+" 选一选二选三班数:"+optClassList.size()+" 学一学二学三班数:"+proClassList.size());
		
		return classInfoList;
	}
	
	
	/**
	 * 检查分班正确性
	 * @param fixedClassList
	 * @param teachClassList
	 */
	public static int checkStudentClass(int totalStudentCount, DivContext divContext, List<ClassResult> adClassList,
			List<ClassResult> layOptClassList, List<ClassResult> layProClassList) {
		
		Map<String, CheckStudent> adCheckStudentMap = new HashMap<>();
		
		//检查行政班
		for (ClassResult classResult : adClassList) {
			for (Student student : classResult.getWishId2StudentList().values()) {
				CheckStudent cStudnet = adCheckStudentMap.get(student.getId());
				if(null == cStudnet) cStudnet = new CheckStudent(student);
				cStudnet.getAdClassList().add(classResult);
				adCheckStudentMap.put(student.getId(), cStudnet);
			}
		}
		
		if (adCheckStudentMap.size() != totalStudentCount)
			throw new RuntimeException( "行政班总人数" + adCheckStudentMap.size() + "与参与人数" + totalStudentCount + "不一致!");
		
		Map<String, CheckStudent> teachCheckStudentMap = new HashMap<>();
		for (ClassResult classResult : layOptClassList) {
			TeachClassResult teachClass = (TeachClassResult)classResult;
			for (Student student : classResult.getWishId2StudentList().values()) {
	
				CheckStudent cStudnet = teachCheckStudentMap.get(student.getId());
				if(null == cStudnet) cStudnet = new CheckStudent(student);
				cStudnet.getClassId2TeachClassResultMap().put(teachClass.getId(), teachClass);
				cStudnet.getSubjectId2TeachClassResultMap().put(teachClass.getSubjectId(), teachClass);
				teachCheckStudentMap.put(student.getId(), cStudnet);
			}
		}
		
		for (ClassResult classResult : layProClassList) {
			TeachClassResult teachClass = (TeachClassResult)classResult;
			for (Student student : classResult.getWishId2StudentList().values()) {
				
				CheckStudent cStudnet = teachCheckStudentMap.get(student.getId());
				if(null == cStudnet) cStudnet = new CheckStudent(student);
				cStudnet.getClassId2TeachClassResultMap().put(teachClass.getId(), teachClass);
				cStudnet.getSubjectId2TeachClassResultMap().put(teachClass.getSubjectId(), teachClass);
				teachCheckStudentMap.put(student.getId(), cStudnet);
			}
		}
		
		if (teachCheckStudentMap.size() != totalStudentCount)
			throw new RuntimeException("学考和选考总人数" + teachCheckStudentMap.size() + "与参与人数" + totalStudentCount + "不一致!");
		
		
		for (CheckStudent checkStudent : teachCheckStudentMap.values()) {
			
			CheckStudent adCheckStudent = adCheckStudentMap.get(checkStudent.getStudent().getId());
			if(null == adCheckStudent) throw new RuntimeException("学生["+checkStudent.getStudent().getId()+"]未找到行政班");
			
			checkStudent.setAdClassList(adCheckStudent.getAdClassList());
			
			String wishIds = checkStudent.getStudent().getSubjectIds();
			List<Subject> optList = new ArrayList<>();
			List<Subject> proList = new ArrayList<>();
			for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
				if(wishIds.indexOf(subject.getId())!=-1) {
					optList.add(subject);
				}else {
					proList.add(subject);
				}
			}

			Set<Integer> seqSet = new HashSet<>();
			for (Subject subject : optList) {
				List<ClassResult> classResultList = checkStudent.getSubjectId2TeachClassResultMap().get(subject.getId());
				if(classResultList.size()<=0) {
					System.out.println(checkStudent.toString());
					throw new RuntimeException("学生["+checkStudent.getStudent().getName()+"]选考科目["+subject.getId()+"]未找到班级信息:");
				}
				if(classResultList.size()>1) {
					System.out.println(checkStudent.toString());
					throw new RuntimeException("学生["+checkStudent.getStudent().getName()+"]选考科目["+subject.getId()+"]找到多个班级:"+classResultList.size());
				}
				
				for (ClassResult classResult : classResultList) {
					if(seqSet.contains(classResult.getSeqId())) {
						System.out.println(checkStudent.toString());
						throw new RuntimeException("学生["+checkStudent.getStudent().getName()+"]在同一序列["+classResult.getSeqId()+"]班级冲突");
					}
					seqSet.add(classResult.getSeqId());
				}
			}
			
			for (Subject subject : proList) {
				List<ClassResult> classResultList = checkStudent.getSubjectId2TeachClassResultMap().get(subject.getId());
				if(classResultList.size()<=0) {
					System.out.println(checkStudent.toString());
					throw new RuntimeException("学生["+checkStudent.getStudent().getName()+"]学考科目["+subject.getId()+"]未找到班级信息");
				}
				if(classResultList.size()>1) {
					System.out.println(checkStudent.toString());
					throw new RuntimeException("学生["+checkStudent.getStudent().getName()+"]学考科目["+subject.getId()+"]找到多个班级:"+classResultList.size());
				}
				for (ClassResult classResult : classResultList) {
					if(seqSet.contains(classResult.getSeqId())) {
						System.out.println(checkStudent.toString());
						throw new RuntimeException("学生["+checkStudent.getStudent().getName()+"]在同一序列["+classResult.getSeqId()+"]班级冲突");
					}
					seqSet.add(classResult.getSeqId());
				}
			}
			
		}
		
		return totalStudentCount;
		
	}
}
