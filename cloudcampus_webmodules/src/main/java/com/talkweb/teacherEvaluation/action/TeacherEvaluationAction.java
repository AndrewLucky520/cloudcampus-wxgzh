package com.talkweb.teacherEvaluation.action;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.utils.SplitUtil;

@RequestMapping("/teacherEvaluation")
@RestController
public class TeacherEvaluationAction {
	//@Autowired
	//private AllCommonDataService allCommonDataService;
    @Autowired  
    private HttpServletRequest request;

	String rootPath = SplitUtil.getRootPath("evaluation.url") + "teacherEvaluation/";

	//EvaluationAppAction
	@RequestMapping("/app/getTeacherEvaluateListForApp")
	@ResponseBody
	public JSONObject getTeacherEvaluateListForApp(@RequestBody JSONObject param){
		return postAction(param, "app/getTeacherEvaluateListForApp");
	}

	@RequestMapping("/app/getEvaluateGroupInfoListForApp")
	@ResponseBody
	public JSONObject getEvaluateGroupInfoListForApp(@RequestBody JSONObject param){
		return postAction(param, "app/getEvaluateGroupInfoListForApp");
	}
	
	@RequestMapping("/app/getEvaluateTecherTargetForApp")
	@ResponseBody
	public JSONObject getEvaluateTecherTargetForApp(@RequestBody JSONObject param){
		return postAction(param, "app/getEvaluateTecherTargetForApp");
	}
	
	@RequestMapping("/app/updateEvaluateTecherTargetForApp")
	@ResponseBody
	public JSONObject updateEvaluateTecherTargetForApp(@RequestBody JSONObject param){
		return postAction(param, "app/updateEvaluateTecherTargetForApp");
	}
	
	@RequestMapping("/app/getTeacherEvaluatedResultForApp")
	@ResponseBody
	public JSONObject getTeacherEvaluatedResultForApp(@RequestBody JSONObject param){
		return postAction(param, "app/getTeacherEvaluatedResultForApp");
	}
	
	//EvaluationCommonAction
	@RequestMapping("/common/getHisEvaluateList")
	@ResponseBody
	public JSONObject getHisEvaluateList(@RequestBody JSONObject param){
		return postAction(param, "common/getHisEvaluateList");
	}
	
	@RequestMapping("/common/getEvaluateWayList")
	@ResponseBody
	public JSONObject getEvaluateWayList(@RequestBody JSONObject param){
		return postAction(param, "common/getEvaluateWayList");
	}
	
	@RequestMapping("/common/getEvaluateObjectList")
	@ResponseBody
	public JSONObject getEvaluateObjectList(@RequestBody JSONObject param){
		return postAction(param, "common/getEvaluateObjectList");
	}
	
	@RequestMapping("/common/getEvaluatedGroupTypeList")
	@ResponseBody
	public JSONObject getEvaluatedGroupTypeList(@RequestBody JSONObject param){
		return postAction(param, "common/getEvaluatedGroupTypeList");
	}
	
	@RequestMapping("/common/getEvaluatedGroupList")
	@ResponseBody
	public JSONObject getEvaluatedGroupList2(@RequestBody JSONObject param){
		return postAction(param, "common/getEvaluatedGroupList");
	}
	
	@RequestMapping("/common/getTargetList")
	@ResponseBody
	public JSONObject getTargetList2(@RequestBody JSONObject param){
		return postAction(param, "common/getTargetList");
	}
	
	@RequestMapping("/common/getEvaluateGroupList")
	@ResponseBody
	public JSONObject getEvaluateGroupList2(@RequestBody JSONObject param){
		return postAction(param, "common/getEvaluateGroupList");
	}
	
	@RequestMapping("/common/getGroupsLevel")
	@ResponseBody
	public JSONObject getGroupsLevel(@RequestBody JSONObject param){
		return postAction(param, "common/getGroupsLevel");
	}
	
	//EvalutionSetAction
	@RequestMapping("/getTargetList")
	@ResponseBody
	public JSONObject getTargetList(@RequestBody JSONObject param){
		return postAction(param, "getTargetList");
	}
	
	@RequestMapping("/createEvaluateTarget")
	@ResponseBody
	public JSONObject createEvaluateTarget(@RequestBody JSONObject param){
		return postAction(param, "createEvaluateTarget");
	}
	
	@RequestMapping("/getEvaluateTargetDetail")
	@ResponseBody
	public JSONObject getEvaluateTargetDetail(@RequestBody JSONObject param){
		return postAction(param, "getEvaluateTargetDetail");
	}
	
