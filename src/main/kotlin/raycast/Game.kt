import raycast.AssetsManager
import raycast.Bound
import raycast.Player
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.image.BufferedImage
import javax.swing.JFrame
import kotlin.concurrent.thread

class Game: JFrame(), KeyListener, MouseMotionListener {
    companion object {
        const val rayStepSize = 0.5
        const val cellSize = 20
        const val fov = 70
        const val fps = 60

        const val DRAW_MAP = false

        val screenW = (cellSize * map[0].size)
        val screenH = (cellSize * map.size)
    }

    private val gradientImage: BufferedImage = BufferedImage(1000, 600, BufferedImage.TYPE_INT_RGB)
        .apply {
            var y = height
            var colorH = 0.0
            while(y > height / 2) {
                this.graphics?.let {
                    it.color = Color(
                        72 - (colorH.toInt() / 2),
                        72 - (colorH.toInt() / 2),
                        72 - (colorH.toInt() / 2)
                    )
                    it.drawRect(0, y, width, 1)
                }

                if(y % 2 == 0)
                    colorH += 0.5
                y -= 1
            }

            y = height / 2
            colorH = 0.0
            while(y > 0) {
                this.graphics?.let {
                    it.color = Color( // 80,130,255
                        80 - (colorH.toInt() / 2),
                        130 - (colorH.toInt() / 2),
                        255 - (colorH.toInt() / 2)
                    )
                    it.drawRect(0, y, width, 1)
                }

                if(y % 2 == 0)
                    colorH += 0.5
                y -= 1
            }
        }

    private val keyBuffer = arrayListOf<Int>()
    private var mouseMoveX = 0
    private var mouseMoveY = 0

    private val player = Player(screenW, screenH)

    private var weaponAnimation = 0
    private var weaponAnimationInvert = false

    fun start() {
        name = "Raycast Engine"
        size = Dimension(1000, 600)
        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true

        addKeyListener(this)
        addMouseMotionListener(this)

        AssetsManager.loadAll()

        thread {
            while (true) {
                Thread.sleep(1000 / fps.toLong())
                Toolkit.getDefaultToolkit().sync()

                drawAll()
            }
        }

        thread {
            while (true) {
                Thread.sleep(1000 / 40)

                input()
            }
        }
    }

