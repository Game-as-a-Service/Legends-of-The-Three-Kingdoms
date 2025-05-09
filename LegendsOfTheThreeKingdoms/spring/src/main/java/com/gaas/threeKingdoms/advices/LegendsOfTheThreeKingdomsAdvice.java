package com.gaas.threeKingdoms.advices;

import com.gaas.threeKingdoms.exception.NotFoundException;
import io.micrometer.common.util.StringUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class LegendsOfTheThreeKingdomsAdvice {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({RuntimeException.class})
    public String badRequest(RuntimeException exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .filter(StringUtils::isNotBlank)
                .collect(joining("\n"));
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler({NotFoundException.class})
    public String notFound(NotFoundException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public String otherException(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

}
