package com.theMs.sakany.shared.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException{
    private final String entityName;
    private final Object id;


    public NotFoundException(String entityName, Object id) {
        super(entityName + " with id '" + id + "' not found"  );
        this.entityName = entityName;
        this.id = id;
    }

}
