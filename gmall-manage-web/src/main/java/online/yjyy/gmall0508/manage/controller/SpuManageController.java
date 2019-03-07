package online.yjyy.gmall0508.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import online.yjyy.gmall0508.bean.BaseSaleAttr;
import online.yjyy.gmall0508.bean.SpuImage;
import online.yjyy.gmall0508.bean.SpuInfo;
import online.yjyy.gmall0508.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class SpuManageController {

    @Autowired
    private ManageService manageService;
    @RequestMapping("spuListPage")
    public String spuListPage(){
        return "spuListPage";
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> spuList(String catalog3Id){
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
       return   manageService.getSpuInfoList(spuInfo);
    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){
        return  manageService.getBaseSaleAttrList();
    }

    @RequestMapping(value = "saveSpuInfo",method = RequestMethod.POST)
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){

        // 保存
        manageService.saveSpuInfo(spuInfo);
        return "success";
    }
/*    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList (String spuId){

        return manageService.getSpuImageList(spuId);
    }*/
@RequestMapping(value ="spuImageList" ,method = RequestMethod.GET)
@ResponseBody
public  List<SpuImage> getSpuImageList(@RequestParam Map<String,String> map){
    String spuId = map.get("spuId");
    List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
    return spuImageList;
}

}
