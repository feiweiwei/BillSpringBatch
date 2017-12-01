package com.monkey01.batch.writer;

import com.monkey01.batch.bean.AlipayTranDO;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: feiweiwei
 * @description:
 * @created Date: 14:28 17/11/29.
 * @modify by:
 */
@Service
public class AlipayDBItemWriter implements ItemWriter<AlipayTranDO> {
	private static final String INSERT_ALYPAY_TRAN =
			"insert into alipay_tran_today(tran_id, channel, tran_type, counter_party, goods, amount, is_debit_credit, state) values(?,?,?,?,?,?,?,?)";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void write(List<? extends AlipayTranDO> list) throws Exception {
		for(AlipayTranDO alipayTran : list){
			jdbcTemplate.update(INSERT_ALYPAY_TRAN,
					alipayTran.getTranId(),
					alipayTran.getChannel(),
					alipayTran.getTranType(),
					alipayTran.getCounterparty(),
					alipayTran.getGoods(),
					alipayTran.getAmount(),
					alipayTran.getIsDebitCredit(),
					alipayTran.getState());
		}
	}
}
