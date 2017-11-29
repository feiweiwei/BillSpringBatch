package com.monkey01.batch.config;

import com.monkey01.batch.bean.AlipayTranDO;
import com.monkey01.batch.bean.HopPayTranDO;
import com.monkey01.batch.processor.AlipayItemProcessor;
import com.monkey01.batch.reader.AlipayFileItemReader;
import com.monkey01.batch.writer.AlipayFileItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author: feiweiwei
 * @description:
 * @created Date: 13:36 17/11/28.
 * @modify by:
 */

@Configuration
@EnableBatchProcessing
public class BillBatchConfig {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public DataSource dataSource;

	@Autowired
	private AlipayFileItemReader alipayFileItemReader;

	@Autowired
	private AlipayItemProcessor alipayItemProcessor;

	@Autowired
	private AlipayFileItemWriter alipayFileItemWriter;

	@Bean
	public Job importAliJob() {
		return jobBuilderFactory.get("importAliJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
				.end()
				.build();
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1")
				.<AlipayTranDO, HopPayTranDO> chunk(10)
				.reader(alipayFileItemReader.getMultiAliReader())
				.processor(alipayItemProcessor)
				.writer(alipayFileItemWriter.getAlipayItemWriter())
				.build();
	}

}
