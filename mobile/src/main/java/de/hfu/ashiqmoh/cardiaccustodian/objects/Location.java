package de.hfu.ashiqmoh.cardiaccustodian.objects;

import java.math.BigDecimal;

public class Location {

    private BigDecimal latitude;
    private BigDecimal longitude;

    public Location(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
