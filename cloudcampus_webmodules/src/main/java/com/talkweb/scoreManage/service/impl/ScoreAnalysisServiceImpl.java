package com.talkweb.scoreManage.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.splitDbAndTable.TermInfoIdUtils;
import com.talkweb.common.tools.BeanTool;
import com.talkweb.common.tools.GrepUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.action.ScoreUtil;
import com.talkweb.scoreManage.dao.ScoreAnalysisDao;
import com.talkweb.scoreManage.dao.ScoreManageDao;
import com.talkweb.scoreManage.po.gm.AppStudentScoreReport;
import com.talkweb.scoreManage.po.gm.ClassScoreLevelMk;
import com.talkweb.scoreManage.po.gm.ClassScoreLevelSequnce;
import com.talkweb.scoreManage.po.gm.CompetitionStu;
import com.talkweb.scoreManage.po.gm.CompetitionStuStatistics;
import com.talkweb.scoreManage.po.gm.Dbkslc;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.po.gm.GroupScoreLevelMk;
import com.talkweb.scoreManage.po.gm.Kmmfqj;
import com.talkweb.scoreManage.po.gm.ScoreClassDistribute;
import com.talkweb.scoreManage.po.gm.ScoreClassDistributeMk;
import com.talkweb.scoreManage.po.gm.ScoreClassStatistics;
import com.talkweb.scoreManage.po.gm.ScoreClassStatisticsMk;
import com.talkweb.scoreManage.po.gm.ScoreClassStatisticsMkRange;
import com.talkweb.scoreManage.po.gm.ScoreClassStatisticsRange;
import com.talkweb.scoreManage.po.gm.ScoreDistribute;
import com.talkweb.scoreManage.po.gm.ScoreDistributeKm;
import com.talkweb.scoreManage.po.gm.ScoreGroupStatistics;
import com.talkweb.scoreManage.po.gm.ScoreGroupStatisticsMk;
import com.talkweb.scoreManage.po.gm.ScoreGroupStatisticsMkRange;
import com.talkweb.scoreManage.po.gm.ScoreGroupStatisticsRange;
import com.talkweb.scoreManage.po.gm.ScoreLevelSequnce;
import com.talkweb.scoreManage.po.gm.ScoreRankStatistics;
import com.talkweb.scoreManage.po.gm.ScoreRankStatisticsMk;
import com.talkweb.scoreManage.po.gm.ScoreStuBzf;
import com.talkweb.scoreManage.po.gm.ScoreStuBzfMk;
import com.talkweb.scoreManage.po.gm.ScoreStuJzsqns;
import com.talkweb.scoreManage.po.gm.ScoreStuJzsqnsMk;
import com.talkweb.scoreManage.po.gm.ScoreStuStatisticsRank;
import com.talkweb.scoreManage.po.gm.ScoreStuStatisticsRankMk;
import com.talkweb.scoreManage.po.gm.SynthScore;
import com.talkweb.scoreManage.po.gm.Zfmfqj;
import com.talkweb.scoreManage.po.sar.AnalysisReportBj;
import com.talkweb.scoreManage.po.sar.AnalysisReportNj;
import com.talkweb.scoreManage.po.sar.AnalysisReportStu;
import com.talkweb.scoreManage.po.sar.SettingBJ;
import com.talkweb.scoreManage.proc.ProgressBar;
import com.talkweb.scoreManage.proc.ScoreProgressProc;
import com.talkweb.scoreManage.service.ScoreAnalysisService;
import com.talkweb.scoreManage.service.ScoreManageService;

/**
 * @ClassName ScoreAnalysisServiceImpl
 * @author Homer
 * @version 1.0
 * @Description 成绩分析业务逻辑接口实现类
 * @date 2015年3月26日
 */
@Service
public class ScoreAnalysisServiceImpl implements ScoreAnalysisService {
	private static final Logger logger = LoggerFactory.getLogger(ScoreAnalysisServiceImpl.class);
	
	@Autowired
	private ScoreAnalysisDao sAlzDao;

	@Autowired
	private ScoreManageDao scoreDao;

	@Autowired
	private ScoreManageService scoreService;

	@Autowired
	private AllCommonDataService commonDataService;

	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;

	/**
	 * 获取临时文件保存目录
	 */
	@Value("#{settings['scoreReportFilePath']}")
	private String scoreReportFilePath;

