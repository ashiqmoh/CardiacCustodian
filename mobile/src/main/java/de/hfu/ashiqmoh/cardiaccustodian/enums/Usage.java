package de.hfu.ashiqmoh.cardiaccustodian.enums;

public enum Usage {
    DEFI, HELPER, PATIENT;

    @Override
    public String toString() {
        switch (this) {
            case DEFI:
                return "DEFI";
            case HELPER:
                return "HELPER";
            case PATIENT:
                return "PATIENT";
            default:
                throw new IllegalArgumentException();
        }
    }
}
