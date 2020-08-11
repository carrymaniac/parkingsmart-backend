package com.oocl.parkingsmart.service;

import com.oocl.parkingsmart.model.ParkingLot;
import com.oocl.parkingsmart.repository.ParkingLotRepositoty;
import com.oocl.parkingsmart.websocket.protocol.data.PageRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class BookSearchServiceTest {
    @Test
    void should_return_parkingLotList_when_hit_search_given_times_longitude_latitude() {
        //given
        ParkingLotRepositoty mockParkingLotRepository = mock(ParkingLotRepositoty.class);
        //when
        PageRequest request = new PageRequest("113.574524","22.373737","2020/08","2020/09");
        ParkingLot parkingLot = new ParkingLot(1, "123", 10, 10, "123");
        List<ParkingLot> parkingLots = Collections.singletonList(parkingLot);
        BookSearchService bookSearchService = new BookSearchService(mockParkingLotRepository);
        given(mockParkingLotRepository.findNearbyParkingLot(request)).willReturn(parkingLots);
        //then
        List<ParkingLot> nearbyParkingLots = bookSearchService.findNearbyParkingLot(request);
        assertIterableEquals(parkingLots,nearbyParkingLots);
    }
}
