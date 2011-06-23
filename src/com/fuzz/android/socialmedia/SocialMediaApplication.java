package com.fuzz.android.socialmedia;


/**
 * Interface to be used only by a class extending Application
 * @author cesaraguilar
 **/
public interface SocialMediaApplication {
	
	/**
	 * Conviences Method to get the instance of the SocialMediaHandler shared
	 * in the application using a custom application class.
	 * 
	 * Sample implementation:
	 * if(handler == null){
	 *		handler = new SocialMediaHandler(this);
	 *	}
	 *	return handler;
	 * 
	 * @return the SocialMediaHandler instance
	 **/
	public SocialMediaHandler getSocialMediaHandler();
	
	
}
