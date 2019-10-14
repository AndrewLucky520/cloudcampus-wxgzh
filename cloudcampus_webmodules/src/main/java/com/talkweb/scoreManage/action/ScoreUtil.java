package com.talkweb.scoreManage.action;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.GrepUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.scoreManage.po.gm.AppStudentScoreReport;
import com.talkweb.scoreManage.po.gm.Kmmfqj;
import com.talkweb.scoreManage.po.gm.ScoreClassStatistics;
import com.talkweb.scoreManage.po.gm.ScoreClassStatisticsMk;
import com.talkweb.scoreManage.po.gm.ScoreDistribute;
import com.talkweb.scoreManage.po.gm.ScoreDistributeKm;
import com.talkweb.scoreManage.po.gm.ScoreLevelTemplate;
import com.talkweb.scoreManage.po.gm.ScoreStuStatisticsRank;
import com.talkweb.scoreManage.po.gm.ScoreStuStatisticsRankMk;
import com.talkweb.scoreManage.po.gm.SynthScore;
import com.talkweb.scoreManage.po.gm.Zfmfqj;
import com.talkweb.scoreManage.po.sar.AnalysisReportBj;
import com.talkweb.scoreManage.po.sar.AnalysisReportStu;

public class ScoreUtil {
	private static final Logger logger = LoggerFactory.getLogger(ScoreUtil.class);
	
	
	public static void main(String[] args) {
		logger.info("main");
		System.out.println("main");
		String djsx = "0A0B0C0D0E";
		for (int i = 0; i < djsx.length(); i += 2) {
			System.out.println("i:"+i+","+djsx.substring(i, i + 1));
			int num = Integer.parseInt(djsx.substring(i, i + 1));
			if (num == 0) {
				djsx = djsx.substring(0, i) + djsx.substring(i + 2, djsx.length());
				i -= 2;
				System.out.println(djsx);
			}
		}
	}
	
