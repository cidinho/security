package br.com.alcidesbezerra.bff.generica.security;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import br.com.alcidesbezerra.bff.generica.security.mapper.EncryptedDataSerializer;


@Configuration
public class RsaKey {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    void objectMapperSecuritySetup() {
        final SimpleModule securityModule = new SimpleModule();
        securityModule.addSerializer(new EncryptedDataSerializer());
        objectMapper.registerModule(securityModule);
    }

    @Bean
    PrivateKey privateKey(@Value("${security.private-key}") final String key) throws GeneralSecurityException {
        final PKCS8EncodedKeySpec pkcs8 = new PKCS8EncodedKeySpec(decodePem(key));
        final KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
        return rsaKeyFactory.generatePrivate(pkcs8);
    }

    @Bean
    PublicKey publicKey(@Value("${security.public-key}") final String key) throws GeneralSecurityException {
        final X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodePem(key));
        final KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
        return rsaKeyFactory.generatePublic(x509);
    }

    private static byte[] decodePem(final String content) {
        final String pem = content
            .replaceAll("-----( *)BEGIN(.*?)KEY( *)-----", "")
            .replaceAll("-----( *)END(.*?)KEY( *)-----", "")
            .replaceAll("\\s", "");
        return Base64.getDecoder().decode(pem);
    }

}
