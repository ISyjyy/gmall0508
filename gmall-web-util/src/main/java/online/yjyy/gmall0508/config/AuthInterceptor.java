package online.yjyy.gmall0508.config;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.impl.Base64UrlCodec;
import online.yjyy.gmall0508.util.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

// 变成拦截器
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    // preHandle 进入拦截器之前执行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token 第一次登录获取到newToken,
        String token = request.getParameter("newToken");
        // 保存cookie
        if (token!=null){
            // 使用cookie工具类
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        // 不走登录，走其他控制器的时候，看cookie 中是否有值，如果有值，则赋给token。
        if (token==null){
            token=CookieUtil.getCookieValue(request,"token",false);
        }
        // token不为空
        if (token!=null){
            // eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.WUvbFvXQnTMBGNyHWT-DE41MR9cn7c_W1oAtDAzb7VU
            // 登录之后，页面应该显示登录的昵称。 用户名从何而来？token 私钥就有用户的昵称
            // 通过Base64UrlCodec 进行解密 eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0
            Map map =  getUserMapByToken(token);
            // {nickName=Administrator, userId=1001}
            String nickName = (String) map.get("nickName");
            // 保存
            request.setAttribute("nickName",nickName);
        }
        // 判断-- 控制器中的每个方法上的RequestMapping()是否需要登录，是否有@LoginRequire
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 获取方法上的注解
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(methodAnnotation!=null){
            // 有注解，需要验证verify  -- token, currentIp
            String remoteAddr  = request.getHeader("x-forwarded-for");
            // 调用认证方法 ，认证，在passport 项目中，需要远程调用
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&currentIp=" + remoteAddr);
            if ("success".equals(result)){
                // 表示认证成功！说明用户已经登录了，保存一个用户Id , 购物车：==== userId
                Map map =  getUserMapByToken(token);
                // {nickName=Administrator, userId=1001}
                String userId = (String) map.get("userId");
                // 保存
                request.setAttribute("userId",userId);
                return true;
            }else{
                if (methodAnnotation.autoRedirect()){
                    // 没有登录
                    // 登录页面需要一个originUrl
                    String requestURL  = request.getRequestURL().toString();
                    // originUrl=http%3A%2F%2Fitem.gmall.com%2F33.html
                    // 转码
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    return false;
                }
            }
        }
        return true;
    }

    private Map getUserMapByToken(String token) {
        // 获取私钥
        // eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.WUvbFvXQnTMBGNyHWT-DE41MR9cn7c_W1oAtDAzb7VU
        String tokenUserInfo  = StringUtils.substringBetween(token, ".");
        // tokenUserInfo = eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        // 字节数组
        byte[] decode = base64UrlCodec.decode(tokenUserInfo);
        // 将字节数组转换成String。
        String tokenJson =null;
        try {
            tokenJson  = new String(decode, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 将字符串转换成Map即可
        Map map = JSON.parseObject(tokenJson, Map.class);
        return map;

    }

}
