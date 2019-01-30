package uk.co.electronstudio

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion

/**
 * Contains a pixmap (the raw image data) and optionally a texture built from the pixmap and a smaller preview texture
 * Textures can only be created on the main thread so if it hasnt done it yet these will be null
 * The preview texture is not really used currently...
 *   * it's faster to load, but the full texture loads in 20ms on modern system so hardly seems worth displaying the
 *   preview for all of 20ms.  might be on android
 *   * if the system ruhs out of vram then we should unload some textures. currently not doing this. but if we did
 *   unload, and the user tried to display the page while unloaded, then preview would be useful to display.
 *   * also might be worth caching the previews so we can instantly re-open a Comic we have read before.
 */
class Page(internal var pixmap: Pixmap?) {

    var texture: TextureRegion? = null

    var previewTexture: TextureRegion? = null


   // fun heapUsed() = pixmap.pixels.array().size

    val width: Float = pixmap?.width?.toFloat() ?: 0f
    val height: Float = pixmap?.height?.toFloat() ?: 0f


//    fun vramUsed(): Int {
//        val width = texture?.texture?.width
//        val height = texture?.texture?.height
//        var vram = 0
//        if(width != null && height != null){
//            vram += width*height*3
//        }
//        val pwidth = previewTexture?.texture?.width
//        val pheight = previewTexture?.texture?.height
//
//        if(pwidth != null && pheight != null){
//            vram += pwidth*pheight*3
//        }
//
//        return vram
//    }

    fun loadPreviewTexture(){
        val p=pixmap
        if(p==null) return
        val smallMap=Pixmap(256,(p.height*(256f/p.width.toFloat())).toInt(),Pixmap.Format.RGB888)
        smallMap.setFilter(Pixmap.Filter.BiLinear)
        p.setFilter(Pixmap.Filter.BiLinear)
        smallMap.drawPixmap(pixmap, 0, 0, p.width, p.height, 0, 0, smallMap.width, smallMap.height)
        previewTexture = TextureRegion(Texture(smallMap))
        previewTexture?.let {
            it.flip(false, true)
        }

        smallMap.dispose()



    }

    fun loadTexture() {
        val p=pixmap
        if(p==null) return
        texture?.texture?.dispose()
        val t = Texture(p, Pixmap.Format.RGB888, false)
        texture = TextureRegion(t)
        texture?.let {
           it.flip(false, true)
        }



    }
    fun unloadTexture(){
        texture?.texture?.dispose()
        texture=null
    }
    fun dispose(){
        texture?.texture?.dispose()
        pixmap?.dispose()
    }
}