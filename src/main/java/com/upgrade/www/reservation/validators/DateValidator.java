package com.upgrade.www.reservation.validators;

import com.upgrade.www.reservation.models.common.DateRange;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
public class DateValidator {

    // Assuming 1-month as 31 days for advance purchase
    private static final int MAX_ADVANCE_PURCHASE = 31;
    private static final long MAX_STAY_LENGTH = 3;

    public boolean validateAvailabilityDataRange(DateRange dateRange, ZoneId zoneId) {
        return dateRange != null && validateStartDate(dateRange.getStartDate(), zoneId);
    }

    public boolean validateBookingDates(DateRange bookingDates, ZoneId zoneId) {
        if (bookingDates == null) return false;

        final long lengthOfStay = ChronoUnit.DAYS.between(bookingDates.getStartDate(), bookingDates.getEndDate());
        return lengthOfStay > 0 && lengthOfStay <= MAX_STAY_LENGTH && validateStartDate(bookingDates.getStartDate(), zoneId);
    }

    /**
     * Validates if the start date is after today and before 31 days (30 days as a month)
     * Assumption that startDate can be on day 30 which means a guest can stay upto day 33.
     * @param startDate booking sta
     * @return true if it is a valid booking range else false
     */
    private boolean validateStartDate(LocalDate startDate, ZoneId zoneId) {
        LocalDate now = LocalDate.now(zoneId);
        LocalDate validEndDate = now.plusDays(MAX_ADVANCE_PURCHASE);
        return startDate != null && startDate.isAfter(now) && startDate.isBefore(validEndDate);
    }
}
