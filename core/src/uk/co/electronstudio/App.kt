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
     //   viewScreen = ViewScreen(this, if(args.size>0){args[0]}else{null})
        viewScreen = ViewScreen(this, "/Volumes/Home/rich/test.cbz")
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


