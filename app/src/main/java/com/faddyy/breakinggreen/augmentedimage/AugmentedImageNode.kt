package com.faddyy.breakinggreen.augmentedimage

import android.content.Context
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import java.util.concurrent.CompletableFuture


class AugmentedImageNode(context: Context,val mRenderable: ModelRenderable) : AnchorNode() {

    var image: AugmentedImage? = null
        set(image) {
            field = image

            anchor = image!!.createAnchor(image.centerPose)

            val centerNode = Node()
            centerNode.setParent(this)
            centerNode.renderable = mRenderable
            val oldScale = centerNode.localScale

            val widthRatio = image.extentX / oldScale.x
            val heightRatio = image.extentZ / oldScale.z
            if (image.extentX > image.extentZ) {
                centerNode.localScale = Vector3(heightRatio * oldScale.x, heightRatio * oldScale.y, heightRatio * oldScale.z)
            } else {
                centerNode.localScale = Vector3(widthRatio * oldScale.x, widthRatio * oldScale.y, widthRatio * oldScale.z)
            }
        }


    companion object {

        private val TAG = "AugmentedImageNode"

    }
}