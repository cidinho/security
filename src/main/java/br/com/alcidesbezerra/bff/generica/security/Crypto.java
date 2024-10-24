package br.com.alcidesbezerra.bff.generica.security;

import static java.util.Base64.getDecoder;
import static java.util.Base64.getEncoder;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import br.com.alcidesbezerra.bff.generica.security.util.SpringContextUtil;


/**
 * Classe auxiliar para criptografar e descriptografar valores com chaves RSA público/privadas.
 */
public class Crypto {

    private final Cipher cipher;
    private final int mode;
    private final byte[] value;

    private Crypto(final int mode, final String value) throws GeneralSecurityException {
        this.cipher = Cipher.getInstance("RSA");
        this.mode = mode;
        this.value = Objects.requireNonNull(value).getBytes();
    }

    /**
     * Realiza a descriptografia do valor <code>value</code> utilizando a chave {@link PublicKey} do contexto do
     * Spring.
     */
    public static String decrypt(final String value) throws GeneralSecurityException {
        return new Crypto(Cipher.DECRYPT_MODE, value)
            .with(SpringContextUtil.getBean(PublicKey.class));
    }

    /**
     * Realiza a criptografia do valor <code>value</code> utilizando a chave {@link PrivateKey} do contexto do Spring.
     */
    public static String encrypt(final String value) throws GeneralSecurityException {
        return new Crypto(Cipher.ENCRYPT_MODE, value)
            .with(SpringContextUtil.getBean(PrivateKey.class));
    }

    /**
     * Indica a chave que deve ser usada e realiza o processo de criptografia ou descriptografia.
     *
     * @param key A chave com a qual se quer realizar a operação.
     */
    private String with(final Key key)
        throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {

        if (key == null || isEmpty(key.getAlgorithm())) {
            return new String(value);
        }

        cipher.init(mode, key);

        if (mode == Cipher.DECRYPT_MODE) {
            return new String(cipher.doFinal(getDecoder().decode(value)));
        } else {
            return getEncoder().encodeToString(cipher.doFinal(value));
        }
    }

}
