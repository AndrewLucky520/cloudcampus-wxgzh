package com.talkweb.wishFilling.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.placementtask.service.PlacementTaskService;
import com.talkweb.wishFilling.dao.WishFillingSetDao;
import com.talkweb.wishFilling.service.WishFillingImportService;

/** 
 * 志愿填报-导入serviceImpl
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月10日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0 2016年11月3日  author：zhh
 */
@Service
public class WishFillingImportServiceImpl implements WishFillingImportService {

	@Autowired
	private WishFillingSetDao wishFillingSetDao;
	@Autowired
	private PlacementTaskService placementTaskService;
	@Autowired
	private AllCommonDataService allCommonDataService;
	private static final Logger logger = LoggerFactory.getLogger(WishFillingImportServiceImpl.class);
	@Override
	public int addStudentTbBatch(Map<String, Object> paramMap) throws Exception {
		String wfWay1 = (String) paramMap.get("wfWay");
		String isByElection1 = (String) paramMap.get("isByElection");
		
		Map<String,List<String>> studentFbMap = (Map<String, List<String>>) paramMap.get("studentFbMap");
		List<JSONObject> insertStudentTb=(List<JSONObject>) paramMap.get("insertStudentTb");
		JSONObject delStudentTbAndZh=(JSONObject) paramMap.get("delStudentTb");
		List<JSONObject> insertStudentZh=(List<JSONObject>) paramMap.get("insertStudentZh");
		JSONObject param = new JSONObject();
		param.put("wfId", delStudentTbAndZh.getString("wfId"));
		param.put("termInfo", delStudentTbAndZh.getString("termInfo"));
		param.put("schoolId", delStudentTbAndZh.getString("schoolId"));
		School school = allCommonDataService.getSchoolById( delStudentTbAndZh.getLong("schoolId"), delStudentTbAndZh.getString("termInfo"));
		int areaCode = school.getAreaCode();
		param.put("areaCode", areaCode);
		JSONObject wf = wishFillingSetDao.getTb(param);
		String wfGradeId = wf.getString("wfGradeId");
		String schoolId = wf.getString("schoolId");
		String wfId = delStudentTbAndZh.getString("wfId");
		String isByElection = wf.getString("isByElection");
		String wfWay = wf.getString("wfWay");
		//在导入过程中有其他管理员修改设置属性
		if(!isByElection.equals(isByElection1)|| !wfWay.equals(wfWay1)){
			return -1;
		}
		//添加学生填报数据
		int count=0;
		if("1".equals(isByElection)&&"0".equals(wfWay)){ //补选
			//先删除学生填报数据(根据 account、schoolId和wfId删除)
			if(delStudentTbAndZh!=null){
				wishFillingSetDao.deleteByStudentTb(delStudentTbAndZh);
			}
			//删除以前的组合数据
		    if(delStudentTbAndZh!=null){
				wishFillingSetDao.deleteByZhStudent(delStudentTbAndZh);
			}
		    //插入学生填报
			if(insertStudentTb!=null && insertStudentTb.size()>0){
			     wishFillingSetDao.insertByStudentTbBatch(insertStudentTb);
			}
			//插入学生组合数据
			if(insertStudentZh!=null && insertStudentZh.size()>0){
				count = wishFillingSetDao.insertByStudentZhBatch(insertStudentZh);
			}
		}else{
			//先删除学生填报数据(根据 account、schoolId和wfId删除)
			if(delStudentTbAndZh!=null){
				wishFillingSetDao.deleteStudentTb(delStudentTbAndZh);
			}
			//删除以前的组合数据
		    if(delStudentTbAndZh!=null){
				wishFillingSetDao.deleteZhStudent(delStudentTbAndZh);
			}
			//插入学生填报
			if(insertStudentTb!=null && insertStudentTb.size()>0){
			     wishFillingSetDao.insertStudentTbBatch(insertStudentTb);
			}
			//插入学生组合数据
			if(insertStudentZh!=null && insertStudentZh.size()>0){
				count = wishFillingSetDao.insertStudentZhBatch(insertStudentZh);
			}
	    }
	   //操作新增修改分班志愿
	   List<JSONObject> studentWishs = new ArrayList<JSONObject>();
	   for (Map.Entry<String, List<String>> m : studentFbMap.entrySet()) {
	         System.out.println("key:" + m.getKey() + " value:" + m.getValue());
	         JSONObject wish = new JSONObject();
			   wish.put("accountId",  m.getKey());
			   List<String> lessonIds = m.getValue();
			   if(lessonIds!=null){
				   String lids = "";
				   for(String lId:lessonIds){
					   lids +=lId+",";
				   }
				   lids = lids.substring(0, lids.length()-1);
				   wish.put("wishId", lids);
			   }
			   studentWishs.add(wish);
	    }
	    logger.info("wishFillingPlacementtask:import  schoolId："+schoolId+"  wfGradeId:"+wfGradeId+" wfId: "+wfId+"  studentWishs:"+studentWishs.toString() );
	    placementTaskService.updateWish(schoolId,wfGradeId, wfId,studentWishs);
		return count;
	}
	
	
}
