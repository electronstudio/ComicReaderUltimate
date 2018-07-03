package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import org.apache.tika.Tika
import uk.co.electronstudio.App.Companion.pleaseRender
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

abstract class Comic(val filename: String) {
     val pages = CopyOnWriteArrayList<Page>()
     private var filter = Texture.TextureFilter.Linear
     val imageRegex = ".*(jpg|png|bmp|jpeg)".toRegex(RegexOption.IGNORE_CASE)


    abstract fun loadPixmaps()
    private var loaded=0

    fun loadUnloadedTexturesFromPixmaps() {
        var count=0
        var time = System.nanoTime()
        pages.forEach{page->
        //    if(loaded<10000){
                if(page.texture==null){
                    page.loadTexture()
                    loaded++
                    count++
                    pleaseRender()
                }
         //   }
        }
        if(count>0){
            val x = ((System.nanoTime() - time) / 1000000f).toInt()
            println("Loaded $count textures in $x ms")
        }
    }

    fun loadPreviewTexturesFromPixmaps() {
        var count=0
        var time = System.nanoTime()
        pages.forEach{page->
            if(page.previewTexture ==null){
                page.loadPreviewTexture()
                count++
                pleaseRender()
            }

        }
        if(count>0){
            val x = ((System.nanoTime() - time) / 1000000f).toInt()
            println("Loaded $count preview textures in $x ms")
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
                "application/x-rar-compressed", "application/vnd.rar", "application/x-cbr" -> RarComicThreaded(filename)
                "application/zip", "application/vnd.comicbook+zip" -> ZipComicThreaded(filename)
                "image/jpeg" -> JpgComic(filename)

                else -> throw Exception("unknown file type $mimeType")
            }

        }
    }

}