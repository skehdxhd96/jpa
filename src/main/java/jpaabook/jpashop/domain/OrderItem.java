package jpaabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpaabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; // 주문당시의 가격

    private int count; // 주문당시의 수량

    //==생성 메서드==//
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count); // 재고까기

        return orderItem;
    }

    //==비즈니스로직==//
    /**
     * 주문취소
     */
    public void cancel() {
        //item 재고를 늘린다.
        getItem().addStock(count);
    }

    //==조회로직==//
    /**
     * 전체가격
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
