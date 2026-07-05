package com.gaas.threeKingdoms.advices;

import com.gaas.threeKingdoms.exception.NotFoundException;
import io.micrometer.common.util.StringUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpStatus.*;

/**
 * 統一錯誤回應格式（issue #200）：
 * <pre>{ "error": "&lt;ExceptionSimpleName&gt;", "message": "&lt;human readable&gt;" }</pre>
 * Stack trace 一律留在 server log，不回傳給前端。
 */
@RestControllerAdvice
public class LegendsOfTheThreeKingdomsAdvice {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({RuntimeException.class})
    public Map<String, String> badRequest(RuntimeException exception) {
        exception.printStackTrace();
        return errorBody(exception);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .filter(StringUtils::isNotBlank)
                .collect(joining("\n"));
        return Map.of("error", "ValidationError", "message", message);
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler({NotFoundException.class})
    public Map<String, String> notFound(NotFoundException exception) {
        return errorBody(exception);
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public Map<String, String> otherException(Exception exception) {
        exception.printStackTrace();
        return errorBody(exception);
    }

    private static Map<String, String> errorBody(Exception exception) {
        String simpleName = exception.getClass().getSimpleName();
        String message = exception.getMessage() != null ? exception.getMessage() : simpleName;
        return Map.of("error", simpleName, "message", message);
    }
}
