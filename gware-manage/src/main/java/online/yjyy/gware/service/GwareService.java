package online.yjyy.gware.service;

import online.yjyy.gware.bean.WareInfo;
import online.yjyy.gware.bean.WareOrderTask;
import online.yjyy.gware.bean.WareSku;

import java.util.List;
import java.util.Map;

/**
 * @param
 * @return
 */
public interface GwareService {
    public Integer  getStockBySkuId(String skuid);

    public boolean  hasStockBySkuId(String skuid,Integer num);

    public List<WareInfo> getWareInfoBySkuid(String skuid);

    public void addWareInfo();

    public Map<String,List<String>> getWareSkuMap(List<String> skuIdlist);

    public void addWareSku(WareSku wareSku);

    public void deliveryStock(WareOrderTask taskExample) ;

    public WareOrderTask saveWareOrderTask(WareOrderTask wareOrderTask );

    public  List<WareOrderTask>   checkOrderSplit(WareOrderTask wareOrderTask);

    public void lockStock(WareOrderTask wareOrderTask);

    public List<WareOrderTask> getWareOrderTaskList(WareOrderTask wareOrderTask);

    public List<WareSku> getWareSkuList();

    public List<WareInfo> getWareInfoList();

   public WareSku queryStockBySkuId(String skuId);

    int delWareSku(WareSku wareSku);

    void updateWareSku(WareSku wareSku);

    WareInfo getWareInfo(WareInfo wareInfo);
}
