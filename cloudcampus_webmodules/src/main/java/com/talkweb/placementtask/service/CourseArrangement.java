package com.talkweb.placementtask.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.talkweb.placementtask.domain.CellCourseInfo;

/**
 * @author Administrator
 * 这里只支持6选3模式下的时间点安排
 * (7选3的交叉方式大体逻辑一样，但是有些地方细节需要重新考虑：比如说可以完整交3个再有一个放前一个时间)
 */
public class CourseArrangement {
	//☆☆☆选考分组数量(6选3和7选3都是3)
	static Integer optNum = 3;
	
	//☆☆☆学考分组数量(6选3为3,7选3为4)
	static Integer proNum = 3;
	
	//选考类型
	final static Integer optType = 1;
	
	//学考类型
	final static Integer proType = 2;
	
	//☆☆☆走班能用的课时(总学时-非走班课时)
	static Integer enablePeriod = 0;
	//☆☆☆走班需要的最小课时(用选-学的课时差值排序，取三个最大的值然后加上所有学考的值)
	static Integer minPeriod = 0;
	//☆☆☆走班课程平铺下来的课时(选考最大课时*optNum+学考最大课时*proNum)
	static Integer maxPeriod = 0;
	//☆☆☆选考科目信息Map,Key为科目id,Value为科目的学时(不上为0),用来初始化平铺课时用
	static Map<String,Integer> optCourseNum = new HashMap<String,Integer>();
	//TODO ☆☆☆选考最大课时(这个值主要是用来做选考的最大序列号，如果出现折叠的话要做调整)
	static Integer optMaxNum = 0;
	//☆☆☆学考科目信息Map,Key为科目id,Value为科目的学时(不上为0),用来初始化平铺课时用
	static Map<String,Integer> proCourseNum = new HashMap<String,Integer>();
	//TODO ☆☆☆学考最大课时(这个值主要是用来做选考的最大序列号，如果出现折叠的话要做调整)
	static Integer proMaxNum = 0;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		/*初始化*/
		enablePeriod = 18;
		minPeriod =18;
		maxPeriod=24;
		optCourseNum.put("4", 3);
		optCourseNum.put("5", 4);
		optCourseNum.put("6", 3);
		optCourseNum.put("7", 5);
		optCourseNum.put("8", 4);
		optCourseNum.put("9", 3);
		optMaxNum=5;
		proCourseNum.put("4", 1);
		proCourseNum.put("5", 1);
		proCourseNum.put("6", 1);
		proCourseNum.put("7", 2);
		proCourseNum.put("8", 2);
		proCourseNum.put("9", 3);
		proMaxNum=3;

