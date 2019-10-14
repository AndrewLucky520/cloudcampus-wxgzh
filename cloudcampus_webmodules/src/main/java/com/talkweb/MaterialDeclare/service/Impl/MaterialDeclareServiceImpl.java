package com.talkweb.MaterialDeclare.service.Impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.MaterialDeclare.dao.MaterialDeclareDao;
import com.talkweb.MaterialDeclare.service.MaterialDeclareService;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.utils.KafkaWXmsgThread;
import com.talkweb.utils.Result;

@Service
public class MaterialDeclareServiceImpl implements MaterialDeclareService{

	private static final Logger logger = LoggerFactory.getLogger(MaterialDeclareServiceImpl.class);
	
	@Autowired
	private MaterialDeclareDao materialDeclareDao;
	
	@Autowired
	private AllCommonDataService commonDataService;

	@Value("#{settings['clientId']}")
	private String clientId;

	@Value("#{settings['clientSecret']}")
	private String clientSecret;

	@Value("#{settings['kafkaUrl']}")
	private String kafkaUrl;
 
	@Value("#{settings['materialDeclare.msgUrlPc']}")
	private String msgUrlPc="https://pre.yunxiaoyuan.com/talkCloud/homePage.html#cloudManager/materialDeclaration";
	
	@Value("#{settings['materialDeclare.msgUrlApp']}")
	private String msgUrlApp="https://pre.yunxiaoyuan.com/apph5/WeChat/#/?name=materialdetail&id=";
	
	private static final String MSG_TYPE_CODE = "WZSB";

	@Override
	public int insertMaterialDeclareDepartment(JSONObject param) {
		int result = 0 ;
	    if (StringUtils.isNotBlank(param.getString("departmentId"))) {
	    	result = materialDeclareDao.updateMaterialDeclareDepartment(param);
		}else {
			param.put("departmentId", UUIDUtil.getUUID());
			result = materialDeclareDao.insertMaterialDeclareDepartment(param);
		}
		return result;
	}

	@Override
	public int deleteMaterialDeclareDepartment(JSONObject param) {
		Integer cnt = materialDeclareDao.getDepartmentCnt(param);
		if (cnt > 0 ) {
			return materialDeclareDao.deleteMaterialDeclareDepartment2(param);
		}else {
			return materialDeclareDao.deleteMaterialDeclareDepartment(param);
		}
	}

	@Override
	public List<JSONObject> getMaterialDeclareDepartment(JSONObject param) {
		 
		return materialDeclareDao.getMaterialDeclareDepartment(param);
	}

	@Override
	public JSONObject getMaterialDeclareDetail(JSONObject param) {
		 
		return materialDeclareDao.getMaterialDeclareDetail(param);
	}

