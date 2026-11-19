package br.senac.alocasenac.config;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Autenticação e autorização por perfil (RF1; ADR-014 revisada, ADR-015).
 *
 * <p>A API ({@code /api/**}) aceita tokens JWT assinados com HMAC-SHA256. O perfil vem da claim {@code perfis}
 * (SOLICITANTE, RESPONSAVEL, ADMINISTRADOR) e o usuário, da claim {@code sub}. A autorização é aplicada na rota e
 * nos métodos dos controllers.
 */
@Configuration
@EnableMethodSecurity
public class SegurancaConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SegurancaConfig.class);
    private static final int TAMANHO_MINIMO_SEGREDO = 32;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    @Bean
    @Order(1)
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers(HttpMethod.POST, "/api/recursos").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/reservas").hasRole("SOLICITANTE")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDePerfis())))
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // API stateless com token no cabeçalho: não há cookie de sessão a proteger contra CSRF
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /** Tudo fora da API fica fechado até a interface Thymeleaf existir. */
    @Bean
    @Order(2)
    SecurityFilterChain restante(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(regras -> regras.anyRequest().denyAll());
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${alocasenac.jwt.segredo:}") String segredo) {
        SecretKey chave = new SecretKeySpec(bytesDoSegredo(segredo), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private static byte[] bytesDoSegredo(String segredo) {
        if (segredo != null && segredo.length() >= TAMANHO_MINIMO_SEGREDO) {
            return segredo.getBytes(StandardCharsets.UTF_8);
        }
        LOG.warn("ALOCASENAC_JWT_SEGREDO ausente ou com menos de {} caracteres: usando chave aleatória; "
                + "nenhum token externo será aceito nesta execução.", TAMANHO_MINIMO_SEGREDO);
        byte[] aleatoria = new byte[TAMANHO_MINIMO_SEGREDO];
        ALEATORIO.nextBytes(aleatoria);
        return aleatoria;
    }

    private static JwtAuthenticationConverter conversorDePerfis() {
        JwtGrantedAuthoritiesConverter perfis = new JwtGrantedAuthoritiesConverter();
        perfis.setAuthoritiesClaimName("perfis");
        perfis.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(perfis);
        return conversor;
    }
}
