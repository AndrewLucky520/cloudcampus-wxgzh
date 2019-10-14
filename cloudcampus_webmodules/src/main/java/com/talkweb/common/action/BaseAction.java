package com.talkweb.common.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.OrgInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.auth.service.AuthService;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.commondata.service.AllCommonDataService;

/**
 * @ClassName BaseAction
 * @author wy
 * @Desc    基类--带(*)的参数为必填
 * @date 2015年3月31日
 */
public class BaseAction {
	
	  @Autowired
	  private AuthService authServiceImpl;
	  @Autowired
	  private AllCommonDataService commonDataService;
	  
	  private static ListComparetor comparetor=new ListComparetor();

    /**
     * 获取当前学年学期
     * @param req
     * @return	当前学年学期
     */
    protected String getCurXnxq(HttpServletRequest req){
        String rs =   (String) req.getSession().getAttribute("curXnxq");
        return rs;
    }
    
    private static final Logger logger = LoggerFactory.getLogger(BaseAction.class);
    /**
     * 获取当前学校代码
     * @param req
     * @return 学校代码
     */
    protected String getXxdm(HttpServletRequest req){
    	 String rs =  req.getSession().getAttribute("xxdm")+"";
         return rs;
    }   
    /**
     * 获取当前学校
     * @param req
     * @param	termInfoId:查询的学年学期
     * @return 学校实体
     * 
     */
    protected School getSchool(HttpServletRequest req,String termInfoId){
    	if(termInfoId==null||termInfoId.trim().length()==0){
    		termInfoId = getCurXnxq(req);
    	}
    	School sch =  (School) req.getSession().getAttribute("school");
    	sch = commonDataService.getSchoolById(sch.getId(),termInfoId);
    	return sch;
    }
    
    
    /**
     * 获取当前登录用户是否为管理员
     * @param req
     * @return 
     */
    protected boolean getIsAdmin(HttpServletRequest req){
        boolean isAdmin = false;
        if(req.getSession().getAttribute("isAdmin")!=null){
            isAdmin = (boolean) req.getSession().getAttribute("isAdmin");
        }
        return isAdmin;
    }
 
    /**
     * 获取格式为yyyy-MM-dd HH:mm:ss的当前时间
     * @return
     */
    protected String getNowString(){
    	String rs = DateUtil.getDateFormatNow();
    	return rs;
    }
    
    /**
     * 获取格式为yyyy-MM-dd 的当前日期
     * @return
     */
    protected String getTodayString(){
    	String rs = DateUtil.getDateDayFormat();
    	return rs;
    }
    /**
     * 判断当前用户是否为当前模块管理员
     * @param req 当前请求
     * @param menuId	菜单模块代码 如cs1011
     * @return
     */
    protected boolean isMoudleManager(HttpServletRequest req,String menuId) {
		boolean rs = false;
		
		logger.debug("【公共】-查模块权限:{}",req.getSession().getAttribute("curRole"));
		logger.debug("【公共】-查模块权限sessionID:{}",req.getSession().getId());
		if(req.getSession().getAttribute("curRole")!=null ){
			JSONObject obj = (JSONObject) req.getSession().getAttribute("curRole");
			Log.info("[isMoudleManager :]curRole:"+obj.toJSONString());
			List<String> roleMds = (List<String>) obj.get("roleMds");
			if(!roleMds.isEmpty()&&roleMds.contains(menuId)){
				return true;
			}
			
		}
		return rs;
	}
    /**
     * 获取当前用户角色
     * @param req 当前请求
     * @return
     */
    protected T_Role getCurrentUserRoleType(HttpServletRequest req ) {
    	T_Role role = T_Role.Teacher ;
    	if(req.getSession().getAttribute("curRole")!=null ){
    		JSONObject obj = (JSONObject) req.getSession().getAttribute("curRole");
    		role = (T_Role) obj.get("roleType");
    		return role;
    	}
    	
    	return role;
    }
    
