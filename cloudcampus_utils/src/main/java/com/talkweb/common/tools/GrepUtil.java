package com.talkweb.common.tools;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName GrepUtil
 * @author wy
 * @Desc 过滤工具类
 * @date 2015年4月21日
 */
public class GrepUtil {

	/**
	 * @param keyName
	 *            过滤键名
	 * @param keyValue
	 *            过滤键值
	 * @param sorList
	 *            源数据List
	 * @return
	 */
	public static List<JSONObject> grepJsonKeyByVal(String[] keyName,
			String[] keyValue, List<JSONObject> sorList) {

		if (keyName.length < keyValue.length) {
			return sorList;
		}
		List<JSONObject> rs = new ArrayList<JSONObject>();
		for (JSONObject o : sorList) {
			boolean grs = true;
			for (int i = 0; i < keyName.length; i++) {
				if (o.containsKey(keyName[i])&&o.getString(keyName[i])!=null) {
					String oVal = o.getString(keyName[i]);
					if (!oVal.equalsIgnoreCase(keyValue[i])) {
						grs = false;
						break;
					}
				} else {
					grs = false;
					break;
				}
			}
			if (grs) {
				rs.add(o);
			}
		}

		return rs;

	}

	/**
	 * 根据单个参数过滤数组
	 * @param keyName
	 * @param keyValue
	 * @param sorList
	 * @return
	 */
	public static List<JSONObject> grepJsonKeyBySingleVal(String keyName,
			String keyValue, List<JSONObject> sorList) {
		// TODO Auto-generated method stub
		List<JSONObject> rs = new ArrayList<JSONObject>();
		for (JSONObject o : sorList) {
			boolean grs = true;
			if (o.containsKey(keyName)&&o.getString(keyName)!=null) {
				String oVal = o.getString(keyName);
				if (!oVal.equalsIgnoreCase(keyValue)) {
					grs = false;
				}
			} else {
				grs = false;
			}
			if (grs) {
				rs.add(o);
			}
		}

		return rs;
	}
	/**
	 * 根据单个参数过滤数组
	 * @param keyName
	 * @param keyValue
	 * @param sorList
	 * @return
	 */
	public static List<JSONObject> grepJsonKeyBySingleNum(String keyName,
			int numMax,int numMin, List<JSONObject> sorList) {
		// TODO Auto-generated method stub
		List<JSONObject> rs = new ArrayList<JSONObject>();
		for (JSONObject o : sorList) {
			boolean grs = true;
			if (o.containsKey(keyName)&&o.getString(keyName)!=null) {
				int oVal = o.getIntValue(keyName);
				if (oVal>numMax||oVal<numMin) {
					grs = false;
				}
			} else {
				grs = false;
			}
			if (grs) {
				rs.add(o);
			}
		}
		
		return rs;
	}
}
