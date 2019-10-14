package com.talkweb.auth.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.OrgInfo;
import com.talkweb.accountcenter.thrift.PermissionTemplateIns;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_OrgType;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.TeacherPart;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.accountcenter.thrift.UserPart;
import com.talkweb.accountcenter.thrift.UserPermissions;
import com.talkweb.auth.dao.AuthDao;
import com.talkweb.auth.entity.MergeTool;
import com.talkweb.auth.entity.NavInfo;
import com.talkweb.auth.service.AuthService;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;

/**
 * @ClassName AuthServiceImpl
 * @author Homer
 * @version 1.0
 * @Description 权限控制Service实现类
 * @date 2015年3月9日
 */
@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private AuthDao authDaoImpl;

	@Autowired
	private AllCommonDataService cmdService;
	
	private static ExecutorService  fixedThreadPool =  Executors.newFixedThreadPool(8);
	/**
	 * redis
	 */
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;

	private static final Logger logger = LoggerFactory
			.getLogger(AuthServiceImpl.class);
	ResourceBundle rb = ResourceBundle.getBundle("constant.constant" );
	
	class SubThread implements Runnable{
		private School school;
		private String xnxq;
		private HttpSession session;
		private List<Grade> njList;
		/**
		 * 学校代码
		 */
		private List<OrgInfo> orgs;
		public SubThread(School school,String xnxq,HttpSession session,List<Grade> njList, List<OrgInfo> orgs){
			this.school = school;
			this.xnxq = xnxq;
			this.session= session;
			this.njList = njList;
			this.orgs = orgs;
			 
		}
		@Override
		public void run() {
			long t1 = (new Date()).getTime();				
			
			//得到姓名-机构-机构名称 @author：zhh
			Map<Long ,String> teacherMap = new HashMap<Long, String>();
			 List<Account> accountList =cmdService.getAllSchoolEmployees(school,xnxq,null);
			 if (CollectionUtils.isNotEmpty(accountList)) {
				 for (Account account : accountList){
					  long teacherId = account.getId();
					  String teacherName = account.getName();
					  teacherMap.put(teacherId, teacherName);
				 } 
			 }	 
			 Object keyAccountList = "auth."+school.getId()+session.getId()+".00.accountList";
			 Object keyTeacherMap = "auth."+school.getId()+session.getId()+".00.teacherMap";
			
			 try {
				redisOperationDAO.set(keyTeacherMap, teacherMap);
				 redisOperationDAO.set(keyAccountList, accountList);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			 
			// redisOperationDAO.setAttribute("auth.accountList", accountList);
			 //redisOperationDAO.setAttribute("auth.teacherMap", teacherMap);
		    
			//获取 姓名-机构类型-机构名称 映射关系， @author zhh
			//机构类型：1:教研组，2:年级组，3:备课组，6:科室 ，7班主任，81教研组负责人、82年级组负责人、83备课组负责人、86科室负责人
			Map<String,Map<String,List<Object>>> orgNameMap = new HashMap<String, Map<String,List<Object>>>();
			HashMap<Long, OrgInfo> orgMap = new HashMap<Long,OrgInfo>();
			for (OrgInfo orgInfo : orgs) {
				orgMap.put(orgInfo.getId(), orgInfo);
				String orgType = orgInfo.getOrgType()+"";
				//得到成员
				List<Long> accountIdList = orgInfo.getMemberAccountIds();
				if( accountIdList!=null){
					for(Long accountId :accountIdList){
						String name = teacherMap.get(accountId);
						if(StringUtils.isBlank(name)){
							continue;
						}
						if(orgNameMap.containsKey(name)){
							Map<String,List<Object>> orgTypeMap = orgNameMap.get(name);
							List<Object> oList = new ArrayList<Object>();
							if(orgTypeMap.containsKey(orgType)){
								oList = orgTypeMap.get(orgType);
							}
							oList.add(orgInfo);
							orgTypeMap.put(orgType, oList);
						}else{
							Map<String,List<Object>> orgTypeMap = new HashMap<String, List<Object>>();
							List<Object> oList = new ArrayList<Object>();
							oList.add(orgInfo);
							orgTypeMap.put(orgType, oList);
							orgNameMap.put(name, orgTypeMap);
						}
					}
				}
				//得到负责人
				List<Long> headIdList = orgInfo.getHeaderAccountIds();
				orgType="8"+orgType;
				if(headIdList!=null){
					for(Long accountId :headIdList){
						String name = teacherMap.get(accountId);
						if(StringUtils.isBlank(name)){
							continue;
						}
						if(orgNameMap.containsKey(name)){
							Map<String,List<Object>> orgTypeMap = orgNameMap.get(name);
							List<Object> oList = new ArrayList<Object>();
							if(orgTypeMap.containsKey(orgType)){
								oList = orgTypeMap.get(orgType);
							}
							oList.add(orgInfo);
							orgTypeMap.put(orgType, oList);
						}else{
							Map<String,List<Object>> orgTypeMap = new HashMap<String, List<Object>>();
							List<Object> oList = new ArrayList<Object>();
							oList.add(orgInfo);
							orgTypeMap.put(orgType, oList);
							orgNameMap.put(name, orgTypeMap);
						}
					}
				}
			}// end of orgs
			
			 /***获取班主任的信息列表*/
				if(njList!=null){
				 for (Grade grade : njList){
					 	T_GradeLevel currentLevel = grade.getCurrentLevel();
					    List<Long> classIds = grade.getClassIds();
					    if (CollectionUtils.isNotEmpty(classIds)){
					    	 HashMap<String,Object> map = new HashMap<String,Object>();
					    	 String ids = classIds.toString();
					    	 ids = ids.replace("[", "").replace("]", "").replace(" ", "");
							 map.put("classId", ids);
							 map.put("schoolId", school.getId());
							 map.put("termInfoId", Long.parseLong(xnxq));
							 List<Account> aList = cmdService.getDeanList(map);
							 if (CollectionUtils.isNotEmpty(aList)) {
								 for(Account info : aList) {
									 String orgType = "7";
								     String name = info.getName();
							
								     //得到班主任关系
								     JSONObject deanObj = new JSONObject();
								     deanObj.put("gradeId", grade.getId());
							    	 deanObj.put("currentLevel", currentLevel);
							    	 deanObj.put("gradeName", AccountStructConstants.T_GradeLevelName
				         						.get((grade.getCurrentLevel())));
								     if(!orgNameMap.containsKey(name)){ 
								    	 Map<String,List<Object>> orgTypeMap = new HashMap<String, List<Object>>();
								    	 List<Object> oList = new ArrayList<Object>();
								    	 oList.add(deanObj);
								    	 orgTypeMap.put(orgType, oList);
										 orgNameMap.put(name, orgTypeMap);
										}else{
											Map<String,List<Object>> orgTypeMap = orgNameMap.get(name);
											List<Object> oList = new ArrayList<Object>();
											if(orgTypeMap.containsKey(orgType)){
												oList = orgTypeMap.get(orgType);
											}
											oList.add(deanObj);
											orgTypeMap.put(orgType, oList);
										}
								    
								 }
							 }
					    }			    
				 }
				}
				//redisOperationDAO.setAttribute("auth.orgNameMap", orgNameMap);
				 Object keyOrgNameMap = "auth."+school.getId()+session.getId()+".00.orgNameMap";
				 try {
					redisOperationDAO.set(keyOrgNameMap, orgNameMap);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			long t2 = (new Date()).getTime();
			logger.debug("导入子线程结束,耗时：" + (t2 - t1));
		}
		
	}
	@SuppressWarnings("unchecked")
	@Override
	public HashMap<String, HashMap<String, HashMap<String, JSONObject>>> getAllRightByParam(
			HttpSession session, String xnxq) {
		long start=new Date().getTime();
    	//logger.info("[authTimes ]"+"start.....");
		// TODO Auto-generated method stub
		String sid = session.getId();
		//logger.info("权限sessionID:" + sid);
		long xxdm = Long.parseLong(session.getAttribute("xxdm").toString());
		if (xxdm == 0l) {
			logger.error("【登陆权限】未取到学校，请求userID:"
					+ session.getAttribute("userId") + ";请求sessionId：" + sid);
		}
		School school = cmdService.getSchoolById(xxdm, xnxq);
		// 科目列表
		List<LessonInfo> kmList = cmdService.getLessonInfoList(school, xnxq);
		HashMap<Long, JSONObject> kmMap = new HashMap<Long, JSONObject>();
		for (LessonInfo km : kmList) {
			JSONObject kmObj = new JSONObject();
			kmObj.put("kmdm", km.getId());
			kmObj.put("kmmc", km.getName());
			kmObj.put("kmlx", km.getType());
			kmMap.put(km.getId(), kmObj);
		}
		long end1=new Date().getTime();
    	//logger.info("[authTimes ]"+"step1....."+(end1-start));
		// 年级-名称Map
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		// 年级--使用年级映射
		HashMap<Long, String> njSynjMap = new HashMap<Long, String>();
		HashMap<String, HashMap<String, JSONObject>> allRtMap = new HashMap<String, HashMap<String, JSONObject>>();
		HashMap<String, JSONObject> synjRtMap = new HashMap<String, JSONObject>();

		List<OrgInfo> orgs = cmdService.getSchoolOrgList(school, xnxq);
		session.setAttribute("auth.orgList", orgs);
		// 机构map
		HashMap<Long, OrgInfo> orgMap = new HashMap<Long, OrgInfo>();
		 for (OrgInfo orgInfo : orgs) {
				orgMap.put(orgInfo.getId(), orgInfo);
	     }
		//得到姓名-机构-机构名称 @author：zhh
		List<Grade> njList = cmdService.getGradeList(school, xnxq);
		long end2=new Date().getTime();
    	//logger.info("[authTimes ]"+"step2....."+(end2-end1));
		fixedThreadPool.execute(new SubThread(school, xnxq, session, njList,orgs));
		//logger.debug("auth login 主线程结束");
		String xn = xnxq.substring(0, 4);
		HashMap<String, String> synjMcMap = new HashMap<String, String>();
		HashMap<Long, String> classMcMap = new HashMap<Long, String>();
		HashMap<Long, Long> classNjMap = new HashMap<Long, Long>();
		HashMap<Integer, Grade> levelGradeMap = new HashMap<Integer, Grade>();
		
		HashMap<Integer,Long> levelGradeIdMap = new HashMap<Integer, Long>();
		session.setAttribute("auth.levelGradeIdMap", levelGradeIdMap);
		List<Long> allClassIds = new ArrayList<Long>();
		for (Grade nj : njList) {
			if (!nj.isGraduate && nj.getClassIds() != null
					&& nj.getClassIds().size() > 0) {
				allClassIds.addAll(nj.getClassIds());
			}
		}
		List<Classroom> bjs = cmdService.getSimpleClassBatch(xxdm, allClassIds,
				xnxq);

		for (Classroom bj : bjs) {
			if (bj.getId() != 0) {

				classMcMap.put(bj.getId(), bj.getClassName());
				classNjMap.put(bj.getId(), bj.getGradeId());
			}
		}
		for (Grade nj : njList) {
			if (nj == null || nj.getCurrentLevel() == null) {
				continue;
			}
			int njdm = nj.getCurrentLevel().getValue();
			levelGradeMap.put(njdm, nj);
			levelGradeIdMap.put(njdm, nj.getId());
			long d25 = new Date().getTime();
			String synj = cmdService.ConvertNJDM2SYNJ(njdm + "", xn);
			long d26 = new Date().getTime();
			//logger.info("【登陆权限】转换使用年级耗时：" + (d26 - d25));
			njSynjMap.put(nj.getId(), synj);
			List<Long> bjIds = nj.getClassIds();
			if (bjIds == null || nj.isGraduate) {
				logger.error(
						"【登陆权限】班级gradeId:{},【年级名称】:{}的班级为空--null！nj.isGraduate{}",
						nj.getId(), nj.getCurrentLevel(), nj.isGraduate);
				continue;
			}

			long d27 = new Date().getTime();
			//logger.info("【登陆权限】查询班级耗时：" + (d27 - d26));
			JSONObject njright = new JSONObject();

			String gradeName = "[" + cmdService.ConvertSYNJ2RXND(synj, xn)
					+ "]" + njName.get(nj.getCurrentLevel());

			synjMcMap.put(synj, gradeName);
			njright.put("gradeName", gradeName);
			HashMap<Long, JSONObject> classList = new HashMap<Long, JSONObject>();
			for (Long clr : bjIds) {
				if (clr != 0 && classMcMap.containsKey(clr)) {
					JSONObject bj = new JSONObject();
					bj.put("className", classMcMap.get(clr));
					bj.put("kmList", kmMap);
					classList.put(clr, bj);
				}
			}

			njright.put("gradeLevel",  nj.getCurrentLevel());
			njright.put("classList", classList);

			synjRtMap.put(synj, njright);
		}
		long end3=new Date().getTime();
    	//logger.info("[authTimes ]"+"step3....."+(end3-end2));
		// 系统管理员权限树
		for (int i = 1000; i <= 1060; i++) {
			allRtMap.put("cs" + i, synjRtMap);
		}
		long accountId = (long) session.getAttribute("accountId");
		// 获取用户权限
		Account act = cmdService.getAccountAllById(xxdm, accountId, xnxq);
		session.setAttribute("account", act);
		List<User> users = act.getUsers();

		List<HashMap<String, HashMap<String, JSONObject>>> rightList = new ArrayList<HashMap<String, HashMap<String, JSONObject>>>();

		boolean isOtherAdmin = false;
		List<String> otherAdminRt=new ArrayList<String>();//String otherAdminRt = ",";
		boolean isAdmin = false;
		boolean isCjgly = false;

		List<String> otherRightStr=new ArrayList<String>();//String otherRightStr = ",";
		
		//为任课老师 但具有机构权限的老师赋予管理员角色
		List<User> fakeStaffAddList = new ArrayList<User>();
		for (User user : users) {
			if(user!=null&&user.getUserPart()!=null&&
					user.getUserPart().getRole()!=null&&
					user.getUserPart().getRole()==T_Role.Teacher&&
					user.getUserPart().getDeanOfOrgIds()!=null
					&&user.getUserPart().getDeanOfOrgIds().size()>0){
				User cuser = user.deepCopy();
				cuser.getUserPart().setRole(T_Role.Staff);
				fakeStaffAddList.add(cuser);
			}
		}
		
		users.addAll(fakeStaffAddList);
		List<User> needRemoveUsers = new ArrayList<User>();
		
		//记录机构权限部分 参数定义
		//当前用户所管理的机构
		List<OrgInfo> manageOrgList = new ArrayList<OrgInfo>();
		//当前用户所管理的使用年级
		List<String> manageSynjList = new ArrayList<String>(); 
		//当前用户所管理的年级
		List<Grade> manageGradeList = new ArrayList<Grade>();
		//当前用户所管理的教研组(科目id)
		List<String> manageTeachOrgList = new ArrayList<String>();
		//当前用户所管理的备课组 synj,lessonId
		List<String> managePreLessonList = new ArrayList<String>();
		long end4=new Date().getTime();
    	//logger.info("[authTimes ]"+"step4....."+(end4-end3));
    	//##################################################################
		for (User user : users) {
			if(user.getUserPart()==null || user.getUserPart().getRole()==T_Role.PlateManager){//平台管理员
				needRemoveUsers.add(user);
				continue;
			}
			School sch = null;
			sch = cmdService.getSchoolByUserId(0, user.getUserPart()
						.getId());
	 
			if (sch == null || sch.getId() != xxdm || user == null
					|| user.getUserPart() == null
					|| user.getUserPart().getRole() == null) {
				if (user.getUserPart() == null
						|| user.getUserPart().getRole() == null || user.getUserPart().getRole()==T_Role.PlateManager) {
					logger.error("【登陆权限】异常 userPart或user的role为空");
				}
				needRemoveUsers.add(user);
				continue;
			}
			long usersysid = user.getUserPart().getId();
			// 任课教师及班主任 通过teacherPart的courseIds、deanOfClassIds来判断
			UserPart up = user.getUserPart();
			int roleType = up.getRole().getValue();

			logger.debug("【登陆权限】查询user角色roleType为{},userId为{}", roleType, user
					.getUserPart().getId());
			// 使用年级-班级-科目树
			HashMap<String, JSONObject> tpnjRight = new HashMap<String, JSONObject>();
			// 学生 家长
			if (roleType == T_Role.Parent.getValue()
					|| roleType == T_Role.Student.getValue()) {
				continue;
			} else if (roleType == T_Role.Teacher.getValue()) {
				// 老师身份
				session.setAttribute("isTeaching", true);
				logger.debug("【登陆权限】设置是否普通老师为true");
				// 教研组长等 通过userPart获取所属机构信息，再获取orgInfos里的headerIds来判断
				TeacherPart tp = user.getTeacherPart();
				// 如果teacherPart不为空 取其teacherPart部分权限
				if (tp != null) {
					HashMap<Long, JSONObject> tpclaRight = new HashMap<Long, JSONObject>();
					// 班主任 含各科权限
					List<Long> deanOfClass = tp.getDeanOfClassIds();
					List<Course> courseIds = tp.getCourseIds();
					if (deanOfClass != null && deanOfClass.size() != 0) {
						for (Long clas : deanOfClass) {
							if (!classMcMap.containsKey(clas)
									|| clas == 0
									|| !njSynjMap.containsKey(classNjMap
											.get(clas))) {
								continue;
							}
							String synj = njSynjMap.get(classNjMap.get(clas));
							JSONObject claJSON = new JSONObject();
							if (tpnjRight.containsKey(synj)) {
								JSONObject njJSON = tpnjRight.get(synj);
								HashMap<Long, JSONObject> clasRight = (HashMap<Long, JSONObject>) njJSON
										.get("classList");
								if (clasRight != null
										&& clasRight.containsKey(clas)) {
									claJSON = clasRight.get(clas);
								}
							}
							claJSON.put("className", classMcMap.get(clas));
							claJSON.put("kmList", kmMap);
							tpclaRight.put(clas, claJSON);
							JSONObject njJSON = new JSONObject();
							if (tpnjRight.containsKey(synj)) {
								njJSON = tpnjRight.get(synj);
								@SuppressWarnings("unchecked")
								HashMap<Long, JSONObject> clasRight = (HashMap<Long, JSONObject>) njJSON
										.get("classList");
								clasRight.put(clas, claJSON);
							} else {
								HashMap<Long, JSONObject> clasRight = new HashMap<Long, JSONObject>();
								clasRight.put(clas, claJSON);
								njJSON.put("classList", clasRight);
							}
							njJSON.put("gradeName", synjMcMap.get(synj));
							tpnjRight.put(synj, njJSON);
						}
					}
					// 任课教师 含部分科目权限
					if (courseIds != null && courseIds.size() != 0) {
						for (Course course : courseIds) {
							if (deanOfClass == null
									|| !deanOfClass.contains(course
											.getClassId())) {
								long lesson = course.getLessonId();
								long classId = course.getClassId();

								if (!classMcMap.containsKey(classId)
										|| classId == 0
										|| !njSynjMap.containsKey(classNjMap
												.get(classId))) {
									continue;
								}
								String synj = njSynjMap.get(classNjMap
										.get(classId));
								JSONObject claJSON = new JSONObject();
								if (tpnjRight.containsKey(synj)) {
									JSONObject njJSON = tpnjRight.get(synj);
									HashMap<Long, JSONObject> clasRight = (HashMap<Long, JSONObject>) njJSON
											.get("classList");
									if (clasRight != null
											&& clasRight.containsKey(classId)) {
										claJSON = clasRight.get(classId);
									}
								}
								claJSON.put("className",
										classMcMap.get(classId));
								HashMap<Long, JSONObject> kmArr = new HashMap<Long, JSONObject>();
								if (claJSON.containsKey("kmList")) {
									kmArr = (HashMap<Long, JSONObject>) claJSON
											.get("kmList");
								} else {
									claJSON.put("kmList", kmArr);
								}
								if (kmMap.get(lesson) == null) {
									continue;
								}
								kmArr.put(lesson, kmMap.get(lesson));

								JSONObject njJSON = new JSONObject();
								if (tpnjRight.containsKey(synj)) {
									njJSON = tpnjRight.get(synj);
									HashMap<Long, JSONObject> clasRight = (HashMap<Long, JSONObject>) njJSON
											.get("classList");
									clasRight.put(classId, claJSON);
								} else {
									HashMap<Long, JSONObject> clasRight = new HashMap<Long, JSONObject>();
									clasRight.put(classId, claJSON);
									njJSON.put("classList", clasRight);
								}
								njJSON.put("gradeName", synjMcMap.get(synj));
								tpnjRight.put(synj, njJSON);
							}
						}
					}
				}

			} else if (roleType == T_Role.Staff.getValue()) {
				// 教研组长 年级组长 备课组长等
				List<Long> orgIds = user.getUserPart().getDeanOfOrgIds();
				if (orgIds == null) {
					logger.error("【登陆权限】查询用户所管辖机构为空！");
					continue;
				}
				for (Long orgId : orgIds) {
					if (orgId == 0) {
						continue;
					}
					OrgInfo org = orgMap.get(orgId);
					if (org == null) {
						continue;
					}
					if(!manageOrgList.contains(org)){
						manageOrgList.add(org);
					}
					// 人员为该机构管理员
					// 根据机构类型来记录机构权限
					int orgType = org.orgType;
					HashMap<Long, JSONObject> kmArr = new HashMap<Long, JSONObject>();
					if (orgType == T_OrgType.T_Teach.getValue()) {
						// 教研组 所有年级 一个科目
						if(org.getLessonIds().size()>0){
							long lessId = org.getLessonIds().get(0);
							String lessonIdStr = String.valueOf(lessId);
							//放教研组权限
							if(!manageTeachOrgList.contains(lessonIdStr)){
								manageTeachOrgList.add(lessonIdStr);
							}
							kmArr.put(lessId, kmMap.get(lessId));
						}
						
						
						HashMap<String, JSONObject> synjRtMapClone = new HashMap<String, JSONObject>();
						for (Iterator<String> synj = synjRtMap.keySet()
								.iterator(); synj.hasNext();) {
							String synjStr = synj.next();
							JSONObject synjObj = synjRtMap.get(synjStr);

							HashMap<Long, JSONObject> classList = (HashMap<Long, JSONObject>) synjObj
									.get("classList");
							HashMap<Long, JSONObject> classListClone = new HashMap<Long, JSONObject>();

							for (Iterator<Long> classId = classList.keySet()
									.iterator(); classId.hasNext();) {
								long ClassId = classId.next();
								JSONObject classObj = classList.get(ClassId);
								JSONObject classObjClone = new JSONObject();
								classObjClone.put("className",
										classObj.get("className"));
								classObjClone.put("kmList", kmArr);
								classListClone.put(ClassId, classObjClone);
							}

							JSONObject synjObjClone = new JSONObject();
							synjObjClone.put("gradeName",
									synjObj.get("gradeName"));
							synjObjClone.put("classList", classListClone);
							synjRtMapClone.put(synjStr, synjObjClone);
						}

						tpnjRight = synjRtMapClone;

					} else if (orgType == T_OrgType.T_Grade.getValue()) {
						// 年级组 一个年级 所有科目
						int orgLevel = -1;
						if(org!=null && org.getScopeTypes()!=null &&  org.getScopeTypes().size()>0){
						  orgLevel = org.getScopeTypes().get(0).getValue();
						}
						Grade gd = levelGradeMap.get(orgLevel);
						if (gd != null) {
							//放年级组权限
							String synj = njSynjMap.get(gd.getId());
							if(!manageSynjList.contains(synj)){
								manageSynjList.add(synj);
							}
							if(!manageGradeList.contains(gd)){
								manageGradeList.add(gd);
							}
							HashMap<String, JSONObject> synjRtMapClone = new HashMap<String, JSONObject>();
							synjRtMapClone.put(njSynjMap.get(gd.getId()),
									synjRtMap.get(njSynjMap.get(gd.getId())));

							tpnjRight = synjRtMapClone;
						}
					} else if (orgType == T_OrgType.T_PreLesson.getValue()) {
						// 备课组 一个年级 一个科目
						long lessId = 0;
						int orgLevel =0;
						if(org!=null && org.getLessonIds()!=null && org.getLessonIds().size()>0){
							lessId = org.getLessonIds().get(0);
						}
						if(org!=null && org.getScopeTypes()!=null &&  org.getScopeTypes().size()>0){
							 orgLevel = org.getScopeTypes().get(0).getValue();
						}
						if(lessId!=0 && orgLevel!=0){
							Grade gd = levelGradeMap.get(orgLevel);
	
							kmArr.clear();
							kmArr.put(lessId, kmMap.get(lessId));
	
							HashMap<String, JSONObject> synjRtMapClone = new HashMap<String, JSONObject>();
							if(gd!=null){
								String synjStr = njSynjMap.get(gd.getId());
								//放备课组权限
								if(!managePreLessonList.contains(synjStr+","+String.valueOf(lessId))){
									managePreLessonList.add(synjStr+","+String.valueOf(lessId));
								}
								JSONObject synjObj = synjRtMap.get(synjStr);
		
								HashMap<Long, JSONObject> classList = (HashMap<Long, JSONObject>) synjObj
										.get("classList");
								HashMap<Long, JSONObject> classListClone = new HashMap<Long, JSONObject>();
		
								for (Iterator<Long> classId = classList.keySet()
										.iterator(); classId.hasNext();) {
									long ClassId = classId.next();
									JSONObject classObj = classList.get(ClassId);
									JSONObject classObjClone = new JSONObject();
									classObjClone.put("className",
											classObj.get("className"));
									classObjClone.put("kmList", kmArr);
									classListClone.put(ClassId, classObjClone);
								}
		
								JSONObject synjObjClone = new JSONObject();
								synjObjClone.put("gradeName", synjObj.get("gradeName"));
								synjObjClone.put("classList", classListClone);
								synjRtMapClone.put(synjStr, synjObjClone);
		
								tpnjRight = synjRtMapClone;
							}
					    }
					}
				}

			}
			//from 为1表示铜仁 项目  为2表示私有化部署
 			String from = rb.getString("from");
 			boolean isFrom=false;
 			if("1".equals(from)){
 				//铜仁项目 老师也要获取权限（从教育云那边获取信息）
 				if(roleType == T_Role.SchoolManager.getValue()||roleType ==  T_Role.Teacher.getValue()){
 					isFrom = true;
 				}
 			}else{
 				if(roleType == T_Role.SchoolManager.getValue()){
 					isFrom = true;
 				}
 			}
				if (isFrom) {
					//logger.info("getUserPermissionById==>  manager or teacher jycloud role get data start");
					// 成绩管理员 /系统管理员等
					isOtherAdmin = true;
					long perLevel = 0;
					long d33 = new Date().getTime();
					UserPermissions upers = cmdService.getUserPermissionById(xxdm,
							usersysid,
							session.getAttribute("accountId").toString());
					long d34 = new Date().getTime();
					//logger.info("【登陆权限】查询userPermission耗时：" + (d34 - d33));
					//logger.info("【登陆权限】userPermission：" + upers+"   userId:"+usersysid);
					if (upers == null) {
						logger.error("【登陆权限】查询userPermission为空：" + upers);
						continue;
					}
					List<PermissionTemplateIns> ps = upers
							.getPermissionTemplateInss();
					for (PermissionTemplateIns p : ps) {
						if (p == null || p.getLevelv2() == 0) {
							continue;
						}
						perLevel = p.getLevelv2();
						otherAdminRt.add(perLevel+"");
						//otherRightStr += perLevel + ",";
						otherRightStr.add(perLevel+"");
						if (perLevel == 1002L) {
								// 成绩管理员
								isCjgly = true;
						}
					}

				}
			
			if (isAdmin) {
				break;
			} else {
				logger.debug("permission otherAdminRt:"+otherAdminRt.toString()+" isOtherAdmin"+isOtherAdmin);
				HashMap<String, HashMap<String, JSONObject>> curUserRt = new HashMap<String, HashMap<String, JSONObject>>();
				if(isOtherAdmin){
					 for(String permissionStr:otherAdminRt){
						 String csPermissionStr= "cs"+permissionStr;
						 curUserRt.put(csPermissionStr, allRtMap.get(csPermissionStr));
						 rightList.add(curUserRt);
					 }
				}
				 
			}

		}
		 
		users.removeAll(needRemoveUsers);
		act.setUsers(users);
		
		session.setAttribute("isAdmin", isAdmin);
		session.setAttribute("isCjgly", isCjgly);
		//放机构权限
		session.setAttribute("manageOrgList" ,manageOrgList);
		//放当前用户所管理的使用年级
		session.setAttribute("manageSynjList" ,manageSynjList); 
		//放当前用户所管理的年级
		session.setAttribute("manageGradeList",manageGradeList);
		//放当前用户所管理的教研组(科目id)
		session.setAttribute("manageTeachOrgList",manageTeachOrgList);
		//放当前用户所管理的备课组 synj,lessonId
		session.setAttribute("managePreLessonList" ,managePreLessonList);
		putCurUserRoleToSession(session, xxdm, otherRightStr);
		/*HashMap<String, HashMap<String, HashMap<String, JSONObject>>> xnxqRightTreeMap = new HashMap<String, HashMap<String, HashMap<String, JSONObject>>>();
		if (session.getAttribute("rightTree") != null) {
			xnxqRightTreeMap = (HashMap<String, HashMap<String, HashMap<String, JSONObject>>>) session
					.getAttribute("rightTree");
		}*/
		long end5=new Date().getTime();
    	//logger.info("[authTimes ]"+"step5....."+(end5-end4));
		/*if (isAdmin) {
			logger.info("xnxq:"+xnxq+"allRtMap:"+allRtMap);
			xnxqRightTreeMap.put(xnxq, allRtMap);
			session.setAttribute("rightTree", xnxqRightTreeMap);
		} else {
			logger.info("rightList:"+rightList);
			if (rightList.size() > 0) {
				long endM=new Date().getTime();
				HashMap<String, HashMap<String, JSONObject>> partRight = MergeAppRight(rightList);
				long endM1=new Date().getTime();
		    	logger.info("[authTimes ]"+"merge....."+(endM1-endM));
				logger.info("partRight:"+partRight+"xnxq:"+xnxq);
				xnxqRightTreeMap.put(xnxq, partRight);
				session.setAttribute("rightTree", xnxqRightTreeMap);
			}
		}

		System.out.println("构建session树完成！");
    	logger.info("xnxqRightTreeMap:"+xnxqRightTreeMap);
    	long end6=new Date().getTime();
    	logger.info("[authTimes ]"+"step6....."+(end6-end5));
		return xnxqRightTreeMap;*/
    	return null;
	}

	/**
	 * @param session
	 * @param xxdm
	 * @param otherRightStr
	 * @throws NumberFormatException
	 */
	public void putCurUserRoleToSession(HttpSession session, long xxdm,
			List<String> otherAdminRt) throws NumberFormatException {
		User curUser = (User) session.getAttribute("user");
		JSONObject curRole = new JSONObject();
		List<String> curUserRole = new ArrayList<String>();
		logger.info("【登陆】【模块授权】:" + otherAdminRt.toString());
		logger.info("【登陆】【模块授权】sessionID:" + session.getId());
		logger.info("permission otherAdminRt:"+otherAdminRt.toString() );
		 for(String permissionStr:otherAdminRt){
			 String csPermissionStr=  "cs"+permissionStr;
			 curUserRole.add(csPermissionStr);
		 }
		 
		curRole.put("roleType", curUser.getUserPart().getRole());
		curRole.put("roleMds", curUserRole);

		session.setAttribute("curRole", curRole);

		logger.debug("【登陆】【模块授权】curRole:{}", curRole);
		logger.debug("【登陆】【模块授权】session-curRole:{}",
				session.getAttribute("curRole"));
	}

	/**
	 * 逐级-合并权限 app-synj-classId-kmList
	 * 
	 * @param rightList
	 * @return
	 */
	private HashMap<String, HashMap<String, JSONObject>> MergeAppRight(
			List<HashMap<String, HashMap<String, JSONObject>>> rightList) {
		// TODO Auto-generated method stub
		HashMap<String, HashMap<String, JSONObject>> rs = new HashMap<String, HashMap<String, JSONObject>>();

		for (HashMap<String, HashMap<String, JSONObject>> appRight : rightList) {
			if (appRight != null && rightList != null) {
				rs = MergeTool.MergeAppLeftRight(rs, appRight);
			}
		}

		return rs;
	}

	@Override
	public boolean getIsMoudleManagerByAccountId(long schoolId, long accountId,
			String appId, String xnxq) {

		// TODO Auto-generated method stub
		// 用户权限 只查当前的
		Account act = cmdService.getAccountAllById(schoolId, accountId, xnxq);
		if (act != null && act.getUsers() != null) {
			List<User> users = act.getUsers();
			for (User u : users) {
				if (u != null
						&& u.getUserPart() != null
						&& u.getUserPart().getRole() != null
						&&( u.getUserPart().getRole()
								.equals(T_Role.SchoolManager)|| u.getUserPart().getRole()
								.equals(T_Role.Teacher)   )) {
					UserPermissions upers = cmdService.getUserPermissionById(
							schoolId, 
							u.getUserPart().getId(),
							accountId+"");
					if (upers == null) {
						logger.error("【登陆权限】查询userPermission为空：" + upers);
						continue;
					}
					String perLevel = "";
					List<PermissionTemplateIns> ps = upers
							.getPermissionTemplateInss();
					logger.info("permission ps:"+ps.toString()+" appId:"+appId);
					for (PermissionTemplateIns p : ps) {
						if (p == null || p.getLevelv2() == 0) {
							continue;
						}
						perLevel = "cs"+p.getLevelv2();
						if(perLevel.equals(appId)){
							return true;
						}
						
					}
				}
			}
		}
		return false;
	}

	@Override
	public List<NavInfo> updateGetNavListByRoleAndSchool(
			HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return authDaoImpl.updateGetNavListByRoleAndSchool(cxMap);
	}

	@Override
	public void updateMoblieLoginState(long accId, String openId,
			String sourceType,int status) {
		// TODO Auto-generated method stub
		if(openId!=null&&openId.trim().length()>0&&sourceType!=null&&sourceType.trim().length()>0&&accId!=0){
			authDaoImpl.updateMoblieLoginState(accId,openId,sourceType,status);
		}
	}

	@Override
	public JSONObject getMoblieLoginState(  String openId, String sourceType) {
		return authDaoImpl.getMoblieLoginState( openId,sourceType);
	}

	@Override
	public JSONObject getNewEntranceSchool(HashMap<String, Object> cxMap) {
		return authDaoImpl.getNewEntranceSchool(cxMap);
	}

	@Override
	public JSONObject getExtIdByUserId(long uId,String termInfoId) {
		return authDaoImpl.getExtIdByUserId(uId,termInfoId);
	}

	@Override
	public JSONObject getExtStudentByParentId(long uId, String termInfoId) {
		return authDaoImpl.getExtStudentByParentId(uId,termInfoId);
	}

}
