package com.wandou.verify;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
/**
 *Title:
 *@author 豌豆先生 jitsiang@163.com
 *@date 2015年7月30日
 *@version 
 */
public class DbHandler {
	String url = "jdbc:mysql://localhost/AnychatDatabase";
	String user = "root";
	String psd = "wandou_mysql";
	private Connection conn;

 
	
	public Connection getConn() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, user, psd);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return conn;
	}

	//注册
	public boolean insert(String sql_select,String sql_insert ,String[] args) {
		boolean flag = false  ;
		try {
			conn = getConn();
		//	System.out.println(sql);
			 PreparedStatement ps1 = conn.prepareStatement(sql_select);
			 PreparedStatement ps2 = conn.prepareStatement(sql_insert);
		 
			 ps1.setString(1, args[0]);				 
			System.out.println("ps1:"+String.valueOf(ps1));
			
			for (int i = 0; i < args.length; i++) {
				ps2.setString(i + 1, args[i]);		
			}
			System.out.println("ps2:"+String.valueOf(ps2));
			
			ResultSet rs1 = ps1.executeQuery();		
			if (rs1.next()) {
				flag =  false;
			} else {
				int i = ps2.executeUpdate();
				System.out.println("fuck" + i);
				if (i == 1) {
					flag = true;
				}
			}
		} catch (Exception e) {

		}
		return flag;
	}

	//登录
	public boolean checkUser(String sql, String[] args) {
		boolean flag = false;
		try {
			conn = getConn();
			PreparedStatement ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setString(i + 1, args[i]);
			}
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				flag = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return flag;
	}
}
