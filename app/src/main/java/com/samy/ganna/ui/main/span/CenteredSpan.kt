package com.samy.ganna.ui.main.span
import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan
import android.graphics.Paint.FontMetricsInt


class CenteredSpan : ReplacementSpan() {
    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: FontMetricsInt?
    ): Int {
        return paint.measureText(text, start, end).toInt()
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
        val textWidth = paint.measureText(text, start, end)
        val offsetX = (canvas.width - textWidth) / 2 - x
        canvas.drawText(text, start, end, x + offsetX, y.toFloat(), paint)
    }
}
