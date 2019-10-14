package com.talkweb.placementtask.utils.div;

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
import com.google.common.collect.TreeBasedTable;
import com.talkweb.placementtask.utils.div.dto.Cell;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.dto.TeachClassResult;

/**
 * 分班基类
 * 
 * @author hushow
 *
 * @param <T>
 */
public abstract class Divide<T> {

	/**
	 * 矩阵数据
	 */
	protected TreeBasedTable<String, String, Cell> table = TreeBasedTable.create();
	

	/**
	 * 上下文对象
	 */
	protected DivContext divContext;
	
	
	/**
	 * 总人数
	 */
	private int totalStudentCount;
	
	
	/**
	 * 
	 * 志愿对应人索引
	 */
	protected ArrayListMultimap<String, Student> wishId2StudentMultimap = ArrayListMultimap.create();
	
	
	
	public Divide(DivContext divContext) {
		this.divContext = divContext;
	}
	
	
	/**
	 * 克隆学生人数
	 * 
	 * @param srcTable
	 */
	public TreeBasedTable<String, String, Integer> cloneStudentCount(TreeBasedTable<String, String, Cell> srcTable) {

		TreeBasedTable<String, String, Integer> tableBak = TreeBasedTable.create();
		for (Entry<String, Map<String, Cell>> rowEntry : srcTable.rowMap().entrySet()) {
			for (Entry<String, Cell> columnEntry : rowEntry.getValue().entrySet()) {
				tableBak.put(rowEntry.getKey(), columnEntry.getKey(), columnEntry.getValue().getStudentCount());
			}
		}
		return tableBak;
	}

	/**
	 * 恢复学生人数
	 * 
	 * @param srcTable
	 * @param targetTable
	 */
	public void recoverStudentCount(TreeBasedTable<String, String, Integer> srcTable,
			TreeBasedTable<String, String, Cell> targetTable) {

		for (Entry<String, Map<String, Integer>> rowEntry : srcTable.rowMap().entrySet()) {
			for (Entry<String, Integer> columnEntry : rowEntry.getValue().entrySet()) {
				Cell targetCell = targetTable.get(rowEntry.getKey(), columnEntry.getKey());
				targetCell.setStudentCount(columnEntry.getValue());
			}
		}
	}
	
	
	/**
	 * 恢复人数
	 * @param fromMap
	 * @param toMap
	 */
    public void recoverMap(Map<String, Integer> fromMap, Map<String, Integer> toMap) {
		for (Entry<String, Integer> entry : fromMap.entrySet()) {
			toMap.put(entry.getKey(), entry.getValue());
		}
    }
	
	
	/**
	 * 统计科目下剩余人数
	 * @return
	 */
	public Map<String, Integer> getCurrentSubjectStudentCount() {
		
		Map<String, Integer> subjectId2StudentCountMap = new HashMap<String, Integer>();
		
		for (Entry<String, Map<String, Cell>> columnEntry : this.table.columnMap().entrySet()) {
			String subjectId = columnEntry.getKey();
			int studentTotal = 0;
			for (Cell cell : columnEntry.getValue().values()) {
				if(cell.getStudentCount()<=0) continue;
				studentTotal+=cell.getStudentCount();
			}
			
			subjectId2StudentCountMap.put(subjectId, studentTotal);
		}
		
		return subjectId2StudentCountMap;
	}
	

