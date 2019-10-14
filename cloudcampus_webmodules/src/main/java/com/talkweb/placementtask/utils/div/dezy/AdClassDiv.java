package com.talkweb.placementtask.utils.div.dezy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.TreeBasedTable;
import com.talkweb.placementtask.utils.div.Divide;
import com.talkweb.placementtask.utils.div.TimeoutWatch;
import com.talkweb.placementtask.utils.div.Utils;
import com.talkweb.placementtask.utils.div.dezy.dto.AdClassResult;
import com.talkweb.placementtask.utils.div.dezy.dto.AdScoreResult;
import com.talkweb.placementtask.utils.div.dto.Cell;
import com.talkweb.placementtask.utils.div.dto.CellStack;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.ScoreResult;
import com.talkweb.placementtask.utils.div.dto.SignalParam;
import com.talkweb.placementtask.utils.div.dto.SignalParam.ProcessType;
import com.talkweb.placementtask.utils.div.dto.SignalParam.ResponseType;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.dto.Subject;
import com.talkweb.placementtask.utils.div.dto.SubjectGroup;

/**
 * 分行政班
 * @author hushow
 *
 */
public class AdClassDiv extends Divide<ScoreResult>{
	
	/**
	 * 处理信号参 数
	 */
	private SignalParam  signalParam;
	
	
	public AdClassDiv(DivContext divContext, ProcessType processType, ResponseType responseType) {
		
		super(divContext);
		
		signalParam = new SignalParam(processType, responseType);
		
		//初始化矩阵图
		for(SubjectGroup wishSubjectGroup: this.divContext.getWishSubjectGroupList()) {
			for (SubjectGroup fixTwoSubjectGroup : this.divContext.getFixTwoSubjectGroupList()) {
				int count = 0;
				if(wishSubjectGroup.contains(fixTwoSubjectGroup.getSubjects())) {
					count = wishSubjectGroup.getStudentCount();
				}
				table.put(wishSubjectGroup.getId(), fixTwoSubjectGroup.getId(), new Cell(wishSubjectGroup.getId(), fixTwoSubjectGroup.getId(), count));
			}
		}
	}
	
	
	@Override
	public ScoreResult excuteDiv() {
		
		System.out.println("==================开始分行政班:");
		this.printInfo();
		
		List<SubjectGroup> sgList = divContext.getFixTwoSubjectGroupList();
		//sgList = sgList.subList(0,15);
		ArrayListMultimap<String, LinkedList<Cell>> adSelectData  = ArrayListMultimap.create();
		ArrayListMultimap<String, Integer> logMap  = ArrayListMultimap.create();
		Long startTime = System.currentTimeMillis();
		//sz = Arrays.copyOfRange(sz, 0, 10);
		List<ScoreResult> scoreList = new ArrayList<>();
		this.divAdClassBacktrace(new ArrayList<SubjectGroup>(sgList), adSelectData, logMap, scoreList);
		System.out.println("行政班计算花费时间:"+((System.currentTimeMillis()-startTime)/1000)+"秒");
		
		//排序
		System.out.println("一共解个数:"+scoreList.size());
		Collections.sort(scoreList, new Comparator<ScoreResult>() {
			public int compare(ScoreResult o1, ScoreResult o2) {
				return o1.getScore().compareTo(o2.getScore());
			};
		});
		
		//打印前20个最优解
		int num=20;
		for (ScoreResult scoreResult : scoreList) {
			if(num<=0) break;
			scoreResult.printScoreInfo(this.divContext);
			num--;
		}
		
		for (SubjectGroup subjectGroup : sgList) {
			int total = 0;
			for (Integer count : logMap.get(subjectGroup.getId())) {
				total+=count;
			}
			System.out.println(subjectGroup.getId()+"循环次数："+total);
		}
		
		if(scoreList.size()<=0) {
			return new AdScoreResult();
		}
		
		ScoreResult adScoreResult = scoreList.get(0);
		
		//设置科目下行政班数
		for (ClassResult divClass : adScoreResult.getClassResultList()) {
			AdClassResult adClass = (AdClassResult)divClass;
			SubjectGroup fixTwoSubjectGroup = adClass.getFixTwoSubjectGroup();
			for (Subject subject : fixTwoSubjectGroup.getSubjects()) {
				subject.setAdClassTotalCount(subject.getAdClassTotalCount()+1);
			}
		}
		
		//行政班分人
		//循环行政班
		Map<String, ArrayList<Student>> wishId2StudentsMap = new HashMap<String, ArrayList<Student>>(); 
		for (SubjectGroup wishSubjectGroup : divContext.getWishSubjectGroupList()) {
			wishId2StudentsMap.put(wishSubjectGroup.getId(), new ArrayList<Student>(wishSubjectGroup.getStudents()));
		}
		
		for (ClassResult classResult : adScoreResult.getClassResultList()) {
			
			AdClassResult adClassResult = (AdClassResult)classResult;
			
			for (SubjectGroup wishSubjectGroup : adClassResult.getWishSubjectGroups()) {
				int needCount = adClassResult.getWishId2StudentCountMap().get(wishSubjectGroup.getId());

				List<Student> srcStudents = wishId2StudentsMap.get(wishSubjectGroup.getId());
				
				if(needCount > srcStudents.size()) {
					throw new RuntimeException(wishSubjectGroup.getName()+" 分班失败，超出志愿人数， needCount:"+ needCount+" srcStudents.size:"+srcStudents.size());
				}
				
				List<Student> extractStudents = srcStudents.subList(0, needCount);				
				adClassResult.getWishId2StudentList().putAll(wishSubjectGroup.getId(), new ArrayList<Student>(extractStudents));
				
				srcStudents.removeAll(extractStudents);
			}
		}
		
		//建立行政班索引
		divContext.addAdClassResult(adScoreResult.getClassResultList());
		
		return adScoreResult;
	}
	
	
	/**
	 * 打印数据
	 */
	@Override
	public void  printInfo() {
		
		StringBuffer title1 = new StringBuffer("       ");
		StringBuffer title = new StringBuffer("       ");
		StringBuffer studentCount = new StringBuffer("学生 数            ");
		StringBuffer avgClassSize = new StringBuffer("平均班额数     ");
		for (Map<String, Cell>  map : this.table.columnMap().values()) {
			SubjectGroup fixTwoSubjectGroup = this.divContext.getFixTwoId2fixTwoSubjectGroup().get(map.values().iterator().next().getColumnKey());
			title1.append("  "+fixTwoSubjectGroup.getName());
			title.append("   "+fixTwoSubjectGroup.getId());
			
			String studentCountstr = String.valueOf(fixTwoSubjectGroup.getStudentCount());
			int x= 3-studentCountstr.toCharArray().length;
			if(x>0) {
				for(int i=0;i<x;i++) {
					studentCountstr="0"+studentCountstr;
				}
			}
			studentCount.append("  "+studentCountstr);
			
			String avgClassSizestr = String.valueOf(fixTwoSubjectGroup.getAvgClassSize());
			int x2= 3-avgClassSizestr.toCharArray().length;
			if(x2>0) {
				for(int i=0;i<x2;i++) {
					avgClassSizestr="0"+avgClassSizestr;
				}
			}
			avgClassSize.append("  "+avgClassSizestr);
		}
		System.out.println(title1.toString());
		System.out.println(title.toString());
		
		for (Map.Entry<String, Map<String, Cell>> rowEntry : table.rowMap().entrySet()) {
			SubjectGroup wishSubjectGroup = this.divContext.getWishId2SubjectGroupMap().get(rowEntry.getKey());
			StringBuffer sb = new StringBuffer(wishSubjectGroup.getName()+wishSubjectGroup.getId());
			for (Map<String, Cell>  map : this.table.columnMap().values()) {
				SubjectGroup fixTwoSubjectGroup = this.divContext.getFixTwoId2fixTwoSubjectGroup().get(map.values().iterator().next().getColumnKey());
				Cell cell = rowEntry.getValue().get(fixTwoSubjectGroup.getId());
				
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
		
		System.out.println(studentCount.toString());
		System.out.println(avgClassSize.toString());
	}
	
	
	/**
	 * 打印数据
	 */
	public void  printInfoSz(List<SubjectGroup> sgs) {
		
		StringBuffer title1 = new StringBuffer("       ");
		StringBuffer title = new StringBuffer("       ");
		StringBuffer studentCount = new StringBuffer("学生 数            ");
		StringBuffer avgClassSize = new StringBuffer("平均班额数     ");
		for (SubjectGroup  fixTwoSubjectGroup : sgs) {
			title1.append("  "+fixTwoSubjectGroup.getName());
			title.append("   "+fixTwoSubjectGroup.getId());
			
			String studentCountstr = String.valueOf(fixTwoSubjectGroup.getStudentCount());
			int x= 3-studentCountstr.toCharArray().length;
			if(x>0) {
				for(int i=0;i<x;i++) {
					studentCountstr="0"+studentCountstr;
				}
			}
			studentCount.append("  "+studentCountstr);
			
			String avgClassSizestr = String.valueOf(fixTwoSubjectGroup.getAvgClassSize());
			int x2= 3-avgClassSizestr.toCharArray().length;
			if(x2>0) {
				for(int i=0;i<x2;i++) {
					avgClassSizestr="0"+avgClassSizestr;
				}
			}
			avgClassSize.append("  "+avgClassSizestr);
		}
		System.out.println(title1.toString());
		System.out.println(title.toString());
		
		
		for (Map.Entry<String, Map<String, Cell>> rowEntry : table.rowMap().entrySet()) {
			SubjectGroup wishSubjectGroup = this.divContext.getWishId2SubjectGroupMap().get(rowEntry.getKey());
			StringBuffer sb = new StringBuffer(wishSubjectGroup.getName()+wishSubjectGroup.getId());

			for (SubjectGroup sg : sgs) {
				
				Cell cell = rowEntry.getValue().get(sg.getId());
				
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
		
		System.out.println(studentCount.toString());
		System.out.println(avgClassSize.toString());
	}
	
	/**
	 * 打印数据
	 */
	public void  printInfoSz(SubjectGroup[] sgs) {
		
		StringBuffer title1 = new StringBuffer("       ");
		StringBuffer title = new StringBuffer("       ");
		StringBuffer studentCount = new StringBuffer("学生 数            ");
		StringBuffer avgClassSize = new StringBuffer("平均班额数     ");
		for (SubjectGroup  fixTwoSubjectGroup : sgs) {
			title1.append("  "+fixTwoSubjectGroup.getName());
			title.append("   "+fixTwoSubjectGroup.getId());
			
			String studentCountstr = String.valueOf(fixTwoSubjectGroup.getStudentCount());
			int x= 3-studentCountstr.toCharArray().length;
			if(x>0) {
				for(int i=0;i<x;i++) {
					studentCountstr="0"+studentCountstr;
				}
			}
			studentCount.append("  "+studentCountstr);
			
			String avgClassSizestr = String.valueOf(fixTwoSubjectGroup.getAvgClassSize());
			int x2= 3-avgClassSizestr.toCharArray().length;
			if(x2>0) {
				for(int i=0;i<x2;i++) {
					avgClassSizestr="0"+avgClassSizestr;
				}
			}
			avgClassSize.append("  "+avgClassSizestr);
		}
		System.out.println(title1.toString());
		System.out.println(title.toString());
		
		
		for (Map.Entry<String, Map<String, Cell>> rowEntry : table.rowMap().entrySet()) {
			SubjectGroup wishSubjectGroup = this.divContext.getWishId2SubjectGroupMap().get(rowEntry.getKey());
			StringBuffer sb = new StringBuffer(wishSubjectGroup.getName()+wishSubjectGroup.getId());

			for (SubjectGroup sg : sgs) {
				
				Cell cell = rowEntry.getValue().get(sg.getId());
				
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
		
		System.out.println(studentCount.toString());
		System.out.println(avgClassSize.toString());
	}
	
	
	
	public boolean divAdClassBacktrace(List<SubjectGroup> fixTwoSubjectGroups,
			ArrayListMultimap<String, LinkedList<Cell>> selectData, ArrayListMultimap<String, Integer> logMap, List<ScoreResult> scoreList) {
		
		if(ResponseType.FAST.equals(signalParam.getResponseType()) && signalParam.isForceOut()) {
			System.out.println("FAST返回结果");
			return true;
		}else if(ResponseType.TIMEOUT.equals(signalParam.getResponseType()) && signalParam.isTimeout()){
			System.out.println("TIMEOUT返回结果");
			return true;
		}

		int total = 0;
		Set<String> tmpUnUsedColumnKeySet = new HashSet<>();
		for (int i=0;i<fixTwoSubjectGroups.size();i++) {
			SubjectGroup nextfixTwoSubjectGroup = fixTwoSubjectGroups.get(i);
			total += this.getColumnAbleAssignStudentCount(nextfixTwoSubjectGroup.getId());
			tmpUnUsedColumnKeySet.add(nextfixTwoSubjectGroup.getId());
		}

		//需要删除的
		List<SubjectGroup> deleteFixTwoSubjectGroups = fixTwoSubjectGroups.stream().filter(p->{
			Integer aValue = getColumnAbleAssignStudentCount(p.getId());
			Float fCount = aValue.floatValue()/p.getAvgClassSize();
			Integer minUsableColumnCount = calculateMinUsableColumnCount(p.getId(), tmpUnUsedColumnKeySet);
			return (fCount<0.6f && minUsableColumnCount>1) || aValue<=0;
		}).sorted((c1,c2)-> {
			Integer c1Count = getColumnAbleAssignStudentCount(c1.getId());
			Integer c2Count = getColumnAbleAssignStudentCount(c2.getId());
			return c1Count.compareTo(c2Count);
		}).collect(Collectors.toList());
		
		
		for (SubjectGroup deleteSg : deleteFixTwoSubjectGroups) {
			Integer minUsableColumnCount = calculateMinUsableColumnCount(deleteSg.getId(), tmpUnUsedColumnKeySet);
			if(minUsableColumnCount>1) {
				fixTwoSubjectGroups.remove(deleteSg);
				tmpUnUsedColumnKeySet.remove(deleteSg.getId());
			}
		}
		
		Set<String> unUsedColumnKeySet = fixTwoSubjectGroups.stream().map(m->m.getId()).collect(Collectors.toSet());

		//最后情况,开始对结果打分
		if(total == 0 || fixTwoSubjectGroups.size()<=0) {
			
			System.out.println("开完所有列=======行政班结果:"+selectData.size());
			
			//所有行政班级
			List<ClassResult> divAdClassList = new ArrayList<>();
			for (String fixTwoId : selectData.keySet()) {
				for (LinkedList<Cell> cells : selectData.get(fixTwoId)) {
					AdClassResult divAdClass = new AdClassResult(divContext, divContext.getFixTwoId2fixTwoSubjectGroup().get(fixTwoId), cells);
					divAdClassList.add(divAdClass);
				}
			}

			ScoreResult scoreResult = Utils.getScore(divContext, divAdClassList);
			if(null == scoreResult) {
				return false;
			}
			
			scoreResult.setRemainTable(this.cloneStudentCount(this.table));
			scoreList.add(scoreResult);
			
			System.out.println("计算分班最优解："+scoreList.size());
			
			if(ResponseType.FAST.equals(signalParam.getResponseType())) {
				signalParam.setForceOut(true);
				return true;
			}
			
			return true;
		}
		

		SubjectGroup[] remainSg = fixTwoSubjectGroups.toArray(new SubjectGroup[fixTwoSubjectGroups.size()]);
		
		fixTwoSubjectGroups.sort(new Comparator<SubjectGroup>() {
			
			public int compare(SubjectGroup subject_i, SubjectGroup subject_j) {
				Integer minUsableColumnCount_i = calculateMinUsableColumnCount(subject_i.getId(), unUsedColumnKeySet);
				Integer minUsableColumnCount_j = calculateMinUsableColumnCount(subject_j.getId(), unUsedColumnKeySet);

            	Integer v_i = getColumnAbleAssignStudentCount(subject_i.getId())-subject_i.getAvgClassSize();
            	Integer v_j = getColumnAbleAssignStudentCount(subject_j.getId())-subject_j.getAvgClassSize();
            	
            	if(minUsableColumnCount_i==minUsableColumnCount_j) {
            		return v_i.compareTo(v_j);
            	}else {
            		return minUsableColumnCount_i.compareTo(minUsableColumnCount_j);
            	}
			};
		});

		SubjectGroup currfixTwoSubjectGroup = fixTwoSubjectGroups.get(0);
		List<Cell> cellList = this.getColumnValidCells(currfixTwoSubjectGroup.getId()); 
		
		Integer studentCount = cellList.stream().mapToInt(m->m.getStudentCount()).sum();
		
		//计算必开项 
		List<Cell> needCells = this.getNeedCells(cellList, remainSg);
		List<Cell> otherCells= this.getOtherCells(cellList, needCells);
		
		//先处理小值
		Collections.sort(otherCells, new Comparator<Cell>() {
			public int compare(Cell o1, Cell o2) {
				return o1.getStudentCount()-o2.getStudentCount();
			};
		});
		
		LinkedList<Cell> defaultSelect = new LinkedList<>(needCells);
		int defatulCount = 0;
		for (Cell cell : defaultSelect) {
			defatulCount+=cell.getStudentCount();
		}
		
		Double adClassCount = Math.floor((Integer)currfixTwoSubjectGroup.getStudentCount()/currfixTwoSubjectGroup.getAvgClassSize());
		//int bigClassSize = adClassCount.intValue()*currfixTwoSubjectGroup.getAvgClassSize();
		List<CellStack> allCellStackListList = new ArrayList<>();
		
		for (int i=adClassCount.intValue();i>0;i--) {
			int bigClassSize = i*currfixTwoSubjectGroup.getAvgClassSize();
			if(defatulCount>bigClassSize) continue;
			List<CellStack> cellStackList = Utils.generateSumCombineArrange(otherCells.toArray(new Cell[otherCells.size()]), defaultSelect, bigClassSize, new TimeoutWatch(60*5L));
			
			cellStackList.stream().forEach(c->c.setAvgClassSize(currfixTwoSubjectGroup.getAvgClassSize()));
			
			allCellStackListList.addAll(cellStackList);
			
			if(studentCount-bigClassSize >0 && studentCount-bigClassSize <= 5) {
				int avgSize = currfixTwoSubjectGroup.getAvgClassSize() + (studentCount-bigClassSize);
				List<CellStack> cellStackList2 =  Utils.generateSumCombineArrange(otherCells.toArray(new Cell[otherCells.size()]), defaultSelect, studentCount, new TimeoutWatch(60*5L));
				cellStackList2.stream().forEach(c->c.setAvgClassSize(avgSize));
				allCellStackListList.addAll(cellStackList2);
			}
		}
		
		//处理所有情况
		logMap.put(currfixTwoSubjectGroup.getId(), allCellStackListList.size()+1);

		for (CellStack cellStack : allCellStackListList) {

			//备份数据
			TreeBasedTable<String, String, Cell> bakTable = this.cloneTable(this.table);
			
			LinkedList<Cell> divSelect = Utils.splitClass(cellStack, this.table);
			List<LinkedList<Cell>> divCells = Utils.splitSubClass(divSelect, cellStack.getAvgClassSize());
			
			//清理数据
			this.cleanMulClassData(divCells);

			selectData.putAll(currfixTwoSubjectGroup.getId(), divCells);
			
			List<SubjectGroup> tmpFixTwoSubjectGroups = new ArrayList<SubjectGroup>(fixTwoSubjectGroups);
			tmpFixTwoSubjectGroups.remove(currfixTwoSubjectGroup);
			this.divAdClassBacktrace(tmpFixTwoSubjectGroups, selectData, logMap, scoreList);
			
			//恢复数据
			selectData.removeAll(currfixTwoSubjectGroup.getId());
			this.recoverTable(bakTable, this.table);	
		}


		if(needCells.size()>0 && allCellStackListList.size()<=0) {

			if(isLastColumnCell(needCells)) {
				
				List<LinkedList<Cell>> lastClassList = Utils.splitSubClass(needCells, currfixTwoSubjectGroup.getAvgClassSize());
				
				//所有行政班级
				List<ClassResult> divAdClassList = new ArrayList<>();
				for (String fixTwoId : selectData.keySet()) {
					for (LinkedList<Cell> cells : selectData.get(fixTwoId)) {
						AdClassResult divAdClass = new AdClassResult(divContext, divContext.getFixTwoId2fixTwoSubjectGroup().get(fixTwoId), cells);
						divAdClassList.add(divAdClass);
					}
				}
				
				for (LinkedList<Cell> cells : lastClassList) {
					AdClassResult divAdClass = new AdClassResult(divContext, divContext.getFixTwoId2fixTwoSubjectGroup().get(currfixTwoSubjectGroup.getId()), cells);
					divAdClassList.add(divAdClass);
				}
				
//				LinkedList<Cell> lastClass = lastClassList.get(lastClassList.size()-1);
//				int lastCount = 0;
//				for (Cell cell : lastClass) {
//					lastCount+=cell.getStudentCount();
//				}
				
				ScoreResult scoreResult = Utils.getScore(divContext, divAdClassList);
				if(null == scoreResult) {
					return false;
				}
				
				scoreList.add(scoreResult);
				
				System.out.println("计算分班最优解："+scoreList.size());
				
				if(ResponseType.FAST.equals(signalParam.getResponseType())) {
					if(scoreResult.getScore()<=25*7) {
						signalParam.setForceOut(true);
						return true;
					}
				}
				
				//备份数据
				TreeBasedTable<String, String, Cell> bakTable = this.cloneTable(this.table);
				
				//清理数据
				this.cleanMulClassData(lastClassList);
				
				selectData.putAll(currfixTwoSubjectGroup.getId(), lastClassList);
				
				this.checkDivClass(selectData, divContext);
				
				selectData.removeAll(currfixTwoSubjectGroup.getId());
				
				this.recoverTable(bakTable, this.table);
				
				return true;

			}else if(ProcessType.BAD_AVG.equals(this.signalParam.getProcessType())) {
				
				List<Cell> allCellList = new ArrayList<>(needCells);
				allCellList.addAll(otherCells);
				
				List<LinkedList<Cell>> lastClassList = Utils.splitSubClass(allCellList, currfixTwoSubjectGroup.getAvgClassSize());

				//备份数据
				TreeBasedTable<String, String, Cell> bakTable = this.cloneTable(this.table);
				
				//清理数据
				this.cleanMulClassData(lastClassList);
				
				selectData.putAll(currfixTwoSubjectGroup.getId(), lastClassList);

				List<SubjectGroup> tmpFixTwoSubjectGroups = new ArrayList<SubjectGroup>(fixTwoSubjectGroups);
				tmpFixTwoSubjectGroups.remove(currfixTwoSubjectGroup);
				
				this.divAdClassBacktrace(tmpFixTwoSubjectGroups, selectData, logMap, scoreList);
				
				selectData.removeAll(currfixTwoSubjectGroup.getId());
				
				this.recoverTable(bakTable, this.table);
				
				return true;
			}

			return false;
		}
		
		//处理当前定二不开情况
		if(needCells.size()<=0) {
			
			List<SubjectGroup> tmpFixTwoSubjectGroups = new ArrayList<SubjectGroup>(fixTwoSubjectGroups);
			tmpFixTwoSubjectGroups.remove(currfixTwoSubjectGroup);
			
			this.divAdClassBacktrace(tmpFixTwoSubjectGroups, selectData, logMap, scoreList);
		}
		
		return false;
	}
	
	
	/**
	 * 计算列上的可用人数
	 * @param columnKey
	 * @return
	 */
	public int getColumnAbleAssignStudentCount(String columnKey) {
		int count = 0;
		for (Cell cell : this.table.column(columnKey).values()) {
			count+=cell.getStudentCount();
		}		
		return count;
	}
	
	

	
	
	/**
	 * 获取当前必须要使用的科目
	 * @param columns
	 * @param subjectCells
	 * @param subjectIndex
	 * @return
	 */
	public List<Cell> getNeedCells(List<Cell> columns, SubjectGroup[] remainSg) {
		
		List<Cell> unList = new ArrayList<Cell>();
		for (Cell cell : columns) {
			
			if(cell.getStudentCount() <= 0) continue;
			
			//计算当前科目人数在后续科目是否还可用
			int maxValue = 0;
			for (SubjectGroup sg : remainSg) {
				
				if(sg.getId() == cell.getColumnKey()) continue;
				Cell nextCell = this.table.get(cell.getRowKey(), sg.getId());
				if(nextCell.getStudentCount()>maxValue)  maxValue=nextCell.getStudentCount();
				
			}
			
			if(maxValue<=0) unList.add(cell);
		}
		
		return unList;
	}
	
	
	/**
	 * 获取其它的格子
	 * @param columns
	 * @param needCells
	 * @param adCell
	 * @return
	 */
	public List<Cell> getOtherCells(List<Cell> columns, List<Cell> needCells) {
		
		if(needCells.size()<=0) return columns;
		
		List<Cell> otherList = new ArrayList<Cell>();
		for (Cell cell : columns) {
			if(needCells.contains(cell)) continue;
			otherList.add(cell);
		}
		
		return otherList;
		
	}
	
	
    /**
     * 克隆新对象
     * @param src
     * @param target
     */
    public TreeBasedTable<String, String, Cell> cloneTable(TreeBasedTable<String, String, Cell> srcTable) {
    	
    	TreeBasedTable<String, String, Cell> newTable=TreeBasedTable.create();
    	
    	for (Entry<String, Map<String, Cell>> rowEntry : srcTable.rowMap().entrySet()) {
    		for (Entry<String, Cell> cellEntry : rowEntry.getValue().entrySet()) {
    			Cell srcCell = srcTable.get(rowEntry.getKey(), cellEntry.getKey());
    			Cell targetCell = new Cell(srcCell);
    			newTable.put(rowEntry.getKey(), cellEntry.getKey(), targetCell);
			}
		}
    	return newTable;
    }
    
    
	/**
	 * 清值
	 * @param cells
	 */
	public void cleanMulClassData(List<LinkedList<Cell>> cells) {
		
		for (LinkedList<Cell> cellList : cells) {
			this.cleanClassData(cellList);
		}
	}
	
	
	/**
	 * 清值
	 * @param cells
	 */
	public void cleanClassData(List<Cell> cells) {

		for (Cell learnCell : cells) {
			
			String rowKey = learnCell.getRowKey();
			
			for (Cell cell : table.row(rowKey).values()) {
				
				if(cell.getStudentCount() <= 0) continue;
				
				cell.setStudentCount(cell.getStudentCount()-learnCell.getStudentCount());
				
				if(cell.getStudentCount()<0) {
					System.out.println("=====");
				}
			}
		}
	}
	
	
    /**
     * 复制到目标对象
     * @param src
     * @param target
     */
    public void recoverTable(TreeBasedTable<String, String, Cell> srcTable, TreeBasedTable<String, String, Cell> targetTable) {
    	
    	for (Entry<String, Map<String, Cell>> rowEntry : srcTable.rowMap().entrySet()) {
    		for (Entry<String, Cell> cellEntry : rowEntry.getValue().entrySet()) {
    			Cell srcCell = srcTable.get(rowEntry.getKey(), cellEntry.getKey());
    			Cell targetCell = targetTable.get(rowEntry.getKey(), cellEntry.getKey());
    			//复制可用人数
    			targetCell.setStudentCount(srcCell.getStudentCount());
			}
		}
    }
    
	
	
	/**
	 * 检查分班结果
	 * @param selectData
	 * @return
	 */
	public boolean checkDivClass(ArrayListMultimap<String, LinkedList<Cell>> selectData, DivContext divContext) {
		
		ArrayListMultimap<String, Integer> wishCountMap = ArrayListMultimap.create();
		
		int totalStudent = 0;
		for (String fixId : selectData.keySet()) {
			for (LinkedList<Cell> cells : selectData.get(fixId)) {
				for (Cell cell : cells) {
					wishCountMap.put(cell.getRowKey(), cell.getStudentCount());
					totalStudent+=cell.getStudentCount();
				}
			}
		}
		
		
		for (SubjectGroup wish : divContext.getWishSubjectGroupList()) {
			int wishTotal = wish.getStudentCount();
			int currTotal = 0;
			for (Integer count : wishCountMap.get(wish.getId())) {
				currTotal+=count;
			}
			
			if(wishTotal!=currTotal) {
				throw new RuntimeException(wish.getId()+"志愿人数"+wishTotal+"和分完后人数："+currTotal+"对不上");
			}
		}
		
		
		if(totalStudent!=divContext.getTotalStudent()) {
			throw new RuntimeException("统计分班人数"+totalStudent+"和参与分班人数："+divContext.getTotalStudent()+"对不上");
		}
		
		return false;
		
		
	}
}
