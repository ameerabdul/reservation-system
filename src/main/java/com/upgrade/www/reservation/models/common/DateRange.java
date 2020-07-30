package com.upgrade.www.reservation.models.common;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class DateRange {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public DateRange(@NotNull LocalDate startDate, @NotNull LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
