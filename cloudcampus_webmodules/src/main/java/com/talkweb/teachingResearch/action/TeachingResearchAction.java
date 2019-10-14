package com.talkweb.teachingResearch.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.util.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.utils.JSONUtil;
import com.talkweb.utils.SplitUtil;

@RequestMapping("/teachingResearch")
@RestController
public class TeachingResearchAction {
	@Autowired
	private FileServer fileServerImplFastDFS;
	@Autowired
	private HttpServletRequest request;

	String rootPath = SplitUtil.getRootPath("evaluation.url") + "teachingResearch/";

	@RequestMapping("/visibleResults/importExcel")
	@ResponseBody
	public JSONObject importExcel(HttpServletRequest req, 
			@RequestParam("excelBody") MultipartFile file,
			@RequestParam("termInfoId") String termInfoId) {
		Object schoolId = request.getSession().getAttribute("xxdm");
		JSONObject param = new JSONObject();
		param.put("termInfo", termInfoId);
		param.put("schoolId", schoolId);
		param.put("sessionId", req.getSession().getId());
		return SplitUtil.postFile(rootPath + "visibleResults/importExcel", file, "excelBody", param);
	}

	@RequestMapping("/visibleResults/getMatchExcel")
	@ResponseBody
	public JSONObject getMatchExcel() {
		return postAction(new JSONObject(), "visibleResults/getMatchExcel");
	}

	@RequestMapping("/visibleResults/startImportTask")
	@ResponseBody
	public JSONObject startImportTask(@RequestBody JSONObject param) {
		return postAction(param, "visibleResults/startImportTask");
	}

	@RequestMapping("/visibleResults/continueImport")
	@ResponseBody
	public JSONObject continueImport() {
		return postAction(new JSONObject(), "visibleResults/continueImport");
	}

	@RequestMapping("/visibleResults/getImportProgress")
	@ResponseBody
	public JSONObject getImportProgress() {
		return postAction(new JSONObject(), "visibleResults/getImportProgress");
	}

	@RequestMapping("/visibleResults/updateDateCheck")
	@ResponseBody
	public JSONObject updateDateCheck(@RequestBody JSONObject param) {
		return postAction(param, "visibleResults/updateDateCheck");
	}

	@RequestMapping("/visibleResults/exportWrongMsg")
	@ResponseBody
	public void exportWrongMsg(HttpServletRequest req, HttpServletResponse res) {
		SplitUtil.exportExcelWithData(req, res);
	}

	@RequestMapping("/app/queryRecordByTeacherForApp")
	@ResponseBody
	public JSONObject queryRecordByTeacherForApp(@RequestBody JSONObject param) {
		return postAction(param, "app/queryRecordByTeacherForApp");
	}

	@RequestMapping("/app/initLevelInfoForApp")
	@ResponseBody
	public JSONObject initLevelInfoForApp(@RequestBody JSONObject param) {
		return postAction(param, "app/initLevelInfoForApp");
	}

	@RequestMapping("/app/initLessonInfoForApp")
	@ResponseBody
	public JSONObject initLessonInfoForApp(@RequestBody JSONObject param) {
		return postAction(param, "app/initLessonInfoForApp");
	}

	@RequestMapping("/app/initGradeInfoForApp")
	@ResponseBody
	public JSONObject initGradeInfoForApp(@RequestBody JSONObject param) {
		return postAction(param, "app/initGradeInfoForApp");
	}

	@RequestMapping("/app/initRewardForApp")
	@ResponseBody
	public JSONObject initRewardForApp(@RequestBody JSONObject param) {
		return postAction(param, "app/initRewardForApp");
	}

	@RequestMapping("/app/initJudgeDetailForApp")
	@ResponseBody
	public JSONObject initJudgeDetailForApp(@RequestBody JSONObject param) {
		return postAction(param, "app/initJudgeDetailForApp");
	}

	@RequestMapping("/app/editRecordByManageForApp")
	@ResponseBody
	public JSONObject editRecordByManageForApp(@RequestBody JSONObject param) {
		return postAction(param, "app/editRecordByManageForApp");
	}

	@RequestMapping("/app/deleteRecordByIdForApp")
	@ResponseBody
	public JSONObject deleteRecordByIdForApp(@RequestBody JSONObject param) {
		return postAction(param, "app/deleteRecordByIdForApp");
	}

