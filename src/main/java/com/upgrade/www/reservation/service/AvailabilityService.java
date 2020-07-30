package com.upgrade.www.reservation.service;

import com.upgrade.www.reservation.exceptions.InvalidInputException;
import com.upgrade.www.reservation.models.common.DateRange;
import com.upgrade.www.reservation.repository.BookingCache;
import com.upgrade.www.reservation.validators.DateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class AvailabilityService {

    private final BookingCache bookingCache;
    private final DateValidator dateValidator;

    // Assuming Pacific timezone for the campsite but can be made dynamic
    private static final ZoneId CAMPSITE_TIMEZONE = ZoneId.of("Pacific/Honolulu");
    private static final int MAX_PURCHASE_WINDOW = 30;

    @Autowired
    public AvailabilityService(BookingCache bookingCache, DateValidator dateValidator) {
        this.bookingCache = bookingCache;
        this.dateValidator = dateValidator;
    }

    public List<LocalDate> getAvailability(DateRange dateRange) throws InvalidInputException {
        DateRange updatedDateRange = dateRange;
        if (updatedDateRange == null) {
            updatedDateRange = getDefaultAvailabilityDateRange();
        }

        final boolean isValidDateRange = dateValidator.validateAvailabilityDataRange(updatedDateRange, CAMPSITE_TIMEZONE);
        if (!isValidDateRange) {
            throw new InvalidInputException("Invalid date range for availability");
        }
        return bookingCache.getAvailableDates(updatedDateRange);
    }

    private DateRange getDefaultAvailabilityDateRange() {
        LocalDate now = LocalDate.now(CAMPSITE_TIMEZONE);
        return new DateRange(now.plusDays(1), now.plusDays(MAX_PURCHASE_WINDOW));
    }
}
