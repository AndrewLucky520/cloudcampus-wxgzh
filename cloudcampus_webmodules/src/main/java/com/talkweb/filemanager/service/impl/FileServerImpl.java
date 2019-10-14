/**
 * 
 */
package com.talkweb.filemanager.service.impl;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerGroup;
import org.csource.fastdfs.TrackerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.PrintStackTrace;
import com.talkweb.common.tools.ValidType;
import com.talkweb.commondata.service.FileImportInfoService;
import com.talkweb.filemanager.service.FileServer;

/**
 * @ClassName: FileServerImpl
 * @version:1.0
 * @Description: 文件管理
 * @author 廖刚 ---智慧校
 * @date 2015年11月27日
 */
@Component(value = "fileServerImplFastDFS")
public class FileServerImpl implements FileServer {
	
	
	@Autowired
	private FileImportInfoService fileImportInfoService;
	
	/*
	private static final Logger logger = LoggerFactory
			.getLogger(FileServerImpl.class);

	*//**
	 * upload file to storage server (by file)
	 * 
	 * @param file
	 *            file content
	 * @return file id(including group name and filename) if success, <br>
	 *         return null if fail
	 *//*
	public String uploadFile(File file) throws IOException, Exception {
		return uploadFile(file, file.getName());
	}

	*//**
	 * upload file to storage server (by file)
	 * 
	 * @param file
	 *            file content
	 * @param fileNme
	 *            file name
	 * @return file id(including group name and filename) if success, <br>
	 *         return null if fail
	 *//*
	public String uploadFile(File file, String fileName) throws IOException,
			Exception {
		byte[] fileBuff = getFileBuffer(file);
		String fileExtName = getFileExtName(fileName);
		return uploadFile(fileBuff, fileExtName, null);
	}

	*//**
	 * upload file to storage server (by file buff)
	 * 
	 * @param fileBuff
	 *            file content/buff
	 * @param fileNme
	 *            file name
	 * @return file id(including group name and filename) if success, <br>
	 *         return null if fail
	 *//*
	public String uploadFile(byte[] fileBuff, String fileName)
			throws IOException, Exception {
		String fileExtName = getFileExtName(fileName);
		return uploadFile(fileBuff, fileExtName, null);
	}

	*//**
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
	 *//*
	public String uploadFile(byte[] fileBuff, String fileExtName,
			NameValuePair[] metaList) throws IOException, Exception {
		String upPath = null;
		StorageClient1 client1 = null;
		try {
			client1 = checkout(10);
			upPath = client1.upload_file1(fileBuff, fileExtName, metaList);
			checkin(client1);
		} catch (InterruptedException e) {
			pintErrorLog(this.getClass().getName(), e);
		} catch (Exception e) {
			drop(client1);
			pintErrorLog(this.getClass().getName(), e);
		}
		return upPath;
	}

	*//**
	 * get file info from storage server
	 * 
	 * @param fileID
	 *            the file id(including group name and filename)
	 * @return FileInfo object for success, return null for fail
	 *//*
	public FileInfo queryFileInfo(String fileID) throws IOException, Exception {
		StorageClient1 client1 = null;
		FileInfo fileInfo = null;
		try {
			client1 = checkout(10);
			fileInfo = client1.query_file_info1(fileID);
			checkin(client1);
		} catch (InterruptedException e) {
			pintErrorLog(this.getClass().getName(), e);
		} catch (Exception e) {
			drop(client1);
			pintErrorLog(this.getClass().getName(), e);
		}
		return fileInfo;
	}

	*//**
	 * delete file from storage server
	 * 
	 * @param fileID
	 *            the file id(including group name and filename)
	 * @return 0 for success, none zero for fail (error code)
	 *//*
	public int deleteFile(String fileID) throws IOException, Exception {
		StorageClient1 client1 = null;
		int result = 0;
		try {
			client1 = checkout(10);
			result = client1.delete_file1(fileID);
			checkin(client1);
		} catch (InterruptedException e) {
			pintErrorLog(this.getClass().getName(), e);
		} catch (Exception e) {
			drop(client1);
			pintErrorLog(this.getClass().getName(), e);
		}
		return result;
	}

	*//**
	 * download file from storage server
	 * 
	 * @param fileID
	 *            the file id(including group name and filename)
	 * @param localFilenName
	 *            the filename on local
	 * @return 0 success, return none zero errno if fail
	 *//*
	public int downloadFile(String fileID, String localFilenName)
			throws IOException, Exception {
		StorageClient1 client1 = null;
		int result = 0;
		try {
			client1 = checkout(10);
			if(client1==null){
				logger.error("downLoadFile checkout(10) get null client");
			}
			logger.info("下载文件参数，fileID:{},localFilenName:{}",fileID,localFilenName);
			result = client1.download_file1(fileID, localFilenName);
			logger.info("下载文件结果代码:{}",result);
			checkin(client1);
		} catch (InterruptedException e) {
			pintErrorLog(this.getClass().getName(), e);
		} catch (Exception e) {
			drop(client1);
			pintErrorLog(this.getClass().getName(), e);
		}
		return result;
	}

	*//**
	 * download file from storage server
	 * 
	 * @param fileID
	 *            the file id(including group name and filename)
	 * @return file content/buffer, return null if fail
	 *//*
	public byte[] downloadFile(String fileID) throws IOException, Exception {
		StorageClient1 client1 = null;
		byte[] result = null;
		try {
			client1 = checkout(10);
			if(client1==null){
				logger.error("downLoadFile checkout(10) get null client");
			}
			logger.info("下载文件参数，fileID:{} ",fileID);
			result = client1.download_file1(fileID);
			checkin(client1);
		} catch (InterruptedException e) {
			// 确实没有空闲连接,并不需要删除与fastdfs连接
			pintErrorLog(this.getClass().getName(), e);
		} catch (Exception e) {
			// 发生io异常等其它异常，默认删除这次连接重新申请
			drop(client1);
			pintErrorLog(this.getClass().getName(), e);
		}
		return result;
	}

	private String getFileExtName(String name) {
		String extName = null;
		if (name != null && name.contains(".")) {
			extName = name.substring(name.lastIndexOf(".") + 1);
		}
		return extName;
	}

	private byte[] getFileBuffer(File file) {
		byte[] fileByte = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			fileByte = new byte[fis.available()];
			fis.read(fileByte);
			fis.close();
		} catch (FileNotFoundException e) {
			pintErrorLog(this.getClass().getName(), e);
		} catch (IOException e) {
			pintErrorLog(this.getClass().getName(), e);
		}
		return fileByte;
	}

	private void pintErrorLog(String classNme, Exception e) {
		logger.error(
				"异常方法:\r\n{}\r\n异常代码:\r\n{}\r\n异常信息:\r\n{}\r\nprintStackTrace:\r\n{}",
				classNme, e.getClass().getName(), e.getMessage(),
				PrintStackTrace.getErrorInfoFromException(e));
	}
	
	
	@Value("#{configProperties['fastdfs.connectTimeout']}")
	private String fastdfsConnectTimeoutStr;
	@Value("#{configProperties['fastdfs.networkTimeout']}")
	private String fastdfsNetworkTimeoutStr;
	@Value("#{configProperties['fastdfs.charset']}")
	private String fastdfsCharsetStr;
	@Value("#{configProperties['fastdfs.http.antiStealToken']}")
	private String fastdfsHttpAntiStealTokenStr;
	@Value("#{configProperties['fastdfs.http.secretkey']}")
	private String fastdfsHttpSecretkeyStr;
	@Value("#{configProperties['fastdfs.trackerServerCount']}")
	private String fastdfsTrackerServerCountStr;
	@Value("#{configProperties['fastdfs.http.trackerHttpPort']}")
	private String fastdfsHttpTrackerHttpPortStr;
	@Value("#{configProperties['fastdfs.trackerServerAddress1']}")
	private String fastdfsTrackerServerAddressStr1;
	@Value("#{configProperties['fastdfs.trackerServerPort1']}")
	private String fastdfsTrackerServerPortStr1;
	@Value("#{configProperties['fastdfs.connectionPoolSize']}")
	private String fastdfsconnectionPoolSizeStr;
	int fastdfsconnectionPoolSize = 1;
	// busy connection instances
	private ConcurrentHashMap<StorageClient1, Object> busyConnectionPool = null;
	// idle connection instances
	private ArrayBlockingQueue<StorageClient1> idleConnectionPool = null;
	private Object obj = new Object();

	// class method
	// singleton
	public FileServerImpl() {
		fastdfsConnectTimeoutStr = "2000";
		fastdfsNetworkTimeoutStr = "30000";
		fastdfsCharsetStr = "UTF-8";
		fastdfsHttpAntiStealTokenStr = "false";
		fastdfsHttpSecretkeyStr = "fastdfsp@ssw0rd";
		ResourceBundle rb = ResourceBundle.getBundle("config.fastdfs" ); 
		if(fastdfsTrackerServerAddressStr1==null){
			fastdfsTrackerServerAddressStr1 = rb.getString("fastdfs.trackerServerAddress1");
			fastdfsTrackerServerPortStr1 = rb.getString("fastdfs.trackerServerPort1");
			
		}
		fastdfsconnectionPoolSizeStr = "3";
		fastdfsTrackerServerCountStr = "1";
		
		if (ValidType.isValidInt(fastdfsconnectionPoolSizeStr))
			fastdfsconnectionPoolSize = Integer.parseInt(fastdfsconnectionPoolSizeStr);
		busyConnectionPool = new ConcurrentHashMap<StorageClient1, Object>();
		idleConnectionPool = new ArrayBlockingQueue<StorageClient1>(fastdfsconnectionPoolSize);
		FastDFSConnectionPool(fastdfsconnectionPoolSize);
	};

	// class method
	// init the connection pool
	private void FastDFSConnectionPool(int size) {
		initClientGlobal();
		TrackerServer trackerServer = null;
		try {
			TrackerClient trackerClient = new TrackerClient();
			// Only tracker
			trackerServer = trackerClient.getConnection();
			for (int i = 0; i < size; i++) {
				StorageServer storageServer = null;
				StorageClient1 client1 = new StorageClient1(trackerServer,
						storageServer);
				idleConnectionPool.add(client1);
			}
			logger.info("fastdfs trackerClient start......");
		} catch (IOException e) {
			logger.error("fastdfs trackerClient Start error: ",
					PrintStackTrace.getErrorInfoFromException(e));
		} finally {
			if (trackerServer != null) {
				try {
					trackerServer.close();
				} catch (IOException e) {
					logger.error("fastdfs trackerClient close error: ",
							PrintStackTrace.getErrorInfoFromException(e));
				}
			}
		}
	}



	// 1. pop the connection from busyConnectionPool;
	// 2. push the connection into idleConnectionPool;
	// 3. do nessary cleanup works.
	public void checkin(StorageClient1 client1) {
		if (busyConnectionPool.remove(client1) != null) {
			idleConnectionPool.add(client1);
		}
	}

	// so if the connection was broken due to some erros (like// : socket init
	// failure, network broken etc), drop this connection
	// from the busyConnectionPool, and init one new connection.
	public void drop(StorageClient1 client1) {
		if (busyConnectionPool.remove(client1) != null) {
			TrackerServer trackerServer = null;
			try {
				TrackerClient trackerClient = new TrackerClient();
				// 此处有内存泄露，因为 trackerServer 没有关闭连接
				trackerServer = trackerClient.getConnection();
				StorageServer storageServer = null;
				StorageClient1 newClient1 = new StorageClient1(trackerServer,
						storageServer);
				idleConnectionPool.add(newClient1);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (trackerServer != null) {
					try {
						trackerServer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}	
	
	private void initClientGlobal() {
		int fastdfsConnectTimeout = 2000;
		int fastdfsNetworkTimeout = 30000;
		String fastdfsCharset = "UTF-8";
		int fastdfsHttpTrackerHttpPort = 80;
		boolean fastdfsHttpAntiStealToken = false;
		String fastdfsHttpSecretkey = "fastdfsp@ssw0rd";
		int fastdfsTrackerServerCount = 0;

		try {			
			if (ValidType.isValidInt(fastdfsConnectTimeoutStr))
				fastdfsConnectTimeout = Integer.parseInt(fastdfsConnectTimeoutStr);
			if (ValidType.isValidInt(fastdfsNetworkTimeoutStr))
				fastdfsNetworkTimeout = Integer.parseInt(fastdfsNetworkTimeoutStr);
			if (null != fastdfsCharsetStr)
				fastdfsCharset = fastdfsCharsetStr;
			if (ValidType.isValidInt(fastdfsHttpTrackerHttpPortStr))
				fastdfsHttpTrackerHttpPort = Integer.parseInt(fastdfsHttpTrackerHttpPortStr);
			if (ValidType.isBoolean(fastdfsHttpAntiStealTokenStr))
				fastdfsHttpAntiStealToken = Boolean.parseBoolean(fastdfsHttpAntiStealTokenStr);
			if (null != fastdfsHttpSecretkeyStr)
				fastdfsHttpSecretkey = fastdfsHttpSecretkeyStr;
			if (ValidType.isValidInt(fastdfsTrackerServerCountStr))
				fastdfsTrackerServerCount = Integer.parseInt(fastdfsTrackerServerCountStr);
			String[] fastdfsTrackerServerAddress = new String[fastdfsTrackerServerCount];
			int[] fastdfsTrackerServerPort = new int[fastdfsTrackerServerCount];
			for (int i = 0; i < fastdfsTrackerServerCount; i++) {
				fastdfsTrackerServerAddress[i] = fastdfsTrackerServerAddressStr1;
				fastdfsTrackerServerPort[i] = Integer.parseInt(fastdfsTrackerServerPortStr1);
			}
			logger.info("Fast DFS configuration: " + " fastdfs.connectTimeout="
					+ fastdfsConnectTimeout + " fastdfs.networkTimeout="
					+ fastdfsNetworkTimeout + " fastdfs.charset="
					+ fastdfsCharset + " fastdfs.http.trackerHttpPort="
					+ fastdfsHttpAntiStealToken
					+ " fastdfs.http.antiStealToken=" + fastdfsNetworkTimeout
					+ " fastdfs.http.secretkey=" + fastdfsHttpSecretkey
					+ " fastdfs.trackerServerAddress="
					+ fastdfsTrackerServerAddress.toString()
					+ " fastdfs.trackerServerPort="
					+ fastdfsTrackerServerPort.toString());
			InetSocketAddress[] trackerServers = new InetSocketAddress[fastdfsTrackerServerCount];
			for (int i = 0; i < fastdfsTrackerServerCount; i++) {
				trackerServers[i] = new InetSocketAddress(
						fastdfsTrackerServerAddress[i],
						fastdfsTrackerServerPort[i]);
			}
			ClientGlobal.setG_tracker_group(new TrackerGroup(trackerServers));
			// 连接超时的时限，单位为毫秒
			ClientGlobal.setG_connect_timeout(fastdfsConnectTimeout);
			// 网络超时的时限，单位为毫秒
			ClientGlobal.setG_network_timeout(fastdfsNetworkTimeout);
			ClientGlobal.setG_anti_steal_token(fastdfsHttpAntiStealToken);
			// 字符集
			ClientGlobal.setG_charset(fastdfsCharset);
			ClientGlobal.setG_secret_key(fastdfsHttpSecretkey);
		} catch (Exception e) {
			logger.error("read fastdfs.properties error: ",
					PrintStackTrace.getErrorInfoFromException(e));
		}
	}

	*//**
	 * @return the fastdfsConnectTimeoutStr
	 *//*
	public String getFastdfsConnectTimeoutStr() {
		return fastdfsConnectTimeoutStr;
	}

	*//**
	 * @param fastdfsConnectTimeoutStr the fastdfsConnectTimeoutStr to set
	 *//*
	public void setFastdfsConnectTimeoutStr(String fastdfsConnectTimeoutStr) {
		this.fastdfsConnectTimeoutStr = fastdfsConnectTimeoutStr;
	}

	*//**
	 * @return the fastdfsNetworkTimeoutStr
	 *//*
	public String getFastdfsNetworkTimeoutStr() {
		return fastdfsNetworkTimeoutStr;
	}

	*//**
	 * @param fastdfsNetworkTimeoutStr the fastdfsNetworkTimeoutStr to set
	 *//*
	public void setFastdfsNetworkTimeoutStr(String fastdfsNetworkTimeoutStr) {
		this.fastdfsNetworkTimeoutStr = fastdfsNetworkTimeoutStr;
	}

	*//**
	 * @return the fastdfsCharsetStr
	 *//*
	public String getFastdfsCharsetStr() {
		return fastdfsCharsetStr;
	}

	*//**
	 * @param fastdfsCharsetStr the fastdfsCharsetStr to set
	 *//*
	public void setFastdfsCharsetStr(String fastdfsCharsetStr) {
		this.fastdfsCharsetStr = fastdfsCharsetStr;
	}

	*//**
	 * @return the fastdfsHttpAntiStealTokenStr
	 *//*
	public String getFastdfsHttpAntiStealTokenStr() {
		return fastdfsHttpAntiStealTokenStr;
	}

	*//**
	 * @param fastdfsHttpAntiStealTokenStr the fastdfsHttpAntiStealTokenStr to set
	 *//*
	public void setFastdfsHttpAntiStealTokenStr(String fastdfsHttpAntiStealTokenStr) {
		this.fastdfsHttpAntiStealTokenStr = fastdfsHttpAntiStealTokenStr;
	}

	*//**
	 * @return the fastdfsHttpSecretkeyStr
	 *//*
	public String getFastdfsHttpSecretkeyStr() {
		return fastdfsHttpSecretkeyStr;
	}

	*//**
	 * @param fastdfsHttpSecretkeyStr the fastdfsHttpSecretkeyStr to set
	 *//*
	public void setFastdfsHttpSecretkeyStr(String fastdfsHttpSecretkeyStr) {
		this.fastdfsHttpSecretkeyStr = fastdfsHttpSecretkeyStr;
	}

	*//**
	 * @return the fastdfsTrackerServerCountStr
	 *//*
	public String getFastdfsTrackerServerCountStr() {
		return fastdfsTrackerServerCountStr;
	}

	*//**
	 * @param fastdfsTrackerServerCountStr the fastdfsTrackerServerCountStr to set
	 *//*
	public void setFastdfsTrackerServerCountStr(String fastdfsTrackerServerCountStr) {
		this.fastdfsTrackerServerCountStr = fastdfsTrackerServerCountStr;
	}

	*//**
	 * @return the fastdfsHttpTrackerHttpPortStr
	 *//*
	public String getFastdfsHttpTrackerHttpPortStr() {
		return fastdfsHttpTrackerHttpPortStr;
	}

	*//**
	 * @param fastdfsHttpTrackerHttpPortStr the fastdfsHttpTrackerHttpPortStr to set
	 *//*
	public void setFastdfsHttpTrackerHttpPortStr(
			String fastdfsHttpTrackerHttpPortStr) {
		this.fastdfsHttpTrackerHttpPortStr = fastdfsHttpTrackerHttpPortStr;
	}

	*//**
	 * @return the fastdfsTrackerServerAddressStr1
	 *//*
	public String getFastdfsTrackerServerAddressStr1() {
		return fastdfsTrackerServerAddressStr1;
	}

	*//**
	 * @param fastdfsTrackerServerAddressStr1 the fastdfsTrackerServerAddressStr1 to set
	 *//*
	public void setFastdfsTrackerServerAddressStr1(
			String fastdfsTrackerServerAddressStr1) {
		this.fastdfsTrackerServerAddressStr1 = fastdfsTrackerServerAddressStr1;
	}

	*//**
	 * @return the fastdfsTrackerServerPortStr1
	 *//*
	public String getFastdfsTrackerServerPortStr1() {
		return fastdfsTrackerServerPortStr1;
	}

	*//**
	 * @param fastdfsTrackerServerPortStr1 the fastdfsTrackerServerPortStr1 to set
	 *//*
	public void setFastdfsTrackerServerPortStr1(String fastdfsTrackerServerPortStr1) {
		this.fastdfsTrackerServerPortStr1 = fastdfsTrackerServerPortStr1;
	}

	*//**
	 * @return the fastdfsconnectionPoolSizeStr
	 *//*
	public String getFastdfsconnectionPoolSizeStr() {
		return fastdfsconnectionPoolSizeStr;
	}

	*//**
	 * @param fastdfsconnectionPoolSizeStr the fastdfsconnectionPoolSizeStr to set
	 *//*
	public void setFastdfsconnectionPoolSizeStr(String fastdfsconnectionPoolSizeStr) {
		this.fastdfsconnectionPoolSizeStr = fastdfsconnectionPoolSizeStr;
	}

*/
 
