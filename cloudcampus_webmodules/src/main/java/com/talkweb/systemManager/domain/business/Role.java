package com.talkweb.systemManager.domain.business;

import java.util.List;

public class Role {

	private String roleDm;
	private String roleMC;
	private String roleBz;
	
	private List<Teacher> teachers;

	
	public String getRoleDm() {
		return roleDm;
	}

	public void setRoleDm(String roleDm) {
		this.roleDm = roleDm;
	}

	public String getRoleMC() {
		return roleMC;
	}

	public void setRoleMC(String roleMC) {
		this.roleMC = roleMC;
	}

	public String getRoleBz() {
		return roleBz;
	}

	public void setRoleBz(String roleBz) {
		this.roleBz = roleBz;
	}

	public List<Teacher> getTeachers() {
		return teachers;
	}

	public void setTeachers(List<Teacher> teachers) {
		this.teachers = teachers;
	}
	
	
}
