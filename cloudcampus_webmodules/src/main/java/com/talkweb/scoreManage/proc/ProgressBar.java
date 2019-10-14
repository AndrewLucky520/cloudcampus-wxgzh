package com.talkweb.scoreManage.proc;

import com.alibaba.fastjson.JSONObject;

public class ProgressBar extends JSONObject {

	private static final long serialVersionUID = 2074189969886315989L;

	public ProgressBar() {
		JSONObject data = new JSONObject();
		data.put("msg", "");
		this.put("data", data);
		this.put("code", 1);
	}

	public int getProgress() {
		return this.getJSONObject("data").getInteger("progress");
	}

	public boolean setProgress(int progress) {
		if(progress >= this.getJSONObject("data").getIntValue("progress")) {
			this.getJSONObject("data").put("progress", progress);
			return true;
		}
		return false;
	}

	public int getCode() {
		return this.getIntValue("code");
	}

	public void setCode(int code) {
		this.put("code", code);
	}

	public String getMsg() {
		return this.getJSONObject("data").getString("msg");
	}

	public void setMsg(String msg) {
		this.getJSONObject("data").put("msg", msg);
	}

	public void setProgressInfo(int code, int progress, String msg) {
		if(this.setProgress(progress)) {
			this.setCode(code);
			this.setMsg(msg);
		}
	}
	
	public void setProgressInfo(int code, String msg) {
		this.setCode(code);
		this.setMsg(msg);
	}

	public void setOtherData(String key, Object value) {
		this.getJSONObject("data").put(key, value);
	}

	public Object getOtherData(String key) {
		return this.getJSONObject("data").get(key);
	}
}
