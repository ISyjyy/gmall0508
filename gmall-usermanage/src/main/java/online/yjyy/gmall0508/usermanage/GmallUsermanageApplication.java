package online.yjyy.gmall0508.usermanage;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "online.yjyy.gmall0508.usermanage.*")//tk.mybatis.spring.annotation.MapperScan;
@ComponentScan(basePackages = "online.yjyy.gmall0508")
public class GmallUsermanageApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallUsermanageApplication.class, args);
    }

}

