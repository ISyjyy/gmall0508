package online.yjyy.gmall0508.item.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import online.yjyy.gmall0508.bean.SkuInfo;
import online.yjyy.gmall0508.service.ManageService;
import online.yjyy.gware.bean.WareSku;
import online.yjyy.gware.service.GwareService;
import org.springframework.stereotype.Component;

@JobHandler(value="secondJobHandler")
@Component
public class secondkTask extends IJobHandler {
    @Reference
    private GwareService gwareService;
    @Reference
    private ManageService manageService;
    @Override
    public ReturnT<String> execute(String s) throws Exception {


        if(null!=s&&!"".equals(s)) {
            String[] split = s.split(",");
            System.out.println("second"+split[0]);
            System.out.println("second"+split[1]);
            WareSku wareSku = new WareSku();
            wareSku.setSkuId(split[0]);
            wareSku.setStock(Integer.parseInt(split[1]));
            wareSku.setWarehouseId("1");
            wareSku.setStockLocked(0);
            wareSku.setId("27");
            SkuInfo skuInfo = manageService.getSkuInfo(split[1]);
            wareSku.setStockName(skuInfo.getSkuName());
            gwareService.updateWareSku(wareSku);
        }
        return SUCCESS;
    }
}
