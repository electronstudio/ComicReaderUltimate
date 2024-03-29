package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import org.apache.tika.Tika
import uk.co.electronstudio.App.Companion.pleaseRender
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class Comic(val filename: String) {
    abstract val pages: List<Page>
    var filter = Texture.TextureFilter.Linear

    val numThreads = Math.max(1,Runtime.getRuntime().availableProcessors()-1)


    abstract fun loadPixmaps()
    var loadedTextures=0
    var loadedPreviews=0

    fun loadUnloadedTexturesFromPixmaps() {
        var count=0
//        var time = System.nanoTime()
        pages?.forEach{page->
        //    if(loaded<10000){
                if(page.texture==null && page.pixmap !=null){
                    page.loadTexture()
                    loadedTextures++
                    count++
                    pleaseRender()
                    return
                }
         //   }
        }
//        if(count>0){
//            val x = ((System.nanoTime() - time) / 1000000f).toInt()
//            println("Loaded $count textures in $x ms")
//        }
    }

    fun loadPreviewTexturesFromPixmaps() {
        println("loadPreviewTexturesFromPixmaps")
        var count=0
        var time = System.nanoTime()
        pages.forEach{page->
            if(page.previewTexture ==null && page.pixmap !=null){
                page.loadPreviewTexture()
                loadedPreviews++
                count++
                pleaseRender()
            }
        }
        if(count>0){
            val x = ((System.nanoTime() - time) / 1000000f).toInt()
            println("Loaded $count preview textures in $x ms")
        }



        //val buffer =  ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asIntBuffer()

        //Gdx.gl.glGetIntegerv(GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX.toInt(), buffer)
        //Gdx.gl.glGetIntegerv(0x9049, buffer)
        //println("VIDEO RAM: ${buffer[0]} ${buffer[1]} ${buffer[2]} ${buffer[3]}")
//        println("VIDEO RAM: ${buffer[4]} ${buffer[5]} ${buffer[6]} ${buffer[7]}")
//        println("VIDEO RAM: ${buffer[8]} ${buffer[9]} ${buffer[10]} ${buffer[11]}")
//        println("VIDEO RAM: ${buffer[12]} ${buffer[13]} ${buffer[14]} ${buffer[15]}")

    }


//    val GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX      =    0x9047
//    val GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX  =  0x9048
//    val GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX = 0x9049
//    val GPU_MEMORY_INFO_EVICTION_COUNT_NVX         =   0x904A
//    val GPU_MEMORY_INFO_EVICTED_MEMORY_NVX         =   0x904B



    fun swapFilter() {
        filter = if (filter == Texture.TextureFilter.Linear) {
            Texture.TextureFilter.Nearest
        } else {
            Texture.TextureFilter.Linear
        }
    }

    fun dispose() {
        println("dispose")
        printFreeMemory()
        pages.forEach(){
            it.pixmap?.dispose()
            it.previewTexture?.texture?.dispose()
            it.texture?.texture?.dispose()
        }
        System.gc()
        printFreeMemory()
    }

    fun allPreviewsAreLoaded(): Boolean {
        return loadedPreviews == pages.size
    }


    companion object {

        val imageRegex = ".*(jpg|png|bmp|jpeg)".toRegex(RegexOption.IGNORE_CASE)

        fun printFreeMemory(){
            val allocatedMemory      = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
            val presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
            println("USED RAM: ${allocatedMemory/1000000} FREE RAM: ${presumableFreeMemory/1000000}")
        }

        private val tika = Tika()
        fun factory(filename: String):Comic{
            if(File(filename).isDirectory) return JpgComic(filename)
            val mimeType = tika.detect(File(filename));
            println("mimetype: $mimeType")
            return when(mimeType){
                "application/x-rar-compressed", "application/vnd.rar", "application/x-cbr" -> RarComicThreaded(filename)
                "application/zip", "application/vnd.comicbook+zip" -> ZipComicThreaded(filename)
                "image/jpeg", "image/png", "image/bmp" -> JpgComic(filename)

                else -> throw Exception("unknown file type $mimeType")
            }

        }
    }

}