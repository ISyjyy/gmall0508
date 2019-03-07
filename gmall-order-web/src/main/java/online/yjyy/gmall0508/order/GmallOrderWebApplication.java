package online.yjyy.gmall0508.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan("online.yjyy.gmall0508")
@MapperScan(basePackages = "online.yjyy.gmall0508.order.mapper")
public class GmallOrderWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallOrderWebApplication.class, args);
    }

}

