package com.talkweb.csbasedata.util;

/**
 * @ClassName: OutputMessage
 * @version:1.0
 * @Description: 平台输出的提示信息枚举
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
public enum ImportOutputMessage {
	
	operationSuccess("0", "操作成功!"),
	formSuccess("1", "文件格式正确,字段无需匹配!"),
	formSuccessMatch("2", "文件格式正确,字段需要匹配!"),
	updateSuccess("3", "修改成功!"),
	delSuccess("4", "删除成功!"),
	examineSuccess("5","校验通过!"),	
	saveSuccess("6","正常启动,保存数据操作成功！"),
	importSuccess("7","导入成功！"),
	
	//业务处理中提示100至200
	readAwait("101","正在读取excel表格数据,请稍后..."),
	packageAwait("102","正在封装excel表头数据,请稍后..."),
	checkAwait("103","正在校验excel数据,请稍后..."),
	changeAwait("104","正在转换excel数据,请稍后..."),
	storeAwait("105","正在保存excel数据,请稍后..."),
	readyImportTask("106","正在准备导入任务!!!"),
	startImportTask("107","正在启动导入任务,请稍后..."),

	//业务失败提示-1至-9999
	queryFail("-1", "查询失败!"),	
	updateFail("-2", "修改失败!"),
	delFail("-3", "删除失败!"),
	saveRedisFail("-4", "正常启动，redis保存变量出错!!!"),
	checkFail("-5","Excel数据校验不通过!"),
	saveDataFail("-6","保存数据失败,请坚持excel或联系管理员！"),
	reImport("-7", "由于长时间未操作，请重新导入"),
	UserInfoFailure("-8","用户身份信息已失效，请重新登陆！"),
	getProgressFail("-9","获取进度信息失败!!!"),
	fileFormFail("-10","文件不是excel格式！"),
	loginFailure("-11","由于长时间未操作，请重新导入!!!"),
	singleUpdateFail("-12","单条修改信息失败!!!"),
    fileDoesntExistError("-2100","文件不存在！"),
    filePattenError("-2101","文件格式错误或未知错误！"),
    fileTransferError("-2102","文件内容错误或不符合规范！"),
    fileContentError("-2103","文件转换异常！"),
     
    //-9000至-9999：系统异常
    unknowSeviceErrorDesc("-9000", "未知错误!");

	
	ImportOutputMessage(String code, String desc) {
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
     * 获取错误描述信息
     */
    public static String getDescByCode(String code) {
        ImportOutputMessage[] values = values();
        for (ImportOutputMessage value : values) {
            if (code != null) {
                if (code.equals(value.getCode())) {
                    return value.getDesc();
                }
            }
        }
        return unknowSeviceErrorDesc.getDesc();
    }

}