package br.com.alcidesbezerra.bff.generica.security.domain;

/**
 * Contrato para representação de dados criptografados.
 *   O modo/formato de criptografia é responsabilidade das implementações.
 */
public interface EncryptedData {

    /**
     * Deve retornar a informação original, sem criptografia.
     */
    String decrypt();

    /**
     * Deve retornar o valor original criptografado como uma String.
     */
    String encrypt();

}
