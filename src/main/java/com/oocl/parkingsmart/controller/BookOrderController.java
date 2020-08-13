package com.oocl.parkingsmart.controller;

import com.oocl.parkingsmart.Enum.BookOrderEnum;
import com.oocl.parkingsmart.entity.BookOrder;
import com.oocl.parkingsmart.entity.CarSpace;
import com.oocl.parkingsmart.entity.RentOrder;
import com.oocl.parkingsmart.form.BookOrderForm;
import com.oocl.parkingsmart.model.ParkingLot;
import com.oocl.parkingsmart.service.BookOrderService;
import com.oocl.parkingsmart.service.CarSpaceService;
import com.oocl.parkingsmart.service.ParkingLotService;
import com.oocl.parkingsmart.utils.ResultVoUtils;
import com.oocl.parkingsmart.vo.ResultVo;
import com.oocl.parkingsmart.websocket.WebSocketServer;
import com.oocl.parkingsmart.websocket.protocol.data.PageRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/bookOrders")
@CrossOrigin
public class BookOrderController {
    @Autowired
    BookOrderService bookOrderService;
    @Autowired
    CarSpaceService carSpaceService;
    @Autowired
    ParkingLotService parkingLotService;
    @Autowired
    WebSocketServer webSocketServer;

    @GetMapping
    public ResultVo getAll(){
        return ResultVoUtils.success("success",bookOrderService.getAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResultVo<CarSpace> addBookOrder(@RequestBody BookOrderForm form) throws IOException, ParseException {
        BookOrder bookOrder = new BookOrder();
        BeanUtils.copyProperties(form,bookOrder);
        ResultVo<CarSpace> resultVo = new ResultVo<>();
        bookOrder.setStatus(BookOrderEnum.BOOKED.getValue());
        List<CarSpace> unUsedCarSpaces = carSpaceService.getUnUsedCarSpaces(bookOrder.getParkingId(), bookOrder.getReservationStartTime(), bookOrder.getReservationEndTime());
        if(unUsedCarSpaces.isEmpty()){
            resultVo.setCode(1);
            resultVo.setMsg("booking fail");
            return resultVo;
        }
        CarSpace carSpace = unUsedCarSpaces.get(0);
        bookOrder.setCarPort(carSpace.getCarPort());
        calculatePrice(bookOrder);
        if(bookOrderService.create(bookOrder)!=null){
            resultVo.setCode(0);
            resultVo.setMsg("booking success");
            resultVo.setData(carSpace);
            //todo
            PageRequest pageRequest = new PageRequest(form.getLatitude(),form.getLongitude(),form.getReservationStartTime().toString(),form.getReservationEndTime().toString());
            webSocketServer.sendList(bookOrder.getUserId(),pageRequest);
            return resultVo;
        }else {
            resultVo.setCode(1);
            resultVo.setMsg("booking fail");
            return resultVo;
        }
    }

    private void calculatePrice(BookOrder bookOrder) {
        Date reservationStartTime = bookOrder.getReservationStartTime();
        Date reservationEndTime = bookOrder.getReservationEndTime();
        long to = reservationEndTime.getTime();
        long from = reservationStartTime.getTime();
        int hours = (int)((to-from)/(1000*60*60));
        ParkingLot parkingLot = parkingLotService.findOne(bookOrder.getParkingId());
        bookOrder.setTotalPrice(hours*parkingLot.getPrice());
        bookOrder.setAddress(parkingLot.getAddress());
    }

    @PostMapping("/personal/{rentOrderId}")
    @ResponseStatus(HttpStatus.CREATED)
    ResultVo addPersonalBookOrder(@RequestBody BookOrder bookOrder,@PathVariable("rentOrderId") Integer rentOrderId){
        BookOrder returnedOrder = bookOrderService.createPersonalOrder(bookOrder, rentOrderId);
        if(returnedOrder != null){
            return ResultVoUtils.success("success",null);
        }
        return ResultVoUtils.fail("book fail");
    }

}
