package top.daoha.domain.goods.service;


import org.springframework.stereotype.Service;
import top.daoha.domain.goods.adapter.repository.IGoodsRepository;

import javax.annotation.Resource;

@Service
public class GoodsService implements IGoodsService {

    @Resource
    private IGoodsRepository goodsRepository;


    @Override
    public void changeOrderDealDone(String tradeNo) {
        //改变订单的状态为完成--已发货
        goodsRepository.changeOrderDealDone(tradeNo);
    }
}
