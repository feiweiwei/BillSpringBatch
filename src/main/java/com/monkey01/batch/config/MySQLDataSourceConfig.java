package com.monkey01.batch.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author: feiweiwei
 * @description:
 * @created Date: 14:24 17/11/29.
 * @modify by:
 */
@Configuration
public class MySQLDataSourceConfig {
	@Bean(name = "mysqlDataSource")
	@Qualifier("mysqlDataSource")
	@ConfigurationProperties(prefix="spring.datasource")
	public DataSource mysqlDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "mysqldbcTemplate")
	public JdbcTemplate mysqlJdbcTemplate(
			@Qualifier("mysqlDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
}
