package online.yjyy.gmall0508.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import online.yjyy.gmall0508.bean.UserAddress;
import online.yjyy.gmall0508.bean.UserInfo;
import online.yjyy.gmall0508.config.RedisUtil;
import online.yjyy.gmall0508.service.UserService;
import online.yjyy.gmall0508.usermanage.mapper.UserAddressMapper;
import online.yjyy.gmall0508.usermanage.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;


import java.util.List;

@Service   //引入dubbo的service  暴露端口
public class UserServiceImpl implements UserService {

    // 用来定义redis key 使用
    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    @Autowired
    private UserMapper userMapper;

  @Autowired
    private UserAddressMapper userAddressMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UserInfo> findAll() {
        return userMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        // select * from user_address where user_id = userId'
        // select * from user_address
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return  userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        // 数据库密码：202cb962ac59075b964b07152d234b70
        // 加密处理
        String passwd = userInfo.getPasswd();
        // 将passwd 转换202cb962ac59075b964b07152d234b70
        String newPassword = DigestUtils.md5DigestAsHex(passwd.getBytes());
        // 赋值
        userInfo.setPasswd(newPassword);
        // 配一个用户对象
        UserInfo info = userMapper.selectOne(userInfo);
        // 登录成功之后，将用户登录信息放入redis 中

        Jedis jedis = redisUtil.getJedis();
        // 定义key  user:userId:info
        String userKey = userKey_prefix+info.getId()+userinfoKey_suffix;
        if (info!=null){
            //设置过期时间
            jedis.setex(userKey,userKey_timeOut, JSON.toJSONString(info));
        }
        return info;
    }

    @Override
    public UserInfo verify(String userId) {
        // 取得redis
        Jedis jedis = redisUtil.getJedis();
        // 拼接key
        String userKey = userKey_prefix+userId+userinfoKey_suffix;
        // 取得redis中的数据
        String userJson = jedis.get(userKey);
        // 用户处于活跃状态
        jedis.expire(userKey,userKey_timeOut);
        // 判断userJson 是否为空
        if (userJson!=null && userJson.length()>0){
            // userJson 将其转化为对象
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }
        return null;
    }

    @Override
    public void addUserInfo(UserInfo userInfo) {
         userMapper.insert(userInfo);
    }

    @Override
    public UserInfo queryUserInfo(String loginName) {
        UserInfo userInfo=new UserInfo();
        userInfo.setLoginName(loginName);
        return  userMapper.selectOne(userInfo);


    }

    @Override
    public UserInfo queryUserInfoById(String userId) {
        UserInfo userInfo = userMapper.selectByPrimaryKey(userId);
        return userInfo;
    }

    @Override
    public int updateUserInfo(UserInfo userInfo) {
        return  userMapper.updateByPrimaryKey(userInfo);

    }
}