	@Override
	public int insertMaterialDeclare(JSONObject param) {
		 
		String applicationId = param.getString("applicationId");
		JSONArray item = param.getJSONArray("items");
		param.put("status", "1");
		List<JSONObject> itemList = new ArrayList<JSONObject>();
        BigDecimal sum = new BigDecimal("0.0");
        List<JSONObject> procedureList = new ArrayList<JSONObject>();
        List<JSONObject> menberList = new ArrayList<JSONObject>();
        if(param.get("count") == null) {
			param.put("count", 3);
		}
        
		if (StringUtils.isNotBlank(applicationId)) {
			for (int i = 0; i < item.size() ; i++) {
				JSONObject elem = item.getJSONObject(i);
				elem.put("applicationId", applicationId);
				elem.put("schoolId", param.getString("schoolId"));
				elem.put("seq", i + 1 );
				BigDecimal aBigDecimal = new BigDecimal(elem.getString("cnt"));
				BigDecimal bBigDecimal = new BigDecimal(elem.getString("unitPrice"));
				sum = sum.add(aBigDecimal.multiply(bBigDecimal));
				itemList.add(elem);
			}
			param.put("total", sum.doubleValue());
			JSONObject auditMenber = materialDeclareDao.getMaterialDeclareAuditMenberByTotal(param);
			if (auditMenber==null  ) {
				return 0;
			}else {
				JSONArray levels = auditMenber.getJSONArray("auditorLevel");
				if (levels !=null) {
					for (int i = 0; i < levels.size(); i++) {
						JSONObject elem = new JSONObject();
						int levelNum = levels.getJSONObject(i).getIntValue("levelNum");
						elem.put("levelNum", levelNum);
						elem.put("applicationId", applicationId);
						JSONArray auditors = levels.getJSONObject(i).getJSONArray("auditors");
						if (auditors != null) {
							for (int j = 0; j < auditors.size(); j++) {
								JSONObject elem2 = new JSONObject();
								elem2.put("applicationId", applicationId);
								elem2.put("levelNum", levelNum);
								elem2.put("teacherId", auditors.getJSONObject(j).getString("teacherId"));
								menberList.add(elem2);
							}
						}else {
							return 0;
						}
						if (i == 0) {
							elem.put("status", 1);// 第一级时候 为审核中
						}else {
							elem.put("status", 0);// 其他为待审核
						}
						procedureList.add(elem);
					}
				}
			 
			}
			materialDeclareDao.deleteMaterialDeclareProcedure(param);
			materialDeclareDao.deleteMaterialDeclareProcedureMenber(param);
			materialDeclareDao.deleteMaterialDeclare(param);
			materialDeclareDao.deleteMaterialDeclareItemdetail(param);
			
			materialDeclareDao.insertMaterialDeclareProcedureMenber(menberList);
			materialDeclareDao.insertMaterialDeclareProcedure(procedureList);
			materialDeclareDao.insertMaterialDeclareItemdetail(itemList);
			materialDeclareDao.insertMaterialDeclare(param);
			
		}else {
			  applicationId = UUIDUtil.getUUID();
			param.put("applicationId", applicationId);
			for (int i = 0; i < item.size() ; i++) {
				JSONObject elem = item.getJSONObject(i);
				elem.put("applicationId", applicationId);
				elem.put("schoolId", param.getString("schoolId"));
				elem.put("seq", i + 1 );
				BigDecimal aBigDecimal = new BigDecimal(elem.getString("cnt"));
				BigDecimal bBigDecimal = new BigDecimal(elem.getString("unitPrice"));
				sum = sum.add(aBigDecimal.multiply(bBigDecimal));
				itemList.add(elem);
			}
			param.put("total", sum.doubleValue());
		 
			JSONObject auditMenber = materialDeclareDao.getMaterialDeclareAuditMenberByTotal(param);
			if (auditMenber==null  ) {
				return 0;
			}else {
				JSONArray levels = auditMenber.getJSONArray("auditorLevel");
				if (levels !=null) {
					for (int i = 0; i < levels.size(); i++) {
						JSONObject elem = new JSONObject();
						int levelNum = levels.getJSONObject(i).getIntValue("levelNum");
						elem.put("levelNum", levelNum);
						elem.put("applicationId", applicationId);
						JSONArray auditors = levels.getJSONObject(i).getJSONArray("auditors");
						if (auditors != null) {
							for (int j = 0; j < auditors.size(); j++) {
								JSONObject elem2 = new JSONObject();
								elem2.put("applicationId", applicationId);
								elem2.put("levelNum", levelNum);
								elem2.put("teacherId", auditors.getJSONObject(j).getString("teacherId"));
								menberList.add(elem2);
							}
						}else {
							return 0;
						}
						
						if (i == 0) {
							elem.put("status", 1);
						}else {
							elem.put("status", 0);
						}
						procedureList.add(elem);
					}
				}
			 
			}
			
			materialDeclareDao.insertMaterialDeclareProcedureMenber(menberList);
			materialDeclareDao.insertMaterialDeclareProcedure(procedureList);
			materialDeclareDao.insertMaterialDeclareItemdetail(itemList);
			materialDeclareDao.insertMaterialDeclare(param);
			
		}
		
		// 发送kafka消息模板
		JSONObject params = new JSONObject();
		params.put("applicationId", applicationId);
		param.put("status", 0);
		param.put("desc", param.getString("reason"));
		Result<JSONObject> result = new Result<>();
		this.sendMsg(param, result);
		System.out.println("物资申请："+result);
		return 1;
	}

