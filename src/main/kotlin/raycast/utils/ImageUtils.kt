package raycast.utils

import java.awt.image.BufferedImage

object ImageUtils {
    const val TEXTURE_W = 126
    const val TEXTURE_H = 126

    fun normalizeImage(image: BufferedImage): BufferedImage {
        val newimage = BufferedImage(TEXTURE_W, TEXTURE_H, BufferedImage.TYPE_INT_ARGB)
        val g = newimage.graphics!!

        g.drawImage(image, 0, 0, newimage.width, newimage.height, null)

        return newimage
    }
}