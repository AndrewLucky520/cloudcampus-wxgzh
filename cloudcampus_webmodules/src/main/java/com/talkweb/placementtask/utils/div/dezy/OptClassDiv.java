package com.talkweb.placementtask.utils.div.dezy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.TreeBasedTable;
import com.talkweb.placementtask.utils.div.Divide;
import com.talkweb.placementtask.utils.div.Utils;
import com.talkweb.placementtask.utils.div.dezy.dto.AdClassResult;
import com.talkweb.placementtask.utils.div.dto.Cell;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.SeqCell;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.dto.Subject;
import com.talkweb.placementtask.utils.div.dto.SubjectGroup;
import com.talkweb.placementtask.utils.div.dto.TeachClassResult;


/**
 * 分选考教学班
 * @author hushow
 *
 */
public class OptClassDiv extends Divide<List<ClassResult>>{
	
	/**
	 * 行政班
	 */
	List<ClassResult> adClassList;
	
	/**
	 * 选三班
	 */
	List<ClassResult> optThreeClassList;
	
	
	/**
	 * 开班分布
	 */
	TreeBasedTable<Integer, String, SeqCell> adClassCountTable;
	
	
	public OptClassDiv(DivContext divContext, List<ClassResult> adClassList, List<ClassResult> optThreeClassList) {
		
		super(divContext);
		
		this.adClassList = adClassList;
		
		//索引行政班+志愿下的学生数据
		for (ClassResult classResult : adClassList) {
			for (Entry<String, Collection<Student>> entry : classResult.getWishId2StudentList().asMap().entrySet()) {
				String wishId = entry.getKey();
				String wishKey = classResult.getId()+Utils.SEPARATOR_SYMBOL+wishId;
				this.wishId2StudentMultimap.putAll(wishKey, new ArrayList<>(entry.getValue()));
			}
		}
		
		this.optThreeClassList = optThreeClassList;
		
		//计算新的科目班级分布图
		 adClassCountTable = Utils.calculateAdClassCountTable(divContext);
	}
	
	
	@Override
	public List<ClassResult> excuteDiv() {
		
		System.out.println("开始分选考班:");
		List<ClassResult> reList = new ArrayList<>();
		TreeBasedTable<String, String, List<Student>> adWish2StudentTable = TreeBasedTable.create();
		for (ClassResult classResult : adClassList) {	
			AdClassResult adClassResult = (AdClassResult)classResult;
			for (SubjectGroup wishSubjectGroup : adClassResult.getWishSubjectGroups()) {
				adWish2StudentTable.put(adClassResult.getId(), wishSubjectGroup.getId(), new ArrayList<Student>(adClassResult.getWishId2StudentList().get(wishSubjectGroup.getId())));
			}
		}
		
		/**
		 * 行政班和选三教学班填入最新科目序列分布图
		 */
		//行政班定二科目填科目序列(完全以行政开教学班)
		List<ClassResult> adTeachList = divTeachClassByAd(divContext, adClassList, adClassCountTable);
		
		ArrayListMultimap<Integer, ClassResult> seqIndex2ClassMultimap = ArrayListMultimap.create();
		for (ClassResult classResult : adTeachList) {
			seqIndex2ClassMultimap.put(classResult.getSeqId(), classResult);
		}
		
		TreeSet<Integer> seqIndexSet = new TreeSet<>(seqIndex2ClassMultimap.keys());
		
		//索引志愿在序列具体用人情况
		TreeBasedTable<String, String, List<Student>> parentWishIdSubjectId2Students = TreeBasedTable.create();
		
		for (Integer seqIndex : seqIndexSet) {
			List<ClassResult> teachList = seqIndex2ClassMultimap.get(seqIndex);
			this.extractStudent(teachList, parentWishIdSubjectId2Students);
			reList.addAll(teachList);
			
//			System.out.println("选考具体人信息：");
//			for (ClassResult classResult : teachList) {
//				classResult.print();
//			}
		}
		
		
		//选三填科目序列
		for (ClassResult tClass : optThreeClassList) {
			TeachClassResult teachClass = (TeachClassResult)tClass;
			for (SeqCell cell : adClassCountTable.row(3).values()) {
				if(cell.getSubject().getId() != teachClass.getSubjectId()) continue;
				teachClass.setSeqId(3);
				cell.getClassResultList().add(teachClass);
			}
		}
		
		
		this.extractStudent(optThreeClassList, parentWishIdSubjectId2Students);
		reList.addAll(optThreeClassList);
		
//		System.out.println("选三具体人信息：");
//		for (ClassResult classResult : optThreeClassList) {
//			classResult.print();
//		}
		
		//打印下班级序列图
		this.printInfo();

		return reList;
	}
	
	
	/**
	 * 提人(保证同一序列同科下没有相同 人)
	 * @param classList
	 */
	public void extractStudent(List<ClassResult> classList, TreeBasedTable<String, String, List<Student>> parentWishIdSubjectId2Students) {
		
		if(this.wishId2StudentMultimap.size()<=0) throw new RuntimeException("志愿对应学生缓存数据为空");
		
		//志愿在序列下原始总人数
		Map<String, List<Student>> origParentWishId2StudentListMap = new HashMap<>();
		for (Entry<String, Collection<Student>> mapEntry : this.wishId2StudentMultimap.asMap().entrySet()) {
			String parentWishId = mapEntry.getKey();
			origParentWishId2StudentListMap.put(parentWishId, new ArrayList<>(mapEntry.getValue()));
		}
		
		//记录序列下已使用的人
		ArrayListMultimap<String, Student> seqUsedWishId2StudentMultimap = ArrayListMultimap.create();
		
		for (ClassResult classResult : classList) {
			TeachClassResult teachClassResult = (TeachClassResult)classResult;
			for (Cell cell : classResult.getCells()) {
				
				if(cell.getStudentCount()<=0) continue;
				
				String expandWishId = cell.getRowKey();
				String parentWishId = expandWishId.split(Utils.WISH_SEPARATOR_SYMBOL)[0];
				
				List<Student> origStudents = new ArrayList<>(origParentWishId2StudentListMap.get(parentWishId));
				
				//志愿在序列下已使用人
				List<Student> seqUsedStudents = seqUsedWishId2StudentMultimap.get(parentWishId);
				
				//志愿在科目下已使用人
				List<Student> subjectUsedStudnets = parentWishIdSubjectId2Students.get(parentWishId, teachClassResult.getSubjectId());
				if(null == subjectUsedStudnets) subjectUsedStudnets = new ArrayList<>();
				

				origStudents.removeAll(subjectUsedStudnets);
				

				origStudents.removeAll(seqUsedStudents);
				
				//抓人
				if(origStudents.size()<cell.getStudentCount()) {
					throw new RuntimeException(parentWishId+"下可用人数不够！");
				}
				
				List<Student> subStudents = origStudents.subList(0, cell.getStudentCount());
				classResult.getWishId2StudentList().putAll(expandWishId, new ArrayList<>(subStudents));
				//记录当前序列下已使用人
				seqUsedWishId2StudentMultimap.putAll(parentWishId, new ArrayList<>(subStudents));
				//记录当前科目下已使用人
				subjectUsedStudnets.addAll(new ArrayList<>(subStudents));
				parentWishIdSubjectId2Students.put(parentWishId, teachClassResult.getSubjectId(), subjectUsedStudnets);

			}
		}
	}
	
	
	@Override
	public void printInfo() {
		
		StringBuffer title = new StringBuffer("                                               ");
		for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
			title.append("  00"+subject.getId());
		}
		System.out.println(title);
		
