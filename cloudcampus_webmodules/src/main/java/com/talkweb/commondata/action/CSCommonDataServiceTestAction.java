package com.talkweb.commondata.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.CurrentTermRsp;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.OrgInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.TermInfo;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.dao.TETestDao;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.HisCommonDataService;
import com.talkweb.repairManage.dao.RepairManageDao;

@Controller
@RequestMapping(value="/cSCommonDataServiceTestAction/")
public class CSCommonDataServiceTestAction {
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	@Autowired
	private TETestDao tETestDao; 
	@Autowired
	private RepairManageDao rd;
	@Autowired
	private AllCommonDataService allCommonDataService;
	@Autowired
	private HisCommonDataService hisCommonDataService;
	private static String termInfoId="20162";
	private static long schoolId=44898;
	//机构id
	private static String orgId="2462383,2462403,2462402,2462401,2462400,2462399,2462398,2462384,2462385";
	private static String name="";
	//机构中的年级组id
	private static String orgGradeId="2462403,2462402,2462401,2462400,2462399,2462398";
	//机构中的教研组id
	private static String researchId="2462384,2463657,2462383,2462397,2462385,2462386";
	//机构类型
	private static int orgType=1;
	private static String gradeId="203516,203517,203518,203519,203563,203564";
	
	private static List<Long> ids=new ArrayList<Long>(); //年级
	private static List<Long> classIds= new ArrayList<Long>();
	private static List<Long> lessonIds= new ArrayList<Long>();
	private static int lessonType=0;
	private static List<T_GradeLevel> tgl=new ArrayList<T_GradeLevel>();
	
