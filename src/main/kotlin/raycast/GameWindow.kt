package raycast

import raycast.manager.AssetsManager
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JFrame
import kotlin.concurrent.thread

class GameWindow: JFrame(), KeyListener {
    companion object {
        const val WINDOW_W = 1400
        const val WINDOW_H = 800
        const val FOV = 70.0
        const val FPS = 60.0
    }

    private val keyBuffer = arrayListOf<Int>()
    private lateinit var level: Level

    private var weaponAnimation = 0
    private var weaponAnimationInvert = false

    fun start() {
        name = "Raycast"
        size = Dimension(WINDOW_W, WINDOW_H)
        isUndecorated = true
        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true

        addKeyListener(this)

        AssetsManager.loadAll()
        level = AssetsManager.getLevel("test1")

        level.addCamera(Player(Size(WINDOW_W, WINDOW_H), level, 0))
        level.addCamera(Camera(Size(400, 600), level, 1))

        startRender()
        startInput()

        thread {
            while (true) {
                Thread.sleep(1000 / 6)
                level.getCamera(1).rotateOffset(1.0)
            }
        }
    }

    private fun startRender() = thread {
        while(isVisible) {
            Thread.sleep((1000 / FPS).toLong())
            Toolkit.getDefaultToolkit().sync()

            graphics?.let { render(it) }
            thread {
                level.update()
            }
        }
    }

    private fun startInput() = thread {
        val map = level.getImage()

        while(isVisible) {
            Thread.sleep((1000 / 40).toLong())

            (keyBuffer.clone() as List<Int>).forEach {
                when(it) {
                    37 -> { // LEFT
                        level.getPlayer().rotateOffset(-0.1)
                    }

                    39 -> { // RIGHT
                        level.getPlayer().rotateOffset(0.1)
                    }

                    38 -> { // UP
                        val dirX = Math.cos(level.getPlayer().getAngle()) * level.getPlayer().getSpeed()
                        val dirY = Math.sin(level.getPlayer().getAngle()) * level.getPlayer().getSpeed()
                        val x = (level.getPlayer().getX() + dirX)
                        val y = (level.getPlayer().getY() + dirY)
                        val id = Color(map.getRGB(x.toInt(), y.toInt())).red

                        // is in world
                        if(x > 0 && y > 0 && y < map.height && x < map.width) {
                            if(id == 0) {
                                level.getPlayer().walk()

                                if(weaponAnimationInvert)
                                    weaponAnimation -= 1
                                else
                                    weaponAnimation += 1

                                if(weaponAnimation > 10 || weaponAnimation < 1) {
                                    weaponAnimationInvert = !weaponAnimationInvert
                                }
                            }
                        }
                    }

                    40 -> { // DOWN

                    }

                    32 -> { // SPACE
                        val hit = level.getPlayer().findHit(100.0)
                        if(hit != null) level.set(hit, 0)
                        keyBuffer.remove(it) // remove key
                    }

                    else -> {
                        println("Unknown key $it")
                        keyBuffer.remove(it)
                    }
                }
            }
        }
    }

    private fun render(g: Graphics) {
        renderWorld(g)
        // renderMap(g)
    }

    private fun renderWorld(g: Graphics) {
        val cameraImage = level.getPlayer().render()
        renderUi(cameraImage.graphics)

        g.drawImage(cameraImage, 0, 0, width, height, null)
    }

    private fun renderUi(g: Graphics) {
        val map = AssetsManager.getLevel("test1").getImage()

        // draw minimap
        for(x in 0 until map.width) {
            for(y in 0 until map.height) {
                val id = Color(map.getRGB(x, y)).red

                g.color = when(id) {
                    1 -> Color.RED
                    2 -> Color.BLUE

                    else -> Color.BLACK
                }
                g.fillRect(x, y, 1, 1)
            }
        }

        // calc
        val pos = level.getPlayer().getPos()
        val rayX = pos.x + Math.cos(level.getPlayer().getAngle()) * 10
        val rayY = pos.y + Math.sin(level.getPlayer().getAngle()) * 10
        val center = Pos((width / 2).toDouble(), (height / 2).toDouble())

        // draw player pos
        g.color = Color.WHITE
        g.drawOval(((pos.x - 5).toInt()), ((pos.y - 5).toInt()), 10, 10)
        g.drawLine(pos.x.toInt(), pos.y.toInt(), rayX.toInt(), rayY.toInt())

        // draw crosshair
        g.color = Color.WHITE
        g.drawLine((center.x - 4).toInt(), center.y.toInt(), (center.x + 4).toInt(), center.y.toInt())
        g.drawLine(center.x.toInt(), (center.y - 4).toInt(), center.x.toInt(), (center.y + 4).toInt())

        // draw weapon
        val img = AssetsManager.getImage("portalgun")
        val w = img.width * 2
        val h = img.height * 2
        g.drawImage(img, width - w, (height - h) + (weaponAnimation / 2), w, h, null, null)
    }

    override fun keyTyped(p0: KeyEvent) { }

    override fun keyPressed(p0: KeyEvent) {
        if(!keyBuffer.contains(p0.keyCode))
            keyBuffer.add(p0.keyCode)
    }

    override fun keyReleased(p0: KeyEvent) {
        keyBuffer.remove(p0.keyCode)
    }
}