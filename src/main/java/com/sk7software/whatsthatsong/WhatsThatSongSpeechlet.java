/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sk7software.whatsthatsong;


/**
 *
 * @author Andrew
 */
public class WhatsThatSongSpeechlet {
/*
    private static final Logger log = LoggerFactory.getLogger(WhatsThatSongSpeechlet.class);

    private SpotifyAuthentication authentication = new SpotifyAuthentication();
    private DeviceControlHandler deviceControlSpeechlet;
    private TrackHandler trackSpeechlet;
    private boolean initialised = false;

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        initialise(session);
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        initialise(session);
        return null; //trackSpeechlet.getNowPlayingResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}, intentName={}", request.getRequestId(),
                session.getSessionId(), (request.getIntent() != null ? request.getIntent().getName() : "null"));

        authentication.setAccessToken(session.getUser().getAccessToken());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : "Invalid";

        switch (intentName) {
            case "WhatsThatSongIntent":
                return trackSpeechlet.getNowPlayingResponse();
            case "TrackExplicitIntent":
                return trackSpeechlet.getTrackExplicitResponse();
            case "PlayerControlSkipIntent":
                return deviceControlSpeechlet.playerControl(PlayerAction.SKIP, trackSpeechlet.getTrack());
            case "PlayerControlRestartIntent":
                return deviceControlSpeechlet.playerControl(PlayerAction.RESTART, trackSpeechlet.getTrack());
            case "PlayerControlPauseIntent":
                return deviceControlSpeechlet.playerControl(PlayerAction.PAUSE, trackSpeechlet.getTrack());
            case "PlayerControlResumeIntent":
                return deviceControlSpeechlet.playerControl(PlayerAction.RESUME, trackSpeechlet.getTrack());
            case "AlbumNameIntent":
                return trackSpeechlet.getAlbumNameResponse();
            case "AlbumPlayIntent":
                return deviceControlSpeechlet.playerControl(PlayerAction.PLAY_ALBUM, trackSpeechlet.getTrack());
            case "OriginalAlbumNameIntent":
                return trackSpeechlet.getTrackOriginalAlbumResponse();
            case "OriginalAlbumPlayIntent":
                return deviceControlSpeechlet.playerControl(PlayerAction.PLAY_ORIGINAL_ALBUM, trackSpeechlet.getTrack());
            case "TrackTimeIntent":
                return trackSpeechlet.getTrackTimeResponse();
            case "DeviceListIntent":
                return deviceControlSpeechlet.getDevicesResponse(true);
            case "DeviceFetchIntent":
                return deviceControlSpeechlet.getDevicesResponse(false);
            case "DevicePlayIntent":
                return deviceControlSpeechlet.getDevicePlayResponse(intent);
            case "DevicePlayByNameIntent":
                return deviceControlSpeechlet.getDevicePlayByNameResponse(intent);
            case "DeviceVolumeIntent":
                return deviceControlSpeechlet.getDeviceVolumeResponse(intent);
            case "DeviceMuteIntent":
                return deviceControlSpeechlet.getDeviceMuteResponse();
            case "DeviceUnmuteIntent":
                return deviceControlSpeechlet.getDeviceUnmuteResponse();
            case "AMAZON.HelpIntent":
                return getHelpResponse();
            case "AMAZON.StopIntent":
                return getStopResponse();
            default:
                throw new SpeechletException("Invalid Intent");
        }

        return null;
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
    }



    private SpeechletResponse getHelpResponse() {
        StringBuilder helpText = new StringBuilder();
        helpText.append("You can ask to skip or restart the song by saying, ");
        helpText.append("Skip, pause, resume, or restart it. ");
        helpText.append("You can ask if it contains explicit lyrics by saying, is it explicit. ");
        helpText.append("You can ask what album is it on. ");
        helpText.append("You can ask it to play the whole album. ");
        helpText.append("Ask to list your devices, then transfer the playback to one of those devices. ");
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
        String stopText = "Enjoy your music. Goodbye";

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(stopText);

        SpeechletResponse stopResponse = new SpeechletResponse();
        stopResponse.setShouldEndSession(true);
        stopResponse.setOutputSpeech(speech);
        
        return stopResponse;
    }

    private void initialise(Session session) {
//        if (!initialised) {
//            authentication.setAccessToken(session.getUser().getAccessToken());
//            log.info("Access token: " + session.getUser().getAccessToken());
//            trackSpeechlet = new TrackHandler(authentication);
//            deviceControlSpeechlet = new DeviceControlHandler(authentication);
//            initialised = true;
//        }
    }
    */
}
