package br.com.stant.libraries.cardshowviewtakenpicturesview.camera

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_IMAGE_CAMERA_LIST
import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract.CardShowTakenCompressedCallback
import br.com.stant.libraries.cardshowviewtakenpicturesview.R
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.utils.CameraSetup
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.utils.CameraSetup.getLensPosition
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraFragmentBinding
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraPhotoPreviewDialogBinding
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.*
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.getBitmapFromFile
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator.fromGallery
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPG_FILE_PREFIX
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPG_FILE_SUFFIX
import com.annimon.stream.Optional.ofNullable
import io.fotoapparat.result.BitmapPhoto
import io.fotoapparat.result.WhenDoneListener
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.util.*

class CameraFragment : Fragment(), CameraContract {

    private lateinit var mCameraFragmentBinding: CameraFragmentBinding
    private lateinit var mCameraPhotoPreviewDialogBinding: CameraPhotoPreviewDialogBinding

    private var mCameraPhotosAdapter: CameraPhotosAdapter? = null
    private var mPath: File?                               = null
    private var mCameraSetup: CameraSetup?                 = null
    private var mImageGenerator: ImageGenerator?           = null
    private var mDialogLoader: DialogLoader?               = null
    private var mPreviewPicDialog: Dialog?                 = null
    private var mOrientationListener: OrientationListener? = null
    private var mSaveOnlySnackbar: Snackbar?               = null
    private var mSaveMode: String                          = STANT_MODE

    private val currentImagesQuantity: Int
        get() = mCameraPhotosAdapter?.itemCount ?: 0

    private val isOverLimit: Boolean
        get() = if (mPhotosLimit == -1) {
            false
        } else mImageListSize + currentImagesQuantity > mPhotosLimit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