	private static HashMap<String,Object> map=new HashMap<String,Object>();
	private static final Logger logger = LoggerFactory.getLogger(CSCommonDataServiceTestAction.class);
	private static School school=new School();
	static{
		school.setId(schoolId);
		map.put("schoolId", schoolId);
		//map.put("lessonId", "1,2,3");
		map.put("classId", "2016982,2016983,2016984,2016985,2016986,2016987,2016988,2016989,2016990,2016991,2016992,2016993,2016994,2016995,2016996,2016997,2016998,2016999,2017000,2017001,2017932,2100557,2102788,2102789,2102790,2102791,2102792,2102793,2102794,2102795,2102796,2102797,2102798,2102799,2102800,2102801,2102802,2102803,2102804,2102805,2102806,2102807,2102808,2102809,2102810,2102811,2102812,2102813,2102814,2102815,2102816,2102817,2102818,2102819,2102820,2102821,2102822,2102823,2102824,2102825,2102826,2102827,2109253,2109254,2109255,2109256,2109257,2109258,2109259,2109260,2109261,2109262,2109263,2109264,2109265,2109266,2109267");
		map.put("termInfoId", termInfoId);
		//map.put("usedGradeId", "2004,2005");
//		name="张";
		orgType=2;
		
		ids.add(203516L);
		ids.add(203517L);
		
		classIds.add(2047142L);
		classIds.add(2041767L);
		classIds.add(2041768L);
		classIds.add(2041769L);
		classIds.add(2041770L);
		classIds.add(2041771L);
		classIds.add(2041772L);
		classIds.add(2041773L);
		classIds.add(2041774L);
		classIds.add(2041791L);
		classIds.add(2041793L);
		classIds.add(2041790L);
		classIds.add(2041788L);
		classIds.add(2041789L);
		classIds.add(2041784L);

		lessonIds.add(1L);
		lessonIds.add(2L);
		
		tgl.add(T_GradeLevel.T_HighOne);
		tgl.add(T_GradeLevel.T_HighTwo);
	}
	@RequestMapping(value="insertSchool")
	public String insertSchool(HttpServletRequest req,HttpServletResponse res){
		JSONObject obj = tETestDao.selectSchool();
		if(obj==null){
			tETestDao.insertSchool();
		}
		Object key = "common.44898.00.schoolPlate";
		try {
			redisOperationDAO.set(key,"1");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "index";
	}
	@RequestMapping(value="deleteSchool")
	public String deleteSchool(HttpServletRequest req,HttpServletResponse res){
		tETestDao.deleteSchool();
		//获取redis数据 
		Object key = "common.44898.00.schoolPlate";
		try {
			redisOperationDAO.del(key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "index";
	}
	@RequestMapping(value="selectSchool")
	public String selectSchool(HttpServletRequest req,HttpServletResponse res){
		JSONObject obj = tETestDao.selectSchool();
		//获取redis数据 
		Object key = "common.44898.00.schoolPlate";
		Object obj1= null;
		try {
			obj1 = redisOperationDAO.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(obj1!=null && obj!=null){
			obj.put("redisMessage", obj1.toString());
		}
		List<Object> list = new ArrayList<Object>();
		JSONArray jsonarr = new JSONArray(list);
		JSONObject col = new JSONObject();
		List<JSONObject> line = new ArrayList<JSONObject>();
		JSONArray excelHeads = new JSONArray();
		col.put("field", "evaluateId");
		col.put("title", "evaluateId");
		line.add(col);
		excelHeads.add(line);
		ExcelTool.exportExcelWithData(jsonarr, excelHeads, "test2", null, req,res);
		return "index";
	}
	@RequestMapping(value="getCommonDataServiceTest")
	public String getHisCommonDataServiceTest(HttpServletRequest req,HttpServletResponse res){
		getCommonDataServiceTest();
		return "index";
	}
	
	public void getCommonDataServiceTest(){
		getCurTermInfo();
		getTermInfoBatch();
		getSchoolById();
		getCurrentXnxq();
		getAccountBatch();
		//getOrgTeacherList1();
		getOrgTeacherList2();
		getOrgTeacherList3();
		getOrgTeacherList4();
		getCourseTeacherList1();
		getCourseTeacherList2();
		getCourseTeacherList3();
		getDeanList();
		getAllSchoolEmployees();
		getGradeList();
		getGradeBatch();
		getClassList();
		getSimpleClassList();
		getClassroomBatch();
		getClassroomBatchNoAccount();
		getSimpleClassBatch();
		getLessonInfoBatch();
		getLessonInfoList();
		getLessonInfoByType();
		getStudentList1();
		getStudentList2();
		getSchoolOrgList1();
		getSchoolOrgList2();
		getSchoolOrgAllById();
		getSchoolOrgBatch();
		getGradeById();
		getGradeByGradeLevel();
		getGradeByGradeLevelBatch();
		getUserBatch();
		getAccountAllById1();
		getAccountAllById2();
		getAccountById();
		getAccountAllByIdNoFilter();
		getAllStudent();
		getAllClass();
		getClassById();
		getUserById1();
		getUserById2();
		getUserPermissionById();
		getAccountAllByAccount();
		getSchoolByUserId();
	}
	
	public void getCurTermInfo(){
		CurrentTermRsp ctr = allCommonDataService.getCurTermInfo(schoolId);
		//System.out.println(ctr);
	}
	public void getTermInfoBatch(){
		List<Long> termIds = new ArrayList<Long>();
		List<TermInfo> tList = allCommonDataService.getTermInfoBatch(termIds,schoolId);
		//System.out.println(tList);
	}
	public void getSchoolById(){
		long time1=new Date().getTime();
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(s);
		logger.info("cszhh基础数据接口：方法【getSchoolById】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getSchoolById】，耗时time:{},入参termInfoId:{},schoolId{},出参School:{}",(time2-time1),termInfoId,schoolId,s);
	}
	public void getCurrentXnxq(){
		School sch = allCommonDataService.getSchoolById(schoolId, termInfoId);
		String s = allCommonDataService.getCurrentXnxq(sch);
		//System.out.println(s);
	}
	public void getAccountBatch(){
		String sid="100988027,100988027,100988027,100988033,100988033,100988033,100988035,100988035,100988035,100988037,100988037,100988037,100988039,100988039,100988039,100988041,100988041,100988041,100988043,100988043,100988043,100988045,100988045,100988045,100988047,100988047,100988047,100988049,100988049,100988049,100988051,100988051,100988051,100988053,100988053,100988053,100988055,100988055,100988055,100988059,100988059,100988059,100988061,100988061,100988061,100988063,100988063,100988063,100988065,100988065,100988065,100988067,100988067,100988067,100988069,100988069,100988069,100988071,100988071,100988071,100988073,100988073,100988073,100988075,100988075,100988075,100988077,100988077,100988077,100988079,100988079,100988079,100988081,100988081,100988081,100988083,100988083,100988083,100988087,100988087,100988087,100988089,100988089,100988089,100988091,100988091,100988091,100988093,100988093,100988093,100988095,100988095,100988095,100988097,100988097,100988097,100988099,100988099,100988099,100988101,100988101,100988101,100988103,100988103,100988103,100988105,100988105,100988105,100988107,100988107,100988107,100988109,100988109,100988109,100988111,100988111,100988111,100988113,100988113,100988113,100988115,100988115,100988115,100988117,100988117,100988117,100988119,100988119,100988119,100988121,100988121,100988121,100988131,100988131,100988131,100988133,100988133,100988133,100988135,100988135,100988135,100988137,100988137,100988137,100988139,100988139,100988139,100988141,100988141,100988141,100988143,100988143,100988143,100988145,100988145,100988145,100988147,100988147,100988147,100988149,100988149,100988149,100988151,100988151,100988151,100988153,100988153,100988153,100988155,100988155,100988155,100988157,100988157,100988157,100988159,100988159,100988159,100988161,100988161,100988161,100988163,100988163,100988163,100988165,100988165,100988165,100988167,100988167,100988167,100988169,100988169,100988169,100988171,100988171,100988171,100988173,100988173,100988173,100988175,100988175,100988175,100988177,100988177,100988177,100988179,100988179,100988179,100988181,100988181,100988181,100988183,100988183,100988183,100988185,100988185,100988185,100988187,100988187,100988187,100988189,100988189,100988189,100988191,100988191,100988191,100988193,100988193,100988193,100988195,100988195,100988195,100988197,100988197,100988197,100988199,100988199,100988199,100988201,100988201,100988201,100988203,100988203,100988203,100988205,100988205,100988205,100988207,100988207,100988207,100988209,100988209,100988209,100988211,100988211,100988211,100988213,100988213,100988213,100988215,100988215,100988215,100988217,100988217,100988217,100988219,100988219,100988219,100988221,100988221,100988221,100988223,100988223,100988223,100988225,100988225,100988225,100988227,100988227,100988227,100988229,100988229,100988229,100988231,100988231,100988231,100988233,100988233,100988233,100988237,100988237,100988237,100988239,100988239,100988239,100988241,100988241,100988241,100988243,100988243,100988243,100988245,100988245,100988245,100988247,100988247,100988247,100988249,100988249,100988249,100988251,100988251,100988251,100988253,100988253,100988253,100988255,100988255,100988255,100988257,100988257,100988257,100988259,100988259,100988259,100988261,100988261,100988261,100988263,100988263,100988263,100988265,100988265,100988265,100988267,100988267,100988267,100988269,100988269,100988269,100988271,100988271,100988271,100988273,100988273,100988273,100988275,100988275,100988275,100988277,100988277,100988277,100988279,100988279,100988279,100988283,100988283,100988283,100988285,100988285,100988285,100988287,100988287,100988287,100988289,108291082,108291082,108291084,108291084,108291084,108291086,108291086,108291086,108291088,108291088,108291088,108291090,108291090,108291090,108291092,108291092,108291092,108291094,108291094,108291094,108291096,108291096,108291096,108291098,108291098,108291098";
		List<Long> aList = StringUtil.toListFromString(sid);
		long time1=new Date().getTime();
		List<Account> list=allCommonDataService.getAccountBatch(schoolId, aList, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(list);
		logger.info("cszhh基础数据接口：方法【getAccountBatch】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getAccountBatch】，耗时time:{},入参termInfoId:{},schoolId{},List<Long>:{},出参List<Account>:{}",(time2-time1),termInfoId,schoolId,aList,list);
	}
	/*public void getOrgTeacherList1(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		String sid = "100984785,100984779,100984822,100985011,100985070,100985025,100984975,100984958,100984777,100984778,100984779,100984791,100984878,100984879,100984905,100984918,100984932,100984936,100984964,100984996,100985025,100985045,100985058,100985076,100985108,101009626,101163322,102798847,102805060";
		List<Long> aList = StringUtil.toListFromString(sid);
		List<Account> list=allCommonDataService.getAccountBatch(schoolId, aList, termInfoId);
		long time1=new Date().getTime();
		List<JSONObject> accountList = allCommonDataService.getOrgTeacherList(list, s, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(accountList);
		logger.info("cszhh基础数据接口：方法【getOrgTeacherList1】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getOrgTeacherList1】，耗时time:{},入参termInfoId:{},school{},List<Account>:{},出参List<JSONObject>:{}",(time2-time1),termInfoId,s,list,accountList);
	}*/
	public void getOrgTeacherList2(){
		long time1=new Date().getTime();
		List<Account> aList = allCommonDataService.getOrgTeacherList(termInfoId, schoolId, orgId, name);
		long time2=new Date().getTime();
		//System.out.println(aList);
		logger.info("cszhh基础数据接口：方法【getOrgTeacherList2】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getOrgTeacherList2】，耗时time:{},入参termInfoId:{},schoolId{},orgId:{},name:{},出参List<Account>:{}",(time2-time1),termInfoId,schoolId,orgId,name,aList);
	}
	public void getOrgTeacherList3(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		long time1=new Date().getTime();
		List<Account> aList = allCommonDataService.getOrgTeacherList(termInfoId, s, 1, name);
		long time2=new Date().getTime();
		//System.out.println(aList);
		logger.info("cszhh基础数据接口：方法【getOrgTeacherList3】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getOrgTeacherList3】，耗时time:{},入参termInfoId:{},school{},type:{},name:{},出参List<Account>:{}",(time2-time1),termInfoId,s,1,name,aList);
	}
	public void getOrgTeacherList4(){
		long time1=new Date().getTime();
		List<Account> aList = allCommonDataService.getOrgTeacherList(termInfoId, schoolId, orgGradeId, researchId, name);
		long time2=new Date().getTime();
		//System.out.println(aList);
		logger.info("cszhh基础数据接口：方法【getOrgTeacherList4】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getOrgTeacherList4】，耗时time:{},入参termInfoId:{},schoolId{},gradeId:{},researchId:{},name:{},出参List<Account>:{}",(time2-time1),termInfoId,schoolId,gradeId,researchId,name,aList);
	}
	public void getCourseTeacherList1(){
		long time1=new Date().getTime();
		List<Account> aList = allCommonDataService.getCourseTeacherList(map);
		long time2=new Date().getTime();
		//System.out.println(aList);
		logger.info("cszhh基础数据接口：方法【getCourseTeacherList1】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getCourseTeacherList1】，耗时time:{},入参map:{},出参List<Account>:{}",(time2-time1),map,aList);
	}
	public void getCourseTeacherList2(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		long time1=new Date().getTime();
		JSONArray aList = allCommonDataService.getCourseTeacherList(s, map);
		long time2=new Date().getTime();
		//System.out.println(aList);
		logger.info("cszhh基础数据接口：方法【getCourseTeacherList2】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getCourseTeacherList2】，耗时time:{},入参termInfoId:{},shool:{},map:{},出参List<Account>:{}",(time2-time1),termInfoId,s,map,aList);
	}
	public void getCourseTeacherList3(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		long time1=new Date().getTime();
		List<Account> aList = allCommonDataService.getCourseTeacherList(termInfoId, s, name);
		long time2=new Date().getTime();
		//System.out.println(aList);
		logger.info("cszhh基础数据接口：方法【getCourseTeacherList3】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getCourseTeacherList3】，耗时time:{},入参termInfoId:{},shool:{},name:{},出参List<Account>:{}",(time2-time1),termInfoId,s,name,aList);
	}
	public void getDeanList(){
		long time1=new Date().getTime();
		List<Account> aList = allCommonDataService.getDeanList(map);
		long time2=new Date().getTime();
		//System.out.println(aList);
		logger.info("cszhh基础数据接口：方法【getDeanList】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getDeanList】，耗时time:{},入参map:{},出参List<Account>:{}",(time2-time1),map,aList);
	}
	public void getAllSchoolEmployees(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		long time1=new Date().getTime();
		List<Account> aList=allCommonDataService.getAllSchoolEmployees(s, termInfoId, name);
		long time2=new Date().getTime();
		//System.out.println(aList);
		logger.info("cszhh基础数据接口：方法【getAllSchoolEmployees】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getAllSchoolEmployees】，耗时time:{},入参termInfoId:{},school:{},name:{},出参List<Account>:{}",(time2-time1),termInfoId,s,name,aList);
	}
	public void getGradeList(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		long time1=new Date().getTime();
		List<Grade> gList = allCommonDataService.getGradeList(s, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(gList);
		logger.info("cszhh基础数据接口：方法【getGradeList】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getGradeList】，耗时time:{},入参termInfoId:{},school:{},出参List<Grade>:{}",(time2-time1),termInfoId,s,gList);
	}
	public void getGradeBatch(){
		List<Long> gList = StringUtil.toListFromString(gradeId);
		long time1=new Date().getTime();
		List<Grade> gl = allCommonDataService.getGradeBatch(schoolId, gList, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(gl);
		logger.info("cszhh基础数据接口：方法【getGradeBatch】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getGradeBatch】，耗时time:{},入参termInfoId:{},schoolId:{},List<Long>:{},出参List<Grade>:{}",(time2-time1),termInfoId,schoolId,gList,gl);
	}
	public void getClassList(){
		long time1=new Date().getTime();
		List<Classroom> cList = allCommonDataService.getClassList(map);
		long time2=new Date().getTime();
		//System.out.println(cList);
		logger.info("cszhh基础数据接口：方法【getClassList】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getClassList】，耗时time:{},入参map:{},出参List<Classroom>:{}",(time2-time1),map,cList);
	}
	public void getSimpleClassList(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		List<Long> g=StringUtil.toListFromString(gradeId);
		List<Grade> gList = allCommonDataService.getGradeBatch(schoolId, g, termInfoId);
		long time1=new Date().getTime();
		List<Classroom> cList = allCommonDataService.getSimpleClassList(s, gList, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(cList);
		logger.info("cszhh基础数据接口：方法【getSimpleClassList】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getSimpleClassList】，耗时time:{},入参termInfoId:{},school:{},gList:{},出参List<Classroom>:{}",(time2-time1),termInfoId,s,gList,cList);
	}
	public void getClassroomBatch(){
		long time1=new Date().getTime();
		List<Classroom> cList = allCommonDataService.getClassroomBatch(schoolId, classIds, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(cList);
		logger.info("cszhh基础数据接口：方法【getClassroomBatch】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getClassroomBatch】，耗时time:{},入参termInfoId:{},schoolId:{},classIds:{},出参List<Classroom>:{}",(time2-time1),termInfoId,schoolId,classIds,cList);
	}
	public void getClassroomBatchNoAccount(){
		long time1=new Date().getTime();
		List<Classroom> cList = allCommonDataService.getClassroomBatchNoAccount(schoolId, classIds, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(cList);
		logger.info("cszhh基础数据接口：方法【getClassroomBatchNoAccount】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getClassroomBatchNoAccount】，耗时time:{},入参termInfoId:{},schoolId:{},classIds:{},出参List<Classroom>:{}",(time2-time1),termInfoId,schoolId,classIds,cList);
	}
	public void getSimpleClassBatch(){
		long time1=new Date().getTime();
		List<Classroom> cList = allCommonDataService.getSimpleClassBatch(schoolId, classIds, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(cList);
		logger.info("cszhh基础数据接口：方法【getSimpleClassBatch】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getSimpleClassBatch】，耗时time:{},入参termInfoId:{},schoolId:{},classIds:{},出参List<Classroom>:{}",(time2-time1),termInfoId,schoolId,classIds,cList);
	}
	public void getLessonInfoBatch(){
		long time1=new Date().getTime();
		List<LessonInfo> lList = allCommonDataService.getLessonInfoBatch(schoolId, lessonIds, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(lList);
		logger.info("cszhh基础数据接口：方法【getLessonInfoBatch】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getLessonInfoBatch】，耗时time:{},入参termInfoId:{},schoolId:{},lessonIds:{},出参List<LessonInfo>:{}",(time2-time1),termInfoId,schoolId,lessonIds,lList);
	}
	public void getLessonInfoList(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		long time1=new Date().getTime();
		List<LessonInfo> lList = allCommonDataService.getLessonInfoList(s,termInfoId);
		long time2=new Date().getTime();
		//System.out.println(lList);
		logger.info("cszhh基础数据接口：方法【getLessonInfoList】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getLessonInfoList】，耗时time:{},入参termInfoId:{},school:{},出参List<LessonInfo>:{}",(time2-time1),termInfoId,s,lList);
	}
	public void getLessonInfoByType(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		long time1=new Date().getTime();
		List<LessonInfo> lList = allCommonDataService.getLessonInfoByType(s, 0, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(lList);
		logger.info("cszhh基础数据接口：方法【getLessonInfoByType】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getLessonInfoByType】，耗时time:{},入参termInfoId:{},school:{},type:{},出参List<LessonInfo>:{}",(time2-time1),termInfoId,s,0,lList);
	}
	public void getStudentList1(){
		long time1=new Date().getTime();
		List<Account> aList = allCommonDataService.getStudentList(map);
		long time2=new Date().getTime();
		//System.out.println(aList);
		logger.info("cszhh基础数据接口：方法【getStudentList1】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getStudentList1】，耗时time:{},入参map:{},出参List<Account>:{}",(time2-time1),map,aList);
	}
	public void getStudentList2(){
		long time1=new Date().getTime();
		List<Account> aList = allCommonDataService.getStudentList(schoolId, termInfoId, "陈佳");
		long time2=new Date().getTime();
		//System.out.println(aList);
		logger.info("cszhh基础数据接口：方法【getStudentList2】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getStudentList2】，耗时time:{},入参termInfoId:{},schoolId:{},name:“陈佳”,出参List<Account>:{}",(time2-time1),termInfoId,schoolId,aList);
	}
	public void getSchoolOrgList1(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		long time1=new Date().getTime();
		List<OrgInfo> list = allCommonDataService.getSchoolOrgList(s, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(list);
		logger.info("cszhh基础数据接口：方法【getSchoolOrgList1】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getSchoolOrgList1】，耗时time:{},入参termInfoId:{},school:{},出参List<OrgInfo>:{}",(time2-time1),termInfoId,s,list);
	}
	public void getSchoolOrgList2(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		long time1=new Date().getTime();
		List<OrgInfo> list = allCommonDataService.getSchoolOrgList(s, 1, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(list);
		logger.info("cszhh基础数据接口：方法【getSchoolOrgList2】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getSchoolOrgList2】，耗时time:{},入参termInfoId:{},school:{},type:{},出参List<OrgInfo>:{}",(time2-time1),termInfoId,s,1,list);
	}
	public void getSchoolOrgAllById(){
		long time1=new Date().getTime();
		OrgInfo o = allCommonDataService.getSchoolOrgAllById(schoolId, 2462383, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(o);
		logger.info("cszhh基础数据接口：方法【getSchoolOrgAllById】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getSchoolOrgAllById】，耗时time:{},入参termInfoId:{},schoolId:{},orgId:{},出参OrgInfo:{}",(time2-time1),termInfoId,schoolId,2462383,o);
	}
	public void getSchoolOrgBatch(){
		List<Long> ll=StringUtil.toListFromString(orgId);
		long time1=new Date().getTime();
		List<OrgInfo> list = allCommonDataService.getSchoolOrgBatch(schoolId, ll, termInfoId);
		long time2=new Date().getTime();
//		System.out.println(list);
		logger.info("cszhh基础数据接口：方法【getSchoolOrgBatch】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getSchoolOrgBatch】，耗时time:{},入参termInfoId:{},schoolId:{},List<Long>:{},出参List<OrgInfo>:{}",(time2-time1),termInfoId,schoolId,ll,list);
	}
	public void getGradeById(){
		long time1=new Date().getTime();
		Grade g = allCommonDataService.getGradeById(schoolId, 208458, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(g);
		logger.info("cszhh基础数据接口：方法【getGradeById】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getGradeById】，耗时time:{},入参termInfoId:{},schoolId:{},gradeId:{},出参Grade:{}",(time2-time1),termInfoId,schoolId,208458,g);
	}
	public void  getGradeByGradeLevel(){
		T_GradeLevel gl = T_GradeLevel.T_HighOne;
		long time1=new Date().getTime();
		Grade g = allCommonDataService.getGradeByGradeLevel(schoolId, gl, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(g);
		logger.info("cszhh基础数据接口：方法【getGradeByGradeLevel】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getGradeByGradeLevel】，耗时time:{},入参termInfoId:{},schoolId:{},T_GradeLevel:{},出参Grade:{}",(time2-time1),termInfoId,schoolId,gl,g);
	}
	public void getGradeByGradeLevelBatch(){
		List<T_GradeLevel> glList = new ArrayList<T_GradeLevel>();
		glList.add(T_GradeLevel.T_HighThree);
		glList.add(T_GradeLevel.T_HighOne);
		glList.add(T_GradeLevel.T_HighTwo);
		glList.add(T_GradeLevel.T_JuniorOne);
		glList.add(T_GradeLevel.T_JuniorThree);
		glList.add(T_GradeLevel.T_JuniorTwo);
		long time1=new Date().getTime();
		List<Grade> gList = allCommonDataService.getGradeByGradeLevelBatch(schoolId, glList, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(gList);
		logger.info("cszhh基础数据接口：方法【getGradeByGradeLevelBatch】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getGradeByGradeLevelBatch】，耗时time:{},入参termInfoId:{},schoolId:{},glList:{},出参List<Grade>:{}",(time2-time1),termInfoId,schoolId,glList,gList);
	}
	public void getUserBatch(){
		String s = "1009277216,1009277218,1009277220,1009277222,1009277224,1009277226,1009277228,1009277230,1009277232,1009277234,1009277236,1009277238,1009277240,1009277242,1009277244,1009277246,1009277248,1009277250,1009277252,1009277254,1009277256,1009277258,1009277260,1009277262,1009277264,1009277266,1009277268,1009277270,1009277272,1009277274,1009277276,1009277278,1009277280,1009277282,1009277284,1009277286,1009277288,1009277290,1009277292,1009277294,1009277296,1009277298,1009277300,1009277302,1009277304,1009277306,1009277308,1009277310,1009277312,1009277314,1009277316,1009277318,1009277320,1009277322,1009277324,1009277326,1009277328,1009277330,1009277332,1009277334,1009277336,1009277338,1009277340,1009277342,1009277344,1009277346,1009277348,1009277350,1009277352,1009277354,1009277356,1009277358,1009277360,1009277362,1009277364,1009277366,1009277368,1009277370,1009277372,1009277374,1009277376,1009277378,1009277380,1009277382,1009277384,1009277386,1009277388,1009277390,1009277392,1009277394,1009277396,1009277398,1009277400,1009277402,1009277404,1009277406,1009277408,1009277410,1009277412,1009277414,1009277416,1009277418,1009277420,1009277422,1009277424,1009277426,1009277428,1009277430,1009277432,1009277434,1009277436,1009277438,1009277440,1009277442,1009277444,1009277446,1009277448,1009277450,1009277452,1009277454,1009277456,1009277458,1009277460,1009277462,1009277464,1009277466";
		List<Long> ids = StringUtil.toListFromString(s);
		long time1=new Date().getTime();
		List<User> uList = allCommonDataService.getUserBatch(schoolId, ids, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(uList);
		logger.info("cszhh基础数据接口：方法【getUserBatch】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getUserBatch】，耗时time:{},入参termInfoId:{},schoolId:{},ids:{},出参List<User>:{}",(time2-time1),termInfoId,schoolId,ids,uList);
	}
	public void getAccountAllById1(){
		long time1=new Date().getTime();
		Account a = allCommonDataService.getAccountAllById(100985011L);
		long time2=new Date().getTime();
		//System.out.println(a);
		logger.info("cszhh基础数据接口：方法【getAccountAllById1】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getAccountAllById1】，耗时time:{},入参id:{},出参Account:{}",(time2-time1),100985011L,a);
	}
	public void getAccountAllById2(){
		long time1=new Date().getTime();
		Account a = allCommonDataService.getAccountAllById(schoolId, 100985011L, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(a);
		logger.info("cszhh基础数据接口：方法【getAccountAllById2】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getAccountAllById2】，耗时time:{},入参termInfoId:{},schoolId:{},id:{},出参Account:{}",(time2-time1),termInfoId,schoolId,100985011L,a);
	}
	public void getAccountById(){
		long time1=new Date().getTime();
		Account a = allCommonDataService.getAccountById(schoolId, 100985011L, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(a);
		logger.info("cszhh基础数据接口：方法【getAccountById】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getAccountById】，耗时time:{},入参termInfoId:{},schoolId:{},id:{},出参Account:{}",(time2-time1),termInfoId,schoolId,100985011L,a);
	}
	public void getAccountAllByIdNoFilter(){
		long time1=new Date().getTime();
		Account a =allCommonDataService.getAccountAllByIdNoFilter(schoolId, 100985011L, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(a);
		logger.info("cszhh基础数据接口：方法【getAccountAllByIdNoFilter】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getAccountAllByIdNoFilter】，耗时time:{},入参termInfoId:{},schoolId:{},id:{},出参Account:{}",(time2-time1),termInfoId,schoolId,100985011L,a);
	}
	public void getAllStudent(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		long time1=new Date().getTime();
		List<Account> aList = allCommonDataService.getAllStudent(s, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(aList);
		logger.info("cszhh基础数据接口：方法【getAllStudent】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getAllStudent】，耗时time:{},入参termInfoId:{},school:{},出参List<Account>:{}",(time2-time1),termInfoId,s,aList);
	}
	public void getAllClass(){
		School s = allCommonDataService.getSchoolById(schoolId, termInfoId);
		long time1=new Date().getTime();
		List<Classroom> cList = allCommonDataService.getAllClass(s, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(cList);
		logger.info("cszhh基础数据接口：方法【getAllClass】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getAllClass】，耗时time:{},入参termInfoId:{},school:{},出参List<Classroom>:{}",(time2-time1),termInfoId,s,cList);
	}
	public void getClassById(){
		long time1=new Date().getTime();
		Classroom c = allCommonDataService.getClassById(schoolId, 2016982, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(c);
		logger.info("cszhh基础数据接口：方法【getClassById】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getClassById】，耗时time:{},入参termInfoId:{},schoolId:{},id:{},出参Classroom:{}",(time2-time1),termInfoId,schoolId,2016982,c);
	}
	public void getUserById1(){
		User c = allCommonDataService.getUserById(schoolId, 1001405174);
		System.out.println(c);
		long time1=new Date().getTime();
		User c1 = allCommonDataService.getUserById(0, 1001405174);
		long time2=new Date().getTime();
		logger.info("cszhh基础数据接口：方法【getUserById1】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getUserById1】，耗时time:{},入参userId:1001405174,schoolId:0,出参User:{}",(time2-time1),c1);
		//System.out.println(c1);
	}
	public void getUserById2(){
		long time1=new Date().getTime();
		User c = allCommonDataService.getUserById(schoolId, 1001405174, termInfoId);
		long time2=new Date().getTime();
		//System.out.println(c);
		logger.info("cszhh基础数据接口：方法【getUserById2】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getUserById2】，耗时time:{},入参termInfoId:{},schoolId:{},id:{},出参User:{}",(time2-time1),termInfoId,schoolId,1001405174,c);
	}
	public void getUserPermissionById(){
		long time1=new Date().getTime();
		//UserPermissions up = allCommonDataService.getUserPermissionById(schoolId, 1001405174);
		long time2=new Date().getTime();
		//System.out.println(up);
		logger.info("cszhh基础数据接口：方法【getUserPermissionById】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getUserPermissionById】，耗时time:{},入参schoolId:{},id:{},出参UserPermissions:{}",(time2-time1),schoolId,1001405174,up);
	}
	public void getAccountAllByAccount(){
		long time1=new Date().getTime();
		Account a = allCommonDataService.getAccountAllByAccount(schoolId, "19310000299");
		long time2=new Date().getTime();
		//System.out.println(a);
		logger.info("cszhh基础数据接口：方法【getAccountAllByAccount】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getAccountAllByAccount】，耗时time:{},入参schoolId:{},id:{},出参Account:{}",(time2-time1),schoolId,"19310000299",a);
	}
	
	public void getSchoolByUserId(){
		School s = allCommonDataService.getSchoolByUserId(schoolId, 1001405174);
		System.out.println(s);
		long time1=new Date().getTime();
		School s1 = allCommonDataService.getSchoolByUserId(0, 1001405174);
		long time2=new Date().getTime();
		//System.out.println(s1);
		logger.info("cszhh基础数据接口：方法【getSchoolByUserId】，耗时time:{}",(time2-time1));
		//logger.info("基础数据接口：方法【getSchoolByUserId】，耗时time:{},入参schoolId:{},id:{},出参School:{}",(time2-time1),0,"1001405174",s1);
	}
}
