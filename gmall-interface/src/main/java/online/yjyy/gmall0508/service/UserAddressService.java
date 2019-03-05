package online.yjyy.gmall0508.service;

import online.yjyy.gmall0508.bean.UserAddress;

import java.util.List;

public interface UserAddressService {
    List<UserAddress> getUserAddressList(String userId);
}
