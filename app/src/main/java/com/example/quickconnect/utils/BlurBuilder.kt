package com.example.quickconnect.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur


object BlurBuilder {
    private const val BLUR_RADIUS = 25f
    fun blur(context: Context, image: Bitmap): Bitmap {
        val width = Math.round(image.width.toFloat())
        val height = Math.round(image.height.toFloat())
        val inputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(inputBitmap)
        canvas.drawBitmap(image, 0f, 0f, null)
        val rs = RenderScript.create(context)
        val allocationIn = Allocation.createFromBitmap(rs, inputBitmap)
        val allocationOut = Allocation.createTyped(rs, allocationIn.type)
        val blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        blur.setInput(allocationIn)
        blur.setRadius(BLUR_RADIUS)
        blur.forEach(allocationOut)
        allocationOut.copyTo(inputBitmap)
        rs.destroy()
        return inputBitmap
    }
}