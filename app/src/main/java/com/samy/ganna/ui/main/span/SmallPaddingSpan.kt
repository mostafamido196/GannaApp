package com.samy.ganna.ui.main.span
import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan
import android.graphics.Typeface

class SmallPaddingSpan(
    private val textSize: Int,
    private val paddingBottom: Int,
    private val textColor: Int,
) : ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        // Save the original text size and set the new text size
        val originalTextSize = paint.textSize
        paint.textSize = textSize.toFloat()

        // Measure the text width
        val size = paint.measureText(text, start, end).toInt()

        // Reset the text size
        paint.textSize = originalTextSize

        // Do not adjust font metrics to avoid adding margin bottom
        fm?.let {
            val originalDescent = it.descent
            val originalBottom = it.bottom

            it.descent = originalDescent
            it.bottom = originalBottom
        }

        return size
    }


    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        // Save the original text size, color, and typeface, then set the new text size, color, and typeface
        val originalTextSize = paint.textSize
        val originalColor = paint.color
        val originalTypeface = paint.typeface

        paint.textSize = textSize.toFloat()
        paint.color = textColor
        paint.typeface = Typeface.create(originalTypeface, Typeface.NORMAL)

        // Draw the text with padding bottom
        canvas.drawText(text, start, end, x, y.toFloat() - paddingBottom, paint)

        // Reset the paint to its original style
        paint.textSize = originalTextSize
        paint.color = originalColor
        paint.typeface = originalTypeface
    }
}
/*

class SmallPaddingSpan(
    private val textSize: Int,
    private val paddingBottom: Int,
    private val textColor: Int,
    onclick: () -> Unit
) : ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        // Save the original text size and set the new text size
        val originalTextSize = paint.textSize
        paint.textSize = textSize.toFloat()

        // Measure the text width
        val size = paint.measureText(text, start, end).toInt()

        // Reset the text size
        paint.textSize = originalTextSize

        // Do not adjust font metrics to avoid adding margin bottom
        fm?.let {
            val originalDescent = it.descent
            val originalBottom = it.bottom

            it.descent = originalDescent
            it.bottom = originalBottom
        }

        return size
    }


    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        // Save the original text size, color, and typeface, then set the new text size, color, and typeface
        val originalTextSize = paint.textSize
        val originalColor = paint.color
        val originalTypeface = paint.typeface

        paint.textSize = textSize.toFloat()
        paint.color = textColor
        paint.typeface = Typeface.create(originalTypeface, Typeface.NORMAL)

        // Draw the text with padding bottom
        canvas.drawText(text, start, end, x, y.toFloat() - paddingBottom, paint)

        // Reset the paint to its original style
        paint.textSize = originalTextSize
        paint.color = originalColor
        paint.typeface = originalTypeface
    }
}
*/
