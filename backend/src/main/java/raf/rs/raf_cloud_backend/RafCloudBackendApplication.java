package raf.rs.raf_cloud_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy
@EnableAsync
public class RafCloudBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RafCloudBackendApplication.class, args);
	}

}
