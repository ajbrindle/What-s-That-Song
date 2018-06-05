package com.sk7software.whatsthatsong.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.display.ElementSelectedRequest;
import com.amazon.ask.request.Predicates;
import com.sk7software.whatsthatsong.exception.SpeechException;
import com.sk7software.whatsthatsong.model.Device;
import com.sk7software.whatsthatsong.network.SpotifyPlayerAPIService;
import com.sk7software.whatsthatsong.util.SpeechSlot;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import com.sun.beans.decoder.ElementHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.sk7software.whatsthatsong.util.SpeechletUtils.buildStandardAskResponse;

public class DeviceSelectedHandler implements RequestHandler {
    private static final Logger log = LoggerFactory.getLogger(DeviceSelectedHandler.class);

    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(Predicates.requestType(ElementSelectedRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        log.debug("Selected item handler");
        ElementSelectedRequest req = (ElementSelectedRequest)handlerInput.getRequestEnvelope().getRequest();
        String id = req.getToken();
        String speechText;
        speechText = playOnDevice(id, handlerInput);
        return buildStandardAskResponse(speechText, true).build();
    }

    private String playOnDevice(String id, HandlerInput handlerInput) {
        try {
            StringBuilder speechText = new StringBuilder();
            Map<String, Object> param = new HashMap<>();
            param.put("device_ids", new String[]{id});

            new SpotifyPlayerAPIService().sendPlayerCommand(SpotifyPlayerAPIService.PLAYER_URL, "PUT", param,
                    new SpotifyAuthentication(handlerInput));
            speechText.append("Playing on selected device");
            return speechText.toString();
        } catch (SpeechException se) {
            return se.getSpeechText();
        }
    }
}
