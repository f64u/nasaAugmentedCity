package com.faddyy.breakinggreen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.gson.Gson

/** Static utility methods to simplify creating multiple demo activities.  */
object DemoUtils {
    private const val TAG = "SceneformDemoUtils"
    private const val MIN_OPENGL_VERSION = 3.0

    /**
     * Creates and shows a Toast containing an error message. If there was an exception passed in it
     * will be appended to the toast. The error will also be written to the Log
     */
    fun displayError(
        context: Context, errorMsg: String, problem: Throwable?
    ) {
        val tag = context.javaClass.simpleName
        val toastText: String
        toastText = when {
            problem?.message != null -> {
                Log.e(tag, errorMsg, problem)
                errorMsg + ": " + problem.message
            }
            problem != null -> {
                Log.e(tag, errorMsg, problem)
                errorMsg
            }
            else -> {
                Log.e(tag, errorMsg)
                errorMsg
            }
        }

        Handler(Looper.getMainLooper())
            .post {
                val toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
    }

    /**
     * Creates an ARCore session. This checks for the CAMERA permission, and if granted, checks the
     * state of the ARCore installation. If there is a problem an exception is thrown. Care must be
     * taken to update the installRequested flag as needed to avoid an infinite checking loop. It
     * should be set to true if null is returned from this method, and called again when the
     * application is resumed.
     *
     * @param activity - the activity currently active.
     * @param installRequested - the indicator for ARCore that when checking the state of ARCore, if
     * an installation was already requested. This is true if this method previously returned
     * null. and the camera permission has been granted.
     */
    @Throws(UnavailableException::class)
    fun createArSession(activity: Activity, installRequested: Boolean): Session? {
        var session: Session? = null
        // if we have the camera permission, create the session
        if (hasCameraPermission(activity)) {
            when (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> return null
                ArCoreApk.InstallStatus.INSTALLED -> {
                }
                null -> {}
            }
            session = Session(activity)
            // IMPORTANT!!!  ArSceneView requires the `LATEST_CAMERA_IMAGE` non-blocking update mode.
            val config = Config(session)
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            session.configure(config)
        }
        return session
    }

    /** Check to see we have the necessary permissions for this app, and ask for them if we don't.  */
    fun requestCameraPermission(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(Manifest.permission.CAMERA), requestCode
        )
    }

    /** Check to see we have the necessary permissions for this app.  */
    fun hasCameraPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Check to see if we need to show the rationale for this permission.  */
    fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity, Manifest.permission.CAMERA
        )
    }

    /** Launch Application Setting to grant permission.  */
    fun launchPermissionSettings(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }

    fun handleSessionException(
        activity: Activity, sessionException: UnavailableException
    ) {

        val message: String
        when (sessionException) {
            is UnavailableArcoreNotInstalledException -> message = "Please install ARCore"
            is UnavailableApkTooOldException -> message = "Please update ARCore"
            is UnavailableSdkTooOldException -> message = "Please update this app"
            is UnavailableDeviceNotCompatibleException -> message = "This device does not support AR"
            else -> {
                message = "Failed to create AR session"
                Log.e(TAG, "Exception: $sessionException")
            }
        }
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     *
     * Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     *
     * Finishes the activity if Sceneform can not run
     */
    @SuppressLint("ObsoleteSdkInt")
    fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }

        val openGlVersionString =
            (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }
}
