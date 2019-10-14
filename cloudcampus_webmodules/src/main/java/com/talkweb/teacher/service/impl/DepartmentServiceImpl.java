package com.talkweb.teacher.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.talkweb.base.common.OutputMessage;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.teacher.dao.BmxxKmDao;
import com.talkweb.teacher.dao.BmxxNjDao;
import com.talkweb.teacher.dao.DepartmentDao;
import com.talkweb.teacher.domain.page.TTrBmxx;
import com.talkweb.teacher.domain.page.TTrBmxxKm;
import com.talkweb.teacher.domain.page.TTrBmxxNj;
import com.talkweb.teacher.service.DepartmentService;

/**
 * @version 2.0
 * @Description: 教师机构业务逻辑实现类
 * @author 吴安辉
 * @date 2015年3月3日
 */

@Service
public class DepartmentServiceImpl implements DepartmentService {

	@Autowired
	private DepartmentDao departmentDao;
	
	@Autowired
	private BmxxNjDao bmxxNjDao;
	
	@Autowired
	private BmxxKmDao bmxxKmDao;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	
	//新增科室部门信息
	public Map<String,Object> addKsInfo(TTrBmxx bmxx){
		Map<String,Object> data = new HashMap<String,Object>();
		int count = departmentDao.addDepartment(bmxx);
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

	//修改部门信息
	@Override
	public Map<String, Object> updateKsInfo(TTrBmxx bmxx) {
		Map<String,Object> data = new HashMap<String,Object>();
		int count = departmentDao.updateDepartment(bmxx);
		if(count > 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.updateDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.updateDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//删除科室信息
	@Override
	public Map<String, Object> deleteKsInfo(String[] bmbh) {
		Map<String,Object> data = new HashMap<String,Object>();
		int count = 0;
		for(String code : bmbh){
			count = departmentDao.deleteDepartment(code);
			if(count < 0){
				break;
			}
		}
		if(count >= 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.delDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.delDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//查询科室列表
	@Override
	public Map<String, Object> getKsList(TTrBmxx bmxx){
		Map<String,Object> data = new HashMap<String,Object>();
		List<Map<String,Object>> list = departmentDao.getDepartmentList(bmxx);
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		if(list != null){
			for(int i = 0; i < list.size();i++){
				Map<String,Object> map = list.get(i);
				if(map.get("bmrs") == null){
					map.put("bmrs", "0");
				}
				if(map.get("fzrgh") == null){
					map.put("bmld", "");
					result.add(map);
				}else{
					boolean flag = false;
					for(int j = 0; j < result.size();j++){
						Map<String,Object> obj = result.get(j);
						if(obj.get("jgh").equals(map.get("jgh"))){
							if(map.get("xm") != null){
								obj.put("bmld", obj.get("bmld")+","+map.get("xm"));
							}else{
								obj.put("bmld", obj.get("bmld")+","+"");
							}
							flag = true;
							break;
						}
					}
					if(!flag){
						if(map.get("xm")==null){
							map.put("bmld", "");
						}else{
							map.put("bmld", map.get("xm"));
						}
						result.add(map);
					}
				}
			}
			data.put("total",result.size());
		}else{
			data.put("total",0);
		}
		data.put("rows",result);
		return data;
	}

	//查询单个科室
	@Override
	public TTrBmxx getOneKsInfo(TTrBmxx bmxx) {
		TTrBmxx bean = null;
		bean = departmentDao.getOneDepartment(bmxx);
		return bean;
	}

	//新增年级组
	@Override
	public Map<String, Object> addGrade(TTrBmxx bmxx, String grade,String xn) {
		Map<String,Object> data = new HashMap<String,Object>();
		//添加部门
		departmentDao.addDepartment(bmxx);
		//添加部门管理年级
		TTrBmxxNj bmnj = new TTrBmxxNj();
		bmnj.setXxdm(bmxx.getXxdm());
		bmnj.setJgh(bmxx.getJgh());
		String njdm = commonDataService.ConvertSYNJ2NJDM(grade, xn);
		bmnj.setNj(njdm);
		int count = bmxxNjDao.addBmxxNjInfo(bmnj);
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

	//删除年级组
	@Override
	public Map<String, Object> deleteGrade(String[] grades) {
		Map<String,Object> data = new HashMap<String,Object>();
		int count = 0;
		for(String grade : grades){
			//删除机构
			departmentDao.deleteDepartment(grade);
			//删除部门管理年级
			count = bmxxNjDao.deleteBmxxNjInfo(grade);
			if(count < 0){
				break;
			}
		}
		if(count >= 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.delDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.delDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//修改年级组
	@Override
	public Map<String, Object> updateGrade(TTrBmxx bmxx) {
		Map<String,Object> data = new HashMap<String,Object>();
		//List<TTrBmxxNj> list = bmxxNjDao.getBmxxNjListByJgh(bmxx.getJgh());
		//修改部门表
		int count = departmentDao.updateDepartment(bmxx);
		if(count > 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.updateDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.updateDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//查询单个年级组
	@Override
	public Map<String, Object> getOneGrade(TTrBmxx bmxx,String xn) {
		Map<String,Object> data = new HashMap<String,Object>();
		data = departmentDao.getOneGrade(bmxx);
		String grade = data.get("grade").toString();
		String synj = commonDataService.ConvertNJDM2SYNJ(grade, xn);
		data.put("grade", synj);
		return data;
	}

	//查询年级组列表
	@Override
	public Map<String, Object> getGradeList(TTrBmxx bmxx) {
		Map<String,Object> data = new HashMap<String,Object>();
		List<Map<String,Object>> list = departmentDao.getGradeList(bmxx);
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		if(list != null){
			for(int i = 0; i < list.size();i++){
				Map<String,Object> map = list.get(i);
				if(map.get("bmrs") == null){
					map.put("bmrs", "0");
				}
				if(map.get("fzrgh") == null){
					map.put("bmld", "");
					result.add(map);
				}else{
					boolean flag = false;
					for(int j = 0; j < result.size();j++){
						Map<String,Object> obj = result.get(j);
						if(obj.get("jgh").equals(map.get("jgh"))){
							if(map.get("xm") != null){
								obj.put("bmld", obj.get("bmld")+","+map.get("xm"));
							}else{
								obj.put("bmld", obj.get("bmld")+","+"");
							}
							flag = true;
							break;
						}
					}
					if(!flag){
						if(map.get("xm")==null){
							map.put("bmld", "");
						}else{
							map.put("bmld", map.get("xm"));
						}
						map.remove("xm");
						result.add(map);
					}
				}
			}
			data.put("total",result.size());
		}else{
			data.put("total",0);
		}
		data.put("rows",result);
		return data;
	}

	//查询教研组列表
	@Override
	public Map<String, Object> getResearchGradeList(TTrBmxx bmxx) {
		Map<String,Object> data = new HashMap<String,Object>();
		List<Map<String,Object>> list = departmentDao.getResearchGroupList(bmxx);
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		if(list != null){
			for(int i = 0; i < list.size();i++){
				Map<String,Object> map = list.get(i);
				if(i == 0){
					if(map.get("bmrs") == null){
						map.put("bmrs", "0");
					}
					if(map.get("fzrgh") == null){
						map.put("bmld", "");
					}else{
						//map.put("bmld", map.get("xm"));
						if(map.get("xm")==null){
							map.put("bmld", "");
						}else{
							map.put("bmld", map.get("xm"));
						}
					}
					//科目
					map.put("grade",map.get("zwmc"));
					//移除多余字段
					map.remove("xm");
					map.remove("zwmc");
					map.remove("jykm");
					result.add(map);
				}else{
					boolean flag = false;
					map.put("grade",map.get("zwmc"));
					for(int j = 0; j < result.size();j++){
						Map<String,Object> obj = result.get(j);
						if(obj.get("jgh").equals(map.get("jgh"))){
							if(obj.get("bmld") != null && map.get("xm")!= null){
								if(obj.get("bmld").toString().indexOf(map.get("xm").toString()) < 0){
									obj.put("bmld", obj.get("bmld")+","+map.get("xm"));
								}
							}
							if(obj.get("grade") != null && map.get("zwmc") != null){
								if(obj.get("grade").toString().indexOf(map.get("zwmc").toString()) < 0){
									obj.put("grade", obj.get("grade")+","+map.get("zwmc"));
								}
							}
							flag = true;
							break;
						}
					}
					if(!flag){
						if(map.get("bmrs") == null){
							map.put("bmrs", "0");
						}
						if(map.get("fzrgh") == null){
							map.put("bmld", "");
						}else{
							//map.put("bmld", map.get("xm"));
							if(map.get("xm")==null){
								map.put("bmld", "");
							}else{
								map.put("bmld", map.get("xm"));
							}
						}
						//移除多余字段
						map.remove("xm");
						map.remove("zwmc");
						map.remove("jykm");
						result.add(map);
					}
				}
			}
			data.put("total",result.size());
		}else{
			data.put("total",0);
		}
		data.put("rows",result);
		return data;
	}

	//新增教研组
	@Override
	public Map<String, Object> addResearchGrade(TTrBmxx bmxx, String[] kmdms) {
		Map<String,Object> data = new HashMap<String,Object>();
		int count = 0;
		//新增部门
		departmentDao.addDepartment(bmxx);
		//新增教研科目
		for(String kmdm : kmdms){
			TTrBmxxKm bmkm = new TTrBmxxKm();
			bmkm.setJgh(bmxx.getJgh());
			bmkm.setXxdm(bmxx.getXxdm());
			bmkm.setYjkm(kmdm);
			count = bmxxKmDao.addBmxxKm(bmkm);
			if(count < 0){
				break;
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

	//修改教研组
	@Override
	public Map<String, Object> updateResearchGrade(TTrBmxx bmxx, String[] kmdms) {
		Map<String,Object> data = new HashMap<String,Object>();

		bmxxKmDao.deleteBmxxKm(bmxx.getJgh());
		for(String kmdm : kmdms){
			TTrBmxxKm bmkm = new TTrBmxxKm();
			bmkm.setJgh(bmxx.getJgh());
			bmkm.setXxdm(bmxx.getXxdm());
			bmkm.setYjkm(kmdm);
			bmxxKmDao.addBmxxKm(bmkm);
		}
		int count = departmentDao.updateDepartment(bmxx);
		if(count > 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.updateDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.updateDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//查询单个教研组
	@Override
	public Map<String, Object> getOneResearchGrade(TTrBmxx bmxx) {
		Map<String,Object> data = new HashMap<String,Object>();
		List<Map<String,Object>> list = departmentDao.getOneResearchGroup(bmxx);
		for(int i = 0; i < list.size();i++){
			Map<String,Object> map = list.get(i);
			if(i==0){
				data.put("jyzdm", map.get("yjkm"));
				data.put("jgh", map.get("jgh"));
				data.put("jgmc", map.get("jgmc"));
			}else{
				data.put("jyzdm", data.get("jyzdm")+","+map.get("yjkm"));
			}
		}
		return data;
	}

	//删除教研组
	@Override
	public Map<String, Object> deleteResearchGrade(String[] bmdm) {
		Map<String,Object> data = new HashMap<String,Object>();
		int count = 0;
		for(String dm : bmdm){
			//先删除教研科目
			bmxxKmDao.deleteBmxxKm(dm);
			//再删除部门信息
			count = departmentDao.deleteDepartment(dm);
			if(count < 0){
				break;
			}
		}
		if(count >= 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.delDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.delDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//查询备课组列表
	@Override
	public Map<String, Object> getLessonPlanningGroupList(TTrBmxx bmxx) {
		Map<String,Object> data = new HashMap<String,Object>();
		List<Map<String,Object>> list = departmentDao.getLessonPlanningGroupList(bmxx);
		//System.out.println(JSON.toJSONString(list));
		List<String> jghs = new ArrayList<String>();
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		for(int i = 0; i < list.size();i++){
			Map<String,Object> map = list.get(i);
			if(map.get("bmrs") == null){
				map.put("bmrs", "0");
			}
			if(map.get("fzrgh") == null){
				map.put("bmld", "");
				result.add(map);
			}else{
				boolean flag = false;
				for(int j = 0; j < result.size();j++){
					Map<String,Object> obj = result.get(j);
					if(obj.get("jgh").equals(map.get("jgh"))){
						if(map.get("xm")!= null){
							obj.put("bmld", obj.get("bmld")+","+map.get("xm"));
						}else{
							obj.put("bmld", obj.get("bmld")+","+"");
						}
						flag = true;
						break;
					}
				}
				if(!flag){
					if(map.get("xm")==null){
						map.put("bmld", "");
					}else{
						map.put("bmld", map.get("xm"));
					}
					map.remove("xm");
					result.add(map);
				}
			}
		}
		for(Map<String,Object> map : result){
			jghs.add(map.get("jgh").toString());
		}
		List<Map<String,Object>> kmList = departmentDao.getLessonPlanningGroupYjkmList(jghs);
		for(int i = 0; i < result.size();i++){
			Map<String,Object> pMap = result.get(i);
			for(Map<String,Object> map : kmList){
				if(pMap.get("jgh").equals(map.get("jgh"))){
					if(pMap.get("yjkm")==null){
						pMap.put("yjkm", map.get("zwmc"));
					}else{
						pMap.put("yjkm", pMap.get("yjkm")+","+map.get("zwmc"));
					}
				}
			}
		}
		data.put("total", result.size());
		data.put("rows", result);
		return data;
	}

	//新增备课组
	@Override
	public Map<String, Object> addLessonPlanningGroup(TTrBmxx bmxx,
			String[] jyzdm, String[] njzdm,String xn) {
		Map<String,Object> data = new HashMap<String,Object>();
		int count = 0;
		//添加部门
		count = departmentDao.addDepartment(bmxx);
		//循环添加部门管理年级
		for(String dm : njzdm){
			TTrBmxxNj bmnj = new TTrBmxxNj();
			bmnj.setJgh(bmxx.getJgh());
			String njdm = commonDataService.ConvertSYNJ2NJDM(dm, xn);
			bmnj.setNj(njdm);
			bmnj.setXxdm(bmxx.getXxdm());
			bmxxNjDao.addBmxxNjInfo(bmnj);
		}
		//循环添加备课组备课科目
		for(String dm : jyzdm){
			TTrBmxxKm kmxx = new TTrBmxxKm();
			kmxx.setJgh(bmxx.getJgh());
			kmxx.setXxdm(bmxx.getXxdm());
			kmxx.setYjkm(dm);
			count = bmxxKmDao.addBmxxKm(kmxx);
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

	//修改备课组
	@Override
	public Map<String, Object> updateLessonPlanningGroup(TTrBmxx bmxx,
			String[] jyzdm, String[] njzdm,String xn) {
		Map<String,Object> data = new HashMap<String,Object>();
		int count = 0;
		bmxxNjDao.deleteBmxxNjInfo(bmxx.getJgh());
		//循环添加部门管理年级
		for(String dm : njzdm){
			TTrBmxxNj bmnj = new TTrBmxxNj();
			bmnj.setJgh(bmxx.getJgh());
			String njdm = commonDataService.ConvertSYNJ2NJDM(dm, xn);
			bmnj.setNj(njdm);
			bmnj.setXxdm(bmxx.getXxdm());
			bmxxNjDao.addBmxxNjInfo(bmnj);
		}
		bmxxKmDao.deleteBmxxKm(bmxx.getJgh());
		//循环添加备课组备课科目
		for(String dm : jyzdm){
			TTrBmxxKm kmxx = new TTrBmxxKm();
			kmxx.setJgh(bmxx.getJgh());
			kmxx.setXxdm(bmxx.getXxdm());
			kmxx.setYjkm(dm);
			bmxxKmDao.addBmxxKm(kmxx);
		}
		count = departmentDao.updateDepartment(bmxx);
		if(count > 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.updateDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.updateDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//查询单个备课组
	@Override
	public Map<String, Object> getOneLessonPlanningGroup(TTrBmxx bmxx,String xn) {
		Map<String,Object> data = new HashMap<String,Object>();
		List<Map<String,Object>> list = departmentDao.getOneLessonPlanningGroup(bmxx);
		for(int i = 0; i < list.size();i++){
			Map<String,Object> map = list.get(i);
			if(i == 0){
				data.put("jgh", map.get("jgh"));
				data.put("jgmc", map.get("jgmc"));
				data.put("jyzdm", map.get("yjkm"));
				String synj = commonDataService.ConvertNJDM2SYNJ(map.get("nj").toString(), xn);
				data.put("njdm", synj);
			}else{
				if(data.get("jyzdm").toString().indexOf(map.get("yjkm").toString()) < 0){
					data.put("jyzdm", data.get("jyzdm")+","+map.get("yjkm"));
				}
				String synj = commonDataService.ConvertNJDM2SYNJ(map.get("nj").toString(), xn);
				if(data.get("njdm").toString().indexOf(synj) < 0){
					data.put("njdm", data.get("njdm")+","+synj);
				}
			}
		}
		return data;
	}

	//删除备课组
	@Override
	public Map<String, Object> deleteLessonPlanningGroup(String[] jgdm) {
		Map<String,Object> data = new HashMap<String,Object>();
		int count = 0;
		for(String dm : jgdm){
			bmxxNjDao.deleteBmxxNjInfo(dm);
			bmxxKmDao.deleteBmxxKm(dm);
			count = departmentDao.deleteDepartment(dm);
			if(count < 0){
				break;
			}
		}
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
	
}
