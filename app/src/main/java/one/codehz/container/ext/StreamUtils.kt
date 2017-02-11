package one.codehz.container.ext

import java.io.InputStream
import java.io.OutputStream

fun streamTransfer(pair: Pair<InputStream, OutputStream>, bufferSize: Int, progressFn: ((Long) -> Unit)? = null): Long {
    val (input, output) = pair
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = input.read(buffer)
    while (bytes >= 0) {
        output.write(buffer, 0, bytes)
        bytesCopied += bytes
        progressFn?.invoke(bytesCopied)
        bytes = input.read(buffer)
    }
    return bytesCopied
}