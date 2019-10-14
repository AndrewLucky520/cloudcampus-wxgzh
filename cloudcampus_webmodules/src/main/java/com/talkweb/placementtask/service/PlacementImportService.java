package com.talkweb.placementtask.service;

import java.util.List;

import com.talkweb.placementtask.vo.AcademicElectiveCross;
import com.talkweb.placementtask.vo.PlacementInfo;

public interface PlacementImportService {
	
	void importPlacementInfo(String termInfo, String placementId, String schoolId,
			List<PlacementInfo> placementRowsList, List<AcademicElectiveCross> crossRowsList);
}