	@RequestMapping("/common/editRecordByManage")
	@ResponseBody
	public JSONObject editRecordByManage(@RequestBody JSONObject param) {
		return postAction(param, "common/editRecordByManage");
	}

	private final Integer IMAGE_WIDTH = 800;
	private final Integer IMAGE_HEIGHT = 600;

	@RequestMapping("/common/uploadFile")
	@ResponseBody
	public JSONObject uploadFile(@RequestParam("fileBoby") MultipartFile file, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject param = new JSONObject();
		String fileNameTemp = file.getOriginalFilename();
		String fileTemp1 = fileNameTemp.replace("\\", "/");
		String fileName = fileTemp1.substring(fileTemp1.lastIndexOf("/") + 1);

		String suffix = fileNameTemp.substring(fileNameTemp.lastIndexOf(".") + 1);
		String[] picSubfix = new String[] { "JPG", "JREG", "PNG", "GIF", "BMP" };
		String[] pdfSubfix = new String[] { "PDF" };
		List<String> picExtension = Arrays.asList(picSubfix);
		List<String> pdfExtension = Arrays.asList(pdfSubfix);
		if (!picExtension.contains(suffix.toUpperCase()) && !pdfExtension.contains(suffix.toUpperCase())) {
			return JSONUtil.getResponse("-1", "文件格式不正确，请上传jpg,jpeg,png,gif,pdf格式文件");
		}
		try {
			byte[] bt = IOUtils.toByteArray(file.getInputStream());
			if (picExtension.contains(suffix.toUpperCase())) {
				ByteArrayInputStream bis = new ByteArrayInputStream(bt);
				byte[] outBuff = SplitUtil.compressImage(bis, IMAGE_WIDTH, IMAGE_HEIGHT, suffix);
				if (outBuff != null) bt = outBuff;
			}
			String url = fileServerImplFastDFS.uploadFile(bt, fileNameTemp);
			param.put("attachmentName", fileName);
			param.put("attachmentAddr", url);
			return postAction(param, "common/uploadFile");
		} catch (Exception e) {
			e.printStackTrace();
			return JSONUtil.getResponse("-1", "服务器错误，上传失败");
		}
	}

