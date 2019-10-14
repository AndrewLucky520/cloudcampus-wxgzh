package com.talkweb.commondata.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.OrgInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.CurCommonDataService;

@Controller
@RequestMapping(value="/allCommonDataServiceTestAction/")
public class AllCommonDataServiceTestAction {
	@Autowired
	private AllCommonDataService allCommonDataService;
	@Autowired
	private CurCommonDataService curCommonDataService;
	//private static String termInfoId="20151";
	private static long schoolId=44898;
	//机构id
	private static String orgId="2462383,2462403";
	private static String name="";
	//机构中的年级组id
	private static String orgGradeId="2462402,2462403,2462400,2462398,2462399";
	//机构中的教研组id
	private static String researchId="2462383";
	//机构类型
	private static int orgType=1;
	private static String gradeId="203516,203517,203518,203519,203563,203564";
	
	private static List<Long> ids=new ArrayList<Long>(); //年级
	private static List<Long> classIds= new ArrayList<Long>();
	private static List<Long> lessonIds= new ArrayList<Long>();
	private static int lessonType=0;
	private static List<T_GradeLevel> tgl=new ArrayList<T_GradeLevel>();
	
	private static String userId="100017,100018,100034";
	private static List<Long> userIds = new ArrayList<Long>() ;
	
	private static HashMap<String,Object> map=new HashMap<String,Object>();
	private static final Logger logger = LoggerFactory.getLogger(AllCommonDataServiceTestAction.class);
	private static School school=new School();
	static{
		school.setId(schoolId);
		map.put("schoolId", schoolId);
		map.put("lessonId", "1,2,3");
//		map.put("classId", "2016959,2016961");
//		map.put("termInfoId", termInfoId);
		map.put("usedGradeId", "2004,2005");
//		name="张";
		orgType=2;
		
		userIds =  StringUtil.toListFromString(userId);
		
		ids.add(203516L);
		ids.add(203517L);
		
		classIds.add(2016989L);
		classIds.add(2016990L);
		classIds.add(2016991L);
		classIds.add(2016992L);
		

		lessonIds.add(1L);
		lessonIds.add(2L);
		
		tgl.add(T_GradeLevel.T_HighOne);
		tgl.add(T_GradeLevel.T_HighTwo);
	}
	
	@RequestMapping(value="getHisCommonDataServiceTest")
	public String getHisCommonDataServiceTest(HttpServletRequest req,HttpServletResponse res){
		String termInfoId = "20152";
		map.put("termInfoId", termInfoId);
		getCommonDataServiceTest(termInfoId);
		return "index";
	}
	
	@RequestMapping(value="getCurCommonDataServiceTest")
	public String getCurCommonDataServiceTest(HttpServletRequest req,HttpServletResponse res){
		String termInfoId = "20161";
		map.put("termInfoId", termInfoId);
		school=curCommonDataService.getSchoolById(schoolId);
		getCommonDataServiceTest(termInfoId);
		return "index";
	}
	
	public void getCommonDataServiceTest(String termInfoId){
		getOrgTeacherList(termInfoId);
		getOrgTeacherList2(termInfoId);
		getOrgTeacherList3(termInfoId);
		//getOrgTeacherList4(termInfoId);
		getCourseTeacherList(termInfoId);
		getCourseTeacherList2(termInfoId);
		getCourseTeacherList3(termInfoId);
		getSchoolById(termInfoId);
		getDeanList(termInfoId);
		getAllSchoolEmployees(termInfoId);
		getAccountBatch(termInfoId);
		getStudentList(termInfoId);
		getMapStudentList(termInfoId);
		getAllClass(termInfoId);
		getAllStudent(termInfoId);
		getAccountAllById(termInfoId);
		getAccountById(termInfoId);
		getClassById(termInfoId);
		getSchoolOrgList(termInfoId);
		QuerySchoolOrgList(termInfoId);
		getSchoolOrgAllById(termInfoId);
		getSchoolOrgBatch(termInfoId);
		testGetGradeList(termInfoId);
		testGetGradeBatch(termInfoId);
		testGetGradeById(termInfoId);
		testGetGradeByGradeLevel(termInfoId);
		testGetGradeByGradeLevelBatch(termInfoId);
		testGetClassList(termInfoId);
		testGetSimpleClassList(termInfoId);
		testGetClassroomBatch(termInfoId);
		testGetClassroomBatchNoAccount(termInfoId);
		testGetSimpleClassBatch(termInfoId);
		testGetLessonInfoBatch(termInfoId);
		testGetLessonInfoList(termInfoId);
		testGetLessonInfoByType(termInfoId);
		testGetUserBatch(termInfoId);
	}
	
