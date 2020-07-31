package com.upgrade.www.reservation.repository;

import com.upgrade.www.reservation.exceptions.ReservationException;
import com.upgrade.www.reservation.models.common.BookingStatus;
import com.upgrade.www.reservation.models.common.DateRange;
import com.upgrade.www.reservation.models.dbo.BookingDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock api to represent a booking database api
 * Opted for mocked list to keep things simple, however would loose the reservation information if restart the server.
 */
@Repository
public class BookingRepository {

    private final BookingCache bookingCache;

    // Mock list to represent database table for booking records
    private final Map<String, BookingDetail> bookingRecords;
    // Mock Set to represent database table for booked dates
    // Can be expanded to Map to keep track of booking dates multiple campsites in future
    private final Set<LocalDate> bookedDates;

    @Autowired
    public BookingRepository(BookingCache bookingCache) {
        this.bookingRecords = new ConcurrentHashMap<>();
        this.bookedDates = new HashSet<>();
        this.bookingCache = bookingCache;
    }

    public BookingDetail completeBooking(String email, DateRange dateRange) throws ReservationException {

        List<LocalDate> stayDates = getStayDates(dateRange.getStartDate(), dateRange.getEndDate());
        BookingDetail booking;
        // Synchronize to mimic atomic transaction
        synchronized (this) {
            final boolean isAvailable = isDateRangeAvailable(dateRange);
            if (!isAvailable) {
                throw new ReservationException("Camp site not available for the selected dates");
            }
            booking = new BookingDetail(email, dateRange, BookingStatus.CONFIRMED);
            bookingRecords.put(booking.getId(), booking);
            addBookedDates(stayDates);
        }

        // Doesn't need the cache update to be synchronized
        bookingCache.addBookedDates(stayDates);
        return booking;
    }

    public BookingDetail modifyBooking(BookingDetail existingBooking, DateRange newDateRange) throws ReservationException {

        List<LocalDate> newStayDates = getStayDates(newDateRange.getStartDate(), newDateRange.getEndDate());
        List<LocalDate> oldStayDates = getStayDates(existingBooking.getStartDate(), existingBooking.getEndDate());
        BookingDetail newBooking;
        // Synchronize to mimic atomic transaction
        synchronized (this) {
            final boolean isAvailable = isDateRangeAvailable(newDateRange);
            if (!isAvailable) {
                throw new ReservationException("Camp site not available for the selected dates");
            }
            newBooking = new BookingDetail(existingBooking.getEmail(), newDateRange, BookingStatus.CONFIRMED);
            existingBooking.setStatus(BookingStatus.CANCELLED);
            bookingRecords.put(existingBooking.getId(), existingBooking); //Updating existing booking
            bookingRecords.put(newBooking.getId(), newBooking); // Creating new booking
            removeBookedDates(oldStayDates);
            addBookedDates(newStayDates);
        }

        bookingCache.addBookedDates(newStayDates);
        bookingCache.removeBookedDates(oldStayDates);
        return newBooking;
    }

    public BookingDetail cancelBooking(BookingDetail existingBooking) {
        // Synchronize to mimic atomic transaction
        List<LocalDate> stayDates = getStayDates(existingBooking.getStartDate(), existingBooking.getEndDate());
        synchronized (this) {
            existingBooking.setStatus(BookingStatus.CANCELLED);
            bookingRecords.put(existingBooking.getId(), existingBooking); //Updating existing booking
            removeBookedDates(stayDates);
        }

        bookingCache.removeBookedDates(stayDates);
        return existingBooking;
    }

    public BookingDetail getBookingDetails(String bookingId, String email) {
        return retrieveBookingDetails(bookingId, email);
    }

    // Mock method to retrieve booking details from database
    private BookingDetail retrieveBookingDetails(String bookingId, String email) {
        final BookingDetail booking = bookingRecords.get(bookingId);
        if (booking != null && booking.getEmail().equals(email)) {
            return booking;
        } else {
            return null;
        }
    }

    private void addBookedDates(List<LocalDate> stayDates) {
        bookedDates.addAll(stayDates);
    }

    private void removeBookedDates(List<LocalDate> stayDates) {
        bookedDates.removeAll(stayDates);
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

    private List<LocalDate> getStayDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate date = startDate;

        while (date.isBefore(endDate)) {
            dates.add(date);
            date = date.plusDays(1);
        }

        return dates;
    }
}
