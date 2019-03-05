package online.yjyy.gmall0508.usermanage.Controller;


import online.yjyy.gmall0508.bean.UserInfo;
import online.yjyy.gmall0508.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ManageController {

    // 调用服务层
    @Autowired
    private UserService userService;
    @RequestMapping("findAll")
    @ResponseBody
    public List<UserInfo> findAll(){
        return userService.findAll();
    }
}
