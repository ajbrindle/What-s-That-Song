package com.sk7software.whatsthatsong.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.display.ElementSelectedRequest;
import com.amazon.ask.request.Predicates;
import com.amazon.ask.response.ResponseBuilder;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk7software.whatsthatsong.exception.SpeechException;
import com.sk7software.whatsthatsong.model.AvailableDevices;
import com.sk7software.whatsthatsong.model.Device;
import com.sk7software.whatsthatsong.model.Track;
import com.sk7software.whatsthatsong.network.DevicesAPIService;
import com.sk7software.whatsthatsong.network.SpotifyPlayerAPIService;
import com.sk7software.whatsthatsong.network.SpotifyWebAPIService;
import com.sk7software.whatsthatsong.util.PlayerAction;
import com.sk7software.whatsthatsong.util.SpeechSlot;
import com.sk7software.whatsthatsong.util.SpeechletUtils;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;
import static com.sk7software.whatsthatsong.util.SpeechletUtils.*;

public class DeviceControlHandler implements RequestHandler {
    private static final Logger log = LoggerFactory.getLogger(DeviceControlHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("DeviceListIntent")) ||
                input.matches(intentName("DeviceFetchIntent")) ||
                input.matches(Predicates.requestType(ElementSelectedRequest.class)) ||
                input.matches(intentName("DevicePlayIntent")) ||
                input.matches(intentName("DevicePlayByNameIntent")) ||
                input.matches(intentName("DeviceVolumeIntent")) ||
                input.matches(intentName("DeviceMuteIntent")) ||
                input.matches(intentName("DeviceUnmuteIntent")) ||
                input.matches(intentName("PlayerControlSkipIntent")) ||
                input.matches(intentName("PlayerControlRestartIntent")) ||
                input.matches(intentName("PlayerControlPauseIntent")) ||
                input.matches(intentName("PlayerControlResumeIntent")) ||
                input.matches(intentName("AlbumPlayIntent")) ||
                input.matches(intentName("OriginalAlbumPlayIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {

        if (handlerInput.matches(intentName("DeviceListIntent"))) {
            return getDevicesResponse(handlerInput, true);
        } else if (handlerInput.matches(intentName("DeviceFetchIntent"))) {
            return getDevicesResponse(handlerInput, false);
        } else if (handlerInput.matches(Predicates.requestType(ElementSelectedRequest.class))) {
            return getDeviceSelectedResponse(handlerInput);
        } else if (handlerInput.matches(intentName("DevicePlayIntent"))) {
            return getDevicePlayByIndexResponse(handlerInput);
        } else if (handlerInput.matches(intentName("DevicePlayByNameIntent"))) {
            return getDevicePlayByNameResponse(handlerInput);
        } else if (handlerInput.matches(intentName("DeviceVolumeIntent"))) {
            return getDeviceVolumeResponse(handlerInput);
        } else if (handlerInput.matches(intentName("DeviceMuteIntent"))) {
            return getDeviceMuteResponse(handlerInput);
        } else if (handlerInput.matches(intentName("DeviceUnmuteIntent"))) {
            return getDeviceUnmuteResponse(handlerInput);
        } else if (handlerInput.matches(intentName("DeviceUnmuteIntent"))) {
            return getDeviceUnmuteResponse(handlerInput);
        } else if (handlerInput.matches(intentName("PlayerControlSkipIntent"))) {
            return playerControl(PlayerAction.SKIP, null, handlerInput);
        } else if (handlerInput.matches(intentName("PlayerControlRestartIntent"))) {
            return playerControl(PlayerAction.RESTART, null, handlerInput);
        } else if (handlerInput.matches(intentName("PlayerControlPauseIntent"))) {
            return playerControl(PlayerAction.PAUSE, null, handlerInput);
        } else if (handlerInput.matches(intentName("PlayerControlResumeIntent"))) {
            return playerControl(PlayerAction.RESUME, null, handlerInput);
        } else if (handlerInput.matches(intentName("AlbumPlayIntent"))) {
            return fetchTrackAndPlayerControl(PlayerAction.PLAY_ALBUM, handlerInput);
        } else if (handlerInput.matches(intentName("OriginalAlbumPlayIntent"))) {
            return fetchTrackAndPlayerControl(PlayerAction.PLAY_ORIGINAL_ALBUM, handlerInput);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Response> getDevicesResponse(HandlerInput handlerInput, boolean list) {
        String speechText;
        AvailableDevices devices = new AvailableDevices();

        try {
            SpotifyWebAPIService devicesService = new DevicesAPIService();
            devices = (AvailableDevices)devicesService.fetchItem(SpotifyWebAPIService.DEVICES_URL,
                    new SpotifyAuthentication(handlerInput));
            addDevicesToSession(devices, handlerInput);
            speechText = devices.getDeviceList(list);
        } catch (SpeechException se) {
            speechText = se.getSpeechText();
            log.error(se.getMessage());
        } catch (Exception e) {
            speechText = "Sorry, there was a problem fetching the devices.";
            log.error(e.getMessage());
        }

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText, true);
        SpeechletUtils.addDeviceListDisplay(handlerInput, devices, responseBuilder);
        return responseBuilder.build();
    }

    public Optional<Response> getDeviceSelectedResponse(HandlerInput handlerInput) {
        log.debug("Selected item handler");
        ElementSelectedRequest req = (ElementSelectedRequest)handlerInput.getRequestEnvelope().getRequest();
        String id = req.getToken();
        AvailableDevices devices = getDevicesFromSession(handlerInput);
        String speechText;

        speechText = playOnDevice(id, devices, handlerInput);
        return buildStandardAskResponse(speechText, true).build();
    }

    /*
     * Transfers play to the requested device id
     */
    public Optional<Response> getDevicePlayByIndexResponse(HandlerInput handlerInput) {
        String speechText;

        try {
            IntentRequest ireq = (IntentRequest)handlerInput.getRequestEnvelope().getRequest();
            int index = SpeechSlot.getIntSlotValue(ireq.getIntent(), SpeechSlot.DEVICE_NUMBER);
            log.info("Requested device: " + index);

            AvailableDevices devices = getDevicesFromSession(handlerInput);
            Device device = devices.getDeviceAtIndex(index);
            speechText = playOnDevice(device.getId(), devices, handlerInput);
        } catch (NumberFormatException nfe) {
            speechText = "Sorry, I couldn't recognise that device number.";
        } catch (SpeechException se) {
            speechText = se.getSpeechText();
        }

        return buildStandardAskResponse(speechText, true).build();
    }

    public Optional<Response> getDevicePlayByNameResponse(HandlerInput handlerInput) {
        String speechText;

        try {
            IntentRequest ireq = (IntentRequest)handlerInput.getRequestEnvelope().getRequest();
            String spokenName = SpeechSlot.getStringSlotValue(ireq.getIntent(), SpeechSlot.DEVICE_NAME);
            log.info("Requested device: [" + spokenName + "]");


            AvailableDevices devices = getDevicesFromSession(handlerInput);
            String closestMatchId = devices.findClosestNameMatchId(spokenName);
            speechText = playOnDevice(closestMatchId, devices, handlerInput);
        } catch (SpeechException se) {
            speechText = se.getSpeechText();
        }

        return buildStandardAskResponse(speechText, true).build();
    }

    private String playOnDevice(String id, AvailableDevices devices, HandlerInput handlerInput) {
        try {
            StringBuilder speechText = new StringBuilder();
            Map<String, Object> param = new HashMap<>();
            param.put("device_ids", new String[]{id});

            new SpotifyPlayerAPIService().sendPlayerCommand(SpotifyPlayerAPIService.PLAYER_URL, "PUT", param,
                    new SpotifyAuthentication(handlerInput));
            devices.setActiveDevice(id);
            addDevicesToSession(devices, handlerInput);
            speechText.append("Playing on " + devices.getActiveDevice().getName());
            return speechText.toString();
        } catch (SpeechException se) {
            return se.getSpeechText();
        }
    }

    public Optional<Response> getDeviceVolumeResponse(HandlerInput handlerInput) {
        String speechText;
        IntentRequest ireq = (IntentRequest)handlerInput.getRequestEnvelope().getRequest();
        String direction = SpeechSlot.getStringSlotValue(ireq.getIntent(), SpeechSlot.VOLUME_DIRECTION);
        int amount = SpeechSlot.getIntSlotValue(ireq.getIntent(), SpeechSlot.VOLUME_AMOUNT);

        log.info("Volume direction: " + direction);
        log.info("Volume amount: " + amount);

        // Find the current active device
        AvailableDevices devices = getDevicesFromSession(handlerInput);
        Device active = devices.getActiveDevice();

        if (active != null) {
            int volume = active.getVolumePercent();
            int oldVolume = volume;

            if ("up".equals(direction)) {
                volume += amount;
            } else {
                volume -= amount;
            }
            speechText = setDeviceVolume(devices, volume, oldVolume, handlerInput);
        } else {
            speechText = "Sorry, I can't find the active device";
        }

        return buildStandardAskResponse(speechText, false).build();
    }

    public Optional<Response> getDeviceMuteResponse(HandlerInput handlerInput) {
        String speechText;

        // Find the current active device
        AvailableDevices devices = getDevicesFromSession(handlerInput);
        Device active = devices.getActiveDevice();

        if (active != null) {
            int oldVolume = active.getVolumePercent();
            speechText = setDeviceVolume(devices, 0, oldVolume, handlerInput);
        } else {
            speechText = "Sorry, I can't find the active device";
        }

        return buildStandardAskResponse(speechText, false).build();
    }

    private String setDeviceVolume(AvailableDevices devices, int newVolume, int oldVolume, HandlerInput handlerInput) {
        StringBuilder speechText = new StringBuilder();
        String volumeURL;

        log.info("Changing volume from " + oldVolume + " to " + newVolume);

        volumeURL = SpotifyPlayerAPIService.VOLUME_URL + String.valueOf(newVolume);

        try {
            new SpotifyPlayerAPIService().sendPlayerCommand(volumeURL, "PUT", null,
                    new SpotifyAuthentication(handlerInput));
            devices.getActiveDevice().setVolumePercent(newVolume);
            devices.getActiveDevice().setOldVolumePercent(oldVolume);
            addDevicesToSession(devices, handlerInput);

            if (newVolume == 0) {
                speechText.append("Muted");
            } else if (oldVolume == 0) {
                speechText.append("Unmuted");
            } else {
                speechText.append("Volume ");
                speechText.append(newVolume < oldVolume ? "down" : "up");
//                    speechText.append(" from ");
//                    speechText.append(oldVolume);
//                    speechText.append(" to ");
//                    speechText.append(newVolume);
            }
            return speechText.toString();
        } catch (SpeechException se) {
            return se.getSpeechText();
        }
    }

    public Optional<Response> getDeviceUnmuteResponse(HandlerInput handlerInput) {
        String speechText;

        // Find the current active device
        AvailableDevices devices = getDevicesFromSession(handlerInput);
        Device active = devices.getActiveDevice();

        if (active != null) {
            int newVolume = active.getOldVolumePercent();
            speechText = setDeviceVolume(devices, newVolume, 0, handlerInput);
        } else {
            speechText = "Sorry, I can't find the active device";
        }

        return buildStandardAskResponse(speechText, false).build();
    }

    private Optional<Response> fetchTrackAndPlayerControl(PlayerAction action, HandlerInput handlerInput) {
        Track track = TrackHandler.getTrackFromSession(handlerInput);
        return playerControl(action, track, handlerInput);
    }

    public Optional<Response> playerControl(PlayerAction action, Track track, HandlerInput handlerInput) {
        StringBuilder speechText = new StringBuilder();

        try {
            String spotifyURL;
            String method;
            Map<String, Object> postParams = new HashMap<>();
            Map<String, Object> position = new HashMap<>();

            switch (action) {
                case SKIP:
                    spotifyURL = SpotifyPlayerAPIService.SKIP_URL;
                    method = "POST";
                    speechText.append("Skipped");
                    break;
                case PREVIOUS:
                    spotifyURL = SpotifyPlayerAPIService.PREV_URL;
                    method = "POST";
                    speechText.append("Skipped");
                    break;
                case RESTART:
                    spotifyURL = SpotifyPlayerAPIService.RESTART_URL;
                    method = "PUT";
                    speechText.append("Restarted");
                    break;
                case PAUSE:
                    spotifyURL = SpotifyPlayerAPIService.PAUSE_URL;
                    method = "PUT";
                    speechText.append("Paused");
                    break;
                case RESUME:
                    spotifyURL = SpotifyPlayerAPIService.RESUME_URL;
                    method = "PUT";
                    speechText.append("Resumed");
                    break;
                case PLAY_ALBUM:
                    spotifyURL = SpotifyPlayerAPIService.PLAY_ALBUM_URL;
                    method = "PUT";
                    postParams.put("context_uri", track.getAlbumUri());
                    position.put("position", 0);
                    postParams.put("offset", position);
                    speechText.append("Playing ");
                    speechText.append(track.getAlbumName());
                    break;
                case PLAY_ORIGINAL_ALBUM:
                    spotifyURL = SpotifyPlayerAPIService.PLAY_ORIGINAL_ALBUM_URL;
                    if (track.hasOriginalAlbum()) {
                        method = "PUT";
                        postParams.put("context_uri", track.getOriginalAlbumUri());
                        position.put("position", 0);
                        postParams.put("offset", position);
                        speechText.append("Playing ");
                        speechText.append(track.getOriginalAlbumName());
                    } else {
                        speechText.append("Please ask if this track has an original album first");
                        return SpeechletUtils.buildStandardAskResponse(speechText.toString(), false).build();
                    }
                    break;
                default:
                    // Never happens as actions are controlled by speechlet
                    throw new Exception("Invalid action");
            }

            new SpotifyPlayerAPIService()
                    .sendPlayerCommand(spotifyURL, method, postParams,
                    new SpotifyAuthentication(handlerInput));
        } catch (SpeechException se) {
            speechText.delete(0, speechText.length());
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.delete(0, speechText.length());
            speechText.append("An error has occurred");
            log.error(e.getMessage());
        }

        ResponseBuilder responseBuilder =
                SpeechletUtils.buildStandardAskResponse(speechText.toString(), false);
        if (track != null) {
            SpeechletUtils.addTrackDisplay(handlerInput, track, responseBuilder);
        }
        return responseBuilder.build();
    }

    private void addDevicesToSession(AvailableDevices devices, HandlerInput handlerInput) {
        // Store device list on session
        ObjectMapper mapper = new ObjectMapper();
        handlerInput.getAttributesManager().getSessionAttributes()
                .put("deviceList", mapper.convertValue(devices, Map.class));
    }

    private AvailableDevices getDevicesFromSession(HandlerInput handlerInput) {
        try {
            if (handlerInput.getAttributesManager().getSessionAttributes().containsKey("deviceList")) {
                JSONObject j = new JSONObject(handlerInput
                        .getAttributesManager()
                        .getSessionAttributes());
                AvailableDevices d = AvailableDevices.createFromJSON(j.getJSONObject("deviceList"));
                log.debug("Restored devices: " + d.getNumberOfDevices());
                return d;
            }
        } catch (JSONException je) {
            log.error("Unable to deserialise devices: " + je.getMessage());
        } catch (IOException ie) {
            log.error("Unable to deserialise devices: " + ie.getMessage());
        }
        return null;
    }
}
