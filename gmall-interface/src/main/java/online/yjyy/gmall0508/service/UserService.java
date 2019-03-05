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

}
