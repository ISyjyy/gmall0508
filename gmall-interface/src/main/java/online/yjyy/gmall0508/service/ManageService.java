package online.yjyy.gmall0508.service;


import online.yjyy.gmall0508.bean.*;


import java.util.List;

public interface ManageService {

    // 查询所有一级分类
    List<BaseCatalog1> getCatalog1();

    List<BaseCatalog2> getCatalog2(String catalog1Id);

    List<BaseCatalog3> getCatalog3(String catalog2Id);

    List<BaseAttrInfo> getAttrList(String catalog3Id);
    // 保存平台属性，平台属性值
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);
    // 根据平台属性名Id，平台属性值中的AttrId
    BaseAttrInfo getAttrInfo(String attrId);
    // 根据三级分类Id catalog3Id 查询spuInfo 列表,将三级分类Id 传入实体类。
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);
    // 查询所有的销售属性
    List<BaseSaleAttr> getBaseSaleAttrList();
    // 保存spuInfo 信息
    void saveSpuInfo(SpuInfo spuInfo);
    // 根据spuId 查询图片列表
    List<SpuImage> getSpuImageList(String spuId);
    //根据spuid查询销售属性
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);
    // 保存skuInfo 对象
    void saveSkuInfo(SkuInfo skuInfo);
     //根据skuId查询保存skuInfo信息
    SkuInfo getSkuInfo(String skuId);
    // 销售属性值，以及销售属性
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo);
    // 根据spuId查询销售属性值的集合
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
    //根据平台属性值id查询平台属性的集合
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
