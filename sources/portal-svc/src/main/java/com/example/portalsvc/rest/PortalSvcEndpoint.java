package com.example.portalsvc.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.example.portalsvc.util.DBUtil;
import com.example.portalsvc.util.UsersSvc;

@Path("/portalsvc")
public class PortalSvcEndpoint {

	private static Properties props = new Properties();
	private static Logger logger = Logger.getLogger(PortalSvcEndpoint.class.getName());
	private static String ingressSubDomain = "portal-svc-governance.cp-india-q3-f2c6cdc6801be85fd188b09d006f13e3-0000.jp-tok.containers.appdomain.cloud";

	static {
		try {
			ClassLoader classLoader = PortalSvcEndpoint.class.getClassLoader();
			InputStream input = classLoader.getResourceAsStream("verify.config");
			props.load(input);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error loading Security Verify configuration.");
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	@GET
	@Path("/register")
	@Produces({ MediaType.TEXT_HTML })
	public InputStream getRegistrationPage() {
		try {
			return this.getClass().getResourceAsStream("/register.html");
		} catch (Exception e) {
			throw new RuntimeException("Exception returning register.html", e);
		}
	}

	@POST
	@Path("regsvc")
	@Produces({ MediaType.TEXT_HTML })
	public Response registerCustomer(@javax.ws.rs.core.Context javax.servlet.http.HttpServletRequest request) {
		String successHTML = null;
		try {
			JSONObject holder = new JSONObject();
			holder.put("fname", request.getParameter("fname"));
			holder.put("mname", request.getParameter("mname"));
			holder.put("lname", request.getParameter("lname"));
			holder.put("mobile", request.getParameter("mobile"));
			holder.put("address", request.getParameter("address"));
			holder.put("email", request.getParameter("email"));

			DBUtil.insertPolicyHolderData(holder);
			UsersSvc.createUser(holder.getString("email"), holder.getString("lname"), holder.getString("fname"),
					holder.getString("email"), holder.getString("mobile"));
			successHTML = FileUtils.readFileToString(
					new File(PortalSvcEndpoint.class.getClassLoader().getResource("success.html").getFile()),
					Charset.defaultCharset());

		} catch (IOException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
		return Response.ok(successHTML).build();
	}

	@POST
	@Path("paymentsvc")
	@Produces({ MediaType.TEXT_HTML })
	public Response completePayment(@javax.ws.rs.core.Context javax.servlet.http.HttpServletRequest request) {
		String successHTML = null;
		try {
			JSONObject payment = new JSONObject();
			payment.put("policyId", request.getParameter("policyId"));
			payment.put("ccnum", request.getParameter("ccnum"));
			payment.put("expiry", request.getParameter("ccmonth") + "/" + request.getParameter("ccyear"));
			DBUtil.updatePaymentDetails(payment);
			successHTML = FileUtils.readFileToString(
					new File(PortalSvcEndpoint.class.getClassLoader().getResource("paymentsuccess.html").getFile()),
					Charset.defaultCharset());
		} catch (Exception e) {
			e.printStackTrace();
			JSONObject error = new JSONObject();
			error.put("message", "Payment failed!");
			return Response.serverError().build();
		}
		
		return Response.ok(successHTML).build();
	}

	@GET
	@Path("/payment")
	@Produces({ MediaType.TEXT_HTML })
	public Response sendPaymentPage(@javax.ws.rs.core.Context javax.servlet.http.HttpServletRequest request) {
		String paymentHTML = null;
		try {
			String policyId = request.getParameter("id");

			paymentHTML = FileUtils.readFileToString(
					new File(PortalSvcEndpoint.class.getClassLoader().getResource("payment.html").getFile()),
					Charset.defaultCharset());
			JSONObject policyObj = DBUtil.getPolicyDetails(policyId);
			JSONObject custObj = DBUtil.getCustomerDetails(policyObj.getString("custId"));
			paymentHTML = paymentHTML.replace("{{custId}}", policyObj.getString("custId"))
					.replace("{{policy}}", policyObj.getString("item"))
					.replace("{{orderdet}}", policyObj.getString("item"))
					.replace("{{address}}", custObj.getString("address"))
					.replace("{{lname}}", custObj.getString("lname")).replace("{{fname}}", custObj.getString("fname"))
					.replace("{{policyId}}", policyObj.getString("policyId"));
		} catch (IOException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
		return Response.ok(paymentHTML).build();

	}

	@GET
	@Path("/surrpolicy")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response surrenderPolicy(@javax.ws.rs.core.Context javax.servlet.http.HttpServletRequest request) {
		try {
			final String authorizationHeaderValue = request.getHeader("verify-token");
			String token = null;
			token = authorizationHeaderValue;
			boolean isValid = checkTokenIsValid(token);
			if (!isValid) {
				return Response.ok("Authorization Failed").build();
			}
			String policyId = request.getParameter("policyid");
			DBUtil.surrenderPolicy(policyId);
			JSONObject resp = new JSONObject();
			resp.put("message", "Policy surrender successful.");
			return Response.ok(resp.toString()).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/createpolicy")
	@Produces({ MediaType.TEXT_HTML })
	public Response createPolicy(@javax.ws.rs.core.Context javax.servlet.http.HttpServletRequest request) {
		int policyId = -1;
		Long premium = 0L;
		try {
			final String authorizationHeaderValue = request.getHeader("verify-token");
			String token = null;
			token = authorizationHeaderValue;
			boolean isValid = checkTokenIsValid(token);
			if (!isValid) {
				return Response.ok("Authorization Failed").build();
			}

			String emailId = request.getParameter("emailid");
			
			String custId = DBUtil.getCustID(emailId);
			
			String cover = request.getParameter("cover");
			premium = Math.round(Integer.valueOf(cover)*0.1);
			String policyType = request.getParameter("policytype");
			String frequency = request.getParameter("frequency");
			if (frequency.equalsIgnoreCase("monthly"))
				premium = Math.round(Integer.valueOf(cover)*0.1/12);
            System.out.println(emailId +" "+custId+" "+premium.toString()+" "+cover+" "+policyType+ " "+frequency);
            
			JSONObject itemObj = new JSONObject();
			itemObj.put("PolicyType", policyType);
			itemObj.put("Cover", cover);
			itemObj.put("Frequency", frequency);
			itemObj.put("Premium", premium.toString());
			JSONObject orderJson = new JSONObject();
			orderJson.put("custid", custId);
			String pattern = "yyyy-MM-dd";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			String orderdt = simpleDateFormat.format(new Date());

			orderJson.put("orderdt", orderdt);
			orderJson.put("status", "InActive");
			orderJson.put("mode", "Chatbot");

			orderJson.put("item", itemObj.toString());
			orderJson.put("amount", premium.toString());
			// payment details will be updated later
			orderJson.put("ccnumfirst12", " ");
			orderJson.put("ccnumlast4", " ");
			orderJson.put("expiry", " ");

			policyId = DBUtil.insertPoliciesData(orderJson);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JSONObject err = new JSONObject();
			err.put("error", e.getMessage());
			Response.ok(e.getMessage()).build();
		}

		JSONObject resp = new JSONObject();
		resp.put("premium", premium.toString());
		resp.put("paymentLink", "http://" + ingressSubDomain + "/ins/portalsvc/payment?id=" + policyId);
		return Response.ok(resp.toString()).build();
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
