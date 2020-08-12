package com.oocl.parkingsmart.service;

import com.oocl.parkingsmart.entity.BookOrder;
import com.oocl.parkingsmart.model.ParkingLot;
import com.oocl.parkingsmart.repository.BookOrderRepository;
import com.oocl.parkingsmart.repository.ParkingLotRepositoty;
import com.oocl.parkingsmart.websocket.protocol.data.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service(value = "BookSearchService")
public class BookSearchService {
    @Autowired
    private ParkingLotRepositoty parkingLotRepositoty;
    @Autowired
    private BookOrderRepository bookOrderRepository;

    public BookSearchService(ParkingLotRepositoty parkingLotRepository, BookOrderRepository bookOrderRepository) {
        this.parkingLotRepositoty = parkingLotRepository;
        this.bookOrderRepository = bookOrderRepository;
    }

    public List<ParkingLot> findNearbyParkingLot(PageRequest pageRequest) throws ParseException {
        Double longitude = Double.valueOf(pageRequest.getLongitude());
        Double latitude = Double.valueOf(pageRequest.getLatitude());
        List<ParkingLot> nearbyParkingLots = parkingLotRepositoty.findAllNearbyParkingLot(longitude, latitude);
        calculationMargin(pageRequest, nearbyParkingLots);
        return nearbyParkingLots;
    }

    public void calculationMargin(PageRequest pageRequest, List<ParkingLot> nearbyParkingLots) throws ParseException {
        if (nearbyParkingLots == null) return;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = simpleDateFormat.parse(pageRequest.getStartTime());
        Date endTime = simpleDateFormat.parse(pageRequest.getEndTime());
        for (ParkingLot parkingLot : nearbyParkingLots) {
            List<BookOrder> bookOrders = bookOrderRepository.findByStatusNotFINISHED(parkingLot.getId());
            Integer count = 0;
            for (BookOrder order : bookOrders) {
                if (order.getReservationStartTime() == null) return;
                int compareToFirst = order.getReservationStartTime().compareTo(endTime);
                int compareToSecond = order.getReservationEndTime().compareTo(startTime);
                if (compareToFirst <= 0 && compareToSecond >= 0) {
                    count++;
                }
            }
            parkingLot.setSize(parkingLot.getSize() - count);
        }
    }

    public List<BookOrder> getBookOrdersUsedByStartTimeAndEndTimeAndParkingLotId(Integer parkingLotId, Date startTime, Date endTime) {
        List<BookOrder> bookOrders = bookOrderRepository.findByStatusNotFINISHED(parkingLotId);
        List<BookOrder> result = new ArrayList<>();
        for (BookOrder order : bookOrders) {
            int compareToFirst = order.getReservationStartTime().compareTo(endTime);
            int compareToSecond = order.getReservationEndTime().compareTo(startTime);
            if (compareToFirst <= 0 && compareToSecond >= 0) {
                result.add(order);
            }
        }
        return result;
    }
}