	@Override
	public int deleteMaterialDeclare(JSONObject param) {
		int result = 0;
		result = materialDeclareDao.deleteMaterialDeclareProcedure(param);
		result = materialDeclareDao.deleteMaterialDeclareProcedureMenber(param);
		result = materialDeclareDao.deleteMaterialDeclare(param);
		result = materialDeclareDao.deleteMaterialDeclareItemdetail(param);
		return result;
	}
 
	@Override
	public int insertMaterialDeclareAuditMenber(List<JSONObject> list) {
		 
		return materialDeclareDao.insertMaterialDeclareAuditMenber(list);
	}

	@Override
	public int deleteMaterialDeclareAuditMenber(JSONObject param) {
		 
		return materialDeclareDao.deleteMaterialDeclareAuditMenber(param);
	}

	@Override
	public List<JSONObject> getMaterialDeclareAuditMenber(JSONObject param) {
		 
		return materialDeclareDao.getMaterialDeclareAuditMenber(param);
	}

	@Override
	public int updateMaterialDeclareProcedure(JSONObject param) {
		String status = param.getString("status");
		 JSONObject teacherLeavel = materialDeclareDao.getMaterialDeclareProcedureTeacherLevelNum(param);
		 if (teacherLeavel==null) {
			return 0 ;
		 }
		 param.put("levelNum", teacherLeavel.getInteger("levelNum"));
		 param.put("processDate", new Date());
		 if ("3".equals(status)) {
			 materialDeclareDao.updateMaterialDeclareProcedure(param);
			 materialDeclareDao.updateMaterialDeclareStatus(param);
		 }else if ("2".equals(status)) {
			 materialDeclareDao.updateMaterialDeclareProcedure(param);// 1==>2
			 param.put("levelNum", teacherLeavel.getInteger("levelNum") + 1);
			 JSONObject object = materialDeclareDao.getMaterialDeclareProcedureLevelNum(param);// 获取下一级
			 if (object==null) {
				 materialDeclareDao.updateMaterialDeclareStatus(param);// 最后一级 
			 }else {
				 param.put("processDate", null);
				 param.put("status", 1);
				 materialDeclareDao.updateMaterialDeclareProcedure(param);//更新审批下一级 0 - 1 
			}
		 }
		
		// 发送kafka消息模板suggestion
		JSONObject params = new JSONObject();
		params.put("applicationId", param.getString("applicationId"));
		param.put("status", Integer.parseInt(param.getString("status")));
		param.put("desc", param.getString("suggestion"));
		this.sendMsg(param, new Result<JSONObject>());
		 
		return 1;
	}
	
	@Override
	public JSONObject updateMaterialDeclareProcedureNew(JSONObject param) {
		JSONObject result = new JSONObject();
		
		String status = param.getString("status");
		JSONObject teacherLeavel = materialDeclareDao.getMaterialDeclareProcedureTeacherLevelNum(param);
		if (teacherLeavel == null) {
			result.put("code",2);
			result.put("msg", "当前身份没有这次数据");
			return result;
		}
		
		param.put("levelNum", teacherLeavel.getInteger("levelNum"));
		param.put("processDate", new Date());
		if ("3".equals(status)) {
			materialDeclareDao.updateMaterialDeclareProcedure(param);
			materialDeclareDao.updateMaterialDeclareStatus(param);
		} else if ("2".equals(status)) {
			materialDeclareDao.updateMaterialDeclareProcedure(param);// 1==>2
			param.put("levelNum", teacherLeavel.getInteger("levelNum") + 1);
			JSONObject object = materialDeclareDao.getMaterialDeclareProcedureLevelNum(param);// 获取下一级
			if (object == null) {
				materialDeclareDao.updateMaterialDeclareStatus(param);// 最后一级
			} else {
				param.put("processDate", null);
				param.put("status", 1);
				materialDeclareDao.updateMaterialDeclareProcedure(param);// 更新审批下一级 0 - 1
			}
		}

		// 发送kafka消息模板suggestion
		JSONObject params = new JSONObject();
		params.put("applicationId", param.getString("applicationId"));
		param.put("status", Integer.parseInt(param.getString("status")));
		param.put("desc", param.getString("suggestion"));
		this.sendMsg(param, new Result<JSONObject>());
		
		result.put("code",1);
		result.put("msg", "success");
		return result;
	}

