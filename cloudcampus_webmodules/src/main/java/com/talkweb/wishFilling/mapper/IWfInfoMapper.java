package com.talkweb.wishFilling.mapper;

import com.talkweb.utils.SqlMapper;
import com.talkweb.wishFilling.vo.WfInfoVo;

@SqlMapper
public interface IWfInfoMapper {
	
	WfInfoVo selectById(String id);
	
}
