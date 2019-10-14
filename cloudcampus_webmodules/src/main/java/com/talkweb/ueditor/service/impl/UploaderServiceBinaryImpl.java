package com.talkweb.ueditor.service.impl;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.talkweb.ueditor.service.State;
import com.talkweb.ueditor.service.UploaderService;
import com.talkweb.ueditor.util.AppInfo;
import com.talkweb.ueditor.util.FileType;

 
@Component(value = "binary")
public class UploaderServiceBinaryImpl implements UploaderService{
	
	@Autowired
	StorageManager storageManager;
 
	
	@Override
	public State save(HttpServletRequest request, Map<String, Object> conf) {

		State state = null;
		boolean isAjaxUpload = request.getHeader( "X_Requested_With" ) != null;
		if (!ServletFileUpload.isMultipartContent(request)) {
			state = new BaseState();
		    state.setProperty(false, null, AppInfo.NOT_MULTIPART_CONTENT);
			return state;
		}
		ServletFileUpload upload = new ServletFileUpload(
				new DiskFileItemFactory());
        if ( isAjaxUpload ) {
            upload.setHeaderEncoding( "UTF-8" );
        }

		try {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request; 
			MultipartFile  file =  multiRequest.getFile("upfile");
 
			if (file == null) {
				 state = new BaseState();
				 state.setProperty(false, null, AppInfo.NOTFOUND_UPLOAD_DATA);
				 return state;
			}

		 
			String originFileName = file.getOriginalFilename();
			String suffix = FileType.getSuffixByFilename(originFileName);

			originFileName = originFileName.substring(0,
					originFileName.length() - suffix.length());
			 

			long maxSize = ((Long) conf.get("maxSize")).longValue();

			if (!validType(suffix, (String[]) conf.get("allowFiles"))) {
				state = new BaseState();
				state.setProperty(false, null, AppInfo.NOT_ALLOW_FILE_TYPE);
				return state;
			}

			InputStream is = file.getInputStream();
			State storageState = storageManager.saveFileByInputStream(is, maxSize );
			is.close();

			if (storageState.isSuccess()) {
				
				storageState.putInfo("type", suffix);
				storageState.putInfo("original", originFileName + suffix);
				storageState.putInfo("url", (String)conf.get("baseUrl") + storageState.gettInfo("url")  + suffix  + "&origin=/" + storageState.gettInfo("original"));

			}
			
			return storageState;
		}catch (Exception e) {
		}
		state = new BaseState();
		state.setProperty(false, null, AppInfo.IO_ERROR);
		return state;
	}

	private static boolean validType(String type, String[] allowTypes) {
		List<String> list = Arrays.asList(allowTypes);
		return list.contains(type);
	}
	
	
	
}
