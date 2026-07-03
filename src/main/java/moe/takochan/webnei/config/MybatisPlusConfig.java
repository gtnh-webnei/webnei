package moe.takochan.webnei.config;

import javax.sql.DataSource;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factory.setConfiguration(configuration);
        factory.setDataSource(dataSource);
        return factory.getObject();
    }
}