	private static final Logger logger = LoggerFactory.getLogger(FileServerImpl.class);
 
    @Value("#{settings['document.serverHost']}")
    protected String serverHost;
    
    @Value("#{settings['document.uploadPath']}")
    protected String uploadPath;
    
    @Value( "#{settings['document.downloadPath']}")
    protected String downloadPath;
    
    
    
/*    @Value("${document.clientId}")
    protected String clientId;

    @Value("${document.clientSecret}")
    protected String clientSecret;*/
    
/*    protected static String HEADER_CLIENTID = "Client-Id";
    protected static String HEADER_CLIENTSECRET = "Client-Secret";*/
    
    
    protected final static String FEILD_DATA = "data";
	protected final static String FEILD_MSG = "msg";
	protected final static String FEILD_ACCESSURL = "accessUrl";
	protected final static String FEILD_NAME = "name";
	protected final static String FEILD_FILEID = "fileId";
 
	
 
	

	@Override
	public String uploadFile(File file) throws IOException, Exception {
		String url = serverHost + uploadPath;
		HttpPost httppost = new HttpPost(url);
		MultipartEntityBuilder entityBuilder = getEntityBuilder(file);
		httppost.setEntity(entityBuilder.build());
		String accessUrl = null;
 
		 String res = this.handleRequest(httppost);
		 if ( StringUtils.isNotBlank(res)) {
			 JSONObject result =  JSONObject.parseObject(res);
			 if (result.containsKey(FEILD_DATA)) {
				 JSONObject data = result.getJSONObject(FEILD_DATA);
				 accessUrl = data.getString("accessUrl");
				 //accessUrl = accessUrl.substring(accessUrl.indexOf("NotAuthentication/") + "NotAuthentication/".length(), accessUrl.length());
				 String fileId =  data.getString("fileId");
				 fileImportInfoService.addFile("0", fileId, accessUrl);
				 
			 }
		  } 
		 return  accessUrl;
		
	}

