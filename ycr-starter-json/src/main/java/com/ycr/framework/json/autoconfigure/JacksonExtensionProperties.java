package com.ycr.framework.json.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ycr.json")
public class JacksonExtensionProperties {

    private boolean bigNumberToString = true;

    public boolean isBigNumberToString() {
        return bigNumberToString;
    }

    public void setBigNumberToString(boolean bigNumberToString) {
        this.bigNumberToString = bigNumberToString;
    }
}
