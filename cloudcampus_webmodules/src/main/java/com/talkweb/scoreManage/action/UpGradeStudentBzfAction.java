package com.talkweb.scoreManage.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.scoreManage.service.ScoreUpGradeService;
import com.talkweb.system.domain.business.TGmScorestubzf;
import com.talkweb.system.domain.business.TGmScorestubzfMk;

@Controller
@RequestMapping("/scoremanage1/upGrade/")
public class UpGradeStudentBzfAction extends BaseAction {

	@Autowired
	private ScoreUpGradeService scoreUpGradeService;

	@RequestMapping(value = "updateBzf", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateBzf(HttpServletRequest req, HttpServletResponse res) {
		JSONObject obj = new JSONObject();
		int xx = 0;
		List<JSONObject> allKs = scoreUpGradeService.getAllKslc();
		scoreUpGradeService.deleteAllStuBzfRes();
		long d1 = new Date().getTime();
		for (JSONObject ks : allKs) {
			String xn = ks.getString("xn");
			String xqm = ks.getString("xqm");
			String kslcdm = ks.getString("kslcdm");
			String xxdm = ks.getString("xxdm");
			HashMap<String, Object> cxMap = new HashMap<String, Object>();
			cxMap.put("xn", xn);
			cxMap.put("xqm", xqm);
			cxMap.put("kslc", kslcdm);
			cxMap.put("xxdm", xxdm);

			HashMap<String, JSONObject> bjBzfMap = scoreUpGradeService.getBjBzfObj(cxMap);
			if (bjBzfMap.keySet().size() == 0) {
				System.out.println("略过--");
				continue;
			} else {
				xx++;
			}
			HashMap<String, JSONObject> bjkmBfzMap = scoreUpGradeService.getBjKmBzfObj(cxMap);
			HashMap<String, JSONObject> stuCjMap = scoreUpGradeService.getXhBhMapByScore(cxMap);

			List<TGmScorestubzf> xszfBzfList = new ArrayList<TGmScorestubzf>();
			List<TGmScorestubzfMk> xskmBzfList = new ArrayList<TGmScorestubzfMk>();

			for (Iterator<String> xsKey = stuCjMap.keySet().iterator(); xsKey.hasNext();) {
				String xh = xsKey.next();
				JSONObject stuObj = stuCjMap.get(xh);
				float zf = stuObj.getFloatValue("zf");
				String bh = stuObj.getString("bh");
				JSONObject bjzfObj = bjBzfMap.get(bh);
				if (bjzfObj != null) {
					// 计算学生总分标准分
					float bjzfBzc = bjzfObj.getFloatValue("bzc");
					float bjzfPjf = bjzfObj.getFloatValue("pjf");
					float xszfbzf = 0;
					if (bjzfBzc != 0) {
						xszfbzf = ScoreUtil.castFloatTowPointNum((zf - bjzfPjf) / bjzfBzc);
					} else {
						System.out.println("---存在总分0分差班级:" + bh + "--" + kslcdm);
					}
					TGmScorestubzf stubzf = new TGmScorestubzf();
					stubzf.setBzf(xszfbzf);
					stubzf.setKslc(kslcdm);
					stubzf.setXh(xh);
					stubzf.setXn(xn);
					stubzf.setXqm(xqm);
					stubzf.setXxdm(xxdm);
					xszfBzfList.add(stubzf);

					List<JSONObject> xskmList = (List<JSONObject>) stuObj.get("xskmList");
					for (JSONObject xskm : xskmList) {
						String kmdm = xskm.getString("kmdm");
						float dkcj = xskm.getFloatValue("cj");
						String bjkmKey = bh + "_" + kmdm;
						JSONObject bjdk = bjkmBfzMap.get(bjkmKey);
						if (bjdk != null) {
							float bjdkBzc = bjdk.getFloatValue("bzc");
							float bjdkPjf = bjdk.getFloatValue("pjf");
							float xsdkbzf = 0;
							if (bjdkBzc != 0) {
								xsdkbzf = ScoreUtil.castFloatTowPointNum((dkcj - bjdkPjf) / bjdkBzc);
							} else {
								System.out.println("---存在单科0分差班级:" + bh + "--" + kslcdm + "--" + kmdm);
							}
							TGmScorestubzfMk stubzfmk = new TGmScorestubzfMk();
							stubzfmk.setBzf(xsdkbzf);
							stubzfmk.setKslc(kslcdm);
							stubzfmk.setXh(xh);
							stubzfmk.setXn(xn);
							stubzfmk.setXqm(xqm);
							stubzfmk.setXxdm(xxdm);
							stubzfmk.setKmdm(kmdm);
							xskmBzfList.add(stubzfmk);
						}
					}
				}
			}

			try {
				long d11 = new Date().getTime();
				cxMap.put("zfBzfList", xszfBzfList);
				cxMap.put("kmBzfList", xskmBzfList);
				scoreUpGradeService.updateUpGradeResult(cxMap);
				long d12 = new Date().getTime();
				System.out.println("---更新成绩耗时：" + (d12 - d11));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (xx == 10) {

				// break;
			}
		}
		long d2 = new Date().getTime();

		System.out.println("---查询及更新成绩耗时：" + (int) ((d2 - d1) / 60 / 1000) + "分");

		return obj;

	}
}
