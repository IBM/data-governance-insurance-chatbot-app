package com.example.chatbotsvc.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.example.chatbotsvc.util.DBUtil;

@Path("/chatbotsvc")
public class ChatbotSvcEndpoint {
	
	private static Properties props = new Properties();
	private static Logger logger = Logger.getLogger(ChatbotSvcEndpoint.class.getName());
	
	static {
		try {
			ClassLoader classLoader = ChatbotSvcEndpoint.class.getClassLoader();
			InputStream input = classLoader.getResourceAsStream("verify.config");
			props.load(input);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error loading Security Verify configuration.");
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	
	@GET
	@Path("/getallpolicies")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllPolicies(@javax.ws.rs.core.Context javax.servlet.http.HttpServletRequest request) {
		try {
			final String authorizationHeaderValue = request.getHeader("verify-token");
			String token = null;
			token = authorizationHeaderValue;
			boolean isValid = checkTokenIsValid(token);
			if (!isValid) {
				return Response.ok("Authorization Failed").build();
			}
			String emailId = request.getParameter("emailid");
			System.out.println("Email -"+ emailId);
			JSONObject resp = DBUtil.getAllPolicies(emailId);
			
			return Response.ok(resp.toString()).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/getallactivepolicies")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllActivePolicies(@javax.ws.rs.core.Context javax.servlet.http.HttpServletRequest request) {
		try {
			final String authorizationHeaderValue = request.getHeader("verify-token");
			String token = null;
			token = authorizationHeaderValue;
			boolean isValid = checkTokenIsValid(token);
			if (!isValid) {
				return Response.ok("Authorization Failed").build();
			}
			String emailId = request.getParameter("emailid");
			System.out.println("Email -"+ emailId);
			JSONObject resp = DBUtil.getAllActivePolicies(emailId);
			
			return Response.ok(resp.toString()).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}
	
	
	private boolean checkTokenIsValid(String token) {
		try {
			boolean isValidRequest = true;

			if (token == null) {
				isValidRequest = false;
			}
			if (token != null) {
				HttpPost post = new HttpPost(props.getProperty("introspectionUrl"));
				List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
				urlParameters.add(new BasicNameValuePair("client_id", props.getProperty("clientId")));
				urlParameters.add(new BasicNameValuePair("client_secret", props.getProperty("clientSecret")));
				urlParameters.add(new BasicNameValuePair("token", token));

				post.setEntity(new UrlEncodedFormEntity(urlParameters));
				String result = "";
				try (CloseableHttpClient httpClient = HttpClients.createDefault();
						CloseableHttpResponse res = httpClient.execute(post)) {
					result = EntityUtils.toString(res.getEntity());
					logger.log(Level.INFO, "Token introspection results:" + result);
					JSONObject tokenIntro = new JSONObject(result);
					if (tokenIntro.getBoolean("active") == false) {
						isValidRequest = false;
					}
				}
			}
			return isValidRequest;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
