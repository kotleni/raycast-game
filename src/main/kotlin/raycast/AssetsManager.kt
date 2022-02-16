package raycast

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object AssetsManager {
    private val images = HashMap<String, BufferedImage>()

    fun loadAll() {
        File("./src/main/resources/").listFiles().forEach {
            val img = ImageIO.read(it)
            images.put(it.name, img)

            println("Loaded image ${it.name}")
        }
    }

    fun getImage(name: String): BufferedImage {
        return images.get("${name}.png")!!
    }
}