package com.talkweb.common.tools.ExcelToolDemo;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.ExcelUtil;

/*
 * 使用步骤:
 * 1.新建一个类,例如StudentVO.
 * 2.设置哪些属性需要导出,哪些需要设置提示.
 * 3.设置实体数据
 * 4.调用exportExcel方法.
 * 本例向您提供以下问题的解决方案:
 * 1.实体对象存放的值需要转换为其他文字的情况,例如:实例中有0,1表示男,女;而导入导出的excel中是中文的"男","女".
 * 2.实体对象的时间类型处理.
 * 
 */
public class ExportTest {
    public static void main(String[] args) {
        /**
         * 示例3 用于合并表头
         */
        JSONArray excelHead = JSON.parseArray("[[{field:'itemid',title:'Item ID',rowspan:3,width:80,sortable:true},{field:'productid',title:'Product ID',rowspan:2,colspan:2,width:80,sortable:true},{title:'Item Details',colspan:6}],"
                + "[{field:'listprice',title:'List Price',colspan:2,width:80,align:'right',sortable:true},{field:'unitcost',colspan:2,title:'Unit Cost',width:80,align:'right',sortable:true},{field:'attr1',title:'Attribute',width:100},{field:'status',title:'Status',width:60}]"
                + ",["
                + "{field:'star3',title:'star3',width:80,align:'right',sortable:true},"
                + "{field:'start3',title:'start3',width:80,align:'right',sortable:true},"
                + "{field:'listprice1',title:'listprice1',width:100},"
                + "{field:'listp2',title:'listp2',width:100},"
                + "{field:'u1',title:'u1',width:100},"
                + "{field:'u2',title:'u2',width:100},"
                + "{field:'a1',title:'a1',width:100},"
                + "{field:'s2',title:'s2',width:100}"
                + "]"
                + "]");
        OutputStream out = null;
        JSONArray data = new JSONArray();
        JSONObject obj = new JSONObject();
        data.add(obj);
        obj.put("itemid", "ssdfsdf");
        obj.put("listprice", ";lldddddssdfsdf");
        obj = new JSONObject();
        data.add(obj);
        obj.put("itemid", "2222ssdfsdf");
        obj.put("listprice", "ll222dddddssdfsdf");
        try {
            out = new FileOutputStream("d:\\success5.xls");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ExcelTool.exportExcelWithData(data , excelHead, null, null, null, null);
        
        
        System.out.println("done!");
    }
    public static void main33(String[] args) {
        // 初始化数据
        List<Student> list = new ArrayList<Student>();
        Student stu = new Student();
        stu.setId(1);
        stu.setName("李坤");
        stu.setSex(0);
        stu.setClazz(5);
        stu.setCompany("天融信");
        stu.setBirthday(new Date());
        list.add(stu);

        Student stu2 = new Student();
        stu2.setId(2);
        stu2.setName("曹贵生");
        stu2.setSex(0);
        stu2.setClazz(5);
        stu2.setCompany("中银");
        list.add(stu2);

        Student stu3 = new Student();
        stu3.setId(3);
        stu3.setName("李学宇");
        stu3.setSex(1);
        stu3.setClazz(6);
        list.add(stu3);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream("d:\\success4.xls");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        ExcelUtil<StudentVO> util = new ExcelUtil<StudentVO>(StudentVO.class);
        util.exportExcel(convertStu2VO(list), "学生信息", 60000, out);
        System.out.println("----执行完毕----------"); 
        /**
         * 示例2 用于动态列的导出
         */
        JSONArray list2 = (JSONArray) JSON.toJSON(list);
        FileOutputStream out2 = null;
        try {
            out2 = new FileOutputStream("d:\\success4.xls");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("toJSON:"+list2);
        String filedNames [] = {"sex","name"};
        String filedNamesCHN [] = {"性别","名称"};
        try {
            ExcelTool.exportExcel(list2, filedNames, filedNamesCHN,"导出信息", out2);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    //将student对象转换为studentVO用于导出.
    private static List<StudentVO> convertStu2VO(List<Student> list) {
        List<StudentVO> list2 = new ArrayList<StudentVO>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d HH:mm:ss");
        for (int i = 0; i < list.size(); i++) {
            StudentVO stuVo = new StudentVO();
            Student stu = list.get(i);
            stuVo.setName(stu.getName());
            switch (stu.getSex()) {
            case 0:
                stuVo.setSex("男");
                break;
            case 1:
                stuVo.setSex("女");
                break;
            default:
                break;
            }
            //处理时间
            if (stu.getBirthday() != null) {
                stuVo.setBirthday(sdf.format(stu.getBirthday()));
            }

            switch (stu.getClazz()) {
            case 5:
                stuVo.setClazz("五期提高班");
                break;
            case 6:
                stuVo.setClazz("六期提高班");
                break;
            case 7:
                stuVo.setClazz("七期提高班");
            default:
                break;
            }
            list2.add(stuVo);
        }

        return list2;
    }

}
