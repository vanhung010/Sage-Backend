package com.sagares.saga_res_api.feat.menu.exception;

import com.sagares.saga_res_api.common.exception.ResourceSavedException;

public class CategoryInsertException extends ResourceSavedException {
    public CategoryInsertException(String message) {
        super(message);
    }
}