	public void getOrgTeacherList(String termInfoId)
	{
		long time1=new Date().getTime();
		List<Account> list=allCommonDataService.getOrgTeacherList(termInfoId,schoolId,orgId,name);
		long time2=new Date().getTime();
		logger.info("基础数据接口：查询多个机构下面的老师，方法【getOrgTeacherList】，耗时time:{},入参termInfoId:{},schoolId{},orgId{},name{},出参：{}",(time2-time1),termInfoId,schoolId,orgId,name,list);
		
	}
	
	public void getOrgTeacherList2(String termInfoId)
	{
		long time1=new Date().getTime();
		List<Account> list=allCommonDataService.getOrgTeacherList(termInfoId, schoolId, orgGradeId, researchId, name);
		long time2=new Date().getTime();
		logger.info("基础数据接口：查询年级组和教研组共有的老师，方法【getOrgTeacherList】，耗时time:{},入参termInfoId:{},schoolId{},orgGradeId{},researchId{},name{},出参：{}",(time2-time1),termInfoId,schoolId, orgGradeId, researchId, name,list);

	}
	
	public void getOrgTeacherList3(String termInfoId)
	{
		long time1=new Date().getTime();
		List<Account> list=allCommonDataService.getOrgTeacherList(termInfoId, school, orgType, name);
		long time2=new Date().getTime();
		logger.info("基础数据接口：查询某种机构(年级组/教研组/备课组/科室)下面的教师信息，方法【getOrgTeacherList】，耗时time:{},入参termInfoId:{},schoolId{},orgType{},name{},出参：{}",(time2-time1),termInfoId,schoolId, orgType, name,list);

	}
	
	/*public void getOrgTeacherList4(String termInfoId)
	{
		long time1=new Date().getTime();
		List<Account> tlist = new ArrayList<Account>();
		Account account = new Account();
		account.setId(100984779);
		List<User> users = new ArrayList<User>();
		User user = new User();
		UserPart userPart = new UserPart();
		userPart.setRole(T_Role.Teacher);
		List<Long> orgIds = new ArrayList<Long>();
		orgIds.add(2462383l);
		orgIds.add(2462403l);
		userPart.setOrgIds(orgIds);
		user.setUserPart(userPart);
		users.add(user);
		account.setUsers(users);
		tlist.add(account);
		List<JSONObject> gList = allCommonDataService.getOrgTeacherList(tlist, school, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：查询所属机构（多个老师list），方法【getOrgTeacherList】，耗时time:{},入参List<Account>:{},school:{},termInfoId:{},出参：{}",(time2-time1),tlist,school, termInfoId,gList);

	}*/
	
