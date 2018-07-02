package com.gram.meme.journalapp.JournalPojos;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 6/28/2018.
 */

public class JournalPojo {
    private String journalContent;

    public void setJournalContent(String journalContent) {
        this.journalContent = journalContent;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getJournalContent() {

        return journalContent;
    }

    public String getName() {
        return name;
    }
    private String name;

    public void setPhotoUrl( String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public  String getPhotoUrl() {
        return photoUrl;
    }

    private String photoUrl;
    private String timeOfPost;

    public void setTimeOfPost(String timeOfPost) {
        this.timeOfPost = timeOfPost;
    }

    public String getTimeOfPost() {
        return timeOfPost;
    }

    public JournalPojo(String journalContent, String name,  String photoUrl, String timeOfPost) {
        this.journalContent = journalContent;
        this.name = name;
        this.photoUrl = photoUrl;
        this.timeOfPost = timeOfPost;
    }

    public JournalPojo() {
        //
    }
}
