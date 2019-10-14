package com.talkweb.ueditor.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.talkweb.filemanager.service.FileServer;
import com.talkweb.ueditor.service.State;
import com.talkweb.ueditor.util.AppInfo;

 
@Component(value = "storageManager")
public class StorageManager{

	@Autowired
	private FileServer fileServerImplFastDFS;
	
 
	
	public static final int BUFFER_SIZE = 8192;
	
	public  State saveBinaryFile(byte[] data) {
		File tmpFile = getTmpFile();
		State state =null;
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(tmpFile));
			bos.write(data);
			bos.flush();
			bos.close();
		} catch (IOException ioe) {
			state = new BaseState();
			state.setProperty(false, null, AppInfo.IO_ERROR);
			return state;
		}

		state = saveTmpFile(tmpFile);
		tmpFile.delete();
		return state;
	}

	public  State saveFileByInputStream(InputStream is, long maxSize ) {
		State state = null;

		File tmpFile = getTmpFile();

		byte[] dataBuf = new byte[ 2048 ];
		BufferedInputStream bis = new BufferedInputStream(is, this.BUFFER_SIZE);

		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(tmpFile), this.BUFFER_SIZE);

			int count = 0;
			while ((count = bis.read(dataBuf)) != -1) {
				bos.write(dataBuf, 0, count);
			}
			bos.flush();
			bos.close();

			if (tmpFile.length() > maxSize) {
				tmpFile.delete();
				state = new BaseState();
				state.setProperty(false, null, AppInfo.MAX_SIZE);
				return state;
			}
			state = saveTmpFile(tmpFile);
			tmpFile.delete();
			return state;
			
		} catch (IOException e) {
		}
		state = new BaseState();
		state.setProperty(false, null, AppInfo.IO_ERROR);
		return state;
	}

	public  State saveFileByInputStream(InputStream is ) {
		State state = null;

		File tmpFile = getTmpFile();

		byte[] dataBuf = new byte[ 2048 ];
		BufferedInputStream bis = new BufferedInputStream(is, this.BUFFER_SIZE);

		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(tmpFile), this.BUFFER_SIZE);

			int count = 0;
			while ((count = bis.read(dataBuf)) != -1) {
				bos.write(dataBuf, 0, count);
			}
			bos.flush();
			bos.close();

			 
			state = saveTmpFile(tmpFile);
			tmpFile.delete();
			return state;
		} catch (IOException e) {
		}
		state = new BaseState();
		state.setProperty(false, null, AppInfo.IO_ERROR);
		return state;
	}
	
	private static File getTmpFile() {
		File tmpDir = FileUtils.getTempDirectory();
		String tmpFileName = (Math.random() * 10000 + "").replace(".", "");
		return new File(tmpDir, tmpFileName);
	}

 
	
	public  State saveTmpFile(File tmpFile) {
 		String url =  null;
 		State state = null;
		try {
 
	       if (tmpFile.length() > 0) {
	    	   for (int i = 0; i <20; i++) {
	    		   url = fileServerImplFastDFS.uploadFile(tmpFile);
	    		   if (StringUtils.isNotEmpty(url)) {
					  break;
				   }
		    	   
			   }
		   }
 
			state = new BaseState();
			state.setProperty(true, null, -100);
			state.putInfo("url",url);
			state.putInfo( "size", tmpFile.length() );
			state.putInfo( "title", tmpFile.getName() );
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return state;
	}

 
	
	
	
}
