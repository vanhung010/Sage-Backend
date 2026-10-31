package com.sagares.saga_res_api.feat.menu.exception;

import com.sagares.saga_res_api.common.exception.ResourceSavedException;

public class MenuInsertException extends ResourceSavedException {
    public MenuInsertException(String message) {
        super(message);
    }
}
