package online.yjyy.gmall0508.usermanage.Controller;


import online.yjyy.gmall0508.bean.UserInfo;
import online.yjyy.gmall0508.config.CookieUtil;
import online.yjyy.gmall0508.config.RedisUtil;
import online.yjyy.gmall0508.service.UserService;
import online.yjyy.gmall0508.usermanage.config.JwtUtil;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
public class ManageController {
    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    // 调用服务层
    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;
    @RequestMapping("findAll")
    @ResponseBody
    public List<UserInfo> findAll(){
        return userService.findAll();
    }
    @RequestMapping("logout")
    public String index(HttpServletRequest request, HttpServletResponse response, Model model) {
        String userId = (String) request.getAttribute("userId");
        if(!"".equals(userId)&&null!=userId) {
            Jedis jedis = redisUtil.getJedis();
            String userKey = userKey_prefix + userId + userinfoKey_suffix;
            // 取得redis中的数据
            jedis.del(userKey);
            request.removeAttribute("nickName");
            CookieUtil.deleteCookie(request,response,"token");
        }
        return "index";
    }
    @RequestMapping("/queryUserInfo.do")
    public String queryUserInfo(HttpServletRequest request, Model model){
        String userId = (String) request.getAttribute("userId");
        UserInfo user = userService.queryUserInfoById(userId);
        model.addAttribute("user",user);
        return "userInfo";
    }
    @RequestMapping("userInfo")
    public String userInfopage(){
        return "userinfo";
    }

    @RequestMapping("/updateUserInfo.do")
    public String updateUserInfo(HttpServletRequest request){
       /* int ret = userService.updateUserInfo(userInfo);
        if(ret>0) {
            return "success";
        }*/
        return "userInfo";
    }

}
