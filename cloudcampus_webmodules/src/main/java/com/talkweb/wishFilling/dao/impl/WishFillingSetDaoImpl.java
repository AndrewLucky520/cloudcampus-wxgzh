package com.talkweb.wishFilling.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.wishFilling.dao.WishFillingSetDao;

/** 
 * 志愿填报-对外DIMPL
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @time 2016年11月23日  author：zhh
 */
@Repository
public class WishFillingSetDaoImpl  extends MyBatisBaseDaoImpl implements WishFillingSetDao {

	@Override
	public List<JSONObject> getSubjectListByTb(JSONObject param) {
		return selectList("getSubjectListByTb",param);
	}

	@Override
	public int hasTbByUseGrades(JSONObject param) {
		return selectOne("hasTbByUseGrades",param);
	}

	@Override
	public JSONObject getTWfWfinfo(JSONObject param){
		return selectOne("getTWfWfinfo",param);
	}
	@Override
	public void createTb(JSONObject param) {
		update("createTb",param);
	}

	@Override
	public List<JSONObject> getTbList(JSONObject param) {
		return selectList("getTbList",param);
	}

	@Override
	public void updateTbName(JSONObject param) {
		update("updateTbName",param);
	}

	@Override
	public JSONObject getTb(JSONObject param) {
		String sId = param.getString("schoolId");
		String areaCode = param.getString("areaCode");
		if(StringUtils.isBlank(sId)|| StringUtils.isBlank(areaCode)){
			return null;
		}
		JSONObject wf = selectOne("getTb",param);
		logger.info("wf:getTbDao"+wf.toJSONString()+" param:"+param.toJSONString());
		if(wf==null ||!wf.containsKey("wfId"))
		{
			return null;
		}
		List<JSONObject> dicSubList = this.getDicSubjectList(sId, areaCode,wf.getString("pycc"),"0");
		Map<String,String> idNameMap = new HashMap<String,String>();
		if(dicSubList!=null){
			for(JSONObject dicSub:dicSubList){
				String subjectId = dicSub.getString("subjectId");
				String subjectName = dicSub.getString("subjectName");
				idNameMap.put(subjectId, subjectName);
			}
		}
		String subjectIds = wf.getString("subjectIds");
		List<String> sIdList =Arrays.asList(subjectIds.split(","));
		String subjectNames = "";
		for(String id:sIdList){
			if(idNameMap.get(id)!=null && StringUtils.isNotBlank(idNameMap.get(id))){
				subjectNames+=idNameMap.get(id)+",";
			}else{
				subjectNames+="[已删除],";
			}
		}
		if(subjectNames.length()>0){
			subjectNames=subjectNames.substring(0, subjectNames.length()-1);
		}
		wf.put("subjectNames", subjectNames);
		return wf;
	}

	@Override
	public void deleteTb(JSONObject param) {
		update("deleteTb",param);
	}

	@Override
	public void updateTb(JSONObject param) {
		update("updateTb",param);
	}

	@Override
	public void deleteSubjectTb(JSONObject param) {
		update("deleteSubjectTb",param);	
	}

	@Override
	public void createSubjectTbBatch(List<JSONObject> param) {
		update("createSubjectTbBatch",param);
	}

	@Override
	public void deleteStudentTb(JSONObject param) {
		update("deleteStudentTb",param);
	}

	@Override
	public void deleteZhSubject(JSONObject param) {
		update("deleteZhSubject",param);
	}

	@Override
	public void deleteZhStudent(JSONObject param) {
		update("deleteZhStudent",param);
	}

	@Override
	public void createZhSubjectBatch(List<JSONObject> param) {
		update("createZhSubjectBatch",param);
	}

	@Override
	public int getTotalStudentCount(JSONObject param) {
		return selectOne("getTotalStudentCount",param);
	}

	@Override
	public int getStudentSubjectCount(JSONObject param) {
		return selectOne("getStudentSubjectCount",param);
	}

	@Override
	public int insertStudentTbBatch(List<JSONObject> param) {
		// TODO Auto-generated method stub
		return update("insertStudentTbBatch",param);
	}

	@Override
	public int insertStudentZhBatch(List<JSONObject> param) {
		// TODO Auto-generated method stub
		return update("insertStudentZhBatch",param);
	}

	@Override
	public List<JSONObject> getZhSubject(JSONObject param) {
		return selectList("getZhSubject",param);
	}

	@Override
	public List<JSONObject> getStudentTb(JSONObject param) {
		// TODO Auto-generated method stub
		return selectList("getStudentTb",param);
	}
	
	@Override
	public List<JSONObject> getStudentTbBy(JSONObject param) {
		// TODO Auto-generated method stub
		return selectList("getStudentTb",param);
	}

