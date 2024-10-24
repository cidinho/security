package br.com.alcidesbezerra.bff.generica.security.domain;

/**
 * Contrato para representação de dados mascarados. O modo/formato de criação da máscara é responsabilidade das
 * implementações.
 */
public interface MaskedData {

    /**
     * Deve retornar a informação original, sem máscara.
     */
    String get();

    /**
     * Deve retornar o valor original mascarado como uma String.
     */
    String mask();

}
