package com.talkweb.commondata.util;

import com.talkweb.accountcenter.thrift.Account;

/** 
 * @author  作者 E-mail: zhuxiaoyue@talkweb.com.cn
 * @date 创建时间：2016年1月8日 下午5:18:29 
 * @Description
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
public class Util {

	/**
	 * 深拷贝account
	 * @param a
	 * @return
	 */
	public static Account deepCopy(Account a)
	{
		Account b=new Account();
		if(null!=a)
		{
			b.setId(a.getId());
			b.setName(a.getName());
			b.setMobilePhone(a.getMobilePhone());
			b.setGender(a.getGender());
		}
		else
		{
			return null;
		}
		return b;
	}
}