	@RequestMapping("/common/downloadFile")
	@ResponseBody
	public void downloadFile(@RequestParam("attachmentId") String attachmentId, HttpServletResponse res) {
		JSONObject param = new JSONObject();
		param.put("attachmentId", attachmentId);
		JSONObject data = postAction(param, "common/downloadFile");
		if (data != null && data.getIntValue("code") >= 0) {
			String attachmentName = data.getString("attachmentName");
			String attachmentAddr = data.getString("attachmentAddr");
			try {
				byte[] bt = getByteStream(attachmentAddr);
				SplitUtil.downloadStream(res, attachmentName, bt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@RequestMapping("/common/preDownloadFile")
	@ResponseBody
	public void preDownloadFile(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
		String urlTemp = req.getParameter("url");
		String url = URLDecoder.decode(urlTemp, "UTF-8");
		float outwidth = Float.parseFloat(req.getParameter("width"));
		float outheight = Float.parseFloat(req.getParameter("height"));
		try {
			String suffix = urlTemp.substring(urlTemp.lastIndexOf(".") + 1);
			String[] picSubfix = new String[] { "JPG", "JREG", "PNG", "GIF" };
			List<String> picExtension = Arrays.asList(picSubfix);

			if (picExtension.contains(suffix.toUpperCase())) {
				BufferedOutputStream bos = null;
				try {
					byte[] buff = SplitUtil.urlToBytes(url);
					ByteArrayInputStream bis = new ByteArrayInputStream(buff);
					byte[] outBuff = SplitUtil.compressImage(bis, outwidth, outheight, suffix);
					if (outBuff == null) outBuff = buff;
					
					res.setContentType("image/jpeg");
					bos = new BufferedOutputStream(res.getOutputStream());
					bos.write(outBuff);
					bos.flush();
				} finally {
					if (bos != null)
						try {
							bos.close();
						} catch (IOException e) {}
				}
			} else {
				byte[] bt = getByteStream(url);
				String fileName = urlTemp.split("/")[urlTemp.split("/").length - 1];
				SplitUtil.downloadStream(res, fileName, bt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/common/packDownloadFile", method = RequestMethod.POST)
	public void packDownloadFile(@RequestParam("recordId") String recordId, @RequestParam("xnxqName") String xnxqName,
			@RequestParam("selectedSemester") String xnxq, HttpServletResponse res) {
		JSONObject param = new JSONObject();
		param.put("recordId", recordId);
		param.put("xnxq", xnxq);
		JSONObject ret = postAction(param, "common/packDownloadFile");
		if (ret != null && ret.getIntValue("code") >= 0) {
			JSONObject fileList = ret.getJSONObject("data");
			try {
				byte[] zip = getTeacherFileByServer(fileList);
				SplitUtil.downloadStream(res, xnxqName + "老师的获奖附件.rar", zip);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@RequestMapping("/common/deleteRecordById")
	@ResponseBody
	public JSONObject deleteRecordById(@RequestBody JSONObject param) {
		return postAction(param, "common/deleteRecordById");
	}

	@RequestMapping("/common/deleteFileById")
	@ResponseBody
	public JSONObject deleteFileById(@RequestBody JSONObject param) {
		return postAction(param, "common/deleteFileById");
	}

	@RequestMapping("/common/initReward")
	@ResponseBody
	public JSONObject initReward(@RequestBody JSONObject param) {
		return postAction(param, "common/initReward");
	}

	@RequestMapping("/common/initJudgeDetail")
	@ResponseBody
	public JSONObject initJudgeDetail(@RequestBody JSONObject param) {
		return postAction(param, "common/initJudgeDetail");
	}

	@RequestMapping("/common/initLevelInfo")
	@ResponseBody
	public JSONObject initLevelInfo(@RequestBody JSONObject param) {
		return postAction(param, "common/initLevelInfo");
	}

	@RequestMapping("/common/initLessonInfo")
	@ResponseBody
	public JSONObject initLessonInfo(@RequestBody JSONObject param) {
		return postAction(param, "common/initLessonInfo");
	}

	@RequestMapping("/common/initGradeInfo")
	@ResponseBody
	public JSONObject initGradeInfo(@RequestBody JSONObject param) {
		return postAction(param, "common/initGradeInfo");
	}

	@RequestMapping("/record/queryRecordByManage")
	@ResponseBody
	public JSONObject queryRecordByManage(@RequestBody JSONObject param) {
		return postAction(param, "record/queryRecordByManage");
	}

	@RequestMapping("/record/queryRecordByRecordId")
	@ResponseBody
	public JSONObject queryRecordByRecordId(@RequestBody JSONObject param) {
		return postAction(param, "record/queryRecordByRecordId");
	}

	@RequestMapping("/record/saveReviewScore")
	@ResponseBody
	public JSONObject saveReviewScore(@RequestBody JSONObject param) {
		return postAction(param, "record/saveReviewScore");
	}

	@RequestMapping("/teacher/queryRecordByTeacher")
	@ResponseBody
	public JSONObject queryRecordByTeacher(@RequestBody JSONObject param) {
		return postAction(param, "teacher/queryRecordByTeacher");
	}

	@RequestMapping("/teacher/rewardScore")
	@ResponseBody
	public JSONObject srewardScore(@RequestBody JSONObject param) {
		return postAction(param, "teacher/rewardScore");
	}

	@RequestMapping("/type/queryTypeList")
	@ResponseBody
	public JSONObject queryTypeList(@RequestBody JSONObject param) {
		return postAction(param, "type/queryTypeList");
	}

	@RequestMapping("/type/createRewardType")
	@ResponseBody
	public JSONObject createRewardType(@RequestBody JSONObject param) {
		return postAction(param, "type/createRewardType");
	}

	@RequestMapping("/type/queryRewardTypeById")
	@ResponseBody
	public JSONObject queryRewardTypeById(@RequestBody JSONObject param) {
		return postAction(param, "type/queryRewardTypeById");
	}

	@RequestMapping("/type/editReward")
	@ResponseBody
	public JSONObject editReward(@RequestBody JSONObject param) {
		return postAction(param, "type/editReward");
	}

	@RequestMapping("/type/createJudgeDetail")
	@ResponseBody
	public JSONObject createJudgeDetail(@RequestBody JSONObject param) {
		return postAction(param, "type/createJudgeDetail");
	}

	@RequestMapping("/type/queryRewardDetailList")
	@ResponseBody
	public JSONObject queryRewardDetailList(@RequestBody JSONObject param) {
		return postAction(param, "type/queryRewardDetailList");
	}

	@RequestMapping("/type/deleteJudgeDetailById")
	@ResponseBody
	public JSONObject deleteJudgeDetailById(@RequestBody JSONObject param) {
		return postAction(param, "type/deleteJudgeDetailById");
	}

	@RequestMapping("/type/queryRewardTypeBase")
	@ResponseBody
	public JSONObject queryRewardTypeBase(@RequestBody JSONObject param) {
		return postAction(param, "type/queryRewardTypeBase");
	}

	@RequestMapping("/type/updateJudeDetailById")
	@ResponseBody
	public JSONObject updateJudeDetailById(@RequestBody JSONObject param) {
		return postAction(param, "type/updateJudeDetailById");
	}

	@RequestMapping("/type/createRewardBase")
	@ResponseBody
	public JSONObject createRewardBase(@RequestBody JSONObject param) {
		return postAction(param, "type/createRewardBase");
	}

	@RequestMapping("/type/deleteRewardBase")
	@ResponseBody
	public JSONObject deleteRewardBase(@RequestBody JSONObject param) {
		return postAction(param, "type/deleteRewardBase");
	}

	@RequestMapping("/type/uploadExcel")
	@ResponseBody
	public JSONObject uploadExcel(@RequestParam("excelBody") MultipartFile file, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject param = new JSONObject();
		param.put("sessionId", req.getSession().getId());
		param.put("schoolId", request.getSession().getAttribute("xxdm"));
		return SplitUtil.postFile(rootPath + "type/uploadExcel", file, "excelBody", param);
	}

	@RequestMapping("/type/importRewardDetail")
	@ResponseBody
	public JSONObject importRewardDetail(@RequestBody JSONObject param) {
		return postAction(param, "type/importRewardDetail");
	}

	@RequestMapping("/visibleResults/queryVisibleResults")
	@ResponseBody
	public JSONObject queryVisibleResults(@RequestBody JSONObject param) {
		return postAction(param, "visibleResults/queryVisibleResults");
	}

	@RequestMapping("/visibleResults/updateVisibleResults")
	@ResponseBody
	public JSONObject updateVisibleResults(@RequestBody JSONObject param) {
		return postAction(param, "visibleResults/updateVisibleResults");
	}

	protected void initParameter(HttpServletRequest req, JSONObject param) {
		param.put("sessionId", request.getSession().getId());
	}

	private JSONObject postAction(JSONObject param, String action) {
		return SplitUtil.postAction(request, rootPath + action, param);
	}

	@SuppressWarnings("unchecked")
	private byte[] getTeacherFileByServer(JSONObject fileList) throws IOException, Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ZipOutputStream out = new ZipOutputStream(bout);
		for (String key : fileList.keySet()) {
			List<JSONObject> value = (List<JSONObject>) fileList.get(key);
			if (CollectionUtils.isNotEmpty(value) && value.size() > 0) {
				for (JSONObject jsonObject : value) {
					if (jsonObject.containsKey("attachmentAddr")) {
						String attachmentName = jsonObject.getString("attachmentName");
						String attachmentAddr = jsonObject.getString("attachmentAddr");
						byte[] bt = getByteStream(attachmentAddr);
						if (bt != null && bt.length > 0) {
							out.putNextEntry(new ZipEntry(jsonObject.getString("teacherName") + "/" + attachmentName));
							// 设置压缩文件内的字符编码，不然会变成乱码
							out.setEncoding("GBK");
							out.write(bt);
						}
					}
				}
			}
		}
		out.closeEntry();
		out.close();
		byte[] ret = bout.toByteArray();
		bout.close();
		return ret;
	}

	private byte[] getByteStream(String url) throws IOException, Exception {
		byte[] bt = null;
		if (url.startsWith("http")) {
			bt = SplitUtil.urlToBytes(url);
		} else {
			bt = fileServerImplFastDFS.downloadFile(url);
		}
		return bt;
	}

}
