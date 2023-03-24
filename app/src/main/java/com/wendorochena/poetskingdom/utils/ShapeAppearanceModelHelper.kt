package com.wendorochena.poetskingdom.utils

import android.content.res.Resources
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.OutlineTypes

class ShapeAppearanceModelHelper {

    companion object {

         fun shapeImageView(imageShape: String, resources : Resources, cornerSize : Float): ShapeAppearanceModel {
            val shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
             println()
            when (imageShape) {
                OutlineTypes.ROUNDED_RECTANGLE.toString() -> {
                    shapeAppearanceModel.setAllCornerSizes(cornerSize)
                    return shapeAppearanceModel.build()
                }
                OutlineTypes.ROTATED_TEARDROP.toString() -> {
                    shapeAppearanceModel.setTopRightCornerSize(resources.getDimension(R.dimen.rotatedTeardropCornerSizeTopRight) - cornerSize)
                    shapeAppearanceModel.setTopLeftCornerSize(resources.getDimension(R.dimen.rotatedTeardropCornerSizeTopLeft))
                    shapeAppearanceModel.setBottomRightCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.rotatedTeardropCornerSizeBottomRight) - cornerSize
                    )
                    shapeAppearanceModel.setBottomLeftCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.rotatedTeardropCornerSizeBottomLeft) - cornerSize
                    )
                    return shapeAppearanceModel.build()
                }
                OutlineTypes.TEARDROP.toString() -> {
                    shapeAppearanceModel.setTopRightCornerSize(resources.getDimension(R.dimen.teardropCornerSizeTopRight))
                    shapeAppearanceModel.setTopLeftCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.teardropCornerSizeTopLeft) + cornerSize
                    )
                    shapeAppearanceModel.setBottomRightCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.teardropCornerSizeBottomRight) + cornerSize
                    )
                    shapeAppearanceModel.setBottomLeftCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.teardropCornerSizeBottomLeft) + cornerSize
                    )
                }
                OutlineTypes.LEMON.toString() -> {
                    shapeAppearanceModel.setTopRightCornerSize(resources.getDimension(R.dimen.lemonCornerSizeTopRight))
                    shapeAppearanceModel.setTopLeftCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.lemonCornerSizeTopLeft) - cornerSize
                    )
                    shapeAppearanceModel.setBottomRightCorner(
                        CornerFamily.ROUNDED,
                        resources.getDimension(R.dimen.lemonCornerSizeBottomRight) - cornerSize
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