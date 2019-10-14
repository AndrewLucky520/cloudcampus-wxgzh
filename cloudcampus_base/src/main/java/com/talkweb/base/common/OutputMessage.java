/**
 * 
 */
package com.talkweb.base.common;

/**
 * @ClassName: OutputMessage
 * @version:1.0
 * @Description: 平台输出的提示信息枚举
 * @author 廖刚 ---智慧校
 * @date 2015年3月4日
 * 
 * 编码规则：
 * 0000至9999：成功提示信息
 * -0001至-0999：HTTP传输请求异常
 * -1000至-1999：数据层异常
 * -2000至-2999：服务层异常
 * -3000至-8999：待定
 * -9000至-9999：系统异常
 */
public enum OutputMessage {
	
	//0000至9999：成功提示信息
	success("0", "请求成功!"),
	querySuccess("1", "查询成功!"),
	addSuccess("2", "增加成功!"),
	updateSuccess("3", "修改成功!"),
	delSuccess("4", "删除成功!"),
	
	
	//-1000至-1999：数据层异常
    databaseError("-1001", "数据库操作失败！"),
    queryDataError("-1002", "数据查询失败！"),
    addDataError("-1003", "新增失败！"),
    updateDataError("-1004", "修改失败！"),
    delDataError("-1005", "删除失败！"),
    
    //-2000至-2999：服务层异常
    
    fileDoesntExistError("-2100","文件不存在！"),
    filePattenError("-2101","文件格式错误！"),
    fileContentError("-2102","文件内容错误或不符合规范！"),
    //-3000至-8999：待定
   
    
    //-9000至-9999：系统异常
    unknowSeviceErrorDesc("-9000", "未知错误!");

	
	OutputMessage(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private String desc;	//信息描述
    private String code;	//信息代码

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /*
     * 获取错误描述代码
     */
    public static String getCodeByDesc(String desc) {
    	OutputMessage[] values = values();
        for (OutputMessage value : values) {
            if (desc != null) {
                if (desc.contains(value.getDesc())) {
                    return value.getCode();
                }
            }
        }

        return unknowSeviceErrorDesc.getCode();
    }   
       
    /*
     * 获取错误描述信息
     */
    public static String getDescByCode(String code) {
        OutputMessage[] values = values();
        for (OutputMessage value : values) {
            if (code != null) {
                if (code.equals(value.getCode())) {
                    return value.getDesc();
                }
            }
        }
        return unknowSeviceErrorDesc.getDesc();
    }
    
    //测试代码
    public static void main(String[] agrs){
    	System.out.println(getDescByCode("-9000"));
    	System.out.println(getCodeByDesc("未知错误!"));
    }

}
