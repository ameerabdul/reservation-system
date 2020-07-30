package com.upgrade.www.reservation.repository;

import com.upgrade.www.reservation.models.common.DateRange;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BookingCache {

    /* ConcurrentHashMap to keep the reads faster but the writes will block the threads
     * Can be switched out to a distributed caching system
     * Also async thread can be used to remove dates in the past from the cache
     * Similarly we can expand the cache key to be a campsite id and date to support multiple sites
     */
    private final ConcurrentHashMap<LocalDate, Long> bookedDatesCache = new ConcurrentHashMap<>();

    public List<LocalDate> getAvailableDates(DateRange dateRange) {
        List<LocalDate> availableDates = new ArrayList<>();
        LocalDate date = dateRange.getStartDate();

        while (date.isBefore(dateRange.getEndDate())) {
            // Can be optimized to get all the keys at once if it is a distributed cache
            if (!bookedDatesCache.contains(date)) {
                availableDates.add(date);
            }
            date = date.plusDays(1);
        }

        return availableDates;
    }

    /**
     * Add booked dates to the cache asynchronously
     * Async due to the cache update need not be transactional update as part of booking
     * @param dateRange booked date range
     */
    @Async
    public void addBookedDates(DateRange dateRange) {
        LocalDate date = dateRange.getStartDate();
        while (date.isBefore(dateRange.getEndDate())) {
            bookedDatesCache.put(date, System.currentTimeMillis());
            date = date.plusDays(1);
        }
    }

    /**
     * Remove cancelled dates from the cache asynchronously
     * Async due to the cache update need not be transactional update change booking
     * @param dateRange cancelled date range
     */
    @Async
    public void removeBookedDates(DateRange dateRange) {
        LocalDate date = dateRange.getStartDate();
        while (date.isBefore(dateRange.getEndDate())) {
            bookedDatesCache.remove(date);
            date = date.plusDays(1);
        }
    }
}
