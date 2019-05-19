package online.yjyy.gmall0508.passport.config;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;

import java.util.Random;
import java.util.UUID;

public class MessageUtil {
    public static String  sendMessage(String phone){

      //  String code=UUID.randomUUID().toString().substring(0,6);
        String  code =String.valueOf((Math.random() * 9 + 1) * 100000).substring(0,6);
      // String code= numbers+"";
        DefaultProfile profile = DefaultProfile.getProfile("default", "LTAIMzPZ96LdKqwf", "nZ6fBMnTnK2MkJIxdanbBGpN9uoZFG");
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", "谷粒谷粒");
        request.putQueryParameter("TemplateCode", "SMS_165108795");
        request.putQueryParameter("TemplateParam", "{\"code\":\""+code+"\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }

        return code;
    }
}
