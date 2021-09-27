package uk.co.electronstudio

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import net.spookygames.gdx.nativefilechooser.NativeFileChooser
import java.io.File
import java.util.logging.Logger


class App(val fileChooser: NativeFileChooser, val log: Logger) : Game() {

    lateinit var viewScreen: ViewScreen
    //lateinit var menuScreen: MenuScreen
    lateinit var config: Config


    var pleaseLoad: String? = null

    override fun create() {
        app = this

        log.info("displaymode "+Gdx.graphics.displayMode)
        Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)

        Gdx.input.isCursorCatched = false

        config = Config()
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
        /** This is the most important line in the whole program, yet it does nothing.
         * Without it, the viewscreen renders everything too small on Windows.
         * With it, rendering works.
         * No-one knows why.
         * It's not necessary for Linux.
         */
        Stage()
        setScreen(viewScreen)
    }

    fun requestLoad(fileToLoad: String) {
        log.info("requestLoad $fileToLoad")
        pleaseLoad=fileToLoad
    }

    companion object {
        lateinit var app: App
        fun pleaseRender() {
            Gdx.graphics.isContinuousRendering = true
            Gdx.graphics.requestRendering()
        }

    }

}