   /* *//**
     * 获取年级列表
	 * @param req	当前请求
	 * @param requestParams appId菜单id(*) termInfoId学年学期 isAll[1,0是否带全部](*)
	 * @param rs
	 *//*
    @SuppressWarnings("unchecked")
	protected List<JSONObject> getGradeRightList(HttpServletRequest req, JSONObject requestParams  )
    throws Exception {
		List<JSONObject> rs = new ArrayList<JSONObject>();
		String menuId = requestParams.getString("appId");
		String xnxq = requestParams.getString("termInfoId");
		String selectedSemester = requestParams.getString("selectedSemester");
		if(selectedSemester!=null&&selectedSemester.trim().length()>0){
			xnxq = selectedSemester;
		}
		
		HashMap<String, JSONObject> njList = new HashMap<String, JSONObject>();
		//如果是模块管理员 赋予全部权限
		if(isMoudleManager(req, menuId)){
			Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
			School sch = commonDataService.getSchoolById(Long.parseLong(getXxdm(req)), xnxq) ;
			logger.info("sch.getId():"+sch.getId()+"  sch.getGrades():"+sch.getGrades()+"    xnxq:"+xnxq);
			List<Grade> gradelist = commonDataService.getGradeBatch(sch.getId(), sch.getGrades(), xnxq);
			logger.info("gradeList:"+gradelist.toString());
			for(Grade gd:gradelist){
				if(gd.getId()==0||gd.isGraduate||gd.getCurrentLevel()==null
						||gd.getClassIds()==null||gd.getClassIds().size()==0){
					logger.info("continue  gd.getId(): "+gd.getId() + " gd.isGraduate: "+gd.isGraduate+"  gd.getCurrentLevel():"+gd.getCurrentLevel());
					logger.info("continue  gd.getClassIds(): "+gd.getClassIds() + " gd.getClassIds().size(): "+gd.getClassIds().size());
					continue;
				}
				
				String xn = xnxq.substring(0,4);
				int njdm = gd.getCurrentLevel().getValue();
				String synj = commonDataService.ConvertNJDM2SYNJ(njdm + "", xn);
				
				String gradeName = "[" + commonDataService.ConvertSYNJ2RXND(synj,xn )
						+ "]" + njName.get(gd.getCurrentLevel());
				
				JSONObject go = new JSONObject();
				go.put("gradeName", gradeName);
				go.put("synj", synj);
				go.put("gradeLevel",  gd.getCurrentLevel());
				njList.put(synj, go);
				logger.info("go:"+go.toJSONString());
				
			}
			
		}else{
			boolean his = true;
			if (xnxq == null || xnxq.trim().length() == 0) {
				xnxq = getCurXnxq(req);
				his = false;
			}else{
				int cxxnxqInt = Integer.parseInt(xnxq);
				int curxnxq = Integer.parseInt(getCurXnxq(req));
				if(cxxnxqInt>=curxnxq){
					xnxq = getCurXnxq(req);
					his = false;
				}
			}
			
			HashMap<String,HashMap<String, HashMap<String, JSONObject>>>  rightTree = new HashMap<String, HashMap<String,HashMap<String,JSONObject>>>();
			rightTree = (HashMap<String, HashMap<String, HashMap<String, JSONObject>>>) req.getSession().getAttribute("rightTree");
			HashMap<String, HashMap<String, JSONObject>> appRight  = new HashMap<String, HashMap<String,JSONObject>>();
			if(rightTree!=null&&rightTree.containsKey(xnxq)){
				
			}else{
				if(his){
					rightTree = commonDataService.getHisRightByParam(req.getSession(), xnxq);
				}else{
					rightTree = authServiceImpl.getAllRightByParam(req.getSession(), xnxq);
				}
			}
			appRight = rightTree.get(xnxq);
			
			njList = appRight.get(menuId);
			
		}
		logger.info("njList:"+njList.toString());
		if(null!=njList)
		{
			String pyccs = requestParams.getString("pyccs");
			int isAll = requestParams.getIntValue("isAll");;
		    StringBuffer all=new StringBuffer();
			for(Iterator<String> nj=njList.keySet().iterator();nj.hasNext();)
			{
				String synj = nj.next();
				
				//过滤培养层次
				if(pyccs!=null&&pyccs.trim().length()>0){
					T_GradeLevel gLevel = (T_GradeLevel) njList.get(synj).get("gradeLevel");
					logger.info("gLevel:"+gLevel.toString());
					//无高中则过滤
					if(gLevel.getValue()>=T_GradeLevel.T_HighOne.getValue()&&pyccs.indexOf("3")==-1
							&&gLevel.getValue()!=T_GradeLevel.T_JuniorFour.getValue()){
						continue;
					}
					//无初中则过滤
					if((gLevel.getValue()>=T_GradeLevel.T_JuniorOne.getValue()
							&&gLevel.getValue()<=T_GradeLevel.T_JuniorThree.getValue()
							||gLevel.getValue()==T_GradeLevel.T_JuniorFour.getValue())
							&&pyccs.indexOf("2")==-1){
						continue;
					}
					//无小学则过滤
					if(gLevel.getValue()>=T_GradeLevel.T_PrimaryOne.getValue()
							&&gLevel.getValue()<=T_GradeLevel.T_PrimarySix.getValue()
							&&pyccs.indexOf("1")==-1){
						continue;
					}
					//幼儿园直接过滤
					if(gLevel.getValue()<T_GradeLevel.T_PrimaryOne.getValue()){
						continue;
					}
				}
				
				String gradeName = njList.get(synj).getString("gradeName");
				logger.info("gradeName:"+gradeName);
				all.append(synj+",");
				JSONObject j = new JSONObject();
				
				j.put("value", synj);
				j.put("text", gradeName);
				rs.add(j);
			}
			Collections.sort(rs,comparetor);
			Collections.reverse(rs);
			if (isAll == 1) {				
				if (all.length() > 0) {
					String a = all.substring(0, all.length() - 1);
					JSONObject obj = new JSONObject();
					obj.put("value", a);
					obj.put("text", "全部");
					rs.add(0,obj);
				}
			}
		}
		return rs;
	}*/
    /* *//**
     * 获取科目列表
   	 * @param req	当前请求
   	 * @param requestParams appId菜单id(*) termInfoId学年学期  usedGradeId使用年级  classId班级 isAll是否带全部(*) type科目类型
   	 * @param rs
   	 */ 
	@SuppressWarnings("unchecked")
	public List<JSONObject>  getSubjectList(HttpServletRequest req,
			JSONObject requestParams)  throws Exception{
		List<JSONObject> rs = new ArrayList<JSONObject>();
		String menuId = requestParams.getString("appId");
		String xnxq = requestParams.getString("termInfoId");
		String selectedSemester = requestParams.getString("selectedSemester");
		int isAll = requestParams.getInteger("isAll");
		String type=requestParams.getString("type");//科目类型 0：普通 1：综合 2：活动 ，多个使用逗号分隔
		//1和2转换一下
		List<String> types = Arrays.asList(type.split(","));
		List<String>  finalTypes = new ArrayList<String>();
		for(String t:types){
			if("1".equals(t)){
				t="2";
			}
			if("2".equals(t)){
				t="1";
			}
			finalTypes.add(t);
		}
		if(selectedSemester!=null&&selectedSemester.trim().length()>0){
			xnxq = selectedSemester;
		}
		HashMap<Long,JSONObject> kmList = new HashMap<Long, JSONObject>();
		 
		School sch = commonDataService.getSchoolById(Long.parseLong(getXxdm(req)), xnxq) ;
		List<LessonInfo> kmll = commonDataService.getLessonInfoList(sch, xnxq);
		for(LessonInfo li:kmll){
			if(li.id!=0&&li.getName().trim().length()>0){
				JSONObject obj = new JSONObject();
				obj.put("kmlx", li.getType());
				obj.put("kmmc", li.getName());
				kmList.put(li.getId(), obj);
			}
		}
		StringBuffer all = new StringBuffer();
		for (Iterator<Long> it = kmList.keySet().iterator();it.hasNext();) {
			long kmdm = it.next();
			JSONObject km = kmList.get(kmdm);
			if(km!=null){
				String kmmc = km.getString("kmmc");
				int kmType = km.getIntValue("kmlx");
				if(finalTypes!=null){
					if(finalTypes.contains(kmType+""))
					{
						JSONObject obj = new JSONObject();
						obj.put("value", kmdm);
						obj.put("text", kmmc);
						all.append(kmdm+",");
						rs.add(obj);
					}
				}else{
					JSONObject obj = new JSONObject();
					obj.put("value", kmdm);
					obj.put("text", kmmc);
					all.append(kmdm+",");
					rs.add(obj);
				}
			}
			
		}
		Collections.sort(rs,comparetor);
		if (isAll == 1) {
			String avalues = "";
			if (all.length() > 0) {
				avalues = all.substring(0, all.length() - 1);
			}
			JSONObject obj = new JSONObject();
			obj.put("value", avalues);
			obj.put("text", "全部");
			rs.add(0,obj);
		}
		return rs;
	} 
   /* *//**
     * 获取科目列表
   	 * @param req	当前请求
   	 * @param requestParams appId菜单id(*) termInfoId学年学期  usedGradeId使用年级  classId班级 isAll是否带全部(*) type科目类型
   	 * @param rs
   	 *//*
	@SuppressWarnings("unchecked")
	public List<JSONObject>  getSubjectRightList(HttpServletRequest req,
			JSONObject requestParams)  throws Exception{
		List<JSONObject> rs = new ArrayList<JSONObject>();
		String menuId = requestParams.getString("appId");
		String xnxq = requestParams.getString("termInfoId");
		String selectedSemester = requestParams.getString("selectedSemester");
		int isAll = requestParams.getInteger("isAll");
		String type=requestParams.getString("type");//科目类型 0：普通 1：综合 2：活动 ，多个使用逗号分隔
		//1和2转换一下
		List<String> types = Arrays.asList(type.split(","));
		List<String>  finalTypes = new ArrayList<String>();
		for(String t:types){
			if("1".equals(t)){
				t="2";
			}
			if("2".equals(t)){
				t="1";
			}
			finalTypes.add(t);
		}
		if(selectedSemester!=null&&selectedSemester.trim().length()>0){
			xnxq = selectedSemester;
		}
		HashMap<Long,JSONObject> kmList = new HashMap<Long, JSONObject>();
		//如果是模块管理员 赋予全部权限
		if(isMoudleManager(req, menuId)){
			School sch = commonDataService.getSchoolById(Long.parseLong(getXxdm(req)), xnxq) ;
			List<LessonInfo> kmll = commonDataService.getLessonInfoList(sch, xnxq);
			for(LessonInfo li:kmll){
				if(li.id!=0&&li.getName().trim().length()>0){
					JSONObject obj = new JSONObject();
					obj.put("kmlx", li.getType());
					obj.put("kmmc", li.getName());
					kmList.put(li.getId(), obj);
				}
			}
		}else{
			boolean his = true;
			if (xnxq == null || xnxq.trim().length() == 0) {
				xnxq = getCurXnxq(req);
				his = false;
			}else{
				int cxxnxqInt = Integer.parseInt(xnxq);
				int curxnxq = Integer.parseInt(getCurXnxq(req));
				if(cxxnxqInt>=curxnxq){
					xnxq = getCurXnxq(req);
					his = false;
				}
			}

			String synj = requestParams.getString("usedGradeId");
			long bj = 0l;
			if(null!=requestParams.get("classId") && StringUtils.isNotEmpty(requestParams.getString("classId"))){
				bj=requestParams.getLongValue("classId");
			}
		
			
			HashMap<String,HashMap<String, HashMap<String, JSONObject>>>  rightTree = (HashMap<String, HashMap<String, HashMap<String, JSONObject>>>) req.getSession().getAttribute("rightTree");
			
			if(rightTree!=null&&rightTree.containsKey(xnxq)){
				rightTree = authServiceImpl.getAllRightByParam(req.getSession(), xnxq);
			}else{
				if(his){
					rightTree = commonDataService.getHisRightByParam(req.getSession(), xnxq);
				}else{
					rightTree = authServiceImpl.getAllRightByParam(req.getSession(), xnxq);
				}
			}
			HashMap<String, HashMap<String, JSONObject>> appRight  = new HashMap<String, HashMap<String,JSONObject>>();
			
			appRight = rightTree.get(xnxq);
			
			
			HashMap<String, JSONObject> njList = appRight.get(menuId);
			
			HashMap<Long, JSONObject> classList = new HashMap<Long, JSONObject>();
			if(synj!=null&&synj.trim().length()>0){
				if(synj.indexOf(",")==-1){
					
					classList = (HashMap<Long, JSONObject>) njList.get(synj).get("classList");
				}else{
					//支持多个年级取科目数据
					String[] njs = synj.split(",");
					for(int j=0;j<njs.length;j++){
						String nj = njs[j];
						HashMap<Long, JSONObject> temp = (HashMap<Long, JSONObject>) njList.get(nj).get("classList");
						classList = MergeTool.MergeClassList(classList, temp);
					}
				}
			}else{
				for(Iterator<String> njit = njList.keySet().iterator();njit.hasNext();){
					String synjIndex = njit.next();
					HashMap<Long, JSONObject> right = (HashMap<Long, JSONObject>) njList.get(synjIndex).get("classList");
					classList = MergeTool.MergeClassList(classList, right);
				}
			}
					
		
			if(bj!=0l){
				
				kmList = (HashMap<Long, JSONObject>) classList.get(bj).get("kmList");
			}else{
				for(Iterator<Long> bjit = classList.keySet().iterator();bjit.hasNext();){
					long bjid = bjit.next();
					HashMap<Long, JSONObject> right = (HashMap<Long, JSONObject>) classList.get(bjid).get("kmList");
					kmList = MergeTool.MergeKmList(kmList, right);
				}
			}
		}
		
		StringBuffer all = new StringBuffer();
		for (Iterator<Long> it = kmList.keySet().iterator();it.hasNext();) {
			long kmdm = it.next();
			JSONObject km = kmList.get(kmdm);
			if(km!=null){
				String kmmc = km.getString("kmmc");
				int kmType = km.getIntValue("kmlx");
				if(finalTypes!=null){
					if(finalTypes.contains(kmType+""))
					{
						JSONObject obj = new JSONObject();
						obj.put("value", kmdm);
						obj.put("text", kmmc);
						all.append(kmdm+",");
						rs.add(obj);
					}
				}else{
					JSONObject obj = new JSONObject();
					obj.put("value", kmdm);
					obj.put("text", kmmc);
					all.append(kmdm+",");
					rs.add(obj);
				}
			}
			
		}
		Collections.sort(rs,comparetor);
		if (isAll == 1) {
			String avalues = "";
			if (all.length() > 0) {
				avalues = all.substring(0, all.length() - 1);
			}
			JSONObject obj = new JSONObject();
			obj.put("value", avalues);
			obj.put("text", "全部");
			rs.add(0,obj);
		}
		return rs;
	}*/
	
