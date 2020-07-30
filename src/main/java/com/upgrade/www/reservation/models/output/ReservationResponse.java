package com.upgrade.www.reservation.models.output;

import java.util.List;

public class ReservationResponse {
    private List<ReservationDetail> reservationDetails;
    private List<String> errors;

    public ReservationResponse(List<ReservationDetail> details, List<String> errors) {
        this.reservationDetails = details;
        this.errors = errors;
    }

    public List<ReservationDetail> getReservationDetails() {
        return reservationDetails;
    }

    public void setReservationDetails(List<ReservationDetail> reservationDetails) {
        this.reservationDetails = reservationDetails;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
