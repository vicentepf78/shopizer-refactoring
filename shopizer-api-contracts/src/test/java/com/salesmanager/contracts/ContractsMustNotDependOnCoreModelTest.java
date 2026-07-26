package com.salesmanager.contracts;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.salesmanager.contracts", importOptions = ImportOption.DoNotIncludeTests.class)
class ContractsMustNotDependOnCoreModelTest {

	@ArchTest
	static final ArchRule contractsMustNotDependOnCoreModel = noClasses()
			.that().resideInAPackage("com.salesmanager.contracts..")
			.should().dependOnClassesThat().resideInAPackage("com.salesmanager.core.model..");

}
