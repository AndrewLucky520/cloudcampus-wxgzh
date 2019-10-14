package com.talkweb.placementtask.utils.div.medium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.google.common.base.Joiner;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.dto.SubjectGroup;
import com.talkweb.placementtask.utils.div.medium.dto.LayInfo;
import com.talkweb.placementtask.utils.div.medium.dto.MediumClassData;
import com.talkweb.placementtask.utils.div.medium.dto.MediumWishSubjectGroup;

public class TestMedium {
	
	public static void main(String[] args) {
		
		//List<MediumWishSubjectGroup> wishDataList1 = createWishData();
		
		List<MediumWishSubjectGroup> wishDataList2 = createWishData2();
		
    	//准备分层数据
//    	LayInfo laya=new LayInfo(1, "物理层", Arrays.asList("789", "678","478","479","679","467"));
//    	LayInfo layb=new LayInfo(1, "历史层", Arrays.asList("458", "589","568","459","569","456"));
//    	layList.add(laya);
//    	layList.add(layb);
		
		//String studentWishJsonStr = JSONObject.toJSONString(studentWishList);
		MediumClassData md = MediumDivOperation.excuteDiv(7, wishDataList2, 5L);
	}
	
	public static void main22(String[] args) {
		
		List<MediumWishSubjectGroup> wishDataList = generateRandomWish(864, 90);
    	
    	//准备分层数据
    	List<LayInfo> layList = new ArrayList<>();
    	LayInfo laya=new LayInfo(1, "物理层", Arrays.asList("789", "678","478","479","679","467"));
    	LayInfo layb=new LayInfo(1, "历史层", Arrays.asList("458", "589","568","459","569","456"));
    	layList.add(laya);
    	layList.add(layb);
		//String studentWishJsonStr = JSONObject.toJSONString(studentWishList);
    	
		MediumDivOperation.excuteDiv(55, wishDataList, 10L);
	}
	
	
	public static List<MediumWishSubjectGroup> generateRandomWish(Integer studentCount, Integer maxClassSize) {
		
		List<MediumWishSubjectGroup> wishDataList = new LinkedList<>();
		MediumWishSubjectGroup mw789 = new MediumWishSubjectGroup();
		mw789.setId("789");
		mw789.setIds(Arrays.asList("7","8","9"));
		mw789.setName("物化生");
		wishDataList.add(mw789);
		
		MediumWishSubjectGroup mw678 = new MediumWishSubjectGroup();
		mw678.setId("678");
		mw678.setIds(Arrays.asList("6","7","8"));
		mw678.setName("地物化");
		wishDataList.add(mw678);
		
		MediumWishSubjectGroup mw478 = new MediumWishSubjectGroup();
		mw478.setId("478");
		mw478.setIds(Arrays.asList("4","7","8"));
		mw478.setName("政物化");
		wishDataList.add(mw478);
		
		MediumWishSubjectGroup mw479 = new MediumWishSubjectGroup();
		mw479.setId("479");
		mw479.setIds(Arrays.asList("4","7","9"));
		mw479.setName("政物生");
		wishDataList.add(mw479);
		
		MediumWishSubjectGroup mw679 = new MediumWishSubjectGroup();
		mw679.setId("679");
		mw679.setIds(Arrays.asList("6","7","9"));
		mw679.setName("地物生");
		wishDataList.add(mw679);
		
		MediumWishSubjectGroup mw467 = new MediumWishSubjectGroup();
		mw467.setId("467");
		mw467.setIds(Arrays.asList("4","6","7"));
		mw467.setName("政地物");
		wishDataList.add(mw467);
		
		MediumWishSubjectGroup mw458 = new MediumWishSubjectGroup();
		mw458.setId("458");
		mw458.setIds(Arrays.asList("4","5","8"));
		mw458.setName("政史化");
		wishDataList.add(mw458);
		
		MediumWishSubjectGroup mw589 = new MediumWishSubjectGroup();
		mw589.setId("589");
		mw589.setIds(Arrays.asList("5","8","9"));
		mw589.setName("史化生");
		wishDataList.add(mw589);
		
		MediumWishSubjectGroup mw568 = new MediumWishSubjectGroup();
		mw568.setId("568");
		mw568.setIds(Arrays.asList("5","6","8"));
		mw568.setName("史地化");
		wishDataList.add(mw568);
		
		MediumWishSubjectGroup mw594 = new MediumWishSubjectGroup();
		mw594.setId("459");
		mw594.setIds(Arrays.asList("4","5","9"));
		mw594.setName("政史地");
		mw594.setStudentCount(79);
		mw594.setFixedClassCount(1);
		mw594.setFixedStudentCount(79);
		wishDataList.add(mw594);
		
		MediumWishSubjectGroup mw596 = new MediumWishSubjectGroup();
		mw596.setId("569");
		mw596.setIds(Arrays.asList("5","6","9"));
		mw596.setName("史地生");
		wishDataList.add(mw596);
		
		MediumWishSubjectGroup mw546 = new MediumWishSubjectGroup();
		mw546.setId("456");
		mw546.setIds(Arrays.asList("4","5","6"));
		mw546.setName("政史地");
		wishDataList.add(mw546);
		
		for (int i=0;i<studentCount;i++) {
			int randomInt = new Random().nextInt(14);
			SubjectGroup wish = wishDataList.get(randomInt);
			String sid = String.valueOf(i+1);
			wish.getStudents().add(new Student(sid, sid, Joiner.on(",").skipNulls().join(wish.getIds()), wish.getName()));
		}
		
		
		for (SubjectGroup subjectGroup : wishDataList) {
			MediumWishSubjectGroup wish = (MediumWishSubjectGroup)subjectGroup;
			int sCount = subjectGroup.getStudents().size();
			wish.setStudentCount(sCount);
			Double maxClassCount = Math.ceil(sCount/maxClassSize);
			if(maxClassCount>1) {
				//固定班数
				int randomInt = new Random().nextInt(maxClassCount.intValue());
				wish.setFixedClassCount(randomInt);
				
				//固定人数
				int fixedCount = maxClassCount.intValue()*randomInt;
				wish.setFixedStudentCount(Math.min(fixedCount, sCount));
			}
		}
		
		return wishDataList;
	}
	
	
	public static List<MediumWishSubjectGroup> createWishData2(){
		
		List<MediumWishSubjectGroup> mediumWishSubjectGroupList = new ArrayList<MediumWishSubjectGroup>();
		MediumWishSubjectGroup MediumWishSubjectGroup1 = new MediumWishSubjectGroup();
		MediumWishSubjectGroup1.setId("568");
		MediumWishSubjectGroup1.setIds(Arrays.asList("5","6","8"));
		MediumWishSubjectGroup1.setName("史地化");
		MediumWishSubjectGroup1.setStudentCount(22);
		MediumWishSubjectGroup1.setFixedClassCount(0);
		MediumWishSubjectGroup1.setFixedStudentCount(0);
		MediumWishSubjectGroup1.setLayId(2);
		MediumWishSubjectGroup1.setLayName("历史层");
		mediumWishSubjectGroupList.add(MediumWishSubjectGroup1);
		
		MediumWishSubjectGroup MediumWishSubjectGroup2 = new MediumWishSubjectGroup();
		MediumWishSubjectGroup2.setId("589");
		MediumWishSubjectGroup2.setIds(Arrays.asList("5","8","9"));
		MediumWishSubjectGroup2.setName("史化生");
		MediumWishSubjectGroup2.setStudentCount(59);
		MediumWishSubjectGroup2.setFixedClassCount(0);
		MediumWishSubjectGroup2.setFixedStudentCount(0);
		MediumWishSubjectGroup2.setLayId(2);
		MediumWishSubjectGroup2.setLayName("历史层");
		mediumWishSubjectGroupList.add(MediumWishSubjectGroup2);
		
		MediumWishSubjectGroup MediumWishSubjectGroup3 = new MediumWishSubjectGroup();
		MediumWishSubjectGroup3.setId("458");
		MediumWishSubjectGroup3.setIds(Arrays.asList("4","5","8"));
		MediumWishSubjectGroup3.setName("政史化");
		MediumWishSubjectGroup3.setStudentCount(60);
		MediumWishSubjectGroup3.setFixedClassCount(1);
		MediumWishSubjectGroup3.setFixedStudentCount(55);
		MediumWishSubjectGroup3.setLayId(2);
		MediumWishSubjectGroup3.setLayName("历史层");
		mediumWishSubjectGroupList.add(MediumWishSubjectGroup3);
		
		MediumWishSubjectGroup MediumWishSubjectGroup4 = new MediumWishSubjectGroup();
		MediumWishSubjectGroup4.setId("569");
		MediumWishSubjectGroup4.setIds(Arrays.asList("5","6","9"));
		MediumWishSubjectGroup4.setName("史地生");
		MediumWishSubjectGroup4.setStudentCount(46);
		MediumWishSubjectGroup4.setFixedClassCount(0);
		MediumWishSubjectGroup4.setFixedStudentCount(0);
		MediumWishSubjectGroup4.setLayId(2);
		MediumWishSubjectGroup4.setLayName("历史层");
		mediumWishSubjectGroupList.add(MediumWishSubjectGroup4);
		
		MediumWishSubjectGroup MediumWishSubjectGroup5 = new MediumWishSubjectGroup();
		MediumWishSubjectGroup5.setId("459");
		MediumWishSubjectGroup5.setIds(Arrays.asList("4","5","9"));
		MediumWishSubjectGroup5.setName("政史生");
		MediumWishSubjectGroup5.setStudentCount(93);
		MediumWishSubjectGroup5.setFixedClassCount(1);
		MediumWishSubjectGroup5.setFixedStudentCount(55);
		MediumWishSubjectGroup5.setLayId(2);
		MediumWishSubjectGroup5.setLayName("历史层");
		mediumWishSubjectGroupList.add(MediumWishSubjectGroup5);
		
		MediumWishSubjectGroup MediumWishSubjectGroup6 = new MediumWishSubjectGroup();
		MediumWishSubjectGroup6.setId("456");
		MediumWishSubjectGroup6.setIds(Arrays.asList("4","5","6"));
		MediumWishSubjectGroup6.setName("政史地");
		MediumWishSubjectGroup6.setStudentCount(61);
		MediumWishSubjectGroup6.setFixedClassCount(1);
		MediumWishSubjectGroup6.setFixedStudentCount(55);
		MediumWishSubjectGroup6.setLayId(2);
		MediumWishSubjectGroup6.setLayName("历史层");
		mediumWishSubjectGroupList.add(MediumWishSubjectGroup6);
		
		MediumWishSubjectGroup MediumWishSubjectGroup7 = new MediumWishSubjectGroup();
		MediumWishSubjectGroup7.setId("678");
		MediumWishSubjectGroup7.setIds(Arrays.asList("6","7","8"));
		MediumWishSubjectGroup7.setName("地物化");
		MediumWishSubjectGroup7.setStudentCount(99);
		MediumWishSubjectGroup7.setFixedClassCount(2);
		MediumWishSubjectGroup7.setFixedStudentCount(99);
		MediumWishSubjectGroup7.setLayId(1);
		MediumWishSubjectGroup7.setLayName("物理层");
		mediumWishSubjectGroupList.add(MediumWishSubjectGroup7);
		
		MediumWishSubjectGroup MediumWishSubjectGroup8 = new MediumWishSubjectGroup();
		MediumWishSubjectGroup8.setId("789");
		MediumWishSubjectGroup8.setIds(Arrays.asList("7","8","9"));
		MediumWishSubjectGroup8.setName("物化生");
		MediumWishSubjectGroup8.setStudentCount(136);
		MediumWishSubjectGroup8.setFixedClassCount(2);
		MediumWishSubjectGroup8.setFixedStudentCount(100);
		MediumWishSubjectGroup8.setLayId(1);
		MediumWishSubjectGroup8.setLayName("物理层");
		mediumWishSubjectGroupList.add(MediumWishSubjectGroup8);
		
		MediumWishSubjectGroup MediumWishSubjectGroup9 = new MediumWishSubjectGroup();
		MediumWishSubjectGroup9.setId("478");
		MediumWishSubjectGroup9.setIds(Arrays.asList("4","7","8"));
		MediumWishSubjectGroup9.setName("政物化");
		MediumWishSubjectGroup9.setStudentCount(88);
		MediumWishSubjectGroup9.setFixedClassCount(1);
		MediumWishSubjectGroup9.setFixedStudentCount(55);
		MediumWishSubjectGroup9.setLayId(1);
		MediumWishSubjectGroup9.setLayName("物理层");
		mediumWishSubjectGroupList.add(MediumWishSubjectGroup9);
		
		MediumWishSubjectGroup MediumWishSubjectGroup10 = new MediumWishSubjectGroup();
		MediumWishSubjectGroup10.setId("679");
		MediumWishSubjectGroup10.setIds(Arrays.asList("6","7","9"));
		MediumWishSubjectGroup10.setName("地物生");
		MediumWishSubjectGroup10.setStudentCount(73);
		MediumWishSubjectGroup10.setFixedClassCount(1);
		MediumWishSubjectGroup10.setFixedStudentCount(50);
		MediumWishSubjectGroup10.setLayId(1);
		MediumWishSubjectGroup10.setLayName("物理层");
		mediumWishSubjectGroupList.add(MediumWishSubjectGroup10);
		
		MediumWishSubjectGroup MediumWishSubjectGroup11 = new MediumWishSubjectGroup();
		MediumWishSubjectGroup11.setId("479");
		MediumWishSubjectGroup11.setIds(Arrays.asList("4","7","9"));
		MediumWishSubjectGroup11.setName("政物生");
		MediumWishSubjectGroup11.setStudentCount(73);
		MediumWishSubjectGroup11.setFixedClassCount(1);
		MediumWishSubjectGroup11.setFixedStudentCount(50);
		MediumWishSubjectGroup11.setLayId(1);
		MediumWishSubjectGroup11.setLayName("物理层");
		mediumWishSubjectGroupList.add(MediumWishSubjectGroup11);
		
		MediumWishSubjectGroup MediumWishSubjectGroup12 = new MediumWishSubjectGroup();
		MediumWishSubjectGroup12.setId("467");
		MediumWishSubjectGroup12.setIds(Arrays.asList("4","6","7"));
		MediumWishSubjectGroup12.setName("政地物");
		MediumWishSubjectGroup12.setStudentCount(44);
		MediumWishSubjectGroup12.setFixedClassCount(0);
		MediumWishSubjectGroup12.setFixedStudentCount(0);
		MediumWishSubjectGroup12.setLayId(1);
		MediumWishSubjectGroup12.setLayName("物理层");
		mediumWishSubjectGroupList.add(MediumWishSubjectGroup12);
		
    	for (MediumWishSubjectGroup wish : mediumWishSubjectGroupList) {
    		List<Student> students = createStudent(wish.getStudentCount(), Joiner.on(",").skipNulls().join(wish.getIds()), wish.getName());
    		wish.setStudents(students);
		}
    	
    	return mediumWishSubjectGroupList;
	}
	
	
	public static List<MediumWishSubjectGroup> createWishData(){

    	
		List<MediumWishSubjectGroup> wishDataList = new ArrayList<>();
		MediumWishSubjectGroup mw789 = new MediumWishSubjectGroup();
		mw789.setId("789");
		mw789.setIds(Arrays.asList("7","8","9"));
		mw789.setName("物化生");
		mw789.setStudentCount(148);
		mw789.setFixedClassCount(2);
		mw789.setFixedStudentCount(96);
		mw789.setLayId(1);
		mw789.setLayName("物理层");
		wishDataList.add(mw789);
		
		MediumWishSubjectGroup mw678 = new MediumWishSubjectGroup();
		mw678.setId("678");
		mw678.setIds(Arrays.asList("6","7","8"));
		mw678.setName("地物化");
		mw678.setStudentCount(92);
		mw678.setFixedClassCount(2);
		mw678.setFixedStudentCount(92);
		mw678.setLayId(1);
		mw678.setLayName("物理层");
		wishDataList.add(mw678);
		
		MediumWishSubjectGroup mw478 = new MediumWishSubjectGroup();
		mw478.setId("478");
		mw478.setIds(Arrays.asList("4","7","8"));
		mw478.setName("政物化");
		mw478.setStudentCount(73); 
		mw478.setFixedClassCount(1);
		mw478.setFixedStudentCount(48);
		mw478.setLayId(1);
		mw478.setLayName("物理层");
		wishDataList.add(mw478);
		
		MediumWishSubjectGroup mw479 = new MediumWishSubjectGroup();
		mw479.setId("479");
		mw479.setIds(Arrays.asList("4","7","9"));
		mw479.setName("政物生");
		mw479.setStudentCount(76); 
		mw479.setFixedClassCount(1);
		mw479.setFixedStudentCount(76);
		mw479.setLayId(1);
		mw479.setLayName("物理层");
		wishDataList.add(mw479);
		
		MediumWishSubjectGroup mw679 = new MediumWishSubjectGroup();
		mw679.setId("679");
		mw679.setIds(Arrays.asList("6","7","9"));
		mw679.setName("地物生");
		mw679.setStudentCount(95); 
		mw679.setFixedClassCount(2);
		mw679.setFixedStudentCount(95);
		mw679.setLayId(1);
		mw679.setLayName("物理层");
		wishDataList.add(mw679);
		
		MediumWishSubjectGroup mw467 = new MediumWishSubjectGroup();
		mw467.setId("467");
		mw467.setIds(Arrays.asList("4","6","7"));
		mw467.setName("政地物");
		mw467.setStudentCount(51); 
		mw467.setFixedClassCount(1);
		mw467.setFixedStudentCount(51);
		mw467.setLayId(1);
		mw467.setLayName("物理层");
		wishDataList.add(mw467);
		
		MediumWishSubjectGroup mw458 = new MediumWishSubjectGroup();
		mw458.setId("458");
		mw458.setIds(Arrays.asList("4","5","8"));
		mw458.setName("政史化");
		mw458.setStudentCount(56); 
		mw458.setFixedClassCount(1);
		mw458.setFixedStudentCount(56);
		mw458.setLayId(1);
		mw458.setLayName("物理层");
		wishDataList.add(mw458);
		
		MediumWishSubjectGroup mw589 = new MediumWishSubjectGroup();
		mw589.setId("589");
		mw589.setIds(Arrays.asList("5","8","9"));
		mw589.setName("史化生");
		mw589.setStudentCount(70); 
		mw589.setFixedClassCount(1);
		mw589.setFixedStudentCount(48);
		mw589.setLayId(1);
		mw589.setLayName("物理层");
		wishDataList.add(mw589);
		
		MediumWishSubjectGroup mw568 = new MediumWishSubjectGroup();
		mw568.setId("568");
		mw568.setIds(Arrays.asList("5","6","8"));
		mw568.setName("史地化");
		mw568.setStudentCount(28); 
		mw568.setFixedClassCount(1);
		mw568.setFixedStudentCount(28);
		mw568.setLayId(1);
		mw568.setLayName("物理层");
		wishDataList.add(mw568);
		
		MediumWishSubjectGroup mw594 = new MediumWishSubjectGroup();
		mw594.setId("459");
		mw594.setIds(Arrays.asList("4","5","9"));
		mw594.setName("政史地");
		mw594.setStudentCount(79);
		mw594.setFixedClassCount(1);
		mw594.setFixedStudentCount(79);
		mw594.setLayId(1);
		mw594.setLayName("物理层");
		wishDataList.add(mw594);
		
		MediumWishSubjectGroup mw596 = new MediumWishSubjectGroup();
		mw596.setId("569");
		mw596.setIds(Arrays.asList("5","6","9"));
		mw596.setName("史地生");
		mw596.setStudentCount(40);
		mw596.setFixedClassCount(1);
		mw596.setFixedStudentCount(40);
		mw596.setLayId(1);
		mw596.setLayName("物理层");
		wishDataList.add(mw596);
		
		MediumWishSubjectGroup mw546 = new MediumWishSubjectGroup();
		mw546.setId("456");
		mw546.setIds(Arrays.asList("4","5","6"));
		mw546.setName("政史地");
		mw546.setStudentCount(56);
		mw546.setFixedClassCount(1);
		mw546.setFixedStudentCount(56);
		mw546.setLayId(1);
		mw546.setLayName("物理层");
		wishDataList.add(mw546);
		
    	for (SubjectGroup wish : wishDataList) {
    		List<Student> students = createStudent(wish.getStudentCount(), Joiner.on(",").skipNulls().join(wish.getIds()), wish.getName());
    		wish.setStudents(students);
		}
    	
    	return wishDataList;
	}

		
	
	
	public static List<Student> createStudent(int number, String wishId, String wishName) {
		List<Student> student678 = new ArrayList<Student>();
		for (int i=0;i<number;i++) {
			student678.add(new Student(wishId+"-"+i, wishName+"-"+i, wishId, wishName));
		}
		return student678;
	}
	
}
