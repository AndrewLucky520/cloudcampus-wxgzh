package com.talkweb.placementtask.utils.div.medium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.placementtask.domain.ClassInfo;
import com.talkweb.placementtask.domain.StudentbaseInfo;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.ClassResult.ClassType;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.SignalParam.ResponseType;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.dto.Subject;
import com.talkweb.placementtask.utils.div.dto.SubjectGroup;
import com.talkweb.placementtask.utils.div.dto.TeachClassResult;
import com.talkweb.placementtask.utils.div.medium.dto.CheckStudent;
import com.talkweb.placementtask.utils.div.medium.dto.FixedClassResult;
import com.talkweb.placementtask.utils.div.medium.dto.LayInfo;
import com.talkweb.placementtask.utils.div.medium.dto.MediumClassData;
import com.talkweb.placementtask.utils.div.medium.dto.MediumFixedClassInfo;
import com.talkweb.placementtask.utils.div.medium.dto.MediumWishSubjectGroup;

public class MediumDivOperation {
	
	/**
	 * 
	 * @param maxClassSize      最大班额
	 * @param wishSubjectGroupList 志愿信息
	 * @param seqTimeoutSecond 序列分班超时是间（秒）
	 * @return
	 */
	public static MediumClassData excuteDiv(Integer maxClassSize,
			List<MediumWishSubjectGroup> mWishSubjectGroupList, Long seqTimeoutSecond) {
		
		System.out.println("excuteDiv:"+JSONObject.toJSONString(mWishSubjectGroupList));
		
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
		
		 Map<Integer, LayInfo> layId2LayMap = new HashMap<>();
		
		//关联科目
		List<SubjectGroup> wishSubjectGroupList = new ArrayList<>();
		for (MediumWishSubjectGroup wish : mWishSubjectGroupList) {
			
			Assert.notNull(wish.getLayId(), wish.getName()+"没有关联层");
			
			Assert.notEmpty(wish.getStudents(), wish.getName()+"没有学生信息");
			
			LayInfo layInfo = layId2LayMap.get(wish.getLayId());
			if(null == layInfo) {
				layInfo = new LayInfo(wish.getLayId(), wish.getLayName());
				layId2LayMap.put(layInfo.getId(), layInfo);
			}
			layInfo.getWishGroupIds().add(wish.getId());
			
    		List<Subject> subjectList = new ArrayList<>();
    		for (String id : wish.getIds()) {
    			Subject subject = subjectId2SubjectMap.get(id);
    			if(null == subject) throw new RuntimeException("不存在科目"+id);
    			subjectList.add(subject);
			}
    		wish.setSubjects(subjectList);
    		    		
    		wishSubjectGroupList.add(wish);
		}
		
		DivContext divContext = new DivContext(subjectId2SubjectMap, wishSubjectGroupList, 1, maxClassSize);
		divContext.initClassCountLayout();
		
		
		int totalStudentCount = 0;
		List<ClassResult> fixedClassList = new FixedClassDiv(divContext).excuteDiv();
		totalStudentCount+=checkAdTotalStudent(divContext, fixedClassList);
		
		List<ClassResult> adClassList= new ArrayList<>();
		
		List<ClassResult> optClassList= new ArrayList<>();
		
		List<ClassResult> proClassList= new ArrayList<>();
		
		//按层分班
		for (LayInfo layInfo : layId2LayMap.values()) {
			
			List<ClassResult> layAdClassList = new AdClassDiv(divContext, layInfo).excuteDiv();
			adClassList.addAll(layAdClassList);
			
			List<ClassResult> layOptClassList = new TeachClassDiv(divContext, layInfo, ClassType.OPT, ResponseType.TIMEOUT, seqTimeoutSecond).excuteDiv();
			optClassList.addAll(layOptClassList);
			
			List<ClassResult> layProClassList = new TeachClassDiv(divContext, layInfo, ClassType.Pro, ResponseType.TIMEOUT, seqTimeoutSecond).excuteDiv();		
			proClassList.addAll(layProClassList);
			
			totalStudentCount += checkStudentClass(divContext, layInfo, layAdClassList, layOptClassList, layProClassList);
			
			System.out.println("==============="+layInfo.getName()+"选考分班结果:");
			for (ClassResult classResult : layOptClassList) {
				classResult.print();
			}
			System.out.println("==============="+layInfo.getName()+"学考分班结果:");
			for (ClassResult classResult : layProClassList) {
				classResult.print();
			}
		}
		
		
		if(totalStudentCount!=divContext.getTotalStudent()) {
			throw new RuntimeException(
					"分班后学生总人数" + totalStudentCount + "与分班总参与人数" + divContext.getTotalStudent() + "不一致!");
		}
		
		
		List<MediumFixedClassInfo> refixedClassList = new ArrayList<>();
		
		List<ClassInfo> reAdClassList= new ArrayList<>();
		
		List<ClassInfo> reTeachClassList= new ArrayList<>();
		
		//分固定班级
		for (ClassResult cr : fixedClassList) {
			
			FixedClassResult fixedClassResult = (FixedClassResult)cr;
			
			MediumFixedClassInfo c = new MediumFixedClassInfo();
			c.setWishId(fixedClassResult.getWishId());
			c.setWishName(divContext.getWishId2SubjectGroupMap().get(fixedClassResult.getWishId()).getName());
			//序列
			c.setClassSeq(0);
			//教学班
			//c.setClassInfo(6);
			//c.setSubjectId("-999");
			//行政班
			//c.setTclassLevel(0);
			//c.setTclassNum(adClass.getWishId2StudentList().size());
			c.setTclassId(fixedClassResult.getId());
			c.setTclassName("");
			c.setGroundId("");
			c.setGroundName("");
			ArrayList<StudentbaseInfo> sList = new ArrayList<StudentbaseInfo>();
			for (Student student : fixedClassResult.getWishId2StudentList().values()) {
				StudentbaseInfo si = new StudentbaseInfo(student.getId(), student.getName());
				si.setWishId(student.getSubjectIds());
				sList.add(si);
			}
			c.setStudentLists(sList);
			
			refixedClassList.add(c);
		}
		
		//行政班
		for (ClassResult classResult : adClassList) {
			ClassInfo c = new ClassInfo();
			//序列
			c.setClassSeq(0);
			//教学班
			c.setClassInfo(6);
			c.setSubjectId("-999");
			//行政班
			c.setTclassLevel(0);
			//c.setTclassNum(adClass.getWishId2StudentList().size());
			c.setTclassId(classResult.getId());
			c.setTclassName("");
			c.setGroundId("");
			c.setGroundName("");
			ArrayList<StudentbaseInfo> sList = new ArrayList<StudentbaseInfo>();
			for (Student student : classResult.getWishId2StudentList().values()) {
				StudentbaseInfo si = new StudentbaseInfo(student.getId(), student.getName());
				si.setWishId(student.getSubjectIds());
				sList.add(si);
			}
			c.setStudentLists(sList);
			
			reAdClassList.add(c);
		}
		
		//选考
		for (ClassResult cr : optClassList) {
			TeachClassResult classResult = (TeachClassResult)cr;
			ClassInfo c = new ClassInfo();
			//序列
			c.setClassSeq(classResult.getSeqId());
			//教学班
			c.setClassInfo(7);
			c.setSubjectId(String.valueOf(classResult.getSubjectId()));
			//学考
			c.setTclassLevel(1);
			c.setTclassNum(classResult.getWishId2StudentList().size());
			c.setTclassId(classResult.getId());
			c.setTclassName("");
			c.setGroundId("");
			c.setGroundName("");
			ArrayList<StudentbaseInfo> sList = new ArrayList<StudentbaseInfo>();
			for (Student student : classResult.getWishId2StudentList().values()) {
				StudentbaseInfo si = new StudentbaseInfo(student.getId(), student.getName());
				si.setWishId(student.getSubjectIds());
				sList.add(si);
			}
			c.setStudentLists(sList);
			reTeachClassList.add(c);
		}
		
		//学考
		for (ClassResult cr : proClassList) {
			TeachClassResult classResult = (TeachClassResult)cr;
			ClassInfo c = new ClassInfo();
			//序列
			c.setClassSeq(classResult.getSeqId());
			//教学班
			c.setClassInfo(7);
			c.setSubjectId(String.valueOf(classResult.getSubjectId()));
			//学考
			c.setTclassLevel(2);
			c.setTclassNum(classResult.getWishId2StudentList().size());
			c.setTclassId(classResult.getId());
			c.setTclassName("");
			c.setGroundId("");
			c.setGroundName("");
			ArrayList<StudentbaseInfo> sList = new ArrayList<StudentbaseInfo>();
			for (Student student : classResult.getWishId2StudentList().values()) {
				StudentbaseInfo si = new StudentbaseInfo(student.getId(), student.getName());
				si.setWishId(student.getSubjectIds());
				sList.add(si);
			}
			c.setStudentLists(sList);
			reTeachClassList.add(c);
		}
		
		MediumClassData mcd = new MediumClassData();
		mcd.setFixedClassList(refixedClassList);
		mcd.setAdClassList(reAdClassList);
		mcd.setTeachClassList(reTeachClassList);
		return mcd;
	}
	
	
	public static int checkAdTotalStudent(DivContext divContext, List<ClassResult> fixedClassList) {
		
		int totalStudentCount = 0;
		for (SubjectGroup sg : divContext.getWishSubjectGroupList()) {
			MediumWishSubjectGroup wish = (MediumWishSubjectGroup)sg;
			totalStudentCount+=wish.getFixedStudentCount();
		}
		
		
		Map<String, CheckStudent> fixedCheckStudentMap = new HashMap<>();
		for (ClassResult classResult : fixedClassList) {
			for (Student student : classResult.getWishId2StudentList().values()) {
				CheckStudent cStudnet = fixedCheckStudentMap.get(student.getId());
				if(null == cStudnet) cStudnet = new CheckStudent(student);
				cStudnet.getAdClassList().add(classResult);
				fixedCheckStudentMap.put(student.getId(), cStudnet);
			}
		}
		
		if (fixedCheckStudentMap.size() != totalStudentCount)
			throw new RuntimeException(
					"固定总人数" + fixedCheckStudentMap.size() + "固定参与人数" + totalStudentCount + "不一致!");
		
		return fixedCheckStudentMap.size();
		
	}

	
	/**
	 * 检查分班正确性
	 * @param fixedClassList
	 * @param teachClassList
	 */
	public static int checkStudentClass(DivContext divContext, LayInfo layInfo, List<ClassResult> adClassList,
			List<ClassResult> layOptClassList, List<ClassResult> layProClassList) {
		
		int totalStudentCount = 0;
		for (String wishId : layInfo.getWishGroupIds()) {
			MediumWishSubjectGroup wish = (MediumWishSubjectGroup)divContext.getWishId2SubjectGroupMap().get(wishId);
			totalStudentCount+=wish.getGoStudentCount();
		}
		
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
			throw new RuntimeException(
					layInfo.getName() + "行政班总人数" + adCheckStudentMap.size() + "与参与人数" + totalStudentCount + "不一致!");
		
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
			throw new RuntimeException(
					layInfo.getName() + "学考和选考总人数" + teachCheckStudentMap.size() + "与参与人数" + totalStudentCount + "不一致!");
		
		
		for (CheckStudent checkStudent : teachCheckStudentMap.values()) {
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
					throw new RuntimeException("学生["+checkStudent.getStudent().getName()+"]选考科目["+subject.getId()+"]未找到班级信息");
				}
				if(classResultList.size()>1) {
					throw new RuntimeException("学生["+checkStudent.getStudent().getName()+"]选考科目["+subject.getId()+"]找到多个班级:"+classResultList.size());
				}
				
				for (ClassResult classResult : classResultList) {
					if(seqSet.contains(classResult.getSeqId())) {
						throw new RuntimeException("学生["+checkStudent.getStudent().getName()+"]在同一序列["+classResult.getSeqId()+"]班级冲突");
					}
					seqSet.add(classResult.getSeqId());
				}
			}
			
			for (Subject subject : proList) {
				List<ClassResult> classResultList = checkStudent.getSubjectId2TeachClassResultMap().get(subject.getId());
				if(classResultList.size()<=0) {
					throw new RuntimeException("学生["+checkStudent.getStudent().getName()+"]学考科目["+subject.getId()+"]未找到班级信息");
				}
				if(classResultList.size()>1) {
					throw new RuntimeException("学生["+checkStudent.getStudent().getName()+"]学考科目["+subject.getId()+"]找到多个班级:"+classResultList.size());
				}
				for (ClassResult classResult : classResultList) {
					if(seqSet.contains(classResult.getSeqId())) {
						throw new RuntimeException("学生["+checkStudent.getStudent().getName()+"]在同一序列["+classResult.getSeqId()+"]班级冲突");
					}
					seqSet.add(classResult.getSeqId());
				}
			}
			
		}
		
		return totalStudentCount;
		
	}

}
