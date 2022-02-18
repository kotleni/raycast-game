package raycast

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.nio.charset.Charset

object LevelCompiler {
    const val LEVEL_SCALE = 10

    fun compileFromFile(file: File): Level {
        println("Compile level ${file.name}")
        return compileFromText(file.readText(Charset.defaultCharset()))
    }

    private fun compileFromText(text: String): Level {
        val lines = text.split("\n")
        val width = lines[0].split(";").size
        val height = lines.size

        val image = BufferedImage(width * LEVEL_SCALE, height * LEVEL_SCALE, BufferedImage.TYPE_INT_RGB)

        for(x in 0 until width) {
            for(y in 0 until height) {
                val id = lines[y].split(";")[x].toInt()
                val color = Color(id, 0, 0)

                image.graphics?.let {
                    it.color = color
                    it.fillRect(x * LEVEL_SCALE, y * LEVEL_SCALE, LEVEL_SCALE, LEVEL_SCALE)
                }
            }
        }

        return Level(image)
    }
}