		for (Entry<Integer, Map<String, SeqCell>> rowEntry : adClassCountTable.rowMap().entrySet()) {
			
			StringBuffer str = new StringBuffer(rowEntry.getKey()+"序列");
			
			for (SeqCell cell : rowEntry.getValue().values()) {
				
				List<String> tempIds = Lists.transform(cell.getClassResultList(), new Function<ClassResult, String>() {
					public String apply(ClassResult input) {
						return input.getId()+"|"+input.getTotalStudent();
					}
				});
				str.append("  "+tempIds);
			}
			
			System.out.println(str.toString());
		}
	}
	
	
	/**
	 * 根据行政班开选一选二
	 * @param divContext
	 * @param adclassList
	 * @param adClassCountTable
	 * @return
	 */
	public static List<ClassResult>  divTeachClassByAd(DivContext divContext, List<ClassResult> adclassList, TreeBasedTable<Integer, String, SeqCell> adClassCountTable) {
		
		HashMultimap<String, String> subject2AdClassResultList = HashMultimap.create();
		for (ClassResult classResult : adclassList) {
			AdClassResult adClassResult = (AdClassResult)classResult;
			for (Subject subject : adClassResult.getFixTwoSubjectGroup().getSubjects()) {
				subject2AdClassResultList.put(subject.getId(), adClassResult.getId());
			}
		}
		
		Collection<Subject> subjects = divContext.getSubjectId2SubjectMap().values();
		TreeBasedTable<Integer, String, Set<String>> adClassTable=TreeBasedTable.create();
		for (int i=1;i<=2;i++) {
			for (Subject subject : subjects) {
				adClassTable.put(i, subject.getId(), new HashSet<String>());
			}
		}
		
		boolean isOk = backTraceAd(subjects.toArray(new Subject[subjects.size()]), 0, subject2AdClassResultList, adClassCountTable, adClassTable);
		if(!isOk) {
			throw new RuntimeException("以行政班开教学班失败");
		}
		
		List<ClassResult> adTeachList = new ArrayList<>();
		for (Entry<String, Set<String>> adSetEntry : adClassTable.row(1).entrySet()) {
			String subjectId = adSetEntry.getKey();
			
			for (String adId : adSetEntry.getValue()) {
				
				AdClassResult adClass = divContext.getAdClassId2AdClassMap().get(adId);
				
				List<Cell> oneTeachCells = new ArrayList<>();
				for (Cell cell : adClass.getCells()) {
					Cell teachCell = new Cell(adId+Utils.SEPARATOR_SYMBOL+cell.getRowKey(), subjectId, cell.getStudentCount());
					oneTeachCells.add(teachCell);
				}
				
				//一序列以行政开教学班
				TeachClassResult adTeachClass1 = new TeachClassResult(divContext, subjectId, oneTeachCells);
				adTeachClass1.setSeqId(1);
				
				//adTeachClass1.setWishId2StudentList(adClass.getWishId2StudentList());
				adTeachList.add(adTeachClass1);
				
				adClassCountTable.get(1, subjectId).getClassResultList().add(adClass);
				
				//二序列以行政开教学班
				String otherSubjectId = null;
				for (String id : adClass.getFixTwoSubjectGroup().getIds()) {
					if(id!=subjectId) {
						otherSubjectId = id;
						break;
					}
				}
				
				List<Cell> twoTeachCells = new ArrayList<>();
				for (Cell cell : adClass.getCells()) {
					Cell teachCell = new Cell(adId+Utils.SEPARATOR_SYMBOL+cell.getRowKey(), otherSubjectId, cell.getStudentCount());
					twoTeachCells.add(teachCell);
				}
				
				TeachClassResult adTeachClass2 = new TeachClassResult(divContext, otherSubjectId, twoTeachCells);
				adTeachClass2.setSeqId(2);
				//adTeachClass2.setWishId2StudentList(adClass.getWishId2StudentList());
				adTeachList.add(adTeachClass2);
				adClassCountTable.get(2, otherSubjectId).getClassResultList().add(adClass);
			}

		}
		
		return adTeachList;
	}
	
	
	/**
	 * 以行政班开教学班
	 * @param subjects
	 * @param subjectIndex
	 * @param subject2AdClassResultSet
	 * @param adClassCountTable
	 * @param adClassTable
	 * @return
	 */
	public static boolean backTraceAd(Subject[] subjects, Integer subjectIndex,
			HashMultimap<String, String> subject2AdClassResultSet,
			TreeBasedTable<Integer, String, SeqCell> adClassCountTable,
			TreeBasedTable<Integer, String, Set<String>> adClassTable) {
		
		if(subjectIndex>=subjects.length) return true;
		
		String subjectId = subjects[subjectIndex].getId();
		
		Set<String>  adIdList = subject2AdClassResultSet.get(subjectId);
		SeqCell seqCell = adClassCountTable.get(1, subjectId);
		
		if(adIdList.size()<=0 || seqCell.getClassCount()<=0) {
			boolean isOk = backTraceAd(subjects, subjectIndex+1, subject2AdClassResultSet, adClassCountTable, adClassTable);
			return isOk;
		}
		
		Set<String> adSets = new HashSet<String>();
		for (Set<String> adSet : adClassTable.row(1).values()) {
			adSets.addAll(adSet);
		}
		
		SetView<String> diff = Sets.difference(adIdList, adSets);
		
		if(diff.size() <=0) return false;
		
		Set<String> cellSet = adClassTable.get(1, subjectId);
		
		List<LinkedList<String>> combineList = Utils.generateCombine(diff.toArray(new String[diff.size()]), 0, new LinkedList<String>(), seqCell.getClassCount());
		for (LinkedList<String> adClassResult : combineList) {
			
			cellSet.addAll(adClassResult);
			
			boolean isOk = backTraceAd(subjects, subjectIndex+1, subject2AdClassResultSet, adClassCountTable, adClassTable);
			if(isOk) {
				return true;
			}
			cellSet.clear();
			
		}
		
		return false;

	}	

}
