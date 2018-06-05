package com.sk7software.whatsthatsong.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.Response;
import com.amazon.ask.response.ResponseBuilder;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;
import static com.sk7software.whatsthatsong.util.SpeechletUtils.*;

public class DeviceControlHandler implements RequestHandler {
    private static final Logger log = LoggerFactory.getLogger(DeviceControlHandler.class);


    private AvailableDevices devices;
    private SpotifyAuthentication authentication;
    private SpotifyPlayerAPIService playerService;

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("DeviceListIntent")) ||
                input.matches(intentName("DeviceFetchIntent")) ||
                input.matches(intentName("DevicePlayIntent")) ||
                input.matches(intentName("DevicePlayByNameIntent")) ||
                input.matches(intentName("DeviceVolumeIntent")) ||
                input.matches(intentName("DeviceMuteIntent")) ||
                input.matches(intentName("DeviceUnmuteIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {

        if (handlerInput.matches(intentName("DeviceListIntent"))) {
            return getDevicesResponse(handlerInput, true);
        } else if (handlerInput.matches(intentName("DeviceFetchIntent"))) {
            return getDevicesResponse(handlerInput, false);
        } else if (handlerInput.matches(intentName("DevicePlayIntent"))) {
            return new TrackHandler().getAlbumNameResponse(handlerInput);
        } else if (handlerInput.matches(intentName("DevicePlayByNameIntent"))) {
            return new TrackHandler().getTrackOriginalAlbumResponse(handlerInput);
        } else if (handlerInput.matches(intentName("DeviceVolumeIntent"))) {
            return new TrackHandler().getTrackTimeResponse(handlerInput);
        } else if (handlerInput.matches(intentName("DeviceMuteIntent"))) {
            return new TrackHandler().getTrackTimeResponse(handlerInput);
        } else if (handlerInput.matches(intentName("DeviceUnmuteIntent"))) {
            return new TrackHandler().getTrackTimeResponse(handlerInput);
        } else {
            return null;
        }
    }

    public Optional<Response> getDevicesResponse(HandlerInput handlerInput, boolean list) {
        String speechText;

        try {
            SpotifyWebAPIService devicesService = new DevicesAPIService();
            devices = (AvailableDevices)devicesService.fetchItem(SpotifyWebAPIService.DEVICES_URL,
                    new SpotifyAuthentication(handlerInput));
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

    /*
     * Transfers play to the requested device id
     */
    public Optional<Response> getDevicePlayResponse(Intent intent) {
        String speechText;

        try {
            int index = SpeechSlot.getIntSlotValue(intent, SpeechSlot.DEVICE_NUMBER);
            log.info("Requested device: " + index);

            Device device = devices.getDeviceAtIndex(index);
            speechText = playOnDevice(device.getId());
        } catch (NumberFormatException nfe) {
            speechText = "Sorry, I couldn't recognise that device number.";
        } catch (SpeechException se) {
            speechText = se.getSpeechText();
        }

        return buildStandardAskResponse(speechText, true).build();
    }

    public Optional<Response> getDevicePlayByNameResponse(Intent intent) {
        String speechText;
        String spokenName = SpeechSlot.getStringSlotValue(intent, SpeechSlot.DEVICE_NAME);

        log.info("Requested device: [" + spokenName + "]");

        try {
            String closestMatchId = devices.findClosestNameMatchId(spokenName);
            speechText = playOnDevice(closestMatchId);
        } catch (SpeechException se) {
            speechText = se.getSpeechText();
        }

        return buildStandardAskResponse(speechText, true).build();
    }

    private String playOnDevice(String id) {
        try {
            StringBuilder speechText = new StringBuilder();
            Map<String, Object> param = new HashMap<>();
            param.put("device_ids", new String[]{id});

            playerService.sendPlayerCommand(SpotifyPlayerAPIService.PLAYER_URL, "PUT", param, authentication);
            devices.setActiveDevice(id);
            speechText.append("Playing on ").append(devices.getActiveDevice().getName());
            return speechText.toString();
        } catch (SpeechException se) {
            return se.getSpeechText();
        }
    }

    public Optional<Response> getDeviceVolumeResponse(Intent intent) {
        String speechText;
        String direction = SpeechSlot.getStringSlotValue(intent, SpeechSlot.VOLUME_DIRECTION);
        int amount = SpeechSlot.getIntSlotValue(intent, SpeechSlot.VOLUME_AMOUNT);

        log.info("Volume direction: " + direction);
        log.info("Volume amount: " + amount);

        // Find the current active device
        Device active = devices.getActiveDevice();

        if (active != null) {
            int volume = active.getVolumePercent();
            int oldVolume = volume;

            if ("up".equals(direction)) {
                volume += amount;
            } else {
                volume -= amount;
            }

            speechText = setDeviceVolume(active, volume, oldVolume);
        } else {
            speechText = "Sorry, I can't find the active device";
        }

        return buildStandardAskResponse(speechText, false).build();
    }

    public Optional<Response> getDeviceMuteResponse() {
        String speechText;

        // Find the current active device
        Device active = devices.getActiveDevice();

        if (active != null) {
            int oldVolume = active.getVolumePercent();
            speechText = setDeviceVolume(active, 0, oldVolume);
        } else {
            speechText = "Sorry, I can't find the active device";
        }

        return buildStandardAskResponse(speechText, false).build();
    }

    private String setDeviceVolume(Device device, int newVolume, int oldVolume) {
        StringBuilder speechText = new StringBuilder();
        String volumeURL;

        log.info("Changing volume from " + oldVolume + " to " + newVolume);

        volumeURL = SpotifyPlayerAPIService.VOLUME_URL + String.valueOf(newVolume);

        try {
            playerService.sendPlayerCommand(volumeURL, "PUT", null, authentication);
            device.setVolumePercent(newVolume);
            device.setOldVolumePercent(oldVolume);

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

    public Optional<Response> getDeviceUnmuteResponse() {
        String speechText;

        // Find the current active device
        Device active = devices.getActiveDevice();

        if (active != null) {
            int newVolume = active.getOldVolumePercent();
            speechText = setDeviceVolume(active, newVolume, 0);
        } else {
            speechText = "Sorry, I can't find the active device";
        }

        return buildStandardAskResponse(speechText, false).build();
    }

    public Optional<Response> playerControl(PlayerAction action, Track track) {
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

            SpotifyPlayerAPIService playerService = new SpotifyPlayerAPIService();
            playerService.sendPlayerCommand(spotifyURL, method, postParams, authentication);
        } catch (SpeechException se) {
            speechText.delete(0, speechText.length());
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.delete(0, speechText.length());
            speechText.append("An error has occurred");
            log.error(e.getMessage());
        }

        return SpeechletUtils.buildStandardAskResponse(speechText.toString(), false).build();
    }

}
