package online.yjyy.gmall0508.passport;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
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

    @Test
    public void message() {
      /*  //测试短信发送
        DefaultProfile profile = DefaultProfile.getProfile("default", "LTAIMzPZ96LdKqwf", "nZ6fBMnTnK2MkJIxdanbBGpN9uoZFG");
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("PhoneNumbers", "13457926297");
        request.putQueryParameter("SignName", "谷粒谷粒");
        request.putQueryParameter("TemplateCode", "SMS_165108795");
        request.putQueryParameter("TemplateParam", "{\"code\":\"123456\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }*/

    }

}
