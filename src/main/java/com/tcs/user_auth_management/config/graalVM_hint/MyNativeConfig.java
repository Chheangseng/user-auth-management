package com.tcs.user_auth_management.config.graalVM_hint;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(ResourceHints.class)
public class MyNativeConfig {
}