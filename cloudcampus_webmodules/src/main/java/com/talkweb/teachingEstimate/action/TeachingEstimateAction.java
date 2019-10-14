package com.talkweb.teachingEstimate.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.utils.SplitUtil;

@RequestMapping("/teachingEstimate")
@RestController
public class TeachingEstimateAction {
    @Autowired  
    private HttpServletRequest request;

	String rootPath = SplitUtil.getRootPath("evaluation.url") + "teachingEstimate/";

	@RequestMapping("/getRole")
	@ResponseBody
	public JSONObject getRole(){
		return postAction(new JSONObject(), "getRole");
	}

	@RequestMapping("/getTeachingEstimateList")
	@ResponseBody
	public JSONObject getTeachingEstimateList(@RequestBody JSONObject param){
		return postAction(param, "getTeachingEstimateList");
	}
	
	@RequestMapping("/addTeachingEstimate")
	@ResponseBody
	public JSONObject addTeachingEstimate(@RequestBody JSONObject param){
		return postAction(param, "addTeachingEstimate");
	}
	
	@RequestMapping("/updateTeachingEstimate")
	@ResponseBody
	public JSONObject updateTeachingEstimate(@RequestBody JSONObject param){
		return postAction(param, "updateTeachingEstimate");
	}
	
	@RequestMapping("/updateTeachingEstimatePublished")
	@ResponseBody
	public JSONObject updateTeachingEstimatePublished(@RequestBody JSONObject param){
		return postAction(param, "updateTeachingEstimatePublished");
	}
	
	@RequestMapping("/deleteTeachingEstimate")
	@ResponseBody
	public JSONObject deleteTeachingEstimate(@RequestBody JSONObject param){
		return postAction(param, "deleteTeachingEstimate");
	}
	
	@RequestMapping("/getTeachingEstimate")
	@ResponseBody
	public JSONObject getTeachingEstimate(@RequestBody JSONObject param){
		return postAction(param, "getTeachingEstimate");
	}
	
	@RequestMapping("/getPersonalTeachingEstimate")
	@ResponseBody
	public JSONObject getPersonalTeachingEstimate(@RequestBody JSONObject param){
		return postAction(param, "getPersonalTeachingEstimate");
	}
	
	@RequestMapping("/importTeachingEstimate")
	@ResponseBody
	public JSONObject importTeachingEstimate(HttpServletRequest req,
			@RequestParam("importFile") MultipartFile file){
		//System.out.println("###############"+req.getParameterMap());
		JSONObject param = new JSONObject();
		param.put("headRowNum", req.getParameter("headRowNum"));
		param.put("sessionId", req.getSession().getId());
		param.put("schoolId", request.getSession().getAttribute("xxdm"));
		return SplitUtil.postFile(rootPath + "importTeachingEstimate", file, "importFile", param);
	}
	
	@RequestMapping("/startImportTask")
	@ResponseBody
	public JSONObject startImportTask(@RequestBody JSONObject param){
		return postAction(param, "startImportTask");
	}
	
	@RequestMapping("/importProgress")
	@ResponseBody
	public JSONObject importProgress(@RequestBody JSONObject param){
		return postAction(param, "importProgress");
	}
	
	@RequestMapping("/singleDataCheck")
	@ResponseBody
	public JSONObject singleDataCheck(@RequestBody JSONObject param){
		return postAction(param, "singleDataCheck");
	}
	
	@RequestMapping("/continueImport")
	@ResponseBody
	public JSONObject continueImport(@RequestBody JSONObject param){
		return postAction(param, "continueImport");
	}
	
	@RequestMapping("/exportWrongMsg")
	@ResponseBody
	public void exportWrongMsg(HttpServletRequest req, HttpServletResponse res){
		SplitUtil.exportExcelWithData(req, res);
	}
	
	@RequestMapping("/presonalEstimate")
	@ResponseBody
	public JSONObject presonalEstimate(@RequestBody JSONObject param){
		return postAction(param, "presonalEstimate");
	}
	
	private JSONObject postAction(JSONObject param, String action) {
		JSONObject obj = (JSONObject) request.getSession().getAttribute("curRole");
		if (obj != null) {
			param.put("roleMds", obj.get("roleMds"));
		}
		return SplitUtil.postAction(request, rootPath + action, param);		
	}

}