	@SuppressWarnings("unchecked")
	public void scoreAnalysis(JSONObject params, ProgressBar progressBar) throws Exception {
		try {
			String progressKey = params.getString("progressKey"); // 进度条关键字
			String exkey = params.getString("exkey");
			School sch = (School) params.get("school");

			params.remove("progressKey");
			params.remove("exkey");
			params.remove("school");

			String xnxq = params.getString("xnxq"); // 学年学期
			String kslc = params.getString("kslc"); // 考试轮次代码
			String xxdm = params.getString("xxdm"); // 学校代码

			String xn = xnxq.substring(0, xnxq.length() - 1); // 学年

			progressBar.setProgressInfo(1, 0, "正在准备分析数据...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());

			ScoreProgressProc spThread = new ScoreProgressProc("正在准备分析数据...", 1, 25, 1, 0, progressKey);
			spThread.start();

			/*************************************** 考试轮次的准备工作 ************************************************/
			// 获取考试轮次信息
			DegreeInfo exam = scoreDao.getDegreeInfoById(xnxq, params);
			Integer autoIncr = exam.getAutoIncr();
			String kslcmc = exam.getKslcmc();

			// 获取考试年级信息
			List<String> njList = scoreDao.getNjFromDegreeInfoNj(xnxq, params);
			if (CollectionUtils.isEmpty(njList)) {
				throw new CommonRunException(-1, "数据库异常，查询年级为空，请联系管理员！");
			}

			// 取得最小的入学年度，从而方便遍历多个按学年学期所分的库
			int idx = 0;
			int minRxnd = Integer.parseInt(commonDataService.ConvertSYNJ2RXND(njList.get(0), xn));
			for (int i = 0, len = njList.size(); i < len; i++) {
				int rxnd = Integer.parseInt(commonDataService.ConvertSYNJ2RXND(njList.get(i), xn));
				if (minRxnd > rxnd) {
					minRxnd = rxnd;
					idx = i;
				}
			}

			// 所有的学年学期列表
			List<String> xnxqList = TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(commonDataService, njList.get(idx),
					xnxq);

			/*************************************** 获取分析配置信息 **********************************************/
			// 设置班级-年级map 高中不需要设置等第
			Map<String, Boolean> needDdByMap = new HashMap<String, Boolean>(); // 班级代码映射是否等第值
			Map<String, String> bhNjNameMap = new HashMap<String, String>(); // 班级映射年级名称

			List<Grade> grades = commonDataService.getGradeList(sch, xnxq);
			for (Grade gd : grades) {
				// 不是毕业的年级
				if (gd == null || gd.isGraduate || gd.getClassIds() == null) {
					continue;
				}
				T_GradeLevel gl = gd.getCurrentLevel();
				boolean need = false;
				if (T_GradeLevel.T_JuniorOne.equals(gl) || T_GradeLevel.T_JuniorTwo.equals(gl)
						|| T_GradeLevel.T_JuniorThree.equals(gl) || T_GradeLevel.T_JuniorFour.equals(gl)) {
					need = true; // 中学需要设置等第值
				}

				List<Long> classIds = gd.getClassIds();
				for (Long cid : classIds) {
					String classId = String.valueOf(cid);
					needDdByMap.put(classId, need); // 需要设置等第值的班级
					bhNjNameMap.put(classId, AccountStructConstants.T_GradeLevelName.get(gl)); // 班级id对应年级名称
				}
			}

			// 分组代码下的单科科目对应的满分值
			List<JSONObject> kmszList = sAlzDao.getDkMf(xnxq, autoIncr, params);
			if (kmszList.size() == 0) {
				throw new CommonRunException(-1, "未设置【统计科目与满分】，无可分析科目!");
			}

			// 获取学生竞赛成绩列表
			List<CompetitionStu> xsjsCjList = sAlzDao.getXsjsCjList(xnxq, autoIncr, params);
			// 是否是竞赛科目，是否是竞赛学生
			Map<String, Boolean> isKmStuInJs = new HashMap<String, Boolean>();

			for (CompetitionStu xsjs : xsjsCjList) {
				String xh = xsjs.getXh();
				String kmdm = xsjs.getKmdm();

				isKmStuInJs.put(kmdm, true);
				isKmStuInJs.put(xh + "_" + kmdm, true);
			}

			// 获取总分及总分分数段规则设置
			List<JSONObject> zfszList = sAlzDao.getZfAndSz(xnxq, autoIncr, params);

			List<ScoreDistribute> scoreDistributeList = new ArrayList<ScoreDistribute>();
			// 总分区间
			List<Zfmfqj> zfmfqjList = new ArrayList<Zfmfqj>();

			Map<String, String> scdWlMap = new HashMap<String, String>();
			// 总分设置map
			Map<String, JSONObject> zfszMap = new HashMap<String, JSONObject>();

			for (JSONObject zfsz : zfszList) {
				String dm = UUIDUtil.getUUID();

				String disr = ScoreUtil.scoreDisSep(zfsz, "zf");

				Zfmfqj zfmf = new Zfmfqj();
				zfmf.setKslc(kslc);
				zfmf.setXxdm(xxdm);
				zfmf.setXnxq(xnxq);
				zfmf.setDm(dm);
				zfmf.setFzdm(zfsz.getString("fzdm"));
				zfmf.setMf(zfsz.getIntValue("zf"));
				zfmf.setNj(zfsz.getString("nj"));
				zfmfqjList.add(zfmf);

				List<ScoreDistribute> temp = ScoreUtil.getDistByZfmfqj(zfmf, disr);
				scoreDistributeList.addAll(temp);

				scdWlMap.put(dm, zfmf.getFzdm());
				zfszMap.put(zfmf.getNj(), zfsz);
			}

			// 获取合并科目列表
			List<SynthScore> hbkmList = sAlzDao.getSynthScoreList(xnxq, autoIncr, params);
			// 合并科目已统计单科的数量
			Map<String, Integer> hb_dkdmMap = new HashMap<String, Integer>();
			for (SynthScore hbkm : hbkmList) {
				String dykm = hbkm.getDykm();
				String kmdm = hbkm.getKmdm();
				String[] keyName = { "fzdm", "nj" };
				String[] keyValue = { hbkm.getFzdm(), hbkm.getNj() };
				List<JSONObject> fss = GrepUtil.grepJsonKeyByVal(keyName, keyValue, kmszList);
				// 计算合并科目满分
				int mf = 0;
				for (JSONObject oj : fss) {
					if (oj.containsKey("mf") && dykm.indexOf(oj.getString("kmdm")) > -1) {
						mf += oj.getIntValue("mf");
						if (hb_dkdmMap.containsKey(kmdm)) {
							hb_dkdmMap.put(kmdm, hb_dkdmMap.get(kmdm) + 1);
						} else {
							hb_dkdmMap.put(kmdm, 1);
						}
					}
				}

				JSONObject json = new JSONObject();
				json.put("nj", hbkm.getNj());
				json.put("fzdm", hbkm.getFzdm());
				json.put("kmdm", hbkm.getKmdm());
				json.put("mf", mf);
				kmszList.add(json);
			}

			// 需移除统计科目不足的合并科目
			List<SynthScore> needremovelist = new ArrayList<SynthScore>();
			// 需要移除的合并科目代码
			List<String> needRemoveHbkmdms = new ArrayList<String>();
			for (SynthScore hbkm : hbkmList) {
				String dykm = hbkm.getDykm();
				if (dykm != null && dykm.trim().length() > 0) {
					String[] dykms = dykm.split(",");
					int num = dykms.length;
					String kmdm = hbkm.getKmdm();
					if (!hb_dkdmMap.containsKey(kmdm) || hb_dkdmMap.get(kmdm) < num) {
						needremovelist.add(hbkm);
						needRemoveHbkmdms.add(kmdm);
					}
				}
			}
			// 移除缺科的合并科目
			hbkmList.removeAll(needremovelist);

			// 用于对总分设置去重
			Set<String> kmzfSet = new HashSet<String>();
			for (JSONObject kmmf : kmszList) {
				String key = new StringBuffer().append(kmmf.getString("fzdm")).append(",").append(kmmf.getString("nj"))
						.append(",").append(kmmf.getIntValue("mf")).toString();

				if (!kmzfSet.contains(key)) {
					kmzfSet.add(key);
				}
			}

			// 单科区间
			List<ScoreDistributeKm> kmScDisList = new ArrayList<ScoreDistributeKm>();
			List<Kmmfqj> kmmfqjList = new ArrayList<Kmmfqj>();
			for (String keyStr : kmzfSet) {
				String key[] = keyStr.split(",");

				String fzdm = key[0];
				String nj = key[1];
				int mf = Integer.parseInt(key[2]);

				JSONObject zfsz = zfszMap.get(nj);
				if (zfsz == null) {
					continue;
				}

				JSONObject zfszClone = (JSONObject) BeanTool.castBeanToFirstLowerKey(zfsz);
				zfszClone.put("mf", mf);
				String disr = ScoreUtil.scoreDisSep(zfszClone, "km");

				String dm = UUIDUtil.getUUID();
				Kmmfqj kmqj = new Kmmfqj();
				kmqj.setDm(dm);
				kmqj.setFzdm(fzdm);
				kmqj.setNj(nj);
				kmqj.setKslc(kslc);
				kmqj.setMf(mf);
				kmqj.setXxdm(xxdm);
				kmqj.setXnxq(xnxq);
				kmmfqjList.add(kmqj);

				List<ScoreDistributeKm> temp = ScoreUtil.getDisByKmmfqj(kmqj, disr);
				kmScDisList.addAll(temp);

				scdWlMap.put(dm, kmqj.getFzdm() + "," + mf);
			}

			// 获取等第设置列表
			List<JSONObject> ddszList = scoreDao.getAllDJList(xnxq, autoIncr, params);
			String djsx = "";
			for (int i = 0; i < ddszList.size(); i++) {
				JSONObject o = ddszList.get(i);
				String djmc = o.getString("levelName");
				o.put("djmc", djmc);
				djsx += "0" + djmc;
				if (djmc.equalsIgnoreCase("A")) {
					o.put("djz", 5);
					o.put("tmpdjz", 5);
				} else if (djmc.equalsIgnoreCase("B")) {
					o.put("djz", 4);
					o.put("tmpdjz", 4);
				} else if (djmc.equalsIgnoreCase("C")) {
					o.put("djz", 3);
					o.put("tmpdjz", 3);
				} else if (djmc.equalsIgnoreCase("D")) {
					o.put("djz", 2);
					o.put("tmpdjz", 2);
				} else if (djmc.equalsIgnoreCase("E")) {
					o.put("djz", 1);
					o.put("tmpdjz", 1);
				}
			}

			List<ScoreLevelSequnce> needInsertLvlSeq = new ArrayList<ScoreLevelSequnce>();
			Map<String, JSONObject> njSeqMap = new HashMap<String, JSONObject>();
			Map<String, String> njHbStr = new HashMap<String, String>();

			if (ddszList.size() > 0) {
				// 获取所有年级所有科目
				List<JSONObject> kmFordd = sAlzDao.getAllKmInLcForDD(xnxq, autoIncr, params);

				Map<String, List<JSONObject>> njkmMap = new HashMap<String, List<JSONObject>>();
				for (JSONObject km : kmFordd) {
					String nj = km.getString("nj").trim();
					if (!njkmMap.containsKey(nj)) {
						njkmMap.put(nj, new ArrayList<JSONObject>());
					}
					njkmMap.get(nj).add(km);
				}

				for (Map.Entry<String, List<JSONObject>> entry : njkmMap.entrySet()) {
					String nj = entry.getKey();
					List<JSONObject> l = entry.getValue();

					List<JSONObject> dkm = GrepUtil.grepJsonKeyByVal(new String[] { "kmlb" }, new String[] { "1" }, l);
					// 一般科目序列
					List<JSONObject> hbkm = GrepUtil.grepJsonKeyByVal(new String[] { "kmlb" }, new String[] { "2" }, l);
					List<JSONObject> xls = ScoreUtil.getDdXlQzByDjAndKmNum(ddszList, dkm.size());

					JSONObject njSeq = new JSONObject();
					List<ScoreLevelSequnce> seq1 = new ArrayList<ScoreLevelSequnce>();
					List<ScoreLevelSequnce> seq2 = new ArrayList<ScoreLevelSequnce>();
					List<ScoreLevelSequnce> seq3 = new ArrayList<ScoreLevelSequnce>();
					njSeq.put("1", seq1);
					njSeq.put("2", seq2);
					njSeq.put("3", seq3);

					njSeqMap.put(nj, njSeq);
					for (JSONObject xl : xls) {
						ScoreLevelSequnce tsq = new ScoreLevelSequnce();
						tsq.setDjqz(xl.getIntValue("djqz"));
						tsq.setDjxl(xl.getString("djxl"));
						tsq.setDjxllb("01");
						tsq.setKslc(kslc);
						tsq.setNj(nj);
						tsq.setXnxq(xnxq);
						tsq.setXxdm(xxdm);

						needInsertLvlSeq.add(tsq);
						seq1.add(tsq);
					}

					// 带综合科目序列
					int kmSeqNum02 = hbkm.size();
					String allStr = ",";
					for (JSONObject hkm : hbkm) {
						allStr += hkm.getString("dykm") + ",";
					}
					njHbStr.put(nj, allStr);

					for (JSONObject km : dkm) {
						if (allStr.indexOf("," + km.getString("kmdm") + ",") == -1) {
							kmSeqNum02++;
						}
					}

					List<JSONObject> xls2 = ScoreUtil.getDdXlQzByDjAndKmNum(ddszList, kmSeqNum02);
					for (JSONObject xl : xls2) {
						ScoreLevelSequnce tsq = new ScoreLevelSequnce();
						tsq.setDjqz(xl.getIntValue("djqz"));
						tsq.setDjxl(xl.getString("djxl"));
						tsq.setDjxllb("02");
						tsq.setKslc(kslc);
						tsq.setNj(nj);
						tsq.setXnxq(xnxq);
						tsq.setXxdm(xxdm);

						needInsertLvlSeq.add(tsq);
						seq2.add(tsq);
					}

					int kmSeqNum03 = 3;
					List<JSONObject> xls3 = ScoreUtil.getDdXlQzByDjAndKmNum(ddszList, kmSeqNum03);
					for (JSONObject xl : xls3) {
						ScoreLevelSequnce tsq = new ScoreLevelSequnce();
						tsq.setDjqz(xl.getIntValue("djqz"));
						tsq.setDjxl(xl.getString("djxl"));
						tsq.setDjxllb("03");
						tsq.setKslc(kslc);
						tsq.setNj(nj);
						tsq.setXnxq(xnxq);
						tsq.setXxdm(xxdm);

						needInsertLvlSeq.add(tsq);
						seq3.add(tsq);
					}
				}
			}

			// 获取不参与列表
			Map<String, JSONObject> xszfMap = new HashMap<String, JSONObject>();
			// 获取统计科目 列表
			List<JSONObject> tjkmList = sAlzDao.getTjKmInWl(xnxq, autoIncr, params);
			if (tjkmList.size() == 0) {
				throw new CommonRunException(-1, "未设置【统计科目与满分】，无可分析科目!");
			}

			Map<String, Map<String, Integer>> tjWlkmMap = new HashMap<String, Map<String, Integer>>();
			for (JSONObject tjkm : tjkmList) {
				String wlfz = tjkm.getString("wlfz").trim(); // 文理分组
				String kmdm = tjkm.getString("kmdm").trim(); // 科目代码
				if (StringUtils.isBlank(wlfz) || StringUtils.isBlank(kmdm)) {
					continue;
				}

				if (!tjWlkmMap.containsKey(wlfz)) {
					tjWlkmMap.put(wlfz, new HashMap<String, Integer>());
				}

				Map<String, Integer> m = tjWlkmMap.get(wlfz);
				m.put(kmdm, 1);
			}

			List<Long> bcyXh = sAlzDao.getBcytjXhList(xnxq, autoIncr, params);
			// 获取学生单科及合并科目成绩xh bh bjfz wlfz kmdm dykm cj tsqk
			JSONObject xscxMap = new JSONObject();
			xscxMap.put("xnxq", xnxq);
			xscxMap.put("xxdm", xxdm);
			xscxMap.put("xn", xn);
			JSONObject stuBase = scoreService.getAllStuByParam(xscxMap);

			Map<String, JSONObject> xjhMap = (Map<String, JSONObject>) stuBase.get("xjhMap");
			// 获取本次考试学生的单科成绩以及合并科目成绩
			List<JSONObject> xskmList = getSingleSubjectScore(sch, params, autoIncr, xjhMap);
			if (xskmList.size() == 0) {
				throw new CommonRunException(-1, "可分析学生成绩数据为空,建议检查导入的成绩、【文理分组】及【班级分组】！");
			}

			Set<String> bcyMap = new HashSet<String>();
			for (Long bxh : bcyXh) {
				bcyMap.add(String.valueOf(bxh));
			}

			List<JSONObject> delList = new ArrayList<JSONObject>();
			DecimalFormat df = new DecimalFormat("######0.00");
			// 用于过滤 不统计班级
			List<String> withScoreBj = new ArrayList<String>();

			Map<String, List<JSONObject>> xsHbcjMap = new HashMap<String, List<JSONObject>>();
			Map<String, JSONObject> xsZcjMap = new HashMap<String, JSONObject>();
			for (JSONObject xk : xskmList) {
				String xh = xk.getString("xh");
				String xm = xk.getString("xm");
				String bh = xk.getString("bh");
				if (!withScoreBj.contains(bh)) {
					withScoreBj.add(bh);
				}
				String wlfz = xk.getString("wlfz"); // 文理分组
				String bjfz = xk.getString("bjfz"); // 班级分组
				String kmdm = xk.getString("kmdm"); // 科目代码
				Double cj = xk.getDouble("cj"); // 成绩
				if(cj == null) {
					xk.put("cj", 0d);
					cj = 0d;
				}

				// 学生不在不参与统计列表
				if (!bcyMap.contains(xh) && xjhMap.containsKey(xh)) {
					if (tjWlkmMap.containsKey(wlfz) && tjWlkmMap.get(wlfz).containsKey(kmdm)) {

						if (!xsZcjMap.containsKey(xh)) {
							JSONObject o = new JSONObject();
							o.put("zcj", 0d);
							o.put("xh", xh);
							o.put("xm", xm);
							o.put("bh", bh);
							o.put("wlfz", wlfz);
							o.put("bjfz", bjfz);
							o.put("kmdm", kmdm);
							o.put("nj", xk.getString("nj"));
							o.put("bzzcj", cj);
							xsZcjMap.put(xh, o);
							xsHbcjMap.put(xh, new ArrayList<JSONObject>());
						}

						JSONObject o = xsZcjMap.get(xh);
						double zcj = o.getDouble("zcj");
						zcj += cj;
						zcj = Double.parseDouble(df.format(zcj));
						o.put("zcj", zcj);

						xsHbcjMap.get(xh).add(xk);

					} else {
						delList.add(xk);
					}
				} else {
					delList.add(xk);
				}
			}
			xskmList.removeAll(delList);

			logger.info("withScoreBj: "+JSONObject.toJSONString(withScoreBj));
			// 获取学生总成绩 xh bh bjfz wlfz zf
			List<JSONObject> xszfList = new ArrayList<JSONObject>();
			Map<String, JSONObject> xhMapScore = new HashMap<String, JSONObject>();

			// 深圳手机端h5接口数据要求
			JSONObject szDataExam = new JSONObject();
			szDataExam.put("examName", kslcmc);
			szDataExam.put("examTime", exam.getCdate().getTime());
			szDataExam.put("studentList", new JSONArray());
			JSONArray studentList = szDataExam.getJSONArray("studentList");
			for (Map.Entry<String, JSONObject> entry : xsZcjMap.entrySet()) {
				String key = entry.getKey();
				JSONObject xszf = entry.getValue();

				xszfList.add(xszf);

				JSONObject stu = new JSONObject();
				stu.put("accountId", Long.parseLong(key));
				stu.put("classId", Long.parseLong(xszf.getString("bh")));
				stu.put("name", xszf.get("xm"));
				stu.put("totalScore", xszf.getDouble("zcj") * 100);

				JSONObject mapScore = new JSONObject();

				List<JSONObject> scores = xsHbcjMap.get(key);
				for (JSONObject dk : scores) {
					mapScore.put(dk.getString("kmmc"), dk.getDouble("cj") * 100);
				}

				stu.put("mapScore", mapScore);
				xhMapScore.put(key, mapScore); // 学号对应科目成绩
				studentList.add(stu);
			}

			// 获取本次考试所有班级 bh,bjmc,bjfz,wlfz,nj
			List<JSONObject> bjList = getAllExamClass(sch, params, autoIncr);
			List<JSONObject> needremovebj = new ArrayList<JSONObject>();
			for (JSONObject bo : bjList) {
				String bh = bo.getString("bh");
				if (bh == null || !withScoreBj.contains(bh)) {
					needremovebj.add(bo);
				}
			}
			bjList.removeAll(needremovebj);
			if (bjList.size() == 0) {
				throw new CommonRunException(-1, "未查询到成绩中班级信息,建议检查分组设置或导入的成绩数据！");
			}

			JSONObject szDataClassList = new JSONObject();
			Map<String, JSONObject> bjJsonMap = new HashMap<String, JSONObject>();
			for (JSONObject bj : bjList) {
				bjJsonMap.put(bj.getString("bh"), bj);
				// szDataH5
				szDataClassList.put(bj.getString("bh"), bj.get("bjmc"));
			}
			szDataExam.put("classList", szDataClassList);

			// 获取统计条件参数 如舞弊、缺考等
			List<JSONObject> tempParam = sAlzDao.getCondionParam(params);
			// 获取所有科目等第设置
			List<JSONObject> allDjList = sAlzDao.getAllDdsz(xnxq, autoIncr, params);
			// 缺考舞弊零分 默认不计入平均分
			int qkbcy = 1, wbbcy = 1, lfbcy = 1;
			if (tempParam.size() > 0) {
				JSONObject t = tempParam.get(0);
				qkbcy = t.getIntValue("qk");
				wbbcy = t.getIntValue("wb");
				lfbcy = t.getIntValue("lf");
			}

			// 获取优秀率合格率、低分率设置参数 dm,fs,mc,fzbfb
			List<JSONObject> scoreSecList = sAlzDao.getSectionParam(params);
			// 获取文理分组及班级分组
			List<JSONObject> bjwlfz = sAlzDao.getWlfzAndBjfz(xnxq, autoIncr, params);
			if (bjwlfz.size() == 0) {
				throw new CommonRunException(-1, "本次考试默认设置失败导致无法分析，建议检查【文理分组】及【班级分组】，或重新导入成绩 ！");
			}

			JSONObject szDataSubjectList = new JSONObject();
			// 获取文理分组下的科目
			List<JSONObject> kmInWlfz = getKmInWlfz(sch, autoIncr, params);
			List<JSONObject> removeKmsInwl = new ArrayList<JSONObject>();
			for (JSONObject km : kmInWlfz) {
				String kmdm = km.getString("kmdm");
				int isHb = km.getIntValue("isHb");
				if (isHb == 1 && needRemoveHbkmdms.contains(kmdm)) {
					removeKmsInwl.add(km);
				}
				// szDataH5
				String kmmc = km.getString("kmmc");
				szDataSubjectList.put(km.getString("kmdm"), kmmc);
			}
			kmInWlfz.removeAll(removeKmsInwl);
			if (kmInWlfz.size() == 0) {
				throw new CommonRunException(-1, "本次考试无可分析科目，建议检查导入的成绩，并检查【统计科目与满分】及【合并科目成绩】！");
			}
			szDataExam.put("subjectList", szDataSubjectList);
			// 获取排名区间
			List<JSONObject> rankDisList = sAlzDao.getScoreRankDistribute(xnxq, autoIncr, params);

			// 获取总分区间 zfmfqjList kmmfqjList
			// 获取对比考试轮次设置
			List<Dbkslc> dbkslcList = sAlzDao.getDbkslc(xnxq, autoIncr, params);

			// 获取学生总分排名
			List<JSONObject> lastXsZfpmList = new ArrayList<JSONObject>();
			// 获取学生单科排名
			List<JSONObject> lastXsDkpmList = new ArrayList<JSONObject>();

			Map<String, JSONObject> lastXsZfMap = new HashMap<String, JSONObject>(); // 最近的学生总分排名（学号映射数据对象）
			Map<String, JSONObject> lastXsKmMap = new HashMap<String, JSONObject>(); // 最近的学生科目排名（学号映射数据对象）

			// 获取前次考试班级总分统计及科目统计
			Map<String, JSONObject> bhClaMap = new HashMap<String, JSONObject>();
			Map<String, JSONObject> bhClaKmMap = new HashMap<String, JSONObject>();

			String dbkslcs = null;

			if (dbkslcList.size() > 0) {
				dbkslcs = dbkslcList.get(0).getDbkslc();

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("xxdm", xxdm);

				for (Dbkslc dbkslc : dbkslcList) {
					String dbxnxq = dbkslc.getDbxnxq();
					String dbkslcdm = dbkslc.getDbkslc();
					map.put("xnxq", dbxnxq);
					map.put("kslc", dbkslcdm);
					map.put("kslcdm", dbkslcdm);

					DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(dbxnxq, map);
					if (degreeInfo == null) {
						continue;
					}
					Integer autoIncr2 = degreeInfo.getAutoIncr();

					// 获取最近一次考试的学生总分排名
					List<JSONObject> tmp1 = sAlzDao.getAllScoreRank(dbxnxq, autoIncr2, map);
					for (JSONObject xs : tmp1) {
						String xh = xs.getString("xh");
						xs.put("cdate", degreeInfo.getCdate().getTime());
						xs.put("kslcmc", degreeInfo.getKslcmc());
						if (!lastXsZfMap.containsKey(xh)) { // 如果没有学生排名数据，则直接插入数据
							lastXsZfMap.put(xh, xs);
						} else { // 如果已经存在学生排名数据，则判断时间是否是最近一次，是更最近的一次，则替换已经存在的数据
							JSONObject xsTmp = lastXsZfMap.get(xh);
							if (xs.getLongValue("cdate") > xsTmp.getLongValue("cdate")) {
								lastXsZfMap.put(xh, xs);
							}
						}
					}

					// 获取对比考试的学生单科排名
					List<JSONObject> tmp2 = sAlzDao.getSingleSubjectRank(dbxnxq, autoIncr2, map);
					for (JSONObject xs : tmp2) {
						String xh = xs.getString("xh");
						String kmdm = xs.getString("kmdm");
						String key = xh + "_" + kmdm;
						xs.put("cdate", degreeInfo.getCdate().getTime());
						xs.put("kslcmc", degreeInfo.getKslcmc());
						if (!lastXsKmMap.containsKey(key)) { // 如果没有学生科目排名数据，则直接插入数据
							lastXsKmMap.put(key, xs);
						} else { // 如果已经存在学生科目排名数据，则判断时间是否是最近一次，是更最近的一次，则替换已经存在的数据
							JSONObject xsTmp = lastXsKmMap.get(key);
							if (xs.getLongValue("cdate") > xsTmp.getLongValue("cdate")) {
								lastXsKmMap.put(key, xs);
							}
						}
					}

					List<JSONObject> claOldStaList = sAlzDao.getOldLcClassStatic(dbxnxq, autoIncr2, map);
					for (JSONObject cla : claOldStaList) {
						String bh = cla.getString("bh");
						cla.put("cdate", degreeInfo.getCdate().getTime());
						cla.put("kslcmc", degreeInfo.getKslcmc());
						if (!bhClaMap.containsKey(bh)) {
							bhClaMap.put(bh, cla);
						} else {
							JSONObject claTmp = bhClaMap.get(bh);
							if (cla.getLongValue("cdate") > claTmp.getLongValue("cdate")) {
								lastXsKmMap.put(bh, cla);
							}
						}
					}

					List<JSONObject> claOldKmStaList = sAlzDao.getOldLcClassKmStatic(dbxnxq, autoIncr2, map);
					for (JSONObject cla : claOldKmStaList) {
						String bh = cla.getString("bh");
						String km = cla.getString("kmdm");
						String key = bh + "_" + km;
						cla.put("cdate", degreeInfo.getCdate().getTime());
						cla.put("kslcmc", degreeInfo.getKslcmc());
						if (!bhClaKmMap.containsKey(key)) {
							bhClaKmMap.put(key, cla);
						} else {
							JSONObject claTmp = bhClaKmMap.get(key);
							if (cla.getLongValue("cdate") > claTmp.getLongValue("cdate")) {
								bhClaKmMap.put(key, cla);
							}
						}
					}
				}

				lastXsZfpmList.addAll(lastXsZfMap.values()); // 最近的学生总分排名
				lastXsDkpmList.addAll(lastXsKmMap.values()); // 最近的学生单科排名
			}

			// 生成分组 参考人数映射
			Map<String, Integer> bjfzCkNumMap = new HashMap<String, Integer>();

			// 教师 班级 科目列表
			List<JSONObject> TeacherClassList = getTeacherClassList(sch, params, bjJsonMap);
			Map<String, String> teacherMapDic = new HashMap<String, String>();
			for (JSONObject obj : TeacherClassList) {
				teacherMapDic.put(obj.getString("zgh"), obj.getString("xm"));
			}

			// 获取班级报告参数设置
			List<SettingBJ> settingBJList = sAlzDao.getSarSetBJList(params);
			int sarSetBj01 = 0, sarSetBj02 = 0;
			for (SettingBJ sar : settingBJList) {
				if ("01".equalsIgnoreCase(sar.getDm())) { // 进步之星：本次考试班级排名上升
					sarSetBj01 = sar.getSzz();
				} else if ("02".equalsIgnoreCase(sar.getDm())) { // 异常学生：本次考试班级排名下降
					sarSetBj02 = sar.getSzz();
				}
			}

			// 考试跟踪，成绩曲线图

			// List<JSONObject> bjReportGzKslcList =
			// sAlzDao.getBjReportGzKslcList(map);
			// map.put("bjReportGzKslcList", bjReportGzKslcList);

			// 获取相关考试 限制10次
			Set<String> relatedBj = bjJsonMap.keySet();
			params.put("relatedBj", relatedBj);
			List<DegreeInfo> relatedKslcList = sAlzDao.getBjReportGzKslcList(xnxqList, exam.getCdate(), params);
			params.remove("relatedBj");

			// 获取班级科目标准分跟踪列表
			List<JSONObject> cjgzList = new ArrayList<JSONObject>();
			// 获取班级各科目考试标准分
			List<JSONObject> kmcjgzList = new ArrayList<JSONObject>();
			if (relatedKslcList.size() > 0) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("xxdm", xxdm);
				for (DegreeInfo degreeInfo : relatedKslcList) {
					String xnxq2 = degreeInfo.getXnxq();
					map.put("kslc", degreeInfo.getKslcdm());
					map.put("xnxq", xnxq2);
					map.put("kslcmc", degreeInfo.getKslcmc());

					cjgzList.addAll(sAlzDao.getBjBzfGz(xnxq2, degreeInfo.getAutoIncr(), map));
					kmcjgzList.addAll(sAlzDao.getBjBzfGzKm(xnxq2, degreeInfo.getAutoIncr(), map));
				}
			}

			// 获取学生报告参数设置
			List<JSONObject> sarSettingStu = sAlzDao.getSarSetStuList(params);

			progressBar.setProgressInfo(1, 28, "正在进行成绩分析...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());

			/**
			 * 全员分析
			 */
			Map<String, List<JSONObject>> wlbjfz = new HashMap<String, List<JSONObject>>();
			for (JSONObject o : bjwlfz) {
				String wlfz = o.getString("wlfz");
				if (!wlbjfz.containsKey(wlfz)) {
					wlbjfz.put(wlfz, new ArrayList<JSONObject>());
				}
				wlbjfz.get(wlfz).add(o);
			}

			// 需 保存结果
			// 学生竞赛成绩
			List<CompetitionStuStatistics> competitionStuStatisticsList = new ArrayList<CompetitionStuStatistics>();
			/**
			 * 学生总分统计结果
			 */
			Map<String, List<Object>> scoreStuStatisticsRankMap = new HashMap<String, List<Object>>();
			Map<String, List<Object>> scoreStuStatisticsRankWlfzMap = new HashMap<String, List<Object>>();
			List<ScoreStuStatisticsRank> scoreStuStatisticsRankList = new ArrayList<ScoreStuStatisticsRank>();
			/**
			 * 学生单科统计结果
			 */

			Map<String, List<Object>> scoreStuStatisticsRankMkMap = new HashMap<String, List<Object>>();
			Map<String, List<Object>> scoreStuStatisticsRankMkMapKM = new HashMap<String, List<Object>>();

			List<ScoreStuStatisticsRankMk> scoreStuStatisticsRankMkList = new ArrayList<ScoreStuStatisticsRankMk>();
			List<ScoreClassStatistics> scoreClassStatisticsList = new ArrayList<ScoreClassStatistics>();
			List<ScoreClassStatisticsMk> scoreClassStatisticsMkList = new ArrayList<ScoreClassStatisticsMk>();

			// 班级总分范围
			List<ScoreClassStatisticsRange> scoreClassStatisticsRangeList = new ArrayList<ScoreClassStatisticsRange>();
			List<ScoreClassStatisticsMkRange> scoreClassStatisticsMkRangeList = new ArrayList<ScoreClassStatisticsMkRange>();
			// 班级总分分布
			List<ScoreClassDistribute> scoreClassDistributeList = new ArrayList<ScoreClassDistribute>();
			List<ScoreClassDistributeMk> scoreClassDistributeMkList = new ArrayList<ScoreClassDistributeMk>();
			// 年级总分排名分布
			List<ScoreRankStatistics> scoreRankStatisticsList = new ArrayList<ScoreRankStatistics>();
			// 年级单科排名分布
			List<ScoreRankStatisticsMk> scoreRankStatisticsMkList = new ArrayList<ScoreRankStatisticsMk>();
			// 年级总分成绩统计结果 含文理分组与班级分组
			List<ScoreGroupStatistics> scoreGroupStatisticsList = new ArrayList<ScoreGroupStatistics>();
			// 年级总分成绩统计结果 含文理分组与班级分组-范围统计
			List<ScoreGroupStatisticsRange> scoreGroupStatisticsRangeList = new ArrayList<ScoreGroupStatisticsRange>();
			// 年级单科成绩统计结果 含文理分组与班级分组
			List<ScoreGroupStatisticsMk> scoreGroupStatisticsMkList = new ArrayList<ScoreGroupStatisticsMk>();
			// 年级单科成绩统计结果 含文理分组与班级分组-范围统计
			List<ScoreGroupStatisticsMkRange> scoreGroupStatisticsMkRangeList = new ArrayList<ScoreGroupStatisticsMkRange>();
			// 学生总分标准分
			List<ScoreStuBzf> scoreStuBzfList = new ArrayList<ScoreStuBzf>();
			// 学生单科标准分
			List<ScoreStuBzfMk> scoreStuBzfMkList = new ArrayList<ScoreStuBzfMk>();
			// 总分 尖子生表
			List<ScoreStuJzsqns> scoreStuJzsqnsList = new ArrayList<ScoreStuJzsqns>();
			// 单科 尖子生表
			List<ScoreStuJzsqnsMk> scoreStuJzsqnsMkList = new ArrayList<ScoreStuJzsqnsMk>();
			// 班级科目等级统计表
			List<ClassScoreLevelMk> classScoreLevelMkList = new ArrayList<ClassScoreLevelMk>();
			// 年级科目等级统计表
			List<GroupScoreLevelMk> groupScoreLevelMkList = new ArrayList<GroupScoreLevelMk>();
			// 班级总分等级序列统计表
			List<ClassScoreLevelSequnce> classScoreLevelSequnceList = new ArrayList<ClassScoreLevelSequnce>();
			// 年级成绩报告结果表
			List<AnalysisReportNj> analysisReportnjList = new ArrayList<AnalysisReportNj>();
			// 班级成绩报告
			List<AnalysisReportBj> analysisReportBjList = new ArrayList<AnalysisReportBj>();
			// 学生成绩报告
			List<AnalysisReportStu> analysisReportStuList = new ArrayList<AnalysisReportStu>();

			// App学生成绩报告
			List<AppStudentScoreReport> appStudentScoreReportList = new ArrayList<AppStudentScoreReport>();

			Map<String, JSONObject> bhkmCjMap = new HashMap<String, JSONObject>();

			// 记录班级_科目--学生成绩排名异常列表
			Map<String, HashMap<String, JSONObject>> bhKmYcXsList = new HashMap<String, HashMap<String, JSONObject>>();

			Map<String, JSONObject> bjDataForXsReport = new HashMap<String, JSONObject>();

			// 班级-排名设置dm-科目代码 人数映射
			Map<String, JSONObject> bhPmszKmRsMap = new HashMap<String, JSONObject>();

			// 开始循环文理分组
			for (Iterator<String> it = wlbjfz.keySet().iterator(); it.hasNext();) {
				// 班级总分数段人数列表 班号,班级名称-分布代码-人数
				Map<String, Map<String, JSONObject>> bhFbdmScoreDisMap = new HashMap<String, Map<String, JSONObject>>();
				// 班级-班级统计结果映射
				Map<String, ScoreClassStatistics> bhClassStaMap = new HashMap<String, ScoreClassStatistics>();
				// 班级-科目-班级统计结果映射
				Map<String, Map<String, ScoreClassStatisticsMk>> bhKmClassStaMap = new HashMap<String, Map<String, ScoreClassStatisticsMk>>();
				String wlfz = it.next();
				HashMap<String, JSONObject> bhDataMap = new HashMap<String, JSONObject>();
				List<JSONObject> bjZfDataList = new ArrayList<JSONObject>();
				List<JSONObject> bjKmDataList = new ArrayList<JSONObject>();
				List<JSONObject> kmList = GrepUtil.grepJsonKeyBySingleVal("wlfz", wlfz, kmInWlfz);
				// 科目去重
				List<JSONObject> needremoveKm = new ArrayList<JSONObject>();
				HashMap<String, Integer> kmNumtj = new HashMap<String, Integer>();
				List<String> kmdms = new ArrayList<String>();
				for (JSONObject km : kmList) {
					String kmdm = km.getString("kmdm");
					int isHb = km.getIntValue("isHb");
					if (kmNumtj.containsKey(kmdm)) {
						kmNumtj.put(kmdm, kmNumtj.get(kmdm) + 1);
					} else {
						kmNumtj.put(kmdm, 1);
					}
				}
				for (JSONObject km : kmList) {
					String kmdm = km.getString("kmdm");
					int isHb = km.getIntValue("isHb");
					if (kmNumtj.get(kmdm) > 1 && isHb == 1) {
						needremoveKm.add(km);
					}
				}
				kmList.removeAll(needremoveKm);
				List<JSONObject> wlzcj = GrepUtil.grepJsonKeyBySingleVal("wlfz", wlfz, xszfList);
				List<JSONObject> wlkmcjNjpm = new ArrayList<JSONObject>();
				if (wlzcj.size() == 0 || kmList.size() == 0) {
					continue;
				}
				List<JSONObject> bjInwlfz = GrepUtil.grepJsonKeyBySingleVal("wlfz", wlfz, bjList);
				long df1 = new Date().getTime();
				List<JSONObject> wlzcjNjpm = ScoreUtil.sorStuScoreList(wlzcj, "zcj", "desc", "nj", "zf");
				long df2 = new Date().getTime();
				System.out.println("总成绩排名耗时：" + (df2 - df1));
				// 合并科目
				List<JSONObject> hbKmList = GrepUtil.grepJsonKeyBySingleVal("isHb", "1", kmList);
				List<JSONObject> wlkmcj = GrepUtil.grepJsonKeyBySingleVal("wlfz", wlfz, xskmList);
				long t1 = new Date().getTime();
				HashMap<String, List<JSONObject>> wlfzHbkmList = new HashMap<String, List<JSONObject>>();
				// 计算合并科目成绩 并标记是否统计
				for (JSONObject hbkm : hbKmList) {
					for (JSONObject xs : wlzcjNjpm) {
						String kmdm = hbkm.getString("kmdm");
						String kmmc = hbkm.getString("kmmc");
						String dykm = hbkm.getString("dykm");
						String xh = xs.getString("xh");
						float cj = ScoreUtil.getHbkmCjByList(xsHbcjMap.get(xh), dykm, xh);
						JSONObject n = new JSONObject();
						n.put("kmdm", kmdm);
						n.put("kmmc", kmmc);
						n.put("dykm", dykm);
						n.put("xh", xh);
						n.put("xm", xs.get("xm"));
						n.put("cj", cj);
						n.put("wlfz", wlfz);
						n.put("bjfz", null);
						n.put("tsqk", null);
						n.put("bh", xs.get("bh"));
						n.put("isHb", 1);

						wlkmcj.add(n);

						xhMapScore.get(xh).put(kmmc, cj * 100);
						List<JSONObject> hblist = new ArrayList<JSONObject>();
						if (wlfzHbkmList.containsKey(xh)) {
							hblist = wlfzHbkmList.get(xh);
							hblist.add(n);
						} else {
							hblist.add(n);
							wlfzHbkmList.put(xh, hblist);
						}
					}
				}
				long t2 = new Date().getTime();
				System.out.println("合并科目成绩耗时：" + (t2 - t1) + "---数据长度:" + wlzcjNjpm.size());

				// 文理下班级科目等第
				List<JSONObject> bhkmDDList = new ArrayList<JSONObject>();
				// 文理下班级分组科目等第
				List<JSONObject> bjfzkmDDList = new ArrayList<JSONObject>();
				// 文理下文理分组科目等第
				List<JSONObject> wlfzkmDDList = new ArrayList<JSONObject>();
				// 文理下排名分布
				List<JSONObject> wlfzRankDisList = GrepUtil.grepJsonKeyBySingleVal("wlfz", wlfz, rankDisList);
				// 对总分进行班级排名
				for (JSONObject bj : bjInwlfz) {
					String bh = bj.getString("bh");
					List<JSONObject> bjzflist = GrepUtil.grepJsonKeyBySingleVal("bh", bh, wlzcj);
					List<JSONObject> bjzfSort = ScoreUtil.sorStuScoreList(bjzflist, "zcj", "desc", "bj", "zf");
				}

				// 标记学生是否参与分析
				wlkmcj = ScoreUtil.setSftjList(wlkmcj, qkbcy, wbbcy, lfbcy, isKmStuInJs);
				for (JSONObject o : wlzcjNjpm) {
					String xh = o.getString("xh");
					String kmdm = o.getString("kmdm");
					// List<JSONObject> singleStuScoreList =
					// GrepUtil.grepJsonKeyBySingleVal("xh",xh,wlkmcj);
					boolean sftj = ScoreUtil.getZfsftj(xsHbcjMap.get(xh));
					o.put("sftj", sftj);
				}

				// 标记是否优秀、合格、等
				int wlzf = 300;
				List<JSONObject> zfszList2 = GrepUtil.grepJsonKeyBySingleVal("fzdm", wlfz, zfszList);
				if (zfszList2.size() > 0) {
					wlzf = zfszList2.get(0).getIntValue("zf");
					if (scoreSecList.size() > 0) {
						wlzcjNjpm = ScoreUtil.setYxtjList(wlzcjNjpm, scoreSecList, zfszList2.get(0).getIntValue("zf"));
					}
				}

				Map<String, List<JSONObject>> xhWlkmcjMap = new HashMap<String, List<JSONObject>>();
				String synj = "";
				// 循环科目 计算单科年级排名与班级排名
				for (JSONObject km : kmList) {
					int mf = 100;
					String kmdm = km.getString("kmdm");
					List<JSONObject> kmszList2 = GrepUtil.grepJsonKeyByVal(new String[] { "kmdm", "fzdm" },
							new String[] { kmdm, wlfz }, kmszList);
					List<JSONObject> kmcjList = GrepUtil.grepJsonKeyBySingleVal("kmdm", kmdm, wlkmcj);
					List<JSONObject> kmcjNjpm = ScoreUtil.sorStuScoreList(kmcjList, "cj", "desc", "nj", "km");
					wlkmcjNjpm.addAll(kmcjNjpm);

					// 对单科进行标记是否优秀、合格、尖子等
					if (kmszList2.size() > 0) {
						mf = kmszList2.get(0).getIntValue("mf");
						kmcjNjpm = ScoreUtil.setYxtjKmList(kmcjNjpm, scoreSecList, mf);
					}
					km.put("mf", mf);

					for (JSONObject bj : bjInwlfz) {
						String bh = bj.getString("bh");
						synj = bj.getString("nj");
						List<JSONObject> bjkmlist = GrepUtil.grepJsonKeyBySingleVal("bh", bh, kmcjNjpm);
						List<JSONObject> bjkmSort = ScoreUtil.sorStuScoreList(bjkmlist, "cj", "desc", "bj", "km");
					}
					// 开始生产单科等第 allDjList
					List<JSONObject> kmddList = GrepUtil.grepJsonKeyByVal(new String[] { "kmdm", "nj" },
							new String[] { kmdm, synj }, allDjList);
					for (JSONObject o : kmcjNjpm) {
						String dd = ScoreUtil.getSingleLevByScore(o.getFloatValue("cj"), kmddList);
						o.put("djmc", dd);
						String xh = o.getString("xh");
						if (xhWlkmcjMap.containsKey(xh)) {
							if (!xhWlkmcjMap.get(xh).contains(o)) {
								xhWlkmcjMap.get(xh).add(o);
							}
						} else {
							List<JSONObject> cjList = new ArrayList<JSONObject>();
							cjList.add(o);
							xhWlkmcjMap.put(xh, cjList);
						}
					}
				}

				// 本次考试的统计范围设置
				List<JSONObject> fwszList = sAlzDao.getFwszList(xnxq, autoIncr, params);

				// 标记学生 是否参与范围统计 wlzcjNjpm
				List<JSONObject> fwszList2 = GrepUtil.grepJsonKeyBySingleVal("wlfz", wlfz, fwszList);
				List<JSONObject> fwszList3 = GrepUtil.grepJsonKeyBySingleVal("type", "1", fwszList2);
				if (fwszList3.size() == 0) {
					wlzcjNjpm = ScoreUtil.setFwtjList(wlzcjNjpm, fwszList2, 2, xsHbcjMap, wlfzHbkmList);
				} else {
					if (fwszList2.size() == 0) {
						for (JSONObject xs : wlzcjNjpm) {
							xs.put("fwtj", true);
						}
						for (JSONObject xs : wlkmcjNjpm) {
							xs.put("fwtj", true);
						}
					} else {
						wlzcjNjpm = ScoreUtil.setFwtjList(wlzcjNjpm, fwszList3, 1, xsHbcjMap, wlfzHbkmList);
					}
				}
				
				logger.info("xhWlkmcjMap: "+JSONObject.toJSONString(xhWlkmcjMap));
				logger.info("djsx: "+djsx);
				// 计算学生的总分等级序列 如上面重复 去掉上面方法
				if (djsx.length() > 0) {

					for (JSONObject xs : wlzcjNjpm) {
						String xh = xs.getString("xh");
						String nj = xs.getString("nj");
						String allStr = njHbStr.get(nj);
						String dd = ScoreUtil.getAllScoreLevByScore(xhWlkmcjMap.get(xh), djsx, "01", null);
						String dd2 = ScoreUtil.getAllScoreLevByScore(xhWlkmcjMap.get(xh), djsx, "02", allStr);
						String dd3 = ScoreUtil.getAllScoreLevByScore(xhWlkmcjMap.get(xh), djsx, "03", null);
						xs.put("djxl", dd);
						xs.put("djxl2", dd2);
						xs.put("djxl3", dd3);
					}
				}

				// 参与人数与统计人数
				int zfcyrs = wlzcjNjpm.size();

				List<JSONObject> zftjList = GrepUtil.grepJsonKeyBySingleVal("sftj", "true", wlzcjNjpm);
				int zftjrs = zftjList.size();
				if (zftjrs == 0) {
					continue;
				}
				// 最高分与最低分
				float maxZfScr = zftjList.get(0).getFloatValue("zcj");
				float minZfScr = zftjList.get(zftjrs - 1).getFloatValue("zcj");
				// float avgZfScr = ScoreUtil.getPjf(zftjList, "zcj");
				float avgZfScr = 0;
				// 优秀
				JSONObject zfYxPerObj = ScoreUtil.getDjPercent(zftjList, "Yx");
				float zfYxPer = zfYxPerObj.getFloatValue("bl");
				int zfYxRs = zfYxPerObj.getIntValue("rs");
				// 合格
				JSONObject zfHgPerObj = ScoreUtil.getDjPercent(zftjList, "Hg");
				float zfHgPer = zfHgPerObj.getFloatValue("bl");
				int zfHgRs = zfHgPerObj.getIntValue("rs");
				// 低分
				JSONObject zfDfPerObj = ScoreUtil.getDjPercent(zftjList, "Df");
				float zfDfPer = zfDfPerObj.getFloatValue("bl");
				int zfDfRs = zfDfPerObj.getIntValue("rs");
				// 尖子
				JSONObject zfJzPerObj = ScoreUtil.getDjPercent(zftjList, "Jz");
				float zfJzPer = zfJzPerObj.getFloatValue("bl");
				int zfJzRs = zfJzPerObj.getIntValue("rs");
				List<JSONObject> zfJzList = (List<JSONObject>) zfJzPerObj.get("list");
				for (JSONObject stu : zfJzList) {
					ScoreStuJzsqns tgs = new ScoreStuJzsqns();
					tgs.setKslc(kslc);
					tgs.setLb("04");
					tgs.setXh(stu.getString("xh"));
					tgs.setXnxq(xnxq);
					tgs.setXxdm(xxdm);
					scoreStuJzsqnsList.add(tgs);
				}
				// 潜能
				JSONObject zfQnPerObj = ScoreUtil.getDjPercent(zftjList, "Qn");
				float zfQnPer = zfQnPerObj.getFloatValue("bl");
				int zfQnRs = zfQnPerObj.getIntValue("rs");
				List<JSONObject> zfQnList = (List<JSONObject>) zfQnPerObj.get("list");

				for (JSONObject stu : zfQnList) {
					ScoreStuJzsqns tgs = new ScoreStuJzsqns();
					tgs.setKslc(kslc);
					tgs.setLb("05");
					tgs.setXh(stu.getString("xh"));
					tgs.setXnxq(xnxq);
					tgs.setXxdm(xxdm);
					scoreStuJzsqnsList.add(tgs);
				}

				Map<String, ScoreGroupStatisticsMk> kmTsgsMap = new HashMap<String, ScoreGroupStatisticsMk>();
				// 单科循环上面的方法
				for (JSONObject km : kmList) {
					String kmdm = km.getString("kmdm");
					ScoreGroupStatisticsMk scoreGroupStatisticsMk = new ScoreGroupStatisticsMk();
					kmTsgsMap.put(kmdm, scoreGroupStatisticsMk);
					List<JSONObject> kmcjList = GrepUtil.grepJsonKeyBySingleVal("kmdm", kmdm, wlkmcjNjpm);
					// 参与人数与统计人数
					int kmcyrs = kmcjList.size();
					List<JSONObject> kmtjList = GrepUtil.grepJsonKeyBySingleVal("sftj", "true", kmcjList);
					int kmtjrs = kmtjList.size();
					if (kmcjList.size() == 0) {
						continue;
					}
					// 单科最高分与最低分
					float maxKmScr = kmtjList.get(0).getFloatValue("cj");
					km.put("wlfzzgf", maxKmScr);
					float minKmScr = kmtjList.get(kmtjrs - 1).getFloatValue("cj");
					float avgKmScr = ScoreUtil.getPjf(kmtjList, "cj");
					// 总分平均分计算方式改变
					if (km.getInteger("isHb") == 0) {

						avgZfScr += avgKmScr;
					}

					km.put("wlfzpjf", avgKmScr);

					JSONObject kmYxPerObj = ScoreUtil.getDjPercent(kmtjList, "Yx");
					float kmYxPer = kmYxPerObj.getFloat("bl");
					int kmYxRs = kmYxPerObj.getIntValue("rs");

					JSONObject kmHgPerObj = ScoreUtil.getDjPercent(kmtjList, "Hg");
					float kmHgPer = kmHgPerObj.getFloat("bl");
					int kmHgRs = kmHgPerObj.getIntValue("rs");

					JSONObject kmDfPerObj = ScoreUtil.getDjPercent(kmtjList, "Df");
					float kmDfPer = kmDfPerObj.getFloat("bl");
					int kmDfRs = kmDfPerObj.getIntValue("rs");

					JSONObject kmQnPerObj = ScoreUtil.getDjPercent(kmtjList, "Qn");
					float kmQnPer = kmQnPerObj.getFloat("bl");
					int kmQnRs = kmQnPerObj.getIntValue("rs");
					List<JSONObject> kmQnList = (List<JSONObject>) kmQnPerObj.get("list");

					JSONObject kmJzPerObj = ScoreUtil.getDjPercent(kmtjList, "Jz");
					float kmJzPer = kmJzPerObj.getFloat("bl");
					int kmJzRs = kmJzPerObj.getIntValue("rs");
					List<JSONObject> kmJzList = (List<JSONObject>) kmJzPerObj.get("list");

					for (JSONObject stu : kmJzList) {
						ScoreStuJzsqnsMk tgs = new ScoreStuJzsqnsMk();
						tgs.setKmdm(kmdm);
						tgs.setKslc(kslc);
						tgs.setLb("04");
						tgs.setXh(stu.getString("xh"));
						tgs.setXnxq(xnxq);
						tgs.setXxdm(xxdm);

						scoreStuJzsqnsMkList.add(tgs);
					}
					for (JSONObject stu : kmQnList) {
						ScoreStuJzsqnsMk tgs = new ScoreStuJzsqnsMk();
						tgs.setKmdm(kmdm);
						tgs.setKslc(kslc);
						tgs.setLb("05");
						tgs.setXh(stu.getString("xh"));
						tgs.setXnxq(xnxq);
						tgs.setXxdm(xxdm);

						scoreStuJzsqnsMkList.add(tgs);
					}

					float kmDiffiPer = ScoreUtil.dificultPoint(avgKmScr, km.getIntValue("mf"));
					float kmDiffRate = ScoreUtil.diffrentRate(kmtjList, "cj", km.getIntValue("mf"));
					List<JSONObject> kmddList = GrepUtil.grepJsonKeyByVal(new String[] { "kmdm", "nj" },
							new String[] { kmdm, synj }, allDjList);
					// 单科等级分析
					for (int l = 0; l < kmddList.size(); l++) {
						JSONObject dd = kmddList.get(l);
						String djmc = dd.getString("djmc");
						float djNum = GrepUtil.grepJsonKeyBySingleVal("djmc", djmc, kmtjList).size();
						float bl = ScoreUtil.castFloatTowPointNum((float) djNum * 100f / kmtjList.size());
						JSONObject bjdd = new JSONObject();
						bjdd.put("bl", bl);
						bjdd.put("djmc", djmc);
						bjdd.put("wlfz", wlfz);
						bjdd.put("kmdm", kmdm);
						bjdd.put("rs", djNum);
						if (l == 0) {
							bjdd.put("ljrs", djNum);
							dd.put("ljrs", djNum);
						} else {
							JSONObject last = kmddList.get(l - 1);
							bjdd.put("ljrs", djNum + last.getIntValue("ljrs"));
							dd.put("ljrs", djNum + last.getIntValue("ljrs"));
						}
						bjdd.put("djdm", dd.get("dm"));

						wlfzkmDDList.add(bjdd);
					}

					scoreGroupStatisticsMk.setCkrs(kmcyrs);
					scoreGroupStatisticsMk.setDfl(kmDfPer);
					scoreGroupStatisticsMk.setDfrs(kmDfRs);
					scoreGroupStatisticsMk.setFzdm(wlfz);
					scoreGroupStatisticsMk.setHgl(kmHgPer);
					scoreGroupStatisticsMk.setHgrs(kmHgRs);
					scoreGroupStatisticsMk.setJzsrs(kmJzRs);
					scoreGroupStatisticsMk.setKmdm(kmdm);
					scoreGroupStatisticsMk.setKslc(kslc);
					scoreGroupStatisticsMk.setLdxs(kmDiffiPer);
					scoreGroupStatisticsMk.setPjf(avgKmScr);
					scoreGroupStatisticsMk.setQfd(kmDiffRate);
					scoreGroupStatisticsMk.setQnsrs(kmQnRs);
					scoreGroupStatisticsMk.setTjrs(kmtjrs);
					scoreGroupStatisticsMk.setXnxq(xnxq);
					scoreGroupStatisticsMk.setXxdm(xxdm);
					scoreGroupStatisticsMk.setYxl(kmYxPer);
					scoreGroupStatisticsMk.setYxrs(kmYxRs);

					// 计算文理分组成绩分析完成 开始竞赛分析
					if (!isKmStuInJs.isEmpty() && isKmStuInJs.containsKey(kmdm)) {
						CompetitionStuStatistics tss = new CompetitionStuStatistics();

						List<JSONObject> jsList = GrepUtil.grepJsonKeyBySingleVal("sfjs", "true", kmtjList);
						if (jsList.size() > 0) {
							JSONObject jsYxPerObj = ScoreUtil.getDjPercent(jsList, "Yx");
							float jsYxPer = jsYxPerObj.getFloat("bl");
							int jsYxRs = jsYxPerObj.getIntValue("rs");

							JSONObject jsHgPerObj = ScoreUtil.getDjPercent(jsList, "Hg");
							float jsHgPer = jsHgPerObj.getFloat("bl");
							int jsHgRs = jsHgPerObj.getIntValue("rs");

							float jspjf = ScoreUtil.getPjf(jsList, "cj");
							tss.setFzdm(wlfz);
							tss.setHgl(ScoreUtil.castFloatTowPointNum(jsHgPer));
							tss.setHgrs(jsHgRs);
							tss.setKmdm(kmdm);
							tss.setKmzmc(km.getString("kmmc") + "组");
							tss.setKslc(kslc);
							tss.setPjf(jspjf);
							tss.setXnxq(xnxq);
							tss.setXxdm(xxdm);
							tss.setYxl(ScoreUtil.castFloatTowPointNum(jsYxPer));
							tss.setYxrs(jsYxRs);

							competitionStuStatisticsList.add(tss);
						}
					}
					// 竞赛分析结束
				}
				float zfDiffiPer = ScoreUtil.dificultPoint(avgZfScr, wlzf);
				float zfDiffRate = ScoreUtil.diffrentRate(zftjList, "zcj", wlzf);

				// 竞赛分析结束

				// 开始按班级分组进行循环
				List<JSONObject> bjfzList = GrepUtil.grepJsonKeyBySingleVal("wlfz", wlfz, bjwlfz);
				for (JSONObject bjfzObj : bjfzList) {
					String bjfz = bjfzObj.getString("bjfz");
					// 参与人数与统计人数
					List<JSONObject> bjfzcjList = GrepUtil.grepJsonKeyBySingleVal("bjfz", bjfz, wlzcjNjpm);
					int bjfzzfcyrs = bjfzcjList.size();
					if (bjfzzfcyrs == 0) {
						continue;
					}
					bjfzCkNumMap.put(bjfz, bjfzzfcyrs);
					List<JSONObject> bjfztjcjList = GrepUtil.grepJsonKeyBySingleVal("sftj", "true", bjfzcjList);
					int bjfzzftjrs = bjfztjcjList.size();
					if (bjfzzftjrs == 0) {
						continue;
					}
					// 最高分与最低分
					float bjfzmaxZfScr = bjfztjcjList.get(0).getFloatValue("zcj");
					float bjfzminZfScr = bjfztjcjList.get(bjfzzftjrs - 1).getFloatValue("zcj");
					// float bjfzavgZfScr = ScoreUtil.getPjf(bjfztjcjList,
					// "zcj");
					float bjfzavgZfScr = 0;
					// 优秀
					JSONObject bjfzYxPerObj = ScoreUtil.getDjPercent(bjfztjcjList, "Yx");
					float bjfzYxPer = bjfzYxPerObj.getFloatValue("bl");
					int bjfzYxRs = bjfzYxPerObj.getIntValue("rs");
					// 合格
					JSONObject bjfzHgPerObj = ScoreUtil.getDjPercent(bjfztjcjList, "Hg");
					float bjfzHgPer = bjfzHgPerObj.getFloatValue("bl");
					int bjfzHgRs = bjfzHgPerObj.getIntValue("rs");
					// 低分
					JSONObject bjfzDfPerObj = ScoreUtil.getDjPercent(bjfztjcjList, "Df");
					float bjfzDfPer = bjfzDfPerObj.getFloatValue("bl");
					int bjfzDfRs = bjfzDfPerObj.getIntValue("rs");
					// 尖子
					JSONObject bjfzJzPerObj = ScoreUtil.getDjPercent(bjfztjcjList, "Jz");
					float bjfzJzPer = bjfzJzPerObj.getFloatValue("bl");
					int bjfzJzRs = bjfzJzPerObj.getIntValue("rs");
					// 潜能
					JSONObject bjfzQnPerObj = ScoreUtil.getDjPercent(bjfztjcjList, "Qn");
					float bjfzQnPer = bjfzQnPerObj.getFloatValue("bl");

					float bjfzzfDiffRate = ScoreUtil.diffrentRate(bjfztjcjList, "zcj", wlzf);

					HashMap<String, ScoreGroupStatisticsMk> kmBjTsgsMap = new HashMap<String, ScoreGroupStatisticsMk>();
					// 单科循环上面的方法
					for (JSONObject km : kmList) {
						String kmdm = km.getString("kmdm");

						ScoreGroupStatisticsMk scoreGroupStatisticsMk = new ScoreGroupStatisticsMk();
						kmBjTsgsMap.put(kmdm, scoreGroupStatisticsMk);

						List<JSONObject> kmcjList = GrepUtil.grepJsonKeyByVal(new String[] { "kmdm", "bjfz" },
								new String[] { kmdm, bjfz }, wlkmcjNjpm);
						// 参与人数与统计人数
						int kmcyrs = kmcjList.size();
						bjfzCkNumMap.put(bjfz + "_" + kmdm, kmcyrs);
						List<JSONObject> kmtjList = GrepUtil.grepJsonKeyBySingleVal("sftj", "true", kmcjList);
						int kmtjrs = kmtjList.size();
						if (kmtjList.size() == 0) {
							continue;
						}
						// 单科最高分与最低分
						float maxKmScr = kmtjList.get(0).getFloatValue("cj");
						float minKmScr = kmtjList.get(kmtjrs - 1).getFloatValue("cj");
						float avgKmScr = ScoreUtil.getPjf(kmtjList, "cj");
						if (km.getInteger("isHb") == 0) {

							bjfzavgZfScr += avgKmScr;
						}
						// float kmYxPer = ScoreUtil.getDjPercent(kmtjList,
						// "Yx");
						// float kmHgPer =
						// ScoreUtil.getDjPercent(kmtjList,"Hg");
						// float kmDfPer =
						// ScoreUtil.getDjPercent(kmtjList,"Df");
						// float kmJzPer =
						// ScoreUtil.getDjPercent(kmtjList,"Jz");
						// float kmQnPer =
						// ScoreUtil.getDjPercent(kmtjList,"Qn");
						JSONObject kmYxPerObj = ScoreUtil.getDjPercent(kmtjList, "Yx");
						float kmYxPer = kmYxPerObj.getFloat("bl");
						int kmYxRs = kmYxPerObj.getIntValue("rs");

						JSONObject kmHgPerObj = ScoreUtil.getDjPercent(kmtjList, "Hg");
						float kmHgPer = kmHgPerObj.getFloat("bl");
						int kmHgRs = kmHgPerObj.getIntValue("rs");

						JSONObject kmDfPerObj = ScoreUtil.getDjPercent(kmtjList, "Df");
						float kmDfPer = kmDfPerObj.getFloat("bl");
						int kmDfRs = kmDfPerObj.getIntValue("rs");

						JSONObject kmQnPerObj = ScoreUtil.getDjPercent(kmtjList, "Qn");
						float kmQnPer = kmQnPerObj.getFloat("bl");
						int kmQnRs = kmQnPerObj.getIntValue("rs");

						JSONObject kmJzPerObj = ScoreUtil.getDjPercent(kmtjList, "Jz");
						float kmJzPer = kmJzPerObj.getFloat("bl");
						int kmJzRs = kmJzPerObj.getIntValue("rs");

						float kmDiffiPer = ScoreUtil.dificultPoint(avgKmScr, km.getIntValue("mf"));
						float kmDiffRate = ScoreUtil.diffrentRate(kmtjList, "cj", km.getIntValue("mf"));
						List<JSONObject> kmddList = GrepUtil.grepJsonKeyByVal(new String[] { "kmdm", "nj" },
								new String[] { kmdm, synj }, allDjList);
						// 单科等级分析
						for (int l = 0; l < kmddList.size(); l++) {
							JSONObject dd = kmddList.get(l);
							String djmc = dd.getString("djmc");
							int djNum = GrepUtil.grepJsonKeyBySingleVal("djmc", djmc, kmtjList).size();

							float bl = ScoreUtil.castFloatTowPointNum((float) djNum * 100f / kmtjList.size());
							JSONObject bjdd = new JSONObject();
							bjdd.put("bl", bl);
							bjdd.put("djmc", djmc);
							bjdd.put("wlfz", wlfz);
							bjdd.put("bjfz", bjfz);
							bjdd.put("kmdm", kmdm);
							bjdd.put("rs", djNum);
							if (l == 0) {
								bjdd.put("ljrs", djNum);
								dd.put("ljrs", djNum);
							} else {
								JSONObject last = kmddList.get(l - 1);
								bjdd.put("ljrs", djNum + last.getIntValue("ljrs"));
								dd.put("ljrs", djNum + last.getIntValue("ljrs"));
							}
							bjdd.put("djdm", dd.get("dm"));

							bjfzkmDDList.add(bjdd);
						}

						scoreGroupStatisticsMk.setCkrs(kmcyrs);
						scoreGroupStatisticsMk.setDfl(kmDfPer);
						scoreGroupStatisticsMk.setDfrs(kmDfRs);
						scoreGroupStatisticsMk.setFzdm(bjfz);
						scoreGroupStatisticsMk.setHgl(kmHgPer);
						scoreGroupStatisticsMk.setHgrs(kmHgRs);
						scoreGroupStatisticsMk.setJzsrs(kmJzRs);
						scoreGroupStatisticsMk.setKmdm(kmdm);
						scoreGroupStatisticsMk.setKslc(kslc);
						scoreGroupStatisticsMk.setLdxs(kmDiffiPer);
						scoreGroupStatisticsMk.setPjf(avgKmScr);
						scoreGroupStatisticsMk.setQfd(kmDiffRate);
						scoreGroupStatisticsMk.setQnsrs(kmQnRs);
						scoreGroupStatisticsMk.setTjrs(kmtjrs);
						scoreGroupStatisticsMk.setXnxq(xnxq);
						scoreGroupStatisticsMk.setXxdm(xxdm);
						scoreGroupStatisticsMk.setYxl(kmYxPer);
						scoreGroupStatisticsMk.setYxrs(kmYxRs);
					}
					float bjfzzfDiffiPer = ScoreUtil.dificultPoint(bjfzavgZfScr, wlzf);

					// 开始按班级循环
					List<JSONObject> bjObjList = GrepUtil.grepJsonKeyBySingleVal("bjfz", bjfz, bjInwlfz);
					for (JSONObject bjObj : bjObjList) {
						bjZfDataList.add(bjObj);
						String bh = bjObj.getString("bh");
						JSONObject bjdata = new JSONObject();
						List<JSONObject> bjcjList = GrepUtil.grepJsonKeyBySingleVal("bh", bh, bjfzcjList);
						int bjzfcyrs = bjcjList.size();
						List<JSONObject> bjtjcjList = GrepUtil.grepJsonKeyBySingleVal("sftj", "true", bjcjList);
						int bjzftjrs = bjtjcjList.size();
						bjObj.put("ckrs", bjzfcyrs);
						bjObj.put("tjrs", bjzftjrs);
						// 最高分与最低分
						if (bjtjcjList.size() == 0) {
							continue;
						}
						float bjmaxZfScr = bjtjcjList.get(0).getFloatValue("zcj");
						bjObj.put("bjmaxZfScr", bjmaxZfScr);
						float bjminZfScr = bjtjcjList.get(bjzftjrs - 1).getFloatValue("zcj");
						bjObj.put("bjminZfScr", bjminZfScr);
						// float bjavgZfScr = ScoreUtil.getPjf(bjtjcjList,
						// "zcj");
						float bjavgZfScr = 0;

						JSONObject bjzfYxObj = ScoreUtil.getDjPercent(bjtjcjList, "Yx");
						float bjzfYxPer = bjzfYxObj.getFloatValue("bl");
						int bjzfYxRs = bjzfYxObj.getIntValue("rs");
						bjObj.put("bjzfYxPer", bjzfYxPer);
						bjObj.put("bjzfYxRs", bjzfYxRs);

						// 学生报告所需数据 bjDataForXsReport
						JSONObject bjDataFx = new JSONObject();
						bjDataForXsReport.put(bh, bjDataFx);

						bjDataFx.put("bjzgf", bjmaxZfScr);
						bjDataFx.put("bjfzmc", bjObj.getString("bjfzmc"));
						bjDataFx.put("bjfzckrs", bjfzzfcyrs);
						bjDataFx.put("bjckrs", bjzfcyrs);
						// 结束
						JSONObject bjzfDfObj = ScoreUtil.getDjPercent(bjtjcjList, "Df");
						float bjzfDfPer = bjzfDfObj.getFloatValue("bl");
						int bjzfDfRs = bjzfDfObj.getIntValue("rs");
						bjObj.put("bjzfDfPer", bjzfDfPer);
						bjObj.put("bjzfDfRs", bjzfDfRs);

						JSONObject bjzfHgObj = ScoreUtil.getDjPercent(bjtjcjList, "Hg");
						float bjzfHgPer = bjzfHgObj.getFloatValue("bl");
						int bjzfHgRs = bjzfHgObj.getIntValue("rs");
						bjObj.put("bjzfHgPer", bjzfHgPer);
						bjObj.put("bjzfHgRs", bjzfHgRs);

						JSONObject bjzfJzObj = ScoreUtil.getDjPercent(bjtjcjList, "Jz");
						float bjzfJzPer = bjzfJzObj.getFloatValue("bl");
						int bjzfJzRs = bjzfJzObj.getIntValue("rs");
						bjObj.put("bjzfJzPer", bjzfJzPer);
						bjObj.put("bjzfJzRs", bjzfJzRs);

						JSONObject bjzfQnObj = ScoreUtil.getDjPercent(bjtjcjList, "Qn");
						float bjzfQnPer = bjzfQnObj.getFloatValue("bl");
						int bjzfQnRs = bjzfQnObj.getIntValue("rs");
						bjObj.put("bjzfQnPer", bjzfQnPer);
						bjObj.put("bjzfQnRs", bjzfQnRs);

						float bjzfDiffRate = ScoreUtil.diffrentRate(bjtjcjList, "zcj", wlzf);
						bjObj.put("bjzfDiffRate", bjzfDiffRate);
						List<Float> left = new ArrayList<Float>();
						for (JSONObject xs : bjtjcjList) {
							left.add(xs.getFloat("zcj"));
						}
						List<JSONObject> bjkmData = new ArrayList<JSONObject>();
						bhDataMap.put(bh, bjdata);

						// 成绩分布 bh in bhFbdmScoreDisMap
						HashMap<String, JSONObject> fbdmfb = new HashMap<String, JSONObject>();
						bhFbdmScoreDisMap.put(bh + "-" + bjObj.getString("bjmc"), fbdmfb);
						for (ScoreDistribute tgs : scoreDistributeList) {
							if (scdWlMap.get(tgs.getDm()).equalsIgnoreCase(wlfz)) {
								ScoreClassDistribute scoreClassDistribute = new ScoreClassDistribute();
								int fbsx = tgs.getFbsx();
								int fbxx = tgs.getFbxx();
								int rs = ScoreUtil.sumNumFromScore(bjcjList, fbsx, fbxx, "zcj");
								int ljrs = rs;
								if (fbxx != 0) {
									ljrs = ScoreUtil.sumLjNumFromScore(bjcjList, fbsx, fbxx, "zcj");
								} else {
									// ljrs = bjtjcjList.size();
									ljrs = bjzfcyrs;
								}
								scoreClassDistribute.setBh(bh);
								scoreClassDistribute.setDm(tgs.getDm());
								scoreClassDistribute.setFbdm(tgs.getFbdm());
								scoreClassDistribute.setKslc(kslc);
								scoreClassDistribute.setXnxq(xnxq);
								scoreClassDistribute.setXxdm(xxdm);
								scoreClassDistribute.setRs(rs);
								scoreClassDistribute.setLjrs(ljrs);
								scoreClassDistributeList.add(scoreClassDistribute);
								// totalScoreDisArray
								JSONObject bjScoreDis = new JSONObject();
								bjScoreDis.put("fbdm", tgs.getFbdm());
								bjScoreDis.put("rs", rs);
								bjScoreDis.put("ljrs", ljrs);
								bjScoreDis.put("fbsx", fbsx);
								bjScoreDis.put("fbxx", fbxx);

								fbdmfb.put(tgs.getFbdm(), bjScoreDis);
							}

						}
						// 排名分布
						int ljrs = 0;
						for (JSONObject pmsz : wlfzRankDisList) {
							int pmfbsx = pmsz.getIntValue("pmfbsx");
							int pmfbxx = pmsz.getIntValue("pmfbxx");
							int rs = ScoreUtil.sumNumFromScore2(bjcjList, pmfbsx, pmfbxx, "njzfpm");
							if (pmfbxx == 1) {
								ljrs = rs;
							} else {
								ljrs += rs;
							}
							// int ljrs =
							// ScoreUtil.sumLjNumFromScore(bjtjcjList,pmfbsx,pmfbxx,"njzfpm");
							String pmfbdm = pmsz.getString("pmfbdm");
							ScoreRankStatistics scoreRankStatistics = new ScoreRankStatistics();
							scoreRankStatistics.setBh(bh);
							scoreRankStatistics.setKslc(kslc);
							scoreRankStatistics.setLjrs(ljrs);
							scoreRankStatistics.setPmfbdm(pmfbdm);
							scoreRankStatistics.setRs(rs);
							scoreRankStatistics.setXnxq(xnxq);
							scoreRankStatistics.setXxdm(xxdm);
							scoreRankStatisticsList.add(scoreRankStatistics);
							// 总分排名分布--用于班级报告
							JSONObject zfdis = new JSONObject();
							zfdis.put("scoreDisName", pmfbxx + "-" + pmfbsx);
							zfdis.put("rs", rs);
							zfdis.put("ljrs", ljrs);
							bhPmszKmRsMap.put(bh + "-" + pmfbdm + "-" + "00000000", zfdis);
						}
						JSONObject njSeq = njSeqMap.get(bjObj.getString("nj"));
						List<ScoreLevelSequnce> seq1 = (List<ScoreLevelSequnce>) njSeq.get("1");
						List<ScoreLevelSequnce> seq2 = (List<ScoreLevelSequnce>) njSeq.get("2");
						List<ScoreLevelSequnce> seq3 = (List<ScoreLevelSequnce>) njSeq.get("3");
						// 班级-总分等级序列1
						int tljrs = 0;
						for (int m = 0; m < seq1.size(); m++) {
							ScoreLevelSequnce tgm = seq1.get(m);
							String djxl = tgm.getDjxl();
							String djxllb = tgm.getDjxllb();
							ClassScoreLevelSequnce tsl = new ClassScoreLevelSequnce();
							int qz = 0, rs = 0;
							qz = tgm.getDjqz();
							rs = GrepUtil.grepJsonKeyBySingleVal("djxl", djxl, bjtjcjList).size();
							if (rs != 0) {
								System.out.println("fdsfs");
							}
							float bl = ScoreUtil.castFloatTowPointNum((100.0f * rs / bjtjcjList.size()));
							tsl.setBh(bjObj.getString("bh"));
							tsl.setBl(bl);
							tsl.setDjxl(tgm.getDjxl());
							tsl.setDjxllb(djxllb);
							tsl.setKslc(kslc);
							tljrs += rs;
							tsl.setLjrs(tljrs);
							// 应设未设 tsl.setPm(pm);
							tsl.setRs(rs);
							tsl.setXnxq(xnxq);
							tsl.setXxdm(xxdm);

							classScoreLevelSequnceList.add(tsl);
						}
						// 等级序列2
						tljrs = 0;
						for (int m = 0; m < seq2.size(); m++) {
							ScoreLevelSequnce tgm = seq2.get(m);
							String djxl = tgm.getDjxl();
							String djxllb = tgm.getDjxllb();
							ClassScoreLevelSequnce tsl = new ClassScoreLevelSequnce();
							int qz = 0, rs = 0;
							qz = tgm.getDjqz();
							rs = GrepUtil.grepJsonKeyBySingleVal("djxl2", djxl, bjtjcjList).size();
							float bl = ScoreUtil.castFloatTowPointNum(100.0f * rs / bjtjcjList.size());
							tsl.setBh(bjObj.getString("bh"));
							tsl.setBl(bl);
							tsl.setDjxl(tgm.getDjxl());
							tsl.setDjxllb(djxllb);
							tsl.setKslc(kslc);
							tljrs += rs;
							tsl.setLjrs(tljrs);
							// 应设未设 tsl.setPm(pm);
							tsl.setRs(rs);
							tsl.setXnxq(xnxq);
							tsl.setXxdm(xxdm);

							classScoreLevelSequnceList.add(tsl);
						}
						// 等级序列3
						tljrs = 0;
						for (int m = 0; m < seq3.size(); m++) {
							ScoreLevelSequnce tgm = seq3.get(m);
							String djxl = tgm.getDjxl();
							String djxllb = tgm.getDjxllb();
							ClassScoreLevelSequnce tsl = new ClassScoreLevelSequnce();
							int qz = 0, rs = 0;
							qz = tgm.getDjqz();
							rs = GrepUtil.grepJsonKeyBySingleVal("djxl3", djxl, bjtjcjList).size();
							float bl = ScoreUtil.castFloatTowPointNum(100.0f * rs / bjtjcjList.size());
							tsl.setBh(bjObj.getString("bh"));
							tsl.setBl(bl);
							tsl.setDjxl(tgm.getDjxl());
							tsl.setDjxllb(djxllb);
							tsl.setKslc(kslc);
							tljrs += rs;
							tsl.setLjrs(tljrs);
							// 应设未设 tsl.setPm(pm);
							tsl.setRs(rs);
							tsl.setXnxq(xnxq);
							tsl.setXxdm(xxdm);

							classScoreLevelSequnceList.add(tsl);
						}
						// 班级-单科循环上面的方法
						for (JSONObject km : kmList) {
							JSONObject bjkmobj = new JSONObject();
							bjkmData.add(bjkmobj);
							// bjKmDataList
							JSONObject bjkmObj = new JSONObject();
							bjKmDataList.add(bjkmObj);
							bjkmObj.put("wlfz", wlfz);
							bjkmObj.put("bjfz", bjfz);
							bjkmObj.put("bjfzmc", bjObj.get("bjfzmc"));
							bjkmObj.put("bh", bh);
							bjkmObj.put("bjmc", bjObj.get("bjmc"));

							String kmdm = km.getString("kmdm");
							bhkmCjMap.put(bh + "_" + kmdm, bjkmObj);
							bjkmObj.put("kmdm", kmdm);
							List<JSONObject> kmcjList = GrepUtil.grepJsonKeyByVal(new String[] { "kmdm", "bh" },
									new String[] { kmdm, bh }, wlkmcjNjpm);
							// 参与人数与统计人数
							int kmcyrs = kmcjList.size();
							bjkmObj.put("ckrs", kmcyrs);
							List<JSONObject> kmtjList = GrepUtil.grepJsonKeyBySingleVal("sftj", "true", kmcjList);
							int kmtjrs = kmtjList.size();
							bjkmObj.put("tjrs", kmtjrs);
							if (kmtjList.size() == 0) {
								continue;
							}
							// 单科最高分与最低分
							float maxKmScr = kmtjList.get(0).getFloatValue("cj");
							bjkmObj.put("maxKmScr", maxKmScr);
							float minKmScr = kmtjList.get(kmtjrs - 1).getFloatValue("cj");
							bjkmObj.put("minKmScr", minKmScr);
							float avgKmScr = ScoreUtil.getPjf(kmtjList, "cj");
							bjkmObj.put("avgKmScr", avgKmScr);
							if (km.getInteger("isHb") == 0) {

								bjavgZfScr += avgKmScr;
							}
							JSONObject kmYxPerObj = ScoreUtil.getDjPercent(kmtjList, "Yx");
							float kmYxPer = kmYxPerObj.getFloat("bl");
							int kmYxRs = kmYxPerObj.getIntValue("rs");
							// 班级科目数据 -学生报告使用
							JSONObject bjDataFxKm = new JSONObject();
							bjDataFx.put(kmdm, bjDataFxKm);
							bjDataFxKm.put("bjpjf", maxKmScr);
							bjDataFxKm.put("bjzgf", avgKmScr);

							JSONObject kmHgPerObj = ScoreUtil.getDjPercent(kmtjList, "Hg");
							float kmHgPer = kmHgPerObj.getFloat("bl");
							int kmHgRs = kmHgPerObj.getIntValue("rs");

							JSONObject kmDfPerObj = ScoreUtil.getDjPercent(kmtjList, "Df");
							float kmDfPer = kmDfPerObj.getFloat("bl");
							int kmDfRs = kmDfPerObj.getIntValue("rs");

							JSONObject kmQnPerObj = ScoreUtil.getDjPercent(kmtjList, "Qn");
							float kmQnPer = kmQnPerObj.getFloat("bl");
							int kmQnRs = kmQnPerObj.getIntValue("rs");

							JSONObject kmJzPerObj = ScoreUtil.getDjPercent(kmtjList, "Jz");
							float kmJzPer = kmJzPerObj.getFloat("bl");
							int kmJzRs = kmJzPerObj.getIntValue("rs");

							// float kmYxPer =
							// ScoreUtil.getDjPercent(kmtjList, "Yx");
							bjkmObj.put("kmYxPer", kmYxPer);
							bjkmObj.put("kmHgPer", kmHgPer);
							bjkmObj.put("kmDfPer", kmDfPer);
							bjkmObj.put("kmJzPer", kmJzPer);
							bjkmObj.put("kmQnPer", kmQnPer);
							bjkmObj.put("kmYxRs", kmYxRs);
							bjkmObj.put("kmHgRs", kmHgRs);
							bjkmObj.put("kmDfRs", kmDfRs);
							bjkmObj.put("kmJzRs", kmJzRs);
							bjkmObj.put("kmQnRs", kmQnRs);
							float kmDiffiPer = ScoreUtil.dificultPoint(avgKmScr, km.getIntValue("mf"));
							bjkmObj.put("kmDiffiPer", kmDiffiPer);
							float kmDiffRate = ScoreUtil.diffrentRate(kmtjList, "cj", km.getIntValue("mf"));
							bjkmObj.put("kmDiffRate", kmDiffRate);
							bjkmobj.put(kmdm, avgKmScr);
							List<JSONObject> kmddList = GrepUtil.grepJsonKeyByVal(new String[] { "kmdm", "nj" },
									new String[] { kmdm, synj }, allDjList);
							List<Float> kmleft = new ArrayList<Float>();
							for (JSONObject xs : kmtjList) {
								kmleft.add(xs.getFloat("cj"));
							}
							float bjkmbzc = ScoreUtil.getDtSq(kmleft, avgKmScr);
							bjkmObj.put("kmbzc", bjkmbzc);
							for (JSONObject xs : kmcjList) {
								float kmbzf = 0;
								if (bjkmbzc != 0) {
									kmbzf = ScoreUtil.castFloatTowPointNum((xs.getFloat("cj") - avgKmScr) / bjkmbzc);
								}
								xs.put("kmbzf", kmbzf);
								// 科目标准分
								ScoreStuBzfMk tzf = new ScoreStuBzfMk();
								tzf.setBzf(kmbzf);
								tzf.setKmdm(kmdm);
								tzf.setKslc(kslc);
								tzf.setXh(xs.getString("xh"));
								tzf.setXnxq(xnxq);
								tzf.setXxdm(xxdm);
								scoreStuBzfMkList.add(tzf);
							}
							// 单科等级分析
							for (int l = 0; l < kmddList.size(); l++) {
								JSONObject dd = kmddList.get(l);
								String djmc = dd.getString("djmc");
								int djNum = GrepUtil.grepJsonKeyBySingleVal("djmc", djmc, kmtjList).size();
								float bl = ScoreUtil.castFloatTowPointNum((float) 100f * djNum / kmtjList.size());
								JSONObject bjdd = new JSONObject();
								bjdd.put("bl", bl);
								bjdd.put("djmc", djmc);
								bjdd.put("bh", bh);
								bjdd.put("wlfz", wlfz);
								bjdd.put("bjfz", bjfz);
								bjdd.put("kmdm", kmdm);
								bjdd.put("rs", djNum);
								if (l == 0) {
									bjdd.put("ljrs", djNum);
									dd.put("ljrs", djNum);
								} else {
									JSONObject last = kmddList.get(l - 1);
									bjdd.put("ljrs", djNum + last.getIntValue("ljrs"));
									dd.put("ljrs", djNum + last.getIntValue("ljrs"));
								}
								bjdd.put("djdm", dd.get("dm"));

								bhkmDDList.add(bjdd);

							}
							if (bhkmDDList.size() == 0) {
								needDdByMap.put(bh, false);
							}
							// 单科排名人数
							// 计算成绩分布 scoreDistributeList Zfmfqj
							int sdlrs = 0;
							for (ScoreDistributeKm tzf : kmScDisList) {
								String ff = scdWlMap.get(tzf.getDm());
								if (ff != null) {
									String lwfz = ff.split(",")[0];
									int mf = Integer.parseInt(ff.split(",")[1]);
									if (lwfz.equalsIgnoreCase(wlfz) && mf == km.getIntValue("mf")) {
										int fbsx = tzf.getFbsx();
										int fbxx = tzf.getFbxx();
										int rs = ScoreUtil.sumNumFromScore(kmcjList, fbsx, fbxx, "cj");
										sdlrs += rs;
										// int ljrs =
										// ScoreUtil.sumLjNumFromScore(kmtjList,fbsx,fbxx,"cj");
										ScoreClassDistributeMk scoreClassDistributeMk = new ScoreClassDistributeMk();
										scoreClassDistributeMk.setKmdm(kmdm);
										scoreClassDistributeMk.setBh(bh);
										scoreClassDistributeMk.setDm(tzf.getDm());
										scoreClassDistributeMk.setFbdm(tzf.getFbdm());
										scoreClassDistributeMk.setKslc(kslc);
										scoreClassDistributeMk.setXnxq(xnxq);
										scoreClassDistributeMk.setXxdm(xxdm);
										scoreClassDistributeMk.setRs(rs);
										scoreClassDistributeMk.setLjrs(sdlrs);
										scoreClassDistributeMkList.add(scoreClassDistributeMk);
									}
								}

							}
							// 计算排名分布
							int kmljrs = 0;
							for (JSONObject pmsz : wlfzRankDisList) {
								int pmfbsx = pmsz.getIntValue("pmfbsx");
								int pmfbxx = pmsz.getIntValue("pmfbxx");
								int rs = ScoreUtil.sumNumFromScore2(kmcjList, pmfbsx, pmfbxx, "njkmpm");
								if (pmfbxx == 1) {
									kmljrs = rs;
								} else {
									kmljrs += rs;
								}
								// int ljrs =
								// ScoreUtil.sumLjNumFromScore(kmtjList,pmfbsx,pmfbxx,"njkmpm");
								String pmfbdm = pmsz.getString("pmfbdm");
								ScoreRankStatisticsMk scoreRankStatisticsMk = new ScoreRankStatisticsMk();
								scoreRankStatisticsMk.setKmdm(kmdm);
								scoreRankStatisticsMk.setBh(bh);
								scoreRankStatisticsMk.setKslc(kslc);
								scoreRankStatisticsMk.setLjrs(kmljrs);
								scoreRankStatisticsMk.setPmfbdm(pmfbdm);
								scoreRankStatisticsMk.setRs(rs);
								scoreRankStatisticsMk.setXnxq(xnxq);
								scoreRankStatisticsMk.setXxdm(xxdm);
								scoreRankStatisticsMkList.add(scoreRankStatisticsMk);
								// 单科排名分布--用于班级报告
								JSONObject zfdis = new JSONObject();
								zfdis.put("scoreDisName", pmfbxx + "-" + pmfbsx);
								zfdis.put("rs", rs);
								zfdis.put("ljrs", ljrs);
								bhPmszKmRsMap.put(bh + "-" + pmfbdm + "-" + kmdm, zfdis);
							}
						}

						float bjzfDiffiPer = ScoreUtil.dificultPoint(bjavgZfScr, wlzf);
						bjObj.put("bjzfDiffiPer", bjzfDiffiPer);
						bjObj.put("bjzfpjf", bjavgZfScr);
						bjdata.put("bjzfpjf", bjavgZfScr);
						bjObj.put("bjavgZfScr", bjavgZfScr);
						bjDataFx.put("bjpjf", bjavgZfScr);
						// 标准差
						float bjzfbzc = ScoreUtil.getDtSq(left, bjavgZfScr);
						bjObj.put("bjzfbzc", bjzfbzc);
						for (JSONObject xs : bjcjList) {
							float zfbzf = 0;
							if (bjzfbzc != 0) {
								zfbzf = ScoreUtil.castFloatTowPointNum((xs.getFloat("zcj") - bjavgZfScr) / bjzfbzc);
							}
							xs.put("zfbzf", zfbzf);

							ScoreStuBzf tzf = new ScoreStuBzf();
							tzf.setBzf(zfbzf);
							tzf.setKslc(kslc);
							tzf.setXh(xs.getString("xh"));
							tzf.setXnxq(xnxq);
							tzf.setXxdm(xxdm);
							scoreStuBzfList.add(tzf);
						}
					}
					// 结束循环班级
					// 计算班级的平均分排名与分差、优秀、合格、低分
					List<JSONObject> bjZfDataList1 = GrepUtil.grepJsonKeyBySingleVal("bjfz", bjfz, bjZfDataList);

					if (bjZfDataList1.size() == 0) {
						continue;
					}
					float maxAvg = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjavgZfScr", "desc", "bj", "pjf").get(0)
							.getFloatValue("bjavgZfScr");
					float maxYxl = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfYxPer", "desc", "bj", "yxl").get(0)
							.getFloatValue("bjzfYxPer");
					float maxHgl = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfHgPer", "desc", "bj", "hgl").get(0)
							.getFloatValue("bjzfHgPer");
					float minDfl = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfDfPer", "asc", "bj", "dfl").get(0)
							.getFloatValue("bjzfDfPer");
					for (JSONObject bjObj : bjZfDataList1) {
						float pjfc = maxAvg - bjObj.getFloatValue("bjavgZfScr");
						bjObj.put("bjzfpjfc", pjfc);
						float yxlc = maxYxl - bjObj.getFloatValue("bjzfYxPer");
						bjObj.put("bjzfyxlc", yxlc);
						float hglc = maxHgl - bjObj.getFloatValue("bjzfHgPer");
						bjObj.put("bjzfhglc", hglc);
						float dflc = bjObj.getFloatValue("bjzfDfPer") - minDfl;
						bjObj.put("bjzfdflc", dflc);
					}
					for (JSONObject km : kmList) {
						String kmdm = km.getString("kmdm");
						List<JSONObject> bjKmDataList1 = GrepUtil.grepJsonKeyByVal(new String[] { "bjfz", "kmdm" },
								new String[] { bjfz, kmdm }, bjKmDataList);
						float kmmaxAvg = ScoreUtil.sorStuScoreList(bjKmDataList1, "avgKmScr", "desc", "km", "pjf")
								.get(0).getFloatValue("avgKmScr");
						float kmmaxYxl = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmYxPer", "desc", "km", "yxl").get(0)
								.getFloatValue("kmYxPer");
						float kmmaxHgl = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmHgPer", "desc", "km", "hgl").get(0)
								.getFloatValue("kmHgPer");
						float kmminDfl = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmDfPer", "asc", "km", "dfl").get(0)
								.getFloatValue("kmDfPer");
						for (JSONObject bjObj : bjKmDataList1) {
							float pjfc = kmmaxAvg - bjObj.getFloatValue("avgKmScr");
							bjObj.put("kmpjfc", pjfc);
							float yxlc = kmmaxYxl - bjObj.getFloatValue("kmYxPer");
							bjObj.put("kmyxlc", yxlc);
							float hglc = kmmaxHgl - bjObj.getFloatValue("kmHgPer");
							bjObj.put("kmhglc", hglc);
							float dflc = bjObj.getFloatValue("kmDfPer") - kmminDfl;
							bjObj.put("kmdflc", dflc);
						}
					}
					// 计算分组的标准差等
					float bjfzZfPjf = ScoreUtil.getPjf(bjZfDataList1, "bjavgZfScr");
					float bjfzZfbzc = ScoreUtil.getFzDtSq(bjZfDataList1, "bjavgZfScr", bjfzZfPjf);
					float bjfzZfpjfcz = ScoreUtil.getFzCz(bjZfDataList1, "bjavgZfScr"); // 班级分组总分平均分差值
					float bjfzZfdfcz = ScoreUtil.getFzCz(bjZfDataList1, "bjzfDfPer"); // 班级分组总分低分率差值
					float bjfzZfhgcz = ScoreUtil.getFzCz(bjZfDataList1, "bjzfHgPer"); // 班级分组总分合格率差值
					float bjfzZfyxcz = ScoreUtil.getFzCz(bjZfDataList1, "bjzfYxPer"); // 班级分组总分优秀率差值

					for (JSONObject km : kmList) {
						String kmdm = km.getString("kmdm");

						List<JSONObject> bjKmDataList1 = GrepUtil.grepJsonKeyByVal(new String[] { "bjfz", "kmdm" },
								new String[] { bjfz, kmdm }, bjKmDataList);
						if (bjKmDataList1.size() == 0) {
							continue;
						}
						ScoreGroupStatisticsMk scoreGroupStatisticsMk = kmBjTsgsMap.get(kmdm);

						float bjfzKmPjf = scoreGroupStatisticsMk.getPjf() == null ? 0f
								: scoreGroupStatisticsMk.getPjf();
						float bjfzKmbzc = ScoreUtil.getFzDtSq(bjKmDataList1, "avgKmScr", bjfzKmPjf);
						float bjfzKmpjfcz = ScoreUtil.getFzCz(bjKmDataList1, "avgKmScr"); // 班级分组科目平均分差值
						float bjfzKmdfcz = ScoreUtil.getFzCz(bjKmDataList1, "kmDfPer"); // 班级分组科目低分率差值
						float bjfzKmhgcz = ScoreUtil.getFzCz(bjKmDataList1, "kmHgPer"); // 班级分组科目合格率差值
						float bjfzKmyxcz = ScoreUtil.getFzCz(bjKmDataList1, "kmYxPer"); // 班级分组科目优秀率差值

						if (scoreGroupStatisticsMk.getKslc() != null) {
							// scoreGroupStatisticsMk.setPjf(bjfzKmPjf);
							scoreGroupStatisticsMk.setBzc(bjfzKmbzc);
							scoreGroupStatisticsMk.setYxlc(bjfzKmyxcz);
							scoreGroupStatisticsMk.setHglc(bjfzKmhgcz);
							scoreGroupStatisticsMk.setDflc(bjfzKmdfcz);
							scoreGroupStatisticsMk.setPjffc(bjfzKmpjfcz);
							scoreGroupStatisticsMk.setXnxq(xnxq);
							scoreGroupStatisticsMk.setXxdm(xxdm);
							scoreGroupStatisticsMk.setKmdm(kmdm);
							scoreGroupStatisticsMkList.add(scoreGroupStatisticsMk);
						}

						// 保存科目班级等第
						List<JSONObject> bhkmDDList1 = GrepUtil.grepJsonKeyByVal(new String[] { "bjfz", "kmdm" },
								new String[] { bjfz, kmdm }, bhkmDDList);
						List<JSONObject> temp = GrepUtil.grepJsonKeyBySingleVal("djmc", "A", bhkmDDList1);
						ScoreUtil.sorStuScoreList(temp, "bl", "desc", "bj", "dd");
						// bhkmDDList1 =
						// ScoreUtil.sorStuScoreList(bhkmDDList1, "bl",
						// "desc", "bj", "dd");
						if (bhkmDDList1.size() > 0) {
							float maxDDbl = temp.get(0).getFloatValue("bl");
							for (JSONObject dd : bhkmDDList1) {
								ClassScoreLevelMk cmk = new ClassScoreLevelMk();
								cmk.setBh(dd.getString("bh"));
								cmk.setBl(ScoreUtil.castFloatTowPointNum(dd.getFloat("bl")));
								cmk.setDj(dd.getString("djmc"));
								cmk.setDjdm(dd.getString("djdm"));
								cmk.setKmdm(kmdm);
								cmk.setKslc(kslc);
								cmk.setLc(ScoreUtil.castFloatTowPointNum(maxDDbl - dd.getFloat("bl")));
								cmk.setLjrs(dd.getIntValue("ljrs"));
								if (dd.containsKey("bjddpm")) {

									cmk.setPm(dd.getIntValue("bjddpm"));
								}
								cmk.setRs(dd.getIntValue("rs"));
								cmk.setXnxq(xnxq);
								cmk.setXxdm(xxdm);

								classScoreLevelMkList.add(cmk);
							}

						}
					}
				}
				// 结束循环班级分组

				// 计算文理分组的标准差等
				// 计算分组的标准差等
				// 计算年级报告
				List<JSONObject> bjZfDataList1 = GrepUtil.grepJsonKeyBySingleVal("wlfz", wlfz, bjZfDataList);
				;
				float wlfzZfPjf = ScoreUtil.getPjf(bjZfDataList1, "bjavgZfScr");
				// float wlfzZfPjf = avgZfScr;
				float wlfzZfbzc = ScoreUtil.getFzDtSq(bjZfDataList1, "bjavgZfScr", wlfzZfPjf);
				AnalysisReportNj analysisReportNj = new AnalysisReportNj();
				analysisReportNj.setXnxq(xnxq);
				analysisReportNj.setXxdm(xxdm);
				analysisReportNj.setKslc(kslc);
				analysisReportNj.setFzdm(wlfz);
				analysisReportNj.setNj(wlbjfz.get(wlfz).get(0).getString("nj"));
				// 优秀 合格 低分 排名与率差
				// 获取平均分排名 取其前三
				List<JSONObject> bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjavgZfScr", "desc", "wlfz",
						"pjf");
				String rt_asg_pjf_top3 = ScoreUtil.getTopNPmInList(bjZfDataList2, "wlfzpjfpm", 3, "bjmc", 0);
				float rt_asg_pjf_cz = bjZfDataList2.get(0).getFloatValue("bjavgZfScr")
						- bjZfDataList2.get(bjZfDataList2.size() - 1).getFloatValue("bjavgZfScr");
				rt_asg_pjf_cz = ScoreUtil.castFloatTowPointNum(rt_asg_pjf_cz);
				JSONObject BJTSY = new JSONObject();
				BJTSY.put("pjf", "班级平均分方面，取得前三名的班级分别是：" + rt_asg_pjf_top3 + ";最高分和最低分相差" + rt_asg_pjf_cz + "分。");
				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfYxPer", "desc", "wlfz", "yxl");
				float wlfzZfyxlc = bjZfDataList2.get(0).getFloatValue("bjzfYxPer")
						- bjZfDataList2.get(bjZfDataList2.size() - 1).getFloatValue("bjzfYxPer");
				wlfzZfyxlc = ScoreUtil.castFloatTowPointNum(wlfzZfyxlc);
				String rt_asg_yxl_top3 = ScoreUtil.getTopNPmInList(bjZfDataList2, "wlfzyxlpm", 3, "bjmc", 0);
				BJTSY.put("yxl", "班级优秀率方面，取得前三名的班级分别是：" + rt_asg_yxl_top3 + ";最高和最低相差" + wlfzZfyxlc + "。");
				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfHgPer", "desc", "wlfz", "hgl");
				float wlfzZfhglc = bjZfDataList2.get(0).getFloatValue("bjzfHgPer")
						- bjZfDataList2.get(bjZfDataList2.size() - 1).getFloatValue("bjzfHgPer");
				wlfzZfhglc = ScoreUtil.castFloatTowPointNum(wlfzZfhglc);
				String rt_asg_hgl_top3 = ScoreUtil.getTopNPmInList(bjZfDataList2, "wlfzhglpm", 3, "bjmc", 0);
				BJTSY.put("hgl", "班级合格率方面，取得前三名的班级分别是：" + rt_asg_hgl_top3 + ";最高和最低相差" + wlfzZfhglc + "。");
				List<JSONObject> kmtemp = GrepUtil.grepJsonKeyBySingleVal("isHb", "0", kmList);
				String rsddqk = ScoreUtil.getRsDdQk(wlzcjNjpm, bjZfDataList2, kmtemp.size(), needDdByMap);
				String fsdqk = ScoreUtil.getFsdQk(wlzcjNjpm, bjZfDataList1, 200);
				BJTSY.put("rsddqk", rsddqk);
				BJTSY.put("fsdqk", fsdqk);
				// 生成平均分对比表数据
				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList2, "bh", "asc", "bh", "pm");
				JSONObject pjfdbbObj = ScoreUtil.getDbbByList(bjZfDataList2, new String[] { "bjavgZfScr", "bjpjfpm" },
						new String[] { "平均分", "平均分排名" });
				analysisReportNj.setBjtsy(BJTSY.toJSONString());
				analysisReportNj.setPjfdbb(pjfdbbObj.toJSONString());	// 平均分对比表
				// 生成优秀率对比表数据
				JSONObject yxldbbObj = ScoreUtil.getDbbByList(bjZfDataList2, new String[] { "bjzfYxPer", "bjyxlpm" },
						new String[] { "优秀率", "优秀率排名" });
				analysisReportNj.setYxldbb(yxldbbObj.toJSONString());	// 优秀率对比表

				// 生成合格率对比表数据
				JSONObject hgldbbObj = ScoreUtil.getDbbByList(bjZfDataList2, new String[] { "bjzfHgPer", "bjhglpm" },
						new String[] { "合格率", "合格率排名" });
				analysisReportNj.setHgldbb(hgldbbObj.toJSONString());	// 合格率对比表
				// 生成等第值对比表数据
				JSONObject ddzddb = ScoreUtil.getDddbbByList(wlzcjNjpm, bjZfDataList2, kmtemp.size(), needDdByMap);
				analysisReportNj.setDdzdbb(ddzddb.toJSONString());	// 等第值对比表
				// 生成排名分布对比表数据
				JSONObject pmdbb = ScoreUtil.getPmdbbByList(wlzcjNjpm, bjZfDataList2);
				analysisReportNj.setPmdbb(pmdbb.toJSONString());	// 排名对比表

				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfDfPer", "asc", "wlfz", "dfl");
				float wlfzZfdflc = bjZfDataList2.get(bjZfDataList2.size() - 1).getFloatValue("bjzfDfPer")
						- bjZfDataList2.get(0).getFloatValue("bjzfDfPer");

				wlfzZfdflc = ScoreUtil.castFloatTowPointNum(wlfzZfdflc);

				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfDiffiPer", "desc", "wlfz", "ld");
				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfbzc", "desc", "wlfz", "bzc");
				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfDiffRate", "desc", "wlfz", "qfd");
				// 文理分组平均分分差
				float wlfzZfpjfcz = ScoreUtil.getFzCz(bjZfDataList1, "bjavgZfScr");

				ScoreGroupStatistics scoreGroupStatistics = new ScoreGroupStatistics();
				scoreGroupStatistics.setBzc(wlfzZfbzc);
				scoreGroupStatistics.setCkrs(zfcyrs);
				scoreGroupStatistics.setDfl(zfDfPer);
				scoreGroupStatistics.setDflc(wlfzZfdflc);
				scoreGroupStatistics.setDfrs(zfDfRs);
				// 难度系数
				scoreGroupStatistics.setDlxs(zfDiffiPer);
				scoreGroupStatistics.setFzdm(wlfz);
				scoreGroupStatistics.setHgl(zfHgPer);
				scoreGroupStatistics.setHglc(wlfzZfhglc);
				scoreGroupStatistics.setHgrs(zfHgRs);
				scoreGroupStatistics.setJzsrs(zfJzRs);
				scoreGroupStatistics.setKslc(kslc);
				scoreGroupStatistics.setPjf(avgZfScr);
				scoreGroupStatistics.setPjffc(wlfzZfpjfcz);
				scoreGroupStatistics.setQfd(zfDiffRate);
				scoreGroupStatistics.setQnsrs(zfQnRs);
				scoreGroupStatistics.setTjrs(zftjrs);
				scoreGroupStatistics.setXnxq(xnxq);
				scoreGroupStatistics.setXxdm(xxdm);
				scoreGroupStatistics.setYxl(zfYxPer);
				scoreGroupStatistics.setYxlc(wlfzZfyxlc);
				scoreGroupStatistics.setYxrs(zfYxRs);

				scoreGroupStatisticsList.add(scoreGroupStatistics);
				// 计算班级标准分
				for (JSONObject bj : bjZfDataList1) {
					if (bj.containsKey("bjavgZfScr")) {
						float param = (bj.getFloatValue("bjavgZfScr") - wlfzZfPjf) / wlfzZfbzc;
						float bjbzf = ScoreUtil.castFloatTowPointNum(param);
						bj.put("bjbzf", bjbzf);
						// 新建班级总分统计结果
						ScoreClassStatistics scoreClassStatistics = new ScoreClassStatistics();
						scoreClassStatistics.setBh(bj.getString("bh"));
						// 班级报告
						bhClassStaMap.put(scoreClassStatistics.getBh(), scoreClassStatistics);
						scoreClassStatistics.setXnxq(xnxq);
						scoreClassStatistics.setKslc(kslc);
						scoreClassStatistics.setXxdm(xxdm);
						scoreClassStatistics.setCkrs(bj.getIntValue("ckrs"));
						scoreClassStatistics.setTjrs(bj.getIntValue("tjrs"));
						scoreClassStatistics.setPjf(bj.getFloat("bjavgZfScr"));
						scoreClassStatistics.setPjffc(ScoreUtil.castFloatTowPointNum(bj.getFloat("bjzfpjfc")));
						scoreClassStatistics.setBzc(bj.getFloat("bjzfbzc"));
						scoreClassStatistics.setZgf(bj.getFloat("bjmaxZfScr"));
						scoreClassStatistics.setZdf(bj.getFloat("bjminZfScr"));
						scoreClassStatistics.setBzf(bjbzf);
						scoreClassStatistics.setYxl(bj.getFloat("bjzfYxPer"));
						scoreClassStatistics.setYxrs(bj.getIntValue("bjzfYxRs"));
						scoreClassStatistics.setYxlc(ScoreUtil.castFloatTowPointNum(bj.getFloatValue("bjzfyxlc")));
						scoreClassStatistics.setHglc(ScoreUtil.castFloatTowPointNum(bj.getFloatValue("bjzfhglc")));
						scoreClassStatistics.setDflc(ScoreUtil.castFloatTowPointNum(bj.getFloatValue("bjzfdflc")));
						scoreClassStatistics.setHgl(bj.getFloat("bjzfHgPer"));
						scoreClassStatistics.setHgrs(bj.getIntValue("bjzfHgRs"));
						scoreClassStatistics.setDfl(bj.getFloat("bjzfDfPer"));
						scoreClassStatistics.setDfrs(bj.getIntValue("bjzfDfRs"));
						scoreClassStatistics.setQnsrs(bj.getIntValue("bjzfQnRs"));
						scoreClassStatistics.setJzsrs(bj.getIntValue("bjzfJzRs"));
						scoreClassStatistics.setPm(bj.getIntValue("bjpjfpm"));	// 班级平均分排名
						scoreClassStatistics.setYxlpm(bj.getIntValue("bjyxlpm"));
						scoreClassStatistics.setHglpm(bj.getIntValue("bjhglpm"));
						scoreClassStatistics.setDflpm(bj.getIntValue("bjdflpm"));
						scoreClassStatistics.setMf((float) wlzf);
						scoreClassStatistics.setLdxs(bj.getFloat("bjzfDiffiPer"));
						scoreClassStatistics.setQfd(bj.getFloat("bjzfDiffRate"));
						scoreClassStatistics.setLdpm(bj.getIntValue("bjldpm"));
						scoreClassStatistics.setBzcpm(bj.getIntValue("bjbzcpm"));
						scoreClassStatistics.setQfdpm(bj.getIntValue("bjqfdpm"));
						scoreClassStatistics.setBh(bj.getString("bh"));
						scoreClassStatisticsList.add(scoreClassStatistics);
					}

				}
				// 计算班级分组单科等第
				for (JSONObject bjdd : bjfzkmDDList) {
					String kmdm = bjdd.getString("kmdm");
					String bjfz = bjdd.getString("bjfz");
					String djdm = bjdd.getString("djdm");
					List<JSONObject> bhkmDDList1 = GrepUtil.grepJsonKeyByVal(new String[] { "bjfz", "kmdm", "djdm" },
							new String[] { bjfz, kmdm, djdm }, bhkmDDList);
					float lc = ScoreUtil.getFzCz(bhkmDDList1, "bl");
					float rsc = ScoreUtil.getFzCz(bhkmDDList1, "rs");
					GroupScoreLevelMk cmk = new GroupScoreLevelMk();
					cmk.setFzdm(bjfz);
					cmk.setBl(bjdd.getFloat("bl"));
					cmk.setDj(bjdd.getString("djmc"));
					cmk.setDjdm(bjdd.getString("djdm"));
					cmk.setKmdm(kmdm);
					cmk.setKslc(kslc);
					cmk.setLc(lc * 100.0f);
					cmk.setLjrs(bjdd.getIntValue("ljrs"));
					cmk.setRs(bjdd.getIntValue("rs"));
					cmk.setXnxq(xnxq);
					cmk.setXxdm(xxdm);
					cmk.setRsc((int) rsc);
					groupScoreLevelMkList.add(cmk);
				}
				// 计算文理分组单科等第
				for (JSONObject bjdd : wlfzkmDDList) {
					String kmdm = bjdd.getString("kmdm");
					String djdm = bjdd.getString("djdm");
					List<JSONObject> bhkmDDList1 = GrepUtil.grepJsonKeyByVal(new String[] { "kmdm", "djdm" },
							new String[] { kmdm, djdm }, bhkmDDList);
					if (bhkmDDList1.size() == 0) {
						continue;
					}
					float lc = ScoreUtil.getFzCz(bhkmDDList1, "bl");
					float rsc = ScoreUtil.getFzCz(bhkmDDList1, "rs");
					GroupScoreLevelMk cmk = new GroupScoreLevelMk();
					cmk.setFzdm(wlfz);
					cmk.setBl(bjdd.getFloat("bl"));
					cmk.setDj(bjdd.getString("djmc"));
					cmk.setDjdm(bjdd.getString("djdm"));
					cmk.setKmdm(kmdm);
					cmk.setKslc(kslc);
					cmk.setLc(lc);
					cmk.setLjrs(bjdd.getIntValue("ljrs"));
					cmk.setRs(bjdd.getIntValue("rs"));
					cmk.setXnxq(xnxq);
					cmk.setXxdm(xxdm);
					cmk.setRsc((int) rsc);
					groupScoreLevelMkList.add(cmk);
				}
				// 学科角度提示语
				JSONArray kmtsy = new JSONArray();
				// 学科对比表
				JSONArray kmddb = new JSONArray();
				// 教师提示语
				JSONArray jstsy = new JSONArray();
				// 教师对比表
				JSONArray jskmdbb = new JSONArray();

				// 循环科目 计算科目的标准差等
				for (JSONObject km : kmList) {
					// 单科提示语
					JSONObject kmts = new JSONObject();
					// 学科对比
					JSONObject xkdb = new JSONObject();
					// 教师科目提示语
					JSONObject jskmts = new JSONObject();

					String kmmc = km.getString("kmmc");
					kmts.put("kmmc", kmmc);
					jskmts.put("kmmc", kmmc);
					String kmdm = km.getString("kmdm");

					ScoreGroupStatisticsMk tsgsMk = kmTsgsMap.get(kmdm);
					if (wlfz.equals("dedbf664-b20f-43a0-a447-1b9715ebb25c")) {
						System.out.println("fdssd");
					}
					List<JSONObject> bjKmDataList1 = GrepUtil.grepJsonKeyByVal(new String[] { "wlfz", "kmdm" },
							new String[] { wlfz, kmdm }, bjKmDataList);
					if (bjKmDataList1.size() == 0) {
						continue;
					}
					kmtsy.add(kmts);
					float wlfzKmPjf = tsgsMk.getPjf();
					List<JSONObject> bjKmDataList0 = ScoreUtil.sorStuScoreList(bjKmDataList1, "avgKmScr", "desc",
							"wlfz", "pjf");
					String xk_pjf_tsy = ScoreUtil.getXkTsyByList("avgKmScr", bjKmDataList0);
					kmts.put("pjf", "平均分" + xk_pjf_tsy);
					float wlfzKmbzc = ScoreUtil.getFzDtSq(bjKmDataList1, "avgKmScr", wlfzKmPjf);
					// 优秀 合格 低分 排名与率差
					List<JSONObject> bjKmDataList2 = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmYxPer", "desc", "wlfz",
							"yxl");
					// 学科优秀率提示语
					String xk_yxl_tsy = ScoreUtil.getXkTsyByList("kmYxPer", bjKmDataList1);
					kmts.put("yxl", "优秀率" + xk_yxl_tsy);
					float wlfzKmyxlc = bjKmDataList2.get(0).getFloatValue("kmYxPer")
							- bjKmDataList2.get(bjKmDataList2.size() - 1).getFloatValue("kmYxPer");
					// 学科合格率提示语
					List<JSONObject> bjKmDataList3 = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmHgPer", "desc", "wlfz",
							"hgl");
					String xk_hgl_tsy = ScoreUtil.getXkTsyByList("kmHgPer", bjKmDataList3);
					kmts.put("hgl", "合格率" + xk_hgl_tsy);
					// A人数提示语
					String xk_ars_tsy = ScoreUtil.getArsTysByList(bjKmDataList1, wlkmcjNjpm, kmdm, needDdByMap);
					kmts.put("Ars", xk_ars_tsy);
					// 生成科目对比表
					xkdb = ScoreUtil.getKmDbbByList(bjKmDataList1, kmmc, needDdByMap);
					kmddb.add(xkdb);
					bjKmDataList2 = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmHgPer", "desc", "wlfz", "hgl");
					float wlfzKmhglc = bjKmDataList2.get(0).getFloatValue("kmHgPer")
							- bjKmDataList2.get(bjKmDataList2.size() - 1).getFloatValue("kmHgPer");
					bjKmDataList2 = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmDfPer", "asc", "wlfz", "dfl");
					float wlfzKmdflc = bjKmDataList2.get(bjKmDataList2.size() - 1).getFloatValue("kmDfPer")
							- bjKmDataList2.get(0).getFloatValue("kmDfPer");
					// 生成教师-科目角度提示语
					List<JSONObject> TeacherClassListKM = GrepUtil.grepJsonKeyByVal(new String[] { "wlfz", "kmdm" },
							new String[] { wlfz, kmdm }, TeacherClassList);
					HashMap<String, JSONObject> teaMap = new HashMap<String, JSONObject>();
					for (JSONObject tc : TeacherClassListKM) {
						JSONObject kmbj = bhkmCjMap.get(tc.getString("bh") + "_" + kmdm);
						if (kmbj == null || kmbj.get("avgKmScr") == null || kmbj.get("kmYxPer") == null) {
							continue;
						}
						if (kmbj != null) {
							tc.put("pjf", kmbj.getFloat("avgKmScr"));
							tc.put("yxl", kmbj.getFloat("kmYxPer"));
						}
						String key = tc.getString("zgh");
						if (teaMap.containsKey(key)) {
							JSONObject obj = teaMap.get(key);
							obj.put("num", obj.getIntValue("num") + 1);
							obj.put("sumpjf", (obj.getFloat("sumpjf") != null ? obj.getFloat("sumpjf") : 0)
									+ kmbj.getFloat("avgKmScr"));
							obj.put("sumyxl", (obj.getFloat("sumyxl") != null ? obj.getFloat("sumyxl") : 0)
									+ kmbj.getFloat("kmYxPer"));
						} else {

							JSONObject obj = new JSONObject();
							obj.put("num", 1);
							if (kmbj != null) {

								obj.put("sumpjf", kmbj.getFloat("avgKmScr"));
								obj.put("sumyxl", kmbj.getFloat("kmYxPer"));
							}
							teaMap.put(key, obj);
						}
					}
					TeacherClassListKM = new ArrayList<JSONObject>();
					for (Iterator<String> tkm = teaMap.keySet().iterator(); tkm.hasNext();) {
						String key = tkm.next();
						JSONObject obj = teaMap.get(key);
						obj.put("pjf",
								(obj.getFloat("sumpjf") != null ? obj.getFloat("sumpjf") : 0) / obj.getIntValue("num"));
						obj.put("yxl",
								(obj.getFloat("sumyxl") != null ? obj.getFloat("sumyxl") : 0) / obj.getIntValue("num"));
						obj.put("zgh", key);
						TeacherClassListKM.add(obj);
					}
					if (TeacherClassListKM.size() > 0) {

						// 获取教师角度 平均分提示语
						String js_km_pjf_tsy = ScoreUtil.getJsKmTsyByList(TeacherClassListKM, "pjf");
						jskmts.put("pjf", js_km_pjf_tsy);
						String js_km_yxl_tsy = ScoreUtil.getJsKmTsyByList(TeacherClassListKM, "yxl");
						jskmts.put("yxl", js_km_yxl_tsy);

						jstsy.add(jskmts);

						// 获取教师学科对比表
						JSONObject jskmdb = ScoreUtil.getJsKmDbByList(TeacherClassListKM, kmmc, teacherMapDic);
						jskmdbb.add(jskmdb);
					}

					// 文理分组平均分分差
					float wlfzKmpjfcz = ScoreUtil.getFzCz(bjKmDataList1, "avgKmScr");
					if (tsgsMk.getKslc() != null) {

						// tsgsMk.setPjf(wlfzKmPjf);
						tsgsMk.setBzc(ScoreUtil.castFloatTowPointNum(wlfzKmbzc));
						tsgsMk.setYxlc(ScoreUtil.castFloatTowPointNum(wlfzKmyxlc));
						tsgsMk.setHglc(ScoreUtil.castFloatTowPointNum(wlfzKmhglc));
						tsgsMk.setDflc(ScoreUtil.castFloatTowPointNum(wlfzKmdflc));
						tsgsMk.setPjffc(ScoreUtil.castFloatTowPointNum(wlfzKmpjfcz));

						scoreGroupStatisticsMkList.add(tsgsMk);
					}

					// 计算班级标准分
					for (JSONObject bj : bjKmDataList1) {
						if (bj.containsKey("avgKmScr")) {
							float param = (bj.getFloatValue("avgKmScr") - wlfzKmPjf) / wlfzKmbzc;
							float bjbzf = ScoreUtil.castFloatTowPointNum(param);
							bj.put("bjbzf", bjbzf);
							// 新建科目总分统计结果
							ScoreClassStatisticsMk scoreClassStatisticsMk = new ScoreClassStatisticsMk();
							scoreClassStatisticsMk.setBh(bj.getString("bh"));
							String bh = scoreClassStatisticsMk.getBh();

							if (!bhKmClassStaMap.containsKey(bh)) {
								bhKmClassStaMap.put(bh, new HashMap<String, ScoreClassStatisticsMk>());
							}
							bhKmClassStaMap.get(bh).put(kmdm, scoreClassStatisticsMk);

							scoreClassStatisticsMk.setXnxq(xnxq);
							scoreClassStatisticsMk.setKslc(kslc);
							scoreClassStatisticsMk.setXxdm(xxdm);
							scoreClassStatisticsMk.setCkrs(bj.getIntValue("ckrs"));
							scoreClassStatisticsMk.setTjrs(bj.getIntValue("tjrs"));
							scoreClassStatisticsMk.setPjf(bj.getFloatValue("avgKmScr"));
							scoreClassStatisticsMk.setPjffc(ScoreUtil.castFloatTowPointNum(bj.getFloat("kmpjfc")));
							scoreClassStatisticsMk.setBzc(ScoreUtil.castFloatTowPointNum(bj.getFloat("kmbzc")));
							scoreClassStatisticsMk.setZgf(bj.getFloatValue("maxKmScr"));
							scoreClassStatisticsMk.setZdf(bj.getFloatValue("minKmScr"));
							scoreClassStatisticsMk.setBzf(bjbzf);
							scoreClassStatisticsMk.setYxl(bj.getFloatValue("kmYxPer"));
							scoreClassStatisticsMk.setYxrs(bj.getIntValue("kmYxRs"));
							scoreClassStatisticsMk.setHgl(bj.getFloatValue("kmHgPer"));
							scoreClassStatisticsMk.setHgrs(bj.getIntValue("kmHgRs"));
							scoreClassStatisticsMk.setDfl(bj.getFloatValue("kmDfPer"));
							scoreClassStatisticsMk.setDfrs(bj.getIntValue("kmDfRs"));
							scoreClassStatisticsMk.setQnsrs(bj.getIntValue("kmQnRs"));
							scoreClassStatisticsMk.setJzsrs(bj.getIntValue("kmJzRs"));
							scoreClassStatisticsMk.setPm(bj.getIntValue("kmpjfpm"));
							scoreClassStatisticsMk.setYxlpm(bj.getIntValue("kmyxlpm"));
							scoreClassStatisticsMk.setHglpm(bj.getIntValue("kmhglpm"));
							scoreClassStatisticsMk.setDflpm(bj.getIntValue("kmdflpm"));
							scoreClassStatisticsMk.setMf(km.getFloatValue("mf"));
							scoreClassStatisticsMk.setLdxs(bj.getFloatValue("kmDiffiPer"));
							scoreClassStatisticsMk.setQfd(bj.getFloatValue("kmDiffRate"));
							scoreClassStatisticsMk.setLdpm(bj.getIntValue("kmldpm"));
							scoreClassStatisticsMk.setBzcpm(bj.getIntValue("kmbzcpm"));
							scoreClassStatisticsMk.setQfdpm(bj.getIntValue("kmqfdpm"));
							scoreClassStatisticsMk.setBh(bj.getString("bh"));
							scoreClassStatisticsMk.setYxlc(ScoreUtil.castFloatTowPointNum((float) bj.get("kmyxlc")));
							scoreClassStatisticsMk.setHglc(ScoreUtil.castFloatTowPointNum((float) bj.get("kmhglc")));
							scoreClassStatisticsMk.setDflc(ScoreUtil.castFloatTowPointNum((float) bj.get("kmdflc")));
							scoreClassStatisticsMk.setKmdm(kmdm);
							scoreClassStatisticsMkList.add(scoreClassStatisticsMk);
						}
					}
				}
				// 科目循环结束
				// 继续生成年级报告
				analysisReportNj.setKmtsy(kmtsy.toJSONString());
				analysisReportNj.setKmdbb(kmddb.toJSONString());
				analysisReportNj.setJstsy(jstsy.toJSONString());
				analysisReportNj.setJskmdbb(jskmdbb.toJSONString());
				analysisReportnjList.add(analysisReportNj);

