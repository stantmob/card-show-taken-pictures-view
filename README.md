# Card Show Taken Picture View

> A custom view to take pictures and put image in card view gallery .

[![Releases](https://jitpack.io/v/stantmob/card-show-taken-pictures-view.svg)](https://jitpack.io/#stantmob/card-show-taken-pictures-view)

<p>
  <img src="https://github.com/stantmob/card-show-taken-pictures-view/blob/master/sample/demo-images/sample-edit-state.jpg" width="250">
  <img src="https://github.com/stantmob/card-show-taken-pictures-view/blob/master/sample/demo-images/sample-normal-state.jpg" width="250">
</p>

## How add into your project
Add the repository to your build.gradle with:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and on your project dependencies:

```gradle
dependencies {
        compile 'com.github.stantmob:card-show-taken-pictures-view:v1.0.0'
}

```

### How to use

1. Add component to view file 
```xml
    <br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView
        android:id="@+id/card_show_view_taken_pictures_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
```

2. Configure context setting Activity or Fragment
```java
    mBinding.cardShowViewTakenPicturesView.setActivity(this);
```
  or
```java
    mBinding.cardShowViewTakenPicturesView.setFragment(this);
```

3. Configure setOnSavedCardListener to get Callback with images 
```java
    mBinding.cardShowViewTakenPicturesView.setOnSavedCardListener(new CardShowTakenPictureViewContract.OnSavedCardListener() {
        @Override
        public void onSaved(List<CardShowTakenImage> imagesAsAdded, List<CardShowTakenImage> imagesAsRemoved) {
        }

        @Override
        public void onCancel() {

        }
    });
```

4. If you want show Edit State when no has Images when on create in view
```java
 // put method where you wish verify 
    mServiceInspectionFormFilledDetailFragBinding
                .cardShowTakenPictureView.ifNoImagesShowEditStateViewConfigurationOnInit();
```

5. Configure onActivityResult to set Image in Card with take picture success 
```java
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mBinding.cardShowViewTakenPicturesView.addImageOnActivityResult(requestCode, resultCode, data);
    }
```

6. Optional component attributes 
- app:pictureByName: `String`
- app:updatedAt: `date`
- app:showNoBorder: `boolean`, default value `false`
- app:editModeOnly: `boolean`, default value `false`

Add Programmatically :
```java
    mBinding.cardShowViewTakenPicturesView.setBinding(mBinding.cardShowViewTakenPicturesView,"Denis Vieira", new Date());
```

7. How setImages in the Card 
- Mapper your Image  object into CardShowTakenImage model . ( You can set remoteImageUrl or LocalImageFilename)
```
    public CardShowTakenImage(String identifier, String remoteImageUrl, String localImageFilename) 
```
- Set CardShowTakenImage List into view component. Use setCardImages method to 
```java
    mBinding.cardShowViewTakenPicturesView.setCardImages(cardShowTakenImagesList);
```


