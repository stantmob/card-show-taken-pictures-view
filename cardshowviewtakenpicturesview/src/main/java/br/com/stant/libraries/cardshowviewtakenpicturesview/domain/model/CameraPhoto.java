package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model;

import java.io.Serializable;
import java.util.Date;

public class CameraPhoto implements Serializable{
    private String localImageFilename;
    private String tempImagePathToShow;
    private Date createdAt;
    private Date updatedAt;
    private String caption;

    public CameraPhoto(String localImageFilename, String tempImagePathToShow, Date createdAt, Date updatedAt) {
        this.localImageFilename  = localImageFilename;
        this.tempImagePathToShow = tempImagePathToShow;
        this.createdAt           = createdAt;
        this.updatedAt           = updatedAt;
    }

    public String getLocalImageFilename() {
        return localImageFilename;
    }

    public String getCaption() {
        return caption;
    }

    public String getTempImagePathToShow() {
        return tempImagePathToShow;
    }

    public void setTempImagePathToShow(String tempImagePathToShow) {
        this.tempImagePathToShow = tempImagePathToShow;
    }

    public void setPhotoCaption(String caption) {
        this.caption = caption;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }


}
