package com.jks.bank.entidades;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table
public class Transacao {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private TipoTransacao tipo;

	@Column(nullable = false)
	private BigDecimal valor;

	@Column(nullable = false)
	private LocalDateTime data;

	@Column(nullable = false)
	private String descricao;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "conta_origem_id", nullable = true)
	private Conta contaOrigem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "conta_destino_id", nullable = true)
	private Conta contaDestino;

	@Column(nullable = false)
	private BigDecimal saldoAnterior;

	@Column(nullable = false)
	private BigDecimal saldoPosterior;

	private Transacao(Builder builder) {
		this.id = builder.id;
		this.tipo = builder.tipo;
		this.valor = builder.valor;
		this.data = builder.data;
		this.descricao = builder.descricao;
		this.contaOrigem = builder.contaOrigem;
		this.contaDestino = builder.contaDestino;
		this.saldoAnterior = builder.saldoAnterior;
		this.saldoPosterior = builder.saldoPosterior;
	}

	public Transacao() {
	}

	public Long getId() {
		return id;
	}
	

	public BigDecimal getSaldoAnterior() {
		return saldoAnterior;
	}

	public BigDecimal getSaldoPosterior() {
		return saldoPosterior;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TipoTransacao getTipo() {
		return tipo;
	}

	public void setTipo(TipoTransacao tipo) {
		this.tipo = tipo;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public LocalDateTime getData() {
		return data;
	}

	public void setData(LocalDateTime data) {
		this.data = data;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Conta getContaOrigem() {
		return contaOrigem;
	}

	public void setContaOrigem(Conta contaOrigem) {
		this.contaOrigem = contaOrigem;
	}

	public Conta getContaDestino() {
		return contaDestino;
	}

	public void setContaDestino(Conta contaDestino) {
		this.contaDestino = contaDestino;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Long id;
		private TipoTransacao tipo;
		private BigDecimal valor;
		private LocalDateTime data;
		private String descricao;
		private Conta contaOrigem;
		private Conta contaDestino;
		private BigDecimal saldoAnterior;
		private BigDecimal saldoPosterior;

		private Builder() {
		}

		public Builder withId(Long id) {
			this.id = id;
			return this;
		}

		public Builder withTipo(TipoTransacao tipo) {
			this.tipo = tipo;
			return this;
		}

		public Builder withValor(BigDecimal valor) {
			this.valor = valor;
			return this;
		}

		public Builder withData(LocalDateTime data) {
			this.data = data;
			return this;
		}

		public Builder withDescricao(String descricao) {
			this.descricao = descricao;
			return this;
		}

		public Builder withContaOrigem(Conta contaOrigem) {
			this.contaOrigem = contaOrigem;
			return this;
		}

		public Builder withContaDestino(Conta contaDestino) {
			this.contaDestino = contaDestino;
			return this;
		}

		public Builder withSaldoAnterior(BigDecimal saldoAnterior) {
			this.saldoAnterior = saldoAnterior;
			return this;
		}

		public Builder withSaldoPosterior(BigDecimal saldoPosterior) {
			this.saldoPosterior = saldoPosterior;
			return this;
		}

		public Transacao build() {
			return new Transacao(this);
		}
	}
}
