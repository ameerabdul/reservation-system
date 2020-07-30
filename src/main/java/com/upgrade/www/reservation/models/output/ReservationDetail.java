package com.upgrade.www.reservation.models.output;

import java.time.LocalDate;

public class ReservationDetail {
    private String id;
    private String email;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;

    public ReservationDetail(String id, String email, String status, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.email = email;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
