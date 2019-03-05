package online.yjyy.gmall0508.manage.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import online.yjyy.gmall0508.bean.*;
import online.yjyy.gmall0508.config.RedisUtil;
import online.yjyy.gmall0508.manage.constant.ManageConst;
import online.yjyy.gmall0508.manage.mapper.*;
import online.yjyy.gmall0508.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private  SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private  SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private  SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private  SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;


    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 catalog2 =new BaseCatalog2();
        catalog2.setCatalog1Id(catalog1Id);
       return baseCatalog2Mapper.select(catalog2);

    }


    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }


    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
   /*     BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        return baseAttrInfoMapper.select(baseAttrInfo);*/

        //        设计到两张以上的表查询，则使用 自定义的xml。
//          getBaseAttrInfoListByCatalog3Id(catalog3Id) 自定义的
        return  baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));
    }


    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        // 第一，发展的眼光看问题！ 插入，保存写在同一个方法了。null == ""; 123
        if (baseAttrInfo.getId()!=null&&baseAttrInfo.getId().length() >0){
            // 做更新
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);

        }else {
            // 做插入 mysql 主键自增，必须当前字段为null
            if (baseAttrInfo.getId().length()==0){ // id="";

                baseAttrInfo.setId(null);

            }

            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        // baseAttrValue; 先删除，在插入。
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        // delete from baseAttrValue where attrId = ?
        baseAttrValueMapper.delete(baseAttrValue);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        // 判断当前集合是否为空
        if (attrValueList!=null && attrValueList.size()>0){
            for (BaseAttrValue attrValue : attrValueList) {
                // 防止当前的id为"";
                if (attrValue.getId().length()==0){
                   attrValue.setId(null);
                }
                // attrJson["attrValueList["+i+"].attrId"]=平台属性名.id; 在插入数据库的时候，直接赋值即可！
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    public BaseAttrInfo getAttrInfo(String attrId) {
        // 通过attrId 获取BaseAttrInfo。 attrId = id
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        // 根据attrId 查询baseAttrValue
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        List<BaseAttrValue> baseAttrValueList  = baseAttrValueMapper.select(baseAttrValue);
        baseAttrInfo.setAttrValueList(baseAttrValueList);

        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        // select * from spuInfo where catalog3Id = spuInfo.catalog3Id
        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);
        return spuInfoList;
    }

    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }


    public void saveSpuInfo(SpuInfo spuInfo) {
        // 先判断spuInfo.id 是否为空 ""; spu_info
        if (spuInfo.getId() == null || spuInfo.getId().length() == 0) {
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        } else {
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        }
        // spu_image 先删除，后添加
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuInfo.getId());
        // delete from spuImage where spuId = spuInfo.id;
        spuImageMapper.delete(spuImage);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0) {
            for (SpuImage image : spuImageList) {
                if (image.getId() == null || image.getId().length() == 0) {
                    image.setId(null);
                }
                // 要赋值spuId = spuInfo.id
                image.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(image);
            }
        }

//        spu_sale_attr  销售属性
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);

//        spu_sale_attr_value
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
            for (SpuSaleAttr saleAttr : spuSaleAttrList) {
                if (saleAttr.getId() == null || saleAttr.getId().length() == 0) {
                    saleAttr.setId(null);
                }
                // 赋值spuId
                saleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(saleAttr);

                // spu_sale_attr_value 插入数据
                List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                    if (saleAttrValue.getId() == null || saleAttrValue.getId().length() == 0) {
                        saleAttrValue.setId(null);
                    }
                    saleAttrValue.setSpuId(spuInfo.getId());
                    // 插入数据
                    spuSaleAttrValueMapper.insertSelective(saleAttrValue);
                }
            }

        }
    }
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
            // 调用mapper 中的接口。 跟数据库有关系，select ，insert ，update ，delete开头
            List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
            return spuSaleAttrList;
        }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {

        // sku_info
        if (skuInfo.getId()==null || skuInfo.getId().length()==0){
            // 设置id 为自增
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //        sku_img,
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);

        // insert
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList!=null && skuImageList.size()>0){
            for (SkuImage image : skuImageList) {
                /* "" 区别 null*/
                if (image.getId()!=null && image.getId().length()==0){
                    image.setId(null);
                }
                // skuId 必须赋值
                image.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(image);
            }
        }
//        sku_attr_value,
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        // 插入数据
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList!=null && skuAttrValueList.size()>0){
            for (SkuAttrValue attrValue : skuAttrValueList) {
                if (attrValue.getId()!=null && attrValue.getId().length()==0){
                    attrValue.setId(null);
                }
                // skuId
                attrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(attrValue);
            }
        }
//        sku_sale_attr_value,
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);
//      插入数据
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList!=null && skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
                if (saleAttrValue.getId()!=null && saleAttrValue.getId().length()==0){
                    saleAttrValue.setId(null);
                }
                // skuId
                saleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }

    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        // try-catch
        SkuInfo skuInfo =null;
        try {
            Jedis jedis = redisUtil.getJedis();
            // 定义sku:skuId:info
            String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            // 取得数据
            String skuJson  = jedis.get(skuInfoKey);
            if (skuJson==null || "".equals(skuJson)){
                System.out.println("没有命中缓存！");
                // 要从数据库中取得数据,准备一个锁
                // set sku:33:info ok px 10000 nx
                String skuLockKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                // 执行这条命令
                String lockKey   = jedis.set(skuLockKey, "OK", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(lockKey)){
                    System.out.println("获得分布式锁");
                    // 从数据库中取得数据放入缓存
                    skuInfo = getSkuInfoDB(skuId);
                    // 将对象放入redis
                    // jedis.set(userKey,JSON.toJSONString(skuInfo));
                    // 设置key的过期时间
                    jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfo));
                    jedis.close();
                    return skuInfo;
                }else {
                    // 睡一会
                    Thread.sleep(1000);
                    // 自旋
                    return getSkuInfo(skuId);
                }
            }else {
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                jedis.close();
                return  skuInfo;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return getSkuInfoDB(skuId);
    }

    public SkuInfo getSkuInfoDB(String skuId){
        // skuInfo 信息放入redis中
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        // 为skuInfo的skuImageList 属性赋值 只需要根据skuId 查询skuImage 表中的数据即可
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);
        // 平台属性值集合
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {

        return  spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(Long.parseLong(skuInfo.getId()),Long.parseLong(skuInfo.getSpuId()));
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuSaleAttrValue> skuSaleAttrValues = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return skuSaleAttrValues;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        // attrValueIdList 这是一个集合，我们要的 集合中的每一个值
        // foreach:mybatis , 没有单个赋值效率高。
        // SELECT * FROM base_attr_info ai INNER JOIN base_attr_value av ON ai.id = av.attr_id WHERE av.id IN (81,14,83)
        String attrValueIds  = StringUtils.join(attrValueIdList.toArray(), ",");
        // 将集合中的每一个数值放进去
        List<BaseAttrInfo>  baseAttrInfoList =   baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);
        return baseAttrInfoList;
    }


}
