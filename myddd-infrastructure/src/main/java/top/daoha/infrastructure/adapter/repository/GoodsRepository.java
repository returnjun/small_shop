package top.daoha.infrastructure.adapter.repository;

import org.springframework.stereotype.Repository;
import top.daoha.domain.goods.adapter.repository.IGoodsRepository;
import top.daoha.infrastructure.dao.IOrderDao;

import javax.annotation.Resource;

@Repository
public class GoodsRepository implements IGoodsRepository {

    @Resource
    private IOrderDao iOrderDao;

    @Override
    public void changeOrderDealDone(String tradeNo) {
        iOrderDao.changeOrderDealDone(tradeNo);
    }
}
