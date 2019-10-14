package com.talkweb.venueManage.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.api.message.utils.MessageNoticeModelEnum;
import com.talkweb.api.message.utils.MessageNoticeTypeEnum;
import com.talkweb.api.message.utils.MessageServiceEnum;
import com.talkweb.api.message.utils.MessageSmsEnum;
import com.talkweb.api.message.utils.MessageStatusEnum;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.MotanService;
import com.talkweb.utils.KafkaWXmsgThread;
import com.talkweb.venueManage.dao.VenueManageSetDao;
import com.talkweb.venueManage.service.VenueManageAppService;
import com.talkweb.venueManage.util.Util;
/** 
 * 场馆使用-App SIMPL
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年4月20日 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Service
public class VenueManageAppServiceImpl implements VenueManageAppService {
	@Autowired
	private VenueManageSetDao venueManageSetDao;
	@Autowired
	private AllCommonDataService allCommonDataService;
	private static final Logger logger = LoggerFactory.getLogger(VenueManageAppServiceImpl.class);
	
	@Value("#{settings['clientId']}")
	private String clientId;

	@Value("#{settings['clientSecret']}")
	private String clientSecret;

	@Value("#{settings['kafkaUrl']}")
	private String kafkaUrl;
 
	@Value("#{settings['venueManage.msgUrlPc']}")
	private String msgUrlPc;
	
	@Value("#{settings['venueManage.msgUrlApp']}")
	private String msgUrlApp;
	
	private static final String MSG_TYPE_CODE = "CGSY";
	 
	 /**
	 * redis
	 */
	@Resource(name="redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;
	
	@Override
	public List<JSONObject> getVenueApplyList(JSONObject param)
			throws Exception {
		final String systemManager ="0"; //系统管理员
		final String AGREE ="1"; //同意
		final String NONEEDEXAM ="2"; //无需审核
		String teacherId = param.getString("teacherId"); //当前用户
		String role = param.getString("role"); //当前用户角色
		//查询申请单（时间段）
	    List<JSONObject> applyOccupyList =	venueManageSetDao.getApplyAndOccupyList(param); 
	    //查询类别和场馆名称下的场馆（场馆名称+类别）
		List<JSONObject> venueList = venueManageSetDao.getVenueSetAndTypeAndManagerList(param);
		//查询所有的审批记录
		List<JSONObject> examApplyList = venueManageSetDao.getExamApplyList(param);
		Map<String,JSONObject> examApplyMap = new HashMap<String, JSONObject>(); 
		for(JSONObject examApply :examApplyList){
			if(examApplyMap.get(examApply.getString("applyId"))!=null){    //根据最近一次的审批情况考虑
				continue;
			}
			examApplyMap.put(examApply.getString("applyId"), examApply);
		}
		//查询所有的检查记录
		List<JSONObject> inspectionApplyist = venueManageSetDao.getInspectionApplyList(param);
		Map<String,JSONObject> inspectionApplyMap = new HashMap<String, JSONObject>(); 
		for(JSONObject inspectionApply :inspectionApplyist){
			if(inspectionApplyMap.get(inspectionApply.getString("applyId"))!=null){    //根据最近一次的审批情况考虑
				continue;
			}
			inspectionApplyMap.put(inspectionApply.getString("applyId"), inspectionApply);
		}
		Map<String, JSONObject> venueMap = new HashMap<String, JSONObject>(); //setId --JSONOBJ
		for(JSONObject venue:venueList){
			String setId = venue.getString("setId");
			venueMap.put(setId, venue);
		}
		//开始返回data
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		int count=0;
		for(int i=0;i<applyOccupyList.size();i++ ){
			JSONObject applyOccupy = applyOccupyList.get(i);
			/*String startTime = DateUtil.getDateW3CFormat(new Date(Long.parseLong(applyOccupy.getString("useStartDate"))));
			String endTime = DateUtil.getDateW3CFormat(new Date(Long.parseLong(applyOccupy.getString("useEndDate"))));*/
			
			String startTime=applyOccupy.getString("useStartDate");
			String endTime=applyOccupy.getString("useEndDate");
			if(StringUtils.isNotBlank(startTime)) {
				String useStartDate = startTime.substring(11,16);
				applyOccupy.put("useStartDate", useStartDate);
				
				String useDate = Util.getFormatDate(startTime, endTime);
				String useDateHead = useDate.substring(0,10);
				String useDateTail = useDate.substring(11,useDate.length());
				applyOccupy.put("useDateHead", useDateHead);
				applyOccupy.put("useDateTail", useDateTail);
			}else {
				/*returnObj.put("useDate", DateUtil.getDateFormat(new Date()));
				String useStartDate =DateUtil.getDateFormat(new Date()).substring(11,16);
				returnObj.put("useStartDate", useStartDate);*/
				
				applyOccupy.put("useDate","");
				applyOccupy.put("useStartDate", "");
			}
			
			if(StringUtils.isNotBlank(endTime)) {
				String useEndDate = endTime.substring(11,16);
				applyOccupy.put("useEndDate", useEndDate);
			}else {
				applyOccupy.put("useEndDate", "");
			}
			
			String applyRole = applyOccupy.getString("applyRole");
			String setId = applyOccupy.getString("setId");
			String applyId = applyOccupy.getString("applyId");
			String teacherIdApply = applyOccupy.getString("teacherId");//申请人
			if(!teacherId.equals(teacherIdApply)){ //当前登录人必须是老师
				continue;
			}
			JSONObject venue = venueMap.get(setId);
			if(venue==null){
				continue;
			}
			//applyOccupy.put("teacherName", venue.getString("teacherNames"));
			applyOccupy.put("venueName", venue.getString("venueName"));
			//applyOccupy.put("venueAddr", venue.getString("venueAddr"));
			applyOccupy.put("isDelete", 2);//初始化 默认为2
			String teacherIds=venue.getString("teacherIds"); //该申请的对应场馆的审批人员
			String isNeedExam=venue.getString("isNeedExam"); //无需审核
			JSONObject examApply = examApplyMap.get(applyId); //最近审批记录
			JSONObject inspectionApply = inspectionApplyMap.get(applyId); //最近检查记录
			
			//申请状态、是否可删除
			if(NONEEDEXAM.equals(isNeedExam)){  //无需审核
				applyOccupy.put("applyState", 1);
				if(inspectionApply!=null){ //有检查记录
					//applyOccupy.put("isUpdate", 2);
					applyOccupy.put("isDelete", 2);
				}else{
					//applyOccupy.put("isUpdate", 1);
					applyOccupy.put("isDelete", 1);
				}
			}else{   //需审核
				if(examApply==null){  //无审核记录
				 	//如果当前登录人是审核人||申请该单的为超级管理员，则直接 “已同意”
				    /*if((StringUtils.isNotBlank(teacherIds) && teacherIds.contains(teacherId))||systemManager.equals(role)){
						applyOccupy.put("applyState", 3);
						//applyOccupy.put("isUpdate", 1);
						if(inspectionApply!=null){ //有检查记录
							applyOccupy.put("isDelete", 2);
						}else{
							applyOccupy.put("isDelete", 1); //无检查记录
						}
					 }else{ //当前登录人不是审核人员，则“待审核”*/
						applyOccupy.put("applyState", 2);
						//applyOccupy.put("isUpdate", 1);
						if(inspectionApply!=null){ //有检查记录
							applyOccupy.put("isDelete", 2);
						}else{
							applyOccupy.put("isDelete", 1); //无检查记录
						}
					 //}
				}else{ //有审核记录
					String examState = examApply.getString("examState");
					if(AGREE.equals(examState)){ //同意
						applyOccupy.put("applyState", 3);
						//applyOccupy.put("isUpdate", 2);
						applyOccupy.put("isDelete", 2);
					}else{   //不同意
						applyOccupy.put("applyState", 4);
						//applyOccupy.put("isUpdate", 2);
						applyOccupy.put("isDelete", 2);
					}
				}
			}
			count++;  
			if(count>20){//返回前20个给App用户
				break;
			}
			returnList.add(applyOccupy);
		}
		return returnList;
	}

	@Override
	public int addAppVenueApply(JSONObject param) throws Exception {
		/*synchronized(this)
		{*/
		    final String OTHERREQUIREMENT = "3"; //设备要求中的其他要求
			String comment = param.getString("comment");
			String schoolId = param.getString("schoolId");
			String teacherId = param.getString("teacherId");
			String userId = param.getString("userId");
			String setId = param.getString("setId");
			String applyId = UUIDUtil.getUUID();
			param.put("applyId", applyId);
			//APP和PC是同一个LOCK
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
					/*useStartTime = DateUtil.getDateW3CFormat(new Date(Long.parseLong(useStartTime)));
					useEndTime = DateUtil.getDateW3CFormat(new Date(Long.parseLong(useEndTime)));*/
					
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
					List<JSONObject> applyAndOccupys = venueManageSetDao.getApplyAndOccupyList(applyOccupyParam);
					for (JSONObject applyAndOccupy : applyAndOccupys) {
						String useStartApply = applyAndOccupy.getString("useStartDate");
						String useEndApply = applyAndOccupy.getString("useEndDate");
						
						/*useStartApply = DateUtil.getDateW3CFormat(new Date(Long.parseLong(useStartApply)));
						useEndApply = DateUtil.getDateW3CFormat(new Date(Long.parseLong(useEndApply)));*/
						
						Date useStartDateApply = DateUtil.parseDateSecondFormat(useStartApply);
						Date useEndDateApply = DateUtil.parseDateSecondFormat(useEndApply);
						if (useStartDateApply != null && useEndDateApply != null && endDate != null
								&& startDate != null) {
							boolean overlap = ((useStartDateApply.getTime() >= startDate.getTime())
									&& useStartDateApply.getTime() < endDate.getTime())
									|| ((useStartDateApply.getTime() > startDate.getTime())
											&& useStartDateApply.getTime() <= endDate.getTime())
									|| ((startDate.getTime() >= useStartDateApply.getTime())
											&& startDate.getTime() < useEndDateApply.getTime())
									|| ((startDate.getTime() > useStartDateApply.getTime())
											&& startDate.getTime() <= useEndDateApply.getTime());
							if (overlap && applyAndOccupy.getInteger("examState") != 2) {
								return -2;
							}
						}
					}
					//删除该申请单下的占用时间设置
					//venueManageSetDao.deleteOccupy(param);
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
					//venueManageSetDao.deleteEquipmentRequireContent(param);
					//该comment为设备要求里的其他要求
					List<JSONObject> equipmentRequiresAdd = new ArrayList<JSONObject>();
					if (StringUtils.isNotBlank(comment)) {
						List<JSONObject> equipmentRequires = venueManageSetDao.getEquipmentRequireList(param);
						for (int i = 0; i < equipmentRequires.size(); i++) {
							JSONObject equipmentRequire = equipmentRequires.get(i);
							if (equipmentRequire.containsKey("type")
									&& OTHERREQUIREMENT.equals(equipmentRequire.getString("type"))) {
								equipmentRequire.put("schoolId", schoolId);
								equipmentRequire.put("contentId", UUIDUtil.getUUID());
								equipmentRequire.put("applyId", applyId);
								equipmentRequire.put("content", comment);
								equipmentRequire.put("num", 0);
								equipmentRequiresAdd.add(equipmentRequire);
								break;
							}
						}
					}
					//添加该申请单下的设备要求内容
					if (equipmentRequiresAdd.size() > 0) {
						venueManageSetDao.addEquipmentRequireContentBatch(equipmentRequiresAdd);
					}
					//添加申请单
					param.put("createDate", DateUtil.getDateFormatNow());
					param.put("checkState", 1); //未检查
					param.put("occupyId", occupyId);
					param.put("applyState", 1); //未审核
					//新增申请单
					venueManageSetDao.addApply(param);
					break;
				} finally {
					redisOperationDAO.del(lock);
				}
		}
			
	    if(isWait){return -4;}
	    
	    try {
	    	param.put("status", 0);
	    	this.sendWxTemplateMsg(param);
		} catch (Exception e) {
			logger.error("场馆申请失败!"+applyId);
		}
	    
	  	return 1;
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
				logger.debug("\n ========== 开始调用RPC noticeMessage()方法============");
				result = MotanService.noticeMessage(jsonObject);
				logger.debug("\n ================ 返回结果："+ result+" ==============");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
	@Override
	public JSONObject getAppVenueApplyExamDetail(JSONObject param)
			throws Exception {
		final String rNUMBER="1";//设备要求类别数量
		final String rCONTENT="2";////设备要求类别内容
		final String rOTHER="3";////设备要求类别其他
				
		logger.debug("getAppVenueApplyExamDetail2:"+param.toJSONString());
		//获取申请单和占用时间等相关信息
		List<JSONObject> applyAndOccupys = venueManageSetDao.getApplyAndOccupyList(param);
		JSONObject applyAndOccupy= new JSONObject();
		if(CollectionUtils.isNotEmpty(applyAndOccupys)&& applyAndOccupys.size()>0){
			applyAndOccupy = applyAndOccupys.get(0);
		}else {
			return null;
		}
		String phone = applyAndOccupy.getString("phone");
		if(phone==null || StringUtils.isBlank(phone)){
			 applyAndOccupy.put("phone", "无");
		}
		/* String startTime=applyAndOccupy.getString("useStartDate");
		String endTime=applyAndOccupy.getString("useEndDate");*/
		
		/*String startTime = DateUtil.getDateW3CFormat(new Date(applyAndOccupy.getLongValue("useStartDate")));
		String endTime = DateUtil.getDateW3CFormat(new Date(applyAndOccupy.getLongValue("useEndDate")));*/
	/*	System.out.println("startTime："+startTime);
		System.out.println("endTime："+endTime);
		
		String useDate = startTime.substring(0,10);
		String useStartDate = startTime.substring(11,16);
		String useEndDate = endTime.substring(11,16);
		applyAndOccupy.put("useStartDate", useStartDate);
		applyAndOccupy.put("useEndDate", useEndDate);
		applyAndOccupy.put("useDate", useDate);*/
		
		String startTime=applyAndOccupy.getString("useStartDate");
		if(StringUtils.isNotBlank(startTime)) {
			String useDate = startTime.substring(0,10);
			String useStartDate = startTime.substring(11,16);
			applyAndOccupy.put("useDate", useDate);
			applyAndOccupy.put("useStartDate", useStartDate);
		}else {
			/*returnObj.put("useDate", DateUtil.getDateFormat(new Date()));
			String useStartDate =DateUtil.getDateFormat(new Date()).substring(11,16);
			returnObj.put("useStartDate", useStartDate);*/
			
			applyAndOccupy.put("useDate","");
			applyAndOccupy.put("useStartDate", "");
		}
		
		String endTime=applyAndOccupy.getString("useEndDate");
		if(StringUtils.isNotBlank(endTime)) {
			String useEndDate = endTime.substring(11,16);
			applyAndOccupy.put("useEndDate", useEndDate);
		}else {
			applyAndOccupy.put("useEndDate", "");
		}
		
		//根据申请单查看该单下的场馆
		applyAndOccupy.put("schoolId", param.getString("schoolId"));
		List<JSONObject> venues=venueManageSetDao.getVenueSetAndTypeAndManagerList(applyAndOccupy);
		JSONObject venue= new JSONObject();
		if(CollectionUtils.isNotEmpty(venues)&& venues.size()>0){
			venue = venues.get(0);
			if(venue!=null){
				applyAndOccupy.put("venueName", venue.getString("venueName"));
				applyAndOccupy.put("venueAddr", venue.getString("venueAddr"));
			}
		}
		//取时间最近的审核对象
		JSONObject examApply = null;
		List<JSONObject>examApplys = venueManageSetDao.getExamApplyList(param);
		if(CollectionUtils.isNotEmpty(examApplys)&& examApplys.size()>0){
			examApply = examApplys.get(0);
		}
		if( examApply!=null && examApply.get("examState")!=null ){
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
		for(JSONObject content:contentList){
			String requireId=content.getString("requireId");
			contentMap.put(requireId, content);
		}
		//List<JSONObject> returnList = new ArrayList<JSONObject>();
		String equipmentRequiresContent="";
		for(int j=0;j<requireList.size();j++ ){
			JSONObject require =requireList.get(j);
			String requireId=require.getString("requireId");
			JSONObject content = contentMap.get(requireId);
			if(content==null){
				continue;
			}
//			JSONObject returnObj = new JSONObject();
//			String type=require.getString("type");
//			returnObj.put("requireName", require.get("requireName"));
//			returnObj.put("requireContent", require.get("requireContent"));
//			returnObj.put("requireId", requireId);
//			returnObj.put("type", type);
//			returnObj.put("content", content.getString("content"));
//			returnObj.put("num", content.getString("num"));
//			returnList.add(returnObj);
			String type=require.getString("type");
			String requireName = require.getString("requireName");
			if(rNUMBER.equals(type)){
				equipmentRequiresContent+=requireName+",数量,"+content.getString("num")+";";
			}else if(rCONTENT.equals(type)){
				equipmentRequiresContent+=requireName+",显示内容:"+content.getString("content")+";";
			}else if(rOTHER.equals(type)){
				equipmentRequiresContent+="其他要求:"+content.getString("content");
			}else{
				continue;
			}
		}
	
		applyAndOccupy.put("equipmentRequiresContent", equipmentRequiresContent);
		//applyAndOccupy.put("equipmentRequires", returnList);
		
		return applyAndOccupy;
	}

	@Override
	public JSONObject getAppVenueApplyInspectionDetail(JSONObject param)
			throws Exception {
		JSONObject res = new JSONObject();
		String teacherId = param.getString("teacherId"); //当前登录用户
		String role = param.getString("role");
		// 取时间最近的检查对象
		List<JSONObject> inspectionItems = venueManageSetDao.getApplyHistoryList(param);

		// 检查项的遍历,一般检查项目个数不多 for循环不大
		List<JSONObject> rList = new ArrayList<JSONObject>();
		for (int i = 0; i < inspectionItems.size(); i++) {
			JSONObject inspectionItem = inspectionItems.get(i);
			String inspectionItemId = inspectionItem.getString("inspectionItemId");
			JSONObject inspectionApply = new JSONObject();
			inspectionApply.put("inspectionItemId", inspectionItemId);
			inspectionApply.put("schoolId", param.getString("schoolId"));
			inspectionApply.put("applyId", param.getString("applyId"));
			List<JSONObject> inspectionApplys = venueManageSetDao.getInspectionApplyList(inspectionApply);
			String getScore = "";
			if (CollectionUtils.isNotEmpty(inspectionApplys)
					&& inspectionApplys.size() > 0) {
				inspectionApply = inspectionApplys.get(0);
				getScore = inspectionApply.getString("score");
			}
			JSONObject rObj =  new JSONObject();
			rObj.put("inspectionItemId", inspectionItemId);
			rObj.put("inspectionItemName", inspectionItem.get("inspectionItemName"));
			rObj.put("score", inspectionItem.get("score"));
			rObj.put("orderNum", inspectionItem.get("orderNum"));
			rObj.put("getScore", StringUtil.formatNumber(getScore, 1));
			rList.add(rObj);
		}
		res.put("inspectionItems", rList);
		JSONObject comment = venueManageSetDao.getInspectionItemComment(param);
		res.put("content", comment.get("content"));
		res.put("applyId", param.get("applyId"));
		return res;
	}

	@Override
	public List<JSONObject> getAppVenueApplyExamList(JSONObject param)
			throws Exception {
		final String systemManager ="0"; //系统管理员
		final String examPerson ="1"; //审核人员
		final String needExam ="1"; //需审核
		final String agree ="1"; //同意
		
		String role = param.getString("role"); //当前登录用户
		String teacherId = param.getString("teacherId"); //当前登录用户
		//查询申请单
		param.put("orderByUseStartDate", 1 );
	    List<JSONObject> applyOccupyList =	venueManageSetDao.getApplyAndOccupyList(param); 
	    
	    //查询类别和场馆名称下的场馆（场馆名称+类别）
		List<JSONObject> venueList = venueManageSetDao.getVenueSetAndTypeAndManagerList(param);
		Map<String, JSONObject> venueMap = new HashMap<String, JSONObject>(); //setId --JSONOBJ
		for(JSONObject venue:venueList){
			String setId = venue.getString("setId");
			venueMap.put(setId, venue);
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
		//查询所有的检查记录
		List<JSONObject> inspectionApplyist = venueManageSetDao.getInspectionApplyList(param);
		Map<String,JSONObject> inspectionApplyMap = new HashMap<String, JSONObject>(); 
		for(JSONObject inspectionApply :inspectionApplyist){
			if(inspectionApplyMap.get(inspectionApply.getString("applyId"))!=null){    //根据最近一次的审批情况考虑
				continue;
			}
			if(inspectionApply.getString("applyId")!=null){
				inspectionApplyMap.put(inspectionApply.getString("applyId"), inspectionApply);
			}
		}
		//开始返回data
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		for(int i=0;i<applyOccupyList.size();i++ ){
			
			JSONObject returnObj = new JSONObject();
			JSONObject applyOccupy = applyOccupyList.get(i);
			String setId = applyOccupy.getString("setId");
			String applyRole = applyOccupy.getString("applyRole");
			String applyId = applyOccupy.getString("applyId");
			//Long schoolId = applyOccupy.getLong("schoolId");
			String teacherIdApply = applyOccupy.getString("teacherId");//申请人
			
			String startTime=applyOccupy.getString("useStartDate");
			if(StringUtils.isNotBlank(startTime)) {
				String useDate = startTime.substring(0,10);
				String useStartDate = startTime.substring(11,16);
				returnObj.put("useDate", useDate);
				returnObj.put("useStartDate", useStartDate);
			}else {
				/*returnObj.put("useDate", DateUtil.getDateFormat(new Date()));
				String useStartDate =DateUtil.getDateFormat(new Date()).substring(11,16);
				returnObj.put("useStartDate", useStartDate);*/
				
				returnObj.put("useDate","");
				returnObj.put("useStartDate", "");
			}
			
			String endTime=applyOccupy.getString("useEndDate");
			if(StringUtils.isNotBlank(endTime)) {
				String useEndDate = endTime.substring(11,16);
				returnObj.put("useEndDate", useEndDate);
			}else {
				returnObj.put("useEndDate", "");
			}
			
			JSONObject venue = venueMap.get(setId);
			if(venue==null){
				continue;
			}
			String teacherIds=venue.getString("teacherIds"); //该申请的对应场馆的审批人员
			String isNeedExam=venue.getString("isNeedExam"); //无需审核
			
			returnObj.put("applyId", applyId);
			returnObj.put("venueName", venue.get("venueName"));
			
			//需审核
			
				//当前登录人是超管||（该条申请单的审核人员是当前登录对象&&角色为审批人员）
				if(systemManager.equals(role)||(examPerson.equals(role)&&StringUtils.isNotBlank(teacherIds)
					&&teacherIds.contains(teacherId))){
					//封装ifGreyInspection和ifIsExam开始
					JSONObject examApply = examApplyMap.get(applyId); //最近审批记录
					JSONObject inspectionApply = inspectionApplyMap.get(applyId); //最近检查记录
					if(needExam.equals(isNeedExam)){
						returnObj.put("ifGreyInspection", 1);
						if(examApply!=null){
							String examState=examApply.getString("examState");
							if(agree.equals(examState)){ //需审核&&已审核 && 同意 ----> “已同意”
								returnObj.put("ifGreyInspection", 2);
								returnObj.put("ifIsExam", 1);
							}else{
								returnObj.put("ifIsExam", 3);
								returnObj.put("ifGreyInspection", 1);
							}
						}else{
							//如果申请者为超管或者审批人员则显示“已审核”
						/*	if(teacherIds!=null&&teacherIds.contains(teacherIdApply)||systemManager.equals(applyRole)){  
								returnObj.put("ifIsExam", 1);
								returnObj.put("ifGreyInspection", 2);
							}else{*/
								returnObj.put("ifIsExam", 2);
								returnObj.put("ifGreyInspection", 1);
							//}
						}
					}else{ //无需审核
						returnObj.put("ifIsExam", 4);
						returnObj.put("ifGreyInspection", 2);
					}
					    returnList.add(returnObj);
				}
			
		}
		//排序：按需审核的先排，其他后排
		List<JSONObject> trueReturnList = new LinkedList<JSONObject>();
		List<JSONObject> falseReturnList = new LinkedList<JSONObject>();
		int count = 0;
		for(int j =0;j<returnList.size();j++){
			JSONObject returnObj = returnList.get(j);
			String ifIsExam = returnObj.getString("ifIsExam");
			if("2".equals(ifIsExam)){
				count++;
				returnObj.put("index", count);
				trueReturnList.add(returnObj);
			}else{
				falseReturnList.add(returnObj);
			}
		}
		for(int j =0;j<falseReturnList.size();j++){
			JSONObject returnObj = falseReturnList.get(j);
			count++;
			returnObj.put("index", count);
			trueReturnList.add(returnObj);
		}
		return trueReturnList;
	}

	@Override
	public JSONObject getVenueAndAprovel(JSONObject param) {
		return venueManageSetDao.getVenueAndAprovel(param);
	}

	@Override
	public List<JSONObject> getExamApplyList(JSONObject param) {
		return venueManageSetDao.getExamApplyList(param);
	}

	@Override
	public void addExamApply(JSONObject param) {
		venueManageSetDao.addExamApply(param);
	}

	@Override
	public List<JSONObject> getVenueManagerList(JSONObject param) {
		return venueManageSetDao.getVenueManagerList(param);
	}

	@Override
	public void sendWxTemplateMsg(JSONObject params) {
		logger.info("\n===================\n=======sendVenueWxTemplateMsg:"+params.toJSONString());
		System.out.println("\n===================\n=======sendVenueWxTemplateMsg2:"+params.toJSONString());
		String applyId = params.getString("applyId");
		try {
			String schoolId = params.getString("schoolId");
			String desc = params.getString("applyReason");

			// 1. 获取申请使用场馆信息
			JSONObject query = new JSONObject();
			query.put("applyId", applyId);
			JSONObject data = venueManageSetDao.getVenueAndAprovel(query);
			if(data == null || data.isEmpty()) {
				logger.info("未查到数据:"+params.toJSONString());
				return;
			}
			logger.info("getVenueAndAprovel："+data);
			// 不需要审核，则不发送模板消息
			if(data.getIntValue("isNeedExam") == 2) {
				return;
			}
			
			String applyName = data.getString("teacherName");
			String applyReson = data.getString("applyReason");
			if(StringUtils.isBlank(desc))
				desc = applyReson;
			
			String venueName = data.getString("venueName");
			String applyTeacherId = data.getString("teacherId");
			String title = "您好，您有一项场馆申请需要审核。";
			String remark = "请点击详情进行审核。";
			
			// 2. 获取场馆审批列表
			List<JSONObject> applyList = venueManageSetDao.getExamApplyList(params);
			
			JSONObject manaParam = new JSONObject();
			manaParam.put("schoolId", Long.parseLong(schoolId));
			manaParam.put("setId", data.getString("setId"));
			
			// 审批人
			List<JSONObject> manager = venueManageSetDao.getVenueManagerList(manaParam);
			
			// 3. 更新场馆审批状态 1 同意 2不同意
			int status = params.getIntValue("status");
			boolean isApprovaled = false;				
			JSONArray receivers = new JSONArray();
			int isAdminAndApplyer = 0; // 0：是审批人不是申请者，1是是审批人也是申请者
			
			// 自己给自己审批
			if(applyList != null && applyList.size()>0) {
				for(JSONObject obj : applyList) {
					// 是是审批人也是申请者
					if(applyTeacherId.equals(obj.getString("teacherId"))){
						isAdminAndApplyer = 1;
					}
				}
				
				title = "您好，您申请的场馆有了新的审核进度！";
				remark =  "请点击详情进行查看。";
				isApprovaled = true;
				
				Account account = allCommonDataService.getAccountAllById(Long.parseLong(schoolId)
						,Long.parseLong(applyTeacherId), 
						params.getString("termInfo"));
				
				JSONObject msgCenterReceiver = new JSONObject();
				msgCenterReceiver.put("userId", account.getExtId());
				msgCenterReceiver.put("userName", account.getName());
				receivers.add(msgCenterReceiver);
			}else {
				title = "您好，你有一项场馆申请需要审核。";
				remark = "请点击详情进行审核。";
				// 获取下个环节审批人
				for(JSONObject mang : manager) {
					JSONObject param = new JSONObject();
					param.put("teacherId", mang.getString("teacherId"));

					
					Account account = allCommonDataService.getAccountAllById(Long.parseLong(schoolId)
							,param.getLongValue("teacherId") 
							,params.getString("termInfo"));
					
					JSONObject msgCenterReceiver = new JSONObject();
					msgCenterReceiver.put("userId", account.getExtId());
					msgCenterReceiver.put("userName", account.getName());
					receivers.add(msgCenterReceiver);
				}
			}

			// 获取学校extId
			School school = allCommonDataService.getSchoolById(Long.parseLong(schoolId), params.getString("termInfo"));
			// 4. 发送消息
			JSONObject msg = new JSONObject();
		    String msgId = UUIDUtil.getUUID().replace("-", "");
			msg.put("msgId", msgId);
			msg.put("msgTitle", title);
			msg.put("msgContent", "你收到一条新的场馆使用审核单!");
			msg.put("msgUrlPc", "" + msgUrlPc);
			msg.put("msgUrlApp", "" + String.format(msgUrlApp, applyId,isApprovaled ? 1 : 0,isAdminAndApplyer));
			msg.put("msgOrigin", "场馆使用提醒");
			msg.put("msgTypeCode", MSG_TYPE_CODE);
			msg.put("msgTemplateType", MSG_TYPE_CODE);
			msg.put("schoolId", school.getExtId());
			msg.put("creatorName", applyName);
			
			// 标题
			JSONObject first = new JSONObject();
			first.put("value",title);

			// 申请人
			JSONObject keyword1 = new JSONObject();
			keyword1.put("value", venueName);
			
			// 申请物品
			JSONObject keyword2 = new JSONObject();
			keyword2.put("value", applyName);

			// 备注
			JSONObject keyword3 = new JSONObject();
			keyword3.put("value", applyReson);
			
			JSONObject remarkObj = new JSONObject();
			remarkObj.put("value", remark);
			
			JSONObject msgData = new JSONObject();
			msgData.put("first", first);
			msgData.put("keyword1", keyword1);
			msgData.put("keyword2", keyword2);
			msgData.put("keyword3", keyword3);
			msgData.put("remark", remarkObj);
			msgData.put("url", String.format(msgUrlApp, applyId,isApprovaled ? 1 : 0,isAdminAndApplyer));
			msg.put("msgWxJson", msgData);
			
			JSONObject msgBody = new JSONObject();
			msgBody.put("msg", msg);
			msgBody.put("receivers", receivers); 
			logger.info("msgBody=====>" + msgBody.toString());

			logger.debug("kafka:"+kafkaUrl+" clientId:"+clientId+" clientSecret:"+clientSecret+" MSG_TYPE_CODE:"+MSG_TYPE_CODE+" msgId:"+msgId);

			KafkaWXmsgThread kafka = new KafkaWXmsgThread(kafkaUrl, msgId, msgBody, MSG_TYPE_CODE, clientId, clientSecret      );
			kafka.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