	/**
	 * 获取班级 还未添加权限判断、当前学年学期获取
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<JSONObject> getClassList(HttpServletRequest req,
			JSONObject requestParams) {
		List<JSONObject> rs = new ArrayList<JSONObject>();
		JSONObject json = new JSONObject();
		String msg = "";
		int code = 0;
		int isAll = requestParams.getIntValue("isAll");
		String menuId = requestParams.getString("appId");
		try {
			//rs = getClassRightList(req, requestParams);
			//
			School sch = new School();
			long schoolId=Long.parseLong(getXxdm(req));
			String synj  = requestParams.getString("usedGradeId");
			String xnxq = requestParams.getString("termInfoId");
			String selectedSemester = requestParams.getString("selectedSemester");
			if(selectedSemester!=null&&selectedSemester.trim().length()>0){
				xnxq = selectedSemester;
			}
			String xn= xnxq.substring(0, 4);
			sch.setId(schoolId);
			Boolean isManager = false;
			if(isMoudleManager(req, menuId)){
				isManager=true;
			}
			logger.info("isManager:"+isManager);
			HashMap<String,Object> map = new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("termInfoId", xnxq);
			map.put("usedGradeId", synj);
			List<Classroom> cList =commonDataService.getClassList(map);
			logger.info("cList:"+cList.toString()); 
			List<Long> ids = new ArrayList<Long>();
			if(!isManager){
				School school = new School();
				school.setId(schoolId);
				List<Long> accountIds = new ArrayList<>();
				long  accountId =    (long) req.getSession().getAttribute("accountId");
				long  userId =    (long) req.getSession().getAttribute("userId");
				accountIds.add( accountId );
				List<Account> aList = commonDataService.getAccountBatch(schoolId, accountIds, xnxq);
				logger.info("aList:"+aList.toString());
				if( accountId==0||userId==0){
					json.put("code", -1);
					json.put("msg", "获取accountId或userId错误");
					json.put("data", rs);
					return rs;
	
				}
				for(Account a :aList){
					if(accountId==a.getId()){
						List<User> userList = a.getUsers();
						if(userList!=null){
							for(User user:userList){
								if(user.getUserPart()!=null&& userId==user.getUserPart().getId() && user.getUserPart().getRole()!=null && user.getUserPart().getRole().getValue()==T_Role.Teacher.getValue())
								{ 
									List<Course> courseList = user.getTeacherPart().getCourseIds();
									if(courseList!=null){
										for(Course c :courseList){
											long classId = c.getClassId();
											ids.add(classId);
										}
									}
									List<Long> cIds = user.getTeacherPart().getDeanOfClassIds();
									if(cIds!=null){
										ids.addAll(cIds);
									}
								}
							}
						}
					}
				}
				logger.info("ids:"+ids);
			}
			StringBuffer all=new StringBuffer();
			for(Classroom c:cList)
			{
				if(!isManager)	 {
					if(!ids.contains(c.getId())){continue;}
				}	
				all.append(c.getId()+",");
				JSONObject j = new JSONObject();
				j.put("value", c.getId());
				j.put("text", c.getClassName());
				rs.add(j);
			}
			Collections.sort(rs,comparetor);
			if (isAll == 1) {				
				if (all.length() > 0) {
					String a = all.substring(0, all.length() - 1);
					JSONObject obj = new JSONObject();
					obj.put("value", a);
					obj.put("text", "全部");
					rs.add(0,obj);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			code = -1;
			msg = "未检索到班级！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", rs);
		logger.info("rs:"+rs.toString());
		return rs;
	}
	/* *//**
     * 获取科目列表
   	 * @param req	当前请求
   	 * @param requestParams appId菜单id(*) termInfoId学年学期  usedGradeId使用年级   isAll是否带全部(*) [0不要全部 1要全部] 
   	 * @param rs
   	 *//*
	@SuppressWarnings("unchecked")
	public List<JSONObject>  getClassRightList(HttpServletRequest req,
			JSONObject requestParams)  throws Exception{
		List<JSONObject> rs = new ArrayList<JSONObject>();
		String menuId = requestParams.getString("appId");
		String xnxq = requestParams.getString("termInfoId");
		String synj  = requestParams.getString("usedGradeId");
		String selectedSemester = requestParams.getString("selectedSemester");
		if(selectedSemester!=null&&selectedSemester.trim().length()>0){
			xnxq = selectedSemester;
		}
		
		HashMap<Long, JSONObject> classList = new HashMap<Long, JSONObject>();
		//如果是模块管理员 赋予全部权限
		if(isMoudleManager(req, menuId)){
			School sch = commonDataService.getSchoolById(Long.parseLong(getXxdm(req)), xnxq) ;
			List<Grade> grades = commonDataService.getGradeBatch(sch.getId(), sch.getGrades(), xnxq);
			Log.info("getClassList  grades:"+grades.toString());
			List<Long> classes = new ArrayList<Long>();
			for(Grade gd:grades){
				if(gd!=null&&!gd.isGraduate&&gd.getClassIds()!=null
						&&gd.getClassIds().size()>0 &&gd.getCurrentLevel()!=null){
					String gs = commonDataService.ConvertNJDM2SYNJ(gd.getCurrentLevel().getValue()+"", xnxq.substring(0,4));
					if(synj.indexOf(gs)!=-1){
						classes.addAll(gd.getClassIds());
					}
				}
			}
			if(classes.size()>0){
				List<Classroom> smclasses = commonDataService.getSimpleClassBatch(sch.getId(), classes, xnxq);
				for(Classroom clr:smclasses){
					if(clr!=null && clr.getId()!=0&& clr.getClassName()!=null &&clr.getClassName().trim().length()>0){
						JSONObject obj = new JSONObject();
						obj.put("classId", clr.getId());
						obj.put("className",clr.getClassName());
						classList.put(clr.getId(), obj);
					}
				}
			}
			Log.info("getClassList  classes:  "+classes.toString()+" classList:"+classList.toString());
		}else{
			boolean his = true;
			if (xnxq == null || xnxq.trim().length() == 0) {
				xnxq = getCurXnxq(req);
				his = false;
			}else{
				int cxxnxqInt = Integer.parseInt(xnxq);
				int curxnxq = Integer.parseInt(getCurXnxq(req));
				if(cxxnxqInt>=curxnxq){
					xnxq = getCurXnxq(req);
					his = false;
				}
			}
			HashMap<String,HashMap<String, HashMap<String, JSONObject>>>  rightTree = (HashMap<String, HashMap<String, HashMap<String, JSONObject>>>) req.getSession().getAttribute("rightTree");
			
			if(rightTree!=null&&rightTree.containsKey(xnxq)){
				
			}else{
				if(his){
					rightTree = commonDataService.getHisRightByParam(req.getSession(), xnxq);
				}else{
					rightTree = authServiceImpl.getAllRightByParam(req.getSession(), xnxq);
				}
			}
			HashMap<String, HashMap<String, JSONObject>> appRight  = new HashMap<String, HashMap<String,JSONObject>>();
			
			appRight = rightTree.get(xnxq);
			logger.info("appRight:"+appRight);
			logger.info("rightTree:"+rightTree);
			HashMap<String, JSONObject> njList = new HashMap<String, JSONObject>();
			if(appRight!=null){
				 njList = appRight.get(menuId);
			}
			if(synj.indexOf(",")!=-1){
				String[] synjs = synj.split(",");
				for(int i=0;i<synjs.length;i++){
					String tsy = synjs[i];
					HashMap<Long, JSONObject> right = (HashMap<Long, JSONObject>) njList.get(tsy).get("classList");;
					classList = MergeTool.MergeClassList(classList, right );
				}
			}else{
				classList = (HashMap<Long, JSONObject>) njList.get(synj).get("classList");
			}
			
		}
		
		int isAll = requestParams.getIntValue("isAll");

		StringBuffer all = new StringBuffer();
		if (null != classList) {
			for (Iterator<Long> it = classList.keySet().iterator();it.hasNext();) {
				long classId  =  it.next();
				String className = classList.get(classId).getString("className");
				JSONObject obj = new JSONObject();
				obj.put("value", classId);
				obj.put("text", className);
				all.append(classId+""+(","));
				rs.add(obj);
			}
		}
//        Collections.sort(rs,comparetor);
		try {
			rs = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, rs, "text");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isAll == 1) {
			String avalues = "";
			if (all.length() > 0) {
				avalues = all.substring(0, all.length() - 1);
			}
			JSONObject obj = new JSONObject();
			obj.put("value", avalues);
			obj.put("text", "全部");
			rs.add(0,obj);
		}
		return rs;
	}*/
	/**
	 * 根据账号id和模块代码获取是否模块管理员
	 * @param accountId
	 * @param appId 模块代码
	 * @param req 
	 * @return
	 */
	public boolean getIsMoudleManagerByAccountId(long schoolId,long accountId, String appId, HttpServletRequest req) {
		return authServiceImpl.getIsMoudleManagerByAccountId(schoolId, accountId, appId,getCurXnxq(req));
		
	}
	/**
	 * 获取当前登陆账户 所可管理的机构列表
	 * @param req
	 * @return
	 * @throws Exception
	 */
	protected List<OrgInfo> getCurrentAccountManageOrgList(HttpServletRequest req   )
		    throws Exception {
		List<OrgInfo> orgList = new ArrayList<OrgInfo>();
		if(req.getSession().getAttribute("manageOrgList")!=null){
			orgList = (List<OrgInfo>) req.getSession().getAttribute("manageOrgList");
		}
		return orgList;
	}
	/**
	 * 获取当前年级组长所管理的使用年级列表（如无年级组长权限则列表为空）
	 * @param req
	 * @return
	 * @throws Exception
	 */
	protected List<String> getCurrentAccountManageSynjList(HttpServletRequest req   )
		    throws Exception {
		List<String> orgList = new ArrayList<String>();
		if(req.getSession().getAttribute("manageSynjList")!=null){
			orgList = (List<String>) req.getSession().getAttribute("manageSynjList");
		}
		return orgList;
	}
	/**
	 * 获取当前年级组长所管理的年级列表（如无年级组长权限则列表为空）
	 * @param req
	 * @return
	 * @throws Exception
	 */
	protected List<Grade> getCurrentAccountManageGradeList(HttpServletRequest req   )
			throws Exception {
		List<Grade> orgList = new ArrayList<Grade>();
		if(req.getSession().getAttribute("manageGradeList")!=null){
			orgList = (List<Grade>) req.getSession().getAttribute("manageGradeList");
		}
		return orgList;
	}
	/**
	 * 获取当前教研组长所管理的科目id列表（如无教研组长权限则列表为空）
	 * @param req
	 * @return
	 * @throws Exception
	 */
	protected List<String> getCurrentAccountManageTeachList(HttpServletRequest req   )
			throws Exception {
		List<String> orgList = new ArrayList<String>();
		if(req.getSession().getAttribute("manageTeachOrgList")!=null){
			orgList = (List<String>) req.getSession().getAttribute("manageTeachOrgList");
		}
		return orgList;
	}
	/**
	 * 获取当前备课组长所管理的<"使用年级,科目id">列表（如无备课组长权限则列表为空,返回list为<使用年级+逗号+科目id>的列表）
	 * @param req
	 * @return
	 * @throws Exception
	 */
	protected List<String> getCurrentAccountManagePreLessonList(HttpServletRequest req   )
			throws Exception {
		List<String> orgList = new ArrayList<String>();
		if(req.getSession().getAttribute("managePreLessonList")!=null){
			orgList = (List<String>) req.getSession().getAttribute("managePreLessonList");
		}
		return orgList;
	}
	
