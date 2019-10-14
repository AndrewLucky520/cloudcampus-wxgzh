package com.talkweb.salary.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.salary.dao.SalaryDao;
import com.talkweb.salary.domain.SalDetail;
import com.talkweb.salary.domain.SalExcel;
import com.talkweb.salary.service.SalaryService;

@Service
public class SalaryServiceImpl implements SalaryService {

	@Autowired
	private SalaryDao salaryDao;
	
	@Override
	public List<JSONObject> getSalaryList(JSONObject object) {
		List<JSONObject> result = salaryDao.getSalaryList(object);
		
		return result;
	}

	@Override
	public int deleteSalary(JSONObject object) {
		salaryDao.deleteSalaryDetail(object);
		salaryDao.deleteSalaryExcel(object);
		return salaryDao.deleteSalary(object);
	}

	@Override
	public int updateSalary(JSONObject object) {
		// TODO Auto-generated method stub
		return salaryDao.updateSalary(object);
	}

	@Override
	public int addSalary(JSONObject object) {
		// TODO Auto-generated method stub
		return salaryDao.addSalary(object);
	}

	@Override
	public JSONObject getSalaries(JSONObject object) {
		final int MAXWIDTH = 12;
		JSONObject data = new JSONObject();
		List<JSONObject> rows = new ArrayList<JSONObject>();
		List<List<JSONObject>> columns = new ArrayList<List<JSONObject>>();
		List<JSONObject> l_se = salaryDao.getAllSalaryExcel(object);
		List<JSONObject> l_sd = salaryDao.getAllSalaryDetail(object);
		
		//过滤重复元素
		List<String> teacherIds = new ArrayList<String>();
		if(!CollectionUtils.isEmpty(l_sd)){
			for(JSONObject sd:l_sd){
				if(!teacherIds.contains(sd.getString("TeacherId"))){
					teacherIds.add(sd.getString("TeacherId"));
				}
			}
		}
		Map<String,Boolean> widthMap = new HashMap<String,Boolean>(); //true 则长度为150，否则为100
		for(int i=0;i<teacherIds.size();i++){
			JSONObject rowInfo = new JSONObject();
			//第一列为教师姓名列
			rowInfo.put("r0c0", teacherIds.get(i));
			for(JSONObject sd:l_sd){
				if(teacherIds.get(i).equals(sd.getString("TeacherId"))){
					String  salaryNum = sd.getString("SalaryNum");
					String salComponentId = sd.getString("SalComponentId");
					String [] salComponents = salComponentId.split("c");
					
					rowInfo.put(salComponentId, salaryNum);
					int intValue = 0;
					if(StringUtils.isNotBlank(salaryNum)){
						intValue = salaryNum.length();
					}
					if(!widthMap.containsKey(salComponents[1])){
						widthMap.put(salComponents[1], false);
					}
					if(intValue>MAXWIDTH){
						widthMap.put(salComponents[1], true);
					}
				}
			}
			rows.add(rowInfo);
		}
		
		
		Integer headRowNum =0;
		if(l_se.size()>0){
			headRowNum = l_se.get(0).getInteger("headRowNum");
		}
		for(int i=0;i<headRowNum;i++){
			List<JSONObject> l_json = new ArrayList<JSONObject>();
			for(JSONObject se:l_se){
				int rowNum = se.getInteger("RowNum");
				int rowspan = se.getInteger("Rowspan");
				int ColNum = se.getIntValue("ColNum");
				boolean  isMaxWidth = false;
				if(rowNum == i){
					JSONObject headInfo = new JSONObject();
					if(headRowNum == rowNum + rowspan){
						 if( !"0".equals(ColNum+"") && widthMap.get(ColNum+"")!=null){
							 isMaxWidth = widthMap.get(ColNum+"");
						 }
						headInfo.put("field", se.getString("SalComponentId"));
						if(ColNum==0){
							headInfo.put("field","r0c0");
						}
					}else{
						headInfo.put("field", null);
					}
					headInfo.put("title", se.getString("SalComponentName"));
					headInfo.put("align", "center");
					if(isMaxWidth){
						headInfo.put("width", 150);
					}else{
						headInfo.put("width", 100);
					}
					headInfo.put("rowspan", rowspan);
					headInfo.put("colspan", se.getInteger("Colspan"));
					headInfo.put("sortable", false);
					l_json.add(headInfo);
				}
			}
			columns.add(l_json);
		}
	
		data.put("total", teacherIds.size());
		data.put("rows", rows);
		if(rows.size()>0){
			data.put("columns", columns);
		}
		return data;
	}

