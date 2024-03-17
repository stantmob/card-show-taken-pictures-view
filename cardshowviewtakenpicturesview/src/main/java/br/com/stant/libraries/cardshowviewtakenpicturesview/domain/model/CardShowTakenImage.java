package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CardShowTakenImage implements Serializable, Cloneable {

    private String identifier;
    private String remoteImageUrl;
    private String localImageFilename;
    private String tempImagePathToShow;
    private Date createdAt;
    private Date updatedAt;
    private String caption;
    private List<String> errors = new ArrayList<>();

    private int order;

    public CardShowTakenImage(String identifier, String remoteImageUrl, String localImageFilename,
                              String tempImagePathToShow, Date createdAt, Date updatedAt, String caption,
                              List<String> errors) {
        this.identifier = identifier;
        this.remoteImageUrl = remoteImageUrl;
        this.localImageFilename = localImageFilename;
        this.tempImagePathToShow = tempImagePathToShow;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.caption = caption;
        this.errors = errors;
    }

    public CardShowTakenImage(String identifier, String remoteImageUrl, String localImageFilename,
                              Date createdAt, Date updatedAt, String caption) {
        this.identifier = identifier;
        this.remoteImageUrl = remoteImageUrl;
        this.localImageFilename = localImageFilename;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.caption = caption;
    }

    public CardShowTakenImage(String identifier, String remoteImageUrl, Date createdAt,
                              Date updatedAt) {
        this.identifier = identifier;
        this.remoteImageUrl = remoteImageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public CardShowTakenImage(String imageFilename, String tempImagePath, Date createdAt
            , Date updatedAt, String caption) {
        this.localImageFilename = imageFilename;
        this.tempImagePathToShow = tempImagePath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.caption = caption;
    }
    public CardShowTakenImage(String identifier, String remoteImageUrl, String localImageFilename,
                              Date createdAt, Date updatedAt, String caption, List<String> errors, int order) {
        this.identifier = identifier;
        this.remoteImageUrl = remoteImageUrl;
        this.localImageFilename = localImageFilename;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.caption = caption;
        this.errors = errors;
        this.order = order;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTempImagePathToShow() {
        return tempImagePathToShow;
    }

    public String getLocalImageFilename() {
        return localImageFilename;
    }

    public String getRemoteImageUrl() {
        return remoteImageUrl;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean hasOnlyRemoteUrl() {
        return !hasLocalImage() && !hasTempPathToShow() && remoteImageUrl != null && !remoteImageUrl.isEmpty();
    }

    private boolean hasTempPathToShow() {
        return tempImagePathToShow != null && !tempImagePathToShow.isEmpty();
    }

    public boolean hasLocalImage() {
        return localImageFilename != null && !localImageFilename.isEmpty();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getErrorsAsString() {
        StringBuilder errorsBuild = new StringBuilder();
        for (int i = 0; i < errors.size()-1; i++) {
            errorsBuild.append(errors.get(i)).append("\n\n");
        }
        errorsBuild.append(errors.get(errors.size()-1));
        return errorsBuild.toString();
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public boolean hasError() {
        return !this.errors.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardShowTakenImage)) return false;
        CardShowTakenImage that = (CardShowTakenImage) o;
        if (identifier != null) {
            return Objects.equals(identifier, that.identifier);
        }
        if (localImageFilename != null) {
            return Objects.equals(localImageFilename, that.localImageFilename);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, localImageFilename);
    }


    @Override
    public CardShowTakenImage clone() {
        try {
            CardShowTakenImage clone = (CardShowTakenImage) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