	@Override
	public JSONObject getAuditMaterialDeclare(JSONObject param) {
		 
		return materialDeclareDao.getAuditMaterialDeclare(param);
	}

	@Override
	public List<JSONObject> getMaterialDeclareStatistics(JSONObject param) {
		 
		return materialDeclareDao.getMaterialDeclareStatistics(param);
	}

	@Override
	public List<JSONObject> getAllTeacherList(JSONObject param) {
		List<JSONObject> rList = new ArrayList<JSONObject>();
		
		Long schoolId = param.getLong("schoolId");
		String termInfoId = param.getString("selectedSemester");
		School school = commonDataService.getSchoolById(schoolId, termInfoId);
		String teacherName = param.getString("teacherName");
		if(teacherName == null) {
			teacherName = "";
		}
		List<Account> tList = commonDataService.getAllSchoolEmployees(school, termInfoId, teacherName.trim());
		for (Account account : tList) {
			if(StringUtils.isBlank(account.getName())) {
				continue;
			}
			JSONObject line = new JSONObject();
			line.put("teacherId", account.getId());
			line.put("teacherName", account.getName());
			rList.add(line);
		}
		return rList;
 
	}

	@Override
	public Integer getAdminMaterialDeclareCnt(JSONObject param) {
		 
		return materialDeclareDao.getAdminMaterialDeclareCnt(param);
	}

	@Override
	public Integer getApplayMaterialDeclareCnt(JSONObject param) {
		 
		return  materialDeclareDao.getApplayMaterialDeclareCnt(param);
	}

	@Override
	public Integer getAuditMaterialDeclareCnt(JSONObject param) {
		 
		return  materialDeclareDao.getAuditMaterialDeclareCnt(param);
	}

	@Override
	public Integer getAuditedMaterialDeclareCnt(JSONObject param) {
		 
		return materialDeclareDao.getAuditedMaterialDeclareCnt(param);
	}

	@Override
	public List<JSONObject> getAdminMaterialDeclareList(JSONObject param) {
	 
		return materialDeclareDao.getAdminMaterialDeclareList(param);
	}

	@Override
	public List<JSONObject> getApplayMaterialDeclareList(JSONObject param) {
		 
		return materialDeclareDao.getApplayMaterialDeclareList(param);
	}

	@Override
	public List<JSONObject> getAuditMaterialDeclareList(JSONObject param) {
		 
		return materialDeclareDao.getAuditMaterialDeclareList(param);
	}

	@Override
	public List<JSONObject> getAuditedMaterialDeclareList(JSONObject param) {
	 
		return materialDeclareDao.getAuditedMaterialDeclareList(param);
	}

	@Override
	public Integer getHasSetMember(JSONObject param) {
		 
		return materialDeclareDao.getHasSetMember(param);
	}

	@Override
	public List<JSONObject> getMaterialDeclareExportList(List<JSONObject> datas) {

		List<JSONObject> tablelist = new ArrayList<JSONObject>();

			JSONObject temp = new JSONObject();			
			String title = "物资申报统计";
 
			JSONObject excelTitle = new JSONObject();
			excelTitle.put("title", title);
			excelTitle.put("align", "center");
			excelTitle.put("colspan", 3);
 
			JSONArray tableHead = new JSONArray();	
			JSONArray headsWeek = new JSONArray();
 
  
				JSONObject headI = new JSONObject();
				headI.put("field", "row_0_0");
				headI.put("title", "申报部门");
				headI.put("boxWidth", 200);
				headI.put("deltaWidth", 9);
				headI.put("auto", true);
				headI.put("align", "center");
				headI.put("width", 120);	
				headsWeek.add(headI);
 	
				headI = new JSONObject();
				headI.put("field", "row_0_1");
				headI.put("title", "申报金额总计/元");
				headI.put("boxWidth", 200);
				headI.put("deltaWidth", 9);
				headI.put("auto", true);
				headI.put("align", "center");
				headI.put("width", 120);
				headsWeek.add(headI);
				
				headI = new JSONObject();
				headI.put("field", "row_0_2");
				headI.put("title", "审批通过金额总计/元");
				headI.put("boxWidth", 200);
				headI.put("deltaWidth", 9);
				headI.put("auto", true);
				headI.put("align", "center");
				headI.put("width", 120);
				headsWeek.add(headI);
				
				
			 
			tableHead.add(headsWeek);
			JSONArray tableDatas = new JSONArray();	
			for(int k = 0; k <datas.size(); k++) {	
				JSONObject object = datas.get(k);
				JSONObject dataOne = new JSONObject();
				dataOne.put("row_0_0", object.getString("departmentName"));
				dataOne.put("row_0_1", object.getDouble("applyTotal"));
				dataOne.put("row_0_2", object.getDouble("auditTotal"));
				tableDatas.add(dataOne);
			}
			JSONArray excelTails = new JSONArray();
			List<String> tails = new ArrayList<String>();
			tails.add("");// 空一行
			for(int l = 0; l < tails.size();l++){
				JSONArray tailsArray = new JSONArray();
				JSONObject tailObj = new JSONObject();
				tailObj.put("title", tails.get(l));
				tailObj.put("colspan", 1);
				tailsArray.add(tailObj);
				excelTails.add(tailsArray);
			}		

			temp.put("excelTitle", excelTitle);	
			temp.put("tableHead", tableHead);	
			temp.put("tableData", tableDatas);
			temp.put("excelTail", excelTails);
			tablelist.add(temp);
	 
		return tablelist;
	
 
	}

