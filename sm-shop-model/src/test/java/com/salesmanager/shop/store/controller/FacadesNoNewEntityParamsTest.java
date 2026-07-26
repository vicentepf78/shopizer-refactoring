package com.salesmanager.shop.store.controller;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import java.util.Set;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.store.controller.category.facade.CategoryFacade;
import com.salesmanager.shop.store.controller.order.facade.v1.OrderFacade;
import com.salesmanager.shop.store.controller.product.facade.ProductCommonFacade;
import com.salesmanager.shop.store.controller.shoppingCart.facade.v1.ShoppingCartFacade;
import com.salesmanager.shop.store.controller.shipping.facade.ShippingFacade;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packages = "com.salesmanager.shop.store.controller", importOptions = ImportOption.DoNotIncludeTests.class)
class FacadesNoNewEntityParamsTest {

	private static final Set<String> CATEGORY_LEGACY_METHODS = Set.of(
			"saveCategory",
			"deleteCategory",
			"move",
			"setVisible");

	private static final Set<String> PRODUCT_LEGACY_METHODS = Set.of(
			"saveProduct",
			"update",
			"deleteProduct",
			"addProductToCategory",
			"removeProductFromCategory",
			"saveOrUpdateReview",
			"deleteReview",
			"updateProductPrice",
			"updateProductQuantity");

	@ArchTest
	static final ArchRule facadesNoNewEntityParams = methods()
			.that().areDeclaredIn(OrderFacade.class)
			.or().areDeclaredIn(ShoppingCartFacade.class)
			.or().areDeclaredIn(ShippingFacade.class)
			.or().areDeclaredIn(CategoryFacade.class)
			.or().areDeclaredIn(ProductCommonFacade.class)
			.should(notDeclareEntityTenantParameters());

	private static ArchCondition<JavaMethod> notDeclareEntityTenantParameters() {
		return new ArchCondition<JavaMethod>("not declare MerchantStore or Language parameters") {
			@Override
			public void check(JavaMethod method, ConditionEvents events) {
				if (allowsLegacyEntityParameters(method)) {
					return;
				}
				for (var parameter : method.getRawParameterTypes()) {
					if (MerchantStore.class.getName().equals(parameter.getName())
							|| Language.class.getName().equals(parameter.getName())) {
						String message = String.format(
								"Method %s.%s must use MerchantStoreId/LanguageCode instead of entity parameters",
								method.getOwner().getSimpleName(),
								method.getName());
						events.add(SimpleConditionEvent.violated(method, message));
					}
				}
			}
		};
	}

	private static boolean allowsLegacyEntityParameters(JavaMethod method) {
		String owner = method.getOwner().getSimpleName();
		String name = method.getName();
		if ("CategoryFacade".equals(owner)) {
			if (CATEGORY_LEGACY_METHODS.contains(name)) {
				return true;
			}
			return "getByCode".equals(name) && method.getRawParameterTypes().size() == 2
					&& "java.lang.String".equals(method.getRawParameterTypes().get(0).getName());
		}
		if ("ProductCommonFacade".equals(owner)) {
			return PRODUCT_LEGACY_METHODS.contains(name);
		}
		return false;
	}
}
