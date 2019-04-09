package online.yjyy.gmall0508.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import online.yjyy.gmall0508.bean.CartInfo;
import online.yjyy.gmall0508.bean.SkuInfo;
import online.yjyy.gmall0508.cart.constant.CartConst;
import online.yjyy.gmall0508.cart.mapper.CartInfoMapper;
import online.yjyy.gmall0508.config.RedisUtil;
import online.yjyy.gmall0508.service.CartService;
import online.yjyy.gmall0508.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ManageService manageService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * @param skuId  商品id
     * @param userId 用户id
     * @param skuNum 商品数据
     */
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        // 查询购物车中是否有该商品
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);
        // select * from cartInfo where skuId = ? and userId = ?
        CartInfo cartInfoExist  = cartInfoMapper.selectOne(cartInfo);
        //  判断是否存在
        if (cartInfoExist!=null){
            // 数量加1
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            // 给skuPrice 赋值。 热部署//
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        }else {
            // 说明购物车中没有该商品，则新增
            // CartInfo 对象中的数据从哪里得来？ skuInfo 得来
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();

            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);

            cartInfoMapper.insertSelective(cartInfo1);
            // 将新增的值，赋给cartInfoExist。为下面redis保存
            cartInfoExist = cartInfo1;
        }

        // 更新缓存
        Jedis jedis = redisUtil.getJedis();
        // 有key user:userId:cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;
        // 购物车实体类的字符串
        jedis.hset(userCartKey,skuId, JSON.toJSONString(cartInfoExist));
        // 可以根据用户的过期时间 user:userId:info
        String userInfoKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        //根据用户的过期时间
        Long ttl = jedis.ttl(userInfoKey);
        // 设置一个过期时间
        jedis.expire(userCartKey,ttl.intValue());
        jedis.close();
    }

    /**
     * @param userId 用户Id
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId) {

        // 需要redis
        Jedis jedis = redisUtil.getJedis();
        // 购物车key user:userId:cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        //取出缓存的值
        // String
        // String cartJson = jedis.get(userCartKey);
        // 将cartJson 转换成
        List<String> cartJson = jedis.hvals(userCartKey);
        // 判断字符串是否为空
        if (cartJson!=null && cartJson.size()>0){
            List<CartInfo> cartInfoList = new ArrayList<>();
            // 循环cartJson
            for (String cart : cartJson) {
                // 将字符串转换成CartInfo对象
                CartInfo cartInfo = JSON.parseObject(cart, CartInfo.class);
                // 添加到集合中
                cartInfoList.add(cartInfo);
            }
            // 排序 hash 结果是无序的。要对查询出来的数据进行排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    // 根据字符串进行比较
                    return o1.getId().compareTo(o2.getId());
                }
            });
            jedis.close();
            return  cartInfoList;
        }else {
            // 从数据库查询数据
            List<CartInfo> cartInfoList =  loadCartCache(userId);
            return cartInfoList;
        }
    }

    /**
     * 合并购物车
     *
     * @param cartListCK cookie 中的购物车
     * @param userId     用户Id
     * @return
     */
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId) {
        // cookie  ---> mysql
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        // 取得cookie -- cartListCK
        for (CartInfo cartInfoCK : cartListCK) {
            // 定义一个标识
            boolean isMatch =false;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                // skuId 一样则进行合并：数量相加
                if (cartInfoCK.getSkuId().equals(cartInfoDB.getSkuId())){
                    // 数量相加
                    cartInfoDB.setSkuNum(cartInfoCK.getSkuNum()+cartInfoDB.getSkuNum());
                    // 放入数据库
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch=true;
                }
            }
            if (!isMatch){
                // insert --- mysql
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }
        // 将更新过的，插入过的数据通通的查询
        List<CartInfo> cartInfoList = loadCartCache(userId);
        // 以上代码全部都是合并的是购物车列表【不区分选中还是未选中】
        // cookie --- > mysql 二层循环嵌套
        // 外层使用数据库
        for (CartInfo cartInfoDB : cartInfoList) {
            // 内层使用cookie
            for (CartInfo cartInfoCK : cartListCK) {
                // skuId ,isChecked = 1
                if (cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())){
                    if ("1".equals(cartInfoCK.getIsChecked())){
                        // 看项目经理需求 [cookie 5 + mysql 4 ？ 9]
                        cartInfoDB.setIsChecked(cartInfoCK.getIsChecked());
                        // 保存一下选中的状态
                        checkCart(cartInfoDB.getSkuId(),cartInfoCK.getIsChecked(),userId);
                    }
                }
            }
        }
        return cartInfoList;
    }

    // 需要取出商品的实时价格
    public List<CartInfo> loadCartCache(String userId) {
        // skuInfo 的价格才是商品的真正价格
        // 需要做一个两个表关联查询 cartInfo ，skuInfo 关联字段skuInfo.id=cartInfo.skuId
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList==null || cartInfoList.size()==0){
            return null;
        }
        // 放入缓存
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        // redis对象
        Jedis jedis = redisUtil.getJedis();
        // 创建一个map 集合
        Map<String,String> map = new HashMap<>(cartInfoList.size());
        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        jedis.hmset(userCartKey,map);
//        jedis.hset(key,field,value);
//        jedis.hmset(key,Map);
        jedis.close();
        return  cartInfoList;
    }

    @Override
    public int delCartItrm(CartInfo cartInfo, String userId) {
        return 0;
    }


    /**
     * @param skuId     商品Id
     * @param isChecked 商品的状态
     * @param userId    用户的Id
     */
    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        // 第一步：将user:userId:cart 总的购物车列表 更新【isChecked】
        Jedis jedis = redisUtil.getJedis();

        // 定义key user:userId:cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        // 取得redis的数据
        String cartJson  = jedis.hget(userCartKey, skuId);

        // 将其转换为对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);

        // 将页面传递过来的商品状态赋给cartInfo
        cartInfo.setIsChecked(isChecked);
        // 将cartInfo 从新放回redis
        jedis.hset(userCartKey,skuId,JSON.toJSONString(cartInfo));

        // 第二步：新增一个key user:userId:checked 存放被选中的商品
        // 定义一个key user:userId:checked
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        if ("1".equals(isChecked)){
            // 将被选中的商品放入redis
            jedis.hset(userCheckedKey,skuId,JSON.toJSONString(cartInfo));
        }else {
            // 删除key
            jedis.hdel(userCheckedKey,skuId);
            // jedis.del(userCheckedKey)
        }

        jedis.close();
    }
    /**
     * @param userId 用户Id
     * @return
     */
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        // 取得被选中的数据 user:userId:checked
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        // 获取redis 对象
        Jedis jedis = redisUtil.getJedis();
        // 准备取值
        List<String> cartCheckedList  = jedis.hvals(userCheckedKey);
        // 集合的字符串【代表的是每一个对象】
        List<CartInfo> newCartList = new ArrayList<>();
        for (String cartJson : cartCheckedList) {
            // 将cartJson 转换为cartInfo 对象，并添加到对应的集合中
            newCartList.add(JSON.parseObject(cartJson,CartInfo.class));
        }
        return newCartList;
    }
}
