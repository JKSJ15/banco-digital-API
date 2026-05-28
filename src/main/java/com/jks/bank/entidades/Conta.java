package com.jks.bank.entidades;

import java.math.BigDecimal;

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
public class Conta {
	@Version
	private Long version;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String agencia;

	@Column(nullable = false)
	private Long numero;
	
	@Column(nullable = false)
	private String chavePix;

	@Column(nullable = false)
	@PositiveOrZero
	private BigDecimal saldo;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private StatusDaConta status;

	@JoinColumn(name = "id_usuario", nullable = false)
	@OneToOne
	private Usuario usuario;

	private Conta(Builder builder) {
		this.version = builder.version;
		this.id = builder.id;
		this.agencia = builder.agencia;
		this.numero = builder.numero;
		this.saldo = builder.saldo;
		this.status = builder.status;
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

	public Long getNumero() {
		return numero;
	}

	public void setNumero(Long numero) {
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
		private Long version;
		private Long id;
		private String agencia;
		private Long numero;
		private BigDecimal saldo;
		private StatusDaConta status;
		private Usuario usuario;

		private Builder() {
		}

		public Builder withVersion(Long version) {
			this.version = version;
			return this;
		}

		public Builder withId(Long id) {
			this.id = id;
			return this;
		}

		public Builder withAgencia(String agencia) {
			this.agencia = agencia;
			return this;
		}

		public Builder withNumero(Long numero) {
			this.numero = numero;
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

		public Builder withUsuario(Usuario usuario) {
			this.usuario = usuario;
			return this;
		}

		public Conta build() {
			return new Conta(this);
		}
	}
}
