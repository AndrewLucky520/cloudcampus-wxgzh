package com.talkweb.commondata.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName FileImportInfoDao
 * @author zhh
 * @version 1.0
 * @Description 公共文件数据库管理Dao
 * @date 2016年3月7日
 */

public interface FileImportInfoDao {
   String getFileBy(JSONObject  param);
   String getFileByFileId(JSONObject  param);
   void addFile(JSONObject  param);
   void deleteFile(JSONObject  param);
List<String> getFileIdsBeforeDate(String strDate);
int deleteFileIdsBeforeDate(String strDate);
int deleteFileByFileId(String fileId);
}
