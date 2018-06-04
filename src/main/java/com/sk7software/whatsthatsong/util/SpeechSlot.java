package com.sk7software.whatsthatsong.util;

import com.amazon.ask.model.Intent;
import com.amazon.ask.model.Slot;

public class SpeechSlot {

    public static final String DEVICE_NUMBER = "deviceNumber";
    public static final String DEVICE_NAME = "deviceName";
    public static final String VOLUME_AMOUNT = "volumeAmount";
    public static final String VOLUME_DIRECTION = "volumeDirection";

    public static String getStringSlotValue(Intent intent, String slotName) {
        Slot slot = intent.getSlots().get(slotName);
        if (slot != null && slot.getValue() != null) {
            return slot.getValue();
        }

        return "";
    }

    public static int getIntSlotValue(Intent intent, String slotName) throws NumberFormatException {
        Slot slot = intent.getSlots().get(slotName);
        if (slot != null && slot.getValue() != null) {
            return Integer.parseInt(slot.getValue());
        } else {
            throw new NumberFormatException("Unable to find number in slot");
        }
    }
}
