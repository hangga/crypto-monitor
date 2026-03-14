@file:Suppress("JSON_FORMAT_REDUNDANT_DEFAULT")

package id.web.hangga

import id.web.hangga.config.CryptoConfig
import id.web.hangga.resilience.CircuitBreakerConfigWrapper
import id.web.hangga.routes.PriceRoutes
import id.web.hangga.service.ExternalCryptoService
import id.web.hangga.service.FallbackCache
import id.web.hangga.stream.PriceFlow
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo
import kotlin.concurrent.thread

fun main() {
    // Force init serialization
    kotlinx.serialization.json.Json { }

    // Force Resilience4j init
    io.github.resilience4j.circuitbreaker.CircuitBreaker.ofDefaults("warmup")

    // Optional: initialize client
    HttpClient(CIO).close()
    val port = System.getenv("PORT")?.toInt() ?: 8080

    embeddedServer(io.ktor.server.cio.CIO, port = port, module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    install(ContentNegotiation) { json() }

    startDeadlockMonitor()

    val config = CryptoConfig.load()

    val cb = CircuitBreakerConfigWrapper.createDefault()
    val cache = FallbackCache.instance
    val externalService = ExternalCryptoService(config.apiUrl)
    val priceFlow = PriceFlow(
        externalService, cache, cb, config.pollIntervalMs
    )

    // --- CircuitBreaker ---
    val cbWrapper = CircuitBreakerConfigWrapper.createDefault()

    // --- External service ---
    val clientService = ExternalCryptoService(config.apiUrl)

    // --- Routing ---
    routing {
        get("/") {
            call.respondText("Crypto Monitor running - see /price and /stream/price")
        }

        // Register price routes
        PriceRoutes.register(this, clientService, cache, cbWrapper, priceFlow)
    }
}

//fun startDeadlockMonitor() {
//
//    thread(isDaemon = true) {
//
//        while (true) {
//
//            detectDeadlock()
//
//            Thread.sleep(5000)
//        }
//    }
//}

//fun startDeadlockMonitor() {
//
//    thread(isDaemon = true) {
//
//        val reportedDeadlocks = mutableSetOf<Long>()
//
//        while (true) {
//
//            val threadMXBean = ManagementFactory.getThreadMXBean()
//            val deadlockedThreads = threadMXBean.findDeadlockedThreads()
//
//            if (deadlockedThreads != null) {
//
//                val newDeadlocks = deadlockedThreads.filter { it !in reportedDeadlocks }
//
//                if (newDeadlocks.isNotEmpty()) {
//
//                    println("Deadlock detected!")
//
//                    val threadInfos = threadMXBean.getThreadInfo(deadlockedThreads, true, true)
//
//                    threadInfos.forEach { threadInfo ->
//
//                        println(
//                            "Thread: ${threadInfo.threadName} " +
//                              "[id:${threadInfo.threadId}, state:${threadInfo.threadState}]"
//                        )
//
//                        println("Waiting on lock: ${threadInfo.lockInfo}")
//
//                        val stackTrace =
//                            threadInfo.stackTrace.joinToString("\n") { it.toString() }
//
//                        println(stackTrace)
//                        println("--------------------------------------------------")
//                    }
//
//                    reportedDeadlocks.addAll(deadlockedThreads.toList())
//                }
//            }
//
//            Thread.sleep(5000)
//        }
//    }
//}

fun startDeadlockMonitor() {

    thread(isDaemon = true) {

        val reportedDeadlocks = mutableSetOf<Long>()

        while (true) {

            val deadlockedThreads = detectDeadlock()

            if (deadlockedThreads != null) {

                val newDeadlocks = deadlockedThreads.filter { it !in reportedDeadlocks }

                if (newDeadlocks.isNotEmpty()) {

                    val threadMXBean = ManagementFactory.getThreadMXBean()
                    val threadInfos = threadMXBean.getThreadInfo(deadlockedThreads, true, true)

                    logDeadlock(threadInfos)

                    reportedDeadlocks.addAll(deadlockedThreads.toList())
                }
            }

            Thread.sleep(5000)
        }
    }
}

fun detectDeadlock(): LongArray? {
    val threadMXBean = ManagementFactory.getThreadMXBean()
    return threadMXBean.findDeadlockedThreads()
}

fun logDeadlock(threadInfos: Array<ThreadInfo>) {

    println("Deadlock detected!")

    threadInfos.forEach { threadInfo ->

        println(
            "Thread: ${threadInfo.threadName} " +
              "[id:${threadInfo.threadId}, state:${threadInfo.threadState}]"
        )

        println("Waiting on lock: ${threadInfo.lockInfo}")

        val stackTrace = threadInfo.stackTrace.joinToString("\n")

        println(stackTrace)
        println("--------------------------------------------------")
    }
}

