package jpaabook.jpashop.domain.service;

import jpaabook.jpashop.domain.Delivery;
import jpaabook.jpashop.domain.Member;
import jpaabook.jpashop.domain.Order;
import jpaabook.jpashop.domain.OrderItem;
import jpaabook.jpashop.domain.item.Item;
import jpaabook.jpashop.repository.ItemRepository;
import jpaabook.jpashop.repository.MemberRepository;
import jpaabook.jpashop.repository.OrderRepository;
import jpaabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        //엔티티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        //배송정보 생성(실제로는 직접 입력해야함.)
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        //주문 저장(Cascade 옵션 이용. 다른데서도 갖다 쓸때는 함부로 Cascade쓰면 안됨)
        orderRepository.save(order);

        return order.getId();
    }

    /**
     * 주문 취소
     */
    public void cancelOrder(Long orderId) {

        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);

        //주문 취소
        order.cancel();
    }

//    검색
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAllByString(orderSearch);
    }
}
