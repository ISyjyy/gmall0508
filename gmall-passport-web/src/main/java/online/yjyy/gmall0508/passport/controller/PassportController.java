package online.yjyy.gmall0508.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import online.yjyy.gmall0508.bean.UserInfo;
import online.yjyy.gmall0508.passport.config.JwtUtil;
import online.yjyy.gmall0508.service.UserService;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Value("${token.key}")
    private String signKey;
    @Reference
    private UserService userService;

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

    // 前台传递参数到后台，对象传值：保证name 属性跟实体类属性名一致
    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){
        // 调用服务层，判断用户登录是否成功
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

}
