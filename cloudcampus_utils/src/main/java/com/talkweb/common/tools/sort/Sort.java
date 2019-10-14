package com.talkweb.common.tools.sort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Classroom;

/**
 * @ClassName ClassNameSort.java
 * @author liboqi
 * @version 1.0
 * @Description 班级名称排序
 * @date 2015年12月18日 下午5:31:59
 */
public class Sort {
	//中山,汕头,广州,安庆,阳江,南京,武汉,北京,安阳,北方,初二,初三,初一,初一,
	//初拾一,初壹零,高特一班,高阿一班,高阿A1303,高阿a1303,高阿C1303,高阿C1302,
	//1302,1301,1201,abs,abc,abd,zyt,a1,a2,b2
	private static Class<? extends Object> class2 = JSONObject.class;
	private static Class<? extends Object> class3 = String.class;
	private static Class<? extends Object> class4 = Integer.class;
	private static Class<? extends Object> class5 = HashMap.class;
	private static Class<? extends Object> class6 = Classroom.class; 
	/***
	 * 排序功能 List<?>
	 * @param <T>
	 * @param sortType 排序规则(SortEnum.descEndingOrder[降序] ，SortEnum.ascEnding0rder[升序] 默认为：SortEnum.ascEnding0rder)
	 * @param List<?> 排序列表 
	 * @param columnName 排序的字段（默认为：className）[支持多列排序，例如：className,subjectId，多个排序字段已逗号分隔]
	 * @return
	 * @throws Exception
	 */
	public static List<?> sort(SortEnum sortType,List<?> list,String columnName) throws Exception{
		if(CollectionUtils.isEmpty(list))
		{
			return list;
		}
		boolean flag = false;
		boolean forSort = false;
		List<?> joinData = null;
		if(null != sortType){
			flag = Boolean.parseBoolean(sortType.getValue());
		}
		if(StringUtils.isBlank(columnName)){
			columnName = "className";
		}else{
			if(columnName.indexOf(",") >= 0){
				forSort = true;
			}
		}
		if(forSort){
			String[] split = columnName.split(",");
			for (String string : split) {
				if(StringUtils.isNotBlank(string)){
					List<String> sortList = new ArrayList<String>();
					Integer type = checkData(list,string,sortList);
					if(type == 3){
						List<String> newList = getNewList(sortList);
						sortList = numberSort(newList,flag);
					}
					if(type == 4){
						sortList = romeSort(sortList, flag);
					}
					if(type == 5){
						metalSort(sortList, flag);
					}
					if(type == 6){
						letterSort(sortList, flag);
					}
					if(type == 7){
						chineseSort(sortList,flag);
					}
					list = joinData(list,sortList,string);
				}
			}
			joinData = list;
		}else{
			List<String> sortList = new ArrayList<String>();
			Integer type = checkData(list,columnName,sortList);
			if(type == 3){
				List<String> newList = getNewList(sortList);
				sortList = numberSort(newList,flag);
			}
			if(type == 4){
				sortList = romeSort(sortList, flag);
			}
			if(type == 5){
				metalSort(sortList, flag);
			}
			if(type == 6){
				letterSort(sortList, flag);
			}
			if(type == 7){
				chineseSort(sortList,flag);
			}
			joinData = joinData(list,sortList,columnName);
		}
		
		return joinData;
	}
	
	@SuppressWarnings("unchecked")
	private static Integer checkData(List<?> list,String columnName,List<String> sortList) throws Exception{
		for (Object obj : list) {
			Class<? extends Object> class1 = obj.getClass();
			if(class1 == class2){
				JSONObject jsonObject = (JSONObject)obj;
				if(!jsonObject.containsKey(columnName.trim())){
					throw new Exception("排序字段不存在["+jsonObject.toJSONString()+"]");
				}
				String columnStr = jsonObject.getString(columnName);
				if(StringUtils.isBlank(columnStr)){
					throw new Exception("排序字段值为空["+jsonObject.toJSONString()+"]");
				}
				sortList.add(columnStr);
			}else if(class1 == class3){
				String str = (String)obj;
				if(StringUtils.isBlank(str)){
					throw new Exception("排序值不存在");
				}
				sortList.add(str);
			}else if(class1 == class4){
				Integer str = (Integer)obj;
				if(StringUtils.isBlank(str.toString())){
					throw new Exception("排序值不存在");
				}
				sortList.add(str.toString());
			}else if(class1 == class5){
				Map<Object, Object> map = (Map<Object, Object>)obj;
				if(!map.containsKey(columnName.trim())){
					throw new Exception("排序字段不存在["+map.toString()+"]");
				}
				String columnStr = map.get(columnName).toString();
				if(StringUtils.isBlank(columnStr)){
					throw new Exception("排序字段值为空["+map.toString()+"]");
				}
				sortList.add(columnStr);
			}else if(class1 == class6){
				Classroom classroom = (Classroom)obj;
				if(StringUtils.isBlank(classroom.getClassName())){
					throw new Exception("排序字段不存在[ClassName]");
				}
				String columnStr = classroom.getClassName();
				if(StringUtils.isBlank(columnStr)){
					throw new Exception("排序字段值为空["+classroom.getClassName()+"]");
				}
				sortList.add(columnStr);
			}else{
				throw new Exception("暂时不支持该类型的排序");
			}
		}
		return checkSortType(sortList);
	}
	
