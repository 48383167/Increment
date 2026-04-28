package com.lin.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "lin.security")
public class SecurityProperties {
    private boolean enabled = true;
    private Token token = new Token();
    private List<String> whitelist = new ArrayList<>();

    @Data
    public static class Token {
        private String secret = "";
        private long expireSeconds = 7200;
    }

    public String[] getWhitelistArray() {
        return whitelist.toArray(new String[0]);
    }
}
