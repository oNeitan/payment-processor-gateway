package br.com.oneitan.model.enums;

public enum ServerContext {
    DEFAULT("default"),
    FALLBACK("fallback");

    private final String context;

    ServerContext(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }
}
