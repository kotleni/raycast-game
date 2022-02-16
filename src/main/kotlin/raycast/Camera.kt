package raycast

import Game.Companion.cellSize
import map
import toRadian

open class Camera(val screenW: Int, val screenH: Int) {
    private var x = (map[0].size / 2) * cellSize + cellSize / 2
    private var y = 14 * cellSize + cellSize / 2
    private var rotation = 0.0
    private var angle = toRadian(-90)

    fun getX(): Int {
        return x
    }

    fun getY(): Int {
        return y
    }

    fun getAngle(): Double {
        return angle
    }

    fun setX(_x: Int) {
        x = _x
    }

    fun setY(_y: Int) {
        y = _y
    }

    fun rotateOffset(_rot: Double) {
        rotation += _rot
    }

    fun moveOffset(_x: Int, _y: Int) {
        x += _x
        y += _y
    }

    fun rayCast(srcX: Int, srcY: Int, angle: Double): List<Bound> {
        val sceneData = arrayListOf<Bound>()

        var rayX = 0.0
        var rayY = 0.0
        var dst = 0.0
        var isHit = false

        while (!isHit && dst < screenW) {
            dst += 0.1
            rayX = srcX + Math.cos(angle) * dst
            rayY = srcY + Math.sin(angle) * dst

            val row = (rayY / cellSize).toInt()
            val col = (rayX / cellSize).toInt()
            val a = getAngle() - angle
            val z = dst * Math.cos(a)
            val h = screenH / 2 * 64 / z

            if (rayX > screenW  - 4 || rayX < 4 || rayY < 4 || rayY >  screenH - 4) {
                isHit = true
                sceneData.add(Bound(h, 5.0, -1, -1, -1)) // world boundaries
            } else
                if (map[row][col] > 0 &&  map[row][col] < ColorMap.values().size) {
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