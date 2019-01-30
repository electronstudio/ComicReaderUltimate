package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import net.spookygames.gdx.nativefilechooser.NativeFileChooserCallback
import net.spookygames.gdx.nativefilechooser.NativeFileChooserConfiguration
import kotlin.concurrent.thread

class ViewScreen(val app: App, fileToLoad: String?) : ScreenAdapter(), InputProcessor {

    private var batch: SpriteBatch = SpriteBatch() //5000, createDefaultShaderGL3())
    private var realCam: OrthographicCamera = OrthographicCamera()
    private var goalCam: OrthographicCamera = OrthographicCamera()
    val font =  BitmapFont()
    val textBatch = SpriteBatch()

    private var comic: Comic? = null

    private var scrollDown = false
    private var scrollUp = false
    private var scrollLeft = false
    private var scrollRight = false
    private var zoomIn = false
    private var zoomOut = false

    private var doublePage = false
    private var continuousScroll = false
    private val background: Color = Color.BLACK
    private val zoomSpeed = 0.04f // 0.01 - 0.10
    private val scrollSpeed = 60f
    private val zoomSens = 1.1f
    private val mouseSens = 2f
    private val mouseSmoothing = false


    init {
        //if passed comic, attempt to load it
        //else attempt to load previous comic
        if (fileToLoad != null) {
            loadComic(fileToLoad)
        } else {
            requestFile()
        }
    }

    override fun resize(width: Int, height: Int) {
        println("resize")
        super.resize(width, height)
        realCam.setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        goalCam.setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        goalCam.zoom = 1f

        realCam.update()
        goalCam.update()
        Gdx.input.inputProcessor = this

        println("CAM X "+realCam.position.x)
        println("CAM Y "+realCam.position.y)
    }

    override fun show() {
        println("show")
        super.show()
        Gdx.input.inputProcessor = this
    }


    private fun requestFile() {
        val conf = NativeFileChooserConfiguration()

        //  conf.directory = Gdx.files.absolute(System.getProperty("user.home"))

        conf.title = "Choose cbr/cbz/jpg"


        app.fileChooser.chooseFile(conf, object : NativeFileChooserCallback {
            override fun onFileChosen(file: FileHandle) {
                loadComic(file.path())
            }

            override fun onCancellation() {

            }

            override fun onError(exception: Exception) {
                throw exception
            }
        })
    }


    fun loadComic(filename: String) {
        println("loadcomic $filename")

        val c = Comic.factory(filename)

        thread(start = true) {
            println("starting pixmap load")
            var time = System.nanoTime()
            c.loadPixmaps()
            val x = ((System.nanoTime() - time) / 1000000f).toInt()
            println("loaded all pixmaps from archive in $x ms")
        }

        comic = c
    }

    override fun render(delta: Float) {
        println("render")
        Gdx.graphics.isContinuousRendering = false
        comic?.loadPreviewTexturesFromPixmaps()
        comic?.loadUnloadedTexturesFromPixmaps()
        processKeyEvents()
        moveRealCamTowardsGoalCam()
        constrainScrolling()
        draw()
    }



    private fun constrainScrolling(){

        comic?.let {
            val pageWidth = it.pages[if(continuousScroll) 0 else currentPage].width
            if(pageWidth==null) return
            val contentWidth = if(doublePage) pageWidth*2 else pageWidth //fixme wrong if one page is wider than others
            if(contentWidth/realCam.zoom < Gdx.graphics.width.toFloat()){
                realCam.position.x=contentWidth/2f
                goalCam.position.x=contentWidth/2f
            }else{
                realCam.position.x= MathUtils.clamp(  realCam.position.x, Gdx.graphics.width*realCam.zoom/2f, contentWidth-(Gdx.graphics.width/2f)*realCam.zoom)
                goalCam.position.x= MathUtils.clamp(  goalCam.position.x, Gdx.graphics.width*goalCam.zoom/2f, contentWidth-(Gdx.graphics.width/2f)*goalCam.zoom)
            }
            realCam.position.y=Math.max( realCam.position.y, Gdx.graphics.height*realCam.zoom/2f)
            goalCam.position.y=Math.max( goalCam.position.y, Gdx.graphics.height*goalCam.zoom/2f)

            //fixme add up all the page heights to find bottom scroll limit

            if(!continuousScroll){
                val pageHeight = it.pages[currentPage].height
                if(pageHeight==null) return
                if(pageHeight/realCam.zoom < Gdx.graphics.height.toFloat()){
                    realCam.position.y=pageHeight/2f
                    goalCam.position.y=pageHeight/2f
                }else {
                    realCam.position.y =
                            Math.min(realCam.position.y, pageHeight - (Gdx.graphics.height / 2f) * realCam.zoom)
                    goalCam.position.y =
                            Math.min(goalCam.position.y, pageHeight - (Gdx.graphics.height / 2f) * goalCam.zoom)
                }
               // goalCam.position.y=Math.max( goalCam.position.y, Gdx.graphics.height*goalCam.zoom/2f)
            }

        }
    }




