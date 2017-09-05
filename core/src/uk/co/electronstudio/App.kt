package uk.co.electronstudio

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx


class App : Game() {
    override fun create() {
        Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)

        Gdx.input.isCursorCatched = true
        screen=ViewScreen()
    }


}


