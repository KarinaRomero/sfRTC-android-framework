package com.karinaromero.sfrtc;

/**
 * This class manage the Exceptions and extend Exception class.
 */
public class WebRTCClientException extends Exception {
    String message;
    /**
     * Default constructor.
     */
    public WebRTCClientException() {
        super();
    }

    /**
     * Constructor of class to initialize cause
     * @param message message to show when error occur
     */
    public WebRTCClientException(String message) {
        super(message);
        this.message = message;
    }
}
