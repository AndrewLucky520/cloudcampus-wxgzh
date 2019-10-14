package com.talkweb.ueditor.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface UploaderService {

	State save(HttpServletRequest request,
			Map<String, Object> conf);
	
}
