package com.getbase.hackkrk.tanks.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class Config {
    @Value("${debug.ui}")
    private boolean showDebugUi;
}
