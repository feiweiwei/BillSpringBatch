package com.monkey01.batch.reader;

import com.monkey01.batch.bean.AlipayTranDO;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * @author: feiweiwei
 * @description:
 * @created Date: 14:23 17/11/28.
 * @modify by:
 */
@Service
public class AlipayFileItemReader {

	private FlatFileItemReader<AlipayTranDO> reader;

	public FlatFileItemReader<AlipayTranDO> getAlipayFileItemReader() {
		reader = new FlatFileItemReader<AlipayTranDO>();
		reader.setLineMapper(new DefaultLineMapper<AlipayTranDO>() {{
			setLineTokenizer(new DelimitedLineTokenizer() {{
				setNames(new String[] { "tranId", "channel", "tranType", "counterparty", "goods", "amount", "isDebitCredit", "state" });
			}});
			setFieldSetMapper(new BeanWrapperFieldSetMapper<AlipayTranDO>() {{
				setTargetType(AlipayTranDO.class);
			}});
		}});
		reader.setLinesToSkip(5);
		return reader;
	}

	public MultiResourceItemReader<AlipayTranDO> getMultiAliReader() {
		//TODO: 获取所有当天待加载的支付宝账单,
		// 这里只是简单的放了两个csv账单文件，
		// 实际处理过程中，肯定是从数据库或者接口获取需要加载的账单文件路径
		MultiResourceItemReader<AlipayTranDO> reader = new MultiResourceItemReader<AlipayTranDO>();
		Resource[] files = new Resource[]{new FileSystemResource("data/alipay/208012345_20141030.csv"),
				new FileSystemResource("data/alipay/208054321_20141030.csv")};
		reader.setResources(files);
		reader.setDelegate(this.getAlipayFileItemReader());

		return reader;
	}
}
