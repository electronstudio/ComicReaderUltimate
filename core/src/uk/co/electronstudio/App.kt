package uk.co.electronstudio

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import net.spookygames.gdx.nativefilechooser.NativeFileChooser
import java.io.File


class App(val fileChooser: NativeFileChooser, val args: Array<String>) : Game() {

    lateinit var viewScreen: ViewScreen
    lateinit var menuScreen: MenuScreen



    override fun create() {

        Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
        Gdx.input.isCursorCatched = true
     //   val testfile = null
        val testfile = "/Volumes/Home/rich/Documents/Vuze Downloads/Buffy the Vampire Slayer - Season 10 TPBs (v01-v06)(2014-2016)(digital)/Buffy the Vampire Slayer - Season 10 v06 - Own It (2016) (Digital) (Kileko-Empire).cbr"
       val testfile2 = "/Volumes/Home/rich/Documents/Vuze Downloads/Buffy Comics Season 9 complete/Buffy the Vampire Slayer Season 9 08.cbz" // null
        val testfile3 = "/Volumes/Home/rich/Pictures/preacher snaps/vlcsnap-2017-02-10-17h40m54s771.png"
        val testfile4 = "/Volumes/Home/rich/Pictures/preacher snaps/"
        val testfile5 = "/Volumes/Home/rich/Documents/Vuze Downloads/DH - Buffy the Vampire Slayer - Season 10/Buffy the Vampire Slayer Season 10 028 (2016) (Digital).cbr"
        var prefs = Gdx.app.getPreferences("uk.co.electronstudio.comicreaderultimate")
        var fileToOpen:String? = null
        var startPage=0
        val lastFile = prefs.getString("lastFile", null)

        //fixme nasty logic
        if(lastFile!=null && File(lastFile).exists()){
            fileToOpen=lastFile
            startPage=prefs.getInteger("currentPage",0)
        }
        if(args.size>0){
            fileToOpen = args[0]
            startPage=0
        }
        viewScreen = ViewScreen(this, fileToOpen, startPage)
     //   viewScreen = ViewScreen(this, "/Volumes/Home/rich/test.cbz")
       // menuScreen = MenuScreen(this)
        screen=viewScreen
    }

    companion object {
        fun pleaseRender() {
            Gdx.graphics.isContinuousRendering = true
            Gdx.graphics.requestRendering()
        }

    }

}


