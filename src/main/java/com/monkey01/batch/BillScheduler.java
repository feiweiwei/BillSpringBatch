package com.monkey01.batch;

import com.monkey01.batch.config.BillBatchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: feiweiwei
 * @description:
 * @created Date: 15:31 17/11/30.
 * @modify by:
 */
@Component
public class BillScheduler {
	@Autowired
	private BillBatchConfig billBatchConfig;

	private static final Logger log = LoggerFactory.getLogger(BillScheduler.class);

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Scheduled(initialDelay=10000, fixedRate = 10000)
	public void fixedBillBatch() {
		log.info("job begin {}", dateFormat.format(new Date()));
		billBatchConfig.run();
		log.info("job end {}", dateFormat.format(new Date()));
	}

	@Scheduled(cron="0 15 10 ? * *")
	public void fixedTimePerDayBillBatch() {
		log.info("job begin {}", dateFormat.format(new Date()));
		billBatchConfig.run();
		log.info("job end {}", dateFormat.format(new Date()));
	}
}
