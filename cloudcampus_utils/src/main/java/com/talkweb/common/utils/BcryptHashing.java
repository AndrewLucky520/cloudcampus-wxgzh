package com.talkweb.common.utils;

import java.security.NoSuchAlgorithmException;

import org.springframework.security.crypto.bcrypt.BCrypt;


public class BcryptHashing {
	
	public static final String pepper_key = "065eb8798b181ff0ea2c5c16aee0ff8b70e04e2ee6bd6e08b49da46924223e39127d5335e466207d42bf2a045c12be5f90e92012a4f05f7fc6d9f3c875f4c95b";
	
	public static void main(String[] args) throws NoSuchAlgorithmException {
		String originalPassword = "438102001020030030";
		String generatedSecuredPasswordHash = BCrypt.hashpw(originalPassword
				+ pepper_key, BCrypt.gensalt(10));
		System.out.println(generatedSecuredPasswordHash);

		boolean matched = BCrypt.checkpw("438102001020030030" + pepper_key,
				"$2a$10$bMrLZTaT70XEUFoE8SXb/uAxtEVWcRC966a68p0.JOpehxESOYESS");
		System.out.println(matched);
	}
	
	public static boolean checkString(String string,String cryptstring){
		return BCrypt.checkpw(string + pepper_key,cryptstring);
	}
	
	public static String encrypt(String string){
		return BCrypt.hashpw(string + pepper_key, BCrypt.gensalt(10));
	}
}
