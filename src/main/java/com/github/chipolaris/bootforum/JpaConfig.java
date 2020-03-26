package com.github.chipolaris.bootforum;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * This class is necessary because of Eclipselink, Spring Boot use Hibernate by default 
 * 
 * References:
 * 	https://www.baeldung.com/spring-eclipselink
 * 	https://blog.marcnuri.com/spring-data-jpa-eclipselink-configuring-spring-boot-to-use-eclipselink-as-the-jpa-provider/
 * 
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("com.github.chipolaris.bootforum")
public class JpaConfig extends JpaBaseConfiguration {

    protected JpaConfig(DataSource dataSource, JpaProperties properties,
			ObjectProvider<JtaTransactionManager> jtaTransactionManager) {
		super(dataSource, properties, jtaTransactionManager);
	}

	@Override
    protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
        return new EclipseLinkJpaVendorAdapter();
    }
	
	@Override
	protected Map<String, Object> getVendorProperties() {
		return new HashMap<>();
	}
}