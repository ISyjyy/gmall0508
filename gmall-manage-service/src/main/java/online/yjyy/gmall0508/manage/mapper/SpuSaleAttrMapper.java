package online.yjyy.gmall0508.manage.mapper;


import online.yjyy.gmall0508.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    // 根据spuId 查询销售属性列表
    List<SpuSaleAttr> selectSpuSaleAttrList(long spuId);

    // 通过spuId，skuId 查询销售属性，销售属性值
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(long skuId,long spuId);
}
