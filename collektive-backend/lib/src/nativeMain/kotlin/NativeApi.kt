import generated.Dinosaur
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
@CName("process_dinosaur")
fun processDinosaur(data: CPointer<ByteVar>, length: Int) {
    val bytes = data.readBytes(length)
    val dinosaur = Dinosaur.ADAPTER.decode(bytes)
    println("[BACKEND] received: ${dinosaur.name}")
}