    fun input() {
        if(mouseMoveX < 0) { // RIGHT by Mouse
            mouseMoveX = 0
            player.rotateOffset(0.1)
        }

        if(mouseMoveX > 0) { // LEFT by Mouse
            mouseMoveX = 0
            player.rotateOffset(-0.1)
        }

        (keyBuffer.clone() as List<Int>).forEach {
            when(it) {
                37 -> { // LEFT
                    player.rotateOffset(-0.1)
                }

                39 -> { // RIGHT
                    player.rotateOffset(0.1)
                }

                38 -> { // UP
                    val dirX = Math.cos(player.getAngle()) * player.getSpeed()
                    val dirY = Math.sin(player.getAngle()) * player.getSpeed()
                    val x = (player.getX() + dirX.toInt()) / cellSize
                    val y = (player.getY() + dirY.toInt()) / cellSize

                    // is in world
                    if(x > 0 && y > 0 && y < map.size && x < map[0].size) {
                        if(map[y][x] == 0) {
                            player.moveUp()

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

                else -> {
                    keyBuffer.remove(it)
                    println("Unknown key pressed: $it")
                }
            }
        }
    }

    fun drawAll() {
        player.update()

        drawBackground()
        drawWorld()
        drawGUI()
    }

    fun drawBackground() {
        graphics?.drawImage(gradientImage, 0, 0, null)
    }

    fun drawGUI() {
        // draw map rect
        if(DRAW_MAP)
        graphics?.let {
            it.color = Color(255, 255, 255, 120)
            it.fillRect(0, 0, screenW / 2, screenH / 2)
        }

        // draw map
        if(DRAW_MAP)
        for(y in 0 until map.size) {
            for(x in 0 until map[y].size) {
                if(map[y][x] > 0 && map[y][x] < ColorMap.values().size) {
                    val color = ColorMap.values()[map[y][x]].color

                    graphics?.let {
                        it.color = color
                        it.fillRect((x * cellSize) / 2, (y * cellSize) / 2, cellSize / 2, cellSize / 2)
                    }
                }
            }
        }

        // draw player on map
        if(DRAW_MAP)
        graphics?.let {
            it.color = Color.RED
            it.drawOval((player.getX() / 2) - 5, (player.getY() / 2) - 5, 10, 10)

            val rayX = player.getX() + Math.cos(player.getAngle()) * 20
            val rayY = player.getX() + Math.sin(player.getAngle()) * 20
            it.drawLine(player.getX() / 2, player.getY() / 2, rayX.toInt() / 2, rayY.toInt() / 2)
        }

        // draw weapon
        graphics?.let {
            val img = AssetsManager.getImage("portalgun")
            val w = img.width * 2
            val h = img.height * 2
            it.drawImage(img, width - w, (height - h) + (weaponAnimation / 2), w, h, null, null)
        }

        // draw +
        graphics?.let {
            val centerX = width / 2
            val centerY = height / 2
            val size = 8

            it.color = Color.WHITE
            it.drawLine(centerX - (size / 2), centerY, centerX + (size / 2), centerY)
            it.drawLine(centerX, centerY - (size / 2), centerX, centerY + (size / 2))
        }
    }

    fun drawWorld() {
        val sceneData = arrayListOf<Bound>()

        var x: Double = -fov.toDouble() / 2
        while(x < fov / 2) {
            val rayAngle = player.getAngle() + toRadian(x)
            val data = player.rayCast(player.getX(), player.getY(), rayAngle)
            sceneData.addAll(data)

            x += rayStepSize
        }

        var lastTarget = -1
        var textureX = 0
        val w = (width / sceneData.size)
        for (i in 0 until sceneData.size) {
            val img_wall = AssetsManager.getImage(when(sceneData[i].target) {
                1 -> "wall1"
                2 -> "wall2"
                // 3 -> "wall3"

                else -> "wall1"
            })

            val h = sceneData[i].h
            val x = i + (i * w)
            val y = height / 2

            if(lastTarget == sceneData[i].target) {
                textureX += 10
            } else {
                textureX = 0
            }

            if(textureX > img_wall.width-1)
                textureX = 0

            graphics?.let {
                it.color = ColorMap.values()[sceneData[i].v.toInt()].color
                it.drawImage(
                    img_wall.getSubimage(textureX, 0, 1, img_wall.height)
                    , x, (y - (h / 2).toInt()) + (weaponAnimation / 2), w + 1, h.toInt(), null)
                // it.fillRect(x, (y - (h / 2).toInt()) + (weaponAnimation / 2), w + 1, h.toInt())

                it.color = shandowColor(h)
                it.fillRect(x, (y - (h / 2).toInt()) + (weaponAnimation / 2), w + 1, h.toInt())
            }

            // save target
            lastTarget = sceneData[i].target
        }
    }

    fun shandowColor(h: Double): Color {
        var alpha = ((h / 200) * 254).toInt()

        if(alpha > 255)
            alpha = 255

        return Color(
            0,
            0,
            0,
            255 - alpha
        )
    }

    override fun keyTyped(p0: KeyEvent) { }
    override fun mouseDragged(p0: MouseEvent?) { }

    override fun keyPressed(p0: KeyEvent) {
        if(!keyBuffer.contains(p0.keyCode))
            keyBuffer.add(p0.keyCode)
    }

    override fun keyReleased(p0: KeyEvent) {
        keyBuffer.remove(p0.keyCode)
    }

    private var mouseX = 0
    private var mouseY = 0
    override fun mouseMoved(p0: MouseEvent) {
        mouseMoveX += mouseX - p0.x
        mouseMoveY += mouseY - p0.y

        mouseX = p0.x
        mouseY = p0.y
    }
}