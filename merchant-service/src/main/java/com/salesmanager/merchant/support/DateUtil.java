package com.salesmanager.merchant.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtil {

	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	private DateUtil() {
	}

	public static String formatDate(Date dt) {
		if (dt == null) {
			return null;
		}
		return new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(dt);
	}

	public static Date getDate(String date) throws ParseException {
		return new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(date);
	}
}
