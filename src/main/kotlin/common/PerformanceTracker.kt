package common

fun <T> track(block: () -> T): T {
    val startTime = System.currentTimeMillis()
    val result = block()
    val endTime = System.currentTimeMillis()
    println(endTime - startTime)
    return result
}