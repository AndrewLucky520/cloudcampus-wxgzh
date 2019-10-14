package com.talkweb.placementtask.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.talkweb.placementtask.domain.CellClassInfo;
import com.talkweb.placementtask.domain.CellCourseInfo;
import com.talkweb.placementtask.domain.WishInfo;

public class ClassArrangement {
	//☆☆☆选考分组数量(6选3和7选3都是3)
	static Integer optNum = 3;
	//☆☆☆学考分组数量(6选3为3,7选3为4)
	static Integer proNum = 3;
	//选考类型
	final static Integer optType = 1;	
	//学考类型
	final static Integer proType = 2;	
	@SuppressWarnings("unchecked")
	@Transactional
	public static void main(String args[]){

		//☆☆☆走班能用的课时(总学时-非走班课时)
		Integer enablePeriod = 0;
		//☆☆☆走班需要的最小课时(用选-学的课时差值排序，取三个最大的值然后加上所有学考的值)
		Integer minPeriod = 0;
		//☆☆☆走班课程平铺下来的课时(选考最大课时*optNum+学考最大课时*proNum)
		Integer maxPeriod = 0;
		//☆☆☆选考科目信息Map,Key为科目id,Value为科目的学时(不上为0),用来初始化平铺课时用
		Map<String,Integer> optCourseNum = new HashMap<String,Integer>();
		//TODO ☆☆☆选考最大课时(这个值主要是用来做选考的最大序列号，如果出现折叠的话要做调整)
		Integer optMaxNum = 0;
		//☆☆☆学考科目信息Map,Key为科目id,Value为科目的学时(不上为0),用来初始化平铺课时用
		Map<String,Integer> proCourseNum = new HashMap<String,Integer>();
		//TODO ☆☆☆学考最大课时(这个值主要是用来做选考的最大序列号，如果出现折叠的话要做调整)
		Integer proMaxNum = 0;
		
		
		//TODO 下面参数都是初始值的准备数据
		//组合信息,直接从选科数据中获取过来
		List<WishInfo> wishInfoList = new ArrayList<WishInfo>();
		//是否有学选交叉
		Boolean ifCross = false;
		//最后一个学考是否有时间点调换
		Boolean ifProOrderChange = false;
		//前面方法中的学选安排方式传进来
		Table<Integer, Integer, List<CellCourseInfo>> courseArrangement = HashBasedTable.create();
		
		//TODO 下面的参数是需要单独传进来的
		Integer classSize = 0;
		Integer maxClassSize = 0;
		
		
		
		
		
		//得到有志愿学生的数量
		Integer allStudentNum = 0;
		try{
			for(WishInfo wishInfo:wishInfoList){
				allStudentNum += wishInfo.getStudentList().size();
			}
			
			//学选交叉信息，key为学考序列+,+学考科目id,Value为选考序列+,+选考科目id的List(主要是拿学考信息来找对应选考)
			Map<String,List<String>> crossInfo = new HashMap<String,List<String>>();
			if(ifCross){
				crossInfo = queryCrossInfo(optNum,optMaxNum,courseArrangement);
			}
			
			//如果有时间点调换的时候，生成每个组合不能安排的情况，Key为组合名，Value分别为选1，选2，选3安排的课程信息。注意这里是保存的不能安排情况
			Map<String,List<Map<Integer,String>>> wishUnableArrangeInfo = new HashMap<String,List<Map<Integer,String>>>();
			if(ifProOrderChange){
				wishUnableArrangeInfo = wishUnableArrangeInfo(optNum,optMaxNum,wishInfoList,courseArrangement);
			}
			
			//各科目班级人数(Key为科目id,Value为人数)
			Map<String,Integer> optsubjectStuNum = new HashMap<String,Integer>();
			for(WishInfo wishInfo:wishInfoList){
				for(String subjectId:wishInfo.getWishSubjectList()){
					if(!optsubjectStuNum.containsKey(subjectId)){
						optsubjectStuNum.put(subjectId, 0);
					}
					Integer stuNum = optsubjectStuNum.get(subjectId);
					stuNum += wishInfo.getStudentList().size();
					optsubjectStuNum.put(subjectId,stuNum);
				}
			}
			//生成每个科目最优人数以及班级个数
			Map<String,Map<String,Integer>> optsubjectBestClassInfo = CalculationBestClassInfo(classSize,maxClassSize,optsubjectStuNum);
			Map<String,Integer> prosubjectStuNum = new HashMap<String,Integer>();
			for(Map.Entry<String,Integer> entry:optsubjectStuNum.entrySet()){
				prosubjectStuNum.put(entry.getKey(), allStudentNum-entry.getValue());
			}
			Map<String,Map<String,Integer>> prosubjectBestClassInfo = CalculationBestClassInfo(classSize,maxClassSize,prosubjectStuNum);
			
			//计算科目在各时间点安排班级数(Key为subjectId，Value为指定时间点对应的班级数，没有要填写0)
			Map<String,Map<Integer,Integer>> optsubjectOrderClassNum = querySubjectOrderClassNum(optNum+proNum,optNum,optCourseNum,optsubjectBestClassInfo);
			Map<String,Map<Integer,Integer>> prosubjectOrderClassNum = querySubjectOrderClassNum(optNum+proNum,proNum,proCourseNum,prosubjectBestClassInfo);
			
			
			JSONObject optWishArrayInfo = creatWishSubjectArrange(optType,optCourseNum,wishInfoList);
			JSONObject proWishArrayInfo = creatWishSubjectArrange(proType,proCourseNum,wishInfoList);
			
			
			//新建一个班级Table，用来保存每个序列(选1到学3分别用1-optNum+proNum代替)，列为科目id(科目和前面optWishArrayInfo、proWishArrayInfo合并去重后的科目id)，
			Table<Integer,String,List<CellClassInfo>> classArrangement = HashBasedTable.create();
			//TODO 分班逻辑	
			for(int order=1;order<=optNum+proNum;order++){
				//需要分班的科目列表(根据是选考还是学考进行获取)
				List<String> subjectListNew = new ArrayList<String>();
				//记录最新的序列对应组合以及人数情况(Key为序列,从0开始，Value中的值分别为组合id和组合在这个序列的人数)
				Map<Integer,Map<String,Integer>> orderWishInfoListNew = new HashMap<Integer,Map<String,Integer>>();
				//组合在科目中的人数(基准表1)
				Table<Integer,String,Integer>  wishSubjectArrange = HashBasedTable.create();
				//本轮学生的分配情况(每一行是哪个科目被安排)
				Map<Integer,String> orderSubjectArrage = new HashMap<Integer,String>();
				//需要排课的科目才有数据，Map中的Key是科目id，Vlaue是本轮科目能使用人数-最优班额的差值(作为科目安排顺序使用,用Value的值排序)
				List<Map<String,Integer>> subjectArrageRank = new ArrayList<Map<String,Integer>>();
				//选1或者学1是从Json中取值
				if(order ==1){
					orderWishInfoListNew = new HashMap<Integer,Map<String,Integer>>((Map<Integer,Map<String,Integer>>)optWishArrayInfo.get("orderWishInfoList"));
					wishSubjectArrange = HashBasedTable.create((Table<Integer,String,Integer>)optWishArrayInfo.get("wishSubjectArrange"));
				}else if(order ==optNum+1){
					orderWishInfoListNew = new HashMap<Integer,Map<String,Integer>>((Map<Integer,Map<String,Integer>>)proWishArrayInfo.get("orderWishInfoList"));
					wishSubjectArrange = HashBasedTable.create((Table<Integer,String,Integer>)proWishArrayInfo.get("wishSubjectArrange"));
				}else{
					//TODO 每一轮结束之后，将对应信息赋值给一个公共变量，然后这个地方值传递拿过来
					orderWishInfoListNew = new HashMap<Integer,Map<String,Integer>>();
					wishSubjectArrange = HashBasedTable.create();
				}
				
				
				
				
				
				
				
				
			}

			//TODO 保存结果以及验证分班数据准确性
			
		}catch(Exception e){
			throw new RuntimeException("分班失败",e);
		}
		
	}
	
	
	
	
	
	
	/**
	 * 生成学选交叉的Map信息
	 * */
	public static Map queryCrossInfo(Integer optNum,Integer optMaxNum,Table<Integer, Integer, List<CellCourseInfo>> courseArrangement){
		Map<String,List<String>> crossInfo = new HashMap<String,List<String>>();
		try{
			for(int row=1;row<=optNum;row++){
				for(int column=1;column<=optMaxNum;column++){
					List<String> optSubjectInfoList = new ArrayList<String>();
					List<String> proSubjectInfoList = new ArrayList<String>();
					for(CellCourseInfo cellCourseInfo:courseArrangement.get(row,column)){
						if(cellCourseInfo.getSubjectType()==optType){
							optSubjectInfoList.add(cellCourseInfo.getSubjectId()+","+cellCourseInfo.getSubjectType());
						}else if(cellCourseInfo.getSubjectType()==proType){
							proSubjectInfoList.add(cellCourseInfo.getSubjectId()+","+cellCourseInfo.getSubjectType());
						}
					}
					//如果都大于0说明是学选交叉的格子
					if(optSubjectInfoList.size()>0&&proSubjectInfoList.size()>0){
						for(String proSubjectInfo:proSubjectInfoList){
							for(String optSubjectInfo:optSubjectInfoList){
								if(!crossInfo.containsKey(proSubjectInfo)){
									crossInfo.put(proSubjectInfo, new ArrayList<String>());
								}
								List<String> optSubjectInfos = crossInfo.get(proSubjectInfo);
								optSubjectInfos.add(optSubjectInfo);
								crossInfo.put(proSubjectInfo, optSubjectInfos);
							}
						}
					}
				}
			}
		}catch (Exception e){
			throw new RuntimeException("生成学选交叉信息失败",e);
		}
		return crossInfo;
	}
	