	@Override
	public String uploadFile(File file, String fileName) throws IOException, Exception {
		String url = serverHost + uploadPath;
		HttpPost httppost = new HttpPost(url);
		MultipartEntityBuilder entityBuilder = getEntityBuilder(file);
		httppost.setEntity(entityBuilder.build());
		String accessUrl = null;
		String res = this.handleRequest(httppost);
		if (StringUtils.isNotBlank(res)) {
			JSONObject result = JSONObject.parseObject(res);
			if (result.containsKey(FEILD_DATA)) {
				 JSONObject data = result.getJSONObject(FEILD_DATA);
				 accessUrl = data.getString("accessUrl");
				 //accessUrl = accessUrl.substring(accessUrl.indexOf("NotAuthentication/") + "NotAuthentication/".length(), accessUrl.length());
				 String fileId =  data.getString("fileId");
				 fileImportInfoService.addFile("0", fileId, accessUrl);
			}
		}
		return  accessUrl;
	}

	@Override
	public String uploadFile(byte[] fileBuff, String fileName) throws IOException, Exception {
		String url = serverHost + uploadPath;
		HttpPost httppost = new HttpPost(url);
		
		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
	    entityBuilder.setCharset(Charset.forName("utf-8"));  
	    entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);  
	    addFile(entityBuilder, fileBuff, fileName);
		httppost.setEntity(entityBuilder.build());
		String accessUrl = null;
		String res = this.handleRequest(httppost);
		if (StringUtils.isNotBlank(res)) {
			JSONObject result = JSONObject.parseObject(res);
			if (result.containsKey(FEILD_DATA)) {
				 JSONObject data = result.getJSONObject(FEILD_DATA);
				 accessUrl = data.getString("accessUrl");
				 //accessUrl = accessUrl.substring(accessUrl.indexOf("NotAuthentication/") + "NotAuthentication/".length(), accessUrl.length());
				 String fileId =  data.getString("fileId");
				 fileImportInfoService.addFile("0", fileId, accessUrl);
				 
			}
		}
		return  accessUrl;
 
	}

	// 文件删除空实现
	@Override
	public int deleteFile(String fileID) throws IOException, Exception {
		 
		return 0;
	}

	@Override
	public byte[] downloadFile(String fileID) throws IOException, Exception {
		byte[] result = null;
		if (fileID.startsWith("http")) {
			String fileId = fileImportInfoService.getFileByFileId("0", fileID);
			result = this.getDocumentData(fileId);
		}else {
			StorageClient1 client1 = null;
			try {
				client1 = checkout(10);
				if(client1==null){
					logger.error("downLoadFile checkout(10) get null client");
				} 
				logger.info("下载文件参数，fileID:{} ",fileID);
				result = client1.download_file1(fileID);
				checkin(client1);
			} catch (InterruptedException e) {
				// 确实没有空闲连接,并不需要删除与fastdfs连接
				pintErrorLog(this.getClass().getName(), e);
			} catch (Exception e) {
				// 发生io异常等其它异常，默认删除这次连接重新申请
				drop(client1);
				pintErrorLog(this.getClass().getName(), e);
			}
		}
	
		return result;
	}

	@Override
	public int downloadFile(String fileID, String localFilenName) throws IOException, Exception {
		int result = 0;
		if (fileID.startsWith("http")) {
			String fileId = fileImportInfoService.getFileByFileId("0", fileID);
			byte[] tmp = this.getDocumentData(fileId);
		    if (tmp != null) {
			   FileOutputStream os = new FileOutputStream(localFilenName);  
			   os.write(tmp, 0, tmp.length);
			   os.close();
			   result = 1;
		    }
		}else {
			StorageClient1 client1 = null;
			try {
				client1 = checkout(10);
				if(client1==null){
					logger.error("downLoadFile checkout(10) get null client");
				}
				logger.info("下载文件参数，fileID:{},localFilenName:{}",fileID,localFilenName);
				result = client1.download_file1(fileID, localFilenName);
				logger.info("下载文件结果代码:{}",result);
				checkin(client1);
			} catch (InterruptedException e) {
				pintErrorLog(this.getClass().getName(), e);
			} catch (Exception e) {
				drop(client1);
				pintErrorLog(this.getClass().getName(), e);
			}
			
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	protected MultipartEntityBuilder getEntityBuilder(Object file) throws Exception {
		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
	    entityBuilder.setCharset(Charset.forName("utf-8"));  
	    entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);  
	    if (file instanceof File) {
	    	addFile(entityBuilder, (File) file);
	    } else if (file instanceof MultipartFile) {
	    	addFile(entityBuilder, (MultipartFile) file);
	    } else if (file instanceof MultipartFile[]) {
	    	MultipartFile[] f = (MultipartFile[]) file;
	    	for (MultipartFile part: f) {
		    	addFile(entityBuilder, part);
	    	}
	    } else if (file instanceof File[]) {
	    	File[] f = (File[]) file;
	    	for (File part: f) {
		    	addFile(entityBuilder, part);
	    	}
	    } 
		return entityBuilder;   
	}
	
	
	protected static String PARAMETER_FILE = "file";
	protected void addFile(MultipartEntityBuilder entityBuilder, File f) {
    	entityBuilder.addPart(PARAMETER_FILE, new FileBody(f));
    	 
	}

	protected void addFile(MultipartEntityBuilder entityBuilder, MultipartFile f) throws Exception {
    	entityBuilder.addBinaryBody(PARAMETER_FILE, f.getBytes(), 
    			ContentType.DEFAULT_BINARY, f.getOriginalFilename());
    	 
	}
	
	protected void addFile(MultipartEntityBuilder entityBuilder, byte[]  f , String fileName) throws Exception {
    	entityBuilder.addBinaryBody(PARAMETER_FILE, f , 
    			ContentType.DEFAULT_BINARY, fileName);
	}
	
	
	protected String handleRequest(HttpUriRequest req) {
		logger.info("req==>" + req.toString());
		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String result = null;
		try {
		    response = httpclient.execute(req);
		    int status = response.getStatusLine().getStatusCode();
		    if (status == 200) {
		    	result = EntityUtils.toString(response.getEntity());
		    }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
			closeResponse(response);
			closeClient(httpclient);
        }
		logger.info("result==>" + result);
		return result ;
	}
	
	protected byte[] handleRequest2(HttpUriRequest req) {
		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		byte[] result = null;
		try {
		    response = httpclient.execute(req);
		    int status = response.getStatusLine().getStatusCode();
		    if (status == 200) {
		    	result = EntityUtils.toByteArray(response.getEntity());
		    }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
			closeResponse(response);
			closeClient(httpclient);
        }
		logger.info("result==>" + result);
		return result ;
	}
	

	protected void closeResponse(CloseableHttpResponse closeableHttpResponse) {
		if (closeableHttpResponse != null) {
            try {
                closeableHttpResponse.close();
            } catch (Exception e) {
            }
        }
	}
	
	protected void closeClient(CloseableHttpClient client) {
		if (client != null) {
            try {
            	client.close();
            } catch (Exception e) {
            }
        }
	}
	
	public byte[] getDocumentData(String fileId) {
    	 
		    String url = serverHost + downloadPath + fileId;
			HttpGet httppost = new HttpGet(url);
			/*httppost.addHeader(HEADER_CLIENTID, clientId);
			httppost.addHeader(HEADER_CLIENTSECRET, clientSecret);*/
			byte[] res = this.handleRequest2(httppost);
		    return res;
        
	}

	@Override
	public String uploadFile(byte[] fileBuff, String fileExtName, NameValuePair[] metaList)
			throws IOException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileInfo queryFileInfo(String fileID) throws IOException, Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	private void pintErrorLog(String classNme, Exception e) {
		logger.error(
				"异常方法:\r\n{}\r\n异常代码:\r\n{}\r\n异常信息:\r\n{}\r\nprintStackTrace:\r\n{}",
				classNme, e.getClass().getName(), e.getMessage(),
				PrintStackTrace.getErrorInfoFromException(e));
	}
	
	// 1. pop one connection from the idleConnectionPool,
	// 2. push the connection into busyConnectionPool;
	// 3. return the connection
	// if no idle connection, do wait for wait_time seconds, and check again
	public StorageClient1 checkout(int waitTimes) throws InterruptedException {
		StorageClient1 client1 = idleConnectionPool.poll(waitTimes,
				TimeUnit.SECONDS);
		busyConnectionPool.put(client1, obj);
		return client1;
	}
	
	
	@Value("#{configProperties['fastdfs.connectTimeout']}")
	private String fastdfsConnectTimeoutStr;
	@Value("#{configProperties['fastdfs.networkTimeout']}")
	private String fastdfsNetworkTimeoutStr;
	@Value("#{configProperties['fastdfs.charset']}")
	private String fastdfsCharsetStr;
	@Value("#{configProperties['fastdfs.http.antiStealToken']}")
	private String fastdfsHttpAntiStealTokenStr;
	@Value("#{configProperties['fastdfs.http.secretkey']}")
	private String fastdfsHttpSecretkeyStr;
	@Value("#{configProperties['fastdfs.trackerServerCount']}")
	private String fastdfsTrackerServerCountStr;
	@Value("#{configProperties['fastdfs.http.trackerHttpPort']}")
	private String fastdfsHttpTrackerHttpPortStr;
	@Value("#{configProperties['fastdfs.trackerServerAddress1']}")
	private String fastdfsTrackerServerAddressStr1;
	@Value("#{configProperties['fastdfs.trackerServerPort1']}")
	private String fastdfsTrackerServerPortStr1;
	@Value("#{configProperties['fastdfs.connectionPoolSize']}")
	private String fastdfsconnectionPoolSizeStr;
	int fastdfsconnectionPoolSize = 1;
	// busy connection instances
	private ConcurrentHashMap<StorageClient1, Object> busyConnectionPool = null;
	// idle connection instances
	private ArrayBlockingQueue<StorageClient1> idleConnectionPool = null;
	private Object obj = new Object();
	
	
	public FileServerImpl() {
		fastdfsConnectTimeoutStr = "2000";
		fastdfsNetworkTimeoutStr = "30000";
		fastdfsCharsetStr = "UTF-8";
		fastdfsHttpAntiStealTokenStr = "false";
		fastdfsHttpSecretkeyStr = "fastdfsp@ssw0rd";
		ResourceBundle rb = ResourceBundle.getBundle("config.fastdfs" ); 
		if(fastdfsTrackerServerAddressStr1==null){
			fastdfsTrackerServerAddressStr1 = rb.getString("fastdfs.trackerServerAddress1");
			fastdfsTrackerServerPortStr1 = rb.getString("fastdfs.trackerServerPort1");
			
		}
		fastdfsconnectionPoolSizeStr = "3";
		fastdfsTrackerServerCountStr = "1";
		
		if (ValidType.isValidInt(fastdfsconnectionPoolSizeStr))
			fastdfsconnectionPoolSize = Integer.parseInt(fastdfsconnectionPoolSizeStr);
		busyConnectionPool = new ConcurrentHashMap<StorageClient1, Object>();
		idleConnectionPool = new ArrayBlockingQueue<StorageClient1>(fastdfsconnectionPoolSize);
		FastDFSConnectionPool(fastdfsconnectionPoolSize);
	};
	
	// 1. pop the connection from busyConnectionPool;
	// 2. push the connection into idleConnectionPool;
	// 3. do nessary cleanup works.
	public void checkin(StorageClient1 client1) {
		if (busyConnectionPool.remove(client1) != null) {
			idleConnectionPool.add(client1);
		}
	}
	
	
	// class method
		// init the connection pool
		private void FastDFSConnectionPool(int size) {
			initClientGlobal();
			TrackerServer trackerServer = null;
			try {
				TrackerClient trackerClient = new TrackerClient();
				// Only tracker
				trackerServer = trackerClient.getConnection();
				for (int i = 0; i < size; i++) {
					StorageServer storageServer = null;
					StorageClient1 client1 = new StorageClient1(trackerServer,
							storageServer);
					idleConnectionPool.add(client1);
				}
				logger.info("fastdfs trackerClient start......");
			} catch (IOException e) {
				logger.error("fastdfs trackerClient Start error: ",
						PrintStackTrace.getErrorInfoFromException(e));
			} finally {
				if (trackerServer != null) {
					try {
						trackerServer.close();
					} catch (IOException e) {
						logger.error("fastdfs trackerClient close error: ",
								PrintStackTrace.getErrorInfoFromException(e));
					}
				}
			}
		}
		
		
		private void initClientGlobal() {
			int fastdfsConnectTimeout = 2000;
			int fastdfsNetworkTimeout = 30000;
			String fastdfsCharset = "UTF-8";
			int fastdfsHttpTrackerHttpPort = 80;
			boolean fastdfsHttpAntiStealToken = false;
			String fastdfsHttpSecretkey = "fastdfsp@ssw0rd";
			int fastdfsTrackerServerCount = 0;

			try {			
				if (ValidType.isValidInt(fastdfsConnectTimeoutStr))
					fastdfsConnectTimeout = Integer.parseInt(fastdfsConnectTimeoutStr);
				if (ValidType.isValidInt(fastdfsNetworkTimeoutStr))
					fastdfsNetworkTimeout = Integer.parseInt(fastdfsNetworkTimeoutStr);
				if (null != fastdfsCharsetStr)
					fastdfsCharset = fastdfsCharsetStr;
				if (ValidType.isValidInt(fastdfsHttpTrackerHttpPortStr))
					fastdfsHttpTrackerHttpPort = Integer.parseInt(fastdfsHttpTrackerHttpPortStr);
				if (ValidType.isBoolean(fastdfsHttpAntiStealTokenStr))
					fastdfsHttpAntiStealToken = Boolean.parseBoolean(fastdfsHttpAntiStealTokenStr);
				if (null != fastdfsHttpSecretkeyStr)
					fastdfsHttpSecretkey = fastdfsHttpSecretkeyStr;
				if (ValidType.isValidInt(fastdfsTrackerServerCountStr))
					fastdfsTrackerServerCount = Integer.parseInt(fastdfsTrackerServerCountStr);
				String[] fastdfsTrackerServerAddress = new String[fastdfsTrackerServerCount];
				int[] fastdfsTrackerServerPort = new int[fastdfsTrackerServerCount];
				for (int i = 0; i < fastdfsTrackerServerCount; i++) {
					fastdfsTrackerServerAddress[i] = fastdfsTrackerServerAddressStr1;
					fastdfsTrackerServerPort[i] = Integer.parseInt(fastdfsTrackerServerPortStr1);
				}
				logger.info("Fast DFS configuration: " + " fastdfs.connectTimeout="
						+ fastdfsConnectTimeout + " fastdfs.networkTimeout="
						+ fastdfsNetworkTimeout + " fastdfs.charset="
						+ fastdfsCharset + " fastdfs.http.trackerHttpPort="
						+ fastdfsHttpAntiStealToken
						+ " fastdfs.http.antiStealToken=" + fastdfsNetworkTimeout
						+ " fastdfs.http.secretkey=" + fastdfsHttpSecretkey
						+ " fastdfs.trackerServerAddress="
						+ fastdfsTrackerServerAddress.toString()
						+ " fastdfs.trackerServerPort="
						+ fastdfsTrackerServerPort.toString());
				InetSocketAddress[] trackerServers = new InetSocketAddress[fastdfsTrackerServerCount];
				for (int i = 0; i < fastdfsTrackerServerCount; i++) {
					trackerServers[i] = new InetSocketAddress(
							fastdfsTrackerServerAddress[i],
							fastdfsTrackerServerPort[i]);
				}
				ClientGlobal.setG_tracker_group(new TrackerGroup(trackerServers));
				// 连接超时的时限，单位为毫秒
				ClientGlobal.setG_connect_timeout(fastdfsConnectTimeout);
				// 网络超时的时限，单位为毫秒
				ClientGlobal.setG_network_timeout(fastdfsNetworkTimeout);
				ClientGlobal.setG_anti_steal_token(fastdfsHttpAntiStealToken);
				// 字符集
				ClientGlobal.setG_charset(fastdfsCharset);
				ClientGlobal.setG_secret_key(fastdfsHttpSecretkey);
			} catch (Exception e) {
				logger.error("read fastdfs.properties error: ",
						PrintStackTrace.getErrorInfoFromException(e));
			}
		}
		
		
		// so if the connection was broken due to some erros (like// : socket init
		// failure, network broken etc), drop this connection
		// from the busyConnectionPool, and init one new connection.
		public void drop(StorageClient1 client1) {
			if (busyConnectionPool.remove(client1) != null) {
				TrackerServer trackerServer = null;
				try {
					TrackerClient trackerClient = new TrackerClient();
					// 此处有内存泄露，因为 trackerServer 没有关闭连接
					trackerServer = trackerClient.getConnection();
					StorageServer storageServer = null;
					StorageClient1 newClient1 = new StorageClient1(trackerServer,
							storageServer);
					idleConnectionPool.add(newClient1);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (trackerServer != null) {
						try {
							trackerServer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}	
		
	
 
}
