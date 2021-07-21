package jpaabook.jpashop.domain.service;

import jpaabook.jpashop.domain.Address;
import jpaabook.jpashop.domain.Member;
import jpaabook.jpashop.domain.Order;
import jpaabook.jpashop.domain.OrderStatus;
import jpaabook.jpashop.domain.item.Book;
import jpaabook.jpashop.exception.NotEnoughStockException;
import jpaabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception{
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, getOrder.getOrderstatus()); //상태
        assertEquals(1, getOrder.getOrderItems().size());//수
        assertEquals(10000 * orderCount, getOrder.getTotalPrice());//가격
        assertEquals(10 - orderCount, book.getStockQuantity());//재고
//        assertEquals(10, book.getStockQuantity());//재고

    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.CANCEL, getOrder.getOrderstatus()); // 상태
        assertEquals(10, book.getStockQuantity());
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        //when
        try {
            orderService.order(member.getId(), book.getId(), orderCount);
        } catch(NotEnoughStockException e) {
            return;
        }

        //then
        fail("재고 수량 부족 예외가 발생해야 한다."); //여기까지 오면 틀린 것.
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }
}