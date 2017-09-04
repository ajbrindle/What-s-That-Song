package com.sk7software.whatsthatsong.exception;

public abstract class SpeechException extends Exception {

    private final String speechText;

    SpeechException(String message) {
        super(message);
        speechText = message;
    }

    public String getSpeechText() {
        return speechText;
    }
}
