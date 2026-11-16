package com.sagares.saga_res_api.feat.menu.exception;

import com.sagares.saga_res_api.common.exception.ResourceNotFoundException;

public class CategoryNotFoundException extends ResourceNotFoundException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
