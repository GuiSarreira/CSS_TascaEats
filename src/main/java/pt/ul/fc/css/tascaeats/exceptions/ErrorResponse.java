package pt.ul.fc.css.tascaeats.exceptions;

import java.time.LocalDateTime;

/**
 * DTO de resposta para erros da API.
 *
 * Retornado pelo {@link GlobalExceptionHandler} em todos os cenários de erro,
 * garantindo um formato consistente independentemente do tipo de exceção.
 *
 * Exemplo de JSON devolvido:
 * {@code
 *  {
 *   "timestamp": "2026-04-09T14:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Restaurante não encontrado com o ID: 5"
 *  }
 * }
 */
public class ErrorResponse {

    /** Momento em que o erro ocorreu. */
    private LocalDateTime timestamp;

    /** Código HTTP do erro (ex: 404, 400, 422). */
    private int status;

    /** Descrição textual do código HTTP (ex: "Not Found"). */
    private String error;

    /** Mensagem de detalhe sobre o erro, proveniente da exceção. */
    private String message;

    public ErrorResponse(int status, String error, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}
