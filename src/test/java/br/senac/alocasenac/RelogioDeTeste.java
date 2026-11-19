package br.senac.alocasenac;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Relógio dos testes de integração: começa num instante fixo e só anda quando o teste manda.
 * Substitui o {@code Clock} da aplicação, para nenhum teste depender do relógio real (D-12).
 */
public class RelogioDeTeste extends Clock {

    /** Três dias antes do dia de referência dos testes ({@link Horarios#DIA}). */
    public static final Instant PADRAO = Instant.parse("2026-11-20T08:00:00Z");

    private volatile Instant agora = PADRAO;

    public void ajustar(Instant novoAgora) {
        this.agora = novoAgora;
    }

    public void voltarAoPadrao() {
        this.agora = PADRAO;
    }

    @Override
    public Instant instant() {
        return agora;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return this;
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class Configuracao {

        @Bean
        @Primary
        RelogioDeTeste relogioDeTeste() {
            return new RelogioDeTeste();
        }
    }
}
