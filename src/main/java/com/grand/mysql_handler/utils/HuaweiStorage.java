package com.grand.mysql_handler.utils;

import java.io.IOException;
import java.io.InputStream;

import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.obs.services.exception.ObsException;

/**
 * 目前可以测试通上传，并没有集成到项目里面，待处理。。。。
 */
public class HuaweiStorage {
	
    private String endpoint = "obs.cn-south-1.myhuaweicloud.com";
    private String ak = "G4FQAY26AELZIDSNPEHR";
    private String sk = "gX50V1pjW0y9qEmBXkSpsa9bi0mxI4VGlXiYoTtP";
    private ObsClient obsClient;
    private String bucketName = "myhh";
    
    public String doInvoke(InputStream inputStream, String fileName) {
    	String key = fileName;
        store(inputStream, key);
        String url = generateUrl(key);
        return url;
    }
    
    private ObsClient getOBSClient() {
    	try {
			ObsConfiguration config = new ObsConfiguration();
			config.setSocketTimeout(30000);
			config.setConnectionTimeout(10000);
			config.setEndPoint(endpoint);
			obsClient = new ObsClient(ak, sk, config);
		} catch (Exception e) {
			e.printStackTrace();
			obsClient = null;
		}
        return obsClient;
    }
    
    private String getBaseUrl() {
        return "https://" + endpoint + "/" + bucketName + "/";
    }
    
	public void store(InputStream inputStream, String keyName) {
        try {
        	obsClient = getOBSClient();
            // 对象键（Key）是对象在存储桶中的唯一标识。
            obsClient.putObject(bucketName, keyName, inputStream);
        } catch (ObsException e) {
            System.out.println("Response Code: " + e.getResponseCode());
            System.out.println("Error Message: " + e.getErrorMessage());
            System.out.println("Error Code:       " + e.getErrorCode());
            System.out.println("Request ID:      " + e.getErrorRequestId());
            System.out.println("Host ID:           " + e.getErrorHostId());
        } finally {
            if (obsClient != null) {
                try {obsClient.close();}catch (IOException e){}
            }
        }
	}

	public String generateUrl(String keyName) {
		return getBaseUrl() + keyName;
	}
}
