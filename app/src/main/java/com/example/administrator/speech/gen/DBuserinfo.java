package com.example.administrator.speech.gen;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class DBuserinfo {

    @Id(autoincrement = true)
    private Long id ;

    private int isRecoder ;

    private String audio;

    private String audioText;

    @Generated(hash = 732703113)
    public DBuserinfo(Long id, int isRecoder, String audio, String audioText) {
        this.id = id;
        this.isRecoder = isRecoder;
        this.audio = audio;
        this.audioText = audioText;
    }

    @Generated(hash = 1828096530)
    public DBuserinfo() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getIsRecoder() {
        return this.isRecoder;
    }

    public void setIsRecoder(int isRecoder) {
        this.isRecoder = isRecoder;
    }

    public String getAudio() {
        return this.audio.split(".wav")[0];
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getAudioText() {
        return this.audioText;
    }

    public void setAudioText(String audioText) {
        this.audioText = audioText;
    }


}
