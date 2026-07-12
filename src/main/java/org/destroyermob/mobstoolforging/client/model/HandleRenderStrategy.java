package org.destroyermob.mobstoolforging.client.model;

import java.util.Locale;

public enum HandleRenderStrategy {
    DEFAULT_HANDLE("default_handle", true, true, false, true, false),
    EXACT_FIRST("exact_first", true, true, false, false, false),
    TEMPLATE_FIRST("template_first", true, true, true, false, false),
    TEMPLATE_ONLY("template_only", false, true, true, false, false),
    EXPLICIT_ONLY("explicit_only", true, false, false, false, false),
    MASKED_HANDLE("masked_handle", true, false, false, false, true),
    TEMPLATE_HANDLE("template_handle", true, true, true, false, false),
    EXPLICIT_HANDLE("explicit_handle", true, false, false, false, false);

    private final String id;
    private final boolean exactTextures;
    private final boolean templateFallback;
    private final boolean templateFirst;
    private final boolean compositeExactAndTemplate;
    private final boolean shapeMasked;

    HandleRenderStrategy(String id, boolean exactTextures, boolean templateFallback, boolean templateFirst, boolean compositeExactAndTemplate, boolean shapeMasked) {
        this.id = id;
        this.exactTextures = exactTextures;
        this.templateFallback = templateFallback;
        this.templateFirst = templateFirst;
        this.compositeExactAndTemplate = compositeExactAndTemplate;
        this.shapeMasked = shapeMasked;
    }

    public boolean usesExactTextures() {
        return exactTextures;
    }

    public boolean usesTemplateFallback() {
        return templateFallback;
    }

    public boolean prefersTemplate() {
        return templateFirst;
    }

    public boolean compositesExactAndTemplate() {
        return compositeExactAndTemplate;
    }

    public boolean usesShapeMask() {
        return shapeMasked;
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
