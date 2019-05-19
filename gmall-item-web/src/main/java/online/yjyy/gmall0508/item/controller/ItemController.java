package online.yjyy.gmall0508.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import online.yjyy.gmall0508.bean.SkuInfo;
import online.yjyy.gmall0508.bean.SkuSaleAttrValue;
import online.yjyy.gmall0508.bean.SpuSaleAttr;
import online.yjyy.gmall0508.config.LoginRequire;
import online.yjyy.gmall0508.service.ListService;
import online.yjyy.gmall0508.service.ManageService;
import online.yjyy.gware.bean.WareInfo;
import online.yjyy.gware.bean.WareSku;
import online.yjyy.gware.service.GwareService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    private ManageService manageService;
    @Reference
    private ListService listService;

    @Reference
    private GwareService gwareService;


    @RequestMapping("{skuId}.html")
    //@LoginRequire(autoRedirect = true)
    public String skuInfoPage(@PathVariable String skuId, HttpServletRequest request) {
        // 能够得到skuId
        WareSku wareSku1 = gwareService.queryStockBySkuId(skuId);
        if(wareSku1!=null){
          //  WareSku wareSku = (WareSku) list.get(0);
            int stock=0;
            int stockLock=0;
            if(null!=wareSku1.getStock()){
                stock=wareSku1.getStock();
            }
            if(null!=wareSku1.getStockLocked()){
                stockLock=wareSku1.getStockLocked();
            }
           int skuStock=stock-stockLock;
            request.setAttribute("skuStock",skuStock);
        }
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        // 保存上skuInfo 信息
        // 1.保存图片数据。2.在前台使用skuInfo.skuImageList
        request.setAttribute("skuInfo", skuInfo);


        // 销售属性，属性值显示
        List<SpuSaleAttr> spuSaleAttrList = manageService.selectSpuSaleAttrListCheckBySku(skuInfo);
        // 将其存入作用域中
        request.setAttribute("saleAttrList", spuSaleAttrList);

        // 调用manageService查询所有的销售属性值
        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        // 拼接字符串
        String jsonKey = "";
        HashMap<String, String> map = new HashMap<>();
        //        map.put("116|118","34");
        // 循环遍历取得销售属性值Id
        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            // 拼接jsonkey
            if (jsonKey.length() != 0) {
                jsonKey += "|";
            }
            // jsonKey=jsonKey+skuSaleAttrValue.getSaleAttrValueId()
            // jsonKey= 116
            // jsonKey= 116|
            // jsonKey= 116|118
            jsonKey += skuSaleAttrValue.getSaleAttrValueId();
            // 不拼接的条件 ，当skuId 与下一条的skuId 不相等，则不拼接。当拼接到最后，结束拼接。
            if ((i + 1) == skuSaleAttrValueListBySpu.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i + 1).getSkuId())) {
                map.put(jsonKey, skuSaleAttrValue.getSkuId());
                jsonKey = "";
            }
        }
        // 将map 转换成json 字符串
        String valuesSkuJson = JSON.toJSONString(map);
        System.out.println(valuesSkuJson);
        request.setAttribute("valuesSkuJson", valuesSkuJson);

        // 调用商品热度排名
        listService.incrHotScore(skuId);
        return "item";
    }

    @RequestMapping("secondKill")
    //@LoginRequire(autoRedirect = true)
    public String secondPage(@RequestParam("skuId") String skuId, HttpServletRequest request) {
        // 能够得到skuId
        System.out.println(skuId);
        WareSku wareSku1 = gwareService.queryStockBySkuId(skuId);
        System.out.println(wareSku1);
        if(null==wareSku1.getStockLocked()||"".equals(wareSku1.getStockLocked())){
            wareSku1.setStockLocked(0);
        }
        if(wareSku1!=null){
            //  WareSku wareSku = (WareSku) list.get(0);
            int skuStock= (wareSku1.getStock())-(wareSku1.getStockLocked());
            request.setAttribute("skuStock",skuStock);
        }
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        // 保存上skuInfo 信息
        // 1.保存图片数据。2.在前台使用skuInfo.skuImageList
        request.setAttribute("skuInfo", skuInfo);


        // 销售属性，属性值显示
        List<SpuSaleAttr> spuSaleAttrList = manageService.selectSpuSaleAttrListCheckBySku(skuInfo);
        // 将其存入作用域中
        request.setAttribute("saleAttrList", spuSaleAttrList);

        // 调用manageService查询所有的销售属性值
        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        // 拼接字符串
        String jsonKey = "";
        HashMap<String, String> map = new HashMap<>();
        //        map.put("116|118","34");
        // 循环遍历取得销售属性值Id
        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            // 拼接jsonkey
            if (jsonKey.length() != 0) {
                jsonKey += "|";
            }
            jsonKey += skuSaleAttrValue.getSaleAttrValueId();
            // 不拼接的条件 ，当skuId 与下一条的skuId 不相等，则不拼接。当拼接到最后，结束拼接。
            if ((i + 1) == skuSaleAttrValueListBySpu.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i + 1).getSkuId())) {
                map.put(jsonKey, skuSaleAttrValue.getSkuId());
                jsonKey = "";
            }
        }
        String valuesSkuJson = JSON.toJSONString(map);
        request.setAttribute("valuesSkuJson", valuesSkuJson);


        return "secondItem";
    }
}
