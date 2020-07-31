package com.upgrade.www.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upgrade.www.reservation.models.input.CancellationRequest;
import com.upgrade.www.reservation.models.input.ReservationRequest;
import com.upgrade.www.reservation.models.input.ReservationUpdateRequest;
import com.upgrade.www.reservation.models.output.ReservationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationSystemApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void testConcurrentBookingRequests() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		ReservationRequest request1 = new ReservationRequest("f1", "l1", "f1@l1.com", tomorrow.plusDays(10).toString(), tomorrow.plusDays(12).toString());
		ReservationRequest request2 = new ReservationRequest("f2", "l2", "f2@l2.com", tomorrow.plusDays(20).toString(), tomorrow.plusDays(21).toString());
		ReservationRequest exceedLengthOfStayRequest = new ReservationRequest("f3", "l3", "f3@l3.com", tomorrow.plusDays(1).toString(), tomorrow.plusDays(6).toString());
		ReservationRequest overlapReservation = new ReservationRequest("f4", "l4", "f4@l4.com", tomorrow.plusDays(10).toString(), tomorrow.plusDays(11).toString());
		ReservationRequest overAMonthReservation = new ReservationRequest("f5", "l5", "f5@l5.com", tomorrow.plusDays(45).toString(), tomorrow.plusDays(46).toString());
		ReservationRequest request6 = new ReservationRequest("f6", "l6", "f6@l6.com", tomorrow.plusDays(1).toString(), tomorrow.plusDays(4).toString());

		try
		{
			mockMvc.perform(post("/makeReservation").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request1))).andExpect(status().isOk());
			mockMvc.perform(post("/makeReservation").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request2))).andExpect(status().isOk());
			mockMvc.perform(post("/makeReservation").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(exceedLengthOfStayRequest))).andExpect(status().is4xxClientError());
			mockMvc.perform(post("/makeReservation").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(overlapReservation))).andExpect(status().is5xxServerError());
			mockMvc.perform(post("/makeReservation").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(overAMonthReservation))).andExpect(status().is4xxClientError());
			mockMvc.perform(post("/makeReservation").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request6))).andExpect(status().isOk());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Test
	void testModifyBookingRequests() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		ReservationRequest request1 = new ReservationRequest("f1", "l1", "f1@l1.com", tomorrow.plusDays(9).toString(), tomorrow.plusDays(10).toString());

		try
		{
			MvcResult mvcResult = mockMvc.perform(post("/makeReservation").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request1))).andReturn();
			String content = mvcResult.getResponse().getContentAsString();
			ReservationResponse reservationResponse = objectMapper.readerFor(ReservationResponse.class).readValue(content);
			String bookingId = reservationResponse.getReservationDetails().get(0).getId();

			ReservationUpdateRequest request2 = new ReservationUpdateRequest(bookingId, request1.getEmail(), tomorrow.plusDays(14).toString(), tomorrow.plusDays(15).toString());
			mockMvc.perform(post("/modifyReservation").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request2))).andExpect(status().isOk());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Test
	void testCancelBookingRequests() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		ReservationRequest request1 = new ReservationRequest("f1", "l1", "f1@l1.com", tomorrow.plusDays(4).toString(), tomorrow.plusDays(5).toString());

		try
		{
			MvcResult mvcResult = mockMvc.perform(post("/makeReservation").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request1))).andReturn();
			String content = mvcResult.getResponse().getContentAsString();
			ReservationResponse reservationResponse = objectMapper.readerFor(ReservationResponse.class).readValue(content);
			String bookingId = reservationResponse.getReservationDetails().get(0).getId();

			CancellationRequest request2 = new CancellationRequest(bookingId, request1.getEmail());
			mockMvc.perform(put("/cancelReservation").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request2))).andExpect(status().isOk());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
