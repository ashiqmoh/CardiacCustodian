package de.hfu.ashiqmoh.cardiaccustodian.objects;

import de.hfu.ashiqmoh.cardiaccustodian.enums.Usage;

public class CcNotifier {

    private String id;
    private User user;
    private Defi defi;
    private Usage usage;

    public CcNotifier(String id, User user, Defi defi, Usage usage) {
        this.id = id;
        this.user = user;
        this.defi = defi;
        this.usage = usage;
    }

}
