package com.jks.bank.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.StatusDaConta;
import com.jks.bank.entidades.Usuario;

public class RetornaEntidades {
	public static Conta gerarConta(Usuario usuario) {
		return Conta.builder().withDataDaCriacao(LocalDate.now()).withSaldo(BigDecimal.ZERO).withCep("55730000")
				.withStatus(StatusDaConta.ATIVA).withAgencia("001").withChavePix(UUID.randomUUID().toString())
				.withNumero(String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999))).withUsuario(usuario).build();
	}
	public static Usuario gerarUsuario() {
		return Usuario.builder().withCpf("00000000000").withDataNasc(LocalDate.of(2007, 10, 20))
				.withLogin("Joao@gmail").withNome("joao").withSenha("123")
				.withTelefone("1234567890").withContaBloqueada(false).build();
	}
}
