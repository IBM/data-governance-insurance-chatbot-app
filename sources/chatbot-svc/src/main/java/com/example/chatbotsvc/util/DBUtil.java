package com.example.chatbotsvc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class DBUtil {

	private static String url = "";
	private static String user = "";
	private static String password = "";
	private static String schema = "";
	private static String mode = "";

	private static Properties props = new Properties();
	private static Logger logger = Logger.getLogger(DBUtil.class.getName());
	private static Connection con = null;
	
	static {
		try {
			ClassLoader classLoader = DBUtil.class.getClassLoader();
			InputStream input = classLoader.getResourceAsStream("db.config");
			props.load(input);
			url = props.getProperty("jdbcurl");
			user = props.getProperty("userid");
			password = props.getProperty("password");
			schema = props.getProperty("schema");
			mode = props.getProperty("mode");

			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			if ("managed".equals(mode))
				con = DriverManager.getConnection(url);
			else
				con = DriverManager.getConnection(url, user, password);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error loading DB configuration.");
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	
	public static JSONObject getAllPolicies(String emailId) {
	

		try {
			// Create the Statement
			Statement stmt = con.createStatement();
			System.out.println("**** Created JDBC Statement object");
			// Execute a query and generate a ResultSet instance
			String stmt1 = "SELECT * FROM " + schema
					+ ".POLICYHOLDER_POLICIES_VIEW WHERE E_MAIL=" + "'" + emailId + "'";
			System.out.println(stmt1);
			ResultSet rs = stmt.executeQuery(stmt1);
			System.out.println("**** Created JDBC ResultSet object");

		    JSONObject allPolicies = new JSONObject();
		    JSONArray policiesArr = new JSONArray();
		    allPolicies.put("policies", policiesArr);
			while (rs.next()) {
				JSONObject policiesObj = new JSONObject();
				policiesObj.put("policyId", rs.getString("POLICY_ID"));
				policiesObj.put("policyDate", rs.getString("POLICY_DT"));
				policiesObj.put("status", rs.getString("STATUS"));
				policiesObj.put("policyDetails", new JSONObject(rs.getString("ITEM")));
				policiesObj.put("purchaseMode", rs.getString("mode"));
				policiesObj.put("creditCard", rs.getString("CCNUM_FIRST12_DIGITS")+rs.getString("CCNUM_LAST4_DIGITS"));
				policiesArr.put(policiesObj);
			}
			return allPolicies;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static JSONObject getAllActivePolicies(String emailId) {
		

		try {
			// Create the Statement
			Statement stmt = con.createStatement();
			System.out.println("**** Created JDBC Statement object");
			// Execute a query and generate a ResultSet instance
			String stmt1 = "SELECT * FROM " + schema
					+ ".POLICYHOLDER_POLICIES_VIEW WHERE STATUS='Active' AND E_MAIL=" + "'" + emailId + "'";
			System.out.println(stmt1);
			ResultSet rs = stmt.executeQuery(stmt1);
			System.out.println("**** Created JDBC ResultSet object");

		    JSONObject allPolicies = new JSONObject();
		    JSONArray policiesArr = new JSONArray();
		    allPolicies.put("policies", policiesArr);
			while (rs.next()) {
				JSONObject policiesObj = new JSONObject();
				policiesObj.put("policyId", rs.getString("POLICY_ID"));
				policiesObj.put("policyDate", rs.getString("POLICY_DT"));
				policiesObj.put("status", rs.getString("STATUS"));
				policiesObj.put("policyDetails", new JSONObject(rs.getString("ITEM")));
				policiesObj.put("purchaseMode", rs.getString("mode"));
				policiesObj.put("creditCard", rs.getString("CCNUM_FIRST12_DIGITS")+rs.getString("CCNUM_LAST4_DIGITS"));
				policiesArr.put(policiesObj);
			}
			return allPolicies;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String args[]) {
       System.out.println(getAllActivePolicies("balaji.kadambi@gmail.com"));
	}

}
