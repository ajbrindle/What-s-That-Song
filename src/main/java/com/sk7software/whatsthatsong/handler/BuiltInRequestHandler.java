package com.sk7software.whatsthatsong.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.response.ResponseBuilder;
import com.sk7software.whatsthatsong.util.PlayerAction;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;
import static com.sk7software.whatsthatsong.util.SpeechletUtils.buildStandardAskResponse;

public class BuiltInRequestHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput input) {
        return
                input.matches(intentName("AMAZON.HelpIntent")) ||
                        input.matches(intentName("AMAZON.MoreIntent")) ||
                        input.matches(intentName("AMAZON.NavigateHomeIntent")) ||
                        input.matches(intentName("AMAZON.NavigateSettingsIntent")) ||
                        input.matches(intentName("AMAZON.NextIntent")) ||
                        input.matches(intentName("AMAZON.PageUpIntent")) ||
                        input.matches(intentName("AMAZON.PageDownIntent")) ||
                        input.matches(intentName("AMAZON.PreviousIntent")) ||
                        input.matches(intentName("AMAZON.ScrollRightIntent")) ||
                        input.matches(intentName("AMAZON.ScrollLeftIntent")) ||
                        input.matches(intentName("AMAZON.ScrollUpIntent")) ||
                        input.matches(intentName("AMAZON.ScrollDownIntent")) ||
                        input.matches(intentName("AMAZON.CancelIntent")) ||
                        input.matches(intentName("AMAZON.StopIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        if (input.matches(intentName("AMAZON.HelpIntent"))) {
            return getHelpResponse();
        } else if (input.matches(intentName("AMAZON.StopIntent")) ||
                   input.matches(intentName("AMAZON.CancelIntent"))) {
            return getStopResponse();
        } else if (input.matches(intentName("AMAZON.NextIntent")) ||
                   input.matches(intentName("AMAZON.ScrollRightIntent"))) {
            return new DeviceControlHandler().playerControl(PlayerAction.SKIP, null, input);
        } else if (input.matches(intentName("AMAZON.PreviousIntent")) ||
                   input.matches(intentName("AMAZON.ScrollLeftIntent"))) {
            return new DeviceControlHandler().playerControl(PlayerAction.PREVIOUS, null, input);
        } else {
            return new ResponseBuilder()
                    .withSpeech("Done")
                    .withShouldEndSession(true)
                    .build();
        }
    }

    private Optional<Response> getHelpResponse() {
        StringBuilder helpText = new StringBuilder();
        helpText.append("You can ask to skip or restart the song by saying, ");
        helpText.append("Skip, pause, resume, or restart it. ");
        helpText.append("You can ask if it contains explicit lyrics by saying, is it explicit. ");
        helpText.append("You can ask what album is it on. ");
        helpText.append("You can ask it to play the whole album. ");
        helpText.append("Ask to list your devices, then transfer the playback to one of those devices. ");
        helpText.append("Please give your next command.");

        return buildStandardAskResponse(helpText.toString(), false).build();
    }

    private Optional<Response> getStopResponse() {
        String stopText = "Goodbye";

        return new ResponseBuilder()
                .withSpeech(StringEscapeUtils.escapeHtml4(stopText))
                .withShouldEndSession(true)
                .build();
    }
}
