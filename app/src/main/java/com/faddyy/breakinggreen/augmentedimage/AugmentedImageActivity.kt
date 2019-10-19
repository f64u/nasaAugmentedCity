package com.faddyy.breakinggreen.augmentedimage

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.faddyy.breakinggreen.ComponentData
import com.faddyy.breakinggreen.DemoUtils
import com.faddyy.breakinggreen.R
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import java.util.concurrent.CompletableFuture


class AugmentedImageActivity : AppCompatActivity() {
    private lateinit var arFragment: ArFragment
    private lateinit var fitToScanView: ImageView

    val componentRenderables = mutableMapOf<String, ModelRenderable>()

    private val augmentedImageMap = mutableMapOf<AugmentedImage, AugmentedImageNode>()

    companion object {
        private const val TAG = "AugmentedImageActivity"
    }


    private fun loadRenderables() {
        val componentFutures = mutableMapOf<String, CompletableFuture<ModelRenderable>>()

        for (component in ComponentData.all.keys) {
            componentFutures[component] =
                ModelRenderable.builder().setSource(this, Uri.parse("$component.sfb")).build()
        }

        CompletableFuture.allOf(*componentFutures.values.toTypedArray())
            .handle { _, throwable ->
                if (throwable != null) {
                    DemoUtils.displayError(this, "Unable to load renderable", throwable)
                    return@handle null
                }

                try {
                    for ((key, component) in componentFutures.entries) {
                        componentRenderables[key] = component.get()
                    }
                } catch (e: Exception) {
                    DemoUtils.displayError(this, "Unable to load renderables", e)
                }

                return@handle null
            }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_put)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment_id) as ArFragment
        fitToScanView = findViewById(R.id.image_view_fit_to_scan)

        loadRenderables()
        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)
    }

    private fun onUpdateFrame(frameTime: FrameTime) {
        val frame = arFragment.arSceneView.arFrame ?: return
        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
        for (augmentedImage in updatedAugmentedImages) {
            when (augmentedImage.trackingState) {
                TrackingState.PAUSED -> {
                    Log.i(TAG, "Detected image ${augmentedImage.index}")
                }

                TrackingState.TRACKING -> {
                    fitToScanView.visibility = View.GONE
                    Log.i(TAG, "Tracking image")
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        val node = AugmentedImageNode(this, componentRenderables[augmentedImage.name]!!)
                        node.image = augmentedImage
                        augmentedImageMap[augmentedImage] = node
                        arFragment.arSceneView.scene.addChild(node)
                    }
                }

                TrackingState.STOPPED -> {
                    augmentedImageMap.remove(augmentedImage)
                }
            }
        }

    }
}