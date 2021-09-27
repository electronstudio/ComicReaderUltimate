package uk.co.electronstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.scene2d.*
import ktx.actors.*
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.*


class MenuScreen(val app: App): ScreenAdapter() {
    val config = app.config
    private var stage: Stage? = null
    private var table: Table? = null
    private val window: Window

    init {
        app.log.info("menuscreen init")



        val s = Skin(Gdx.files.internal("skin/uiskin.json"))



        Scene2DSkin.defaultSkin = s



        table = table {
            button { cell ->
                // Changing button properties - button is "this":
                color = Color.CORAL
                // Changing cell properties:
                cell.fillX().row()
            }
            table {
                // Changing nested table properties:
                defaults().pad(2f)
                // Adding table children:
                label("Nested")
                label("table")
                label("of")
                label("labels.")
                // Cell of the nested actor is also available through "it":
                it.spaceBottom(10f).row()
            }
            textButton(text = "Click me!")
            // Packing the root window:
           // pack()
        }




        window = window(title = "Settings") {
//            button { cell ->
//                // Changing button properties - button is "this":
//                color = Color.CORAL
//                // Changing cell properties:
//                cell.fillX().row()
//            }
//            table {
//                // Changing nested table properties:
//                defaults().pad(2f)
//                // Adding table children:
//                label("Nested")
//                label("table")
//                label("of")
//                label("labels.")
//                // Cell of the nested actor is also available through "it":
//                it.spaceBottom(10f).row()
//            }
            textButton(text = "TOGGLE MENU [ESC]").onClick {
                app.setScreen(app.viewScreen)
            }

            textButton(text = "OPEN FILE [O]").onClick {
                app.viewScreen.requestFile()
            }
            row()
            textButton(text = "QUIT [Q]").onClick {
                app.viewScreen.quit()
            }
            textButton(text = "RESET DEFAULTS").onClick {
                config.defaultPrefs()
                app.setScreen(app.viewScreen)
            }
            row()
            label("Double pages [D]")
            checkBox(""){
                onChange {
                    config.doublePage=isChecked
                }
                isChecked=config.doublePage
            }
            row()
            label("Continuous scroll [C]")
            checkBox(""){
                onChange {
                    config.continuousScroll=isChecked
                }
                isChecked=config.continuousScroll
            }
            row()
            label("Mouse smoothing")
            checkBox(""){
                onChange {
                    config.mouseSmoothing=isChecked
                }
                isChecked=config.mouseSmoothing
            }
            row()
            label("Quit after final page")
            checkBox(""){
                onChange {
                    config.quitAtEnd=isChecked
                }
                isChecked=config.quitAtEnd
            }
            row()
            label("Low VRAM mode")
            checkBox(""){
                onChange {
                    config.lowRes=isChecked
                }
                isChecked=config.lowRes
            }
            row()
            label("Preview pages")
            checkBox(""){
                onChange {
                    config.previews=isChecked
                }
                isChecked=config.previews
            }
            row()
            label("Show debug into")
            checkBox(""){
                onChange {
                    config.showDebug=isChecked
                }
                isChecked=config.showDebug
            }
            row()
            label("Use mouse movement to zoom")
            checkBox(""){
                onChange {
                    config.useMouseMoveForZoom=isChecked
                }
                isChecked=config.useMouseMoveForZoom
            }
            row()
            label("Use mouse wheel to zoom")
            checkBox(""){
                onChange {
                    config.useMouseWheelForZoom=isChecked
                }
                isChecked=config.useMouseWheelForZoom
            }
            row()
            label("Reverse scroll direction")
            checkBox(""){
                onChange {
                    config.reverseScroll=isChecked
                }
                isChecked=config.reverseScroll
            }
            row()
            label("Mouse acceleration")
            slider(min=1f, max=2f, step=0.1f){
                onChange {
                    config.mouseAcceleration=value
                }
                value=config.mouseAcceleration
            }
            row()
            label("Amount to scroll on space bar / left click")
            slider(min=0.1f, max=1f, step=0.1f){
                onChange {
                    config.spaceBarAdvanceAmount=value
                }
                value=config.spaceBarAdvanceAmount
            }
            row()
            label("Speed of zoom animation")
            slider(min=0.01f, max=0.15f, step=0.01f){
                onChange {
                    config.zoomSpeed=value
                }
                value=config.zoomSpeed
            }
            row()
            label("Speed of scroll animation")
            slider(min=1f, max=120f, step=1f){
                onChange {
                    config.scrollSpeed=value
                }
                value=config.scrollSpeed
            }
            row()
            label("Zoom sensitivity")
            slider(min=1.01f, max=2f, step=0.01f){
                onChange {
                    config.zoomSens=value
                }
                value=config.zoomSens
            }
            row()
            label("Scroll sensitivity")
            slider(min=1f, max=150f, step=1f){
                onChange {
                    config.mouseSens=value
                }
                value=config.mouseSens
            }

            // Packing the root window:
            pack()
        }





//        val window2 = window(title = "Settings") {
//            chooser
//            pack()
//        }
//
//        chooser.setFillParent(true)
        window.setFillParent(true)
      //  window2.add(chooser)
      //  window2.pack()





        //table!!.setDebug(true) // This is optional, but enables debug lines for tables.

        // Add widgets to the table here.
    }

    override fun resize(width: Int, height: Int) {
        app.log.info("menuscreen resize $width $height")

        stage = Stage()

        stage!!.addListener(object : InputListener() {
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                when(keycode){
                    Input.Keys.ESCAPE, Input.Keys.TAB -> app.setScreen(app.viewScreen)
                    Input.Keys.Q -> app.viewScreen.quit()
                    Input.Keys.L, Input.Keys.O -> app.viewScreen.requestFile()
                }
                return super.keyDown(event, keycode)
            }
        })

        table!!.setFillParent(true)
        stage!!.addActor(window)

        Gdx.input.isCursorCatched = false
        Gdx.input.inputProcessor = stage

        //stage?.getViewport()?.update(width, height, true)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f,0f,0f,255f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage?.act(Gdx.graphics.deltaTime)
        stage?.draw()
    }

    override fun show() {
        app.log.info("menuscreen show")
    }

    override fun dispose() {
        app.log.info("menuscreen dispose")
        stage!!.dispose()
    }
}