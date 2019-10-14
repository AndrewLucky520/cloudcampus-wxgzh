package com.talkweb.placementtask.utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

public class DezyGenClassAlg {
	Map<String,String> hafManCodeMap = new HashMap<String,String>(); 
	static String courseArray[] = {"政治","历史","地理","物理","化学","生物","技术"};
	static String courseHafCodes[] = {"01100","01101","0111","010","10","11","00"};
	Map<String,String> codeNameMap = new HashMap<String,String>();
	Map<String,String> codeFullNameMap = new HashMap<String,String>();
	
	//分班结果（用于判断分班不出而退出分班的情况）
	Map<String,String> divGroupResult = new HashMap<String,String>();
	
	//算法参数	
	static final int LOOP = 100;	//打乱频率（重分）
	int CLASS_CAPACITY_MIN,CLASS_CAPACITY_MAX;	//班级容量区间(根据志愿数可以调整)
	int CLASS_ROUND_NUM;
	int SHREDSHOLD;
	//（目标）分班数
	int targetDivClassNum = 0;
	//放宽人数限制
	int broadenWidth = 0;
	int largeWishingOfStu = 0;
	String techId = null;
	
	//分班方案（1:不跨组方案；2：跨组方案）
	int placeAlgMethod = 1;
	
	public void setPlaceAlgMethod(int placeAlgMethod) {
		this.placeAlgMethod = placeAlgMethod;
	}

	public void setTechId(String techId) {
		this.techId = techId;
	}

	//初始化参数
	public DezyGenClassAlg(int stuInClassMax,int stuInClassMin, int classRoundNum,long shredshold){
		this.CLASS_CAPACITY_MAX = stuInClassMax;
		this.CLASS_CAPACITY_MIN = stuInClassMin;
		this.CLASS_ROUND_NUM = classRoundNum;		
		this.SHREDSHOLD = (int)shredshold;
		
		this.largeWishingOfStu = stuInClassMax;
	}
	
	public int getLargeWishingOfStu() {
		return largeWishingOfStu;
	}

	public int getBroadenWidth() {
		return broadenWidth;
	}
	public void setBroadenWidth(int broadenWidth) {
		this.broadenWidth = broadenWidth;
	}
	public void setTargetDivClassNum(int targetDivClassNum) {
		this.targetDivClassNum = targetDivClassNum;
	}

	public int getSHREDSHOLD() {
		return SHREDSHOLD;
	}

	public int getCLASS_CAPACITY_MIN() {
		return CLASS_CAPACITY_MIN;
	}

	public int getCLASS_CAPACITY_MAX() {
		return CLASS_CAPACITY_MAX;
	}

	/*static*/ List<Map.Entry<String,Integer>> wishingMapList;
	public List<Map.Entry<String, Integer>> getWishingMapList() {
		return wishingMapList;
	}

	public void setWishingMapList(List<Map.Entry<String, Integer>> wishingMapList) {
		this.wishingMapList = wishingMapList;
	}

	Map<String, Integer> originWishingMap = new HashMap<String,Integer>();
	/*static */Map<String,String> subjectIdHafCodeMap = new HashMap<String,String>();
	/*static */Map<String,String> hafCodeSubjectIdMap = new HashMap<String,String>();
	/*static */Map<String,String> hafCodeSubjectNameMap = new HashMap<String,String>();
	/*static */List<Map<String,Integer>> commonWishingGroups = new ArrayList<Map<String,Integer>>();
	/*static */Map<String,String> hafCodeWishingIdMap = new HashMap<String,String>();
	/*static*/ Map<String,String> hafCodeWishingNameMap = new HashMap<String,String>();
	Map<String, Integer> classWisthSingleWishings = new HashMap<String,Integer>();
	
	//实验班(科目组合Id,实验班志愿Map)
	List<MapEntry<String,Map<String,Integer>>> expClassList ;

	/**
	 * 注：必须先于分班调用
	 * 设置实验班,并更改targetClsNum,wishingMapList,originWishingMap,SHREDSHOLD等参数
	 * @param tclassIdWishingMap(key:expSubs,value<subjects,stuNum>)
	 * @param expClsNum 行政班级数
	 **/
	public void setExpClass(List<Entry<String,Map<String,Integer>>> expSubWishingList, int expClsNum){
		expClassList = new ArrayList<MapEntry<String,Map<String,Integer>>>();		
		
		//添加当前班级的实验班志愿
		for(Entry<String,Map<String,Integer>>expSubWishingEntry : expSubWishingList){
			String subs = expSubWishingEntry.getKey();
			String[] subList = subs.split(",");
			Map<String,Integer> cls = expSubWishingEntry.getValue();
			
			MapEntry<String,Map<String,Integer>> expClsEntry = new MapEntry<String,Map<String,Integer>>(subs,null);
			//添加（过滤）当前行政班下的志愿
			Iterator<Entry<String, Integer>> clsIter = cls.entrySet().iterator();
			nextWishing:
			while(clsIter.hasNext()){
				Entry<String, Integer> clsEntry = clsIter.next();
				String subWishing = clsEntry.getKey();
				List<String> subWishingList = Arrays.asList(subWishing.split(","));
				for(String sub : subList){
					if(!subWishingList.contains(sub)){
						continue nextWishing;
					}
				}
				
				Map<String, Integer> expCls = expClsEntry.getValue();
				if(null==expCls){
					expCls = new HashMap<String,Integer>();
					expClsEntry.setValue(expCls);
					expClassList.add(expClsEntry);
				}
				expCls.put(getWishingIdBySubjectIds(subWishing), cls.get(subWishing));
				clsIter.remove();
			}	
		}
		
		//移除已经进入实验班的志愿
		Iterator<Entry<String,Map<String,Integer>>> expGrpIter = expSubWishingList.iterator();
		while(expGrpIter.hasNext()){
			Entry<String, Map<String, Integer>> expGrpEntry = expGrpIter.next();			
			if(expGrpEntry.getValue().size()==0){
				expGrpIter.remove();
			}
		}
		
		//添加其它剩余实验班志愿(补偿实验班人数)
		if(expSubWishingList.size()>0 && expClassList.size()>0){		
												
			//当前待添加人数的实验班
			Map<String,Integer> curDealCls = null;
			do{	
				
				//待添加人数的实验班(人数最少)
				Entry<String,Map<String, Integer>> minStuCls 
					= getNextMinStuCls(expClassList, getTotalStuNumInWishingGroup(curDealCls));				
				
				//实验班人数超额/迭代完成
				if(minStuCls==null){
					break;
				}
				curDealCls = minStuCls.getValue();
				if(getTotalStuNumInWishingGroup(curDealCls)>CLASS_CAPACITY_MAX){
					break;
				}			
				
				String minStuClsSubs = minStuCls.getKey();

				//判断剩余实验班志愿是否可以加入其它实验班
				Iterator<Entry<String, Map<String, Integer>>> expSubWishingIter 
							= expSubWishingList.iterator();
				
				while(expSubWishingIter.hasNext()){	//实验班组
					Entry<String, Map<String, Integer>> expSubWishingEntry = expSubWishingIter.next();					
					Map<String,Integer> expSubWishings = expSubWishingEntry.getValue();		
					Iterator<Entry<String, Integer>>expWishingEntryIter = expSubWishings.entrySet().iterator();
					
					next:
					while(expWishingEntryIter.hasNext()){	//实验班
						Entry<String, Integer> expWishingEntry = expWishingEntryIter.next();
						String subs = expWishingEntry.getKey();
						
						//匹配
						List<String> subList = Arrays.asList(subs.split(","));
						for(String sub : minStuClsSubs.split(",")){
							if(!subList.contains(sub)){
								continue next;	//迭代下一志愿
							}
						}
						
						//添加
						curDealCls.put(getWishingIdBySubjectIds(subs), expWishingEntry.getValue());
						
						//删除剩余志愿和剩余志愿组
						expWishingEntryIter.remove();
						if(expSubWishings.size()==0){
							expSubWishingIter.remove();
						}
					}//end 实验班
				}	//end 实验班组
			}while(true);
			
		}
		
		
		//修改平行班分班数量
		targetDivClassNum -= expClassList.size();
		
		//修改分班参数
		for(Entry<String,Map<String,Integer>>expClassMapEntry : expClassList){			
			Map<String, Integer> expCls = expClassMapEntry.getValue();			
			for(String subs : expCls.keySet()){
				//修改originWishingMap
				Integer stuNum = expCls.get(subs);
				Integer stuNumInOri = originWishingMap.get(subs);
				if(null!=stuNum && null!=stuNumInOri){
					if(stuNum>=stuNumInOri){
						originWishingMap.remove(subs);	
					}else{
						originWishingMap.put(subs, stuNumInOri - stuNum);
					}
				}
			}
		}
		
		//修改Shredshould、CLASS_CAPACITY_MAX等参数
		int stuInOriCls = getTotalStuNumInWishingGroup(originWishingMap);
		SHREDSHOLD = (stuInOriCls%targetDivClassNum==0)?
				stuInOriCls/targetDivClassNum:stuInOriCls/targetDivClassNum+1; 
		CLASS_CAPACITY_MAX = SHREDSHOLD + CLASS_ROUND_NUM;
		CLASS_CAPACITY_MIN = SHREDSHOLD - CLASS_ROUND_NUM;
		
		//修改wishingMapList
		retainOrigin();
	}
	/**
	 * 获取最小人数实验班
	 * @param expClassList
	 * @return
	 */
	private Entry<String,Map<String,Integer>> getNextMinStuCls(List<MapEntry<String, Map<String, Integer>>> expClassList,int curStuNumInCls){
		int suitableStuNumMax = 0;
		Entry<String,Map<String,Integer>> minStuCls = null;
		for(Entry<String, Map<String,Integer>> cls : expClassList){
			int stuNumInWishing = getTotalStuNumInWishingGroup(cls.getValue());
			if(stuNumInWishing>curStuNumInCls && suitableStuNumMax==0){
				suitableStuNumMax = stuNumInWishing;
				minStuCls = cls;
				continue;
			}
			
			if(stuNumInWishing>curStuNumInCls && stuNumInWishing<suitableStuNumMax){
				suitableStuNumMax = stuNumInWishing;
				minStuCls = cls;
			}
		}
		return minStuCls;
	}
	
	/**
	 * 获取行政班级列表
	 * @return
	 */
	public List<MapEntry<String,Map<String,Integer>>> getExpClassList() {
		return expClassList;
	}

	//分班不出来,剔除掉的剩余志愿
	Map<String,Integer> divLeftWishings = new HashMap<String,Integer>();
	
	
	public Map<String, Integer> getDivLeftWishings() {
		return divLeftWishings;
	}

	public Map<String, Integer> getClassWisthSingleWishings(List<Entry<String, Integer>> wishingMapList) {
		splitOverNumberWishings2(convertToMap(wishingMapList));
		return classWisthSingleWishings;
	}

