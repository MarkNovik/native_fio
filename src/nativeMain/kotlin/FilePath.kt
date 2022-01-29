@file:Suppress("unused")

import OpenMode.READING
import OpenMode.WRITING
import kotlinx.cinterop.*
import platform.posix.*

value class FilePath(private val filePath: String) {

    fun readLines(): List<String> = openFor(READING) { filePtr ->
        return buildList {
            memScoped {
                val readBufferLength = 65536
                val buffer = allocArray<ByteVar>(readBufferLength)
                var line = fgets(buffer, readBufferLength, filePtr)?.toKString()
                while (line != null) {
                    add(line.removeSuffix("\n").removeSuffix("\r"))
                    line = fgets(buffer, readBufferLength, filePtr)?.toKString()
                }
            }
        }
    }

    fun readText(): String = openFor(READING) { filePtr ->
        val returnBuffer = StringBuilder()
        memScoped {
            val readBufferLength = 65536
            val buffer = allocArray<ByteVar>(readBufferLength)
            var line = fgets(buffer, readBufferLength, filePtr)?.toKString()
            while (line != null) {
                returnBuffer.append(line)
                line = fgets(buffer, readBufferLength, filePtr)?.toKString()
            }
        }
        return returnBuffer.toString()
    }

    fun writeText(text: String): Unit = openFor(WRITING) { fputs(text, it) }

    private inline fun <T> openFor(mode: String, block: (ptr: CPointer<FILE>) -> T): T {
        val filePtr = fopen(filePath, mode) ?: error("Error with opening $filePath for `$mode`")
        try {
            return block(filePtr)
        } finally {
            fclose(filePtr)
        }
    }
}