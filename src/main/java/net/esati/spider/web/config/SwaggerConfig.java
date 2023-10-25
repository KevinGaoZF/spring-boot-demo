package net.esati.spider.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Swagger2的接口配置
 * @Date 2022年5月18日
 * @author gao
 */
@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {

    private static final String SEPARATOR = ";";

    @Bean(value = "defaultApi")
    public Docket defaultApi() {
        Docket docket=new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("数据接口文档-esati tech")
                        .description("数据接口文档")
                        .termsOfServiceUrl("http://www.esati.net/")
                        .contact(new Contact("Gao","http://www.esati.net","jobs-gao@foxmail.com"))
                        .version("1.0")
                        .title("数据接口文档")
                        .build())
                //分组名称
                .groupName("全部接口")
                .select()
                //这里指定Controller扫描包路径
                .apis(basePackage("net.esati.spider.web.controller"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }


    private static Function<Class<?>, Boolean> handlerAllPackage(final String basePackage) {
        return (input) -> {
            for(String packageUrl:basePackage.split(SEPARATOR)){
                ClassUtils.getPackageName(input).startsWith(packageUrl);
            }
            return true;
        };
    }

    public static Predicate<RequestHandler> basePackage(final String basePackage) {
        return (input) -> (Boolean)declaringClass(input).map(handlerPackage(basePackage)).orElse(true);
    }

    /**
     * @author gao
     * @description 重写basePackage方法，使能够实现多包访问
     * @param basePackage 所有包路径
     * @return Function<Class<?>, Boolean>
     */
    private static Function<Class<?>, Boolean> handlerPackage(final String basePackage)     {
        return input -> {
            // 循环判断匹配
            for (String strPackage : basePackage.split(SEPARATOR)) {
                assert input != null;
                boolean isMatch = input.getPackage().getName().startsWith(strPackage);
                if (isMatch) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Optional<? extends Class<?>> declaringClass(RequestHandler input) {
        return Optional.ofNullable(input.declaringClass());
    }
}
