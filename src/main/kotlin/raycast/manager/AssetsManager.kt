package raycast.manager

import raycast.Level
import raycast.LevelCompiler
import raycast.utils.ImageUtils
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object AssetsManager {
    private val images = HashMap<String, BufferedImage>()
    private val levels = HashMap<String, Level>()

    fun loadAll() {
        loadImages()
        loadLevels()
    }

    private fun loadImages() {
        File("./src/main/resources/images/").listFiles().forEach {
            val img = if(it.name.contains("wall")) ImageUtils.normalizeImage(ImageIO.read(it)) else ImageIO.read(it)
            images.put(it.name, img)

            println("Loaded image ${it.name}")
        }
    }

    private fun loadLevels() {
        File("./src/main/resources/levels/").listFiles().forEach {
            val level = LevelCompiler.compileFromFile(it)
            levels.put(it.name, level)

            println("Loaded level ${it.name}")
        }
    }

    fun getImage(name: String): BufferedImage {
        return images.get("${name}.png")!!
    }

    fun getLevel(name: String): Level {
        return levels.get("${name}.level")!!
    }
}