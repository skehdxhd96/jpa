package jpaabook.jpashop.domain.item;

import jpaabook.jpashop.domain.Category;
import jpaabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
//setter을 통한 값 변경 보다는 핵심 비즈니스로직을 넣어서 거기서 값을 변경하는것이 응집력이 높다.
public abstract class Item {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    //공통속성
    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    //==비즈니스 로직==//
    public void addStock(int quantity) { //재고수량 증가
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity) {

        int restStock = this.stockQuantity - quantity;
        if(restStock < 0) {
            //Exception : 0보다 작으면 안됨.
            throw new NotEnoughStockException("need more stock");
        }

        this.stockQuantity = restStock;
    }
}
