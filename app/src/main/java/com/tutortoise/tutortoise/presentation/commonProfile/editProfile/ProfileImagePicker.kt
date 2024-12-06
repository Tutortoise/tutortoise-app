package com.tutortoise.tutortoise.presentation.commonProfile.editProfile

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.yalantis.ucrop.UCrop
import java.io.File

class ProfileImagePicker(
    private val activity: AppCompatActivity,
    private val onImageSelected: (Uri) -> Unit
) {
    private val galleryLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            result.data?.data?.let { uri ->
                startCropping(uri)
            }
        }
    }

    private val cropLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val uri = result.data?.let { UCrop.getOutput(it) }
            uri?.let(onImageSelected)
        }
    }

    fun launch() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun startCropping(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(
            File(activity.cacheDir, "cropped_profile_image.jpg")
        )

        val uCropIntent = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)  // Square crop
            .withMaxResultSize(500, 500)
            .getIntent(activity)

        cropLauncher.launch(uCropIntent)
    }
}