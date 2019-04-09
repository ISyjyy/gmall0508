package online.yjyy.gmall0508.service;

import online.yjyy.gmall0508.bean.UserAddress;
import online.yjyy.gmall0508.bean.UserInfo;

import java.util.List;

// xxxMapper extends Mapper<UserInfo>
public interface UserService {

    // 查询所有用户的接口
    List<UserInfo> findAll();
    // 根据userId 查询user的地址
    List<UserAddress> getUserAddressList(String userId);
    // 登录接口
    UserInfo login(UserInfo userInfo);
    // 认证用户是否登录
    UserInfo verify(String userId);
 //注册用户
    void addUserInfo(UserInfo userInfo);
//查询用户名是否重复
    UserInfo queryUserInfo(String LoginName);
    //id查询用户
    UserInfo queryUserInfoById(String userId);
  //更新用户信息
    int updateUserInfo(UserInfo userInfo);
//增加用户收货地址
    int addUserAddress(UserAddress userAddress);
//查询当前用户的所有收货地址
    List<UserAddress> queryUserAddress(UserAddress userAddress);
//删除用户收货地址
    int delUserAddress(String id);
//更新用户密码
    int upUserPassword(UserInfo userInfo);
}