				Map<String, Integer> xhbzzfmap = new HashMap<String, Integer>();
				for (JSONObject xs : wlkmcjNjpm) {
					ScoreStuStatisticsRankMk scoreStuStatisticsRankMk = new ScoreStuStatisticsRankMk();
					scoreStuStatisticsRankMk.setBjpm(xs.getIntValue("bjkmpm"));
					scoreStuStatisticsRankMk.setNjpm(xs.getIntValue("njkmpm"));
					if (dbkslcs != null && dbkslcs.length() > 0) {
						JSONObject old = lastXsKmMap.get(xs.getString("xh") + "_" + xs.getString("kmdm"));
						if (old != null) {

							scoreStuStatisticsRankMk
									.setBjpmgz(old.getIntValue("bjpm") - scoreStuStatisticsRankMk.getBjpm());
							scoreStuStatisticsRankMk
									.setNjpmgz(old.getIntValue("njpm") - scoreStuStatisticsRankMk.getNjpm());
							String kmdm = xs.getString("kmdm");
							String kmmc = xs.getString("kmmc");
							JSONObject stu = new JSONObject();
							stu.put("xh", xs.getString("xh"));
							stu.put("xm", xs.getString("xm"));
							int bpmgz = scoreStuStatisticsRankMk.getBjpmgz();
							stu.put("pmsj", bpmgz);

							if (bpmgz >= 0 && bpmgz >= sarSetBj01) {
								HashMap<String, JSONObject> ycMap = new HashMap<String, JSONObject>();
								if (bhKmYcXsList.containsKey(xs.getString("bh"))) {
									ycMap = bhKmYcXsList.get(xs.getString("bh"));
								}
								JSONObject o = new JSONObject();
								if (ycMap.containsKey(kmdm)) {
									o = bhKmYcXsList.get(xs.getString("bh")).get(kmdm);
								}
								JSONArray jb = new JSONArray();
								if (o.containsKey("jb")) {
									jb = o.getJSONArray("jb");
								}
								jb.add(stu);
								o.put("kmdm", kmdm);
								o.put("kmmc", kmmc);
								o.put("jb", jb);
								ycMap.put(kmdm, o);
								bhKmYcXsList.put(xs.getString("bh"), ycMap);
							} else if (bpmgz <= 0 && Math.abs(bpmgz) >= sarSetBj02) {
								HashMap<String, JSONObject> ycMap = new HashMap<String, JSONObject>();
								if (bhKmYcXsList.containsKey(xs.getString("bh"))) {
									ycMap = bhKmYcXsList.get(xs.getString("bh"));
								}
								JSONObject o = new JSONObject();
								if (ycMap.containsKey(kmdm)) {
									o = bhKmYcXsList.get(xs.getString("bh")).get(kmdm);
								}
								JSONArray jb = new JSONArray();
								if (o.containsKey("yc")) {
									jb = o.getJSONArray("yc");
								}
								jb.add(stu);
								o.put("kmdm", kmdm);
								o.put("kmmc", kmmc);
								o.put("yc", jb);
								ycMap.put(kmdm, o);
								bhKmYcXsList.put(xs.getString("bh"), ycMap);
							}
						}
					}
					scoreStuStatisticsRankMk.setXh(xs.getString("xh"));
					scoreStuStatisticsRankMk.setXnxq(xnxq);
					scoreStuStatisticsRankMk.setKslc(kslc);
					scoreStuStatisticsRankMk.setXxdm(xxdm);
					scoreStuStatisticsRankMk.setCj(xs.getFloatValue("cj"));
					scoreStuStatisticsRankMk.setDj(xs.getString("djmc"));
					scoreStuStatisticsRankMk.setKmdm(xs.getString("kmdm"));
					int mf = 0;
					for (JSONObject km : kmList) {
						if (km.getString("kmdm").equals(xs.getString("kmdm"))) {
							mf = km.getIntValue("mf");
						}
					}
					double cj = 0d;
					if (xs.containsKey("cj") && !xs.getString("cj").equals("")) {
						cj = xs.getDouble("cj");
					}
					Double bzcj = Math.sqrt(mf * cj);
					scoreStuStatisticsRankMk.setBzcj((float) bzcj.intValue());
					if (xhbzzfmap.containsKey(xs.getString("xh"))) {
						Integer bzzf = xhbzzfmap.get(xs.getString("xh"));
						for (JSONObject km : kmList) {
							if (km.getString("kmdm").equals(xs.getString("kmdm")) && xs.getString("isHb").equals("0")) {
								bzzf += bzcj.intValue();
								xhbzzfmap.put(xs.getString("xh"), bzzf);
							}
						}

					} else {
						xhbzzfmap.put(xs.getString("xh"), bzcj.intValue());
					}
					if (xs.getBooleanValue("sftj")) {
						scoreStuStatisticsRankMk.setSftj("1");
					} else {
						scoreStuStatisticsRankMk.setSftj("0");
					}
					scoreStuStatisticsRankMkList.add(scoreStuStatisticsRankMk);
					xs.put("bjkmpmgz", scoreStuStatisticsRankMk.getBjpmgz());
					xs.put("njkmpmgz", scoreStuStatisticsRankMk.getNjpmgz());

					String key = wlfz + xs.getString("kmdm");
					if (!scoreStuStatisticsRankMkMapKM.containsKey(key)) {
						scoreStuStatisticsRankMkMapKM.put(key, new ArrayList<Object>());
					}
					scoreStuStatisticsRankMkMapKM.get(key).add(scoreStuStatisticsRankMk);

					String key2 = xs.getString("bh") + "_" + xs.getString("kmdm");
					if (!scoreStuStatisticsRankMkMap.containsKey(key2)) {
						scoreStuStatisticsRankMkMap.put(key2, new ArrayList<Object>());
					}
					scoreStuStatisticsRankMkMap.get(key2).add(scoreStuStatisticsRankMk);
				}

