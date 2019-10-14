package com.talkweb.commondata.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;

/**
 * 导入姓名重复判断
x
 * @version 1.0 2017-08-15
 *
 */
public  class NameRepeatJudge {
	 static ResourceBundle rb = ResourceBundle.getBundle("constant.constant" );
	/* @Autowired
	 private static AllCommonDataService allCommonService; */
	 /**姓名重复判断方法（不包括数据库中不存在，格式错误等问题）
	 * 
	 *  @param  入参
	 * HashMap<Integer, JSONObject> pureDatas（必填）： excel的行号及数据
	 * List<JSONObject> titleNames(必填):pureDatas的JSON对应的需要校验的名称title（可能一行多个姓名都需要校验）
	 *                 [{
	 *                      classTitleName 如果是学生 则需传该学生的班级title
	 *                      titleName：需要校验的名称title
	 *                      splitIndex ：一个titleName可能多个姓名,为分隔符内容，仅一个则传空
	 *				   }]	 
	 * List<Account> list（必填）: 全校学生或老师的AccountList
	 * Map<String,String> classIdNameMap(学生必填)：全校的班级信息 classId-className
	 * excelNameMap(ruleType为0或1时必填)：防止excel重复map 
	 * nameRepeatMap:姓名重复匹配Map
	 * roleType（必填）:Teacher或者Student
	 * plate （必填）：1为铜仁项目
	 * excelRule（必填）：需要判断唯一标识姓名的列名 
	 * excelType：0仅excel中不允许重复，1仅excel和db都不能重复(默认)，2仅db不能重复
	 * @return 出参JSONObject
	 *  {
	 * 		nameRepeatMap：<String,String> 行号_titleName:accountId(多个按顺序逗号分隔)   对应的map
	 * 		wrongMsg[{
	 *           mrows [{err,oldValue,titleEnName,title}]
	 *           rows :行号
	 *         }] 错误信息  为null则出错
	 *       excelNameMap:name_accountId:name_accountId 防止excel姓名重复map
	 *       stringMap:电话号码/身份证:姓名对应关系map
	 *  }
	 *  @author zhanghuihui
	 * @throws Exception 
	 */
	public static JSONObject judgeNameRepeatImport (JSONObject param) throws Exception{
		JSONObject returnObj = new JSONObject();
		JSONArray wrongMsg = new JSONArray();
		
		String excelRule = param.getString("excelRule");
		String excelType = param.getString("excelType");
		String roleType = param.getString("roleType");
		HashMap<Integer, JSONObject> pureDatas = (HashMap<Integer, JSONObject>) param.get("pureDatas");
		List<JSONObject> titleNames = (List<JSONObject>) param.get("titleNames");
		String plate = param.getString("plate");
		String ruleString = rb.getString(plate+"."+roleType);
		List<String> rules = Arrays.asList(ruleString.split("_"));
		List<Account> list = (List<Account>) param.get("list");
		Map<String,String> classIdNameMap = (Map<String,String>) param.get("classIdNameMap");
		//名字-JSONObjList
		Map<String,List<JSONObject>> JSONMap = new HashMap<String,List<JSONObject>>();
		Map<String,String> stringMap = new HashMap<String,String>();
		Map<String,String> excelNameMap = (Map<String, String>) param.get("excelNameMap");
		if(excelNameMap==null){
			excelNameMap = new HashMap<String,String>();//name_accountId-rowNums     防止excel名称重复
		}
		if(StringUtils.isBlank(excelRule)||list==null||pureDatas==null || titleNames==null||StringUtils.isBlank(plate)||StringUtils.isBlank(roleType)){
			return null;
		}
		if("Student".equals(roleType)&& classIdNameMap==null){
			return null;
		}
		List<String> names = new ArrayList<String>();
		if(list!=null){
			for(Account a:list){
				Long classId = 0L ;
				List<User> uList = a.getUsers();
				for(User u:uList){
					if(u.getUserPart().getRole()==T_Role.Student){
						classId = u.getStudentPart().getClassId();
						break;
					}
				}
				String name = a.getName();
				if(!names.contains(name)){
					names.add(name);
				}
				JSONObject obj = convertToJSON(a);
				for(String rule:rules){
					String ruleValue = obj.getString(rule);
					stringMap.put(ruleValue, name);
				}
				String key = name ;
				if("Student".equals(roleType)){
					key = name+"_"+classIdNameMap.get(classId+"");
				}
				if(JSONMap.containsKey(key)){
					List<JSONObject> jsonList = JSONMap.get(key);
					jsonList.add(obj);
					JSONMap.put(key, jsonList);
				}else{
					List<JSONObject> jsonList = new ArrayList<JSONObject>();
					jsonList.add(obj);
					JSONMap.put(key, jsonList);
				}
			}
		}
		if(StringUtils.isBlank(excelType)){
			excelType="1";
		}
		Map<String,String> nameRepeatMap = (Map<String, String>) param.get("nameRepeatMap");
		if(nameRepeatMap==null){
			nameRepeatMap = new HashMap<String,String>(); //行号_titleName:accountId(多个按顺序逗号分隔)
		}
		//开始---------------------
		for (Iterator<Integer> it = pureDatas.keySet().iterator(); it.hasNext();) 
		{ //行循环
			int rowNum = it.next();
			JSONObject wmsg = new JSONObject();
			wmsg.put("row", rowNum);
			JSONArray mrows = new JSONArray();
			wmsg.put("mrows", mrows);
			JSONObject pd = pureDatas.get(rowNum);
			for(JSONObject titleNameObj: titleNames){ //列循环
				//分隔
				String titleName = titleNameObj.getString("titleName");
				String classTitleName = titleNameObj.getString("classTitleName");
				/*if(excelRule.equals(titleName)){
					continue;
				}*/
				String classTitleNameValue = pd.getString(classTitleName);
				String titleChinaName = pd.getString(titleName + "Name");
				String  excelRuleValues = pd.getString(excelRule);
				String  excelChinaName = pd.getString(excelRule+"Name");
				String titleNameValues = pd.getString(titleName);
				String splitIndex = titleNameObj.getString("splitIndex");
				List<String> titleNameValueSplits = new ArrayList<String>();
				List<String> excelRuleValueSplits  = new ArrayList<String>();
				if(StringUtils.isNotBlank(splitIndex)){
					excelRuleValueSplits.addAll(Arrays.asList(excelRuleValues.split(splitIndex)));
					titleNameValueSplits.addAll(Arrays.asList(titleNameValues.split(splitIndex)));
				}else{
					excelRuleValueSplits.add(excelRuleValues);
					titleNameValueSplits.add(titleNameValues);
					
				}
				//为空continue
				if(("Student".equals(roleType)&&StringUtils.isBlank(classTitleNameValue))||StringUtils.isBlank(titleNameValues)){
					continue;
				}
				//班级查不到 continue
				if("Student".equals(roleType) && !classIdNameMap.containsValue(classTitleNameValue)){
					continue;
				}
				//单元格
				int k=0; 
				boolean isCellRemove = false;
				Map<String,String> excelCellNameMap = new HashMap<String,String>(); //判断单元格是否有姓名重复的
				for(String titleNameValueSplit:titleNameValueSplits){
					String nameVal = titleNameValueSplit;//得到姓名
					nameVal = nameVal.replaceAll(" ", "");
					//老师查不到 continue 
					if(!names.contains(nameVal)){
						continue;
					}
					String key = nameVal;
					if("Student".equals(roleType)){
						 key = nameVal+"_"+classTitleNameValue;
					}
					Boolean isMatch = false; //是否与库中匹配
					Boolean isCellRepeat = false; //是否已匹配重复
					Boolean isExcelRepeat = false; //是否已匹配重复
					List<JSONObject>  JSONList = JSONMap.get(key);//得到库中的对象
					if(JSONList==null ){
						 nameVal = stringMap.get(nameVal);
						 if("Student".equals(roleType)){
							 key = nameVal+"_"+classTitleNameValue;
						 }
						 JSONList = JSONMap.get(key);
					}
					String excelRepeatKey = "";
					if(JSONList!=null&& JSONList.size()>1){ //不为空且数据库中的个数大于1才算重复
						for(JSONObject json:JSONList){//匹配了数据库中的数据
							for(String rule:rules){ //idNumber或者mobilePhone
								String ruleValue = json.getString(rule);
								String excelRuleValue =  excelRuleValueSplits.get(k);
								if(StringUtils.isNotBlank(ruleValue) && ruleValue.equals(excelRuleValue)){
									//判断单元格是否重复
									if(excelCellNameMap.containsKey(nameVal+"_"+json.getString("id"))){
										isCellRepeat=true;
										break;
									}
									//判断excel是否重复
									excelRepeatKey=nameVal+"_"+json.getString("id");
									if(!"2".equals(excelType)){
										if(excelNameMap.containsKey(nameVal+"_"+json.getString("id"))){
											isExcelRepeat=true;
											break;
										}
									}
									//得到唯一名字和库对应关系
									isMatch=true;
									excelCellNameMap.put(nameVal+"_"+json.getString("id"),nameVal+"_"+json.getString("id"));
									excelNameMap.put(nameVal+"_"+json.getString("id"),rowNum+"");
									if(nameRepeatMap.containsKey(rowNum+"_"+titleName)){
										String s = nameRepeatMap.get(rowNum+"_"+titleName);
										nameRepeatMap.put(rowNum+"_"+titleName, s+","+json.getString("id"));	
										break;
									}else{
										nameRepeatMap.put(rowNum+"_"+titleName, json.getString("id"));	
										break;
									}
								}
								
							}
							if(isMatch||isCellRepeat||isExcelRepeat){
								break;
							}
						}//end of 库对象数据遍历
					}else if (JSONList!=null&& JSONList.size()==1){ //end of db数据 size>2
						JSONObject json = JSONList.get(0);
						//判断单元格是否重复
						if(excelCellNameMap.containsKey(nameVal+"_"+json.getString("id"))){
							isCellRepeat=true;
						}else{
							excelCellNameMap.put(nameVal+"_"+json.getString("id"),nameVal+"_"+json.getString("id"));
						}
						//判断excel中重复
						if(!"2".equals(excelType)){
							excelRepeatKey=nameVal+"_"+json.getString("id");
							if(excelNameMap.containsKey(nameVal+"_"+json.getString("id"))){
								String rowN = excelNameMap.get(nameVal+"_"+json.getString("id"));
								if(!(rowNum+"").equals(rowN)){
									isExcelRepeat=true;
								}
							}else{
								excelNameMap.put(nameVal+"_"+json.getString("id"),rowNum+"");
							}
						}
					} //end of db数据size 1
					if(!isMatch && JSONList!=null &&JSONList.size()>1|| JSONList==null ||isExcelRepeat||isCellRepeat){ //单元格中的姓名在：excel在库中找不到 或者 excel匹配多条
						isCellRemove = true;
						String err="";
						if(isCellRepeat){//重复或不匹配请输入手机号/身份证号
							 err = "单元格中重复！";
						}else if(isExcelRepeat){
							err="excel中重复！";
						}else if (JSONList!=null && JSONList.size()>1){
							err="系统中有重名数据";
						}else{
							continue;
						}
						JSONObject wsg = new JSONObject();
						wsg.put("title", titleChinaName);
						wsg.put("oldValue", titleNameValues);
						wsg.put("err", err);
						mrows.add(wsg);
						boolean isFind = false; //是否找到
						if(isExcelRepeat){//如果excel重复 则将前面的重复项也拿出来处理
							String repeatRowNum = excelNameMap.get(excelRepeatKey);
							for(int j = 0;j<wrongMsg.size();j++){
								JSONObject wmsg1 = (JSONObject) wrongMsg.get(j);
								int rowN = wmsg1.getIntValue("row");
								JSONArray mrows1 = wmsg1.getJSONArray("mrows");
								if(mrows1==null){
									mrows1=new JSONArray();
								}
								if(rowNum>rowN){
									if(repeatRowNum.equals(rowN+"")){
										JSONObject wsg1 = new JSONObject();
										wsg1.put("title", titleChinaName);
										wsg1.put("oldValue", titleNameValues);
										wsg1.put("err", err);
										mrows1.add(wsg1);
										excelNameMap.put(excelRepeatKey,"");
										isFind=true;
									}
								}else{
									break;
								}
							}
							//第一次的情况 即wrongMsg为空
							if(!isFind && StringUtils.isNotBlank(excelNameMap.get(excelRepeatKey))){
								JSONObject wmsg1 = new JSONObject();
								JSONArray mrows1=new JSONArray();
								JSONObject wsg1 = new JSONObject();
								wsg1.put("title", titleChinaName);
								wsg1.put("oldValue", titleNameValues);
								wsg1.put("err", err);
								mrows1.add(wsg1);
								wmsg1.put("row", repeatRowNum);
								wmsg1.put("mrows", mrows1);
								wrongMsg.add(wmsg1);
								excelNameMap.put(excelRepeatKey,"");
							}
						}
						/*JSONObject wsg1 = new JSONObject();
						wsg1.put("title", excelChinaName);
						wsg1.put("oldValue", "");
						if(excelRuleValues!=null){
							wsg1.put("oldValue", excelRuleValues);
						}
						wsg1.put("err", "输入值不合法");
						mrows.add(wsg1);*/
					}
					k++;
					if(isCellRemove){
						nameRepeatMap.remove(rowNum+"_"+titleName);
					}
				} //end of 单元格
				
			} //end of 列
			if (mrows.size() > 0) 
			{
				wrongMsg.add(wmsg);
			} 
			else 
			{
				wmsg = null;
			}
		}//end of 行
		returnObj.put("wrongMsg", wrongMsg);
		returnObj.put("nameRepeatMap", nameRepeatMap);
		returnObj.put("stringMap", stringMap);
		if(!"2".equals(excelType)){
			returnObj.put("excelNameMap", excelNameMap);
		}
		return returnObj;
	}
	public static JSONObject convertToJSON(Account a) {
		String jsonString = JSON.toJSONString(a);  //pojo转json串
		JSONObject obj = JSON.parseObject(jsonString); //json串转JSONObject
		return obj;
	}

}
