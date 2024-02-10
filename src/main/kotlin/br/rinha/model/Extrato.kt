package br.rinha.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class Extrato (
    val saldo: Saldo,

    @JsonProperty("ultimas_transacoes")
    val ultimasTransacoes: List<Transacao>,
) {

    data class Saldo(
        val limite: Int,
        val total: Int,

        @JsonProperty("data_extrato")
        val dataExtrato: ZonedDateTime,
    )

    data class Transacao(
        val valor: Int,
        val tipo: String,
        val descricao: String,

        @JsonProperty("realizada_em")
        val realizadaEm: LocalDateTime,
    )

}
