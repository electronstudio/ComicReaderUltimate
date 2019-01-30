package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
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
    private val scrollSpeed = 20f
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
        processMouseEvents()
        constrainScrolling()
        draw()
    }



    private fun constrainScrolling(){

        comic?.let {
            val pageWidth = it.pages[0].width
            if(pageWidth==null) return
            val contentWidth = if(doublePage) pageWidth*2 else pageWidth
            if(contentWidth/realCam.zoom < Gdx.graphics.width.toFloat()){
                realCam.position.x=contentWidth/2f
            }else{
                realCam.position.x= MathUtils.clamp(  realCam.position.x, Gdx.graphics.width*realCam.zoom/2f, contentWidth-(Gdx.graphics.width/2f)*realCam.zoom)
                goalCam.position.x= MathUtils.clamp(  goalCam.position.x, Gdx.graphics.width*goalCam.zoom/2f, contentWidth-(Gdx.graphics.width/2f)*goalCam.zoom)
            }
            realCam.position.y=Math.max( realCam.position.y, Gdx.graphics.height*realCam.zoom/2f)
            goalCam.position.y=Math.max( goalCam.position.y, Gdx.graphics.height*goalCam.zoom/2f)

            //fixme add up all the page heights to find bottom scroll limit

        }
    }




    private fun draw() {
        goalCam.update()
        realCam.update()
        batch.projectionMatrix = realCam.combined
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
        comic?.let {
            render(it, batch, if(doublePage) 2 else 1)
        }
        batch.end()


        textBatch.begin()
        font.draw(textBatch, "x: ${realCam.position.x} width: ${comic?.pages?.get(0)?.width}", 10f, 10f);
        textBatch.end()


    }

    fun render(comic: Comic, batch: SpriteBatch, cols: Int) {
        var x = 0f
        var y = 0f

        var col = 0


        var c = 0
        comic.pages?.forEach { page: Page ->
            val pixmap = page.pixmap
            if (pixmap == null) return@forEach
            val tex = if (realCam.zoom > 10f) page.previewTexture else page.texture ?: page.previewTexture
            if (tex == null) return@forEach
            //val t = page.previewTexture


            tex.texture?.setFilter(comic.filter, comic.filter)
            if (realCam.frustum.sphereInFrustum(x + pixmap.width / 2,
                    y + pixmap.height / 2,
                    0f,
                    pixmap.height.toFloat())
            ) {
                batch.draw(tex, x, y, pixmap.width.toFloat(), pixmap.height.toFloat())
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


    private fun processMouseEvents() {
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
            Input.Keys.O -> requestFile()
            Input.Keys.SPACE -> goalCam.translate(0f, 1000f)
            Input.Keys.B -> comic?.swapFilter()
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
        }
        App.pleaseRender()
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        App.pleaseRender()
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