package com.example.portalsvc.util;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

public class DBUtil {

	private static String url = "";
	private static String user = "";
	private static String password = "";
	private static String schema = "";

	private static Properties props = new Properties();
	private static Logger logger = Logger.getLogger(DBUtil.class.getName());

	static {
		try {
			ClassLoader classLoader = DBUtil.class.getClassLoader();
			InputStream input = classLoader.getResourceAsStream("db.config");
			props.load(input);
			url = props.getProperty("jdbcurl");
			user = props.getProperty("userid");
			password = props.getProperty("password");
			schema = props.getProperty("schema");

		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error loading DB configuration.");
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void main(String args[]) {
		// setupDB();
//		JSONObject holder = new JSONObject();
//		holder.put("fname","Balaji");
//		holder.put("mname"," ");
//		holder.put("lname", "Kadambi");
//		holder.put("mobile", "9886733215");
//		holder.put("address", "Bangalore Karnataka");
//		holder.put("email", "balaji.kadambi@gmail.com");
//
//		insertPolicyHolderData(holder);
		
		
//		JSONObject itemObj = new JSONObject();
//		itemObj.put("Policy Type", "Vehicle");
//		itemObj.put("Cover", "100000");
//		itemObj.put("Frequency", "Quarterly");
//		JSONObject orderJson = new JSONObject();
//		orderJson.put("custid", "1");
//		String pattern = "yyyy-MM-dd";
//		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//		String orderdt = simpleDateFormat.format(new Date());
//
//		orderJson.put("orderdt", orderdt);
//
//		orderJson.put("status", "InActive");
//		
//		orderJson.put("mode", "Chatbot");
//	
//
//		orderJson.put("item", itemObj.toString());
//		orderJson.put("amount", "2394");
//		// payment details will be updated later
//		orderJson.put("ccnumfirst12", " ");
//		orderJson.put("ccnumlast4", " ");
//		orderJson.put("expiry", " ");
//
//		DBUtil.insertPoliciesData(orderJson);

//		System.out.println(getCustID("balaji.kadambi@gmail.com"));
//		System.out.println(getLastOrderId("1"));
		
//		JSONObject payment = new JSONObject();
//		payment.put("policyId", "2");
//		payment.put("ccnum", "2345-7654-6767-4565");
//		payment.put("expiry", "05" + "/" + "2023");
//		updatePaymentDetails(payment);
		
//		surrenderPolicy("1");
		
//		System.out.println(getCustomerDetails("1").toString());
//		System.out.println(getPolicyDetails("1").toString());
		
	}

	public static JSONObject setupDB() {
		JSONObject respJson = new JSONObject();
		Connection con = null;
		Statement stmt;

		try {
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			con = DriverManager.getConnection(url, user, password);
			// Commit changes manually
			con.setAutoCommit(false);
			System.out.println("**** Created a JDBC connection to the data source");

			// Create the Statement
			stmt = con.createStatement();
			System.out.println("**** Created JDBC Statement object");

			List<String> statements = getStatements("legacy-tables.ddl");
			for (String statement : statements)
				stmt.executeUpdate(statement.replace("{{schema}}", schema));

			con.commit();
		} catch (Exception e) {
			e.printStackTrace();
			respJson.put("error", e.getMessage());
			return respJson;
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		respJson.put("message", "DB setup successful.");
		return respJson;

	}

	public static JSONObject cleanDB() {
		JSONObject respJson = new JSONObject();
		Connection con = null;
		Statement stmt;

		try {
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			con = DriverManager.getConnection(url, user, password);
			// Commit changes manually
			con.setAutoCommit(false);
			System.out.println("**** Created a JDBC connection to the data source");

			// Create the Statement
			stmt = con.createStatement();
			System.out.println("**** Created JDBC Statement object");

			List<String> statements = getStatements("legacy-tables-drop.ddl");
			for (String statement : statements)
				stmt.executeUpdate(statement.replace("{{schema}}", schema));

			con.commit();
		} catch (Exception e) {
			e.printStackTrace();
			respJson.put("error", e.getMessage());
			return respJson;
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		respJson.put("message", "DB tables deleted.");
		return respJson;

	}

	public static JSONObject insertPolicyHolderData(JSONObject cust) {
		JSONObject respJson = new JSONObject();
		Connection con = null;
		Statement stmt;

		try {
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			con = DriverManager.getConnection(url, user, password);
			// Commit changes manually
			con.setAutoCommit(false);
			System.out.println("**** Created a JDBC connection to the data source");

			// Create the Statement
			stmt = con.createStatement();
			System.out.println("**** Created JDBC Statement object");
			List<String> statements = getStatements("customer.dml");

			for (String statement : statements) {
				statement = statement.replace("{{schema}}", schema).replace("{{fname}}", cust.getString("fname"))
						.replace("{{mname}}", cust.getString("mname")).replace("{{lname}}", cust.getString("lname"))
						.replace("{{address}}", cust.getString("address"))
						.replace("{{mobile}}", cust.getString("mobile")).replace("{{email}}", cust.getString("email"));

				// Execute a query and generate a ResultSet instance
				System.out.println(statement);
				stmt.executeUpdate(statement);
			}

			con.commit();
		} catch (Exception e) {
			e.printStackTrace();
			respJson.put("error", e.getMessage());
			return respJson;
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		respJson.put("message", "Customer record successfully added.");
		return respJson;
	}

	public static int insertPoliciesData(JSONObject order) {
		JSONObject respJson = new JSONObject();
		Connection con = null;
		Statement stmt;

		try {
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			con = DriverManager.getConnection(url, user, password);
			// Commit changes manually
			con.setAutoCommit(false);
			System.out.println("**** Created a JDBC connection to the data source");

			// Create the Statement
			stmt = con.createStatement();
			System.out.println("**** Created JDBC Statement object");
			List<String> statements = getStatements("policy.dml");

			for (String statement : statements) {
				statement = statement.replace("{{schema}}", schema)
						.replace("{{custid}}", order.get("custid").toString())
						.replace("{{orderdt}}", order.getString("orderdt"))
						.replace("{{status}}", order.getString("status")).replace("{{mode}}", order.getString("mode"))
						.replace("{{item}}", order.getString("item")).replace("{{amount}}", order.getString("amount"))
						.replace("{{ccnumfirst12}}", order.getString("ccnumfirst12"))
						.replace("{{expiry}}", order.getString("expiry"))
						.replace("{{ccnumlast4}}", order.getString("ccnumlast4"));
				// Execute a query and generate a ResultSet instance
				System.out.println(statement);
				stmt.executeUpdate(statement);
			}

			con.commit();
			con.close();
			return getLastOrderId(order.get("custid").toString());
		} catch (Exception e) {
			e.printStackTrace();
			respJson.put("error", e.getMessage());
			return -1;
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static int updatePaymentDetails(JSONObject payment) {
		JSONObject respJson = new JSONObject();
		Connection con = null;
		Statement stmt;

		try {
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			con = DriverManager.getConnection(url, user, password);
			// Commit changes manually
			con.setAutoCommit(false);
			System.out.println("**** Created a JDBC connection to the data source");

			// Create the Statement
			stmt = con.createStatement();
			System.out.println("**** Created JDBC Statement object");
			String statement = "UPDATE " + schema + ".POLICIES SET CCNUM_FIRST12_DIGITS='"
					+ payment.getString("ccnum").substring(0, 14) + "', CCNUM_LAST4_DIGITS='"
					+ payment.getString("ccnum").substring(14) + "', STATUS='Active', EXPIRY='"+ payment.getString("expiry") + "' WHERE POLICY_ID="
					+ payment.getString("policyId");
			System.out.println(statement);
			int ret = stmt.executeUpdate(statement);

			con.commit();
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			respJson.put("error", e.getMessage());
			return -1;
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static int getLastOrderId(String custId) {
		Connection con = null;
		Statement stmt;

		try {
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			con = DriverManager.getConnection(url, user, password);
			// Commit changes manually
			con.setAutoCommit(false);
			System.out.println("**** Created a JDBC connection to the data source");

			// Create the Statement
			stmt = con.createStatement();
			System.out.println("**** Created JDBC Statement object");
			// Execute a query and generate a ResultSet instance
			String stmt1 = "SELECT MAX(POLICY_ID) FROM " + schema + ".POLICIES WHERE CUST_ID="  + custId;
			System.out.println(stmt1);
			ResultSet rs = stmt.executeQuery(stmt1);
			System.out.println("**** Created JDBC ResultSet object");
			int orderId = -1;
			// Print all of the employee numbers to standard output device
			while (rs.next()) {
				orderId = rs.getInt(1);
			}
			return orderId;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static JSONObject getPolicyDetails(String policyId) {
		Connection con = null;
		Statement stmt;

		try {
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			con = DriverManager.getConnection(url, user, password);
			System.out.println("**** Created a JDBC connection to the data source");

			// Create the Statement
			stmt = con.createStatement();
			System.out.println("**** Created JDBC Statement object");
			// Execute a query and generate a ResultSet instance
			String stmt1 = "SELECT POLICY_ID, POLICY_DT, STATUS, ITEM, MODE, AMOUNT, CUST_ID" + "  FROM " + schema
					+ ".POLICIES WHERE POLICY_ID=" + policyId;
			System.out.println(stmt1);
			ResultSet rs = stmt.executeQuery(stmt1);
			System.out.println("**** Created JDBC ResultSet object");

			JSONObject policyObj = new JSONObject();
			Integer id = -1;
			String policyDt = "";
			String status = "";
			String item = "";
			String mode = "";
			int premium = 0;
			Integer custId = -1;

			while (rs.next()) {
				id = rs.getInt("POLICY_ID");
				policyDt = rs.getString("POLICY_DT");
				status = rs.getString("STATUS");
				item = rs.getString("ITEM");
				mode = rs.getString("MODE");
				premium = rs.getInt("AMOUNT");
				custId = rs.getInt("CUST_ID");
			}

			policyObj.put("policyId", id.toString());
			policyObj.put("policyDt", policyDt);
			policyObj.put("status", status);
			policyObj.put("item", item);
			policyObj.put("mode", mode);
			policyObj.put("premium", premium);
			policyObj.put("custId", custId.toString());

			return policyObj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static JSONObject getCustomerDetails(String custId) {
		Connection con = null;
		Statement stmt;

		try {
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			con = DriverManager.getConnection(url, user, password);
			System.out.println("**** Created a JDBC connection to the data source");

			// Create the Statement
			stmt = con.createStatement();
			System.out.println("**** Created JDBC Statement object");
			// Execute a query and generate a ResultSet instance
			String stmt1 = "SELECT CUST_ID, FIRST_NAME, MIDDLE_NAME, LAST_NAME, ADDRESS, E_MAIL, MOBILE FROM " + schema
					+ ".CUSTOMER WHERE CUST_ID=" + "'" + custId + "'";
			System.out.println(stmt1);
			ResultSet rs = stmt.executeQuery(stmt1);
			System.out.println("**** Created JDBC ResultSet object");

			JSONObject custObj = new JSONObject();
			int id = -1;
			String fname = "";
			String lname = "";
			String address = "";
			String email = "";
			String mobile = "";

			while (rs.next()) {
				id = rs.getInt("CUST_ID");
				fname = rs.getString("FIRST_NAME");
				lname = rs.getString("LAST_NAME");
				address = rs.getString("ADDRESS");
				email = rs.getString("E_MAIL");
				mobile = rs.getString("MOBILE");
			}

			custObj.put("custId", id);
			custObj.put("fname", fname);
			custObj.put("lname", lname);
			custObj.put("address", address);
			custObj.put("email", email);
			custObj.put("mobile", mobile);
			custObj.put("custId", custId);

			return custObj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static List<String> getStatements(String fileName) {
		FileInputStream inputStream = null;
		ArrayList<String> statements = new ArrayList<String>();
		try {
			// Getting ClassLoader obj
			ClassLoader classLoader = DBUtil.class.getClassLoader();
			// Getting resource(File) from class loader
			File configFile = new File(classLoader.getResource(fileName).getFile());
			inputStream = new FileInputStream(configFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			StringBuffer statementBuffer = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				statementBuffer.append(line);
				if (line.contains(";")) {
					String tempStr = statementBuffer.toString();
					tempStr = tempStr.replaceAll(System.getProperty("line.separator"), " ");
					tempStr = tempStr.replaceAll(";", " ");
					statements.add(tempStr);
					statementBuffer = new StringBuffer();
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return statements;
	}

	public static String getCustID(String emailId) {
		Connection con = null;
		Statement stmt;

		try {
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			con = DriverManager.getConnection(url, user, password);
			System.out.println("**** Created a JDBC connection to the data source");

			// Create the Statement
			stmt = con.createStatement();
			System.out.println("**** Created JDBC Statement object");
			// Execute a query and generate a ResultSet instance
			String stmt1 = "SELECT * FROM " + schema + ".POLICYHOLDER WHERE E_MAIL=" + "'" + emailId + "'";
			System.out.println(stmt1);
			ResultSet rs = stmt.executeQuery(stmt1);
			System.out.println("**** Created JDBC ResultSet object");

			// Print all of the employee numbers to standard output device
			while (rs.next()) {
				String custId = rs.getString("CUST_ID");
				System.out.println("Cust id : " + custId);
				return custId;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;

	}

	public static int surrenderPolicy(String orderId) {
		Connection con = null;
		Statement stmt;

		try {
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			con = DriverManager.getConnection(url, user, password);
			// Commit changes manually
			con.setAutoCommit(false);
			System.out.println("**** Created a JDBC connection to the data source");

			// Create the Statement
			stmt = con.createStatement();
			System.out.println("**** Created JDBC Statement object");
			// Execute a query and generate a ResultSet instance
			String stmt1 = "UPDATE " + schema + ".POLICIES SET STATUS=" + "'Surrendered'" + " WHERE POLICY_ID=" + orderId;
			System.out.println(stmt1);
			int rs = stmt.executeUpdate(stmt1);
			System.out.println("**** Created JDBC ResultSet object");
			con.commit();
			return rs;

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
