package com.fuzz.android.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.helpers.DefaultHandler;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.util.Xml;

public class HttpRequest {

	private int timeout = 30000;
	private int readtimeout = 20000;
	URL u;
	OutputStreamWriter wr;
	public HttpURLConnection conn;
	String n;
	
	private HashMap<String,HashMap<String,Object>> mFiles = new HashMap<String,HashMap<String,Object>>();
	private String USER_AGENT = "Mobile/Safari";
	private String CONTENT_TYPE = "application/x-www-form-urlencoded";
	private String METHOD = "POST";
	
	
	private String lineEnd = "\r\n";  
	private String twoHyphens = "--";  
	private String boundary = "*****";  
	
	public HttpRequest(String url){
		HttpURLConnection.setFollowRedirects(false);
		try {
			u= new URL(url);
			conn = (HttpURLConnection) u.openConnection();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public HttpRequest(URL url) throws IOException{
		HttpURLConnection.setFollowRedirects(false);
		
		u= url;
		
		
		conn = (HttpURLConnection) u.openConnection();
		
	}
	
	public void setMethodType(String method){
		METHOD = method;
	}
	
	public void setUserAgent(String agent){
		USER_AGENT = agent;
	}
	
	public void setContentType(String type){
		CONTENT_TYPE = type;
	}
	
	public void Setup() throws IOException{
		if(mFiles.size() > 0){
			SetupFiles();
			return;
		}
		
		conn.setRequestMethod(METHOD);
		
		if(CONTENT_TYPE != ""){
			conn.setRequestProperty("Content-Type",CONTENT_TYPE);
			if(n != null){
				conn.setRequestProperty("Content-Length", "" + Integer.toString(n.toString().getBytes().length));
			}else{
				conn.setRequestProperty("Content-Length",0 + "");
			}
			conn.setRequestProperty("Content-Language", "en-US");
		}
	
	    if(USER_AGENT != "")
	    	conn.setRequestProperty("User-Agent", USER_AGENT);
	    conn.setUseCaches(false);
	    conn.setDoOutput(true);
	    conn.setDoInput(true);
	}
	
	public void SetupFiles() throws IOException{
		conn.setRequestMethod(METHOD);
		conn.setRequestProperty("Content-Type","multipart/form-data;boundary=" + boundary);
		conn.setRequestProperty("Content-Language", "en-US");
		
	
	    if(USER_AGENT != "")
	    	conn.setRequestProperty("User-Agent", USER_AGENT);
	    conn.setUseCaches(false);
	    conn.setDoOutput(true);
	    conn.setDoInput(true);
	}
	
	public String getResponse() throws IOException{
		InputStream is = conn.getInputStream();
		if(is == null){
		}
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuffer response = new StringBuffer(); 
		while((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\n');
		}
		rd.close();
		conn.disconnect();
		return response.toString();
	}
	
	public String getErrorResponse() throws IOException{
		InputStream is = conn.getErrorStream();
		if(is == null){
		}
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuffer response = new StringBuffer(); 
		while((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\n');
		}
		rd.close();
		conn.disconnect();
		return response.toString();
	}
	
	public void SendRequest() throws IOException{ 
		if(n!=null){
			
			if(mFiles.size() > 0){
				writeFiles(new DataOutputStream(conn.getOutputStream()));
			}else{
				wr = new OutputStreamWriter(conn.getOutputStream());
				if(wr == null){
				}
				DEBUGREQUEST();
				wr.write(n);
				
				wr.flush();
				wr.close();
			}
			
		}
	}
	
	public void writeFiles(DataOutputStream dos)throws IOException{
		for(String key : mFiles.keySet()){
			HashMap<String,Object> tmpMap = mFiles.get(key);
			String imagePath = (String) tmpMap.get("FILENAME");
			
			
			dos.writeBytes(twoHyphens + boundary + lineEnd);  
			dos.writeBytes("Content-Disposition: form-data; name=\""+ key +"\";" + " filename=\"" + imagePath + "\"" + lineEnd);  
			dos.writeBytes(lineEnd);  
			
			Bitmap tmpBitmap = (Bitmap)tmpMap.get("FILE");
			tmpBitmap.compress(CompressFormat.JPEG, 95, dos);
			
			
			dos.writeBytes(lineEnd);  
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
		}
		
		String temp[] = n.split("&");
		for(int i=0; i<temp.length; i++){
			String key = temp[i].split("=")[0];
			String value = temp[i].split("=")[1];
			
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\""+ key +"\"" + lineEnd + lineEnd + value + lineEnd);
		}
		
		dos.flush();  
		dos.close();  
	}
	
	public Document AutoXML(){
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Setup();
			SendRequest();
			if(conn.getResponseCode() == 200){
				return db.parse(conn.getInputStream());
			}else{
				Log.v("HTTP REQUEST ERROR",getErrorResponse());
				return null;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Document AutoXMLCustom(){
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setCoalescing(true);
			dbf.setExpandEntityReferences(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			SendRequest();
			if(conn.getResponseCode() == 200){
				return db.parse(conn.getInputStream());
			}else{
				Log.v("HTTP REQUEST ERROR",getResponse());
				Log.v("HTTP REQUEST ERROR",getErrorResponse());
				return null;
			}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String AutoPlain(){
		String ret = "FAILED";
		
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			Setup();
			DEBUGSETUP();
			SendRequest();
			ret = getResponse();
		} catch (Throwable e) {
			try{
				ret = getErrorResponse();
			}catch(Throwable t){
			}
		}
		DEBUGCONNECTION();
		
		return ret;
	}
	
	public String AutoPlainCustom(){
		String ret = "FAILED";
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			SendRequest();
			ret = getResponse();
		} catch (Throwable e) {
			try{
				ret = getErrorResponse();
			}catch(Throwable t){
			}
		}
		return ret;
	}
	
	public int StatusCode() throws IOException{
		return conn.getResponseCode();
	}
	
	public void close(){
		conn.disconnect();
	}

	public void setPostBody(String post) {
		// TODO Auto-generated method stub
		n = post;
	}
	
	public void setPostMap(Hashtable<Object,Object> m){
		n = m.toString().replace(", ", "&").replace("{", "").replace("}", "");
	}
	
	public void setPostArray(String key, ArrayList<Object> arr){
		n = key + "=" + arr.toString();
	}

	public Document AutoXMLNoWrite() {
		// TODO Auto-generated method stub
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setCoalescing(true);
			dbf.setIgnoringElementContentWhitespace(true);
			dbf.setIgnoringComments(true);
			
			dbf.setExpandEntityReferences(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			if(conn.getResponseCode() == 200){
				return db.parse(conn.getInputStream());
			}else{
				Log.v("HTTP REQUEST ERROR",getResponse());
				Log.v("HTTP REQUEST ERROR",getErrorResponse());
				return null;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void AutoXMLSAXNoWrite(DefaultHandler handler){
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
        try {
            if(conn.getResponseCode() == 200){
            	Xml.parse(conn.getInputStream(), Xml.Encoding.UTF_8, handler);
            	 //parser.parse(conn.getInputStream(), handler);
			}else{
				Log.v("HTTP REQUEST ERROR",getErrorResponse());
			}
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } 
	}

	public void AutoXMLSAX(DefaultHandler handler){
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
        try {
        	Setup();
			SendRequest();
            if(conn.getResponseCode() == 200){
            	Xml.parse(conn.getInputStream(), Xml.Encoding.UTF_8, handler);
            	 //parser.parse(conn.getInputStream(), handler);
			}else{
				Log.v("HTTP REQUEST ERROR",getErrorResponse());
			}
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } 
	}
	
	public String AutoPlainNoWrite() {
		String ret = "FAILED";
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			DEBUGSETUP();
			ret = getResponse();
		} catch (Throwable e) {
			try{
				ret = getErrorResponse();
			}catch(Throwable t){
			}
		}
		
		DEBUGCONNECTION();
		return ret;
	}
	
	public static String intToIp(int i) {
    	return ( i & 0xFF) + "." +
    	((i >>  8 ) & 0xFF) + "." +
    	((i >> 16 ) & 0xFF) + "." +
        ((i >> 24 ) & 0xFF);
    }
	
	public static String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Log.e("SOCKETEXCEPTION", ex.toString());
	    }
	    return null;
	}
	
	public JSONArray AutoJSONArrayNoWrite(){
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			if(conn.getResponseCode() == 200){
				return new JSONArray(getResponse());
			}else{
				return new JSONArray(getResponse());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONArray AutoJSONArray(){
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			Setup();
			DEBUGSETUP();
			SendRequest();
			if(conn.getResponseCode() == 200){
				DEBUGCONNECTION();
				return new JSONArray(getResponse());
			}else{
				DEBUGCONNECTION();
				Log.v("HTTP REQUEST ERROR",getErrorResponse());
				return new JSONArray(getResponse());
			}
		} catch (Throwable e) {
			e.printStackTrace();	
		}
		return null;
	}
	
	public JSONArray AutoJSONArrayCustom(){
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			SendRequest();
			if(conn.getResponseCode() == 200){
				return new JSONArray(getResponse());
			}else{
				Log.v("HTTP REQUEST ERROR",getErrorResponse());
				return new JSONArray(getResponse());
			}	
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject AutoJSONNoWrite(){
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			if(conn.getResponseCode() == 200){
				return new JSONObject(getResponse());
			}else{
				return new JSONObject(getResponse());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject AutoJSON(){
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			Setup();
			DEBUGSETUP();
			SendRequest();
			if(conn.getResponseCode() == 200){
				DEBUGCONNECTION();
				return new JSONObject(getResponse());
			}else{
				DEBUGCONNECTION();
				Log.v("HTTP REQUEST ERROR",getErrorResponse());
				return new JSONObject(getResponse());
			}
		} catch (Throwable e) {
			e.printStackTrace();	
		}
		return null;
	}
	
	public JSONObject AutoJSONError(){
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			Setup();
			DEBUGSETUP();
			SendRequest();
			if(conn.getResponseCode() == 200){
				DEBUGCONNECTION();
				return new JSONObject(getResponse());
			}else{
				DEBUGCONNECTION();
				return new JSONObject(getErrorResponse());
			}
		} catch (Throwable e) {
			e.printStackTrace();	
		}
		return null;
	}
	
	public JsonParser AutoStreamJSONError(){
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			Setup();
			DEBUGSETUP();
			SendRequest();
			if(conn.getResponseCode() == 200){
				DEBUGCONNECTION();
				JsonFactory f = new JsonFactory();
				return f.createJsonParser(conn.getInputStream());
			}else{
				DEBUGCONNECTION();
				JsonFactory f = new JsonFactory();
				return f.createJsonParser(conn.getErrorStream());
			}
		} catch (Throwable e) {
			e.printStackTrace();	
		}
		return null;
	}
	
	public JSONObject AutoJSONCustom(){
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
		try {
			SendRequest();
			if(conn.getResponseCode() == 200){
				return new JSONObject(getResponse());
			}else{
				Log.v("HTTP REQUEST ERROR",getErrorResponse());
				return new JSONObject(getResponse());
			}	
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setTimeOut(int i) {
		// TODO Auto-generated method stub
		timeout = i;
	}
	
	public void setReadTimeOut(int i) {
		// TODO Auto-generated method stub
		readtimeout = i;
	}

	public void add(String key, String value) {
		// TODO Auto-generated method stub
		if(n==null){
			n = "";
		}else{
			n += "&";
		}
		
		String val = "";
		int lPtr = 0;
		for(lPtr = 0; lPtr<value.length(); ){
			int numtojump = (lPtr+1000 < value.length()) ? 1000 : value.length()-(lPtr > 0 ? lPtr : lPtr);
			
			val += URLEncoder.encode(value.substring(lPtr,lPtr + numtojump)).replace("%2B", "+");
			lPtr += numtojump;
		}
		
		
		
		n += key + "=" + val;
	}

	private void DEBUGSETUP(){
//		Log.v("RequestMethod",conn.getRequestMethod());
//		
//		Map<String, List<String>> headers = conn.getRequestProperties();
//		for(String s : headers.keySet()){
//			List<String> header = headers.get(s);
//			for(int i=0; i<header.size(); i++){
//				Log.v("RequestProperties","Header: " + s + " Field: " + header.get(i));
//			}
//		}
//		
//		Log.v("ipAddress",HttpRequest.getLocalIpAddress());
	}
	
	private void DEBUGREQUEST(){
//		Log.v("Request",n);
	}
	
	private void DEBUGCONNECTION(){
//		try {
//			Log.v("Response Message",conn.getResponseMessage());
//		} catch (Throwable e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		try{
//			Log.v("Response Code",conn.getResponseCode() + "");
//		}catch (Throwable e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		Map<String, List<String>> headers = conn.getHeaderFields();
//		for(String s : headers.keySet()){
//			List<String> header = headers.get(s);
//			for(int i=0; i<header.size(); i++){
//				Log.v("HeaderInfo","Header: " + s + " Field: " + header.get(i));
//			}
//		}
	}
	
	public String getPostBody() {
		// TODO Auto-generated method stub
		return n;
	}

	
	public void addFile(String filename, Bitmap value, String key) {
		// TODO Auto-generated method stub
		HashMap<String,Object> tmpInfo = new HashMap<String,Object>();
		tmpInfo.put("FILENAME",filename);
		tmpInfo.put("FILE", value);
		
		mFiles.put(key, tmpInfo);
	}

	
	public void AutoXMLSAXCustom(DefaultHandler handler) {
		// TODO Auto-generated method stub
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(readtimeout);
        try {
        	SendRequest();
            if(conn.getResponseCode() == 200){
            	Xml.parse(conn.getInputStream(), Xml.Encoding.UTF_8, handler);
			}else{
				Log.v("HTTP REQUEST ERROR",getErrorResponse());
			}
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } 
	}

}
