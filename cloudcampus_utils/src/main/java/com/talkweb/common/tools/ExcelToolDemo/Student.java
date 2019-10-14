package com.talkweb.common.tools.ExcelToolDemo;


import java.util.Date;
/**
 * @ClassName: Student.java	
 * @version:1.0
 * @Description: excel导入导出示例 演示类
 * @author 武洋 ---智慧校
 * @date 2015年3月3日
 */
public class Student {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getClazz() {
        return clazz;
    }

    public void setClazz(int clazz) {
        this.clazz = clazz;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;

    private String name;

    private int sex;

    private int clazz;

    private Date birthday;

    private String company;

    public int getId() {
        return id;
    }

   //get和set方法(略).....

    @Override
    public String toString() {
        return "Student [birthday=" + birthday + ", clazz=" + clazz
                + ", company=" + company + ", id=" + id + ", name=" + name
                + ", sex=" + sex + "]";
    }

}
