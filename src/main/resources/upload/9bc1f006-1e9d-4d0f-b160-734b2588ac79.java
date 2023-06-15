package com.main.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.main.util.ParameterParser;

/**
 * Handles requests for the application home page.
 */
@Controller
public class popupController {
	
	private static final Logger logger = LoggerFactory.getLogger(loginController.class);
	private final String tmap_appkey = "TfT15eEYgc8gc21L5QJ11adg6xncrqBE5B3ANMs4";  // slsolution 계정 - hanaroTNS 앱
	
	//주소검색
	@RequestMapping("/addrSearch")
	public ModelAndView user_login( HttpServletRequest request, HttpServletResponse response, Model model ) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName( "popup/addr_search" );
		return mav;
	}
	
	
	@RequestMapping(value = "/selectAddrSearch")
	public void selectNoticeDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ParameterParser parser = new ParameterParser(request);
		String addr = parser.getString("addr", "");
		String pageno = parser.getString("pageno", "");
		
		JSONObject jo = null;
		try {
			
			String pyload = apiCall_addressSearch(getUrlEncoding(addr, "UTF-8"), pageno);
			
			HashMap map = new HashMap();
			map.put("code", "Y");
			map.put("message", "success");
			map.put("result", pyload);

			jo = new JSONObject(map);

			

		} catch (Exception e) {
			e.printStackTrace();
			
			HashMap map = new HashMap();
			map.put("code", "N");
			map.put("message", "fail");

			jo = new JSONObject(map);
		}
		
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.resetBuffer();
		response.setContentType("application/json");
		response.getWriter().print(jo);
		response.getWriter().flush();
	}
	
	// 주소검색 요청 메소드
	/**
	 * String searchKeyword :: 검색하기위한 주소
	 * String page :: 요청페이지번호 (기본값 1)
	 * String keywordCnt :: 가져올 주소 갯수(페이지당)
	 * String AppKey :: 사용할 Tmap 앱키
	 */
	public String apiCall_addressSearch(String searchKeyword, String page) {
		StringBuffer response = new StringBuffer();
		URL url = null;
		HttpURLConnection con = null;
		InputStream iStream = null;
		String data = "";
		try {
			
			String appKeyValue = tmap_appkey;
			
			String urlStr = "https://apis.openapi.sk.com/tmap/geo/postcode?appKey=" + appKeyValue; // TODO:: 운영API주소
			if (searchKeyword != null && !"".equals(searchKeyword)) {
				urlStr += ("&format=JSON&coordType=WGS84GEO&addressFlag=F00&addr=" + searchKeyword + "&count=20&page=" + page);
			}
			url = new URL(urlStr);
			con = (HttpURLConnection) url.openConnection();
			con.addRequestProperty("appKey", appKeyValue);
			con.connect(); // 연결처리
	
			int responseCode = con.getResponseCode();
			String responseMessage = con.getResponseMessage();
			if (responseCode != 200) {
				logger.error("apiCall :: RESPONSE FAIL(responseMsg) - " + responseMessage + ", responseCode:" + responseCode);
				data = null;
			} else {
				iStream = con.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(iStream, "utf-8"));
				String line = "";
				while ((line = br.readLine()) != null) {
					response.append(getUrlDecoding(line, "utf-8"));
				}
				data = response.toString();
				iStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			data = null;
		} finally {
			con.disconnect();
		}
		return data;
	}


	private String getUrlEncoding(String str, String enc) {
		String returnValue = "";
		try {
			returnValue = URLEncoder.encode(str, enc);
			//returnValue = returnValue.replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnValue;
	}
	
	public static String getUrlDecoding(String str, String enc) {
		logger.debug("getUrlDecoding :: str::" + str);
		String returnValue = "";
		try {
			// 수정용
			returnValue = str.replace("%20", "+").replace("%2A", "*").replace("%7E", "~").replace("%", "%25");
			// 기존
			//	         returnValue = str.replace("%20", "+").replace("%2A", "*").replace("%7E", "~");
			returnValue = URLDecoder.decode(returnValue, enc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnValue;
	}	
		
}
