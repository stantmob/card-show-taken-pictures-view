package br.com.stant.libraries.cameraimagegalleryview.injections;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class CardShowTakenImageInjection {

    private static CardShowTakenImageInjection cardShowTakenPictureInjection;

    private List<ImageCallback> listeners;
    private List<CardShowTakenImage> images;
    private List<CardShowTakenImage> removed;
    private List<CardShowTakenImage> added;
    private List<CardShowTakenImage> updated;

    private CardShowTakenImageInjection() {
        images = new ArrayList<>();
        removed = new ArrayList<>();
        added = new ArrayList<>();
        updated = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public static CardShowTakenImageInjection getCardShowTakenPictureInjection() {
        if (cardShowTakenPictureInjection == null) {
            cardShowTakenPictureInjection = new CardShowTakenImageInjection();
        }

        return cardShowTakenPictureInjection;
    }

    public boolean hasImages() {
        return !images.isEmpty();
    }

    public CardShowTakenImage getImageByIdentifier(String identifier) {
        for (CardShowTakenImage cardShowTakenImage : images) {
            if (identifier.equals(cardShowTakenImage.getIdentifier()))
                return cardShowTakenImage;
        }
        return null;
    }

    public boolean hasImageByIdentifier(String identifier) {
        for (CardShowTakenImage cardShowTakenImage : images) {
            if (identifier.equals(cardShowTakenImage.getIdentifier()))
                return true;
        }
        return false;
    }

    public boolean hasImagesWithErrors() {
        for (CardShowTakenImage image : images) {
            if (image.hasError()) return true;
        }
        return false;
    }

    public void setImages(List<CardShowTakenImage> newImages) {
        images = (List) ((ArrayList) newImages).clone();
        notifyListeners();
    }

    public void addImage(CardShowTakenImage image) {
        images.add(image);
        added.add(image);
        notifyListeners();
    }

    public void removeImage(CardShowTakenImage image) {
        removed.add(image);
        images.remove(image);
        added.remove(image);
        updated.remove(image);
        notifyListeners();
    }

    public void updateImage(CardShowTakenImage image) {
        if(!added.contains(image)){
            updated.add(image);
        } else {
            int index = added.indexOf(image);
            added.set(index, image);
        }
        int index = images.indexOf(image);
        images.set(index, image);
        notifyListeners();
    }

    public void removeList(List<CardShowTakenImage> cardShowTakenImages) {
        images.removeAll(cardShowTakenImages);
        removed.addAll(cardShowTakenImages);
        added.removeAll(cardShowTakenImages);
        notifyListeners();
    }

    public void clear() {
        images.clear();
        removed.clear();
        added.clear();
        listeners.clear();
    }

    public List<CardShowTakenImage> getAll() {
        return images;
    }

    public List<CardShowTakenImage> getAllRemoved() {
        return removed;
    }
    public List<CardShowTakenImage> getAllAdded() {
        return added;
    }

    public List<CardShowTakenImage> getAllUpdated() {
        return updated;
    }

    public void addListener(ImageCallback imageCallback) {
        listeners.add(imageCallback);
    }

    public void removeAllListener() {
        listeners.clear();
    }

    private void notifyListeners() {
        for (ImageCallback callback : listeners) {
            callback.action();
        }
    }
}