				for (Map.Entry<String, List<Object>> entry : scoreStuStatisticsRankMkMap.entrySet()) {
					List<Object> obj = entry.getValue();
					ScoreUtil.sorStuScoreListTwo(obj, "bzbjpm", "desc", "nj", "zf");
				}

				for (Iterator<String> tkm = scoreStuStatisticsRankMkMapKM.keySet().iterator(); tkm.hasNext();) {
					String key = tkm.next();
					List<Object> obj = scoreStuStatisticsRankMkMapKM.get(key);
					ScoreUtil.sorStuScoreListTwo(obj, "bznjpm", "desc", "nj", "zf");
				}

				// bjDataForXsReport
				HashMap<String, JSONObject> kmdmKmObjMap = new HashMap<String, JSONObject>();
				for (JSONObject km : kmList) {
					kmdmKmObjMap.put(km.getString("kmdm"), km);
				}

				for (JSONObject xs : wlzcjNjpm) {
					ScoreStuStatisticsRank scoreStuStaRank = new ScoreStuStatisticsRank();
					scoreStuStaRank.setBjpm(xs.getIntValue("bjzfpm"));
					scoreStuStaRank.setNjpm(xs.getIntValue("njzfpm"));
					String dbkslcmc = "";

					String bh = xs.getString("bh");
					String xh = xs.getString("xh");

					JSONObject old = lastXsZfMap.get(xh);
					if (old != null) {
						dbkslcmc = old.getString("kslcmc");
						scoreStuStaRank.setDbkslc(old.getString("kslc"));
						scoreStuStaRank.setDbxnxq(old.getString("xnxq"));
						scoreStuStaRank.setBjpmgz(old.getIntValue("bjpm") - scoreStuStaRank.getBjpm());
						scoreStuStaRank.setNjpmgz(old.getIntValue("njpm") - scoreStuStaRank.getNjpm());
						int bpmgz = scoreStuStaRank.getBjpmgz();
						JSONObject stu = new JSONObject();
						stu.put("xh", xh);
						stu.put("xm", xs.getString("xm"));
						stu.put("pmsj", bpmgz);
						stu.put("bjzfpmgz", bpmgz);
						stu.put("njzfpm", scoreStuStaRank.getNjpmgz());
						if (bpmgz >= 0 && bpmgz >= sarSetBj01) {
							HashMap<String, JSONObject> ycMap = new HashMap<String, JSONObject>();
							if (bhKmYcXsList.containsKey(bh)) {
								ycMap = bhKmYcXsList.get(bh);
								JSONObject o = new JSONObject();
								if (ycMap.containsKey("00000000")) {
									o = bhKmYcXsList.get(xs.getString("bh")).get("00000000");
								}
								JSONArray jb = new JSONArray();
								if (o.containsKey("jb")) {
									jb = o.getJSONArray("jb");
								}
								jb.add(stu);
								o.put("kmdm", "00000000");
								o.put("kmmc", "总分");
							}
						} else if (bpmgz <= 0 && Math.abs(bpmgz) >= sarSetBj02) {
							HashMap<String, JSONObject> ycMap = new HashMap<String, JSONObject>();
							if (bhKmYcXsList.containsKey(bh)) {
								ycMap = bhKmYcXsList.get(bh);
								JSONObject o = new JSONObject();
								if (ycMap.containsKey("00000000")) {
									o = bhKmYcXsList.get(bh).get("00000000");
								}
								JSONArray jb = new JSONArray();
								if (o.containsKey("yc")) {
									jb = o.getJSONArray("yc");
								}
								jb.add(stu);
								o.put("kmdm", "00000000");
								o.put("kmmc", "总分");
							}
						}
					}
					scoreStuStaRank.setXh(xh);
					scoreStuStaRank.setXnxq(xnxq);
					scoreStuStaRank.setKslc(kslc);
					scoreStuStaRank.setXxdm(xxdm);
					scoreStuStaRank.setZf(xs.getFloatValue("zcj"));
					scoreStuStaRank.setDjxl(xs.getString("djxl"));
					scoreStuStaRank.setDjxl2(xs.getString("djxl2"));
					scoreStuStaRank.setDjxl3(xs.getString("djxl3"));
					int bzzcj = 0;
					if (xhbzzfmap.containsKey(xh)) {
						bzzcj = xhbzzfmap.get(xh);
					}
					if (xs.getBooleanValue("sftj")) {
						scoreStuStaRank.setSftj("1");
					} else {
						scoreStuStaRank.setSftj("0");
					}
					scoreStuStaRank.setBzzcj((float) bzzcj);
					scoreStuStatisticsRankList.add(scoreStuStaRank);

					if (!scoreStuStatisticsRankWlfzMap.containsKey(wlfz)) {
						scoreStuStatisticsRankWlfzMap.put(wlfz, new ArrayList<Object>());
					}
					scoreStuStatisticsRankWlfzMap.get(wlfz).add(scoreStuStaRank);

					if (!scoreStuStatisticsRankMap.containsKey(bh)) {
						scoreStuStatisticsRankMap.put(bh, new ArrayList<Object>());
					}
					scoreStuStatisticsRankMap.get(bh).add(scoreStuStaRank);

					// 获取学生报告
					if (sarSettingStu.size() > 0) {

						AnalysisReportStu analysisReportStu = ScoreUtil.getStuReportByParam(xhWlkmcjMap,
								bjDataForXsReport, xs, scoreStuStaRank.getBjpmgz(), scoreStuStaRank.getNjpmgz(),
								dbkslcmc, wlzf, kmList, maxZfScr, avgZfScr, sarSettingStu, kmdmKmObjMap);
						if (analysisReportStu != null) {
							analysisReportStu.setXnxq(xnxq);
							analysisReportStu.setXxdm(xxdm);
							analysisReportStu.setKslc(kslc);
							analysisReportStuList.add(analysisReportStu);
						}
					}

					// 获取App学生报告
					if (sarSettingStu.size() > 0) {
						AppStudentScoreReport appStudentReport = ScoreUtil.getAppStuReportByParam(xhWlkmcjMap,
								bjDataForXsReport, xs, scoreStuStaRank.getBjpmgz(), scoreStuStaRank.getNjpmgz(),
								dbkslcmc, wlzf, kmList, maxZfScr, avgZfScr, sarSettingStu, kmdmKmObjMap, wlzcjNjpm,
								bhClassStaMap, bhNjNameMap);
						if (appStudentReport != null) {
							appStudentReport.setSchoolId(xxdm);
							appStudentReport.setExamId(kslc);
							appStudentReport.setTermInfoId(xnxq);
							appStudentScoreReportList.add(appStudentReport);
						}
					}
				}

