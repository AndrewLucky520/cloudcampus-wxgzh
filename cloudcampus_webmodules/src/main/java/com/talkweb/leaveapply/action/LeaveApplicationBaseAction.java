package com.talkweb.leaveapply.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.leaveapply.service.LeaveApplicationService;
import com.talkweb.ueditor.action.UEditorAction;
import com.talkweb.utils.KafkaUtils;

public class LeaveApplicationBaseAction extends BaseAction {
	@Value("#{settings['clientId']}")
	private String clientId;

	@Value("#{settings['clientSecret']}")
	private String clientSecret;

	@Value("#{settings['kafkaUrl']}")
	private String kafkaUrl;
	
	@Value("#{settings['teacherleave.msgUrlPc']}")
	private String msgUrlPc;
	
	@Value("#{settings['teacherleave.msgUrlApp']}")
	private String msgUrlApp;
	
	private static final String MSG_TYPE_CODE = "JSQJ";
	//生产者ID，唯一标识。按自身业务来命名 (教师请假)
	//暂定与MSG_TYPE_CODE相同
	private static final String PRODUCER_ID = MSG_TYPE_CODE;
	
	protected Logger logger = LoggerFactory.getLogger(LeaveApplicationBaseAction.class);
	@Autowired
	protected LeaveApplicationService leaveApplicationService;
	@Autowired
	protected FileServer fileServerImplFastDFS;
	@Autowired
	protected AllCommonDataService commonDataService;
	
	protected ResourceBundle rbConstant = ResourceBundle.getBundle("constant.constant" );
	
	protected JSONObject getLeaveRole(JSONObject param){
		JSONObject response = new JSONObject();
		
		String schoolId = param.getString("schoolId") ;
		String termInfoId = param.getString("termInfoId");
		String accountId = param.getString("accountId");
		Account account = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(accountId), termInfoId);
		List<User> userList = account.getUsers();
		param.put("teacherId", accountId);
		if (userList!=null) {
			for (int i = 0; i < userList.size(); i++) {
				User user = userList.get(i);
				if (user.getUserPart().getRole() ==T_Role.Teacher ) {
					 response.put("isTeacher", 1);
					 break;
				}  
			}
		}
		
		response.put("isAdmin", param.getString("isAdmin"));

		List<JSONObject> list = leaveApplicationService.getAuditorList(param);
		int count = list.size();
		if(count>0){
			response.put("isAuditor", 1);
		}else {
			response.put("isAuditor", 0);
		}
		
		setPromptMessage(response, "0", "查询成功");
		
