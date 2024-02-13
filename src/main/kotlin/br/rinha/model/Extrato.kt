package br.rinha.model

import java.time.LocalDateTime
import java.time.ZonedDateTime

data class Extrato (
    val saldo: Saldo,
    val ultimas_transacoes: List<Transacao>,
) {

    data class Saldo(
        val limite: Int,
        val total: Int,
        val data_extrato: ZonedDateTime,
    )

    data class Transacao(
        val valor: Int,
        val tipo: String,
        val descricao: String,
        val realizada_em: LocalDateTime,
    )

}
