package com.talkweb.placementtask.utils.newdzb2;

import java.util.List;

/**
 * 用于
 * @author lenovo
 *
 */
public class SeqTclassCountChangeBean {
	Subject subject;
	Subject otherSubject;
	int isOpt;
	int[] changeNums;
	List<SubjectCombinationSwap> swapList;
	public SeqTclassCountChangeBean(Subject subject, Subject otherSubject,
			int isOpt, int[] changeNums) {
		super();
		this.subject = subject;
		this.otherSubject = otherSubject;
		this.isOpt = isOpt;
		this.changeNums = changeNums;
	}
	
	
}
