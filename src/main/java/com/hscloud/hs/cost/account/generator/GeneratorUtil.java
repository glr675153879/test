package com.hscloud.hs.cost.account.generator;

import cn.hutool.core.util.StrUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GeneratorUtil {

    // G:\nfworkspace\hs-kpi-platform\hs-cost-account\src\main\java\com\hscloud\hs\cost\account\controller\second\
    String outputDir;

    // com\hscloud\hs\cost\account
    String comPath;

    // com.hscloud.hs.cost.account
    String comPathDot;

    // 业务大类 sencond
    String childSuf = "";


    // 实际业务类（左匹配）
    String entitySuf = "";

    // G:\nfworkspace\hs-kpi-platform/hs-cost-account/src/main/java/
    String perPath;

    // G:\nfworkspace\hs-kpi-platform/hs-cost-account/src/main/resources/
    String resourcesPath;

    // entity
    String entityCode;
    // entity中文名
    String entityName;

    // 生成的文件名
    String fileName;

    /**
     * 构造方法
     *
     * @param comPathDot com.hscloud.hs.cost.account
     * @param moduleName /hs-cost-account  或  /hs-cost-account/hs-cost-biz
     * @param childSuf   second 可空
     * @param entitySuf  UnitTaskDetailItemWork 业务类前缀匹配（empty则全匹配，主要使用于增量增加entity中）
     */
    public GeneratorUtil(String comPathDot, String moduleName, String childSuf, String entitySuf) {
        if (comPathDot != null) {
            this.comPathDot = comPathDot;
            this.comPath = comPathDot.replaceAll("[.]", "/") + "/";
        }
        if (childSuf != null)
            this.childSuf = childSuf;
        if (entitySuf != null)
            this.entitySuf = entitySuf;
        if (moduleName != null) {
            this.perPath = System.getProperty("user.dir") + moduleName + "/src/main/java/";
            this.resourcesPath = System.getProperty("user.dir") + moduleName + "/src/main/resources/";
        }
    }

//    public static void main(String[] args) throws Exception {
//        GeneratorUtil generator = new GeneratorUtil("com.hscloud.hs.cost.account","/hs-cost-account","second");
//        generator.scanEntity();
//    }

    public void scanEntity() throws Exception {
        try {
            String filepath = (comPathDot + ".model.entity." + childSuf).replaceAll("[.]", "/");
            filepath = perPath + filepath;
            File file = new File(filepath);
            if (!file.isDirectory()) {
                System.out.println("不是文件夹");
            } else if (file.isDirectory()) {
                // System.out.println("文件夹");
                String[] filelist = file.list();
                for (int i = 0; i < filelist.length; i++) {
                    File readfile = new File(filepath + "/" + filelist[i]);
                    if (!readfile.isDirectory()) {
                        String fileName = readfile.getName();
                        if (!StrUtil.startWith(fileName, entitySuf)) {
                            System.out.println(fileName + " 不符合左匹配，忽略");
                            continue;
                        }
                        fileName = fileName.substring(0, fileName.indexOf("."));
                        // System.out.println("fileName="+fileName);
                        this.entityName = this.getEntityName(readfile);
                        this.entityCode = StrUtil.lowerFirst(fileName);
                        this.genContorller();
                        this.genService();
                        this.genMapper();
                    }
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("readfile()   Exception:" + e.getMessage());
        }
    }

    private String getEntityName(File readfile) {
        String targetString = "@Schema(";
        try (BufferedReader br = new BufferedReader(new FileReader(readfile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 如果读取到以指定字符串开头的行，则提取字符串并返回
                if (line.trim().startsWith(targetString)) {
                    // 获取字符串部分
                    String extractedString = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'));
                    // System.out.println("Extracted String: " + extractedString);
                    return extractedString; // 停止读取
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "entity名称未取到";
    }


    public void genContorller() throws Exception {
        this.outputDir = this.perPath + this.comPath + "controller/" + (this.childSuf == null ? "" : this.childSuf + "/");
        this.fileName = StrUtil.upperFirst(this.entityCode) + "Controller.java";
        this.generator("Controller.ftl");
    }

    public void genService() throws Exception {
        this.outputDir = this.perPath + this.comPath + "service/" + (this.childSuf == null ? "" : this.childSuf + "/");
        this.fileName = "I" + StrUtil.upperFirst(this.entityCode) + "Service.java";
        this.generator("IService.ftl");

        this.outputDir = this.perPath + this.comPath + "service/impl/" + (this.childSuf == null ? "" : this.childSuf + "/");
        this.fileName = StrUtil.upperFirst(this.entityCode) + "Service.java";
        this.generator("Service.ftl");
    }

    public void genMapper() throws Exception {
        this.outputDir = this.perPath + this.comPath + "mapper/" + (this.childSuf == null ? "" : this.childSuf + "/");
        this.fileName = StrUtil.upperFirst(this.entityCode) + "Mapper.java";
        this.generator("Mapper.ftl");

        this.outputDir = this.resourcesPath + "mapper/" + (this.childSuf == null ? "" : this.childSuf + "/");
        this.fileName = StrUtil.upperFirst(this.entityCode) + "Mapper.xml";
        this.generator("MapperXml.ftl");
    }

    public void generator(String templateFileName) throws Exception {
        // 定义模板文件路径
        String templateDir = "templates/";

        // 定义生成文件路径
        // String outputDir = "output/";
        String packageName = "com.example.controller";
        String className = "User1Controller";
        String classNamePlural = "Users";

        // entity字段
        // String entityName = "任务";

        // 创建 FreeMarker 配置对象
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassLoaderForTemplateLoading(GeneratorUtil.class.getClassLoader(), templateDir);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // 创建数据模型
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("comPathDot", this.comPathDot);
        dataModel.put("childSufDot", "." + this.childSuf);
        dataModel.put("childSufPath", "/" + this.childSuf);
        dataModel.put("entityCode", this.entityCode);
        dataModel.put("entityName", this.entityName);
        dataModel.put("className", this.entityCode);
        // 加载模板文件
        Template template = cfg.getTemplate(templateFileName);

        // 创建输出目录
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        // 定义输出文件
        File outputFile = new File(outputDirectory, this.fileName);
        if (!outputFile.exists()) {
            // 使用 FreeMarker 渲染模板并生成代码
            try (Writer fileWriter = new FileWriter(outputFile)) {
                template.process(dataModel, fileWriter);
            }
            System.out.println(this.entityCode + this.entityName + " generated successfully: " + outputFile.getAbsolutePath());
        }
    }


}
