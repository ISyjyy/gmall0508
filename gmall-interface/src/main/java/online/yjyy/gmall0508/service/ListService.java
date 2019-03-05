package online.yjyy.gmall0508.service;

import online.yjyy.gmall0508.bean.SkuLsInfo;
import online.yjyy.gmall0508.bean.SkuLsParams;
import online.yjyy.gmall0508.bean.SkuLsResult;

public interface ListService {
    // 真正的es的数据模型
    void saveSkuInfo(SkuLsInfo skuLsInfo);
    // 根据检索条件查询数据
    SkuLsResult search(SkuLsParams skuLsParams);
    // 根据skuId 来更新商品的热度排名
    void incrHotScore(String skuId);
}
