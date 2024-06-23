package com.samy.ganna.ui.main.span

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.text.style.ReplacementSpan


class CenteredBoldMarginSpan() : ReplacementSpan() {
    private val marginTop = 12
    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: FontMetricsInt?,
    ): Int {
        // Set the paint to bold for measuring the text width
        val oldStyle = paint.style
        paint.isFakeBoldText = true
        val size = paint.measureText(text, start, end).toInt()
        paint.isFakeBoldText = false // Reset the paint to its original style

        // Adjust font metrics to include the top margin

        // Adjust font metrics to include the top margin
        if (fm != null) {
            fm.ascent -= marginTop
            fm.top -= marginTop
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
        paint: Paint,
    ) {

        // Set the paint to bold for drawing the text
        val oldStyle = paint.style
        paint.isFakeBoldText = true

        // Measure the text width and calculate the offset to center it
        val textWidth = paint.measureText(text, start, end)
        val offsetX = (canvas.width - textWidth) / 2 - x

        // Draw the text with the top margin
        canvas.drawText(text, start, end, x + offsetX, (y + marginTop).toFloat(), paint)

        paint.isFakeBoldText = false // Reset the paint to its original style
    }


}


