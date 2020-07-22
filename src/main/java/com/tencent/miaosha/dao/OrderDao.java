package com.tencent.miaosha.dao;

import com.tencent.miaosha.domain.MiaoshaOrder;
import com.tencent.miaosha.domain.OrderInfo;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {

    String TABLE_NAME = "order_info";
    String INSERT_FIELDS = " user_id, goods_id, delivery_addr_id, goods_name, goods_count, goods_price, order_channel, status, create_date, pay_date ";
    String SELECT_FIELDS = " id," + INSERT_FIELDS;
    @Select("select * from miaosha_order where user_id = #{id} and goods_id = #{goodsId}")
    MiaoshaOrder getMiaoshaUserIdGoodsId(@Param("id") long id, @Param("goodsId") long goodsId) ;

    @Insert({"insert into ", TABLE_NAME, "(",INSERT_FIELDS,") values(#{userId},#{goodsId},#{deliveryAddrId},#{goodsName},#{goodsCount},#{goodsPrice},#{orderChannel},#{status},#{createDate},#{payDate})" })
    @SelectKey(keyColumn ="id", keyProperty = "id", resultType = long.class, before = false,statement = "select last_insert_id()")
    long insert(OrderInfo orderInfo) ;

    @Insert("insert into miaosha_order(user_id, goods_id, order_id) values(#{userId},#{goodsId},#{orderId})")
    void insertMiaoshaOrder(MiaoshaOrder miaoshaOrder) ;

    @Select("select * from order_info where id = #{orderId}")
    OrderInfo getOrderById(@Param("orderId") long orderId);
}
