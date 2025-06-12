package com.retailmax.inventario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // Mapea esta excepción a un código de estado HTTP 409 Conflict
public class ProductoExistenteException extends RuntimeException {

    public ProductoExistenteException(String message) {
        super(message);
    }
}
