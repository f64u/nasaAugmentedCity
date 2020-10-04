package com.faddyy.breakinggreen.augmentedimage

import android.app.ActivityManager
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import com.faddyy.breakinggreen.ComponentData
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.io.IOException


/**
 * Extend the ArFragment to customize the ARCore session configuration to include Augmented Images.
 */
class AugmentedImageFragment : ArFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
        }

        val openGlVersionString =
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
        return view
    }

    override fun getSessionConfiguration(session: Session): Config {
        val config = super.getSessionConfiguration(session)
        if (!setupAugmentedImageDatabase(config, session)) {
            Log.e(TAG, "Could not setup augmented image database")
        }
        config.focusMode = Config.FocusMode.AUTO
        return config
    }

    private fun setupAugmentedImageDatabase(config: Config, session: Session): Boolean {
        var augmentedImageDatabase: AugmentedImageDatabase? = null

        val assetManager = if (context != null) context!!.assets else null
        if (assetManager == null) {
            Log.e(TAG, "Context is null, cannot initialize image database.")
            return false
        }

        if (LOAD_IMAGES) {
            augmentedImageDatabase = AugmentedImageDatabase(session)
            val assets = assetManager.list("ad")
            for (componentData in ComponentData.all) {
                if (!assets.contains("${componentData.key}.png")) continue
                val bitmap = loadAugmentedImageBitmap(assetManager, "ad/${componentData.key}.png") ?: return false
                augmentedImageDatabase.addImage(componentData.key, bitmap)
                Log.i(TAG, "added image")
            }
        } else {
            // This is an alternative way to initialize an AugmentedImageDatabase instance,
            // load a pre-existing augmented image database.
            try {
                context!!.assets.open(SAMPLE_IMAGE_DATABASE).use { `is` ->
                    augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, `is`)
                }
            } catch (e: IOException) {
                Log.e(TAG, "IO exception loading augmented image database.", e)
                return false
            }

        }

        config.augmentedImageDatabase = augmentedImageDatabase
        return true
    }

    private fun loadAugmentedImageBitmap(assetManager: AssetManager, assetName: String): Bitmap? {
        try {
            assetManager.open(assetName)
                .use { `is` -> return BitmapFactory.decodeStream(`is`) }
        } catch (e: IOException) {
            Log.e(TAG, "IO exception loading augmented image bitmap.", e)
        }

        return null
    }

    companion object {
        private val TAG = "AugmentedImageFragment"

        // This is a pre-created database containing the sample image.
        private val SAMPLE_IMAGE_DATABASE = "sample_database.imgdb"

        // Augmented image configuration and rendering.
        // Load a single image (true) or a pre-generated image database (false).
        private val LOAD_IMAGES = false


        private val MIN_OPENGL_VERSION = 3.0
    }
}