package de.hfu.ashiqmoh.cardiaccustodian.enums;

public enum HttpMethod {
    POST, GET;

    @Override
    public String toString() {
        switch (this) {
            case POST:
                return "POST";
            case GET:
                return "GET";
            default:
                throw new IllegalArgumentException();
        }
    }
}
