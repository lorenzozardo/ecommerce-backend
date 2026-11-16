package br.unisinos.ecommerce.exception;

public class RegraNegocioException extends RuntimeException {

    // 1. Construtor padrão sem argumentos
    public RegraNegocioException() {
        super();
    }

    // 2. Construtor apenas com a mensagem de erro
    public RegraNegocioException(String message) {
        super(message);
    }

    // 3. Construtor com a mensagem e a causa raiz (outra exceção)
    public RegraNegocioException(String message, Throwable cause) {
        super(message, cause);
    }

    // 4. Construtor apenas com a causa raiz
    public RegraNegocioException(Throwable cause) {
        super(cause);
    }
}
