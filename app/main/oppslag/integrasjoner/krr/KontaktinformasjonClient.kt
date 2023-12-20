package oppslag.integrasjoner.krr

import com.fasterxml.jackson.annotation.JsonAlias
import oppslag.http.HttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.prometheus.client.Summary
import kotlinx.coroutines.runBlocking
import no.nav.aap.ktor.client.TokenXProviderConfig
import no.nav.aap.ktor.client.TokenXTokenProvider
import oppslag.KrrConfig



private const val KRR_CLIENT_SECONDS_METRICNAME = "krr_client_seconds"
private val clientLatencyStats: Summary = Summary.build()
    .name(KRR_CLIENT_SECONDS_METRICNAME)
    .quantile(0.5, 0.05) // Add 50th percentile (= median) with 5% tolerated error
    .quantile(0.9, 0.01) // Add 90th percentile with 1% tolerated error
    .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
    .help("Latency krr, in seconds")
    .register()

class KontaktinformasjonClient(tokenXProviderConfig: TokenXProviderConfig, private val krrConfig: KrrConfig) {
    private val httpClient = HttpClientFactory.create()
    private val tokenProvider = TokenXTokenProvider(tokenXProviderConfig, krrConfig.scope, httpClient)

    fun hentKrr(
        tokenXToken: String,
        callId: String
    ): KrrRespons =
        clientLatencyStats.startTimer().use {
            runBlocking {
                val obotoken = tokenProvider.getOnBehalfOfToken(tokenXToken)
                val response = httpClient.get("${krrConfig.baseUrl}/rest/v1/person") {
                    accept(ContentType.Application.Json)
                    header("Nav-Callid", callId)
                    bearerAuth(obotoken)
                }
                if (response.status.isSuccess() || response.status.value == 409) {
                    KrrRespons(response.body())
                } else {
                    error("Feil mot krr (${response.status}): ${response.bodyAsText()}")
                }
            }
        }
}




