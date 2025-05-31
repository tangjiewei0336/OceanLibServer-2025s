package com.oriole.ocean;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Ocean QA Service All Tests")
@SelectPackages({
    "com.oriole.ocean.service.impl",  // 单元测试
    "com.oriole.ocean.integration"    // 集成测试
})
public class TestSuite {
    // 这个类不需要任何实现
    // 它只是作为一个容器来组织测试
} 