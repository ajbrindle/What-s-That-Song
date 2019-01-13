package com.sk7software.whatsthatsong.model;

import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Lyrics {

    @JsonProperty("lyrics_body")
    private String lyrics;
    @JsonProperty("lyrics_copyright")
    private String copyright;

    public static Lyrics createFromJSON(JSONObject response) throws IOException {
        Lyrics lyrics;

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        lyrics = mapper.readValue(response.toString(), Lyrics.class);

        return lyrics;
    }

    private String format(String str) {
        return str.replace("\n", "<br/>");
    }

    public String getFormattedLyrics() {
        return format(lyrics);
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
}
