package raycast

class Player(screenW: Int, screenH: Int): Camera(screenW, screenH) {
    private var speed = 4.0

    fun getSpeed(): Double {
        return speed
    }

    fun moveUp() {
        val dirX = Math.cos(getAngle()) * speed
        val dirY = Math.sin(getAngle()) * speed

        moveOffset(dirX.toInt(), dirY.toInt())
    }
}