	/**
	 * 清值
	 * @param cells
	 */
	public void cleanMulClassData(List<LinkedList<Cell>> cells, boolean isLastSeq) {
		
		for (LinkedList<Cell> cellList : cells) {
			this.cleanClassData(cellList, isLastSeq);
		}
	}
	
	
	/**
	 * 清值
	 * @param cells
	 */
	public void cleanClassData(List<Cell> cells, boolean isLastSeq) {
		
		for (Cell learnCell : cells) {
			
			String rowKey = learnCell.getRowKey();
			
			//最后序列不同科目下不会有相同的学生
			if(isLastSeq) {
				table.get(rowKey, learnCell.getColumnKey()).setStudentCount(0);
				continue;
			}
			
			for (Cell cell : table.row(rowKey).values()) {
				if(cell.getStudentCount() > 0) {
					
					cell.setStudentCount(cell.getStudentCount()-learnCell.getStudentCount());
					
					if(cell.getStudentCount()<0) {
						System.out.println("=====");
					}
				}
			}
		}
	}
	
	
	/**
	 * 是否为最后一批开班数据
	 * @param cells
	 * @return
	 */
	public boolean isLastColumnCell(List<Cell> cells) {
		
		Set<String> rowKeySet = new HashSet<>();
		for (Cell cell : cells) {
			rowKeySet.add(cell.getRowKey());
		}
		
		int total =0;
		for (Entry<String, Map<String, Cell>> rowEntry : table.rowMap().entrySet()) {
			
			if(rowKeySet.contains(rowEntry.getKey())) continue;
			
			for (Cell cell : rowEntry.getValue().values()) {
				if(cell.getStudentCount()<=0) continue;
				total+=cell.getStudentCount();
				break;
			}
		}
		
		if(total<=0) return true;
		
		return false;
	}
	
	
	/**
	 * 获取列下有效cell
	 * @param columnKey  列key
	 * @return 有效cell
	 */
	public List<Cell> getColumnValidCells(String columnKey){
		List<Cell> validList = new ArrayList<>();
		for (Cell cell : this.table.column(columnKey).values()) {
			if(cell.getStudentCount()<=0) continue;
			validList.add(cell);
		}
		return validList;
	}
	
	
	/**
	 * 获取行有效cell
	 * @param rowKey  列key
	 * @return 有效的行cell
	 */
	public List<Cell> getRowValidCells(String rowKey){
		List<Cell> validList = new ArrayList<>();
		for (Cell cell : this.table.row(rowKey).values()) {
			if(cell.getStudentCount()<=0) continue;
			validList.add(cell);
		}
		return validList;
	}
	
	
	public int calculateRemainCount() {
		int total = 0;
		for (com.google.common.collect.Table.Cell<String, String, Cell> cell : this.table.cellSet()) {
			if(cell.getValue().getStudentCount()<=0) continue;
			total+=cell.getValue().getStudentCount();
		}
		return total;
	}
	
	
	/**
	 * 获取列剩下人数
	 * @param columnKey
	 * @return
	 */
	public int calculateRemainCountByColumn(String columnKey) {
		int total = 0;
		for (Cell cell : this.table.column(columnKey).values()) {
			if(cell.getStudentCount()<=0) continue;
			total+=cell.getStudentCount();
		}
		return total;
	}
	
