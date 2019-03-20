package online.yjyy.gmall0508.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import online.yjyy.gmall0508.bean.SkuInfo;
import online.yjyy.gmall0508.bean.SkuLsInfo;
import online.yjyy.gmall0508.bean.SpuSaleAttr;
import online.yjyy.gmall0508.bean.SpuSaleAttrValue;
import online.yjyy.gmall0508.manage.mapper.SkuInfoMapper;
import online.yjyy.gmall0508.service.ListService;
import online.yjyy.gmall0508.service.ManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SkuManageController {

    @Autowired
    private ManageService manageService;

    @Reference
    private ListService listService;
    @RequestMapping("skuInfoListBySpu")
    @ResponseBody
    public List<SpuSaleAttr> getSkuInfoListBySpu(HttpServletRequest httpServletRequest){
        String spuId = httpServletRequest.getParameter("spuId");
        List skuInfoList = manageService.getSkuInfoListBySpu(spuId);
        return skuInfoList;}

    @RequestMapping("deleteSkuInfo")
    @ResponseBody
    public void deleteSkuInfo(HttpServletRequest httpServletRequest){
        String skuId = httpServletRequest.getParameter("id");
        manageService.deleteSkuInfoById(skuId);

        listService.deleteEsSkuInfo(skuId);

    }


    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> getSpuSaleAttrList(HttpServletRequest httpServletRequest) {
        String spuId = httpServletRequest.getParameter("spuId");
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            Map map = new HashMap();
            map.put("total", spuSaleAttrValueList.size());
            map.put("rows", spuSaleAttrValueList);
            // String spuSaleAttrValueJson = JSON.toJSONString(map);
            spuSaleAttr.setSpuSaleAttrValueJson(map);
        }
        return spuSaleAttrList;
    }


    @RequestMapping(value = "saveSku", method = RequestMethod.POST)
    @ResponseBody
    public String saveSku(SkuInfo skuInfo) {

        manageService.saveSkuInfo(skuInfo);
         //上架商品
        // SkuInfo skuInfo = manageService.getSkuInfo(skuInfo.getId());
       // 创建一个skuLsInfo对象
        SkuLsInfo skuLsInfo = new SkuLsInfo();
       //  审核流程，true ：saveSkuInfo false: rollback
        // 使用工具类：
        try {
            BeanUtils.copyProperties(skuLsInfo, skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuInfo(skuLsInfo);

        return "success";
    }


}