				for (Map.Entry<String, List<Object>> entry : scoreStuStatisticsRankMap.entrySet()) {
					List<Object> obj = entry.getValue();
					ScoreUtil.sorStuScoreListTwo(obj, "bzbjpm", "desc", "nj", "zf");
				}
				for (Iterator<String> tkm = scoreStuStatisticsRankWlfzMap.keySet().iterator(); tkm.hasNext();) {
					String key = tkm.next();
					List<Object> obj = scoreStuStatisticsRankWlfzMap.get(key);
					ScoreUtil.sorStuScoreListTwo(obj, "bznjpm", "desc", "nj", "zf");
					// ScoreUtil.sorStuScoreListTwo(obj,"bznjpm",
					// "desc","nj", "zf");
				}
				// 获取文理下的总分成绩分布表格
				JSONObject ZFDRS = ScoreUtil.getZfdRsTable(wlfz, scoreDistributeList, bhFbdmScoreDisMap, scdWlMap);
				String zfdrs = ZFDRS.toJSONString();
				// 循环生成班级报告
				for (JSONObject bj : bjZfDataList) {
					String bh = bj.getString("bh");
					ScoreClassStatistics scoreClassStatistics = bhClassStaMap.get(bh);
					if (scoreClassStatistics != null) {
						AnalysisReportBj analysisReportBj = new AnalysisReportBj();
						analysisReportBj.setXnxq(xnxq);
						analysisReportBj.setXxdm(xxdm);
						analysisReportBj.setKslc(kslc);
						analysisReportBj.setBh(bh);
						analysisReportBj.setZfdrs(zfdrs);

						// 生成总分考试情况
						JSONObject ksqk = new JSONObject();
						JSONArray columns = ScoreUtil.getBJReportDataGridTitle();
						ksqk.put("columns", columns);
						JSONArray rows = new JSONArray();
						ksqk.put("rows", rows);
						JSONObject zfrow = new JSONObject();
						zfrow.put("pjf", ScoreUtil.castFloatTowPointNum((Float) scoreClassStatistics.getPjf()));
						zfrow.put("pjfpm", scoreClassStatistics.getPm());
						zfrow.put("pjffc", ScoreUtil.castFloatTowPointNum((Float) scoreClassStatistics.getPjffc()));
						zfrow.put("yxl", ScoreUtil.castFloatTowPointNum((Float) scoreClassStatistics.getYxl()));
						zfrow.put("yxlpm", scoreClassStatistics.getYxlpm());
						zfrow.put("yxlfc", ScoreUtil.castFloatTowPointNum((Float) scoreClassStatistics.getYxlc()));
						zfrow.put("hgl", ScoreUtil.castFloatTowPointNum((Float) scoreClassStatistics.getHgl()));
						zfrow.put("hglpm", scoreClassStatistics.getHglpm());
						zfrow.put("hglfc", ScoreUtil.castFloatTowPointNum((Float) scoreClassStatistics.getHglc()));
						zfrow.put("jzsrs", scoreClassStatistics.getJzsrs());
						zfrow.put("qnsrs", scoreClassStatistics.getQnsrs());
						JSONObject lastZfSta = null;
						if (bhClaMap.containsKey(bh)) {
							lastZfSta = bhClaMap.get(bh);
							zfrow.put("pjfpmgz", lastZfSta.getIntValue("pm") - scoreClassStatistics.getPm());
							zfrow.put("yxlpmgz", lastZfSta.getIntValue("yxlpm") - scoreClassStatistics.getYxlpm());
							zfrow.put("hglpmgz", lastZfSta.getIntValue("hglpm") - scoreClassStatistics.getHglpm());
							zfrow.put("jzsrsgz", scoreClassStatistics.getJzsrs() - lastZfSta.getIntValue("jzsrs"));
							zfrow.put("qnsrsgz", lastZfSta.getIntValue("qnsrs") - scoreClassStatistics.getQnsrs());
						}
						zfrow.put("item", "总分");

						rows.add(zfrow);
						// 添加科目行
						for (JSONObject km : kmList) {
							String kmdm = km.getString("kmdm");
							JSONObject kmrow = new JSONObject();
							ScoreClassStatisticsMk kmcla = bhKmClassStaMap.get(bh).get(kmdm);
							if (kmcla == null) {
								continue;
							}
							kmrow.put("item", km.get("kmmc"));
							kmrow.put("pjf", ScoreUtil.castFloatTowPointNum((Float) kmcla.getPjf()));
							kmrow.put("pjfpm", kmcla.getPm());
							kmrow.put("pjffc", ScoreUtil.castFloatTowPointNum((Float) kmcla.getPjffc()));
							kmrow.put("yxl", ScoreUtil.castFloatTowPointNum((Float) kmcla.getYxl()));
							kmrow.put("yxlpm", kmcla.getYxlpm());
							kmrow.put("yxlfc", ScoreUtil.castFloatTowPointNum((Float) kmcla.getYxlc()));
							kmrow.put("hgl", ScoreUtil.castFloatTowPointNum((Float) kmcla.getHgl()));
							kmrow.put("hglpm", kmcla.getHglpm());
							kmrow.put("hglfc", ScoreUtil.castFloatTowPointNum((Float) kmcla.getHglc()));
							kmrow.put("jzsrs", kmcla.getJzsrs());
							kmrow.put("qnsrs", kmcla.getQnsrs());
							JSONObject lastKmSta = null;
							if (bhClaKmMap.containsKey(bh + "_" + kmdm)) {
								lastKmSta = bhClaKmMap.get(bh + "_" + kmdm);
								kmrow.put("pjfpmgz", lastKmSta.getIntValue("pm") - kmcla.getPm());
								kmrow.put("yxlpmgz", lastKmSta.getIntValue("yxlpm") - kmcla.getYxlpm());
								kmrow.put("hglpmgz", lastKmSta.getIntValue("hglpm") - kmcla.getHglpm());
								kmrow.put("jzsrsgz", kmcla.getJzsrs() - lastKmSta.getIntValue("jzsrs"));
								kmrow.put("qnsrsgz", lastKmSta.getIntValue("qnsrs") - kmcla.getQnsrs());
							}
							rows.add(kmrow);
						}
						//
						String zmc = bj.getString("bjfzmc");
						int zrs = bjfzCkNumMap.get(bj.getString("bjfz"));
						String lastLc = "";
						if (bhClaMap.containsKey(bh)) {
							lastLc = "（" + bhClaMap.get(bh).getString("kslcmc") + "）";
						}
						String sm = "参加本次考试的班级人数：" + scoreClassStatistics.getCkrs() + "人，" + zmc + "组：" + zrs
								+ "人；各科目排名是指在全年级中的位置，排名后的进步名次（↑）、" + "退步名次（↓）是与上次考试" + lastLc
								+ "的对比，最高分（率）差是指本班各项目分析结果与全年级的对比差值。以下的分析均以总分" + "作为参考依据。“班级人数”指班级总分参考人数，”" + zmc
								+ "组”指班级分组，“" + zmc + "组人数”指班级分组总分参考人数。";
						ksqk.put("sm", sm);
						analysisReportBj.setKsqk(ksqk.toJSONString());
						JSONArray xsycqk = new JSONArray();
						HashMap<String, JSONObject> li = bhKmYcXsList.get(bh);
						if (li != null) {
							for (Iterator<String> itt = li.keySet().iterator(); itt.hasNext();) {
								String km = itt.next();
								xsycqk.add(li.get(km));
							}
						}
						float nBzf = (float) scoreClassStatistics.getBzf();
						List<JSONObject> bjcjgzList = GrepUtil.grepJsonKeyBySingleVal("bh", bh, cjgzList);
						List<JSONObject> bjcjgzListkm = GrepUtil.grepJsonKeyBySingleVal("bh", bh, kmcjgzList);
						JSONObject cjgzsj = ScoreUtil.getBjBzfGzList(nBzf, kslc, kslcmc, bjcjgzList);

						analysisReportBj.setXsycqk(xsycqk.toJSONString());
						analysisReportBj.setCjgzsj(cjgzsj.toJSONString());
						// wlfzRankDisList bhPmszKmRsMap 获取排名短分布表格
						JSONObject MCDRS = ScoreUtil.getBjMcdRsTable(bh, wlfzRankDisList, bhPmszKmRsMap, kmList);
						analysisReportBj.setMcdrs(MCDRS.toJSONString());
						JSONObject reportdata = ScoreUtil.getAppClassReportData(analysisReportBj, rows, zmc, zrs,
								scoreClassStatistics.getCkrs(), lastLc, kmList, bjcjgzListkm, xsycqk, bhKmClassStaMap,
								bh, kslc, kslcmc);
						analysisReportBj.setReportdata(reportdata.toJSONString());
						analysisReportBjList.add(analysisReportBj);

					}
				}
				// 结束班级报告
				// 结束全员分析部分
				// 开始成绩报告部分

