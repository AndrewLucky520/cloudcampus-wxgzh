package com.talkweb.placementtask.utils.div.dto;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class SubjectGroup {
	
	/**
	 * 组id
	 */
	public String id;
	
	/**
	 * 组名
	 */
	public String name;
	
	/**
	 * 科目
	 */
	private List<Subject> subjects;
	
	
	/**
	 * 科目id集合
	 */
	public List<String> ids;
	
	
	/**
	 * 学生人数
	 */
	private int studentCount;
	

	/**
	 * 学生信息
	 */
	private List<Student> students; 
	
	
	/**
	 * 平均班额
	 */
	private int avgClassSize;

	
	/**
	 * 判断当前科目组是否包含给定科目集合
	 * @param subjects
	 * @return
	 */
	public boolean contains(List<Subject> subjects) {
		
		List<String> tempIds = Lists.transform(subjects, new Function<Subject, String>() {
			public String apply(Subject input) {
				return input.getId();
			}
		});
		
		return ids.containsAll(tempIds);
	}
	
	public SubjectGroup() {
	}
	
	
	public SubjectGroup(List<Subject> subjectlist) {
		
//		Collections.sort(list, new Comparator<Subject>() {
//			public int compare(Subject o1, Subject o2) {
//				return o1.getId()-o2.getId();
//			};
//		});
		
		ids = Lists.transform(subjectlist, new Function<Subject, String>() {
			public String apply(Subject input) {
				return input.getId();
			}
		});
		
		List<String> nameList = Lists.transform(subjectlist, new Function<Subject, String>() {
			public String apply(Subject input) {
				return input.getName();
			}
		});
		
		this.id = Joiner.on("").skipNulls().join(ids);
		this.name = Joiner.on("").skipNulls().join(nameList);
		this.subjects = subjectlist;
	}
	
	public SubjectGroup(List<Subject> subjectlist, List<Student> students) {
		
		ids = Lists.transform(subjectlist, new Function<Subject, String>() {
			public String apply(Subject input) {
				return input.getId();
			}
		});
		
		List<String> nameList = Lists.transform(subjectlist, new Function<Subject, String>() {
			public String apply(Subject input) {
				return input.getName();
			}
		});
		
		this.id = Joiner.on("").skipNulls().join(ids);
		this.name = Joiner.on("").skipNulls().join(nameList);
		this.studentCount = students.size();
		this.students = students;
		for (Student student : this.students) {
			student.setSubjectIds(this.getId());
		}
		this.subjects = subjectlist;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public List<Subject> getSubjects() {
		return subjects;
	}


	public void setSubjects(List<Subject> subjects) {
		this.subjects = subjects;
	}


	public List<String> getIds() {
		return ids;
	}


	public void setIds(List<String> ids) {
		this.ids = ids;
	}


	public int getStudentCount() {
		return studentCount;
	}


	public void setStudentCount(int studentCount) {
		this.studentCount = studentCount;
	}


	public int getAvgClassSize() {
		return avgClassSize;
	}


	public void setAvgClassSize(int avgClassSize) {
		this.avgClassSize = avgClassSize;
	}

	public List<Student> getStudents() {
		return students;
	}

	public void setStudents(List<Student> students) {
		this.students = students;
	}
	
	
}
