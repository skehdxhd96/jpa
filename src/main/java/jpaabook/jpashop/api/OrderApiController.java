package jpaabook.jpashop.api;

import jpaabook.jpashop.domain.Address;
import jpaabook.jpashop.domain.Order;
import jpaabook.jpashop.domain.OrderItem;
import jpaabook.jpashop.domain.OrderStatus;
import jpaabook.jpashop.repository.OrderRepository;
import jpaabook.jpashop.repository.OrderSearch;
import jpaabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpaabook.jpashop.repository.order.query.OrderQueryDto;
import jpaabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // order.getMember까지는 proxy, getName에 손 대는 순간 DB에 조회 시작.
            order.getDelivery().getAddress(); // order.getDelivery까지는 proxy, getAddress에 손 대는 순간 DB에 조회 시작.
            List<OrderItem> orderItems = order.getOrderItems(); // 아직 DB에 손 안댐.
            orderItems.stream().forEach(o -> o.getItem().getName()); // DB에 손대기 시작.
        }

        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> orderV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return collect;
    }

    @GetMapping("/api/v2/orders2")
    public List<OrderDto2> orderV22() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto2> collect = orders.stream()
                .map(o -> new OrderDto2(o))
                .collect(Collectors.toList());

        return collect;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto2> orderV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        List<OrderDto2> collect = orders.stream()
                .map(o -> new OrderDto2(o))
                .collect(Collectors.toList());

        return collect;
    };

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto2> orderv3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDeliveryPage(offset, limit);

        List<OrderDto2> collect = orders.stream()
                .map(o -> new OrderDto2(o))
                .collect(Collectors.toList());

        return collect;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> orderV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> orderV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @Data
    static class OrderDto {

        //지금까지 엔티티를 외부로 노출시키지 말라고 했다.
        //이유는 api스펙이 바뀌어버릴수 있기 때문. -> Dto사용
        //하지만 엔티티를 외부로 노출시키지 말라고 하는 것은 단순히 api함수 만들때 리턴타입 하나 dto로 바꾸는 것만으로 끝나는 것이 아니다.
        //OrderDto에도 List<OrderItem>처럼 엔티티가 들어가 있으면 안 된다. -> 엔티티가 의존하기 시작함.
        //List<OrderItem>조차도 DTO로 다 바꿔줘야 한다.

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItem> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderstatus();
            address = order.getDelivery().getAddress();
            order.getOrderItems().stream().forEach(o -> o.getItem().getName()); // OrderItem객체가 지연로딩때문에 초기화되지 않으므로 초기화 하고
            orderItems = order.getOrderItems(); // 넣어줘야함.
        }
    }

    @Data
    static class OrderDto2 {

        //OrderDto는 OrderDto2 처럼 바꿔야 한다.

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; // 주목!

        public OrderDto2(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderstatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem)).collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName; //상품명
        private int orderPrice; //주문가격
        private int count; //주문수량


        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
