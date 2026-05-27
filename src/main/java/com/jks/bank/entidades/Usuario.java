package com.jks.bank.entidades;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false)
	private String login;

	@Column(nullable = false)
	private String cpf;

	@Column(nullable = false)
	private String senha;

	@Column(nullable = false)
	private LocalDate dataDaCriacao;

	@Column(nullable = false)
	private boolean contaBloqueada = false;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "usuario", cascade = CascadeType.ALL)
	private Conta conta;

	private Usuario(Builder builder) {
		this.id = builder.id;
		this.nome = builder.nome;
		this.login = builder.login;
		this.cpf = builder.cpf;
		this.senha = builder.senha;
		this.dataDaCriacao = builder.dataDaCriacao;
		this.contaBloqueada = builder.contaBloqueada;
		this.conta = builder.conta;
	}

	public Usuario() {
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.emptyList();
	}

	@Override
	public String getPassword() {
		return senha;
	}

	@Override
	public String getUsername() {
		return login;
	}

	public boolean isAccountLocked() {
		return this.contaBloqueada;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public Conta getConta() {
		return conta;
	}

	public void setConta(Conta conta) {
		this.conta = conta;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public LocalDate getDataDaCriacao() {
		return dataDaCriacao;
	}

	public boolean isContaBloqueada() {
		return contaBloqueada;
	}

	public void setContaBloqueada(boolean contaBloqueada) {
		this.contaBloqueada = contaBloqueada;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Long id;
		private String nome;
		private String login;
		private String cpf;
		private String senha;
		private LocalDate dataDaCriacao;
		private boolean contaBloqueada = false;
		private Conta conta;

		private Builder() {
		}

		public Builder withId(Long id) {
			this.id = id;
			return this;
		}

		public Builder withNome(String nome) {
			this.nome = nome;
			return this;
		}

		public Builder withLogin(String login) {
			this.login = login;
			return this;
		}

		public Builder withCpf(String cpf) {
			this.cpf = cpf;
			return this;
		}

		public Builder withSenha(String senha) {
			this.senha = senha;
			return this;
		}

		public Builder withDataDaCriacao(LocalDate dataDaCriacao) {
			this.dataDaCriacao = dataDaCriacao;
			return this;
		}

		public Builder withContaBloqueada(boolean contaBloqueada) {
			this.contaBloqueada = contaBloqueada;
			return this;
		}

		public Builder withConta(Conta conta) {
			this.conta = conta;
			return this;
		}

		public Usuario build() {
			return new Usuario(this);
		}
	}
}