	/**
	 * 有时间的点调换的情况下，保存组合不能安排的情况
	 * 只针对最后两列的学选交叉情况做处理
	 * 返回值wishUnableArrangeInfo：Key为组合名，Value分别为选1，选2，选3安排的课程信息。注意这里是保存的不能安排情况
	 * */
	public static Map<String,List<Map<Integer,String>>> wishUnableArrangeInfo(Integer optNum,Integer optMaxNum,List<WishInfo> wishInfoList,Table<Integer, Integer, List<CellCourseInfo>> courseArrangement){
		Map<String,List<Map<Integer,String>>> wishUnableArrangeInfo = new HashMap<String,List<Map<Integer,String>>>();
		try{
			
			//先确定哪些组合可能出现不能排的情况
			//选考两个时间点的科目(Key为科目id,Value为出现次数)
			Map<String,Integer> optAllSubjects = new HashMap<String,Integer>();
			//学考两个时间点的科目(Key为科目id,Value为出现次数)
			Map<String,Integer> proAllSubjects = new HashMap<String,Integer>();
			
			for(int column=optMaxNum-1;column<=optMaxNum;column++){
				for(CellCourseInfo cellCourseInfo:courseArrangement.row(optNum).get(column)){
					String subjectId = cellCourseInfo.getSubjectId();
					Integer subjectType = cellCourseInfo.getSubjectType();
					if(optType==subjectType){
						if(!optAllSubjects.containsKey(subjectId)){
							optAllSubjects.put(subjectId, 0);
						}
						Integer subjectCount =  optAllSubjects.get(subjectId);
						subjectCount++;
						optAllSubjects.put(subjectId, subjectCount);
					}
					if(proType==subjectType){
						if(!proAllSubjects.containsKey(subjectId)){
							proAllSubjects.put(subjectId, 0);
						}
						Integer subjectCount =  proAllSubjects.get(subjectId);
						subjectCount++;
						proAllSubjects.put(subjectId, subjectCount);
					}
				}
			}
			
			//选考两个时间点的科目(Key为科目id,Value为出现次数)
			List<String> optSameSubjectList = new ArrayList<String>();
			List<String> optAllSubjectList = new ArrayList<String>();
			for(Map.Entry<String,Integer> entry:optAllSubjects.entrySet()){
				optAllSubjectList.add(entry.getKey());
				if(entry.getValue()>1){
					optSameSubjectList.add(entry.getKey());
				}
			}
			//学考两个时间点的科目(Key为科目id,Value为出现次数)
			List<String> proSameSubjectList = new ArrayList<String>();
			for(Map.Entry<String,Integer> entry:proAllSubjects.entrySet()){
				if(entry.getValue()>1){
					proSameSubjectList.add(entry.getKey());
				}
			}
			
			//先找出哪些组合会出问题
			List<WishInfo> needToDeleteWishInfo = new ArrayList<WishInfo>();
			for(WishInfo wishInfo:wishInfoList){
				List<String> subjectList = wishInfo.getWishSubjectList();
				if(querySameSubjectList(subjectList,optSameSubjectList).size()<=0){
					//没有选考相同科目的组合不会有绝对冲突
					needToDeleteWishInfo.add(wishInfo);
					break;
				}
				if(querySameSubjectList(subjectList,proSameSubjectList).size()==proSameSubjectList.size()){
					//学考相同的科目全部都有的组合不会有绝对冲突
					needToDeleteWishInfo.add(wishInfo);
					break;
				}
				if(querySameSubjectList(subjectList,optSameSubjectList).size()==querySameSubjectList(subjectList,optAllSubjectList).size()){
					//选考科目只有两个时间点都有的组合不会有绝对冲突
					needToDeleteWishInfo.add(wishInfo);
					break;
				}
			}
			//将不会有绝对冲突的组合删掉
			for(WishInfo wishInfo:needToDeleteWishInfo){
				wishInfoList.remove(wishInfo);
			}
			
			//判断剩下的每种组合在什么安排情况下会有冲突
			for(WishInfo wishInfo:wishInfoList){
				Table<Integer, Integer, Boolean> courseConflict = HashBasedTable.create();
				//初始化所有的都为true,true标识能用
				for(int row=1;row<=optNum;row++){
					for(int column=1;column<=2;column++){
						courseConflict.put(row, column, true);
					}
				}
				List<String> subjectList = wishInfo.getWishSubjectList();
				for(int row = 1;row<=optNum;row++){
					for(int column=optMaxNum-1;column<=optMaxNum;column++){
						for(CellCourseInfo cellCourseInfo:courseArrangement.row(row).get(column)){
							if(subjectList.get(row).equals(cellCourseInfo.getSubjectId())&&cellCourseInfo.getSubjectType()==optType){
								courseConflict.put(row,column-(optMaxNum-2),false);
							}
						}
					}
				}
				List<Map<Integer,String>> UnableArrangeList = new ArrayList<Map<Integer,String>>();
				if((courseConflict.get(1,1)&&courseConflict.get(2,2))||(courseConflict.get(2,1)&&courseConflict.get(3,2))||(courseConflict.get(3,1)&&courseConflict.get(1,2))){
					//这个顺序能安排,需要反过来保存进Map
					List<String> UnableArrangeOrderList = new ArrayList<String>();
					for(int i=1;i<=2;i++){
						for(int j=subjectList.size()-1;i>=0;i--){
							UnableArrangeOrderList.add(subjectList.get(j));
						}
					}
					for(int i=0;i<optNum;i++){
						Map<Integer,String> UnableArrange = new HashMap<Integer,String>();
						UnableArrange.put(1, UnableArrangeOrderList.get(i));
						UnableArrange.put(2, UnableArrangeOrderList.get(i+1));
						UnableArrange.put(3, UnableArrangeOrderList.get(i+2));
						UnableArrangeList.add(UnableArrange);
					}
					wishUnableArrangeInfo.put(wishInfo.getWishzhId(), UnableArrangeList);
				}else{
					//这个顺序有问题，直接保存进Map
					List<String> UnableArrangeOrderList = new ArrayList<String>();
					for(int i=1;i<=2;i++){
						for(int j=0;i<=subjectList.size()-1;i++){
							UnableArrangeOrderList.add(subjectList.get(j));
						}
					}
					for(int i=0;i<optNum;i++){
						Map<Integer,String> UnableArrange = new HashMap<Integer,String>();
						UnableArrange.put(1, UnableArrangeOrderList.get(i));
						UnableArrange.put(2, UnableArrangeOrderList.get(i+1));
						UnableArrange.put(3, UnableArrangeOrderList.get(i+2));
						UnableArrangeList.add(UnableArrange);
					}
					wishUnableArrangeInfo.put(wishInfo.getWishzhId(), UnableArrangeList);
				}
			}
		}catch (Exception e){
			throw new RuntimeException("生成组合不能安排情况失败",e);
		}
		
		return wishUnableArrangeInfo;
	}
	
