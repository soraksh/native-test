package com.example.nativetest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleBean {
    Logger logger = LoggerFactory.getLogger(SampleBean.class);

    public String getMessage() {
        logger.info("getMessage() executed");
        return "Hello World!";
    }
}
