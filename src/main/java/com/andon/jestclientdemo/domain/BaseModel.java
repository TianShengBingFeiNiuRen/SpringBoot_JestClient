package com.andon.jestclientdemo.domain;

import java.io.Serializable;

/**
 * @author Andon
 * @date 2019/5/27
 */
public abstract class BaseModel implements Serializable {

    public abstract String getPK();

    public abstract String getType();
}