				// 结束成绩报告部分

				// 开始范围统计部分
				// 参与人数与统计人数
				zfcyrs = wlzcjNjpm.size();
				zftjList = GrepUtil.grepJsonKeyBySingleVal("fwtj", "true", wlzcjNjpm);
				zftjrs = zftjList.size();
				if (zftjrs == 0) {
					continue;
				}
				// 最高分与最低分
				maxZfScr = zftjList.get(0).getFloatValue("zcj");
				minZfScr = zftjList.get(zftjrs - 1).getFloatValue("zcj");
				// avgZfScr = ScoreUtil.getPjf(zftjList, "zcj");
				avgZfScr = 0;
				// 优秀
				zfYxPerObj = ScoreUtil.getDjPercent(zftjList, "Yx");
				zfYxPer = zfYxPerObj.getFloatValue("bl");
				zfYxRs = zfYxPerObj.getIntValue("rs");
				// 合格
				zfHgPerObj = ScoreUtil.getDjPercent(zftjList, "Hg");
				zfHgPer = zfHgPerObj.getFloatValue("bl");
				zfHgRs = zfHgPerObj.getIntValue("rs");
				// 低分
				zfDfPerObj = ScoreUtil.getDjPercent(zftjList, "Df");
				zfDfPer = zfDfPerObj.getFloatValue("bl");
				zfDfRs = zfDfPerObj.getIntValue("rs");
				// 尖子
				zfJzPerObj = ScoreUtil.getDjPercent(zftjList, "Jz");
				zfJzPer = zfJzPerObj.getFloatValue("bl");
				zfJzRs = zfJzPerObj.getIntValue("rs");
				zfJzList = (List<JSONObject>) zfJzPerObj.get("list");

				// 潜能
				zfQnPerObj = ScoreUtil.getDjPercent(zftjList, "Qn");
				zfQnPer = zfQnPerObj.getFloatValue("bl");
				zfQnRs = zfQnPerObj.getIntValue("rs");
				zfQnList = (List<JSONObject>) zfQnPerObj.get("list");

				Map<String, ScoreGroupStatisticsMkRange> kmTsgssMap = new HashMap<String, ScoreGroupStatisticsMkRange>();
				// 单科循环上面的方法
				for (JSONObject km : kmList) {
					String kmdm = km.getString("kmdm");
					ScoreGroupStatisticsMkRange scoreGroupStatisticsMkRange = new ScoreGroupStatisticsMkRange();
					kmTsgssMap.put(kmdm, scoreGroupStatisticsMkRange);
					List<JSONObject> kmcjList = GrepUtil.grepJsonKeyBySingleVal("kmdm", kmdm, wlkmcjNjpm);
					// 参与人数与统计人数
					int kmcyrs = kmcjList.size();
					List<JSONObject> kmtjList = GrepUtil.grepJsonKeyBySingleVal("fwtj", "true", kmcjList);
					if (kmtjList.size() == 0) {
						continue;
					}

					int kmtjrs = kmtjList.size();
					// 单科最高分与最低分
					float maxKmScr = kmtjList.get(0).getFloatValue("cj");
					float minKmScr = kmtjList.get(kmtjrs - 1).getFloatValue("cj");
					float avgKmScr = ScoreUtil.getPjf(kmtjList, "cj");
					if (km.getInteger("isHb") == 0) {

						avgZfScr += avgKmScr;
					}

					JSONObject kmYxPerObj = ScoreUtil.getDjPercent(kmtjList, "Yx");
					float kmYxPer = kmYxPerObj.getFloat("bl");
					int kmYxRs = kmYxPerObj.getIntValue("rs");

					JSONObject kmHgPerObj = ScoreUtil.getDjPercent(kmtjList, "Hg");
					float kmHgPer = kmHgPerObj.getFloat("bl");
					int kmHgRs = kmHgPerObj.getIntValue("rs");

					JSONObject kmDfPerObj = ScoreUtil.getDjPercent(kmtjList, "Df");
					float kmDfPer = kmDfPerObj.getFloat("bl");
					int kmDfRs = kmDfPerObj.getIntValue("rs");

					JSONObject kmQnPerObj = ScoreUtil.getDjPercent(kmtjList, "Qn");
					float kmQnPer = kmQnPerObj.getFloat("bl");
					int kmQnRs = kmQnPerObj.getIntValue("rs");
					List<JSONObject> kmQnList = (List<JSONObject>) kmQnPerObj.get("list");

					JSONObject kmJzPerObj = ScoreUtil.getDjPercent(kmtjList, "Jz");
					float kmJzPer = kmJzPerObj.getFloat("bl");
					int kmJzRs = kmJzPerObj.getIntValue("rs");
					List<JSONObject> kmJzList = (List<JSONObject>) kmJzPerObj.get("list");

					float kmDiffiPer = ScoreUtil.dificultPoint(avgKmScr, km.getIntValue("mf"));
					float kmDiffRate = ScoreUtil.diffrentRate(kmtjList, "cj", km.getIntValue("mf"));
					scoreGroupStatisticsMkRange.setCkrs(kmcyrs);
					scoreGroupStatisticsMkRange.setDfl(kmDfPer);
					scoreGroupStatisticsMkRange.setDfrs(kmDfRs);
					scoreGroupStatisticsMkRange.setFzdm(wlfz);
					scoreGroupStatisticsMkRange.setHgl(kmHgPer);
					scoreGroupStatisticsMkRange.setHgrs(kmHgRs);
					scoreGroupStatisticsMkRange.setJzsrs(kmJzRs);
					scoreGroupStatisticsMkRange.setKmdm(kmdm);
					scoreGroupStatisticsMkRange.setKslc(kslc);
					scoreGroupStatisticsMkRange.setLdxs(kmDiffiPer);
					scoreGroupStatisticsMkRange.setPjf(avgKmScr);
					scoreGroupStatisticsMkRange.setQfd(kmDiffRate);
					scoreGroupStatisticsMkRange.setQnsrs(kmQnRs);
					scoreGroupStatisticsMkRange.setTjrs(kmtjrs);
					scoreGroupStatisticsMkRange.setXnxq(xnxq);
					scoreGroupStatisticsMkRange.setXxdm(xxdm);
					scoreGroupStatisticsMkRange.setYxl(kmYxPer);
					scoreGroupStatisticsMkRange.setYxrs(kmYxRs);
				}

				zfDiffiPer = ScoreUtil.dificultPoint(avgZfScr, wlzf);
				zfDiffRate = ScoreUtil.diffrentRate(zftjList, "zcj", wlzf);
				// 开始按班级分组进行循环
				bjfzList = GrepUtil.grepJsonKeyBySingleVal("wlfz", wlfz, bjwlfz);