	//public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/*for(int i=0; i<=courseArray.length; i++){
			hafManCodeMap.put(courseHafCodes[i], courseArray[i]);			
		}*/
		//classQueue = new LinkedList<Map.Entry<String,Integer>>();
		/*wishingMapList = geneteWishing();
		sort(wishingMapList,true);*/
		
		/*divClass();*/
		/*String group1="0101011+0101001101",group2="101101100+101101101";
		group1 = getCommonCourse(group1);
		group2 = getCommonCourse(group2);*/
		//两两组合-去大端，中间（顺序）
		//addGroup(classQueue, wishingMapList,min,max);
		
		//迭代，直至所有志愿完成组合

		
	//	System.out.println("done");
	//}

	/**
	 * 获取/生成(不存在时)哈夫曼编码对应的志愿Id
	 * @param hafCode
	 * @return
	 */
	public /*static*/ String getWishingIdByCode(String hafCode){
		String wishingId = hafCodeWishingIdMap.get(hafCode);
		if(null==wishingId){
			wishingId = UUID.randomUUID().toString();
			hafCodeWishingIdMap.put(hafCode, wishingId);
		}
		return wishingId;
	}
	
	/**
	 * 
	 * @param subjectIds{4,5,6}
	 * @return	{hafCode}
	 */
	public /*static*/ String getWishingIdBySubjectIds(String subjectIds){
		//String hafCode = null;
		if(!hafCodeSubjectIdMap.containsValue(subjectIds)){
			return null;
		}
		for(String key : hafCodeSubjectIdMap.keySet()){
			if(hafCodeSubjectIdMap.get(key).equals(subjectIds)){
				//hafCode = key;
				return key;
			}
		}
	
		return null;
		//return hafCodeWishingIdMap.get(hafCode);
	}
	/**
	 * 根据志愿id生成hafman编码
	 * @param data
	 * @return
	 */
	public /*static*/ List<Map.Entry<String, Integer>> genWishingList(List<JSONObject> data,Map<String,String> courseIdNameMap,Map<String,String> simpleCourseIdNameMap){
		Map<String,Integer> wishingMap = new HashMap<String,Integer>();		
		wishingMapList = new ArrayList<Map.Entry<String,Integer>>();
		try {			
			int subMark = 0;
			for(JSONObject object : data){
				String subjectIds = object.getString("subjectIds");				
				for(String subjectId : subjectIds.split(",")){
					if(!subjectIdHafCodeMap.containsKey(subjectId)){						
						codeNameMap.put(courseHafCodes[subMark], simpleCourseIdNameMap.get(subjectId));
						codeFullNameMap.put(courseHafCodes[subMark], courseIdNameMap.get(subjectId));
						subjectIdHafCodeMap.put(subjectId, courseHafCodes[subMark++]);
						if(subMark>=courseHafCodes.length){
							break;
						}
					}
				}
			}
			
			for(JSONObject object : data){
				String subjectIds = object.getString("subjectIds");
				String subjectCompName = object.getString("zhName");
				StringBuffer sb = new StringBuffer();
				for(String subjectId : subjectIds.split(",")){
					sb.append(subjectIdHafCodeMap.get(subjectId));					
				}
				hafCodeSubjectIdMap.put(sb.toString(), subjectIds);
				hafCodeSubjectNameMap.put(sb.toString(), subjectCompName);
				int stuNum = object.getIntValue("studentNum");
				wishingMap.put(sb.toString(), stuNum);
				originWishingMap.put(sb.toString(), stuNum);
			}
			
			wishingMapList.addAll(wishingMap.entrySet());
			return wishingMapList;
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	/**
	 * 重新恢复wishingMapList的数据
	 */
	private void retainOrigin(){
		if(originWishingMap==null || originWishingMap.size()==0){
			return;
		}
		
		Map<String,Integer> wishingMap = new HashMap<String,Integer>();		
		for(String key : originWishingMap.keySet()){
			wishingMap.put(new String(key), originWishingMap.get(key));
		}
		
		
		//classWisthSingleWishings.clear();
		//divLeftWishings.clear();
		wishingMapList.clear();
		wishingMapList.addAll(wishingMap.entrySet());
	}
	
	/**
	 * 33个组合
	 * @return
	 */
	public static List<Map.Entry<String,Integer>> geneteWishing(){
		//hafman编码{1(政治)-01100,2(历史)-01101,3(地理)-0111,4(物理)-010,5(化学)-10,6(生物)-11,7(技术)-00}
		Map<String,Integer> wishingMap = new HashMap<String,Integer>();		
		List<Map.Entry<String,Integer>> wishingMapList = new ArrayList<Map.Entry<String,Integer>>();
		
		wishingMap.put("0101011", 35);//物化生-35
		wishingMap.put("0101001101", 13);//物化史-13
		
		
		wishingMap.put("101101100", 35);//化生政-35
		wishingMap.put("101101101", 6);//化生历-6
		
		wishingMap.put("01100011010111", 33);//政史地-33
		wishingMap.put("011000110111", 14);//政史生-14
		wishingMap.put("0110001101010", 4);//政史物-4
		
		wishingMap.put("11011110", 28);//生地化-28
		wishingMap.put("11011100", 11);//生地技-11
		
		wishingMap.put("01100011110", 29);//政地化-29
		wishingMap.put("01100011111", 17);//政地生-17
		
		wishingMap.put("1000010", 26);//化技物-26
		wishingMap.put("100011", 17);//化技生-17
		wishingMap.put("100001101", 9);//化技史-9
		
		wishingMap.put("100111010", 26);//化地物-26
		wishingMap.put("10011100", 16);//化地技-16
		
		wishingMap.put("100110001101", 24);//化政史-24
		wishingMap.put("100110000", 24);//化政技-9
		wishingMap.put("1001100010", 10);//化政物-10
		
		wishingMap.put("01101011110", 26);//史地化-26
		wishingMap.put("01101011100", 11);//史地化-11
		wishingMap.put("01101011111", 16);//史地生-16
		
		wishingMap.put("010011100", 18);//物地技-18
		wishingMap.put("010011101100", 7);//物地政-7
		wishingMap.put("010011101101", 7);//物地史-7
		
		wishingMap.put("01100000111", 18);//政技地-18
		wishingMap.put("0110000010", 8);//政技物-8
		wishingMap.put("011000011", 6);//政技生-6
		wishingMap.put("011000001101", 10);//政技史-10
		
		wishingMap.put("0101100", 18);//物生技-9
		wishingMap.put("0101101100", 18);//物生政-11
		wishingMap.put("0101101101", 18);//物生史-6
		wishingMap.put("010110111", 18);//物生地-9
		
		for(Entry<String, Integer> entry : wishingMap.entrySet()){
			wishingMapList.add(entry);
		}
		
		return wishingMapList;
	}
	
	/**
	 * 判断两个志愿是否包含两个相同科目
	 * @param wishingGroupId1
	 * @param wishingGroupId2
	 */
	public boolean judgeInCommon(String wishingGroupId1,String wishingGroupId2,int commonSubNum, boolean excludeTech){
		//组合志愿判断
		if(wishingGroupId1.contains("+")){
			wishingGroupId1 = getCommonCourse(wishingGroupId1,null);
		}
		if(wishingGroupId2.contains("+")){
			wishingGroupId2 = getCommonCourse(wishingGroupId2,null);
		}
		
		if(StringUtils.isEmpty(wishingGroupId2) 
				|| StringUtils.isEmpty(wishingGroupId1)){
			return false;
		}
		
		int commonCount = 0;		
		List<String> hafCodes = decodeHafmanCode(wishingGroupId1);
		List<String> group2WishingCodes = decodeHafmanCode(wishingGroupId2);
		for(String hafCode : hafCodes){
			if(group2WishingCodes.contains(hafCode)){
				if(excludeTech && hafCode.equals(techId)){
					continue;
				}
				commonCount++;   
			}
		}
		
		return commonCount>=commonSubNum;
	}
	/**
	 * 获取组合志愿之间相同科目（以‘+’号分隔）
	 * @param wishingGroupId
	 * @return hafman编码（两科）
	 */
	public String getCommonCourse(String wishingGroupId,List<String> commonCode){
		if(wishingGroupId.contains("+")){
			StringBuffer sb = new StringBuffer();
			String[] wishingGroupIds = wishingGroupId.split("\\+");
			List<String> hafCodes = commonCode;//decodeHafmanCode(wishingGroupIds[0]);			
			List<String> commonHafCodes = new ArrayList<String>();
			if(null==hafCodes){
				hafCodes = decodeHafmanCode(wishingGroupIds[0]);
			}
			
			for(int i = 1; i<wishingGroupIds.length; i++){
				List<String> hafCodes1 = decodeHafmanCode(wishingGroupIds[i]);
				if(sb.length()>0){
					sb.delete(0, sb.length());
				}
				for(String hafCode : hafCodes1){
					if(hafCodes.contains(hafCode)){
						commonHafCodes.add(hafCode);
						sb.append(hafCode);
					}
				}	
				hafCodes.clear();
				hafCodes.addAll(commonHafCodes);
				commonHafCodes.clear();
			}		
			return sb.toString();
		}
		return "";
	}

	/**
	 * 获取志愿组中相同的志愿代码（hafman）
	 * @param commonCode
	 * @return
	 */
	private String getCommonCourse(Map<String,Integer> commonCode){		
		List<String> wishingIdList = new ArrayList<String>();
		
		if(commonCode.size()==0){
			return "";
		}
		wishingIdList.addAll(commonCode.keySet());
		String wishingId1 = wishingIdList.get(0);
		
		for(int i=1; i<wishingIdList.size(); i++){
			String wishingId2 = wishingIdList.get(i);
			List<String>commonCodes = getCommonHafCodes(wishingId1, wishingId2);
			
			StringBuffer sb = new StringBuffer();
			for(String hafCode : commonCodes){
				sb.append(hafCode);
			}
			wishingId1 = sb.toString();
		}
		
		return wishingId1;
	}
	
	/**
	 * 获取量志愿相同科目代码（hafman）
	 * @param wishingId1
	 * @param wishingId2
	 * @return
	 */
	private List<String> getCommonHafCodes(String wishingId1, String wishingId2){
		List<String>result = new ArrayList<String>();
		if(null==wishingId1 || null== wishingId2){
			return result;
		}
		
		List<String> hafCodes1 = decodeHafmanCode(wishingId1);
		List<String> hafCodes2 = decodeHafmanCode(wishingId2);
		
		for(String hafCode1 : hafCodes1){			
			if(hafCodes2.contains(hafCode1)){
				result.add(hafCode1);
			}
		}
		return result;
	}
	
	
	/**
	 * 生成排序后的志愿名称(例如：政治，历史，地理)
	 */
	public void genSortedWishingName(List<Map<String, Integer>> divResult){		
		Iterator<Map<String, Integer>> wishingIter = divResult.iterator();
		while(wishingIter.hasNext()){
			Map<String, String> subMap = getSortedWishingSubject(wishingIter.next());
			hafCodeWishingNameMap.putAll(subMap);
		}
		
	}
	
	/**
	 * 
	 * @param wishingId
	 * @return {key:wishingId,wishingName(如：政治,地理,化学)}
	 */
	public /*static*/ String getsortedWisingName(String wishingId,List<Map<String, Integer>> divResult){
		String sortedWishingName = null;
		if(0 == hafCodeWishingNameMap.size()){	
			List<Map<String, Integer>> allRs = new ArrayList<Map<String,Integer>>();
			allRs.addAll(divResult);
			if(null!=expClassList && expClassList.size()>0){
				for(Entry<String, Map<String, Integer>> expEntry : expClassList){
					allRs.add(expEntry.getValue());
				}
			}
			genSortedWishingName(allRs);
		}
		
		sortedWishingName = hafCodeWishingNameMap.get(wishingId);
		//没排序的志愿
		if(StringUtils.isEmpty(sortedWishingName)){
			sortedWishingName = hafCodeSubjectNameMap.get(wishingId);			
		}
		return sortedWishingName;
	}
	/**
	 * 
	 * @param wishingMaps
	 * @return {key:wishingId,wishingName(如：政治,历史,化学)}
	 */
	private /*static*/ Map<String,String> getSortedWishingSubject(Map<String,Integer> wishingMaps){
		Map<String,String> wishingNameMap = new HashMap<String,String>();
		if(null==wishingMaps || wishingMaps.size()==0){
			return wishingNameMap;
		}		
		
		//多志愿行政班
		List<String> commHafCode = getCommonCodesInGroup(wishingMaps);
		
		
		for(String key : wishingMaps.keySet()){
			StringBuffer rs = new StringBuffer();
			//固定部分
			for(int n=0; n<commHafCode.size(); n++){
				rs.append(codeFullNameMap.get(commHafCode.get(n)));
				
				rs.append(",");
				
			}
			
			List<String> hafcodes = decodeHafmanCode(key);
			for(String hafCode : hafcodes){
				if(!commHafCode.contains(hafCode)){
					rs.append(codeFullNameMap.get(hafCode));
					rs.append(",");
				}
			}
			//删除逗号
			rs.delete(rs.length()-1, rs.length());
			
			wishingNameMap.put(key, rs.toString());
		}
		
		return wishingNameMap;
	}
	/**
	 * 解码hafman
	 */
	public List<String> decodeHafmanCode(String wishingGroupId){		
		List<String> result = new ArrayList<String>();
		StringBuffer sb = new StringBuffer(wishingGroupId);
		int start = 0;
		for(int j=2; j<=wishingGroupId.length(); j++){
			for(int i=0; i<courseHafCodes.length; i++){
				if(courseHafCodes[i].equals(sb.substring(start, j))){
					result.add(new String(courseHafCodes[i]));
					start=j;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * 获取志愿组两科相同的科目编码
	 * @param wishingGroup
	 * @return hafman编码(单志愿情况下,获取该志愿的所有编码-3科)
	 */
	private List<String> getCommonCodesInGroup(Map<String,Integer> wishingGroup){
		if(null==wishingGroup || wishingGroup.size()==0){
			return null;
		}
		if(wishingGroup.size()==1){
			Iterator<String> keyIter = wishingGroup.keySet().iterator();
			return decodeHafmanCode(keyIter.next());
		}
				
		StringBuffer sb = new StringBuffer();
		for(Entry<String, Integer> wishingEntry : wishingGroup.entrySet()){
			sb.append(wishingEntry.getKey());
			sb.append("+");
		}
		String commonSubs = getCommonCourse(wishingGroup);/*getCommonCourse(sb.substring(0,sb.length()-1), null);*/
		
		return decodeHafmanCode(commonSubs);
	}
	
	/**
	 * 根据人数排序
	 * @param wishingMap
	 * @param asc
	 */
	public void sort(List<Map.Entry<String, Integer>> wishingMap,final boolean asc){
		//根据人数排序
		Collections.sort(wishingMap, new Comparator<Map.Entry<String, Integer>>() {   
		   
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
		        return asc?(o2.getValue() - o1.getValue()):(o1.getValue() - o2.getValue()); 
		    }
		});
		
	}
	
	public void sort1(List<Map<String, Integer>> commonWishingGroups,final boolean asc){
		Collections.sort(commonWishingGroups, new Comparator<Map<String, Integer>>() {

			@Override
			public int compare(Map<String, Integer> o1, Map<String, Integer> o2) {
				// TODO Auto-generated method stub
				return asc?(getTotalStuNumInWishingGroup(o1)-getTotalStuNumInWishingGroup(o2))
						:(getTotalStuNumInWishingGroup(o2)-getTotalStuNumInWishingGroup(o1));
			}
		});
	}
	/**
	 * 将list<map.entry<K,V>>转换成map对象
	 * @param mapList
	 * @return
	 */
	public <K,V>Map<K,V> convertToMap(List<Entry<K,V>> mapList){
		Map<K,V> listMap = new HashMap<K,V>();
		for(Entry<K,V> entry : mapList){
			K key = entry.getKey();
			V value = entry.getValue();
			listMap.put(key, value);
		}		
		return listMap;
	}
	
	/**
	 * 判断志愿组合是否满足行政班条件;
	 * 组合若不满足行政班开班条件,则拆分该组合
	 * @param min
	 * @param max
	 * @return
	 */
	public /*static*/ boolean isGroupSatisfy(int min, int max,Map<String,Integer> wishingMaps){	
		
		int allWishingCount = wishingMapList.size();
		int bondedWishingCount = 0;
		for(Map<String,Integer>commonWishing :  commonWishingGroups){
			bondedWishingCount+=commonWishing.size();
		}
		if(allWishingCount!=(bondedWishingCount+wishingMaps.size())){
			System.out.println("lose wishings");
		}
		
		boolean satisfy = true;		
		Iterator<Map<String, Integer>> commonWishingIterator = commonWishingGroups.iterator();
		//设定不满足条件的志愿组合个数（只有一个志愿组合不满足时/不满足的志愿组合人数不满足开办人数时，将陷入死循环）
		int unSatisfyCount = 0;
		int unSatisfyPeople = 0;
		while(commonWishingIterator.hasNext()){
			int totalCount = 0;
			Map<String, Integer> commonWishingMap = commonWishingIterator.next();
			for(String key : commonWishingMap.keySet()){
				totalCount += commonWishingMap.get(key);				
			}
			if(totalCount<min || totalCount>max){
				unSatisfyPeople+=totalCount;
				unSatisfyCount++;
				satisfy = false;
				wishingMaps.putAll(commonWishingMap);
				commonWishingIterator.remove();
			}
		}
		
				
		bondedWishingCount = 0;
		for(Map<String,Integer>commonWishing :  commonWishingGroups){
			bondedWishingCount+=commonWishing.size();
		}
		if(allWishingCount!=(bondedWishingCount+wishingMaps.size())){
			System.out.println("lose wishings");
		}
		
		if(satisfy){
			return satisfy;
		}
		
		//不满足条件的待拆分志愿只有一个或者剩余志愿不能组合行政班
		if(unSatisfyCount<2 || unSatisfyPeople<min){			
			return false;
		}
		
		return satisfy;
	}
	
	/**
	 * 将超过人数限制的志愿切分,并返回可以单独开班的志愿列表
	 * 返回key:志愿haffman编码,value：开班数
	 */
	public /*static*/ Map<String, Integer> splitOverNumberedWishings(int stuInClassNum){		
		
		if(stuInClassNum<=0){
			stuInClassNum = CLASS_CAPACITY_MAX;
		}
		largeWishingOfStu = stuInClassNum;
		Iterator<Entry<String,Integer>> wishingIte = wishingMapList.iterator();
		List<Entry<String,Integer>> overNumberWishings = new ArrayList<Entry<String,Integer>>();
		
		while(wishingIte.hasNext()){
			Entry<String,Integer> entry = wishingIte.next();
			//志愿总人数
			Integer count = entry.getValue();
			if(count>stuInClassNum){	//大于班级容量志愿
				wishingIte.remove();
				
				entry.setValue(count%stuInClassNum);
				overNumberWishings.add(entry);
				classWisthSingleWishings.put(entry.getKey(), count/stuInClassNum);
			}
		}
		for(Entry<String,Integer> entry : overNumberWishings){
			wishingMapList.add(entry);
		}
		
		return classWisthSingleWishings;		
	}
	
	public List<Map<String,Integer>> divClass(){
		List<Map<String, Integer>> result = new CopyOnWriteArrayList<Map<String,Integer>>();
		List<Map<String, Integer>> resultWithPrio = new ArrayList<Map<String,Integer>>();
		
		while(true){
			//人数比较集中与同一个志愿
			if(wishingMapList.size()<targetDivClassNum/2){
				List<Map<String, Integer>> oriResult = divClass4/*_1*/(wishingMapList);
				for(Map<String, Integer> commWishing : oriResult){
					Map<String, Integer> commWishingRS = new HashMap<String,Integer>();
					for(String key : commWishing.keySet()){
						commWishingRS.put(new String(key), commWishing.get(key));
					}
					result.add(commWishingRS);
				}
				
				retainOrigin();
				if(checkResultNum(result)){
					return result;
				}
				result.clear();
				
			}else{
				
				double sigma = -1d;
				for(int i=0; i<10; i++){
					retainOrigin();
					//普适分班算法
					List<Map<String, Integer>> tmpResult = null;
					int totalStuNum = getTotalStuNumInWishingGroup(convertToMap(wishingMapList));
					if(totalStuNum/wishingMapList.size()<20){					
						tmpResult = divClass6(wishingMapList,targetDivClassNum,true,1);
					}else{
						tmpResult = divClass6_1(wishingMapList,targetDivClassNum);
					}
						//选优
					double curSigma = resultVariance(tmpResult,true);
					if(-1d==sigma || sigma > curSigma){
						sigma = curSigma;
						resultWithPrio = tmpResult;
					}
				}			
				//showWishingsInClass(resultWithPrio, false);
				if(checkResultNum(resultWithPrio)){
					return resultWithPrio;
				}else{
					return null;
				}
			}
		}
	}
	
	/**
	 * 人数大于班级容量的志愿,需要先进行拆分(调用该方法前,首先调用splitOverNumberedWishings()方法)
	 * @return
	 */
	public /*static*/ List<Map<String, Integer>>  divClass1(List<Entry<String, Integer>> wishingMapList){
		List<Map<String,Integer>> result = new ArrayList<Map<String,Integer>>();	
		if(wishingMapList.size() == 0 || targetDivClassNum<minGroupOfWishings(wishingMapList)){
			return result;
		}
		
		//允许最大分班数
		int divClassMaxCount = wishingMapList.size();
		//存放选优结果
		List<Map<String, Integer>> divProiResult = new ArrayList<Map<String,Integer>>();
		Map<String,Integer> wishingMaps = convertToMap(wishingMapList);
		
		//初始化结果集
		List<Map<String, Integer>> commonWishingGroups = new ArrayList<Map<String,Integer>>();
		Map<String, Integer> divLeftWishings = new HashMap<String,Integer>();
		
		//班级人数容量扩展
		int broadenCondition=0;
		int finalLoop = 100;
		//志愿数
		int wishingMapListSize = wishingMapList.size();

		result:
		while(true){		
			//打乱
			Collections.shuffle(wishingMapList,new Random(finalLoop));	

			//形成志愿组（行政班雏形）
			for(int i=0; i<wishingMapListSize; i++){
				Entry<String, Integer> wishing1 = wishingMapList.get(i);
				String wishingId1 = wishing1.getKey();
				if(!wishingMaps.containsKey(wishingId1)){
					continue;
				}
				int totalNum = wishing1.getValue();
				Map<String,Integer>commonWishingGroup = new HashMap<String,Integer>();
				commonWishingGroup.put(wishingId1, totalNum);
				wishingMaps.remove(wishingId1);
				StringBuffer sb = new StringBuffer(wishingId1);
				
				//单独开班的志愿
				if(totalNum > CLASS_CAPACITY_MIN){
					commonWishingGroups.add(commonWishingGroup);	
					if(wishingMaps.size()>0){
						continue;
					}
				}
				
				//选出能够组合的志愿	
				for(int n=wishingMapListSize-1; n>-1; n--){
					
					Entry<String, Integer> bondWishing = wishingMapList.get(n);
					String toBeBondWishingId = bondWishing.getKey();
					
					//从第一个能够满足行政班的志愿开始进行组合
					if(!wishingMaps.containsKey(toBeBondWishingId) 
							/*|| n==i*/){ 
						continue;
					}
					
					if(judgeInCommon(wishingId1, toBeBondWishingId,2,false)){					
						int num2 = bondWishing.getValue();
						totalNum+=num2;
						
						//超过最大班额，该组不再增加志愿进行合并
						if(totalNum>(SHREDSHOLD + broadenWidth)){							
							break;
						}						
						//固二科目剔除技术						
						/*if(null!=techId){
							List<String> commonHafCodes = getCommonHafCodes(wishingId1, toBeBondWishingId);
							if(commonHafCodes.contains(subjectIdHafCodeMap.get(techId))){
								continue;
							}
						}*/
						
						sb.append("+"); sb.append(toBeBondWishingId);
						String [] wishingsInWishingId1 = sb.toString().split("\\+");
						
						//第一对志愿组合时确定"定二"科目
						if(wishingsInWishingId1.length==2){													
							List<String> commonCodes = 
									getCommonHafCodes(wishingsInWishingId1[0],wishingsInWishingId1[1]);
							StringBuffer buffer = new StringBuffer();
							for(String hafCode : commonCodes){
								buffer.append(hafCode);
							}
							wishingId1 = buffer.toString();							
						}
							
						commonWishingGroup.put(toBeBondWishingId,num2);															
						wishingMaps.remove(toBeBondWishingId);
					}
				}	//end for
				
				//志愿太小,归还志愿
				/*if(getTotalStuNumInWishingGroup(commonWishingGroup)<CLASS_CAPACITY_MAX/2){
					wishingMaps.putAll(commonWishingGroup);
					continue;
				}*/
				commonWishingGroups.add(commonWishingGroup);				
				
				//全部组合完毕（人数待优化）
				if(wishingMaps.size()==0){
					if(commonWishingGroups.size()==targetDivClassNum){
						break;/*continue result*/
					}
					//记录最小分班数
					if(divClassMaxCount>commonWishingGroups.size()){
						divClassMaxCount = commonWishingGroups.size();
						divProiResult.clear();
						divProiResult.addAll(commonWishingGroups);
					}
					
					//放宽人数条件
					if(--broadenCondition < 0){
						broadenWidth = (commonWishingGroups.size()>targetDivClassNum)?
								++broadenWidth:--broadenWidth;
						broadenCondition = (CLASS_CAPACITY_MAX - broadenWidth) * 10;	 
						
						//大于班级人数,说明志愿不可能组合完成（剔除掉，重新分班-需要做后处理）
						if(broadenWidth>CLASS_CAPACITY_MAX){
							if(divProiResult.size() != 0){															
								commonWishingGroups.clear();
								commonWishingGroups.addAll(divProiResult);
								divProiResult.clear();
								
								//除掉不能组合的志愿,重新分班
								removeUncombinableWishings(commonWishingGroups, wishingMapList,divLeftWishings, divClassMaxCount - targetDivClassNum);
								break;
							}
						}
					}
					//打乱-避免循环计算结果一致
					Collections.shuffle(wishingMapList,new Random(i));	
					wishingMaps = convertToMap(wishingMapList);
					commonWishingGroups.clear();
					continue result;
				} //end if
				if(i==wishingMapListSize-1){
					System.out.println("break out!");
				}
			}	//end for
		
		
			if(wishingMaps.size()>0){
				System.out.println("has left wishings");
			}
			
			//最终的寻优的循环次数
			int preOverNumbered = 0;
			while(true){
				//分班后处理
				if(divLeftWishings.size()>0 ){
					if(procAfterDiv(commonWishingGroups, divLeftWishings,true,1)>0){
						 continue result;
					}
				}

				int overNumbered = balanceDivResult2(commonWishingGroups);
				//行政班志愿组合为空
				/*if(overNumbered < 0){
					continue result;
				}*/
				
				//保存最佳结果
				if(preOverNumbered>overNumbered || preOverNumbered==0){
					result.clear();
					preOverNumbered = overNumbered;
					for(Map<String, Integer> commWishing : commonWishingGroups){
						Map<String, Integer> commWishingRS = new HashMap<String,Integer>();
						for(String key : commWishing.keySet()){
							commWishingRS.put(new String(key), commWishing.get(key));
						}
						result.add(commWishingRS);
					}
				}
				
				if(overNumbered == 0){
					break result;
				}else{
					if(--finalLoop<0){
						break result;
					}
					
					//重新初始化
					retainOrigin();
					continue result;
				}
			}
		} //end while
		commonWishingGroups = result;
	//	System.out.println("\n");
		//showWishingsInClass(commonWishingGroups,false);
		//System.out.println("\n");
		//showWishingsInClass(commonWishingGroups,true);
		System.out.println("donew");
		/*return commonWishingGroups;*/
		
		
		return result;		
	}
	
	/**
	 * 分班后处理（不能入班的小志愿,强行加入-不拆分）
	 * @param commonWishingGroups
	 * @param divLeftWishings
	 */
	private int procAfterDiv(List<Map<String, Integer>>commonWishingGroups,Map<String, Integer> divLeftWishings,boolean excludeTech, int commonSub){				
		//重新加入（不可重入志愿-单科相同（除技术））
		sort1(commonWishingGroups, true);
		Iterator<Entry<String, Integer>> leftWishingsIter = divLeftWishings.entrySet().iterator();
		while(leftWishingsIter.hasNext()){
			Entry<String, Integer>leftWishingEntry = leftWishingsIter.next();
			String wishingId = leftWishingEntry.getKey();
			
			for(Map<String, Integer>commonWishingGroup : commonWishingGroups){
				String commonCodes = getCommonCourse(commonWishingGroup);
				List<String> hafcodes = decodeHafmanCode(wishingId);
				
				//获取剩余志愿中除技术外的所有科目hafman编码
				StringBuffer sb = new StringBuffer();
				for(String hafcode : hafcodes){
					if(excludeTech && hafcode.equals(hafCodeWishingIdMap.get(techId))){
						continue;
					}
					sb.append(hafcode);
				}
				if(judgeInCommon(sb.toString(), commonCodes, commonSub,excludeTech)){
					int stuNum = 0;
					if(commonWishingGroup.containsKey(wishingId)){
						int preNum = commonWishingGroup.get(wishingId);
						stuNum+=preNum;
					}
					stuNum += leftWishingEntry.getValue();
					commonWishingGroup.put(wishingId, stuNum);
					leftWishingsIter.remove();
					
					//重排序
					sort1(commonWishingGroups, true);
					break;
				}
			}
		}
		
		return divLeftWishings.size();		
	}
	
	/**
	 * 分班后处理（不能入班的小志愿,强行加入-拆分）
	 * @param commonWishingGroups
	 * @param divLeftWishings
	 * @param excludeTech
	 * @param commonSub
	 * @return
	 */
	private int procAfterDiv2(List<Map<String, Integer>>commonWishingGroups,Map<String, Integer> divLeftWishings,boolean excludeTech, int commonSub){				
		//重新加入（不可重入志愿-单科相同（除技术））
		List<Map<String, Integer>> leftDivCls = new ArrayList<Map<String,Integer>>();		
		combineGroup(divLeftWishings, leftDivCls,wishingMapList);
		
		sort1(commonWishingGroups, true);		
		
		Iterator<Map<String, Integer>>leftDivClsIter = leftDivCls.iterator();
		next:
		while(leftDivClsIter.hasNext()){
			for(int i=0; i<commonWishingGroups.size(); i++){
				Map<String, Integer> commonWishingGroup = commonWishingGroups.get(i);
				if(clsCanReIn(commonWishingGroup,commonWishingGroups,i)){
					commonWishingGroups.remove(i);
					commonWishingGroups.add(leftDivClsIter.next());
					procAfterDiv(commonWishingGroups, commonWishingGroup, false, 2);
					leftDivClsIter.remove();
					if(leftDivCls.size()<=0){
						return 0;
					}
					continue next;
				}				
			}
			break;
		}
		
		//还原剩余不能替换志愿
		for(int i=0; i<leftDivCls.size();i++){
			divLeftWishings.putAll(leftDivCls.get(i));
		}
		
		return leftDivCls.size();
	}
	
	/**
	 * 判断行政班志愿是否能重新加入
	 * @param commonWishingGroup
	 * @return
	 */
	private boolean clsCanReIn(Map<String, Integer> commonWishingGroup,List<Map<String, Integer>> commonWishingGroups,int pos){
		nextWishing:
		for(String wishingId : commonWishingGroup.keySet()){
			for(int j=0; j<commonWishingGroups.size() && j!=pos; j++){
				if(hasTwoSubsInCommon(wishingId, commonWishingGroups.get(j), false)){
					continue nextWishing;
				}
			}
			//有一个志愿不能重入
			return false;
		}
		return true;
	}
	
	/**
	 * 在不能满足行政班开班条件情况下，移除不能进班志愿
	 * @param commonWishingGroups
	 * @param num
	 */
	private void removeUncombinableWishings(List<Map<String, Integer>>commonWishingGroups,
			List<Entry<String, Integer>> wishingMapList,Map<String, Integer> divLeftWishings,int num){
		if(null==commonWishingGroups 
				|| commonWishingGroups.size()==0
				|| 0==num){
			return ;
		}
		
		for(int i=0; i<num; i++){
			
			//寻找最小人数志愿组
			sort1(commonWishingGroups, true);
			Map<String, Integer> commonWishingGroup = commonWishingGroups.remove(0);
			if(commonWishingGroup.size() == 0){
				continue;
			}
			
			//移除最小人数志愿组
			for(String wishingId : commonWishingGroup.keySet()){
				Iterator<Entry<String, Integer>> wishingMapListIter = wishingMapList.iterator();
				while(wishingMapListIter.hasNext()){
					Entry<String, Integer> wishingMapEntry = wishingMapListIter.next();
					String key = wishingMapEntry.getKey();
					if(key.equals(wishingId)){
						wishingMapListIter.remove();
						divLeftWishings.put(key, wishingMapEntry.getValue());
					}
				}
			}
		}
	}
	
	/**
	 * 获取分班结果最后的班级总人数
	 * @param result
	 * @return
	 */
	private int getTotalStuNumInDivResult(List<Map<String,Integer>> result){
		int totalWishingStuNum = 0;
		for(Map<String,Integer> cls : result){
			totalWishingStuNum += getTotalStuNumInWishingGroup(cls);
		}
		return totalWishingStuNum;
	}
	
	/**
	 * 获取志愿组中人数
	 * @param wishingMap
	 * @return
	 */
	private int getTotalStuNumInWishingGroup(Map<String,Integer> wishingMap){
		if(null==wishingMap || wishingMap.size()==0){
			return 0;
		}
		int totalNum = 0;
		for(int stu : wishingMap.values()){
			totalNum += stu;
		}
		return totalNum;
	}
	
	/**
	 * 处理小志愿（将大人数班级中的志愿拆分到人数较少的班级）
	 * @param commonWishingGroups
	 * @return
	 */
	public int balanceDivResult2(List<Map<String, Integer>> commonWishingGroups){
		List<Map<String, Integer>> belowIdeaGroups = new ArrayList<Map<String,Integer>>();
		List<Map<String, Integer>> aboveIdeaGroups = new ArrayList<Map<String,Integer>>();
		
		//统计人数不满足开班条件的志愿组
		Iterator<Map<String, Integer>> commonWishingGroupIter = commonWishingGroups.iterator();
		while(commonWishingGroupIter.hasNext()){
			Map<String, Integer> commonWishings = commonWishingGroupIter.next();
			int totalNum = getTotalStuNumInWishingGroup(commonWishings);
			
			//去掉空班级
			if(totalNum==0){
				commonWishingGroupIter.remove();				
				continue;
			}
			
			//人数不足
			if(totalNum < CLASS_CAPACITY_MIN ){
				belowIdeaGroups.add(commonWishings);
				commonWishingGroupIter.remove();
				continue;
			}
			
			//人数超额
			/*if(totalNum > CLASS_CAPACITY_MAX){
				aboveIdeaGroups.add(commonWishings);
				commonWishingGroupIter.remove();
				continue;
			}*/			
		}
		
		//打乱,使每次结果不一样
		Collections.shuffle(commonWishingGroups);
		
		//均衡人数
		for(Map<String,Integer> belowIdeaWishings : belowIdeaGroups){
			//List<String> commonCodes = getCommonCodesInGroup(belowIdeaWishings);
			int stuNum = getTotalStuNumInWishingGroup(belowIdeaWishings);
			
			for(Map<String,Integer> spareWishings : commonWishingGroups){
				Iterator<String>spareWishingIdIter = spareWishings.keySet().iterator();
				int stuNumInAboveWishings = getTotalStuNumInWishingGroup(spareWishings);
				int gap = stuNumInAboveWishings - stuNum;
				
				//大志愿中的志愿组合
				while(spareWishingIdIter.hasNext()){
					String spareWishingId = spareWishingIdIter.next();
					int stuInWishing = spareWishings.get(spareWishingId);
					
					if(hasTwoSubsInCommon(spareWishingId, belowIdeaWishings, false)){
						if(Math.abs(stuNumInAboveWishings-stuNum-2*stuInWishing)<gap 
								|| (stuNumInAboveWishings - stuInWishing)>=(CLASS_CAPACITY_MIN-CLASS_CAPACITY_MIN%10)){
							//相同志愿的情况
							if(belowIdeaWishings.keySet().contains(spareWishingId)){
								belowIdeaWishings.put(spareWishingId, belowIdeaWishings.get(spareWishingId)+stuInWishing);
							}else{
								belowIdeaWishings.put(spareWishingId, stuInWishing);							
							}
							spareWishingIdIter.remove();
							continue;
						}							
					}
				}//end while
				
			}//end for
		}//end for
		
		commonWishingGroups.addAll(belowIdeaGroups);
		
		//统计超额志愿组
		int aboveNum = 0;
		for(Map<String, Integer>aboveMap : commonWishingGroups/*aboveIdeaGroups*/){
			int upLimit = CLASS_CAPACITY_MAX + CLASS_ROUND_NUM;
			int stuNum = getTotalStuNumInWishingGroup(aboveMap);
			if(stuNum == 0){
				return -1;
			}
			
			if(stuNum>upLimit){
				aboveNum++;
			}
		}		
		
		return aboveNum;
	}
	
	
	/**
	 * 深度优化
	 */
	private void deepOptResult(List<Map<String,Integer>> result, int alg, int deep){
		//初始化列表
		List<Map<String,Integer>> adaptedResult = new ArrayList<Map<String,Integer>>();
		List<Map<String,Integer>> toBeOpted = new ArrayList<Map<String,Integer>>();
		List<Map<String,Integer>> tmpResult = new ArrayList<Map<String,Integer>>();
		//tmpResult.addAll(result);
		double balanceDegree = resultVariance(result,true);
		
		//初始化待排志愿列表
		List<Entry<String,Integer>> wishingMapList = new ArrayList<Map.Entry<String,Integer>>();
		
		int resultSize = result.size(); 
		for(int i=0; i<resultSize ; i++){
			int stuNumInCls = getTotalStuNumInWishingGroup(result.get(i));
			if(stuNumInCls>=CLASS_CAPACITY_MIN && stuNumInCls<=CLASS_CAPACITY_MAX){
				adaptedResult.add(result.get(i));
			}else{
				toBeOpted.add(result.get(i));
				wishingMapList.addAll(result.get(i).entrySet());
			}			
		}
		//tmpResult.clear();
		
		//限制优化级别
		int adaptedCount = adaptedResult.size();
		deep=(deep>adaptedCount)?adaptedCount:deep;
		
		//全排列
		List<List<Integer>> combiner = new ArrayList<List<Integer>>();
		List<Integer> data = new ArrayList<Integer>();
		for(int n=0; n<adaptedCount; n++){
			data.add(n);
		}	
		
		//剩余志愿重新分班,并保存最优结果
		List<Map<String,Integer>> adaptedTmpResult = new ArrayList<Map<String,Integer>>();
		List<Map<String,Integer>> toBeOptedTmp = new ArrayList<Map<String,Integer>>();
		List<Entry<String,Integer>> wishingMapListTmp = new ArrayList<Map.Entry<String,Integer>>();
		for(int n=1; n<=adaptedCount && n<=deep; n++){
			adaptedTmpResult.clear();
			toBeOptedTmp.clear();
			wishingMapListTmp.clear();
			
			adaptedTmpResult.addAll(adaptedResult);
			toBeOptedTmp.addAll(toBeOpted);			
			
			//输出A(n,n)的全排列
			combiner.clear();
			combinerSelect(data, new ArrayList<Integer>(), data.size(), n,combiner); 
			
			for(int i=0; i<combiner.size(); i++){
				wishingMapListTmp.clear();
				for(Map<String,Integer> tobeOptWishings : toBeOpted){
					wishingMapListTmp.addAll(tobeOptWishings.entrySet());
				}
				
				List<Integer> subs = combiner.get(i);
				for(Integer sub : subs){
					wishingMapListTmp.addAll(adaptedTmpResult.get(sub).entrySet());
					//adaptedTmpResult.remove(sub.intValue());
				}
				
				//分班
				List<Map<String, Integer>> reminderRs = null;
				switch (alg) {
				case 6:
					reminderRs = 
						divClass6(wishingMapListTmp, targetDivClassNum-adaptedTmpResult.size()+subs.size(),false,1);					
					break;
				case 61:
					reminderRs = 
						divClass6_1(wishingMapListTmp, targetDivClassNum-adaptedTmpResult.size()+subs.size());	
					break;
				default:
					break;
				}
				
				//添加剩余班级组
				for(int m=0; m<adaptedTmpResult.size(); m++){
					for(Integer sub : subs){
						if(m==sub){
							continue;
						}
						tmpResult.add(adaptedTmpResult.get(m));
					}
				}
				tmpResult.addAll(reminderRs);

				//评优
				double sigma = resultVariance(tmpResult,true);
				if(checkResultNum(tmpResult) && balanceDegree>sigma){
					result.clear();
					result.addAll(tmpResult);
					balanceDegree = sigma;
				}
				tmpResult.clear();
			}
			
		}
	}
	
	
	/**
	 * 优化平衡分班结果
	 */
	public void balanceDivResult(){
		//拆分大人数行政班
		/*if(targetDivClassNum>commonWishingGroups.size()){
			for(int i=0; i<targetDivClassNum-commonWishingGroups.size(); i++){
				Map<String, Integer> wishingMap = new HashMap<String,Integer>();
				Map<String, Integer> divWishingMap = null;
				int preTotalNum = 0;
				for(Map<String, Integer> wishingInMap : commonWishingGroups){
					int totalNum = 0;
					for(String key : wishingInMap.keySet()){
						totalNum+=wishingInMap.get(key);
					}
					if(totalNum<2*CLASS_CAPACITY_MAX-SHREDSHOLD){
						continue;
					}
					if(totalNum>preTotalNum){
						preTotalNum = totalNum;
						divWishingMap = wishingInMap;
					}
				}
				
				if(null!=divWishingMap && divWishingMap.size()>1){
					Entry<String, Integer> divWishingEntry = null;
					int preMaxEntry = 0;
					for(Entry<String, Integer> wishingEntry : divWishingMap.entrySet()){
						if(wishingEntry.getValue()>preMaxEntry){
							divWishingEntry = wishingEntry;
						}
					}
					divWishingMap.remove(divWishingEntry);
					wishingMap.put(divWishingEntry.getKey(), divWishingEntry.getValue());
					commonWishingGroups.add(wishingMap);
				}
			}
		}*/
		//重组小人数行政班
		for(Map<String, Integer> wishingMap : commonWishingGroups){
			int totalNum = 0;
			StringBuffer sb = new StringBuffer();
			String wishingId=null;
			for(String key : wishingMap.keySet()){
				sb.append(key+"+");
				totalNum += wishingMap.get(key);
			}
			if(totalNum>CLASS_CAPACITY_MIN){
				continue;
			}
			List<String> commonCodes = new ArrayList<String>();
			if(wishingMap.size()==1){
				wishingId = sb.substring(0, sb.length()-1);
			}else{
				commonCodes = decodeHafmanCode(getCommonCourse(sb.substring(0,sb.length()-1), null));
				StringBuffer commonCodeSb = new StringBuffer(commonCodes.get(0));
				commonCodeSb.append(commonCodes.get(1));
				wishingId = commonCodeSb.toString();
			}
			
			
			int gap = CLASS_CAPACITY_MIN-totalNum;
			//重组大人数志愿组合中的志愿（分配到当前不能满足最小开班数的志愿组）
			Entry<String, Integer> dividedWishingMap = getAndResetWishing(wishingId, gap);
			while(null!=dividedWishingMap){	//多次补偿
				wishingMap.put(dividedWishingMap.getKey(), dividedWishingMap.getValue());	
				totalNum+=dividedWishingMap.getValue();
				if(totalNum>CLASS_CAPACITY_MIN){
					break;
				}
				gap = CLASS_CAPACITY_MIN-totalNum;
				dividedWishingMap = getAndResetWishing(wishingId, gap);
			}
		}
	}
	
	/**
	 * 重新调整志愿组中志愿（平衡志愿组）
	 * @param wishingId/wishingGroupId
	 * @param gap
	 * @return
	 */
	private Map.Entry<String, Integer> getAndResetWishing(String wishingId,int gap){
		
		for(Map<String, Integer> wishingMap : commonWishingGroups){
			int totalNum = 0;
			//StringBuffer sb = new StringBuffer();
			Iterator<Map.Entry<String, Integer>>wishingMapIter = wishingMap.entrySet().iterator();
			
			for(String key : wishingMap.keySet()){
				//sb.append(key+"+");
				totalNum += wishingMap.get(key);
			}
			
			if((totalNum-=CLASS_CAPACITY_MIN)<=0){
				continue;
			}
			while(wishingMapIter.hasNext()){
				Entry<String, Integer> wishingEntry = wishingMapIter.next();
				int stuInWishing = wishingEntry.getValue();
				if(judgeInCommon(wishingId, wishingEntry.getKey(),2,false)
						&& ((totalNum-stuInWishing)>0) || (stuInWishing-totalNum)<gap){
					wishingMapIter.remove();
					return wishingEntry;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 按序显示科目组合
	 */
	public void showWishingsInClass(List<Map<String,Integer>> wishingMapsInClass,boolean withOrder){
		Iterator<Map<String, Integer>> wishingMapsIterator = wishingMapsInClass.iterator();
		while(wishingMapsIterator.hasNext()){
			Map<String,Integer> wishings = wishingMapsIterator.next();
			Iterator<Entry<String, Integer>> wishingEntryIter = wishings.entrySet().iterator();
			while(wishingEntryIter.hasNext()){
				Entry<String, Integer> wishing = wishingEntryIter.next();
				int num = wishing.getValue();
				String wishingId = wishing.getKey();
				if(withOrder){
					System.out.println(getsortedWisingName(wishingId,commonWishingGroups));
				}else{
					for(String code :decodeHafmanCode(wishingId)){
						System.out.print(codeNameMap.get(code));
					}
				}
				System.out.println(" - "+num);
			}
			System.out.println();
		}
	}
	
	/**
	 * 根据wishingId获取志愿名称(如：政史地)
	 * @param wishingId
	 * @return
	 */
	public /*static*/ String getWishingName(String wishingId){		
		
		return hafCodeSubjectNameMap.get(wishingId);
	}
	
	/**
	 * 根据wishingId获取志愿名称(如：4,5,6)
	 * @param wishingId
	 * @return
	 */
	public /*static*/ String getWishingIds(String wishingId){
		
		return hafCodeSubjectIdMap.get(wishingId);
	}
	
	
	public List<Map<String,Integer>> divClass2(){
		List<Map<String,Integer>> result = new ArrayList<Map<String,Integer>>();
		//初始化算法
		Map<String,Integer> wishingMaps = convertToMap(wishingMapList);
		commonWishingGroups.clear();		
		
		List<Map<String, Integer>> satisfyWishingGroup = new CopyOnWriteArrayList<Map<String, Integer>>();
		List<Map<String, Integer>> unSatisfyWishingGroup = new CopyOnWriteArrayList<Map<String, Integer>>();
		
		Collections.shuffle(wishingMapList);
		
		//step1: 初始化satisfyWishingGroup、unSatisfyWishingGroup以及wishingMaps
		combineGroup(wishingMaps, satisfyWishingGroup,wishingMapList);
		Iterator<Map<String, Integer>> satisfyWishingIter = satisfyWishingGroup.iterator();
		while(satisfyWishingIter.hasNext()){
			Map<String, Integer> wishingMap = satisfyWishingIter.next();
			if(!isClassSatisfy(wishingMap)){
				satisfyWishingGroup.remove(wishingMap);
				//satisfyWishingIter.remove();
				for(String key : wishingMap.keySet()){
					wishingMaps.put(key, wishingMap.get(key));
				}
			}
		}
		
		int leftSpace = targetDivClassNum-satisfyWishingGroup.size();
		//处理剩余
		for(int n = 0; n<wishingMapList.size(); n++){
			Entry<String, Integer> wishingEntry = wishingMapList.get(n);
			String wishingId = wishingEntry.getKey();
			if(!wishingMaps.containsKey(wishingId)){
				continue;
			}
			//添加志愿到志愿组
			boolean addWishingToGroupSuc = addWishingSucceed(wishingEntry, 
					unSatisfyWishingGroup,
					satisfyWishingGroup);
			if(addWishingToGroupSuc){
				wishingMaps.remove(wishingEntry.getKey());
				continue;
			}
			
			//未添加成功(看是否仍有空间容纳该志愿)
			if(leftSpace-->0){
				String key = wishingEntry.getKey();
				Map<String, Integer>wishingGroup = new HashMap<String, Integer>();
				wishingGroup.put(key, wishingEntry.getValue());
				unSatisfyWishingGroup.add(wishingGroup);
				wishingMaps.remove(wishingEntry.getKey());
			}
		}		
		
		//step2: 剩余志愿加入未满足行政班的志愿组
		Set<Entry<String, Integer>> wishingEntrySet = wishingMaps.entrySet();
		Iterator<Entry<String, Integer>> leftWishingIter = wishingEntrySet.iterator();
		for(int n = 0; n<wishingMapList.size(); n++){
			Entry<String, Integer> wishingEntry = wishingMapList.get(n);
			String wishingId = wishingEntry.getKey();
			if(!wishingMaps.containsKey(wishingId)){
				continue;
			}
			if(addWishingSucceed(wishingEntry, unSatisfyWishingGroup, satisfyWishingGroup)){
				wishingMaps.remove(wishingId);
			}
		}		
		
		//step3-1: 重组满足行政班队列中的志愿组
		leftWishingIter = wishingMaps.entrySet().iterator();
		nextSwap:
		while(leftWishingIter.hasNext()){	//wishingMaps
			Entry<String, Integer> leftWishingEntry = leftWishingIter.next();
			String leftWishingId = leftWishingEntry.getKey();
			int stuInLeftWishing = leftWishingEntry.getValue();
			
			//换出组合中志愿
			List<Map<String, Integer>> wishingGroups = new ArrayList<Map<String,Integer>>();
			wishingGroups.addAll(satisfyWishingGroup);
			wishingGroups.addAll(unSatisfyWishingGroup);			
			for(Map<String,Integer> wishingGroup : wishingGroups){	//wishingGroups
				//判断是否可以交换（条件一）
				if(hasTwoSubsInCommon(leftWishingId, wishingGroup, false)){
					int totalNum = stuInLeftWishing;
					for(String key : wishingGroup.keySet()){
						totalNum+=wishingGroup.get(key);
					}
					
					//志愿组中可换出志愿的人数范围（必要条件）小于等于
					int leftBond=totalNum-CLASS_CAPACITY_MIN;
					Set<Entry<String, Integer>> groupEntry = wishingGroup.entrySet();
					Iterator<Entry<String, Integer>>groupEntryIter = groupEntry.iterator();
					while(groupEntryIter.hasNext()){	//wishingGroup
						Entry<String, Integer> beAddWishingEntry = groupEntryIter.next();
						int stuNum = beAddWishingEntry.getValue();
						//判断是否符合交换条件（太大、太小）
						if(stuNum>leftBond || totalNum-stuNum>CLASS_CAPACITY_MAX){ //条件二
							continue;
						}
						
						
						//符合交换条件(进行交换)
						if(addWishingSucceed(beAddWishingEntry, 
								unSatisfyWishingGroup, 
								satisfyWishingGroup)){
							groupEntryIter.remove();
							leftWishingIter.remove();	
							
							wishingGroup.put(leftWishingId, stuInLeftWishing);							
							continue nextSwap;
						}
					}
					
				}
			}
		}
		
		//step 3-2: 重组不满足行政班开班条件的志愿组中志愿
		Iterator<Map<String, Integer>> unsatisfyGroupIter = unSatisfyWishingGroup.iterator();
		while(unsatisfyGroupIter.hasNext()){
			Map<String, Integer> unsatisfyWishingMap = unsatisfyGroupIter.next();
			//unsatisfyGroupIter.remove();
			unSatisfyWishingGroup.remove(unsatisfyWishingMap);
			
			Iterator<Entry<String, Integer>> unsatisfyEntryIter = unsatisfyWishingMap.entrySet().iterator();
			while(unsatisfyEntryIter.hasNext()){
				Entry<String, Integer> unsatisfyWishingEntry = unsatisfyEntryIter.next();
				String wishingId = unsatisfyWishingEntry.getKey();
				
				//添加成功
				if(addWishingSucceed(unsatisfyWishingEntry, 
						unSatisfyWishingGroup, 
						satisfyWishingGroup)){
					unsatisfyEntryIter.remove();
				}
				
				//添加失败
				wishingMaps.put(wishingId, unsatisfyWishingEntry.getValue());
			}
		}
		//剩余志愿再组合
		Iterator<Entry<String, Integer>> wishingMapsIter = wishingMaps.entrySet().iterator();
		while(wishingMapsIter.hasNext()){			
			if(addWishingSucceed(wishingMapsIter.next(), 
					unSatisfyWishingGroup, 
					satisfyWishingGroup)){
				wishingMapsIter.remove();
			}
		}
		
		//采用分班方案一对剩余组合进行分
		List<Entry<String, Integer>>  wishingMapList	= new ArrayList<Entry<String, Integer>>();	
		wishingMapList.addAll(wishingMaps.entrySet());
		while(true){
			List<Map<String, Integer>> reminderList = divClass1(wishingMapList);
			if(null!=reminderList){
				result.addAll(reminderList);	
				break;
			}else{
				broadenWidth = (broadenWidth<CLASS_CAPACITY_MIN)?++broadenWidth:broadenWidth;
				//CLASS_CAPACITY_MIN=(CLASS_CAPACITY_MIN>0)?--CLASS_CAPACITY_MIN:1;
			}
		}
		
		//返回结果
		if(null!=satisfyWishingGroup && satisfyWishingGroup.size()>0){
			result.addAll(satisfyWishingGroup);
		}		
		return result;
	}
	
	/**
	 * 判断志愿是否能加入行政班(并做相应处理)
	 * @param wishingId
	 * @param unSatisfyWishingGroup
	 * @return
	 */
	private boolean addWishingSucceed(Entry<String, Integer> beAddWishingEntry,
			List<Map<String, Integer>> unSatisfyWishingGroup,
			List<Map<String, Integer>> satisfyWishingGroup){
		if(null==unSatisfyWishingGroup || unSatisfyWishingGroup.size()==0){
			return false;
		}
		List<String> beAddWishCodes = decodeHafmanCode(beAddWishingEntry.getKey());
		
		Iterator<Map<String, Integer>> iterator = unSatisfyWishingGroup.iterator();
		while(iterator.hasNext()){
			Map<String, Integer> wishingGroup = iterator.next();
			//判断行政班人数
			int totalCount = beAddWishingEntry.getValue();
			for(Entry<String,Integer> wishingEntry : wishingGroup.entrySet()){
				totalCount+=wishingEntry.getValue();
			}			
			if(totalCount>CLASS_CAPACITY_MAX){
				continue;
			}
			
			//判断是否有相同两科
			int commonSubCount = 0;
			List<String> commonCodes = getCommonCodesInGroup(wishingGroup);
			for(String beAddWishingCode : beAddWishCodes){
				if(commonCodes.contains(beAddWishingCode)){
					commonSubCount++;
				}
			}
			if(commonSubCount<2){
				continue;
			}
			
			//符合条件,加入行政班
			//wishingGroup.entrySet().add(beAddWishingEntry);
			wishingGroup.put(beAddWishingEntry.getKey(), beAddWishingEntry.getValue());
			
			//判断加入后的志愿组是否满足行政班(满足的话,将该志愿组加入满足队列)
			if(isClassSatisfy(wishingGroup)){
				unSatisfyWishingGroup.remove(wishingGroup);
				//iterator.remove();
				satisfyWishingGroup.add(wishingGroup);
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 判断班级是否达到了行政班的标准
	 * @param wishingMap
	 * @return
	 */
	private boolean isClassSatisfy(Map<String,Integer> wishingMap){
		if(null==wishingMap || wishingMap.size()==0){
			return false;
		}
		int totalNum = 0;
		for(Entry<String,Integer> wishingEntry : wishingMap.entrySet()){
			totalNum+= wishingEntry.getValue();
		}
		if(totalNum<=CLASS_CAPACITY_MAX && totalNum>=CLASS_CAPACITY_MIN){
			return true;
		}
		
		return false;
	}
	

	/**
	 * 判断WishingId是否能加入wishingGroup组
	 * @param wishingId
	 * @param wishingGroup
	 * @return
	 */
	private boolean hasTwoSubsInCommon(String wishingId, Map<String,Integer> wishingGroup, boolean excludeTech){		
		if(null==wishingGroup){
			return false;
		}
		
		Set<String> wishingIdSet = wishingGroup.keySet();
		Iterator<String> wishingIdIter = wishingIdSet.iterator();
		String wishingGroupId = null;
		switch(wishingGroup.size()){
		case 0:
			return false;
		case 1:			
			wishingGroupId = wishingIdIter.next();
			break;
		default:
			StringBuffer sb = new StringBuffer();
			while(wishingIdIter.hasNext()){
				sb.append(wishingIdIter.next());
				sb.append("+");
			}
			
			wishingGroupId=sb.substring(0, sb.length()-1);		
		}
		return judgeInCommon(wishingId, wishingGroupId,2,excludeTech);
	}
	
	/**
	 * 组合单个志愿成志愿组合
	 * @param wishingMaps
	 */
	private void combineGroup(Map<String,Integer> wishingMaps, List<Map<String,Integer>> satisfyWishingGroup,List<Entry<String, Integer>> wishingMapList){		
		int wishingMapListSize = wishingMapList.size();
		
		//顺序迭代
		for(int i=0; i<wishingMapListSize; i++){
			Entry<String, Integer> wishing1 = wishingMapList.get(i);
			String wishingId1 = wishing1.getKey();
			if(!wishingMaps.containsKey(wishingId1)){
				continue;
			}
			int totalNum = wishing1.getValue();
			Map<String,Integer>commonWishingGroup = new HashMap<String,Integer>();
			commonWishingGroup.put(wishingId1, totalNum);
			wishingMaps.remove(wishingId1);
			StringBuffer sb = new StringBuffer(wishingId1);

			//选出能够组合的志愿（逆向查询）	
			for(int n=wishingMapListSize-1; n>-1; n--){
				
				Entry<String, Integer> bondWishing = wishingMapList.get(n);
				String toBeBondWishingId = bondWishing.getKey();
				//从第一个能够满足行政班的志愿开始进行组合
				if(!wishingMaps.containsKey(toBeBondWishingId) 
						|| n==i){ //(min/2):太小的组合没意义，走班人数会增加
					continue;
				}
				
				if(judgeInCommon(wishingId1, toBeBondWishingId,2,false)){					
					int num2 = bondWishing.getValue();
					totalNum+=num2;
					//超过最大班额，该组不再增加志愿进行合并
					if(totalNum>CLASS_CAPACITY_MAX){
						break;
					}						
											
					sb.append("+"); sb.append(toBeBondWishingId);
					String [] wishingsInWishingId1 = sb.toString().split("\\+");
					
					//第一对志愿组合时确定"定二"科目
					if(wishingsInWishingId1.length==2){
						List<String> commonCodes = (wishingsInWishingId1.length>2)?
								decodeHafmanCode(wishingId1):null;														
						wishingId1 = getCommonCourse(sb.toString(),commonCodes);
					}
						
					commonWishingGroup.put(toBeBondWishingId,num2);															
					wishingMaps.remove(toBeBondWishingId);
				}//end if
			}//end for
			satisfyWishingGroup.add(commonWishingGroup);
		}//end for
	}
	
	public List<Map<String,Integer>> divClass3(){
		List<Map<String,Integer>> result = new ArrayList<Map<String,Integer>>();
		//初始化算法
		Map<String,Integer> wishingMaps = convertToMap(wishingMapList);
		commonWishingGroups.clear();	
		
		//step1: 初始化二十一种组合
		Map<String,List<String>> wishingsInGroupMap = new HashMap<String,List<String>>();
		Map<String,Integer> subGroupMap = getStuInGrp(wishingMapList, wishingsInGroupMap);
		
		//step 3: 
		Map<String, List<String>> satisfyGroup = new HashMap<String, List<String>>();
		while(true){
			//有志愿不能满足行政班开班条件（进行回滚）
			if(!allWishingsCanInClass(wishingMaps, subGroupMap)){
				
				break;
			}
			
			//step 3-1: 直接开班志愿组合
			Iterator<Entry<String, Integer>> subGroupMapIter = subGroupMap.entrySet().iterator();
			String wishingGroupId = null;
			while(subGroupMapIter.hasNext()){
				Entry<String,Integer> subGroupMapEntry = subGroupMapIter.next();
				wishingGroupId = subGroupMapEntry.getKey();
				int stuInGroup = subGroupMap.get(wishingGroupId);
				//判断志愿组是否满足行政班开班条件
				if(CLASS_CAPACITY_MIN<=stuInGroup && stuInGroup<=CLASS_CAPACITY_MAX){
					wishingsInGroupMap.remove(wishingGroupId);
					subGroupMapIter.remove();
					//行政班（志愿组）
					satisfyGroup.put(wishingGroupId, wishingsInGroupMap.get(wishingGroupId));
				}else{	//选取
					
				}
			}
			
			//step 3-2: 更新志愿组合 wishingsInGroupMap
			
		}
		//step 3-3: 判断所有志愿是否仍能满足开班条件
		
		//step 4: 组合剩余wishingMaps中的志愿
		
		
		return result;
	}
	
	/**
	 * 获取21中志愿组学生人数映射
	 * @return
	 */
	private Map<String,Integer> getStuInGrp(List<Entry<String, Integer>> wishingMapList,Map<String,List<String>> wishingsInGroupMap){
		//step1: 初始化二十一种组合
		Map<String,Integer> subGroupMap = new HashMap<String,Integer>();				
		for(int i=0; i<courseHafCodes.length; i++){
			for(int j=i+1; j<courseHafCodes.length; j++){
				StringBuffer sb = new StringBuffer(courseHafCodes[i]);
				sb.append(courseHafCodes[j]);
				subGroupMap.put(sb.toString(), 0);
			}
		}
			
		//step2: 补充志愿组人数（有重复-志愿组最大人数）
		for(Entry<String,Integer>wishingEntry : wishingMapList){
			String wishingId = wishingEntry.getKey();
			List<String> wishingCodes = decodeHafmanCode(wishingId);
			int stuNum = wishingEntry.getValue();
			
			for(int i=0; i<wishingCodes.size(); i++){
				for(int j=0; j<wishingCodes.size(); j++){
					if(i==j){
						continue;
					}
					StringBuffer sb = new StringBuffer(wishingCodes.get(i));
					sb.append(wishingCodes.get(j));
					String wishingGroupId = sb.toString(); 
					if(subGroupMap.containsKey(wishingGroupId)){
						int totalNumInGroup = subGroupMap.get(wishingGroupId);
						totalNumInGroup+=stuNum;
						subGroupMap.put(wishingGroupId, totalNumInGroup);
						
						//填充辅助Map
						List<String> wishingIds = null;
						if(wishingsInGroupMap.get(wishingGroupId)==null){
							wishingsInGroupMap.put(wishingGroupId, new ArrayList<String>());
						}
						wishingIds = wishingsInGroupMap.get(wishingGroupId);
						wishingIds.add(wishingId);						
					}
				} //end for
			} //end for			
		} //end for
		return subGroupMap;
	}
	
	/**
	 * 判断所有志愿是否全部都能归入志愿组（即行政班开班）
	 * @param wishingMaps
	 * @param subGroupMap
	 * @return
	 */
	private boolean allWishingsCanInClass(Map<String,Integer> wishingMaps, Map<String, Integer>subGroupMap){
		int minInClass = CLASS_CAPACITY_MIN;
		
		Iterator<Entry<String, Integer>> wishingMapsIter = wishingMaps.entrySet().iterator();
		outer:
		while(wishingMapsIter.hasNext()){
			Entry<String, Integer> wishingEntry = wishingMapsIter.next();
			String wishingId = wishingEntry.getKey();
			
			//int curMax = 0;
			for(String groupId : subGroupMap.keySet()){
				int stuInGroup = subGroupMap.get(groupId);
				if(!judgeInCommon(wishingId, groupId,2,false) && (stuInGroup<minInClass)){
					continue;
				}
				
				//curMax =(curMax<stuInGroup)?stuInGroup:curMax;
				//当前志愿能够组班			
				if(stuInGroup>=minInClass){	
					continue outer;
				}				
			} //end for
			return false;
		} //end while
		
		//迭代完成（都满足条件）
		return true;
	}
	/**
	 * 将map转换成List
	 * @param wishingMaps
	 * @return
	 */
	private List<Entry<String,Integer>> convertMap2List(Map<String,Integer> wishingMaps){
		List<Entry<String,Integer>> entryList = new ArrayList<Map.Entry<String,Integer>>();
		for(Entry<String,Integer> wishingEntry : wishingMaps.entrySet()){
			entryList.add(wishingEntry);
		}
		return entryList;
	}
	
	/**
	 * 志愿及不均衡的情况（大志愿较多）
	 * @return
	 */
	public List<Map<String,Integer>> divClass4(List<Entry<String,Integer>> wishingMapList){
		List<Map<String,Integer>> result = new ArrayList<Map<String,Integer>>();
		int totalNum = 0;
		
		//List<Map<String, Integer>> clsGroup = recombinedUnCombinableWishings(wishingMapList);
		
		//第一个能单独开班的志愿位置
		int satisfyWishingFrom = 0;
		for(Entry<String, Integer> wishingEntry : wishingMapList){
			int stuInWishing = wishingEntry.getValue();
			if(stuInWishing<CLASS_CAPACITY_MAX){
				satisfyWishingFrom++;
			}
			totalNum += stuInWishing;
		}
		//人数太少,不可能分出班
		if(totalNum<CLASS_CAPACITY_MIN){
			return null;
		}
		
		Map<String,Integer> wishingMaps=null;
		if(satisfyWishingFrom == 0){
			result.clear();
			wishingMaps = convertToMap(wishingMapList);
		}
		
		int wishingCount = wishingMapList.size();
		int start = 0;
		
		//组合所有志愿（每个志愿组必须大于最小开班人数,并且遵循小志愿组合优先原则）
		finishOrAdjust:
		for( ; start<satisfyWishingFrom; start++){ //调整组合志愿的优先级别
			//初始化
			if(wishingMaps!=null && wishingMaps.size()!=0){
				wishingMaps.clear();
			}
			if(null!=result && result.size()!=0){
				result.clear();
			}
			wishingMaps = convertToMap(wishingMapList);
			sort(wishingMapList, false);
		
			
			//优先级start → (satisfyWishingFrom + start-1) % satisfyWishingFrom
			next:
			for(int i=start; (i-start)<satisfyWishingFrom; i++){
				Entry<String, Integer> wishingEntry = wishingMapList.get(i%satisfyWishingFrom);
				String key = wishingEntry.getKey();
				int stuInWishing = wishingEntry.getValue();
				int stuInGroup = stuInWishing;
				if(!wishingMaps.containsKey(key)){
					continue;
				}
				Map<String,Integer> combinedMap = new HashMap<String,Integer>();
				combinedMap.put(key, stuInWishing);
				wishingMaps.remove(key);
				
				//组合可组合志愿（合成志愿组 - 组合不能单独开班志愿）
				for(int j=start+1; j<(satisfyWishingFrom+start); j++){
					Entry<String, Integer> combinedWishingEntry = wishingMapList.get(j%satisfyWishingFrom);
					String combinedWishingKey = combinedWishingEntry.getKey();
					int combinedStuNum =  combinedWishingEntry.getValue();
					
					if(!wishingMaps.containsKey(combinedWishingKey) 
							|| !hasTwoSubsInCommon(combinedWishingKey, combinedMap,false)){
						continue;
					}
					stuInGroup+=combinedStuNum;					
					
					combinedMap.put(combinedWishingKey, combinedStuNum);
					wishingMaps.remove(combinedWishingKey);
					
					//满足行政班开班条件，迭代下一个志愿
					if(stuInGroup>=CLASS_CAPACITY_MIN){						
						result.add(combinedMap);
						continue next; //unreachable code
					}			
				}//end for
				
				//小志愿组合不能满足开班条件（加入大志愿）
				if(stuInGroup<CLASS_CAPACITY_MIN){
					for(int n=satisfyWishingFrom; n<wishingCount; n++){
						Entry<String, Integer> bigWishingEntry = wishingMapList.get(n);						
						String bigWishingId = bigWishingEntry.getKey();						
						
						//刷选两两相同科目的志愿
						if(wishingMaps.containsKey(bigWishingId)){
							int stuInBigWishing =  wishingMaps.get(bigWishingId);
							if(hasTwoSubsInCommon(bigWishingId, combinedMap,false)){
								//更新志愿人数（合并/拆分大志愿）
								int compromizeCount = 0;
								if(stuInBigWishing + stuInGroup<=CLASS_CAPACITY_MAX){//可以完全吞并
									compromizeCount = stuInBigWishing;
									wishingMaps.remove(bigWishingId);
								}else{	//不完全吞并
									compromizeCount = /*CLASS_CAPACITY_MIN*/SHREDSHOLD - stuInGroup;
									if(stuInBigWishing<=compromizeCount){
										wishingMaps.remove(bigWishingId);
									}else{
										wishingMaps.put(bigWishingId, stuInBigWishing - compromizeCount);
									}
								}						
								combinedMap.put(bigWishingId, compromizeCount);
																
								result.add(combinedMap);
								
								//判断小志愿是否已全部组合
								if(isAllSingleWishingSatisfy(wishingMaps,false)){
									break finishOrAdjust;
								}		
								
								//迭代下一小志愿
								continue next;
							}else if(combinedMap.size()>1
								//拆分小志愿,与大志愿进行组合						
								&& wishingMaps.containsKey(bigWishingId)){
									Iterator<Entry<String, Integer>> combinedMapIter = combinedMap.entrySet().iterator();
									while(combinedMapIter.hasNext()){
										Entry<String, Integer>combinedEntry = combinedMapIter.next();
										String wishingId = combinedEntry.getKey();
										
										int stuInCombinedWishing = combinedMap.get(wishingId);
										if(judgeInCommon(bigWishingId, wishingId,2,false)){
											Map<String,Integer> wishingGroup = new HashMap<String,Integer>();
											wishingGroup.put(wishingId, stuInCombinedWishing);
											
											//更新志愿人数（合并/拆分大志愿）
											int compromizeCount = 0;
											if(stuInCombinedWishing + stuInBigWishing<=CLASS_CAPACITY_MAX){//可以完全吞并
												compromizeCount = stuInBigWishing;
												wishingMaps.remove(stuInBigWishing);
											}else{	//不完全吞并
												compromizeCount = SHREDSHOLD/*CLASS_CAPACITY_MIN*/ - stuInCombinedWishing;
												wishingMaps.put(bigWishingId, stuInBigWishing - compromizeCount);
											}						
											wishingGroup.put(bigWishingId, compromizeCount);
											//sd
											result.add(wishingGroup);
											combinedMapIter.remove();
										}
										
										if(combinedMap.size()==0){
											continue next;
										}
									}//end while
							
							}//end else if
						}//end if
					}//end for(big wishing)					
					
					//如果仍有小志愿不能进入志愿组,则将这些小志愿归还到wishingMaps,重新组合
					int totalStuInCombinedMap = 0;
					for(int stuNum : combinedMap.values()){
						totalStuInCombinedMap += stuNum;
					}
					if(totalStuInCombinedMap<CLASS_CAPACITY_MIN && combinedMap.size()>0){
						wishingMaps.putAll(combinedMap);
						continue finishOrAdjust;
					}
					
					//判断小志愿是否已全部组合
					if(isAllSingleWishingSatisfy(wishingMaps,false)){
						break finishOrAdjust;
					}					
					
					return null;
					//System.out.println("code goes wrong!");
				}else{
					//小志愿组合可以满足开班条件
					result.add(combinedMap);		
					
					//判断小志愿是否已全部组合
					if(isAllSingleWishingSatisfy(wishingMaps,false)){
						break finishOrAdjust;
					}					
				}		
			}//end for(next)
		}//end for(finishOrAdjust)
		
		List<Map<String, Integer>> overNumberedWishings = splitOverNumberWishings2(wishingMaps);
		result.addAll(overNumberedWishings);
		//result.addAll(clsGroup);
		arrangeWishingGroups(result);
		
		//均衡选优
		List<Map<String, Integer>> resultWithPrio = new ArrayList<Map<String,Integer>>();		
		double sigma=-1;
		for(int i=0; i<10; i++){
			//总体均衡
			balanceDivResult2(result);
			double curSigma = resultVariance(result,true);
			if(-1d==sigma || sigma > curSigma){
				sigma = curSigma;
				resultWithPrio = result;
			}
		}
		return resultWithPrio;
	}
	
	
	/**
	 * 少数志愿不均衡情况（大志愿较少）
	 * @param wishingMapList
	 * @return
	 */
	public List<Map<String,Integer>> divClass4_1(List<Entry<String,Integer>> wishingMapList){
		List<Map<String,Integer>> result = new ArrayList<Map<String,Integer>>();
		int totalNum = 0;
		
		//第一个能单独开班的志愿位置
		int satisfyWishingFrom = 0;
		for(Entry<String, Integer> wishingEntry : wishingMapList){
			int stuInWishing = wishingEntry.getValue();
			if(stuInWishing<CLASS_CAPACITY_MAX){
				satisfyWishingFrom++;
			}
			totalNum += stuInWishing;
		}
		//人数太少,不可能分出班
		if(totalNum<CLASS_CAPACITY_MIN){
			return null;
		}
		
		//子志愿列表
		List<Entry<String,Integer>> subWishingMapList = new ArrayList<Map.Entry<String,Integer>>();
		sort(wishingMapList, false);
		
		subWishingMapList.addAll(wishingMapList.subList(0,satisfyWishingFrom));
		
		//组合剩余志愿
		Map<String,Integer> wishingMaps = convertToMap(subWishingMapList);
		List<Map<String, Integer>> satisfyWishingGroup = new ArrayList<Map<String,Integer>>();
		Collections.shuffle(subWishingMapList);
		combineGroup(wishingMaps, satisfyWishingGroup, subWishingMapList);
		
		sort1(satisfyWishingGroup, true);
		
		result = combineLeftBigWishings(satisfyWishingGroup, wishingMapList.subList(satisfyWishingFrom, wishingMapList.size()));
		
		//均衡志愿
		balanceDivResult2(result);
		
		return result;
	}
	
	/**
	 * 将剩余的大志愿与组合的小志愿组合合并
	 * @param satisfyWishingGroup
	 * @param leftBigWishingMapList
	 */
	private List<Map<String, Integer>> combineLeftBigWishings(List<Map<String, Integer>> satisfyWishingGroup,List<Entry<String,Integer>> leftBigWishingMapList){		
		Map<String,Integer> bigWishings = convertToMap(leftBigWishingMapList);
		
		
		for(Map<String, Integer> wishingGrp : satisfyWishingGroup){
			int stuNumInGrp = getTotalStuNumInWishingGroup(wishingGrp);
			//大于开班人数
			if(stuNumInGrp>=SHREDSHOLD){
				continue;
			}
			
			//大志愿组合完毕
			if(bigWishings.size()==0){
				break;
			}
			
			//组合大志愿与小志愿组合
			Iterator<Entry<String, Integer>>bigWishingsIter = bigWishings.entrySet().iterator();
			while(bigWishingsIter.hasNext()){
				Entry<String, Integer> bigWishingEntry = bigWishingsIter.next();
				String bigWishingId = bigWishingEntry.getKey();
				if(hasTwoSubsInCommon(bigWishingId, wishingGrp, false)){
					int stuInBigWishing = bigWishingEntry.getValue();
					int gap = SHREDSHOLD - stuNumInGrp;
					gap = (gap>=stuInBigWishing)?stuInBigWishing:gap;
					
					wishingGrp.put(bigWishingId, gap);
					
					//大志愿是否分解完毕
					stuInBigWishing-=gap;
					if(stuInBigWishing<=0){
						bigWishingsIter.remove();
					}else{
						bigWishingEntry.setValue(stuInBigWishing);
					}
				}				
			}

		}
		
		return satisfyWishingGroup;
	}
	/**
	 * 重新划分大志愿
	 * @param wishingMapList
	 */
	private List<Map<String, Integer>>  recombinedUnCombinableWishings(List<Entry<String, Integer>> wishingMapList){
		List<Map<String, Integer>> clsGroup = new ArrayList<Map<String,Integer>>();
		
		next:
		for(Entry<String, Integer>wishingMap : wishingMapList){
			String wishingId = wishingMap.getKey();
			int stuNum = wishingMap.getValue();
		
			if(stuNum<CLASS_CAPACITY_MIN){
				continue;
			}
			
			//寻找行政班最优分班人数
			int stuNumInClass=CLASS_CAPACITY_MAX;
			for(; stuNumInClass > CLASS_CAPACITY_MIN; stuNumInClass--){				
				if(stuNum%stuNumInClass<=CLASS_CAPACITY_MAX 
						&& CLASS_CAPACITY_MIN<=stuNum%stuNumInClass){
					continue next;
				}								
			}
			
			//可以单独形成班级的志愿加入班级列表
			for(int n=stuNum; n>SHREDSHOLD ;n-=SHREDSHOLD){
				Map<String,Integer> cls = new HashMap<String,Integer>();
				cls.put(wishingId, SHREDSHOLD);
				clsGroup.add(cls);
			}
					
			//不能形成班级的剩余志愿
			wishingMap.setValue(stuNum%SHREDSHOLD);
		}
		
		return clsGroup;
	}
	
	/**
	 * 大志愿分行政班
	 * @return
	 */
	public List<Map<String, Integer>>  splitOverNumberWishings2(Map<String, Integer> wishingMaps){
		List<Map<String, Integer>> singleWishingClassList = new ArrayList<Map<String,Integer>>();		
		
		for(String wishingId : wishingMaps.keySet()){
			int stuNum = wishingMaps.get(wishingId);
			
			//小于最小班级数的直接单独开班
			if(stuNum<CLASS_CAPACITY_MIN){
				Map<String, Integer> singleWishingClass = new HashMap<String,Integer>(); 
				singleWishingClass.put(wishingId, stuNum);
				singleWishingClassList.add(singleWishingClass);
				continue;
			}
			
			//最佳班级人数(人数均衡优先)
			int suitableStuNumInClass = 0;
			int stuNumInClass=SHREDSHOLD;//CLASS_CAPACITY_MAX;
			int broaden = (CLASS_CAPACITY_MAX-CLASS_CAPACITY_MIN)/2;
			for(int i=0; i<broaden; i++){				
				if(stuNum%(stuNumInClass+i)<=CLASS_CAPACITY_MAX 
						&& CLASS_CAPACITY_MIN<=stuNum%(stuNumInClass+i)){
					suitableStuNumInClass = stuNumInClass+i;
					break;
				}else if(stuNum%(stuNumInClass-i)<=CLASS_CAPACITY_MAX 
						&& CLASS_CAPACITY_MIN<=stuNum%(stuNumInClass-i)){
					suitableStuNumInClass = stuNumInClass-i;
					break;
				}
			}
			
			if(suitableStuNumInClass == 0){
				suitableStuNumInClass = SHREDSHOLD;
				//continue;
			}
			//形成班级
			for(int n=stuNum; n>0; n-=suitableStuNumInClass){
				Map<String, Integer> singleWishingClass = new HashMap<String,Integer>(); 
				
				singleWishingClass.put(wishingId, (n>=suitableStuNumInClass)?suitableStuNumInClass:stuNum%suitableStuNumInClass);
				singleWishingClassList.add(singleWishingClass);
			}
		}
		
		return singleWishingClassList;
	}
	
	/**
	 * 判断单个志愿是否都能满足行政班开班条件
	 * @param wishingMaps,judgeUpBound
	 * @return
	 */
	private boolean isAllSingleWishingSatisfy(Map<String, Integer> wishingMaps,boolean judgeUpBound){
		if(null==wishingMaps || wishingMaps.size()==0){
			return true;
		}
		
		for(int stuNum : wishingMaps.values()){
			if(stuNum<CLASS_CAPACITY_MIN){
				return false;
			}
			if(judgeUpBound && stuNum>CLASS_CAPACITY_MAX){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 计算结果的离散程度（样本方差）,决定是否取用该结果的判断依据
	 * @param commonWishingGroups
	 * @return -1.0：不存在
	 */
	public double resultVariance(List<Map<String, Integer>> result,boolean considerSubComm){
		if(result==null || result.size()<2){
			return -1.0d;
		}
		
		int count = result.size();
		int totalStuNum = 0;	//和
		int square = 0;	//平方和
		for(Map<String, Integer>commonWishingGroup  : result){
			int stuNum =  getTotalStuNumInWishingGroup(commonWishingGroup);
			int subFactor = 0;
			if(considerSubComm){
				int commonSubNum = 0;
				try{
					commonSubNum = getCommonCodesInGroup(commonWishingGroup).size();					
				}catch (Exception e) {					
					System.out.println("There is no common subjects in class/group!");
				}
				subFactor = (commonSubNum>=2)?0:(2-commonSubNum)*((CLASS_CAPACITY_MAX-CLASS_CAPACITY_MIN)/2);
			}
			
			totalStuNum += stuNum;
			square += (stuNum+subFactor) * (stuNum+subFactor);
		}
		
		return Math.sqrt((square - (totalStuNum * totalStuNum)/count)/(count-1));
	}
	
	/**
	 * 核验分班后数据
	 * @param result
	 * @return
	 */
	private boolean checkResultNum(List<Map<String,Integer>> result){
		return getTotalStuNumInDivResult(result) == getTotalStuNumInWishingGroup(originWishingMap);
	}
	
	public List<Map<String,Integer>> divClass5(List<Entry<String,Integer>> wishingMapList){
		//最优分班结果
		List<Map<String, Integer>> divProiResult = new ArrayList<Map<String,Integer>>();
		List<Map<String, Integer>> result = new ArrayList<Map<String,Integer>>();
		
		//与预设分班数的差距
		int gap = wishingMapList.size();
		//寻优最大循环次数
		int finalLoop = 100;
		
		//小志愿优先组合
		sort(wishingMapList, true);	
		result = combineWishings(wishingMapList, 2, false, true);
		
		//移除不能满足开班条件的志愿,对这些志愿重新组合
		List<Entry<String, Integer>> leftWishingMaps = new ArrayList<Map.Entry<String,Integer>>();
		removeUnSatisfyGroup(result, leftWishingMaps, CLASS_CAPACITY_MIN, CLASS_CAPACITY_MAX);
		int solideGroup = result.size();
		
		if(leftWishingMaps.size()<1){
			return result;
		}
		
		while(finalLoop-->0){
			
			Collections.shuffle(leftWishingMaps);
			List<Map<String, Integer>> leftResult = combineWishings(leftWishingMaps,1 ,false, true);
			
			//记录最接近预设的分班数			
			int divClassCount = leftResult.size() + solideGroup;
			int curGap = Math.abs(targetDivClassNum - divClassCount);
			if(curGap < gap){
				divProiResult.clear();
				divProiResult.addAll(leftResult);
				gap = curGap;
				if(gap==0){
					break;
				}
			}
		}
		
		result.addAll(divProiResult);
		
		//拆分/组合志愿组,达到预定的行政班数
		arrangeWishingGroups(result);
		
		//均衡班级人数
		balanceDivResult2(result);
		
		
		return result;
	}
	
	/**
	 * 将不满足开班条件[min,max]的志愿放入leftWishingMaps中
	 * @param result
	 * @param leftWishingMaps
	 * @param min
	 * @param max
	 */
	private void removeUnSatisfyGroup(List<Map<String, Integer>> result,List<Entry<String, Integer>> leftWishingMaps, int min, int max){
		if(CollectionUtils.isEmpty(result)){
			return;
		}
		Iterator<Map<String, Integer>> resultIter = result.iterator();
		while(resultIter.hasNext()){
			Map<String, Integer> group = resultIter.next();
			int stuNumInGroup = getTotalStuNumInWishingGroup(group);
			if(min>stuNumInGroup || max<stuNumInGroup){
				resultIter.remove();
				leftWishingMaps.addAll(group.entrySet());
			}			
		}
	}
	
	/**
	 * 拆分/组合志愿组,达到预定的行政班数
	 * @param wishingGroups
	 * @return
	 */
	public int arrangeWishingGroups(List<Map<String, Integer>> wishingGroups){
		int curClassNum = wishingGroups.size();
		if(curClassNum == targetDivClassNum){
			return targetDivClassNum;
		}
		
		
		//拆分
		while(curClassNum < targetDivClassNum){
			sort1(wishingGroups, false);
			Map<String, Integer> commonWishingGroup = wishingGroups.get(0);
			String wishingWithMaxStu = null;
			int maxStuNum = 0;
			for(String wishingId : commonWishingGroup.keySet()){
				if(maxStuNum<commonWishingGroup.get(wishingId)){
					maxStuNum = commonWishingGroup.get(wishingId);
					wishingWithMaxStu = wishingId;
				}
			}
			
			commonWishingGroup.remove(wishingWithMaxStu);
			Map<String,Integer> addedWishingGroup = new HashMap<String,Integer>();
			addedWishingGroup.put(wishingWithMaxStu, maxStuNum);
			wishingGroups.add(addedWishingGroup);
			
			curClassNum = wishingGroups.size();
		}
		
		//合并
		while(curClassNum > targetDivClassNum){
			sort1(wishingGroups, true);
			Map<String, Integer> splitWishingGroup = wishingGroups.remove(0);
			
			for(Map<String, Integer>commonWishingGroup : wishingGroups){
				Iterator<Entry<String, Integer>> entryIter = splitWishingGroup.entrySet().iterator();
				while(entryIter.hasNext()){
					Entry<String, Integer>entry = entryIter.next();
					String wishingId = entry.getKey();
					String commonCodes = getCommonCourse(commonWishingGroup);
					if(judgeInCommon(wishingId, commonCodes, 1,true)){
						commonWishingGroup.put(wishingId, splitWishingGroup.get(wishingId));
						entryIter.remove();
					}
				}
			}
			
			curClassNum = wishingGroups.size();
			if(splitWishingGroup.size()>0){
				System.out.println("error!");
				return 0;
			}
		}
		
		
		return wishingGroups.size();
	}
	
	/**
	 * 加入周山规则
	 * @param wishingMapList,techSolidate-技术科目是否参与固二科目,classLimit-班容限制条件
	 * @return
	 */
	public List<Map<String,Integer>> combineWishings(List<Entry<String, Integer>> wishingMapList, int commonSub, boolean techSolidate, boolean classLimit){
		if(CollectionUtils.isEmpty(wishingMapList)){
			return null;
		}
		
		//自由志愿（待组合志愿）
		Map<String, Integer> wishingMaps = convertToMap(wishingMapList);
		
		//分班结果
		List<Map<String, Integer>> commonWishingGroups = new ArrayList<Map<String,Integer>>();
		
		//志愿数
		int wishingMapListSize = wishingMapList.size();		
		
		//形成志愿组（行政班雏形）
		for(int i=0; i<wishingMapListSize; i++){
			Entry<String, Integer> wishing1 = wishingMapList.get(i);
			String wishingId1 = wishing1.getKey();
			if(!wishingMaps.containsKey(wishingId1)){
				continue;
			}
			int totalNum = wishing1.getValue();
			Map<String,Integer>commonWishingGroup = new HashMap<String,Integer>();
			commonWishingGroup.put(wishingId1, totalNum);
			wishingMaps.remove(wishingId1);
			StringBuffer sb = new StringBuffer(wishingId1);
			
			//选出能够组合的志愿	
			for(int n=wishingMapListSize-1; n>-1; n--){
				
				Entry<String, Integer> bondWishing = wishingMapList.get(n);
				String toBeBondWishingId = bondWishing.getKey();
				
				//从第一个能够满足行政班的志愿开始进行组合
				if(!wishingMaps.containsKey(toBeBondWishingId)){ 
					continue;
				}
				
				if(judgeInCommon(wishingId1, toBeBondWishingId,commonSub,false)){					
					int num2 = bondWishing.getValue();					
						
					//班容条件
					if(classLimit && (totalNum+num2)>CLASS_CAPACITY_MAX){
						break;
					}
					totalNum+=num2;
					
					//固二科目剔除技术						
					if(!techSolidate && null!=techId){
						List<String> commonHafCodes = getCommonHafCodes(wishingId1, toBeBondWishingId);
						if(commonHafCodes.contains(subjectIdHafCodeMap.get(techId))){
							continue;
						}
					}
					
					sb.append("+"); sb.append(toBeBondWishingId);
					String [] wishingsInWishingId1 = sb.toString().split("\\+");
					
					//第一对志愿组合时确定"定二"科目
					if(wishingsInWishingId1.length==2){													
						List<String> commonCodes = 
								getCommonHafCodes(wishingsInWishingId1[0],wishingsInWishingId1[1]);
						StringBuffer buffer = new StringBuffer();
						for(String hafCode : commonCodes){
							buffer.append(hafCode);
						}
						wishingId1 = buffer.toString();							
					}
						
					commonWishingGroup.put(toBeBondWishingId,num2);															
					wishingMaps.remove(toBeBondWishingId);
				}
			}	//end for
			
			commonWishingGroups.add(commonWishingGroup);				
			
			//全部组合完毕（人数待优化）
			if(wishingMaps.size()==0){
				return commonWishingGroups;
			}
		}	//end for
		return null;
	}
	
	
	/**
	 * 适用范围（普适性）
	 * 最优适配范围（人数较少,平均每个志愿≤20）
	 * 定一分班策略(优先技术定二)
	 * @param wishingMapList
	 * @return
	 */
	public List<Map<String,Integer>> divClass6(List<Entry<String,Integer>> wishingMapList, int targetDivClassNum,boolean advancedOpt,int solideSub){				
		//最优分班结果
		List<Map<String, Integer>> divProiResult = new ArrayList<Map<String,Integer>>();
		
		//初始化
		//班级人数下限
		int minimun = CLASS_CAPACITY_MIN;
		int finalLoop = 100;
		
		//有一科相同的志愿组（七选三共7组）
		nextLoop:
		while(finalLoop-->0){
			Map<String, Map<String, Integer>> oneCommonSubGroups = getOneCommonSubGroups(wishingMapList,true);
			
			//生成行政班级
			List<Map<String, Integer>> classGroup = new ArrayList<Map<String,Integer>>();
			
			//优先技术第一轮
			List<String> clsGroups = new ArrayList<String>(oneCommonSubGroups.keySet());
			Collections.shuffle(clsGroups);
			String selectGroup = null/*this.techId*/;
			
			Random r = new Random();
			int range = r.nextInt(CLASS_CAPACITY_MAX-CLASS_CAPACITY_MIN);
			minimun = CLASS_CAPACITY_MIN + range;
			
			int repeat = 0; //周期
			while(oneCommonSubGroups.size() > 0 && minimun>0){
				int loop = 0;
				while(classGroup.size()<targetDivClassNum
						&& splitClassFromGroup(oneCommonSubGroups, classGroup , selectGroup, minimun,false)){					
					loop++;
					selectGroup = clsGroups.get(loop%clsGroups.size());
				}
				repeat=(++repeat)%clsGroups.size();
				selectGroup = clsGroups.get(repeat);
				if(repeat == 0){
					minimun--;		
				}
			}
			
			//后处理
			if(oneCommonSubGroups.size() > 0){
				//补全班级数
				Map<String,Integer> leftWishings = null;
				if(reviateHigh(wishingMapList)){
					leftWishings = getLeftWishings(oneCommonSubGroups);
					if(leftWishings.size()>0){
						makeUpCls(classGroup, leftWishings);
					}
				}else if(advancedOpt){
					classGroup.addAll(makeUpCls(oneCommonSubGroups,solideSub,targetDivClassNum-classGroup.size()));
					leftWishings = getLeftWishings(oneCommonSubGroups);
					advancedOpt = false;
				}else{
					leftWishings = getLeftWishings(oneCommonSubGroups);
				}
				
				
				//继续定二、或者选择定一
				int commonSub = (placeAlgMethod==2)?1:2;
				if(procAfterDiv(classGroup, leftWishings,false,2)>0
						/*&&procAfterDiv(classGroup, leftWishings,false,1)>0*/
					&& procAfterDiv2(classGroup,leftWishings,false,commonSub)>0
					&&procAfterDiv(classGroup, leftWishings,false,1)>0){
					//继续后处理
					continue nextLoop;
				}	
				
			}
			
			//均衡班级人数
			balanceDivResult2(classGroup);
			
			//深度优化
			if(advancedOpt && !reviateHigh(wishingMapList)){
				deepOptResult(classGroup, 6, 2);
			}
			
			return classGroup;	
		}
		
		return divProiResult;
	}
	
	/**
	 * 剩余志愿-补全班级数
	 * @param oneCommonSubGroups
	 * @param commonSub
	 * @param targetClsNum
	 * @return
	 */
	private List<Map<String,Integer>> makeUpCls(Map<String, Map<String, Integer>> oneCommonSubGroups,int commonSub,int makeUpClsNum){
		List<Map<String,Integer>> rs = new ArrayList<Map<String,Integer>>();
		
		int curClsNum = oneCommonSubGroups.size();
		next:
		for(int n=0; n<makeUpClsNum; n++){
			//定一优先
			if(commonSub<2){
				if(oneCommonSubGroups.size()==0){
					continue;
				}
				//选出最多人数组合
				String dealGroup = null;
				int dealGroupStu = 0;
				Map<String, Integer> dealSubGroup = null;
				for(String grp : oneCommonSubGroups.keySet()){
					Map<String, Integer> oneCommonSubGroup = oneCommonSubGroups.get(grp);
					int totalStuNum = getTotalStuNumInWishingGroup(oneCommonSubGroup);
					if(totalStuNum>dealGroupStu){
						dealGroup = grp;
						dealGroupStu = totalStuNum;
						dealSubGroup = oneCommonSubGroup;
					}
				}
				//获取最佳组合
				List<Entry<String, Integer>> cls = getOptResultInGrp(dealSubGroup, CLASS_CAPACITY_MIN);
				
				
				//删除其它志愿组中包含班级中的志愿
				Iterator<Entry<String, Map<String, Integer>>>commonGroupsIter = oneCommonSubGroups.entrySet().iterator();
				while(commonGroupsIter.hasNext()){
					 Entry<String, Map<String, Integer>>commonGroupsEntry = commonGroupsIter.next();
					 Map<String, Integer> oneCommonSubGroup = commonGroupsEntry.getValue();
					
					 for(Entry<String,Integer> wishingEntry : cls){
						 oneCommonSubGroup.remove(wishingEntry.getKey());
					 }
					 
					 if(oneCommonSubGroup.size() == 0){
						 commonGroupsIter.remove();
					 }
				}
				rs.add(convertToMap(cls));
			}else{//定二优先
				for(int clsNum=SHREDSHOLD; clsNum>0; clsNum--){
					for(String grp : oneCommonSubGroups.keySet()){
						if(splitClassFromGroup(oneCommonSubGroups, rs , grp, clsNum,false)){
							continue next;
						}
					}
				}
			}
			
		}
		return rs;
	}
	
	/**
	 * 判断志愿人数是否极度不均衡
	 * @param wishingMapList
	 * @return
	 */
	private boolean reviateHigh(List<Entry<String,Integer>>wishingMapList){
		for(Entry<String, Integer> wishing: wishingMapList){
			if(wishing.getValue()>2*CLASS_CAPACITY_MAX){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 补全班级数
	 * @param classGroup
	 * @param leftWishings
	 */
	private void makeUpCls(List<Map<String, Integer>> classGroup,Map<String,Integer> leftWishings){
		int clsGap = targetDivClassNum-classGroup.size();
		List<Entry<String, Integer>> leftWishingEntrys = convertMap2List(leftWishings);
		
		//粗略补偿
		for(int i=0; i<clsGap && leftWishingEntrys.size()>0; i++){		
			sort(leftWishingEntrys, true);
			Entry<String, Integer> leftEntry = leftWishingEntrys.get(0);
			Map<String,Integer> reminder1 = new HashMap<String,Integer>();
			int stuInEntry = leftEntry.getValue();
			if(stuInEntry>SHREDSHOLD){
				reminder1.put(leftEntry.getKey(), SHREDSHOLD);
				leftEntry.setValue(stuInEntry-SHREDSHOLD);
			}else{
				leftWishings.remove(leftEntry.getKey());
				leftWishingEntrys.remove(0);
				reminder1.put(leftEntry.getKey(), stuInEntry);
			}
			
			classGroup.add(reminder1);
		}
	}
	
	/**
	 * 分班6改进版 - 适用范围（普适性）
	 * 最优适配范围（人数较多,平均每个志愿＞20,但在分班算法4之外）
	 * 定一分班策略(优先技术定二)
	 * @param wishingMapList
	 * @return
	 */
	public List<Map<String,Integer>> divClass6_1(List<Entry<String,Integer>> wishingMapList, int targetDivClassNum){
		List<Map<String,Integer>> result = new ArrayList<Map<String,Integer>>();
		
		//打乱,避免每次分班结果一致
		Collections.shuffle(wishingMapList);
		
		//补偿人数
		int compromiseCount = 0;	
		
		Iterator<Entry<String, Integer>>wishingMapIter = wishingMapList.iterator();
		while(wishingMapIter.hasNext()){
			Entry<String, Integer>wishingMapEntry = wishingMapIter.next();
			int stuNum = wishingMapEntry.getValue();
			
			//形成行政班
			if(stuNum>=CLASS_CAPACITY_MIN && stuNum <= CLASS_CAPACITY_MAX){
				Map<String,Integer> cls = new HashMap<String,Integer>();
				cls.put(wishingMapEntry.getKey(), stuNum);
				wishingMapIter.remove();
				result.add(cls);
				compromiseCount += (SHREDSHOLD - stuNum);
			}
			
			//进行补偿
			if(stuNum>CLASS_CAPACITY_MAX){
				Map<String,Integer> cls = new HashMap<String,Integer>();
				int maxComp = SHREDSHOLD-CLASS_CAPACITY_MIN;
				int compCount = (Math.abs(compromiseCount)>=maxComp)?
						maxComp*(compromiseCount/Math.abs(compromiseCount)):compromiseCount%(maxComp);
				
				//完全吞并
				if(compCount+SHREDSHOLD>=stuNum){				
					cls.put(wishingMapEntry.getKey(), stuNum);
					wishingMapIter.remove();
					result.add(cls);
					compromiseCount += (SHREDSHOLD - stuNum);
					continue;
				}
				
				//生成行政班并更改志愿班级人数
				int stuNumInCls = SHREDSHOLD + compCount;
				cls.put(wishingMapEntry.getKey(), stuNumInCls);
				wishingMapEntry.setValue(stuNum-stuNumInCls);
				result.add(cls);
				compromiseCount += compCount;
			}
		}
		
		result.addAll(divClass6(wishingMapList, targetDivClassNum-result.size(),true,1));
		
		//核对行政班级数
		arrangeWishingGroups(result);
		
		//均衡班级人数
		balanceDivResult2(result);
		///deepOptResult(result, 61, 1);
		
		return result;
	}
	
	/**
	 * 获取分班后的剩余志愿
	 * @param oneCommonSubGroups
	 * @return
	 */
	
	public List<Map<String,Integer>> divClassWithParams(List<Entry<String,Integer>> wishingMapList, int targetDivClassNum,int solideSub){
		//最优分班结果
		List<Map<String, Integer>> divProiResult = new ArrayList<Map<String,Integer>>();
		
		//初始化
		//班级人数下限
		int minimun = CLASS_CAPACITY_MIN;
		int finalLoop = 100;
		
		//有一科相同的志愿组（七选三共7组）
		nextLoop:
		while(finalLoop-->0){
			Map<String, Map<String, Integer>> oneCommonSubGroups = getOneCommonSubGroups(wishingMapList,true);
			
			//生成行政班级
			List<Map<String, Integer>> classGroup = new ArrayList<Map<String,Integer>>();
			
			//优先技术第一轮
			List<String> clsGroups = new ArrayList<String>(oneCommonSubGroups.keySet());
			Collections.shuffle(clsGroups);
			String selectGroup = null;
			
			Random r = new Random();
			int range = r.nextInt(CLASS_CAPACITY_MAX-CLASS_CAPACITY_MIN);
			minimun = CLASS_CAPACITY_MIN + range;
			
			//固二
			int repeat = 0; //周期
			while(oneCommonSubGroups.size() > 0 && minimun>0){
				int loop = 0;
				while(classGroup.size()<targetDivClassNum
						&& splitClassFromGroup(oneCommonSubGroups, classGroup , selectGroup, minimun,false)){					
					loop++;
					selectGroup = clsGroups.get(loop%clsGroups.size());
				}
				repeat=(++repeat)%clsGroups.size();
				selectGroup = clsGroups.get(repeat);
				if(repeat == 0){
					minimun--;		
				}
			}
			
			//补全班级数
			int makeUpClsNum = targetDivClassNum - classGroup.size();
			classGroup.addAll(makeUpCls(oneCommonSubGroups, solideSub, makeUpClsNum));
			
			
		}
		return divProiResult;
	}
	
	private Map<String,Integer> getLeftWishings(Map<String, Map<String, Integer>> oneCommonSubGroups){
		Map<String,Integer> leftWishings = new HashMap<String,Integer>();
		
		for(String hafcode : courseHafCodes){
			Map<String,Integer> removedWishings = oneCommonSubGroups.remove(hafcode);
			if(removedWishings == null || removedWishings.size() == 0){
				continue;
			}
			leftWishings.putAll(removedWishings);
			removeUselessGroup(oneCommonSubGroups, removedWishings, hafcode);
			if(oneCommonSubGroups.size()==0){
				break;
			}
		}
		
		
		return leftWishings;
	}
	
	/**
	 * 从最小人数志愿中选出班级（默认从最小组合拆分）
	 * @param oneCommonSubGroups
	 * @param classGroup
	 * @param groupCode：优先拆分的志愿组
	 */
	private boolean splitClassFromGroup(Map<String, Map<String, Integer>> oneCommonSubGroups,List<Map<String,Integer>> classGroup,String groupCode, int minimum,boolean techExclude){
		String divGroup = groupCode;
		int broadenWidth = CLASS_CAPACITY_MIN - minimum;
		
		int minGroup = 0;
		List<String> groups = new ArrayList<String>();
		groups.addAll(oneCommonSubGroups.keySet());
		
		//默认选最小的进行拆分
		if(StringUtils.isEmpty(groupCode) 
				|| !canGroupFormClass(groupCode, oneCommonSubGroups.get(groupCode), minimum, true)){		
			//选出最小人数的志愿组
			for(String hafcode : groups){
				//技术定二优先级最低
				if(techExclude && hafcode.equals(subjectIdHafCodeMap.get(techId))){
					continue;
				}
				int curStuNum = getTotalStuNumInWishingGroup(oneCommonSubGroups.get(hafcode));
				if(!canGroupFormClass(hafcode, oneCommonSubGroups.get(hafcode), minimum, true)
						||oneCommonSubGroups.get(hafcode).size()==1){
					continue;
				}
				if(minGroup>curStuNum || minGroup == 0){
					minGroup = curStuNum;
					divGroup = hafcode;
				}			
			}
		}else{
			minGroup = getTotalStuNumInWishingGroup(oneCommonSubGroups.get(groupCode));
		}
		//没有能够满足行政班开班的志愿组
		if(minGroup == 0){
			return false;
		}
		
		
		//分裂（形成行政班）
		Map<String, Integer> splitGroup = oneCommonSubGroups.get(divGroup);
		for(String hafCode : courseHafCodes){
			if(hafCode.equals(divGroup)){
				continue;
			}
			//技术优先级最低
			if(techExclude && hafCode.equals(subjectIdHafCodeMap.get(techId))){
   				continue;
			}
			
			//行政班志愿组
			Map<String,Integer> wishingGroup = new HashMap<String,Integer>();
			
			//查找另外的可以满足行政班开班条件的定一科目
			int stuNumInSub = 0 ;
			for(String wishingId : splitGroup.keySet()){
				List<String> hafcodes = decodeHafmanCode(wishingId);
				if(hafcodes.contains(hafCode)){
					stuNumInSub += splitGroup.get(wishingId);
					wishingGroup.put(wishingId, splitGroup.get(wishingId));
				}
			}
			//一科留作补偿用
			if(wishingGroup.size()==1 && stuNumInSub>CLASS_CAPACITY_MAX){
				continue;
			}
			//表明当前组合不可用
			if(stuNumInSub < minimum){
				continue;
			}
			
			List<Entry<String, Integer>> optResult = getOptResultInGrp(wishingGroup, minimum);
			if(null==optResult){
				return false;
			}
			
			int prioStuNum = getTotalStuNumInWishingGroup(convertToMap(optResult));			
			
			//不存在、或者人数超额
			String otherWishingId = null;
			int addedStuNum = 0;
			if(prioStuNum < CLASS_CAPACITY_MIN || prioStuNum>CLASS_CAPACITY_MAX + broadenWidth){
				if(prioStuNum>=SHREDSHOLD){
					continue;
				}
				//超额人数进行分解
				if(getTotalStuNumInWishingGroup(wishingGroup)>CLASS_CAPACITY_MIN){
					List<String> wishingList = new ArrayList<String>();
					for(Entry<String, Integer> optResultEntry : optResult){
						wishingList.add(optResultEntry.getKey());
					}
					
					for(Entry<String, Integer>wishingGroupEntry : wishingGroup.entrySet()){
						String wishingId = wishingGroupEntry.getKey();
						if(!wishingList.contains(wishingId)){
							int otherStu = wishingGroup.get(wishingId);
							//计算分裂出的人数
							if((otherStu+prioStuNum)>=CLASS_CAPACITY_MIN){
								otherWishingId = wishingId;
								addedStuNum = (otherStu+prioStuNum)>SHREDSHOLD?
										(SHREDSHOLD-prioStuNum):otherStu;
									break;
							}else{
								//理论上不可达
								optResult.add(wishingGroupEntry);
							}
						}
					}
				}else{
					continue;
				}
			}
			
			//加入班级列表
			Map<String,Integer> cls = new HashMap<String, Integer>();
			Iterator<Entry<String, Integer>> optResultIter = optResult.iterator();
			while(optResultIter.hasNext()){
				Entry<String, Integer> wishingEntry = optResultIter.next();
				cls.put(wishingEntry.getKey(), wishingEntry.getValue());
			}
			classGroup.add(cls);
			//加入分裂出来的志愿
			if(null!=otherWishingId){
				cls.put(otherWishingId, addedStuNum);
			}
			 
			//删除其它志愿组中包含班级中的志愿
			Iterator<Entry<String, Map<String, Integer>>>commonGroupsIter = oneCommonSubGroups.entrySet().iterator();
			while(commonGroupsIter.hasNext()){
				 Entry<String, Map<String, Integer>>commonGroupsEntry = commonGroupsIter.next();
				 Map<String, Integer> oneCommonSubGroup = commonGroupsEntry.getValue();
				 for(String wishingId : cls.keySet()){
					 Integer otherStu = oneCommonSubGroup.get(wishingId);						 
					 //处理分裂志愿
					 if(wishingId.equals(otherWishingId) && null!=otherStu){
						 int partOfOtherStu = cls.get(wishingId);
						 if(otherStu > partOfOtherStu){
							 oneCommonSubGroup.put(otherWishingId, otherStu - partOfOtherStu);
							 continue;
						 }
					 }
					//移除已形成班级的其它志愿组中志愿
					oneCommonSubGroup.remove(wishingId);					
				 }
				 if(oneCommonSubGroup.size() == 0){
					 commonGroupsIter.remove();
				 }
			}
			
			
			
			return wishingGroup.size()>0;
		}
		divGroup = null;
		
		return false;
	}
	
	/**
	 * 获取志愿组中最能匹配班级数的子组合
	 * @param wishingGroup
	 * @param minimum
	 * @return
	 */
	private List<Entry<String,Integer>> getOptResultInGrp(Map<String,Integer> wishingGroup, int minimum){
		if(null==wishingGroup || wishingGroup.size()==0){
			return null;
		}
		
		//选出最接近阈值的组合
		List<List<Entry<String,Integer>>> result = new ArrayList<List<Entry<String,Integer>>>();
		List<Entry<String,Integer>> data = new ArrayList<Entry<String,Integer>>();
		data.addAll(wishingGroup.entrySet());
		//输出A(n,n)的全排列
		for(int i = 1; i <= wishingGroup.size(); i++)
			combinerSelect(data, new ArrayList<Entry<String,Integer>>(), wishingGroup.size(), i,result); 
		
		List<Entry<String, Integer>> optResult = new ArrayList<Map.Entry<String,Integer>>();
		int gap = minimum;
		for(List<Entry<String,Integer>> stuNumList : result){
			int totalStuInGroup = 0;
			for(Entry<String,Integer> stuNumEntry : stuNumList){
				totalStuInGroup += stuNumEntry.getValue();
			}				
			
			int curGap = Math.abs(totalStuInGroup - SHREDSHOLD);
			if(gap > curGap){
				gap = curGap;
				optResult = stuNumList;
				if(gap == 0){	//相等（最优）
					break;
				}
			}				
		}
		return optResult;
	}
	
	/**
	 * 组合定一科目组（有重复）
	 * @param wishingMapList
	 * @return
	 */
	private Map<String,Map<String,Integer>> getOneCommonSubGroups(List<Entry<String,Integer>> wishingMapList,boolean excludeTech){
		Map<String,Map<String, Integer>> subWishingGroupMap = new HashMap<String,Map<String, Integer>>();
		
		//节省时间,存放志愿Id与hafcode映射
		Map<String,List<String>> wishingIdHafcodeMap = new HashMap<String,List<String>>();
		for(Entry<String,Integer>wishingEntry : wishingMapList){
			String wishingId = wishingEntry.getKey();
			List<String> hafcodes = decodeHafmanCode(wishingId);
			wishingIdHafcodeMap.put(wishingId, hafcodes);
		}
		
		
		
		//七个定一科目组
		for(String hafCode : courseHafCodes){
			//过滤技术
			if(excludeTech && hafCode.equals(techId)){
				continue;
			}
			
			Map<String, Integer> wishingGroup = subWishingGroupMap.get(hafCode);
			if(wishingGroup == null){
				wishingGroup = new HashMap<String,Integer>();
			}
			
			//定一科目组
			for(Entry<String,Integer>wishingEntry : wishingMapList){
				String wishingId = wishingEntry.getKey();
				List<String> hafcodes = wishingIdHafcodeMap.get(wishingId);
				if(hafcodes.contains(hafCode)){
					int value = wishingEntry.getValue();
					wishingGroup.put(wishingId, value);
				}				
			}
				
			
			subWishingGroupMap.put(hafCode, wishingGroup);
		}
		
		return subWishingGroupMap;
	}
	
	/**
	 * 判断剩余志愿是否全部都能形成行政班
	 * @param leftGroups
	 * @param min
	 * @param has2
	 * @return
	 */
	private boolean canGroupFormClass(String hafcode,Map<String,Integer> leftGroup, int min, boolean has2){
		//单科验证
		int totalStuNumInGroup = getTotalStuNumInWishingGroup(leftGroup);
		if(totalStuNumInGroup < min){
			return false;
		}
		
		//需要两科相同
		if(has2){
			//节省时间,存放志愿Id与hafcode映射
			Map<String,List<String>> wishingIdHafcodeMap = new HashMap<String,List<String>>();
			for(Entry<String,Integer>wishingEntry : wishingMapList){
				String wishingId = wishingEntry.getKey();
				List<String> hafcodes = decodeHafmanCode(wishingId);
				wishingIdHafcodeMap.put(wishingId, hafcodes);
			}
			
			//七个定一科目组
			for(int j=0; j<courseHafCodes.length && !hafcode.equals(courseHafCodes[j]); j++){
				int stuNumInSub = 0;
				for(String wishingId : leftGroup.keySet()){
					List<String> hafcodes = wishingIdHafcodeMap.get(wishingId);
					if(hafcodes.contains(courseHafCodes[j])){
						stuNumInSub += leftGroup.get(wishingId);
					}
					
					//一科满足就证明整个组可以形成行政班
					if(stuNumInSub>=min){
						return true;
					}
				}					
			}
		}
		
		return false;
	}
	
	/**
	 * 移除无用组（不能形成行政班）
	 * @param leftGroups
	 * @param toBeRemoved
	 * @return
	 */
	private boolean removeUselessGroup(Map<String,Map<String,Integer>> leftGroups,Map<String,Integer> toBeRemoved,String toBeRemoveCode){
		Map<String,Integer> removedWishings = new HashMap<String,Integer>();
		//删除不能组合行政班的志愿组
		finalLoop:
		for(String hafCode : leftGroups.keySet()){
			Map<String, Integer> leftGroup = leftGroups.get(hafCode);
			for(String wishingId : leftGroup.keySet()){
				if(toBeRemoveCode.contains(wishingId)){
					int stuNum = toBeRemoved.remove(wishingId);
					removedWishings.put(wishingId, stuNum);
					if(toBeRemoved.size() == 0){
						break finalLoop;
					}
				}
			}
		}
	
		//移除空志愿组
		for(String hafCode : courseHafCodes){
			if(leftGroups.get(hafCode) != null && leftGroups.get(hafCode).size()==0){
				leftGroups.remove(hafCode);
			}
		}
		
		if(toBeRemoved.size() == 0){
			leftGroups.remove(toBeRemoveCode);
			return true;
		}
		
		//还原
		toBeRemoved.putAll(removedWishings);
		return false;
	}
	
	/**
	 * 计算最小开班数
	 * @param wishingMaps
	 * @return
	 */
	private int minGroupOfWishings(List<Entry<String, Integer>> wishingMaps){		
		List<String> wishings = new ArrayList<String>();
		next:
		for(Entry<String, Integer> entry : wishingMaps){
			String wishingId = entry.getKey();
			if(wishings.size()==0){
				wishings.add(wishingId);
			}else{				
				for(String wishing : wishings){
					if(judgeInCommon(wishing, wishingId, 2, false)){
						continue next;
					}
				}
			}
			wishings.add(wishingId);
		}
		
		return wishings.size();
	}
	
	public static <E> void combinerSelect(List<E> data, List<E> workSpace, int n, int k,List<List<E>>result) {
		List<E> copyData;
		List<E> copyWorkSpace;
		
		if(workSpace.size() == k) {
			result.add(workSpace);
		}
		
		for(int i = 0; i < data.size(); i++) {
			copyData = new ArrayList<E>(data);
			copyWorkSpace = new ArrayList<E>(workSpace);
			
			copyWorkSpace.add(copyData.get(i));
			for(int j = i; j >=  0; j--)
				copyData.remove(j);
			combinerSelect(copyData, copyWorkSpace, n, k,result);
		}
		
	}
	
	public static void main(String[] args) {
		List<List<Character>> result = new ArrayList<List<Character>>();
		List<Character> data = new ArrayList<Character>();
		data.add('a');
		data.add('b');
		data.add('c');
		data.add('d');
		
		//输出A(n,n)的全排列
		for(int i = 1; i <= data.size(); i++)
			combinerSelect(data, new ArrayList<Character>(), data.size(), i,result); 
		
		System.out.println(result);
	}
	
	public class MapEntry<K,V> implements Map.Entry<K, V>{
		private K key;
		private V value;
		
		
		public MapEntry(K key, V value) {
			super();
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			// TODO Auto-generated method stub
			return key;
		}

		@Override
		public V getValue() {
			// TODO Auto-generated method stub
			return value;
		}

		@Override
		public V setValue(V value) {
			// TODO Auto-generated method stub
			this.value = value;
			return value;
		}		
	}
	
	//合并大志愿
	public void cutBigWishings(List<Entry<String,Integer>> wishingMapList){
		List<Entry<String,Integer>> bigWishings = new ArrayList<Map.Entry<String,Integer>>();
		for(Entry<String,Integer> wishingEntry : wishingMapList){
			//if(wishingEntry.getValue()>max)
		}
	}
	
	/**
	 * 定二走一分班（分两大模块：1、定一、定二算法,2、大志愿分隔算法）
	 * @param wishingMapList
	 */
	public static void dezyDivClass(List<Entry<String,Integer>> wishingMapList){
		
	}
}

