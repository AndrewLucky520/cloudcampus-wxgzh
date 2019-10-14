package com.talkweb.placementtask.utils.div.medium;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.talkweb.placementtask.utils.div.Divide;
import com.talkweb.placementtask.utils.div.TimeoutWatch;
import com.talkweb.placementtask.utils.div.Utils;
import com.talkweb.placementtask.utils.div.dto.Cell;
import com.talkweb.placementtask.utils.div.dto.CellStack;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.ClassResult.ClassType;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.ScoreResult;
import com.talkweb.placementtask.utils.div.dto.SeqCell;
import com.talkweb.placementtask.utils.div.dto.SignalParam;
import com.talkweb.placementtask.utils.div.dto.SignalParam.ResponseType;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.dto.Subject;
import com.talkweb.placementtask.utils.div.dto.SubjectGroup;
import com.talkweb.placementtask.utils.div.dto.TeachClassResult;
import com.talkweb.placementtask.utils.div.medium.dto.LayInfo;
import com.talkweb.placementtask.utils.div.medium.dto.MediumWishSubjectGroup;


/**
 * 分教学班级
 * @author hushow
 *
 */
public class TeachClassDiv extends Divide<List<ClassResult>>{
	
	/**
	 * 分层信息
	 */
	private LayInfo layInfo;
	
	/**
	 * 班级类型
	 */
	private ClassType classType;
	
	
	/**
	 * 响应类型
	 */
	private ResponseType responseType;
	
	/**
	 * 超时时间
	 */
	private Long seqTimeoutSecond;
	
	
	/**
	 * 学选序列编号
	 */
	private LinkedListMultimap<ClassType, Integer> seqIndexMultimap = LinkedListMultimap.create();
	
	/**
	 * 学班信息配置缓存
	 */
	private Map<String, Subject> teachSubjectId2SubjectMap = new HashMap<>();
	
	/**
	 * 最优开班数分布图
	 */
	Table<Integer, String, SeqCell> seqClassCountLayoutTable=HashBasedTable.create();
	
	
	public TeachClassDiv(DivContext divContext, LayInfo layInfo, ClassType classType, ResponseType responseType, Long seqTimeoutSecond) {
		
		super(divContext);
		
		this.responseType = responseType;

		this.layInfo = layInfo;
		
		this.classType = classType;
		
		seqIndexMultimap.put(ClassType.OPT, 1);
		seqIndexMultimap.put(ClassType.OPT, 2);
		seqIndexMultimap.put(ClassType.OPT, 3);
		seqIndexMultimap.put(ClassType.Pro, 4);
		seqIndexMultimap.put(ClassType.Pro, 5);
		seqIndexMultimap.put(ClassType.Pro, 6);
		
		this.seqTimeoutSecond = seqTimeoutSecond;
		
		//建立新的教学班信息配置
		for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
			Subject teachSubject = new Subject(subject.getId(), subject.getName());
			teachSubjectId2SubjectMap.put(subject.getId(), teachSubject);
		}
		
		//初始化分层志愿数据
		int totalStudentCount=0;
		for (String wishId : layInfo.getWishGroupIds()) {
			
			MediumWishSubjectGroup wish = (MediumWishSubjectGroup)divContext.getWishId2SubjectGroupMap().get(wishId);
			
			this.wishId2StudentMultimap.putAll(wish.getId(), new ArrayList<>(wish.getGoStudents()));
			
			for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
				
				Cell cell = new Cell(wish.getId(), subject.getId(), 0);
				//学考科目
				if(ClassType.Pro.equals(classType) && !wish.getIds().contains(subject.getId())) {
					cell.setStudentCount(wish.getGoStudentCount());
					cell.setStudents(new ArrayList<>(wish.getGoStudents()));
					Subject teachSubject = teachSubjectId2SubjectMap.get(subject.getId());
					teachSubject.setTotalStudentCount(teachSubject.getTotalStudentCount()+wish.getGoStudentCount());
				}else if(ClassType.OPT.equals(classType) && wish.getIds().contains(subject.getId()) ) {
					cell.setStudentCount(wish.getGoStudentCount());
					cell.setStudents(new ArrayList<>(wish.getGoStudents()));
					Subject teachSubject = teachSubjectId2SubjectMap.get(subject.getId());
					teachSubject.setTotalStudentCount(teachSubject.getTotalStudentCount()+wish.getGoStudentCount());
				}
				this.table.put(wish.getId(), subject.getId(), cell);
			}
			
