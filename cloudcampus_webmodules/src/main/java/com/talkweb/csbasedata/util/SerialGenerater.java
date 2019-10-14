package com.talkweb.csbasedata.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SerialGenerater {
	
    private static SerialGenerater primaryGenerater = null;
 
    private SerialGenerater() {
    }
 
    /**
     * 取得PrimaryGenerater的单例实现
     *
     * @return
     */
    public static SerialGenerater getInstance() {
        if (primaryGenerater == null) {
            synchronized (SerialGenerater.class) {
                if (primaryGenerater == null) {
                    primaryGenerater = new SerialGenerater();
                }
            }
        }
        return primaryGenerater;
    }
    
    /**
     * 生成下一个编号
     */
    public synchronized String getNextSerial(String sno) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HHmmss");
        String rand = (Math.random() + "").replace("0.", "");
        return sno + formatter.format(date) + rand.substring(0, 4);
    }

}