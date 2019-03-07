package online.yjyy.gmall0508.manage.mapper;


import online.yjyy.gmall0508.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    //根据三级分类id去查询BaseAttrInfo  借助xml来实现
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(long catalog3Id);
    // 根据平台属性值Id 查询平台属性集合
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param(value = "valueIds") String valueIds);
}
