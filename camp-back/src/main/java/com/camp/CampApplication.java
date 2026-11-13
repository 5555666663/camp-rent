package com.camp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 多网点露营装备租赁系统 - 后端启动类
 *
 * @author camp
 */
@SpringBootApplication
@MapperScan("com.camp.mapper")
public class CampApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampApplication.class, args);
        System.out.println("==========================================");
        System.out.println(" 露营装备租赁系统后端启动成功！");
        System.out.println(" 访问地址: http://localhost:8080");
        System.out.println("==========================================");
    }
}
