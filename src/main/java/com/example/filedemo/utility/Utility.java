package com.example.filedemo.utility;

import java.io.File;

public class Utility {

	public static boolean createDirectoryIfNotExist(String path) {
		if (path != null) {
			File file = new File(path);
			return file.mkdir();
		}
		return false;
	}
	public static String directoryPath(String dirName) {
		return System.getProperty("user.home")+File.separator+dirName;
	}
}
