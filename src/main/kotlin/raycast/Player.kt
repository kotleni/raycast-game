package raycast

import raycast.enum.Direction

class Player(viewSize: Size, level: Level, id: Int): Camera(viewSize, level, id) {
    private var speed = 0.4

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