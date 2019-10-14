package com.talkweb.common.threadpool;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {

	private final static int initPoolSize = 4;
	private static ExecutorService fixedThreadPool;
	static{
		fixedThreadPool =   Executors.newFixedThreadPool(initPoolSize);
	}
	 
	public static  void RunTask(Runnable task){
		fixedThreadPool.execute(task);
	}
	
	
	
	class Quene<Thread>{
		 private LinkedList<Thread> threadList = new LinkedList<Thread>(); 
		  public void put(Thread v) {  
			  threadList.addFirst(v);  
		  }  
		  public Thread get() {  
		    return threadList.getLast();  
		  }  
		  public void remove(){
			  threadList.removeLast();
		  }
		  public boolean isEmpty() {  
		    return threadList.isEmpty();  
		  }  
	}
}
