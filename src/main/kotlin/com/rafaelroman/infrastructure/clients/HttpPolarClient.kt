package com.rafaelroman.infrastructure.clients

import com.google.gson.annotations.SerializedName
import com.rafaelroman.domain.polar.PolarAccessCodeProvider
import com.rafaelroman.domain.polar.PolarAccessToken
import com.rafaelroman.domain.polar.PolarAuthorizationRequestCode
import com.rafaelroman.domain.polar.PolarSleepDataProvider
import com.rafaelroman.domain.polar.PolarSleepNight
import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.Url
import io.ktor.http.contentType
import org.slf4j.LoggerFactory
import java.util.Base64

private val logger = LoggerFactory.getLogger(HttpPolarClient::class.java)

class HttpPolarClient(
    private val client: HttpClient,
    private val clientId: String,
    private val clientSecret: String,
) : PolarAccessCodeProvider, PolarSleepDataProvider {

    override suspend fun withCode(polarAuthorizationRequestCode: PolarAuthorizationRequestCode): PolarAccessToken =
        client.submitForm<PolarAccessTokenHttpResponse>("https://polarremote.com/v2/oauth2/token") {
            accept(ContentType.Application.Json)
            header("Authorization", "Basic ${Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())}")
            body = FormDataContent(
                Parameters.build {
                    append("code", polarAuthorizationRequestCode.value)
                    append("grant_type", "authorization_code")
                }
            )
        }
            .toPolarAccessToken()
            .also {
                try {
                    client.post("https://www.polaraccesslink.com/v3/users") {
                        contentType(ContentType.Application.Json)
                        header("Authorization", "Bearer ${it.accessToken}")
                        body = PolarRegisterUserHttpRequest(it.userId.toString())
                    }
                } catch (e: ClientRequestException) {
                    if (e.response.status.value == 409) {
                        logger.info("process=register_user status=already-registered")
                    } else {
                        throw e
                    }
                }
            }

    override suspend fun latest(polarAccessToken: PolarAccessToken): List<PolarSleepNight> =
        client.get<HttpPolarSleepNightsResponse>("https://www.polaraccesslink.com/v3/users/sleep") {
            header("Authorization", "Bearer ${polarAccessToken.accessToken}")
        }.nights.map {
            it.toPolarSleepNight()
        }
}

private data class PolarRegisterUserHttpRequest(
    @SerializedName("member-id")
    val userId: String,
)

private data class PolarAccessTokenHttpResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expiresInSeconds: Long,
    @SerializedName("x_user_id")
    val userId: Long,

) {
    fun toPolarAccessToken(): PolarAccessToken = PolarAccessToken(
        accessToken = accessToken,
        expiresIn = expiresInSeconds,
        userId = userId
    )
}

data class HttpPolarSleepNightsResponse(

    @SerializedName("nights") val nights: List<HttpPolarSleepNight>,
)

data class HttpPolarSleepNight(
    @SerializedName("polar_user")
    val polarUser: String,

    val date: String,

    @SerializedName("sleep_start_time")
    val sleepStartTime: String,

    @SerializedName("sleep_end_time")
    val sleepEndTime: String,

    @SerializedName("device_id")
    val deviceID: String,

    val continuity: Double,

    @SerializedName("continuity_class")
    val continuityClass: Long,

    @SerializedName("light_sleep")
    val lightSleep: Long,

    @SerializedName("deep_sleep")
    val deepSleep: Long,

    @SerializedName("rem_sleep")
    val remSleep: Long,

    @SerializedName("unrecognized_sleep_stage")
    val unrecognizedSleepStage: Long,

    @SerializedName("sleep_score")
    val sleepScore: Long,

    @SerializedName("total_interruption_duration")
    val totalInterruptionDuration: Long,

    @SerializedName("sleep_charge")
    val sleepCharge: Long,

    @SerializedName("sleep_goal")
    val sleepGoal: Long,

    @SerializedName("sleep_rating")
    val sleepRating: Long,

    @SerializedName("short_interruption_duration")
    val shortInterruptionDuration: Long,

    @SerializedName("long_interruption_duration")
    val longInterruptionDuration: Long,

    @SerializedName("sleep_cycles")
    val sleepCycles: Long,

    @SerializedName("group_duration_score")
    val groupDurationScore: Long,

    @SerializedName("group_solidity_score")
    val groupSolidityScore: Long,

    @SerializedName("group_regeneration_score")
    val groupRegenerationScore: Double,

    val hypnogram: Map<String, Long>,

    @SerializedName("heart_rate_samples")
    val heartRateSamples: Map<String, Long>,
) {
    fun toPolarSleepNight() = PolarSleepNight(
        polarUser = Url(polarUser),
        date = date,
        sleepStartTime = sleepStartTime,
        sleepEndTime = sleepEndTime,
        deviceID = deviceID,
        continuity = continuity,
        continuityClass = continuityClass,
        lightSleep = lightSleep,
        deepSleep = deepSleep,
        remSleep = remSleep,
        unrecognizedSleepStage = unrecognizedSleepStage,
        sleepScore = sleepScore,
        totalInterruptionDuration = totalInterruptionDuration,
        sleepCharge = sleepCharge,
        sleepGoal = sleepGoal,
        sleepRating = sleepRating,
        shortInterruptionDuration = shortInterruptionDuration,
        longInterruptionDuration = longInterruptionDuration,
        sleepCycles = sleepCycles,
        groupDurationScore = groupDurationScore,
        groupSolidityScore = groupSolidityScore,
        groupRegenerationScore = groupRegenerationScore,
        hypnogram = hypnogram,
        heartRateSamples = heartRateSamples

    )
}
