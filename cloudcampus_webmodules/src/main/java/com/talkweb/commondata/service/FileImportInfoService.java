package com.talkweb.commondata.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.commondata.dao.FileImportInfoDao;
/**
 * @ClassName FileImportInfoService
 * @author zhh
 * @version 1.0
 * @Description 公共文件数据库管理Service
 * @date 2016年3月7日
 */
@Service
public class FileImportInfoService {
	@Autowired
	private FileImportInfoDao fileImportInfoDao;
	/**
	 * 获取
	 * @param schoolId 必传
	 * @param keyId必传
	 * @return
	 * @author zhh
	 */
	public String getFileBy(String schoolId,String keyId){
		JSONObject param= new JSONObject();
		param.put("schoolId", schoolId);
		param.put("keyId", keyId);
		return fileImportInfoDao.getFileBy(param);
	}
	/**
	 *添加 
	 * @param schoolId 必传
	 * @param keyId  必传
	 * @param fileId  必传
	 * @author zhh
	 */
	public void addFile(String schoolId,String keyId,String fileId){
		JSONObject param= new JSONObject();
		param.put("schoolId", schoolId);
		param.put("keyId", keyId);
		param.put("createDate", new Date());
		param.put("fileId", fileId);
		 fileImportInfoDao.addFile(param);
	}
	
	/**
	 * 删除
	 * @param schoolId 必传
	 * @param keyId  必传
	 * @param fileId 
	 * @author zhh
	 */
	public void deleteFile(String schoolId,String keyId){
		JSONObject param= new JSONObject();
		param.put("schoolId", schoolId);
		param.put("keyId", keyId);
	    fileImportInfoDao.deleteFile(param);
	}
	/**
	 * 获取给定时间之前的所有fileIds
	 * @param endDate
	 * @return
	 * @author wxq
	 */
	public List<String> getFileIdsBeforeDate(Date endDate){
		SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		String strDate = df.format(endDate);
		List<String> ls = fileImportInfoDao.getFileIdsBeforeDate(strDate);
		return ls;
	}
	/**
	 * 删除给定时间之前的记录
	 * @param endDate
	 * @return 受影响的行数
	 * @author wxq
	 */
	public int deleteFileIdsBeforeDate(Date endDate){
		SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		String strDate = df.format(endDate);
		return fileImportInfoDao.deleteFileIdsBeforeDate(strDate);
	}
	/**
	 * 根据文件ID删除文件
	 * @param fileId
	 * @return 
	 * @author wxq
	 */
	public int deleteFileByFileId(String fileId){
		return fileImportInfoDao.deleteFileByFileId(fileId);
	}
	
	
	public String getFileByFileId(String schoolId,String fileId){
		JSONObject param= new JSONObject();
		param.put("schoolId", schoolId);
		param.put("fileId", fileId);
		return fileImportInfoDao.getFileByFileId(param);
	}
	
}
