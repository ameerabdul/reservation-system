package com.upgrade.www.reservation.models.output;

import java.time.LocalDate;
import java.util.List;

public class AvailabilityDetailsResponse {
    private List<LocalDate> availableDates;
    private List<String> errors;

    public AvailabilityDetailsResponse(List<LocalDate> dates, List<String> errors) {
        this.availableDates = dates;
        this.errors = errors;
    }

    public List<LocalDate> getAvailableDates() {
        return availableDates;
    }

    public void setAvailableDates(List<LocalDate> availableDates) {
        this.availableDates = availableDates;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
