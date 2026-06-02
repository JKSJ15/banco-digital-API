package com.jks.bank.repositorios;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jks.bank.entidades.TipoTransacao;
import com.jks.bank.entidades.Transacao;

public interface RepositorioTransacao extends JpaRepository<Transacao, Long> {
	Page<Transacao> findByContaOrigemIdOrContaDestinoId(Long id, Long id1, Pageable pageable);

	@Query("""
			    SELECT t
			    FROM Transacao t
			    WHERE (t.contaOrigem.id = :contaId
			           OR t.contaDestino.id = :contaId)
			      AND t.data BETWEEN :inicio AND :fim
			""")
	Page<Transacao> buscarExtratoPorPeriodo(@Param("contaId") Long contaId, @Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim, Pageable pageable);

	@Query("""
			SELECT COALESCE(SUM(t.valor), 0)
			FROM Transacao t
			WHERE t.tipo = :tipo
			AND t.contaOrigem.id = :contaId
			AND t.data BETWEEN :inicio AND :fim
			""")
	BigDecimal somarValoresPorPeriodo(@Param("tipo") TipoTransacao tipo, @Param("contaId") Long contaId,
			@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

	@Query("""
			    SELECT COUNT(t)
			    FROM Transacao t
			    WHERE t.tipo = :tipo
			      AND t.contaOrigem.id = :contaId
			      AND t.data BETWEEN :inicio AND :fim
			""")
	Long quantidadePixMes(@Param("tipo") TipoTransacao tipo, @Param("contaId") Long contaId,
			@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

	@Query("""
			    SELECT COALESCE(SUM(t.valor), 0)
			    FROM Transacao t
			    WHERE t.contaOrigem.id = :contaId
			      AND t.data BETWEEN :inicio AND :fim
			""")
	BigDecimal totalEnviadoMes(@Param("contaId") Long contaId, @Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim);

	@Query("""
			    SELECT COALESCE(SUM(t.valor), 0)
			    FROM Transacao t
			    WHERE t.contaDestino.id = :contaId
			      AND t.data BETWEEN :inicio AND :fim
			""")
	BigDecimal totalRecebidoMes(@Param("contaId") Long contaId, @Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim);
}