        context?.let {
            setupDialog(it)

            mPath                = ImageViewFileUtil.getPrivateTempDirectory(it)
            mDialogLoader        = DialogLoader(it)
            mImageGenerator      = ImageGenerator(it)
            mCameraPhotosAdapter = CameraPhotosAdapter(it, this)
        }
    }

    private fun setupDialog(context: Context) {
        mPreviewPicDialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)

        mCameraPhotoPreviewDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.camera_photo_preview_dialog, null, false)

        mCameraPhotoPreviewDialogBinding.handler = this
        mPreviewPicDialog?.setContentView(mCameraPhotoPreviewDialogBinding.root)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mCameraFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.camera_fragment, container, false)

        if (hasNavigationBar()) {
            setNavigationCameraControlsPadding()
        }

        return mCameraFragmentBinding.root
    }

    private fun hasNavigationBar(): Boolean {
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    private fun setNavigationCameraControlsPadding() {
        ofNullable(mCameraFragmentBinding.cameraFragmentBottomLinearLayout).executeIfPresent { navigationCamera ->
            navigationCamera.setPadding(
                    navigationCamera.paddingLeft,
                    navigationCamera.paddingTop,
                    navigationCamera.paddingRight,
                    navigationCamera.bottom + convertDpToPixels(48))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSaveOnlySnackbar = Snackbar.make(mCameraFragmentBinding.root,
                "Those images will be saved at \"Pictures/Stant/\" folder only",
                Snackbar.LENGTH_SHORT)

        setButtonsClick(mCameraFragmentBinding.cameraFragmentCloseImageView,
                mCameraFragmentBinding.cameraFragmentChangeSavePicturesMode,
                mCameraFragmentBinding.cameraFragmentCaptureImageButton,
                mCameraFragmentBinding.cameraFragmentGalleryImageView,
                mCameraFragmentBinding.cameraFragmentSaveImageView)

        setAdapter(mCameraFragmentBinding.cameraPhotosRecyclerView)

        setCameraSetup(mCameraFragmentBinding.cameraFragmentSwitchFlashImageView,
                mCameraFragmentBinding.cameraFragmentZoomSeekBar,
                mCameraFragmentBinding.cameraFragmentSwitchLensImageView)

        configureOrientationListener(mCameraFragmentBinding.cameraFragmentCloseImageView,
                mCameraFragmentBinding.cameraFragmentChangeSavePicturesMode,
                mCameraFragmentBinding.cameraFragmentCaptureImageButton,
                mCameraFragmentBinding.cameraFragmentGalleryImageView,
                mCameraFragmentBinding.cameraFragmentSaveImageView,
                mCameraFragmentBinding.cameraFragmentSwitchLensImageView,
                mCameraFragmentBinding.cameraFragmentChipLinearLayout,
                mCameraFragmentBinding.cameraFragmentSwitchFlashImageView)

        updateCounters()
    }

    private fun setButtonsClick(closeButton: ImageView, changeSavePicturesReason: ImageView,
                                captureButton: ImageButton, openGalleryButton: ImageView, savePhotosButton: ImageView) {
        closeButton.setOnClickListener { closeCamera() }

        changeSavePicturesReason.setOnClickListener{
            mSaveMode = if (mSaveMode == STANT_MODE) {
                changeSavePicturesReason.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_stant_disabled))
                showSaveOnlySnackBar()
                SAVE_ONLY_MODE
            } else {
                changeSavePicturesReason.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_stant_enabled))
                hideSaveOnlySnackBar()
                STANT_MODE
            }
        }

        captureButton.setOnClickListener {
            if (cameraImagesQuantityIsNotOnLimit()) {
                showToastWithCameraLimitQuantity()
            } else {
                takePicture()
            }
        }

        openGalleryButton.setOnClickListener {
            if (cameraImagesQuantityIsNotOnLimit()) {
                showToastWithCameraLimitQuantity()
            } else {
                openGallery()
            }
        }

        savePhotosButton.setOnClickListener {
            if (isOverLimit) {
                Toast.makeText(context, getString(R.string.camera_photo_reached_limit), Toast.LENGTH_SHORT).show()
            } else {
                returnImagesToCardShowTakenPicturesView()
            }
        }
    }

    private fun showSaveOnlySnackBar() {
        mSaveOnlySnackbar?.show()
        }

    private fun hideSaveOnlySnackBar() {
        mSaveOnlySnackbar?.dismiss()
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.isFocusable              = false
        recyclerView.adapter                  = mCameraPhotosAdapter

        if (mCameraPhotos != null) {
            mCameraPhotosAdapter?.addAllPhotos(mCameraPhotos)
        }
    }

    private fun setCameraSetup(flashImageView: ImageView, zoomSeekBar: VerticalSeekBar,
                               switchCameraImageView: ImageView) {
        mCameraSetup = CameraSetup(context,
                mCameraFragmentBinding.cameraFragmentView,
                mCameraFragmentBinding.cameraFragmentFocusView)

        mCameraSetup?.let {
            it.toggleTorchOnSwitch(flashImageView)
            it.zoomSeekBar(zoomSeekBar)
            it.switchCameraOnClick(switchCameraImageView, flashImageView)
        }
    }

    private fun configureOrientationListener(closeButton: ImageView, changeSavePicturesReason: ImageView,
                                             captureButton: ImageButton, openGalleryButton: ImageView,
                                             savePhotosButton: ImageView, switchCameraImageView: ImageView,
                                             chipLinearLayout: LinearLayout, flashImageView: ImageView) {
        mOrientationListener = object : OrientationListener(context, closeButton, changeSavePicturesReason, captureButton,
                savePhotosButton, openGalleryButton, switchCameraImageView, chipLinearLayout, flashImageView) {
            override fun onSimpleOrientationChanged(orientation: Int) {
                setOrientationView(orientation)
            }
        }
    }

    private fun getColor(color: Int): Int {
        return context?.let { ContextCompat.getColor(it, color) } ?: 0
    }

    fun updateCounters() {
        if (cameraImagesQuantityIsNotOnLimit()) {
            setDesignPhotoLimitIsTrue()
        } else {
            setDesignPhotoLimitIsFalse()
        }

        activity?.runOnUiThread {
            mCameraFragmentBinding.cameraFragmentCurrentValue.text = currentImagesQuantity.toString()
            mCameraFragmentBinding.cameraFragmentLimitValue.text   = CAMERA_IMAGES_QUANTITY_LIMIT.toString()
        }
    }

    private fun setDesignPhotoLimitIsTrue() {
        mCameraFragmentBinding.cameraFragmentCurrentValue.setTextColor(getColor(R.color.white))
        mCameraFragmentBinding.cameraFragmentLimitValue.setTextColor(getColor(R.color.white))
        mCameraFragmentBinding.cameraFragmentChipDivisorTextView.setTextColor(getColor(R.color.white))
        mCameraFragmentBinding.cameraFragmentChipLinearLayout.background = context?.let { ContextCompat.getDrawable(it, R.drawable.shape_rectangle_red) }
    }

    private fun setDesignPhotoLimitIsFalse() {
        mCameraFragmentBinding.cameraFragmentCurrentValue.setTextColor(getColor(R.color.black))
        mCameraFragmentBinding.cameraFragmentLimitValue.setTextColor(getColor(R.color.black))
        mCameraFragmentBinding.cameraFragmentChipDivisorTextView.setTextColor(getColor(R.color.black))
        mCameraFragmentBinding.cameraFragmentChipLinearLayout.background = context?.let { ContextCompat.getDrawable(it, R.drawable.shape_rectangle_chip) }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"

        if (mIsMultipleGallerySelection) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.gallery_select_pictures)), REQUEST_IMAGE_LIST_GALLERY_RESULT)
    }

    override fun onResume() {
        super.onResume()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        mOrientationListener?.enable()
    }

    override fun onPause() {
        super.onPause()
        mOrientationListener?.disable()
    }

    override fun onStart() {
        super.onStart()
        mCameraSetup?.fotoapparat?.start()
    }

    override fun onStop() {
        super.onStop()
        mCameraSetup?.fotoapparat?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mOrientationListener?.disable()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_LIST_GALLERY_RESULT && resultCode == Activity.RESULT_OK && data != null) {
            if (data.data != null) {
                val imageUri = data.data

                generateImageCallback(imageUri)
            } else if (data.clipData != null) {
                val count = data.clipData?.itemCount ?: 0

                for (i in 0 until count) {
                    val imageUri = data.clipData?.getItemAt(i)?.uri

                    generateImageCallback(imageUri)
                }
            }

            updateCounters()

            val bundle = Bundle()
            bundle.putSerializable(BUNDLE_PHOTOS, mCameraPhotosAdapter?.list as Serializable)

            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.replace(R.id.camera_content_frame, CameraFragment.newInstance(mPhotosLimit,
                    mImageListSize, mIsMultipleGallerySelection, bundle))
            fragmentTransaction?.commit()
        }
    }

    private fun generateImageCallback(imageUri: Uri?) {
        mImageGenerator?.generateCardShowTakenImageFromImageGallery(imageUri, fromGallery,
                object : CardShowTakenCompressedCallback {
                    override fun onSuccess(bitmap: Bitmap, imageFilename: String, tempImagePath: String) {
                        val cameraPhoto = CameraPhoto(imageFilename, tempImagePath, Date(), Date())

                        mCameraPhotosAdapter?.addPicture(cameraPhoto)
                        mCameraFragmentBinding.cameraPhotosRecyclerView.smoothScrollToPosition(
                                (mCameraPhotosAdapter?.itemCount ?: 0) - 1)
                    }

                    override fun onError() {

                    }
                })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, getString(R.string.camera_no_permission), Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }

    override fun closeCamera() {
        activity?.finish()

        mCameraPhotos = null
    }

    override fun setPhotos(photos: ArrayList<CameraPhoto>?) {
        if (photos != null) {
            mCameraPhotosAdapter?.addAllPhotos(photos)
        }
    }

    private fun showToastWithCameraLimitQuantity() {
        Toast.makeText(context, String.format(getString(R.string.card_show_taken_picture_view_camera_quantity_limit), CAMERA_IMAGES_QUANTITY_LIMIT), Toast.LENGTH_SHORT).show()
    }

    private fun cameraImagesQuantityIsNotOnLimit(): Boolean {
        return currentImagesQuantity >= CAMERA_IMAGES_QUANTITY_LIMIT
    }

    override fun takePicture() {
        val photoResult = mCameraSetup?.fotoapparat?.takePicture()
        val uuid        = UUID.randomUUID().toString()
        val photoPath   = File(mPath?.toString() + "/" + JPG_FILE_PREFIX + uuid + JPG_FILE_SUFFIX)

        mDialogLoader?.showLocalLoader()

        photoResult?.saveToFile(photoPath)

        photoResult?.toBitmap()?.whenDone(object : WhenDoneListener<BitmapPhoto> {
            override fun whenDone(it: BitmapPhoto?) {
                it?.let {
                    val bitmap: Bitmap = it.bitmap
                    val rotationDegrees: Int = it.rotationDegrees
                    if (mSaveMode == SAVE_ONLY_MODE) {
                        mImageGenerator?.saveInPictures(bitmap, rotationDegrees, UUID.randomUUID().toString())
                        mDialogLoader?.hideLocalLoader()
                    } else {
                        mImageGenerator?.generateCardShowTakenImageFromCamera(bitmap, getLensPosition(),
                                rotationDegrees,
                                object : CardShowTakenCompressedCallback {
                                    override fun onSuccess(bitmap: Bitmap, imageFilename: String, tempImagePath: String) {
                                        val cameraPhoto = CameraPhoto(imageFilename, tempImagePath, Date(), Date())

                                        mCameraPhotosAdapter?.addPicture(cameraPhoto)
                                        mCameraFragmentBinding.cameraPhotosRecyclerView.smoothScrollToPosition((mCameraPhotosAdapter?.itemCount
                                                ?: 0) - 1)

                                        photoPath.delete()

                                        updateCounters()

                                        mDialogLoader?.hideLocalLoader()
                                    }

                                    override fun onError() {
                                    }
                                })
                    }
                }
            }
        })
    }

    override fun returnImagesToCardShowTakenPicturesView() {
        val returnIntent = Intent()

        returnIntent.putExtra(KEY_IMAGE_CAMERA_LIST, mCameraPhotosAdapter?.list as Serializable)

        activity?.let {
            it.setResult(Activity.RESULT_OK, returnIntent)
            it.finish()
        }

        mCameraPhotos = null
    }

    override fun showPreviewPicDialog(cameraPhoto: CameraPhoto) {
        getBitmapFromFile(cameraPhoto.tempImagePathToShow, 1, object : BitmapFromFileCallback {
            override fun onBitmapDecoded(bitmap: Bitmap) {
                mCameraPhotoPreviewDialogBinding.previewImageView.setImageBitmap(bitmap)
                mPreviewPicDialog?.show()
            }

            override fun fileNotFound() {

            }
        })
    }

    override fun closePreviewPicDialog(View: View) {
        mPreviewPicDialog?.cancel()
    }

    private fun convertDpToPixels(dpValue: Int): Int {
        val roundingValue = 0.5f
        val scale         = context?.resources?.displayMetrics?.density ?: 1f

        return (dpValue * scale + roundingValue).toInt()
    }

    companion object {

        private var mPhotosLimit: Int                      = -1
        private var mImageListSize: Int                    = 0
        private var mCameraPhotos: ArrayList<CameraPhoto>? = null
        private var mIsMultipleGallerySelection            = false
        private var CAMERA_IMAGES_QUANTITY_LIMIT: Int      = 10

        const val REQUEST_IMAGE_LIST_GALLERY_RESULT: Int = 1
        private const val REQUEST_CAMERA_PERMISSION: Int = 200
        const val BUNDLE_PHOTOS: String                  = "photos"
        const val SAVE_ONLY_MODE                         = "save_only_mode"
        const val STANT_MODE                             = "stant_mode"

        fun newInstance(limitOfImages: Int?, imageListSize: Int?,
                        isMultipleGallerySelection: Boolean?, bundlePhotos: Bundle?): CameraFragment {
            mPhotosLimit   = limitOfImages ?: -1
            mImageListSize = imageListSize ?: 0

            val remainingImages = mPhotosLimit - mImageListSize

            if (isMultipleGallerySelection != null) {
                mIsMultipleGallerySelection = isMultipleGallerySelection
            }

            if (remainingImages < CAMERA_IMAGES_QUANTITY_LIMIT && isHasNotLimitOfImages(limitOfImages)) {
                CAMERA_IMAGES_QUANTITY_LIMIT = remainingImages
            }

            if (bundlePhotos != null) {
                mCameraPhotos = bundlePhotos.getSerializable(BUNDLE_PHOTOS) as ArrayList<CameraPhoto>
            }

            return CameraFragment()
        }

        private fun isHasNotLimitOfImages(limitOfImages: Int?): Boolean {
            return limitOfImages != -1
        }


    }


}
