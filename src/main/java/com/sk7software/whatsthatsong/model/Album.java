/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sk7software.whatsthatsong.model;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 *
 * @author Andrew
 */
public class Album {
    private String name;
    private String uri;
    private String id;
    private AlbumArt[] images;
    private TrackPage tracks;
    
    @JsonProperty("release_date")
    private String releaseDate;
    
    @JsonProperty("release_date_precision")
    private String releaseDatePrecision;

    public Album() {
    }

    public static Album createFromJSON(JSONObject response) throws IOException, JSONException {
        Album album = new Album();
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        album = mapper.readValue(response.toString(), Album.class);
        return album;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlbumArt[] getImages() {
        return images;
    }

    public void setImages(AlbumArt[] images) {
        this.images = images;
    }  

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TrackPage getTracks() {
        return tracks;
    }

    public void setTracks(TrackPage tracks) {
        this.tracks = tracks;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getReleaseDatePrecision() {
        return releaseDatePrecision;
    }

    public void setReleaseDatePrecision(String releaseDatePrecision) {
        this.releaseDatePrecision = releaseDatePrecision;
    }
    
    private String getReleaseYear() {
        if (releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        } else {
            return "Unknown";
        }
    }
    public String getAlbumInfo() {
        StringBuilder info = new StringBuilder();
        info.append("It was released in ");
        info.append(getReleaseYear());
        info.append(" and contains ");
        info.append(tracks.getTotal());
        if (tracks.getTotal() != 1) {
            info.append(" tracks");
        } else {
            info.append(" track");
        }
        
        return info.toString();
    }
}
