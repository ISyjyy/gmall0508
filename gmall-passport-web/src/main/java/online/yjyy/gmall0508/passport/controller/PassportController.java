package online.yjyy.gmall0508.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import online.yjyy.gmall0508.bean.UserInfo;
import online.yjyy.gmall0508.config.RedisUtil;
import online.yjyy.gmall0508.passport.config.JwtUtil;
import online.yjyy.gmall0508.passport.config.MessageUtil;
import online.yjyy.gmall0508.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class    PassportController {
    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    @Value("${token.key}")
    private String signKey;
    @Reference
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping("index")
    public String index(HttpServletRequest request){
        // https://passport.jd.com/new/login.aspx?ReturnUrl=https%3A%2F%2Fitem.jd.com%2F8735304.html
        //                                        originUrl=https%3A%2F%2Fitem.jd.com%2F8735304.html
        String originUrl = request.getParameter("originUrl");
        System.out.println("originUrl:"+originUrl);
        // 将originUrl 存储
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    @RequestMapping("registerUser")
    @ResponseBody
    public String registerUser(UserInfo userInfo, HttpServletRequest request){
       String LoginName=userInfo.getLoginName();
        UserInfo info = userService.queryUserInfo(LoginName);
        if(null!=info){
        if(info.getLoginName().equals(userInfo.getLoginName())){
            return "LoginFail";
        }
        }
        UserInfo userInfo1 = userService.queryUserInfoByPhone(userInfo.getPhoneNum());
        if(null!=userInfo1){
         if(userInfo.getPhoneNum().equals(userInfo.getPhoneNum())){
        return "PhoneFail";
         }
        }
        UserInfo userInfo2 = userService.queryUserInfoByEmail(userInfo.getEmail());
        if(null!=userInfo2){
            if(userInfo.getEmail().equals(userInfo.getEmail())){
                return "EmailFail";
            }
        }

        String newPassword = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        // 赋值
        userInfo.setPasswd(newPassword);
        userInfo.setNickName(LoginName+UUID.randomUUID().toString().substring(0,5));
        userService.addUserInfo(userInfo);
        return "register";
    }

    @RequestMapping("register")
    public String register(UserInfo userInfo,HttpServletRequest request){
        return "register";
    }
    // 前台传递参数到后台，对象传值：保证name 属性跟实体类属性名一致
    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){
        // 调用服务层，判断用户登录是否成功
        String em = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";/*注：js和java用正则表达式不一样*/
        /*String em ="/^\\w+@\\w+\\.[A-Za-z]{2,3}(\\.[A-Za-z]{2,3})?$/";*/     /*js用正则表达式*/
        String ph = "^((13[0-9])|(15[^4,\\D])|(17[0-9])|(18[0,5-9]))\\d{8}$";  /*java用验证手机号*/
        if(userInfo.getLoginName().matches(em)){
            userInfo.setEmail(userInfo.getLoginName());
            userInfo.setLoginName(null);
        }else if(userInfo.getLoginName().matches(ph)){
            userInfo.setPhoneNum(userInfo.getLoginName());
            userInfo.setLoginName(null);
        }

        UserInfo info = userService.login(userInfo);
        if (info!=null){
            // 服务器的Ip 如何取得 - linux 中
            String salt = request.getHeader("X-forwarded-for");
            // nginx 配置文件中添加 配置
            //   proxy_set_header X-forwarded-for $proxy_add_x_forwarded_for;
            // 私钥
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId",info.getId());
            map.put("nickName",info.getNickName());
            // 生成token
            String token = JwtUtil.encode(signKey, map, salt);
            return token;
        }else{
            return "fail";
        }
    }
    // verify?token = xxxxx&s
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        // 需要token
        String token = request.getParameter("token");
        // 解密需要ip地址
        String currentIp = request.getParameter("currentIp");
        // 解密token token
        Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);
        if (map!=null){
            // 取得map中的数据userId
            String userId = (String) map.get("userId");
            // 通过userId 验证 redis 中是否有userInfo对象
            UserInfo userInfo = userService.verify(userId);
            if (userInfo!=null){
                return "success";
            }else {
                return "fail";
            }
        }else {
            return "fail";
        }
    }
    @RequestMapping("findPassword")
    public String findPassword(UserInfo userInfo,HttpServletRequest request){
        return "findPassword";
    }
    @RequestMapping("userFindPassword")
    @ResponseBody
    public String userFindPassword(UserInfo userInfo,HttpServletRequest request, HttpSession session){
        UserInfo userInfo1 = userService.queryUserInfoByInfo(userInfo);
        if (userInfo1!=null){
            session.setAttribute("userInfo2",userInfo1);
            Jedis jedis = redisUtil.getJedis();
            String userKey = userKey_prefix + userInfo.getPhoneNum() + userinfoKey_suffix;
            String code = jedis.get(userKey);
            return code;
        }
          return "fail";
    }

    @RequestMapping("resetPassWord")
    public String resetPassWord(UserInfo userInfo, HttpServletRequest request){
       /* String user = request.getParameter("user");
        String phoneNum = request.getParameter("phoneNum");
        String email = request.getParameter("email");
        if (!StringUtils.isEmpty(user)&&!StringUtils.isEmpty(phoneNum)&&!StringUtils.isEmpty(email)) {
            userInfo.setLoginName(user);
            userInfo.setPhoneNum(phoneNum);
            userInfo.setEmail(email);

        }*/
      //  session.setAttribute("userInfo2",userInfo);
        return "resetPassWord";
    }

    @RequestMapping("userResetPassWord")
    @ResponseBody
    public String userResetPassWord(UserInfo userInfo,HttpServletRequest request,HttpSession session) {
        UserInfo userInfo2 = (UserInfo) session.getAttribute("userInfo2");
        if (!StringUtils.isEmpty(userInfo2.getLoginName())&&!StringUtils.isEmpty(userInfo2.getPhoneNum())&&!StringUtils.isEmpty(userInfo2.getEmail())) {
            //密码加密
            String newPassword = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
            userInfo2.setPasswd(newPassword);
            int ret = userService.updateUserInfo(userInfo2);
            if (ret > 0) {
                return "success";
            }
        }
            return "fail";
        }
    @RequestMapping("getMessageNum")
    @ResponseBody
    public String getMessageNum(UserInfo userInfo, HttpServletRequest request, HttpSession session) {
           String code="";
        if(userInfo!=null) {
            code = MessageUtil.sendMessage(userInfo.getPhoneNum());
            Jedis jedis = redisUtil.getJedis();
            String userKey = userKey_prefix + userInfo.getPhoneNum() + userinfoKey_suffix;
            jedis.set(userKey,code);
            jedis.expire(userKey,3*60);
            jedis.close();
        }
         return code;
    }
}
