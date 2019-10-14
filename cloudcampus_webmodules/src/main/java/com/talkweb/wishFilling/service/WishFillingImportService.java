package com.talkweb.wishFilling.service;

import java.util.Map;

/** 
 * 志愿填报-导入service
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月10日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return
 * @version 2.0 2016年11月3日  author：zhh  
 */
public interface WishFillingImportService {
	int addStudentTbBatch(Map<String,Object> paramMap)throws Exception;
}
