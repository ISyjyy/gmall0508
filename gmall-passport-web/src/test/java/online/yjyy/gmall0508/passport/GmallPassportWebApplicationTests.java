package online.yjyy.gmall0508.passport;

import online.yjyy.gmall0508.passport.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

    @Test
    public void contextLoads() {
    }
    @Test
    public void test01(){
        //  公共部分
        String key = "Atguigu";
        // 私钥
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId","1001");
        map.put("nickName","Administrator");
        // 签名部分 salt
        String salt = "192.168.67.2";

        // 67.2  == 672.2
        // 加密
        // eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIxMDAxIn0.ljGIO4Wr3U9kZf3N1eR-FNJUtATs8Kw7VwRTt3GPJuY
        String token = JwtUtil.encode(key, map, salt);
        System.out.println("token:="+token);

        // 解密
        Map<String, Object> objectMap = JwtUtil.decode(token, key, "192.168.672.2");
        System.out.println(objectMap);
//		{nickName=Administrator, userId=1001}

    }
}
