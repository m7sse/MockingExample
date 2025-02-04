package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingSystemTest {

    private BookingSystem systemUnderTest;
    private RoomRepository roomRepo;
    private NotificationService notifier;
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        roomRepo = mock(RoomRepository.class);
        notifier = mock(NotificationService.class);
        timeProvider = mock(TimeProvider.class);
        systemUnderTest = new BookingSystem(timeProvider, roomRepo, notifier);
    }

    /**
     * Testar att boka ett rum framgångsrikt när rummet är tillgängligt.
     * Förväntat resultat: Boken ska genomföras och en bekräftelse skickas.
     */
    @Test
    void shouldSuccessfullyBookRoom() throws NotificationException {
        String roomId = "room1";
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);

        Room room = mock(Room.class);
        when(roomRepo.findById(roomId)).thenReturn(Optional.of(room));
        when(room.isAvailable(startTime, endTime)).thenReturn(true);
        when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());

        boolean isBooked = systemUnderTest.bookRoom(roomId, startTime, endTime);

        assertThat(isBooked).isTrue();
        verify(room).addBooking(any(Booking.class));
        verify(roomRepo).save(room);
        verify(notifier).sendBookingConfirmation(any(Booking.class));
    }

    /**
     * Testar att bokningen misslyckas om rummet inte är tillgängligt.
     * Förväntat resultat: Bokningen ska misslyckas och inga bekräftelser skickas.
     */
    @Test
    void shouldFailBookingWhenRoomIsNotAvailable() throws NotificationException {
        String roomId = "room1";
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);

        Room room = mock(Room.class);
        when(roomRepo.findById(roomId)).thenReturn(Optional.of(room));
        when(room.isAvailable(startTime, endTime)).thenReturn(false);
        when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());

        boolean isBooked = systemUnderTest.bookRoom(roomId, startTime, endTime);

        assertThat(isBooked).isFalse();
        verify(room, never()).addBooking(any(Booking.class));
        verify(roomRepo, never()).save(any(Room.class));
        verify(notifier, never()).sendBookingConfirmation(any(Booking.class));
    }

    /**
     * Testar att bokning kastar ett undantag vid ogiltiga argument (null starttid, sluttid eller rum-id).
     * Förväntat resultat: IllegalArgumentException ska kastas med lämpligt felmeddelande.
     */
    @Test
    void shouldThrowExceptionForInvalidBookingArguments() {
        assertThatThrownBy(() -> systemUnderTest.bookRoom("room1", null, LocalDateTime.now().plusHours(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");

        assertThatThrownBy(() -> systemUnderTest.bookRoom("room1", LocalDateTime.now(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");

        assertThatThrownBy(() -> systemUnderTest.bookRoom(null, LocalDateTime.now(), LocalDateTime.now().plusHours(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");
    }

    /**
     * Testar att bokning inte tillåts om starttiden är i det förflutna.
     * Förväntat resultat: IllegalArgumentException ska kastas.
     */
    @Test
    void shouldNotAllowBookingInThePast() {
        String roomId = "room1";
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);

        when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());

        assertThatThrownBy(() -> systemUnderTest.bookRoom(roomId, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Testar att bokning inte tillåts om sluttiden är före starttiden.
     * Förväntat resultat: IllegalArgumentException ska kastas med lämpligt felmeddelande.
     */
    @Test
    void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
        String roomId = "room1";
        LocalDateTime now = LocalDateTime.of(2025, 1, 30, 12, 0);
        LocalDateTime startTime = now.plusHours(1);
        LocalDateTime endTime = startTime.minusHours(1);

        when(timeProvider.getCurrentTime()).thenReturn(now);

        assertThatThrownBy(() -> systemUnderTest.bookRoom(roomId, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sluttid måste vara efter starttid");
    }

    /**
     * Testar att systemet returnerar rätt tillgängliga rum när rum finns som är tillgängliga för bokning.
     * Förväntat resultat: Endast tillgängliga rum ska inkluderas i listan.
     */
    @Test
    void shouldReturnAvailableRoomsCorrectly() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);

        Room room1 = mock(Room.class);
        Room room2 = mock(Room.class);
        Room room3 = mock(Room.class);

        when(room1.isAvailable(startTime, endTime)).thenReturn(true);
        when(room2.isAvailable(startTime, endTime)).thenReturn(false);
        when(room3.isAvailable(startTime, endTime)).thenReturn(true);

        when(roomRepo.findAll()).thenReturn(List.of(room1, room2, room3));

        List<Room> availableRooms = systemUnderTest.getAvailableRooms(startTime, endTime);

        assertThat(availableRooms).hasSize(2);
        assertThat(availableRooms).contains(room1, room3);
        assertThat(availableRooms).doesNotContain(room2);
    }

    /**
     * Testar att systemet kastar ett undantag när ogiltiga argument används vid hämtning av tillgängliga rum.
     * Förväntat resultat: IllegalArgumentException ska kastas.
     */
    @Test
    void shouldThrowExceptionForInvalidRoomQueryArguments() {
        assertThatThrownBy(() -> systemUnderTest.getAvailableRooms(null, LocalDateTime.now().plusHours(1)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> systemUnderTest.getAvailableRooms(LocalDateTime.now(), null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> systemUnderTest.getAvailableRooms(LocalDateTime.now().plusHours(1), LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Testar att avbokning av bokning fungerar korrekt när den är framtida.
     * Förväntat resultat: Bokningen ska tas bort och en avbokningsbekräftelse skickas.
     */
    @Test
    void shouldSuccessfullyCancelBooking() throws NotificationException {
        String bookingId = "booking1";
        LocalDateTime futureStartTime = LocalDateTime.now().plusHours(1);
        Booking booking = mock(Booking.class);

        Room room = mock(Room.class);
        when(room.hasBooking(bookingId)).thenReturn(true);
        when(room.getBooking(bookingId)).thenReturn(booking);
        when(booking.getStartTime()).thenReturn(futureStartTime);

        when(roomRepo.findAll()).thenReturn(List.of(room));
        when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());

        boolean isCancelled = systemUnderTest.cancelBooking(bookingId);

        assertThat(isCancelled).isTrue();
        verify(room).removeBooking(bookingId);
        verify(roomRepo).save(room);
        verify(notifier).sendCancellationConfirmation(booking);
    }

    /**
     * Testar att avbokning misslyckas om bokningen inte existerar.
     * Förväntat resultat: Avbokningen ska misslyckas och returnera false.
     */
    @Test
    void shouldFailToCancelNonExistentBooking() {
        String bookingId = "nonExistentBooking";

        when(roomRepo.findAll()).thenReturn(List.of());

        boolean isCancelled = systemUnderTest.cancelBooking(bookingId);

        assertThat(isCancelled).isFalse();
    }

    /**
     * Testar att avbokning misslyckas om boknings-id är null.
     * Förväntat resultat: IllegalArgumentException ska kastas.
     */
    @Test
    void shouldThrowExceptionForNullBookingIdDuringCancellation() {
        when(roomRepo.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> systemUnderTest.cancelBooking(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Testar att avbokning misslyckas om bokningen har påbörjats.
     * Förväntat resultat: IllegalStateException ska kastas.
     */
    @Test
    void shouldFailToCancelStartedBooking() {
        String bookingId = "booking1";
        LocalDateTime pastStartTime = LocalDateTime.now().minusHours(1);
        Booking booking = mock(Booking.class);

        Room room = mock(Room.class);
        when(room.hasBooking(bookingId)).thenReturn(true);
        when(room.getBooking(bookingId)).thenReturn(booking);
        when(booking.getStartTime()).thenReturn(pastStartTime);

        when(roomRepo.findAll()).thenReturn(List.of(room));
        when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());

        assertThatThrownBy(() -> systemUnderTest.cancelBooking(bookingId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kan inte avboka påbörjad eller avslutad bokning");
    }
}
