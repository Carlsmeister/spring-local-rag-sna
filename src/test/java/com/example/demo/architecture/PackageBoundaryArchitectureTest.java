package com.example.demo.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.demo", importOptions = ImportOption.DoNotIncludeTests.class)
public class PackageBoundaryArchitectureTest {

    // Rule 1: Career Document Analysis core packages should have absolutely zero dependencies on conceptual future spaces.
    @ArchTest
    public static final ArchRule protectCoreModulesFromFutureSlices = noClasses()
        .that().resideInAnyPackage(
            "com.example.demo.cv..",
            "com.example.demo.job..",
            "com.example.demo.rewrite..",
            "com.example.demo.document..",
            "com.example.demo.ai.."
        )
        .should().dependOnClassesThat().resideInAnyPackage(
            "com.example.demo.socialnetwork..",
            "com.example.demo.risk..",
            "com.example.demo.securityanalysis.."
        );

    // Rule 2: Controllers must only be accessed by our web infrastructure layers, never by services or repositories.
    @ArchTest
    public static final ArchRule restrictControllerAccess = classes()
        .that().resideInAPackage("..controller..")
        .should().onlyBeAccessed().byAnyPackage("..controller..", "..security..", "..web..");

    // Rule 3: Repositories must only be accessed by services, enforcing Thin Controllers (controllers must never directly call database repositories).
    @ArchTest
    public static final ArchRule enforceThinControllersNoDirectRepositoryAccess = noClasses()
        .that().resideInAPackage("..controller..")
        .should().dependOnClassesThat().resideInAPackage("..repository..");

    // Rule 4: Services must not be accessed by repositories.
    @ArchTest
    public static final ArchRule restrictServiceAccessFromRepositories = noClasses()
        .that().resideInAPackage("..repository..")
        .should().dependOnClassesThat().resideInAPackage("..service..");
}
