package com.jks.bank.entidades;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_token")
public class RefreshToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "token", unique = true, nullable = false)
	private String token;
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;
	@Column(name = "expira_em", unique = true, nullable = false)
	private Instant expiraEm;

	private RefreshToken(Builder builder) {
		this.id = builder.id;
		this.token = builder.token;
		this.usuario = builder.usuario;
		this.expiraEm = builder.expiraEm;
	}

	public boolean estaExpirado() {
		if (expiraEm.isBefore(Instant.now())) {
			return true;
		}
		return false;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Instant getexpiraEm() {
		return expiraEm;
	}

	public void setexpiraEm(Instant expiraEm) {
		this.expiraEm = expiraEm;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Long id;
		private String token;
		private Usuario usuario;
		private Instant expiraEm;

		private Builder() {
		}

		public Builder withId(Long id) {
			this.id = id;
			return this;
		}

		public Builder withToken(String token) {
			this.token = token;
			return this;
		}

		public Builder withUsuario(Usuario usuario) {
			this.usuario = usuario;
			return this;
		}

		public Builder withExpiraEm(Instant expiraEm) {
			this.expiraEm = expiraEm;
			return this;
		}

		public RefreshToken build() {
			return new RefreshToken(this);
		}
	}

}
