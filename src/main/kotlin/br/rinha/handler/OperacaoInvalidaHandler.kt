package br.rinha.handler

import br.rinha.model.OperacaoInvalida
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.jooq.exception.IntegrityConstraintViolationException


@Provider
class OperacaoInvalidaHandler : ExceptionMapper<OperacaoInvalida> {

    override fun toResponse(exception: OperacaoInvalida?): Response {
        return Response.status(422).entity("").build()
    }

}
