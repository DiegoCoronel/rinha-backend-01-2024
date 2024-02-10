package br.rinha.resource

import br.rinha.meta.Tables.TRANSACAO
import br.rinha.meta.tables.Cliente.CLIENTE
import br.rinha.model.Extrato
import br.rinha.model.Operacao
import br.rinha.model.OperacaoInvalida
import br.rinha.model.OperacaoRealizada
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.transaction.Transactional
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

@Path("/clientes/{id}")
@Produces("application/json")
class ClienteResource(
    val dsl: DSLContext
) {

    @POST @Transactional
    @Path("/transacoes")
    @RunOnVirtualThread
    fun novaTransacao(
        @PathParam("id") idCliente: Long,
        operacao: Operacao
    ): OperacaoRealizada {
        operacao.validar()
        return when (operacao.tipo) {
            "c" -> credito(idCliente, operacao)
            "d" -> debito(idCliente, operacao)
            else -> throw OperacaoInvalida("Tipo de operação inválida")
        }
    }

    @GET
    @Path("/extrato")
    @RunOnVirtualThread
    fun extrato(@PathParam("id") idCliente: Long): Extrato {
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
                ).`as`("transacoes")
            )
            .from(CLIENTE)
            .where(CLIENTE.ID.eq(idCliente))
            .fetchOne()
            ?.map {
                Extrato(
                    saldo = Extrato.Saldo(
                        limite = it[CLIENTE.LIMITE],
                        total = it[CLIENTE.SALDO],
                        dataExtrato = ZonedDateTime.now(),
                    ),
                    ultimasTransacoes = (it["transacoes"] as Iterable<org.jooq.Record>).map { t ->
                        Extrato.Transacao(
                            valor = t[TRANSACAO.VALOR],
                            tipo = t[TRANSACAO.TIPO],
                            descricao = t[TRANSACAO.DESCRICAO],
                            realizadaEm = t[TRANSACAO.REALIZADA_EM]
                        )
                    }
                )
            } ?: throw NotFoundException("Cliente não encontrado")
    }

    private fun credito(idCliente: Long, operacao: Operacao): OperacaoRealizada {
        val (limite, saldo) = dsl.update(CLIENTE)
            .set(CLIENTE.SALDO, CLIENTE.SALDO.plus(operacao.valor))
            .where(
                CLIENTE.ID.eq(idCliente)
            )
            .returningResult(CLIENTE.LIMITE, CLIENTE.SALDO)
        .fetchOne() as Record2<Int, Int>? ?: throw NotFoundException("Cliente não encontrado")

        logarOperacao(idCliente, operacao)
        return OperacaoRealizada(limite,saldo)
    }

    private fun debito(idCliente: Long, operacao: Operacao): OperacaoRealizada {
        val (limite, saldo) = dsl.update(CLIENTE)
            .set(CLIENTE.SALDO, CLIENTE.SALDO.minus(operacao.valor))
            .where(
                CLIENTE.ID.eq(idCliente)
            )
            .returningResult(CLIENTE.LIMITE, CLIENTE.SALDO)
            .fetchOne() as Record2<Int, Int>? ?: throw NotFoundException("Cliente não encontrado")

        logarOperacao(idCliente, operacao)
        return OperacaoRealizada(limite,saldo)
    }

    private fun logarOperacao(idCliente: Long, operacao: Operacao) {
        dsl.insertInto(TRANSACAO)
            .set(TRANSACAO.ID_CLIENTE, idCliente)
            .set(TRANSACAO.VALOR, operacao.valor.toInt())
            .set(TRANSACAO.TIPO, operacao.tipo)
            .set(TRANSACAO.DESCRICAO, operacao.descricao)
        .execute()
    }

}
