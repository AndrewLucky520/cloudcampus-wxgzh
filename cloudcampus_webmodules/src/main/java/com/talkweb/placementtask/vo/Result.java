package com.talkweb.placementtask.vo;

/**  
 * 结果集
* @author hushowly@foxmail.com  
*/  
public class Result<T> {
	
	private String code = null;

	private String message = null;
	
	private T data = null;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public void setCode(int code) {
		this.code = String.valueOf(code);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Result [code=" + code + ", message=" + message + ", data=" + data + "]";
	}
	
	
	
	
	
}
