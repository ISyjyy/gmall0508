package online.yjyy.gmall0508.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import online.yjyy.gmall0508.bean.BaseAttrInfo;
import online.yjyy.gmall0508.bean.BaseAttrValue;
import online.yjyy.gmall0508.bean.SkuInfo;
import online.yjyy.gmall0508.bean.SkuLsInfo;
import online.yjyy.gmall0508.service.ListService;
import online.yjyy.gmall0508.service.ManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Controller
public class AttrManageController {

    @Reference
    private ManageService manageService;
    @Reference
    private ListService listService;

    @RequestMapping(value = "saveAttrInfo",method = RequestMethod.POST)
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return "success";
    }
    // 所有的easyuI控件都必须返回Json ,显示的数据应该是平台属性值的集合。
    // baseAttrInfo , baseAttrValue  baseAttrInfo.id = baseAttrValue.attrId;
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId){
        // 调用服务
        BaseAttrInfo baseAttrInfo =  manageService.getAttrInfo(attrId);
        // baseAttrInfo.id = baseAttrValue.attrId;
        return baseAttrInfo.getAttrValueList();
    }

    // 商品上架 根据商品id进行上架
    // http://localhost:8082/onSale?skuId=33
    @RequestMapping("onSale")
    @ResponseBody
    public String onSale(String skuId){
        // 根据商品Id 获取到skuInfo ,然后将skuInfo 的属性 copy 到skuLsInfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        // 创建一个skuLsInfo对象
        //  skuLsInfo.skuAttrValueList = skuInfo.skuAttrValueList
        SkuLsInfo skuLsInfo = new SkuLsInfo();
//        skuLsInfo.setId(skuInfo.getId());
//        skuLsInfo.setCatalog3Id(skuInfo.getCatalog3Id());
//        skuLsInfo.setSkuDefaultImg(skuInfo.getSkuDefaultImg());
//        审核流程，true ：saveSkuInfo false: rollback
        // 使用工具类：
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuInfo(skuLsInfo);
        return "success";
    }
}
