package com.monkey01.batch.fault;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author: feiweiwei
 * @description:
 * @created Date: 16:39 17/11/29.
 * @modify by:
 */
public class UnresolveAlipayTranException extends Exception {
	private static final String INSERT_ALYPAY_ERROR =
			"insert into alipay_tran_error(error) values(?)";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public UnresolveAlipayTranException(){
		super();
	}

	public UnresolveAlipayTranException(String message, Throwable cause){
		super(message, cause);
		jdbcTemplate.update(INSERT_ALYPAY_ERROR, message);
	}

	public UnresolveAlipayTranException(String message){
		super(message);
		jdbcTemplate.update(INSERT_ALYPAY_ERROR, message);
	}

	public UnresolveAlipayTranException(Throwable cause){
		super(cause);
		jdbcTemplate.update(INSERT_ALYPAY_ERROR, cause.getMessage());

	}
}
