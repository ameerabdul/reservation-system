package com.upgrade.www.reservation.service;

import com.upgrade.www.reservation.exceptions.InvalidInputException;
import com.upgrade.www.reservation.exceptions.ReservationException;
import com.upgrade.www.reservation.models.common.DateRange;
import com.upgrade.www.reservation.models.dbo.BookingDetail;
import com.upgrade.www.reservation.repository.BookingRepository;
import com.upgrade.www.reservation.validators.DateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Service
public class BookingService {

    private final DateValidator dateValidator;
    private final BookingRepository bookingRepository;

    // Assuming Pacific timezone for the campsite but can be made dynamic
    private static final ZoneId CAMPSITE_TIMEZONE = ZoneId.of("Pacific/Honolulu");

    @Autowired
    public BookingService(DateValidator dateValidator, BookingRepository bookingRepository) {
        this.dateValidator = dateValidator;
        this.bookingRepository = bookingRepository;
    }

    public BookingDetail completeBooking(String email, DateRange dateRange) throws InvalidInputException, ReservationException
    {
        validateBookingDates(dateRange);
        return bookingRepository.completeBooking(email, dateRange);
    }

    public BookingDetail modifyBooking(String bookingId, String email, DateRange newDateRange) throws InvalidInputException, ReservationException
    {
        validateBookingDates(newDateRange);
        final BookingDetail existingBooking = bookingRepository.getBookingDetails(bookingId, email);

        if (existingBooking == null) {
            throw new InvalidInputException("No booking details for the booking id and email provided");
        }

        return bookingRepository.modifyBooking(existingBooking, newDateRange);
    }

    public BookingDetail cancelBooking(String bookingId, String email) throws InvalidInputException {
        final BookingDetail existingBooking = bookingRepository.getBookingDetails(bookingId, email);

        if (existingBooking == null) {
            throw new InvalidInputException("No booking details for the booking id and email provided");
        }

        return bookingRepository.cancelBooking(existingBooking);
    }

    public BookingDetail getBooking(String bookingId, String email) {
        return bookingRepository.getBookingDetails(bookingId, email);
    }

    private void validateBookingDates(DateRange dateRange) throws InvalidInputException {
        final boolean isValidDate = dateValidator.validateBookingDates(dateRange, CAMPSITE_TIMEZONE);

        if (!isValidDate) {
            throw new InvalidInputException("Invalid date range for booking. Should be at least one day prior to today and within a month");
        }
    }
}
