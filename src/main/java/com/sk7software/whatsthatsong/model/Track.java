/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sk7software.whatsthatsong.model;

import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andrew on 24/07/2017.
 */
public class Track implements Serializable {
    private TrackItem item;
    @JsonProperty("progress_ms")
    private int progress;
    private String originalAlbumUri;
    private String originalAlbumName;

    public Track() {}

    public static Track createFromJSON(JSONObject response) throws IOException {
        Track track;

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        track = mapper.readValue(response.toString(), Track.class);

        return track;
    }

    public static Track createFromItemJSON(JSONObject response) throws IOException {
        TrackItem trackItem;

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        trackItem = mapper.readValue(response.toString(), TrackItem.class);

        Track track = new Track();
        track.setItem(trackItem);
        return track;
    }

    public TrackItem getItem() {
        return item;
    }

    public void setItem(TrackItem item) {
        this.item = item;
    }

    @JsonIgnore
    public String getName() {
        return item.getName();
    }

    // Don't expose artist array outside of object
    @JsonIgnore
    private Artist[] getArtists() {
        return item.getArtists();
    }

    @JsonIgnore
    public Album getAlbum() {
        return item.getAlbum();
    }

    @JsonIgnore
    public String getId() {
        return item.getId();
    }

    @JsonIgnore
    public boolean isExplicit() {
        return item.isExplicit();
    }

    @JsonIgnore
    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @JsonIgnore
    public int getDuration() {
        return item.getDuration();
    }

    @JsonIgnore
    public int getNumber() { return item.getNumber(); }

    @JsonIgnore
    public String getArtistName() {
        int numArtists = item.getArtists().length;
        if (numArtists == 1) {
            return item.getArtists()[0].getName();
        } else if (numArtists > 1) {
            int lastItem = numArtists - 1;
            StringBuilder artistsStr = new StringBuilder();
            for (int i=0; i<numArtists; i++) {
                if (i > 0) {
                    if (i == lastItem) {
                        artistsStr.append(" and ");
                    } else {
                        artistsStr.append(", ");
                    }
                }
                artistsStr.append(item.getArtists()[i].getName());
            }
            return artistsStr.toString();
        } else {
            return "Unknown artist";
        }
    }

    @JsonIgnore
    public List<String> getArtistIds() {
        List<String> ids = new ArrayList<>();
        for (Artist a : this.getItem().getArtists()) {
            ids.add(a.getId());
        }
        return ids;
    }

    @JsonIgnore
    public String getAlbumName() {
        return item.getAlbum().getName();
    }

    @JsonIgnore
    public String getAlbumArtist() { return item.getAlbum().getArtistName(); }

    @JsonIgnore
    public String getAlbumUri() {
        return item.getAlbum().getUri();
    }

    @JsonIgnore
    public String getAlbumId() {
        return item.getAlbum().getId();
    }

    @JsonIgnore
    public String getUri() { return item.getUri(); }

    public String getOriginalAlbumUri() {
        return originalAlbumUri;
    }

    public void setOriginalAlbumUri(String originalAlbumUri) {
        this.originalAlbumUri = originalAlbumUri;
    }

    public String getOriginalAlbumName() {
        return originalAlbumName;
    }

    public void setOriginalAlbumName(String originalAlbumName) {
        this.originalAlbumName = originalAlbumName;
    }

    public boolean hasOriginalAlbum() {
        return (originalAlbumUri != null && !"".equals(originalAlbumUri));
    }

    @JsonIgnore
    public String getArtworkUrl() { return item.getAlbum().getAlbumArtUrl(); }

    @JsonIgnore
    public String getFullDescription() {
        StringBuilder description = new StringBuilder();
        description.append("This song is ");
        description.append(getName());
        description.append(", by ");
        description.append(getArtistName());
        return description.toString();
    }

    @JsonIgnore
    public String getProgressDurationString() {
        StringBuilder info = new StringBuilder();
        info.append("The track is ");
        info.append(getTimeString(item.getDuration()));
        info.append(" long. It is currently ");
        info.append(getTimeString(progress));
        info.append(" of the way through.");
        return info.toString();
    }

    @JsonIgnore
    public String getFullAlbumDescription() {
        StringBuilder description = new StringBuilder();
        description.append(getAlbumName());
        description.append(", by ");
        description.append(getAlbumArtist());
        return description.toString();
    }

    @JsonIgnore
    public String getTimeString(int millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        StringBuilder durationString = new StringBuilder();
        if (hours > 0) {
            durationString.append(hours);
            durationString.append(" hour");
            if (hours > 1) { durationString.append("s"); }
            durationString.append(" ");
        }
        if (minutes > 0) {
            durationString.append(minutes);
            durationString.append(" minute");
            if (minutes > 1) { durationString.append("s"); }
            durationString.append(" ");
        }
        if (seconds > 0 || durationString.length() == 0) {
            durationString.append(seconds);
            durationString.append(" second");
            if (seconds != 1) { durationString.append("s"); }
        }
        
        return durationString.toString().trim();
    }
}