				List<JSONObject> bjkmDataList = new ArrayList<JSONObject>();
				for (JSONObject bjfzObj : bjfzList) {

					String bjfz = bjfzObj.getString("bjfz");
					// 参与人数与统计人数
					List<JSONObject> bjfzcjList = GrepUtil.grepJsonKeyBySingleVal("bjfz", bjfz, wlzcjNjpm);
					int bjfzzfcyrs = bjfzcjList.size();
					List<JSONObject> bjfztjcjList = GrepUtil.grepJsonKeyBySingleVal("fwtj", "true", bjfzcjList);
					int bjfzzftjrs = bjfztjcjList.size();
					if (bjfzzftjrs == 0) {
						continue;
					}
					// 最高分与最低分
					float bjfzmaxZfScr = bjfztjcjList.get(0).getFloatValue("zcj");
					float bjfzminZfScr = bjfztjcjList.get(bjfzzftjrs - 1).getFloatValue("zcj");
					// float bjfzavgZfScr = ScoreUtil.getPjf(bjfztjcjList,
					// "zcj");

					float bjfzavgZfScr = 0;
					// 优秀
					JSONObject bjfzYxPerObj = ScoreUtil.getDjPercent(bjfztjcjList, "Yx");
					float bjfzYxPer = bjfzYxPerObj.getFloatValue("bl");
					int bjfzYxRs = bjfzYxPerObj.getIntValue("rs");
					// 合格
					JSONObject bjfzHgPerObj = ScoreUtil.getDjPercent(bjfztjcjList, "Hg");
					float bjfzHgPer = bjfzHgPerObj.getFloatValue("bl");
					int bjfzHgRs = bjfzHgPerObj.getIntValue("rs");
					// 低分
					JSONObject bjfzDfPerObj = ScoreUtil.getDjPercent(bjfztjcjList, "Df");
					float bjfzDfPer = bjfzDfPerObj.getFloatValue("bl");
					int bjfzDfRs = bjfzDfPerObj.getIntValue("rs");
					// 尖子
					JSONObject bjfzJzPerObj = ScoreUtil.getDjPercent(bjfztjcjList, "Jz");
					float bjfzJzPer = bjfzJzPerObj.getFloatValue("bl");
					int bjfzJzRs = bjfzJzPerObj.getIntValue("rs");
					// 潜能
					JSONObject bjfzQnPerObj = ScoreUtil.getDjPercent(bjfztjcjList, "Qn");
					float bjfzQnPer = bjfzQnPerObj.getFloatValue("bl");
					int bjfzQnRs = bjfzQnPerObj.getIntValue("rs");
					Map<String, ScoreGroupStatisticsMkRange> kmBjTsgssMap = new HashMap<String, ScoreGroupStatisticsMkRange>();
					// 单科循环上面的方法
					for (JSONObject km : kmList) {
						String kmdm = km.getString("kmdm");
						ScoreGroupStatisticsMkRange scoreGroupStatisticsMkRange = new ScoreGroupStatisticsMkRange();

						List<JSONObject> kmcjList = new ArrayList<JSONObject>();
						if (km.getInteger("isHb") == 0) {

							kmcjList = GrepUtil.grepJsonKeyByVal(new String[] { "kmdm", "bjfz" },
									new String[] { kmdm, bjfz }, wlkmcjNjpm);
						} else {
							kmcjList = GrepUtil.grepJsonKeyBySingleVal("kmdm", kmdm, wlkmcjNjpm);

						}
						// 参与人数与统计人数
						int kmcyrs = kmcjList.size();
						List<JSONObject> kmtjList = GrepUtil.grepJsonKeyBySingleVal("fwtj", "true", kmcjList);
						int kmtjrs = kmtjList.size();
						if (kmtjList.size() == 0) {
							continue;
						}
						kmBjTsgssMap.put(kmdm, scoreGroupStatisticsMkRange);
						// 单科最高分与最低分
						float maxKmScr = kmtjList.get(0).getFloatValue("cj");
						float minKmScr = kmtjList.get(kmtjrs - 1).getFloatValue("cj");
						float avgKmScr = ScoreUtil.getPjf(kmtjList, "cj");
						if (km.getInteger("isHb") == 0) {

							bjfzavgZfScr += avgKmScr;
						}

						JSONObject kmYxPerObj = ScoreUtil.getDjPercent(kmtjList, "Yx");
						float kmYxPer = kmYxPerObj.getFloat("bl");
						int kmYxRs = kmYxPerObj.getIntValue("rs");

						JSONObject kmHgPerObj = ScoreUtil.getDjPercent(kmtjList, "Hg");
						float kmHgPer = kmHgPerObj.getFloat("bl");
						int kmHgRs = kmHgPerObj.getIntValue("rs");

						JSONObject kmDfPerObj = ScoreUtil.getDjPercent(kmtjList, "Df");
						float kmDfPer = kmDfPerObj.getFloat("bl");
						int kmDfRs = kmDfPerObj.getIntValue("rs");

						JSONObject kmQnPerObj = ScoreUtil.getDjPercent(kmtjList, "Qn");
						float kmQnPer = kmQnPerObj.getFloat("bl");
						int kmQnRs = kmQnPerObj.getIntValue("rs");

						JSONObject kmJzPerObj = ScoreUtil.getDjPercent(kmtjList, "Jz");
						float kmJzPer = kmJzPerObj.getFloat("bl");
						int kmJzRs = kmJzPerObj.getIntValue("rs");

						float kmDiffiPer = ScoreUtil.dificultPoint(avgKmScr, km.getIntValue("mf"));
						float kmDiffRate = ScoreUtil.diffrentRate(kmtjList, "cj", km.getIntValue("mf"));

						scoreGroupStatisticsMkRange.setCkrs(kmcyrs);
						scoreGroupStatisticsMkRange.setDfl(kmDfPer);
						scoreGroupStatisticsMkRange.setDfrs(kmDfRs);
						scoreGroupStatisticsMkRange.setFzdm(bjfz);
						scoreGroupStatisticsMkRange.setHgl(kmHgPer);
						scoreGroupStatisticsMkRange.setHgrs(kmHgRs);
						scoreGroupStatisticsMkRange.setJzsrs(kmJzRs);
						scoreGroupStatisticsMkRange.setKmdm(kmdm);
						scoreGroupStatisticsMkRange.setKslc(kslc);
						scoreGroupStatisticsMkRange.setLdxs(kmDiffiPer);
						scoreGroupStatisticsMkRange.setPjf(avgKmScr);
						scoreGroupStatisticsMkRange.setQfd(kmDiffRate);
						scoreGroupStatisticsMkRange.setQnsrs(kmQnRs);
						scoreGroupStatisticsMkRange.setTjrs(kmtjrs);
						scoreGroupStatisticsMkRange.setXnxq(xnxq);
						scoreGroupStatisticsMkRange.setXxdm(xxdm);
						scoreGroupStatisticsMkRange.setYxl(kmYxPer);
						scoreGroupStatisticsMkRange.setYxrs(kmYxRs);
						kmBjTsgssMap.put(kmdm, scoreGroupStatisticsMkRange);
					}
					float bjfzzfDiffiPer = ScoreUtil.dificultPoint(bjfzavgZfScr, wlzf);
					float bjfzzfDiffRate = ScoreUtil.diffrentRate(bjfztjcjList, "zcj", wlzf);

					// 开始按班级循环
					List<JSONObject> bjObjList = GrepUtil.grepJsonKeyBySingleVal("bjfz", bjfz, bjInwlfz);

					for (JSONObject bjObj : bjObjList) {
						String bh = bjObj.getString("bh");
						JSONObject bjdata = new JSONObject();
						List<JSONObject> bjcjList = GrepUtil.grepJsonKeyBySingleVal("bh", bh, bjfzcjList);
						int bjzfcyrs = bjcjList.size();
						List<JSONObject> bjtjcjList = GrepUtil.grepJsonKeyBySingleVal("fwtj", "true", bjcjList);
						int bjzftjrs = bjtjcjList.size();
						bjObj.put("ckrs", bjzfcyrs);
						bjObj.put("fwtjrs", bjzftjrs);
						if (bjtjcjList.size() == 0) {
							continue;
						}
						// 最高分与最低分
						float bjmaxZfScr = bjtjcjList.get(0).getFloatValue("zcj");
						bjObj.put("bjmaxZfScr", bjmaxZfScr);
						float bjminZfScr = bjtjcjList.get(bjzftjrs - 1).getFloatValue("zcj");
						bjObj.put("bjminZfScr", bjminZfScr);
						// float bjavgZfScr = ScoreUtil.getPjf(bjtjcjList,
						// "zcj");

						float bjavgZfScr = 0;

						JSONObject bjzfYxObj = ScoreUtil.getDjPercent(bjtjcjList, "Yx");
						float bjzfYxPer = bjzfYxObj.getFloatValue("bl");
						int bjzfYxRs = bjzfYxObj.getIntValue("rs");
						bjObj.put("bjzfYxPer", bjzfYxPer);
						bjObj.put("bjzfYxRs", bjzfYxRs);

						JSONObject bjzfDfObj = ScoreUtil.getDjPercent(bjtjcjList, "Df");
						float bjzfDfPer = bjzfDfObj.getFloatValue("bl");
						int bjzfDfRs = bjzfDfObj.getIntValue("rs");
						bjObj.put("bjzfDfPer", bjzfDfPer);
						bjObj.put("bjzfDfRs", bjzfDfRs);

						JSONObject bjzfHgObj = ScoreUtil.getDjPercent(bjtjcjList, "Hg");
						float bjzfHgPer = bjzfHgObj.getFloatValue("bl");
						int bjzfHgRs = bjzfHgObj.getIntValue("rs");
						bjObj.put("bjzfHgPer", bjzfHgPer);
						bjObj.put("bjzfHgRs", bjzfHgRs);

						JSONObject bjzfJzObj = ScoreUtil.getDjPercent(bjtjcjList, "Jz");
						float bjzfJzPer = bjzfJzObj.getFloatValue("bl");
						int bjzfJzRs = bjzfJzObj.getIntValue("rs");
						bjObj.put("bjzfJzPer", bjzfJzPer);
						bjObj.put("bjzfJzRs", bjzfJzRs);

						JSONObject bjzfQnObj = ScoreUtil.getDjPercent(bjtjcjList, "Qn");
						float bjzfQnPer = bjzfQnObj.getFloatValue("bl");
						int bjzfQnRs = bjzfQnObj.getIntValue("rs");
						bjObj.put("bjzfQnPer", bjzfQnPer);
						bjObj.put("bjzfQnRs", bjzfQnRs);

						List<JSONObject> bjkmData = new ArrayList<JSONObject>();

						// 班级-单科循环上面的方法
						for (JSONObject km : kmList) {
							JSONObject bjkmobj = new JSONObject();
							// bjKmDataList
							JSONObject bjkmObj = new JSONObject();
							bjkmData.add(bjkmObj);
							// bjKmDataList.add(bjkmObj);
							bjkmObj.put("wlfz", wlfz);
							bjkmObj.put("bjfz", bjfz);
							bjkmObj.put("bh", bh);

							String kmdm = km.getString("kmdm");
							bjkmObj.put("kmdm", kmdm);
							List<JSONObject> kmcjList = GrepUtil.grepJsonKeyByVal(new String[] { "kmdm", "bh" },
									new String[] { kmdm, bh }, wlkmcjNjpm);
							// 参与人数与统计人数
							int kmcyrs = kmcjList.size();
							bjkmObj.put("ckrs", kmcyrs);
							List<JSONObject> kmtjList = GrepUtil.grepJsonKeyBySingleVal("fwtj", "true", kmcjList);
							int kmtjrs = kmtjList.size();
							bjkmObj.put("tjrs", kmtjrs);
							bjkmObj.put("fwtjrs", kmtjrs);
							if (kmtjList.size() == 0) {
								continue;
							}
							// 单科最高分与最低分
							float maxKmScr = kmtjList.get(0).getFloatValue("cj");
							bjkmObj.put("maxKmScr", maxKmScr);
							float minKmScr = kmtjList.get(kmtjrs - 1).getFloatValue("cj");
							bjkmObj.put("minKmScr", minKmScr);
							float avgKmScr = ScoreUtil.getPjf(kmtjList, "cj");
							bjkmObj.put("avgKmScr", avgKmScr);
							if (km.getInteger("isHb") == 0) {

								bjavgZfScr += avgKmScr;
							}
							JSONObject kmYxPerObj = ScoreUtil.getDjPercent(kmtjList, "Yx");
							float kmYxPer = kmYxPerObj.getFloat("bl");
							int kmYxRs = kmYxPerObj.getIntValue("rs");

							JSONObject kmHgPerObj = ScoreUtil.getDjPercent(kmtjList, "Hg");
							float kmHgPer = kmHgPerObj.getFloat("bl");
							int kmHgRs = kmHgPerObj.getIntValue("rs");

							JSONObject kmDfPerObj = ScoreUtil.getDjPercent(kmtjList, "Df");
							float kmDfPer = kmDfPerObj.getFloat("bl");
							int kmDfRs = kmDfPerObj.getIntValue("rs");

							JSONObject kmQnPerObj = ScoreUtil.getDjPercent(kmtjList, "Qn");
							float kmQnPer = kmQnPerObj.getFloat("bl");
							int kmQnRs = kmQnPerObj.getIntValue("rs");

							JSONObject kmJzPerObj = ScoreUtil.getDjPercent(kmtjList, "Jz");
							float kmJzPer = kmJzPerObj.getFloat("bl");
							int kmJzRs = kmJzPerObj.getIntValue("rs");

							// float kmYxPer =
							// ScoreUtil.getDjPercent(kmtjList, "Yx");
							bjkmObj.put("kmYxPer", kmYxPer);
							bjkmObj.put("kmHgPer", kmHgPer);
							bjkmObj.put("kmDfPer", kmDfPer);
							bjkmObj.put("kmJzPer", kmJzPer);
							bjkmObj.put("kmQnPer", kmQnPer);
							bjkmObj.put("kmYxRs", kmYxRs);
							bjkmObj.put("kmHgRs", kmHgRs);
							bjkmObj.put("kmDfRs", kmDfRs);
							bjkmObj.put("kmJzRs", kmJzRs);
							bjkmObj.put("kmQnRs", kmQnRs);

							float kmDiffiPer = ScoreUtil.dificultPoint(avgKmScr, km.getIntValue("mf"));
							bjkmObj.put("kmDiffiPer", kmDiffiPer);
							float kmDiffRate = ScoreUtil.diffrentRate(kmtjList, "cj", km.getIntValue("mf"));
							bjkmObj.put("kmDiffRate", kmDiffRate);
							bjkmobj.put(kmdm, avgKmScr);
							List<Float> kmleft = new ArrayList<Float>();
							for (JSONObject xs : kmtjList) {
								kmleft.add(xs.getFloat("cj"));
							}
							float bjkmbzc = ScoreUtil.getDtSq(kmleft, avgKmScr);
							bjkmObj.put("kmbzc", bjkmbzc);
						}

						bjkmDataList.addAll(bjkmData);

						bjObj.put("bjavgZfScr", bjavgZfScr);
						float bjzfDiffiPer = ScoreUtil.dificultPoint(bjavgZfScr, wlzf);
						bjObj.put("bjzfDiffiPer", bjzfDiffiPer);
						float bjzfDiffRate = ScoreUtil.diffrentRate(bjtjcjList, "zcj", wlzf);
						bjObj.put("bjzfDiffRate", bjzfDiffRate);
						List<Float> left = new ArrayList<Float>();
						for (JSONObject xs : bjtjcjList) {
							left.add(xs.getFloat("zcj"));
						}
						bjObj.put("bjzfpjf", bjavgZfScr);
						bjdata.put("bjzfpjf", bjavgZfScr);
						bhDataMap.put(bh, bjdata);
						// 标准差
						float bjzfbzc = ScoreUtil.getDtSq(left, bjavgZfScr);
						bjObj.put("bjzfbzc", bjzfbzc);

					}
					// 结束循环班级
					// 计算班级的平均分排名与分差、优秀、合格、低分
					bjZfDataList1 = GrepUtil.grepJsonKeyBySingleVal("bjfz", bjfz, bjObjList);
					float maxAvg = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjavgZfScr", "desc", "bj", "pjf").get(0)
							.getFloatValue("bjavgZfScr");
					float maxYxl = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfYxPer", "desc", "bj", "yxl").get(0)
							.getFloatValue("bjzfYxPer");
					float maxHgl = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfHgPer", "desc", "bj", "hgl").get(0)
							.getFloatValue("bjzfHgPer");
					float minDfl = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfDfPer", "asc", "bj", "dfl").get(0)
							.getFloatValue("bjzfDfPer");
					for (JSONObject bjObj : bjZfDataList1) {
						float pjfc = maxAvg - bjObj.getFloatValue("bjavgZfScr");
						bjObj.put("bjpjfc", pjfc);
						float yxlc = maxYxl - bjObj.getFloatValue("bjzfYxPer");
						bjObj.put("bjyxlc", yxlc);
						float hglc = maxHgl - bjObj.getFloatValue("bjzfHgPer");
						bjObj.put("bjhglc", hglc);
						float dflc = bjObj.getFloatValue("bjzfDfPer") - minDfl;
						bjObj.put("bjdflc", dflc);
					}
					for (JSONObject km : kmList) {
						String kmdm = km.getString("kmdm");
						List<JSONObject> bjKmDataList1 = GrepUtil.grepJsonKeyByVal(new String[] { "bjfz", "kmdm" },
								new String[] { bjfz, kmdm }, bjkmDataList);
						float kmmaxAvg = ScoreUtil.sorStuScoreList(bjKmDataList1, "avgKmScr", "desc", "km", "pjf")
								.get(0).getFloatValue("avgKmScr");
						float kmmaxYxl = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmYxPer", "desc", "km", "yxl").get(0)
								.getFloatValue("kmYxPer");
						float kmmaxHgl = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmHgPer", "desc", "km", "hgl").get(0)
								.getFloatValue("kmHgPer");
						float kmminDfl = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmDfPer", "asc", "km", "dfl").get(0)
								.getFloatValue("kmDfPer");
						for (JSONObject bjObj : bjKmDataList1) {
							float pjfc = kmmaxAvg - bjObj.getFloatValue("avgKmScr");
							bjObj.put("kmpjfc", pjfc);
							float yxlc = kmmaxYxl - bjObj.getFloatValue("kmYxPer");
							bjObj.put("kmyxlc", yxlc);
							float hglc = kmmaxHgl - bjObj.getFloatValue("kmHgPer");
							bjObj.put("kmhglc", hglc);
							float dflc = bjObj.getFloatValue("kmDfPer") - kmminDfl;
							bjObj.put("kmdflc", dflc);
						}
					}
					// 计算分组的标准差等
					float bjfzZfPjf = ScoreUtil.getPjf(bjZfDataList1, "bjavgZfScr");
					float bjfzZfbzc = ScoreUtil.getFzDtSq(bjZfDataList1, "bjavgZfScr", bjfzZfPjf);
					float bjfzZfpjfcz = ScoreUtil.getFzCz(bjZfDataList1, "bjavgZfScr"); // 班级分组总分平均分差值
					float bjfzZfdfcz = ScoreUtil.getFzCz(bjZfDataList1, "bjzfDfPer"); // 班级分组总分低分率差值
					float bjfzZfhgcz = ScoreUtil.getFzCz(bjZfDataList1, "bjzfHgPer"); // 班级分组总分合格率差值
					float bjfzZfyxcz = ScoreUtil.getFzCz(bjZfDataList1, "bjzfYxPer"); // 班级分组总分优秀率差值

					ScoreGroupStatisticsRange scoreGroupStatisticsRange = new ScoreGroupStatisticsRange();
					scoreGroupStatisticsRange.setBzc(bjfzZfbzc);
					scoreGroupStatisticsRange.setCkrs(bjfzzfcyrs);
					scoreGroupStatisticsRange.setDfl(ScoreUtil.castFloatTowPointNum(bjfzDfPer));
					scoreGroupStatisticsRange.setDflc(ScoreUtil.castFloatTowPointNum(bjfzZfdfcz));
					scoreGroupStatisticsRange.setDfrs(bjfzDfRs);
					scoreGroupStatisticsRange.setFzdm(bjfz);
					scoreGroupStatisticsRange.setHgl(ScoreUtil.castFloatTowPointNum(bjfzHgPer));
					scoreGroupStatisticsRange.setHglc(ScoreUtil.castFloatTowPointNum(bjfzZfhgcz));
					scoreGroupStatisticsRange.setHgrs(bjfzHgRs);
					scoreGroupStatisticsRange.setJzsrs(bjfzJzRs);
					scoreGroupStatisticsRange.setKslc(kslc);
					scoreGroupStatisticsRange.setLdxs(ScoreUtil.castFloatTowPointNum(bjfzzfDiffiPer));
					scoreGroupStatisticsRange.setPjf(bjfzavgZfScr);
					scoreGroupStatisticsRange.setPjffc(ScoreUtil.castFloatTowPointNum(bjfzZfpjfcz));
					scoreGroupStatisticsRange.setQfd(bjfzzfDiffRate);
					scoreGroupStatisticsRange.setQnsrs(bjfzQnRs);
					scoreGroupStatisticsRange.setTjrs(bjfzzftjrs);
					scoreGroupStatisticsRange.setXnxq(xnxq);
					scoreGroupStatisticsRange.setXxdm(xxdm);
					scoreGroupStatisticsRange.setYxl(bjfzYxPer);
					scoreGroupStatisticsRange.setYxlc(bjfzZfyxcz);
					scoreGroupStatisticsRange.setYxrs(bjfzYxRs);

					scoreGroupStatisticsRangeList.add(scoreGroupStatisticsRange);

					for (JSONObject km : kmList) {
						String kmdm = km.getString("kmdm");

						List<JSONObject> bjKmDataList1 = GrepUtil.grepJsonKeyByVal(new String[] { "bjfz", "kmdm" },
								new String[] { bjfz, kmdm }, bjkmDataList);

						if (bjKmDataList1.size() == 0) {
							continue;
						}

						ScoreGroupStatisticsMkRange scoreGroupStatisticsMkRange = kmBjTsgssMap.get(kmdm);
						if (scoreGroupStatisticsMkRange == null) {
							continue;
						}
						float bjfzKmPjf = (float) scoreGroupStatisticsMkRange.getPjf();
						float bjfzKmbzc = ScoreUtil.getFzDtSq(bjKmDataList1, "avgKmScr", bjfzKmPjf);
						float bjfzKmpjfcz = ScoreUtil.getFzCz(bjKmDataList1, "avgKmScr"); // 班级分组科目平均分差值
						float bjfzKmdfcz = ScoreUtil.getFzCz(bjKmDataList1, "kmDfPer"); // 班级分组科目低分率差值
						float bjfzKmhgcz = ScoreUtil.getFzCz(bjKmDataList1, "kmHgPer"); // 班级分组科目合格率差值
						float bjfzKmyxcz = ScoreUtil.getFzCz(bjKmDataList1, "kmYxPer"); // 班级分组科目优秀率差值

						if (scoreGroupStatisticsMkRange.getKslc() != null) {

							// scoreGroupStatisticsMkRange.setPjf(bjfzKmPjf);
							scoreGroupStatisticsMkRange.setBzc(ScoreUtil.castFloatTowPointNum(bjfzKmbzc));
							scoreGroupStatisticsMkRange.setYxlc(ScoreUtil.castFloatTowPointNum(bjfzKmyxcz));
							scoreGroupStatisticsMkRange.setHglc(ScoreUtil.castFloatTowPointNum(bjfzKmhgcz));
							scoreGroupStatisticsMkRange.setDflc(ScoreUtil.castFloatTowPointNum(bjfzKmdfcz));
							scoreGroupStatisticsMkRange.setPjffc(ScoreUtil.castFloatTowPointNum(bjfzKmpjfcz));

							scoreGroupStatisticsMkRangeList.add(scoreGroupStatisticsMkRange);
						}
					}
				}
				// 结束循环班级分组

				// 计算文理分组的标准差等
				// 计算分组的标准差等
				bjZfDataList1 = GrepUtil.grepJsonKeyBySingleVal("wlfz", wlfz, bjZfDataList);
				;
				wlfzZfPjf = ScoreUtil.getPjf(bjZfDataList1, "bjavgZfScr");
				wlfzZfbzc = ScoreUtil.getFzDtSq(bjZfDataList1, "bjavgZfScr", wlfzZfPjf);
				// 优秀 合格 低分 排名与率差
				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfYxPer", "desc", "wlfz", "yxl");
				wlfzZfyxlc = bjZfDataList2.get(0).getFloatValue("bjzfYxPer")
						- bjZfDataList2.get(bjZfDataList2.size() - 1).getFloatValue("bjzfYxPer");
				float fzMaxYxl = bjZfDataList2.get(0).getFloatValue("bjzfYxPer");
				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfHgPer", "desc", "wlfz", "hgl");
				wlfzZfhglc = bjZfDataList2.get(0).getFloatValue("bjzfHgPer")
						- bjZfDataList2.get(bjZfDataList2.size() - 1).getFloatValue("bjzfHgPer");
				float fzMaxHgl = bjZfDataList2.get(0).getFloatValue("bjzfHgPer");
				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfDfPer", "asc", "wlfz", "dfl");
				wlfzZfdflc = bjZfDataList2.get(bjZfDataList2.size() - 1).getFloatValue("bjzfDfPer")
						- bjZfDataList2.get(0).getFloatValue("bjzfDfPer");
				float fzMinDfl = bjZfDataList2.get(0).getFloatValue("bjzfDfPer");
				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfDiffiPer", "desc", "wlfz", "ld");
				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfbzc", "desc", "wlfz", "bzc");
				bjZfDataList2 = ScoreUtil.sorStuScoreList(bjZfDataList1, "bjzfDiffRate", "desc", "wlfz", "qfd");
				// 文理分组平均分分差
				wlfzZfpjfcz = ScoreUtil.getFzCz(bjZfDataList1, "bjavgZfScr");

