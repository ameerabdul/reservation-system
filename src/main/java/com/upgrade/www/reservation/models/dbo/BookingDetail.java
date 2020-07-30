package com.upgrade.www.reservation.models.dbo;

import com.upgrade.www.reservation.models.common.BookingStatus;
import com.upgrade.www.reservation.models.common.DateRange;

import java.time.LocalDate;
import java.util.UUID;

public class BookingDetail {

    private final String id;
    private final String email;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private BookingStatus status;

    public BookingDetail(String email, DateRange dateRange, BookingStatus status) {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.startDate = dateRange.getStartDate();
        this.endDate = dateRange.getEndDate();
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
