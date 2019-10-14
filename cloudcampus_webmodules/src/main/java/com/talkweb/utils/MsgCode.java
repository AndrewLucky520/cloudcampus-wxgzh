package com.talkweb.utils;

public enum MsgCode {
	SUCCESS(0, "成功"),
	FAILED(1, "操作失败"),
	ERROR_PARAMS(2, "参数异常"),
	NOT_FOUND_RECORD(3, "未找到相关数据"),
	NOT_FOUND(404, "未找到相关的服务"),
	SERVER_ERROR(500, "服务器内部错误");

    private int code;
    private String description;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    MsgCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MsgCode getByCode(int code) {
        for (MsgCode codeEnum : MsgCode.values()) {
            if (codeEnum.getCode() == code) {
                return codeEnum;
            }
        }

        return null;
    }
}
