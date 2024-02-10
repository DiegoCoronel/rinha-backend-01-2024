package br.rinha.model

data class Operacao(
    val valor: Double,
    val tipo: String,
    val descricao: String?,
) {

    fun validar() {
        if(descricao == null) {
            throw OperacaoInvalida("Descrição é obrigatória")
        }
        if(descricao.isBlank() || descricao.length > 10) {
            throw OperacaoInvalida("Descrição deve ter no máximo 10 caracteres")
        }
        if(valor % 1 != 0.0) {
            throw OperacaoInvalida("Valor deve ser um número inteiro")
        }
    }

}
