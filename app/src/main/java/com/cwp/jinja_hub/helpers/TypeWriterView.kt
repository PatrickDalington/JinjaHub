package com.cwp.jinja_hub.com.cwp.jinja_hub.helpers


import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class TypeWriterView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    private var mText: CharSequence = ""
    private var mIndex: Int = 0
    private var mDelay: Long = 150 // Default delay (milliseconds) between characters
    private val mHandler = Handler(Looper.getMainLooper())

    private val characterAdder = object : Runnable {
        override fun run() {
            text = mText.subSequence(0, mIndex)
            mIndex++
            if (mIndex <= mText.length) {
                mHandler.postDelayed(this, mDelay)
            }
        }
    }

    fun animateText(txt: CharSequence) {
        mText = txt
        mIndex = 0
        text = ""
        mHandler.removeCallbacks(characterAdder)
        mHandler.postDelayed(characterAdder, mDelay)
    }

    fun setCharacterDelay(millis: Long) {
        mDelay = millis
    }
}
