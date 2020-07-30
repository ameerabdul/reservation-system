package com.upgrade.www.reservation.models.input;

public class ReservationUpdateRequest
{
    private String bookingId;
    private String email;
    private String updatedStartDate;
    private String updatedEndDate;

    public ReservationUpdateRequest(String bookingId, String email, String updatedStartDate, String updatedEndDate) {
        this.bookingId = bookingId;
        this.email = email;
        this.updatedStartDate = updatedStartDate;
        this.updatedEndDate = updatedEndDate;
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

    public String getUpdatedStartDate() {
        return updatedStartDate;
    }

    public void setUpdatedStartDate(String updatedStartDate) {
        this.updatedStartDate = updatedStartDate;
    }

    public String getUpdatedEndDate() {
        return updatedEndDate;
    }

    public void setUpdatedEndDate(String updatedEndDate) {
        this.updatedEndDate = updatedEndDate;
    }
}
