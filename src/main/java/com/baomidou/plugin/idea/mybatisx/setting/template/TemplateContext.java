package com.baomidou.plugin.idea.mybatisx.setting.template;

import com.baomidou.plugin.idea.mybatisx.generate.template.GenerateConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 末班上下文配置
 */
public class TemplateContext {
    /**
     * 项目路径
     */
    private String projectPath;
    /**
     * 模块名称
     */
    private String moduleName;
    /**
     * 注解类型
     */
    private String annotationType;
    /**
     * 扩展模板类型
     */
    private Map<String, List<TemplateSettingDTO>> templateSettingMap = new HashMap<>();

    private GenerateConfig generateConfig;

    public GenerateConfig getGenerateConfig() {
        return generateConfig;
    }

    public void setGenerateConfig(GenerateConfig generateConfig) {
        this.generateConfig = generateConfig;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }

    public Map<String, List<TemplateSettingDTO>> getTemplateSettingMap() {
        return templateSettingMap;
    }

    public void setTemplateSettingMap(Map<String, List<TemplateSettingDTO>> templateSettingMap) {
        this.templateSettingMap = templateSettingMap;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}