	@Override
	public List<JSONObject> getItemDetailById(String applicationId) {
		return materialDeclareDao.getItemDetailById(applicationId);
	}

	@Override
	public List<JSONObject> getProcedureMember(String applicationId) {
		return materialDeclareDao.getProcedureMember(applicationId);
	}

	@Override
	public List<JSONObject> getProcedure(String applicationId) {
		return materialDeclareDao.getProcedure(applicationId);
	}

	@Override
	public JSONObject getApplicationById(String applicationId) {
		return materialDeclareDao.getApplicationById(applicationId);
	}

	@Override
	public void sendMsg(JSONObject param, Result<JSONObject> result) {
		/**
		 * 1. 根据applicationId获取老师申请物资详情
		 * 2. 获取这次审批流程，根据审批结果判断是否流转下一个审批环节
		 * 3. 根据status判断审批状态： status：1审核中 2 同意 3 不同意 0审核中（可以删除）
		 */
		String applicationId = param.getString("applicationId");
		int status = param.getIntValue("status");
		String desc = param.getString("desc");
		
		JSONObject params = new JSONObject();
		params.put("applicationId", applicationId);
		
		// 获取application详情 t_md_application
		JSONObject appliationObj = materialDeclareDao.getApplicationById(applicationId);
		if(appliationObj == null || appliationObj.isEmpty()) {
			result.fail(applicationId+"数据不存在!");
			return;
		}
		
		// 申请人
		String applyTeacherId = appliationObj.getString("teacherId");
		
		// 获取applicationItem详情 t_md_itemdetail
		List<JSONObject> materialDetials = materialDeclareDao.getItemDetailById(applicationId);
		String msgTitle = "";
		if(materialDetials == null || materialDetials.size() == 0) {
			result.fail("数据不存在!");
			return;
		}
		
		// 获取老师所申请的所有物品
		for(JSONObject obj : materialDetials) {
			if(StringUtils.isBlank(msgTitle))
				msgTitle = obj.getString("productName");
			else
				msgTitle += ","+obj.getString("productName");
		}
		
		// 获取下一步审批人 t_md_procedure 根据status=1和levelNum进行判断该环节到了那一步。
		List<JSONObject> produceMemberList = materialDeclareDao.getProcedureMember(applicationId);
		if(materialDetials == null || materialDetials.size() == 0) {
			 result.fail("当前审批人数据不存在!");
			 return;
		}
		
		List<JSONObject> produceList = materialDeclareDao.getProcedure(applicationId);
		if(materialDetials == null || materialDetials.size() == 0) {
			 result.fail("当前审批流程数据不存在!");
			 return;
		}
		
		// 当前所处环节
		int level = 0;
		status = status == 0 ? 1 : status;
		// 当前审批人的teacherId
		boolean isLastProval = false;
		// 下一个审批人teacherId
		// long netTeacherId = 0l;
		
		// 当前处在的审批流程
		String processTeacherId = "";
		for(JSONObject produce : produceList) {
			if(produce.getIntValue("status") == 1) {
				level = produce.getIntValue("levelNum");
			}
		}
		
		// 获取schoolExtId
		long schoolId = Long.parseLong(appliationObj.getString("schoolId"));
		String TermInfoId = commonDataService.getCurTermInfoId(schoolId);
		School school = commonDataService.getSchoolById(schoolId, TermInfoId);

		// 审批最后环节
		if(produceList.size() == 1 && status != 1) {
			isLastProval = true;
		}
		
		Account account = commonDataService.getAccountById(schoolId, Long.parseLong(applyTeacherId), TermInfoId);
		JSONArray receivers = new JSONArray();
		if(isLastProval && status !=1 ) {
			JSONObject msgCenterReceiver = new JSONObject();
			msgCenterReceiver.put("userId", account.getExtId());
			msgCenterReceiver.put("userName", account.getName());
			receivers.add(msgCenterReceiver);
		}else {
			for(JSONObject member : produceMemberList) {
				if(member.getIntValue("levelNum") == level) {
					Account account_ = commonDataService.getAccountById(schoolId, member.getLongValue("teacherId"), TermInfoId);
					
					JSONObject msgCenterReceiver = new JSONObject();

					msgCenterReceiver.put("userId", account_.getExtId());
					msgCenterReceiver.put("userName", account_.getName());
					receivers.add(msgCenterReceiver);
				}
			}
		}
		
		// 组装消息体，发送消息
		JSONObject msgBody = new JSONObject();
		
		String title = "你收到一条新的物资申报审核单！";
		String remark = "请点击详情进行审核！";
		
		// 审批不通过或是最后一个环节
		if(status == 3 || isLastProval) {
			title = "你提交的物资申报审核单有了新的批复！";
		}
		
		if(isLastProval) {
			remark = "请点击详情进行查看！";
		}
		
		JSONObject teacherParam = new JSONObject();
		teacherParam.put("teacherId", applyTeacherId);
		
		JSONObject msg = new JSONObject();
	    String msgId = UUIDUtil.getUUID().replace("-", "");
		msg.put("msgId", msgId);
		msg.put("msgTitle", title);
		msg.put("msgContent", "你收到一条新的物资申报审核单!");
		msg.put("msgUrlPc", "" + msgUrlPc);
		msg.put("msgUrlApp", "" + msgUrlApp+applicationId);
		msg.put("msgOrigin", "物资申报提醒");
		msg.put("msgTypeCode", MSG_TYPE_CODE);
		msg.put("msgTemplateType", MSG_TYPE_CODE);
		msg.put("schoolId", school.getExtId());
		msg.put("creatorName", account.getName());

		// 标题
		JSONObject first = new JSONObject();
		first.put("value",title);

		// 申请人
		JSONObject keyword1 = new JSONObject();
		keyword1.put("value", account.getName());
		
		// 申请物品
		JSONObject keyword2 = new JSONObject();
		keyword2.put("value", msgTitle);

		// 备注
		JSONObject keyword3 = new JSONObject();
		keyword3.put("value", desc);
		
		JSONObject remarkObj = new JSONObject();
		remarkObj.put("value", remark);
		
		JSONObject data = new JSONObject();
		data.put("first", first);
		data.put("keyword1", keyword1);
		data.put("keyword2", keyword2);
		data.put("keyword3", keyword3);
		data.put("remark", remarkObj);
		data.put("url", msgUrlApp+applicationId);
		msg.put("msgWxJson", data);
		
		msgBody.put("msg", msg);
		msgBody.put("receivers", receivers); 
		try {
			KafkaWXmsgThread kafka = new KafkaWXmsgThread(kafkaUrl, msgId, msgBody, MSG_TYPE_CODE, clientId, clientSecret);
			kafka.start();
			result.success();
			result.setData(msgBody);
		} catch (Exception e) {
			result.fail("数据异常!");
		}
	}
 
	@Override
	public JSONObject getSchoolById(JSONObject param) {
		return materialDeclareDao.getSchoolById(param);
	}

	@Override
	public int updateMaterialDeclareCount(JSONObject param) {
		return materialDeclareDao.updateMaterialDeclareCount(param);
	}
	
}