	@Override
	public List<JSONObject> getPersonalSalary(JSONObject object) {
		List<JSONObject> data = new ArrayList<JSONObject>();
		List<JSONObject> l_salaryIds = null;
		if(StringUtils.isEmpty(object.getString("year"))){
			l_salaryIds = salaryDao.getSalaryIdMax(object);
		}else{
			l_salaryIds = salaryDao.getSalaryId(object);
		}
		for(JSONObject jo:l_salaryIds){
		String salaryId = jo.getString("SalaryId");
		JSONObject result = new JSONObject();
		if(StringUtils.isEmpty(object.getString("year"))){
			result.put("year", jo.getString("Year"));
			result.put("month", jo.getString("Month"));
		}
		String salaryName = salaryDao.getSalaryNameById(salaryId);
		result.put("salaryName", salaryName);
		String teacherId = object.getString("teacherId");
		JSONObject param = new JSONObject();
		param.put("salaryId", salaryId);
		param.put("teacherId", teacherId);
		List<JSONObject> l_salDetail = salaryDao.getSalaryDetail(param);
		List<JSONObject> l_salExcel= salaryDao.getSalaryExcel(salaryId);
		int size = l_salDetail.size();
		List<List<JSONObject>> llj = new ArrayList<List<JSONObject>>();
		int index = 1;
		for(int s = 0;s<size;s++){
			List<JSONObject> onerow = new ArrayList<JSONObject>();
			for(int i = index;i<l_salExcel.size();i++){
				JSONObject cell = new JSONObject();
				JSONObject excel = l_salExcel.get(i);
				if(excel.getIntValue("colNum")==0)continue;
				cell.put("headRowNum", excel.getInteger("headRowNum"));
				int rcspan = excel.getInteger("rowNum")+excel.getInteger("rowspan");
				if(rcspan==excel.getInteger("headRowNum")){
					cell.put("rowspan", excel.getInteger("colspan"));
					cell.put("colspan", excel.getInteger("rowspan"));
					cell.put("value", excel.getString("salComponentName"));
					onerow.add(cell);
					//再加入工资栏
					JSONObject cell_last = new JSONObject();
					String en_id = excel.getString("salComponentId");
					String salaryNum = null;
					for(JSONObject sd:l_salDetail){
						if(sd.getString("salComponentId").equals(en_id)){
							salaryNum = sd.getString("salaryNum");
							break;
						}
					}
					cell_last.put("headRowNum", excel.getInteger("headRowNum"));
					cell_last.put("rowspan", 1);
					cell_last.put("colspan", 1);
					cell_last.put("value", salaryNum);
					onerow.add(cell_last);
					index = i+1;
					break;
				}else{
					cell.put("rowspan", excel.getInteger("colspan"));
					cell.put("colspan", excel.getInteger("rowspan"));
					cell.put("value", excel.getString("salComponentName"));
					onerow.add(cell);
				}
			}
			llj.add(onerow);
		}
		result.put("rows", llj);
		data.add(result);
		}
		return data;
	}

	@Override
	public int addImportSalary(List<SalDetail> successInfos,
			List<SalExcel> l_salExcel) {
		List<JSONObject> l_params = new ArrayList<JSONObject>();
		String salaryId = successInfos.get(0).getSalaryId();
		for(SalDetail sd:successInfos){
			for(int i =0;i<sd.getSalComponentIds().size();i++){
				JSONObject object = new JSONObject();
				object.put("salaryId", sd.getSalaryId());
				object.put("schoolId", sd.getSchoolId());
				object.put("teacherId", sd.getTeacherId());
				object.put("salComponentId", sd.getSalComponentIds().get(i));
				object.put("salaryNum", sd.getSalNums().get(i));
				l_params.add(object);
			}
		}
		//在重新导入之前，删除已经导入的信息
		JSONObject json = new JSONObject();
		json.put("salaryId", salaryId);
		salaryDao.deleteSalaryExcel(json);
		salaryDao.deleteSalaryDetail(json);
		
		salaryDao.insertSalExcel(l_salExcel);
		int result = salaryDao.insertSalDetail(l_params);
		salaryDao.updateSalaryImported(salaryId);
		return result;
	}

