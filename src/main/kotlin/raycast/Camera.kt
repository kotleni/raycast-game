package raycast

import raycast.manager.AssetsManager
import java.awt.Color
import java.awt.image.BufferedImage

open class Camera(
    private val viewSize: Size,
    private val level: Level,
    private val id: Int
) {
    private val pos = Pos(50.0, 50.0)
    private var rotation = 0.0
    private var angle = toRadian(-90)

    fun getId(): Int {
        return id
    }

    fun getAngle(): Double {
        return angle
    }

    fun getPos(): Pos {
        return pos
    }

    fun getX(): Double {
        return pos.x
    }

    fun getY(): Double {
        return pos.y
    }

    fun rotateOffset(_rot: Double) {
        rotation += _rot
    }

    fun moveOffset(_x: Double, _y: Double) {
        pos.x += _x
        pos.y += _y
    }

    fun update() {
        rotation *= 0.5
        angle += rotation
    }

    fun findHit(maxLen: Double): Pos? {
        val map = level.getImage()

        var len = 0.0
        while(len < maxLen) {
            len += 0.1

            val rayX = getX() + Math.cos(angle) * len
            val rayY = getY() + Math.sin(angle) * len

            if(rayX < 0 || rayY < 0 || rayX > map.width-1 || rayY > map.height-1)
                return null

            val id = Color(map.getRGB(rayX.toInt(), rayY.toInt())).red

            if(id > 0) {
                return Pos(rayX, rayY)
            }
        }

        return null
    }

    fun render(): BufferedImage {
        val image = BufferedImage(viewSize.w, viewSize.h, BufferedImage.TYPE_INT_RGB)
        val g = image.graphics!!

        // sky
        g.color = Color.CYAN
        g.fillRect(0, 0, image.width, image.height / 2)

        // ground
        g.color = Color.DARK_GRAY
        g.fillRect(0, image.height / 2, image.width, image.height)

        val map = level.getImage()

        var i: Double = -(GameWindow.FOV / 2)
        while(i < GameWindow.FOV / 2) {
            val rayAngle = getAngle() + toRadian(i)

            val srcX = getX()
            val srcY = getY()

            var isHit = false
            var len = 0.0

            while(!isHit && len < map.height) {
                len += 0.1

                val rayX = srcX + Math.cos(rayAngle) * len
                val rayY = srcY + Math.sin(rayAngle) * len

                if(rayX < 0 || rayY < 0 || rayX > map.width-1 || rayY > map.height-1)
                    break

                val a = getAngle() - rayAngle
                val z = len * Math.cos(a)
                val h = map.height / 2 * 64 / z

                val w = (viewSize.w / (GameWindow.FOV / 2)).toInt()
                val ii = i + (GameWindow.FOV / 4)
                val x = ii + (ii * w)
                val y = viewSize.h / 2

                val id = Color(map.getRGB(rayX.toInt(), rayY.toInt())).red

                var img = AssetsManager.getImage(when(id) {
                    1 -> "wall1"
                    2 -> "wall2"
                    3 -> "portal"

                    else -> "wall2"
                })

                if(id == 3 && getId() != 1) {
                    img = level.getCamera(1).render()
                }

                if(id > 0) {
                    isHit = true

                    g.color = when(id) {
                        1 -> Color.RED
                        2 -> Color.BLUE

                        else -> Color.BLACK
                    }

                    // math textureX
                    var textureX = 0
                    var xx = rayX
                    val step = (w / 4)
                    while(xx > 0 && xx < level.getImage().width) {
                        val _id = level.get(Pos(xx, rayY))
                        if(id != _id)
                            break

                        textureX += step
                        xx -= 1

                        if(textureX + (step) > img.width)
                            textureX = 0
                    }

                    // draw wall
                    g.drawImage(
                        img.getSubimage(textureX, 0, img.width - textureX, img.height),
                        x.toInt(), (y - (h / 2).toInt()), w / 4, h.toInt(),
                        null
                    )
                    // g.fillRect(x.toInt(), (y - (h / 2).toInt()), w / 4, h.toInt())

                    // draw shadow
                    g.color = shadowColor(h)
                    g.fillRect(x.toInt(), (y - (h / 2).toInt()), w / 4, h.toInt())
                }
            }
            i += 0.2
        }

        return image
    }

    private fun shadowColor(h: Double): Color {
        var alpha = ((h / 200) * 254).toInt()

        if(alpha > 255)
            alpha = 255

        if(alpha < 0)
            alpha = 0

        return Color(
            0,
            0,
            0,
            255 - alpha
        )
    }
}