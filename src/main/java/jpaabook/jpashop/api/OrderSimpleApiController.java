package jpaabook.jpashop.api;


import javassist.Loader;
import jpaabook.jpashop.domain.Address;
import jpaabook.jpashop.domain.Order;
import jpaabook.jpashop.domain.OrderStatus;
import jpaabook.jpashop.repository.OrderRepository;
import jpaabook.jpashop.repository.OrderSearch;
import jpaabook.jpashop.repository.SimpleOrderQueryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne : ManyToOne, OneToOne
 * Order -> Member(ManyToOne)
 * Order -> Delivery(OneToOne)
 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for(Order order : all) { // 배열로 하는건 안좋다.
            //hibernate5module이 proxy 강제 초기화 된건 데이터를 넘겨주고 초기화 안된 proxy 객체는 null을 넘겨줌.
            order.getMember().getName();
            //order.getMember()까지는 Member의 Order이 lazy이기때문에 order는 proxy객체가 넘어옴.
            //getName까지 하는순간 프록시 객체가 실제 엔티티를 가리키게 되면서 lazy햇던 proxy가 강제 초기화 되게 됨
            order.getDelivery().getAddress();
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public Result orderV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<SimpleOrderDto> result = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());

        return new Result(result.size(), result);
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v4/simple-orders")
    public List<SimpleOrderQueryDto> orderV4() {
        return orderRepository.findOrderDtos();
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name; // 주문한 사람
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address; // 배송지정보

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // lazy proxy 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderstatus();
            address = order.getDelivery().getAddress(); // lazy proxy 초기화
        }
    }

}
