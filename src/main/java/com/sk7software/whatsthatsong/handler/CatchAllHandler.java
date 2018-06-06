package com.sk7software.whatsthatsong.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;import com.amazon.ask.request.Predicates;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class CatchAllHandler implements RequestHandler {
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
        String speechText = "This is the default handler";
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("What's Playing", speechText)
                .withReprompt(speechText)
                .withShouldEndSession(true)
                .build();
    }
}
