package com.upgrade.www.reservation.repository;

import com.upgrade.www.reservation.models.common.DateRange;
import com.upgrade.www.reservation.models.dbo.BookingDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mock api to represent a booking database api
 */
@Repository
public class BookingRepository {

    private final BookingCache bookingCache;

    // Mock list to represent database table for booking records
    private final List<BookingDetail> bookingRecords;
    // Mock Set to represent database table for booked dates
    private final Set<LocalDate> bookedDates;

    @Autowired
    public BookingRepository(BookingCache bookingCache) {
        this.bookingRecords = new ArrayList<>();
        this.bookedDates = new HashSet<>();
        this.bookingCache = bookingCache;
    }

    public BookingDetail completeBooking(String email, DateRange dateRange) {

        bookingCache.addBookedDates(dateRange);
        return null;
    }

    public BookingDetail modifyBooking(BookingDetail existingBooking, DateRange newDateRange) {

        bookingCache.addBookedDates(newDateRange);
        bookingCache.removeBookedDates(new DateRange(existingBooking.getStartDate(), existingBooking.getEndDate()));
        return null;
    }

    public BookingDetail cancelBooking(BookingDetail existingBooking) {

        bookingCache.removeBookedDates(new DateRange(existingBooking.getStartDate(), existingBooking.getEndDate()));
        return null;
    }

    // Mock method to retrieve booking details from database
    public BookingDetail getBookingDetails(String bookingId, String email) {
        return bookingRecords.stream()
                .filter(booking -> booking.getId().equals(bookingId) && booking.getEmail().equals(email))
                .findFirst().orElse(null);
    }

    // Mock method to check if dates are available in the database instead of cache before booking
    private boolean isDateRangeAvailable(DateRange dateRange) {
        LocalDate date = dateRange.getStartDate();

        while (date.isBefore(dateRange.getEndDate())) {
            if (bookedDates.contains(date)) {
                return false;
            }
            date = date.plusDays(1);
        }

        return true;
    }
}
