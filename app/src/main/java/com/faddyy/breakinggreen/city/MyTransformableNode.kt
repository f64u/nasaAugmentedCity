package com.faddyy.breakinggreen.city

import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem


class MyTransformableNode(
    transformationSystem: TransformationSystem,
    arFragment: CustomArFragment
) : TransformableNode(transformationSystem) {
    val moveController = MoveController(this, transformationSystem.dragRecognizer, arFragment)

    init {
//        translationController.isEnabled = false
        scaleController.isEnabled = false
//        rotationController.isEnabled = false
        addTransformationController(moveController)
    }
}