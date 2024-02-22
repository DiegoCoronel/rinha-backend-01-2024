package br.rinha.resource

import br.rinha.meta.Tables.TRANSACAO
import br.rinha.meta.tables.Cliente.CLIENTE
import br.rinha.model.*
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import org.jooq.DSLContext
import org.jooq.Record2
import org.jooq.impl.DSL
import java.time.ZonedDateTime
import java.util.concurrent.Executors


@Path("/clientes/{id}")
@Produces("application/json")
@Consumes("application/json")
class ClienteResource(
    val dsl: DSLContext
) {

    @POST @Transactional
    @Path("/transacoes")
    fun novaTransacao(
        @PathParam("id") idCliente: Long,
        operacao: Operacao
    ): OperacaoRealizada {
        if (idCliente <= 0 || idCliente >= 6) {
            throw NotFoundException(CLIENTE_NAO_ENCONTRADO)
        }
        operacao.validar()

        return when (operacao.tipo) {
            CREDITO, DEBITO -> operacao(idCliente, operacao)
            else -> throw OperacaoInvalida(TIPO_OPERACAO_INVALIDA)
        }
    }

    @GET
    @Path("/extrato")
    @RunOnVirtualThread
    fun extrato(@PathParam("id") idCliente: Long): Extrato {
        if (idCliente <= 0 || idCliente >= 6) {
            throw NotFoundException(CLIENTE_NAO_ENCONTRADO)
        }

        return dsl
            .select(
                CLIENTE.LIMITE,
                CLIENTE.SALDO,
                DSL.multiset(
                    DSL.select(
                        TRANSACAO.VALOR,
                        TRANSACAO.TIPO,
                        TRANSACAO.DESCRICAO,
                        TRANSACAO.REALIZADA_EM
                    )
                    .from(TRANSACAO)
                    .where(TRANSACAO.ID_CLIENTE.eq(idCliente))
                    .orderBy(TRANSACAO.REALIZADA_EM.desc())
                    .limit(10)
                ).`as`(TRANSACOES)
            )
            .from(CLIENTE)
            .where(CLIENTE.ID.eq(idCliente))
            .fetchOne()
            ?.map {
                Extrato(
                    saldo = Extrato.Saldo(
                        limite = it[CLIENTE.LIMITE],
                        total = it[CLIENTE.SALDO],
                        data_extrato = ZonedDateTime.now(),
                    ),
                    ultimas_transacoes = (it[TRANSACOES] as Iterable<org.jooq.Record>).map { t ->
                        Extrato.Transacao(
                            valor = t[TRANSACAO.VALOR],
                            tipo = t[TRANSACAO.TIPO],
                            descricao = t[TRANSACAO.DESCRICAO],
                            realizada_em = t[TRANSACAO.REALIZADA_EM]
                        )
                    }
                )
            } ?: throw NotFoundException(CLIENTE_NAO_ENCONTRADO)
    }

    private inline fun operacao(idCliente: Long, operacao: Operacao): OperacaoRealizada {
        dsl.resultQuery("SELECT pg_advisory_xact_lock(${idCliente});").fetchOne()

        val (limite, saldo) = dsl.update(CLIENTE)
            .set(
                CLIENTE.SALDO,
                if (operacao.tipo == CREDITO) CLIENTE.SALDO.plus(operacao.valor)
                else CLIENTE.SALDO.minus(operacao.valor)
            )
            .where(
                CLIENTE.ID.eq(idCliente)
            )
            .returningResult(CLIENTE.LIMITE, CLIENTE.SALDO)
            .fetchOne() as Record2<Int, Int>

        logarOperacao(idCliente, operacao)
        return OperacaoRealizada(limite,saldo)
    }

    private fun logarOperacao(idCliente: Long, operacao: Operacao) {
        dsl.insertInto(TRANSACAO)
            .set(TRANSACAO.ID_CLIENTE, idCliente)
            .set(TRANSACAO.VALOR, operacao.valor.toInt())
            .set(TRANSACAO.TIPO, operacao.tipo)
            .set(TRANSACAO.DESCRICAO, operacao.descricao)
        .executeAsync(Executors.newVirtualThreadPerTaskExecutor())
    }

    companion object {
        const val CLIENTE_NAO_ENCONTRADO: String = "Cliente não encontrado"
        const val TIPO_OPERACAO_INVALIDA: String = "Tipo de operação inválida"
        const val TRANSACOES: String = "transacoes"
        const val CREDITO: String = "c"
        const val DEBITO: String = "d"
    }

}
