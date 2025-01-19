package com.cwp.jinja_hub.ui.edit_text

import android.content.ClipDescription
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import android.view.inputmethod.InputConnectionWrapper
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText

class CommitContentEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    var onContentReceived: ((Uri) -> Unit)? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
    }

    private companion object {
        const val MIMETYPE_IMAGE_GIF = "image/gif" // Define GIF MIME type
        const val MIMETYPE_IMAGE_JPEG = "image/jpeg" // Define JPEG MIME type
        const val MIMETYPE_IMAGE_PNG = "image/png" // Define PNG MIME type
    }

    override fun onCreateInputConnection(outAttrs: android.view.inputmethod.EditorInfo): InputConnection? {
        val inputConnection = super.onCreateInputConnection(outAttrs)
        return inputConnection?.let { CustomInputConnectionWrapper(it) }
    }

    private inner class CustomInputConnectionWrapper(
        target: InputConnection
    ) : InputConnectionWrapper(target, true) {

        @RequiresApi(Build.VERSION_CODES.N_MR1)
        override fun commitContent(
            inputContentInfo: InputContentInfo,
            flags: Int,
            opts: android.os.Bundle?
        ): Boolean {
            if (inputContentInfo.description.hasMimeType(MIMETYPE_IMAGE_GIF) ||
                inputContentInfo.description.hasMimeType(MIMETYPE_IMAGE_JPEG) ||
                inputContentInfo.description.hasMimeType(MIMETYPE_IMAGE_PNG)
            )  {
                try {
                    inputContentInfo.requestPermission()
                    val gifUri: Uri = inputContentInfo.contentUri
                    handleGif(gifUri) // Custom function to handle GIFs
                    inputContentInfo.releasePermission()
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return super.commitContent(inputContentInfo, flags, opts)
        }
    }

    private fun handleGif(uri: Uri) {
        // TODO: Add your logic to handle the GIF URI
        // Example: You can display it in an ImageView or send it to a chat message
        println("Received GIF URI: $uri")
    }

    private fun handleContent(uri: Uri) {
        onContentReceived?.invoke(uri)
    }
}