			totalStudentCount+=wish.getGoStudentCount();
		}
		
		//计算科目开班数和班额
		for (Subject subject : teachSubjectId2SubjectMap.values()) {
			Integer subjectTotalStudentCount = subject.getTotalStudentCount();
			if(subjectTotalStudentCount<=0) continue;
			Double upClassCount = Math.ceil(subjectTotalStudentCount.doubleValue()/this.divContext.getMaxClassSize());
			BigDecimal avgSize = new BigDecimal(subjectTotalStudentCount.doubleValue()/upClassCount).setScale(2, BigDecimal.ROUND_HALF_UP);
			subject.setClassTotalCount(upClassCount.intValue());
			subject.setAvgClassSize(avgSize);
		}
		
		//计算开班分布
		this.initClassCountLayout();
		
		this.setTotalStudentCount(totalStudentCount);
		
	}
	
	
	/**
	 * 初始化最优班级数分布情况
	 */
	public void initClassCountLayout() {
		
		//初始化矩阵图
		for (Integer seqIndex : seqIndexMultimap.get(this.classType)) {
			for (Map.Entry<String, Subject> entry : this.teachSubjectId2SubjectMap.entrySet()) {
				seqClassCountLayoutTable.put(seqIndex, entry.getKey(), new SeqCell(seqIndex, entry.getValue()));
			}
		}
		
		//根据科目总人数计算最优班级分布
		calculateHourTable();
		
//		seqClassCountLayoutTable.put(1, "4", new SeqCell(1, this.teachSubjectId2SubjectMap.get("4"),0));
//		seqClassCountLayoutTable.put(1, "5", new SeqCell(1, this.teachSubjectId2SubjectMap.get("5"),0));
//		seqClassCountLayoutTable.put(1, "6", new SeqCell(1, this.teachSubjectId2SubjectMap.get("6"),1));
//		seqClassCountLayoutTable.put(1, "7", new SeqCell(1, this.teachSubjectId2SubjectMap.get("7"),1));
//		seqClassCountLayoutTable.put(1, "8", new SeqCell(1, this.teachSubjectId2SubjectMap.get("8"),1));
//		seqClassCountLayoutTable.put(1, "9", new SeqCell(1, this.teachSubjectId2SubjectMap.get("9"),0));
//		
//		seqClassCountLayoutTable.put(2, "4", new SeqCell(2, this.teachSubjectId2SubjectMap.get("4"),1));
//		seqClassCountLayoutTable.put(2, "5", new SeqCell(2, this.teachSubjectId2SubjectMap.get("5"),0));
//		seqClassCountLayoutTable.put(2, "6", new SeqCell(2, this.teachSubjectId2SubjectMap.get("6"),1));
//		seqClassCountLayoutTable.put(2, "7", new SeqCell(2, this.teachSubjectId2SubjectMap.get("7"),1));
//		seqClassCountLayoutTable.put(2, "8", new SeqCell(2, this.teachSubjectId2SubjectMap.get("8"),0));
//		seqClassCountLayoutTable.put(2, "9", new SeqCell(2, this.teachSubjectId2SubjectMap.get("9"),1));
//		
//		seqClassCountLayoutTable.put(3, "4", new SeqCell(3, this.teachSubjectId2SubjectMap.get("4"),1));
//		seqClassCountLayoutTable.put(3, "5", new SeqCell(3, this.teachSubjectId2SubjectMap.get("5"),0));
//		seqClassCountLayoutTable.put(3, "6", new SeqCell(3, this.teachSubjectId2SubjectMap.get("6"),0));
//		seqClassCountLayoutTable.put(3, "7", new SeqCell(3, this.teachSubjectId2SubjectMap.get("7"),1));
//		seqClassCountLayoutTable.put(3, "8", new SeqCell(3, this.teachSubjectId2SubjectMap.get("8"),1));
//		seqClassCountLayoutTable.put(3, "9", new SeqCell(3, this.teachSubjectId2SubjectMap.get("9"),1));
		
//		if(this.classType.equals(ClassType.OPT)) {
//			
//			LinkedListMultimap<Integer, Integer> seqCountMap = LinkedListMultimap.create();
//			
//			seqCountMap.putAll(1, Arrays.asList(1,0,0,1,0,0));
//			seqCountMap.putAll(2, Arrays.asList(0,0,0,1,0,1));
//			seqCountMap.putAll(3, Arrays.asList(0,0,1,0,1,0));
//			
//			for (Entry<Integer, Collection<Integer>> entry : seqCountMap.asMap().entrySet()) {
//				Integer subjectId=4;
//				for (Integer count : entry.getValue()) {
//					seqClassCountLayoutTable.put(entry.getKey(), subjectId.toString(), new SeqCell(entry.getKey(), this.teachSubjectId2SubjectMap.get(subjectId.toString()), count));
//					subjectId++;
//				}
//			}
//		}
		

	}
	
	
	public void printWishInfo() {
		for (Entry<String, Collection<Student>> entry : this.wishId2StudentMultimap.asMap().entrySet()) {
			System.out.println("wish:"+entry.getKey()+" 人数:"+entry.getValue().size());
		}
	}
	
	
	@Override
	public List<ClassResult> excuteDiv() {
		
		List<ClassResult> teachClassList = new ArrayList<>();
		
		System.out.println("==========================="+layInfo.getName()+"开始分["+this.classType.name()+"]教学班级,参与总人数:"+this.getTotalStudentCount());
		printWishInfo();
		printSeqHour();
		this.printInfo();

		//序列开班
		List<Integer> seqIndexList = seqIndexMultimap.get(this.classType);
		
		//记录上序列志愿信息
		ArrayListMultimap<String, Student> preParentWishId2StudentMultimap = ArrayListMultimap.create(this.wishId2StudentMultimap);
		
		for (Integer seqIndex : seqIndexList) {
			
			boolean isLastSeq = (seqIndex==seqIndexList.get(seqIndexList.size()-1));
			
			//备份数据
			TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
			
			ScoreResult bestScoreResult = divSeqTeach(seqIndex, isLastSeq);
			
			//还原数据
			this.recoverStudentCount(bakTable, this.table);
			
			//拆减志愿人数
			this.seqSplitWish(bestScoreResult.getClassResultList());
			
			//打印序列结果
			this.seqPrint(seqIndex, bestScoreResult);
			
			//为序列提人
			preParentWishId2StudentMultimap = extractStudent(bestScoreResult.getClassResultList(), preParentWishId2StudentMultimap);
			
			
			teachClassList.addAll(bestScoreResult.getClassResultList());
			
			//标记序列单元格已用完
			for (SeqCell seqCell : this.seqClassCountLayoutTable.row(seqIndex).values()) {
				seqCell.setIsDiv(true);
			}
		}
		
		return teachClassList;
	}
	
	
	public ScoreResult divSeqTeach(int seqIndex, boolean isLastSeq) {

		System.out.println("***************************"+layInfo.getName()+"层执行分班序列:"+seqIndex);
		
		//循环序列下所有科目,计算可用科目
		List<Subject> subjectList = new ArrayList<>();
		for (Subject subject :  this.teachSubjectId2SubjectMap.values()) {
			if(subject.getTotalStudentCount()<=0) continue;
			if(this.seqClassCountLayoutTable.get(seqIndex, subject.getId()).getClassCount() <=0) {
				for (Cell cell : this.table.column(subject.getId()).values()) {
					cell.setStudentCount(0);
				}
				continue;
			}
			subjectList.add(subject);
		}
		
		Subject[] subjects = subjectList.toArray(new Subject[subjectList.size()]);
		
		List<ScoreResult> scoreList = new ArrayList<>();
		
		//序列分班前，计算科目下人数必需(下个序列开班数为0了)
		//计算科目每个剩下序列的开班数
		 HashMultiset<String> subjectId2ClassCount = HashMultiset.create();
		for (Integer nextSeqIndex : this.seqIndexMultimap.get(this.classType)) {
			if(nextSeqIndex<=seqIndex) continue;
			for (SeqCell seqCell : this.seqClassCountLayoutTable.row(nextSeqIndex).values()) {
				subjectId2ClassCount.add(seqCell.getSubject().getId(), seqCell.getClassCount());
			}
		}
		
		for (com.google.common.collect.Table.Cell<String, String, Cell> cell : this.table.cellSet()) {
			String subjectId = cell.getColumnKey();
			int remainCount = subjectId2ClassCount.count(subjectId);
			if(remainCount<=0) {
				cell.getValue().setIsNeed(true);
			}
		}
		
		//序列开始前，记录科目人数
		Map<String, Integer> subjectId2StudentCountMap = this.getCurrentSubjectStudentCount();
		
		
		SignalParam signalParam = new SignalParam(null, responseType);
		signalParam.setTimeoutSecond(this.seqTimeoutSecond);
		Long startTime = System.currentTimeMillis();
		this.divSeqTeachRecursion(subjects, 0, seqIndex, isLastSeq, new LinkedHashMap<String, List<LinkedList<Cell>>>(),
				scoreList, subjectId2StudentCountMap, signalParam);
		//排序
		System.out.println("一共解个数:"+scoreList.size()+" 花费时间:"+((System.currentTimeMillis()-startTime)/1000)+"秒");
		Collections.sort(scoreList, new Comparator<ScoreResult>() {
			public int compare(ScoreResult o1, ScoreResult o2) {
				return o1.getScore().compareTo(o2.getScore());
			};
		});
		
		//打印前10个最优解
//		int n=0;
//		for (ScoreResult scoreResult : scoreList) {
//			if(n>50) break;
//			System.out.println("第"+n);
//			scoreResult.printScoreInfo(this.divContext);
//			n++;
//		}
//		
		if(scoreList.size()<=0) {
			throw new RuntimeException(this.layInfo.getName()+"["+seqIndex+"]序列计算分班结果无解");
		}
		ScoreResult bestScoreResult = null;
//		if(seqIndex == 1 && layInfo.getId().equals(1)) {
//			bestScoreResult= scoreList.get(21);
//		}else {
//			bestScoreResult = scoreList.get(0);
//		}
		
		bestScoreResult = scoreList.get(0);
		
		System.out.println("选中最好结果:");
		bestScoreResult.printScoreInfo(this.divContext);
		
		return bestScoreResult;
	}
	
	
	public void seqPrint(Integer seqIndex, ScoreResult bestScoreResult) {
		
		System.out.println(this.layInfo.getName()+"["+seqIndex+"]序列恢复后拆减人结果");
		this.printInfo();
		
		System.out.println(this.layInfo.getName()+"["+seqIndex+"]序列开班结果");
		int seqTotal = 0;
		ArrayListMultimap<String, Integer> rowKey2CountMap = ArrayListMultimap.create();
		for (ClassResult classEntiry : bestScoreResult.getClassResultList()) {
			
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
		
		System.out.println(seqIndex+"序列总人数:"+seqTotal+"分类["+layInfo.getName()+"]参与分班总人数："+ this.getTotalStudentCount());

		if(seqTotal!=this.getTotalStudentCount()) {
			
			for (Entry<String, Collection<Integer>> rowEntry : rowKey2CountMap.asMap().entrySet()) {
				int tCount = 0;
				for (Integer count : rowEntry.getValue()) {
					tCount+=count;
				}
				System.out.println(rowEntry.getKey()+" 下总人数:"+tCount);
			}
			
			throw new RuntimeException(seqIndex+"序列分班失败人数不对，序列总人数:"+seqTotal+"分类["+layInfo.getName()+"]参与分班总人数："+this.getTotalStudentCount());
		}
		
	}
	
	
	
	public boolean divSeqTeachRecursion(Subject[] subjects, int subjectIndex, int seqIndex, boolean isLastSeq,
			LinkedHashMap<String, List<LinkedList<Cell>>> selectData, List<ScoreResult> scoreList,
			Map<String, Integer> subjectId2StudentCountMap, SignalParam signalParam) {
		
		if(subjectIndex>=subjects.length) {
			
			//if(this.calculateRemainCount()>0) return true;
			System.out.println(this.getLayInfo().getName()+"["+seqIndex+"]序列开完所有列,班级结果:"+selectData.toString());
			
			//开班级
			List<ClassResult> divClassList = new ArrayList<>();
			for (String subjectId : selectData.keySet()) {
				for (LinkedList<Cell> cells : selectData.get(subjectId)) {
					TeachClassResult teachClass = new TeachClassResult(divContext, subjectId, cells);
					teachClass.setSeqId(seqIndex);
					divClassList.add(teachClass);
				}
			}
			
			ScoreResult scoreResult = this.getScore(divContext, divClassList);
			if(null == scoreResult) {
				return false;
			}
			
			Iterator<String> it = selectData.keySet().iterator();
			while (it.hasNext()) {
				scoreResult.getSubjectOrderList().add(it.next());
			}
			
			scoreResult.setRemainTable(this.cloneStudentCount(this.table));
			scoreList.add(scoreResult);
			
			System.out.println("计算分班最优解："+scoreList.size());
			
			return true;
		}
		
		if(ResponseType.FAST.equals(signalParam.getResponseType()) && signalParam.isForceOut()) {
			System.out.println("FAST返回结果");
			return true;
		}else if(ResponseType.TIMEOUT.equals(signalParam.getResponseType()) && signalParam.isTimeout()){
			System.out.println("TIMEOUT返回结果");
			return true;
		}
		
		Set<String> unUsedColumnKeySet = new HashSet<>();
		for (int i=subjectIndex;i<subjects.length;i++) {
			unUsedColumnKeySet.add(subjects[i].getId());
		}
		
		//计算当前科目剩余人数
		Map<String, Integer> currSubjectId2StudentCountMap = this.getCurrentSubjectStudentCount();
		
		//科目排序
		for(int i=subjectIndex;i<subjects.length;i++) {
			for(int j=i+1;j<subjects.length;j++) {
				Subject subject_i = subjects[i];
				Subject subject_j = subjects[j];
				
				int minUsableColumnCount_i = this.calculateMinUsableColumnCount(subject_i.getId(), unUsedColumnKeySet);
				int minUsableColumnCount_j = this.calculateMinUsableColumnCount(subject_j.getId(), unUsedColumnKeySet);
				
				int classCount_i = seqClassCountLayoutTable.get(seqIndex, subject_i.getId()).getClassCount();
				int classCount_j = seqClassCountLayoutTable.get(seqIndex, subject_j.getId()).getClassCount();

				Integer curr_count_i = currSubjectId2StudentCountMap.get(subject_i.getId());
				Integer curr_count_j = currSubjectId2StudentCountMap.get(subject_j.getId());
				
				Integer count_i = subject_i.getAvgClassSize().multiply(new BigDecimal(classCount_i)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
				Integer count_j = subject_j.getAvgClassSize().multiply(new BigDecimal(classCount_j)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
				
				int value_i = curr_count_i - count_i;
				int value_j = curr_count_j - count_j;
				
            	if(minUsableColumnCount_i==minUsableColumnCount_j) {
    				if(value_i>value_j){
    					Subject temp=subjects[i];
    					subjects[i]=subjects[j];
    					subjects[j]=temp;
    				}
            	}else {
    				if(minUsableColumnCount_i>minUsableColumnCount_j){
    					Subject temp=subjects[i];
    					subjects[i]=subjects[j];
    					subjects[j]=temp;
    				}
            	}
			}
		}

		Subject[] remainSubjects = Arrays.copyOfRange(subjects, subjectIndex+1, subjects.length);
		
		//获取当前科目
		Subject currSubject = subjects[subjectIndex];
		String currSubjectId = currSubject.getId();

		//当前科目当前序列必须开班
		Integer mustClassNum = this.seqClassCountLayoutTable.get(seqIndex, currSubjectId).getClassCount();
		if(mustClassNum ==0) {
			this.divSeqTeachRecursion(subjects, subjectIndex++, seqIndex, isLastSeq, selectData, scoreList,
					subjectId2StudentCountMap, signalParam);
			return true;
		}
		
		//计算科目剩下开班数
		int remainClassCount = 0;
		for (SeqCell seqCell : this.seqClassCountLayoutTable.column(currSubjectId).values()) {
			if(seqCell.getIsDiv()) continue;
			remainClassCount+=seqCell.getClassCount();
		}
		if(remainClassCount ==0) return true;
		
		int avgClassSize = currSubject.getAvgClassSize().setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

		Collection<Cell> columns = this.getColumnValidCells(currSubjectId);
		if(isLastSeq) {
			
			int subjectCurrStudentCount = currSubjectId2StudentCountMap.get(currSubjectId);
			
			//备份数据
			TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
			
			Double d = Math.ceil(subjectCurrStudentCount/mustClassNum.floatValue());
			List<LinkedList<Cell>> splitList = Utils.splitSubClass(columns, d.intValue());
			
			this.cleanMulClassData(splitList, isLastSeq);
		
			selectData.put(currSubjectId, splitList);
			
			this.divSeqTeachRecursion(subjects, subjectIndex + 1, seqIndex, isLastSeq, selectData, scoreList,
					subjectId2StudentCountMap, signalParam);
			
			//还原数据
			selectData.remove(currSubjectId);
			this.recoverStudentCount(bakTable, this.table);
			return true;
		}

		//获取必须要使用的科目
		List<Cell> needCells = this.getNeedCells(seqIndex, columns, remainSubjects);
		
		//获取其它的项
		List<Cell> otherCells  = this.getOtherCells(columns, needCells);

		LinkedList<Cell> defaultSelect = new LinkedList<Cell>(needCells);
		List<CellStack> cellStackList = Utils.generateSumCombineArrange(otherCells.toArray(new Cell[otherCells.size()]),
				defaultSelect, avgClassSize * mustClassNum, new TimeoutWatch(signalParam.getTimeoutSecond()));
		
		for (CellStack cellStack : cellStackList) {
			
			if(ResponseType.FAST.equals(signalParam.getResponseType()) && signalParam.isForceOut()) {
				System.out.println("FAST返回结果");
				return true;
			}else if(ResponseType.TIMEOUT.equals(signalParam.getResponseType()) && signalParam.isTimeout()){
				System.out.println("TIMEOUT返回结果");
				return true;
			}
			
			//备份数据
			TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
			LinkedList<Cell> selectLinked = Utils.splitClass(cellStack, table);
			List<LinkedList<Cell>> classList = Utils.splitSubClass(selectLinked, avgClassSize);
			this.cleanMulClassData(classList, isLastSeq);
			selectData.put(currSubjectId, classList);

			//计算科目剩下开班数
			int remainCount = 0;
			for (SeqCell seqCell : this.seqClassCountLayoutTable.column(currSubjectId).values()) {
				if(seqCell.getIsDiv()) continue;
				if(seqCell.getSeqId()==seqIndex) continue;
				remainCount+=seqCell.getClassCount();
			}
			
			//如果科目人数未开完且剩余序列还剩余开班数
			if(this.calculateRemainCountByColumn(currSubjectId)>0 && remainCount<=0) {
				System.out.println(currSubjectId+"人数未开完，但还剩余开班数"+remainCount);
			}else {
				this.divSeqTeachRecursion(subjects, subjectIndex + 1, seqIndex, isLastSeq, selectData, scoreList,
						subjectId2StudentCountMap, signalParam);

			}
			
			//还原数据
			selectData.remove(currSubjectId);
			this.recoverStudentCount(bakTable, this.table);
			
		}
		
		List<Cell> allCells = new ArrayList<>(needCells);
		allCells.addAll(otherCells);
		
		int needCount = 0;
		for (Cell cell : needCells) {
			needCount+=cell.getStudentCount();
		}
		if(cellStackList.size()<=0 && allCells.size()>0) {
			
			//备份数据
			TreeBasedTable<String, String, Integer> bakTable = this.cloneStudentCount(this.table);
			
			List<LinkedList<Cell>> lastClassList = null;
			if(needCount>=avgClassSize*mustClassNum) {
				Double d = Math.ceil(needCount/mustClassNum.floatValue());
				 lastClassList = Utils.splitSubClass(needCells, d.intValue());
			}else {
				lastClassList = Utils.splitSubClass(allCells, avgClassSize);
			}
			
			this.cleanMulClassData(lastClassList, isLastSeq);
			selectData.put(currSubjectId, lastClassList);

			//计算科目剩下开班数
			int remainCount = 0;
			for (SeqCell seqCell : this.seqClassCountLayoutTable.column(currSubjectId).values()) {
				if(seqCell.getIsDiv()) continue;
				if(seqCell.getSeqId()==seqIndex) continue;
				remainCount+=seqCell.getClassCount();
			}
			
			//如果科目人数未开完，判断是否满足有剩余开班数
			if(this.calculateRemainCountByColumn(currSubjectId)>0 && remainCount<=0) {
				System.out.println(currSubjectId+"人数未开完，但还剩余开班数"+remainCount);
			}else {
				this.divSeqTeachRecursion(subjects, subjectIndex + 1, seqIndex, isLastSeq, selectData, scoreList,
						subjectId2StudentCountMap, signalParam);
			}
			
			//还原数据
			selectData.remove(currSubjectId);
			this.recoverStudentCount(bakTable, this.table);
		}
		
		return false;
	}
	
	
	
	/**
	 * 获取其它的格子
	 * @param columns
	 * @param needCells
	 * @param adCell
	 * @return
	 */
	public List<Cell> getOtherCells(Collection<Cell> columns, List<Cell> needCells) {
		List<Cell> otherList = new ArrayList<Cell>();
		for (Cell learnCell : columns) {
			if(learnCell.getStudentCount()<=0) continue;
			if(needCells.contains(learnCell)) continue;
			otherList.add(learnCell);
		}
		return otherList;
	}
	
	/**
	 * 获取当前必须要使用的科目
	 * @param columns
	 * @param subjectCells
	 * @param subjectIndex
	 * @return
	 */
	public List<Cell> getNeedCells(int seqIndex, Collection<Cell> columns, Subject[] remainSubjects) {
		
		List<Cell> unList = new ArrayList<Cell>();
		for (Cell learnCell : columns) {
			
			if(learnCell.getStudentCount() <= 0) continue;
			
			if(learnCell.getIsNeed()) {
				unList.add(learnCell);
				continue;
			}
			
			//计算当前科目人数在后续科目是否还可用
			int maxValue = 0;
			for (Subject subject : remainSubjects) {
				
				SeqCell cell = this.seqClassCountLayoutTable.get(seqIndex, subject.getId());
				
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
	
	
	public void  calculateHourTable() {
		//初始化矩阵科目开班分布
		int preIndex = 0;
		//循环科目
		for (Subject subject : this.teachSubjectId2SubjectMap.values()) {
			int classNumber = subject.getClassTotalCount();
			Map<Integer, SeqCell> seqMap =  seqClassCountLayoutTable.column(subject.getId());
			Integer[] seqArray = seqMap.keySet().toArray(new Integer[seqMap.keySet().size()]);
			int i=preIndex>=seqMap.keySet().size()?0:preIndex;
			//循环序列
			while(i<seqArray.length && classNumber > 0) {
				SeqCell seqCell = seqMap.get(seqArray[i]);
				seqCell.setClassCount(seqCell.getClassCount()+1);
				classNumber--;
				if(i==seqArray.length-1 && classNumber>0) {
					i=0;
				}else {
					i++;
				}
			}
			preIndex = i;
		}
	}
	
	
	/**
	 * 打分
	 * @param divContext
	 * @param divAdClassList
	 * @return
	 */
	public ScoreResult getScore(DivContext divContext, List<ClassResult> divClassList) {
		
		ScoreResult score = new ScoreResult();
		score.setClassResultList(divClassList);
		
		Double adVarianceValue = Utils.calculateVariance(divClassList, null);
		score.setScore(adVarianceValue);
		
		return score;
	}
	
	
	/**
	 * 打印科目序列课时分布图
	 */
	public void  printSeqHour() {
		
		StringBuffer title = new StringBuffer("      ");
		StringBuffer avgClassSizeStr = new StringBuffer("平均班额  ");
		StringBuffer classTotalStr  = new StringBuffer("科目班数  ");
		for (Subject subject : this.teachSubjectId2SubjectMap.values()) {
			title.append("   "+subject.getName()+"("+subject.getTotalStudentCount()+")");
			avgClassSizeStr.append("  "+subject.getAvgClassSize());
			classTotalStr.append("  "+subject.getClassTotalCount());
		}
		System.out.println(title.toString());
		
		for (Map.Entry<Integer, Map<String, SeqCell>> rowEntry : this.seqClassCountLayoutTable.rowMap().entrySet()) {
			StringBuffer sb = new StringBuffer(rowEntry.getKey()+"序列       ");
			for (Subject subject : this.teachSubjectId2SubjectMap.values()) {
				SeqCell cell = rowEntry.getValue().get(subject.getId());
				sb.append("  "+cell.getClassCount());
			}
			System.out.println(sb.toString());
		}
		
		System.out.println(classTotalStr);
		System.out.println(avgClassSizeStr);
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
		for (Subject  subject : this.teachSubjectId2SubjectMap.values()) {
			
			title1.append("  "+subject.getName());
			title.append("  00"+subject.getId());
			studentTotalCount.append("  "+Utils.fillStr(subject.getTotalStudentCount(), 3));
			classTotalCount.append("  "+Utils.fillStr(subject.getClassTotalCount(), 3));
			avgClassCount.append("  "+subject.getAvgClassSize());
		}
		System.out.println(title1.toString());
		System.out.println(title.toString());
		
		for (Entry<String, Map<String, Cell>> rowEntry : this.table.rowMap().entrySet()) {
			String  wishId = Utils.extractWishSubjectGroupId(rowEntry.getKey());
			SubjectGroup wishSubjectGroup = this.divContext.getWishId2SubjectGroupMap().get(wishId);
			StringBuffer sb = new StringBuffer(wishSubjectGroup.getName()+rowEntry.getKey());
			for (Subject  subject : this.teachSubjectId2SubjectMap.values()) {
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


	public LayInfo getLayInfo() {
		return layInfo;
	}


	public void setLayInfo(LayInfo layInfo) {
		this.layInfo = layInfo;
	}
	
}
