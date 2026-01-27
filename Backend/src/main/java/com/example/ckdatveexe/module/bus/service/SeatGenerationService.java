package com.example.ckdatveexe.module.bus.service;

import com.example.ckdatveexe.shared.entity.*;
import com.example.ckdatveexe.shared.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatGenerationService {

    private final SeatRepository seatRepository;

    @Transactional
    public void generateSeatsForBus(Bus bus) {
        // Xóa ghế cũ nếu có
        seatRepository.deleteByBusId(bus.getId());

        List<Seat> seats = new ArrayList<>();

        switch (bus.getBusType()) {
            case GHE_NGOI:
                seats = generateSeatsForGheNgoi(bus);
                break;
            case GIUONG_NAM:
                seats = generateSeatsForGiuongNam(bus);
                break;
            case LIMOUSINE:
                seats = generateSeatsForLimousine(bus);
                break;
        }

        seatRepository.saveAll(seats);
    }

    private List<Seat> generateSeatsForGheNgoi(Bus bus) {
        List<Seat> seats = new ArrayList<>();

        // Xe ghế ngồi thường có 4 cột (2 bên trái, lối đi, 2 bên phải)
        // Số hàng = capacity / 4 (làm tròn lên)
        int rows = (int) Math.ceil((double) bus.getCapacity() / 4);
        int seatNumber = 1;

        for (int row = 1; row <= rows; row++) {
            // Cột 1 và 2 (bên trái)
            for (int col = 1; col <= 2; col++) {
                if (seatNumber <= bus.getCapacity()) {
                    Seat seat = createSeat(bus, String.format("A%02d", seatNumber),
                            SeatType.NORMAL, row, col, 100000.0);
                    seats.add(seat);
                    seatNumber++;
                }
            }

            // Cột 3 và 4 (bên phải)
            for (int col = 4; col <= 5; col++) {
                if (seatNumber <= bus.getCapacity()) {
                    Seat seat = createSeat(bus, String.format("A%02d", seatNumber),
                            SeatType.NORMAL, row, col, 100000.0);
                    seats.add(seat);
                    seatNumber++;
                }
            }
        }

        return seats;
    }

    private List<Seat> generateSeatsForGiuongNam(Bus bus) {
        List<Seat> seats = new ArrayList<>();

        // Xe giường nằm thường có 3 cột (2 tầng mỗi bên + 1 tầng giữa)
        // Tầng dưới: 2 bên, tầng trên: 2 bên, tầng giữa: 1 bên
        int rows = (int) Math.ceil((double) bus.getCapacity() / 6); // 6 giường mỗi hàng
        int seatNumber = 1;

        for (int row = 1; row <= rows; row++) {
            // Tầng dưới bên trái
            if (seatNumber <= bus.getCapacity()) {
                Seat seat = createSeat(bus, String.format("T%02d", seatNumber),
                        SeatType.SLEEPER_LOWER, row, 1, 150000.0);
                seats.add(seat);
                seatNumber++;
            }

            // Tầng trên bên trái
            if (seatNumber <= bus.getCapacity()) {
                Seat seat = createSeat(bus, String.format("T%02d", seatNumber),
                        SeatType.SLEEPER_UPPER, row, 2, 140000.0);
                seats.add(seat);
                seatNumber++;
            }

            // Tầng dưới bên phải
            if (seatNumber <= bus.getCapacity()) {
                Seat seat = createSeat(bus, String.format("T%02d", seatNumber),
                        SeatType.SLEEPER_LOWER, row, 4, 150000.0);
                seats.add(seat);
                seatNumber++;
            }

            // Tầng trên bên phải
            if (seatNumber <= bus.getCapacity()) {
                Seat seat = createSeat(bus, String.format("T%02d", seatNumber),
                        SeatType.SLEEPER_UPPER, row, 5, 140000.0);
                seats.add(seat);
                seatNumber++;
            }

            // Tầng dưới giữa (nếu còn chỗ)
            if (seatNumber <= bus.getCapacity()) {
                Seat seat = createSeat(bus, String.format("T%02d", seatNumber),
                        SeatType.SLEEPER_LOWER, row, 3, 150000.0);
                seats.add(seat);
                seatNumber++;
            }

            // Tầng trên giữa (nếu còn chỗ)
            if (seatNumber <= bus.getCapacity()) {
                Seat seat = createSeat(bus, String.format("T%02d", seatNumber),
                        SeatType.SLEEPER_UPPER, row, 6, 140000.0);
                seats.add(seat);
                seatNumber++;
            }
        }

        return seats;
    }

    private List<Seat> generateSeatsForLimousine(Bus bus) {
        List<Seat> seats = new ArrayList<>();

        // Xe Limousine thường có 3 cột, ghế rộng rãi hơn
        // 2 ghế bên trái, 1 ghế bên phải hoặc ngược lại
        int rows = (int) Math.ceil((double) bus.getCapacity() / 3);
        int seatNumber = 1;

        for (int row = 1; row <= rows; row++) {
            // 2 ghế bên trái
            for (int col = 1; col <= 2; col++) {
                if (seatNumber <= bus.getCapacity()) {
                    Seat seat = createSeat(bus, String.format("L%02d", seatNumber),
                            SeatType.VIP, row, col, 200000.0);
                    seats.add(seat);
                    seatNumber++;
                }
            }

            // 1 ghế bên phải
            if (seatNumber <= bus.getCapacity()) {
                Seat seat = createSeat(bus, String.format("L%02d", seatNumber),
                        SeatType.VIP, row, 4, 200000.0);
                seats.add(seat);
                seatNumber++;
            }
        }

        return seats;
    }

    private Seat createSeat(Bus bus, String seatNumber, SeatType seatType,
            int row, int column, Double price) {
        Seat seat = new Seat();
        seat.setBus(bus);
        seat.setSeatNumber(seatNumber);
        seat.setSeatType(seatType);
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setRowNumber(row);
        seat.setColumnNumber(column);
        seat.setPriceForSeatType(price);
        return seat;
    }
}