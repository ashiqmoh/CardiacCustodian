package de.hfu.ashiqmoh.cardiaccustodian.objects;

import de.hfu.ashiqmoh.cardiaccustodian.enums.Usage;

public class CcEmergency {

    private String id;
    private User user;
    private String defi;
    private Usage usage;

    public CcEmergency(String id, User user, String defi, Usage usage) {
        this.id = id;
        this.user = user;
        this.defi = defi;
        this.usage = usage;
    }
}
