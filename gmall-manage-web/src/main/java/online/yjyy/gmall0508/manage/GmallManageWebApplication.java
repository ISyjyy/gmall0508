package online.yjyy.gmall0508.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@ComponentScan(basePackages = "online.yjyy.gmall0508")
@MapperScan(basePackages = "online.yjyy.gmall0508.manage.mapper")
public class GmallManageWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallManageWebApplication.class, args);
    }

}

