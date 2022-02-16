package raycast

import raycast.enum.Direction

class Player(screenW: Int, screenH: Int): Camera(screenW, screenH) {
    private var speed = 4.0

    // get current speed
    fun getSpeed(): Double {
        return speed
    }

    // smooth move player
    fun walk(direction: Direction = Direction.UP) {
        if(direction == Direction.UP) {
            val dirX = Math.cos(getAngle()) * speed
            val dirY = Math.sin(getAngle()) * speed
            moveOffset(dirX, dirY)
        } else if(direction == Direction.DOWN) {
            val dirX = Math.cos(getAngle()) * -speed
            val dirY = Math.sin(getAngle()) * -speed
            moveOffset(dirX, dirY)
        }
    }
}