	/**
	 * 根据组合id获取指定组合对象
	 * */
	public static WishInfo queryWishInfo(String wishzhId,List<WishInfo> wishInfoList){
		WishInfo appointWishInfo = new WishInfo();
		for(WishInfo wishInfo:wishInfoList){
			if(wishzhId.equals(wishInfo.getWishzhId())){
				appointWishInfo =wishInfo;
				break;
			}
		}
		return appointWishInfo;
	}
	
	/**
	 *   查询两个科目List中相同的科目List
	 * */
	public static List<String> querySameSubjectList(List<String> subjectList1,List<String> subjectList2){
		List<String> sameSubjectList = new ArrayList<String>();
		for(String subject1:subjectList1){
			for(String subject2:subjectList2){
				if(subject1.equals(subject2)){
					sameSubjectList.add(subject1);
				}
			}
		}
		return sameSubjectList;
	}
	/**
	 * 各科目最优班级个数以及班额计算
	 * 传参：前端设置的班额、最大班额、各科目人数(Key为科目id，Value为学生数)
	 * */
	public static Map<String,Map<String,Integer>> CalculationBestClassInfo(Integer classSize,Integer maxClassSize,Map<String,Integer> subjectStuNum){
		Map<String,Map<String,Integer>> subjectBestClassInfos = new HashMap<String,Map<String,Integer>>();
		try{
			//先计算每个科目直接相除得到的班级数(小数)
			Map<String,Float> subjectClassInfo = new HashMap<String,Float>();
			Map<String,Integer> subjectClassInfoNew = new HashMap<String,Integer>();
			for(Map.Entry<String,Integer> entry:subjectStuNum.entrySet()){
				Float classNum =  (float)entry.getValue()/(float)classSize;
				subjectClassInfo.put(entry.getKey(), classNum);
			}
			//把科目的班级数小数位放到subjectClassRemainList里面，进行排序
			List<Map<String,Object>> subjectClassRemainList = new ArrayList<Map<String,Object>>();
			for(Map.Entry<String,Float> entry :subjectClassInfo.entrySet()){
				Map<String,Object> subjectClassRemain = new HashMap<String,Object>();
				subjectClassRemain.put("subjectId",entry.getKey());
				subjectClassRemain.put("classNum",entry.getValue()%1);
				subjectClassRemainList.add(subjectClassRemain);
			}
			//升序排列
			Collections.sort(subjectClassRemainList, new Comparator<Map<String,Object>>(){
				@Override
				public int compare(Map<String,Object> o1, Map<String,Object> o2) {
				Float gradeId1 = (float) o1.get("classNum");
				Float gradeId2 = (float) o2.get("classNum");
				if(gradeId1>=gradeId2){
					return 1;
				}else{
					return -1;
				 } 
				}
			});
			//取小数最小的三个，向下取整(需要注意不能超过最大班额，如果超过的话这个科目不能向下取整)
			Integer count = 0;
			for(Map<String,Object> subjectClassRemain:subjectClassRemainList){
				if(count<3){
					String subjectId = subjectClassRemain.get("subjectId").toString();
					Float classNum = (Float) subjectClassRemain.get("classNum");
					Integer subjectNum = subjectStuNum.get(subjectId);
					Float classNumNew = (float)subjectNum/(float)(Math.floor(classNum));
					if(classNumNew<=maxClassSize){
						subjectClassInfoNew.put(subjectId,(int) Math.ceil(classNum));
						count++;
					}
				}
			}
			//将其他科目向上取整
			for(Map.Entry<String,Float> entry:subjectClassInfo.entrySet()){
				if(!subjectClassInfoNew.containsKey(entry.getKey())){
					subjectClassInfoNew.put(entry.getKey(), (int) Math.ceil(entry.getValue()));
				}
			}
			//计算每个科目最优班额,四舍五入,并且写进subjectBestClassInfos
			for(Map.Entry<String,Integer> entry:subjectClassInfoNew.entrySet()){
				Integer subjectNum = subjectStuNum.get(entry.getKey());
				Integer subjectStuBestNum = Math.round((float)subjectNum/(float)(entry.getValue()));
				if(subjectStuBestNum>maxClassSize){
					subjectStuBestNum--;
				}
				Map<String,Integer> subjectBestClassInfo = new HashMap<String,Integer>();
				subjectBestClassInfo.put("classNum", entry.getValue());
				subjectBestClassInfo.put("stuNum", subjectStuBestNum);
				subjectBestClassInfos.put(entry.getKey(), subjectBestClassInfo);
			}	
		}catch (Exception e){
			throw new RuntimeException("计算科目班级数以及最优班额失败！",e);
		}
		return subjectBestClassInfos;
	}
	/**
	 * 计算科目在各时间点的开班数
	 * 参数科目总数：optNum+proNum；本类型序列数量(选考是optNum，学考是proNum)，对应分班类型的最优班级、人数Map
	 * */
	public static Map<String,Map<Integer,Integer>> querySubjectOrderClassNum(Integer subjectNum,Integer orderNum,Map<String,Integer> CourseNum,Map<String,Map<String,Integer>> optsubjectBestClassInfo){
		Map<String,Map<Integer,Integer>> subjectOrderClassNum = new HashMap<String,Map<Integer,Integer>>();
		try{
			//先初始化到List里面的值
			//再相加，每个科目完成的时候记住最后停留的i位置(i为list的序列)
			for(int j =4;j<subjectNum+4;j++){
				if(CourseNum.get(j)>0){
					Map<Integer,Integer> subjectClassNumList = new HashMap<Integer,Integer>();
					for(int i=1;i<=orderNum;j++){
						subjectClassNumList.put(i,0);
					}
					if(j<10){
						String subjectId = j+"";
						subjectOrderClassNum.put(subjectId, subjectClassNumList);
					}else if(j==10){
						subjectOrderClassNum.put("19", subjectClassNumList);
					}	
				}
			}
			//先生成每个科目对应的班级数量表
			Map<String,Integer> subjectClassNum = new HashMap<String,Integer>();
			for(Map.Entry<String,Map<String,Integer>> entry:optsubjectBestClassInfo.entrySet()){
				if(CourseNum.get(entry.getKey())>0){
					subjectClassNum.put(entry.getKey(), entry.getValue().get("classNum"));
				}
			}
			//再根据每个科目的班级个数进行遍历插入数据(Order是下一个班级要插入的位置)
			Integer order =1;
			for(Map.Entry<String,Integer> entry:subjectClassNum.entrySet()){
				String subjectId = entry.getKey();
				Integer classNum = entry.getValue();
				Map<Integer,Integer> orderClassNum  = subjectOrderClassNum.get(subjectId);
				for(int i=1;i<=classNum;i++){
					if(order==orderNum+1){
						order=1;
					}
					orderClassNum.put(order, orderClassNum.get(order)+1);
					order++;
				}
				subjectOrderClassNum.put(subjectId,orderClassNum);
			}
			
		}catch(Exception e){
			throw new RuntimeException("计算科目在各时间点的开班数失败！",e);
		}
		return subjectOrderClassNum;
	}
	
	
	
