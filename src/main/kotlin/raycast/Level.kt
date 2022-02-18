package raycast

import java.awt.Color
import java.awt.image.BufferedImage

class Level(
    private val bufferedImage: BufferedImage
) {
    private val cameras: ArrayList<Camera> = ArrayList()

    fun addCamera(camera: Camera) {
        cameras.add(camera)
    }

    fun getCamera(id: Int): Camera {
        return cameras.get(id)
    }

    fun getPlayer(): Player {
        return cameras[0] as Player
    }

    fun update() {
        cameras.forEach { it.update() }
    }

    fun getImage(): BufferedImage {
        return bufferedImage
    }

    fun get(pos: Pos): Int {
        return Color(bufferedImage.getRGB(pos.x.toInt(), pos.y.toInt())).red
    }

    fun set(pos: Pos, id: Int) {
        val color = Color(id, 0, 0)
        bufferedImage.setRGB(pos.x.toInt(), pos.y.toInt(), color.rgb)
    }
}