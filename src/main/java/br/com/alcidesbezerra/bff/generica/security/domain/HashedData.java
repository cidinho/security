package br.com.alcidesbezerra.bff.generica.security.domain;

/**
 * Contrato para representação de dados como "hashes". O algoritmo utilizado e a geração do valor como hash são
 * responsabilidades das implementações.
 */
public interface HashedData {

    /**
     * Deve retornar a informação original, sem hash.
     */
    String get();

    /**
     * Deve retornar o valor original como um hash, independentemente do algoritmo.
     */
    String hash();

}
