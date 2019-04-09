package online.yjyy.gmall0508.usermanage.Controller;


import online.yjyy.gmall0508.bean.UserAddress;
import online.yjyy.gmall0508.bean.UserInfo;
import online.yjyy.gmall0508.config.CookieUtil;
import online.yjyy.gmall0508.config.LoginRequire;
import online.yjyy.gmall0508.config.RedisUtil;
import online.yjyy.gmall0508.config.WebConst;
import online.yjyy.gmall0508.service.UserService;
import online.yjyy.gmall0508.usermanage.config.JwtUtil;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ManageController {
    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    @Value("${token.key}")
    private String signKey;
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
    @LoginRequire(autoRedirect = true)
    public String queryUserInfo(HttpServletRequest request, Model model){
        String userId = (String) request.getAttribute("userId");
        UserInfo user = userService.queryUserInfoById(userId);
        model.addAttribute("user",user);
        return "userinfo";
    }

    @RequestMapping("userAddress")
    public String userAddressd(HttpServletRequest request,Model model){
        String userId = (String) request.getAttribute("userId");
        UserAddress userAddress=new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> userAddresses = userService.queryUserAddress(userAddress);
        model.addAttribute("userAddressList",userAddresses);
        return "userAddress";
    }

    @RequestMapping("/delUserAddress")
    public String delUserAddress(HttpServletRequest request){
        String id = request.getParameter("id");
        userService.delUserAddress(id);
        return "redirect:userAddress";
    }

    @RequestMapping("/addUserAddress")
    @ResponseBody
    public String addUserAddress(@RequestBody UserAddress userAddress, HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        userAddress.setUserId(userId);
        /*System.out.println(userAddress.getConsignee());
        System.out.println(userAddress.getUserAddress());
        System.out.println(userAddress.getPhoneNum());
        System.out.println(userAddress.getIsDefault());*/
       int ret = userService.addUserAddress(userAddress);
        if(ret<0) {
            return "fail";
        }
        return "success";
    }

    @RequestMapping("userInfo")
    public String userInfod(HttpServletRequest request){
        return "userInfo";
    }
    @RequestMapping("userPassword")
    public String userPassword(HttpServletRequest request){

        return "userPassword";
    }
    @RequestMapping("upUserPassword")
    @ResponseBody
    public String upUserPassword(@RequestBody Map map,HttpServletRequest request){
        System.out.println(map);

      //  System.out.println(OldPassword);
       UserInfo userInfo =new UserInfo();
        String userId = (String) request.getAttribute("userId");
        UserInfo user = userService.queryUserInfoById(userId);
        System.out.println(user.getPasswd());
        //加密操作
        String newPassword = DigestUtils.md5DigestAsHex(map.get("OldPassword").toString().getBytes());
        System.out.println(newPassword);
       //判断就密码是否正确
        if(!user.getPasswd().equals(newPassword))
        {
            return "fail";
        }
        String newPassword2 = DigestUtils.md5DigestAsHex(map.get("password").toString().getBytes());
        userInfo.setPasswd(newPassword2);
        userInfo.setId(userId);
       int ret=userService.upUserPassword(userInfo);
        if(ret<0) {
            return "fail";
        }
        return "success";

    }

    @RequestMapping("/updateUserInfo")
    @ResponseBody
    public String updateUserInfo(@RequestBody UserInfo userInfo, HttpServletRequest request,HttpServletResponse response){
/*        System.out.println(userInfo.getNickName());
        System.out.println(userInfo.getId());
        System.out.println(userInfo.getSex());
        System.out.println(userInfo.getPhoneNum());
        System.out.println(userInfo.getEmail());*/
       int ret = userService.updateUserInfo(userInfo);
        if(ret<0) {
            return "fail";
        }
        String salt = request.getHeader("X-forwarded-for");
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId",userInfo.getId());
        map.put("nickName",userInfo.getNickName());
        // 生成token
        String token = online.yjyy.gmall0508.passport.config.JwtUtil.encode(signKey, map, salt);
        CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        return "success";
    }

}
