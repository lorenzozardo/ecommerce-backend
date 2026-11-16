package br.unisinos.ecommerce.exception;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<String> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return mensagem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<String> tratarRegraNegocio(RegraNegocioException ex) {
        return mensagem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(erro -> erros.put(erro.getField(), erro.getDefaultMessage()));
        return ResponseEntity.badRequest().body(erros);
    }

    // ---- Tratamentos complementares (evitam respostas 500 para erros do cliente) ----

    // Validação feita pelo Hibernate ao gravar a entidade
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> tratarViolacaoDeRestricao(ConstraintViolationException ex) {
        Map<String, String> erros = new HashMap<>();
        ex.getConstraintViolations()
            .forEach(violacao -> erros.put(violacao.getPropertyPath().toString(), violacao.getMessage()));
        return ResponseEntity.badRequest().body(erros);
    }

    // JSON mal formatado ou com tipo errado (ex.: "preco": "abc")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> tratarCorpoInvalido(HttpMessageNotReadableException ex) {
        return mensagem(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou mal formatado");
    }

    // Parâmetro de URL com tipo errado (ex.: /produtos/abc ou ?categoriaId=abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> tratarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return mensagem(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + ex.getName() + "'");
    }

    // Violação de restrição do banco (chave estrangeira, tamanho de coluna etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> tratarIntegridadeDeDados(DataIntegrityViolationException ex) {
        return mensagem(HttpStatus.CONFLICT, "A operação viola uma restrição de integridade dos dados");
    }

    // Mensagens simples são devolvidas como texto puro (text/plain; charset=UTF-8),
    // mesmo quando o cliente pede JSON (o axios envia "Accept: application/json, text/plain, */*")
    private ResponseEntity<String> mensagem(HttpStatus status, String texto) {
        return ResponseEntity.status(status).contentType(TEXTO_UTF8).body(texto);
    }

    private static final MediaType TEXTO_UTF8 = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);
}
