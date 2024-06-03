package com.abb.pfg.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Performs an http request from the URL and type of request
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Data
@NoArgsConstructor
public class HttpRequest {
	
	private String url;
	
	public HttpRequest(String url) {
		this.url = url;
	}
	
	/**
	 * Executes a login http request
	 * 
	 * @return String - response body if it has been logged in, null if not
	 */
	public String executeLoginRequest() {
		var httpClient = HttpClients.createDefault();
		var httpPost = new HttpPost(getUrl());
		try {
			var httpResponse = httpClient.execute(httpPost);
			var statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_CREATED) {
				return EntityUtils.toString(httpResponse.getEntity());
			}
			return null;
		} catch(IOException | NullPointerException e) {
			return null;
		}
	}
	
	/**
	 * Sends a get request
	 * 
	 * @param authToken - token used to authenticate the user
	 * @return String - response body
	 */
	public String executeHttpGet(String authToken) {
		var httpClient = HttpClients.createDefault();
		var httpGet = new HttpGet(getUrl());
		if(authToken != null) {
			httpGet.setHeader("Authorization", "Bearer " + authToken );
			httpGet.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		}
		try {
			var httpResponse = httpClient.execute(httpGet);
			var statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_OK) {
				return EntityUtils.toString(httpResponse.getEntity());
			}
			return null;
		} catch (IOException | NullPointerException e) {
			return null;
		}
	}
	
	/**
	 * Sends a post request
	 * 
	 * @param authToken - token used to authenticate the user
	 * @param requestBody - request body needed to post new info
	 * @return boolean - true if response is OK, false if not
	 */
	public boolean executeHttpPost(String authToken, String requestBody) {
		var httpClient = HttpClients.createDefault();
		var httpPost = new HttpPost(getUrl());
		if(authToken != null) {
			httpPost.setHeader("Authorization", "Bearer " + authToken );
			httpPost.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		}
		try {
			if(requestBody != null) {
				httpPost.setEntity(new StringEntity(requestBody, ContentType.create("application/json", StandardCharsets.UTF_8)));
			}
			var httpResponse = httpClient.execute(httpPost);
			var statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_CREATED) {
				return true;
			}
			return false;
		} catch(IOException | NullPointerException e) {
			return false;
		}
	}
	
	/**
	 * 
	 * Sends a put request
	 * 
	 * @param authtoken - token used to authenticate the user
	 * @param bodyRequest - body request needed to update
	 * @return boolean - true if response is OK, false if not
	 */
	public boolean executeHttpPut(String authToken, String bodyRequest) {
		var httpClient = HttpClients.createDefault();
		var httpPut = new HttpPut(getUrl());
		httpPut.setHeader("Authorization", "Bearer " + authToken);
		httpPut.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		try {
			httpPut.setEntity(new StringEntity(bodyRequest, ContentType.create("application/json", StandardCharsets.UTF_8)));
			var httpResponse = httpClient.execute(httpPut);
			var statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_OK) {
				return true;
			}
			return false;
		} catch(IOException | NullPointerException e) {
			return false;
		}
	}
	
	/**
	 * Sends a delete request
	 * 
	 * @param authtoken - token used to authenticate the user
	 * @return boolean - true if response is OK, false if not
	 */
	public boolean executeHttpDelete(String authToken) {
		var httpClient = HttpClients.createDefault();
		var httpDelete = new HttpDelete(getUrl());
		httpDelete.setHeader("Authorization", "Bearer " + authToken);
		httpDelete.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		try {
			var httpResponse = httpClient.execute(httpDelete);
			var statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_NO_CONTENT) {
				return true;
			}
			return false;
		} catch(IOException | NullPointerException e) {
			return false;
		}
	}
}
