package com.sagares.saga_res_api.feat.menu.exception;

import com.sagares.saga_res_api.common.exception.ResourceNotFoundException;

public class MenuNotFoundException extends ResourceNotFoundException {
    public MenuNotFoundException(String message) {
        super(message);
    }
}
