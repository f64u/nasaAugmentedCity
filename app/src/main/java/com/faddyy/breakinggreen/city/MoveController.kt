package com.faddyy.breakinggreen.city

import com.google.ar.sceneform.ux.*

class MoveController(
    transformableNode: BaseTransformableNode,
    dragGestureRecognizer: DragGestureRecognizer,
    val arFragment: ArFragment
) : BaseTransformationController<DragGesture>(transformableNode, dragGestureRecognizer) {
    override fun onContinueTransformation(gesture: DragGesture?) {

    }

    companion object {
        private val DELTA_MULTIPLIER = -0.0002f
        private val MAX_X = MainActivity.desertDimensions.x / 2
        private val MAX_Z = MainActivity.desertDimensions.z / 2
    }

    override fun canStartTransformation(gesture: DragGesture?): Boolean {
        return transformableNode.isSelected
    }

//    override fun onContinueTransformation(gesture: DragGesture?) {
//        val localPos = transformableNode.localPosition
//        val newX =
//            MathHelper.clamp(localPos.x + (gesture!!.delta!!.x * DELTA_MULTIPLIER), -MAX_X,
//                MAX_X
//            )
//        val newZ =
//            MathHelper.clamp(localPos.z + (gesture.delta!!.z * DELTA_MULTIPLIER), -MAX_Z, -MAX_Z)
//        Log.i(MainActivity.TAG, "X: $newX, Z: $newZ")
//        localPos.x = newX
//        localPos.z = newZ
//
//        transformableNode.localPosition = localPos
//    }

    override fun onEndTransformation(gesture: DragGesture?) {
        val nodes = arFragment.arSceneView.scene.overlapTestAll(transformableNode)
        if (nodes.isNotEmpty()) {
            arFragment.arSceneView.scene.removeChild(transformableNode)
        }
    }
}