	@Override
	public int updateSalaryPublished(JSONObject param) {
		return salaryDao.updateSalaryPublished(param);
		
	}

	@Override
	public List<JSONObject> getAppPersonalSalary(JSONObject param) {
		// TODO Auto-generated method stub
		List<JSONObject> data = new ArrayList<JSONObject>();
		if(StringUtils.isEmpty(param.getString("year"))){
			JSONObject ymjson = salaryDao.getMaxYearMonth(param);
			if(ymjson!=null){
				param.put("year", ymjson.getString("Year"));
				param.put("month", ymjson.getString("Month"));
			}else{
				return data;
			}
		}
		 List<JSONObject> l_salaryIds = salaryDao.getSalaryId(param);
		for(JSONObject json:l_salaryIds){
			String salaryId = json.getString("SalaryId");
			JSONObject result = new JSONObject();
			result.put("year", json.getString("Year"));
			result.put("month", json.getString("Month"));
			result.put("salaryName", json.getString("SalaryName"));
			
			String teacherId = param.getString("teacherId");
			JSONObject param1 = new JSONObject();
			param1.put("salaryId", salaryId);
			param1.put("teacherId", teacherId);
			param1.put("schoolId", param.getString("schoolId"));
			List<JSONObject> l_salDetail = salaryDao.getSalaryDetail(param1);
			List<JSONObject> l_salExcel= salaryDao.getSalaryExcel(salaryId);
			int size = l_salDetail.size();
			int index = 1;
			List<JSONObject> onerow = new ArrayList<JSONObject>();
			for(int s = 0;s<size;s++){
				for(int i = index;i<l_salExcel.size();i++){
					JSONObject excel = l_salExcel.get(i);
					int rcspan = excel.getInteger("rowNum")+excel.getInteger("rowspan");
					if(rcspan==excel.getInteger("headRowNum")){
						JSONObject cell = new JSONObject();
						cell.put("text", excel.getString("salComponentName"));
						//再加入工资栏
						String en_id = excel.getString("salComponentId");
						String salaryNum = null;
						for(JSONObject sd:l_salDetail){
							if(sd.getString("salComponentId").equals(en_id)){
								salaryNum = sd.getString("salaryNum");
								break;
							}
						}
						//判断最后一行是否是实发，总（计），合（计）
						if(i==l_salExcel.size()-1){
							String str = excel.getString("salComponentName");
							if(str.indexOf("实发")>-1||str.indexOf("总")>-1||str.indexOf("合")>-1){
								result.put("total", salaryNum);
							}else{
								result.put("total", "");
							}
						}
						cell.put("value", salaryNum);
						onerow.add(cell);
						index = i+1;
						break;
					}
				}
			}
			result.put("rows", onerow);
			data.add(result);
		}
		return data;
	}

	@Override
	public JSONObject getCjSalaryAccount(JSONObject param) {
 
		return salaryDao.getCjSalaryAccount(param);
	}

	@Override
	public int updateCjSalaryAccount(JSONObject param) {
		JSONObject object = getCjSalaryAccount(param) ;
		if (object==null) {
			return salaryDao.insertCjSalaryAccount(param);
		}else {
			return salaryDao.updateCjSalaryAccount(param);
		}
		
	
	}

	@Override
	public JSONObject getCjSchool(JSONObject param) {
		 
		return salaryDao.getCjSchool(param);
	}

	@Override
	public List<JSONObject> getTeacherBySalaryId(JSONObject param) {
		 
		return salaryDao.getTeacherBySalaryId(param);
	}

}
