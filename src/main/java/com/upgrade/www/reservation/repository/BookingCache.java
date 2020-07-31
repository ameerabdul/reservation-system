package com.upgrade.www.reservation.repository;

import com.upgrade.www.reservation.models.common.DateRange;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache which gets updated Async whenever bookings are done. Provides faster lookup of availability for users.
 * Cache can be slightly stale as it is only used to check availability
 * All booking confirmations read the database records to confirm availability
 */
@Component
public class BookingCache {

    /* ConcurrentHashMap to keep the reads faster but the writes will block the threads
     * Can be switched out to an external distributed caching system for faster availability checks
     * Async thread can be used to remove dates in the past from the cache
     * Similarly we can expand the cache key to be a campsite id and date to support multiple sites
     */
    private final ConcurrentHashMap<String, LocalDate> bookedDatesCache = new ConcurrentHashMap<>();

    public List<LocalDate> getAvailableDates(DateRange dateRange) {
        List<LocalDate> availableDates = new ArrayList<>();
        LocalDate date = dateRange.getStartDate();

        while (date.isBefore(dateRange.getEndDate())) {
            // Can be optimized to get all the keys at once if it is a distributed cache
            if (!bookedDatesCache.containsKey(date.toString())) {
                availableDates.add(date);
            }
            date = date.plusDays(1);
        }

        return availableDates;
    }

    /**
     * Add booked dates to the cache asynchronously
     * Async as the cache update need not be transactional update as part of booking
     * @param stayDates booked date range
     */
    @Async
    public void addBookedDates(List<LocalDate> stayDates) {
        for (LocalDate date : stayDates) {
            bookedDatesCache.put(date.toString(), date);
        }
    }

    /**
     * Remove cancelled dates from the cache asynchronously
     * Async due to the cache update need not be transactional update change booking
     * @param stayDates cancelled date range
     */
    @Async
    public void removeBookedDates(List<LocalDate> stayDates) {
        for (LocalDate date : stayDates) {
            bookedDatesCache.remove(date.toString());
        }
    }
}
