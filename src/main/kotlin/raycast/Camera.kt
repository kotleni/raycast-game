package raycast

import map
import toRadian

open class Camera(val screenW: Int, val screenH: Int) {
    private var posX: Double = ((map[0].size / 2) * Config.CELL_SIZE + Config.CELL_SIZE / 2).toDouble()
    private var posY: Double = (14 * Config.CELL_SIZE + Config.CELL_SIZE / 2).toDouble()
    private var rotation = 0.0
    private var angle = toRadian(-90)

    fun getX(): Double {
        return posX
    }

    fun getY(): Double {
        return posY
    }

    fun getAngle(): Double {
        return angle
    }

    fun setX(_x: Double) {
        posX = _x
    }

    fun setY(_y: Double) {
        posY = _y
    }

    fun rotateOffset(_rot: Double) {
        rotation += _rot
    }

    fun moveOffset(_x: Double, _y: Double) {
        posX += _x
        posY += _y
    }

    fun rayCast(srcX: Double, srcY: Double, angle: Double): List<Bound> {
        val sceneData = arrayListOf<Bound>()

        var rayX = 0.0
        var rayY = 0.0
        var dst = 0.0
        var isHit = false

        while (!isHit && dst < screenW) {
            dst += 0.1
            rayX = srcX + Math.cos(angle) * dst
            rayY = srcY + Math.sin(angle) * dst

            val row = (rayY / Config.CELL_SIZE).toInt()
            val col = (rayX / Config.CELL_SIZE).toInt()
            val a = getAngle() - angle
            val z = dst * Math.cos(a)
            val h = screenH / 2 * 64 / z

            if (rayX > screenW  - 4 || rayX < 4 || rayY < 4 || rayY >  screenH - 4) { // hit to end of level
                isHit = true
                sceneData.add(Bound(h, 5.0, -1, -1, -1)) // world boundaries
            } else if (map[row][col] > 0) { // hit to wall
                isHit = true
                sceneData.add(Bound(h, map[row][col].toDouble(), map[row][col], row, col))
            }
        }

        return sceneData
    }

    fun update() {
        rotation *= 0.5
        angle += rotation
    }
}