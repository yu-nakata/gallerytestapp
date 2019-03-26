package marutune.jp.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.GestureDetectorCompat
import android.view.*
import kotlinx.android.synthetic.main.fragment_image_viewer.*

class ImageViewerFragment: Fragment() {
    private lateinit var mScaleGestureDetector: ScaleGestureDetector
    private lateinit var mPanGestureDetector: GestureDetectorCompat

    private var mScaleFactor = 1.0f
    private var mTranslationX = 0f
    private var mTranslationY = 0f
    private var mImageWidth = 0f
    private var mImageHeight = 0f
    private var mDefaultImageWidth = 0f
    private var mDefaultImageHeight = 0f
    private var mViewPortWidth = 0f
    private var mViewPortHeight = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_image_viewer, container, false)

        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                mScaleGestureDetector.onTouchEvent(event)
                mPanGestureDetector.onTouchEvent(event)

                return true
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val byteArray = arguments!!.getByteArray("image")
        val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.count())
        mImageView.setImageBitmap(bitmap)

        mScaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        mPanGestureDetector = GestureDetectorCompat(context, PanListener())

        val viewTreeObserver = mImageView.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mImageView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val imageAspectRatio = mImageView.drawable.intrinsicHeight.toFloat() / mImageView.drawable.intrinsicWidth.toFloat()
                    val viewAspectRatio = mImageView.height.toFloat() / mImageView.width.toFloat()

                    mImageWidth = if (imageAspectRatio < viewAspectRatio) {
                        // landscape image
                        mImageView.width.toFloat()
                    } else {
                        // Portrait image
                        mImageView.height.toFloat() / imageAspectRatio
                    }

                    mImageHeight = if (imageAspectRatio < viewAspectRatio) {
                        // landscape image
                        mImageView.width.toFloat() * imageAspectRatio
                    } else {
                        // Portrait image
                        mImageView.height.toFloat()
                    }

                    mDefaultImageWidth = mImageWidth
                    mDefaultImageHeight = mImageHeight

                    mViewPortWidth = mImageView.width.toFloat()
                    mViewPortHeight = mImageView.height.toFloat()
                }
            })
        }
    }

    private inner class PanListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent, e2: MotionEvent,
            distanceX: Float, distanceY: Float
        ): Boolean {
            val translationX = mTranslationX - distanceX
            val translationY = mTranslationY - distanceY

            adjustTranslation(translationX, translationY)

            return true
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= mScaleGestureDetector.scaleFactor
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 5.0f))
            mImageView.scaleX = mScaleFactor
            mImageView.scaleY = mScaleFactor
            mImageWidth = mDefaultImageWidth * mScaleFactor
            mImageHeight = mDefaultImageHeight * mScaleFactor

            adjustTranslation(mTranslationX, mTranslationY)

            return true
        }
    }

    private fun adjustTranslation(translationX: Float, translationY: Float) {
        val translationXMargin = Math.abs((mImageWidth - mViewPortWidth) / 2)
        val translationYMargin = Math.abs((mImageHeight - mViewPortHeight) / 2)

        if (translationX < 0) {
            mTranslationX = Math.max(translationX, -translationXMargin)
        } else {
            mTranslationX = Math.min(translationX, translationXMargin)
        }

        if (mTranslationY < 0) {
            mTranslationY = Math.max(translationY, -translationYMargin)
        } else {
            mTranslationY = Math.min(translationY, translationYMargin)
        }

        mImageView.translationX = mTranslationX
        mImageView.translationY = mTranslationY
    }
}