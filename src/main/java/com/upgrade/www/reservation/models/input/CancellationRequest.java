package com.upgrade.www.reservation.models.input;

public class CancellationRequest {
    private String bookingId;
    private String email;

    public CancellationRequest(String bookingId, String email) {
        this.bookingId = bookingId;
        this.email = email;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
