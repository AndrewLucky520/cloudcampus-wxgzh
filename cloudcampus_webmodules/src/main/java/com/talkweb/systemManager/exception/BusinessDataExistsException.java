package com.talkweb.systemManager.exception;


/**
 * @ClassName BusinessDataExistsException
 * @author Homer
 * @version 1.0
 * @Description 业务数据已经存在异常
 * @date 2015年3月20日
 */
public class BusinessDataExistsException extends Exception {

	private static final long serialVersionUID = -8669723327536721952L;

	public BusinessDataExistsException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BusinessDataExistsException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public BusinessDataExistsException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public BusinessDataExistsException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public BusinessDataExistsException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
		
}
