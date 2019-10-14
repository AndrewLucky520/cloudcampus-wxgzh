package com.talkweb.ueditor.service.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.talkweb.ueditor.service.State;
import com.talkweb.ueditor.service.UploaderService;
import com.talkweb.ueditor.util.AppInfo;
import com.talkweb.ueditor.util.FileType;

 
@Component(value = "base64")
public class UploaderServiceBase64Impl implements UploaderService{

	@Autowired
	StorageManager storageManager;
	
 
	
	@Override
	public State save(HttpServletRequest request, Map<String, Object> conf) {
		  String filedName = (String) conf.get("fieldName");
			String fileName = request.getParameter(filedName);
			State state = null;
			byte[] data = decode(fileName);

			long maxSize = ((Long) conf.get("maxSize")).longValue();

			if (!validSize(data, maxSize)) {
				state = new BaseState();
				state.setProperty(false, null, AppInfo.MAX_SIZE);
				return state;
			}

			String suffix = FileType.getSuffix("JPG");

 

			State storageState = storageManager.saveBinaryFile(data);

			if (storageState.isSuccess()) {
				storageState.putInfo("url", (String)conf.get("baseUrl") + storageState.gettInfo("url") + suffix + "&origin=tuya.jpg" );
				storageState.putInfo("type", suffix);
				storageState.putInfo("original", "");

			}

			return storageState;
	}
	
 

	private static byte[] decode(String content) {
		return Base64.decodeBase64(content);
	}

	private static boolean validSize(byte[] data, long length) {
		return data.length <= length;
	}
	


}
