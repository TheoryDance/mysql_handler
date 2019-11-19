package com.grand.mysql_handler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.grand.mysql_handler.config.MyConfig;
import com.grand.mysql_handler.mapper.SystemMapper;
import com.grand.mysql_handler.utils.HttpUtils;
import com.grand.mysql_handler.utils.HuaweiStorage;

import lombok.extern.log4j.Log4j2;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Log4j2
@SpringBootTest
@RunWith(SpringRunner.class)
public class MyRestTest {
	
	private HuaweiStorage storage;
	@Resource
	private SystemMapper systemMapper;
	@Resource
	private MyConfig myconfig;
	
	private String toStr(Object obj) {
		if(obj==null) {
			return "''";
		}
		return "'" + obj.toString() + "'";
	}
	
	@Test
	public void pdfToImages() {
		String sql = "select id,`name`,brief,addtime,pdfUrl,sortNo from litemall_family order by id";
		List<Map<String,Object>> pdfList = systemMapper.selectBySql(sql);
		storage = new HuaweiStorage();
		for (Map<String, Object> pdfItem : pdfList) {
			System.out.println(pdfItem);
			int id = Integer.parseInt(pdfItem.get("id").toString());
			String pdfUrl = pdfItem.get("pdfUrl").toString();
			getInsertSQL(id, pdfUrl);
		}
	}
	
	private String getInsertSQL(int id,String pdfUrl) {
		String path = "d:/tmp/pdfImages/" + id + "/";
		File dir = new File(path);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		downloadPdf(id,dir, pdfUrl);
		return null;
	}
	
	private void downloadPdf(int id,File saveDir, String pdfUrl) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(pdfUrl).openConnection();
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(10000);
			conn.connect();
			InputStream input = conn.getInputStream();
			transferToImages(id,saveDir, input);
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void transferToImages(int id,File saveDir, InputStream input) {
		try {
            PDDocument doc = PDDocument.load(input);
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();
            for (int i = 0; i < pageCount; i++) {
                // 方式1,第二个参数是设置缩放比(即像素)，DPI是每英寸的像素点，长度单位，一般WEB为72即可，照片打印用300，在这里使用72放大后比较模糊，选用144比较好
                BufferedImage image = renderer.renderImageWithDPI(i, 144);// 方式2,第二个参数是设置缩放比(即像素)
                // BufferedImage image = renderer.renderImage(i, 2.5f);
                String filename = "pdfbox_image_"+i+".png";
                File outfile = new File(saveDir, filename);
                ImageIO.write(image, "PNG", outfile);
                String picurl = storage.doInvoke(new FileInputStream(outfile), "pdfImages/id_" + id + "/" + filename);
                systemMapper.insertBySql("insert into litemall_family_img(familyId,picUrl) values("+id+",'"+picurl+"')");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@Test
	public void getShareList() {
		List<Map<String,String>> shareApiList = myconfig.getShareApiList();
		for (Map<String, String> shareApi : shareApiList) {
			String url = shareApi.get("url");
			Map<String,String> params = new HashMap<>();
			params.put("key", "96c0de62ae0f11cc39fe5b7de9e33c04");
			params.put("type", "4"); // 4表示80条
			
			int page = 1;
			int resNum = 80;
			while(resNum >= 80) {
				params.put("page", page + "");
				String content = HttpUtils.myGet(url, params);
				if(StringUtils.isNotEmpty(content)) {
					JSONObject json = JSONObject.fromObject(content);
					if(json.getInt("error_code") == 0) {
						JSONArray array = json.getJSONObject("result").getJSONArray("data");
						resNum = json.getJSONObject("result").getInt("num");
						for (Object object : array) {
							JSONObject item = (JSONObject) object;
							String symbol = item.getString("symbol");
							String name = item.getString("name");
							String sql = "insert into share_info(share_code,share_name) values('"+symbol+"','"+name+"')";
							systemMapper.insertBySql(sql);
						}
					}else {
						break;
					}
				}
				page ++;
			}
		}
	}
	
	@Test
	public void createUser() {
		List<Map<String, Object>> list = systemMapper.selectBySql("select id,username,region_code,person_id from sys_user");
		System.out.println(list.size());
		for (Map<String, Object> map : list) {
			String temp = "create (:User{id: #id, username: #username, region_code: #region_code, person_id: #person_id})";
			temp = temp.replace("#id", toStr(map.get("id")));
			temp = temp.replace("#username", toStr(map.get("username")));
			temp = temp.replace("#region_code", toStr(map.get("region_code")));
			temp = temp.replace("#person_id", toStr(map.get("person_id")));
			System.out.println(temp);
		}
	}

	@Test
	public void testPageContentParse() {
		try {
			String path = "http://stock.stcn.com/2019/1012/15426846.shtml";
			String content = HttpUtils.myGet(path);
			List<Map<String, Object>> shareAll = systemMapper.selectBySql("select share_code,share_name from share_info");
			Set<String> shareNames = new HashSet<>();
			for (Map<String, Object> shareItem : shareAll) {
				String shareName = (String) shareItem.get("share_name");
				shareNames.add(shareName);
			}
			
			for (String shareName : shareNames) {
				if(content.contains(shareName)) {
					System.out.println(shareName);
				}
			}
			System.out.println(shareNames.size());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void fillBasicInfo() {
		List<Map<String,Object>> shareAll = systemMapper.selectBySql("select share_code,share_name from share_info where company_code is null");
		// sz02, sh01
		for (Map<String, Object> shareItem : shareAll) {
			try {
				String share_code = (String) shareItem.get("share_code");
				String fc = null;
				if(share_code.toLowerCase().startsWith("sz")) {
					fc = share_code.toLowerCase().replace("sz", "") + "02";
				}else if(share_code.toLowerCase().startsWith("sh")) {
					fc = share_code.toLowerCase().replace("sh", "") + "01";
				}else {
					continue;
				}
				String path = "https://emh5.eastmoney.com/api/GongSiGaiKuang/GetJiBenZiLiao";
				Map<String,String> body = new HashMap<>();
				body.put("SecurityCode", "SZ300059");
				body.put("fc", fc);
				String content = HttpUtils.myPost(path, body);
				JSONObject json = JSONObject.fromObject(content);
				JSONObject basicInfo = json.getJSONObject("Result").getJSONObject("JiBenZiLiao");
				
				Map<String,String> tt = new HashMap<>();
				for(Object key : basicInfo.keySet()) {
					tt.put(key.toString().toLowerCase(), basicInfo.getString(key.toString()));
				}
				
				Map<String,String> tt2 = new HashMap<>();
				List<String> attrs = Arrays.asList("company_code", // "share_code","share_name",
						"company_name","industry","block","chairman","website","registered_address",
						"comp_rofile","main_business","representative","general_manager","secretaries",
						"found_date","registered_capital","employees","managers","phone","email",
						"security_code_type","code_type");
				for (String key : attrs) {
					tt2.put(key, tt.get(key.replace("_", "")));
				}
				tt2.put("share_code", share_code);
				
				String sqlsetStr = "";
				for (String key : attrs) {
					String cc = tt2.get(key);
					cc = cc.replace("'", "\"");
					sqlsetStr += "," + key + "='"+cc+"'";
				}
				
				String sql = "update share_info set " + sqlsetStr.substring(1);
				sql += " where share_code = '"+share_code+"'";
				System.out.println(sql);
				systemMapper.updateBySql(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
}
