package org.destroyermob.mobstoolforging.client.model;

import java.util.Locale;

public enum HandleRenderStrategy {
    DEFAULT_HANDLE("default_handle", true),
    TEMPLATE_HANDLE("template_handle", true),
    EXPLICIT_HANDLE("explicit_handle", false);

    private final String id;
    private final boolean templateFallback;

    HandleRenderStrategy(String id, boolean templateFallback) {
        this.id = id;
        this.templateFallback = templateFallback;
    }

    public boolean usesTemplateFallback() {
        return templateFallback;
    }

    public String id() {
        return id;
    }

    public static HandleRenderStrategy parse(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        for (HandleRenderStrategy strategy : values()) {
            if (strategy.id.equals(normalized) || strategy.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return strategy;
            }
        }
        return DEFAULT_HANDLE;
    }
}
