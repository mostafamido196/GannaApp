package com.samy.ganna.ui.main.span

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.ReplacementSpan
import androidx.core.content.res.ResourcesCompat

class CustomSpan(
    private val context: Context,
    private val paddingTop: Int,
    private val paddingBottom: Int,
    private val textSize: Float, // Text size in sp
    private val fontResId: Int, // Font resource ID from res/font
    private val isBold: Boolean, // Flag to indicate bold text
    private val textColor: Int // Text color
) : ReplacementSpan() {

    private val textPaint: TextPaint = TextPaint().apply {
        isAntiAlias = true
        this.textSize = this@CustomSpan.textSize * context.resources.displayMetrics.scaledDensity
        // Set the custom text color
        this.color = textColor
        // Load custom font from resources
        val typeface = ResourcesCompat.getFont(context, fontResId)
        typeface?.let {
            this.typeface = it
            if (isBold) {
                this.typeface = Typeface.create(it, Typeface.BOLD)
            }
        }
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        // Update the font metrics
        fm?.let {
            val originalAscent = fm.ascent
            val originalDescent = fm.descent

            fm.ascent = (originalAscent - paddingTop).toInt()
            fm.descent = (originalDescent + paddingBottom).toInt()

            fm.top = fm.ascent
            fm.bottom = fm.descent
        }
        return textPaint.measureText(text, start, end).toInt()
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
        // Measure the text width and calculate the offset to center it
        val textWidth = textPaint.measureText(text, start, end)
        val offsetX = (canvas.width - textWidth) / 2 - x
        val newY = y + paddingTop - paddingBottom / 2 // Adjust y-coordinate for padding
        canvas.drawText(text, start, end, x + offsetX, newY.toFloat(), textPaint)
    }
}
