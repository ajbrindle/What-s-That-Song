package com.sk7software.whatsthatsong;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
import com.amazon.ask.dispatcher.exception.ExceptionHandler;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.response.ResponseBuilder;
import com.sk7software.whatsthatsong.handler.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class could be the handler for an AWS Lambda function powering an Alexa Skills Kit
 * experience. To do this, simply set the handler field in the AWS Lambda console to
 * "helloworld.JourneyTimerSpeechletRequestStreamHandler" For this to work, you'll also need to build
 * this project using the {@code lambda-compile} Ant task and upload the resulting zip file to power
 * your function.
 */
public class WhatsThatSongSpeechletRequestStreamHandler extends SkillStreamHandler {
    private static final Logger log = LoggerFactory.getLogger(WhatsThatSongSpeechletRequestStreamHandler.class);
    private static final Set<String> supportedApplicationIds = new HashSet<>();

    private static Skill getSkill() {
        return Skills.standard()
                .addRequestHandlers(
                        new WhatsThatSongIntentHandler(),
                        new DeviceControlHandler(),
                        new LaunchRequestHandler(),
                        new BuiltInRequestHandler())
                .addExceptionHandler(
                        new ExceptionHandler() {
                            @Override
                            public boolean canHandle(HandlerInput handlerInput, Throwable throwable) {
                                return true;
                            }

                            @Override
                            public Optional<Response> handle(HandlerInput handlerInput, Throwable throwable) {
                                throwable.printStackTrace();
                                log.debug(throwable.getMessage());
                                return new ResponseBuilder()
                                        .withSpeech("There was an error looking up the track")
                                        .withShouldEndSession(true)
                                        .build();
                            }
                        }
                )
                .withSkillId("amzn1.ask.skill.d107f617-9a63-464e-95b7-9f29403e716b")
                .build();
    }


    public WhatsThatSongSpeechletRequestStreamHandler() {
        super(getSkill());
    }
}
