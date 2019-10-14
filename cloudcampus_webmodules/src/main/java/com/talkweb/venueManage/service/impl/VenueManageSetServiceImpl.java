package com.talkweb.venueManage.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.api.message.utils.MessageNoticeModelEnum;
import com.talkweb.api.message.utils.MessageNoticeTypeEnum;
import com.talkweb.api.message.utils.MessageServiceEnum;
import com.talkweb.api.message.utils.MessageSmsEnum;
import com.talkweb.api.message.utils.MessageStatusEnum;
import com.talkweb.auth.service.AuthService;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.business.EasyUIDatagridHead;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.MotanService;
import com.talkweb.venueManage.dao.VenueManageSetDao;
import com.talkweb.venueManage.service.VenueManageAppService;
import com.talkweb.venueManage.service.VenueManageSetService;
import com.talkweb.venueManage.util.Util;

/** 
 * 场馆使用-设置SIMPL
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2015年11月24日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Service
public class VenueManageSetServiceImpl implements VenueManageSetService{
	@Autowired
	private VenueManageSetDao venueManageSetDao;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private VenueManageAppService venueManageAppService;
	
	@Autowired
    private AuthService authServiceImpl;
	private static final Logger logger = LoggerFactory.getLogger(VenueManageSetServiceImpl.class);
	 /**
		 * redis
		 */
	@Resource(name="redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;
	
	@Override
	public List<JSONObject> getVenueSetListPlus(JSONObject param) throws Exception {
		List<JSONObject> setTypeList = venueManageSetDao.getVenueSetAndTypeAndManagerList(param);
		return setTypeList;
	}
	
	/**
	 * 获取场馆列表
	 * @author zhh
	 */
	@Override
	public List<JSONObject> getVenueSetList(JSONObject param) throws Exception {
		final String NEEDEXAM="1";
		final int GREY=1;  //变灰
		final int NOGREY=2; //不变灰
		final String NOAGREE="2"; //不同意
		List<JSONObject> setTypeList = venueManageSetDao.getVenueSetAndTypeAndManagerList(param);
	    //占用时间查询  
		List<JSONObject> applyAndOccupys = venueManageSetDao.getApplyAndOccupyList(param);
	    //setId --  占用时间list
		Map<String,List<JSONObject>> occupyMap = new HashMap<String, List<JSONObject>>();
	    for(JSONObject occupy:applyAndOccupys){
	    	List<JSONObject> list=occupyMap.get(occupy.getString("setId"));
	    	if(list==null){
	    		List<JSONObject> list1 = new ArrayList<JSONObject>();
	    		list1.add(occupy);
	    		occupyMap.put(occupy.getString("setId"), list1);
	    	}else{
	    		list.add(occupy);
	    		occupyMap.put(occupy.getString("setId"), list);
	    	}
	    }
	  //查询所有的审批记录
	  	List<JSONObject> examApplyList = venueManageSetDao.getExamApplyList(param);
	  	Map<String,JSONObject> examApplyMap = new HashMap<String, JSONObject>(); 
	  	for(JSONObject examApply :examApplyList){
	  		if(examApplyMap.get(examApply.getString("applyId"))!=null){    //根据最近一次的审批情况考虑
	  			continue;
	  		}
	  		examApplyMap.put(examApply.getString("applyId"), examApply);
	  	}
	    //返回data
	   boolean getAllSet=false;  //true 才返回给前台占用情况 是否变灰
	   String useStartTime = param.getString("useStartTime");
	   String useEndTime = param.getString("useEndTime");
	   Date startDate=null;
	  if(StringUtils.isNotBlank(useStartTime)){
	   startDate = DateUtil.parseDateSecondFormat(useStartTime);
	   getAllSet=true;
	  }
	  Date endDate=null;
	  if(StringUtils.isNotBlank(useEndTime)){
	   endDate = DateUtil.parseDateSecondFormat(useEndTime);
	   getAllSet=true;
	  }
		for(int i=0; i<setTypeList.size();i++ ){
			JSONObject setType = setTypeList.get(i);
			String isNeedExam = setType.getString("isNeedExam");
			String comment = setType.getString("comment");
			String venueAddr = setType.getString("venueAddr");
			if(StringUtils.isBlank(venueAddr)||venueAddr==null){
				setType.put("venueAddr", "");
			} 
			if(StringUtils.isBlank(comment)){
				 setType.put("comment", "无");
			}
			if(StringUtils.isNotBlank(isNeedExam)){
				if(NEEDEXAM.equals(isNeedExam)){
					setType.put("isNeedExam","是");
				}else{
					setType.put("isNeedExam","否");
				}
			}

			String venueNum = setType.getString("venueNum");
			if(StringUtils.isBlank(venueNum)||venueNum==null){
				 setType.put("venueNum","");
			}else{
				setType.put("venueNum",venueNum+"人");
			}
			//查看该场馆的申请占用情况
			 if(getAllSet==true){
			 String setId = setType.getString("setId");
			 int isgrey=NOGREY;  //不变灰
			 List<JSONObject> occupys = occupyMap.get(setId);
			 if(occupys!=null){
				 for(JSONObject occupy:occupys){
					 String applyId = occupy.getString("applyId");
					 //判断该申请单的状态  若为“不同意状态”即使有申请记录也“不变灰”否则“变灰”
					 if(StringUtils.isNotBlank(isNeedExam)){
							if(NEEDEXAM.equals(isNeedExam)){ //需要审核
								//获取最近的审核记录
								JSONObject examApply=examApplyMap.get(applyId);
								if(examApply!=null ){
									String examState = examApply.getString("examState");
									if(NOAGREE.equals(examState)){ //不同意
										continue;  //不同意则取 NOGREY
									}
								}
							}
						}
					 String useStartApply = occupy.getString("useStartDate");
					 String useEndApply = occupy.getString("useEndDate");
					 Date useStartDateApply = DateUtil.parseDateSecondFormat(useStartApply);
					 Date useEndDateApply = DateUtil.parseDateSecondFormat(useEndApply);
					 if(useStartDateApply!=null && useEndDateApply!=null &&endDate!=null && startDate!=null){
					 boolean overlap =	 ((useStartDateApply.getTime() >= startDate.getTime())   
				                    && useStartDateApply.getTime() < endDate.getTime())  
				            ||  
				            ((useStartDateApply.getTime() > startDate.getTime())   
				                    && useStartDateApply.getTime() <= endDate.getTime())  
				            ||  
				            ((startDate.getTime() >= useStartDateApply.getTime())   
				                    && startDate.getTime() < useEndDateApply.getTime())  
				            ||  
				            ((startDate.getTime() > useStartDateApply.getTime())   
				                    && startDate.getTime() <= useEndDateApply.getTime());  
					 if(overlap ){
						 isgrey=GREY;
						 break;
					 }else{
						 isgrey=NOGREY;
					 }
		
					 }
				 }
			 }
			
			 setType.put("isgrey", isgrey);
			 }
		}
	
		return setTypeList ;
	}
	/**
	 * 得到场馆列表，供申请单编辑时使用
	 * @author zhh
	 */
	@Override
	public List<JSONObject> getVenueSetListForEdit(JSONObject param)
			throws Exception {
		final String NEEDEXAM="1";
		final String NONEEDEXAM="2";
		final int GREY=1;  //变灰
		final int NOGREY=2; //不变灰
		final String NOAGREE="2"; //不同意
		List<JSONObject> setTypeList = venueManageSetDao.getVenueSetAndTypeAndManagerList(param);
	    //占用时间查询  
		List<JSONObject> applyAndOccupys = venueManageSetDao.getApplyAndOccupyList(param);
	    //setId --  占用时间list
		Map<String,List<JSONObject>> occupyMap = new HashMap<String, List<JSONObject>>();
	    for(JSONObject occupy:applyAndOccupys){
	    	List<JSONObject> list=occupyMap.get(occupy.getString("setId"));
	    	if(list==null){
	    		List<JSONObject> list1 = new ArrayList<JSONObject>();
	    		list1.add(occupy);
	    		occupyMap.put(occupy.getString("setId"), list1);
	    	}else{
	    		list.add(occupy);
	    		occupyMap.put(occupy.getString("setId"), list);
	    	}
	    }
	    //查询所有的审批记录
	  	List<JSONObject> examApplyList = venueManageSetDao.getExamApplyList(param);
	  	Map<String,JSONObject> examApplyMap = new HashMap<String, JSONObject>(); 
	  	for(JSONObject examApply :examApplyList){
	  		if(examApplyMap.get(examApply.getString("applyId"))!=null){    //根据最近一次的审批情况考虑
	  			continue;
	  		}
	  		examApplyMap.put(examApply.getString("applyId"), examApply);
	  	}
	    //返回data
	   boolean getAllSet=false;  //true 才返回给前台占用情况 是否变灰
	   String useStartTime = param.getString("useStartTime");
	   String useEndTime = param.getString("useEndTime");
	   Date startDate=null;
	  if(StringUtils.isNotBlank(useStartTime)){
	   startDate = DateUtil.parseDateSecondFormat(useStartTime);
	   getAllSet=true;
	  }
	  Date endDate=null;
	  if(StringUtils.isNotBlank(useEndTime)){
	   endDate = DateUtil.parseDateSecondFormat(useEndTime);
	   getAllSet=true;
	  }
		for(int i=0; i<setTypeList.size();i++ ){
			JSONObject setType = setTypeList.get(i);
			String isNeedExam = setType.getString("isNeedExam");
			String comment = setType.getString("comment");
			String venueAddr = setType.getString("venueAddr");
			if(StringUtils.isBlank(venueAddr)||venueAddr==null){
				setType.put("venueAddr", "");
			}
			if(StringUtils.isBlank(comment)){
				 setType.put("comment", "无");
			}
			if(StringUtils.isNotBlank(isNeedExam)){
				if(NEEDEXAM.equals(isNeedExam)){
					setType.put("isNeedExam","是");
				}else{
					setType.put("isNeedExam","否");
				}
			}
			String venueNum = setType.getString("venueNum");
			if(StringUtils.isBlank(venueNum)||venueNum==null){
				 setType.put("venueNum","");
			}else{
				setType.put("venueNum",venueNum+"人");
			}
			//查看该场馆的申请占用情况
			 if(getAllSet==true){
			 String setId = setType.getString("setId");
			 int isgrey=NOGREY;  
			 List<JSONObject> occupys = occupyMap.get(setId);
			 if(occupys!=null){
				 for(JSONObject occupy:occupys){
					 String applyId = occupy.getString("applyId");
					 //判断该申请单的状态  若为“不同意状态”即使有申请记录也“不变灰”否则“变灰”
					 if(StringUtils.isNotBlank(isNeedExam)){
							if(NEEDEXAM.equals(isNeedExam)){ //需要审核
								//获取最近的审核记录
								JSONObject examApply=examApplyMap.get(applyId);
								if(examApply!=null ){
									String examState = examApply.getString("examState");
									if(NOAGREE.equals(examState)){ //不同意
										continue;  //不同意则取 NOGREY
									}
								}
							}
					 }
							
					 String useStartApply = occupy.getString("useStartDate");
					 String useEndApply = occupy.getString("useEndDate");
					 Date useStartDateApply = DateUtil.parseDateSecondFormat(useStartApply);
					 Date useEndDateApply = DateUtil.parseDateSecondFormat(useEndApply);
					 if(useStartDateApply!=null && useEndDateApply!=null){
					 boolean overlap =	 ((useStartDateApply.getTime() >= startDate.getTime())   
				                    && useStartDateApply.getTime() < endDate.getTime())  
				            ||  
				            ((useStartDateApply.getTime() > startDate.getTime())   
				                    && useStartDateApply.getTime() <= endDate.getTime())  
				            ||  
				            ((startDate.getTime() >= useStartDateApply.getTime())   
				                    && startDate.getTime() < useEndDateApply.getTime())  
				            ||  
				            ((startDate.getTime() > useStartDateApply.getTime())   
				                    && startDate.getTime() <= useEndDateApply.getTime());  
					 if(overlap ){
						 isgrey=GREY;
						 break;
					 }else{
						 isgrey=NOGREY;
					 }
		
					 }
				 }
			 }
			
			 setType.put("isgrey", isgrey);
			 }
		}
	
		return setTypeList ;
	}

	/**
	 * 新增/编辑场馆
	 * @author zhh
	 */
	@Override
	public int addVenueSet(JSONObject param) throws Exception {
		int r=1;
		String schoolId = param.getString("schoolId");
		String setId=param.getString("setId");
		boolean isUpdate=true;
		if(StringUtils.isBlank(setId)){
			setId = UUIDUtil.getUUID();
			param.put("setId", setId);
			isUpdate=false;
		}

		//判断场馆是否有重名
		JSONObject venueSet1 = new JSONObject();
		venueSet1.put("schoolId", schoolId);
		venueSet1.put("venueName", param.get("venueName"));
		//是否已存在该类别
		JSONObject venueType1 = venueManageSetDao.getVenueType(param);
		if(venueType1!=null){
			//根据该类别和场馆名称判断是否重名
			venueSet1.put("venueTypeId",venueType1.getString("venueTypeId") );
			JSONObject venueManageSet = venueManageSetDao.getVenueSet(venueSet1);
			if (venueManageSet != null ) {
				if(!setId.equals(venueManageSet.getString("setId"))){ //场馆名称没有更改 则也可以提交通过
					r = -2;
					return r;
				}
				
			}
		}
		//获取该类别名称下的类别代码
		String venueTypeId="";
		JSONObject venueType = venueManageSetDao.getVenueType(param);
		if( venueType!=null ){ //若有，获取该类别代码
	        venueTypeId = venueType.getString("venueTypeId");
	        param.put("venueTypeId",venueTypeId);
	        //判断下以前的类别 是否需要删除
	        JSONObject venue=new JSONObject();
	        venue.put("schoolId", schoolId);
			venue.put("setId", setId);
			JSONObject venueSet = venueManageSetDao.getVenueSet(venue);
			if(venueSet!=null){
	            JSONObject v1 = new JSONObject();
				v1.put("schoolId", schoolId);
				v1.put("venueTypeId", venueSet.get("venueTypeId"));
				List<JSONObject> venueSets = venueManageSetDao.getVenueSetAndTypeAndManagerList(v1);
				if(CollectionUtils.isEmpty(venueSets) || venueSets.size()<=1 ){
					if(venueSets.size()==1){  //只有一个的时候
						JSONObject v=venueSets.get(0);
						if(!v.getString("venueType").equals(param.getString("venueType"))){ //场馆类别和传入的一致 不删除
							venueManageSetDao.deleteVenueType(v1); //删除场馆类别
						}
					}else{
						venueManageSetDao.deleteVenueType(v1); //删除场馆类别
					}
				}
			}
		}else{ //若无，新增这个名称的类别
			venueTypeId = UUIDUtil.getUUID();
			param.put("venueTypeId",venueTypeId );
			venueManageSetDao.addVenueType(param);
			//删除原场馆的类别（若该类别下只有一个场馆了 ）
			JSONObject venue = new JSONObject();
			venue.put("schoolId", schoolId);
			venue.put("setId", setId);
			JSONObject venueSet = venueManageSetDao.getVenueSet(venue);
			if(venueSet!=null){
			    JSONObject v1= new JSONObject();
				v1.put("schoolId", schoolId);
				v1.put("venueTypeId", venueSet.get("venueTypeId"));
				//以前的oldvenueTypeId
				List<JSONObject> venueSets = venueManageSetDao.getVenueSetAndTypeAndManagerList(v1);
				if(CollectionUtils.isEmpty(venueSets) || venueSets.size()<=1 ){
					venueManageSetDao.deleteVenueType(v1); //删除场馆类别
				}
			}
		}
		
		//删除该场馆下的所有管理人员
		venueManageSetDao.deleteManager(param);
		List<JSONObject> teachers = (List<JSONObject>) param.get("teachers");
		for(int i=0;i<teachers.size();i++){
			JSONObject teacher = teachers.get(i);
			teacher.put("schoolId", schoolId);
			teacher.put("setId", setId);
		}
		//新增管理人员
		if(teachers.size()>0){
			venueManageSetDao.addManagerBatch(teachers);
		}
		
		venueManageSetDao.deleteEquipmentManager(param);
		List<JSONObject> equipmentTeachers = (List<JSONObject>) param.get("equipmentTeachers");
		if (equipmentTeachers!=null && equipmentTeachers.size() > 0) {
			for (int i = 0; i < equipmentTeachers.size(); i++) {
				JSONObject teacher = equipmentTeachers.get(i);
				teacher.put("schoolId", schoolId);
				teacher.put("setId", setId);
			}
			venueManageSetDao.addEquipmentManagerBatch(equipmentTeachers);
		}
		
		
		//新增场馆
		param.put("isImport", 2);
		param.put("createDate", DateUtil.getDateFormatNow());
		venueManageSetDao.addVenueSet(param);
		return r;
	}
	/**
	 * 获取场馆详情
	 * @author zhh
	 */
	@Override
	public JSONObject getVenueSet(JSONObject param) throws Exception {
		//查询场馆设置
		JSONObject venueSet = venueManageSetDao.getVenueSet(param);
		
		//查询场馆类别
		param.put("venueTypeId", venueSet.get("venueTypeId"));
		JSONObject venueType = venueManageSetDao.getVenueType(param);
		venueSet.put("venueType", venueType.get("venueType"));
		venueSet.put("venueTypeId", venueType.get("venueTypeId"));
		
		//查询场馆管理人员
		List<JSONObject> teachers = venueManageSetDao.getVenueManagerList(param);
		List<JSONObject> equipmentTeachers =venueManageSetDao.getEquipmentManagerList(param);
		venueSet.put("equipmentTeachers", equipmentTeachers);
		venueSet.put("teachers", teachers);
		return venueSet;
	}
	/**
	 * 删除场馆
	 * @author zhh
	 */
	@Override
	public int deleteVenueSet(JSONObject param) throws Exception {
		int r=1;
		//删除该场馆下的管理人员
		venueManageSetDao.deleteManager(param);
		
		//删除原场馆的类别（若该类别下只有一个场馆了 ）
		JSONObject venue = new JSONObject();
		venue.put("schoolId", param.get("schoolId"));
		venue.put("setId", param.get("setId"));
		JSONObject venueSet = venueManageSetDao.getVenueSet(venue);
		if(venueSet!=null){
			venue.put("venueTypeId", venueSet.get("venueTypeId"));//以前的oldvenueTypeId
			venue.remove("setId");
			List<JSONObject> venueSets = venueManageSetDao.getVenueSetAndTypeAndManagerList(venue);
			if(CollectionUtils.isEmpty(venueSets) || venueSets.size()<=1 ){
				venueManageSetDao.deleteVenueType(venue); //删除场馆类别
			}
			venueManageSetDao.deleteEquipmentManager(param);
		}
		//删除场馆
		venueManageSetDao.deleteVenueSet(param);
		//删除场馆对应的申请单
		List<String> applyIds=null;
		List<JSONObject> applyList = venueManageSetDao.getApplyAndOccupyList(param); //得到该场馆下的所有申请单代码
		if(CollectionUtils.isNotEmpty(applyList)){
			applyIds=new ArrayList<String>();
			for(JSONObject apply:applyList){
				applyIds.add(apply.getString("applyId"));
			}
		}
	
		venueManageSetDao.deleteApply(param);
		//删除场馆申请单下的占用时间
		venueManageSetDao.deleteOccupy(param);
		if(CollectionUtils.isNotEmpty(applyList)){
			//删除场馆申请单下的 设备要求内容
			param.put("applyIds", applyIds);
			venueManageSetDao.deleteEquipmentRequireContent(param);
			//删除该申请单下的审核信息
			 venueManageSetDao.deleteExamApply(param);
			//删除该申请单下的检查信息
			 venueManageSetDao.deleteInspectionApply(param);
			//删除该申请单下的检查内容
			 venueManageSetDao.deleteInspectionRecordApply(param);
			 //删除场馆历史数据
			 venueManageSetDao.deleteApplyHistory(param);
		}
		return r;
	}
	/**
	 * 新增/编辑检查项目
	 * @author zhh
	 */
	@Override
	public int addInspectionItemSet(JSONObject param) throws Exception {
		int i=1;
		String schoolId = param.getString("schoolId");
	    boolean isUpdate=true;
		List<String> names= new ArrayList<String>();
		List<JSONObject> inspectionItems = (List<JSONObject>) param.get("inspectionItems");
		//拼接检查项目
		for( int j=0;j<inspectionItems.size(); j++ ){
			isUpdate=true;
			JSONObject inspectionItem = inspectionItems.get(j);
			String inspectionItemName = inspectionItem.getString("inspectionItemName");
			String inspectionItemId = inspectionItem.getString("inspectionItemId");
			if(StringUtils.isBlank(inspectionItemId)){
				inspectionItemId= UUIDUtil.getUUID();
				isUpdate=false;
			}
			inspectionItem.put("schoolId", schoolId);
			inspectionItem.put("inspectionItemId", inspectionItemId);
			inspectionItem.put("orderNum", j+1);
			if(!names.contains(inspectionItemName)){
				names.add(inspectionItemName);
			}else{  //重名返回提示
				i=-2;
				return i;
			}
			if(isUpdate){
				//更新历史数据的表头 
				venueManageSetDao.updateApplyHistory(inspectionItem);
			}
		}
	
		//拼接检查项目备注
		JSONObject itemComment = new JSONObject();
		itemComment.put("schoolId", schoolId);
		String commentId = param.getString("commentId");
		if(StringUtils.isBlank(commentId)){
			itemComment.put("commentId", UUIDUtil.getUUID());
		}else{
			itemComment.put("commentId", commentId);
		}
		itemComment.put("content", param.get("content"));
		//插入
		if(inspectionItems.size()>0){
			venueManageSetDao.addInspectionItemBatch(inspectionItems);
		}
		venueManageSetDao.addInspectionItemComment(itemComment);
		return i;
	}
	/**
	 * 删除检查项目
	 * @author zhh
	 */
	@Override
	public int deleteInspectionItemSet(JSONObject param) throws Exception {
		int i=1;
		venueManageSetDao.deleteInspectionItem(param);
		return i;
	}
	/**
	 * 获取检查项目
	 * @author zhh
	 */
	@Override
	public JSONObject getInspectionItemSet(JSONObject param) throws Exception {
		JSONObject returnObj = new JSONObject();
		List<JSONObject> inspectionItemList = venueManageSetDao.getInspectionItemList(param);
		JSONObject inspectionItemComment = venueManageSetDao.getInspectionItemComment(param);
		if(inspectionItemComment!=null){
			returnObj.put("commentId", inspectionItemComment.getString("commentId"));
			returnObj.put("content", inspectionItemComment.getString("content"));
		}
		returnObj.put("inspectionItems", inspectionItemList);
		return returnObj;
	}
	/**
	 * 新增/编辑申请单
	 * @author zhh
	 */
	@Override
	public int addVenueApply(JSONObject param) throws Exception {
		int r=1;
		final String NEEDEXAM="1";
		final int GREY=1;  //变灰
		final int NOGREY=2; //不变灰
		final String NOAGREE="2"; //不同意
//		synchronized(this)
//		{
		
			String schoolId = param.getString("schoolId");
			String teacherId = param.getString("teacherId");
			String applyId = param.getString("applyId");
			String setId = param.getString("setId");
			Long userId = param.getLong("userId");
			if(StringUtils.isBlank(applyId)){
				applyId=UUIDUtil.getUUID();
				param.put("applyId", applyId);
			}
			String lock = "venueManage."+schoolId+"."+setId+".lock"; //lock住同一个学校同一个场馆
			boolean isWait=false;
			for(int x=0;x<300;x++){
				Boolean isLocked=redisOperationDAO.setNX(lock, 1);
				if(!isLocked){
					Thread.sleep(10);
					isWait=true;
					continue;
				}
				  try {
					//争取到lock则
					isWait = false;
					redisOperationDAO.set(lock, 1, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
					//并发判断，是否该时间的场馆已被其他人申请占用
					//占用时间查询  
					String useStartTime = param.getString("useStartDate");
					String useEndTime = param.getString("useEndDate");
					Date startDate = null;
					if (StringUtils.isNotBlank(useStartTime)) {
						startDate = DateUtil.parseDateSecondFormat(useStartTime);
					}
					Date endDate = null;
					if (StringUtils.isNotBlank(useEndTime)) {
						endDate = DateUtil.parseDateSecondFormat(useEndTime);
					}
					JSONObject applyOccupyParam = new JSONObject();
					applyOccupyParam.put("schoolId", param.getString("schoolId"));
					applyOccupyParam.put("setId", param.getString("setId"));
					applyOccupyParam.put("applyIdForEdit", applyId);
					List<JSONObject> applyAndOccupys = venueManageSetDao.getApplyAndOccupyList(applyOccupyParam);
					 //查询所有的审批记录
					JSONObject json = new JSONObject();
					json.put("schoolId", schoolId);
				  	List<JSONObject> examApplyList = venueManageSetDao.getExamApplyList(json); //applyId,setId
				  	Map<String,JSONObject> examApplyMap = new HashMap<String, JSONObject>(); 
				  	for(JSONObject examApply :examApplyList){
				  		if(examApplyMap.get(examApply.getString("applyId"))!=null){    //根据最近一次的审批情况考虑
				  			continue;
				  		}
				  		examApplyMap.put(examApply.getString("applyId"), examApply);
				  	}
				  	//查询所有的场馆设置
				  	List<JSONObject> setTypeList = venueManageSetDao.getVenueSetAndTypeAndManagerList(json); //setId-isNeedExam
				  	Map<String,JSONObject> setTypeMap = new HashMap<String, JSONObject>(); 
				  	for(JSONObject setType :setTypeList){
				  		setTypeMap.put(setType.getString("setId"), setType);
				  	}
				  	//判断时间冲突
				  	for (JSONObject applyAndOccupy : applyAndOccupys) {
				  		String aId=applyAndOccupy.getString("applyId");
				  		String sId=applyAndOccupy.getString("setId");
				  		JSONObject setTypeObj = setTypeMap.get(sId);
				  		if(setTypeObj!=null ){
				  			String isNeedExam = setTypeObj.getString("isNeedExam");
				  			if(NEEDEXAM.equals(isNeedExam)){ //需要审核
				  			//获取最近的审核记录
								JSONObject examApply=examApplyMap.get(aId);
								if(examApply!=null ){
									String examState = examApply.getString("examState");
									if(NOAGREE.equals(examState)){ //不同意
										continue;  //不同意则跳过这条判断
									}
								}
				  			}
				  		}
						String useStartApply = applyAndOccupy.getString("useStartDate");
						String useEndApply = applyAndOccupy.getString("useEndDate");
						Date useStartDateApply = DateUtil.parseDateSecondFormat(useStartApply);
						Date useEndDateApply = DateUtil.parseDateSecondFormat(useEndApply);
						if (useStartDateApply != null && useEndDateApply != null) {
							boolean overlap = ((useStartDateApply.getTime() >= startDate.getTime())
									&& useStartDateApply.getTime() < endDate.getTime())
									|| ((useStartDateApply.getTime() > startDate.getTime())
											&& useStartDateApply.getTime() <= endDate.getTime())
									|| ((startDate.getTime() >= useStartDateApply.getTime())
											&& startDate.getTime() < useEndDateApply.getTime())
									|| ((startDate.getTime() > useStartDateApply.getTime())
											&& startDate.getTime() <= useEndDateApply.getTime());
							if (overlap) {
								return -2;
							}
						}
					}
					//删除该申请单下的占用时间设置
					venueManageSetDao.deleteOccupy(param);
					//添加该申请单下的占用时间
					String occupyId = UUIDUtil.getUUID();
					param.put("occupyId", occupyId);
					venueManageSetDao.addOccupy(param);
					List<JSONObject> inspectionItems = venueManageSetDao.getApplyHistoryList(param);
					if (CollectionUtils.isEmpty(inspectionItems)) {
						List<JSONObject> items = venueManageSetDao.getInspectionItemList(param);
						if (CollectionUtils.isEmpty(items)) {
							return -3;
						}
						for (int i = 0; i < items.size(); i++) {
							JSONObject item = items.get(i);
							item.put("schoolId", param.getString("schoolId"));
							item.put("createDate", DateUtil.getDateFormatNow());
							item.put("applyId", param.getString("applyId"));
							item.put("historyId", UUIDUtil.getUUID());
						}
						if (items != null && items.size() > 0) {
							venueManageSetDao.addApplyHistoryBatch(items);
						}
						inspectionItems = venueManageSetDao.getApplyHistoryList(param);
					}
					//删除该申请单下的设备要求内容
					venueManageSetDao.deleteEquipmentRequireContent(param);
					List<JSONObject> equipmentRequires = (List<JSONObject>) param.get("equipmentRequires");
					List<JSONObject> equipmentRequiresAdd = new ArrayList<JSONObject>();
					for (int i = 0; i < equipmentRequires.size(); i++) {
						JSONObject equipmentRequire = equipmentRequires.get(i);
						if (!equipmentRequire.getBoolean("isSelect")) {
							continue;
						}
						equipmentRequire.put("schoolId", schoolId);
						equipmentRequire.put("contentId", UUIDUtil.getUUID());
						equipmentRequire.put("applyId", applyId);
						equipmentRequiresAdd.add(equipmentRequire);
						
					}
					//添加该申请单下的设备要求内容
					if (equipmentRequiresAdd.size() > 0) {
						venueManageSetDao.addEquipmentRequireContentBatch(equipmentRequiresAdd);
						param.put("equipmentStatus", "1");
					}else{
						param.put("equipmentStatus", "0");
					}
					//添加申请单
					param.put("createDate", DateUtil.getDateFormatNow());
					param.put("checkState", 1);
					param.put("occupyId", occupyId);
					/*	   //判断该当前登录对象是否为申请场馆的审核人员 （若是则审核直接通过，否则走普通老师流程）
					List<JSONObject> venueSets = venueManageSetDao.getVenueSetAndTypeAndManagerList(param);
					if(CollectionUtils.isNotEmpty(venueSets) && venueSets.size()>0){
						JSONObject venueSet = venueSets.get(0);
						String teacherIds = venueSet.getString("teacherIds");
						if(StringUtils.isNotBlank(teacherIds) && teacherIds.contains(param.getString("teacherId"))){
							param.put("applyState",2); //已审核
							//插入相关参数到审核表中
							JSONObject exam= new JSONObject();
							exam.put("schoolId", schoolId);
							exam.put("examId", UUIDUtil.getUUID());
							exam.put("applyId", applyId);
							exam.put("examTeacherId", teacherId);
							exam.put("examState", 1);
							exam.put("createDate",DateUtil.getDateFormatNow());
							venueManageSetDao.addExamApply(exam);
						}else{
							param.put("applyState",1); //未审核
						}
					}*/
					param.put("applyState", 1); //未审核
					//新增申请单
					venueManageSetDao.addApply(param);
					break;
				} finally {
					redisOperationDAO.del(lock);
				}
		 }
		if(isWait){return -4;}
		
		if(r>0){
			
			JSONObject json = new JSONObject();
			json.put("schoolId", schoolId);
			json.put("setId", setId);
			List<JSONObject> list = venueManageSetDao.getVenueManagerList(json);
			List<String> ids = new ArrayList<String>();
			for(JSONObject obj:list){
				String tId = obj.getString("teacherId");
				ids.add(tId);
			}
			logger.info("=========================消息推送场馆使用==================");
//			String result = sendMsg(schoolId, ids, userId+"", applyId, MessageNoticeModelEnum.DEFAULT, "你有一条新的场馆使用需审核");
			//logger.info("=========================消息推送场馆使用========="+result+"=========");
		}
		return r;
	}
/**
 * 消息推送 
 * @author zhanghuihui
 * @param schoolId 学校代码 
 * @param ids accountIds 被发送者的accountId
 * @param userId 发送者的userId
 * @param serviceId  业务Id （applyId）
 * @param noticeModel DEFUALT
 * @param venueName  体育馆
 * @param userType 被发送者的类型
 * @return
 */
	public String sendMsg(String schoolId,List<String> ids,String userId,
			String serviceId,MessageNoticeModelEnum noticeModel,String msgContent){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("noticeServiceType", MessageServiceEnum.venueManage.toInteger());
		jsonObject.put("noticeDate", new Date());
		jsonObject.put("schoolId", schoolId);
		jsonObject.put("userId", userId);
		jsonObject.put("noticeType", MessageNoticeTypeEnum.TIMELY.toInteger());
		jsonObject.put("needConfirm", "0");
		//？
		//jsonObject.put("noticeUserType", MessageNoticeUserTypeEnum.PARTSTEACHER.toInteger());
		jsonObject.put("noticeModel", noticeModel.toInteger());
		jsonObject.put("noticeStatus", MessageStatusEnum.SUCCESS.toInteger());
		jsonObject.put("noticeSendDate", new Date());
		jsonObject.put("serviceId", serviceId);
		//noticeOperate
		jsonObject.put("sendMsgStatus", MessageSmsEnum.DEFAULT.toInteger());
		List<JSONObject> noticeDetails = new ArrayList<JSONObject>();	
		
		if(CollectionUtils.isNotEmpty(ids) && ids.size() > 0){
			for (String id : ids) {
				//String[] split = id.split(":");
				JSONObject noticeDetail = new JSONObject();
				noticeDetail.put("noticeDetailsContent", msgContent);
				//noticeDetail.put("userId", split[1]); 
				noticeDetail.put("classId", "");
				//noticeDetail.put("userType", userType); //与t_role一致
				noticeDetail.put("schoolId", schoolId);
				noticeDetail.put("accountId", id);
				noticeDetail.put("isStatus", "0");
				noticeDetail.put("family", "");
				noticeDetails.add(noticeDetail);
			}
		}
		jsonObject.put("noticeDetails", noticeDetails);
		String result="";
		try {
			logger.info("\n ========== 开始调用RPC noticeMessage()方法============");
			result = MotanService.noticeMessage(jsonObject);
			logger.info("\n ================ 返回结果："+ result+" ==============");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 删除场馆申请单
	 * @author zhh
	 */
	@Override
	public int deleteVenueApply(JSONObject param) throws Exception {
		int i=1;
		venueManageSetDao.deleteApply(param);
		venueManageSetDao.deleteEquipmentRequireContent(param);
		venueManageSetDao.deleteOccupy(param);
		// 删除该申请单下的审核信息
		venueManageSetDao.deleteExamApply(param);
		// 删除该申请单下的检查信息
		venueManageSetDao.deleteInspectionApply(param);
		// 删除该申请单下的检查内容
		venueManageSetDao.deleteInspectionRecordApply(param);
		//删除历史数据
		venueManageSetDao.deleteApplyHistory(param);
		return i;
	}
	/**
	 * 获取场馆申请单详情
	 * @author zhh
	 */
	@Override
	public JSONObject getVenueApplyDetail(JSONObject param) throws Exception {
		final int ISSELECTED=1;
		final int NOSELECTD=0;
		String phone = param.getString("phone");
		List<JSONObject> applys = venueManageSetDao.getApplyAndOccupyList(param);
		JSONObject apply=new JSONObject();
		if(CollectionUtils.isNotEmpty(applys)&& applys.size()>0  ){
			apply = applys.get(0);
		}else{
			return apply;
		}
		apply.put("schoolId", param.get("schoolId"));
		//查询场馆
		JSONObject venueSet = venueManageSetDao.getVenueSet(apply);
		if(venueSet!=null){
			String venueTypeId = venueSet.getString("venueTypeId");
			apply.put("venueTypeId", venueTypeId);
		}
		//获取设备要求内容
		Map<String,JSONObject> contentMap = new HashMap<String, JSONObject>();
		List<JSONObject> equipmentRequires = venueManageSetDao.getEquipmentRequireContentList(param);
		for(JSONObject content: equipmentRequires){
			contentMap.put(content.getString("requireId"), content);
		}
		List<JSONObject> requireList = venueManageSetDao.getEquipmentRequireList(param);
		for(int i=0;i<requireList.size();i++){
			JSONObject require=requireList.get(i);
			String requireId = require.getString("requireId");
			JSONObject content = contentMap.get(requireId);
			if(content!=null){
				require.put("num", content.getString("num"));
				require.put("content", content.getString("content"));
				require.put("isSelect", 1);
			}else{
				require.put("isSelect", 0);
				require.put("num", "");
				require.put("content", "");
			}
		}
		
		
		apply.put("equipmentRequires", requireList);
		return apply;
	}
	
	@Override
	public List<JSONObject> getVenueApplyListPlus(JSONObject param) throws Exception {
		//查询所有的审批记录
		List<JSONObject> applyOccupyList =	venueManageSetDao.getApplyAndOccupyListPlus(param);
		return applyOccupyList;
	}
	
	/**
	 * 获取场馆申请单列表
	 * @author zhh
	 */
	@Override
	public List<JSONObject> getVenueApplyList(JSONObject param)
			throws Exception {
		final String systemManager ="0"; //系统管理员
		final String agree ="1"; //同意
		final String noNeedExam ="2"; //无需审核
		String teacherId = param.getString("teacherId"); //当前登录用户
		String role = param.getString("role");
		String queryType = param.getString("queryType");
		String status = param.getString("status");
		//查询申请单（时间段）
		param.put("orderByUseStartDate", 1);
	    List<JSONObject> applyOccupyList =	venueManageSetDao.getApplyAndOccupyList(param); 
	    //查询类别和场馆名称下的场馆（场馆名称+类别）
		List<JSONObject> venueList = venueManageSetDao.getVenueSetAndTypeAndManagerList(param);
		Map<String, JSONObject> venueMap = new HashMap<String, JSONObject>(); //setId --JSONOBJ
		for(JSONObject venue:venueList){
			String setId = venue.getString("setId");
			venueMap.put(setId, venue);
		}
		//查询所有的审批记录

		//查询所有的检查记录
		List<JSONObject> inspectionApplyList = venueManageSetDao.getInspectionApplyList(param);
		Map<String,JSONObject> inspectionApplyMap = new HashMap<String, JSONObject>(); 
		for(JSONObject inspectionApply :inspectionApplyList){
			if(inspectionApplyMap.get(inspectionApply.getString("applyId"))!=null){    //根据最近一次的审批情况考虑
				continue;
			}
			if(inspectionApply.getString("applyId")!=null){
				inspectionApplyMap.put(inspectionApply.getString("applyId"), inspectionApply);
			}
		}
		
		List<JSONObject> preparedList = venueManageSetDao.getEquipmentrequirecontentPrepared(param);
		Map<String,JSONObject> preparedMap = new HashMap<String, JSONObject>(); 
		for(JSONObject prepared :preparedList){
			preparedMap.put(prepared.getString("applyId"), prepared);
		}
		
		//开始返回data
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		int count = 0;
		for(int i=0;i<applyOccupyList.size();i++ ){
			JSONObject applyOccupy = applyOccupyList.get(i);
			String  examState  = applyOccupy.getString("examState");
			String examTeacherId = applyOccupy.getString("examTeacherId");
			String setId = applyOccupy.getString("setId");
			String applyId = applyOccupy.getString("applyId");
			String useDate = Util.getFormatDate(applyOccupy.getString("useStartDate"),applyOccupy.getString("useEndDate"));
			applyOccupy.put("useDate", useDate);
			JSONObject venue = venueMap.get(setId);
			if(venue==null){
				continue;
			}
	        applyOccupy.put("teacherNames", venue.getString("teacherNames"));
			applyOccupy.put("venueName", venue.getString("venueName"));
			applyOccupy.put("venueAddr", venue.getString("venueAddr"));
			applyOccupy.put("isDelete", 2);//初始化 默认为2
			String teacherIds=venue.getString("teacherIds"); //该申请的对应场馆的审批人员
			String isNeedExam=venue.getString("isNeedExam"); //无需审核
			
			JSONObject inspectionApply = inspectionApplyMap.get(applyId); //最近检查记录
			JSONObject prepared = preparedMap.get(applyId); //最近检查记录
			applyOccupy.put("queryType", queryType);
			
			// 如果是我申请的，记录全都有
			if ("0".equals(queryType)) {
				if(noNeedExam.equals(isNeedExam)){//不需要检查
					applyOccupy.put("applyState", 6);// 只能 查看 不能编辑
				}else {//需要检查
					if(StringUtils.isNotBlank(examState)){ //审核了
						if(agree.equals(examState)){
							applyOccupy.put("applyState", 4); //“已同意”（不能改审核状态）
						}else {
							applyOccupy.put("applyState", 8); //“不同意不能改”
						}
					}else {//没有审核
						applyOccupy.put("applyState", 1);//需审核 &&  未审核 && 当前登录用户 == 申请者&&无检查数据   ---> “编辑”
					}
				}
			}else if ("1".equals(queryType)) {// 是系统管理员 设备管理员
				if(noNeedExam.equals(isNeedExam)){//不需要检查不出现在列表中
					continue;// 不出现在审核单中
				}else {//需要检查
					if("0".equals(role) || ("1".equals(role) && teacherIds.contains(teacherId))){
						applyOccupy.put("applyState", 3);//需审核 &&未审核  -----“审核”
					}else {
						continue;// 不出现在审核单中
					}
					
				}
			}else if ("2".equals(queryType)) {//谁审核谁见
				if(noNeedExam.equals(isNeedExam) ){//不需要检查
					if (systemManager.equals(role) || (teacherIds!=null && teacherIds.contains(teacherId))) {
						applyOccupy.put("applyState", 2);//无需审核 && 当前登录用户 != 申请者  &&未检查 ---> “无需审核”
					}else{
						continue;
					}
				}else {//需要检查
					 if (teacherId.equals(examTeacherId)) {
						 if (inspectionApply!=null || prepared!=null) {// 已经做了 检查 或者 设备做了准备
							 applyOccupy.put("applyState", 4); //“已同意”（不能改审核状态）
						 }else{
							 if(StringUtils.isNotBlank(examState)){ //审核了
									if(agree.equals(examState)){
										applyOccupy.put("applyState", 4); //“已同意”（不能改审核状态）
									}else {
										applyOccupy.put("applyState", 8); //“不同意不能改”
									}
								}
						 }

					}else{
						continue;
					}
				}
				//////////////////////////////
				 if (inspectionApply!=null) {
					 applyOccupy.put("checkState", 2);  //“已检查”
				 }else {
					 if (applyOccupy.getInteger("applyState")==8) {
						 applyOccupy.put("checkState", 3);  //  &&审批：不同意
					 }else {
						 applyOccupy.put("checkState", 1);  //“检查”
					 }
					
				 }
				
			}
			
			if (StringUtils.isNotBlank(status)) {
				int applyState = 0;
				applyState = applyOccupy.getIntValue("applyState");
				if ("0".equals(status)) {//待审核
					if (! ( applyState==3 || applyState == 1 )  ) {
						continue;
					} 
				}else if ("1".equals(status)) {//已同意
					if (! (applyState==4 || applyState==7)) {
						continue;
					} 
				}else if ("2".equals(status)) {//不同意
					if (applyState!=8 ) {
						continue;
					} 
				}else if ("3".equals(status)) {//无需审核
					if (!(applyState==6 || applyState==2)) {
						continue;
					} 
				}
				
				
			}
			
			if(inspectionApply!=null || prepared!=null || agree.equals(examState)){
				applyOccupy.put("isDelete", 2);
			}else{
				applyOccupy.put("isDelete", 1);
			}
			count++;
			applyOccupy.put("index", count);
			returnList.add(applyOccupy);
			
		}

		return returnList;
	}
	/**
	 * 新增申请单检查
	 * @author zhh
	 */
	@Override
	public int addVenueApplyInspection(JSONObject param) throws Exception {
		int r=1;
		String schoolId = param.getString("schoolId");
		String applyId = param.getString("applyId");
		String datetime = DateUtil.getDateFormatNow();
		//获取某学校的检查项目代码
		venueManageSetDao.deleteApplyHistory(param);
		List<JSONObject> itemList = venueManageSetDao.getInspectionItemList(param);
		for(int i=0;i<itemList.size();i++){
			JSONObject item=itemList.get(i);
			item.put("schoolId", schoolId);
			item.put("applyId", applyId);
			item.put("createDate", datetime);
			item.put("historyId", UUIDUtil.getUUID());
		}
		//将itemList插入历史数据表中
		if(itemList.size()>0){
			venueManageSetDao.addApplyHistoryBatch(itemList);
		}
	    List<JSONObject>   inspections = (List<JSONObject>) param.get("inspections");
	    for( int i=0;i<inspections.size();i++ ){
	    	JSONObject inspection = inspections.get(i);
	    	inspection.put("schoolId", schoolId);
	    	inspection.put("applyId", applyId);
	    	inspection.put("createDate", datetime);
	    	inspection.put("inspectionId",  UUIDUtil.getUUID());
	    }
	    //添加检查项目内容
	    if(inspections.size()>0){
	    	venueManageSetDao.deleteInspectionApply(param);
	    	venueManageSetDao.addInspectionApplyBatch(inspections);
	    }
		//添加检查申请
		param.put("createDate", DateUtil.getDateFormatNow());
		param.put("inspectionTeacherName", param.getString("teacherName"));
		param.put("inspectionTeacherId", param.getString("teacherId"));
		param.put("inspectionApplyId", datetime);
		venueManageSetDao.addInspectionApply(param);
		//更新申请表的检查状态
		venueManageSetDao.updateCheckState(param);
		return r;
	}
	/**
	 * 新增申请单审核
	 * @author zhh
	 */
	@Override
	public int addVenueApplyExam(JSONObject param) throws Exception {
		int r=1;
		final String NEEDEXAM="1";
		final int GREY=1;  //变灰
		final int NOGREY=2; //不变灰
		final String NOAGREE="2"; //不同意
		final String AGREE = "1";
		String schoolId = param.getString("schoolId");
		String userId=param.getString("userId");
		String applyId = param.getString("applyId");
		String examState = param.getString("examState");
		param.put("createDate", DateUtil.getDateFormatNow());
		param.put("examId", UUIDUtil.getUUID());
		param.put("schoolId", param.get("schoolId"));
		param.put("examTeacherId", param.get("teacherId"));
		param.put("examTeacherName", param.get("teacherName"));
		//检查该申请的时间段是否已有人申请成功 （改成同意的情况下）
		if(AGREE.equals(examState)){
			List<JSONObject> applyList = venueManageSetDao.getApplyAndOccupyList(param);
			JSONObject needAddApplyExamObj = null; //获取当前申请单信息
			Date startDate=null;
			Date endDate = null;
			String setId = "";
			if(applyList!=null && applyList.size()>0){
				needAddApplyExamObj = applyList.get(0);
			}
			if(needAddApplyExamObj!=null){
				startDate = needAddApplyExamObj.getDate("useStartDate");
				endDate = needAddApplyExamObj.getDate("useEndDate");
				setId = needAddApplyExamObj.getString("setId");
			}
			JSONObject json = new JSONObject();
			json.put("setId", setId);
			json.put("schoolId", schoolId);
			json.put("applyIdForEdit", applyId);
			List<JSONObject> applyListAll = venueManageSetDao.getApplyAndOccupyList(json);
			 //查询所有的审批记录
		  	List<JSONObject> examApplyList = venueManageSetDao.getExamApplyList(json); //applyId,setId
		  	Map<String,JSONObject> examApplyMap = new HashMap<String, JSONObject>(); 
		  	for(JSONObject examApply :examApplyList){
		  		if(examApplyMap.get(examApply.getString("applyId"))!=null){    //根据最近一次的审批情况考虑
		  			continue;
		  		}
		  		examApplyMap.put(examApply.getString("applyId"), examApply);
		  	}
		  	//查询所有的场馆设置
		  	List<JSONObject> setTypeList = venueManageSetDao.getVenueSetAndTypeAndManagerList(json); //setId-isNeedExam
		  	Map<String,JSONObject> setTypeMap = new HashMap<String, JSONObject>(); 
		  	for(JSONObject setType :setTypeList){
		  		setTypeMap.put(setType.getString("setId"), setType);
		  	}
		  //判断时间冲突
		  	for (JSONObject applyAndOccupy : applyListAll) {
		  		String aId=applyAndOccupy.getString("applyId");
		  		String sId=applyAndOccupy.getString("setId");
		  		JSONObject setTypeObj = setTypeMap.get(sId);
		  		if(setTypeObj!=null ){
		  			String isNeedExam = setTypeObj.getString("isNeedExam");
		  			if(NEEDEXAM.equals(isNeedExam)){ //需要审核
		  			//获取最近的审核记录
						JSONObject examApply=examApplyMap.get(aId);
						if(examApply!=null ){
							String examState1 = examApply.getString("examState");
							if(NOAGREE.equals(examState1)){ //不同意
								continue;  //不同意则跳过这条判断
							}
							if(AGREE.equals(examState1)){ //同意则判断时间冲突
								String useStartApply = applyAndOccupy.getString("useStartDate");
								String useEndApply = applyAndOccupy.getString("useEndDate");
								Date useStartDateApply = DateUtil.parseDateSecondFormat(useStartApply);
								Date useEndDateApply = DateUtil.parseDateSecondFormat(useEndApply);
								if (useStartDateApply != null && useEndDateApply != null) {
									boolean overlap = ((useStartDateApply.getTime() >= startDate.getTime())
											&& useStartDateApply.getTime() < endDate.getTime())
											|| ((useStartDateApply.getTime() > startDate.getTime())
													&& useStartDateApply.getTime() <= endDate.getTime())
											|| ((startDate.getTime() >= useStartDateApply.getTime())
													&& startDate.getTime() < useEndDateApply.getTime())
											|| ((startDate.getTime() > useStartDateApply.getTime())
													&& startDate.getTime() <= useEndDateApply.getTime());
									if (overlap) {
										return -2;
									}
								}
							}
						}
		  			}
		  		}
				
			}
		}
		
		venueManageSetDao.addExamApply(param);
		//更新申请表的审批状态
		venueManageSetDao.updateApplyState(param);

		//消息推送
		if(AGREE.equals(examState)){
			logger.info("=========================消息推送场馆使用==================");
			venueManageAppService.sendWxTemplateMsg(param);
		}

		return r;
	}
	/**
	 * 获取申请单检查详情
	 * @author zhh
	 */
	@Override
	public JSONObject getVenueApplyInspectionDetail(JSONObject param)
			throws Exception {
		//获取申请单和占用时间等相关信息
		List<JSONObject> applyAndOccupys = venueManageSetDao.getApplyAndOccupyList(param);
		JSONObject applyAndOccupy= new JSONObject();
		if(CollectionUtils.isNotEmpty(applyAndOccupys)&& applyAndOccupys.size()>0){
			applyAndOccupy = applyAndOccupys.get(0);
		}
		String useDate = Util.getFormatDate(applyAndOccupy.getString("useStartDate"),applyAndOccupy.getString("useEndDate"));
		applyAndOccupy.put("useDate", useDate);
		//根据申请单查看该单下的场馆
		applyAndOccupy.put("schoolId", param.getString("schoolId"));
		List<JSONObject> venues=venueManageSetDao.getVenueSetAndTypeAndManagerList(applyAndOccupy);
		JSONObject venue= new JSONObject();
		if(CollectionUtils.isNotEmpty(venues)&& venues.size()>0){
			venue = venues.get(0);
			applyAndOccupy.put("venueSets", venue);
		}
		venue.put("venueNum", venue.getString("venueNum")+"人");
		//取时间最近的检查对象
		List<JSONObject> inspectionItems=venueManageSetDao.getApplyHistoryList(param);
		
		  //检查项的遍历,一般检查项目个数不多  for循环不大
		for( int i=0;i<inspectionItems.size();i++){
			JSONObject  inspectionItem = inspectionItems.get(i);
			String inspectionItemId = inspectionItem.getString("inspectionItemId");
			JSONObject inspectionApply = new JSONObject();
			inspectionApply.put("inspectionItemId", inspectionItemId);
			inspectionApply.put("schoolId", param.getString("schoolId"));
			inspectionApply.put("applyId", param.getString("applyId"));
			List<JSONObject> inspectionApplys = venueManageSetDao.getInspectionApplyList(inspectionApply);
			String getScore="";
			if(CollectionUtils.isNotEmpty(inspectionApplys)&& inspectionApplys.size()>0){
				inspectionApply = inspectionApplys.get(0);
				getScore=inspectionApply.getString("score");
			}
			inspectionItem.put("getScore", StringUtil.formatNumber(getScore, 1));
		}
		applyAndOccupy.put("inspectionItems", inspectionItems);
		JSONObject comment=venueManageSetDao.getInspectionItemComment(param);
		applyAndOccupy.put("content", comment.get("content"));
		
		return applyAndOccupy;
	
	}
	/**
	 * 获取申请单审核详情
	 * @author zhh
	 */
	@Override
	public JSONObject getVenueApplyExamDetail(JSONObject param)
			throws Exception {
		String schoolId =  param.getString("schoolId");
		String termInfoId = param.getString("termInfoId");
		List<JSONObject> venueSets = new ArrayList<JSONObject>();
		//获取申请单和占用时间等相关信息
		List<JSONObject> applyAndOccupys = venueManageSetDao.getApplyAndOccupyList(param);
		JSONObject applyAndOccupy= new JSONObject();
		if(CollectionUtils.isNotEmpty(applyAndOccupys)&& applyAndOccupys.size()>0){
			applyAndOccupy = applyAndOccupys.get(0);
		}
		String useDate = Util.getFormatDate(applyAndOccupy.getString("useStartDate"),applyAndOccupy.getString("useEndDate"));
		applyAndOccupy.put("useDate", useDate);
		//根据申请单查看该单下的场馆
		applyAndOccupy.put("schoolId", param.getString("schoolId"));
		List<JSONObject> venues=venueManageSetDao.getVenueSetAndTypeAndManagerList(applyAndOccupy);
		JSONObject venue= new JSONObject();
		if(CollectionUtils.isNotEmpty(venues)&& venues.size()>0){
			venue = venues.get(0);
			venueSets.add(venue);
			applyAndOccupy.put("venueSets", venueSets);
		}
		venue.put("venueNum", venue.getString("venueNum")+"人");
		//取时间最近的审核对象
		JSONObject examApply = null;
		List<JSONObject>examApplys = venueManageSetDao.getExamApplyList(param);
		if(CollectionUtils.isNotEmpty(examApplys)&& examApplys.size()>0){
			examApply = examApplys.get(0);
		}
		if( examApply!=null){
			applyAndOccupy.put("examState", examApply.get("examState"));
			applyAndOccupy.put("disagreeReason", examApply.get("disagreeReason"));
		}else{
			applyAndOccupy.put("examState", "");
			applyAndOccupy.put("disagreeReason", "");
		}
		//设备要求
		Map<String, JSONObject> contentMap = new HashMap<String, JSONObject>();
		List<JSONObject> contentList = venueManageSetDao.getEquipmentRequireContentList(param);
		List<JSONObject> requireList = venueManageSetDao.getEquipmentRequireList(param);
		Set<Long> ids = new HashSet<Long>();
		for(JSONObject content:contentList){
			String requireId=content.getString("requireId");
			contentMap.put(requireId, content);
			if (StringUtils.isNotBlank(content.getString("teacherId"))) {
				ids.add(content.getLong("teacherId"));
			}
			
		}
		
		List<Account> accList = commonDataService.getAccountBatch(Long.parseLong(schoolId), new ArrayList<Long>(ids), termInfoId);
		Map<Long, String> id2Name = new HashMap<Long, String>();
		if(CollectionUtils.isNotEmpty(accList)) {
			for(Account acc : accList) {
				id2Name.put(acc.getId(), acc.getName());
			}
		}
		
		
		
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		for(int j=0;j<requireList.size();j++ ){
			JSONObject require =requireList.get(j);
			String requireId=require.getString("requireId");
			JSONObject content = contentMap.get(requireId);
			if(content==null){
				continue;
			}
			JSONObject returnObj = new JSONObject();
			String type=require.getString("type");
			returnObj.put("requireName", require.get("requireName"));
			returnObj.put("requireContent", require.get("requireContent"));
			returnObj.put("requireId", requireId);
			returnObj.put("type", type);
			returnObj.put("content", content.getString("content"));
			returnObj.put("num", content.getString("num"));
			 
			returnObj.put("prepared", content.getInteger("prepared"));
			if (StringUtils.isNotBlank(content.getString("teacherId"))) {
				returnObj.put("teacherId", content.getString("teacherId"));
				returnObj.put("teacherName", id2Name.get(content.getLongValue("teacherId")));
			}
			
			returnList.add(returnObj);
			
		}
		applyAndOccupy.put("equipmentRequires", returnList);
		return applyAndOccupy;
	}
	/**
	 * 获取统计列表
	 * //包括了 普通老师、审核人员、系统管理员的统计 
	 * @author zhh
	 */
	@Override
	public JSONObject getVenueStaticList(JSONObject param)
			throws Exception {
		JSONObject data = new JSONObject();
		List<JSONObject> rows = new ArrayList<JSONObject>();
		List<List<EasyUIDatagridHead>> columns = new ArrayList<List<EasyUIDatagridHead>>();
		List<EasyUIDatagridHead> l_col1 = new ArrayList<EasyUIDatagridHead>();
		List<EasyUIDatagridHead> l_col2 = new ArrayList<EasyUIDatagridHead>();
		final String systemManager ="0"; //系统管理员
		final String examPerson ="1"; //审核人员
		final String generalTeacher ="2"; //一般老师
		String role = param.getString("role");
		String teacherId = param.getString("teacherId");
		//查询所有申请单+场馆信息+使用时间（已检查）
		List<JSONObject> venues = venueManageSetDao.getVenueStaticList(param);
		List<JSONObject> venuesReturn = new ArrayList<JSONObject>();
		//查询所有申请单+检查项目+检查内容（已检查）
		Map<String, Map<String, JSONObject>> inspectionMap = new HashMap<String, Map<String,JSONObject>>();
		List<JSONObject> inspections = venueManageSetDao.getInspectionStaticList(param);
		 
		List<JSONObject> inspectionList =venueManageSetDao.getInspectionItemList(param);
		for(JSONObject inspection:inspections){
			String applyId=inspection.getString("applyId");
			String inspectionItemId=inspection.getString("inspectionItemId");
			if(applyId!=null && inspectionItemId!=null){
				Map<String,JSONObject> map=inspectionMap.get(applyId);
				if(map==null){
					map = new LinkedHashMap<String, JSONObject>();
				}
				if( map.get(inspectionItemId)==null){
					map.put(inspectionItemId,inspection);
					 
				}
				inspectionMap.put(applyId, map);
			}
		}
		
		 String isNeedExam = null;
		 String examState = null;
		if(systemManager.equals(role)){ //系统管理员查询所有已审核已检查的申请单
			 for(JSONObject venue: venues){
				 isNeedExam = venue.getString("isNeedExam");
				 if ("2".equals(isNeedExam)) {//不需要审核
					 venuesReturn.add(venue);
				 }else {
					 examState = venue.getString("examState");
					if ("1".equals(examState)) {//审核同意了
						 venuesReturn.add(venue);
					}
				}
			}
			
			
		}else if (generalTeacher.equals(role)){ //一般老师查询自己已审核已检查的申请单
			for(JSONObject venue: venues){
				String applyTeacherId = venue.getString("teacherId");
				if(teacherId.equals(applyTeacherId)){ //如果当前登录用户是申请人
					 isNeedExam = venue.getString("isNeedExam");
					 if ("2".equals(isNeedExam)) {//不需要审核
						 venuesReturn.add(venue);
					 }else {
						 examState = venue.getString("examState");
						if ("1".equals(examState)) {//审核同意了
							 venuesReturn.add(venue);
						}
					}
				}
			}
		}else if(examPerson.equals(role)){ //审核人员（自己+管理人员）
			List<JSONObject> managerList = venueManageSetDao.getVenueSetAndTypeAndManagerList(param);
			Map<String,JSONObject> managerMap = new HashMap<String, JSONObject>();
			for(JSONObject manager:managerList){
				managerMap.put(manager.getString("setId"), manager);
			}
			for(JSONObject venue: venues){
				String setId = venue.getString("setId");
				JSONObject manager = managerMap.get(setId);
				String applyTeacherId = venue.getString("teacherId");
				if(teacherId.equals(applyTeacherId)||(manager!=null &&  manager.getString("teacherIds").contains(teacherId)) ){
					 isNeedExam = venue.getString("isNeedExam");
					 if ("2".equals(isNeedExam)) {//不需要审核
						 venuesReturn.add(venue);
					 }else {
						 examState = venue.getString("examState");
						if ("1".equals(examState)) {//审核同意了
							 venuesReturn.add(venue);
						}
					}
				}
				
			}
		}else{  //无该角色
			return null;
		}
		
		
		
		
		List<String> itemListSize = new ArrayList<String>();
		
		for (int i = 0; i < inspectionList.size(); i++) {
			JSONObject object = inspectionList.get(i);
			String inspectionItemId = object.getString("inspectionItemId");
	    	String inspectionItemName = object.getString("inspectionItemName");
	    	String title=inspectionItemId+"_"+inspectionItemName;
	    	if(!itemListSize.contains(title)){
	    		itemListSize.add(title);
	    	}
		}
		
		//开始返回data
		Map<String,JSONObject> rowsMap= new LinkedHashMap<String, JSONObject>(); //一行所有东西所存的map
	    if(venues!=null){
			for(int i=0;i<venuesReturn.size();i++){
				JSONObject venue = venuesReturn.get(i);
				String applyId = venue.getString("applyId");
				String venueName = venue.getString("venueName");
				String teacherName = venue.getString("teacherName");
				String applyReason = venue.getString("applyReason");
				String useStartDate = venue.getString("useStartDate");
				String useEndDate = venue.getString("useEndDate"); 
				String useTime = venue.getString("useTime");
				
				//rows
				if(rowsMap.get(applyId)==null){
					JSONObject lineObj = new JSONObject();
					lineObj.put("venueName", venueName);
					lineObj.put("teacherName", teacherName);
					lineObj.put("applyReason", applyReason);
					String useDate = Util.getFormatDate(useStartDate,useEndDate);
					lineObj.put("useDate", useDate);
					//这里以后可以做优化（这里去掉“共计”和“钟”）
					String time=useTime.substring(2, useTime.length());
					if(time.endsWith("钟")){
						 time=useTime.substring(2, useTime.length()-1);
					}
					lineObj.put("useTime", time);
					lineObj.put("totalScore", 0);
					rowsMap.put(applyId, lineObj);
				}
				JSONObject lineObj = rowsMap.get(applyId);
				if(lineObj!=null&& inspectionMap.get(applyId)!=null){
					Map<String,JSONObject> inspectionContentMap = inspectionMap.get(applyId);
				    for (Map.Entry<String, JSONObject > entry : inspectionContentMap.entrySet()) {
				    	JSONObject inspection =entry.getValue();
				    	String inspectionItemId = inspection.getString("inspectionItemId");
				    	String inspectionItemName = inspection.getString("inspectionItemName");
				    	String title=inspectionItemId+"_"+inspectionItemName;
				    	if(!itemListSize.contains(title)){
				    		itemListSize.add(title);
				    	}
						float getScore = inspection.getFloat("getScore");
						lineObj.put(inspectionItemId, getScore);

						float totalScore = lineObj.getFloat("totalScore");
						//float temp = (float)(Math.round((totalScore+getScore)*10))/10;
						lineObj.put("totalScore", StringUtil.formatNumber(totalScore+getScore, 1));

						
					}
				    rowsMap.put(applyId, lineObj);
				}else if (lineObj!=null&& inspectionMap.get(applyId)==null) {
					for (int j = 0; j < inspectionList.size(); j++) {
						JSONObject object = inspectionList.get(j);
						String inspectionItemId = object.getString("inspectionItemId");
						lineObj.put(inspectionItemId, "");
					}
					lineObj.put("totalScore", "");
				}
			}
	    }
	    l_col1.add(new EasyUIDatagridHead("col","使用后检查项目", "center", 0, 1, itemListSize.size()+1, false));
	    for(int i=0;i<itemListSize.size();i++){
	    	String title=itemListSize.get(i);
	    	String []  titleArr=title.split("_");
	    	/*int width=12*4+15*2; //默认宽度为12*4+15*2
	    	int num=titleArr[1].length();
	    	if(num>4){ //如果大于7个字 则
	    		width=num*12+15*2;
	    	}
	    	if(width>300){ //限制最大宽度为300
	    		width=300;
	    	}*/
	    	 l_col2.add(new EasyUIDatagridHead(titleArr[0],titleArr[1] , "center", 130, 1, 1, false));
	    }
	    for (Map.Entry<String, JSONObject > entry : rowsMap.entrySet()) {
			rows.add(entry.getValue());
		}
	    l_col2.add(new EasyUIDatagridHead("totalScore", "综合得分", "center", 87,1, 1, false));
		columns.add(l_col1);
		columns.add(l_col2);
		data.put("rows", rows);
		data.put("columns", columns);
		return data;
	}
	@Override
	public List<JSONObject> getPrePareEquipMentList(JSONObject param) throws Exception {

		param.put("orderByUseStartDate", 1);
	    List<JSONObject> applyOccupyList =	venueManageSetDao.getApplyAndOccupyList(param); 
		List<JSONObject> venueList = venueManageSetDao.getVenueSetAndTypeAndManagerList(param);
		Map<String, JSONObject> venueMap = new HashMap<String, JSONObject>(); //setId --JSONOBJ
		String role = param.getString("role");
		for(JSONObject venue:venueList){
			String setId = venue.getString("setId");
			venueMap.put(setId, venue);
		}
		List<JSONObject> equipmentManagerList = null;
		Map<String, JSONObject> equipmentMap =null;
		if (!"0".equals(role)) {
			 equipmentMap = new HashMap<String, JSONObject>(); //setId --JSONOBJ
			 equipmentManagerList = venueManageSetDao.getEquipmentManagerList(param);
			for (JSONObject equipment :equipmentManagerList) {
				String setId = equipment.getString("setId");
				 equipmentMap.put(setId, equipment);
			}
			 
		}
	 
		
		
        Date now = new Date();
        Date useStartDate = null;
        final String noNeedExam ="2"; //无需审核
        final String agree ="1"; //同意
        String isNeedExam= null;
        String examState= null;
    	List<JSONObject> returnList = new ArrayList<JSONObject>();
    	int index = 0;
		for(int i=0;i<applyOccupyList.size();i++ ){
			JSONObject applyOccupy = applyOccupyList.get(i);
			String setId = applyOccupy.getString("setId");
			useStartDate = applyOccupy.getDate("useStartDate");
			String useDate = Util.getFormatDate(applyOccupy.getString("useStartDate"),applyOccupy.getString("useEndDate"));
			applyOccupy.put("useDate", useDate);
			JSONObject venue = venueMap.get(setId);
			if(venue==null){
				continue;
			}
			isNeedExam=venue.getString("isNeedExam"); 
			examState = applyOccupy.getString("examState");
			if (!(noNeedExam.equals(isNeedExam)) && !agree.equals(examState)) {
				continue;
			}
			if (!"0".equals(role) ) {
				if (equipmentMap.get(setId) == null) {
					continue;
				}
			}
			
			if (now.getTime() > useStartDate.getTime() &&"1".equals(applyOccupy.getString("equipmentStatus")) ) {
				applyOccupy.put("equipmentStatus", "3");//过期未准备
			}
			applyOccupy.put("venueName", venue.getString("venueName"));
			applyOccupy.put("venueAddr", venue.getString("venueAddr"));
			index = index + 1 ;
			applyOccupy.put("index", index);
			returnList.add(applyOccupy);
		} 
		return returnList;
 
	}
	@Override
	public int updateVenueApplyEquipmentExam(JSONObject param) throws Exception {
		 
		
		venueManageSetDao.updateVenueApplyEquipmentExam(param);
		
		return 0;
	}
	@Override
	public int updateApplyEquipmentStatus(JSONObject param) throws Exception {
		venueManageSetDao.updateApplyEquipmentStatus(param);
		return 0;
	}
	@Override
	public List<JSONObject> getEquipmentRequireContentList(JSONObject param) {
		 
		return venueManageSetDao.getEquipmentRequireContentList(param);
	}

	@Override
	public List<JSONObject> getExamApplyList(JSONObject param) {
		return venueManageSetDao.getExamApplyList(param);
	}
}
