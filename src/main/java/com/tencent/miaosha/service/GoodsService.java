package com.tencent.miaosha.service;

import com.tencent.miaosha.dao.GoodsDao;
import com.tencent.miaosha.domain.MiaoshaGoods;
import com.tencent.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    public final static Logger logger = LoggerFactory.getLogger(GoodsService.class);

    @Autowired(required = false)
    GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    public void reduceStock(GoodsVo goods) {
        MiaoshaGoods g = new MiaoshaGoods();
        g.setGoodsId(goods.getId());

        /*int stock = g.getStockCount();
        logger.info(String.valueOf(stock));*/
        goodsDao.reduceStock(g.getGoodsId());
        /*int afterStock = g.getStockCount();
        logger.info(String.valueOf(afterStock));*/

    }
}
