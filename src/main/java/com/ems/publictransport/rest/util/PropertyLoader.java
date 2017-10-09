package com.ems.publictransport.rest.util;

import org.apache.deltaspike.core.api.config.PropertyFileConfig;

public class PropertyLoader implements PropertyFileConfig {
    @Override
    public String getPropertyFileName() {
        return "application.properties";
    }

    @Override
    public boolean isOptional() {
        return false;
    }
}
