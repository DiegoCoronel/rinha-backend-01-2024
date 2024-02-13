package br.rinha.model

data class Operacao(
    val valor: Double,
    val tipo: String,
    val descricao: String?,
) {

    fun validar() {
        if(descricao == null) {
            throw OperacaoInvalida(DESCRICAO_OBRIGATORIO)
        }
        if(descricao.isBlank() || descricao.length > 10) {
            throw OperacaoInvalida(DESCRICAO_TAMANHO)
        }
        if(valor % 1 != 0.0) {
            throw OperacaoInvalida(VALOR_INTEIRO)
        }
    }

    companion object {
        const val DESCRICAO_OBRIGATORIO = "Descrição é obrigatória"
        const val DESCRICAO_TAMANHO = "Descrição deve ter no máximo 10 caracteres"
        const val VALOR_INTEIRO = "Valor deve ser um número inteiro"
    }

}
