package uk.co.electronstudio

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion

class Page(internal val pixmap: Pixmap) {

    var texture: TextureRegion? = null

    var previewTexture: TextureRegion? = null



    fun loadPreviewTexture(){
        val smallMap=Pixmap(256,(pixmap.height*(256f/pixmap.width.toFloat())).toInt(),Pixmap.Format.RGB888)
        smallMap.setFilter(Pixmap.Filter.BiLinear)
        pixmap.setFilter(Pixmap.Filter.BiLinear)
        smallMap.drawPixmap(pixmap, 0, 0, pixmap.width, pixmap.height, 0, 0, smallMap.width, smallMap.height)
        previewTexture = TextureRegion(Texture(smallMap))
        previewTexture?.flip(false, true)
    }

    fun loadTexture() {
        texture?.texture?.dispose()
        val t = Texture(pixmap, Pixmap.Format.RGB888, false)
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