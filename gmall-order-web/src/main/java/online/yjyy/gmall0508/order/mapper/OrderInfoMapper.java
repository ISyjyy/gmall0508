package online.yjyy.gmall0508.order.mapper;

import online.yjyy.gmall0508.bean.OrderInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface OrderInfoMapper extends Mapper<OrderInfo> {
    List<OrderInfo> selectOrderInfoList(Long userId);
}
