package online.yjyy.gmall0508.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import online.yjyy.gmall0508.bean.BaseAttrInfo;
import online.yjyy.gmall0508.bean.BaseCatalog1;
import online.yjyy.gmall0508.bean.BaseCatalog2;
import online.yjyy.gmall0508.bean.BaseCatalog3;
import online.yjyy.gmall0508.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
public class ManageController {

    @Autowired
    private ManageService manageService;
    @RequestMapping("index")
    public String index(){
        // 表示返回一个试图 ， 新建一个试图
        return "index";
    }
    @RequestMapping("attrListPage")
    public String attrListPage(){
        return "attrListPage";
    }

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
        return    manageService.getCatalog1();
    }
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        return    manageService.getCatalog2(catalog1Id);
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog1(String catalog2Id){
        return    manageService.getCatalog3(catalog2Id);
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        //ctrl+alt+b  跳转到实现类
        return manageService.getAttrList(catalog3Id);
    }




}
