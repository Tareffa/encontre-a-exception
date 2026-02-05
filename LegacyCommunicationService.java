import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class TokenParsingException extends RuntimeException {
    public TokenParsingException(String message) {
        super(message);
    }

    public TokenParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class LegacyCommunicationService {

    private static final int POS_CELULAR = 0;

    public String getCelularFromToken(String token) {
        return Optional.ofNullable(token)
                .filter(t -> !t.isBlank())
                .map(this::parseTokenLogic)
                .orElseThrow(() -> new TokenParsingException("Token nulo ou vazio não pode ser processado."));
    }

    // Exemplo de token: 65378;+55 (47) 90000-0000;{"data":"some data"};meta
    private String parseTokenLogic(String token) {
        if (!token.contains(";")) {
            return token.trim();
        }
        String[] parts = token.split(";");
        // Formato: [celular];[id_campanha];[metadata];meta
        return validateAndClean(parts[POS_CELULAR]);
    }

    private String validateAndClean(String rawCelular) {
        if (rawCelular == null || rawCelular.isBlank()) {
            throw new TokenParsingException("Campo de celular encontrado, mas está vazio.");
        }
        // Remove caracteres não numéricos (ex: +55 (11) ... -> 5511...)
        return rawCelular.replaceAll("[^0-9]", "");
    }

    public List<String> processBatch(List<String> rawTokens) {
        if (rawTokens == null) return Collections.emptyList(); // Proteção contra NPE
        return rawTokens.stream()
                .map(t -> {
                    try {
                        return Optional.ofNullable(getCelularFromToken(t));
                    } catch (TokenParsingException e) {
                        System.err.println("Erro de parsing: " + e.getMessage());
                        /*
                         * Imagine que há algum tratamento de erro aqui
                         */
                        return Optional.<String>empty();
                    } catch (Exception e) {
                        System.err.println("Erro inesperado no token: " + e.getMessage());
                        /*
                         * Localize o que está causando o erro inesperado
                         */
                        return Optional.<String>empty();
                    }
                })
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

}