	@RequestMapping("/updateEvaluateTarget")
	@ResponseBody
	public JSONObject updateEvaluateTarget(@RequestBody JSONObject param){
		return postAction(param, "updateEvaluateTarget");
	}
	
	@RequestMapping("/deleteEvaluateTarget")
	@ResponseBody
	public JSONObject deleteEvaluateTarget(@RequestBody JSONObject param){
		return postAction(param, "deleteEvaluateTarget");
	}
	
	@RequestMapping("/getEvaluateGroupList")
	@ResponseBody
	public JSONObject getEvaluateGroupList(@RequestBody JSONObject param){
		return postAction(param, "getEvaluateGroupList");
	}
	
	@RequestMapping("/createOrUpdateEvaluateGroup")
	@ResponseBody
	public JSONObject createOrUpdateEvaluateGroup(@RequestBody JSONObject param){
		return postAction(param, "createOrUpdateEvaluateGroup");
	}
	
	@RequestMapping("/deleteElectiveGroup")
	@ResponseBody
	public JSONObject deleteElectiveGroup(@RequestBody JSONObject param){
		return postAction(param, "deleteElectiveGroup");
	}
	
	@RequestMapping("/getEvaluatedGroupListFor")
	@ResponseBody
	public JSONObject getEvaluatedGroupListFor(@RequestBody JSONObject param){
		return postAction(param, "getEvaluatedGroupListFor");
	}
	
	@RequestMapping("/setEvaluatedGroupFor")
	@ResponseBody
	public JSONObject setEvaluatedGroupFor(@RequestBody JSONObject param){
		return postAction(param, "setEvaluatedGroupFor");
	}
	
	@RequestMapping("/startAlyEvaluate")
	@ResponseBody
	public JSONObject startAlyEvaluate(@RequestBody JSONObject param){
		return postAction(param, "startAlyEvaluate");
	}
	
	@RequestMapping("/getAlyEvaluateProgress")
	@ResponseBody
	public JSONObject getAlyEvaluateProgress(@RequestBody JSONObject param){
		return postAction(param, "getAlyEvaluateProgress");
	}
	
	@RequestMapping("/defaultSetEvaluatedGroup")
	@ResponseBody
	public JSONObject defaultSetEvaluatedGroup(@RequestBody JSONObject param){
		return postAction(param, "defaultSetEvaluatedGroup");
	}
	
	@RequestMapping("/getTeacherEvaluateList")
	@ResponseBody
	public JSONObject getTeacherEvaluateList(@RequestBody JSONObject param){
		return postAction(param, "getTeacherEvaluateList");
	}
	
	@RequestMapping("/createEvaluate")
	@ResponseBody
	public JSONObject createEvaluate(@RequestBody JSONObject param){
		return postAction(param, "createEvaluate");
	}
	
	@RequestMapping("/updateEvaluate")
	@ResponseBody
	public JSONObject updateEvaluate(@RequestBody JSONObject param){
		return postAction(param, "updateEvaluate");
	}
	
	@RequestMapping("/updateEvaluateTime")
	@ResponseBody
	public JSONObject updateEvaluateTime(@RequestBody JSONObject param){
		return postAction(param, "updateEvaluateTime");
	}
	
	@RequestMapping("/getEvaluateObject")
	@ResponseBody
	public JSONObject getEvaluateObject(@RequestBody JSONObject param){
		return postAction(param, "getEvaluateObject");
	}
	
	@RequestMapping("/updateEvaluateObject")
	@ResponseBody
	public JSONObject updateEvaluateObject(@RequestBody JSONObject param){
		return postAction(param, "updateEvaluateObject");
	}
	
	@RequestMapping("/deleteEvaluate")
	@ResponseBody
	public JSONObject deleteEvaluate(@RequestBody JSONObject param){
		return postAction(param, "deleteEvaluate");
	}
	
	@RequestMapping("/publishEvaluateToTeachers")
	@ResponseBody
	public JSONObject publishEvaluateToTeachers(@RequestBody JSONObject param){
		return postAction(param, "publishEvaluateToTeachers");
	}
	
	@RequestMapping("/getEvaluatedGroupList")
	@ResponseBody
	public JSONObject getEvaluatedGroupList(@RequestBody JSONObject param){
		return postAction(param, "getEvaluatedGroupList");
	}
	
