package com.grand.mysql_handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.grand.mysql_handler.mapper.SystemMapper;
import com.grand.mysql_handler.utils.HttpUtils;

import lombok.extern.log4j.Log4j2;
import net.sf.json.JSONObject;

@Log4j2
@SpringBootTest
@RunWith(SpringRunner.class)
public class SpiderTest {

	private List<JSONObject> keywords = new ArrayList<>();
	private List<JSONObject> websites = new ArrayList<>();
	private List<String> suffixList = Arrays.asList(".shtml",".html",".jsp",".asp",".ph");
	@Resource
	private SystemMapper sysMapper;
	
	@Before
	public void init() {
		String sql = "select url,deep_max deepMax from website_info";
		List<Map<String,Object>> websites = sysMapper.selectBySql(sql);
		for (Map<String, Object> map : websites) {
			this.websites.add(JSONObject.fromObject(map));
		}
		
		sql = "select id,keyword from subscribe_keys";
		List<Map<String,Object>> keywords = sysMapper.selectBySql(sql);
		for (Map<String, Object> map : keywords) {
			this.keywords.add(JSONObject.fromObject(map));
		}
	}
	
	@Test
	public void start() {
		try {
			for (JSONObject website : websites) {
				int deepMax = website.getInt("deepMax");
				digui(Arrays.asList(website.getString("url")), 0, deepMax);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void digui(List<String> urls, int currentDeep, int deepMax) {
		if(currentDeep > deepMax) {
			return;
		}
		List<String> newUrls = new ArrayList<>();
		for (String url : urls) {
			System.out.println(currentDeep+","+url);
			String content = HttpUtils.myGet(url);
			if (StringUtils.isEmpty(content)) {
				continue;
			}
			List<Long> keywordIds = new ArrayList<>();
			for (JSONObject item : keywords) {
				String keyword = item.getString("keyword");
				long id = item.getLong("id");
				if(content.contains(keyword)) {
					keywordIds.add(id);
				}
			}
			if(keywordIds.size()>0) {
				String sql = "insert into subscribe_news(url,keyword_ids) values('{url}','{keyword_ids}')";
				sql = sql.replace("{url}", url);
				sql = sql.replace("{keyword_ids}", keywordIds.toString());
				sysMapper.insertBySql(sql);
			}
			
			Pattern p = Pattern.compile(" href=[\"\'](.*?)[\"\']");
	        Matcher m = p.matcher(content);
	        boolean b = m.find();
	        while(b){
	            String path = m.group(1);
	            for (String suffix : suffixList) {
					if(path.endsWith(suffix)) {
						newUrls.add(path);
					}
				}
	            b = m.find();
	        }
		}
		digui(newUrls, currentDeep+1, deepMax);
	}
	
}