	/**
	 * 生成组合的课程安排情况
	 * 参数：班级类型(学选)、志愿组合的学生信息、科目对应类型的课时信息(主要是考虑到学考的时候，有些科目可能不上课用，不上课会传0)
	 * 生成的Table除了组合对应科目的人数外，还冗余一列可用位置的参数(canUseNum)、行信息、列信息
	 * 最下面的总计不显示在这地方，单独做一个Map(这个地方会因为拆分新加行，不方便显示总计)
	 * */
	public static JSONObject creatWishSubjectArrange(Integer Type,Map<String,Integer> CourseNum,List<WishInfo> wishInfoList){
		JSONObject Json = new JSONObject();
		try{
			Table<Integer,String,Integer> wishSubjectArrange = HashBasedTable.create();
			//科目列表
			List<String> subjectList = new ArrayList<String>();
			//TODO 结果需要调整，记录当前的组合分配情况，Key为序列从0开始，Value中的Key分别为"wishId"、"orderNum"、"subjects"，保存志愿id、行人数、还未安排的志愿List
			Map<Integer,Map<String,Integer>> orderWishInfos = new HashMap<Integer,Map<String,Integer>>();
			for(Map.Entry<String,Integer> entry:CourseNum.entrySet()){
				if(entry.getValue()>0){
					subjectList.add(entry.getKey());	
				}
			}
			Integer rowNum = 0;
			if(Type==optType){
				for(WishInfo wishInfo:wishInfoList){
					for(String subjectId1:subjectList){
						Boolean ifArrage = false;
						for(String subjectId2:wishInfo.getWishSubjectList()){
							if(subjectId1.equals(subjectId2)){
								wishSubjectArrange.put(rowNum, subjectId1, wishInfo.getStudentList().size());
								ifArrage = true;
								break;
							}
						}
						if(!ifArrage){
							wishSubjectArrange.put(rowNum, subjectId1,0);
						}
					}
					wishSubjectArrange.put(rowNum,"orderWishNum", wishInfo.getStudentList().size());
					Map<String,Integer> orderWishInfo = new HashMap<String,Integer>();
					orderWishInfo.put(wishInfo.getWishzhId(), wishInfo.getStudentList().size());
					orderWishInfos.put(rowNum, orderWishInfo);
					rowNum++;
				}
			}else if(Type==proType){
				for(WishInfo wishInfo:wishInfoList){
					for(String subjectId1:subjectList){
						Boolean ifArrage = false;
						for(String subjectId2:wishInfo.getWishSubjectList()){
							if(!(subjectId1.equals(subjectId2))){
								wishSubjectArrange.put(rowNum, subjectId1, wishInfo.getStudentList().size());
								ifArrage = true;
								break;
							}
						}
						if(!ifArrage){
							wishSubjectArrange.put(rowNum, subjectId1,0);
						}
					}
					wishSubjectArrange.put(rowNum,"orderWishNum", wishInfo.getStudentList().size());
					Map<String,Integer> orderWishInfo = new HashMap<String,Integer>();
					orderWishInfo.put(wishInfo.getWishzhId(), wishInfo.getStudentList().size());
					orderWishInfos.put(rowNum, orderWishInfo);
					rowNum++;
				}
			}
			
			wishSubjectArrange = UpdateCanUseInfo(subjectList,orderWishInfos,wishSubjectArrange);
			Json.put("subjectList",subjectList);
			Json.put("orderWishInfos",orderWishInfos);
			Json.put("wishSubjectArrange", wishSubjectArrange);		
		}catch(Exception e){
			throw new RuntimeException("生成组合的课程安排情况失败！",e);
		}
		return Json;
	}
	
	/**
	 * 更新可用位置信息
	 * */
	public static Table<Integer,String,Integer> UpdateCanUseInfo(List<String> subjectList,Map<Integer,Map<String,Integer>> orderWishInfos,Table<Integer,String,Integer> wishSubjectArrange){
		Integer canUseNum = 0;
		for(Map.Entry<Integer,Map<String,Integer>>entry :orderWishInfos.entrySet()){
			Integer row =entry.getKey();
			for(String subjectId:subjectList){
				if(wishSubjectArrange.row(row).get(subjectId)>0){
					canUseNum++;
				}
			}
			wishSubjectArrange.put(row, "canUseNum", canUseNum);
		}
		return wishSubjectArrange;
	}
}
