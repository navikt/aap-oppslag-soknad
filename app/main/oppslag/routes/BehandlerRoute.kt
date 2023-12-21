package oppslag.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppslag.integrasjoner.behandler.BehandlerClient

fun Route.behandlerRoute(behandler: BehandlerClient) {
    route("/behandler") {
        get {
            val callId = requireNotNull(call.request.header("x-callid")) { "x-callid ikke satt" }
            call.respond(HttpStatusCode.OK, behandler.hentBehandler(call.authentication.toString(), callId))
        }

    }
}