    private fun draw() {
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)


        textBatch.begin()
        font.draw(textBatch, "${currentPage+1}/${comic?.pages?.size}", 0f, 11f);
        textBatch.end()

        goalCam.update()
        realCam.update()
        batch.projectionMatrix = realCam.combined


        batch.begin()
        comic?.let {
            if(continuousScroll){
                renderAll(it, batch, if(doublePage) 2 else 1)
            }else{
               renderSingle(it, batch)
            }
        }
        batch.end()





    }

    var currentPage=0

    fun renderSingle(comic: Comic, batch: SpriteBatch) {
        val page=comic.pages.get(currentPage)
        val tex = page.texture ?: page.previewTexture
        tex?.let {
            tex.texture?.setFilter(comic.filter, comic.filter)
            batch.draw(it, 0f, 0f, page.width, page.height)
        }
        if(doublePage && currentPage<comic.pages.lastIndex){
            val page2=comic.pages.get(currentPage+1)
            val tex2 = page2.texture ?: page2.previewTexture
            tex2?.let {
                tex2.texture?.setFilter(comic.filter, comic.filter)
                batch.draw(it, page.width, 0f, page2.width, page2.height)
            }
        }
    }

    fun renderAll(comic: Comic, batch: SpriteBatch, cols: Int) {
        var x = 0f
        var y = 0f
        var col = 0
        var c = 0
        comic.pages.forEach { page: Page ->
            val pixmap = page.pixmap
            if (pixmap == null) return@forEach
            val tex = if (realCam.zoom > 10f) page.previewTexture else (page.texture ?: page.previewTexture)
            if (tex == null) return@forEach
            tex.texture?.setFilter(comic.filter, comic.filter)
            if (realCam.frustum.sphereInFrustum(x + pixmap.width / 2,
                    y + pixmap.height / 2,
                    0f,
                    pixmap.height.toFloat())
            ) {
                batch.draw(tex, x, y, page.width, page.height)//, pixmap.width.toFloat(), pixmap.height.toFloat())
            }
            x += pixmap.width
            col++
            if (col == cols) {
                col = 0
                x = 0f
                y += pixmap.height
            }
        }

    }

    private fun processKeyEvents() {
        if (scrollLeft || scrollUp || scrollDown || scrollRight || zoomOut || zoomIn) {
            App.pleaseRender()
        }

        val xd: Float = if (scrollLeft) -scrollSpeed else if (scrollRight) scrollSpeed else 0f
        val yd: Float = if (scrollUp) -scrollSpeed else if (scrollDown) scrollSpeed else 0f
        val zd: Float = if (zoomIn) 1 - zoomSpeed else if (zoomOut) 1 + zoomSpeed else 1f

        realCam.translate(xd, yd)
        goalCam.translate(xd, yd)

        realCam.zoom = realCam.zoom * zd
        goalCam.zoom = goalCam.zoom * zd
    }


    private fun moveRealCamTowardsGoalCam() {
        if (goalCam.zoom < realCam.zoom) {
            realCam.zoom *= (1 - zoomSpeed)
            if (goalCam.zoom > realCam.zoom) realCam.zoom = goalCam.zoom
            App.pleaseRender()
        } else if (goalCam.zoom > realCam.zoom) {
            realCam.zoom *= (1 + zoomSpeed)
            if (goalCam.zoom < realCam.zoom) realCam.zoom = goalCam.zoom
            App.pleaseRender()
        }

        if (goalCam.position.y < realCam.position.y) {
            realCam.position.y -= scrollSpeed
            if (goalCam.position.y > realCam.position.y) realCam.position.y = goalCam.position.y
            App.pleaseRender()
        } else if (goalCam.position.y > realCam.position.y) {
            realCam.position.y += scrollSpeed
            if (goalCam.position.y < realCam.position.y) realCam.position.y = goalCam.position.y
            App.pleaseRender()
        }
    }

    override fun dispose() {
        batch.dispose()
    }

    val mouseHistoryX = ArrayList<Float>()
    val mouseHistoryY = ArrayList<Float>()

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val x = Gdx.input.deltaX.toFloat() * realCam.zoom * mouseSens
        val y = Gdx.input.deltaY.toFloat() * realCam.zoom * mouseSens

        if(mouseSmoothing) {
            println("mouse moved y $y")
            mouseHistoryX.add(x)
            mouseHistoryY.add(y)
            if(mouseHistoryX.size>10) mouseHistoryX.removeAt(0)
            if(mouseHistoryY.size>10) mouseHistoryY.removeAt(0)
            val averageX = mouseHistoryX.average().toFloat()
            val averageY = mouseHistoryY.average().toFloat()
            realCam.translate(averageX, averageY)
            goalCam.translate(averageX, averageY)
        }else{
            realCam.translate(x, y)
            goalCam.translate(x, y)
        }
        App.pleaseRender()
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        return true
    }

    override fun scrolled(amount: Int): Boolean {
        println(amount)

        if (amount > 0) {
            goalCam.zoom *= zoomSens
        } else {
            goalCam.zoom /= zoomSens
        }
        App.pleaseRender()
        return true

    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.DOWN -> scrollDown = false
            Input.Keys.UP -> scrollUp = false
            Input.Keys.LEFT -> scrollLeft = false
            Input.Keys.RIGHT -> scrollRight = false
            Input.Keys.EQUALS -> zoomIn = false
            Input.Keys.MINUS -> zoomOut = false
        }
        App.pleaseRender()


        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        App.pleaseRender()
        return true
    }


    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE -> {
                System.exit(0)
            }
            Input.Keys.Q -> {
                System.exit(0)
            }
            Input.Keys.DOWN -> scrollDown = true
            Input.Keys.UP -> scrollUp = true
            Input.Keys.LEFT -> scrollLeft = true
            Input.Keys.RIGHT -> scrollRight = true
            Input.Keys.EQUALS -> zoomIn = true
            Input.Keys.MINUS -> zoomOut = true
            Input.Keys.D -> doublePage = !doublePage
            Input.Keys.PAGE_DOWN -> nextPage()
            Input.Keys.PAGE_UP -> prevPage()
            Input.Keys.O -> requestFile()
            Input.Keys.SPACE -> advance()
            Input.Keys.B -> comic?.swapFilter()
            Input.Keys.C -> continuousScroll = !continuousScroll
        }
        App.pleaseRender()
        return true
    }

    fun nextPage(){
        currentPage = MathUtils.clamp(currentPage+1, 0, comic?.pages?.lastIndex ?: 0)
        moveCameraToStartPosition()
    }
    fun prevPage(){
        currentPage = MathUtils.clamp(currentPage-1, 0, comic?.pages?.lastIndex ?: 0)
        moveCameraToStartPosition()
    }

    fun moveCameraToStartPosition(){
        goalCam.position.x=Gdx.graphics.width/2f
        realCam.position.x=Gdx.graphics.width/2f
        goalCam.position.y=Gdx.graphics.height/2f
        realCam.position.y=Gdx.graphics.height/2f
    }

    fun advance(){
        if(goalCam.position.y >= ((comic?.pages?.get(currentPage)?.height ?: 0f) - (Gdx.graphics.height / 2f) * realCam.zoom)-1){
            nextPage()
        }else{
            goalCam.translate(0f, (comic?.pages?.get(currentPage)?.height ?: 0f) * 1f )//goalCam.zoom)
        }
        App.pleaseRender()
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        advance()
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        App.pleaseRender()
        return true
    }


    fun createDefaultShaderGL3(): ShaderProgram {
        val vertexShader = ("in vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" + //
                "in vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" + //
                "in vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" + //
                "uniform mat4 u_projTrans;\n" + //
                "out vec4 v_color;\n" + //
                "out vec2 v_texCoords;\n" + //
                "\n" + //
                "void main()\n" + //
                "{\n" + //
                "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" + //
                "   v_color.a = v_color.a * (255.0/254.0);\n" + //
                "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" + //
                "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" + //
                "}\n")
        val fragmentShader = ("#ifdef GL_ES\n" + //
                "#define LOWP lowp\n" + //
                "precision mediump float;\n" + //
                "#else\n" + //
                "#define LOWP \n" + //
                "#endif\n" + //
                "in LOWP vec4 v_color;\n" + //
                "in vec2 v_texCoords;\n" + //
                "out vec4 fragColor;\n" + //
                "uniform sampler2D u_texture;\n" + //
                "void main()\n" + //
                "{\n" + //
                "  fragColor = v_color * texture(u_texture, v_texCoords);\n" + //
                "}")

        ShaderProgram.prependFragmentCode = "#version 330\n"
        ShaderProgram.prependVertexCode = "#version 330\n"
        val shader = ShaderProgram(vertexShader, fragmentShader)
        if (shader.isCompiled == false) throw IllegalArgumentException("Error compiling shader: " + shader.log)
        return shader
    }


}