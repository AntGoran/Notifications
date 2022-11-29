package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.renderscript.Sampler.Value
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var textWidth = 0f

    private var textSize: Float = resources.getDimension(R.dimen.default_text_size)
    private var circleXOffset = textSize / 2
    private var duration = 2000
    private var progressWidth = 0f
    private var progressCircle = 0f

    private var buttonTitle: String
    private var valueAnimator = ValueAnimator()

    private var buttonColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private var loadingColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    private var circleColor = ContextCompat.getColor(context, R.color.colorAccent)

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Clicked -> {
                buttonTitle = "Button clicked"
                invalidate()
            }
            ButtonState.Loading -> {
                buttonTitle = resources.getString(R.string.button_loading)
                valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                valueAnimator.setDuration(duration.toLong())

                valueAnimator.addUpdateListener { animation ->
                    progressWidth = widthSize * animation.animatedValue as Float
                    progressCircle = 360f * animation.animatedValue as Float
                    invalidate()
                }

                valueAnimator.addListener (object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        progressWidth = 0f
                        if(buttonState == ButtonState.Loading) {
                            buttonState = ButtonState.Loading
                        }
                    }
                })
                valueAnimator.start()
            }
            ButtonState.Completed -> {
                progressWidth = 0f
                progressCircle = 0f
                valueAnimator.cancel()
                buttonTitle = "Downloaded"
                invalidate()
            }
        }
    }

    init {
        buttonTitle = "Download"
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonColor = getColor(R.styleable.LoadingButton_buttonColor, 0)
            loadingColor = getColor(R.styleable.LoadingButton_buttonLoadingColor, 0)
            circleColor = getColor(R.styleable.LoadingButton_loadingCircleColor, 0)
        }

    }

    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        textSize = resources.getDimension(R.dimen.default_text_size)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Draw background color
        paint.color = buttonColor
        canvas?.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)

        // Draw progress
        paint.color = Color.DKGRAY
        canvas?.drawRect(0f, 0f, progressWidth, heightSize.toFloat(), paint)

        // Draw a text
        paint.color = Color.WHITE
        textWidth = paint.measureText(buttonTitle)
        canvas?.drawText(buttonTitle, widthSize / 2 - textWidth / 2,heightSize / 2 - (paint.descent() + paint.ascent()) / 2, paint)

        // Draw a circle
        paint.color = Color.RED
        canvas?.save()
        canvas?.translate(widthSize / 2 + textWidth / 2 + circleXOffset, heightSize / 2 - textSize / 2)
        canvas?.drawArc(RectF(0f, 0f, textSize, textSize), 0F, progressCircle, true, paint)
        canvas?.restore()

        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}