package com.pgs.gamehelper.models

data class Player(
    val name: String,
) {
    override fun toString(): String {
        return name
    }
}
