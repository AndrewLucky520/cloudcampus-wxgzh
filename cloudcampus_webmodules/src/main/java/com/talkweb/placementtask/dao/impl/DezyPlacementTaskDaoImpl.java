package com.talkweb.placementtask.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.placementtask.dao.DezyPlacementTaskDao;
import com.talkweb.placementtask.domain.TPlDezyAdvancedOpt;
import com.talkweb.placementtask.domain.TPlDezyAutorize;
import com.talkweb.placementtask.domain.TPlDzbClassLevel;

@Repository
public class DezyPlacementTaskDaoImpl extends MyBatisBaseDaoImpl implements DezyPlacementTaskDao {
	Logger logger = LoggerFactory.getLogger(DezyPlacementTaskDaoImpl.class);
	
	@Override
	public int insertDezyPreSetting(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return insert("com.talkweb.placementtask.dao.insertDezyPreSetting", obj);
	}

	@Override
	public int insertDezySubjectGroup(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return insert("com.talkweb.placementtask.dao.insertDezySubjectGroup", obj);
	}

	@Override
	public int insertDezySubjectSet(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return insert("com.talkweb.placementtask.dao.insertDezySubjectSet", obj);
	}

	@Override
	public JSONObject queryDezyPreSetting(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectOne("com.talkweb.placementtask.dao.queryDezyPreSetting", obj);
	}

	@Override
	public <T> void updateBatchInsertEntity(String termInfo,List<T> dataList) throws Exception {
		// TODO Auto-generated method stub
		int size = dataList.size();
		//数据量过大时分批提交
		if(size>5000){
			int b = 0;
			
			while(b<size){
				b+=2000;
				
				int start = b-2000;
				if(b>size){
					b = size;
				}
				List<T> sublist = dataList.subList(start, b);
				this.updateBatchInsertEntityChild(termInfo,sublist);
			}
		}else{
			this.updateBatchInsertEntityChild(termInfo,dataList);
		}
	}

	private<T> void updateBatchInsertEntityChild(String termInfo,List<T> dataList) throws Exception {
		if(dataList.size()>0){
			String cla = dataList.get(0).getClass().getSimpleName();
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("termInfo", termInfo);
			map.put("list", dataList);
			update("com.talkweb.placementtask.dao.batchInsert"+cla+"List",map);
		}
	}

	@Override
	public String queryLatestDivData(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectOne("com.talkweb.placementtask.dao.queryLatestDivData",obj);
	}

	@Override
	public List<String> queryTableColumnNames(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.queryTableColumnNames",obj);
	}

	@Override
	public int batchInsertTPlDezyTableFromItself(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return update("com.talkweb.placementtask.dao.batchInsertTPlDezyTableFromItself",obj);
	}

	@Override
	public List<TPlDezyAutorize> queryDezyAutorize(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.TPlDezyAutorize",obj);
	}

	@Override
	public List<TPlDezyAdvancedOpt> queryDezyAdvancedOpt(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getAdvancedOptList",obj);
	}

	@Override
	public int recoverClassName(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return update("com.talkweb.placementtask.dao.recoverClassName", obj);
	}

	@Override
	public void updateClearLargeResult(JSONObject obj) {
		// TODO Auto-generated method stub
		 update("com.talkweb.placementtask.dao.updateClearLargeResult",obj);
	}

	@Override
	public JSONObject getDzbPreSettings(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectOne("com.talkweb.placementtask.dao.getDzbPreSettings", obj);
	}

	@Override
	public List<TPlDzbClassLevel> getDzbClassLevel(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getDzbClassLevel", obj);
	}

	@Override
	public List<JSONObject> getDzbDivResult(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getDzbDivResult", obj);
	}

	@Override
	public List<JSONObject> queryDzbStuClassInfoDetail(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getDzbStuClassInfoDetail", obj);
	}

	@Override
	public List<JSONObject> getDzbDivQueryParams(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.getDzbDivQueryParams", obj);
	}

	@Override
	public List<JSONObject> queryDzbTeachingResource(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectList("queryDzbTeachingResource", obj);
	}

	@Override
	public List<JSONObject> queryDzbMainTable(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectList("getDzbMainTable", obj);
		
	}
	
	@Override
	public List<JSONObject> queryDzbPreview(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectList("queryDzbPreview", obj);
		
	}
	

	@Override
	public List<JSONObject> selectGroundIdBysubjectIds(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.placementtask.dao.selectGroundIdBysubjectIds",obj);
	}
	
	
}
