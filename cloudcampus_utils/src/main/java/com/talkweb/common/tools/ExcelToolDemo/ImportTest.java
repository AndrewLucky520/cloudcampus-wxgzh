package com.talkweb.common.tools.ExcelToolDemo;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.talkweb.common.tools.ExcelUtil;

/**
 * @ClassName: ImportTest.java	
 * @version:1.0
 * @Description: excel导入实例
 * @author 武洋 ---智慧校
 * @date 2015年3月3日
 */
public class ImportTest {
    /**
     * 测试示例
     * @param args
     */
    public static void main(String[] args) {
        FileInputStream fis = null;
        try {
            String file = "d:\\success4.xls";
            fis = new FileInputStream(file);
            /**
             * 初始化excel导入工具实例
             */
            ExcelUtil<StudentVO> util = new ExcelUtil<StudentVO>(
                    StudentVO.class);
            /**
             * 根据excel页签获取excel内容
             */
            String fileType = "xls";
            if(file.endsWith("xlsx")){
                fileType = "xlsx";
            }
//            List<StudentVO> list = util.importExcel("学生信息", fis,fileType);
//            System.out.println(list);
            //输出:[StudentVO [birthday=2012/9/22 15:44:10, clazz=五期提高班, company=null, name=李坤, sex=男], StudentVO [birthday=null, clazz=五期提高班, company=null, name=曹贵生, sex=男], StudentVO [birthday=null, clazz=六期提高班, company=null, name=李学宇, sex=女]]
            /**
             * 将vo实体数组转换为逻辑实体数组
             */
//            System.out.println(convertStu2VO(list));
            //输出:[Student [birthday=Sat Sep 22 15:44:10 CST 2012, clazz=5, company=null, id=0, name=李坤, sex=0], Student [birthday=null, clazz=5, company=null, id=0, name=曹贵生, sex=0], Student [birthday=null, clazz=6, company=null, id=0, name=李学宇, sex=1]]
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 需要自己写的方法
     * @param list
     * @return
     */
    private static List<Student> convertStu2VO(List<StudentVO> list) {
        List<Student> retList = new ArrayList<Student>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d HH:mm:ss");
        for (int i = 0; i < list.size(); i++) {
            StudentVO vo = list.get(i);
            Student student = new Student();
            student.setName(vo.getName());

            //在这个方法中还可以控制不允许为空,年龄值不能小于0后大于100等逻辑.
            
            String sex = vo.getSex();
            if (sex.equals("男")) {
                student.setSex(0);
            } else {
                student.setSex(1);
            }
            
            if (vo.getBirthday()!=null && !vo.getBirthday().trim().equals("")) {
                try {
                    student.setBirthday(sdf.parse(vo.getBirthday()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            String clazz = vo.getClazz();
            if (clazz != null) {
                if (clazz.equals("五期提高班")) {
                    student.setClazz(5);
                } else if (clazz.equals("六期提高班")) {
                    student.setClazz(6);
                } else if (clazz.equals("七期提高班")) {
                    student.setClazz(7);
                } else {
                    System.out.println("输入的数据不合法");
                }
            }
            retList.add(student);
        }
        return retList;
    }
}
