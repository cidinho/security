package br.com.alcidesbezerra.bff.generica.security.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Message {
    CPF_INVALIDO("cpf.invalido"),
    FALHA_INESPERADA("falha.inesperada"),
    FALHA_DADOS_SENSIVEIS_EXTERNOS("falha.dados.sensiveis.externos"),
    PARAMETRO_INVALIDO("parametro.invalido"),
    REQUEST_INVALIDA("request.invalida"),
    SERVICO_INDISPONIVEL("servico.indisponivel");

    private String message;
}
