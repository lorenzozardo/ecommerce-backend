package br.unisinos.ecommerce.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    // 1. Construtor padrão sem argumentos
    public RecursoNaoEncontradoException() {
        super();
    }

    // 2. Construtor apenas com a mensagem de erro
    public RecursoNaoEncontradoException(String message) {
        super(message);
    }

    // 3. Construtor com a mensagem e a causa raiz (outra exceção)
    public RecursoNaoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }

    // 4. Construtor apenas com a causa raiz
    public RecursoNaoEncontradoException(Throwable cause) {
        super(cause);
    }
}
