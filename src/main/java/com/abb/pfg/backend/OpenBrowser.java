package com.abb.pfg.backend;
import java.io.IOException;

import lombok.Data;

/**
 * Class which represents a java object which is able to open the default web browser from a URL.
 * 
 * @author Adrián Barco Barona
 * @version 1.0
 *
 */
@Data
public class OpenBrowser {

	private String url;
	
	/**
	 * Default constructor
	 * 
	 * @param url - url to establish a http or https connection with
	 */
	public OpenBrowser(String url) {
		this.url = url;
	}
	
	/**
	 * Opens the default web browser from a URL in a Micsosoft Windows device
	 * 
	 * @param url - url to establish a http or https connection with
	 * @throws IOException
	 */
	public void openWindowsDefaultBrowser(String url) throws IOException{
	        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
	}
	
	/**
	 * Opens the default web browser from a URL in a Linux device
	 * 
	 * @param url - url to establish a http or https connection with
	 * @throws IOException
	 */
	public void openLinuxDefaultBrowser(String url) throws IOException{
        Runtime.getRuntime().exec("xdg-open " + url);
    }
	
	/**
	 * Opens the default web browser from a URL in a MAC OS device
	 * 
	 * @param url - url to establish a http or https connection with
	 * @throws IOException
	 */
    public void openMacOSDefaultBrowser(String url) throws IOException{
        Runtime.getRuntime().exec("open " + url);
    }
	
    /**
	 * Opens the default web browser from a URL, depending on the OS of the device
	 * 
	 * @param url - url to establish a http or https connection with
	 * @throws IOException
	 */
	public void openDefaultBrowser(String url) throws IOException{
        String osName = System.getProperty("os.name");
        if(osName.contains("Windows")) {
        	openWindowsDefaultBrowser(url);
        } else if(osName.contains("Linux")) {
        	openLinuxDefaultBrowser(url);
        } else if(osName.contains("Mac OS X")) {
        	openMacOSDefaultBrowser(url);
        }
    }
}
