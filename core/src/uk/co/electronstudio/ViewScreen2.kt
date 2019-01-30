package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import net.spookygames.gdx.nativefilechooser.NativeFileChooserCallback
import net.spookygames.gdx.nativefilechooser.NativeFileChooserConfiguration
import kotlin.concurrent.thread

class ViewScreen2(val app: App, fileToLoad: String?): ScreenAdapter(), InputProcessor {


    private var batch: SpriteBatch = SpriteBatch()

    private val zoomSens = 1.1f
    private val mouseSens = 2f

    private var scrollDown = false
    private var scrollUp = false
    private var scrollLeft = false
    private var scrollRight = false
    private var zoomIn = false
    private var zoomOut = false

    private var cam: OrthographicCamera = OrthographicCamera()



    //var comic = Comic("/Volumes/Home/rich/test.cbz")
    private var comic: Comic? = null


    private val cols = 1

    private val background: Color = Color.BLACK

    private val zoomSpeed = 0.04f // 0.01 - 0.10

    private val scrollSpeed = 20f

    var x=0f
    var y=0f
    var zoom=1f


    init {





        cam.setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
      //  cam.translate(Gdx.graphics.width.toFloat()/2, Gdx.graphics.height.toFloat()/2)

        //    Gdx.graphics.isContinuousRendering = false;
        //   Gdx.graphics.requestRendering();

        Gdx.input.inputProcessor = this

        cam.update()



        //if passed comic, attempt to load it
        //else attempt to load previous comic

        if(fileToLoad!=null){
            loadComic(fileToLoad)
        }

        //if fail...
      //  requestFile()
    }

    private fun requestFile() {
        val conf = NativeFileChooserConfiguration()

      //  conf.directory = Gdx.files.absolute(System.getProperty("user.home"))

        conf.title = "Choose cbr/cbz"


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


    fun loadComic(filename:String){
        println("loadcomic $filename")

        val c= Comic.factory(filename)

        thread(start = true) {
            println("starting")
            c.loadPixmaps()
            println("loaded")
        }

        comic = c
    }

    override fun render(delta: Float) {
       // Gdx.graphics.isContinuousRendering = false
        comic?.loadUnloadedTexturesFromPixmaps()
        processOngoingKeyEvents()
        updateCameras()
        draw()
    }

    private fun draw() {
        cam.update()
        batch.projectionMatrix = cam.combined

        batch.transformMatrix= Matrix4().
              //  translate(-x,-y,0f).
                translate(Gdx.graphics.width/2f, Gdx.graphics.height/2f,0f).
                scale(zoom,zoom,1f).
                translate(-Gdx.graphics.width/2f, -Gdx.graphics.height/2f,0f).
                translate(-x,-y,0f)
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
       // comic?.render(batch, cols)
        batch.end()
    }

    private fun processOngoingKeyEvents() {
        if (scrollLeft || scrollUp || scrollDown || scrollRight || zoomOut || zoomIn) {
            Gdx.graphics.isContinuousRendering = true
        }

        val xd: Float = if (scrollLeft) -scrollSpeed else if (scrollRight) scrollSpeed else 0f
        val yd: Float = if (scrollUp) -scrollSpeed else if (scrollDown) scrollSpeed else 0f
        val zd: Float = if (zoomIn) 1 - zoomSpeed else if (zoomOut) 1 + zoomSpeed else 1f

        zoom*=zd
        x+=xd
        y+=yd



    }


    private fun updateCameras() {

//        println(goalCam.position.x)
//        if(comic!!.pages[0].texture!!.regionWidth> Gdx.graphics.width) {
//            if (goalCam.position.x < goalCam.zoom * Gdx.graphics.width / 2f) {
//                goalCam.position.x = goalCam.zoom * Gdx.graphics.width / 2f
//                realCam.position.x = goalCam.zoom * Gdx.graphics.width / 2f
//            }
//        }
//
//
//        if (goalCam.zoom < realCam.zoom) {
//            realCam.zoom *= (1 - zoomSpeed)
//            if (goalCam.zoom > realCam.zoom) realCam.zoom = goalCam.zoom
//            Gdx.graphics.isContinuousRendering = true
//        } else if (goalCam.zoom > realCam.zoom) {
//            realCam.zoom *= (1 + zoomSpeed)
//            if (goalCam.zoom < realCam.zoom) realCam.zoom = goalCam.zoom
//            Gdx.graphics.isContinuousRendering = true
//        }
//
//        if (goalCam.position.y < realCam.position.y) {
//            realCam.position.y -= scrollSpeed
//            if (goalCam.position.y > realCam.position.y) realCam.position.y = goalCam.position.y
//            Gdx.graphics.isContinuousRendering = true
//        } else if (goalCam.position.y > realCam.position.y) {
//            realCam.position.y += scrollSpeed
//            if (goalCam.position.y < realCam.position.y) realCam.position.y = goalCam.position.y
//            Gdx.graphics.isContinuousRendering = true
//        }
//
//        if (goalCam.position.x < realCam.position.x) {
//            realCam.position.x -= scrollSpeed
//            if (goalCam.position.x > realCam.position.x) realCam.position.x = goalCam.position.x
//            Gdx.graphics.isContinuousRendering = true
//        } else if (goalCam.position.x > realCam.position.x) {
//            realCam.position.x += scrollSpeed
//            if (goalCam.position.x < realCam.position.x) realCam.position.x = goalCam.position.x
//            Gdx.graphics.isContinuousRendering = true
//        }
    }
    override fun dispose() {
        batch.dispose()
        //   img.dispose()
    }

    /**
     * ignores the dual camera setup and just moves immediately so there's no lag
     */
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val xd = Gdx.input.deltaX.toFloat()  * mouseSens
        val yd = Gdx.input.deltaY.toFloat()  * mouseSens
        x+=xd
        y+=yd
        Gdx.graphics.isContinuousRendering = true
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        return true
    }

    override fun scrolled(amount: Int): Boolean {
        println(amount)

        if (amount > 0) {
            zoom *= zoomSens
        } else {
            zoom /= zoomSens
        }
        Gdx.graphics.isContinuousRendering = true
        return true

    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.O -> requestFile()
           // Input.Keys.SPACE -> goalCam.translate(0f, 1000f)
            Input.Keys.B -> comic?.swapFilter()
            Input.Keys.DOWN -> scrollDown = false
            Input.Keys.UP -> scrollUp = false
            Input.Keys.LEFT -> scrollLeft = false
            Input.Keys.RIGHT -> scrollRight = false
            Input.Keys.EQUALS -> zoomIn = false
            Input.Keys.MINUS -> zoomOut = false
        }
        Gdx.graphics.isContinuousRendering = true


        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        Gdx.graphics.isContinuousRendering = true
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
            Input.Keys.LEFT -> {
                println("left")
                scrollLeft = true
            }
            Input.Keys.RIGHT -> scrollRight = true
            Input.Keys.EQUALS -> zoomIn = true
            Input.Keys.MINUS -> zoomOut = true
        }
        Gdx.graphics.isContinuousRendering = true
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        Gdx.graphics.isContinuousRendering = true
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        Gdx.graphics.isContinuousRendering = true
        return true
    }





}