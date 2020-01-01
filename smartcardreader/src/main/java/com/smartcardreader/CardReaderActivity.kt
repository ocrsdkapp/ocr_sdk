package com.smartcardreader

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.TextureView
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.FlashMode
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureConfig
import androidx.camera.core.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.camera_view_activity.*
import kotlinx.android.synthetic.main.camera_view_activity.capture
import kotlinx.android.synthetic.main.camera_view_activity.capturedimagesList
import kotlinx.android.synthetic.main.camera_view_activity.gallery
import java.io.File

// This is an arbitrary number we are using to keep tab of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts
private const val REQUEST_CODE_PERMISSIONS = 10
private const val REQUEST_CODE_GALLERY = 11


// This is an array of all the permission specified in the manifest
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE
)

class CardReaderActivity : AppCompatActivity() {
    lateinit var arrayOfImagePath: ArrayList<String>

    var flashModeInt = 0

    // Build the image capture use case and attach button click listener
    // Create configuration object for the image capture use case
    lateinit var imageCaptureConfig: ImageCaptureConfig
    lateinit var imageCapture: ImageCapture
    var capturedImages = ArrayList<String>()
    lateinit var adapter: CapturedImagesAdapter
    lateinit var preview: Preview
    private lateinit var viewFinder: TextureView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.camera_view_activity)

        initAdapter(capturedImages, this)


        initClickListeners()
    }

    override fun onResume() {
        super.onResume()
        capture.isEnabled = true
        // Request camera permissions
        if (allPermissionsGranted()) {
            view_camera.bindToLifecycle(this@CardReaderActivity)

        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

    }

    private fun initClickListeners() {

        proceed.setOnClickListener {
            runOcr()
        }

        capture.setOnClickListener {
            capture.isEnabled = false

            if (capturedImages.size >= 2) {
                Toast.makeText(this, "Max limit of pictures at a time is 2", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")

            view_camera.takePicture(file, object : ImageCapture.OnImageSavedListener {

                override fun onError(
                    imageCaptureError: ImageCapture.ImageCaptureError,
                    message: String,
                    cause: Throwable?
                ) {
                    val msg = "Photo capture failed: $message"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    cause?.printStackTrace()
                }

                override fun onImageSaved(file: File) {
                    val path = file.path

                    Log.d("path", path)

                    CropImage.activity(Uri.fromFile(file))
                        .start(this@CardReaderActivity)

                }
            })
        }

        gallery.setOnClickListener {

            if (capturedImages.size >= 2) {
                Toast.makeText(this, "Max limit of pictures at a time is 2", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            sendGalleryForImageIntent(REQUEST_CODE_GALLERY)
        }

        flash_mode.setOnClickListener {
            when (view_camera.flash) {
                FlashMode.OFF -> {
                    view_camera.flash = FlashMode.ON
                    flash_mode.setImageResource(R.drawable.flash_on)
                }
                FlashMode.ON -> {
                    view_camera.flash = FlashMode.OFF
                    flash_mode.setImageResource(R.drawable.flash_off)
                }
            }

        }
    }

    private fun initAdapter(list: ArrayList<String>, context: Context?) {
        adapter = CapturedImagesAdapter(list, {
            //preview
//            previewImage(File(it))
        }, {
            //delete
            capturedImages.remove(it)
            adapter.notifyDataSetChanged()
            capture.isEnabled = true
        })
        capturedimagesList.adapter = adapter
        capturedimagesList.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL, false
        )
    }

    fun sendGalleryForImageIntent(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.type = "*/*"
            val mimetypes = arrayOf("image/*")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        } else {
            //|video/mp4
            intent.type = "image/*"
        }
        startActivityForResult(intent, requestCode)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                view_camera.bindToLifecycle(this@CardReaderActivity)
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                val bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri)
                capturedImages.add(File(resultUri.path).absolutePath)
                adapter.notifyDataSetChanged()

                if (adapter.list.size < 2) {
                    askForMore()
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }

        else if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            CropImage.activity(data.data)
                .start(this@CardReaderActivity)
        }
    }

    private fun runOcr() {
        if (capturedImages.isNotEmpty()) {
            CardReader(this, capturedImages)
        } else {
            Toast.makeText(
                this,
                "Provide atleast one image to proceed.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun askForMore() {
        AlertDialog.Builder(this@CardReaderActivity)
            .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }
            .setMessage("Capture back side of your card, OR press proceed to continue.")
            .show()
    }
}