package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color

class Config {
    val prefs = Gdx.app.getPreferences("uk.co.electronstudio.comicreaderultimate")
    init {
        defaultPrefs()
        loadPrefs()
    }


    var doublePage = false
    var continuousScroll = false
    var background = Color.BLACK
    var zoomSpeed = 0.1f // 0.01 - 0.10
    var scrollSpeed = 60f
    var zoomSens = 1.1f
    var mouseSens = 5f
    var mouseSmoothing = false
    var quitAtEnd = true
    var spaceBarAdvanceAmount = 0.5f
    var mouseAcceleration = 1.1f // 1 to 1.99
    var showDebug = true

    fun defaultPrefs(){
        doublePage = false
        continuousScroll = false
        background = Color.BLACK
        zoomSpeed = 0.1f // 0.01 - 0.10
        scrollSpeed = 60f
        zoomSens = 1.1f
        mouseSens = 5f
        mouseSmoothing = false
        quitAtEnd = true
        spaceBarAdvanceAmount = 0.5f
        mouseAcceleration = 1.1f // 1 to 1.99
        showDebug = true
    }

    fun loadPrefs(){
        doublePage = prefs.getBoolean("doublePage", doublePage)
        continuousScroll = prefs.getBoolean("continuousScroll", continuousScroll)
        background = Color.BLACK
        zoomSpeed = prefs.getFloat("zoomSpeed", zoomSpeed)
        scrollSpeed = prefs.getFloat("scrollSpeed", scrollSpeed)
        zoomSens = prefs.getFloat("zoomSens", zoomSens)
        mouseSens = prefs.getFloat("mouseSens", mouseSens)
        mouseSmoothing = prefs.getBoolean("mouseSmoothing", mouseSmoothing)
        quitAtEnd = prefs.getBoolean("quitAtEnd", quitAtEnd)
        spaceBarAdvanceAmount = prefs.getFloat("spaceBarAdvanceAmount", spaceBarAdvanceAmount)
        mouseAcceleration = prefs.getFloat("mouseAcceleration", mouseAcceleration)
        showDebug = prefs.getBoolean("showDebug", showDebug)
    }

    fun savePrefs(){
        prefs.putBoolean("doublePage", doublePage)
        prefs.putBoolean("continuousScroll", continuousScroll)
        prefs.putFloat("zoomSpeed", zoomSpeed)
        prefs.putFloat("scrollSpeed", scrollSpeed)
        prefs.putFloat("zoomSens", zoomSens)
        prefs.putFloat("mouseSens", mouseSens)
        prefs.putBoolean("mouseSmoothing", mouseSmoothing)
        prefs.putBoolean("quitAtEnd", quitAtEnd)
        prefs.putFloat("spaceBarAdvanceAmount", spaceBarAdvanceAmount)
        prefs.putFloat("mouseAcceleration", mouseAcceleration)
        prefs.putBoolean("showDebug", showDebug)
        prefs.flush()
    }


}