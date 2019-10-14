package com.talkweb.placementtask.utils.div.dezy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.talkweb.placementtask.utils.div.Divide;
import com.talkweb.placementtask.utils.div.TimeoutWatch;
import com.talkweb.placementtask.utils.div.Utils;
import com.talkweb.placementtask.utils.div.dezy.dto.AdClassResult;
import com.talkweb.placementtask.utils.div.dto.Cell;
import com.talkweb.placementtask.utils.div.dto.CellStack;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.SeqCell;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.dto.Subject;
import com.talkweb.placementtask.utils.div.dto.Subject.UP_OR_DOWN;
import com.talkweb.placementtask.utils.div.dto.SubjectAvgSize;
import com.talkweb.placementtask.utils.div.dto.SubjectGroup;
import com.talkweb.placementtask.utils.div.dto.TeachClassResult;


/**
 * 分学考教学班
 * @author hushow
 *
 */
public class ProClassDiv extends Divide<List<ClassResult>>{
	
	/**
	 * 行政班
	 */
	private List<ClassResult> adClassList;
	
	
	/**
	 * 行政班人数对应人数索引
	 */
	private Map<String, Integer> adId2StudentCountMap;
	
	
		
	HashMultimap<String, String> subjectAbleAdMutilMap;
	
	
	public ProClassDiv(DivContext divContext, List<ClassResult> adClassList) {
		
		super(divContext);
		
		this.adClassList = adClassList;
		
		this.adId2StudentCountMap = new HashMap<String, Integer>();
		
		//初始化矩阵图
		for (ClassResult adClassResult : adClassList) {
			
			for (Entry<String, Collection<Student>> wishEntry : adClassResult.getWishId2StudentList().asMap().entrySet()) {
				String wishId = wishEntry.getKey();
				SubjectGroup wish = this.divContext.getWishId2SubjectGroupMap().get(wishId);
				Collection<Student> students = wishEntry.getValue();
				String key = adClassResult.getId()+Utils.SEPARATOR_SYMBOL+wishId;
				//索引行政班+志愿下人数
				this.wishId2StudentMultimap.putAll(key, students);
				for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
					Cell cell = new Cell(key, subject.getId());
					
					//学考科目
					if(!wish.getIds().contains(subject.getId())) {
						subject.setLearnStudentTotalCount(subject.getLearnStudentTotalCount()+students.size());
						cell.setStudentCount(students.size());
						cell.setStudents(new ArrayList<Student>(students));
					}
					this.table.put(key, subject.getId(), cell);
				}
			}
			
			//索引行政班人数
			adId2StudentCountMap.put(adClassResult.getId(), adClassResult.getTotalStudent());
		}
		
		this.calculateAvgSize(this.divContext.getSubjectId2SubjectMap());
		
