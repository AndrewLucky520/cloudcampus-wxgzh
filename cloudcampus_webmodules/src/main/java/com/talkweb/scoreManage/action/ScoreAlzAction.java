package com.talkweb.scoreManage.action;

import java.util.concurrent.FutureTask;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.scoreManage.proc.ProgressBar;
import com.talkweb.scoreManage.service.ScoreAnalysisService;

@Controller
@RequestMapping("/scoremanage1/scoreAlz/")
public class ScoreAlzAction extends BaseAction {

	@Autowired
	private ScoreAnalysisService sAlzService;

	/**
	 * 多线程 每个用户有自己的参数组和进程
	 */

	private FutureTask task;
	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;

	/**
	 * 成绩分析--启动分析任务接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "startResultAnalysisTask")
	@ResponseBody
	public synchronized ProgressBar startResultAnalysisTask(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		
		ProgressBar progressBar = null;
		try {
			String xnxq = request.getString("termInfoId");
			String kslcdm = request.getString("examId");
			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslcdm)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			String kslcmc = request.getString("examName");
			
			String xxdm=null;
			String sessionId =null;
			String progressKey = null;
			String exkey = null;
			School sch = null;
			if(request.get("progressBar")!=null){
				progressBar =(ProgressBar) request.get("progressBar");
				progressBar.setCode(1);
				progressBar.setMsg("开始成绩分析！");
				progressBar.setProgress(51);
				xxdm=request.getString("xxdm");
				sessionId = request.getString("sessionId");
				redisOperationDAO = (RedisOperationDAO) request.get("redisOperationDAO");
				sch = (School) request.get("school");
				
				String newProgressKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
						.append(".importScore.progress").toString();
				redisOperationDAO.set(newProgressKey, progressBar,
						CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			}else{
				progressBar = new ProgressBar();
				xxdm = getXxdm(req);
				sessionId = req.getSession().getId();

				progressKey = new StringBuffer().append("score.scoreAlz.").append(xxdm).append(".").append(sessionId)
						.append(".progress").toString();

				exkey = new StringBuffer().append("score.scoreAlz.").append(xxdm).append(".").append(kslcdm)
						.append(".").append(sessionId).append(".exist").toString();

				progressBar.setProgressInfo(0, 0, "启动成功！");
				
				sch = this.getSchool(req, xnxq);
				String exists = (String) redisOperationDAO.get(exkey);
				if ("true".equals(exists)) {
					return progressBar;
				}
				redisOperationDAO.set(progressKey, progressBar, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			}


			
			String newProgressKey = new StringBuffer().append("scoreManage.").append(xxdm).append(sessionId)
					.append(".importScore.progress").toString();
			redisOperationDAO.set(newProgressKey, progressBar,
					CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			if(null== exkey){
				//task = new FutureTask<Void>(new SubProcess(xnxq, kslcdm, sessionId, xxdm, kslcmc, sch), null);
				//task.get();
				new SubProcess(xnxq, kslcdm, sessionId, xxdm, kslcmc, sch).start();
				progressBar.setCode(2);
				progressBar.setMsg("分析完成！");
				progressBar.setProgress(100);
				redisOperationDAO.set(newProgressKey, progressBar,
						CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			}
			SubProcess sp = new SubProcess(xnxq, kslcdm, sessionId, xxdm, kslcmc, sch);
			sp.start();			
			
			redisOperationDAO.set(exkey, "true", CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
		} catch (CommonRunException e) {
			progressBar.setProgressInfo(e.getCode(), 100, e.getMessage());
		} catch (Exception e) {
			progressBar.setProgressInfo(-1, 100, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return progressBar;
	}

	/**
	 * 成绩分析--获取导入进度
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getResultAnalysisProgress", method = RequestMethod.POST)
	@ResponseBody
	public ProgressBar getResultAnalysisProgress(HttpServletRequest req, HttpServletResponse res) {
		ProgressBar progressBar = new ProgressBar();
		try {
			String xxdm = getXxdm(req);
			String sessionId = req.getSession().getId();
			String progressKey = new StringBuffer().append("score.scoreAlz.").append(xxdm).append(".").append(sessionId)
					.append(".progress").toString();

			progressBar = (ProgressBar) redisOperationDAO.get(progressKey);
			if (progressBar == null) {
				throw new CommonRunException(-50, "由于长时间未操作，数据过期，请重新导入数据！");
			}
		} catch (CommonRunException e) {
			progressBar.setProgressInfo(e.getCode(), 100, e.getMessage());
		} catch (Exception e) {
			progressBar.setProgressInfo(-1, 100, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return progressBar;
	}

	class SubProcess extends Thread {
		private String xnxq;
		private String kslc;
		private String xxdm;
		private School sch;

		private String progressKey = null;
		private String exkey = null;

		public SubProcess(String xnxq, String kslc, String sessionId, String xxdm, String kslcmc, School sch) {
			this.xnxq = xnxq;
			this.kslc = kslc;
			this.xxdm = xxdm;
			this.sch = sch;

			this.progressKey = new StringBuffer().append("score.scoreAlz.").append(xxdm).append(".").append(sessionId)
					.append(".progress").toString();

			this.exkey = new StringBuffer().append("score.scoreAlz.").append(xxdm).append(".").append(kslc).append(".")
					.append(sessionId).append(".exist").toString();
		}

		@Override
		public void run() {
			ProgressBar progressBar = new ProgressBar();
			try {
				progressBar = (ProgressBar) redisOperationDAO.get(progressKey);
				if (progressBar == null) {
					progressBar = new ProgressBar();
					throw new CommonRunException(-50, "由于长时间未操作，数据过期，请重新导入数据！");
				}

				progressBar.setProgressInfo(1, 0, "正在准备分析数据...");
				redisOperationDAO.set(progressKey, progressBar,
						CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());

				JSONObject params = new JSONObject();
				params.put("xxdm", xxdm);
				params.put("kslc", kslc);
				params.put("kslcdm", kslc);
				params.put("xnxq", xnxq);

				params.put("school", sch);
				params.put("progressKey", progressKey);
				params.put("exkey", exkey);

				sAlzService.scoreAnalysis(params, progressBar);

			} catch (CommonRunException e) {
				progressBar.setProgressInfo(e.getCode(), 100, e.getMessage());
				try {
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} catch (Exception e) {
				progressBar.setProgressInfo(-1, 100, "分析出错，请检查各设置项或联系管理员！！");
				e.printStackTrace();
				try {
					redisOperationDAO.set(progressKey, progressBar,
							CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} finally {
				try {
					redisOperationDAO.del(this.exkey);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