	/**
	 * 根据成绩对数组进行冒泡排序（成绩从大到小排序）
	 * 
	 * @param sourceArr
	 * @return
	 */
	public static JSONArray sortByScore(JSONArray sourceArr) {
		int lth = sourceArr.size();
		for (int i = 0; i < lth; i++) {
			JSONObject l = sourceArr.getJSONObject(i);
			if (!l.containsKey("cj") || l.getFloat("cj") == null || StringUtils.isEmpty(l.get("cj"))) {
				sourceArr.remove(l);
				lth--;
			}
		}

		Collections.sort(sourceArr, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				float cj1 = ((JSONObject) o1).getFloatValue("cj");
				float cj2 = ((JSONObject) o2).getFloatValue("cj");
				return -Float.compare(cj1, cj2);
			}
		});

		return sourceArr;
	}

	public static JSONObject getFzAndRsByArr(JSONArray kmCjArr, float bl, int lastRs, float lastCj) {
		JSONObject result = new JSONObject();

		int rs = (int) (kmCjArr.size() * bl) / 100;

		if (rs == 0) {
			result.put("rs", 0);
			result.put("cj", lastCj);
			return result;
		}

		if (kmCjArr.size() >= (rs - 1)) {
			float cj = kmCjArr.getJSONObject(rs - 1).getFloatValue("cj");
			while (rs < kmCjArr.size() && kmCjArr.getJSONObject(rs).getFloat("cj") >= cj) {
				rs++;
			}
			if (rs - lastRs < 0) {
				result.put("rs", 0);
			} else {
				result.put("rs", rs - lastRs);
			}
			result.put("cj", cj);
		} else {
			result.put("rs", 0);
			result.put("cj", 0);
		}
		return result;
	}

	/**
	 * 单科科目通过设置分数得到对应的人数
	 * 
	 * @param kmCjArr
	 * @param score
	 *            本等级分数 90
	 * @param score0上一等级分数
	 *            110
	 * @return
	 */
	public static int getSingleFzAndRsByArr(JSONArray kmCjArr, float cj, float lastCj) {
		int rs = 0;
		for (int i = 0; i < kmCjArr.size(); i++) {
			float score = kmCjArr.getJSONObject(i).getFloatValue("cj");
			if (score < lastCj && score >= cj) {
				rs++;
			}
		}
		return rs;
	}

	/**
	 * 通过综合科目list、成绩list得到最终结果 （科目-成绩）
	 * 
	 * @param tsynthList
	 * @param cjList
	 * @return
	 */
	public static Map<String, JSONArray> getXskmcjMap(List<SynthScore> tsynthList, List<JSONObject> cjList) {
		// 学号-分组-科目：成绩
		Map<String, Map<String, Map<String, Float>>> xsfzcjMap = new HashMap<String, Map<String, Map<String, Float>>>();
		// 学号-科目：成绩
		Map<String, Map<String, Float>> xskmcjMap = new HashMap<String, Map<String, Float>>();

		for (JSONObject o : cjList) {
			String xh = o.getString("xh"); // 学生代码
			String kmdm = o.getString("kmdm"); // 科目代码
			String fzdm = o.getString("bmfz"); // 分组代码
			float cj = o.getFloatValue("cj");

			if (!xsfzcjMap.containsKey(xh)) {
				xsfzcjMap.put(xh, new HashMap<String, Map<String, Float>>());
			}
			Map<String, Map<String, Float>> fzcj = xsfzcjMap.get(xh);
			if (!fzcj.containsKey(fzdm)) {
				fzcj.put(fzdm, new HashMap<String, Float>());
			}
			fzcj.get(fzdm).put(kmdm, cj);

			if (!xskmcjMap.containsKey(xh)) {
				xskmcjMap.put(xh, new HashMap<String, Float>());
			}
			xskmcjMap.get(xh).put(kmdm, cj);
		}

		// 获取合并科目成绩
		if (tsynthList.size() > 0) {
			for (SynthScore tth : tsynthList) {
				String fzdm = tth.getFzdm(); // 分组代码
				String kmdm = tth.getKmdm(); // 科目代码
				String[] zhkms = tth.getDykm().split(","); // 综合成绩来源模块列表

				for (String xh : xskmcjMap.keySet()) {
					float tsum = 0;
					boolean ext = true;
					for (int k = 0; k < zhkms.length; k++) {
						Map<String, Float> km2cj = xsfzcjMap.get(xh).get(fzdm);
						if (km2cj != null && km2cj.containsKey(zhkms[k])) {
							tsum += km2cj.get(zhkms[k]);
						} else {
							ext = false;
							continue;
						}
					}
					if (ext) {
						Map<String, Float> kmcj = xskmcjMap.get(xh);
						if (!kmcj.containsKey(kmdm)) {
							kmcj.put(kmdm, tsum);
						}
					}
				}
			}
		}

		// 转换为科目-学号-成绩映射
		Map<String, JSONArray> kmXhCjMap = new HashMap<String, JSONArray>();
		for (Map.Entry<String, Map<String, Float>> entry : xskmcjMap.entrySet()) {
			String xh = entry.getKey();
			Map<String, Float> kmcj = entry.getValue();
			for (String kmdm : kmcj.keySet()) {
				if (!kmXhCjMap.containsKey(kmdm)) {
					kmXhCjMap.put(kmdm, new JSONArray());
				}
				JSONObject json = new JSONObject();
				json.put("xh", xh.toString());
				json.put("cj", kmcj.get(kmdm));
				kmXhCjMap.get(kmdm).add(json);
			}
		}
		return kmXhCjMap;
	}

	/**
	 * 将各科目成绩排序分组得到最终的结果
	 * 
	 * @param map
	 * @param sltpList
	 *            等第值
	 * @param tsynthList
	 *            综合科目成绩list
	 * @param cjList
	 *            成绩list
	 * @return
	 */
	public static List<ScoreLevelTemplate> getDJInfo(JSONObject params, List<ScoreLevelTemplate> sltpList,
			List<SynthScore> tsynthList, List<JSONObject> cjList) {
		String xxdm = params.getString("xxdm");
		String xnxq = params.getString("xnxq");
		String kslc = params.getString("kslc");
		String nj = params.getString("nj");

		Collections.sort(sltpList, new Comparator<ScoreLevelTemplate>() {
			@Override
			public int compare(ScoreLevelTemplate o1, ScoreLevelTemplate o2) {
				int dm1 = Integer.parseInt(o1.getDm());
				int dm2 = Integer.parseInt(o2.getDm());
				return Integer.compare(dm1, dm2);
			}
		});

		Map<String, JSONArray> kmXhCjMap = getXskmcjMap(tsynthList, cjList);
		List<ScoreLevelTemplate> sltpList2 = new ArrayList<ScoreLevelTemplate>();
		// 循环每个科目
		for (Map.Entry<String, JSONArray> entry : kmXhCjMap.entrySet()) {
			// 科目成绩列表-学号：成绩
			String kmdm = entry.getKey();
			JSONArray kmCjArr = entry.getValue();
			if (kmCjArr == null) {
				kmCjArr = new JSONArray();
			}
			kmCjArr = ScoreUtil.sortByScore(kmCjArr);

			// 循环每个比例
			float lastCj = 0;// 最近的成绩
			int lastRs = 0;
			int size = sltpList.size();
			boolean less = true;
			for (int ts = 0; ts < size; ts++) {
				ScoreLevelTemplate se = sltpList.get(ts);
				ScoreLevelTemplate scoreLevelTemplate = new ScoreLevelTemplate();
				float bl = 0f;
				if (ts == size - 1) {
					bl = 100;
				} else {
					if (se.getBl() != null) {
						bl = se.getBl();
					}
				}

				JSONObject rs = new JSONObject();
				if (less) {
					rs = ScoreUtil.getFzAndRsByArr(kmCjArr, bl, lastRs, lastCj);
					if (rs.getIntValue("rs") == kmCjArr.size() && ts == 0) {
						less = false;
					}
				} else {
					rs.put("rs", 0);
					rs.put("cj", 0);
				}
				lastRs += rs.getIntValue("rs");
				lastCj = rs.getFloat("cj");

				scoreLevelTemplate.setBl(bl);
				scoreLevelTemplate.setCj(rs.getFloat("cj"));
				scoreLevelTemplate.setRs(rs.getIntValue("rs"));
				scoreLevelTemplate.setDjmc(se.getDjmc());
				scoreLevelTemplate.setDm(se.getDm());
				scoreLevelTemplate.setKmdm(kmdm);
				scoreLevelTemplate.setKslc(kslc);
				scoreLevelTemplate.setNj(nj);
				scoreLevelTemplate.setXnxq(xnxq);
				scoreLevelTemplate.setXxdm(xxdm);
				sltpList2.add(scoreLevelTemplate);
			}
		}
		if (null == params.get("kmdm")) {
			sltpList2.addAll(sltpList);
		}
		return sltpList2;
	}

	/**
	 * 单科科目根据分数得到对应的人数
	 * 
	 * @param map
	 * @param sltpList
	 *            等第值
	 * @param tsynthList
	 *            综合科目成绩list
	 * @param cjList
	 *            成绩list
	 * @return
	 */
	public static List<ScoreLevelTemplate> getSingleDJInfo(JSONObject params, List<ScoreLevelTemplate> sltpList,
			List<SynthScore> tsynthList, List<JSONObject> cjList) {
		String xxdm = params.getString("xxdm");
		String xnxq = params.getString("xnxq");
		String kslc = params.getString("kslc");
		String nj = params.getString("nj");

		Collections.sort(sltpList, new Comparator<ScoreLevelTemplate>() {
			@Override
			public int compare(ScoreLevelTemplate o1, ScoreLevelTemplate o2) {
				int dm1 = Integer.parseInt(o1.getDm());
				int dm2 = Integer.parseInt(o2.getDm());
				return Integer.compare(dm1, dm2);
			}
		});

		Map<String, JSONArray> kmXhCjMap = getXskmcjMap(tsynthList, cjList);
		List<ScoreLevelTemplate> sltpList2 = new ArrayList<ScoreLevelTemplate>();
		// 循环每个科目
		for (Map.Entry<String, JSONArray> entry : kmXhCjMap.entrySet()) {
			// 科目成绩列表-学号：成绩
			String kmdm = entry.getKey();
			JSONArray kmCjArr = entry.getValue();
			if (kmCjArr == null) {
				kmCjArr = new JSONArray();
			}

			int allDdrs = 0;
			float lastCj = Float.MAX_VALUE; // 上一次等级的分数
			String lastDjmc = null;
			int size = sltpList.size();
			for (int ts = 0; ts < size; ts++) {
				ScoreLevelTemplate se = sltpList.get(ts);
				ScoreLevelTemplate scoreLevelTemplate = new ScoreLevelTemplate();

				float bl = 0f; // 设置比例
				if (ts == size - 1) {
					bl = 100;
				} else {
					if (se.getBl() != null) {
						bl = se.getBl();
					}
				}

				Float cj = se.getCj();
				if (cj == null) {
					throw new CommonRunException(-1, "等级" + se.getDjmc() + "的分数不能为空！");
				}
				if (cj > lastCj) {
					throw new CommonRunException(-1, "等级" + se.getDjmc() + "的分数应小于等级" + lastDjmc + "的分数");
				}

				int rs = ScoreUtil.getSingleFzAndRsByArr(kmCjArr, cj, lastCj);

				scoreLevelTemplate.setRs(rs);
				if (allDdrs == kmCjArr.size()) {
					scoreLevelTemplate.setRs(0);
				}
				if (!se.getDjmc().equalsIgnoreCase("E") && allDdrs < kmCjArr.size()) {
					allDdrs += rs;
				}

				scoreLevelTemplate.setDjmc(se.getDjmc());
				scoreLevelTemplate.setBl(bl);
				scoreLevelTemplate.setCj(cj);
				scoreLevelTemplate.setDm(se.getDm());
				scoreLevelTemplate.setKmdm(kmdm);
				scoreLevelTemplate.setKslc(kslc);
				scoreLevelTemplate.setNj(nj);
				scoreLevelTemplate.setXnxq(xnxq);
				scoreLevelTemplate.setXxdm(xxdm);
				sltpList2.add(scoreLevelTemplate);

				lastDjmc = se.getDjmc();
				lastCj = cj;
			}

			if (CollectionUtils.isNotEmpty(kmCjArr)) {
			}

			for (ScoreLevelTemplate se : sltpList2) {
				if (se.getDjmc().equalsIgnoreCase("E")) {
					se.setRs(kmCjArr.size() - allDdrs);
					break;
				}
			}
		}

		return sltpList2;
	}

	/**
	 * 将float转为两位小数（四舍五入）
	 * 
	 * @param param
	 * @return
	 */
	public static float castFloatTowPointNum(float param) {
		try {

			BigDecimal b = new BigDecimal(param);
			float rs = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			if (!Float.isNaN(rs)) {

				return rs;
			} else {
				return 0;
			}
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * 根据分数段划分规则 生成分段结果
	 * 
	 * @param zfsz
	 * @param string
	 * @return
	 */
	public static String scoreDisSep(JSONObject zfsz, String string) {
		// TODO Auto-generated method stub
		String rs = "";
		if (string.equalsIgnoreCase("zf")) {
			float zf = zfsz.getFloatValue("zf");
			String fs = zfsz.getString("fs");
			String zdytext = zfsz.getString("zdytext");
			int bl = zfsz.getIntValue("bl");
			int fz = zfsz.getIntValue("fz");
			int zhqjbl = zfsz.getIntValue("zhqjbl");
			int zhqjfz = zfsz.getIntValue("zhqjfz");

			int cz = (int) zf * bl / 100;
			if (fs.equalsIgnoreCase("01")) {
				rs = getFsdByMf(zf, bl, zhqjbl);
			} else if (fs.equalsIgnoreCase("02")) {
				rs = (int) (zf * 0.4) + ",0";
				if (zhqjfz != 0) {
					rs = zhqjfz + ",0";
				}
				// float mzf = (float) (zf * 0.4);
				String lr = "";
				int loop = 0;
				while (zf > zhqjfz && loop < 99) {
					loop++;
					lr += (int) zf + ",";
					zf = zf - fz;
				}
				rs = lr + rs;
			} else if (fs.equalsIgnoreCase("03")) {
				String zfstr = zfsz.getString("zf");
				float num1 = Float.parseFloat(zdytext.split(",")[0]);
				if (!zdytext.startsWith(zfstr) && num1 < zf) {
					zdytext = zfstr + "," + zdytext;
				}
				if (!zdytext.endsWith(",0")) {
					zdytext = zdytext + ",0";
				}
				rs = zdytext;
			}
		} else {
			float zf = zfsz.getFloatValue("mf");
			int dkbl = zfsz.getIntValue("dkbl");
			int dkzhqjbl = zfsz.getIntValue("dkzhqjbl");
			rs = getFsdByMf(zf, dkbl, dkzhqjbl);
		}
		return rs;
	}

	private static String getFsdByMf(float zf, int dkbl, int dkzhqjbl) {
		// TODO Auto-generated method stub
		int zhqj = (int) (zf * dkzhqjbl / 100);
		int cz = (int) (zf * dkbl / 100);
		String rs = zhqj + ",0";
		int loop = 0;
		int lzf = (int) zf;
		String lrs = "";
		while (lzf - cz > zhqj && loop < 96 && cz != 0) {
			loop++;
			lzf = lzf - cz;
			lrs += "," + lzf;
		}

		rs = ((int) zf) + lrs + "," + rs;

		return rs;
	}

	/**
	 * 根据总分满分设置生成总分区间
	 * 
	 * @param zfmf
	 * @param disr
	 * @return
	 */
	public static List<ScoreDistribute> getDistByZfmfqj(Zfmfqj zfmf, String disr) {
		// TODO Auto-generated method stub
		List<ScoreDistribute> rs = new ArrayList<ScoreDistribute>();
		String dm = zfmf.getDm();
		String xxdm = zfmf.getXxdm();
		String kslc = zfmf.getKslc();
		String xnxq = zfmf.getXnxq();
		if (disr.indexOf(",") > 0) {
			String[] dist = disr.split(",");
			for (int i = 1; i < dist.length; i++) {
				ScoreDistribute td = new ScoreDistribute();
				td.setKslc(kslc);
				td.setXxdm(xxdm);
				td.setXnxq(xnxq);
				td.setDm(dm);
				if (i < 10) {
					td.setFbdm("0" + i);
				} else {
					td.setFbdm("" + i);
				}
				// 临时代码 超过99的区间不管
				if (i >= 100) {
					continue;
				}
				td.setFbsx(Integer.parseInt(dist[i - 1]));
				td.setFbxx(Integer.parseInt(dist[i]));
				rs.add(td);
			}
		}
		return rs;
	}

	/**
	 * 
	 * 根据参数生成成绩区间段
	 * 
	 * @param kmqj
	 * @param disr
	 * @return
	 */
	public static List<ScoreDistributeKm> getDisByKmmfqj(Kmmfqj kmqj, String disr) {
		// TODO Auto-generated method stub
		List<ScoreDistributeKm> rs = new ArrayList<ScoreDistributeKm>();
		String dm = kmqj.getDm();
		String xxdm = kmqj.getXxdm();
		String kslc = kmqj.getKslc();
		String xnxq = kmqj.getXnxq();
		if (disr.indexOf(",") > 0) {
			String[] dist = disr.split(",");
			for (int i = 1; i < dist.length; i++) {
				ScoreDistributeKm td = new ScoreDistributeKm();
				td.setDm(dm);
				td.setXxdm(xxdm);
				td.setKslc(kslc);
				td.setXnxq(xnxq);
				if (i < 10) {
					td.setFbdm("0" + i);
				} else {
					td.setFbdm("" + i);
				}
				td.setFbsx(Integer.parseInt(dist[i - 1]));
				td.setFbxx(Integer.parseInt(dist[i]));
				rs.add(td);
			}
		}
		return rs;
	}

	public static List<JSONObject> getDdXlQzByDjAndKmNum(List<JSONObject> ddszList, int kmNum) {
		// TODO Auto-generated method stub
		ddszList = sorStuScoreList(ddszList, "tmpdjz", "desc", "", "tmpdjz");
		for (int i = 0; i < ddszList.size(); i++) {
			JSONObject dj = ddszList.get(i);
			if (dj.getString("djmc").equalsIgnoreCase("A")) {
				dj.put("djz", 1);
			} else {
				int lastDjz = ddszList.get(i - 1).getIntValue("djz");
				int djz = lastDjz * kmNum + 1;
				dj.put("djz", djz);
			}
		}
		// 最终结果
		long d0 = new Date().getTime();
		List<JSONObject> ddxl = getSubDdXlQzByDjAndKmNum("", 0, ddszList, kmNum, 0, kmNum);

		sorStuScoreList(ddxl, "djqz", "desc", "", "qz");
		for (JSONObject dd : ddxl) {
			dd.put("djqz", dd.get("qzpm"));
		}
		sorStuScoreList(ddxl, "djqz", "desc", "ot", "qz");
		long d1 = new Date().getTime();
		System.out.println("生成等第序列耗时：" + (d1 - d0) + "---等第序列长度：" + ddxl.size());

		return ddxl;
	}

	/**
	 * 根据等级设置及科目数获取等级序列及权重
	 * 
	 * @param left
	 * @param lqz
	 * @param ddszList
	 * @param kmNum
	 * @param ddll
	 * @param rKmNum
	 * @return
	 */
	public static List<JSONObject> getSubDdXlQzByDjAndKmNum(String left, int lqz, List<JSONObject> ddszList, int kmNum,
			int ddll, int rKmNum) {
		// TODO Auto-generated method stub
		// 最终结果
		List<JSONObject> ddxl = new ArrayList<JSONObject>();
		// p1 A
		if (ddll < ddszList.size()) {
			for (int i = ddll; i < ddszList.size(); i++) {
				int djz = ddszList.get(i).getIntValue("djz");
				String djmc = ddszList.get(i).getString("djmc");
				int qzz = djz * kmNum + lqz;
				JSONObject obj = new JSONObject();
				obj.put("djxl", left + rKmNum + djmc);
				obj.put("djqz", qzz);
				ddxl.add(obj);
				for (int j = rKmNum - 1; j > 0; j--) {
					// qzz =0;

					qzz = qzz - djz;
					List<JSONObject> temp = getSubDdXlQzByDjAndKmNum(left + j + djmc, qzz, ddszList, rKmNum - j, 1 + i,
							rKmNum - j);
					ddxl.addAll(temp);
				}
			}
		}

		return ddxl;
	}

	/**
	 * 根据成绩列表获取排序后的列表 并存入排名字段
	 * 
	 * @param scoreList
	 *            成绩列表
	 * @param orderKey
	 *            排序字段
	 * @param orderType
	 *            排序类型：asc/desc
	 * @param njbj
	 *            是年级排名还是班级排名
	 * @param km
	 *            是总分还是科目
	 * @return
	 */
	public static List<JSONObject> sorStuScoreList(List<JSONObject> scoreList, String orderKey, String orderType,
			String njbj, String km) {
		// TODO Auto-generated method stub
		long d1 = new Date().getTime();
		// 对学生进行排名
		if (orderType.equalsIgnoreCase("asc")) {
			for (int i = 0; i < scoreList.size() - 1; i++) {
				for (int j = 0; j < scoreList.size() - 1 - i; j++) {
					JSONObject l = scoreList.get(j);
					JSONObject r = scoreList.get(j + 1);
					double fl = 0, rl = 0;
					if (l.containsKey(orderKey)) {
						fl = l.getFloatValue(orderKey);
					}
					if (r.containsKey(orderKey)) {
						rl = r.getFloatValue(orderKey);
					}
					if (rl < fl) {
						scoreList.set(j, r);
						scoreList.set(j + 1, l);
					}
				}
			}
		} else {
			for (int i = 0; i < scoreList.size() - 1; i++) {
				for (int j = 0; j < scoreList.size() - 1 - i; j++) {
					JSONObject l = scoreList.get(j);
					JSONObject r = scoreList.get(j + 1);
					double fl = 0, rl = 0;
					if (l.containsKey(orderKey)) {
						fl = l.getFloatValue(orderKey);
					}
					if (r.containsKey(orderKey)) {
						rl = r.getFloatValue(orderKey);
					}
					if (rl > fl) {
						scoreList.set(j, r);
						scoreList.set(j + 1, l);
					}

				}
			}
		}

		if (km.equals("dfl")) {
			// 放入成绩排名
			int pm = 1;
			boolean never = true;
			for (int i = 0; i < scoreList.size(); i++) {
				if (scoreList.get(i).containsKey(orderKey)) {
					never = false;
				}
				if (scoreList.get(i).containsKey(orderKey) && i != 0) {
					double last = scoreList.get(i - 1).getFloatValue(orderKey);
					double now = scoreList.get(i).getFloatValue(orderKey);
					if (now != last && !never) {
						pm++;
					}
				}
				scoreList.get(i).put(njbj + km + "pm", pm);
			}
		} else {

			// 放入成绩排名
			for (int i = 0; i < scoreList.size(); i++) {
				if (i != 0) {
					double last = scoreList.get(i - 1).getFloatValue(orderKey);
					double now = scoreList.get(i).getFloatValue(orderKey);
					int lastPm = scoreList.get(i - 1).getIntValue(njbj + km + "pm");
					if (now == last) {
						scoreList.get(i).put(njbj + km + "pm", lastPm);
					} else {
						scoreList.get(i).put(njbj + km + "pm", i + 1);
					}
				} else {
					scoreList.get(i).put(njbj + km + "pm", 1);
				}
			}
		}
		return scoreList;
	}

	/**
	 * 根据成绩列表获取排序后的列表 并存入排名字段(不占位排序 如：1,2,2,3)
	 * 
	 * @param scoreList
	 *            成绩列表
	 * @param orderKey
	 *            排序字段
	 * @param orderType
	 *            排序类型：asc/desc
	 * @param njbj
	 *            是年级排名还是班级排名
	 * @param km
	 *            是总分还是科目
	 * @return
	 */
	public static List<Object> sorStuScoreListTwo(List<Object> scoreList, String orderKey, String orderType,
			String njbj, String km) {
		// 对学生进行排名
		if (orderType.equalsIgnoreCase("asc")) {
			for (int i = 0; i < scoreList.size() - 1; i++) {
				for (int j = 0; j < scoreList.size() - 1 - i; j++) {
					Object gg = scoreList.get(j);
					if (gg instanceof ScoreStuStatisticsRank) {
						ScoreStuStatisticsRank l = (ScoreStuStatisticsRank) scoreList.get(j);
						ScoreStuStatisticsRank r = (ScoreStuStatisticsRank) scoreList.get(j + 1);
						double fl = 0, rl = 0;
						fl = Double.valueOf(l.getBzzcj().toString());
						rl = Double.valueOf(r.getBzzcj().toString());
						if (rl < fl) {
							scoreList.set(j, r);
							scoreList.set(j + 1, l);
						}
					} else if (gg instanceof ScoreStuStatisticsRankMk) {
						ScoreStuStatisticsRankMk l = (ScoreStuStatisticsRankMk) scoreList.get(j);
						ScoreStuStatisticsRankMk r = (ScoreStuStatisticsRankMk) scoreList.get(j + 1);
						double fl = 0, rl = 0;
						fl = Double.valueOf(l.getBzcj().toString());
						rl = Double.valueOf(r.getBzcj().toString());
						if (rl < fl) {
							scoreList.set(j, r);
							scoreList.set(j + 1, l);
						}
					}
				}
			}
		} else {
			for (int i = 0; i < scoreList.size() - 1; i++) {
				for (int j = 0; j < scoreList.size() - 1 - i; j++) {
					Object gg = scoreList.get(j);
					if (gg instanceof ScoreStuStatisticsRank) {
						ScoreStuStatisticsRank l = (ScoreStuStatisticsRank) scoreList.get(j);
						ScoreStuStatisticsRank r = (ScoreStuStatisticsRank) scoreList.get(j + 1);
						double fl = 0, rl = 0;
						fl = Double.valueOf(l.getBzzcj().toString());
						rl = Double.valueOf(r.getBzzcj().toString());
						if (rl > fl) {
							scoreList.set(j, r);
							scoreList.set(j + 1, l);
						}
					} else if (gg instanceof ScoreStuStatisticsRankMk) {
						ScoreStuStatisticsRankMk l = (ScoreStuStatisticsRankMk) scoreList.get(j);
						ScoreStuStatisticsRankMk r = (ScoreStuStatisticsRankMk) scoreList.get(j + 1);
						double fl = 0, rl = 0;
						fl = Double.valueOf(l.getBzcj().toString());
						rl = Double.valueOf(r.getBzcj().toString());
						if (rl > fl) {
							scoreList.set(j, r);
							scoreList.set(j + 1, l);
						}
					}
				}
			}
		}

		// 放入成绩排名
		for (int i = 0; i < scoreList.size(); i++) {
			if (i != 0) {
				Object gg = scoreList.get(i);
				if (gg instanceof ScoreStuStatisticsRankMk) {
					ScoreStuStatisticsRankMk ggg = (ScoreStuStatisticsRankMk) scoreList.get(i);
					ScoreStuStatisticsRankMk ggl = (ScoreStuStatisticsRankMk) scoreList.get(i - 1);
					int lastPm = 0;
					double last = 0d;
					double now = 0d;
					if (orderKey.equals("bzbjpm")) {
						last = Double.valueOf(ggl.getBzcj().toString());
						now = Double.valueOf(ggg.getBzcj().toString());
						lastPm = ggl.getBzbjpm();
						if (now == last) {
							ggg.setBzbjpm(lastPm);
						} else {
							ggg.setBzbjpm(lastPm + 1);
						}
					}
					if (orderKey.equals("bznjpm")) {
						last = Double.valueOf(ggl.getBzcj().toString());
						now = Double.valueOf(ggg.getBzcj().toString());
						lastPm = ggl.getBznjpm();
						if (now == last) {
							ggg.setBznjpm(lastPm);
						} else {
							ggg.setBznjpm(lastPm + 1);
						}
					}
				} else if (gg instanceof ScoreStuStatisticsRank) {
					ScoreStuStatisticsRank ggg = (ScoreStuStatisticsRank) scoreList.get(i);
					ScoreStuStatisticsRank ggl = (ScoreStuStatisticsRank) scoreList.get(i - 1);
					int lastPm = 0;
					double last = 0d;
					double now = 0d;
					if (orderKey.equals("bzbjpm")) {
						last = Double.valueOf(ggl.getBzzcj().toString());
						now = Double.valueOf(ggg.getBzzcj().toString());
						lastPm = ggl.getBzbjpm();
						if (now == last) {
							ggg.setBzbjpm(lastPm);
						} else {
							ggg.setBzbjpm(lastPm + 1);
						}
					}
					if (orderKey.equals("bznjpm")) {
						last = Double.valueOf(ggl.getBzzcj().toString());
						now = Double.valueOf(ggg.getBzzcj().toString());
						lastPm = ggl.getBznjpm();
						if (now == last) {
							ggg.setBznjpm(lastPm);
						} else {
							ggg.setBznjpm(lastPm + 1);
						}
					}
				}
			} else {
				Object gg = scoreList.get(i);
				if (gg instanceof ScoreStuStatisticsRankMk) {
					ScoreStuStatisticsRankMk ggg = (ScoreStuStatisticsRankMk) scoreList.get(i);
					if (orderKey.equals("bzbjpm")) {
						ggg.setBzbjpm(1);
					}
					if (orderKey.equals("bznjpm")) {
						ggg.setBznjpm(1);
					}
				} else if (gg instanceof ScoreStuStatisticsRank) {
					ScoreStuStatisticsRank ggg = (ScoreStuStatisticsRank) scoreList.get(i);
					if (orderKey.equals("bzbjpm")) {
						ggg.setBzbjpm(1);
					}
					if (orderKey.equals("bznjpm")) {
						ggg.setBznjpm(1);
					}
				}
			}
		}
		return scoreList;
	}

	/**
	 * 获取单科成绩等级
	 * 
	 * @param score
	 * @param ddList
	 * @return
	 */
	public static String getSingleLevByScore(float score, List<JSONObject> ddList) {
		// TODO Auto-generated method stub
		for (int i = 0; i < ddList.size(); i++) {
			float r = ddList.get(i).getFloatValue("zscj");
			if (score >= r) {
				return ddList.get(i).getString("djmc");
			}
		}
		return null;
	}

	/**
	 * 
	 * @param stuScoreList
	 *            学生科目等级列表 需含djmc字段
	 * @param djsx
	 *            0A0B0C0D.....
	 * @param djxllb
	 *            等级序列类别
	 * @return
	 */
	public static String getAllScoreLevByScore(List<JSONObject> stuScoreList, String djsx, String lb, String allStr) {
		// TODO Auto-generated method stub
		for (JSONObject o : stuScoreList) {
			String kmdm = o.getString("kmdm");
			int isHb = o.getIntValue("isHb");
			String djmc = o.getString("djmc");
			if (djmc != null && djmc.trim().length() > 0) {

				if (lb.equalsIgnoreCase("01")) {
					if (isHb == 0) {
						// 一般科目
						int ix = djsx.indexOf(djmc);
						if (ix > 0) {
							int num = Integer.parseInt(djsx.substring(ix - 1, ix));
							num = num + 1;
							djsx = djsx.substring(0, ix - 1) + num + djsx.substring(ix, djsx.length());
						}
					}
				} else if (lb.equalsIgnoreCase("02")) {
					if (isHb == 1 || allStr.indexOf("," + kmdm + ",") == -1) {
						// 合并科目
						int ix = djsx.indexOf(djmc);
						if (ix > 0) {
							int num = Integer.parseInt(djsx.substring(ix - 1, ix));
							num = num + 1;
							djsx = djsx.substring(0, ix - 1) + num + djsx.substring(ix, djsx.length());
						}
					}
				} else if (lb.equalsIgnoreCase("03")) {
					if (kmdm.equalsIgnoreCase("1") || kmdm.equalsIgnoreCase("2") || kmdm.equalsIgnoreCase("3")) {
						// 语数外
						int ix = djsx.indexOf(djmc);
						if (ix > 0) {
							int num = Integer.parseInt(djsx.substring(ix - 1, ix));
							num = num + 1;
							djsx = djsx.substring(0, ix - 1) + num + djsx.substring(ix, djsx.length());
						}
					}
				}
			}
		}
		for (int i = 0; i < djsx.length(); i += 2) {
			int num = Integer.parseInt(djsx.substring(i, i + 1));
			if (num == 0) {
				djsx = djsx.substring(0, i) + djsx.substring(i + 2, djsx.length());
				i -= 2;
			}
		}
		return djsx;
	}

	/**
	 * 设置是否参与统计
	 * 
	 * @param stuScoreList
	 * @param qkbcy
	 *            缺考不参与统计设置
	 * @param wbbcy
	 *            舞弊不参与统计设置
	 * @param lfbcy
	 *            零分不参与统计设置
	 * @param isKmStuInJs
	 * @return
	 */
	public static List<JSONObject> setSftjList(List<JSONObject> stuScoreList, int qkbcy, int wbbcy, int lfbcy,
			Map<String, Boolean> isKmStuInJs) {
		// TODO Auto-generated method stub
		HashMap<String, Boolean> xhKm_listMap = new HashMap<String, Boolean>();
		// 对单科进行设置是否统计
		for (JSONObject o : stuScoreList) {
			int isHb = o.getIntValue("isHb");
			if (isHb == 0) {
				float cj = o.getFloatValue("cj");
				String tsqk = o.getString("tsqk");
				if (qkbcy == 1 && tsqk != null && tsqk.equalsIgnoreCase("02")) {
					o.put("sftj", false);
				}
				if (wbbcy == 1 && tsqk != null && tsqk.equalsIgnoreCase("01")) {
					o.put("sftj", false);
				}
				if (lfbcy == 1 && cj == 0.0) {
					o.put("sftj", false);
				}
				if (!o.containsKey("sftj")) {
					o.put("sftj", true);
				}
				String xh = o.getString("xh");
				String kmdm = o.getString("kmdm");
				// 标记学生是否为竞赛学生
				if (!isKmStuInJs.isEmpty() && isKmStuInJs.containsKey(xh + "_" + kmdm)) {
					o.put("sfjs", true);
				} else {
					o.put("sfjs", false);
				}
				xhKm_listMap.put(o.getString("xh") + "_" + o.getString("kmdm"), o.getBoolean("sftj"));
			}
		}
		// 对合并科目设置是否统计
		for (JSONObject o : stuScoreList) {
			int isHb = o.getIntValue("isHb");
			String xh = o.getString("xh");
			if (isHb == 1) {
				boolean sftj = true;
				String dykm = o.getString("dykm");
				if (dykm != null && dykm.trim().length() > 0) {
					String[] kms = dykm.split(",");
					for (int i = 0; i < kms.length; i++) {
						boolean dk = false;
						if (xhKm_listMap.containsKey(xh + "_" + kms[i])) {
							dk = xhKm_listMap.get(xh + "_" + kms[i]);
							if (dk == false) {
								sftj = false;
								break;
							}
						}
					}
				} else {
					sftj = false;
				}
				o.put("sftj", sftj);
			}
		}
		return stuScoreList;
	}

	/**
	 * 获取总分是否统计设置
	 * 
	 * @param singleStuScoreList
	 * @return
	 */
	public static boolean getZfsftj(List<JSONObject> singleStuScoreList) {
		// TODO Auto-generated method stub
		for (JSONObject o : singleStuScoreList) {
			if (!o.getBooleanValue("sftj")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 设置学生是否参与范围统计
	 * 
	 * @param scoreList
	 *            成绩列表
	 * @param fwszList
	 *            范围设置
	 * @param fztype
	 *            范围设置方式 1为文理 2为班级
	 * @param xsHbcjMap
	 * @param wlfzHbkmList
	 * @return
	 */
	public static List<JSONObject> setFwtjList(List<JSONObject> scoreList, List<JSONObject> fwszList, int fztype,
			Map<String, List<JSONObject>> xsHbcjMap, Map<String, List<JSONObject>> wlfzHbkmList) {
		// TODO Auto-generated method stub
		// 按文理范围设置
		if (fztype == 1) {
			JSONObject fzsz = fwszList.get(0);
			int fs = fzsz.getIntValue("fs");
			int fw = fzsz.getIntValue("fw");
			int inx = 0;
			if (fs == 1) {
				inx = fw;
			} else if (fs == 2) {
				inx = scoreList.size() * fw / 100;
			}
			for (int i = 0; i < scoreList.size(); i++) {
				int njzfpm = scoreList.get(i).getIntValue("njzfpm");
				String xh = scoreList.get(i).getString("xh");
				if (njzfpm <= inx) {
					scoreList.get(i).put("fwtj", true);
				} else {
					scoreList.get(i).put("fwtj", false);
				}

				boolean fwtj = scoreList.get(i).getBooleanValue("fwtj");
				List<JSONObject> dkList = xsHbcjMap.get(xh);
				for (JSONObject dk : dkList) {
					dk.put("fwtj", fwtj);
				}

				List<JSONObject> hbList = wlfzHbkmList.get(xh);
				if (hbList != null && hbList.size() > 0) {

					for (JSONObject dk : hbList) {
						dk.put("fwtj", fwtj);
					}
				}

			}
		} else {

			HashMap<String, JSONObject> bhszMap = new HashMap<String, JSONObject>();
			// scoreList许对
			// 总部分数过滤器
			for (JSONObject o : scoreList) {

				String bh = o.getString("bh");
				JSONObject bjsz = new JSONObject();
				if (bhszMap.containsKey("bhszMap")) {
					bjsz = bhszMap.get(bh);
				} else {
					List<JSONObject> sz = GrepUtil.grepJsonKeyByVal(new String[] { "bh" }, new String[] { bh },
							fwszList);
					if (sz.size() > 0) {
						bjsz = sz.get(0);
					} else {
						bjsz.put("fw", 0);
					}
				}
				int fw = bjsz.getIntValue("fw");
				int bjzfpm = o.getIntValue("bjzfpm");
				if (fw != 0) {
					if (bjzfpm <= fw) {
						o.put("fwtj", true);
					}
				} else {
					o.put("fwtj", true);
				}

				boolean fwtj = o.getBooleanValue("fwtj");
				List<JSONObject> dkList = xsHbcjMap.get(o.get("xh"));
				for (JSONObject dk : dkList) {
					dk.put("fwtj", fwtj);
				}

				List<JSONObject> hbList = wlfzHbkmList.get(o.get("xh"));
				if (hbList != null && hbList.size() > 0) {

					for (JSONObject dk : hbList) {
						dk.put("fwtj", fwtj);
					}
				}
			}
		}
		return scoreList;
	}

	/**
	 * 获取学生是否为优秀、合格、低分、尖子生、潜能生设置
	 * 
	 * @param scoreList
	 *            成绩列表
	 * @param pmszList
	 *            参数设置
	 * @return
	 */
	public static List<JSONObject> setYxtjList(List<JSONObject> scoreList, List<JSONObject> pmszList, int zf) {
		// TODO Auto-generated method stub
		for (JSONObject sz : pmszList) {
			String dm = sz.getString("dm");
			String mdm = "";
			if (dm.equalsIgnoreCase("01")) {
				mdm = "Yx";
			} else if (dm.equalsIgnoreCase("02")) {
				mdm = "Hg";
			} else if (dm.equalsIgnoreCase("03")) {
				mdm = "Df";
			} else if (dm.equalsIgnoreCase("04")) {
				mdm = "Jz";
			} else if (dm.equalsIgnoreCase("05")) {
				mdm = "Qn";
			}
			sz.put("mdm", mdm);
		}
		int fs = pmszList.get(0).getIntValue("fs");
		List<JSONObject> scoreList1 = GrepUtil.grepJsonKeyBySingleVal("sftj", "true", scoreList);
		for (JSONObject sz : pmszList) {
			int inx = 0;
			String szdm = sz.getString("dm");
			String mdm = sz.getString("mdm");
			// 方式一 分析是否超过分数线
			if (fs == 1) {
				inx = zf * sz.getIntValue("fzbfb") / 100;
				for (JSONObject o : scoreList) {
					float zcj = o.getFloatValue("zcj");
					// 是否优秀、合格、尖子
					if (szdm.equalsIgnoreCase("01") || szdm.equalsIgnoreCase("02") || szdm.equalsIgnoreCase("04")) {
						if (zcj >= inx) {
							o.put("is" + mdm, true);
						} else {
							o.put("is" + mdm, false);
						}
					} else {
						if (zcj < inx) {
							o.put("is" + mdm, true);
						} else {
							o.put("is" + mdm, false);
						}
					}
				}
			} else if (fs == 2) {
				// 方式二 按排名百分比计算

				inx = scoreList1.size() * sz.getIntValue("fzbfb") / 100;

				int rInx = scoreList1.size() - inx;

				for (JSONObject o : scoreList) {
					// 总分排名
					int zcj = o.getIntValue("njzfpm");
					if (szdm.equalsIgnoreCase("01") || szdm.equalsIgnoreCase("02") || szdm.equalsIgnoreCase("04")) {
						if (zcj <= inx) {
							o.put("is" + mdm, true);
						} else {
							o.put("is" + mdm, false);
						}
					} else {
						if (zcj > rInx) {
							o.put("is" + mdm, true);
						} else {
							o.put("is" + mdm, false);
						}
					}
				}
			}
		}
		return scoreList;
	}

	/**
	 * 获取学生科目成绩是否为优秀、合格、低分、尖子生、潜能生设置
	 * 
	 * @param scoreList
	 *            成绩列表
	 * @param pmszList
	 *            参数设置
	 * @return
	 */
	public static List<JSONObject> setYxtjKmList(List<JSONObject> scoreList, List<JSONObject> pmszList, int zf) {
		// TODO Auto-generated method stub
		for (JSONObject sz : pmszList) {
			String dm = sz.getString("dm");
			String mdm = "";
			if (dm.equalsIgnoreCase("01")) {
				mdm = "Yx";
			} else if (dm.equalsIgnoreCase("02")) {
				mdm = "Hg";
			} else if (dm.equalsIgnoreCase("03")) {
				mdm = "Df";
			} else if (dm.equalsIgnoreCase("04")) {
				mdm = "Jz";
			} else if (dm.equalsIgnoreCase("05")) {
				mdm = "Qn";
			}
			sz.put("mdm", mdm);
		}
		int fs = pmszList.get(0).getIntValue("fs");
		List<JSONObject> scoreList1 = GrepUtil.grepJsonKeyBySingleVal("sftj", "true", scoreList);
		for (JSONObject sz : pmszList) {
			int inx = 0;
			String szdm = sz.getString("dm");
			String mdm = sz.getString("mdm");
			// 方式一 分析是否超过分数线
			if (fs == 1) {
				inx = zf * sz.getIntValue("fzbfb") / 100;
				for (JSONObject o : scoreList) {
					float zcj = o.getFloatValue("cj");
					// 是否优秀、合格、尖子
					if (szdm.equalsIgnoreCase("01") || szdm.equalsIgnoreCase("02") || szdm.equalsIgnoreCase("04")) {
						if (zcj >= inx) {
							o.put("is" + mdm, true);
						} else {
							o.put("is" + mdm, false);
						}
					} else {
						if (zcj < inx) {
							o.put("is" + mdm, true);
						} else {
							o.put("is" + mdm, false);
						}
					}
				}
			} else if (fs == 2) {
				// 方式二 按排名百分比计算
				inx = scoreList1.size() * sz.getIntValue("fzbfb") / 100;

				int rInx = scoreList1.size() - inx;
				for (JSONObject o : scoreList) {
					// 单科排名
					int zcj = o.getIntValue("njkmpm");
					if (szdm.equalsIgnoreCase("01") || szdm.equalsIgnoreCase("02") || szdm.equalsIgnoreCase("04")) {
						if (zcj <= inx) {
							o.put("is" + mdm, true);
						} else {
							o.put("is" + mdm, false);
						}
					} else {
						if (zcj >= rInx) {
							o.put("is" + mdm, true);
						} else {
							o.put("is" + mdm, false);
						}
					}
				}
			}
		}
		return scoreList;
	}

	/**
	 * 获取班级平均分
	 * 
	 * @param scoreList
	 *            成绩列表
	 * @param cjKey
	 *            成绩键名
	 * @return
	 */
	public static float getPjf(List<JSONObject> scoreList, String cjKey) {
		// TODO Auto-generated method stub
		float sum = 0;
		for (JSONObject o : scoreList) {
			if (o.containsKey(cjKey)) {
				sum += o.getFloatValue(cjKey);
			}
		}
		float pjf = castFloatTowPointNum(sum / scoreList.size());
		return pjf;
	}

	/**
	 * 获取标准方差
	 * 
	 * @param left
	 *            循环值
	 * @param right
	 *            固定值
	 * @return
	 */
	public static float getDtSq(List<Float> left, float right) {
		// TODO Auto-generated method stub
		float fm = left.size();
		float fz = 0;
		for (Float l : left) {
			fz = fz + (l - right) * (l - right);
		}
		float rs = castFloatTowPointNum((float) Math.sqrt(fz / fm));
		return rs;
	}

	/**
	 * 获取分组标准方差
	 * 
	 * @param left
	 * @param key
	 * @param right
	 * @return
	 */
	public static float getFzDtSq(List<JSONObject> left, String key, float right) {
		float fm = left.size();
		float fz = 0;
		for (JSONObject o : left) {
			float l = o.getFloatValue(key);
			fz = fz + (l - right) * (l - right);
		}
		float rs = castFloatTowPointNum((float) Math.sqrt(fz / fm));
		return rs;
	}

	/**
	 * 获取优秀率、合格率、低分率...等
	 * 
	 * @param scoreList
	 *            成绩列表
	 * @param key
	 *            Yx,Hg,Df,...
	 * @return
	 */
	public static JSONObject getDjPercent(List<JSONObject> scoreList, String key) {
		// TODO Auto-generated method stub
		List<JSONObject> djList = new ArrayList<JSONObject>();
		key = "is" + key;
		float num = 0;
		for (JSONObject o : scoreList) {
			if (o.containsKey(key) && o.getBooleanValue(key)) {
				num++;
				djList.add(o);
			}
		}
		JSONObject rs = new JSONObject();
		rs.put("rs", num);
		rs.put("list", djList);
		float bl = (float) num / scoreList.size();
		rs.put("bl", castFloatTowPointNum(bl * 100));
		return rs;
	}

	/**
	 * 获取率差
	 * 
	 * @param list
	 * @return
	 */
	public float getDjPertcentDt(List<Float> list) {
		// TODO Auto-generated method stub
		float min = list.get(0);
		float max = list.get(0);
		for (float f : list) {
			if (f > max) {
				max = f;
			}
			if (f < min) {
				min = f;
			}
		}
		return castFloatTowPointNum(max - min);
	}

	/**
	 * 难度系数
	 * 
	 * @param pjf
	 *            平均分
	 * @param mf
	 *            满分
	 * @return
	 */
	public static float dificultPoint(float pjf, int mf) {
		// TODO Auto-generated method stub
		return castFloatTowPointNum(pjf / mf);
	}

	/**
	 * 将已按成绩大小正序排好顺序的列表计算区分度
	 * 
	 * @param scoreList
	 * @param cjKey
	 * @param mf
	 * @return
	 */
	public static float diffrentRate(List<JSONObject> scoreList, String cjKey, int mf) {
		// TODO Auto-generated method stub
		int all = scoreList.size();
		int inx1 = all * 27 / 100;
		List<JSONObject> l1 = scoreList.subList(0, inx1);
		List<JSONObject> l2 = scoreList.subList(all - inx1, all);
		float p1 = getPjf(l1, cjKey);
		float p2 = getPjf(l2, cjKey);
		float rs = (p1 - p2) / mf;
		return castFloatTowPointNum(rs);
	}

	/**
	 * 根据成绩列表获取成绩分布人数
	 * 
	 * @param bjtjcjList
	 * @param fbsx
	 * @param fbxx
	 * @param cjKey
	 * @return
	 */
	public static int sumNumFromScore(List<JSONObject> bjtjcjList, int fbsx, int fbxx, String cjKey) {
		// TODO Auto-generated method stub
		int rs = 0;
		for (JSONObject xs : bjtjcjList) {
			float cj = xs.getFloatValue(cjKey);
			if (cj >= (float) fbxx && cj < fbsx) {
				rs++;
			}
		}
		return rs;
	}

	/**
	 * 根据成绩列表获取成绩分布人数
	 * 
	 * @param bjtjcjList
	 * @param fbsx
	 * @param fbxx
	 * @param cjKey
	 * @return
	 */
	public static int sumNumFromScore2(List<JSONObject> bjtjcjList, int fbsx, int fbxx, String cjKey) {
		// TODO Auto-generated method stub
		int rs = 0;
		if (fbxx == 1) {

			for (JSONObject xs : bjtjcjList) {
				float cj = xs.getFloatValue(cjKey);
				if (cj >= (float) fbxx && cj <= fbsx) {
					rs++;
				}
			}
		} else {
			for (JSONObject xs : bjtjcjList) {
				float cj = xs.getFloatValue(cjKey);
				if (cj > (float) fbxx && cj <= fbsx) {
					rs++;
				}
			}
		}
		return rs;
	}

	/**
	 * 获取下限之上的人数
	 * 
	 * @param bjtjcjList
	 * @param fbsx
	 * @param fbxx
	 * @param cjKey
	 * @return
	 */
	public static int sumLjNumFromScore(List<JSONObject> bjtjcjList, int fbsx, int fbxx, String cjKey) {
		// TODO Auto-generated method stub
		int rs = 0;
		for (JSONObject xs : bjtjcjList) {
			float cj = xs.getFloatValue(cjKey);
			if (cj >= (float) fbxx) {
				rs++;
			}
		}
		return rs;
	}

	/**
	 * 获取分组差值 最大-最小
	 * 
	 * @param bjZfDataList1
	 *            分组数据
	 * @param string
	 *            分组差值依据
	 * @return
	 */
	public static float getFzCz(List<JSONObject> bjZfDataList1, String string) {
		// TODO Auto-generated method stub
		float max = bjZfDataList1.get(0).getFloatValue(string);
		float min = max;
		for (int i = 1; i < bjZfDataList1.size(); i++) {
			float val = bjZfDataList1.get(i).getFloatValue(string);
			if (val >= max) {
				max = val;
			}
			if (min > val) {
				min = val;
			}
		}
		return castFloatTowPointNum(max - min);
	}

	/**
	 * 获取合并科目成绩
	 * 
	 * @param wlkmcj
	 * @param dykm
	 * @param xh
	 * @return
	 */
	public static float getHbkmCjByList(List<JSONObject> wlkmcj, String dykm, String xh) {
		// TODO Auto-generated method stub
		float cj = 0;
		String[] dyks = dykm.split(",");
		for (JSONObject o : wlkmcj) {
			String kmdm = o.getString("kmdm");
			String sxh = o.getString("xh");
			float scj = o.getFloatValue("cj");
			if (sxh.equalsIgnoreCase(xh) && isInArr(kmdm, dyks)) {
				cj += scj;
			}
		}
		return cj;
	}

	private static boolean isInArr(String kmdm, String[] dyks) {
		// TODO Auto-generated method stub
		for (int i = 0; i < dyks.length; i++) {
			if (dyks[i].equalsIgnoreCase(kmdm)) {
				return true;
			}
		}
		return false;
	}

	public static String getTopNPmInList(List<JSONObject> scoreList, String key, int n, String rsKey, int type) {
		// TODO Auto-generated method stub
		String rs = "";
		int i = 0;
		while (i < scoreList.size() && scoreList.get(i).getIntValue(key) <= n) {
			if (scoreList.get(i).containsKey(rsKey)) {
				if (type == 0) {
					rs += scoreList.get(i).getString(rsKey) + "班、";
				} else if (type == 1) {
					rs += scoreList.get(i).getString(rsKey) + "班" + scoreList.get(i).getString("fsdrs") + "人、";
				}
			}
			i++;
		}
		if (rs.length() > 0) {
			rs = rs.substring(0, rs.length() - 1);
		}
		return rs;
	}

	/**
	 * 学业报告 等第部分 班级等第提示语
	 * 
	 * @param wlzcjNjpm
	 *            总成绩列表
	 * @param bjList
	 * @param kmNum
	 * @param needDdByMap
	 * @return
	 */
	public static String getRsDdQk(List<JSONObject> wlzcjNjpm, List<JSONObject> bjList, int kmNum,
			Map<String, Boolean> needDdByMap) {
		int kmSize = kmNum;
		// TODO Auto-generated method stub
		String classId = wlzcjNjpm.get(0).getString("bh");
		if (!needDdByMap.get(classId)) {
			return null;
		}
		List<JSONObject> ddRs = GrepUtil.grepJsonKeyBySingleVal("djxl", kmSize + "A", wlzcjNjpm);
		List<JSONObject> ddRs2 = new ArrayList<JSONObject>();
		// while(ddRs.size()==0&&kmSize>0){
		// kmSize--;
		// ddRs = GrepUtil.grepJsonKeyBySingleVal("ddxl", kmSize+"A",
		// wlzcjNjpm);
		// }
		String rs = "班级等第人数情况方面，";
		rs += kmSize + "A人数全年级总共" + ddRs.size() + "人。";
		for (JSONObject o : bjList) {
			String bh = o.getString("bh");
			List<JSONObject> bjDdRs = GrepUtil.grepJsonKeyByVal(new String[] { "djxl", "bh" },
					new String[] { kmSize + "A", bh }, wlzcjNjpm);
			o.put(kmSize + "ARs", bjDdRs.size());
		}
		bjList = ScoreUtil.sorStuScoreList(bjList, kmSize + "ARs", "desc", "", kmSize + "ARs");
		rs += "人数最多的是" + bjList.get(0).getString("bjmc") + "班" + bjList.get(0).getString(kmSize + "ARs") + "人";
		for (int i = 1; i < bjList.size(); i++) {
			if (bjList.get(i).getInteger(kmSize + "ARs") == bjList.get(0).getInteger(kmSize + "ARs")) {
				rs += "、" + bjList.get(i).getString("bjmc") + "班" + bjList.get(i).getString(kmSize + "ARs") + "人";
			} else {
				break;
			}
		}
		rs += "；人数最少的为" + bjList.get(bjList.size() - 1).getString(kmSize + "ARs") + "人。";

		// for(int i=bjList.size()-2;i<bjList.size()-1;i++){
		// if(bjList.get(i).getInteger(kmSize+"ARs")==bjList.get(bjList.size()-1).getInteger(kmSize+"ARs")){
		// rs+=
		// bjList.get(i).getString("bjmc")+"班"+bjList.get(i).getString(kmSize+"ARs")+"人、";
		// }else{
		// break;
		// }
		// }
		// rs= rs.substring(0,rs.length()-1);
		// if(kmSize >0){
		String dd = (kmSize - 1) + "A1B";
		ddRs2 = GrepUtil.grepJsonKeyBySingleVal("djxl", dd, wlzcjNjpm);

		for (JSONObject o : bjList) {
			String bh = o.getString("bh");
			List<JSONObject> bjDdRs = GrepUtil.grepJsonKeyByVal(new String[] { "djxl", "bh" }, new String[] { dd, bh },
					wlzcjNjpm);
			o.put(dd + "Rs", bjDdRs.size());
		}
		bjList = ScoreUtil.sorStuScoreList(bjList, dd + "Rs", "desc", "", dd + "Rs");

		rs += dd + "共" + ddRs2.size() + "人，其中人数最多的是" + bjList.get(0).getString("bjmc") + "班"
				+ bjList.get(0).getString(dd + "Rs") + "人";
		for (int i = 1; i < bjList.size(); i++) {
			if (bjList.get(i).getInteger(kmSize + "ARs") == bjList.get(0).getInteger(kmSize + "ARs")) {
				rs += "、" + bjList.get(i).getString("bjmc") + "班" + bjList.get(i).getString(kmSize + "ARs") + "人";
			} else {
				break;
			}
		}
		rs += "。";
		// }
		int a1 = ddRs.size() + ddRs2.size();
		rs += "两项加起来，总共" + a1 + "人,占全年级总人数的" + ScoreUtil.castFloatTowPointNum(100f * a1 / wlzcjNjpm.size()) + "%。";

		List<JSONObject> lddRs = GrepUtil.grepJsonKeyBySingleVal("djxl", kmNum + "E", wlzcjNjpm);
		// while(lddRs.size()==0&&kmNum>0){
		// kmNum--;
		// lddRs = GrepUtil.grepJsonKeyBySingleVal("ddxl", kmNum+"E",
		// wlzcjNjpm);
		// }
		// if(lddRs.size()>0){

		dd = kmNum + "E";
		ddRs2 = GrepUtil.grepJsonKeyBySingleVal("djxl", dd, wlzcjNjpm);

		for (JSONObject o : bjList) {
			String bh = o.getString("bh");
			List<JSONObject> bjDdRs = GrepUtil.grepJsonKeyByVal(new String[] { "djxl", "bh" }, new String[] { dd, bh },
					wlzcjNjpm);
			o.put(dd + "Rs", bjDdRs.size());
		}
		bjList = ScoreUtil.sorStuScoreList(bjList, dd + "Rs", "desc", "", "");

		rs += dd + "人数全年级总共" + ddRs2.size() + "人，其中人数最多的班有"
				+ (bjList.get(0).getString(dd + "Rs") == null ? "0" : bjList.get(0).getString(dd + "Rs")) + "个";
		// }

		return rs;
	}

	public static String getFsdQk(List<JSONObject> wlzcjNjpm, List<JSONObject> bjList, int pm) {
		// TODO Auto-generated method stub
		if (wlzcjNjpm.size() == 0 || wlzcjNjpm.size() == 1) {
			return "";
		}
		int inx = pm - 1;
		if (wlzcjNjpm.size() < pm) {
			inx = wlzcjNjpm.size() - 1;
		}
		int njpm = wlzcjNjpm.get(inx).getIntValue("njzfpm");
		while (inx < wlzcjNjpm.size() - 1 && njpm <= pm) {
			inx++;
			njpm = wlzcjNjpm.get(inx).getIntValue("njzfpm");
		}
		// 获取到一名后大于截至排名 则取上一个不大于的
		// inx --;
		List<JSONObject> rl = wlzcjNjpm.subList(0, inx);
		Float sfz = rl.get(rl.size() - 1).getFloatValue("zcj");
		String rs = "分数段人数方面，" + sfz + "分以上，即全校前" + rl.size() + "名，";

		for (JSONObject o : bjList) {

			String bh = o.getString("bh");
			int r = GrepUtil.grepJsonKeyBySingleVal("bh", bh, rl).size();
			o.put("fsdrs", r);
		}

		List<JSONObject> list = ScoreUtil.sorStuScoreList(bjList, "fsdrs", "desc", "nj", "fsdrs");
		String r1 = getTopNPmInList(list, "njfsdrspm", 3, "bjmc", 1);
		rs += "人数最多的是" + r1 + "，人数最少的有" + list.get(list.size() - 1).getIntValue("fsdrs") + "人。";
		return rs;
	}

	public static JSONObject getDbbByList(List<JSONObject> bjList, String[] keys, String[] keyName) {
		Collections.sort(bjList, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String bjmc1 = o1.getString("bjmc");
				if (bjmc1 == null) {
					bjmc1 = "";
				}
				String bjmc2 = o2.getString("bjmc");
				if (bjmc2 == null) {
					bjmc2 = "";
				}
				return bjmc1.compareTo(bjmc2);
			}
		});

		JSONObject obj = new JSONObject();
		String[][] values = new String[keyName.length][bjList.size()];
		JSONArray series = new JSONArray();
		String[] xAxis = new String[bjList.size()];
		for (int j = 0; j < bjList.size(); j++) {
			JSONObject bj = bjList.get(j);
			xAxis[j] = bj.getString("bjmc");
			for (int i = 0; i < keys.length; i++) {
				values[i][j] = bj.getString(keys[i]);
			}
		}

		for (int i = 0; i < keys.length; i++) {
			JSONObject b = new JSONObject();
			b.put("name", keyName[i]);
			b.put("data", changeDataType(values[i]));

			series.add(b);
		}
		obj.put("xAxis", xAxis);
		obj.put("series", series);

		return obj;
	}

	private static Float[] changeDataType(String[] strs) {
		// TODO Auto-generated method stub
		Float[] rs = new Float[strs.length];
		for (int i = 0; i < strs.length; i++) {
			if (strs[i] != null) {

				rs[i] = castFloatTowPointNum(Float.parseFloat(strs[i]));
			} else {
				rs[i] = (float) 0;
			}
		}
		return rs;
	}

	/**
	 * 获取等第值对比表数据
	 * 
	 * @param wlzcjNjpm
	 *            总成绩年级排名（含等第）
	 * @param bjList
	 *            班级列表
	 * @param kmSize
	 *            科目数量
	 * @param needDdByMap
	 * @return
	 */
	public static JSONObject getDddbbByList(List<JSONObject> wlzcjNjpm, List<JSONObject> bjList, int kmSize,
			Map<String, Boolean> needDdByMap) {
		Collections.sort(bjList, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String bjmc1 = o1.getString("bjmc");
				if (bjmc1 == null) {
					bjmc1 = "";
				}
				String bjmc2 = o2.getString("bjmc");
				if (bjmc2 == null) {
					bjmc2 = "";
				}
				return bjmc1.compareTo(bjmc2);
			}
		});

		String classId = wlzcjNjpm.get(0).getString("bh");
		if (!needDdByMap.get(classId)) {
			JSONObject rs = new JSONObject();
			return rs;
		}
		String mdd = kmSize + "A";
		String ldd = kmSize - 1 + "A1B";
		String tdd = kmSize + "E";
		int data[][] = new int[3][bjList.size()];
		String[] xAxis = new String[bjList.size()];

		for (int i = 0; i < bjList.size(); i++) {
			JSONObject bj = bjList.get(i);
			String bh = bj.getString("bh");
			xAxis[i] = bj.getString("bjmc");
			List<JSONObject> bjxs = GrepUtil.grepJsonKeyBySingleVal("bh", bh, wlzcjNjpm);
			data[0][i] = GrepUtil.grepJsonKeyBySingleVal("djxl", mdd, bjxs).size();
			data[1][i] = GrepUtil.grepJsonKeyBySingleVal("djxl", ldd, bjxs).size();
			data[2][i] = GrepUtil.grepJsonKeyBySingleVal("djxl", tdd, bjxs).size();
		}

		JSONObject rs = new JSONObject();
		rs.put("xAxis", xAxis);
		JSONArray series = new JSONArray();
		rs.put("series", series);

		JSONObject o1 = new JSONObject();
		o1.put("name", mdd);
		o1.put("data", data[0]);
		series.add(o1);

		o1 = new JSONObject();
		o1.put("name", ldd);
		o1.put("data", data[1]);
		series.add(o1);

		o1 = new JSONObject();
		o1.put("name", tdd);
		o1.put("data", data[2]);
		series.add(o1);

		return rs;
	}

	/**
	 * 获取排名对比表 前200名 及后100
	 * 
	 * @param wlzcjNjpm
	 *            成绩排名
	 * @param bjList
	 *            班级列表
	 * @return
	 */
	public static JSONObject getPmdbbByList(List<JSONObject> wlzcjNjpm, List<JSONObject> bjList) {
		Collections.sort(bjList, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String bjmc1 = o1.getString("bjmc");
				if (bjmc1 == null) {
					bjmc1 = "";
				}
				String bjmc2 = o2.getString("bjmc");
				if (bjmc2 == null) {
					bjmc2 = "";
				}
				return bjmc1.compareTo(bjmc2);
			}
		});

		int idx = wlzcjNjpm.size() > 101 ? wlzcjNjpm.size() - 101 : 0;
		int last = wlzcjNjpm.get(idx).getIntValue("njzfpm");

		int data[][] = new int[2][bjList.size()];
		String[] xAxis = new String[bjList.size()];

		for (int i = 0; i < bjList.size(); i++) {
			JSONObject bj = bjList.get(i);
			String bh = bj.getString("bh");
			xAxis[i] = bj.getString("bjmc");
			List<JSONObject> bjxs = GrepUtil.grepJsonKeyBySingleVal("bh", bh, wlzcjNjpm);
			int n1 = 0;
			int n2 = 0;
			for (JSONObject xs : bjxs) {
				int pm = xs.getIntValue("njzfpm");
				if (pm <= 200) {
					n1++;
				}
			}
			for (int j = bjxs.size() - 1; j >= 0; j--) {
				int pm = bjxs.get(j).getIntValue("njzfpm");
				if (pm > last) {
					n2++;
				}
			}
			data[0][i] = n1;
			data[1][i] = n2;
		}

		JSONObject rs = new JSONObject();
		rs.put("xAxis", xAxis);
		JSONArray series = new JSONArray();
		rs.put("series", series);

		JSONObject o1 = new JSONObject();
		o1.put("name", "前200名共计");
		o1.put("data", data[0]);
		series.add(o1);

		o1 = new JSONObject();
		o1.put("name", "后100名共计");
		o1.put("data", data[1]);
		series.add(o1);
		return rs;
	}

	/**
	 * 根据纬度获取提示语
	 * 
	 * @param string
	 *            提示语依据键
	 * @param bjKmDataList1
	 *            班级列表
	 * @return
	 */
	public static String getXkTsyByList(String key, List<JSONObject> bjKmDataList1) {
		// TODO Auto-generated method stub
		String rs = "最高的班级是";
		float v = bjKmDataList1.get(0).getFloatValue(key);
		rs += bjKmDataList1.get(0).getString("bjmc") + "班";
		int i = 1;
		while (bjKmDataList1.size() > i && bjKmDataList1.get(i).getFloatValue(key) == v) {
			rs += "、" + bjKmDataList1.get(i).getString("bjmc") + "班";
			i++;
		}

		float min = bjKmDataList1.get(bjKmDataList1.size() - 1).getFloatValue(key);
		String sub = "";
		String sub2 = "";
		if (key.equalsIgnoreCase("avgKmScr")) {
			sub = "分";
			if (v - min > 5) {
				sub2 = "，超过5分";
			}
		}
		rs += "，最高" + sub + "与最低" + sub + "相差" + castFloatTowPointNum(v - min) + sub + sub2 + "。";
		return rs;
	}

	/**
	 * 获取班级A等人数提示语
	 * 
	 * @param bjKmDataList1
	 * @param wlkmcjNjpm
	 * @param kmdm
	 * @param needDdByMap
	 * @return
	 */
	public static String getArsTysByList(List<JSONObject> bjKmDataList1, List<JSONObject> wlkmcjNjpm, String kmdm,
			Map<String, Boolean> needDdByMap) {
		String classId = wlkmcjNjpm.get(0).getString("bh");
		if (!needDdByMap.get(classId)) {
			return "";
		}
		// TODO Auto-generated method stub
		List<JSONObject> list = GrepUtil.grepJsonKeyByVal(new String[] { "kmdm", "djmc" }, new String[] { kmdm, "A" },
				wlkmcjNjpm);
		for (JSONObject bj : bjKmDataList1) {
			String bh = bj.getString("bh");
			List<JSONObject> l1 = GrepUtil.grepJsonKeyBySingleVal("bh", bh, list);
			bj.put("Ars", l1.size());
		}

		bjKmDataList1 = sorStuScoreList(bjKmDataList1, "Ars", "desc", "km", "Ars");
		int max = bjKmDataList1.get(0).getIntValue("Ars");
		String rs = "A等人数最多的是" + bjKmDataList1.get(0).getString("bjmc") + "班";
		int i = 1;
		while (bjKmDataList1.size() > i && bjKmDataList1.get(i) != null
				&& bjKmDataList1.get(i).getInteger("Ars") == max) {
			rs += "、" + bjKmDataList1.get(i).getString("bjmc") + "班";
			i++;
		}
		int min = bjKmDataList1.get(bjKmDataList1.size() - 1).getIntValue("Ars");
		rs += max + "人，最多与最少之间相差" + (max - min) + "人。";
		return rs;
	}

	/**
	 * 获取学科对比表
	 * 
	 * @param needDdByMap
	 * 
	 * @param bjKmDataList1
	 * @return
	 */
	public static JSONObject getKmDbbByList(List<JSONObject> bjList, String kmmc, Map<String, Boolean> needDdByMap) {
		Collections.sort(bjList, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String bjmc1 = o1.getString("bjmc");
				if (bjmc1 == null) {
					bjmc1 = "";
				}
				String bjmc2 = o2.getString("bjmc");
				if (bjmc2 == null) {
					bjmc2 = "";
				}
				return bjmc1.compareTo(bjmc2);
			}
		});

		String judebh = bjList.get(0).getString("bh");
		boolean need = needDdByMap.get(judebh);
		JSONObject rs = new JSONObject();
		rs.put("title", kmmc);
		String[] xAxis = new String[bjList.size()];
		JSONArray series = new JSONArray();
		float[][] data = new float[3][bjList.size()];
		// if(!need){
		// data = new float[2][bjList.size()];
		// }
		int[] Ars = new int[bjList.size()];
		for (int i = 0; i < bjList.size(); i++) {
			JSONObject bj = bjList.get(i);
			xAxis[i] = bj.getString("bjmc");
			// 平均分
			data[0][i] = bj.getFloatValue("avgKmScr");
			// 优秀率
			data[1][i] = bj.getFloatValue("kmYxPer");
			// 合格率
			data[2][i] = bj.getFloatValue("kmHgPer");
			// A人数
			if (need) {

				Ars[i] = bj.getIntValue("Ars");
			}
		}

		JSONObject o = new JSONObject();
		o.put("name", "平均分");
		o.put("data", data[0]);
		series.add(o);

		o = new JSONObject();
		o.put("name", "优秀率");
		o.put("data", data[1]);
		series.add(o);
		o = new JSONObject();
		o.put("name", "合格率");
		o.put("data", data[2]);
		series.add(o);
		if (need) {

			o = new JSONObject();
			o.put("name", "A等人数");
			o.put("data", Ars);
			series.add(o);
		}

		rs.put("xAxis", xAxis);
		rs.put("series", series);
		return rs;
	}

	/**
	 * 获取教师-科目角度提示语
	 * 
	 * @param teacherClassListKM
	 * @param key
	 * @return
	 */
	public static String getJsKmTsyByList(List<JSONObject> teacherClassListKM, String key) {
		// TODO Auto-generated method stub
		String rs = "";
		if (key.equalsIgnoreCase("pjf")) {
			teacherClassListKM = sorStuScoreList(teacherClassListKM, "pjf", "desc", "km", "pjf");
			float max = castFloatTowPointNum(teacherClassListKM.get(0).getFloatValue("pjf"));
			float min = castFloatTowPointNum(
					teacherClassListKM.get(teacherClassListKM.size() - 1).getFloatValue("pjf"));
			String sub = "";
			if (max - min > 5) {
				sub = "，高过5分";
			}
			rs += "平均分均值最高" + max + "、最低" + min + "，分差" + castFloatTowPointNum(max - min) + "分" + sub + "。";
		} else if (key.equalsIgnoreCase("yxl")) {
			teacherClassListKM = sorStuScoreList(teacherClassListKM, "yxl", "desc", "km", "yxl");
			float max = castFloatTowPointNum(teacherClassListKM.get(0).getFloatValue("yxl"));
			float min = castFloatTowPointNum(
					teacherClassListKM.get(teacherClassListKM.size() - 1).getFloatValue("yxl"));
			rs += "优秀率均值最高" + max + "、最低" + min + "，相差" + castFloatTowPointNum(max - min) + "。";
		}
		return rs;
	}

	/**
	 * 获取教师学科对比表
	 * 
	 * @param teacherClassListKM
	 * @param kmmc
	 * @return
	 */
	public static JSONObject getJsKmDbByList(List<JSONObject> teacherClassListKM, String kmmc,
			Map<String, String> teacherMapDic) {
		// TODO Auto-generated method stub
		JSONObject rs = new JSONObject();
		rs.put("title", kmmc);
		HashMap<String, List<JSONObject>> jsKmCjMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject js : teacherClassListKM) {
			String zgh = js.getString("zgh");
			if (jsKmCjMap.containsKey(zgh)) {
				jsKmCjMap.get(zgh).add(js);

			} else {
				List<JSONObject> list = new ArrayList<JSONObject>();
				list.add(js);
				jsKmCjMap.put(zgh, list);
			}
		}
		int l = jsKmCjMap.keySet().size();
		String[] xAxis = new String[l];
		float[][] data = new float[2][l];

		int start = 0;
		for (Iterator<String> it = jsKmCjMap.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			List<JSONObject> cjList = jsKmCjMap.get(key);
			if (cjList.size() > 0) {
				xAxis[start] = teacherMapDic.get(key);
				float pjf = getPjf(cjList, "pjf");
				float yxl = getPjf(cjList, "yxl");
				data[0][start] = pjf;
				data[1][start] = yxl;
				start++;
			}
		}
		String[] xAxisN = new String[start];
		float[][] dataN = new float[2][start];
		for (int i = 0; i < start; i++) {
			xAxisN[i] = xAxis[i];
			dataN[0][i] = data[0][i];
			dataN[1][i] = data[1][i];
		}
		JSONArray series = new JSONArray();
		JSONObject o = new JSONObject();
		o.put("name", "平均分均值");
		o.put("data", data[0]);
		series.add(o);
		o = new JSONObject();
		o.put("name", "优秀率均值");
		o.put("data", data[1]);
		series.add(o);

		rs.put("xAxis", xAxisN);
		rs.put("series", series);
		return rs;
	}

	/**
	 * 获取班级报告的表头
	 * 
	 * @return
	 */
	public static JSONArray getBJReportDataGridTitle() {
		// TODO Auto-generated method stub
		JSONArray columns = new JSONArray();
		// 第一行
		JSONArray columns1 = new JSONArray();
		JSONObject col = new JSONObject();
		col.put("field", "item");
		col.put("title", "科目/项目");
		col.put("align", "center");
		col.put("width", 66);
		col.put("rowspan", 2);
		col.put("sortable", "false");
		columns1.add(col);
		col = new JSONObject();
		col.put("title", "平均分");
		col.put("align", "center");
		col.put("width", 66);
		col.put("colspan", 3);
		col.put("sortable", "false");
		columns1.add(col);

		col = new JSONObject();
		col.put("title", "优秀率");
		col.put("align", "center");
		col.put("width", 66);
		col.put("colspan", 3);
		col.put("sortable", "false");
		columns1.add(col);

		col = new JSONObject();
		col.put("title", "及格率");
		col.put("align", "center");
		col.put("width", 66);
		col.put("colspan", 3);
		col.put("sortable", "false");
		columns1.add(col);

		col = new JSONObject();
		col.put("field", "jzsrs");
		col.put("title", "尖子生人数");
		col.put("align", "center");
		col.put("width", 66);
		col.put("rowspan", 2);
		col.put("sortable", "false");
		columns1.add(col);

		col = new JSONObject();
		col.put("field", "qnsrs");
		col.put("title", "潜能生人数");
		col.put("align", "center");
		col.put("width", 66);
		col.put("rowspan", 2);
		col.put("sortable", "false");
		columns1.add(col);

		// 第二行
		JSONArray columns2 = new JSONArray();
		col = new JSONObject();
		col.put("field", "pjf");
		col.put("title", "平均分");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns2.add(col);

		col = new JSONObject();
		col.put("field", "pjfpm");
		col.put("title", "排名");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns2.add(col);

		col = new JSONObject();
		col.put("field", "pjffc");
		col.put("title", "平均分差");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns2.add(col);

		col = new JSONObject();
		col.put("field", "yxl");
		col.put("title", "优秀率");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns2.add(col);

		col = new JSONObject();
		col.put("field", "yxlpm");
		col.put("title", "排名");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns2.add(col);

		col = new JSONObject();
		col.put("field", "yxlfc");
		col.put("title", "优秀率差");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns2.add(col);

		col = new JSONObject();
		col.put("field", "hgl");
		col.put("title", "合格率");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns2.add(col);

		col = new JSONObject();
		col.put("field", "hglpm");
		col.put("title", "排名");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns2.add(col);

		col = new JSONObject();
		col.put("field", "hglfc");
		col.put("title", "合格率差");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns2.add(col);

		columns.add(columns1);
		columns.add(columns2);

		return columns;
	}

	/**
	 * 获取班级标准分跟踪折线图
	 * 
	 * @param nBzf
	 * @param kslcmc
	 * @param bjcjgzList
	 * @return
	 */
	public static JSONObject getBjBzfGzList(float nBzf, String kslcdm, String kslcmc, List<JSONObject> bjcjgzList) {
		// TODO Auto-generated method stub
		int l = bjcjgzList.size();
		String[] xAxis = new String[l + 1];
		float[] data = new float[l + 1];
		JSONArray datas = new JSONArray();
		JSONArray kslc = new JSONArray();
		JSONArray series = new JSONArray();
		int inx = 1;
		for (JSONObject bj : bjcjgzList) {
			inx++;
			xAxis[inx - 1] = "T" + inx;
			float bzf = bj.getFloatValue("bzf");
			data[inx - 1] = bzf;

			JSONObject ks = new JSONObject();
			ks.put("kslcdm", bj.getString("kslcdm"));
			ks.put("kslcmc", bj.getString("kslcmc"));
			ks.put("mc", xAxis[inx - 1]);
			kslc.add(ks);

			// 放数据
			JSONObject ydata = new JSONObject();
			ydata.put("name", "T" + inx + ":" + bj.getString("kslcmc"));
			ydata.put("y", bzf);

			datas.add(ydata);
		}
		// 本次考试标准分
		xAxis[0] = "T1";
		float bzf = nBzf;
		data[0] = bzf;

		JSONObject ydata = new JSONObject();
		ydata.put("name", "T1" + ":" + kslcmc);
		ydata.put("y", bzf);
		datas.add(0, ydata);

		JSONObject ks = new JSONObject();
		ks.put("kslcdm", kslcdm);
		ks.put("kslcmc", kslcmc);
		ks.put("mc", xAxis[0]);
		kslc.add(0, ks);

		JSONObject series1 = new JSONObject();
		series1.put("name", "历次考试标准分");
		series1.put("data", datas);
		series.add(series1);

		JSONObject rs = new JSONObject();
		rs.put("xAxis", xAxis);
		rs.put("series", series);
		rs.put("kslc", kslc);
		return rs;
	}

	/**
	 * 获取班级各科名次段人数
	 * 
	 * @param wlfzRankDisList
	 * @param bhPmszKmRsMap
	 * @param kmList
	 * @return
	 */
	public static JSONObject getBjMcdRsTable(String bh, List<JSONObject> wlfzRankDisList,
			Map<String, JSONObject> bhPmszKmRsMap, List<JSONObject> kmList) {
		// TODO Auto-generated method stub
		JSONObject table = new JSONObject();
		JSONArray columns = new JSONArray();
		JSONArray rows = new JSONArray();
		// 第一行
		JSONArray columns1 = new JSONArray();
		JSONObject col = new JSONObject();
		col.put("field", "scoreDisName");
		col.put("title", "分数段");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns1.add(col);

		col = new JSONObject();
		col.put("field", "dis00000000");
		col.put("title", "总分");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns1.add(col);

		for (JSONObject km : kmList) {
			if (km.getInteger("isHb") == 0) {

				col = new JSONObject();
				col.put("field", "dis" + km.getString("kmdm"));
				col.put("title", km.getString("kmmc"));
				col.put("align", "center");
				col.put("width", 66);
				col.put("sortable", "false");
				columns1.add(col);
			}
		}

		columns.add(columns1);

		for (JSONObject disRank : wlfzRankDisList) {
			String fbdm = disRank.getString("pmfbdm");
			String scoreDisName = disRank.getString("pmfbxx") + "-" + disRank.getString("pmfbsx");

			JSONObject row = new JSONObject();
			row.put("scoreDisName", scoreDisName);
			int zrs = 0;
			String zfkey = bh + "-" + fbdm + "-00000000";
			if (bhPmszKmRsMap.containsKey(zfkey) && bhPmszKmRsMap.get(zfkey).containsKey("rs")) {
				zrs = bhPmszKmRsMap.get(zfkey).getIntValue("rs");
			}
			row.put("dis00000000", zrs);

			for (JSONObject km : kmList) {
				if (km.getInteger("isHb") == 0) {
					String fbkey = bh + "-" + fbdm + "-" + km.getString("kmdm");
					int rs = 0;
					JSONObject rsObj = bhPmszKmRsMap.get(fbkey);
					if (rsObj != null && rsObj.containsKey("rs")) {
						rs = rsObj.getIntValue("rs");
					}
					row.put("dis" + km.getString("kmdm"), rs);
				}
			}

			rows.add(row);
		}
		table.put("columns", columns);
		table.put("rows", rows);
		return table;
	}

	/**
	 * 获取文理下的总分成绩分布表格
	 * 
	 * @param wlfz
	 * @param gmScDisList
	 * @param bhFbdmScoreDisMap
	 * @param scdWlMap
	 * @return
	 */
	public static JSONObject getZfdRsTable(String wlfz, List<ScoreDistribute> gmScDisList,
			Map<String, Map<String, JSONObject>> bhFbdmScoreDisMap, Map<String, String> scdWlMap) {
		// TODO Auto-generated method stub
		JSONObject table = new JSONObject();
		JSONArray columns = new JSONArray();
		List<JSONObject> rows = new ArrayList<JSONObject>();
		// 第一行
		JSONArray columns1 = new JSONArray();
		JSONObject col = new JSONObject();
		col.put("field", "bjmc");
		col.put("title", "班级");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns1.add(col);
		for (ScoreDistribute tgs : gmScDisList) {
			if (scdWlMap.get(tgs.getDm()).equalsIgnoreCase(wlfz)) {
				col = new JSONObject();
				col.put("field", "dis" + tgs.getFbdm());
				col.put("title", tgs.getFbsx() + "分-" + tgs.getFbxx() + "分");
				col.put("align", "center");
				col.put("width", 66);
				col.put("sortable", "false");
				columns1.add(col);
			}
		}
		columns.add(columns1);

		for (Iterator<String> it = bhFbdmScoreDisMap.keySet().iterator(); it.hasNext();) {

			String s = it.next();
			String bjmc = s.split("-")[1];

			JSONObject row = new JSONObject();

			row.put("bjmc", bjmc);

			row.put("bh", s.split("-")[0]);

			Map<String, JSONObject> fbdmScoreMap = bhFbdmScoreDisMap.get(s);
			for (Iterator<String> itt = fbdmScoreMap.keySet().iterator(); itt.hasNext();) {
				String fbdm = itt.next();
				JSONObject re = fbdmScoreMap.get(fbdm);
				row.put("dis" + fbdm, re.get("rs"));
			}

			rows.add(row);
		}
		rows = sorStuScoreList(rows, "bh", "asc", "bh", "");
		table.put("columns", columns);
		table.put("rows", rows);
		return table;
	}

	/**
	 * 生成学生成绩报告
	 * 
	 * @param xhWlkmcjMap
	 *            学生单科成绩排名及跟踪
	 * @param bjDataForXsReport
	 *            班级数据及平均分、最高分
	 * @param xs
	 *            学生总分数据
	 * @param bjpmgz
	 *            学生总分排名跟踪
	 * @param njpmgz
	 * @param dbkslcmc
	 *            对比考试轮次名称
	 * @param wlzf
	 *            年级总分满分
	 * @param kmList
	 *            科目列表及科目满分、年级科目最高及平均分
	 * @param maxZfScr
	 *            年级总分最高分
	 * @param avgZfScr
	 *            年级总分平均分
	 * @return
	 */
	public static AnalysisReportStu getStuReportByParam(Map<String, List<JSONObject>> xhWlkmcjMap,
			Map<String, JSONObject> bjDataForXsReport, JSONObject xs, Integer bjpmgz, Integer njpmgz, String dbkslcmc,
			Integer wlzf, List<JSONObject> kmList, Float maxZfScr, Float avgZfScr, List<JSONObject> tsss,
			Map<String, JSONObject> kmdmKmObjMap) {

		bjpmgz = bjpmgz == null ? 0 : bjpmgz;
		njpmgz = njpmgz == null ? 0 : njpmgz;
		wlzf = wlzf == null ? 0 : wlzf;
		maxZfScr = maxZfScr == null ? 0f : maxZfScr;
		avgZfScr = avgZfScr == null ? 0f : avgZfScr;

		// TODO Auto-generated method stub
		AnalysisReportStu xsReport = new AnalysisReportStu();
		String xh = xs.getString("xh");
		String bh = xs.getString("bh");
		if (!bjDataForXsReport.containsKey(bh)) {
			return null;
		}
		xsReport.setXh(xh);
		// 声明变量
		JSONObject cjd = new JSONObject();
		JSONArray columns = new JSONArray();
		JSONArray rows = new JSONArray();
		// 第一行
		JSONArray columns1 = new JSONArray();
		JSONObject col = new JSONObject();
		col.put("field", "itemName");
		col.put("title", "科目");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns1.add(col);

		col = new JSONObject();
		col.put("field", "kmzf");
		col.put("title", "总分");
		col.put("align", "center");
		col.put("width", 66);
		col.put("sortable", "false");
		columns1.add(col);

		JSONObject rowmf = new JSONObject();
		rows.add(rowmf);
		rowmf.put("itemName", "满分");
		rowmf.put("kmzf", wlzf);

		JSONObject rowzgf = new JSONObject();
		rows.add(rowzgf);
		rowzgf.put("itemName", "最高分");
		rowzgf.put("kmzf", maxZfScr);

		JSONObject rowcj = new JSONObject();
		rows.add(rowcj);
		rowcj.put("itemName", "成绩");
		rowcj.put("kmzf", xs.get("zcj"));

		JSONObject rowbjpm = new JSONObject();
		rows.add(rowbjpm);
		rowbjpm.put("itemName", "班级排名");
		rowbjpm.put("kmzf", xs.get("bjzfpm") + "|" + bjpmgz);

		JSONObject rownjpm = new JSONObject();
		rows.add(rownjpm);
		rownjpm.put("itemName", "年级排名");
		rownjpm.put("kmzf", xs.get("njzfpm") + "|" + njpmgz);

		for (JSONObject km : kmList) {
			col = new JSONObject();
			String kkmdm = "km" + km.getString("kmdm");
			col.put("field", kkmdm);
			col.put("title", km.getString("kmmc"));
			col.put("align", "center");
			col.put("width", 66);
			col.put("sortable", "false");
			columns1.add(col);

			rowmf.put(kkmdm, km.getIntValue("mf"));
			rowzgf.put(kkmdm, km.get("wlfzzgf"));

		}
		columns.add(columns1);
		cjd.put("columns", columns);
		cjd.put("rows", rows);
		JSONObject bjData = bjDataForXsReport.get(bh);
		int bjckrs = bjData.getIntValue("bjckrs");
		int fzckrs = bjData.getIntValue("bjfzckrs");
		String bjfzmc = bjData.getString("bjfzmc");
		String cjdsm = "参加本次考试的班级人数：" + bjckrs + "人，" + bjfzmc + "组人数" + fzckrs
				+ "人；各科目排名是指在全年级中的位置，排名后的进步名次（↑）、退步名次（↓）是与上次考试" + "（" + dbkslcmc + "）的对比，最高分（率）差是指本班各项目分析结果与全年级的对比差值。"
				+ "以下的分析均以总分作为参考依据。“班级人数”指班级总分参考人数，”" + bjfzmc + "”指班级分组，" + "“" + bjfzmc + "”指班级分组总分参考人数。";
		cjd.put("sm", cjdsm);

		HashMap<String, Integer> tUp = new HashMap<String, Integer>();
		HashMap<String, Integer> tDw = new HashMap<String, Integer>();
		HashMap<String, Integer> tNj = new HashMap<String, Integer>();

		for (JSONObject ts : tsss) {
			String dm = ts.getString("dm");
			if (dm.equalsIgnoreCase("01")) {
				tUp.put(ts.getString("tsydm"), ts.getInteger("szz"));
			} else if (dm.equalsIgnoreCase("02")) {
				tDw.put(ts.getString("tsydm"), ts.getInteger("szz"));
			} else if (dm.equalsIgnoreCase("03")) {
				tNj.put(ts.getString("tsydm"), ts.getInteger("szz"));
			}
		}
		// 进步提示语Map
		HashMap<String, JSONArray> jbtsyMap = new HashMap<String, JSONArray>();
		// 退步提示语Map
		HashMap<String, JSONArray> tbtsyMap = new HashMap<String, JSONArray>();
		// 科目优劣提示语Map
		HashMap<String, JSONArray> njtsyMap = new HashMap<String, JSONArray>();

		List<JSONObject> xsKmList = xhWlkmcjMap.get(xh);

		// 学生科目优劣数据
		String[] kmxAxis = new String[xsKmList.size()];
		// 0 个人成绩 1年级平均分 2年级最高分
		float[][] kmdata = new float[3][xsKmList.size()];
		// kmxAxis[0] = "总分";
		// kmdata[0][0] = xs.getFloatValue("zcj");
		// kmdata[1][0] = avgZfScr;
		// kmdata[2][0] = maxZfScr;
		int index = -1;
		// int index = 0;
		for (JSONObject xskm : xsKmList) {
			index++;
			float cj = xskm.getFloatValue("cj");

			String kkmdm = "km" + xskm.getString("kmdm");
			rowcj.put(kkmdm, cj);

			int bjpg = xskm.getIntValue("bjkmpmgz");
			int njpg = xskm.getIntValue("njkmpmgz");
			rowbjpm.put(kkmdm, xskm.get("bjkmpm") + "|" + bjpg);
			rownjpm.put(kkmdm, xskm.get("njkmpm") + "|" + njpg);

			String kmdm = xskm.getString("kmdm");
			String kmmc = xskm.getString("kmmc");

			// 科目优劣数据
			JSONObject km = kmdmKmObjMap.get(kmdm);
			kmxAxis[index] = kmmc;
			kmdata[0][index] = cj;
			kmdata[1][index] = km.getFloat("wlfzpjf");
			kmdata[2][index] = km.getFloat("wlfzzgf");
			if (njpg < 0) {
				njpg = (int) Math.abs(njpg);
				String tsydm = "";
				if (njpg >= tDw.get("01") && njpg <= tDw.get("02")) {
					tsydm = "01";
				} else if (njpg > tDw.get("02") && njpg <= tDw.get("03")) {
					tsydm = "02";
				} else if (njpg > tDw.get("03")) {
					tsydm = "03";
				}
				JSONObject now = new JSONObject();
				now.put("kmdm", kmdm);
				now.put("kmmc", kmmc);
				JSONArray tbs = new JSONArray();
				if (tbtsyMap.containsKey(tsydm)) {
					tbs = tbtsyMap.get(tsydm);
				} else {
					tbtsyMap.put(tsydm, tbs);
				}
				tbs.add(now);
			} else if (njpg > 0) {
				njpg = (int) Math.abs(njpg);
				String tsydm = "";
				if (njpg >= tUp.get("03") && njpg <= tUp.get("02")) {
					tsydm = "03";
				} else if (njpg > tUp.get("02") && njpg <= tUp.get("01")) {
					tsydm = "02";
				} else if (njpg > tUp.get("01")) {
					tsydm = "01";
				}
				JSONObject now = new JSONObject();
				now.put("kmdm", kmdm);
				now.put("kmmc", kmmc);
				JSONArray jbs = new JSONArray();
				if (jbtsyMap.containsKey(tsydm)) {
					jbs = jbtsyMap.get(tsydm);
				} else {
					jbtsyMap.put(tsydm, jbs);
				}
				jbs.add(now);

			}
			String tsydm = ""; // 提示语
			float njpjf = km.getFloat("wlfzpjf");
			if ((cj - njpjf) >= tNj.get("01")) {
				tsydm = "01";
			} else if ((cj - njpjf) >= tNj.get("03") && (cj - njpjf) <= tNj.get("01")) {
				tsydm = "02";
			} else if ((cj - njpjf) <= tNj.get("03")) {
				tsydm = "03";
			}

			JSONObject now = new JSONObject();
			now.put("kmdm", kmdm);
			now.put("kmmc", kmmc);
			JSONArray njs = new JSONArray();
			if (njtsyMap.containsKey(tsydm)) {
				njs = njtsyMap.get(tsydm);
			} else {
				njtsyMap.put(tsydm, njs);
			}
			njs.add(now);
		}
		JSONArray jbtsy = new JSONArray();
		for (Iterator<String> it = jbtsyMap.keySet().iterator(); it.hasNext();) {
			String tsydm = it.next();
			JSONObject jb = new JSONObject();
			jb.put("tsydm", tsydm);
			jb.put("km", jbtsyMap.get(tsydm));

			jbtsy.add(jb);
		}
		JSONArray tbtsy = new JSONArray();
		for (Iterator<String> it = tbtsyMap.keySet().iterator(); it.hasNext();) {
			String tsydm = it.next();
			JSONObject tb = new JSONObject();
			tb.put("tsydm", tsydm);
			tb.put("km", tbtsyMap.get(tsydm));

			tbtsy.add(tb);
		}
		// 科目优劣提示语
		JSONArray kmyltsj = new JSONArray();
		for (Iterator<String> it = njtsyMap.keySet().iterator(); it.hasNext();) {
			String tsydm = it.next();
			JSONObject tb = new JSONObject();
			tb.put("tsydm", tsydm);
			tb.put("km", njtsyMap.get(tsydm));

			kmyltsj.add(tb);
		}

		// 科目优劣数据
		JSONObject kmylsj = new JSONObject();

		JSONArray kmseries = new JSONArray();
		JSONObject ser = new JSONObject();
		ser.put("name", "考试成绩");
		ser.put("data", kmdata[0]);
		kmseries.add(ser);

		ser = new JSONObject();
		ser.put("name", "年级平均分");
		ser.put("data", kmdata[1]);
		kmseries.add(ser);

		ser = new JSONObject();
		ser.put("name", "年级最高分");
		ser.put("data", kmdata[2]);
		kmseries.add(ser);
		kmylsj.put("xAxis", kmxAxis);
		kmylsj.put("series", kmseries);

		// 暂时不用
		JSONObject cjbhsj = new JSONObject();
		JSONObject cjbhtsy = new JSONObject();

		xsReport.setKmylsj(kmylsj.toJSONString());
		xsReport.setKmyltsy(kmyltsj.toJSONString());
		xsReport.setJbtsy(jbtsy.toJSONString());
		xsReport.setTbtsy(tbtsy.toJSONString());
		xsReport.setCjd(cjd.toJSONString());

		return xsReport;
	}

	/**
	 * 生成校内考试App学生成绩报告
	 * 
	 * @param xhWlkmcjMap
	 *            学生单科成绩排名及跟踪
	 * @param bjDataForXsReport
	 *            班级数据及平均分、最高分
	 * @param xs
	 *            学生总分数据
	 * @param bjpmgz
	 *            学生总分排名跟踪
	 * @param njpmgz
	 * @param dbkslcmc
	 *            对比考试轮次名称
	 * @param wlzf
	 *            年级总分满分
	 * @param kmList
	 *            科目列表及科目满分、年级科目最高及平均分
	 * @param maxZfScr
	 *            年级总分最高分
	 * @param avgZfScr
	 *            年级总分平均分
	 * @return
	 */
	public static AppStudentScoreReport getAppStuReportByParam(Map<String, List<JSONObject>> xhWlkmcjMap,
			Map<String, JSONObject> bjDataForXsReport, JSONObject xs, Integer bjpmgz, Integer njpmgz, String dbkslcmc,
			Integer wlzf, List<JSONObject> kmList, Float maxZfScr, Float avgZfScr, List<JSONObject> tsss,
			Map<String, JSONObject> kmdmKmObjMap, List<JSONObject> wlzcjNjpm,
			Map<String, ScoreClassStatistics> bhClassStaMap, Map<String, String> bhNjNameMap) {

		bjpmgz = bjpmgz == null ? 0 : bjpmgz;
		njpmgz = njpmgz == null ? 0 : njpmgz;
		wlzf = wlzf == null ? 0 : wlzf;
		maxZfScr = maxZfScr == null ? 0f : maxZfScr;
		avgZfScr = avgZfScr == null ? 0f : avgZfScr;

		// 1.定义保存数据的对象reportData,取出学号，班号
		String xh = xs == null ? "" : xs.getString("xh");// 学生编号
		String bh = xs == null ? "" : xs.getString("bh");// 班级编号
		if (!bjDataForXsReport.containsKey(bh)) {
			return null;
		}

		JSONObject reportData = new JSONObject();// 学生报告数据
		// 班级总分统计结果
		ScoreClassStatistics scoreClassStatistics = bhClassStaMap == null ? null
				: (ScoreClassStatistics) bhClassStaMap.get(bh);

		// 1.1 设置学生报告数据的总人数,总分,总分等第,考试成就(比如：击败了80%的同学),班级平均分,班级最高分,考试排名趋势
		reportData.put("totalFullMark", wlzf);// 满分
		reportData.put("totalScore", xs == null ? 0 : xs.get("zcj"));// 总分
		reportData.put("totalScoreLevel", xs == null ? "" : xs.getString("djxl"));// 总分等第

		float joinExamNum = scoreClassStatistics == null ? 1 : scoreClassStatistics.getCkrs();// 参加考试人数
		float classRank = xs == null ? joinExamNum : xs.getIntValue("bjzfpm");// 班级排名

		DecimalFormat format = new DecimalFormat("0");
		String achievement = format.format((1 - classRank / joinExamNum) * 100);

		reportData.put("achievement", achievement + "%");// 考试成就(比如：击败了80%的同学)

		reportData.put("classAverageScore", StringUtil.formatNumber(avgZfScr, 2));// 年级平均分
		reportData.put("classMaxScore", maxZfScr);// 年级最高分

		reportData.put("rankTendency", "");// 考试排名趋势

		// 1.2.考试详细情况进步科目，退步科目，提示语计算处理

		JSONObject bjData = bjDataForXsReport == null ? null : bjDataForXsReport.get(bh);
		JSONObject examDetails = new JSONObject();// 考试详细情况
		examDetails.put("classStudentNum", bjData == null ? 0 : bjData.getIntValue("bjckrs"));// 设置参加本次考试的班级人数
		examDetails.put("gradeStudentNum", bjData == null ? 0 : bjData.getIntValue("bjfzckrs"));// 设置年级人数
		examDetails.put("gradeName", bhNjNameMap == null ? "" : bhNjNameMap.get(bh));// 设置年级名称

		// 1.2.1 对比考试情况
		JSONObject compareExamInfo = new JSONObject();
		compareExamInfo.put("compareExamName", dbkslcmc);// 设置比较考试名称

		// 1.2.1.1 对比考试数据列表
		List<JSONObject> dataList = new ArrayList<JSONObject>();

		List<JSONObject> studentSubjectList = xhWlkmcjMap == null ? null : xhWlkmcjMap.get(xh);
		Map<String, JSONObject> studentSubjectMap = StringUtil.convertJSONObjectToMap(studentSubjectList, "kmdm");

		if (CollectionUtils.isNotEmpty(kmList)) {
			for (JSONObject subject : kmList) {
				if (subject == null)
					continue;

				JSONObject data = new JSONObject();// 表格数据

				String subjectId = StringUtil.transformString(subject.get("kmdm"));

				data.put("subjectName", StringUtil.transformString(subject.get("kmmc")));// 科目名称
				data.put("gradeMaxScore", StringUtil.formatNumber(subject.get("wlfzzgf"), 1));// 科目最高分
				data.put("gradeAverageScore", StringUtil.formatNumber(subject.get("wlfzpjf"), 2));// 科目平均分

				if (CollectionUtils.isEmpty(studentSubjectList))
					continue;

				JSONObject studentSubject = studentSubjectMap.get(subjectId);

				if (studentSubject != null) {
					float score = studentSubject.getFloatValue("cj");
					int bjpg = studentSubject.getIntValue("bjkmpmgz");
					int njpg = studentSubject.getIntValue("njkmpmgz");
					data.put("studentScore", StringUtil.formatNumber(score, 1));// 科目成绩
					if (bjpg == 0) {
						data.put("studentClassRank", studentSubject.get("bjkmpm") + "|");// 班级排名
					} else {
						data.put("studentClassRank", studentSubject.get("bjkmpm") + "|" + bjpg);// 班级排名
					}
					if (njpg == 0) {
						data.put("studentGradeRank", studentSubject.get("njkmpm") + "|");// 年级排名
					} else {
						data.put("studentGradeRank", studentSubject.get("njkmpm") + "|" + njpg);// 年级排名
					}

				}

				dataList.add(data);
			}

			// 添加总分成绩数据
			JSONObject data = new JSONObject();// 表格数据
			data.put("subjectName", "总分");// 科目名称
			data.put("gradeMaxScore", StringUtil.formatNumber(maxZfScr, 1));// 科目最高分
			data.put("gradeAverageScore", StringUtil.formatNumber(avgZfScr, 2));// 科目平均分
			data.put("studentScore", xs == null ? "" : StringUtil.formatNumber(xs.get("zcj"), 1));// 科目成绩

			data.put("studentClassRank", xs.get("bjzfpm") + "|" + bjpmgz);
			data.put("studentGradeRank", xs.get("njzfpm") + "|" + njpmgz);

			dataList.add(0, data);
		}

		compareExamInfo.put("data", dataList);// 比较考试数据
		examDetails.put("compareExamInfo", compareExamInfo);

		// 1.2.1.2 进步科目情况,退步科目情况，所有科目在班级情况总结

		JSONObject advanceSubjectInfo = new JSONObject();// 进步科目

		List<JSONObject> moreAdvanceSubject = new ArrayList<JSONObject>();// 进步明显的科目
		List<JSONObject> someAdvanceSubject = new ArrayList<JSONObject>();// 有一些进步的科目
		List<JSONObject> smallAdavceSubject = new ArrayList<JSONObject>();// 略有有进步的科目

		JSONObject badSubjectInfo = new JSONObject();// 需要加强科目情况

		List<JSONObject> moreBadSubject = new ArrayList<JSONObject>();// 退步明显科目
		List<JSONObject> someBadSubject = new ArrayList<JSONObject>();// 有一些退步科目
		List<JSONObject> smallBadSubject = new ArrayList<JSONObject>();// 略有退步科目

		String subjectScoreSummary = "";// 所有科目在班级情况总结
		String moreGoodSum = "", someGoodSum = "", smallGoodSum = "";// 明显优于，有一些优于，略优于年级其他同学的科目

		int goodStudemtNum = 0, badStudemtNum = 0;// 进步科目数量，退步科目数量

		HashMap<String, Integer> tUp = new HashMap<String, Integer>();
		HashMap<String, Integer> tDw = new HashMap<String, Integer>();
		HashMap<String, Integer> tNj = new HashMap<String, Integer>();

		if (CollectionUtils.isNotEmpty(tsss)) {
			for (JSONObject ts : tsss) {
				if (ts == null)
					continue;

				String dm = ts.getString("dm");
				if (StringUtil.isEmpty(dm))
					continue;

				if (dm.equalsIgnoreCase("01")) {
					tUp.put(ts.getString("tsydm"), ts.getInteger("szz"));
				} else if (dm.equalsIgnoreCase("02")) {
					tDw.put(ts.getString("tsydm"), ts.getInteger("szz"));
				} else if (dm.equalsIgnoreCase("03")) {
					tNj.put(ts.getString("tsydm"), ts.getInteger("szz"));
				}
			}
		}

		// 计算出优劣科目提示语，明显进步，一些进步和略有进步的科目，还有明显退步，有一些退步和略有退步的科目
		if (CollectionUtils.isNotEmpty(studentSubjectList)) {
			for (JSONObject xskm : studentSubjectList) {
				if (xskm == null)
					continue;
				float cj = xskm.getFloatValue("cj");
				int bjpg = xskm.getIntValue("bjkmpmgz");
				int njpg = xskm.getIntValue("njkmpmgz");
				String kmdm = xskm.getString("kmdm");
				String kmmc = StringUtil.transformString(xskm.getString("kmmc"));
				// 科目优劣数据
				JSONObject km = kmdmKmObjMap == null ? null : kmdmKmObjMap.get(kmdm);

				if (njpg < 0) {
					njpg = (int) Math.abs(njpg);
					String tsydm = "";
					if (njpg >= tDw.get("01") && njpg <= tDw.get("02")) {// 略有退步
						JSONObject badSubject = new JSONObject();
						badSubject.put("subjectName", kmmc);
						badSubject.put("value", njpg);
						smallBadSubject.add(badSubject);
						badStudemtNum++;
					} else if (njpg > tDw.get("02") && njpg <= tDw.get("03")) {// 有退步
						JSONObject badSubject = new JSONObject();
						badSubject.put("subjectName", kmmc);
						badSubject.put("value", njpg);
						someBadSubject.add(badSubject);
						badStudemtNum++;
					} else if (njpg > tDw.get("03")) {// 有较大退步
						JSONObject badSubject = new JSONObject();
						badSubject.put("subjectName", kmmc);
						badSubject.put("value", njpg);
						moreBadSubject.add(badSubject);
						badStudemtNum++;
					}
				} else if (njpg > 0) {
					njpg = (int) Math.abs(njpg);
					if (njpg >= tUp.get("03") && njpg <= tUp.get("02")) {// 略有进步
						JSONObject advanceSubject = new JSONObject();
						advanceSubject.put("subjectName", kmmc);
						advanceSubject.put("value", njpg);
						smallAdavceSubject.add(advanceSubject);

						goodStudemtNum++;
					} else if (njpg > tUp.get("02") && njpg <= tUp.get("01")) {// 有一些进步
						JSONObject advanceSubject = new JSONObject();
						advanceSubject.put("subjectName", kmmc);
						advanceSubject.put("value", njpg);
						someAdvanceSubject.add(advanceSubject);

						goodStudemtNum++;
					} else if (njpg > tUp.get("01")) {// 进步明显

						JSONObject advanceSubject = new JSONObject();
						advanceSubject.put("subjectName", kmmc);
						advanceSubject.put("value", njpg);
						moreAdvanceSubject.add(advanceSubject);

						goodStudemtNum++;
					}
				}

				float njpjf = km == null ? 0 : km.getFloat("wlfzpjf");
				if (njpjf <= cj && (cj - njpjf) >= tNj.get("01")) {
					if (!StringUtil.isEmpty(kmmc))
						moreGoodSum = moreGoodSum + "," + kmmc;
				} else if ((cj - njpjf) >= tNj.get("03") && (cj - njpjf) <= tNj.get("01")) {
					if (!StringUtil.isEmpty(kmmc))
						someGoodSum = someGoodSum + "," + kmmc;
				} else if ((cj - njpjf) <= tNj.get("03")) {
					if (!StringUtil.isEmpty(kmmc))
						smallGoodSum = smallGoodSum + "," + kmmc;
				}
			}

			if (moreGoodSum.length() > 0)
				moreGoodSum = moreGoodSum.substring(1);
			if (someGoodSum.length() > 0)
				someGoodSum = someGoodSum.substring(1);
			if (smallGoodSum.length() > 0)
				smallGoodSum = smallGoodSum.substring(1);
		}

		advanceSubjectInfo.put("subjectNum", goodStudemtNum);// 设置进步科目数量
		advanceSubjectInfo.put("moreAdvanceSubject", moreAdvanceSubject);// 设置进步科目数量
		advanceSubjectInfo.put("someAdvanceSubject", someAdvanceSubject);// 设置进步科目数量
		advanceSubjectInfo.put("smallAdavceSubject", smallAdavceSubject);// 设置进步科目数量
		examDetails.put("advanceSubjectInfo", advanceSubjectInfo);

		badSubjectInfo.put("subjectNum", badStudemtNum);// 需要加强科目数量
		badSubjectInfo.put("moreBadSubject", moreBadSubject);// 设置退步明显科目
		badSubjectInfo.put("someBadSubject", someBadSubject);// 设置有一些退步的科目
		badSubjectInfo.put("smallBadSubject", smallBadSubject);// 设置略有退步科目
		examDetails.put("badSubjectInfo", badSubjectInfo);// 把退步考试添加到考试详情中

		if (!StringUtil.isEmpty(moreGoodSum)) {
			subjectScoreSummary = moreGoodSum + "成绩明显优于年级其它学生;";
		}
		if (!StringUtil.isEmpty(someGoodSum)) {
			subjectScoreSummary = subjectScoreSummary + someGoodSum + "成绩与年级其它学生差不多";
		}
		if (!StringUtil.isEmpty(smallGoodSum)) {
			subjectScoreSummary = subjectScoreSummary + smallGoodSum + "成绩低于年级其它学生;";
		}

		examDetails.put("subjectScoreSummary", subjectScoreSummary);// 设置所有科目在班级情况总结

		// 1.3 设置考试详情
		reportData.put("examDetails", examDetails);// 设置考试详情

		// 1.4 前三奖榜计算

		List<JSONObject> topThree = new ArrayList<JSONObject>();

		if (CollectionUtils.isNotEmpty(wlzcjNjpm)) {// 对文理分组的学生总成绩进行排序
													// 取出前面三个的总分
			// Map<String,JSONObject> rankMap=new HashMap<String, JSONObject>();
			// 记录分值
			List<Double> rankList = new ArrayList<Double>();
			double lastfz = 0;
			for (int i = 0; i < wlzcjNjpm.size() && rankList.size() < 3; i++) {
				JSONObject student = wlzcjNjpm.get(i);
				double zcj = student.getDouble("zcj");
				if (zcj != lastfz) {
					lastfz = zcj;
					rankList.add(zcj);
				}
				// rankMap.put(gradeRank+"", wlzcjNjpm.get(i));

			}

			if (rankList.size() > 1) {
				Collections.sort(rankList, new Comparator<Double>() {// 排序
					@Override
					public int compare(Double o1, Double o2) {
						// TODO Auto-generated method stub
						return o1 < o2 ? 1 : -1;
					}

				});
			}

			for (int i = 0; i < 3 && i < rankList.size(); i++) {
				double zcj = rankList.get(i);
				// JSONObject student=rankMap.get(gradeRank+"");
				// if(student!=null)
				// {
				//
				JSONObject top = new JSONObject();
				top.put("totalScore", zcj);
				top.put("rank", i + 1);
				topThree.add(top);
				// }
			}

		}

		reportData.put("topThree", topThree);// 设置前三奖榜

		// 2.返回结果
		AppStudentScoreReport appStudentReport = new AppStudentScoreReport();// 存放app学生成绩报告数据
		appStudentReport.setStudentId(xh);
		appStudentReport.setReportData(JSONObject.toJSONString(reportData));
		return appStudentReport;

	}

	public static JSONObject getAppClassReportData(AnalysisReportBj tSARAnalysisReportbj, JSONArray rows, String zmc,
			Integer zckrs, Integer bjckrs, String lastLc, List<JSONObject> kmList, List<JSONObject> bjcjgzListkm,
			JSONArray xsycqk, Map<String, Map<String, ScoreClassStatisticsMk>> bhKmClassStaMap, String bh,
			String kslcdm, String kslcmc) {

		zckrs = zckrs == null ? 0 : zckrs;
		bjckrs = bjckrs == null ? 0 : bjckrs;

		JSONObject cjgzsj = JSON.parseObject(tSARAnalysisReportbj.getCjgzsj());
		JSONObject reportData = new JSONObject();

		JSONObject examSituation = new JSONObject();
		examSituation.put("totalSocreRankRrend", cjgzsj);
		// 考试科目成绩排名情况
		JSONArray subjectScoreDetails = new JSONArray();
		for (int i = 0; i < rows.size(); i++) {
			JSONObject row = rows.getJSONObject(i);
			JSONObject detail = new JSONObject();
			detail.put("subjectName", row.get("item"));
			detail.put("averageScore", row.get("pjf"));
			detail.put("averageRank",
					row.get("pjfpm") + (row.containsKey("pjfpmgz") ? "|" + row.getString("pjfpmgz") : ""));
			detail.put("goodScore", row.get("yxl"));
			detail.put("goodRank",
					row.get("yxlpm") + (row.containsKey("yxlpmgz") ? "|" + row.getString("yxlpmgz") : ""));
			detail.put("passScore", row.get("hgl"));
			detail.put("passRank",
					row.get("hglpm") + (row.containsKey("hglpmgz") ? "|" + row.getString("hglpmgz") : ""));

			subjectScoreDetails.add(detail);
		}
		examSituation.put("subjectScoreDetails", subjectScoreDetails);
		// 考试科目说明
		JSONObject examSubjectInstructions = new JSONObject();
		examSubjectInstructions.put("classNum", bjckrs);
		examSubjectInstructions.put("groupNum", zckrs);
		examSubjectInstructions.put("groupName", zmc);
		examSubjectInstructions.put("contrastExamName", lastLc);

		examSituation.put("examSubjectInstructions", examSubjectInstructions);
		reportData.put("examSituation", examSituation);
		// 学生异常情况map
		HashMap<String, JSONObject> kmXsycList = new HashMap<String, JSONObject>();
		for (int i = 0; i < xsycqk.size(); i++) {
			JSONObject xsyc = xsycqk.getJSONObject(i);
			String kmdm = xsyc.getString("kmdm");
			if (kmdm != null && kmdm.trim().length() > 0) {
				kmXsycList.put(kmdm, xsyc);
			}
		}
		// 学生变化情况
		JSONArray studentChangeSituation = new JSONArray();
		reportData.put("studentChangeSituation", studentChangeSituation);
		for (JSONObject km : kmList) {
			String kmdm = km.getString("kmdm");
			String kmmc = km.getString("kmmc");
			List<JSONObject> bjgzList = GrepUtil.grepJsonKeyBySingleVal("kmdm", kmdm, bjcjgzListkm);
			JSONObject studentCs = new JSONObject();
			studentChangeSituation.add(studentCs);
			studentCs.put("subjected", kmdm);
			studentCs.put("subjectName", kmmc);
			studentCs.put("subjectRankRrend", getSubjectRankRrend(bjgzList, bhKmClassStaMap, kmdm, bh, kslcdm, kslcmc));
			JSONArray jb = null;
			JSONArray yc = null;
			if (kmXsycList.containsKey(kmdm)) {
				jb = kmXsycList.get(kmdm).getJSONArray("jb");
				yc = kmXsycList.get(kmdm).getJSONArray("yc");
			}
			// 进步学生
			JSONObject progressStudent = new JSONObject();
			if (jb != null && jb.size() > 0) {
				int studentNum = jb.size();
				JSONArray studentList = new JSONArray();
				for (int i = 0; i < jb.size(); i++) {
					JSONObject stu = jb.getJSONObject(i);
					JSONObject stucopy = new JSONObject();
					stucopy.put("studentName", stu.get("xm"));
					stucopy.put("progressRank", stu.get("pmsj"));
					studentList.add(stucopy);
				}

				progressStudent.put("studentNum", studentNum);
				progressStudent.put("studentList", studentList);
			}
			studentCs.put("progressStudent", progressStudent);
			// 退步学生
			JSONObject setbackStudent = new JSONObject();
			if (yc != null && yc.size() > 0) {
				int studentNum = yc.size();
				JSONArray studentList = new JSONArray();
				for (int i = 0; i < yc.size(); i++) {
					JSONObject stu = yc.getJSONObject(i);
					JSONObject stucopy = new JSONObject();
					stucopy.put("studentName", stu.get("xm"));
					stucopy.put("progressRank", stu.get("pmsj"));
					studentList.add(stucopy);
				}

				setbackStudent.put("studentNum", studentNum);
				setbackStudent.put("studentList", studentList);
			}
			studentCs.put("setbackStudent", setbackStudent);

		}

		return reportData;
	}

	/**
	 * 获取班级科目标准分跟踪图表数据
	 * 
	 * @param bjgzList
	 * @param bhKmClassStaMap
	 * @param kmdm
	 * @param bh
	 * @param kslcmc
	 * @return
	 */
	private static JSONObject getSubjectRankRrend(List<JSONObject> bjgzList,
			Map<String, Map<String, ScoreClassStatisticsMk>> bhKmClassStaMap, String kmdm, String bh, String kslcdm,
			String kslcmc) {
		float kmbzf = 0;
		if (bhKmClassStaMap.containsKey(bh) && bhKmClassStaMap.get(bh).containsKey(kmdm)) {
			kmbzf = (float) bhKmClassStaMap.get(bh).get(kmdm).getBzf();
		}
		JSONObject rs = new JSONObject();
		rs = getBjBzfGzList(kmbzf, kslcdm, kslcmc, bjgzList);
		return rs;
	}

}
