package com.faddyy.breakinggreen.city

import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

class CustomArFragment : ArFragment() {
    override fun getSessionConfiguration(session: Session?): Config {
        val config = Config(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        config.focusMode = Config.FocusMode.AUTO
        session!!.configure(config)

        arSceneView.setupSession(session)

        return config
    }
}