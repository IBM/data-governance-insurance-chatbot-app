package com.example.portalsvc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;


public class UsersSvc {
	private static Properties props = new Properties();
	private static Logger logger = Logger.getLogger(UsersSvc.class.getName());

	
	private static String token = "";

	// In this static block, get the token and assign it to the token variable.
	static {
		try {
			ClassLoader classLoader = UsersSvc.class.getClassLoader();
			InputStream input = classLoader.getResourceAsStream("verify.config");
			props.load(input);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error loading Security Verify configuration.");
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		token = getToken();
	}

	private static String getToken() {
		String command = "curl  -d 'grant_type=client_credentials&client_id=" +  props.getProperty("apiClientId") + "&client_secret="
				+ props.getProperty("apiClientSecret")
				+ "&scope=openid' -H 'Content-type: application/x-www-form-urlencoded' -H 'Accept: application/json' "
				+ props.getProperty("tokenUrl");
		System.out.println(command);
		String resp = org.toilelibre.libe.curl.Curl.$(command);
		System.out.println(resp);
		JSONObject jsonResp = new JSONObject(resp);
		return jsonResp.getString("access_token");
	}

	public UsersSvc() {
	}

	private static String readFile(InputStream is) throws IOException {
		StringBuilder resultStringBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = br.readLine()) != null) {
				resultStringBuilder.append(line).append("\n");
			}
		}
		return resultStringBuilder.toString();
	}

	public static int createUser(String username, String familyname, String givenname, String email,
			String mobilenumber) {
		try {
			ClassLoader classLoader = UsersSvc.class.getClassLoader();
			// Getting resource(File) from class loader
			// File usrTemplateFile = new
			// File(classLoader.getResource("config/user.template").getFile());

			InputStream input = classLoader.getResourceAsStream("user.template");

			// String usrTemplateString = readFile(usrTemplateFile);
			String usrTemplateString = readFile(input);
			String usrDetails = usrTemplateString.replace("{{username}}", username)
					.replace("{{familyname}}", familyname)
					.replace("{{givenname}}", givenname).replace("{{email}}", email)
					.replace("{{mobilenumber}}", mobilenumber);
			System.out.println("usrDetails = " + usrDetails);
			URL url2 = new URL(props.getProperty("usersUrl"));
			HttpURLConnection http1 = (HttpURLConnection) url2.openConnection();
			http1.setRequestMethod("POST");
			http1.setDoOutput(true);
			http1.setRequestProperty("Authorization", "Bearer " + token);
			http1.setRequestProperty("Content-Type", "application/scim+json");

			String data = usrDetails;
			OutputStream stream = http1.getOutputStream();
			stream.write(data.getBytes(StandardCharsets.UTF_8));

			System.out.println(http1.getResponseCode() + " " + http1.getResponseMessage());

			if (http1.getResponseCode() == 401) {
				getToken();
				http1.disconnect();
				http1 = (HttpURLConnection) url2.openConnection();
				http1.setRequestMethod("POST");
				http1.setDoOutput(true);
				http1.setRequestProperty("Authorization", "Bearer " + token);
				http1.setRequestProperty("Content-Type", "application/scim+json");

				stream = http1.getOutputStream();
				stream.write(data.getBytes(StandardCharsets.UTF_8));
				System.out.println("Checkpoint1");

				System.out.println(http1.getResponseCode() + " " + http1.getResponseMessage());
				int respCode = http1.getResponseCode();
				http1.disconnect();
				return respCode;
			} else {
				int respCode = http1.getResponseCode();
				http1.disconnect();
				return respCode;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return 401;
		}
	}
	
	
	public static void main(String args[]) {
		createUser("balaji.kadambi", "Kadambi", "Balaji", "balaji.kadambi@gmail.com","9774462267");
	}

}

