package com.mkyuan.fountaingateway.controller;

import java.io.Serializable;

public class RouteAdminException extends Exception implements Serializable {
    private static final long serialVersionUID = 1L;

    public RouteAdminException() {
    }

    public RouteAdminException(String msg) {
        super(msg);
    }

    public RouteAdminException(Throwable cause) {
        super(cause);
    }

    public RouteAdminException(String message, Throwable cause) {
        super(message, cause);
    }
}