				ScoreGroupStatisticsRange scoreGroupStatisticsRange = new ScoreGroupStatisticsRange();
				scoreGroupStatisticsRange.setBzc(ScoreUtil.castFloatTowPointNum(wlfzZfbzc));
				scoreGroupStatisticsRange.setCkrs(zfcyrs);
				scoreGroupStatisticsRange.setDfl(zfDfPer);
				scoreGroupStatisticsRange.setDflc(ScoreUtil.castFloatTowPointNum(wlfzZfdflc));
				scoreGroupStatisticsRange.setDfrs(zfDfRs);
				// 难度系数
				scoreGroupStatisticsRange.setLdxs(ScoreUtil.castFloatTowPointNum(zfDiffiPer));
				scoreGroupStatisticsRange.setFzdm(wlfz);
				scoreGroupStatisticsRange.setHgl(zfHgPer);
				scoreGroupStatisticsRange.setHglc(ScoreUtil.castFloatTowPointNum(wlfzZfhglc));
				scoreGroupStatisticsRange.setHgrs(zfHgRs);
				scoreGroupStatisticsRange.setJzsrs(zfJzRs);
				scoreGroupStatisticsRange.setKslc(kslc);
				scoreGroupStatisticsRange.setPjf(avgZfScr);
				scoreGroupStatisticsRange.setPjffc(ScoreUtil.castFloatTowPointNum(wlfzZfpjfcz));
				scoreGroupStatisticsRange.setQfd(ScoreUtil.castFloatTowPointNum(zfDiffRate));
				scoreGroupStatisticsRange.setQnsrs(zfQnRs);
				scoreGroupStatisticsRange.setTjrs(zftjrs);
				scoreGroupStatisticsRange.setXnxq(xnxq);
				scoreGroupStatisticsRange.setXxdm(xxdm);
				scoreGroupStatisticsRange.setYxl(zfYxPer);
				scoreGroupStatisticsRange.setYxlc(ScoreUtil.castFloatTowPointNum(wlfzZfyxlc));
				scoreGroupStatisticsRange.setYxrs(zfYxRs);

				scoreGroupStatisticsRangeList.add(scoreGroupStatisticsRange);
				// 计算班级标准分
				for (JSONObject bj : bjZfDataList1) {
					if (bj.containsKey("bjavgZfScr")) {
						float param = (bj.getFloatValue("bjavgZfScr") - wlfzZfPjf) / wlfzZfbzc;
						float bjbzf = ScoreUtil.castFloatTowPointNum(param);
						bj.put("bjbzf", bjbzf);
						// 新建班级总分统计结果
						ScoreClassStatisticsRange scoreClassStatisticsRange = new ScoreClassStatisticsRange();
						scoreClassStatisticsRange.setBh(bj.getString("bh"));
						scoreClassStatisticsRange.setXnxq(xnxq);
						scoreClassStatisticsRange.setKslc(kslc);
						scoreClassStatisticsRange.setXxdm(xxdm);
						scoreClassStatisticsRange.setCkrs(bj.getIntValue("ckrs"));
						int tjrs = bj.getIntValue("fwtjrs");
						if (tjrs == 0) {
							continue;
						}
						scoreClassStatisticsRange.setTjrs(tjrs);
						scoreClassStatisticsRange.setPjf(bj.getFloat("bjavgZfScr"));
						scoreClassStatisticsRange.setPjffc(ScoreUtil.castFloatTowPointNum(bj.getFloat("bjpjfc")));
						scoreClassStatisticsRange.setBzc(ScoreUtil.castFloatTowPointNum(bj.getFloat("bjzfbzc")));
						scoreClassStatisticsRange.setZgf(bj.getFloat("bjmaxZfScr"));
						scoreClassStatisticsRange.setZdf(bj.getFloat("bjminZfScr"));
						scoreClassStatisticsRange.setBzf(bjbzf);
						scoreClassStatisticsRange.setYxl(ScoreUtil.castFloatTowPointNum(bj.getFloat("bjzfYxPer")));
						scoreClassStatisticsRange.setYxrs(bj.getIntValue("bjzfYxRs"));
						scoreClassStatisticsRange
								.setYxlc(ScoreUtil.castFloatTowPointNum(fzMaxYxl - bj.getFloatValue("bjzfYxPer")));
						scoreClassStatisticsRange
								.setHglc(ScoreUtil.castFloatTowPointNum(fzMaxHgl - bj.getFloatValue("bjzfHgPer")));
						scoreClassStatisticsRange
								.setDflc(ScoreUtil.castFloatTowPointNum(bj.getFloatValue("bjzfDfPer") - fzMinDfl));
						scoreClassStatisticsRange.setHgl(ScoreUtil.castFloatTowPointNum(bj.getFloat("bjzfHgPer")));
						scoreClassStatisticsRange.setHgrs(bj.getIntValue("bjzfHgRs"));
						scoreClassStatisticsRange.setDfl(ScoreUtil.castFloatTowPointNum(bj.getFloat("bjzfDfPer")));
						scoreClassStatisticsRange.setDfrs(bj.getIntValue("bjzfDfRs"));
						scoreClassStatisticsRange.setQnsrs(bj.getIntValue("bjzfQnRs"));
						scoreClassStatisticsRange.setJzsrs(bj.getIntValue("bjzfJzRs"));
						scoreClassStatisticsRange.setPm(bj.getIntValue("bjpjfpm"));
						scoreClassStatisticsRange.setYxlpm(bj.getIntValue("bjyxlpm"));
						scoreClassStatisticsRange.setHglpm(bj.getIntValue("bjhglpm"));
						scoreClassStatisticsRange.setDflpm(bj.getIntValue("bjdflpm"));
						scoreClassStatisticsRange.setMf((float) wlzf);
						scoreClassStatisticsRange.setLdxs(ScoreUtil.castFloatTowPointNum(bj.getFloat("bjzfDiffiPer")));
						scoreClassStatisticsRange.setQfd(ScoreUtil.castFloatTowPointNum(bj.getFloat("bjzfDiffRate")));
						scoreClassStatisticsRange.setLdpm(bj.getIntValue("bjldpm"));
						scoreClassStatisticsRange.setBzcpm(bj.getIntValue("bjbzcpm"));
						scoreClassStatisticsRange.setQfdpm(bj.getIntValue("bjqfdpm"));
						scoreClassStatisticsRange.setBh(bj.getString("bh"));
						scoreClassStatisticsRangeList.add(scoreClassStatisticsRange);
					}
				}

				// 循环科目 计算科目的标准差等
				for (JSONObject km : kmList) {
					String kmdm = km.getString("kmdm");
					ScoreGroupStatisticsMkRange scoreGroupStatisticsMkRange = kmTsgssMap.get(kmdm);
					if (scoreGroupStatisticsMkRange == null || scoreGroupStatisticsMkRange.getKslc() == null) {
						continue;
					}
					List<JSONObject> bjKmDataList1 = GrepUtil.grepJsonKeyByVal(new String[] { "wlfz", "kmdm" },
							new String[] { wlfz, kmdm }, bjkmDataList);
					float wlfzKmPjf = (float) scoreGroupStatisticsMkRange.getPjf();
					float wlfzKmbzc = ScoreUtil.getFzDtSq(bjKmDataList1, "avgKmScr", wlfzKmPjf);
					// 优秀 合格 低分 排名与率差
					List<JSONObject> bjKmDataList2 = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmYxPer", "desc", "wlfz",
							"yxl");
					float wlfzKmyxlc = bjKmDataList2.get(0).getFloatValue("kmYxPer")
							- bjKmDataList2.get(bjKmDataList2.size() - 1).getFloatValue("kmYxPer");
					float fzkmMaxYxl = bjKmDataList2.get(0).getFloatValue("kmYxPer");
					bjKmDataList2 = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmHgPer", "desc", "wlfz", "hgl");
					float wlfzKmhglc = bjKmDataList2.get(0).getFloatValue("kmHgPer")
							- bjKmDataList2.get(bjKmDataList2.size() - 1).getFloatValue("kmHgPer");
					float fzkmMaxHgl = bjKmDataList2.get(0).getFloatValue("kmHgPer");
					bjKmDataList2 = ScoreUtil.sorStuScoreList(bjKmDataList1, "kmDfPer", "asc", "wlfz", "dfl");
					float wlfzKmdflc = bjKmDataList2.get(bjKmDataList2.size() - 1).getFloatValue("kmDfPer")
							- bjKmDataList2.get(0).getFloatValue("kmDfPer");
					float fzkmMinDfl = bjKmDataList2.get(0).getFloatValue("kmDfPer");
					// 文理分组平均分分差
					float wlfzKmpjfcz = ScoreUtil.getFzCz(bjKmDataList1, "avgKmScr");
					if (scoreGroupStatisticsMkRange.getKslc() != null) {

						// scoreGroupStatisticsMkRange.setPjf(wlfzKmPjf);
						scoreGroupStatisticsMkRange.setBzc(ScoreUtil.castFloatTowPointNum(wlfzKmbzc));
						scoreGroupStatisticsMkRange.setYxlc(ScoreUtil.castFloatTowPointNum(wlfzKmyxlc));
						scoreGroupStatisticsMkRange.setHglc(ScoreUtil.castFloatTowPointNum(wlfzKmhglc));
						scoreGroupStatisticsMkRange.setDflc(ScoreUtil.castFloatTowPointNum(wlfzKmdflc));
						scoreGroupStatisticsMkRange.setPjffc(ScoreUtil.castFloatTowPointNum(wlfzKmpjfcz));
						scoreGroupStatisticsMkRange.setXnxq(xnxq);

						scoreGroupStatisticsMkRangeList.add(scoreGroupStatisticsMkRange);
					}

					// 计算班级标准分
					for (JSONObject bj : bjKmDataList1) {
						if (bj.containsKey("avgKmScr")) {
							float param = (bj.getFloatValue("avgKmScr") - wlfzKmPjf) / wlfzKmbzc;
							float bjbzf = ScoreUtil.castFloatTowPointNum(param);
							bj.put("bjbzf", bjbzf);
							// 新建科目总分统计结果
							ScoreClassStatisticsMkRange scoreClassStatisticsMkRange = new ScoreClassStatisticsMkRange();
							scoreClassStatisticsMkRange.setBh(bj.getString("bh"));
							scoreClassStatisticsMkRange.setXnxq(xnxq);
							scoreClassStatisticsMkRange.setKslc(kslc);
							scoreClassStatisticsMkRange.setXxdm(xxdm);
							scoreClassStatisticsMkRange.setCkrs(bj.getIntValue("ckrs"));
							int tjrs = bj.getIntValue("fwtjrs");
							if (tjrs == 0) {
								continue;
							}
							scoreClassStatisticsMkRange.setTjrs(tjrs);
							scoreClassStatisticsMkRange.setPjf(bj.getFloat("avgKmScr"));
							scoreClassStatisticsMkRange.setPjffc(ScoreUtil.castFloatTowPointNum(bj.getFloat("kmpjfc")));
							scoreClassStatisticsMkRange.setBzc(ScoreUtil.castFloatTowPointNum(bj.getFloat("kmbzc")));
							// scoreClassStatisticsMkRange.setZgf(bj.getFloat("maxKmScr"));
							// //缺字段
							// scoreClassStatisticsMkRange.setZdf(bj.getFloat("minKmScr"));
							// scoreClassStatisticsMkRange.setBzf(bjbzf);
							scoreClassStatisticsMkRange.setYxl(ScoreUtil.castFloatTowPointNum(bj.getFloat("kmYxPer")));
							scoreClassStatisticsMkRange.setYxrs(bj.getIntValue("kmYxRs"));
							scoreClassStatisticsMkRange.setHgl(ScoreUtil.castFloatTowPointNum(bj.getFloat("kmHgPer")));
							scoreClassStatisticsMkRange.setHgrs(bj.getIntValue("kmHgRs"));
							scoreClassStatisticsMkRange.setDfl(ScoreUtil.castFloatTowPointNum(bj.getFloat("kmDfPer")));
							scoreClassStatisticsMkRange.setDfrs(bj.getIntValue("kmDfRs"));
							// scoreClassStatisticsMkRange.setQnsrs(bj.getIntValue("kmQnRs"));
							// scoreClassStatisticsMkRange.setJzsrs(bj.getIntValue("kmJzRs"));
							scoreClassStatisticsMkRange.setPm(bj.getIntValue("kmpjfpm"));
							scoreClassStatisticsMkRange.setYxlpm(bj.getIntValue("kmyxlpm"));
							scoreClassStatisticsMkRange.setHglpm(bj.getIntValue("kmhglpm"));
							scoreClassStatisticsMkRange.setDflpm(bj.getIntValue("kmdflpm"));
							scoreClassStatisticsMkRange.setMf(km.getFloat("mf"));
							scoreClassStatisticsMkRange
									.setLdxs(ScoreUtil.castFloatTowPointNum(bj.getFloat("kmDiffiPer")));
							scoreClassStatisticsMkRange.setQfd(bj.getFloat("kmDiffRate"));
							scoreClassStatisticsMkRange.setLdpm(bj.getIntValue("kmldpm"));
							scoreClassStatisticsMkRange.setBzcpm(bj.getIntValue("kmbzcpm"));
							scoreClassStatisticsMkRange.setQfdpm(bj.getIntValue("kmqfdpm"));
							scoreClassStatisticsMkRange.setBh(bj.getString("bh"));
							scoreClassStatisticsMkRange
									.setYxlc(ScoreUtil.castFloatTowPointNum(fzkmMaxYxl - bj.getFloatValue("kmYxPer")));
							scoreClassStatisticsMkRange
									.setHglc(ScoreUtil.castFloatTowPointNum(fzkmMaxHgl - bj.getFloatValue("kmHgPer")));
							scoreClassStatisticsMkRange
									.setDflc(ScoreUtil.castFloatTowPointNum(bj.getFloatValue("kmDfPer") - fzkmMinDfl));
							scoreClassStatisticsMkRange.setKmdm(kmdm);
							scoreClassStatisticsMkRangeList.add(scoreClassStatisticsMkRange);
						}
					}
				}
			}

			progressBar.setProgressInfo(1, 30, "正在删除原分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());

			// 删除所有的分析数据
			sAlzDao.deleteOldExamAlzRs(xnxq, autoIncr, params);

			progressBar.setProgressInfo(1, 31, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			/**
			 * 保存分析结果
			 */
			// 保存等级序列
			if (needInsertLvlSeq.size() > 0) {
				// 批量插入等级序列权重
				sAlzDao.insertScoreLevelSequnceBatch(xnxq, autoIncr, needInsertLvlSeq);
			}
			if (scoreStuStatisticsRankList.size() > 0) {
				sAlzDao.insertScoreStuStatisticsRankBatch(xnxq, autoIncr, scoreStuStatisticsRankList);
			}
			progressBar.setProgressInfo(1, 32, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (scoreStuStatisticsRankMkList.size() > 0) {
				sAlzDao.insertScoreStuStatisticsRankMkBatch(xnxq, autoIncr, scoreStuStatisticsRankMkList);
			}
			progressBar.setProgressInfo(1, 35, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (scoreClassStatisticsList.size() > 0) {
				sAlzDao.insertScoreClassStatisticsBatch(xnxq, autoIncr, scoreClassStatisticsList);
			}
			progressBar.setProgressInfo(1, 40, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (scoreClassStatisticsMkList.size() > 0) {
				sAlzDao.insertScoreClassStatisticsMkBatch(xnxq, autoIncr, scoreClassStatisticsMkList);
			}
			progressBar.setProgressInfo(1, 45, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());

			if (scoreClassStatisticsRangeList.size() > 0) {
				sAlzDao.insertScoreClassStatisticsRangeBatch(xnxq, autoIncr, scoreClassStatisticsRangeList);
			}

			progressBar.setProgressInfo(1, 50, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (scoreClassStatisticsMkRangeList.size() > 0) {
				sAlzDao.insertScoreClassStatisticsMkRange(xnxq, autoIncr, scoreClassStatisticsMkRangeList);
			}
			progressBar.setProgressInfo(1, 55, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (scoreClassDistributeList.size() > 0) {
				sAlzDao.insertScoreClassDistributeBatch(xnxq, autoIncr, scoreClassDistributeList);
			}
			progressBar.setProgressInfo(1, 60, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (scoreClassDistributeMkList.size() > 0) {
				sAlzDao.insertScoreClassDistributeMkBatch(xnxq, autoIncr, scoreClassDistributeMkList);
			}
			progressBar.setProgressInfo(1, 65, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (scoreRankStatisticsList.size() > 0) {
				sAlzDao.insertScoreRankStatisticsBatch(xnxq, autoIncr, scoreRankStatisticsList);
			}
			if (scoreRankStatisticsMkList.size() > 0) {
				sAlzDao.insertScoreRankStatisticsMk(xnxq, autoIncr, scoreRankStatisticsMkList);
			}
			if (scoreGroupStatisticsList.size() > 0) {
				sAlzDao.insertScoreGroupStatisticsBatch(xnxq, autoIncr, scoreGroupStatisticsList);
			}
			progressBar.setProgressInfo(1, 70, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (scoreGroupStatisticsRangeList.size() > 0) {
				sAlzDao.insertScoreGroupStatisticsRangeBatch(xnxq, autoIncr, scoreGroupStatisticsRangeList);
			}
			if (scoreGroupStatisticsMkList.size() > 0) {
				sAlzDao.insertScoreGroupStatisticsMkBatch(xnxq, autoIncr, scoreGroupStatisticsMkList);
			}
			if (scoreGroupStatisticsMkRangeList.size() > 0) {
				sAlzDao.insertScoreGroupStatisticsMkRange(xnxq, autoIncr, scoreGroupStatisticsMkRangeList);
			}
			progressBar.setProgressInfo(1, 75, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (scoreStuBzfList.size() > 0) {
				sAlzDao.insertScoreStuBzfBatch(xnxq, autoIncr, scoreStuBzfList);
			}
			if (scoreStuBzfMkList.size() > 0) {
				sAlzDao.insertScoreStuBzfMkBatch(xnxq, autoIncr, scoreStuBzfMkList);
			}
			if (scoreStuJzsqnsList.size() > 0) {
				sAlzDao.insertScoreStuJzsqnsBatch(xnxq, autoIncr, scoreStuJzsqnsList);
			}
			progressBar.setProgressInfo(1, 80, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (scoreStuJzsqnsMkList.size() > 0) {
				sAlzDao.insertScoreStuJzsqnsMkBatch(xnxq, autoIncr, scoreStuJzsqnsMkList);
			}
			progressBar.setProgressInfo(1, 82, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (classScoreLevelMkList.size() > 0) {
				sAlzDao.insertClassScoreLevelMkBatch(xnxq, autoIncr, classScoreLevelMkList);
			}
			progressBar.setProgressInfo(1, 85, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (groupScoreLevelMkList.size() > 0) {
				sAlzDao.insertGroupScoreLevelMkBatch(xnxq, autoIncr, groupScoreLevelMkList);
			}
			progressBar.setProgressInfo(1, 90, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (classScoreLevelSequnceList.size() > 0) {
				sAlzDao.insertClassScoreLevelSequnceBatch(xnxq, autoIncr, classScoreLevelSequnceList);
			}
			// 竞赛报告
			if (competitionStuStatisticsList.size() > 0) {
				sAlzDao.insertCompetitionStuStatisticsBatch(xnxq, autoIncr, competitionStuStatisticsList);
			}
			// 成绩报告
			// 年级报告
			if (analysisReportnjList.size() > 0) {
				sAlzDao.insertAnalysisReportNjBatch(xnxq, autoIncr, analysisReportnjList);
			}
			if (analysisReportBjList.size() > 0) {
				sAlzDao.insertAnalysisReportBjBatch(xnxq, autoIncr, analysisReportBjList);
			}
			
			progressBar.setProgressInfo(1, 95, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (analysisReportStuList.size() > 0) {
				sAlzDao.insertAnalysisReportStuBatch(xnxq, autoIncr, analysisReportStuList);
			}

			progressBar.setProgressInfo(1, 96, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			if (appStudentScoreReportList.size() > 0) {// app学生报告
				sAlzDao.insertAppStudentScoreReportBatch(xnxq, autoIncr, appStudentScoreReportList);
			}
			
			progressBar.setProgressInfo(1, 97, "正在保存分析结果...");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			// 保存设置项
			if (scoreDistributeList.size() > 0) {
				// 批量更新总分设置
				sAlzDao.insertScoreDistributeBatch(xnxq, autoIncr, scoreDistributeList);
			}
			if (zfmfqjList.size() > 0) {
				// 批量更新总分区间设置
				sAlzDao.insertZfmfqjBatch(xnxq, autoIncr, zfmfqjList);
			}
			if (kmmfqjList.size() > 0) {
				// 批量更新科目满分区间
				sAlzDao.insertKmmfqjBatch(xnxq, autoIncr, kmmfqjList);
			}
			if (kmScDisList.size() > 0) {
				// 批量更新科目满分设置
				sAlzDao.insertScoreDistributeKmBatch(xnxq, autoIncr, kmScDisList);
			}

			exam.setFxflag("1");
			scoreDao.updatetDegreeInfo(xnxq, exam);

			// 生成成绩报告文件
			File folder = new File(scoreReportFilePath);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			String reportName = xxdm + "_" + kslc + "_" + new Date().getTime() + ".json";
			// 去重文件
			for (int i = 0; i < folder.listFiles().length; i++) {
				File now = folder.listFiles()[i];
				if (now.getName().startsWith(xxdm + "_" + kslc + "_")) {
					try {
						if (now.canWrite()) {
							now.delete();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			File f = new File(folder + "/" + reportName);
			if (f.exists()) {
				f.delete();
			}
			
			f.createNewFile();
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			JSONArray szDataExams = new JSONArray();
			szDataExams.add(szDataExam);

			bw.write(szDataExams.toJSONString());
			bw.close();
			
			progressBar.setProgressInfo(2, 100, "恭喜，分析成功");
			redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new CommonRunException(-1, "分析出错，成绩或设置项中有不合法数值！");
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new CommonRunException(-1, "发现不合法设置项，请检查各设置项或联系管理员！");
		} catch (MyBatisSystemException e) {
			e.printStackTrace();
			throw new CommonRunException(-1, "保存数据时出错，请稍后再试！");
		}
	}

	private List<JSONObject> getSingleSubjectScore(School school, JSONObject params, Integer autoIncr,
			Map<String, JSONObject> xjhMap) {
		String xnxq = params.getString("xnxq");
		List<JSONObject> rs = sAlzDao.getSingleSubjectScore(xnxq, autoIncr, params);

		// 获取科目名称
		List<LessonInfo> kmList = commonDataService.getLessonInfoList(school, xnxq);
		Map<Long, LessonInfo> kmMap = new HashMap<Long, LessonInfo>();
		for (LessonInfo lessonInfo : kmList) {
			kmMap.put(lessonInfo.getId(), lessonInfo);
		}

		for (JSONObject stu : rs) {
			long xh = stu.getLongValue("xh");
			long kmdm = stu.getLongValue("kmdm");
			String xjh = String.valueOf(xh);
			String xm = "";
			if (xjhMap.containsKey(xjh)) {
				xm = xjhMap.get(xjh).getString("xm");
			}
			stu.put("xm", xm);

			LessonInfo km = kmMap.get(kmdm);
			if (km != null) {
				stu.put("kmmc", km.getName());
			}
		}
		return rs;
	}

	private List<JSONObject> getAllExamClass(School sch, JSONObject params, Integer autoIncr) {
		String xnxq = params.getString("xnxq");
		List<JSONObject> claList = sAlzDao.getAllExamClass(xnxq, autoIncr, params);

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", params.getLong("xxdm"));
		map.put("termInfoId", xnxq);
		List<Classroom> bjList = commonDataService.getClassList(map);

		Map<Long, Classroom> bjMap = new HashMap<Long, Classroom>();
		for (Classroom cla : bjList) {
			bjMap.put(cla.getId(), cla);
		}

		List<Grade> njList = commonDataService.getGradeList(sch, xnxq);
		Map<Long, Grade> njdmNjMap = new HashMap<Long, Grade>();
		for (Grade gd : njList) {
			njdmNjMap.put(gd.getId(), gd);
		}

		String xn = xnxq.substring(0, xnxq.length() - 1);

		List<JSONObject> needRemoveDelBjs = new ArrayList<JSONObject>();
		for (JSONObject o : claList) {
			long bh = o.getLongValue("bh");
			Classroom cr = bjMap.get(bh);
			if (cr == null || !njdmNjMap.containsKey(cr.getGradeId()) || cr.getClassName() == null) {
				needRemoveDelBjs.add(o);
				continue;
			}
			o.put("bjmc", cr.getClassName());
			int njdm = njdmNjMap.get(cr.getGradeId()).getCurrentLevel().getValue();
			String synj = commonDataService.ConvertNJDM2SYNJ(String.valueOf(njdm), xn);
			o.put("nj", synj);
			o.put("synj", synj);
		}
		claList.removeAll(needRemoveDelBjs);
		return claList;
	}

	private List<JSONObject> getKmInWlfz(School school, Integer autoIncr, JSONObject params) {
		String xnxq = params.getString("xnxq");

		List<JSONObject> list = sAlzDao.getKmInWlfz(xnxq, autoIncr, params);
		List<LessonInfo> kmList = commonDataService.getLessonInfoList(school, xnxq);
		HashMap<Long, String> kmmcMap = new HashMap<Long, String>();
		for (LessonInfo km : kmList) {
			kmmcMap.put(km.getId(), km.getName());
		}
		for (JSONObject o : list) {
			o.put("kmmc", kmmcMap.get(o.getLong("kmdm")));
		}
		return list;
	}

	private List<JSONObject> getTeacherClassList(School school, JSONObject params, Map<String, JSONObject> bjJsonMap) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", params.getLong("xxdm"));
		map.put("termInfoId", params.get("xnxq"));
		JSONArray temp = commonDataService.getCourseTeacherList(school, map);

		List<JSONObject> rs = new ArrayList<JSONObject>();
		for (int i = 0; i < temp.size(); i++) {
			JSONObject t = temp.getJSONObject(i);
			String bh = t.get("classId").toString();

			if (bjJsonMap.containsKey(bh)) {
				JSONObject o = new JSONObject();
				o.put("zgh", t.get("teaId").toString());
				o.put("kmdm", t.get("lessonId").toString());
				o.put("xm", t.get("teaName"));
				o.put("kmmc", t.get("lessonName"));
				o.put("bh", bh);
				o.put("wlfz", bjJsonMap.get(bh).get("wlfz"));
				rs.add(o);
			}
		}
		return rs;
	}
}
