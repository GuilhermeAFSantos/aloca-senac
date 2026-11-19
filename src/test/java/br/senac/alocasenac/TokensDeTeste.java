package br.senac.alocasenac;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Gera tokens JWT HS256 sintéticos para os testes, assinados à mão com HMAC-SHA256.
 * Assim é possível assinar com qualquer tamanho de segredo, inclusive os curtos que a aplicação deve recusar.
 */
public final class TokensDeTeste {

    /** Expiração fixa em 2100-01-01, para o teste não depender do relógio. */
    private static final long EXPIRACAO = 4_102_444_800L;

    private TokensDeTeste() {
    }

    public static String assinar(String segredo, String usuario, List<String> perfis) {
        String perfisJson = perfis.stream().map(p -> "\"" + p + "\"").collect(Collectors.joining(",", "[", "]"));
        String cabecalho = base64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String conteudo = base64("{\"sub\":\"" + usuario + "\",\"perfis\":" + perfisJson + ",\"exp\":" + EXPIRACAO + "}");
        String assinatura = hmac(segredo, cabecalho + "." + conteudo);
        return cabecalho + "." + conteudo + "." + assinatura;
    }

    private static String base64(String texto) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private static String hmac(String segredo, String dados) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(dados.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao assinar o token de teste", e);
        }
    }
}
