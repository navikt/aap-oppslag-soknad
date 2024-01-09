package oppslag

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import oppslag.fakes.BehandlerFake
import oppslag.fakes.KrrFake
import oppslag.fakes.TokenXFake

class Fakes : AutoCloseable {
    val krr = embeddedServer(Netty, port = 0, module = Application::KrrFake).apply { start() }
    val tokenx = embeddedServer(Netty, port = 0, module = Application::TokenXFake).apply { start() }
    val behandler = embeddedServer(Netty, port = 0, module = Application::BehandlerFake).apply { start() }

    override fun close() {
        krr.stop(0L, 0L)
        tokenx.stop(0L, 0L)
        behandler.stop(0L, 0L)
    }
}

fun NettyApplicationEngine.port() = runBlocking { resolvedConnectors() }.first { it.type == ConnectorType.HTTP }.port