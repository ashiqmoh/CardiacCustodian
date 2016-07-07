package de.hfu.ashiqmoh.cardiaccustodian;

import java.math.BigDecimal;

public class Location {

    private BigDecimal latitude;
    private BigDecimal longitude;

    public Location() {}

    public Location(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
}
