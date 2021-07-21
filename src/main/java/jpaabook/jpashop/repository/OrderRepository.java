package jpaabook.jpashop.repository;

import jpaabook.jpashop.api.OrderSimpleApiController;
import jpaabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long orderId) {
        return em.find(Order.class, orderId);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.orderstatus = :orderstatus";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        } TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("orderstatus", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                            " join fetch o.member m" +
                            " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    public List<SimpleOrderQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new jpaabook.jpashop.repository.SimpleOrderQueryDto(o.id, m.name, o.orderDate, o.orderstatus, d.address) from Order o" +
                        " join o.member m" +
                        " join o.delivery d", SimpleOrderQueryDto.class)
                .getResultList();
    }

    public List<Order> findAllWithItem() {

        // 기본적으로 JPA 는 PK가 같으면 같은 객체로 취급한다.
        // fetch join 시, 데이터가 뻥튀기되는 결과가 발생하는데, 이는 join하려는 두 테이블의 low수가 맞지 않기 때문에 그렇다.
        // 기본적으로 DB는 low가 다르면 많은 쪽의 데이터를 더 삽입하기 때문이다.
        // 그렇기 때문에 JPA에서는 distinct라는 키워드를 하나 제공하는데, 이는 DB의 distinct와는 다르다.
        // DB의 distinct는 한 low가 아얘 똑같아야지만 중복을 제거해 주지만, JPA의 distinct는 가져오려는 객체의 pk가 같으면 알아서 하나를 날려버린다.

        // 단, 기본적으로 DB에서는 뻥튀기된 데이터가 있고, JPA에서 중복된걸 버려주는 시스템이기 때문에 DB에서 JPA에 전송하는 전송량 자체는 뻥튀기된 데이터의 양과 똑같다.
        /**
         * 1대 다 조건에서는 페이징을 걸면 안된다.
         */
        return em.createQuery(
                "select distinct o from Order o" +
                            " join fetch o.member m" +
                            " join fetch o.delivery d" +
                            " join fetch o.orderItems oi" +
                            " join fetch oi.item i", Order.class)
                .getResultList();
    }

    public List<Order> findAllWithMemberDeliveryPage(int offset, int limit) {

        // order3_page api
        // 일대 다 관계의 패치조인에서 페이징까지 최적화 하는 방법
        // 1. ToOne 관계까지는 일단 패치조인으로 땡겨온다.

        return em.createQuery(
                "select o from Order o" +
                            " join fetch o.member m" +
                            " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