	@Override
	public JSONObject getZhStudent(JSONObject param) {
		return selectOne("getZhStudent",param);
	}

	@Override
	public List<JSONObject> getStudentZhAndSubject(JSONObject param) {
		return selectList("getStudentZhAndSubject", param);
	}

	@Override
	public List<Long> getHasSelectedStudentIds(JSONObject param) {
		return selectList("getHasSelectedStudentIds",param);
	}

	@Override
	public List<JSONObject> getStaticListBySubject(JSONObject param) {
		return selectList("getStaticListBySubject",param);
	}

	@Override
	public List<JSONObject> getStaticListByZh(JSONObject param) {
		return selectList("getStaticListByZh",param);
	}

	@Override
	public int getZhTotalStudentNum(JSONObject param) {
		return selectOne("getZhTotalStudentNum",param);
	}

	@Override
	public List<JSONObject> getStudentZh(JSONObject param) {
		return selectList("getStudentZh",param);
	}

	@Override
	public List<JSONObject> getAllStudentZh(JSONObject param) {
		// TODO Auto-generated method stub
		return selectList("getAllStudentZh",param);
	}

	@Override
	public List<Long> getStudentIds(JSONObject param) {
		return selectList("getStudentIds",param);
	}

	@Override
	public List<JSONObject> getZhListByTb(JSONObject param) {
		return selectList("getZhListByTb",param);
	}

	@Override
	public List<JSONObject> getTbSelectList(JSONObject param) {
		return selectList("getTbSelectList",param);
	}

	@Override
	public List<JSONObject> getDicSubjectList(String sId,String areaCode,String pycc,String isDivided) {
		if(StringUtils.isBlank(sId)||StringUtils.isBlank(areaCode)){
			return null;
		}
		String countyCode = areaCode+"";
		String cityCode = countyCode.substring(0, 4)+"00";
		String provinceCode = countyCode.substring(0, 2)+"0000" ;
		
		JSONObject json= new JSONObject();
		json.put("pycc", pycc);
		json.put("isDivided", isDivided);
		List<JSONObject> dicSubList = selectList("getDicSubjectList",json);
		List<JSONObject> returnDefaultList = new ArrayList<JSONObject>();
		List<JSONObject> returnSchoolList = new ArrayList<JSONObject>();
		List<JSONObject> returnCountryList = new ArrayList<JSONObject>();
		List<JSONObject> returnCityList = new ArrayList<JSONObject>();
		List<JSONObject> returnProvinceList = new ArrayList<JSONObject>();
		if(dicSubList!=null){
			for(JSONObject dicSub:dicSubList){
				String scopeId = dicSub.getString("scopeId");
				String scopeType = dicSub.getString("scopeType");
				if("0".equals(scopeType)){ //默认6选3
					returnDefaultList.add(dicSub);
				}else if("1".equals(scopeType) && sId.equals(scopeId)){ 
					//学校
					returnSchoolList.add(dicSub);
				}else if("2".equals(scopeType)){ 
					//区域判断
					if(countyCode.equals(scopeId)){
						returnCountryList.add(dicSub);
					}else if(cityCode.equals(scopeId)){
						returnCityList.add(dicSub);
					}else if(provinceCode.equals(scopeId)){
						returnProvinceList.add(dicSub);
					}
				}
			}
		}
		
		if(returnSchoolList.size()>0){return returnSchoolList;}
		if(returnCountryList.size()>0){return returnCountryList;}
		if(returnCityList.size()>0){return returnCityList;}
		if(returnProvinceList.size()>0){return returnProvinceList;}
		if(returnDefaultList.size()>0){return returnDefaultList;}
		return null;
	}

	@Override
	public List<JSONObject> getByZhStudentNum(JSONObject param) {
		return selectList("getByZhStudentNum",param); 
	}

	@Override
	public int getByTotalStudentCount(JSONObject param) {
		return selectOne("getByTotalStudentCount",param);
	}

	@Override
	public List<JSONObject> getByZhStudentCount(JSONObject param) {
		return selectList("getByZhStudentCount",param);
	}

	@Override
	public void updateByTb(JSONObject param) {
		 update("updateByTb",param);
	}

	@Override
	public int updateZhSubject(JSONObject param) {
		return update("updateZhSubject",param);
	}

	@Override
	public void insertByZhStudentBatch(JSONObject param) {
		 update("insertByZhStudentBatch",param);
		
	}

	@Override
	public List<JSONObject> getByStudentByFixedZhIds(JSONObject param) {
		return selectList("getByStudentByFixedZhIds",param);
	}

	@Override
	public void insertByStudentTbBatchForSet(JSONObject param) {
		update("insertByStudentTbBatchForSet",param);
		
	}

