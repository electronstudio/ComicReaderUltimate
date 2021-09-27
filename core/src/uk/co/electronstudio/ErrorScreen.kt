package uk.co.electronstudio


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage



class ErrorScreen(val app: App, val title: String?, val message: String?): ScreenAdapter() {

    private var batch: SpriteBatch = SpriteBatch()
    val font = BitmapFont()
    val fixedFont = BitmapFont()

    val help =
            """
| Function          | Keys        |
| ----------------  | ----        |
| Quit              | Q           |
| Open file         | O, L        |
| Menu              | Escape, Tab |
|                   |             |
| Scroll            | Cursor keys |
| First page        | Home        |
| Last page         | End         |
| Next page         | Page Down   |
| Prev page         | Page Up     |
| Next screen       | Space       |
|                   |             |
| Zoom in           | +           |
| Zoom out          | -           |
| Zoom to fit       | Z           |
| Zoom default      | R           |
|                   |             |
| Double pages      | D           |
| Bilinear filter   | B           |
| Continuous scroll | C           |
            """.trimIndent()

    init {
        fixedFont.setFixedWidthGlyphs("ABCDEFGHIJKLMNOPQRSTUVWXYZ-abcdefghijklmnopqrstuvwxyz ,+-")
    }
        override fun show()
    {
        app.log.info("errorscreen show")
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun keyDown(keycode: Int): Boolean {
                app.setScreen(MenuScreen(app))
                return true
            }


        }
    }

    override fun hide() {
        app.log.info("errorscreen hide")
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        font.setColor(Color.WHITE)
        title?.let {
            font.draw(batch, it, 50f,  Gdx.graphics.height.toFloat()-100f);
        }
        font.setColor(Color.GRAY)
        message?.let {
            font.draw(batch, it, 50f,  Gdx.graphics.height.toFloat()-200f);
        }
        font.setColor(Color.WHITE)
        fixedFont.draw(batch, help, Gdx.graphics.width/2f, Gdx.graphics.height.toFloat()-100f);

        batch.end()

    }
}