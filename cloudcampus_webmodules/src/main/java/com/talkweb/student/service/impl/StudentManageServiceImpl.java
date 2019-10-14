package com.talkweb.student.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.student.dao.StudentManageDao;
import com.talkweb.student.domain.business.TSsStudent;
import com.talkweb.student.service.StudentManageService;

/**
 * 
 * @ClassName: StudentManageServiceImpl.java
 * @version:2.0
 * @Description: 学生信息维护管理接口实现
 * @author 吴安辉
 * @date 2015年3月9日
 */

@Service
public class StudentManageServiceImpl implements StudentManageService {

	@Autowired 
	private StudentManageDao studentDao;

	@Autowired
	private AllCommonDataService commonDataService;

	//新增
	@Override
	public Map<String, Object> addStudent(TSsStudent student,String xn,String xqm) {
		Map<String, Object> data = new HashMap<String, Object>();
		String nj = student.getNj();
		String pycc = commonDataService.getPYCCBySYNJ(nj,xn);
		student.setPycc(pycc);
		
		String rxnd = commonDataService.ConvertSYNJ2RXND(nj,xn);
		student.setNj(rxnd);

		//添加学生基本信息
		int count = studentDao.addStudent(student);
		//获取所在班级的学制信息
		Map<String,Object> map = studentDao.getXzInfoByBh(student.getBh());
		int xz = new Integer(map.get("xz").toString());
		//入学年度（整型）
		int rxnj = new Integer(rxnd);
		//添加学生注册信息
		for(int i = rxnj; i < rxnj+xz - 1; i++){
			for(int j = 1; j <= 2;j++){
				Map<String,String> param = new HashMap<String,String>();
				param.put("xn",new Integer(i).toString());
				param.put("xqm", new Integer(j).toString());
				param.put("xxdm", student.getXxdm());
				param.put("xh", student.getXh());
				param.put("bh",student.getBh());
				param.put("sfzx","1");
				count = studentDao.addStudenrol(param);
			}
		}
		if(count > 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.addDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.addDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//查询列表
	@Override
	public JSONObject getStudentList(Map<String,Object> param,String xn) {
	    JSONObject data = new JSONObject();
		List<JSONObject> list = studentDao.getStudentList(param);
		for(JSONObject map : list){
			if(map.get("csrq") != null){
				String csrq = map.get("csrq").toString();
				if(csrq.length() > 10){
					csrq = csrq.substring(10);
				}
				map.put("csrq", csrq);
			}
		}
		data.put("total",list.size());
		data.put("rows",(JSONArray) JSONArray.toJSON(list));
		return data;
	}

	//删除
	@Override
	public Map<String, Object> deleteStudent(String[] xhs) {
		Map<String, Object> data = new HashMap<String, Object>();
		int count = 0;
		List<String> param = Arrays.asList(xhs);
		studentDao.deleteStudenrol(param);
		count = studentDao.deleteStudent(param);
		if(count > 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.delDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.delDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//查询单个学生
	@Override
	public TSsStudent getStudentById(String xh) {
		TSsStudent student = null;
		student = studentDao.getStudentById(xh);
		String nj = student.getNj();
		String pycc = student.getPycc();
		String synj = commonDataService.ConvertRXND2SYNJ(nj,pycc);
		student.setNj(synj);
		return student;
	}

	//修改学生信息
	@Override
	public Map<String, Object> updateStudent(TSsStudent student,String xn) {
		TSsStudent entity = null;
		String xh = student.getXh();
		entity = studentDao.getStudentById(xh);
		//先查询当前学生信息跟提交的学生信息比较
		String sfzx = entity.getSfzx();
		boolean flag = false;
		int count = 0;
		if(sfzx.equals(student.getSfzx())){
			flag = true;
		}
		//提交的使用年级
		String synj = student.getNj();
		//当前年级(入学年度)
		String dqnj = entity.getNj();
		//当前培养层次
		String pycc = entity.getPycc();
		Map<String,String> param = new HashMap<String,String>();
		param.put("xh", student.getXh());
		param.put("sfzx",student.getSfzx());

		//修改后的入学年度
		String nj = commonDataService.ConvertSYNJ2RXND(synj,xn);
		student.setNj(nj);
		//当前使用年级
		String dqsynj = commonDataService.ConvertRXND2SYNJ(dqnj,pycc);
		if(synj.equals(dqsynj)){
			//如果年级不变，在校状态不变，则只修改学生信息表
			if(flag){
				count = studentDao.updateStudent(student);
			}else{
				//年级不变，在校状态改变，则只修改学生信息表和学生注册表
				count = studentDao.updateStudent(student);
				count = studentDao.updateStudenrol(param);
			}
		}else{
			//获取该学生注册信息中当前最大的学年信息
			Map<String,Object> maxXnxqMap = studentDao.getMaxXnxqByXh(xh);
			//最大学年
			String maxXn = maxXnxqMap.get("xn").toString();
			count = studentDao.updateStudent(student);
			int a = new Integer(dqsynj) -  new Integer(synj);
			if(a < 0){
				//如果修改后年级比当前年级大：留级（增加两条注册记录）
				count = addZcjl(new Integer(maxXn),a,xh,student.getBh(),entity.getXxdm());
			}else{
				//如果修改后年级比当前年级小：跳级（减少两条注册记录）
				count = deleteZcjl(new Integer(maxXn),a,xh);
				if(!flag){
					//修改注册表的在校状态
					count = studentDao.updateStudenrol(param);
				}
			}
		}
		Map<String, Object> data = new HashMap<String, Object>();
		if(count >= 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.updateDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.updateDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	/**
	 * 删除学生注册记录
	 * @param xn 
	 * @param a 
	 * @param xh 
	 * @return
	 */
	private int deleteZcjl(int xn,int a,String xh){
		int count = 0;
		for(int i = xn;i > xn - Math.abs(a) + 1;i--){
			for(int j = 1; j <= 2; j++){
				Map<String,Object> par = new HashMap<String,Object>();
				par.put("xn", i);
				par.put("xqm", j);
				par.put("xh", xh);
				count = studentDao.deleteStudenrolByXh(par);
			}
		}
		return count;
	}
	
	/**
	 * 添加学生注册记录
	 * @param xn
	 * @param a
	 * @param xh
	 * @param bh
	 * @param xxdm
	 * @return
	 */
	private int addZcjl(int xn,int a,String xh,String bh,String xxdm){
		int count = 0;
		for(int i = xn+1;i > xn + Math.abs(a);i++){
			for(int j = 1; j <= 2; j++){
				Map<String,String> par = new HashMap<String,String>();
				par.put("xn", new Integer(i).toString());
				par.put("xqm", new Integer(j).toString());
				par.put("xh", xh);
				par.put("xxdm",xxdm);
				par.put("bh",bh);
				par.put("sfzx","1");
				count = studentDao.addStudenrol(par);
			}
		}
		return count;
	}
}
