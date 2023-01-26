package com.wendorochena.poetskingdom.utils

import android.content.res.Resources
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.OutlineTypes

class ShapeAppearanceModelHelper {

    companion object {

         fun shapeImageView(imageShape: String, resources : Resources): ShapeAppearanceModel {
            val shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
            when (imageShape) {
                OutlineTypes.ROUNDED_RECTANGLE.toString() -> {
                    shapeAppearanceModel.setAllCornerSizes(resources.getDimension(R.dimen.roundedRectCornerSize))
                    return shapeAppearanceModel.build()
                }
                OutlineTypes.ROTATED_TEARDROP.toString() -> {
                    shapeAppearanceModel.setTopRightCornerSize(resources.getDimension(R.dimen.rotatedTeardropCornerSizeTopRight))
                    shapeAppearanceModel.setTopLeftCornerSize(resources.getDimension(R.dimen.rotatedTeardropCornerSizeTopLeft))
                    shapeAppearanceModel.setBottomRightCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.rotatedTeardropCornerSizeBottomRight)
                    )
                    shapeAppearanceModel.setBottomLeftCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.rotatedTeardropCornerSizeBottomLeft)
                    )
                    return shapeAppearanceModel.build()
                }
                OutlineTypes.TEARDROP.toString() -> {
                    shapeAppearanceModel.setTopRightCornerSize(resources.getDimension(R.dimen.teardropCornerSizeTopRight))
                    shapeAppearanceModel.setTopLeftCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.teardropCornerSizeTopLeft)
                    )
                    shapeAppearanceModel.setBottomRightCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.teardropCornerSizeBottomRight)
                    )
                    shapeAppearanceModel.setBottomLeftCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.teardropCornerSizeBottomLeft)
                    )
                }
                OutlineTypes.LEMON.toString() -> {
                    shapeAppearanceModel.setTopRightCornerSize(resources.getDimension(R.dimen.lemonCornerSizeTopRight))
                    shapeAppearanceModel.setTopLeftCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.lemonCornerSizeTopLeft)
                    )
                    shapeAppearanceModel.setBottomRightCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.lemonCornerSizeBottomRight)
                    )
                    shapeAppearanceModel.setBottomLeftCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.lemonCornerSizeBottomLeft)
                    )
                }
            }
            return shapeAppearanceModel.build()
        }
    }
}