	/**
	 * 通过学生accountId获取家长信息
	 * @param schoolId
	 * @param accountIds
	 * @param termInfoId
	 * @return Map<String, String> 格式：[学生accountId： 家长accountId：家长userId, 家长accountId：家长userId]
	 */
	public Map<String, String> getUserFamily(long schoolId,List<Long> accountIds,String termInfoId){
		Map<String, String> returnMap = new HashMap<String, String>();
		List<Long> parentIds = new ArrayList<Long>();
		List<Account> accountBatch = commonDataService.getAccountBatch(schoolId, accountIds, termInfoId);
		if(CollectionUtils.isNotEmpty(accountBatch) && accountBatch.size() > 0){
			for (Account account : accountBatch) {
				List<User> users = account.getUsers();
				if(CollectionUtils.isNotEmpty(users) && users.size() > 0){
					for (User user : users) {
						StudentPart studentPart = user.getStudentPart();
						if(studentPart != null){
							returnMap.put(user.getUserPart().getAccountId()+"", null);
							parentIds.add(studentPart.getParentId());
						}
					}
				}
			}
		}
		if(CollectionUtils.isNotEmpty(parentIds) && parentIds.size() > 0){
			List<User> userBatch = commonDataService.getUserBatch(schoolId, parentIds, termInfoId);
			if(CollectionUtils.isNotEmpty(userBatch) && userBatch.size() > 0){
				for (User user : userBatch) {
					if(returnMap.containsKey(user.getParentPart().getStudentId()+"")){
						long accountId = user.getUserPart().getAccountId();
						long id = user.getUserPart().getId();
						returnMap.put(user.getParentPart().getStudentId()+"", accountId+":"+id);
					}
				}
			}
		}
		return returnMap;
	}
}
