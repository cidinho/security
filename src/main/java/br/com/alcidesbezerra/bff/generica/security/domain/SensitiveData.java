package br.com.alcidesbezerra.bff.generica.security.domain;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import java.io.Serializable;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import br.com.alcidesbezerra.bff.generica.security.Crypto;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

/**
 * Classe abstrata para representação de informações sensíveis. Uma informação sensível, neste contexto, representa
 * qualquer dado que deva ser criptografado, mascarado e também representado como um hash.
 *
 * <br/><br/>Todos os métodos das interfaces {@link EncryptedData}, {@link HashedData} e {@link MaskedData} foram
 * implementados e configurados aqui como <code>final</code> para não possibilitar mudança de comportamento.
 *
 * <br/><br/>Todas as operações sobre o valor <em>(mascaramento, hash e criptografia)</em> são feitos com base no
 * <code>toString()</code> do valor original.
 *
 * <br/><ul>
 * <li>O valor criptografado é gerado conforme chamada a {@link Crypto#encrypt(String)}.</li>
 * <li>O dado em hash é gerado simplesmente como um MD5.</li>
 * <li>A informação mascarada é gerada com base na substituição de caracteres por asteriscos <code>*</code>,
 * conforme cálculo do tamanho do dado menos o número de "caracteres limpos" ({@link #unmaskSize()}).</li>
 * </ul>
 *
 * Esta classe também sobrescreve o método {@link String#toString()} e o define como <code>final</code>, visando
 * garantir que nenhuma de suas extensões modifique este comportamento e acabe exibindo o valor original de forma
 * indevida. O resultado do método será sempre no formato <strong>SimpleName[value]</strong>.
 */
@EqualsAndHashCode(of = "value")
public abstract class SensitiveData implements EncryptedData, HashedData, MaskedData, Serializable {

    private static final long serialVersionUID = 9042409702621832579L;

    private final String value;

    private String encryptedValue;
    private String hashedValue;
    private String maskedValue;

    /**
     * Cria uma instância de dado sensível indicando se o valor é o original ou criptografado. Se for criptografado,
     * realiza neste momento a descriptografia.
     *
     * @param value O valor original ou criptografado.
     * @param isEncrypted <strong>true</strong> se o valor está criptografado; <strong>false</strong> senão.
     */
    @SneakyThrows
    public  SensitiveData(final String value, final boolean isEncrypted) {
        if (isEncrypted) {
            this.encryptedValue = value;
            this.value = doDecrypt(value);
        } else {
            this.value = formatValue(value);
        }
    }

    /**
     * Indica a quantidade de caracteres que **não** deve ser mascarada no {@link #value}.
     */
    public abstract Integer unmaskSize();

    /**
     * Indica o tipo de formatação que o {@link #value} precisa receber antes de ser criptografado.
     */
    public String formatValue(final String value) {
        return ofNullable(value).map(String::trim).orElseGet(() -> value);
    }

    /**
     * Indica se o {@link #value} é valido.
     */
    public boolean isValid() {
        return nonNull(get());
    }

    @Override
    public final String get() {
        return value;
    }

    @Override
    public final String decrypt() {
        return value;
    }

    @Override
    @SneakyThrows
    public final String encrypt() {
        if (isNull(encryptedValue)) {
            encryptedValue = ofNullable(value)
                .map(SensitiveData::doEncrypt)
                .orElse(null);
        }

        return encryptedValue;
    }

    @SneakyThrows
    private static String doEncrypt(final String value) {
        return Crypto.encrypt(value);
    }

    @SneakyThrows
    private String doDecrypt(final String value) {
        return formatValue(Crypto.decrypt(value));
    }

    @Override
    public final String hash() {
        if (isNull(hashedValue)) {
            hashedValue = ofNullable(value)
                .map(DigestUtils::md5Hex)
                .orElse(null);
        }

        return hashedValue;
    }

    @Override
    public final String mask() {
        if (isNull(maskedValue)) {
            maskedValue = ofNullable(value)
                .map(val -> {
                    final int maskSize = val.length() - unmaskSize();
                    return StringUtils.overlay(val, StringUtils.repeat("*", maskSize), 0, maskSize);
                })
                .orElse(null);
        }

        return maskedValue;
    }

    @Override
    public final String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), hash());
    }

}