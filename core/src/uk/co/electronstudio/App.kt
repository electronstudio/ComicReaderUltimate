package uk.co.electronstudio

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import net.spookygames.gdx.nativefilechooser.NativeFileChooser


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
        viewScreen = ViewScreen(this, if(args.size>0){args[0]}else{testfile})
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


