package com.salesmanager.merchant.support;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import com.salesmanager.core.model.common.Criteria;
import com.salesmanager.core.model.merchant.MerchantStoreCriteria;

public final class ServiceRequestCriteriaBuilderUtils {

	private ServiceRequestCriteriaBuilderUtils() {
	}

	public static Criteria buildRequestCriterias(
			Criteria criteria,
			Map<String, String> mappingFields,
			HttpServletRequest request) {
		if (criteria == null) {
			throw new RestApiException("A criteria class type must be instantiated");
		}
		mappingFields.keySet().forEach(parameterName -> {
			try {
				setValue(criteria, request, parameterName, mappingFields.get(parameterName));
			} catch (Exception e) {
				throw new RestApiException("Error while binding request parameters");
			}
		});
		return criteria;
	}

	private static void setValue(
			Criteria criteria,
			HttpServletRequest request,
			String parameterName,
			String setterName) throws Exception {
		PropertyAccessor criteriaAccessor = PropertyAccessorFactory.forDirectFieldAccess(criteria);
		String parameterValue = request.getParameter(parameterName);
		if (parameterValue == null) {
			return;
		}
		criteriaAccessor.setPropertyValue(setterName, parameterValue);
	}

	public static MerchantStoreCriteria createMerchantStoreCriteria(
			Map<String, String> mappingFields,
			HttpServletRequest request) {
		return (MerchantStoreCriteria) buildRequestCriterias(new MerchantStoreCriteria(), mappingFields, request);
	}
}
