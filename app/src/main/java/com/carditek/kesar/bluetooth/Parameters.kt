package com.carditek.kesar.bluetooth

import java.nio.ByteBuffer
import java.nio.ByteOrder

class Parameters(val version: Short, val leads: Short, val limb: Short, val chest: Short) {
    override fun toString(): String {
        return "Parameters(version=$version, leads=$leads, limb=$limb, chest=$chest)"
    }

    companion object {
        fun from(serialized: ByteArray): Parameters {
            val buffer = ByteBuffer.wrap(serialized).order(ByteOrder.LITTLE_ENDIAN)
            return Parameters(
                buffer.getShort(0), buffer.getShort(2),
                buffer.getShort(4), buffer.getShort(6)
            )
        }
    }
}