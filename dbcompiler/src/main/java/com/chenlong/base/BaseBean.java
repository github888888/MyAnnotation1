package com.chenlong.base;

import com.chenlong.anno.Column;
import com.chenlong.anno.ID;

import java.io.Serializable;

public class BaseBean implements Serializable {
    private static final long serialVersionUID = 1L;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ID
    @Column("id")
    public long id;
}
