package com.faddyy.breakinggreen

import com.google.ar.sceneform.math.Vector3
import com.google.gson.Gson

val gson = Gson()

class SVector3(x: Float, y: Float = 0.0f, z: Float, val isObject: Boolean = false) :
    Vector3(
        (if (isObject) OBJECT_SCALE * SCALE else SCALE) * x,
        (if (isObject) OBJECT_SCALE * SCALE else SCALE) * y,
        (if (isObject) OBJECT_SCALE * SCALE else SCALE) * z
    ) {
    companion object {
        const val SCALE = .7f
        const val OBJECT_SCALE = .4f
    }

    val original: SVector3
        get() {
            return SVector3(
                x / SCALE,
                y / SCALE,
                z / SCALE
            )
        }
}

data class Coordinates(val x: Float, val y: Float) {
    fun toSVector3() =
        SVector3(x, 0f, y)

}
