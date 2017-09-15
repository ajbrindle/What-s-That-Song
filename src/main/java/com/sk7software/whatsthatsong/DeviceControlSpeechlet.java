package com.sk7software.whatsthatsong;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.exception.SpeechException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.model.AvailableDevices;
import com.sk7software.whatsthatsong.model.Device;
import com.sk7software.whatsthatsong.util.SpeechSlot;
import com.sk7software.whatsthatsong.util.SpeechletUtils;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import com.sun.prism.Texture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.sk7software.whatsthatsong.util.SpeechletUtils.RESPONSE_DONE;
import static com.sk7software.whatsthatsong.util.SpeechletUtils.RESPONSE_RETRY;
import static com.sk7software.whatsthatsong.util.SpeechletUtils.buildStandardAskResponse;

public class DeviceControlSpeechlet {
    private static final Logger log = LoggerFactory.getLogger(DeviceControlSpeechlet.class);

    private AvailableDevices devices;

    private static DeviceControlSpeechlet instance = null;

    private DeviceControlSpeechlet() {
        this.devices = new AvailableDevices();
    }

    public static synchronized DeviceControlSpeechlet getInstance() {
        if (instance == null) {
            instance = new DeviceControlSpeechlet();
        }
        return instance;
    }

    public SpeechletResponse getDevicesResponse(boolean list) {
        String speechText;

        try {
            String devicesStr = SpeechletUtils.getJsonResponse("https://api.spotify.com/v1/me/player/devices",
                    SpotifyAuthentication.getAccessToken());
            log.info(devicesStr);
            devices = AvailableDevices.createFromJSON(new JSONObject(devicesStr));
            speechText = devices.getDeviceList(list);
        } catch (UsageLimitException ule) {
            speechText = ule.getSpeechText();
            log.error(ule.getMessage());
        } catch (SpeechException se) {
            speechText = se.getSpeechText();
            log.error(se.getMessage());
        } catch (Exception e) {
            speechText = "Sorry, there was a problem fetching the devices.";
            log.error(e.getMessage());
        }

        return buildStandardAskResponse(speechText, true);
    }

    /*
     * Transfers play to the requested device id
     */
    public SpeechletResponse getDevicePlayResponse(Intent intent) {
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

        return buildStandardAskResponse(speechText, true);
    }

    public SpeechletResponse getDevicePlayByNameResponse(Intent intent) {
        String speechText;
        String spokenName = SpeechSlot.getStringSlotValue(intent, SpeechSlot.DEVICE_NAME);

        log.info("Requested device: [" + spokenName + "]");

        try {
            String closestMatchId = devices.findClosestNameMatchId(spokenName);
            speechText = playOnDevice(closestMatchId);
        } catch (SpeechException se) {
            speechText = se.getSpeechText();
        }

        return buildStandardAskResponse(speechText, true);
    }

    private String playOnDevice(String id) {
        try {
            StringBuilder speechText = new StringBuilder();
            Map<String, Object> param = new HashMap<>();
            param.put("device_ids", new String[]{id});

            int responseCode = PlayerControlSpeechlet.getInstance()
                    .sendPlayerCommand(
                            "https://api.spotify.com/v1/me/player",
                            "PUT", param);

            if (responseCode == RESPONSE_RETRY) {
                speechText.append("Sorry, that didn't work.  Please try again.");
            } else if (responseCode != RESPONSE_DONE) {
                speechText.append("Sorry, I'm unable to complete that action.");
            } else {
                devices.setActiveDevice(id);
                speechText.append("Playing on ").append(devices.getActiveDevice().getName());
            }

            return speechText.toString();
        } catch (UsageLimitException ule) {
            return ule.getSpeechText();
        }
    }

    public SpeechletResponse getDeviceVolumeResponse(Intent intent) {
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

        return buildStandardAskResponse(speechText, false);
    }

    public SpeechletResponse getDeviceMuteResponse() {
        String speechText;

        // Find the current active device
        Device active = devices.getActiveDevice();

        if (active != null) {
            int oldVolume = active.getVolumePercent();
            speechText = setDeviceVolume(active, 0, oldVolume);
        } else {
            speechText = "Sorry, I can't find the active device";
        }

        return buildStandardAskResponse(speechText, false);
    }

    private String setDeviceVolume(Device device, int newVolume, int oldVolume) {
        StringBuilder speechText = new StringBuilder();
        String volumeURL;

        log.info("Changing volume from " + oldVolume + " to " + newVolume);

        volumeURL = "https://api.spotify.com/v1/me/player/volume?volume_percent=" + String.valueOf(newVolume);

        try {
            int responseCode = PlayerControlSpeechlet.getInstance().sendPlayerCommand(
                    volumeURL.toString(),
                    "PUT", null);

            if (responseCode == RESPONSE_RETRY) {
                // Can retry twice, 5 seconds apart
                for (int i = 0; i < 2; i++) {
                    try {
                        Thread.sleep(5000);
                        responseCode = PlayerControlSpeechlet.getInstance().sendPlayerCommand(
                                volumeURL.toString(),
                                "PUT", null);
                        if (responseCode != RESPONSE_RETRY) {
                            break;
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }

            if (responseCode != RESPONSE_DONE) {
                speechText.append("Sorry, I'm unable to complete that action.");
            } else {
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
            }

            return speechText.toString();
        } catch (UsageLimitException ule) {
            return ule.getSpeechText();
        }
    }

    public SpeechletResponse getDeviceUnmuteResponse() {
        String speechText;

        // Find the current active device
        Device active = devices.getActiveDevice();

        if (active != null) {
            int newVolume = active.getOldVolumePercent();
            speechText = setDeviceVolume(active, newVolume, 0);
        } else {
            speechText = "Sorry, I can't find the active device";
        }

        return buildStandardAskResponse(speechText, false);
    }
}
