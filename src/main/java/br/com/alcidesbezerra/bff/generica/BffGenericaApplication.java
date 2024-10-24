package br.com.alcidesbezerra.bff.generica;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.com.alcidesbezerra.bff.generica.security.domain.Cpf;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class BffGenericaApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(BffGenericaApplication.class, args);
	}

	/**
	 * @param args
	 */
	@Override
	public void run(final String... args) throws Exception {
		Cpf cpf = new Cpf("01118438396", false);
		String cpfCriptografado = cpf.encrypt();
		log.info("CPF criptografado: {}", cpfCriptografado);

		Cpf cpfCrpt = new Cpf(cpfCriptografado);
		String cpfDescriptografado = cpfCrpt.decrypt();
		log.info("CPF descriptografado: {}", cpfDescriptografado);
	}
}
