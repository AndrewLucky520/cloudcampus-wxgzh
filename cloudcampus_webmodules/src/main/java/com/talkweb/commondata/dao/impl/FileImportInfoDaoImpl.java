package com.talkweb.commondata.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.commondata.dao.FileImportInfoDao;
/**
 * @ClassName FileImportInfoDaoImpl
 * @author zhh
 * @version 1.0
 * @Description 公共文件数据库管理DaoImpl
 * @date 2016年3月7日
 */
@Repository
public class FileImportInfoDaoImpl  extends MyBatisBaseDaoImpl implements FileImportInfoDao{

	@Override
	public String getFileBy(JSONObject param) {
		return selectOne("getFileBy",param);
	}

	@Override
	public void addFile(JSONObject param) {
		 update("addFile",param);
	}

	@Override
	public void deleteFile(JSONObject param) {
		 update("deleteFile",param);
	}

	@Override
	public List<String> getFileIdsBeforeDate(String strDate) {
		return selectList("getFileIdsBeforeDate",strDate);
	}

	@Override
	public int deleteFileIdsBeforeDate(String strDate) {
		return delete("deleteFileIdsBeforeDate",strDate);
	}

	@Override
	public int deleteFileByFileId(String fileId) {
		// TODO Auto-generated method stub
		return delete("deleteFileByFileId", fileId);
	}

	@Override
	public String getFileByFileId(JSONObject param) {
		return selectOne("getFileByFileId",param);
	}

}
