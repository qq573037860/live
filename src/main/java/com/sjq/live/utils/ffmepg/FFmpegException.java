package com.sjq.live.utils.ffmepg;

public class FFmpegException extends Exception {

	public FFmpegException() {
	super();
	}
	 
	public FFmpegException(String message){
	super(message);
	}
	 
	public FFmpegException(Throwable cause){
	super(cause);
	}
	 
	public FFmpegException(String message,Throwable cause) {
	super(message, cause);
	}

}
