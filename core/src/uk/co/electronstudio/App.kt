package uk.co.electronstudio

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import net.spookygames.gdx.nativefilechooser.NativeFileChooser
import java.io.File
import java.util.logging.Logger


class App(val fileChooser: NativeFileChooser, val log: Logger) : Game() {

    lateinit var viewScreen: ViewScreen
    lateinit var menuScreen: MenuScreen


    var pleaseLoad: String? = null

    override fun create() {

        Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
        Gdx.input.isCursorCatched = true

        var prefs = Gdx.app.getPreferences("uk.co.electronstudio.comicreaderultimate")
        var fileToOpen:String? = null
        var startPage=0
        val lastFile = prefs.getString("lastFile", null)


        if(pleaseLoad!=null){
            fileToOpen = pleaseLoad
            startPage = 0
        } else  if(lastFile!=null && File(lastFile).exists()){
            fileToOpen=lastFile
            startPage=prefs.getInteger("currentPage",0)
        }
        log.info("creating viewscreen, pleaseLoad is $pleaseLoad")
        viewScreen = ViewScreen(this, fileToOpen, startPage)
     //   viewScreen = ViewScreen(this, "/Volumes/Home/rich/test.cbz")
        menuScreen = MenuScreen(this)
        screen=viewScreen
    }

    fun requestLoad(fileToLoad: String) {
        log.info("requestLoad $fileToLoad")
        pleaseLoad=fileToLoad

    }

    companion object {
        fun pleaseRender() {
            Gdx.graphics.isContinuousRendering = true
            Gdx.graphics.requestRendering()
        }

    }

}


