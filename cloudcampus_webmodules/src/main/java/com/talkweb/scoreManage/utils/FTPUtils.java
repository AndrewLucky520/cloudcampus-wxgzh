package com.talkweb.scoreManage.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FTPUtils {
	
	   private String hostname = "139.159.163.50";
	   private Integer port=21;
	   private String username = "kw";
	   private String password = "e9mBR2&3T";
	   private FTPClient ftpClient = null;

	   public void init() {
	       ftpClient = new FTPClient();
	       ftpClient.setControlEncoding("utf-8");
	       try {
	           System.out.println("connecting...ftp服务器:" + hostname + ":" + port);
	           ftpClient.connect(hostname, port); //连接ftp服务器
	           ftpClient.login(username, password); //登录ftp服务器
	           int replyCode = ftpClient.getReplyCode(); //是否成功登录服务器
	           if (!FTPReply.isPositiveCompletion(replyCode)) {
	               System.out.println("connect failed...ftp服务器:" + hostname + ":" + port);
	           }
	           System.out.println("connect successfu...ftp服务器:" + hostname + ":" + port);
	       } catch (MalformedURLException e) {
	           e.printStackTrace();
	       } catch (IOException e) {
	           e.printStackTrace();
	       }
	   }
	   
	   
	   public boolean downloadFile(String pathname, String filename, String localpath) {
	       ftpClient.enterLocalPassiveMode();
	       boolean flag = false;
	       OutputStream os = null;
	       try {
	           //切换FTP目录
	           ftpClient.changeWorkingDirectory(pathname);
	           FTPFile[] ftpFiles = ftpClient.listFiles();
	           for (FTPFile file : ftpFiles) {
	               if (filename.equalsIgnoreCase(file.getName())) {
	                   File localFile = new File(localpath + file.getName());
	                   os = new FileOutputStream(localFile);
	                   ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	                   flag = ftpClient.retrieveFile(file.getName(), os);
	                   if (!flag) {
	                	   System.out.println( "下载文件失败" );
	                      
	                   } else {
	                	   System.out.println( "下载文件成功" );
	                     
	                   }
	                   os.close();
	               }
	           }
	       } catch (Exception e) {
	    	   System.out.println( "下载文件失败" + e.getMessage());
	           e.printStackTrace();
	       } finally {
	           if (null != os) {
	               try {
	                   os.close();
	               } catch (IOException e) {
	                   e.printStackTrace();
	               }
	           }
	       }
	       return flag;
	   }
	   
	   
	   public boolean deleteFile(String filename, String pathname) {
	       ftpClient.enterLocalPassiveMode();
	       boolean flag = false;
	       try {
	           ftpClient.changeWorkingDirectory(pathname);
	           System.out.println("开始删除文件");
	           flag = ftpClient.deleteFile(filename);
	           System.out.println("删除文件成功");
	       } catch (Exception e) {
	           System.out.println("删除文件失败");
	           e.printStackTrace();
	       }  
	       return flag;
	   }
	   
	   public void close(){
		   if (ftpClient != null) {
			   try {
				ftpClient.logout();
			   } catch (IOException e) {
				e.printStackTrace();
			   }
			  if (ftpClient.isConnected()) {
	               try {
	                   ftpClient.disconnect();
	               } catch (IOException e) {
	                   e.printStackTrace();
	               }
	           }
			   
		   }
	   } 

	   public void list(String pathName, List<String> arFiles)  {
	       ftpClient.enterLocalPassiveMode();
	       if (pathName.startsWith("/") && pathName.endsWith("/")) {
	           String directory = pathName;
	           try {
				 this.ftpClient.changeWorkingDirectory(directory);
				 FTPFile[] files = this.ftpClient.listFiles();
		         for (FTPFile file : files) {
		               if (file.isFile()) {
		                   arFiles.add(directory + file.getName());
		               } else if (file.isDirectory()) {
		                   list(directory + file.getName() + "/", arFiles);
		               }
		           }
			   } catch (IOException e) {
				  e.printStackTrace();
			   }
	       }
	   }
	   
	   
	   public void list(String pathName, String ext, List<String> arFiles) {
	       ftpClient.enterLocalPassiveMode();
	       if (pathName.startsWith("/") && pathName.endsWith("/")) {
	           String directory = pathName;
	           //更换目录到当前目录
	           try {
				 this.ftpClient.changeWorkingDirectory(directory);
				 FTPFile[] files = this.ftpClient.listFiles();
		           for (FTPFile file : files) {
		               if (file.isFile()) {
		                   if (file.getName().endsWith(ext)) {
		                       arFiles.add(directory + file.getName());
		                   }
		               } else if (file.isDirectory()) {
		                   list(directory + file.getName() + "/", ext, arFiles);
		               }
		           }
			   } catch (IOException e) {
				 e.printStackTrace();
			  }
	       }
	   }
	   
	   

		 public  boolean delAllFile(String path) {
		       boolean flag = false;
		       File file = new File(path);
		       if (!file.exists()) {
		         return flag;
		       }
		       if (!file.isDirectory()) {
		         return flag;
		       }
		       String[] tempList = file.list();
		       File temp = null;
		       for (int i = 0; i < tempList.length; i++) {
		          if (path.endsWith(File.separator)) {
		             temp = new File(path + tempList[i]);
		          } else {
		              temp = new File(path + File.separator + tempList[i]);
		          }
		          if (temp.isFile()) {
		             temp.delete();
		          }
		          if (temp.isDirectory()) {
		             delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
		             delFolder(path + "/" + tempList[i]);//再删除空文件夹
		             flag = true;
		          }
		       }
		       return flag;
		     }
		 
		   public  void delFolder(String folderPath) {
			     try {
			        delAllFile(folderPath); //删除完里面所有内容
			        String filePath = folderPath;
			        filePath = filePath.toString();
			        java.io.File myFilePath = new java.io.File(filePath);
			        myFilePath.delete(); //删除空文件夹
			     } catch (Exception e) {
			       e.printStackTrace(); 
			     }
			} 
	   
	   
 
}
