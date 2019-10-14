/**
 * 
 */
package com.talkweb.filemanager.service;

import java.io.File;
import java.io.IOException;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.FileInfo;

/**
 * @ClassName: FileServer
 * @version:1.0
 * @Description: 文件管理
 * @author 廖刚 ---智慧校
 * @date 2015年11月25日
 */
public interface FileServer {
	
	
 

	/**
	 * upload file to storage server (by file)
	 * 
	 * @param file
	 *            file content
	 * @return file id(including group name and filename) if success, <br>
	 *         return null if fail
	 */ 
	public String uploadFile(File file) throws IOException, Exception;

	/**
	 * upload file to storage server (by file)
	 * 
	 * @param file
	 *            file content
	 * @param fileNme
	 *            file name
	 * @return file id(including group name and filename) if success, <br>
	 *         return null if fail
	 */
	public String uploadFile(File file, String fileName) throws IOException,
			Exception;

	/**
	 * upload file to storage server (by file buff)
	 * 
	 * @param fileBuff
	 *            file content/buff
	 * @param fileNme
	 *            file name
	 * @return file id(including group name and filename) if success, <br>
	 *         return null if fail
	 */
	public String uploadFile(byte[] fileBuff, String fileName)
			throws IOException, Exception;

	/**
	 * upload file to storage server (by file buff)
	 * 
	 * @param fileBuff
	 *            file content/buff
	 * @param fileExtName
	 *            file ext name, do not include dot(.)
	 * @param metaList
	 *            meta info array
	 * @return file id(including group name and filename) if success, <br>
	 *         return null if fail
	 */
	public String uploadFile(byte[] fileBuff, String fileExtName,
			NameValuePair[] metaList) throws IOException, Exception;

	/**
	 * get file info from storage server
	 * 
	 * @param fileID
	 *            the file id(including group name and filename)
	 * @return FileInfo object for success, return null for fail
	 */
	public FileInfo queryFileInfo(String fileID) throws IOException, Exception;

	/**
	 * delete file from storage server
	 * 
	 * @param fileID
	 *            the file id(including group name and filename)
	 * @return 0 for success, none zero for fail (error code)
	 */
	public int deleteFile(String fileID) throws IOException, Exception;

	/**
	 * download file from storage server
	 * 
	 * @param fileID
	 *            the file id(including group name and filename)
	 * @return file content/buffer, return null if fail
	 */
	public byte[] downloadFile(String fileID) throws IOException, Exception;

	/**
	 * download file from storage server
	 * 
	 * @param fileID
	 *            the file id(including group name and filename)
	 * @param localFilenName
	 *            the filename on local
	 * @return 0 success, return none zero errno if fail
	 */
	public int downloadFile(String fileID, String localFilenName)
			throws IOException, Exception;

}
