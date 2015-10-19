package com.wandou.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class RegisterToServer {

	String urlAddress = "http://116.57.86.70:8080/MyCallCenterServlet/RegisterServlet";
	public String doGet(String username, String password) {
		String getUrl = urlAddress + "?username=" + username + "&password=" + password;
		HttpGet httpGet = new HttpGet(getUrl);
		HttpClient hc = new DefaultHttpClient();		
		try {
			HttpResponse ht = hc.execute(httpGet); 
			HttpEntity he = ht.getEntity(); 
			InputStream is = he.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"GBK"));
			String response = "";
			String readLine = null;
			while ((readLine = br.readLine()) != null) {
				response = response + readLine;
			}
			is.close();
			return response;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return "exception";
		} catch (IOException e) {
			e.printStackTrace();
			return "exception";
		}
	}

	public String doPost(String username, String password) {
		HttpPost httpPost = new HttpPost(urlAddress);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		NameValuePair pair1 = new BasicNameValuePair("username", username);
		NameValuePair pair2 = new BasicNameValuePair("password", password);
		params.add(pair1);
		params.add(pair2);
		HttpEntity he;
		try {
			he = new UrlEncodedFormEntity(params, "gbk");
			httpPost.setEntity(he);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		HttpClient hc = new DefaultHttpClient();
		try {
			HttpResponse ht = hc.execute(httpPost);
			if (ht.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity het = ht.getEntity();
				InputStream is = het.getContent();
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				String response = "";
				String readLine = null;
				while ((readLine = br.readLine()) != null) {
					response = response + readLine;
				}
				is.close();
				return response;
			} else {
				return "error";
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return "exception";
		} catch (IOException e) {
			e.printStackTrace();
			return "exception";
		}

	}
}