	@RequestMapping("/createOrUpdateEvaluatedGroup")
	@ResponseBody
	public JSONObject createOrUpdateEvaluatedGroup(@RequestBody JSONObject param){
		return postAction(param, "createOrUpdateEvaluatedGroup");
	}
	
	@RequestMapping("/updateGroupHeads")
	@ResponseBody
	public JSONObject updateGroupHeads(@RequestBody JSONObject param){
		return postAction(param, "updateGroupHeads");
	}
	
	@RequestMapping("/getEvaluatedGroupAllMembers")
	@ResponseBody
	public JSONObject getEvaluatedGroupAllMembers(@RequestBody JSONObject param){
		return postAction(param, "getEvaluatedGroupAllMembers");
	}
	
	@RequestMapping("/getEvaluatedGroupMembers")
	@ResponseBody
	public JSONObject getEvaluatedGroupMembers(@RequestBody JSONObject param){
		return postAction(param, "getEvaluatedGroupMembers");
	}
	
	@RequestMapping("/copyHisEvaluateData")
	@ResponseBody
	public JSONObject copyHisEvaluateData(@RequestBody JSONObject param){
		return postAction(param, "copyHisEvaluateData");
	}
	
	@RequestMapping("/deleteElectivedGroup")
	@ResponseBody
	public JSONObject deleteElectivedGroup(@RequestBody JSONObject param){
		return postAction(param, "deleteElectivedGroup");
	}
	
	@RequestMapping("/teacher/getEvaluateGroupInfoList")
	@ResponseBody
	public JSONObject getEvaluateGroupInfoList(@RequestBody JSONObject param){
		return postAction(param, "teacher/getEvaluateGroupInfoList");
	}
	
	@RequestMapping("/teacher/getEvaluateTecherTarget")
	@ResponseBody
	public JSONObject getEvaluateTecherTarget(@RequestBody JSONObject param){
		return postAction(param, "teacher/getEvaluateTecherTarget");
	}
	
	@RequestMapping("/teacher/updateEvaluateTecherTarget")
	@ResponseBody
	public JSONObject updateEvaluateTecherTarget(@RequestBody JSONObject param){
		return postAction(param, "teacher/updateEvaluateTecherTarget");
	}
	
	@RequestMapping("/getEvaluateExcellent")
	@ResponseBody
	public JSONObject getEvaluateExcellent(@RequestBody JSONObject param){
		return postAction(param, "getEvaluateExcellent");
	}
	
	@RequestMapping("/createEvaluateExcellent")
	@ResponseBody
	public JSONObject createEvaluateExcellent(@RequestBody JSONObject param){
		return postAction(param, "createEvaluateExcellent");
	}
	
	@RequestMapping("/updateEvaluateExcellent")
	@ResponseBody
	public JSONObject updateEvaluateExcellent(@RequestBody JSONObject param){
		return postAction(param, "updateEvaluateExcellent");
	}
	
	//EvaluationStaticAction
	@RequestMapping("/static/getStaticResult")
	@ResponseBody
	public JSONObject getStaticResult(@RequestBody JSONObject param){
		return postAction(param, "static/getStaticResult");
	}
	
	@RequestMapping("/static/getEvaluateResult")
	@ResponseBody
	public JSONObject getEvaluateResult(@RequestBody JSONObject param){
		return postAction(param, "static/getEvaluateResult");
	}
	
	@RequestMapping("/teacher/getTeacherEvaluatedResult")
	@ResponseBody
	public JSONObject getTeacherEvaluatedResult(@RequestBody JSONObject param){
		return postAction(param, "teacher/getTeacherEvaluatedResult");
	}
	
	@RequestMapping("/static/getEvaluateGroupMemberIsEvaluate")
	@ResponseBody
	public JSONObject getEvaluateGroupMemberIsEvaluate(@RequestBody JSONObject param){
		return postAction(param, "static/getEvaluateGroupMemberIsEvaluate");
	}
	
	@RequestMapping("/static/sendMsgToNoEvalPerson")
	@ResponseBody
	public JSONObject sendMsgToNoEvalPerson(@RequestBody JSONObject param){
		return postAction(param, "static/sendMsgToNoEvalPerson");
	}
	
	private JSONObject postAction(JSONObject param, String action) {
		return postAction(param, action, null);
	}
	
	private JSONObject postAction(JSONObject param, String action, T_Role role) {
		return SplitUtil.postAction(request, rootPath + action, param, role);		
	}

}