		this.setTotalStudentCount(divContext.getTotalStudent());
	}
	
	
	@Override
	public List<ClassResult> excuteDiv() {
		
		System.out.println("====================开始分学考班:");
		
		//分学考前打印原始数据
		this.printInfo();
		
		Table<Integer, String, SeqCell> learnSeqTable = calculateHourTable();
		printSeqHour(learnSeqTable);
		
		
		Set<String> subjectSet = divContext.getSubjectId2SubjectMap().keySet();
		
		//保存学考教学班
		List<ClassResult> teachList = new ArrayList<>();
		
		//优先以行政班分完各序列
		List<Integer> remainSeqIndexList = new ArrayList<Integer>();
		
		//记录上序列志愿信息
		ArrayListMultimap<String, Student> preParentWishId2StudentMultimap = ArrayListMultimap.create(this.wishId2StudentMultimap);
		for (int i=4;i<=6;i++) {
			
			HashMultimap<String, String> selectMutilMap = HashMultimap.create();
			
			//计算能开的行班级
			this.subjectAbleAdMutilMap =  calculateAdClassMutilMap();
			
			boolean isOk = adClassBacktrace(subjectSet.toArray(new String[subjectSet.size()]), 0, selectMutilMap , learnSeqTable, i);
			
			System.out.println(i+"序列是否以行政班开完:"+ isOk);
			
			System.out.println(selectMutilMap.toString());

			//可以按行政班开完
			if(isOk && i!=6) {
				List<ClassResult> tList = divByAdClass(selectMutilMap, learnSeqTable, i);
				//为序列提人
				preParentWishId2StudentMultimap = extractStudent(tList, preParentWishId2StudentMultimap);
				teachList.addAll(tList);
			}else {
				remainSeqIndexList.add(i);
			}
		}
		
		System.out.println("====按行政班开完结果,数量:"+teachList.size());
		printInfo();
		
		//处理剩下序列
		int remainSeqCount = remainSeqIndexList.size();
		for (Integer seqIndex : remainSeqIndexList) {
			
			//备份数据
			TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
			
			List<ClassResult> teachClassList= divSeqTeach(divContext, learnSeqTable, seqIndex, remainSeqCount==1);

			System.out.println(seqIndex+"序列恢复前结果");
			this.printInfo();
			
			//还原数据
			this.recoverStudentCount(bakTable, this.table);
			
			//拆减志愿人数
			this.seqSplitWish(teachClassList);
			
			//打印序列结果
			this.seqPrint(seqIndex, teachClassList);
			
			//为序列提人
			preParentWishId2StudentMultimap = extractStudent(teachClassList, preParentWishId2StudentMultimap);
			teachList.addAll(teachClassList);
			
			//标记序列单元格已用完
			Collection<SeqCell> cells = learnSeqTable.row(seqIndex).values();
			for (SeqCell seqCell : cells) {
				seqCell.setIsDiv(true);
			}
			
			remainSeqCount--;
		}
		
		return teachList;
	}
	
	
	public void seqPrint(Integer seqIndex, List<ClassResult> classResultList) {
		
		System.out.println("["+seqIndex+"]序列恢复后拆减人结果");
		this.printInfo();
		
		System.out.println("["+seqIndex+"]序列开班结果");
		int seqTotal = 0;
		ArrayListMultimap<String, Integer> rowKey2CountMap = ArrayListMultimap.create();
		for (ClassResult classEntiry : classResultList) {
			
			TeachClassResult teachClass = (TeachClassResult)classEntiry;
			String subjectId = teachClass.getSubjectId();
			
			StringBuilder txt = new StringBuilder();
			int subjectTotal = 0;
			for (Cell seqCell : teachClass.getCells()) {
				subjectTotal+=seqCell.getStudentCount();
				rowKey2CountMap.put(seqCell.getRowKey(), seqCell.getStudentCount());
				txt.append("    志愿:"+seqCell.getRowKey()+" 人数:"+seqCell.getStudentCount()+"\n");	
			}
			System.out.println(classEntiry.getId()+" 科目"+subjectId+"总人数:"+subjectTotal+" 班级组成:");
			System.out.println(txt.toString());
			
			seqTotal+=subjectTotal;
		}
		
		System.out.println(seqIndex+"序列总人数:"+seqTotal+"参与分班总人数："+ this.getTotalStudentCount());

		if(seqTotal!=this.getTotalStudentCount()) {
			
			for (Entry<String, Collection<Integer>> rowEntry : rowKey2CountMap.asMap().entrySet()) {
				int tCount = 0;
				for (Integer count : rowEntry.getValue()) {
					tCount+=count;
				}
				System.out.println(rowEntry.getKey()+" 下总人数:"+tCount);
			}
			
			throw new RuntimeException(seqIndex+"序列分班失败人数不对，序列总人数:"+seqTotal+"参与分班总人数："+this.getTotalStudentCount());
		}
		
	}
	
	
	/**
	 * 计算平均班额
	 * @param subjectMap
	 */
	public void calculateAvgSize(Map<String, Subject> subjectMap) {
		
		List<SubjectAvgSize> bestList = new ArrayList<>();
		
		List<SubjectAvgSize> sAvgList = new ArrayList<>();
		for (Subject subject : subjectMap.values()) {
			SubjectAvgSize sas = new SubjectAvgSize();
			Double classCount = subject.getLearnStudentTotalCount()/this.divContext.getGlobalAgvClassSize();
			sas.setClassCountMod(classCount%1);
			classCount = Math.ceil(classCount);
			sas.setClassCount(classCount);
			sas.setUpOrDown(UP_OR_DOWN.UP);
			sas.setAvgSize(subject.getLearnStudentTotalCount()/classCount);
			sas.setSubjectId(subject.getId());
			sas.setTotalStudentCount(subject.getLearnStudentTotalCount());
			sAvgList.add(sas);
		}
		
		Double minOffset = Double.MAX_VALUE;
		List<LinkedList<SubjectAvgSize>> upCombineList = Utils.generateCombine(sAvgList.toArray(new SubjectAvgSize[sAvgList.size()]), 0, new LinkedList<>(), 3);
		for (LinkedList<SubjectAvgSize> upList : upCombineList) {
			
			Double maxAvgSize = Double.MIN_VALUE;
			Double minAvgSize = Double.MAX_VALUE;
			
			for (SubjectAvgSize subjectAvgSize : upList) {
				Double upClassCount = Math.ceil(subjectAvgSize.getTotalStudentCount()/this.divContext.getGlobalAgvClassSize());
				subjectAvgSize.setClassCount(upClassCount);
				subjectAvgSize.setAvgSize(subjectAvgSize.getTotalStudentCount()/upClassCount);
				subjectAvgSize.setUpOrDown(UP_OR_DOWN.UP);
				
				if(subjectAvgSize.getAvgSize()>maxAvgSize) maxAvgSize=subjectAvgSize.getAvgSize();
				if(subjectAvgSize.getAvgSize()<minAvgSize) minAvgSize=subjectAvgSize.getAvgSize();
			}
			
			List<SubjectAvgSize> downList = new ArrayList<>(sAvgList);
			downList.removeAll(upList);
			for (SubjectAvgSize subjectAvgSize : downList) {
				Double downClassCount = Math.floor(subjectAvgSize.getTotalStudentCount()/this.divContext.getGlobalAgvClassSize());
				subjectAvgSize.setClassCount(downClassCount);
				subjectAvgSize.setAvgSize(subjectAvgSize.getTotalStudentCount()/downClassCount);
				subjectAvgSize.setUpOrDown(UP_OR_DOWN.DOWN);
				
				if(subjectAvgSize.getAvgSize()>maxAvgSize) maxAvgSize=subjectAvgSize.getAvgSize();
				if(subjectAvgSize.getAvgSize()<minAvgSize) minAvgSize=subjectAvgSize.getAvgSize();
			}
			
			//记录最小数据
			if(maxAvgSize-minAvgSize<minOffset) {
				bestList.clear();
				minOffset = maxAvgSize-minAvgSize;
				for (SubjectAvgSize subjectAvgSize : upList) {
					bestList.add(new SubjectAvgSize().copyFrom(subjectAvgSize));
				}
				for (SubjectAvgSize subjectAvgSize : downList) {
					bestList.add(new SubjectAvgSize().copyFrom(subjectAvgSize));
				}
			}
			
		}
		
		for (SubjectAvgSize subjectAvgSize : bestList) {
			
			if(subjectAvgSize.getTotalStudentCount()<=0) continue;
			
			Subject subject = subjectMap.get(subjectAvgSize.getSubjectId());
			
			subject.setLearnClassTotalCount(subjectAvgSize.getClassCount().intValue());
			subject.setUpOrDown(subjectAvgSize.getUpOrDown());
			try {
				subject.setLearnAvgClassSize(new BigDecimal(subjectAvgSize.getAvgSize()).setScale(2, BigDecimal.ROUND_HALF_UP));
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * 打印数据
	 */
	@Override
	public void  printInfo() {
		
		StringBuffer title1 = new StringBuffer("       ");
		StringBuffer title = new StringBuffer("             ");
		StringBuffer studentTotalCount = new StringBuffer("总人数                               ");
		StringBuffer classTotalCount = new StringBuffer("开班数                               ");
		StringBuffer avgClassCount = new StringBuffer("平均值                               ");
		for (Subject  subject : this.divContext.getSubjectId2SubjectMap().values()) {
			
			title1.append("  "+subject.getName());
			title.append("  00"+subject.getId());
			studentTotalCount.append("  "+Utils.fillStr(subject.getLearnStudentTotalCount(), 3));
			classTotalCount.append("  "+Utils.fillStr(subject.getLearnClassTotalCount(), 3));
			avgClassCount.append("  "+subject.getLearnAvgClassSize());
		}
		System.out.println(title1.toString());
		System.out.println(title.toString());
		
		for (Entry<String, Map<String, Cell>> rowEntry : this.table.rowMap().entrySet()) {
			String  wishId = Utils.extractWishSubjectGroupId(rowEntry.getKey());
			SubjectGroup wishSubjectGroup = this.divContext.getWishId2SubjectGroupMap().get(wishId);
			StringBuffer sb = new StringBuffer(wishSubjectGroup.getName()+rowEntry.getKey());
			for (Subject  subject : this.divContext.getSubjectId2SubjectMap().values()) {
				Cell cell = rowEntry.getValue().get(subject.getId());
				
				String studentCountstr = String.valueOf(cell.getStudentCount());
				int x= 3-studentCountstr.toCharArray().length;
				if(x>0) {
					for(int i=0;i<x;i++) {
						studentCountstr="0"+studentCountstr;
					}
				}
				
				sb.append("  "+studentCountstr);
				
			}
			System.out.println(sb.toString());
		}
		
		System.out.println(studentTotalCount);
		System.out.println(classTotalCount);
		System.out.println(avgClassCount);
	}
	
	
	/**
	 * 计算选考最优学时分布图
	 * @param subjectMap
	 * @return
	 */
	public Table<Integer, String, SeqCell>  calculateHourTable() {
		
		//初始化矩阵图
		Table<Integer, String, SeqCell> classHourTable=HashBasedTable.create();
		for(int i=4;i<=6;i++) {
			for (Entry<String, Subject> entry : divContext.getSubjectId2SubjectMap().entrySet()) {
				classHourTable.put(i, entry.getKey(), new SeqCell(i, entry.getValue()));
			}
		}
		
		//初始化矩阵科目开班分布
		int preIndex = 0;
		//循环科目
		for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
			int classNumber = subject.getLearnClassTotalCount();
			Map<Integer, SeqCell> seqMap =  classHourTable.column(subject.getId());
			Integer[] seqArray = seqMap.keySet().toArray(new Integer[seqMap.keySet().size()]);
			int i=preIndex>=seqMap.keySet().size()?0:preIndex;
			//循环序列
			while(i<seqArray.length && classNumber > 0) {
				SeqCell cell = seqMap.get(seqArray[i]);
				cell.setClassCount(cell.getClassCount()+1);
				classNumber--;
				if(i==seqArray.length-1 && classNumber>0) {
					i=0;
				}else {
					i++;
				}
			}
			preIndex = i;
		}
		
		
		return classHourTable;
	}
	
	
	/**
	 * 打印科目序列课时分布图
	 */
	public void  printSeqHour(Table<Integer, String, SeqCell> table) {
		
		StringBuffer title = new StringBuffer("      ");
		StringBuffer avgClassSizeStr = new StringBuffer("平均班额  ");
		StringBuffer classTotalStr  = new StringBuffer("科目班数  ");
		for (Subject subject : this.divContext.getSubjectId2SubjectMap().values()) {
			title.append("   "+subject.getName()+"("+subject.getTotalStudentCount()+")");
			avgClassSizeStr.append("  "+subject.getLearnAvgClassSize());
			classTotalStr.append("  "+subject.getLearnClassTotalCount());
		}
		System.out.println(title.toString());
		
		for (Map.Entry<Integer, Map<String, SeqCell>> rowEntry : table.rowMap().entrySet()) {
			StringBuffer sb = new StringBuffer(rowEntry.getKey()+"序列       ");
			for (Subject subject : this.divContext.getSubjectId2SubjectMap().values()) {
				SeqCell cell = rowEntry.getValue().get(subject.getId());
				sb.append("  "+cell.getClassCount());
			}
			System.out.println(sb.toString());
		}
		
		System.out.println(classTotalStr);
		System.out.println(avgClassSizeStr);
	}
	
	public HashMultimap<String, String> calculateAdClassMutilMap() {
		
		HashMultimap<String, String> mutilMap = HashMultimap.create(); 
		
		//循环科目
		for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
			//循环行政班级
			for (ClassResult adClassResult : this.adClassList) {
				
				int totalStudent = 0;
				//循环所有单元格
				for (Cell cell :  this.table.column(subject.getId()).values()) {
					String adClassId = cell.getRowKey().split(Utils.SEPARATOR_SYMBOL)[0];
					if(!adClassId.equals(adClassResult.getId())) continue;
					totalStudent+=cell.getStudents().size();
				}
				
				if(adClassResult.getTotalStudent() == totalStudent) mutilMap.put(subject.getId(), adClassResult.getId());
			}
		}
		
		return mutilMap;
	}
	
	public boolean adClassBacktrace(String[] subjectIds, int index, HashMultimap<String, String> selectMutilMap,
			Table<Integer, String, SeqCell> learnSeqTable, int seqIndex) {
		
		if(index == subjectIds.length) {
			return true;
		}
		
		String currSubjectId =  subjectIds[index];
		
		Set<String> adClassIds = this.subjectAbleAdMutilMap.get(currSubjectId);
		
		//从开班分布决定选择数量
		Map<String, SeqCell> rows = learnSeqTable.row(seqIndex);
		int selectCount = rows.get(currSubjectId).getClassCount();
		
		//取差集
		SetView<String> diff = Sets.difference(adClassIds, new HashSet<String>(selectMutilMap.values()));
		
		//不够开班，返回false
		if(diff.size() < selectCount) {
			return false;
		}
		
		//计算组合数
		List<LinkedList<String>> combineList = Utils.generateCombine(diff.toArray(new String[diff.size()]),
				0, new LinkedList<String>(), selectCount);
		
		//处理依次选中情况
		for (LinkedList<String> selectCombine : combineList) {
			
			selectMutilMap.putAll(currSubjectId, selectCombine);
			
			boolean isOk = this.adClassBacktrace(subjectIds, index+1, selectMutilMap, learnSeqTable, seqIndex);
			
			if(isOk) return true;
			
			selectMutilMap.removeAll(currSubjectId);
		}
		
		return false;
		
	}
	
	/**
	 * 序列按行政班开班
	 * @param selectMutilMap
	 * @param learnSeqTable
	 * @param seqIndex
	 */
	public List<ClassResult> divByAdClass(HashMultimap<String, String> selectMutilMap, Table<Integer, String, SeqCell> learnSeqTable, int seqIndex) {
		
		List<ClassResult> teachList = new ArrayList<>();
		
		for (String subjectId : selectMutilMap.keySet()) {
			Collection<Cell> cells = this.table.column(subjectId).values();
			SeqCell cell = learnSeqTable.row(seqIndex).get(subjectId);
			for (String adClassId : selectMutilMap.get(subjectId)) {
				
				AdClassResult adClass = divContext.getAdClassId2AdClassMap().get(adClassId);
				
				List<Cell> teachCells = new ArrayList<>();
				for (Cell c : adClass.getCells()) {
					Cell teachCell = new Cell(adClassId+Utils.SEPARATOR_SYMBOL+c.getRowKey(), subjectId, c.getStudentCount());
					teachCell.setSplitWishId(teachCell.getRowKey());
					teachCells.add(teachCell);
				}
				
				//以行政班相关人员分配教学班级
				TeachClassResult cr = new TeachClassResult(divContext, subjectId, teachCells);
//				cr.setWishId2StudentList(adClass.getWishId2StudentList());
				cr.setWishId2StudentCountMap(adClass.getWishId2StudentCountMap());
				cr.setWishSubjectGroups(adClass.getWishSubjectGroups());
				cr.setSeqId(seqIndex);
				cell.getClassResultList().add(cr);
				
				teachList.add(cr);
				
				//this.cleanClassData(teachCells, false);
		
				//清理当前序列下行政班人数据
				for (Cell learnCell : cells) {
					String adId = learnCell.getRowKey().split(Utils.SEPARATOR_SYMBOL)[0];
					if(!adId.equals(adClassId)) continue;
					learnCell.getStudents().clear();
					learnCell.setStudentCount(0);
				}
			}
			//标记已经开完班
			cell.setIsDiv(true);
		}
		
		
		return teachList;
	}
	
	
	public List<ClassResult> divSeqTeach(DivContext divContext, Table<Integer, String, SeqCell> learnSeqTable, int seqIndex,
			boolean isLastSeq) {
		
		//开班数为0的科目上置0
		for (Entry<String, Map<String, Cell>> columnEntry : this.table.columnMap().entrySet()) {
			String subjectId = columnEntry.getKey();

			if(learnSeqTable.get(seqIndex, subjectId).getClassCount() <=0) {
				for (Cell cell : columnEntry.getValue().values()) {
					cell.setStudentCount(0);
				}
				continue;
			}
		}
		
		//序列开始前，计算科目班额
		Map<String, Integer> subjectId2StudentCountMap = this.getCurrentSubjectStudentCount();
		
		//循环序列下所有科目
		Collection<SeqCell> cells = learnSeqTable.row(seqIndex).values();
		
		HashMultimap<String, LinkedList<Cell>> selectData = HashMultimap.create();
		
		SeqCell[] subjectCells = cells.toArray(new SeqCell[cells.size()]);
		
		for (int subjectIndex=0;subjectIndex<subjectCells.length;subjectIndex++) {

			//计算当前科目剩余人数
			Map<String, Integer> currSubjectId2StudentCountMap = this.getCurrentSubjectStudentCount();
			
			//未分完科目排序
			for(int i=subjectIndex;i<subjectCells.length;i++) {
				for(int j=i+1;j<subjectCells.length;j++) {
					Subject subject_i = subjectCells[i].getSubject();
					Subject subject_j = subjectCells[j].getSubject();
					int classCount_i = learnSeqTable.get(seqIndex, subject_i.getId()).getClassCount();
					int classCount_j = learnSeqTable.get(seqIndex, subject_j.getId()).getClassCount();

					Integer curr_count_i = currSubjectId2StudentCountMap.get(subject_i.getId());
					Integer curr_count_j = currSubjectId2StudentCountMap.get(subject_j.getId());
					
					Integer count_i = subject_i.getLearnAvgClassSize().multiply(new BigDecimal(classCount_i)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
					Integer count_j = subject_j.getLearnAvgClassSize().multiply(new BigDecimal(classCount_j)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
					
					int value_i = curr_count_i - count_i;
					int value_j = curr_count_j - count_j;
					
					if(value_i>value_j){
						SeqCell temp=subjectCells[i];
						subjectCells[i]=subjectCells[j];
						subjectCells[j]=temp;
					}
				}
			}

			this.divTeachSubject(divContext, subjectCells[subjectIndex], subjectCells, learnSeqTable, seqIndex, isLastSeq, subjectId2StudentCountMap, selectData);

			subjectCells[subjectIndex].setIsDiv(true);
		}
		
		List<ClassResult> classLists = new ArrayList<>();
		//循环科目
		for (Subject subject : this.divContext.getSubjectId2SubjectMap().values()) {
			
			//循环科目下所有班级
			Set<LinkedList<Cell>> subjectClassList = selectData.get(subject.getId());
			for (LinkedList<Cell> oneClassList : subjectClassList) {
				//开班
				TeachClassResult classResult = new TeachClassResult(divContext, subject.getId(), oneClassList);
				classResult.setSeqId(seqIndex);
				classLists.add(classResult);
			}
		}
		return classLists;
	}
	

	public boolean divTeachSubject(DivContext divContext, SeqCell currCell, SeqCell[] subjectCells, Table<Integer, String, SeqCell> learnSeqTable, int seqIndex,
			boolean isLastSeq, Map<String, Integer> subjectId2StudentCountMap, HashMultimap<String, LinkedList<Cell>> selectData) {		

		//获取当前科目
		String subjectId = currCell.getSubject().getId();

		//当前科目当前序列必须开班
		Integer mustClassNum = currCell.getClassCount();
		if(mustClassNum ==0) return true;

		//计算科目剩下开班数
		int remainClassCount = 0;
		for (SeqCell seqCell : learnSeqTable.column(subjectId).values()) {
			if(seqCell.getIsDiv()) continue;
			remainClassCount+=seqCell.getClassCount();
		}

		if(remainClassCount ==0) return true;
		
		int subjectCurrStudentCount = subjectId2StudentCountMap.get(subjectId);
		
		int avgClassSize = 0;
		try {
			avgClassSize =  new BigDecimal(((Integer)subjectCurrStudentCount).doubleValue()/remainClassCount).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

		}catch(Exception e) {
			e.printStackTrace();
		}
		
		//判断情况
		
		Collection<Cell> columns = this.getColumnValidCells(subjectId);

		if(isLastSeq) {
			LinkedList<Cell> newList = new LinkedList<Cell>();
			for (Cell learnCell : columns) {
				if(learnCell.getStudentCount()<=0) continue;
				newList.add(learnCell);
			}
			Double d = Math.ceil(subjectCurrStudentCount/mustClassNum.floatValue());
			List<LinkedList<Cell>> splitList = Utils.splitSubClass(newList, d.intValue());
			this.cleanMulClassData(splitList, isLastSeq); 
			selectData.putAll(subjectId, splitList);
			return true;
		}
		
		//获取必须要使用的科目
		List<Cell> needCells = this.getNeedCells(columns, subjectCells);
		int mustStuNUM = 0;
		for (Cell learnCell : needCells) {
			mustStuNUM+=learnCell.getStudentCount();
		}

		//获取可开的行政班
		ArrayListMultimap<String, Cell> adCellsMap = this.getAdCells(columns);
		//排除是行政班又是必填项
		for (Cell learnCell : needCells) {
			String adClassId = learnCell.getRowKey().split(Utils.SEPARATOR_SYMBOL)[0];
			adCellsMap.removeAll(adClassId);
		}
		
		int adminClassNum = adCellsMap.keySet().size();
		
		//获取其它的项
		List<Cell> otherCells  = this.getOtherCells(columns, needCells, adCellsMap.values());
		int teachStuNumber = 0;
		for (Cell learnCell : otherCells) {
			teachStuNumber+=learnCell.getStudentCount();
		}

		int preMaxDiffValue = Integer.MIN_VALUE;
		List<LinkedList<Cell>> preLinked = null;

		//1、没有必选也没有行政班的情况
		if(mustStuNUM ==0 && adminClassNum ==0){
			//TODO 直接用这个科目所有教学班遍历找结果
			LinkedList<Cell> defaultSelect = new LinkedList<Cell>();

			List<CellStack> cellStackList = Utils.generateSumCombineArrange(
					otherCells.toArray(new Cell[otherCells.size()]), defaultSelect, avgClassSize*mustClassNum, new TimeoutWatch(60*5L));
			for (CellStack cellStack : cellStackList) {
				//备份数据
				TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
				
				LinkedList<Cell> selectLinked = Utils.splitClass(cellStack, table);
				List<LinkedList<Cell>> classList = Utils.splitSubClass(selectLinked, avgClassSize);
				this.cleanMulClassData(classList, isLastSeq);
				int  totalDiff = this.calculateTotalDiff(subjectId, subjectCells);
				if(totalDiff>=0) {
					selectData.putAll(subjectId, classList);
					return true;
				}else {
					//记录此次好的结果
					if(totalDiff > preMaxDiffValue) {
						preMaxDiffValue = totalDiff;
						preLinked = classList;
					}
					
					//还原数据
					this.recoverStudentCount(bakTable, this.table);
				}
			}
		}else if(mustStuNUM !=0 && adminClassNum ==0){//2、有必选没有行政班
			//2.1、如果必选人数就足够将班级开下来
			if(mustStuNUM>= mustClassNum*avgClassSize){ 
				//TODO 直接以必选人数安排
				Double d = Math.ceil(mustStuNUM/mustClassNum.floatValue());
				List<LinkedList<Cell>> splitList = Utils.splitSubClass(needCells, d.intValue());
				this.cleanMulClassData(splitList, isLastSeq);
				selectData.putAll(subjectId, splitList);
				return true;
				//Utils.splitSubClass(needCells, avgClassSize);
			}else{ //2.2、必选人数不够开班

				//只剩下必选了，不管多少直接开班
				if(otherCells.size()<=0) {
					Double d = Math.ceil(mustStuNUM/mustClassNum.floatValue());
					List<LinkedList<Cell>> list = Utils.splitSubClass(needCells, d.intValue());
					this.cleanMulClassData(list, isLastSeq);
					selectData.putAll(subjectId, list);
					return true;
				}

				if(mustStuNUM+teachStuNumber<avgClassSize*mustClassNum) {
					LinkedList<Cell> newList = new LinkedList<Cell>();
					newList.addAll(needCells);
					newList.addAll(otherCells);
					List<LinkedList<Cell>> list = Utils.splitSubClass(newList, avgClassSize);
					this.cleanMulClassData(list, isLastSeq);
					selectData.putAll(subjectId, list);
					return true;
				}

				//TODO 所有必选人数+遍历其他组合学生
				LinkedList<Cell> defaultSelect = new LinkedList<Cell>(needCells);
				List<CellStack> cellStackList = Utils.generateSumCombineArrange(
						otherCells.toArray(new Cell[otherCells.size()]), defaultSelect, avgClassSize*mustClassNum, new TimeoutWatch(60*5L));
				for (CellStack cellStack : cellStackList) {
					//备份数据
					TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
					LinkedList<Cell> selectLinked = Utils.splitClass(cellStack, table);
					List<LinkedList<Cell>> classList = Utils.splitSubClass(selectLinked, avgClassSize);
					this.cleanMulClassData(classList, isLastSeq);
					int  totalDiff = this.calculateTotalDiff(subjectId, subjectCells);
					if(totalDiff>=0) {
						selectData.putAll(subjectId, classList);
						return true;
					}else {
						//记录此次好的结果
						if(totalDiff > preMaxDiffValue) {
							preMaxDiffValue = totalDiff;
							preLinked = classList;
						}
						//还原数据
						this.recoverStudentCount(bakTable, this.table);
					}
				}
			}
		} else if(mustStuNUM ==0 && adminClassNum !=0){//3、没有必选有行政班
			//3.1、如果行政班开班数>需要开的班级数
			if(adminClassNum >= mustClassNum){
				//先尝试完全行政班开班，从adminClassNum个行政班中随机取theoryAdminClassNum个，如果成功的话保存结果，跳出分班
				Set<String> adSet = adCellsMap.keySet();
				List<LinkedList<String>> adSelectList = Utils.generateCombine(adSet.toArray(new String[adSet.size()]), 0, new LinkedList<String>(), mustClassNum);
				for (LinkedList<String> ads : adSelectList) {
					
					//备份数据
					TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
					
					List<LinkedList<Cell>> adList = new ArrayList<LinkedList<Cell>>();
					for (String adId : ads) {
						List<Cell> scrList = adCellsMap.get(adId);
						adList.add(new LinkedList<Cell>(Utils.copyLearnCellList(scrList)));
					}

					this.cleanMulClassData(adList, isLastSeq);

					int  totalDiff = this.calculateTotalDiff(subjectId, subjectCells);
					if(totalDiff>=0) {

						selectData.putAll(subjectId, adList);
						return true;
					}else {
						//记录此次好的结果
						if(totalDiff > preMaxDiffValue) {
							preMaxDiffValue = totalDiff;
							preLinked = adList;
						}
						//还原数据
						this.recoverStudentCount(bakTable, this.table);
					}
				}
			}
			
			//实际最大能开的行政班个数
			int theoryAdminClassNum = Math.min(adminClassNum,mustClassNum);
			//☆本科目教学班学生数目（非行政班）
			//Integer teachStuNum =0;
			
			//3.2、没跳出的话说明第一步不能满足，尝试部分行政班+教学班组合，这一步是的行政班不拆分
			for (;theoryAdminClassNum>0;theoryAdminClassNum--){
				//如果出现教学班不够的情况，那么说明行政班已经不能再减少了，跳出循环
				int remainNeedNum = (mustClassNum-theoryAdminClassNum)*avgClassSize;
				if(teachStuNumber<remainNeedNum){
					break;
				} 
				//TODO 从所有行政班中任意取theoryAdminClassNum个行政班，然后再加教学班组合，如果成功的话保存结果跳出分班
				Set<String> adSet = adCellsMap.keySet();
				List<LinkedList<String>> adSelectList = Utils.generateCombine(adSet.toArray(new String[adSet.size()]), 0, new LinkedList<String>(), theoryAdminClassNum);
				List<CellStack> teachCellStackList = Utils.generateSumCombineArrange(
						otherCells.toArray(new Cell[otherCells.size()]), new LinkedList<Cell>(), remainNeedNum, new TimeoutWatch(60*5L));

				for (LinkedList<String> ads : adSelectList) {
					
					for (CellStack teachCellStack : teachCellStackList) {
						
						//备份数据
						TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
						
						//行政开班
						List<LinkedList<Cell>> adList = new ArrayList<LinkedList<Cell>>();
						for (String adId : ads) {
							adList.add(new LinkedList<Cell>(Utils.copyLearnCellList(adCellsMap.get(adId))));
						}
						
						//教学开班
						LinkedList<Cell> splitItem = Utils.splitClass(teachCellStack, table);
						List<LinkedList<Cell>> classList = Utils.splitSubClass(splitItem, avgClassSize);
						for (LinkedList<Cell> linkedList : classList) {
							adList.add(linkedList);
						}

						this.cleanMulClassData(adList, isLastSeq);
						
						int  totalDiff = this.calculateTotalDiff(subjectId, subjectCells);
						if(totalDiff>=0) {
							selectData.putAll(subjectId, adList);
							return true;
						}else {
							//记录此次好的结果
							if(totalDiff > preMaxDiffValue) {
								preMaxDiffValue = totalDiff;
								preLinked = adList;
							}
							//还原数据
							this.recoverStudentCount(bakTable, this.table);
						}
						
					}
				}
			}
			
			//3.3、如果用上面行政班+教学班方式无法开班
			//TODO 不管行政班还是教学班，一起遍历找最优解
			List<CellStack> cellStackList = Utils.generateSumCombineArrange(
					otherCells.toArray(new Cell[otherCells.size()]), new LinkedList<Cell>(), avgClassSize*mustClassNum, new TimeoutWatch(60*5L));
			for (CellStack cellStack : cellStackList) {
				//备份数据
				TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
				
				LinkedList<Cell> selectLinked = Utils.splitClass(cellStack, table);
				List<LinkedList<Cell>> classList = Utils.splitSubClass(selectLinked, avgClassSize);
				this.cleanMulClassData(classList, isLastSeq);
				int  totalDiff = this.calculateTotalDiff(subjectId, subjectCells);
				if(totalDiff>=0) {
					selectData.putAll(subjectId, classList);
					return true;
				}else {
					//记录此次好的结果
					if(totalDiff > preMaxDiffValue) {
						preMaxDiffValue = totalDiff;
						preLinked = classList;
					}
					//还原数据
					this.recoverStudentCount(bakTable, this.table);
				}
			}
			
		}else if(mustStuNUM !=0 && adminClassNum !=0){  //4、有必选也有行政班
			//4.1、如果必须安排人数>=需要安排的人数
			if(mustStuNUM>=mustClassNum*avgClassSize){
				//TODO 直接以必选人数安排
				List<LinkedList<Cell>> splitList = Utils.splitSubClass(needCells, avgClassSize);
				this.cleanMulClassData(splitList, isLastSeq);
				selectData.putAll(subjectId, splitList);
				return true;
			}else if(mustStuNUM>(mustClassNum-1)*avgClassSize){ //4.2、最后一个班已经有人了，但是没满(不可能放完整的行政班)

				//必选开班
				List<LinkedList<Cell>> needClassList = Utils.splitSubClass(new LinkedList<Cell>(needCells), avgClassSize);
				LinkedList<Cell> remainNeedList =  needClassList.remove(needClassList.size()-1);

				//TODO 4.2.1、剩下必选+教学班
				List<CellStack> needTeachCellStackList = Utils.generateSumCombineArrange(
						otherCells.toArray(new Cell[otherCells.size()]), new LinkedList<Cell>(remainNeedList), avgClassSize, new TimeoutWatch(60*5L));
				for (CellStack needTeachCellStack : needTeachCellStackList) {
					
					//备份数据
					TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
					
					List<LinkedList<Cell>> adList = new ArrayList<LinkedList<Cell>>(needClassList);
					
					//必选+教学班数据
					LinkedList<Cell> splitClassList = Utils.splitClass(needTeachCellStack, table);
					adList.add(splitClassList);
					
					this.cleanMulClassData(adList, isLastSeq);
					
					int  totalDiff = this.calculateTotalDiff(subjectId, subjectCells);
					if(totalDiff>=0) {
						selectData.putAll(subjectId, adList);
						return true;
					}else {
						//记录此次好的结果
						if(totalDiff > preMaxDiffValue) {
							preMaxDiffValue = totalDiff;
							preLinked = adList;
						}
						//还原数据
						this.recoverStudentCount(bakTable, this.table);
					}
					
				}
				
				//TODO 4.2.2、剩下必选+行政班教学班
				LinkedList<Cell> adCellList  = new LinkedList<>(adCellsMap.values());
				adCellList.addAll(otherCells);
				List<CellStack> needAdTeachCellStackList = Utils.generateSumCombineArrange(
						adCellList.toArray(new Cell[adCellList.size()]), new LinkedList<Cell>(remainNeedList), avgClassSize, new TimeoutWatch(60*5L));
				
				//优化排序
//				Collections.sort(needAdTeachList, new Comparator<LinkedList<LearnCell>>() {
//					@Override
//					public int compare(LinkedList<LearnCell> o1, LinkedList<LearnCell> o2) {
//						ArrayListMultimap<String, Integer> adCountMap = ArrayListMultimap.create();
//						for (LearnCell cell : o1) {
//							adCountMap.put(cell.getAdClassId(), cell.getStudentCount());
//						}
//						
//						
//						return 0;
//					}
//				});
				
				for (CellStack needAdTeachCellStack : needAdTeachCellStackList) {					
					
					//备份数据
					TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
					
					List<LinkedList<Cell>> adList = new ArrayList<LinkedList<Cell>>(needClassList);
					
					//必选+教学班数据
					LinkedList<Cell> splitClassList = Utils.splitClass(needAdTeachCellStack, table);
					adList.add(splitClassList);
					
					this.cleanMulClassData(adList, isLastSeq);
					
					int  totalDiff = this.calculateTotalDiff(subjectId, subjectCells);
					if(totalDiff>=0) {
						selectData.putAll(subjectId, adList);
						return true;
					}else {
						//记录此次好的结果
						if(totalDiff > preMaxDiffValue) {
							preMaxDiffValue = totalDiff;
							preLinked = adList;
						}
						//还原数据
						this.recoverStudentCount(bakTable, this.table);
					}
					
				}
				
			}else{ //4.3、 必须人数不大于mustClassNum-1个班(可能放完整的行政班进来) mustStuNUM<=(mustClassNum-1)*avgClassSize
				
				//int remainCount = mustStuNUM%avgClassSize;
				
				//理论上最多可以放的行政班数量(可能刚好整除为整数，也可能为小数)
				
				float adminMaxClassNum = (mustClassNum-((float)mustStuNUM/avgClassSize));

				//必选开班
				LinkedList<Cell> remainNeedList =  new LinkedList<Cell>();
				List<LinkedList<Cell>> needClassList = new ArrayList<LinkedList<Cell>>();
				List<LinkedList<Cell>> allNeedClassList = Utils.splitSubClass(new LinkedList<Cell>(needCells), avgClassSize);
				int needTotal = 0;
				
				for (LinkedList<Cell> linkedList : allNeedClassList) {
					int total=0;
					for (Cell learnCell : linkedList) {
						total+=learnCell.getStudentCount();
					}
					if(total < avgClassSize) {
						remainNeedList = linkedList;
					}else {
						needClassList.add(linkedList);
						needTotal+=total;
					}
				}
				
				
//				List<List<LinkedList<LearnCell>>> needLinkedList = new ArrayList<List<LinkedList<LearnCell>>>();
//				List<ArrangeItem> needList = Utils.generateSumCombineArrange(
//						needCells.toArray(new LearnCell[needCells.size()]), new LinkedList<LearnCell>(),
//						mustStuNUM - remainCount);
//				for (ArrangeItem arrangeItem : needList) {
//					//开大班情况
//					LinkedList<LearnCell> splitClassList = Utils.splitClass(arrangeItem.getItems(), mustStuNUM-remainCount, remainSeqCount, table);
//					//具体班级情况
//					List<LinkedList<LearnCell>> splitSubClassList = Utils.splitSubClass(splitClassList, avgClassSize);
//					needLinkedList.add(splitSubClassList);
//				}
				
				//4.3.1、如果adminMaxClassNum的值和向下取整一样，那么的结果是整数，就可能出现只使用必选+行政班来达到目的
				if(adminMaxClassNum == Math.floor(adminMaxClassNum)){
					//只有行政班的数量大于等于理论上最多可以放的行政班数量，才可能出现必选+行政班的情况
					if(adminClassNum>=Math.floor(adminMaxClassNum)){
						//4.3.1.1、直接从adminClassNum个行政班中随机取adminMaxClassNum个班级，如果成功的话保存结果(不需要做教学班遍历，因为下面会统一做处理)
						int count = ((Double)Math.floor(adminMaxClassNum)).intValue();
						Set<String> adSet = adCellsMap.keySet();
						List<LinkedList<String>> adSelectList = Utils.generateCombine(adSet.toArray(new String[adSet.size()]), 0, new LinkedList<String>(), count);
						for (LinkedList<String> ads : adSelectList) {
							
							//备份数据
							TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
							
							//行政班
							List<LinkedList<Cell>> adList = new ArrayList<LinkedList<Cell>>();
							for (String adId : ads) {
								adList.add(new LinkedList<Cell>(Utils.copyLearnCellList(adCellsMap.get(adId))));
							}
							
							//必选
							adList.addAll(needClassList);
							
							this.cleanMulClassData(adList, isLastSeq);
							
							int  totalDiff = this.calculateTotalDiff(subjectId, subjectCells);
							if(totalDiff>=0) {
								selectData.putAll(subjectId, adList);
								return true;
							}else {
								//记录此次好的结果
								if(totalDiff > preMaxDiffValue) {
									preMaxDiffValue = totalDiff;
									preLinked = adList;
								}
								//还原数据
								this.recoverStudentCount(bakTable, this.table);
							}
						}
					}
				}else{ //4.3.2、如果adminMaxClassNum的结果不是整数，那么就必须会用到教学班人数
					//实际最多用到的行政班个数(用理论上能用到的行政班和实际行政班的个数，取最小值)，这一步是的行政班不拆分
					Integer adminMaxClassNumNew =Math.min(adminClassNum, (int)(Math.floor(adminMaxClassNum)));
						for (;adminMaxClassNumNew>0;adminMaxClassNumNew--){
							//如果出现教学班不够的情况，那么说明行政班已经不能再减少了，跳出循环
							int remainNeedNum = (mustClassNum-adminMaxClassNumNew)*avgClassSize;
							if((teachStuNumber+mustStuNUM) < remainNeedNum){
								break;
							}
	
							//TODO 4.3.2.1、从所有行政班中任意取adminMaxClassNumNew个行政班，然后再加教学班组合，如果成功的话保存结果跳出分班
							Set<String> adSet = adCellsMap.keySet();
							List<LinkedList<String>> adSelectList = Utils.generateCombine(adSet.toArray(new String[adSet.size()]), 0, new LinkedList<String>(), adminMaxClassNumNew);
							
							//必选剩余+教学
							List<CellStack> needTeachCellStackList = Utils.generateSumCombineArrange(
									otherCells.toArray(new Cell[otherCells.size()]), new LinkedList<Cell>(remainNeedList), remainNeedNum-needTotal, new TimeoutWatch(60*5L));
							
							for (LinkedList<String> ads : adSelectList) {
								
								for (CellStack needTeachCellStack : needTeachCellStackList) {
									
									//备份数据
									TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
									
									//行政开班
									List<LinkedList<Cell>> adList = new ArrayList<LinkedList<Cell>>();
									for (String adId : ads) {
										adList.add(new LinkedList<Cell>(Utils.copyLearnCellList(adCellsMap.get(adId))));
									}

									//必选完整班级
									adList.addAll(needClassList);
									
									//教学开班
									LinkedList<Cell>  splitList = Utils.splitClass(needTeachCellStack, table);
									List<LinkedList<Cell>> classList = Utils.splitSubClass(splitList, avgClassSize);
									for (LinkedList<Cell> linkedList : classList) {
										adList.add(linkedList);
									}

									this.cleanMulClassData(adList, isLastSeq);

									int  totalDiff = this.calculateTotalDiff(subjectId, subjectCells);
									if(totalDiff>=0) {
										selectData.putAll(subjectId, adList);
										return true; 
									}else {
										//记录此次好的结果
										if(totalDiff > preMaxDiffValue) {
											preMaxDiffValue = totalDiff;
											preLinked = adList;
										}
										//还原数据
										this.recoverStudentCount(bakTable, this.table);
									}
									
								}
							}
							
						}
				}
				//4.3.2.2(4.3.1.2)、如果用上面行政班+教学班方式无法开班
				//TODO 必选+不管行政班还是教学班，一起遍历找最优解

				//必选剩余+行政教学
				int remainAdTeachCount = (mustClassNum-needClassList.size())*avgClassSize;
				List<Cell> adNeedTeachList = new ArrayList<Cell>(otherCells);
				adNeedTeachList.addAll(adCellsMap.values());
				List<CellStack> needAdTeachCellStackList = Utils.generateSumCombineArrange(
						adNeedTeachList.toArray(new Cell[adNeedTeachList.size()]), new LinkedList<Cell>(remainNeedList), remainAdTeachCount, new TimeoutWatch(60*5L));
				for (CellStack needAdTeachCellStack : needAdTeachCellStackList) {
					
					//备份数据
					TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
					
					List<LinkedList<Cell>> adList = new ArrayList<LinkedList<Cell>>();
					
					//教学开班
					 LinkedList<Cell> splitList = Utils.splitClass(needAdTeachCellStack, table);
					List<LinkedList<Cell>> classList = Utils.splitSubClass(splitList, avgClassSize);
					for (LinkedList<Cell> linkedList : classList) {
						adList.add(linkedList);
					}

					//必选开班
					adList.addAll(needClassList);
					
					this.cleanMulClassData(adList, isLastSeq);
					
					int  totalDiff = this.calculateTotalDiff(subjectId, subjectCells);
					if(totalDiff>=0) {
						selectData.putAll(subjectId, adList);
						return true;
					}else {
						//记录此次好的结果
						if(totalDiff > preMaxDiffValue) {
							preMaxDiffValue = totalDiff;
							preLinked = adList;
						}
						//还原数据
						this.recoverStudentCount(bakTable, this.table);
					}
				}
			}
		}
		
		//清值
		if(null == preLinked) throw new RuntimeException(seqIndex+"序列下科目"+subjectId+"未找到结果");
		System.out.println(seqIndex+"序列下科目"+subjectId+"使用负数结果");
		this.cleanMulClassData(preLinked, isLastSeq);
		selectData.putAll(subjectId, preLinked);
		return true;
	}
	
	

	
	
	/**
	 * 获取当前必须要使用的科目
	 * @param columns
	 * @param subjectCells
	 * @param subjectIndex
	 * @return
	 */
	public List<Cell> getNeedCells(Collection<Cell> columns, SeqCell[] remainCells) {
		
		List<Cell> unList = new ArrayList<Cell>();
		for (Cell learnCell : columns) {
			
			if(learnCell.getStudentCount() <= 0) continue;
			
			//计算当前科目人数在后续科目是否还可用
			int maxValue = 0;
			for (SeqCell cell : remainCells) {
				
				if(cell.getIsDiv()) continue;
				
				if(cell.getSubject().getId() == learnCell.getColumnKey()) continue;
				
				String subjectId = cell.getSubject().getId();
				Cell nextCell = this.table.get(learnCell.getRowKey(), subjectId);
				if(nextCell.getStudentCount()>maxValue)  maxValue=nextCell.getStudentCount();
				
			}
			
			if(maxValue<=0||learnCell.getStudentCount()>maxValue) unList.add(learnCell);
		}
		
		return unList;
	}
	
	
	/**
	 * 获取可以以行政班开班的数据
	 * @param columns
	 * @param subjectCells
	 * @param subjectIndex
	 * @return
	 */
	public ArrayListMultimap<String, Cell> getAdCells(Collection<Cell> columns) {
		
		ArrayListMultimap<String, Cell> adMutilMap = ArrayListMultimap.create();
		for (Cell learnCell : columns) {
			if(learnCell.getStudentCount()<=0) continue;
			String adClassId = learnCell.getRowKey().split(Utils.SEPARATOR_SYMBOL)[0];
			adMutilMap.put(adClassId, learnCell);
		}
		
		ArrayListMultimap<String, Cell> adDivMap = ArrayListMultimap.create();
		for (Entry<String, Collection<Cell>> adEntry : adMutilMap.asMap().entrySet()) {
			
			int total = 0;
			for (Cell learnCell : adEntry.getValue()) {
				total+=learnCell.getStudentCount();
			}
			
			//获取原始人数
			int orgCount = adId2StudentCountMap.get(adEntry.getKey());
			
			//记录可以行政开班数据
			if(total == orgCount) adDivMap.putAll(adEntry.getKey(), adEntry.getValue());
			
		}
		
		return adDivMap;
	}
	
	
	/**
	 * 获取其它的格子
	 * @param columns
	 * @param needCells
	 * @param adCell
	 * @return
	 */
	public List<Cell> getOtherCells(Collection<Cell> columns, List<Cell> needCells, Collection<Cell> adCells) {
		List<Cell> otherList = new ArrayList<Cell>();
		for (Cell learnCell : columns) {
			if(learnCell.getStudentCount()<=0) continue;
			if(needCells.contains(learnCell)) continue;
			if(adCells.contains(learnCell)) continue;
			otherList.add(learnCell);
		}
		return otherList;
	}
	
	
	/**
	 * 获取总差值，0证明可以成功开班
	 * @param subjectCells
	 * @param subjectIndex
	 * @param cellList
	 * @return
	 */
	public int calculateTotalDiff(String currSubjectId, SeqCell[] subjectCells) {
		
		Map<String, Integer> currSubjectId2StudentCountMap = this.getCurrentSubjectStudentCount();
		
		int total = 0;
		for (SeqCell seqCell : subjectCells) {
			
			if(seqCell.getIsDiv()) continue;
			
			if(seqCell.getSubject().getId() == currSubjectId) continue;
			
			int currStudentCount = currSubjectId2StudentCountMap.get(seqCell.getSubject().getId());
			
			Integer theoryStudentCount = seqCell.getSubject().getLearnAvgClassSize().multiply(new BigDecimal(seqCell.getClassCount())).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
			int diff = currStudentCount - theoryStudentCount;
			
			if(diff <=0) {
				total += diff;
			}
		}
		return total;
	}

}