		try{
			//1、判断学时是否足够
			if(minPeriod>enablePeriod){
				throw new Exception("学时不够");
			}
			//新建一个HashBasedTable(行和列的序号都是从1开始),用来保存每个时间点的科目上课情况(包括学选交叉情况以及事件点调换情况)
			Table<Integer, Integer, List<CellCourseInfo>> courseArrangement = HashBasedTable.create();
			//为了防止报错，需要先将所有格子的值初始化
			for(int row =1;row<=optNum+proNum;row++){
				for(int column =1;column<=Math.max(optMaxNum, proMaxNum);column++){
					courseArrangement.put(row, column, new ArrayList<CellCourseInfo>());
				}
			}
			
			//是否有学选交叉
			Boolean ifCross = false;
			//最后一个学考是否有时间点调换
			Boolean ifProOrderChange = false;
			//是否有科目顺序调换
			Boolean ifSubjectOrderChange = false;
			//TODO 最后一列能被学考完全交叉的选考序列(可以理解为本次学选交叉的时候要被交叉的选考列)。初始值为选考学时的最大值(没考虑折叠的情况)
			Integer beCrossColumn = optMaxNum;
			
			for (int column =1; column<=optMaxNum;column++){
				for(String subjectId:optCourseNum.keySet()){
					if(optCourseNum.get(subjectId)>0){
						for(int row =1;row<=optNum;row++){
							CellCourseInfo cellCourseInfo = new CellCourseInfo();
							cellCourseInfo.setSubjectType(optType);
							cellCourseInfo.setSubjectId(subjectId);
							cellCourseInfo.setCellRowNum(row);
							cellCourseInfo.setCellColumnNum(column);
							courseArrangement.column(column).get(row).add(cellCourseInfo);
						}
					}
					optCourseNum.put(subjectId, optCourseNum.get(subjectId)-1);
				}
			}
			
			//将学考课程放到courseArrangement第4行开始对应格子中，每个格子的值是List<CellCourseInfo>
			for (int column =1; column<=proMaxNum;column++){
				for(String subjectId:proCourseNum.keySet()){
					if(proCourseNum.get(subjectId)>0){
						for(int row =optNum+1;row<=optNum+proNum;row++){
							CellCourseInfo cellCourseInfo = new CellCourseInfo();
							cellCourseInfo.setSubjectType(proType);
							cellCourseInfo.setSubjectId(subjectId);
							cellCourseInfo.setCellRowNum(row);
							cellCourseInfo.setCellColumnNum(column);
							courseArrangement.column(column).get(row).add(cellCourseInfo);
						}
						proCourseNum.put(subjectId, proCourseNum.get(subjectId)-1);
					}
				}
			}

			
			
			//判断平铺下来学时是否足够
			if(maxPeriod<=enablePeriod){
				//平铺下来格子足够，不需要做学选交叉，直接返回格子中的安排情况				
				System.out.println("是否有学选交叉:"+ifCross);
				System.out.println("是否有时间点调换:"+ifProOrderChange);
				System.out.println("课程分布安排：");
				//打印Table
				print(optNum+proNum,Math.max(optMaxNum, proMaxNum),courseArrangement);
				return;


			}
			//上面判断没跳出方法，就说明需要有学选交叉
			ifCross =true;
			//完整相交的数量
			Integer completeCross =(maxPeriod-enablePeriod)/optNum;
			//单独相交的数量
			Integer aloneCross=(maxPeriod-enablePeriod)%optNum;		
			
			//最后一个时间点如果不是完全交叉，直接学选交叉就可以
			if(aloneCross>0){
				//找出选考和学考最后一列的序号
				int optLastColumn = lastValuableColumn(optType,courseArrangement);
				int proLastColumn = lastValuableColumn(proType,courseArrangement);
				for(int row=1; row<=aloneCross;row++){
					//把最后一个序列的学考放aloneCross个到最后一个选考序列(先学1放选1，然后学2放选2)
					courseArrangement.row(row).get(optLastColumn).addAll(courseArrangement.row(row+optNum).get(proLastColumn));
					//删除对应学考序列中的信息
					courseArrangement.put(row+optNum, proLastColumn, new ArrayList<CellCourseInfo>());
				}
				//最后一列能完全交叉的选考序列-1
				beCrossColumn--;
			}
			
			//判断是否有完全交叉的情况
			if(completeCross>0){
				for(;completeCross>0;completeCross--){
					//遍历每一列，接收返回的beCrossColumn和courseArrangement作为下一次迭代的参数，直到所有列交叉完成为止
					JSONObject jsonNow =completeCross(beCrossColumn,courseArrangement);
					//获取返回的值，作为下一次交叉的基准值
					beCrossColumn = (Integer) jsonNow.get("beCrossColumn");
					courseArrangement = (Table<Integer, Integer, List<CellCourseInfo>>) jsonNow.get("courseArrangement");
					if(!ifSubjectOrderChange){
						ifSubjectOrderChange = (Boolean) jsonNow.get("ifSubjectOrderChange");
					}
				}
			}
			
			//全部处理完之后，遍历每个格子，如果出现一个格子里出现同科目同类型的CellCourseInfo，抛出异常
			for(int row=1;row<=optNum+proNum;row++){
				for(int column=1;column<=Math.max(optMaxNum,proMaxNum);column++){
					//判断是否有重复的科目类型
					if(ifExistRepeatData(courseArrangement.row(row).get(column))){
						throw new Exception("出现重复科目类型,分班有误");
					}
				}
			}			
			
			//判断最后两个选考时间点是否需要学选交叉
			if(ifSubjectOrderChange){
				//判断是否有时间点调换
				if(ifNeedChangeProTime(optMaxNum,courseArrangement)){
					ifProOrderChange = true;
					//将最后一个时间点的学考课程下移(先拿出来保存并且删除学考信息)
					Map<Integer,List<CellCourseInfo>> lastProInfos = new HashMap<Integer,List<CellCourseInfo>>();
					for(int row =1; row<=optNum;row++){
						List<CellCourseInfo> lastProInfo = new ArrayList<CellCourseInfo>();
						for(CellCourseInfo cellCourseInfo:courseArrangement.row(row).get(optMaxNum)){
							if(cellCourseInfo.getSubjectType()==proType){
								lastProInfo.add(cellCourseInfo);
								courseArrangement.row(row).get(optMaxNum).remove(cellCourseInfo);
							}
						}
						lastProInfos.put(row, lastProInfo);
					}
					
					//将学考信息错位加上来
					for(int row =1; row<=optNum;row++){
						if(row==optNum){
							courseArrangement.row(row).get(optMaxNum).addAll(lastProInfos.get(1));
						}else{
							courseArrangement.row(row).get(optMaxNum).addAll(lastProInfos.get(row+1));
						}
					}
				}
			}
			
			System.out.println("是否有学选交叉："+ifCross);
			System.out.println("是否有时间点调换："+ifProOrderChange);
			System.out.println("课程分布安排：");
			//打印Table
			print(optNum+proNum,Math.max(optMaxNum, proMaxNum),courseArrangement);
			return;
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 最后三个学考交到最后一列没使用的选考上面
	 * (参数:最后一列交叉了的选考序号，如果为-1，说明还没交叉；HashBasedTable)
	 * 返回值：新的Table数据,选考已经被使用到的列beCrossColumn
	 * */
	public static JSONObject completeCross(Integer beCrossColumn, Table<Integer,Integer,List<CellCourseInfo>> courseArrangement){
		//是否有科目顺序调换
		Boolean ifSubjectOrderChange =false;
		JSONObject json = new JSONObject();
		try{
			//proLastColumn最后一列有值的学考序列，也就是需要交到选考的列；(本轮分配的时候不可能变化，所以操作完之后不用--)
			int proLastColumn = lastValuableColumn(proType,courseArrangement);
			
			// 最后一个学考时间的科目信息(因为初始值的时候，每行数据一样，所以不需要传行号，直接以学考第一行查询)
			List<String> proSubjectList = querySubjectList(proType,proLastColumn,courseArrangement);
			// 需要做学选交叉的选考序列科目信息(因为初始值的时候，每行数据一样，所以不需要传行号，直接以选考第一行查询)
			List<String> optSubjectList = querySubjectList(optType,beCrossColumn,courseArrangement);
			if(proSubjectList.size()==0||optSubjectList.size()==0){
				throw new Exception("学时信息有误");
			}
			
			// 最后要交叉时间点学或者选的科目个数大于3,学考拿两个放到最后一个能用的选考序列，最后一个学考放到倒数第二个能用的选考序列中(不可能直接学选三个交叉)
			if(proSubjectList.size()>3||optSubjectList.size()>3){
				for(int i=1;i<optNum;i++){
					//将学1、学2分别和选1、选2覆盖,并删除对应学考格子中数据
					courseArrangement.row(i).get(beCrossColumn).addAll(courseArrangement.row(i+optNum).get(proLastColumn));
					courseArrangement.put(i+optNum, proLastColumn, new ArrayList<CellCourseInfo>());
				}
				beCrossColumn--;
				courseArrangement.row(1).get(beCrossColumn).addAll(courseArrangement.row(optNum*2).get(proLastColumn));
				courseArrangement.put(optNum*2, proLastColumn, new ArrayList<CellCourseInfo>());			
				beCrossColumn--;			
			}
			//学考和选考科目个数都小于等于3
			else {
				//先将科目这个时间点的学选科目去重，计算有学选加起来多少个科目
				List<String> subjectList = mergeSubjectList("",optSubjectList,proSubjectList);
				
				//如果科目总数小于等于3，可以直接交叉
				if(subjectList.size()<=3){
					for(int row =1;row<=optNum;row++){
						courseArrangement.row(row).get(beCrossColumn).addAll(courseArrangement.row(row+optNum).get(proLastColumn));
						courseArrangement.put(row+optNum, proLastColumn, new ArrayList<CellCourseInfo>());
					}
					beCrossColumn--;
				}
				//科目总数大于3，不能直接交叉
				else{
					//如果当前选考是最后一列；两个放最后，一个放倒数第二个
					if(beCrossColumn == optMaxNum){
						for(int i =1; i<optNum;i++){
							courseArrangement.row(i).get(beCrossColumn).addAll(courseArrangement.row(i+optNum).get(proLastColumn));
							courseArrangement.put(i+optNum, proLastColumn, new ArrayList<CellCourseInfo>());
						}
						beCrossColumn--;
						courseArrangement.row(1).get(beCrossColumn).addAll(courseArrangement.row(optNum*2).get(proLastColumn));
						courseArrangement.put(optNum*2, proLastColumn, new ArrayList<CellCourseInfo>());
						beCrossColumn--;
					}
					
					else{
						//优先做科目调换时间(如果成功的话，ifSuccess字段改成true,不用跨两个时间安排交叉情况)
						Boolean ifSuccess =false;
						//遍历总科目以及在选考中存在的科目，就是学考独有的科目
						List<String> onlyProSubjectList = queryOnlySubjectList(subjectList,optSubjectList);
						//遍历总科目以及在学考中存在的科目，获得选考独有的科目
						List<String> onlyOptSubjectList = queryOnlySubjectList(subjectList,proSubjectList);
						//选考和学考合并之后的总科目，用这个List.size判断是否还要继续减少科目。如果等于3的话不用再继续移动科目
						List<String> mergeSubjectList = mergeSubjectList("",optSubjectList,proSubjectList);
						
						//需要跳出去的科目数
						int mustDeleteNum = mergeSubjectList.size()-3;
						
						//从最后一列选考开始往前遍历查找，直到能将多余的科目分出去为止(最多找到本序列后面那个序列)
						for(Integer optColumn = optMaxNum;optColumn>beCrossColumn;optColumn--){
							//找optColumn列optNum行选考中学考和选考科目信息(学和选的科目都从这里找)
							List<String> lastColumnOptSubjectList = new ArrayList<String>();
							List<String> lastColumnProSubjectList = new ArrayList<String>();
							//用optNum行的数据这一行可能出现没有和学考交叉
							for(CellCourseInfo cellCourseInfo: courseArrangement.row(optNum).get(optColumn)){
								if(cellCourseInfo.getSubjectType() == optType){
									lastColumnOptSubjectList.add(cellCourseInfo.getSubjectId());
								}else if(cellCourseInfo.getSubjectType() == proType){
									lastColumnProSubjectList.add(cellCourseInfo.getSubjectId());
								}
							}
							
							List<String> optCanMoveSubjectList =queryOnlySubjectList(onlyOptSubjectList,lastColumnOptSubjectList);
							List<String> proCanMoveSubjectList =queryOnlySubjectList(onlyProSubjectList,lastColumnProSubjectList);
							
							if(lastColumnProSubjectList.size()<3&&proCanMoveSubjectList.size()>0){
								//遍历学考能移动到optColumn列的科目
								for(String proSubjectId:proCanMoveSubjectList){
									if(mergeSubjectList(proSubjectId,lastColumnOptSubjectList,lastColumnProSubjectList).size()<=3){
										ifSubjectOrderChange = true;
										for(int row=optNum+1;row<=optNum*2;row++){
											//遍历每行把指定科目加到optColumn列
											CellCourseInfo cellCourseInfo = new CellCourseInfo();
											cellCourseInfo.setSubjectId(proSubjectId);
											cellCourseInfo.setSubjectType(proType);
											cellCourseInfo.setCellRowNum(row);
											cellCourseInfo.setCellColumnNum(optColumn);
											//遍历行把对应科目信息加进来
											courseArrangement.row(row-optNum).get(optColumn).add(cellCourseInfo);
											List<CellCourseInfo> CellCourseInfos = new ArrayList<CellCourseInfo>();
											CellCourseInfos = deleteCellCourseInfoBySubject(proSubjectId,proType,courseArrangement.row(row).get(proLastColumn));
											//删除原始列中被移出的科目			
											courseArrangement.put(row, proLastColumn,CellCourseInfos);
										}
										lastColumnProSubjectList.add(proSubjectId);
										//移动一个科目后,mustDeleteNum需要减少1,如果减少到0说明不需要再移动科目
										mustDeleteNum--;
										if(mustDeleteNum<=0){
											ifSuccess=true;
											break;
										}
									}
								}
							}
							
							//先判断是否还要移动科目，如果还需要移动科目的话再找选考能移动到optColumn列的科目
							if(!ifSuccess){
								if(lastColumnOptSubjectList.size()<3&&optCanMoveSubjectList.size()>0){
									
									//遍历选考能移动到optColumn列的科目
									for(String optSubjectId:optCanMoveSubjectList){
										if(mergeSubjectList(optSubjectId,lastColumnOptSubjectList,lastColumnProSubjectList).size()<=3){
											ifSubjectOrderChange = true;
											for(int row=1;row<=optNum;row++){
												//遍历每行把指定科目加到optColumn列
												CellCourseInfo cellCourseInfo = new CellCourseInfo();
												cellCourseInfo.setSubjectId(optSubjectId);
												cellCourseInfo.setSubjectType(optType);
												cellCourseInfo.setCellRowNum(row);
												cellCourseInfo.setCellColumnNum(optColumn);
												courseArrangement.row(row).get(optColumn).add(cellCourseInfo);
												//删除原始列中被移出的科目
												List<CellCourseInfo> CellCourseInfos = new ArrayList<CellCourseInfo>();
												CellCourseInfos = deleteCellCourseInfoBySubject(optSubjectId,optType,courseArrangement.row(row).get(beCrossColumn));
												courseArrangement.put(row,beCrossColumn,CellCourseInfos);
											}
											lastColumnOptSubjectList.add(optSubjectId);
											//移动一个科目后,mustDeleteNum需要减少1,如果减少到0说明不需要再移动科目
											mustDeleteNum--;
											if(mustDeleteNum<=0){
												ifSuccess=true;
												break;
											}
										}
									}
								}
							}
							if(ifSuccess){
								//如果成功,将剩余的学和选进行交叉并跳出循环
								for(int row =1;row<=optNum;row++){
									courseArrangement.row(row).get(beCrossColumn).addAll(courseArrangement.row(row+optNum).get(proLastColumn));
									courseArrangement.put(row+optNum, proLastColumn, new ArrayList<CellCourseInfo>());
								}
								beCrossColumn--;
								
								break;
							}
						}
						
						//科目调换时间不能达到要求的时候，只能占两个序列
						if(!ifSuccess){
							//把proLastColumn学1、学2的数据放到beCrossColumn选1和选2；把proLastColumn学3的数据放到beCrossColumn-1选1中
							for(int row=1;row<optNum;row++){
								courseArrangement.row(row).get(beCrossColumn).addAll(courseArrangement.row(row+optNum).get(proLastColumn));
								courseArrangement.put(row+optNum, proLastColumn, new ArrayList<CellCourseInfo>());
							}
							beCrossColumn--;
							courseArrangement.row(1).get(beCrossColumn).addAll(courseArrangement.row(optNum*2).get(proLastColumn));
							courseArrangement.put(optNum*2, proLastColumn, new ArrayList<CellCourseInfo>());
							beCrossColumn--;
						}
					}	
				}
			}
			json.put("ifSubjectOrderChange", ifSubjectOrderChange);
			json.put("beCrossColumn", beCrossColumn);
			json.put("courseArrangement", courseArrangement);
		}catch (Exception e){
			e.printStackTrace();
		}
		return json;
	} 
	
	/**
	 * 获得最后一个有值的序号(初始值应该就是optMaxNum和proMaxNum，但如果学考有交叉到选考之后，值就会变动)
	 * (参数:科目类型;HashBasedTable)
	 * */
	public static int lastValuableColumn(Integer subjectType, Table<Integer,Integer,List<CellCourseInfo>> courseArrangement){
		int lastValuableColumn = 0;
		int row = 1;
		if(subjectType==proType){
			row = row+optNum;
		}
		for(int column = 1;column <=Math.max(optMaxNum, proMaxNum);column++){
			//因为每行数据一样，所以取学和选对应的第一行值
			if(courseArrangement.column(column).get(row).size()>0){
				lastValuableColumn = column;
			}else{
				//只要出现size=0的情况，后面就不可能再出现值。直接退出
				break;
			}
		}
		return lastValuableColumn;
	}
	/**
	 *获取班级类型指定列的科目List<SubjectId>。因为同类型的行数据一样，所以判断学和选的第一行数据得到结果
	 *参数列、学选信息
	 * */
	public static List<String> querySubjectList(Integer subjectType, Integer column, Table<Integer,Integer,List<CellCourseInfo>> courseArrangement){
		List<String> subjectList = new ArrayList<String>();
		int row = 1;
		if(subjectType == proType){
			row= 1+optNum;
		}
		List<CellCourseInfo> cellCourseInfos =courseArrangement.row(row).get(column);
		for(CellCourseInfo cellCourseInfo:cellCourseInfos){
			subjectList.add(cellCourseInfo.getSubjectId());
		}
		return subjectList;
	}
	/**
	 * 遍历查找第一个科目列表在第二个科目列表中不存在的科目List
	 * */
	public static List<String> queryOnlySubjectList(List<String> subjectList,List<String> beSearchsubjectList){
		List<String> onlySubjectList = new ArrayList<String>();
		for(String subjectId:subjectList){
			Boolean ifExist =false;
			for(String beSearchsubjectId:beSearchsubjectList){
				if(subjectId.equals(beSearchsubjectId)){
					ifExist=true;
					break;
				}
			}
			if(!ifExist){
				onlySubjectList.add(subjectId);
			}
		}
		return onlySubjectList;
	}
	/**
	 * 这两个科目List再加上一个科目id，最终合并的科目List
	 * */
	public static List<String> mergeSubjectList(String addSubjectId,List<String> subjectList1,List<String> subjectList2){
		List<String> mergeSubjectList = new ArrayList<String>();
		for(String subjectId:subjectList1){
			mergeSubjectList.add(subjectId);
		}
		mergeSubjectList.addAll(queryOnlySubjectList(subjectList2,subjectList1));
		if(!("".equals(addSubjectId))){
			Boolean isExits = false;
			for(String mergeSubject:mergeSubjectList){
				if(mergeSubject.equals(addSubjectId)){
					isExits=true;
					break;
				}
			}
			if(!isExits){
				mergeSubjectList.add(addSubjectId);
			}
		}
		return mergeSubjectList;
	}
	
	/**
	 * 从List<CellCourseInfo>根据subjectType和subjectId删除指定的记录
	 * */
	public static List<CellCourseInfo> deleteCellCourseInfoBySubject(String subjectId,Integer subjectType,List<CellCourseInfo> CellCourseInfos){
		CellCourseInfo cellCourseInfo = new CellCourseInfo();
		for(CellCourseInfo CellCourseInfo:CellCourseInfos){
			if(subjectId.equals(CellCourseInfo.getSubjectId())&&subjectType.equals(CellCourseInfo.getSubjectType())){
				cellCourseInfo = CellCourseInfo;
				break;
			}
		}
		
		CellCourseInfos.remove(cellCourseInfo);
		return CellCourseInfos;
	}
	/**
	 * 判断一个格子中的值CellCourseInfos中是否有重复数据(subjectId和SubjectType一样认为是重复数据)
	 * */
	public static Boolean ifExistRepeatData(List<CellCourseInfo> CellCourseInfos){
		Boolean ifExistRepeatData = false;
		Map<String,String> subjectMap =new HashMap<String,String>();
		for(CellCourseInfo cellCourseInfo:CellCourseInfos){
			String Key = cellCourseInfo.getSubjectId()+cellCourseInfo.getSubjectType();
			if(subjectMap.containsKey(Key)){
				ifExistRepeatData=true;
				break;
			}
			subjectMap.put(Key, Key);
		}
		return ifExistRepeatData;
	}
	/**
	 * 根据最后两个学选交叉时间点的信息，判断是否需要有学考调换时间点
	 * */
	public static Boolean ifNeedChangeProTime(Integer optMaxNum,Table<Integer,Integer,List<CellCourseInfo>> courseArrangement){
		Boolean ifNeedChangeProTime = false;
		Map<String,String> optSubjects = new HashMap<String,String>();
		Map<String,String> lastProSubjects = new HashMap<String,String>();
		Map<String,String> lastButOneProSubjects = new HashMap<String,String>();
		List<String> sameProSubjectList = new ArrayList<String>();
		Integer optSubjectNum =0;
		for(int i=0;i<=1;i++){
			for(CellCourseInfo CellCourseInfo:courseArrangement.row(1).get(optMaxNum-i)){
				//如果是选考
				if(CellCourseInfo.getSubjectType()==optType){
					//因为选考有两个序列，做一下判重(主要是针对optSubjectNum已经存在的话不能再+1)
					if(!optSubjects.containsKey(CellCourseInfo.getSubjectId())){
						//主要是拿Key做判重，所以Value的值随意写
						optSubjects.put(CellCourseInfo.getSubjectId(), CellCourseInfo.getSubjectId());
						optSubjectNum++;
					}
				}
				
				//如果是学考
				else if(CellCourseInfo.getSubjectType()==proType){
					if(i==0){
						lastProSubjects.put(CellCourseInfo.getSubjectId(), CellCourseInfo.getSubjectId());
					}else if(i==1){
						lastButOneProSubjects.put(CellCourseInfo.getSubjectId(), CellCourseInfo.getSubjectId());
					}
				}
			}
		}
		
		//遍历lastProSubjects的Key，如果在lastButOneProSubjects中有，就是两个时间点都有的学考科目
		for(Map.Entry<String,String> entry:lastProSubjects.entrySet()){
			if(lastButOneProSubjects.containsKey(entry.getKey())){
				sameProSubjectList.add(entry.getKey());
			}
		}
		
		for(String sameProSubject:sameProSubjectList){
			Integer optSubjectNum2 = optSubjectNum;
			if(optSubjects.containsKey(sameProSubject)){
				optSubjectNum2--;
			}
			if(optSubjectNum2 > 2){
				ifNeedChangeProTime = true;
			}
		}
		
		return ifNeedChangeProTime;
	}
	
	/**
	 * 打印
	 * */
	public static void print(Integer row,Integer column,Table<Integer, Integer, List<CellCourseInfo>> courseArrangement){
		for(int i=1;i<=row;i++){
			for(int j=1;j<=column;j++){
				List<String> optSubjectIds = new ArrayList<String>();
				List<String> proSubjectIds = new ArrayList<String>();
				List<CellCourseInfo> cellCourseInfos = new ArrayList<CellCourseInfo>();
				cellCourseInfos = courseArrangement.row(i).get(j);
				for(CellCourseInfo cellCourseInfo:cellCourseInfos){
					if(cellCourseInfo.getSubjectType()==1){
						//optSubjectIds.add(cellCourseInfo.getSubjectId());
						optSubjectIds.add("科目:"+cellCourseInfo.getSubjectId()+"  行号:"+cellCourseInfo.getCellRowNum());
					}else if(cellCourseInfo.getSubjectType()==2){
						//proSubjectIds.add(cellCourseInfo.getSubjectId());
						proSubjectIds.add("科目:"+cellCourseInfo.getSubjectId()+"  行号:"+cellCourseInfo.getCellRowNum());
					}
				}
				
				   Collections.sort(optSubjectIds, new Comparator<Object>() {
						@Override
						public int compare(Object o1, Object o2) {
							String str1=(String) o1;
							String str2=(String) o2;
					        if (str1.compareToIgnoreCase(str2)<0){  
					            return -1;  
					        }  
					        return 1;  
						}
				   });


				   Collections.sort(proSubjectIds, new Comparator<Object>() {
						@Override
						public int compare(Object o1, Object o2) {
							String str1=(String) o1;
							String str2=(String) o2;
					        if (str1.compareToIgnoreCase(str2)<0){  
					            return -1;  
					        }  
					        return 1;  
						}
				   });
				System.out.println("第 "+i+" 行,第 "+j+" 列的数据为：");
					System.out.print("选考科目：");
					if(optSubjectIds.size()>0){
						for(String optSubjectId :optSubjectIds){
							System.out.print(optSubjectId+",");
						}
					}
					System.out.println();
				
					System.out.print("学考科目：");
					if(proSubjectIds.size()>0){
						for(String proSubjectId :proSubjectIds){
							System.out.print(proSubjectId+",");
						}	
					}
					System.out.println();
					
				if(proSubjectIds.size()+optSubjectIds.size()>0){
					System.out.println();
				}
			}
			
			System.out.println("——————————————————下一行数据——————————————————");
		}
	}
}
