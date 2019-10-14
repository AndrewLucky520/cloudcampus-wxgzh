package com.talkweb.scoreManage.scheduler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.CommonDataService;
import com.talkweb.http.service.CallRemoteInterface;
import com.talkweb.http.service.impl.CallRemoteInterfaceImpl;
import com.talkweb.scoreManage.po.gm.ScoreInfo;
import com.talkweb.scoreManage.service.ScoreManageService;
import com.talkweb.scoreManage.utils.FTPUtils;
import com.talkweb.student.domain.page.StartImportTaskParam;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

@Component
@EnableScheduling
public class ScoreScheduler {
	
 
	@Value("#{settings['tempFTPPath']}")
	private String tempFTPPath;
	
	@Value("#{settings['tempFTPBakPath']}")
	private String tempFTPBakPath;
	
	@Value("#{settings['currentTermInfo']}")
	private String currentTermInfo;
	
	
	@Value("#{settings['kwSwitch']}")
	private String kwSwitch;
	
	@Autowired
	private ScoreManageService scoreManageService;
	
	@Autowired
	CommonDataService commonDataService;
	
 
	@Autowired
	private AllCommonDataService allCommonDataService;
 
	
	@Autowired
	CallRemoteInterface callRemoteInterface;
 
 
	@Scheduled(cron = "0 0 6 * * ? ")
	public void insertExamNetByFtp() {
		
		System.out.println( "执行....." + new Date() );
		if (!"1".equals(kwSwitch)) {
			return ;
		}
		
		List<JSONObject> errorList = new ArrayList<JSONObject>();
 
		Map<String , T_GradeLevel> map = new HashMap<String , T_GradeLevel>();
		map.put("1", T_GradeLevel.T_PrimaryOne);//一年级
		map.put("2", T_GradeLevel.T_PrimaryTwo);//二年级
		map.put("3", T_GradeLevel.T_PrimaryThree);//三年级
		map.put("4", T_GradeLevel.T_PrimaryFour);//四年级
		map.put("5", T_GradeLevel.T_PrimaryFive);//五年级
		map.put("6", T_GradeLevel.T_PrimarySix);//六年级
		map.put("7", T_GradeLevel.T_JuniorOne);//初一
		map.put("8", T_GradeLevel.T_JuniorTwo);//初二
		map.put("9", T_GradeLevel.T_JuniorThree);//初三
		map.put("10", T_GradeLevel.T_HighOne);//高一
		map.put("11", T_GradeLevel.T_HighTwo);//高二
		map.put("12", T_GradeLevel.T_HighThree);//高三
		
		Map<String , String> subjectMap = new HashMap<String , String>();
		subjectMap.put("语文", "1");
		subjectMap.put("数学", "2");
		subjectMap.put("文数", "2");
		subjectMap.put("理数", "2");
		subjectMap.put("英语", "3");
		subjectMap.put("政治", "4");
		subjectMap.put("历史", "5");
		subjectMap.put("地理", "6");
		subjectMap.put("物理", "7");
		subjectMap.put("化学", "8");
		subjectMap.put("生物", "9");
		subjectMap.put("文综", "34");
		subjectMap.put("理综", "33");
		
		subjectMap.put("技术", "19");
		subjectMap.put("信息技术", "13");
		subjectMap.put("通用技术", "19");
		subjectMap.put("外语", "3");
		subjectMap.put("思想品德", "4");
		//subjectMap.put("科学", "");
		//subjectMap.put("历史与社会", "");
		subjectMap.put("品德与社会", "11");
		subjectMap.put("品德与生活", "10");
		
 
		String xnxq = currentTermInfo;
		String xn = xnxq.substring(0, 4);
		String xq =xnxq.substring(4);
 
		// 1 下载文件
		List<String> arFiles = new ArrayList<String>();
		FTPUtils util = new FTPUtils();
		util.init();
		util.list("/kw/",".zip", arFiles); // 列出考网所有zip 文件
		if (arFiles.size() > 0) {
			String file = null;
			String pathname = null;
			String filename = null;
			String extendFile = null;
			List<String> ftpDel = new ArrayList<String>();
			for (int i = 0; i < arFiles.size(); i++) {
				file =  arFiles.get(i);
				pathname =file.substring(0, file.lastIndexOf("/")) ; // 最后一层的路径
				filename = file.substring(file.lastIndexOf("/") + 1); // 带后缀的文件名
				extendFile = filename.substring(0, filename.indexOf(".zip")); //不带路径的文件名
				util.downloadFile(pathname, filename, tempFTPPath);
				//2 解压文件
		        String dest= tempFTPPath + extendFile;  // 解压的文件夹名
		         try{
			         File zipFile=new File(tempFTPPath + filename);
			         if (!zipFile.exists() || zipFile.length() == 0) {// 是否下载成功
						 continue;
					 }else { // 转存
						 File bak = new File(tempFTPBakPath + filename);
						 copyFile(zipFile ,bak);
					}
			        ZipFile zFile = new ZipFile(zipFile); 
			        zFile.setFileNameCharset("GBK");
			        File destDir = new File(tempFTPPath + extendFile);
		            zFile.extractAll(dest);
		            List<FileHeader > headerList = zFile.getFileHeaders(); 
		            List<File> extractedFileList= new ArrayList<File>(); 
		            for(FileHeader fileHeader : headerList) { 
			             if (!fileHeader.isDirectory()) { 
			                    extractedFileList.add(new File(destDir,fileHeader.getFileName()));
			             }
		            }
		            File [] extractedFiles = new File[extractedFileList.size()];
		            extractedFileList.toArray(extractedFiles);
		            JSONObject examInfo = null;
		            JSONObject candidateItemScore = null;
		            for(File f:extractedFileList){
		                 if (f.getAbsolutePath().indexOf("examInfo") > -1) {
		                	  examInfo = readFile(f);
						 }else if (f.getAbsolutePath().indexOf("candidateItemScore") > -1) {
							 candidateItemScore = readFile(f);
						}
	                 }
		            if (examInfo == null || candidateItemScore==null) {
		            	 //文件无信息
						continue;  
					}
		            JSONObject exam = new JSONObject();
		            exam.put("lrr", 0L);
		            String schoolId = null;
		            Map<String, Integer> fullScoreMap = new HashMap<String, Integer>();
		            if (examInfo != null) {
							JSONObject object = examInfo.getJSONObject("data");
							exam.put("examName", object.getString("examName"));
							exam.put("gradeLevel",  object.getString("gradeLevel"));
							exam.put("examId", object.getString("examId"));
							JSONArray array = object.getJSONArray("subjects");
							for (int j = 0; j < array.size(); j++) {
								JSONObject object2 = array.getJSONObject(j);
								 Double fullScore = Double.parseDouble(object2.getString("fullScore"));
								 fullScoreMap.put( object2.getString("subjectId") ,  fullScore.intValue() );//科目满分
							} 
					}

		            exam.put("kslcmc", exam.getString("examName"));
		            exam.put("xnxq", xnxq);
		        	String nj= commonDataService.ConvertNJDM2SYNJ(map.get(exam.getString("gradeLevel")).getValue()+"", xn);
		            exam.put("usedGradeId", nj ) ;
		            JSONObject relate = scoreManageService.getDegreeinfoRelate(exam);
		            String kslcdm = null;
 
		            List<ScoreInfo> scoreInfos = new ArrayList<ScoreInfo>();
		            Set<String> studentIdSet = new HashSet<String>();
	                Set<String> classIdSet = new HashSet<String>();
		            if (candidateItemScore!=null) {
		                 
						   JSONArray datas = candidateItemScore.getJSONArray("data");
						   for (int j = 0; j < datas.size(); j++) {
							    JSONObject object = datas.getJSONObject(j);
								   schoolId =  object.getString("schoolId");// 学校有问题 不是extid 也不是id
								   JSONArray subjects = object.getJSONArray("subjects");
								   for (int k = 0; k < subjects.size(); k++) {
									  JSONObject subject =  subjects.getJSONObject(k);
									  ScoreInfo scoreInfo = new ScoreInfo();
									  scoreInfo.setCj(subject.getFloat("subjectScore") );
									  scoreInfo.setKmdm(subjectMap.get(subject.getString("subjectName")));
									  scoreInfo.setMf(fullScoreMap.get(subject.getString("subjectId")));
									  scoreInfo.setXnxq(xnxq);
									  scoreInfo.setXxdm(schoolId);
									  scoreInfo.setBh(object.getString("classId"));
									  classIdSet.add(object.getString("classId"));
									  scoreInfo.setXh(object.getString("candidateId"));
									  studentIdSet.add(object.getString("candidateId"));
									  scoreInfo.setNj(nj);
									  scoreInfos.add(scoreInfo);
								  }
						   }
					}
		            

		            
		            List<JSONObject> stuList = getStudent(schoolId, new ArrayList<String>(studentIdSet));
		            Set<String> stus = new HashSet<String>();
		            Map<String, String> stuMap = new HashMap<String, String>();
		           
		            for (int j = 0; j < stuList.size(); j++) {
						JSONObject stu = stuList.get(j);
						stus.add(stu.getString("accountId"));
						stuMap.put(stu.getString("studentCode"), stu.getString("accountId")); // 考网学号 和 用户的 extId
						schoolId = stu.getString("schoolId");
					}

		            String res = callRemoteInterface.HttpGet("https://www.yunxiaoyuan.com/basedataApi/school/getSchoolByExtId?termInfo=" + xnxq +"&extSchoolId=" + schoolId);
		            JSONObject obj = JSONObject.parseObject(res);
		            System.out.println("obj===" + obj );
		            if (obj == null || StringUtils.isBlank(obj.getString("id"))) {
		            	// 接口取不到学校
			        	 JSONObject error = new JSONObject();
			        	 error.put("fileName", file);
			        	 error.put("cdate", new Date());
			        	 error.put("msg", "学校接口调用失败"+schoolId);
			        	 errorList.add(error);
						continue; //取不到学校
					}
		            if (StringUtils.isNotBlank(obj.getString("id"))) {
		            	schoolId = obj.getString("id");
					}
		            Map<String , Object> map2 = new HashMap<String , Object>();
		            map2.put("termInfo", xnxq);
		           StringBuffer stuBuff = new StringBuffer();
		           String str = "";
		           Iterator<String> itor = stus.iterator();
		            while (itor.hasNext() ) {
		            	stuBuff.append(itor.next()+",");
					}
		            if (stuBuff.length() > 1) {
		            	str = stuBuff.substring(0, stuBuff.length() - 1);
					}
		            map2.put("extUserIds", str);
		            System.out.println("map2==" + map2 );
		            res =  callRemoteInterface.updateHttpRemoteInterface("https://www.yunxiaoyuan.com/basedataApi/account/listAccountByExtUserId", map2);
		            System.out.println("res==" +res  );
		            JSONArray arr = JSONObject.parseArray(res);
		            Map<String , String> stu2Map = new HashMap<String , String>();
		            List<Long> accounts = new ArrayList<Long>();
		            if (arr!=null && arr.size() > 0) {
		            	 for (int j = 0; j <arr.size(); j++) {
		            		 JSONObject stu = arr.getJSONObject(j);
		            		 JSONArray array = stu.getJSONArray("users");
		            		 if (array!=null) {
		            			 for (int k = 0; k < array.size(); k++) {
		            				 JSONObject user = array.getJSONObject(k);
		            				 stu2Map.put(user.getString("extId"), stu.getString("id"));	
		            				 accounts.add(stu.getLong("id"));
								 }
							}
						 }
					}
		            List<Account> accounts2 =allCommonDataService.getAccountBatch(Long.parseLong(schoolId), accounts , xnxq);
		            Map<String , String> stuClass = new HashMap<String , String>();
		            for (int j = 0; j < accounts2.size(); j++) {
		            	Account account = accounts2.get(j);
		            	List<User> users = account.getUsers();
		            	for (int k = 0; k < users.size(); k++) {
							User user = users.get(k);
							if (user.getStudentPart()!=null) {
								stuClass.put(account.getId() + "", user.getStudentPart().getClassId()  + "");
								break;
							}		
						}
					}
		        
		            
		            if (relate == null || StringUtils.isBlank(relate.getString("kslcdm"))) {
		            	  exam.put("xxdm", schoolId);
		            	  JSONObject result = scoreManageService.createExam(exam);//创建考试名称
		            	  result.put("xnxq", xnxq);
		            	  scoreManageService.insertDegreeinfoRelate(result);// 插入关联
		            	  kslcdm = result.getString("kslcdm");
					}else {
						  kslcdm = relate.getString("kslcdm");
					}
 		            Iterator<ScoreInfo> iterator = scoreInfos.iterator();
		            while (iterator.hasNext()) {
		            	  ScoreInfo scoreInfo = iterator.next();
		            	  String xh =  stu2Map.get(stuMap.get(scoreInfo.getXh()));
		            	  scoreInfo.setKslc(kslcdm);
		            	  scoreInfo.setBh(stuClass.get(xh));
		            	  if (StringUtils.isEmpty(xh) || StringUtils.isEmpty(stuClass.get(xh))) {
							  JSONObject error = new JSONObject();
							  error.put("fileName", file);
					          error.put("cdate", new Date());
					          error.put("msg", "找不到的学号或者班号："+scoreInfo.getXh());
					          errorList.add(error);
		            		  iterator.remove();// 学号没有  
		            		  continue;
						  }
		            	  if (StringUtils.isEmpty(scoreInfo.getKmdm())) {
		            		  JSONObject error = new JSONObject();
							  error.put("fileName", file);
					          error.put("cdate", new Date());
					          error.put("msg", "科目没有匹配到"+scoreInfo.getXh() );
					          errorList.add(error);
		            		  iterator.remove();// 学号没有  
		            		  continue;
		            		  
						  }
		            	  scoreInfo.setXh(xh); //
		            	  scoreInfo.setXxdm(schoolId);
					}
		            
		            //新建关联关系
		            StartImportTaskParam param = new StartImportTaskParam();
		            param.setXn(xn);
		            param.setXq(xq);
		            param.setXxdm(schoolId);
		            param.setKslc(kslcdm);
		            scoreManageService.insertScoreInfoBatch(scoreInfos , param); // 导入考试详细信息List<ScoreInfo> scoreList, StartImportTaskParam taskParam
		            ftpDel.add( file );
		            if (errorList.size() > 0) {
		            	scoreManageService.insertDegreeinfoError(xnxq, errorList);
					}else {
						 JSONObject error = new JSONObject();
						  error.put("fileName", file);
				          error.put("cdate", new Date());
				          error.put("msg", "考试数据正常" );
				          errorList.add(error);
						scoreManageService.insertDegreeinfoError(xnxq, errorList);
					}
		         }catch(ZipException e){
	            	  e.printStackTrace();
	              }
			}
			
			if (ftpDel.size() > 0) {
				for (int i = 0; i < ftpDel.size(); i++) {
					util.deleteFile(ftpDel.get(i), "/kw/"); //删除FTP 上的文件
				}
				util.delAllFile(tempFTPPath); //删除本地的下载文件和解压文件
			}

		}
 
		System.out.println( "执行完成....." + new Date() );
	} 
	
	
 
	
	 public JSONObject readFile(File f)  {
		  JSONObject jsonObject = null;
		  try {
			  InputStream in = new FileInputStream(f);   
		      byte b[]=new byte[(int)f.length()];     //创建合适文件大小的数组   
		      in.read(b);    //读取文件中的内容到b[]数组   
		      in.close();
		      String aString  = new String(b);
		      jsonObject =   JSONObject.parseObject(aString);
		      return jsonObject;
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		  return jsonObject;
	    
	 }
	 
 
	 
	public List<JSONObject> getStudent(String schoolId , List<String> studentCodes) {
		
		 Connection con =null;
		 String driver = "com.mysql.jdbc.Driver";
		 String url = "jdbc:mysql://kaowouter1.mysql.rds.aliyuncs.com:3306/exambase";
		 String user = "tw_view_user";
		 String password = "TW#_Pass#2019";
		 ResultSet rs = null;
		List<JSONObject> students = new ArrayList<JSONObject>();
		 try {
			 Class.forName(driver);
			 con = DriverManager.getConnection(url,user,password);
			 if(!con.isClosed())
			 System.out.println("Succeeded connecting to the Database!");
			 Statement statement = con.createStatement();
			 StringBuffer sql = new StringBuffer();
			 sql.append( " select accountId , studentCode , schoolId  from student_out_view where 1 = 1");
			 sql.append(" and kwSchoolId = '" +schoolId+ "' ");
			 if (studentCodes!=null && studentCodes.size() > 0) {
				 StringBuffer stu = new StringBuffer();
				 for (int i = 0; i < studentCodes.size(); i++) {
					 stu.append( "'" +  studentCodes.get(i) + "' ,");
				 }
				 sql.append( " and studentCode in ( " +stu.substring(0, stu.length() - 1)+ ")");
			 }
//			 System.out.println( "sql.toString()==" + sql.toString() );
			 rs = statement.executeQuery(sql.toString());
			 while(rs.next()){
				 JSONObject stu = new JSONObject();
				 stu.put("accountId", rs.getString("accountId"));
				 stu.put("studentCode", rs.getString("studentCode"));
				 stu.put("schoolId",  rs.getString("schoolId"));
				 students.add( stu );
			 }
			 System.out.println( "students===" + students.size() );
		} catch (Exception e) {
			 e.printStackTrace();
		}finally{
			 try {
				rs.close();
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		return students;
		
	}
	
	
	public static void main(String[] args) {
		
		CallRemoteInterface interface1 = new CallRemoteInterfaceImpl();
		String xnxq = "20182";
		String schoolId = "cbd282d3a964465fac038e19a4b8b543";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("extUserIds", "[\"cbd282d3a964465fac038e19a4b8b543\",\"cbd282d3a964465fac038e19a4b8b543\"]");
		map.put("termInfo", xnxq);
		String result = interface1.updateHttpRemoteInterface("https://www.yunxiaoyuan.com/basedataApi/account/listAccountByExtUserId"  , map);
		System.out.println(result);
	}
	
	
	public void copyFile(File sourceFile,File targetFile)  {    
			  try {
				  // 新建文件输入流并对它进行缓冲   
			        FileInputStream input = new FileInputStream(sourceFile);  
			        BufferedInputStream inBuff=new BufferedInputStream(input);  
			        // 新建文件输出流并对它进行缓冲   
			        FileOutputStream output = new FileOutputStream(targetFile);  
			        BufferedOutputStream outBuff=new BufferedOutputStream(output);  
			        // 缓冲数组   
			        byte[] b = new byte[1024 * 5];  
			        int len;  
			        while ((len =inBuff.read(b)) != -1) {  
			            outBuff.write(b, 0, len);  
			        }  
			        // 刷新此缓冲的输出流   
			        outBuff.flush();  
			        //关闭流   
			        inBuff.close();  
			        outBuff.close();  
			        output.close();  
			        input.close();  
			  } catch (Exception e) {
				 e.printStackTrace();
			  }
			       
	  }  
	
 
	
}
