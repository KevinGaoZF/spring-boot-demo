package net.esati.spider.web.dao;

import net.esati.spider.web.domain.Order;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Gao
 * @version 1.0
 * @description: TODO
 * @date 2023/8/9 23:31
 */
@Component
public interface OrderRepository extends PagingAndSortingRepository<Order, Integer> {

    @Query("select * FROM t_order")
    List<Order> selectOrder();

    @Query("select * FROM t_order where order_id = :orderId ")
    List<Order> selectOrderById(@Param("orderId") String dataId);

    @Modifying
    @Query(" update t_order set status = :status where order_id = :orderId ")
    int updateOrderStatus(@Param("orderId") String orderId, @Param("status") Integer status);

}
