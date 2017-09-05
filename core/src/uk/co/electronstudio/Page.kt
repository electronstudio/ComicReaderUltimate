package uk.co.electronstudio

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion

class Page(private val pixmap: Pixmap) {
    var texture: TextureRegion? = null

    fun loadTexture() {
        texture?.texture?.dispose()
        val t = Texture(pixmap)
        texture = TextureRegion(t)
        texture?.flip(false, true)
    }
    fun unloadTexture(){
        texture?.texture?.dispose()
        texture=null
    }
    fun dispose(){
        texture?.texture?.dispose()
        pixmap.dispose()
    }
}