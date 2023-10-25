package net.esati.spider.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

/**
 * @author Gao
 * @version 1.0
 * @description: 订单实体对象
 * @date 2023/8/9 23:32
 */
@Data
@Table("t_order")
public class Order {

    @Id
    private Integer orderId;
    private Integer goodsId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    private Integer status;
    private String mark;

}
