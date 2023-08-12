package com.example.filedemo.exception;

public class HubNotFoundException extends RuntimeException {
    public HubNotFoundException(String message) {
        super(message);
    }
}