package com.talkweb.workbench.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.workbench.service.ExamForWBService;
import com.talkweb.workbench.service.NoticeForWBService;
import com.talkweb.workbench.service.PendingItem;
import com.talkweb.workbench.service.PendingItemService;
import com.talkweb.workbench.service.TimeTableForWBService;
import com.talkweb.workbench.service.WeekWorkForWBService;
import com.talkweb.workbench.service.WorkbenchService;
/**
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("/workbench/")
public class WorkbenchAction extends BaseAction{
	private static final Logger logger = LoggerFactory.getLogger(WorkbenchAction.class);
	@Autowired
	WorkbenchService wbService;
	@Autowired
	TimeTableForWBService ttService;
	@Autowired
	WeekWorkForWBService wwService;
	@Autowired
	ExamForWBService examService;
	@Autowired
	NoticeForWBService noticeService;
	
	static String MSG_SUCCESS = "执行成功";
	static String MSG_FAIL = "执行失败";
	static int ROLE_CD_ALL = 1;
	static int ROLE_CD_TCH = 2;
	static int ROLE_CD_SP = 3;
	static int ROLE_CD_MG = 4;
	
	/**
	 * 获取老师工作台信息
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/teacher/getTeacherWorkbench",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherWorkbench(HttpServletRequest req, HttpServletResponse res){
		JSONObject resJSON = new JSONObject();
		Long teacherId = Long.parseLong(req.getSession().getAttribute("userId").toString());
		Long schoolId  = Long.parseLong(req.getSession().getAttribute("xxdm").toString());
		boolean isManager = (boolean) req.getSession().getAttribute("isManager");
		JSONObject param = new JSONObject();
		param.put("teacherId", teacherId);
		param.put("userId", teacherId);
		param.put("schoolId", schoolId);
		List roles = new ArrayList();
		roles.add(ROLE_CD_ALL);
		roles.add(ROLE_CD_TCH);
		if(isManager){
			roles.add(ROLE_CD_MG);
		}
		param.put("roles",roles);
		logger.info("workbench isManager "+isManager);
		try{
			List<JSONObject> menus =  wbService.getNavs(param);
			List<JSONObject> returnMenus = new ArrayList<>(menus);
			if(isManager){
				returnMenus.clear();
				for(JSONObject m:menus){
					if(!"2003".equals(m.getString("navId"))){
						returnMenus.add(m);
					}
				}
			}
			resJSON.put("code", 1);
			resJSON.put("msg", MSG_SUCCESS);
			JSONObject data = new JSONObject();
			data.put("menus", returnMenus);
			resJSON.put("data", data);
			//取得周工作
			try{
				List<JSONObject> wwJSON = wwService.getWeekWorkItems(param);
				data.put("timetable", wwJSON);
			}catch(Exception e){
				logger.error("获取周工作出错！！！");
				e.printStackTrace();
			}
			//取得课表
			try{
				JSONObject ttJSON = ttService.getTimetableForWB(param);
				data.put("timetable", ttJSON);
			}catch(Exception e){
				logger.error("获取课表出错！！！");
				e.printStackTrace();
			}
			//取得校内通知
			try{
				JSONObject noticeJSON = noticeService.getNoticeForWB(param);
				data.put("notice", noticeJSON);
			}catch(Exception e){
				logger.error("获取校内通知出错！！！");
				e.printStackTrace();
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
			resJSON.put("code", -1);
			resJSON.put("msg", MSG_FAIL);
		}
		return resJSON;
	}
	
	/**
	 * 获取当前登录人的周工作详情
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/teacher/getWeekwork",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject getWeekwork(HttpServletRequest req, HttpServletResponse res){
		JSONObject resJSON = new JSONObject();
		return resJSON;
	}
	
	/**
	 * 获取学生角色工作台数据
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/sp/getSPWorkbench",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject getSPWorkbench(HttpServletRequest req, HttpServletResponse res){
		JSONObject resJSON = new JSONObject();
		Long teacherId = Long.parseLong(req.getSession().getAttribute("userId").toString());
		Long schoolId  = Long.parseLong(req.getSession().getAttribute("xxdm").toString());
		JSONObject param = new JSONObject();
		param.put("teacherId", teacherId);
		param.put("schoolId", schoolId);
		List roles = new ArrayList();
		roles.add(ROLE_CD_ALL);
		roles.add(ROLE_CD_SP);
		param.put("roles",roles);
		try{
			List<JSONObject> menus =  wbService.getNavs(param);
			resJSON.put("code", 1);
			resJSON.put("msg", MSG_SUCCESS);
			JSONObject data = new JSONObject();
			data.put("menus", menus);
			List<PendingItem> pendingItems = new ArrayList<PendingItem>();
			//取得所有待办事项的Service实例
			WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
			Map<String,PendingItemService> pendingItemServiceMap = wac.getBeansOfType(com.talkweb.workbench.service.PendingItemService.class);
			for(PendingItemService one:pendingItemServiceMap.values()){
				try{
					pendingItems.addAll(one.getPendingItem(param));
				}catch(Exception e){
					logger.error("获取"+one.getModualType()+"待办事项出错！！！");
					e.printStackTrace();
				}
			}
			data.put("pendingItems", pendingItems);
			//考试
			try{
				JSONObject exam = examService.getExamForWB(param);
				data.put("exam", exam);
			}catch(Exception e){
				logger.error("获取考试信息出错！！！");
				e.printStackTrace();
			}
			//校内通知
			try{
				JSONObject notice = noticeService.getNoticeForWB(param);
				data.put("notice", notice);
			}catch(Exception e){
				logger.error("获取校内通知出错！！！");
				e.printStackTrace();
			}
			resJSON.put("data", data);
		}catch(Exception e){
			e.printStackTrace();
			resJSON.put("code", -1);
			resJSON.put("msg", MSG_FAIL);
		}
		return resJSON;
	}
	
	/**
	 * 设置工作台常用应用
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/teacher/setFreMenu",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject setFreMenu(@RequestBody JSONObject data,HttpServletRequest req, HttpServletResponse res){
		JSONObject resJSON = new JSONObject();
		Long teacherId = Long.parseLong(req.getSession().getAttribute("userId").toString());
		try{
			JSONObject param = new JSONObject();
			param.put("teacherId", teacherId);
			param.put("navIds", data.getJSONArray("navIds"));
			wbService.setFreMenu(param);
			resJSON.put("code", 1);
			resJSON.put("msg", MSG_SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			resJSON.put("code", -1);
			resJSON.put("msg", MSG_FAIL);
		}
		
		return resJSON;
	}
	
	/**
	 * 销毁当前登录人的消息
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/common/deleteMessage",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteMessage(HttpServletRequest req, HttpServletResponse res){
		JSONObject resJSON = new JSONObject();
		return resJSON;
	}
	
	/**
	 * 获取通知详情
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/common/getInformInfo",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject getInformInfo(HttpServletRequest req, HttpServletResponse res){
		JSONObject resJSON = new JSONObject();
		return resJSON;
	}

}
