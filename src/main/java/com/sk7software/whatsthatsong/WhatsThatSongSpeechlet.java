/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sk7software.whatsthatsong;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;

import com.sk7software.whatsthatsong.util.PlayerAction;
import com.sk7software.whatsthatsong.util.SpeechletUtils;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sk7software.whatsthatsong.model.Track;

/**
 *
 * @author Andrew
 */
public class WhatsThatSongSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(WhatsThatSongSpeechlet.class);

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        SpotifyAuthentication.setAccessToken(session.getUser().getAccessToken());
        log.info("Access token: " + session.getUser().getAccessToken());

        return TrackSpeechlet.getInstance().getNowPlayingResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}, intentName={}", request.getRequestId(),
                session.getSessionId(), (request.getIntent() != null ? request.getIntent().getName() : "null"));

        SpotifyAuthentication.setAccessToken(session.getUser().getAccessToken());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : "Invalid";
        
        switch (intentName) {
            case "WhatsThatSongIntent":
                return TrackSpeechlet.getInstance().getNowPlayingResponse();
            case "TrackExplicitIntent":
                return TrackSpeechlet.getInstance().getTrackExplicitResponse();
            case "PlayerControlSkipIntent":
                return PlayerControlSpeechlet.getInstance().playerControl(PlayerAction.SKIP);
            case "PlayerControlRestartIntent":
                return PlayerControlSpeechlet.getInstance().playerControl(PlayerAction.RESTART);
            case "PlayerControlPauseIntent":
                return PlayerControlSpeechlet.getInstance().playerControl(PlayerAction.PAUSE);
            case "PlayerControlResumeIntent":
                return PlayerControlSpeechlet.getInstance().playerControl(PlayerAction.RESUME);
            case "AlbumNameIntent":
                return TrackSpeechlet.getInstance().getAlbumNameResponse();
            case "AlbumPlayIntent":
                return PlayerControlSpeechlet.getInstance().playerControl(PlayerAction.PLAY_ALBUM);
            case "OriginalAlbumNameIntent":
                return TrackSpeechlet.getInstance().getTrackOriginalAlbumResponse();
            case "OriginalAlbumPlayIntent":
                return PlayerControlSpeechlet.getInstance().playerControl(PlayerAction.PLAY_ORIGINAL_ALBUM);
            case "TrackTimeIntent":
                return TrackSpeechlet.getInstance().getTrackTimeResponse();
            case "DeviceListIntent":
                return DeviceControlSpeechlet.getInstance().getDevicesResponse(true);
            case "DeviceFetchIntent":
                return DeviceControlSpeechlet.getInstance().getDevicesResponse(false);
            case "DevicePlayIntent":
                return DeviceControlSpeechlet.getInstance().getDevicePlayResponse(intent);
            case "DevicePlayByNameIntent":
                return DeviceControlSpeechlet.getInstance().getDevicePlayByNameResponse(intent);
            case "DeviceVolumeIntent":
                return DeviceControlSpeechlet.getInstance().getDeviceVolumeResponse(intent);
            case "DeviceMuteIntent":
                return DeviceControlSpeechlet.getInstance().getDeviceMuteResponse();
            case "DeviceUnmuteIntent":
                return DeviceControlSpeechlet.getInstance().getDeviceUnmuteResponse();
            case "AMAZON.HelpIntent":
                return getHelpResponse();
            case "AMAZON.StopIntent":
                return getStopResponse();
            default:
                throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        SpotifyAuthentication.setAccessToken("");
    }




    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        StringBuilder helpText = new StringBuilder();
        helpText.append("You can ask to skip or restart the song by saying, ");
        helpText.append("Skip, pause, resume, or restart it. ");
        helpText.append("You can ask if it contains explicit lyrics by saying, is it explicit. ");
        helpText.append("You can ask what album is it on. ");
        helpText.append("You can ask it to play the whole album. ");
        helpText.append("Please give your next command.");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(helpText.toString());

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(SpeechletUtils.REPROMPT_TEXT);
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    private SpeechletResponse getStopResponse() {
        String stopText = "Goodbye";

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(stopText);

        SpeechletResponse stopResponse = new SpeechletResponse();
        stopResponse.setShouldEndSession(true);
        stopResponse.setOutputSpeech(speech);
        
        return stopResponse;
    }
}