	public void getCourseTeacherList(String termInfoId)
	{
		long time1=new Date().getTime();
		List<Account> list=allCommonDataService.getCourseTeacherList(termInfoId, school, name);
		long time2=new Date().getTime();
		logger.info("基础数据接口：获取全校当前任课的老师信息，方法【getCourseTeacherList】，耗时time:{},入参termInfoId:{},schoolId{},name{},出参：{}",(time2-time1),termInfoId,schoolId,name,list);

	}
	
	
	public void getCourseTeacherList2(String termInfoId)
	{
		long time1=new Date().getTime();
		List<Account> list=allCommonDataService.getCourseTeacherList(map);
		long time2=new Date().getTime();
		logger.info("基础数据接口：获取任课老师信息，方法【getCourseTeacherList】，耗时time:{},入参map:{},出参：{}",(time2-time1),map.toString(),list);

	}
	
	
	public void getCourseTeacherList3(String termInfoId)
	{
		long time1=new Date().getTime();
		JSONArray list=allCommonDataService.getCourseTeacherList(school,map);
		long time2=new Date().getTime();
		logger.info("基础数据接口：获取任课老师信息，方法【getCourseTeacherList】，耗时time:{},入参schoolId:{},map{},出参：{}",(time2-time1),schoolId,map.toString(),list);

	}
	
	
	public void getSchoolById(String termInfoId)
	{
		long time1=new Date().getTime();
		School school=allCommonDataService.getSchoolById(schoolId,termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：获取任课老师信息，方法【getSchoolById】，耗时time:{},入参schoolId:{},termInfoId{},出参：{}",(time2-time1),schoolId,termInfoId,school);

	}
	
	
	public void getDeanList(String termInfoId)
	{
		long time1=new Date().getTime();
		List<Account> list=allCommonDataService.getDeanList(map);
		long time2=new Date().getTime();
		logger.info("基础数据接口：获取班主任信息，方法【getDeanList】，耗时time:{},入参map{},出参：{}",(time2-time1),map,list);

	}
	
	
	public void getAllSchoolEmployees(String termInfoId)
	{
		long time1=new Date().getTime();
		List<Account> list=allCommonDataService.getAllSchoolEmployees(school, termInfoId, name);
		long time2=new Date().getTime();
		logger.info("基础数据接口：获取全校所有教职工，方法【getAllSchoolEmployees】，耗时time:{},入参schoolId:{},termInfoId{},name{},出参：{}",(time2-time1),school, termInfoId, name,list);

	}
	
	public void getAccountBatch(String termInfoId)
	{
	
		long time1=new Date().getTime();
		String ids="100985010,100985011,100985016,100985017,100985020,100985023,100985025,100985029,100985030,100985033,100985037,100985042,"+
				"100985045,100985046,100985051,100985052,100985055,100985057,100985058,100985061,100985062,100985063,100985066,100985076,"+
				"100985077,100985080,100985085,100985086,100985087,100985092,100985093,100985101,100985105,100985107,100985108,100985110,"+
				"100985111,100985112,100985113,100985114,100985122,100985124,100984958,100985012,100985014,100985019,100985026,100985027,"+
				"100985031,100985032,100985040,100985047,100985048,100985049,100985050,100985053,100985060,100985065,100985067,100985070,"+
				"100985072,100985074,100985088,100985089,100985090,100985091,100985094,100985095,100985096,100985097,100985098,100985100,"+
				"100985102,100985103,100985106,100985115,100985116,100985118,100985119,100985120,100985121,100985123,100985125,100985126,"+
				"101009523,101009524,101009525,101009526,101009527,101009528,101009529,101009530,101009531,101009532,101009533,101009534,"+
				"101009535,101009536,101009537,101009538,101009539,101009540,101009541,101009542,101009543,101009544,101009545,101009546,"+
				"101009547,101009548,101009549,101009550,101009551,101009552,101009553,101009554,101009555,101009556,101009557,101009558,"+
				"101009559,101009560,101009561,101009562,101009563,101009564,101009565,101009566,101009567,101009568,101009569,101009570,"+
				"101009571,101009572,101009573,101009574,101009575,101009576,101009577,101009578,101009579,101009580,101009581,101009582,"+
				"101009583,101009584,101009585,101009586,101009587,101009588,101009589,101009590,101009591,101009592,101009593,101009594,"+
				"101009595,101009596,101009597,101009598,101009599,101009600,100984757,100984758,100984759,100984760,100984761,100984762,"+
				"100984763,100984764,100984765,100984766,100984767,100984768,100984769,100984770,100984771,100984772,100984773,100984774,"+
				"100984775,100984776,100984777,100984778,100984779,100984781,100984785,100984787,100984790,100984791,100984798,100984801,"+
				"100984804,100984813,100984814,100984818,100984823,100984826,100984829,100984831,100984832,100984835,100984837,100984849,"+
				"100984854,100984863,100984864,100984868,100984871,100984872,100984873,100984875,100984878,100984879,100984880,100984882,"+
				"100984886,100984890,100984893,100984894,100984899,100984905,100984906,100984908,100984909,100984910,100984915,100984917,"+
				"100984918,100984920,100984923,100984924,100984925,100984927,100984928,100984930,100984931,100984932,100984933,100984936,"+
				"100984937,100984942,100984946,100984948,100984949,100984950,100984956,100984959,100984961,100984963,100984964,100984967,"+
				"100984970,100984971,100984973,100984974,100984979,100984980,100984989,100984990,100984995,100984996,100985003,100985008,"+
				"100984738,100984739,100984740,100984741,100984742,100984743,100984744,100984745,100984746,100984747,100984748,100984749,"+
				"100984750,100984751,100984752,100984753,100984754,100984755,100984756,100984780,100984782,100984784,100984788,100984793,"+
				"100984795,100984800,100984802,100984807,100984810,100984811,100984812,100984816,100984820,100984822,100984827,100984834,"+
				"100984836,100984842,100984843,100984844,100984848,100984850,100984860,100984861,100984862,100984865,100984866,100984869,"+
				"100984870,100984877,100984884,100984887,100984888,100984889,100984895,100984896,100984901,100984902,100984912,100984913,"+
				"100984916,100984919,100984921,100984926,100984929,100984934,100984935,100984939,100984940,100984941,100984943,100984945,"+
				"100984947,100984951,100984953,100984955,100984957,100984960,100984965,100984972,100984975,100984977,100984983,100984984,"+
				"100984987,100984988,100984993,100984999,100985004,100985006,100984737,101009601,101009602,101009603,101009604,101009605,"+
				"101009606,101009607,101009608,101009609,101009610,101009611,101009612,101009613,101009614,101009615,101009616,101009617,"+
				"101009618,101009619,101009620,101009621,101009622,101009623,101009624,101009625,101009626,101009627,101009628,101009629,"+
				"101009630,101009631,101009632,101009633,101009634,101009635,101107227,101010434,101163322,101163323,101163324,101163325,"+
				"101163326,101163327,101163328";
		
		
		List<Long> accountIds=StringUtil.toListFromString(ids);
		
		List<Account> list=allCommonDataService.getAccountBatch(schoolId, accountIds, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：批量获取机构人员acccount,方法【getAccountBatch】，耗时time:{},入参schoolId:{},accountIds{},termInfoId{},出参：{}",(time2-time1),schoolId,accountIds, accountIds,termInfoId,list);

	}
	
	public void getStudentList(String termInfoId){
		long time1=new Date().getTime();
		List<Account> la = allCommonDataService.getStudentList(schoolId, termInfoId, name);
		long time2=new Date().getTime();
		logger.info("基础数据接口：获取全校所有学生，方法【getStudentList】，耗时time:{},入参schoolId:{},termInfoId{},name{},出参：{}",(time2-time1),school, termInfoId, name,la);

	}
	
	public void getMapStudentList(String termInfoId){
		long time1=new Date().getTime();
		map.put("keyword", "张");
		List<Account> la = allCommonDataService.getStudentList(map);
		long time2=new Date().getTime();
		logger.info("基础数据接口：获取学生列表，方法【getStudentList】，耗时time:{},入参map{},出参：{}",(time2-time1),map,la);

	}
	
	public void getAllClass(String termInfoId){
		long time1=new Date().getTime();
		school.setGrades(StringUtil.toListFromString(gradeId));
		List<Classroom> lc = allCommonDataService.getAllClass(school, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：获取全校所有班级，方法【getAllClass】，耗时time:{},入参school:{},termInfoId:{},出参：{}",(time2-time1),school,termInfoId,lc);

	}
	
	public void getAllStudent(String termInfoId){
		long time1=new Date().getTime();
		school.setGrades(StringUtil.toListFromString(gradeId));
		List<Account> la = allCommonDataService.getAllStudent(school, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：获取全校所有学生，方法【getAllStudent】，耗时time:{},入参school:{},termInfoId:{},出参：{}",(time2-time1),school,termInfoId,la);

	}
	
	public void getAccountAllById(String termInfoId){
		long time1=new Date().getTime();
		Account account = allCommonDataService.getAccountAllById(schoolId, 100471624, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据accountId获取account信息，方法【getAccountAllById】，耗时time:{},入参schoolId:{},accountId:{},termInfoId:{},出参：{}",(time2-time1),schoolId,100471624,termInfoId,account);

	}
	
	public void getAccountById(String termInfoId){
		long time1=new Date().getTime();
		Account account = allCommonDataService.getAccountById(schoolId, 100471624, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据accountId获取account信息简版，方法【getAccountById】，耗时time:{},入参schoolId:{},accountId:{},termInfoId:{},出参：{}",(time2-time1),schoolId,100471624,termInfoId,account);
	}
	
	public void getClassById(String termInfoId){
		long time1=new Date().getTime();
		long classId = 2016998;
		Classroom classroom = allCommonDataService.getClassById(schoolId, classId, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据班级Id获取班级信息，方法【getClassById】，耗时time:{},入参schoolId:{},classId:{},termInfoId:{},出参：{}",(time2-time1),schoolId,classId,termInfoId,classroom);
	}
	
	
	public void getSchoolOrgList(String termInfoId){
		long time1=new Date().getTime();
		int type = 1;
		List<OrgInfo> gList = allCommonDataService.getSchoolOrgList(school, type, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：获取机构信息（年级组，教研组，备课组，科室），方法【getSchoolOrgList】，耗时time:{},入参school:{},type:{},termInfoId:{},出参：{}",(time2-time1),school,type,termInfoId,gList);
	}
	
	
	public void QuerySchoolOrgList(String termInfoId){
		long time1=new Date().getTime();
		List<OrgInfo> gList = allCommonDataService.getSchoolOrgList(school, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：通过学校获取机构信息，方法【getSchoolOrgList】，耗时time:{},入参school:{},termInfoId:{},出参：{}",(time2-time1),school,termInfoId,gList);
	}
	
	
	public void getSchoolOrgAllById(String termInfoId){
		long time1=new Date().getTime();
		long sorgId = 2462383;
		OrgInfo info = allCommonDataService.getSchoolOrgAllById(schoolId, sorgId, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：通过机构ID获取机构详细信息，方法【getSchoolOrgAllById】，耗时time:{},入参schoolId:{},sorgId:{},termInfoId:{},出参：{}",(time2-time1),schoolId,sorgId,termInfoId,info);
	}
	
	
	public void getSchoolOrgBatch(String termInfoId){
		long time1=new Date().getTime();
		List<Long> orgids = new ArrayList<Long>();
		orgids.add(2462383l);
		orgids.add(2462403l);
		List<OrgInfo> gList = allCommonDataService.getSchoolOrgBatch(schoolId, orgids, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：批量接口 通过机构ID获取机构详细信息，方法【getSchoolOrgBatch】，耗时time:{},入参schoolId:{},orgids:{},termInfoId:{},出参：{}",(time2-time1),schoolId,orgids,termInfoId,gList);
	}
	
	
	public void testGetGradeList(String termInfoId) {
		long time1=new Date().getTime();
		List<Grade> gList=allCommonDataService.getGradeList(school, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据学校ID获取年级列表，方法【getGradeList】，耗时time:{},入参schoolId:{},termInfoId:{},出参：{}",(time2-time1),schoolId,termInfoId,gList);
	}
	
	
	public void testGetGradeBatch(String termInfoId) {
		long time1=new Date().getTime();
		List<Grade> gList2=allCommonDataService.getGradeBatch(schoolId, ids, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据年级IDs获取年级列表，方法【getGradeBatch】，耗时time:{},入参schoolId:{},termInfoId:{},gradeIds:{},出参：{}",(time2-time1),schoolId,termInfoId,ids.toString(),gList2);
	}
	
	public void testGetGradeById(String termInfoId) {
		long time1=new Date().getTime();
	    Grade g2=allCommonDataService.getGradeById(schoolId,ids.get(0),termInfoId);
	    long time2=new Date().getTime();
	    logger.info("基础数据接口：根据年级ID获取年级对象，方法【getGradeById】，耗时time:{},入参schoolId:{},termInfoId:{},gradeId:{},出参：{}",(time2-time1),schoolId,termInfoId,ids.get(0).toString(),g2);
	}
	
	public void testGetGradeByGradeLevel(String termInfoId) {
		long time1=new Date().getTime();
	    Grade g2=allCommonDataService.getGradeByGradeLevel(schoolId,tgl.get(0), termInfoId);
	    long time2=new Date().getTime();
	    logger.info("基础数据接口：根据年级Level获取年级对象，方法【getGradeByGradeLevel】，耗时time:{},入参schoolId:{},termInfoId:{},gradeLevel:{},出参：{}",(time2-time1),schoolId,termInfoId,tgl.get(0).toString(),g2);
	}
 	
	
	public void testGetGradeByGradeLevelBatch(String termInfoId) {
		long time1=new Date().getTime();
	    List<Grade> gList2=allCommonDataService.getGradeByGradeLevelBatch(schoolId,tgl, termInfoId);
	    long time2=new Date().getTime();
	    logger.info("基础数据接口：根据年级Levels获取年级对象，方法【getGradeByGradeLevelBatch】，耗时time:{},入参schoolId:{},termInfoId:{},gradeLevels:{},出参：{}",(time2-time1),schoolId,termInfoId,tgl.toString(),gList2);
	} 
	
	public void testGetClassList(String termInfoId) {
		long time1=new Date().getTime();
		List<Classroom> gList2=allCommonDataService.getClassList(map);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据全校ID获取班级列表，方法【getClassList】，耗时time:{},入参map:{},出参：{}",(time2-time1),map.toString(),gList2);
	}
	
	
	public void testGetSimpleClassList(String termInfoId) {
		long time1=new Date().getTime();
		List<Classroom> gList2=allCommonDataService.getSimpleClassList(school, null, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据全校ID获取班级列表简版，方法【getSimpleClassList】，耗时time:{},入参schoolId:{},termInfoId:{},List<Grade>:{},出参：{}",(time2-time1),schoolId,termInfoId,null,gList2);
	}
	
	
	public void testGetClassroomBatch(String termInfoId) {
		long time1=new Date().getTime();
		List<Classroom> gList2=allCommonDataService.getClassroomBatch(schoolId, classIds, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据班级IDs获取班级列表，方法【getClassroomBatch】，耗时time:{},入参schoolId:{},termInfoId:{},classIds:{},出参：{}",(time2-time1),schoolId,termInfoId,classIds,gList2);
	}
	public void testGetClassroomBatchNoAccount(String termInfoId) {
		long time1=new Date().getTime();
		List<Classroom> gList2=allCommonDataService.getClassroomBatchNoAccount(schoolId, classIds, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据班级IDs获取班级列表，方法【getClassroomBatchNoAccount】，耗时time:{},入参schoolId:{},termInfoId:{},classIds:{},出参：{}",(time2-time1),schoolId,termInfoId,classIds,gList2);
	}
	
	public void testGetSimpleClassBatch(String termInfoId) {
		long time1=new Date().getTime();
		List<Classroom> gList2=allCommonDataService.getSimpleClassBatch(schoolId, classIds,termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据班级IDs获取班级列表简版，方法【getSimpleClassBatch】，耗时time:{},入参schoolId:{},termInfoId:{},classIds:{},出参：{}",(time2-time1),schoolId,termInfoId,classIds,gList2);
	}
	
	public void testGetLessonInfoBatch(String termInfoId) {
		long time1=new Date().getTime();
		List<LessonInfo> gList2=allCommonDataService.getLessonInfoBatch(schoolId, lessonIds, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据科目Ids获取科目列表，方法【getLessonInfoBatch】，耗时time:{},入参schoolId:{},termInfoId:{},lessonIds:{},出参：{}",(time2-time1),schoolId,termInfoId,lessonIds,gList2);
	}
	
	public void testGetLessonInfoList(String termInfoId) {
		long time1=new Date().getTime();
		List<LessonInfo> gList2=allCommonDataService.getLessonInfoList(school, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据学校ID获取科目列表，方法【getLessonInfoList】，耗时time:{},入参schoolId:{},termInfoId:{},出参：{}",(time2-time1),schoolId,termInfoId,gList2);
	}
	
	public void testGetLessonInfoByType(String termInfoId) {
		long time1=new Date().getTime();
		List<LessonInfo> gList2=allCommonDataService.getLessonInfoByType(school,lessonType, termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据学校ID获取某种类型科目列表，方法【getLessonInfoList】，耗时time:{},入参schoolId:{},termInfoId:{},lessonType:{},出参：{}",(time2-time1),schoolId,termInfoId,lessonType,gList2);
	}
	public void testGetUserBatch(String termInfoId){
		long time1=new Date().getTime();
		List<User> list=allCommonDataService.getUserBatch(schoolId, userIds,termInfoId);
		long time2=new Date().getTime();
		logger.info("基础数据接口：根据学校ID获取某种类型科目列表，方法【getUserBatch】，耗时time:{},入参schoolId:{},userIds:{},出参：{}",(time2-time1),schoolId,termInfoId,list);
	}
	//----------------------end of author:zhh
}
