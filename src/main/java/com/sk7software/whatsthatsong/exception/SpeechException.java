package com.sk7software.whatsthatsong.exception;

public abstract class SpeechException extends Exception {

    protected String speechText;

    public SpeechException() {
        super();
        speechText = "An error occurred";
    }

    public SpeechException(String message) {
        super(message);
        speechText = message;
    }

    public String getSpeechText() {
        return speechText;
    }
}
