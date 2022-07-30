package com.carditek.kesar

interface Backend {
    suspend fun me(): String?
    suspend fun fetch(address: String, stamp: Int): ByteArray?
    suspend fun store(chunk: Chunk)
    suspend fun note(address: String, stamp: Int, text: String)
}
