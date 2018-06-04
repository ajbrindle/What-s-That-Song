package com.sk7software.whatsthatsong.util;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.SupportedInterfaces;
import com.amazon.ask.model.interfaces.display.DisplayInterface;

import java.util.Map;

public class DeviceCapability {

    DisplayInterface display;

    public DeviceCapability(HandlerInput handlerInput) {
        SupportedInterfaces supportedInterfaces = handlerInput.getRequestEnvelope()
                .getContext()
                .getSystem()
                .getDevice()
                .getSupportedInterfaces();

        display = supportedInterfaces.getDisplay();
    }

    public DisplayInterface getDisplay() {
        return display;
    }

    public void setDisplay(DisplayInterface display) {
        this.display = display;
    }

    public boolean hasDisplay() {
        return display != null;
    }
}
