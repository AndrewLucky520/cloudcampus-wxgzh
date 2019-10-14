package com.talkweb.timetable.dynamicProgram.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleTable;

public class DynamicProcess {
	/**
	 * 根据样本获取最佳排课结果
	 * @param scheduleTable
	 * @param sessionID 
	 * @param progressMap 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	
	public static ScheduleTable getBestScheduleTable(ScheduleTable scheduleTable, String sessionID, Hashtable<String, JSONObject> progressMap) throws ClassNotFoundException, IOException, InterruptedException {
		
		List<ScheduleTable> tables = new ArrayList<ScheduleTable>();
		ScheduleTable temp = getBestTableSub(scheduleTable, sessionID, progressMap);
		tables.add(temp);
		int tryTimes = 0;
		while (temp.getProgramProgress()!=100&&tryTimes<3){
			tryTimes++;
			ScheduleTable temp1 = getBestTableSub(scheduleTable, sessionID, progressMap);
			tables.add(temp1);
		}
		ScheduleTable best = new ScheduleTable();
		double p = 0;
		for(ScheduleTable tp:tables){
			if(tp.getProgramProgress()>p){
				best = tp;
			}
		}
		
		return best;
	}

	/**
	 * @param scheduleTable
	 * @param sessionID 
	 * @param progressMap 
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	private static ScheduleTable getBestTableSub(ScheduleTable scheduleTable, String sessionID, Hashtable<String, JSONObject> progressMap)
			throws IOException, ClassNotFoundException, InterruptedException {
		List<ScheduleTable> scheduleTableList = new ArrayList<ScheduleTable>();
		ExecutorService executor = Executors.newCachedThreadPool();
		for (int i = 0; i < 30; i++) {
			ScheduleTable schTableClone = (ScheduleTable) scheduleTable
					.deepClone();
			DynamicSubTask myTask = new DynamicSubTask(schTableClone);
			myTask.setTaskNum(i);
			myTask.setProgressMap(progressMap);
			myTask.setSessionID(sessionID);
			executor .execute(myTask);
//			schTableClone.startArrange();
			scheduleTableList.add(schTableClone);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
			Thread.sleep(300);
		}

		double progress = 0;
		double score = 0;
		ScheduleTable best = null;
		for (ScheduleTable scht : scheduleTableList) {
			if (scht.getProgramProgress() > progress) {
				best = scht;
			}
			if (scht.getProgramProgress() >= progress
					&& (scht.getProgramScore() > score)) {
				best = scht;
			}
		}
		System.out.println("获取到最佳：最佳得分"+best.getProgramProgress());
		return best;
	}

}
