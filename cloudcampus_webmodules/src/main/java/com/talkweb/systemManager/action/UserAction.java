package com.talkweb.systemManager.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.auth.entity.NavInfo;
import com.talkweb.auth.service.AuthService;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.SchoolPlateService;
import com.talkweb.http.service.CallRemoteInterface;
import com.talkweb.scoreManage.action.ScoreUtil;
import com.talkweb.systemManager.service.UserService;

/**
 * @ClassName UserAction
 * @author Homer
 * @version 1.0
 * @Description 系统管理
 * @date 2015年3月4日
 */
@Controller(value = "userAction2")
@RequestMapping(value = "/systemManager/")
public class UserAction extends BaseAction{

	@Autowired
	private SchoolPlateService schoolPlateService;
    @Autowired
    private UserService userServiceImpl2;
    @Autowired
    private AllCommonDataService commonDataService;
    @Autowired
    private AuthService authServiceImpl;
    
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	/**
	 * 深圳直通卡收费屏蔽菜单
	 */
	@Value("#{settings['sz.ztk.navIds']}")
	private String ztkNavIds;
	
	/**
	 * 深圳直通卡收费查询路径
	 */
	@Value("#{settings['sz.ztk.feeservice.url']}")
    private String ztkFeeUrl;
	
	@Autowired
	private  CallRemoteInterface crservice;
	
    private static final Logger logger = LoggerFactory.getLogger(UserAction.class);

