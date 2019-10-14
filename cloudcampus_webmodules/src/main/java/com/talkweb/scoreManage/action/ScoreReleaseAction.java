package com.talkweb.scoreManage.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.scoreManage.service.ScoreManageService;

/**
 * @ClassName ScoreLessionSetAction
 * @author wy
 * @Desc 成绩分析 - 报告参数设置
 * @date 2015年4月8日
 */
@Controller
@RequestMapping("/scoremanage1/")
public class ScoreReleaseAction extends BaseAction {
	@Autowired
	private ScoreManageService scoreService;
	
	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	/**
	 * 
	 * 功能描述：发布成绩列表
	 *
	 * @author 魏春林
	 *         <p>
	 *         创建日期 ：2015年4月14日 下午2:54:40
	 *         </p>
	 *
	 * @param req
	 * @param res
	 * @return
	 *
	 *         <p>
	 *         修改历史 ：(修改人，修改时间，修改原因/内容)
	 *         </p>
	 */
	@RequestMapping(value = "getScoreReleaseList", method = RequestMethod.POST)
	@ResponseBody
	public List<JSONObject> getScoreReleaseList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String kslc = request.getString("examId");
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslc)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("kslcdm", kslc);
			params.put("xxdm", getXxdm(req));
			
			List<JSONObject> list = scoreService.getScoreReleaseList(params);
			
			setResponse(response, 1, "");
			return list;
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch(Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return new ArrayList<JSONObject>();
	}

	@RequestMapping(value = "scoreAlz/publishExamResult", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateScoreRelease(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String kslc = request.getString("examId");
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslc)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			HttpSession session = req.getSession();
			
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xxdm", getXxdm(req));
			
			params.put("fbflag", request.get("pub2StuFlag"));
			params.put("fbteaflag", request.get("pub2TeaFlag"));
			params.put("fbpmflag", request.get("stuRankPubFlag"));
			params.put("fbteapmflag", request.get("teaRankPubFlag"));
			params.put("accountId", session.getAttribute("accountId"));
			
			response.putAll(scoreService.updateScoreRelease(params));
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch(Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

}
