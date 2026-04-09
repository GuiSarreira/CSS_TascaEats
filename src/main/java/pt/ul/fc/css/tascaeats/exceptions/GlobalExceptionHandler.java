package pt.ul.fc.css.tascaeats.exceptions;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Tratamento global de exceções da API TascaEats.
 *
 * Interceta as exceções lançadas pelos serviços e controllers e devolve
 * respostas HTTP com o código de estado adequado e um corpo JSON consistente
 * ({@link ErrorResponse}).
 *
 * Mapeamento de exceções:
 *   - {@link RuntimeException}                  → 404 Not Found (recurso não encontrado)
 *   - {@link IllegalStateException}             → 422 Unprocessable Entity (regra de negócio violada)
 *   - {@link IllegalArgumentException}          → 400 Bad Request (input inválido)
 *   - {@link SecurityException}                 → 403 Forbidden (sem permissão)
 *   - {@link OptimisticLockingFailureException} → 409 Conflict (concorrência otimista)
 *   - {@link Exception}                         → 500 Internal Server Error (erro inesperado)
 *
 * Nota: {@code IllegalStateException} e {@code IllegalArgumentException} são
 * subclasses de {@code RuntimeException}, por isso os seus handlers são declarados
 * primeiro para garantir que têm precedência.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata violações de regras de negócio.
     *
     * Exemplos: restaurante fechado, produto esgotado, pedido não cancelável,
     * entregador indisponível, utilizador com pedidos ativos.
     *
     * @param ex a exceção capturada
     * @return 422 Unprocessable Entity com a mensagem de detalhe
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(422, "Unprocessable Entity", ex.getMessage()));
    }

    /**
     * Trata erros de validação de input.
     *
     * Exemplos: email já registado, NIF duplicado, quantidade inválida,
     * mapa de itens vazio.
     *
     * @param ex a exceção capturada
     * @return 400 Bad Request com a mensagem de detalhe
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Bad Request", ex.getMessage()));
    }

    /**
     * Trata acessos não autorizados.
     *
     * Exemplos: utilizador não é Admin, admin a tentar editar restaurante de outro admin.
     *
     * @param ex a exceção capturada
     * @return 403 Forbidden com a mensagem de detalhe
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(SecurityException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(403, "Forbidden", ex.getMessage()));
    }

    /**
     * Trata conflitos de concorrência otimista (campo {@code @Version} no Pedido).
     *
     * Ocorre quando dois processos tentam atualizar o mesmo pedido em simultâneo
     * (ex: dois entregadores a aceitar o mesmo pedido ao mesmo tempo).
     *
     * @param ex a exceção capturada
     * @return 409 Conflict
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, "Conflict",
                        "O recurso foi modificado por outro processo. Por favor tente novamente."));
    }

    /**
     * Trata recursos não encontrados.
     *
     * Exemplos: cliente, restaurante, produto, pedido, pagamento ou entrega
     * com ID inexistente.
     *
     * Nota: declarado após os handlers mais específicos de {@code RuntimeException}
     * ({@code IllegalStateException}, {@code IllegalArgumentException}) para que
     * estes tenham precedência.
     *
     * @param ex a exceção capturada
     * @return 404 Not Found com a mensagem de detalhe  
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Not Found", ex.getMessage()));
    }

    /**
     * Apanha qualquer exceção não tratada pelos handlers anteriores.
     *
     * @param ex a exceção capturada
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal Server Error",
                        "Ocorreu um erro inesperado. Por favor contacte o suporte."));
    }
}
