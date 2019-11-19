package com.grand.mysql_handler;

import java.util.HashMap;
import java.util.Map;

import com.grand.mysql_handler.utils.HttpUtils;

public class TestMain {

	public static void main(String[] args) throws Exception{
		String path = "https://emh5.eastmoney.com/html/?color=w&fc=00223602#/gsgk"; // 
		String content = HttpUtils.myGet(path);
		System.out.println(content);
		
		String path1 = "https://emh5.eastmoney.com/api/GongSiGaiKuang/GetJiBenZiLiao";
		Map<String,String> body = new HashMap<>();
		body.put("SecurityCode", "SZ300059");
		body.put("fc", "00223602");
		String content1 = HttpUtils.myPost(path1, body);
		System.out.println(content1);
		
	}

}
