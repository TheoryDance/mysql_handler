package com.grand.mysql_handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 使ffmpeg进行截取图片
 * @author TheoryDance
 *
 */
public class VideoCutImage {

	public void test() {
		
		String ffmepgPath = "D:/common/ffmpeg-20191004-e6625ca-win64-static/bin/ffmpeg";
		String videoRealPath = "D:/tmp/v1/34020000002000000200_34020000002000000201-217.ts";
		
		makeScreenCut(ffmepgPath,videoRealPath,"d:/tmp/v2/a1.jpg");
		makeScreenCut(ffmepgPath,videoRealPath,"d:/tmp/v2/a2.jpg");
		makeScreenCut(ffmepgPath,videoRealPath,"d:/tmp/v2/a3.jpg");
		makeScreenCut(ffmepgPath,videoRealPath,"d:/tmp/v2/a4.jpg");
	}
	
	//cmd:
    //c:\ffmpeg -i c:\abc.mp4 e:\sample.jpg -ss 00:00:05  -r 1 -vframes 1  -an -vcodec mjpeg
    public static void makeScreenCut(String ffmepgPath,String videoRealPath,String imageRealName){
        List<String> commend = new ArrayList<String>();
        commend.add(ffmepgPath);
        commend.add("-i");
        commend.add(videoRealPath);
        commend.add("-y");
        commend.add("-f");
        commend.add("image2");
//        commend.add("-ss");
//        commend.add("8");
        commend.add("-t");
        commend.add("0.001");
        commend.add(imageRealName);
 
        try {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(commend);
        builder.redirectErrorStream(true);
        System.out.println("视频截图开始...");
 
        Process process = builder.start();
        InputStream in = process.getInputStream();
        byte[] bytes = new byte[1024];
        System.out.print("正在进行截图，请稍候");
        while (in.read(bytes)!= -1){
            //System.out.println(".");
        }
        System.out.println("视频截取完成...");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("视频截图失败！");
        }
    }
	
}
