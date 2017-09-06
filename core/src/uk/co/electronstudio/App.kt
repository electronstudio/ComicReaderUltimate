package uk.co.electronstudio

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import net.spookygames.gdx.nativefilechooser.NativeFileChooser


class App(val fileChooser: NativeFileChooser) : Game() {

    lateinit var viewScreen: ViewScreen
    lateinit var menuScreen: MenuScreen


    override fun create() {

        Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
        //  Gdx.input.isCursorCatched = true
       // viewScreen = ViewScreen(this)
        menuScreen = MenuScreen(this)

        screen=menuScreen
    }


}


