package com.grand.mysql_handler.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(value="myconfig")
public class MyConfig {

	private List<Map<String,String>> shareApiList;
	
}