	@Override
	public void deleteByStudentTb(JSONObject param) {
		update("deleteByStudentTb",param);
	}

	@Override
	public void deleteByZhStudent(JSONObject param) {
		update("deleteByZhStudent",param);
		
	}

	@Override
	public List<Long> getByStudentIds(JSONObject param) {
		return selectList("getByStudentIds",param);
	}

	@Override
	public List<JSONObject> getByStudentZhAndSubject(JSONObject param) {
		return selectList("getByStudentZhAndSubject",param);
	}

	@Override
	public List<Long> getByHasSelectedStudentIds(JSONObject param) {
		return selectList("getByHasSelectedStudentIds",param);
	}

	@Override
	public JSONObject getByZhStudent(JSONObject param) {
		return selectOne("getByZhStudent",param);
	}

	@Override
	public void deleteByStudentTbByAccountId(JSONObject param) {
		update("deleteByStudentTbByAccountId",param);
		
	}

	@Override
	public int insertByStudentTbBatch(List<JSONObject> param) {
		return update("insertByStudentTbBatch",param);
	}

	@Override
	public void insertZhStudent(JSONObject param) {
		update("insertZhStudent",param);
	}

	@Override
	public void insertByZhStudent(JSONObject param) {
		update("insertByZhStudent",param);
	}

	@Override
	public List<JSONObject> getByStaticListBySubject(JSONObject param) {
		return selectList("getByStaticListBySubject",param);
	}

	@Override
	public List<JSONObject> getByStaticListByZh(JSONObject param) {
		return selectList("getByStaticListByZh",param);
	}

	@Override
	public int getByZhTotalStudentNum(JSONObject param) {
		return selectOne("getByZhTotalStudentNum",param);
	}

	@Override
	public List<JSONObject> getByStudentZh(JSONObject param) {
		return selectList("getByStudentZh",param);
	}

	@Override
	public List<JSONObject> getByAllStudentZh(JSONObject param) {
		return selectList("getByAllStudentZh",param);
	}

	@Override
	public List<JSONObject> getTbNameList(JSONObject param) {
		return selectList("getTbNameList",param);
	}

	@Override
	public List<JSONObject> getByAllZhStudent(JSONObject param) {
		return selectList("getByAllZhStudent",param);
	}

	@Override
	public int insertByStudentZhBatch(List<JSONObject> param) {
		return update("insertByStudentZhBatch",param);		
	}

	@Override
	public List<String> getWfListByAccountId(JSONObject param) {
		return selectList("getWfListByAccountId",param);
	}

	@Override
	public List<String> getWfListByOpenTime(JSONObject param) {
		return selectList("getWfListByOpenTime",param);
	}

	@Override
	public void updateZhSubjectFixedHasNum(JSONObject param) {
		update("updateZhSubjectFixedHasNum",param);
		
	}

	@Override
	public List<JSONObject> getZhStudentCount(JSONObject param) {
		return selectList("getZhStudentCount",param);
	}

	@Override
	public void updateByZhStudentBatch(JSONObject param) {
		update("updateByZhStudentBatch",param);
	}

	@Override
	public void updateByStudentTbBatchForSet(JSONObject param) {
		update("updateByStudentTbBatchForSet",param);
	}

	@Override
	public void updateSubjectTbByBatch(JSONObject param) {
		update("updateSubjectTbByBatch",param);
	}

	@Override
	public void updateSubjectTbNoByBatch(JSONObject param) {
		update("updateSubjectTbNoByBatch",param);
	}

	@Override
	public List<JSONObject> getAllStudentZhNoGroupBy(JSONObject param) {
		return selectList("getAllStudentZhNoGroupBy",param);
	}

	@Override
	public List<JSONObject> getByAllStudentZhNoGroupBy(JSONObject param) {
		return selectList("getByAllStudentZhNoGroupBy",param);
	}

	@Override
	public List<JSONObject> getLastOpenTb(JSONObject param) {
		return selectList("getLastOpenTb",param);
	}

	@Override
	public void insertFile(JSONObject param) {
		update("insertFileWF",param);
	}

	@Override
	public void deleteFile(JSONObject param) {
		update("deleteFileWF",param);
	}

	@Override
	public JSONObject getFile(JSONObject param) {
		return selectOne("getFileWF",param);
	}

	@Override
	public JSONObject getFileById(JSONObject param) {
		return selectOne("getFileByIdWF",param);
	}

	@Override
	public void insertByStudentTbBatchForSetNoPerson(JSONObject param) {
		update("insertByStudentTbBatchForSetNoPerson",param);
	}

	@Override
	public List<JSONObject> getDividedSubjectList(JSONObject param) {
		return selectList("getDividedSubjectList",param);
	}
}
