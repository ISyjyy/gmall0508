package online.yjyy.gware.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import online.yjyy.gware.bean.WareInfo;
import online.yjyy.gware.bean.WareOrderTask;
import online.yjyy.gware.bean.WareSku;
import online.yjyy.gware.enums.TaskStatus;
import online.yjyy.gware.service.GwareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @return
 */

@Controller
public class GwareController {

    @Autowired
    GwareService gwareService;


    @RequestMapping("index")
    public String index(){
        return "index";
    }
    @RequestMapping("wareSkuListPage")
    public String wareSkuListPage(){
        return "wareSkuListPage";
    }
    @RequestMapping("orderTaskList")
    public String orderTaskList(){
        return "orderTaskList";
    }

    //查询库存数量
    @RequestMapping("queryStock")
    @ResponseBody
    public WareSku queryStock(@RequestParam("skuId") String skuid){
        System.out.println(skuid);

        WareSku wareSku = gwareService.queryStockBySkuId(skuid);
        return wareSku;
    }


    //根据sku判断是否有库存
    @RequestMapping("hasStock")
    @ResponseBody
    public ResponseEntity<String> hasStock(@RequestParam Map<String,String> hashMap){
        String numstr = (String) hashMap.get("num");
        Integer num=Integer.parseInt(numstr);
        String skuid =(String)hashMap.get("skuId");
        boolean hasStock = gwareService.hasStockBySkuId( skuid , num);
        if(hasStock){
            return   ResponseEntity.ok("1");
        }
        return  ResponseEntity.ok("0");
    }


    //根据skuid 返回 仓库
    @RequestMapping(value = "skuWareInfo")
    @ResponseBody
    public  ResponseEntity<String> getWareInfoBySkuid(String skuid){
        if(skuid==null){
            return     ResponseEntity.noContent().build();
        }
        List<WareInfo> wareInfos = gwareService.getWareInfoBySkuid( skuid );
        String jsonString = JSON.toJSONString(wareInfos);
        return ResponseEntity.ok(jsonString);
    }


    @RequestMapping(value = "wareInfo")
    @ResponseBody
    public void addWareInfo(){
          gwareService.addWareInfo();
    }

    //根据skuid 返回 仓库
    @RequestMapping(value = "wareSkuMap"  )
    @ResponseBody
    public ResponseEntity<String> getWareSkuMap(@RequestParam("skuid") List<String> skuidsList){
       // List<String> skuidsList = JSON.parseArray(skuids, String.class) ;
        Map<String, List<String>> wareSkuMap = gwareService.getWareSkuMap(skuidsList);
        String jsonString = JSON.toJSONString(wareSkuMap);
        return ResponseEntity.ok(jsonString);
    }


    @RequestMapping(value = "validate")
    @ResponseBody
    public ResponseEntity<Void> validate(){
        System.out.println("保存成功!");
        return ResponseEntity.ok().build();
    }
    @RequestMapping(value = "saveWareSku" ,method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Void> addWareSku( WareSku wareSku){
         gwareService.addWareSku(wareSku);
         return ResponseEntity.ok().build();
    }
    @RequestMapping(value = "updateWareSku" ,method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Void> updateWareSku( WareSku wareSku){
        gwareService.updateWareSku(wareSku);
        return ResponseEntity.ok().build();
    }
    @RequestMapping(value = "delWareSku" ,method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Void> delWareSku(WareSku wareSku,HttpServletRequest request){
        wareSku.setId(request.getParameter("id"));
        gwareService.delWareSku(wareSku);
        return ResponseEntity.ok().build();
    }


    @RequestMapping(value = "wareSkuList" ,method = RequestMethod.GET,produces= MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<WareSku> getWareSkuList(HttpServletResponse response){
        List<WareSku> wareSkuList = gwareService.getWareSkuList();
        return wareSkuList;
    }

    @RequestMapping(value = "wareInfoList" ,method = RequestMethod.GET,produces="application/json;charset=UTF-8")
    @ResponseBody
    public List<WareInfo> getWareInfoList(){
        List<WareInfo> wareInfoList = gwareService.getWareInfoList();
        return wareInfoList;
    }
    @RequestMapping(value = "getWareInfo")
    @ResponseBody
    public WareInfo getWareInfo(HttpServletRequest request,WareInfo wareInfo){
        wareInfo.setId( request.getParameter("id"));
        return   gwareService.getWareInfo(wareInfo);

    }


    /***
     * 出库
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value="delivery",method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Void> deliveryStock(HttpServletRequest httpServletRequest){
        String id = httpServletRequest.getParameter("id");
        String trackingNo = httpServletRequest.getParameter("trackingNo");
        WareOrderTask wareOrderTask=new WareOrderTask();
        wareOrderTask.setId(id);
        wareOrderTask.setTrackingNo(trackingNo);
        gwareService.deliveryStock(wareOrderTask);
        return  ResponseEntity.ok().build();
    }


    @RequestMapping("taskList")
    @ResponseBody
    public ResponseEntity<String> getWareOrderTaskList(HttpServletRequest httpServletRequest){
        WareOrderTask wareOrderTask=new WareOrderTask();
        List<WareOrderTask> wareOrderTaskList = gwareService.getWareOrderTaskList(wareOrderTask);
        SerializeConfig config = new SerializeConfig();
        config.configEnumAsJavaBean(TaskStatus.class);
        String jsonString = JSON.toJSONString(wareOrderTaskList);
        return ResponseEntity.ok().body(jsonString);
    }



}