	/**
	 * 计算该列下志愿组合的最小可用例数
	 */
	public int calculateMinUsableColumnCount(String columnKey, Set<String> unUsedColumnKeySet) {
		
    	Map<String, Cell> column1 = table.column(columnKey);
    	
    	Integer minUsableColumnCount1 = Integer.MAX_VALUE;
    	
    	//循环所有列
    	for (Cell cell : column1.values()) {
    		if(cell.getStudentCount()<=0) continue;
    		
    		if(cell.getIsNeed()) {
    			minUsableColumnCount1 = 1;
    			break;
    		}
    		
    		//计算该单元格所在行可用列数
    		int rowUsableColumnCount = calculateUsableColumnCount(cell.getRowKey(), unUsedColumnKeySet);
    		
    		//记录最小值
    		if(rowUsableColumnCount < minUsableColumnCount1) {
    			minUsableColumnCount1 = rowUsableColumnCount;
    		}
		}
    	
    	return minUsableColumnCount1;
	}	
	
	
	public int calculateUsableColumnCount(String rowKey, Set<String> unUsedColumnKeySet) {
		
		Collection<Cell> rowCells = this.table.row(rowKey).values();
		int rowUsableColumnCount = 0;
		for (Cell rowCell : rowCells) {
			if(rowCell.getStudentCount()<=0) continue;
			//已经使用的不统计
			if(!unUsedColumnKeySet.contains(rowCell.getColumnKey())) continue;
			rowUsableColumnCount++;
		}
		
		return rowUsableColumnCount;
	}
	

	
	/**
	 * 提人(保证同一序列同科下没有相同 人)
	 * @param classList
	 */
	public ArrayListMultimap<String, Student> extractStudent(List<ClassResult> classList, ArrayListMultimap<String, Student> parentWishId2StudentMultimap) {
		
		
		if(parentWishId2StudentMultimap.size()<=0) throw new RuntimeException("志愿对应学生缓存数据为空");
		
		//父子志愿关联
		ArrayListMultimap<String, Cell> parentWishId2ChildWishCellMultimap = ArrayListMultimap.create();
		for (ClassResult classResult : classList) {
			for (Cell cell : classResult.getCells()) {
				if(null!=cell.getSplitWishId() && !cell.getRowKey().equals(cell.getSplitWishId())) {
					String parentWishId = cell.getRowKey();
					parentWishId2ChildWishCellMultimap.put(parentWishId, cell);
				}
			}
		}
		
		//父志愿人员合理分配给子志愿
		ArrayListMultimap<String, Student> origSplitWishId2StudentMultimap = ArrayListMultimap.create();
		
		for (Entry<String, Collection<Student>> mapEntry : parentWishId2StudentMultimap.asMap().entrySet()) {
			String parentWishId = mapEntry.getKey();
			List<Student> parentWishStudents = new ArrayList<>(mapEntry.getValue());
			List<Cell> childWishCellList = parentWishId2ChildWishCellMultimap.get(parentWishId);
			
			//未被拆志愿
			if(childWishCellList.size() <=0) {
				origSplitWishId2StudentMultimap.putAll(parentWishId, parentWishStudents);
				continue;
			}
			
			//被拆志愿
			for (Cell childWishCell : childWishCellList) {
				List<Student> subStudents = parentWishStudents.subList(0, childWishCell.getStudentCount());
				origSplitWishId2StudentMultimap.putAll(childWishCell.getSplitWishId(), new ArrayList<>(subStudents));
				parentWishStudents.removeAll(subStudents);
			}
			
			if(parentWishStudents.size()>0) {
				throw new RuntimeException(parentWishStudents+"分配子志愿异常，还有剩余人数！");
			}
		}
		
		
		for (ClassResult classResult : classList) {
			for (Cell cell : classResult.getCells()) {
				String splitWishId = cell.getSplitWishId();
				List<Student> splitStudents = origSplitWishId2StudentMultimap.get(splitWishId);
				classResult.getWishId2StudentList().putAll(cell.getRowKey(), new ArrayList<>(splitStudents));
			}
		}
		
		return origSplitWishId2StudentMultimap;
	}
	
	
	/**
	 * 提人(保证同一序列同科下没有相同 人)
	 * @param classList
	 */
	public void extractStudent1(List<ClassResult> classList, TreeBasedTable<String, String, List<Student>> parentWishIdSubjectId2Students) {
		
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
	
	
	/**
	 * 拆减志愿
	 * @param classResultList
	 */
	public void seqSplitWish(List<ClassResult> classResultList) {
		
		//减掉当前分班人数
		ArrayListMultimap<String, Cell> wish2CellMap = ArrayListMultimap.create();
		for (ClassResult classEntiry : classResultList) {
			for (Cell classCell : classEntiry.getCells()) {
				wish2CellMap.put(classCell.getRowKey(), classCell);
			}
		}
		
		
		//减人和拆子志愿
		for (String wishId : wish2CellMap.keySet()) {
			
			List<Cell> cells = wish2CellMap.get(wishId);
			
			List<Cell> waitDivCells = new ArrayList<>(this.table.row(wishId).values());
			if(cells.size()>1) {
				int num=1;
				for (Cell classCell : cells) {
					String newWishId=wishId+Utils.WISH_SEPARATOR_SYMBOL+num;
					for (Cell waitCell : waitDivCells) {
						int studentCount = 0;
						if(waitCell.getStudentCount()>0 && !waitCell.getColumnKey().equals(classCell.getColumnKey())) {
							studentCount = classCell.getStudentCount();
						}
						Cell newCell = new Cell(newWishId, waitCell.getColumnKey(), studentCount);
						this.table.put(newWishId, waitCell.getColumnKey(), newCell);
						
						//删掉被拆志愿
						this.table.remove(wishId, waitCell.getColumnKey());
					}
					
					//关联被拆后新的志愿id
					classCell.setSplitWishId(newWishId);
					
					num++;
				}
			}else {
				Cell classCell = cells.get(0);
				Cell cell = this.table.get(classCell.getRowKey(), classCell.getColumnKey());
				cell.setStudentCount(cell.getStudentCount()-classCell.getStudentCount());
				
				//关联被拆后新的志愿id
				classCell.setSplitWishId(wishId);
			}
		}
	}
	

	/**
	 * 执行分班
	 * 
	 * @return
	 */
	public abstract T excuteDiv();

	
	/**
	 * 打印当前数据
	 */
	public abstract void printInfo();

	
	public int getTotalStudentCount() {
		return totalStudentCount;
	}

	public void setTotalStudentCount(int totalStudentCount) {
		this.totalStudentCount = totalStudentCount;
	}
}