    /**
     * 登录(126的login  注释By:zhh)
     * @param userId 账号
     * @param passWord 密码
     * @param session 会话
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject login(HttpServletRequest req,long userId) {  
    	
    	HttpSession session = req.getSession();
    	JSONObject result = new JSONObject();
    	System.out.println(req.getRequestURI());
    	logger.info("登陆sessionID::"+session.getId());
    	logger.info("登陆userID::"+userId);
		int code = 1;
	    String msg = "登陆成功！";
	    //获取所有的学校关联平台表数据
	    JSONObject param = new JSONObject();
	    List<JSONObject> schoolList = schoolPlateService.getSchoolPlateListBy(param);
		Map<String,String> paramMap = new HashMap<String,String>();
	    for(JSONObject schoolObj : schoolList){
			String schoolId = schoolObj.getString("schoolId");
			String schoolPlateKey = "common."+schoolId+".00.schoolPlate";
			paramMap.put(schoolPlateKey, "1");
	    }
	    try {
			redisOperationDAO.multiSet(paramMap);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	    
    	if(userId!=0l){
    		User user = commonDataService.getUserById(0,userId);
    		if(user==null){
    			result.put("code",-1);
    			msg = "未查询到用户！";
		        result.put("msg",msg);
		        result.put("sessionID", session.getId());
		        logger.error(msg);
		        
		        return result;
    		}
    		School sch = commonDataService.getSchoolByUserId(0,userId);
    		if(sch==null||sch.getId()==0l ){
    			result.put("code",-2);
    			msg = "未查询到用户所在学校！";
		        result.put("msg",msg);
		        result.put("sessionID", session.getId());
		        return result;
    		}
    		long schoolId = sch.getId();
    		// 获取学年 eg:2014
    		String xnxq = commonDataService.getCurrentXnxq(sch);
    		
    		//注释写死学年学期
    		//xnxq = "20171";
    		logger.info("【登录】获取当前学年学期为：{}",xnxq);
    		
    		if(xnxq==null||xnxq.trim().length()==0){
    			result.put("code",-2);
    			msg = "未查询到当前学年学期！";
		        result.put("msg",msg);
		        result.put("sessionID", session.getId());
		        return result;
    		}
    		//如果身份不是000 就 获取学校代码
    		session.setAttribute("xxdm", schoolId+"");
    		session.setAttribute("user", user);
    		logger.info("【登录】获取当前用户为：{}",user);
    		session.setAttribute("school", sch);
    		//设置session有效期10小时
    		session.setMaxInactiveInterval(3600*10);
//    		SubProcess sub = new SubProcess(schoolId, session,xnxq, userId, user);
    		
    		int roleType = user.getUserPart().getRole().getValue();
        	
        	session.setAttribute("userId", userId);
        	session.setAttribute("accountId", user.getAccountPart().getId());
        	session.setAttribute("account", user.getAccountPart());
        	session.setAttribute("curXnxq", xnxq);
        	session.setAttribute("isTeaching", false);
        	String sf = "";
    		sf = "002";
    		Object key = "common."+schoolId+".00.currentTermInfo";
    		
    		try {
				redisOperationDAO.set(key, xnxq, CacheExpireTime.sessionMaxExpireTime.getTimeValue());
				String rs = (String) redisOperationDAO.get(key);
				
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	session.setAttribute("sf", sf);
    		//    	Account account = commonDataService.getAccountAllByName(accountId+"");
        	boolean needztkCheck = false;
    		if(roleType==T_Role.Student.getValue()||roleType==T_Role.Parent.getValue()){
    			needztkCheck = true;
    		}else{
    			long d1 = new Date().getTime();
    			try{
    				
    				authServiceImpl.getAllRightByParam(session,xnxq);
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    			long d2 = new Date().getTime();
    			
    			logger.debug("【登陆】-【处理权限】耗时："+(d2-d1));
    			System.out.println("【登陆】-【处理权限】耗时："+(d2-d1));
    		}
    		
//    		sub.start();
    	}else{
    		code = -1;
    		msg = "登陆失败，未查询到用户！";
    	}
    	
        
        result.put("code",code);
        result.put("msg",msg);
        result.put("sessionID", session.getId());
        return result;
    }

    
    
    /**
     * 登录 (预发布的login  注释By:zhh)
     * @param userId 账号
     * @param passWord 密码
     * @param session 会话
     * @return
     */
    @RequestMapping(value = "loginPlus", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject loginPlus(HttpServletRequest req,long userId) { //@RequestBody JSONObject userObj
    	//long userId = userObj.getLongValue("userId");
    	HttpSession session = req.getSession();
    	JSONObject result = new JSONObject();
    	System.out.println(req.getRequestURI());
    	logger.info("登陆sessionID::"+session.getId());
    	logger.info("登陆userID::"+userId);
		int code = 1;
	    String msg = "登陆成功！";
	    //获取所有的学校关联平台表数据
	    JSONObject param = new JSONObject();
	    List<JSONObject> schoolList = schoolPlateService.getSchoolPlateListBy(param);
		Map<String,String> paramMap = new HashMap<String,String>();
	    for(JSONObject schoolObj : schoolList){
			String schoolId = schoolObj.getString("schoolId");
			String schoolPlateKey = "common."+schoolId+".00.schoolPlate";
			paramMap.put(schoolPlateKey, "1");
	    }
	    try {
			redisOperationDAO.multiSet(paramMap);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	if(userId!=0l){
    		User user = commonDataService.getUserById(0,userId);
    		if(user==null){
    			result.put("code",-1);
    			msg = "未查询到用户！";
		        result.put("msg",msg);
		        result.put("sessionID", session.getId());
		        logger.error(msg);
		        
		        return result;
    		}
    		School sch = commonDataService.getSchoolByUserId(0,userId);
    		if(sch==null||sch.getId()==0l ){
    			result.put("code",-2);
    			msg = "未查询到用户所在学校！";
		        result.put("msg",msg);
		        result.put("sessionID", session.getId());
		        return result;
    		}
    		long schoolId = sch.getId();
    		// 获取学年 eg:2014
    		String xnxq = commonDataService.getCurrentXnxq(sch);
    		//xnxq = "20171";
    		logger.info("【登录】获取当前学年学期为：{}",xnxq);
    		
    		if(xnxq==null||xnxq.trim().length()==0){
    			result.put("code",-2);
    			msg = "未查询到当前学年学期！";
		        result.put("msg",msg);
		        result.put("sessionID", session.getId());
		        return result;
    		}
    		//如果身份不是000 就 获取学校代码
    		session.setAttribute("xxdm", schoolId+"");
    		session.setAttribute("user", user);
    		logger.info("【登录】获取当前用户为：{}",user);
    		session.setAttribute("school", sch);
    		//设置session有效期10小时
    		session.setMaxInactiveInterval(3600*10);
//    		SubProcess sub = new SubProcess(schoolId, session,xnxq, userId, user);
    		
    		int roleType = user.getUserPart().getRole().getValue();
        	
        	session.setAttribute("userId", userId);
        	session.setAttribute("accountId", user.getAccountPart().getId());
        	session.setAttribute("account", user.getAccountPart());
        	session.setAttribute("curXnxq", xnxq);
        	session.setAttribute("isTeaching", false);
        	//将学校当前学年学期放入redis
    		Object key = "common."+schoolId+".00.currentTermInfo";
    		
    		try {
    			logger.info("loginPlus:----key:{},value:{}",key,xnxq);
				redisOperationDAO.set(key, xnxq, CacheExpireTime.sessionMaxExpireTime.getTimeValue());
				String rs = (String) redisOperationDAO.get(key);
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("loginPlus捕获异常为：{}",e.getCause());
				logger.info("loginPlus捕获异常为：{}",e.getMessage());
				logger.info("loginPlus捕获异常为：{}",e.getStackTrace());
			}
    		boolean needControlMenu = true;
    		boolean isPureManager = false;
    		if(roleType==T_Role.Student.getValue()||roleType==T_Role.Parent.getValue()){
    			needControlMenu = false;
    		}else{
    			long d1 = new Date().getTime();
    			try{
    				
    				authServiceImpl.getAllRightByParam(session,xnxq);
    				
    				if(session.getAttribute("isTeaching")==null||!(boolean)session.getAttribute("isTeaching")){
    					isPureManager = true;
    				}
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    			long d2 = new Date().getTime();
    			
    			logger.debug("【登陆】-【处理权限】耗时："+(d2-d1));
    			System.out.println("【登陆】-【处理权限】耗时："+(d2-d1));
    		}
    		
    		//取菜单
        	try{
        		String role = "";
        		if(roleType==T_Role.Parent.getValue()){
        			role = "Parent";
        		}else if(roleType==T_Role.Student.getValue()){
        			role = "Student";
        		}else if(roleType==T_Role.Teacher.getValue()){
        			role = "Teacher";
        		}else if(roleType==T_Role.SchoolManager.getValue()){
        			role = "SchoolManager";
        		}
        		
        		HashMap<String, Object> cxMap = new HashMap<String, Object>();
        		List<Integer> pyccList = new ArrayList<Integer>();
        		List<Grade> grades = commonDataService.getGradeBatch(sch.getId(), sch.getGrades(), xnxq);
        		for(Grade g:grades){
        			if(g==null||g.getCurrentLevel()==null){
        				continue;
        			}
        			T_GradeLevel glvl = g.getCurrentLevel();
        			if(glvl.equals(T_GradeLevel.T_HighOne)||glvl.equals(T_GradeLevel.T_HighTwo)||glvl.equals(T_GradeLevel.T_HighThree)){
        				if(!pyccList.contains(3)){
        					pyccList.add(3);
        				}
        			}else if(glvl.equals(T_GradeLevel.T_JuniorOne)||glvl.equals(T_GradeLevel.T_JuniorTwo)||glvl.equals(T_GradeLevel.T_JuniorThree)){
        				if(!pyccList.contains(2)){
        					
        					pyccList.add(2);
        				}
        			}else if(glvl.equals(T_GradeLevel.T_PrimaryOne)||glvl.equals(T_GradeLevel.T_PrimaryTwo)||glvl.equals(T_GradeLevel.T_PrimaryThree)
        					||glvl.equals(T_GradeLevel.T_PrimaryFour)||glvl.equals(T_GradeLevel.T_PrimaryFive)||glvl.equals(T_GradeLevel.T_PrimarySix)){
        				if(!pyccList.contains(1)){
        					
        					pyccList.add(1);
        				}
        			}
        		}
        		
        		String pyccs = "";
        		for(Integer p:pyccList){
        			pyccs = pyccs+p+",";
        		}
        		if(pyccs.trim().length()>0){
        			pyccs = pyccs.substring(0, pyccs.length()-1);
        		}
        		cxMap.put("role", role);
        		cxMap.put("pyccs", pyccs);
        		cxMap.put("xxdm", String.valueOf(schoolId));
        		String countyCode = sch.getAreaCode()+"";
        		String cityCode = countyCode.substring(0, 4)+"00";
        		String provinceCode = countyCode.substring(0, 2)+"0000" ;
				cxMap.put("countyCode", countyCode );
				cxMap.put("cityCode",  cityCode);
				cxMap.put("provinceCode", provinceCode);
				List<NavInfo> navList = new ArrayList<NavInfo>();
				if(pyccs.trim().length()>0){
					navList = 	authServiceImpl.updateGetNavListByRoleAndSchool(cxMap);
				}
				JSONObject  newEntranceSchool = authServiceImpl.getNewEntranceSchool(cxMap);
				logger.info("[actionceng]cxMap:"+cxMap);
				logger.info("[actionceng]navList:"+navList);
				logger.info("[actionceng]newEntranceSchool:"+newEntranceSchool);
				Map<String,NavInfo> navMap = new HashMap<String, NavInfo>();
 				for(NavInfo navInfo:navList){
					String navId = navInfo.getNavId();
					String pId = navInfo.getParentId();
					if("0".equals(pId)){
						navMap.put(navId, navInfo);
					}
				}
 				List<NavInfo> returnNavList = new ArrayList<NavInfo>();
 				if(roleType==T_Role.SchoolManager.getValue()){
 					for(NavInfo navInfo:navList){
 						String pId = navInfo.getParentId();
 						String permissonId=navInfo.getPermissionId();
	 					if(isMoudleManager(req, permissonId)||"-1".equals(permissonId)){
	 						returnNavList.add(navInfo);
	 						NavInfo pNavInfo = navMap.get(pId);
		 					if(pNavInfo!=null&&!returnNavList.contains(pNavInfo)){
		 							returnNavList.add(pNavInfo);	
		 					}
						}
	 					
	 				}
 				}else{
 					returnNavList= new ArrayList<NavInfo>(navList);
 				}
				//学生和家长时需要控制直通卡权限
				if(!needControlMenu){
					returnNavList = removeZtkBlockMenues(userId,schoolId ,returnNavList);
				}
				logger.info("[actionceng]:"+returnNavList+"----长度："+returnNavList.size());
				List<JSONObject> navJSONList = changeMenuListType(returnNavList);
				result.put("navList", navJSONList);
        	}catch(Exception e){
        		e.printStackTrace();	
        	}
//    		sub.start();
    	}else{
    		code = -1;
    		msg = "登陆失败，未查询到用户！";
    	}
    	
        
        result.put("code",code);
        result.put("msg",msg);
        result.put("sessionID", session.getId());
        return result;
    }
    /**
     * 移除直通卡屏蔽菜单
     * @param userId
     * @param schoolId 
     * @param navList
     * @return
     */
    private List<NavInfo> removeZtkBlockMenues( long userId, long schoolId, List<NavInfo> navList) {
		// TODO Auto-generated method stub
    	List<NavInfo> needRemovelist = new ArrayList<NavInfo>();
    	int sfjf = 0;
    	try{
    		Map<String, Object> uObj = new HashMap<String, Object>();
    		uObj.put("userId", userId);
    		uObj.put("schoolId", schoolId);
    		long cd0 = new Date().getTime();
//    		String cxrs = crservice.updateHttpRemoteInterface(ztkFeeUrl, uObj);
    		sfjf = commonDataService.getUserPrivilegeStatus(schoolId, userId);
    		long cd1 = new Date().getTime();
//    		JSONObject cxObj = JSON.parseObject(cxrs);
//    		sfjf = cxObj.getIntValue("status");
//    		int retCode = cxObj.getIntValue("retCode");
//    		if(cxObj.containsKey("retCode") ){
    			System.out.println(
//    					"查询深圳直通卡收费状态："+retCode+";"
    					 "0为成功,负数为失败;耗时："+(cd1-cd0)+";查询结果为："+sfjf);
//    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	//查询为未收费 则进行菜单过滤
    	if(sfjf==1&&ztkNavIds!=null&&ztkNavIds.trim().length()>0){
    		String[] blockIds = ztkNavIds.split(",");
    		for(NavInfo nav:navList){
    			String vid = nav.getNavId();
    			if(isStrInArr(vid,blockIds)){
    				needRemovelist.add(nav);
    			}
    		}
    		
    	}
    	navList.removeAll(needRemovelist);
		return navList;
	}



	private boolean isStrInArr(String vid, String[] blockIds) {
		// TODO Auto-generated method stub
		for(int i=0;i<blockIds.length;i++){
			String n = blockIds[i].trim();
			if(vid.trim().equals(n)){
				return true;
			}
		}
		return false;
	}



	/**
     * 格式转换、树构建、排序
     * @param navList
     * @return
     */
	private List<JSONObject> changeMenuListType(List<NavInfo> navList) {
		// TODO Auto-generated method stub
		HashMap<Long,List<JSONObject>> children = new HashMap<Long, List<JSONObject>>();
		List<JSONObject> rs = new ArrayList<JSONObject>();
		
		for(NavInfo nav:navList){
			JSONObject navJSON = (JSONObject) JSON.toJSON(nav);
			if(!navJSON.containsKey("iconUrl")){
				navJSON.put("iconUrl", "");
			}
			if(!navJSON.containsKey("navUrl")){
				navJSON.put("navUrl", "");
			}
			navJSON.put("navId", Long.valueOf(navJSON.getString("navId")));
			navJSON.put("parentId", Long.valueOf(navJSON.getString("parentId")));
			if(Integer.parseInt(nav.getParentId())==0){
				rs.add(navJSON);
			}else{
				//存入map
				long pid = navJSON.getLongValue("parentId") ;
				List<JSONObject> cList = new ArrayList<JSONObject>();
				if(children.containsKey(pid)){
					cList = children.get(pid);
				}
				cList.add(navJSON);
				children.put(pid, cList);
			}
		}
		ScoreUtil.sorStuScoreList(rs, "sort", "asc", "", "");
		for(JSONObject p:rs){
			p.remove("pm");
			p.remove("role");
			List<JSONObject> child = children.get(p.getLong("navId"));
			if(child!=null&&child.size()>0){
				
				ScoreUtil.sorStuScoreList(child, "sort", "asc", "", "");
				for(JSONObject c:child){
					c.remove("pm");
					c.remove("role");
				}
				p.put("child", child);
			}
		}
		return rs;
	}

	class SubProcess extends Thread {
		private long xxdm;

		private HttpSession session;
		
		private String xnxq;
		
		private long userId;
		
		private User user;
		
		public SubProcess(long xxdm,HttpSession session,String xnxq,long userId,User user) {
			this.xxdm = xxdm;
			this.session = session;
			this.xnxq = xnxq;
			this.userId = userId;
			this.user = user;
		}

		@Override
		public void run() {
			 try{
				 
				long d1 = new Date().getTime();
	        	int roleType = user.getUserPart().getRole().getValue();
	        	
	        	session.setAttribute("userId", userId);
	        	session.setAttribute("accountId", user.getAccountPart().getId());
	        	session.setAttribute("account", user.getAccountPart());
	        	session.setAttribute("xxdm", xxdm);
	        	session.setAttribute("curXnxq", xnxq);
	        	String sf = "";
        		sf = "002";
        		Account account = user.getAccountPart();
        		//    	Account account = commonDataService.getAccountAllByName(accountId+"");
        		long accountId = account.getId();
        		if(roleType==T_Role.Student.getValue()||roleType==T_Role.Parent.getValue()){
        			
        		}else{
        			
        			authServiceImpl.getAllRightByParam(session,xnxq);
        		}
	        	
	        	session.setAttribute("sf", sf);
	        	 long d2 = new Date().getTime();
	        	 
	        	 System.out.println("登陆处理权限耗时:"+(d2-d1));
	        }catch (Exception e){
	        	e.printStackTrace();
	        }
		    	
		}
	}
  
    private JSONArray getRightList(int i, JSONArray r,JSONArray rs,int ids) {
        JSONObject cur = r.getJSONObject(i);
        // TODO Auto-generated method stub
        JSONArray children = new JSONArray();
        String treeDm = cur .getString("treeDm");
//        if(type.equalsIgnoreCase("add") ){
        rs.add(cur);
        cur.put("id", ids);
        ids++;
//        }
        for(int j=i+1;j<r.size();j++){
            JSONObject next = r.getJSONObject(j);
            String nextDm = next.getString("treeDm");
            if(nextDm.indexOf(treeDm)>-1){
                ids++;
                next.put("id", ids);
                children.add(next);
                cur.put("children", children);
            }else{
                if(children.size()>0){
                    children = getRightList(0,children,new JSONArray(),ids);
                    cur.put("children", children); 
                }
                rs = getRightList(j,r,rs,ids);
                break;
            }
        }
        return rs;
    }   
	
	
}
