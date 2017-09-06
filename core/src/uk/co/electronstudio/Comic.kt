package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import org.apache.tika.Tika
import java.io.File

abstract class Comic(val filename: String) {
     val pages = ArrayList<Page>(100)
     private var filter = Texture.TextureFilter.Linear
     val imageRegex = ".*(jpg|png|bmp|jpeg)".toRegex(RegexOption.IGNORE_CASE)


    abstract fun loadPages()
    private var loaded=0

    fun continueLoading() {
        pages.forEach{page->
            if(page.previewTexture ==null){
                page.loadPreviewTexture()
                Gdx.graphics.isContinuousRendering = true
                Gdx.graphics.requestRendering()
            }
            if(loaded<10){
                if(page.texture==null){
                    page.loadTexture()
                    loaded++
                    Gdx.graphics.isContinuousRendering = true
                    Gdx.graphics.requestRendering()
                }
            }
        }
    }

    fun render(batch: SpriteBatch,  cols:Int) {
        var x = 0f
        var y = 0f

        var col = 0


        pages.forEach{page:Page ->
            val t = page.texture ?: page.previewTexture
            t?.let {
                it.texture?.setFilter(filter, filter)
                batch.draw(it, x, y, page.pixmap.width.toFloat(), page.pixmap.height.toFloat())
                x += page.pixmap.width
                col++
                if (col == cols) {
                    col = 0
                    x = 0f
                    y += page.pixmap.height
                }
            }
        }
    }

    fun swapFilter() {
        filter = if (filter == Texture.TextureFilter.Linear) {
            Texture.TextureFilter.Nearest
        } else {
            Texture.TextureFilter.Linear
        }
    }



    companion object {
        private val tika = Tika()
        fun factory(filename: String):Comic{
            val mimeType = tika.detect(File(filename));
            println("mimetype: $mimeType")
            return when(mimeType){
                "application/x-rar-compressed" -> RarComic(filename)
                "application/zip" -> ZipComic(filename)
                else -> throw Exception("unknown file type $mimeType")
            }

        }
    }

}