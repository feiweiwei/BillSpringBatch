package com.monkey01.batch.config;

import com.monkey01.batch.bean.AlipayTranDO;
import com.monkey01.batch.bean.HopPayTranDO;
import com.monkey01.batch.fault.UnresolveAlipayTranException;
import com.monkey01.batch.listener.AlipaySkipListener;
import com.monkey01.batch.listener.JobCompletionNotificationListener;
import com.monkey01.batch.processor.AlipayItemProcessor;
import com.monkey01.batch.processor.AlipayValidateProcessor;
import com.monkey01.batch.reader.AlipayFileItemReader;
import com.monkey01.batch.writer.AlipayDBItemWriter;
import com.monkey01.batch.writer.AlipayFileItemWriter;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	public JobLauncher jobLauncher;

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	private AlipayFileItemReader alipayFileItemReader;

	@Autowired
	private AlipayItemProcessor alipayItemProcessor;

	@Autowired
	private AlipayFileItemWriter alipayFileItemWriter;

	@Autowired
	private AlipayDBItemWriter alipayDBItemWriter;

	@Autowired
	private AlipaySkipListener listener;

	public void run() {
		try {
			String dateParam = new Date().toString();
			JobParameters param =    new JobParametersBuilder().addString("date", dateParam).toJobParameters();
			System.out.println(dateParam);
			JobExecution execution = jobLauncher.run(importAliJob(), param);             //执行job
			System.out.println("Exit Status : " + execution.getStatus());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Bean
	public Job importAliJob() {
		return jobBuilderFactory.get("importAliJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
//				.next(step2())
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


	@Bean
	public Step step2() {
		return stepBuilderFactory.get("step2")
				.<AlipayTranDO, AlipayTranDO> chunk(10)
				.reader(alipayFileItemReader.getMultiAliReader())
				.writer(alipayDBItemWriter)
				.faultTolerant()
				.skipLimit(20)
				.skip(Exception.class)
				.listener(listener)
				.retryLimit(3)
				.retry(RuntimeException.class)
				.build();
	}

	@Bean
	public Step step3() {
		CompositeItemProcessor<AlipayTranDO,HopPayTranDO> compositeItemProcessor = new CompositeItemProcessor<AlipayTranDO,HopPayTranDO>();
		List compositeProcessors = new ArrayList();
		compositeProcessors.add(new AlipayValidateProcessor());
		compositeProcessors.add(new AlipayItemProcessor());
		compositeItemProcessor.setDelegates(compositeProcessors);
		return stepBuilderFactory.get("step3")
				.<AlipayTranDO, HopPayTranDO> chunk(10)
				.reader(alipayFileItemReader.getMultiAliReader())
				.processor(compositeItemProcessor)
				.writer(alipayFileItemWriter.getAlipayItemWriter())
				.build();
	}

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(4);
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}

	@Bean
	public Step step4() {
		return stepBuilderFactory.get("step3")
				.<AlipayTranDO, HopPayTranDO> chunk(10)
				.reader(alipayFileItemReader.getMultiAliReader())
				.processor(alipayItemProcessor)
				.writer(alipayFileItemWriter.getAlipayItemWriter())
				.taskExecutor(taskExecutor())
				.throttleLimit(4)
				.build();
	}

}
