package com.upgrade.www.reservation.controller;

import com.upgrade.www.reservation.exceptions.InvalidInputException;
import com.upgrade.www.reservation.exceptions.ReservationException;
import com.upgrade.www.reservation.models.common.DateRange;
import com.upgrade.www.reservation.models.dbo.BookingDetail;
import com.upgrade.www.reservation.models.input.CancellationRequest;
import com.upgrade.www.reservation.models.input.ReservationRequest;
import com.upgrade.www.reservation.models.input.ReservationUpdateRequest;
import com.upgrade.www.reservation.models.output.AvailabilityDetailsResponse;
import com.upgrade.www.reservation.models.output.ReservationDetail;
import com.upgrade.www.reservation.models.output.ReservationResponse;
import com.upgrade.www.reservation.service.AvailabilityService;
import com.upgrade.www.reservation.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(value = "/", produces = "application/json")
public class ReservationController {

    private final AvailabilityService availabilityService;
    private final BookingService bookingService;

    @Autowired
    public ReservationController(AvailabilityService availabilityService, BookingService bookingService) {
        this.availabilityService = availabilityService;
        this.bookingService = bookingService;
    }

    /**
     * @param startDate startDate for the availability info
     * @param endDate endDate for the availability info
     * Defaults to a month from tomorrow for availability
     * @return Returns available dates for the campsite
     */
    @RequestMapping(value = "/getAvailability", method = RequestMethod.GET)
    @ResponseBody
    public AvailabilityDetailsResponse getAvailability(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            HttpServletResponse response
    ) {
        try {
            DateRange dateRange = parseDateRange(startDate, endDate);
            final List<LocalDate> dates = availabilityService.getAvailability(dateRange);
            return new AvailabilityDetailsResponse(dates, emptyList());
        } catch (InvalidInputException exception) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return new AvailabilityDetailsResponse(emptyList(), List.of(exception.getMessage()));
        }
    }

    /**
     * @param bookingId id for the reservation
     * @param email email id used for the reservation
     * @return ReservationResponse details about the reservation if found or error
     */
    @RequestMapping(value = "/getReservation", method = RequestMethod.GET)
    @ResponseBody
    public ReservationResponse getReservation(
            @RequestParam(value = "bookingId") String bookingId,
            @RequestParam(value = "email") String email,
            HttpServletResponse response
    ) {
        try {
            validateBookingInformation(bookingId, email);
            final BookingDetail bookingDetail = bookingService.getBooking(bookingId, email);
            if (bookingDetail == null) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return new ReservationResponse(emptyList(), List.of("No reservation details found"));
            } else {
                final List<ReservationDetail> details = List.of(
                    new ReservationDetail(bookingDetail.getId(), bookingDetail.getEmail(), bookingDetail.getStatus().name(), bookingDetail.getStartDate(), bookingDetail.getEndDate())
                );
                return new ReservationResponse(details, emptyList());
            }
        } catch (InvalidInputException exception) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return new ReservationResponse(emptyList(), List.of(exception.getMessage()));
        }
    }

    /**
     *
     * @param reservationRequest with email id, name and stay range
     * @return ReservationResponse successful booking details or error incase of no availability
     */
    @RequestMapping(value = "/makeReservation", method = RequestMethod.POST)
    @ResponseBody
    public ReservationResponse makeReservation(
            @RequestBody final ReservationRequest reservationRequest,
            HttpServletResponse response
    ) {
        try {
            validateReservationParameters(reservationRequest.getEmail(), reservationRequest.getFirstName(), reservationRequest.getLastName());
            final DateRange dateRange = parseDateRange(reservationRequest.getStartDate(), reservationRequest.getEndDate());
            final BookingDetail bookingDetail = bookingService.completeBooking(reservationRequest.getEmail(), dateRange);

            ReservationResponse reservationResponse;
            if (bookingDetail == null) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

                reservationResponse = new ReservationResponse(emptyList(), List.of("Unable to finish the booking please try again"));
            } else {
                final List<ReservationDetail> details = List.of(
                        new ReservationDetail(bookingDetail.getId(), bookingDetail.getEmail(), bookingDetail.getStatus().name(), bookingDetail.getStartDate(), bookingDetail.getEndDate())
                );

                reservationResponse = new ReservationResponse(details, emptyList());
            }

            return reservationResponse;
        } catch (InvalidInputException exception) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return new ReservationResponse(emptyList(), List.of(exception.getMessage()));
        } catch (ReservationException exception) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ReservationResponse(emptyList(), List.of(exception.getMessage()));
        }
    }

    /**
     * @param cancellationRequest with booking id and corresponding email id
     * @return ReservationResponse with cancellation details if successful or error in case no booking is found.
     */
    @RequestMapping(value = "/cancelReservation", method = RequestMethod.PUT)
    @ResponseBody
    public ReservationResponse cancelReservation(
            @RequestBody final CancellationRequest cancellationRequest,
            HttpServletResponse response
    ) {
        try {
            validateBookingInformation(cancellationRequest.getBookingId(), cancellationRequest.getEmail());
            final BookingDetail bookingDetail = bookingService.cancelBooking(cancellationRequest.getBookingId(), cancellationRequest.getEmail());

            ReservationResponse reservationResponse;
            if (bookingDetail == null) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                reservationResponse = new ReservationResponse(emptyList(), List.of("Unable to cancel the booking please try again"));
            } else {
                final List<ReservationDetail> details = List.of(
                        new ReservationDetail(bookingDetail.getId(), bookingDetail.getEmail(), bookingDetail.getStatus().name(), bookingDetail.getStartDate(), bookingDetail.getEndDate())
                );
                reservationResponse = new ReservationResponse(details, emptyList());
            }

            return reservationResponse;
        } catch (InvalidInputException exception) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return new ReservationResponse(emptyList(), List.of(exception.getMessage()));
        }
    }

    /**
     * reservationUpdateRequest: Assuming we can only modify booking dates but not email or names
     * @return Reservation response with old and new booking information
     */
    @RequestMapping(value = "/modifyReservation", method = RequestMethod.POST)
    @ResponseBody
    public ReservationResponse modifyReservation(
            @RequestBody final ReservationUpdateRequest reservationUpdateRequest,
            HttpServletResponse response
    ) {
        try {
            final DateRange dateRange = parseDateRange(reservationUpdateRequest.getUpdatedStartDate(), reservationUpdateRequest.getUpdatedEndDate());
            validateBookingInformation(reservationUpdateRequest.getBookingId(), reservationUpdateRequest.getEmail());
            final List<BookingDetail> bookingDetails = bookingService.modifyBooking(reservationUpdateRequest.getBookingId(), reservationUpdateRequest.getEmail(), dateRange);

            ReservationResponse reservationResponse;
            if (bookingDetails == null) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

                reservationResponse = new ReservationResponse(emptyList(), List.of("Unable to modify the booking please try again"));
            } else {
                final List<ReservationDetail> details = bookingDetails.stream()
                        .map( bookingDetail -> new ReservationDetail(bookingDetail.getId(), bookingDetail.getEmail(), bookingDetail.getStatus().name(), bookingDetail.getStartDate(), bookingDetail.getEndDate()))
                        .collect(toList());
                reservationResponse = new ReservationResponse(details, emptyList());
            }

            return reservationResponse;
        } catch (InvalidInputException exception) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());

            return new ReservationResponse(emptyList(), List.of(exception.getMessage()));
        } catch (ReservationException exception) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

            return new ReservationResponse(emptyList(), List.of(exception.getMessage()));
        }
    }

    private DateRange parseDateRange(String startDateParam, String endDateParam) throws InvalidInputException {
        if (!StringUtils.isEmpty(startDateParam) && !StringUtils.isEmpty(endDateParam)) {
            try {
                LocalDate startDate = LocalDate.parse(startDateParam, DateTimeFormatter.ISO_DATE);
                LocalDate endDate = LocalDate.parse(endDateParam, DateTimeFormatter.ISO_DATE);

                if (!startDate.isBefore(endDate)) {
                    throw new InvalidInputException("Start date should be before end date");
                }
                return new DateRange(startDate, endDate);
            } catch (DateTimeParseException exception) {
                throw new InvalidInputException("Invalid date parameters. Please provide YYYY-MM-DD format");
            }
        }

        return null;
    }

    private void validateBookingInformation(String bookingId, String email) throws InvalidInputException {
        if (StringUtils.isEmpty(bookingId) || StringUtils.isEmpty(email)) {
            throw new InvalidInputException("Need a valid booking id and email");
        }
    }

    private void validateReservationParameters(String email, String firstName, String lastName) throws InvalidInputException {
        // Assuming valid email pattern
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(firstName) || StringUtils.isEmpty(lastName)) {
            throw new InvalidInputException("Need a valid email, firstName and lastName for a booking");
        }
    }
}
