package com.jks.bank.entidades;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table
public class Conta implements Serializable {
	private static final long serialVersionUID = 1L;

	@Version
	private Long version;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String agencia;

	@Column(nullable = false, unique = true)
	private String numero;

	@Column(nullable = false, unique = true)
	private String chavePix;

	@Column(nullable = false)
	@PositiveOrZero
	private BigDecimal saldo;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private StatusDaConta status;

	@Column(nullable = false)
	private LocalDate dataDaCriacao;

	@Column(nullable = false)
	private String Cep;

	@JoinColumn(name = "id_usuario", nullable = false)
	@OneToOne
	private Usuario usuario;

	private Conta(Builder builder) {
		this.id = builder.id;
		this.agencia = builder.agencia;
		this.numero = builder.numero;
		this.chavePix = builder.chavePix;
		this.saldo = builder.saldo;
		this.status = builder.status;
		this.dataDaCriacao = builder.dataDaCriacao;
		this.Cep = builder.Cep;
		this.usuario = builder.usuario;
	}

	public Conta() {
	}

	public void bloquearConta() {
		usuario.setContaBloqueada(true);
		status = StatusDaConta.BLOQUEADA;
	}

	public void desbloquearConta() {
		usuario.setContaBloqueada(false);
		status = StatusDaConta.ATIVA;
	}

	public void encerrarConta() {
		usuario.setContaBloqueada(true);
		status = StatusDaConta.ENCERRADA;
	}

	public String getCep() {
		return Cep;
	}

	public void setCep(String cep) {
		Cep = cep;
	}

	public LocalDate getDataDaCriacao() {
		return dataDaCriacao;
	}

	public void setDataDaCriacao(LocalDate dataDaCriacao) {
		this.dataDaCriacao = dataDaCriacao;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getChavePix() {
		return chavePix;
	}

	public void setChavePix(String chavePix) {
		this.chavePix = chavePix;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAgencia() {
		return agencia;
	}

	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

	public StatusDaConta getStatus() {
		return status;
	}

	public void setStatus(StatusDaConta status) {
		this.status = status;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Long id;
		private String agencia;
		private String numero;
		private String chavePix;
		private BigDecimal saldo;
		private StatusDaConta status;
		private LocalDate dataDaCriacao;
		private String Cep;
		private Usuario usuario;

		private Builder() {
		}

		public Builder withId(Long id) {
			this.id = id;
			return this;
		}

		public Builder withAgencia(String agencia) {
			this.agencia = agencia;
			return this;
		}

		public Builder withNumero(String numero) {
			this.numero = numero;
			return this;
		}

		public Builder withChavePix(String chavePix) {
			this.chavePix = chavePix;
			return this;
		}

		public Builder withSaldo(BigDecimal saldo) {
			this.saldo = saldo;
			return this;
		}

		public Builder withStatus(StatusDaConta status) {
			this.status = status;
			return this;
		}

		public Builder withDataDaCriacao(LocalDate dataDaCriacao) {
			this.dataDaCriacao = dataDaCriacao;
			return this;
		}

		public Builder withCep(String Cep) {
			this.Cep = Cep;
			return this;
		}

		public Builder withUsuario(Usuario usuario) {
			this.usuario = usuario;
			return this;
		}

		public Conta build() {
			return new Conta(this);
		}
	}
}