	private static Integer checkSortType(List<String> list){
		int index = Integer.valueOf(SortEnum.numberOrder.getValue());
		int maxIndex = Integer.valueOf(SortEnum.chineseOrder.getValue());
		boolean numeric,romeic,metalic,letteric;
		int numberNum = 0,romeNum = 0,metalNum = 0,letterNum = 0;
		for (;index < maxIndex;index++) {
			for (String string : list) {
				if(index == 3){
					numeric = NumberSort.isNumeric(string);
					if(!numeric){
						++numberNum;
						break;
					}
				}
				if(index == 4){
					romeic = RomeSort.isRome(string);
					if(!romeic){
						++romeNum;
						break;
					}
				}
				if(index == 5){
					metalic = MetalSort.isMetal(string);
					if(!metalic){
						++metalNum;
						break;
					}
				}
				if(index == 6){
					letteric = LetterSort.isLetter(string);
					if(!letteric){
						++letterNum;
						break;
					}
				}
			}
			if(index == 3 && numberNum == 0){
				break;
			}
			if(index == 4 && romeNum == 0){
				break;
			}
			if(index == 5 && metalNum == 0){
				break;
			}
			if(index == 6 && letterNum == 0){
				break;
			}
		}
		if(numberNum != 0 && romeNum != 0 && metalNum != 0 && letterNum != 0){
			index = 7;
		}
		return index;
	}
	
	private static List<String> numberSort(List<String> sortList,boolean flag){
		List<String> newList = new ArrayList<String>();
		String[] array = sortList.toArray(new String[sortList.size()]);
		if(flag){
			NumberSort.halfSortBySx(array);
		}else{
			NumberSort.halfSortByJx(array);
		}
		for (String string : array) {
			newList.add(string);
		}
		return newList;
	}
	
	private static List<String> romeSort(List<String> sortList,boolean flag) throws Exception{
		if(flag){
			sortList = RomeSort.sortByJx(sortList);
		}else{
			sortList = RomeSort.sortBySx(sortList);
		}
		return sortList;
	}
	
	private static void metalSort(List<String> sortList,boolean flag) throws Exception{
		String[] array = sortList.toArray(new String[sortList.size()]);
		if(flag){
			MetalSort.sortByJx(array);
		}else{
			MetalSort.sortBySx(array);
		}
	}
	
	private static void letterSort(List<String> sortList,boolean flag) throws Exception{
		String[] array = sortList.toArray(new String[sortList.size()]);
		if(flag){
			LetterSort.sortByJx(array);
		}else{
			LetterSort.sortBySx(array);
		}
	}
	
	private static void chineseSort(List<String> sortList,boolean flag) throws Exception{
		if(flag){
			ChineseSort.sortByJx(sortList);
		}else{
			ChineseSort.sortBySx(sortList);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static List<?> joinData(List<?> list,List<String> sortList,String columnName){
		List<Object> jsonObjects = new ArrayList<Object>();
		List<String> newList = getNewList(sortList);
		for (String sortStr : newList) {
			for (Object obj : list) {
				Class<? extends Object> class1 = obj.getClass();
				if(class1 == class2){
					JSONObject jsonObject = (JSONObject)obj;
					Object object = jsonObject.get(columnName);
					if(sortStr.equals(object)){
						jsonObjects.add(jsonObject);
					}
				}
				if(class1 == class3){
					String str = (String)obj;
					if(sortStr.equals(str)){
						jsonObjects.add(str);
					}
				}
				if(class1 == class4){
					Integer str = (Integer)obj;
					if(sortStr.equals(str)){
						jsonObjects.add(str);
					}
				}
				if(class1 == class5){
					Map<Object, Object> map = (Map<Object, Object>)obj;
					Object object = map.get(columnName).toString();
					if(sortStr.equals(object)){
						jsonObjects.add(map);
					}
				}
				if(class1 == class6){
					Classroom classroom = (Classroom)obj;
					Object object = classroom.getClassName();
					if(sortStr.equals(object)){
						jsonObjects.add(classroom);
					}
				}
			}
		}
		return jsonObjects;
	}
	
    private static List<String> getNewList(List<String> li){
        List<String> list = new ArrayList<String>();
        for(int i=0; i<li.size(); i++){
            String str = li.get(i);  //获取传入集合对象的每一个元素
            if(!list.contains(str)){   //查看新集合中是否有指定的元素，如果没有则加入
                list.add(str);
            }
        }
        return list;  //返回集合
    }
    
}
