package com.talkweb.systemManager.action;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ThreeDES {

private static final String Algorithm = "DESede"; //定义 加密算法,可用 DES,DESede,Blowfish
    
    //keybyte为加密密钥，长度为12字节
    //src为被加密的数据缓冲区（源）
    public static byte[] encryptMode(String key, byte[] src) {
       try {
            //生成密钥
            SecretKey deskey = new SecretKeySpec(build3DesKey(key), Algorithm);
            //加密
            Cipher c1 = Cipher.getInstance(Algorithm);
            c1.init(Cipher.ENCRYPT_MODE, deskey);
            return c1.doFinal(src);
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (java.lang.Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    //keybyte为加密密钥，长度为24字节
    //src为加密后的缓冲区
    public static byte[] decryptMode(String keybyte, byte[] src) {      
    try {
            //生成密钥
            SecretKey deskey = new SecretKeySpec(build3DesKey(keybyte), Algorithm);

            //解密
            Cipher c1 = Cipher.getInstance(Algorithm);
            c1.init(Cipher.DECRYPT_MODE, deskey);
            return c1.doFinal(src);
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (java.lang.Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    //转换成十六进制字符串
    public static String byte2hex(byte[] b) {
        String hs="";
        String stmp="";

        for (int n=0;n<b.length;n++) {
            stmp=(java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length()==1) hs=hs+"0"+stmp;
            else hs=hs+stmp;
            if (n<b.length-1)  hs=hs+":";
        }
        return hs.toUpperCase();
    }
    
    public static void main(String[] args)
    {
        //添加新安全算法,如果用JCE就要把它添加进去

        final byte[] keyBytes = "FD363AFD71CE48E56B865D0B".getBytes();
//        "FD 36 3A FD 71 CE 48 E5 6B 86 5D 0B";
        String szSrc = "{ accountName:19310000299, timestamp:1458628905314,  nonce:2343243434323423 }";
        
        
        final byte[] descBytes = "DF5C58DDBF076A92C1253721".getBytes();
        System.out.println("加密前的字符串:" + szSrc);
        
    }
    private static byte[] build3DesKey(String keyStr)
            throws UnsupportedEncodingException {
        byte[] key = new byte[24];
        byte[] temp = keyStr.getBytes("UTF-8");
        if (key.length > temp.length) {
            System.arraycopy(temp, 0, key, 0, temp.length);
        } else {
            System.arraycopy(temp, 0, key, 0, key.length);
        }
        return key;
    }
}