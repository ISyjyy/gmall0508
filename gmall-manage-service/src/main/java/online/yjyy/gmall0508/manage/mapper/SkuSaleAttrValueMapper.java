package online.yjyy.gmall0508.manage.mapper;

import online.yjyy.gmall0508.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    // 根据spuId 查询出对应的销售属性值
    List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu (String spuId);
}
