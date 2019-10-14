package com.talkweb.common.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @ClassName: FileUtil.java
 * @version:1.0
 * @Description: 文件处理工具类
 * @author 武洋 ---智慧校
 * @date 2015年3月3日
 */

public class FileTool {

    /**
     * 复制文件
     * 
     * @param 源文件
     * @param targetFile 目标文件
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static void copyFile(String srcFile, String targetFile) throws IOException {

        FileInputStream reader = new FileInputStream(srcFile);
        FileOutputStream writer = new FileOutputStream(targetFile);
        byte[] buffer = new byte[4096];
        int len;
        try {
            reader = new FileInputStream(srcFile);
            writer = new FileOutputStream(targetFile);

            while ((len = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (writer != null)
                writer.close();
            if (reader != null)
                reader.close();
        }
    }

    // 复制文件
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } finally {
            // 关闭流
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }
}