		return response;
	}
	
	protected void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	
	/**
     * 获取当前学校
     * @param  schoolId
     * @param  termInfoId:查询的学年学期
     * @return 学校实体
     * 
     */
    protected School getSchoolByUserId(long userId){
    	return commonDataService.getSchoolByUserId(0, userId);
    	/*School school = commonDataService.getSchoolById(Long.valueOf(schoolId),termInfoId);
    	return school;*/
    }
    
 	/** 获取教师子节点的基础信息*/
 	protected JSONObject getTeacherArray(String parentId,
 			List<Long> accountIdList,Map<Long,String> teacherMap,String selectedTeacherIds) {
 		JSONArray teacherArray = new JSONArray();
 		if (CollectionUtils.isNotEmpty(accountIdList)) {
 			for (long accountId : accountIdList) {
 				if(!StringUtils.isEmpty(selectedTeacherIds)&&selectedTeacherIds.contains(accountId+""))
 				{
 					continue;
 				}
 				 String teacherName = "";
				 if (teacherMap.containsKey(accountId)){
					 teacherName = teacherMap.get(accountId); 
				 }
				 JSONObject temp = new JSONObject();
				 temp.put("id", parentId + accountId);
				 temp.put("text", teacherName);
 	
 				//temp.put("checked", "false");
 				JSONObject attibute = new JSONObject();
 				attibute.put("teacherId", accountId + "");
 				if (StringUtils.isBlank(teacherName)) {
					continue;
				}
 				attibute.put("teacherName", teacherName);
 				temp.put("attributes", attibute);
 				teacherArray.add(temp);
 			}
 		}
 		JSONObject result = new JSONObject();
 		if (teacherArray.size() > 0) {
 			result.put("isEmpty", "false");
 			result.put("teachers", teacherArray);
 		} else {
 			result.put("isEmpty", "true");
 		}
 		return result;
 	} 
	
	protected  String[][] getTeacherIdNames(School school,String selectedSemester){
		List<Account> ll = commonDataService.getAllSchoolEmployees(school,selectedSemester,"");
		String[][] teacherIdName = new String[2][ll.size()];
		for(int i = 0;i<ll.size();i++){
			String teacherId = String.valueOf(ll.get(i).getId());
			String teacherName = ll.get(i).getName();
			teacherIdName[0][i] = teacherId;
			teacherIdName[1][i] = teacherName;
		}
		return teacherIdName;
	}
	
	/**
	 * 返回字符串在数组中的位置
	 * @param string
	 * @param arr
	 * @return -1 不在数组中
	 */
	protected int indexInArray(String string, String[] arr) {
		String source = string.replace(" ", "");				
		int rs = -1;
		for (int i = 0; i < arr.length; i++) {
			String target = (arr[i] == null?"":arr[i]);
			target = target.replace(" ", "");
			if (target.equalsIgnoreCase(source)) {
				rs = i; break;
			}
		}
		return rs;
	}
	
	protected String replaceFileName(String fileName) {
		 fileName = fileName.replace("%28", "(");
		 fileName = fileName.replace("%29", ")");
		 fileName = fileName.replace("%7E", "~");
		 fileName = fileName.replace("%21", "!");
		 fileName = fileName.replace("%40", "@");
		 return fileName;
	}
	
	protected JSONObject isSetProcedure(JSONObject param){
		JSONObject response = new JSONObject();
		List<JSONObject> list = leaveApplicationService.getAuditorList(param);
		
		if(list==null || list.size() == 0){
			response.put("isSet", "0");
		}else{
			response.put("isSet", "1");
		}
		setPromptMessage(response , "0" , "成功");
		return response;
	}
	
	protected JSONObject getLeaveTypeList(JSONObject param){
		JSONObject response = new JSONObject();
		List<JSONObject>  list = leaveApplicationService.getLeaveTypeList(param);
		if(list==null || list.size() == 0){
			param.put("schoolId", "ALL");
			list = leaveApplicationService.getLeaveTypeList(param);
		}
		response.put("data", list);
		setPromptMessage(response , "0" , "成功");
		return response;
	}
	
	protected JSONObject deleteLeaveApply(JSONObject param){
		JSONObject response = new JSONObject();
		try {
			leaveApplicationService.delProcedure(param);//删除审核流程
			leaveApplicationService.deleteLeaveApplyProcedureMember(param);
			leaveApplicationService.delLeaveApplyFile(param);//删除申请记录
			leaveApplicationService.delLeaveApplyRecord(param);//删除申请文件
			
		} catch (Exception e) {
			setPromptMessage(response , "-1" , "删除失败");
			return response;
		}
		
		setPromptMessage(response , "0" , "成功");
		return response;
	}
	 
	protected JSONObject uploadFile(MultipartFile file) {
		JSONObject response = new JSONObject();
		if (file!=null) {
			 File df = null;
				try {
					String fileName = file.getOriginalFilename();
					String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
					fileName = fileName.substring(0,fileName.lastIndexOf(".")+1) +suffix;
					String tempName0 = UUID.randomUUID().toString()+"."+suffix;
					df = new File(tempName0);
					file.transferTo(df);
					String fileId = fileServerImplFastDFS.uploadFile(df,tempName0);
					if (StringUtils.isNotBlank(fileId)) {
						response.put("appFileName", fileName);
						response.put("fileUrl", fileId);
						setPromptMessage(response, "0", "上传成功");
					}else{
						setPromptMessage(response, "-3", "文件上传出现问题,请联系管理员!");
					}
				} catch (Exception e) {
					setPromptMessage(response, "-3", "文件上传出现问题,请联系管理员!");
					e.printStackTrace();
				}finally {
					if(df!=null)df.delete();
				}
		}
		return response;
	}
	
	protected JSONObject deleteFile(JSONObject request) {
		JSONObject response = new JSONObject();
		String url = request.getString("fileUrl");
		try {
			if (StringUtils.isNotBlank(url)) {
				fileServerImplFastDFS.deleteFile(url);
				setPromptMessage(response, "0", "删除成功!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "0", "删除失败!");
		}
		return response;
	}
	
	protected void downloadFile(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
		String urlTemp = req.getParameter("fileUrl");
		String originTemp = req.getParameter("appFileName");
		String url = URLDecoder.decode(urlTemp, "UTF-8");

		if (originTemp!=null) {
			String encode = UEditorAction.getEncoding(originTemp); 
			originTemp = new String(originTemp.getBytes(encode),"UTF-8");
		}
 
		ByteArrayInputStream bis = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bis1 = null;
		File temp = null;
		try {
				String fileName = originTemp;
				fileName = fileName.replace(" ", "");
				fileServerImplFastDFS.downloadFile(url, fileName);
				temp = new File(fileName);
				// 写入数据结束
				res.setContentType("octets/stream");
				String downLoadName = String.valueOf(URLEncoder.encode(fileName, "UTF-8"));
				downLoadName = replaceFileName(downLoadName);
				System.out.println(downLoadName);
				res.addHeader("Content-Disposition", "attachment;filename=".concat(downLoadName ));
				bis1 = new BufferedInputStream(new FileInputStream(temp));
				bos = new BufferedOutputStream(res.getOutputStream());
				byte[] buff = new byte[2048];
				int bytesRead;
				while (-1 != (bytesRead = bis1.read(buff, 0, buff.length))) {
					bos.write(buff, 0, bytesRead);
				}
				bos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (bis1 != null)
				try {
					bis1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if(temp!=null){
				temp.delete();
			}
		}
	}
	
	protected JSONObject updateProcedure(@RequestBody JSONObject request,  HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = request;
		int status = request.getInteger("status");

		String accountId = request.getString("accountId");
		param.put("processDate", new Date());
		param.put("auditor", accountId);// 处理人
		param.put("accountId", accountId);
		
		int levelNum = 0;
		JSONObject levelNumJson = leaveApplicationService.getLeaveApplyprocedureLevelByTeacherId(param);
		if(levelNumJson == null || levelNumJson.getString("levelNum") == null) {
			setPromptMessage(response, "-1", "没有对应审批级别，无法审批");
			return response;
		} else {
			levelNum = Integer.valueOf(levelNumJson.getString("levelNum"));
		}
		param.put("levelNum", levelNum);
		
		//设置消息接收人列表(下级审批)
		List<JSONObject> msgReceiversArray = null;
		String schoolId = request.getString("schoolId");
		if(schoolId == null || schoolId.length() == 0) {
			setPromptMessage(response, "-1", "参数错误:schoolId不能为空。");
			return response;
		}
		param.put("schoolId", schoolId);
		JSONObject applyData = leaveApplicationService.getLeaveApply(param);
		if(applyData == null) {
			setPromptMessage(response, "-1", "没有对应审批详情数据，无法审批");
			return response;
		}
		boolean isComplete = false;//是否为审批完成
		String curTermInfoId = param.getString("curTermInfoId");
		
		try {
			leaveApplicationService.updateProcedure(param);
			if (status == -1) {// 不同意
				leaveApplicationService.updateLeaveApplyStatus(param);
				setPromptMessage(response, "0", "审核成功");
				isComplete = true;
				//申请人accountId
				String applyUserId = applyData.getString("applyUserId");
				msgReceiversArray = getMsgReceiversArrayForUser(applyUserId,schoolId,curTermInfoId);
				
			} else {
				// 寻找下级审批
				int nextLevelNum = levelNum + 1;
				boolean hasNext = false;
				JSONArray auditDetail = applyData.getJSONArray("auditDetail");
				if(auditDetail != null) {
					for(int i=0; i<auditDetail.size(); i++) {
						JSONObject auditData = auditDetail.getJSONObject(i);
						if(auditData.getInteger("levelNum") == nextLevelNum) {
							//寻找到下一级
							hasNext = true;
							param.put("levelNum", nextLevelNum);
							param.put("status", 1);
							leaveApplicationService.updateProcedureStatus(param);// 寻找到下一级 更新为审批中
							setPromptMessage(response, "0", "审核成功");
							
							//下一级审批者列表
							msgReceiversArray = getMsgReceiversArray(nextLevelNum,auditDetail,schoolId,curTermInfoId);
							break;
						}
					}
				}
				
				if(!hasNext) {
					// 无最后审批的级数
					leaveApplicationService.updateLeaveApplyStatus(param);
					setPromptMessage(response, "0", "请假成功");
					isComplete = true;
					//申请人accountId
					String applyUserId = applyData.getString("applyUserId");
					msgReceiversArray = getMsgReceiversArrayForUser(applyUserId,schoolId,curTermInfoId);
				}
			}
		} catch (Exception e) {
			setPromptMessage(response, "-1", "设置失败");
			return response;
		}
		
		//发送模板消息
		if(msgReceiversArray != null && msgReceiversArray.size() > 0) {
			//存在接收者列表时
			JSONObject paramMsg = new JSONObject();
			//设置消息接受者列表
			paramMsg.put("receivers", msgReceiversArray);
			
			int leaveStartTimeAMPM = applyData.getIntValue("leaveStartTimeAMPM");
			int leaveEndTimeAMPM = applyData.getIntValue("leaveEndTimeAMPM");
			Date leaveStartTime = applyData.getDate("leaveStartTime");
			Date leaveEndTime = applyData.getDate("leaveEndTime");
			double totalDays = applyData.getDouble("leavedays");
			List<Long>  accountIds =  new  ArrayList<Long>();
			accountIds.add(Long.valueOf(accountId));
			//审批人
			List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, curTermInfoId);
			
			//请假人
			//请假人accountId
			String applyUserId = applyData.getString("applyUserId");
			accountIds.clear();
			accountIds.add(Long.valueOf(applyUserId));
			List<Account> accountsApply = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, curTermInfoId);
			
			if(isComplete) {
				/**
				 * 通知申请人
				 */
				paramMsg.put("first", "你的请假申请得到批复！");
				paramMsg.put("remark", "请及时查阅！");
				
				//设置审批人
				paramMsg.put("auditor",  accounts.get(0).name);
				
				//设置审批结果
				String applyStatus = "未定义";
				switch(status) {
				case -1:
					applyStatus = "不同意";
					break;
				case 1:
					applyStatus = "审批中";
					break;
				case 2:
					applyStatus = "同意";
					break;
				}
				paramMsg.put("auditStatus", applyStatus);
				
				//设置模板类型
				paramMsg.put("msgTemplateType", "JSQJ2");
				
			} else {
				/**
				 * 通知下一级
				 */
				paramMsg.put("first", "你收到一条新的教师请假审核单！");
				paramMsg.put("remark", "请点击详情进行审核！");
				
				//设置请假天数
				paramMsg.put("leavedays", ""+totalDays + "天");
				
				//设置模板类型
				paramMsg.put("msgTemplateType", "JSQJ1");
			}
			
			//设置请假人姓名
			paramMsg.put("applyName", accountsApply.get(0).name);
			
			//设置请假类型
			String leaveTypeName = getLeaveTypeName(applyData.getString("leaveType"));
			paramMsg.put("leaveType", leaveTypeName);
			
			//设置请假时间
			String startAMPM = leaveStartTimeAMPM == 0 ? "上午" : "下午";
			String endAMPM = leaveEndTimeAMPM == 0 ?  "上午" : "下午";
			String leaveTime = leaveStartTime.toString() + " " + startAMPM + "至" + leaveEndTime.toString() + " " + endAMPM;
			paramMsg.put("leaveTime", leaveTime);
			
			//设置请假理由
			paramMsg.put("reason", applyData.getString("reason"));
			
			//创建者（自己）
			paramMsg.put("creatorName", accounts.get(0).name);
			
			//设置学校ID
			School school = commonDataService.getSchoolById(Long.valueOf(schoolId), curTermInfoId);
			if(school != null) {
				paramMsg.put("schoolId", school.getExtId());
			}
			
			paramMsg.put("applicationId", request.getString("applicationId"));
			
			if(!sendAppMsg(paramMsg)) {
				setPromptMessage(response, "1", "发送通知失败！");
				return response;
			}
		}
		
		return response;
	}
	
	protected void checkApplyStartEndDate(JSONObject request) {
		String applyStart = request.getString("applyStart");
		if(applyStart != null && applyStart.length() > 0) {
			applyStart += " 00:00:00";
			request.put("applyStart", applyStart);
		}
		String applyEnd = request.getString("applyEnd");
		if(applyEnd != null && applyEnd.length()>0) {
			applyEnd += " 23:59:59";
			request.put("applyEnd", applyEnd);
		}
	}
	
	/**
	 * 设置请假天数
	 * @param param
	 * @param response
	 * @return
	 */
	protected boolean setLeaveDays(JSONObject param,JSONObject response) {
		String schoolId = param.getString("schoolId");
		Date leaveStartTime = param.getDate("leaveStartTime");
		long lStart = leaveStartTime.getTime();
		Date leaveEndTime = param.getDate("leaveEndTime");
		long lEnd = leaveEndTime.getTime();
		int leaveStartTimeAMPM = param.getIntValue("leaveStartTimeAMPM");
		int leaveEndTimeAMPM = param.getIntValue("leaveEndTimeAMPM");
		
		param.put("schoolId", "ALL");
		List<JSONObject> festivallist = leaveApplicationService.getFestivalList(param);
		HashMap<Date, Integer> festivalMap = new HashMap<Date, Integer>();
		HashMap<Date, String> festival2Map = new HashMap<Date, String>();
		for (int i = 0; i < festivallist.size(); i++) {
			JSONObject object = festivallist.get(i);
			festivalMap.put(object.getDate("festivalDay"), object.getInteger("onduty"));
			festival2Map.put(object.getDate("festivalDay"), object.getString("ampm"));
		}
		param.put("schoolId", schoolId);
		festivallist = leaveApplicationService.getFestivalList(param);
		for (int i = 0; i < festivallist.size(); i++) {
			JSONObject object = festivallist.get(i);
			festivalMap.put(object.getDate("festivalDay"), object.getInteger("onduty"));
			festival2Map.put(object.getDate("festivalDay"), object.getString("ampm"));
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(leaveStartTime);
		double totalDays = 0;

		while (true) {
			
			if (cal.getTime().getTime() - leaveEndTime.getTime() > 0) {
				break;
			}

			/**
			 * 周六日为上班时，onduty为1
			 * 平日设置了休假时，onduty为0，且ampm为0,1,2
			 */
			Integer onduty = festivalMap.get(cal.getTime());// 上下班标志
			String ampm = festival2Map.get(cal.getTime());// 上下午标志
			long lNow = cal.getTime().getTime();

			//是周六日
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				if(onduty!= null && onduty == 1) {
					//如果周六日是上班
					if (lNow == lStart) {// 当前日期等于开始时间
						if (lNow == lEnd) {// 当前日期等于结束日期
							if (leaveStartTimeAMPM == 0) {
								if (leaveEndTimeAMPM == 0) {
//									totalDays = totalDays + 0.5;
									if ("0".equals(ampm)) {//全天上班
										 totalDays = totalDays + 0.5 ;
									 }else if ("1".equals(ampm)) {//上午上班下午休息
										 totalDays = totalDays + 0.5 ;
									 }else if ("2".equals(ampm)) {//上午休息下午上班
										 //不统计
									 }
								} else {
//									totalDays = totalDays + 1;
									if ("0".equals(ampm)) {//全天上班
										 totalDays = totalDays + 1 ;
									 }else if ("1".equals(ampm)) {//上午上班下午休息
										 totalDays = totalDays + 0.5 ;
									 }else if ("2".equals(ampm)) {//上午休息下午上班
										 totalDays = totalDays + 0.5 ;
									 }
								}
							} else {
								if (leaveEndTimeAMPM == 1) {// 都是下午
//									totalDays = totalDays + 0.5;
									if ("0".equals(ampm)) {//全天上班
										 totalDays = totalDays + 0.5 ;
									 }else if ("1".equals(ampm)) {//上午上班下午休息
										 //不统计
									 }else if ("2".equals(ampm)) {//上午休息下午上班
										 totalDays = totalDays + 0.5 ;
									 }
								} else {
									setPromptMessage(response, "-1", "请假日期错误！");
//										return response;
									return false;
								}

							}
						} else {// 只考虑开始
							if (leaveStartTimeAMPM == 0) {
//								totalDays = totalDays + 1;
								if ("0".equals(ampm)) {//全天上班
									 totalDays = totalDays + 1 ;
								 }else if ("1".equals(ampm)) {//上午上班下午休息
									 totalDays = totalDays + 0.5 ;
								 }else if ("2".equals(ampm)) {//上午休息下午上班
									 totalDays = totalDays + 0.5 ;
								 }
							} else {
//								totalDays = totalDays + 0.5;
								if ("0".equals(ampm)) {//全天上班
									totalDays = totalDays + 0.5 ;
								}else if ("1".equals(ampm)) {//上午上班下午休息
									//不统计
								}else if ("2".equals(ampm)) {//上午休息下午上班
									totalDays = totalDays + 0.5 ;
								}
							}
						}
					} else {// 只考虑结束时间
						if (lNow == lEnd) {
							if (leaveEndTimeAMPM == 0) {
//								totalDays = totalDays + 0.5;
								if ("0".equals(ampm)) {//全天上班
									 totalDays = totalDays + 0.5 ;
								 }else if ("1".equals(ampm)) {//上午上班下午休息
									 totalDays = totalDays + 0.5 ;
								 }else if ("2".equals(ampm)) {//上午休息下午上班
									 //不统计
								 }
							} else {
//								totalDays = totalDays + 1;
								if ("0".equals(ampm)) {//全天上班
									 totalDays = totalDays + 1 ;
								 }else if ("1".equals(ampm)) {//上午上班下午休息
									 totalDays = totalDays + 0.5 ;
								 }else if ("2".equals(ampm)) {//上午休息下午上班
									 totalDays = totalDays + 0.5 ;
								 }
							}
						} else {
//							totalDays = totalDays + 1;
							if ("0".equals(ampm)) {//全天上班
								 totalDays = totalDays + 1 ;
							}else if ("1".equals(ampm)) {//上午上班下午休息
								 totalDays = totalDays + 0.5 ;
							}else if ("2".equals(ampm)) {//上午休息下午上班
								 totalDays = totalDays + 0.5 ;
							}
						}
					}
				}
			} else {// 不是周六日
				if (lNow == lStart) {// 当前日期等于开始时间
					if (lNow == lEnd) {// 当前日期等于结束日期
						if (leaveStartTimeAMPM == 0) {
							if (leaveEndTimeAMPM == 0) {
								if(onduty == null || onduty == 1) {
									if(onduty != null && onduty == 1) {
										if ("0".equals(ampm)) {//全天上班
											 totalDays = totalDays + 0.5 ;
										}else if ("1".equals(ampm)) {//上午上班下午休息
											 totalDays = totalDays + 0.5 ;
										}else if ("2".equals(ampm)) {//上午休息下午上班
											 //不统计
										}			
									} else {
										totalDays = totalDays + 0.5;
									}
								} else {
									if ("2".equals(ampm)) {// 下午休息
										totalDays = totalDays + 0.5;
									}
								}
							} else {
								if(onduty == null || onduty == 1) {
									if(onduty != null && onduty == 1) {
										if ("0".equals(ampm)) {//全天上班
											 totalDays = totalDays + 1 ;
										}else if ("1".equals(ampm)) {//上午上班下午休息
											 totalDays = totalDays + 0.5 ;
										}else if ("2".equals(ampm)) {//上午休息下午上班
											totalDays = totalDays + 0.5 ;
										}
									} else {
										totalDays = totalDays + 1;
									}
								} else {
									if ("0".equals(ampm)) {// 全天休息
										//不统计
									} else if ("1".equals(ampm)) {// 上午休息
										totalDays = totalDays + 0.5;
									} else if ("2".equals(ampm)) {// 下午休息
										totalDays = totalDays + 0.5;
									}
								}
							}
						} else {
							if (leaveEndTimeAMPM == 1) {// 都是下午
								if(onduty == null || onduty == 1) {
									if(onduty != null && onduty == 1) {
										if ("0".equals(ampm)) {//全天上班
											 totalDays = totalDays + 0.5 ;
										}else if ("1".equals(ampm)) {//上午上班下午休息
											 //不统计
										}else if ("2".equals(ampm)) {//上午休息下午上班
											totalDays = totalDays + 0.5 ;
										}
									} else {
										totalDays = totalDays + 0.5;
									}
								} else {
									if ("0".equals(ampm)) {// 全天休息
										//不统计
									} else if ("1".equals(ampm)) {// 上午休息
										totalDays = totalDays + 0.5;
									} else if ("2".equals(ampm)) {// 下午休息
										//不统计
									}
								}
							} else {
								setPromptMessage(response, "-1", "请假日期错误！");
//									return response;
								return false;
							}
						}
					} else {// 只考虑开始
						if (leaveStartTimeAMPM == 0) {
							if(onduty == null || onduty == 1) {
								if(onduty != null && onduty == 1) {
									if ("0".equals(ampm)) {//全天上班
										 totalDays = totalDays + 1 ;
									}else if ("1".equals(ampm)) {//上午上班下午休息
										totalDays = totalDays + 0.5 ;
									}else if ("2".equals(ampm)) {//上午休息下午上班
										totalDays = totalDays + 0.5 ;
									}
								} else {
									totalDays = totalDays + 1;
								}
							} else {
								if ("0".equals(ampm)) {// 全天休息
									//不统计
								} else if ("1".equals(ampm)) {// 上午休息
									totalDays = totalDays + 0.5;
								} else if ("2".equals(ampm)) {// 下午休息
									totalDays = totalDays + 0.5;
								}
							}
						} else {
							if(onduty == null || onduty == 1) {
								if(onduty != null && onduty == 1) {
									if ("0".equals(ampm)) {//全天上班
										 totalDays = totalDays + 0.5 ;
									}else if ("1".equals(ampm)) {//上午上班下午休息
										//不统计
									}else if ("2".equals(ampm)) {//上午休息下午上班
										totalDays = totalDays + 0.5 ;
									}
								} else {
									totalDays = totalDays + 0.5;
								}
							} else {
								if ("0".equals(ampm)) {// 全天休息
									//不统计
								} else if ("1".equals(ampm)) {// 上午休息
									totalDays = totalDays + 0.5;
								} else if ("2".equals(ampm)) {// 下午休息
									//不统计
								}
							}
						}
					}
				} else {// 只考虑结束时间
					if (lNow == lEnd) {
						if (leaveEndTimeAMPM == 0) {
							if(onduty == null || onduty == 1) {
								if(onduty != null && onduty == 1) {
									if ("0".equals(ampm)) {//全天上班
										 totalDays = totalDays + 0.5 ;
									}else if ("1".equals(ampm)) {//上午上班下午休息
										totalDays = totalDays + 0.5 ;
									}else if ("2".equals(ampm)) {//上午休息下午上班
										//不统计
									}
								} else {
									totalDays = totalDays + 0.5;
								}
							} else {
								if ("0".equals(ampm)) {// 全天休息
									//不统计
								} else if ("1".equals(ampm)) {// 上午休息
									//不统计
								} else if ("2".equals(ampm)) {// 下午休息
									totalDays = totalDays + 0.5;
								}
							}
						} else {
							if(onduty == null || onduty == 1) {
								if(onduty != null && onduty == 1) {
									if ("0".equals(ampm)) {//全天上班
										 totalDays = totalDays + 1 ;
									}else if ("1".equals(ampm)) {//上午上班下午休息
										totalDays = totalDays + 0.5 ;
									}else if ("2".equals(ampm)) {//上午休息下午上班
										totalDays = totalDays + 0.5 ;
									}
								} else {
									totalDays = totalDays + 1;
								}
							} else {
								if ("0".equals(ampm)) {// 全天休息
									//不统计
								} else if ("1".equals(ampm)) {// 上午休息
									totalDays = totalDays + 0.5;
								} else if ("2".equals(ampm)) {// 下午休息
									totalDays = totalDays + 0.5;
								}
							}
						}
					} else {
						if(onduty == null || onduty == 1) {
							if(onduty != null && onduty == 1) {
								if ("0".equals(ampm)) {//全天上班
									 totalDays = totalDays + 1 ;
								}else if ("1".equals(ampm)) {//上午上班下午休息
									totalDays = totalDays + 0.5 ;
								}else if ("2".equals(ampm)) {//上午休息下午上班
									totalDays = totalDays + 0.5 ;
								}
							} else {
								totalDays = totalDays + 1;
							}
						} else {
							if ("0".equals(ampm)) {// 全天休息
								//不统计
							} else if ("1".equals(ampm)) {// 上午休息
								totalDays = totalDays + 0.5;
							} else if ("2".equals(ampm)) {// 下午休息
								totalDays = totalDays + 0.5;
							}
						}
					}
				}
			}
			cal.add(Calendar.DATE, 1);
		}

		param.put("leavedays", totalDays);
		return true;
	}
	
	protected String getLeaveTypeName(String leaveTypeStr) {
		String leaveTypeName = "无定义";
		if(leaveTypeStr != null && leaveTypeStr.length() >0) {
			int levelType = Integer.valueOf(leaveTypeStr);
			switch(levelType) {
			case 1:
				leaveTypeName = "公假";
				break;
			case 2:
				leaveTypeName = "病假";
				break;
			case 3:
				leaveTypeName = "事假";
				break;
			case 4:
				leaveTypeName = "丧假";
				break;
			case 5:
				leaveTypeName = "婚假";
				break;
			case 6:
				leaveTypeName = "产假(陪产)";
				break;
			case 7:
				leaveTypeName = "保胎假";
				break;
			case 8:
				leaveTypeName = "产前假";
				break;
			case 9:
				leaveTypeName = "哺乳假";
				break;
			default:
			}
		}
		return leaveTypeName;
	}
	
	/**
	 * 获取某个级别的审批教师列表
	 * @param checkLevelNum
	 * @param auditorList
	 * @param schoolId
	 * @param termInfoId
	 * @return
	 */
	protected List<JSONObject> getMsgReceiversArray(int checkLevelNum,JSONArray auditorList,String schoolId,String termInfoId) {
		List<JSONObject> msgReceiversArray = new ArrayList<JSONObject>();
		
		for (int i = 0; i < auditorList.size(); i++) {
			JSONObject auditor = auditorList.getJSONObject(i);
			int levelNum = auditor.getIntValue("levelNum");
			if(checkLevelNum == levelNum) {
				JSONArray auditors = auditor.getJSONArray("audits");
				if(auditors == null) {
					auditors = auditor.getJSONArray("auditors");//字段兼容
				}
				if (auditors != null && auditors.size() > 0) {
					List<Long>  accountIds =  new  ArrayList<Long>();
					for(int j=0; j<auditors.size();j++) {
						JSONObject each = (JSONObject) auditors.getJSONObject(j);
						String teacherAccount = each.getString("teacherId"); 
						accountIds.add(Long.valueOf(teacherAccount));
					}
					
					if(accountIds.size() > 0) {
						List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfoId);
						for (int k = 0; k < accounts.size(); k++) {
							JSONObject msgReceiver = new JSONObject();
							msgReceiver.put("userId", accounts.get(k).getExtId());
							msgReceiver.put("userName", accounts.get(k).getName());
							msgReceiversArray.add(msgReceiver);
						}
					}
				}
				break;
			}
		}
		return msgReceiversArray;
	}
	
	/**
	 * 获取申请人消息接收者
	 * @param accountId
	 * @return
	 */
	protected List<JSONObject> getMsgReceiversArrayForUser(String accountId,String schoolId,String termInfoId){
		List<JSONObject> msgReceiversArray = new ArrayList<JSONObject>();
		List<Long>  accountIds =  new  ArrayList<Long>();
		accountIds.add(Long.valueOf(accountId));
		List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfoId);
		for (int i = 0; i < accounts.size(); i++) {
			JSONObject msgReceiver = new JSONObject();
			msgReceiver.put("userId", accounts.get(i).getExtId());
			msgReceiver.put("userName", accounts.get(i).getName());
			msgReceiversArray.add(msgReceiver);
		}
		return msgReceiversArray;
	}
	
	/**
	 * 发送Kafka消息通知
	 */
	protected boolean sendAppMsg(JSONObject param) {
		String msgTemplateType = param.getString("msgTemplateType");
		
		/**
		 * url后缀:
		 * &type=xx&id=xx
		 * type = 0 :审批中
		 * type = 1: 审批完成
		 */
		String urlSuffix = "";
		if("JSQJ1".equals(msgTemplateType)) {
			//通知下一级审批人
			urlSuffix = "&type=0&id=" + param.getString("applicationId");
		} else if("JSQJ2".equals(msgTemplateType)) {
			//通知请假人完成审批
			urlSuffix = "&type=1&id=" + param.getString("applicationId");
		}
		
		//消息内容，要求一个完整的JSON的结构体
		JSONObject msgCenterPayLoad = new JSONObject();
		JSONObject msg = new JSONObject();
	    String msgId = UUIDUtil.getUUID().replace("-", "");
		msg.put("msgId", msgId);
		msg.put("msgTitle", "你收到教师请假通知！");
		msg.put("msgContent", "请假人: " + param.getString("applyName") + " " + param.getString("leaveType") + " " + param.getString("reason"));
		msg.put("msgUrlPc", msgUrlPc);
		msg.put("msgUrlApp", msgUrlApp + urlSuffix);
		msg.put("msgOrigin", "教师请假提醒");
		msg.put("msgTypeCode", MSG_TYPE_CODE);
		msg.put("msgTemplateType", msgTemplateType);
		
		msg.put("schoolId", param.getString("schoolId"));
		msg.put("creatorName", param.getString("creatorName"));

		/**
		 * 根据产品需求文档，设置数据
		 * first,keyword1,keyword2,...,remark
		 */
		JSONObject first = new JSONObject();
		first.put("value", param.get("first"));
		
		JSONObject keyword1 = new JSONObject();
		JSONObject keyword2 = new JSONObject();
		JSONObject keyword3 = new JSONObject();
		JSONObject keyword4 = new JSONObject();
		JSONObject keyword5 = new JSONObject();
		
		if("JSQJ1".equals(msgTemplateType)) {
			//通知下一级审批人
			keyword1.put("value", param.getString("applyName"));
			keyword2.put("value", param.getString("leaveType"));
			keyword3.put("value", param.getString("leaveTime"));
			keyword4.put("value", param.getString("leavedays"));
			keyword5.put("value", param.getString("reason"));
		} else if("JSQJ2".equals(msgTemplateType)) {
			//通知请假人完成审批
			keyword1.put("value", param.getString("leaveType"));
			keyword2.put("value", param.getString("leaveTime"));
			keyword3.put("value", param.getString("reason"));
			keyword4.put("value", param.getString("auditor"));
			keyword5.put("value", param.getString("auditStatus"));
		}
		
		JSONObject remark = new JSONObject();
		remark.put("value", param.get("remark"));
		
		JSONObject data = new JSONObject();
		data.put("first", first);
		data.put("keyword1", keyword1);
		data.put("keyword2", keyword2);
		data.put("keyword3", keyword3);
		data.put("keyword4", keyword4);
		data.put("keyword5", keyword5);
		data.put("remark", remark);
		data.put("url", msgUrlApp + urlSuffix);
		msg.put("msgWxJson", data);
		
		//消息接收者列表
		@SuppressWarnings("unchecked")
		List<JSONObject> msgCenterReceiversArray = (List<JSONObject>) param.get("receivers");
		/**
		 * 接收人去重复处理
		 */
		Map<String,String> filter = new HashMap<String,String>();
		Iterator<JSONObject> iterator = msgCenterReceiversArray.iterator();
        while(iterator.hasNext()){
        	JSONObject msgReceiver = iterator.next();
        	String userId = msgReceiver.getString("userId");
            if(filter.containsKey(userId)) {
            	iterator.remove();
            } else {
            	filter.put(userId, userId);
            }
        }
		
		msgCenterPayLoad.put("msg", msg);
		msgCenterPayLoad.put("receivers", msgCenterReceiversArray);
		logger.info("teacherLeave : sendAppMsg : " + msg.toString());
		try {
			KafkaUtils.sendAppMsg(kafkaUrl, msgId, msgCenterPayLoad, PRODUCER_ID, clientId, clientSecret);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("sendAppMsg : error " + e.toString());
			return false;
		}
		return true;
	}
}
