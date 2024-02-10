package br.rinha.handler

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.jooq.exception.IntegrityConstraintViolationException


@Provider
class JOOQIntegrityConstraintViolationHandler : ExceptionMapper<IntegrityConstraintViolationException> {

    override fun toResponse(exception: IntegrityConstraintViolationException?): Response {
        return Response.status(422).entity("").build()
    }

}
