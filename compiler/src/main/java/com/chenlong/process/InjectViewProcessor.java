package com.chenlong.process;

import com.chenlong.anno.InjectView;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by chenlong on 2016/6/16.
 */
@SupportedAnnotationTypes({"com.chenlong.anno.InjectView", "com.chenlong.anno.InjectLayout"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class InjectViewProcessor extends AbstractProcessor {
    private static final String MODIFY_PRIVATE = "private";
    private static final String PROXY = "$$PROXY";
    private Name basePackageName = null;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Map<Name, ClazzInfo> map = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(com.chenlong.anno.InjectView.class);
        if(elementsAnnotatedWith == null || elementsAnnotatedWith.size() == 0) return false;
        // 解析数据封装到map集合中
        for(Element element : elementsAnnotatedWith) {
            VariableElement variableElement = (VariableElement) element;

            // 判断被注解成员变量的访问权限
            String str_modify = variableElement.getModifiers().toString();
            if(str_modify.contains(MODIFY_PRIVATE)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@InjectView 注解的成员变量访问权限不能是私有，否则代理类无法访问");
                return false;
            }

            // 封装一个成员对象，需要获取对应的资源id， 成员变量名称， 成员变量的类型描述
            int id = variableElement.getAnnotation(InjectView.class).value();
            Name name = variableElement.getSimpleName();
            TypeMirror type = variableElement.asType();
            com.chenlong.process.MemberInfo meminfo = new com.chenlong.process.MemberInfo(id, name, type);

            // 获取对应的类信息
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            Name qualifiedName = enclosingElement.getQualifiedName();
            Name simpleName = enclosingElement.getSimpleName();

            // 判断对应的类是否在map集合中
            if(map.containsKey(qualifiedName)) {
                ClazzInfo info = map.get(qualifiedName);
                info.memberMap.put(id, meminfo);
            } else {
                // 获取包名
                Name packageQualifiedName = elementUtils.getPackageOf(enclosingElement).getQualifiedName();
                if(basePackageName == null) {
                    basePackageName = packageQualifiedName;
                }

                // 获取布局资源id
                com.chenlong.anno.InjectLayout annotation = enclosingElement.getAnnotation(com.chenlong.anno.InjectLayout.class);
                if(null == annotation) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "请在类的前面标示布局资源 eg: @InjectLayout(R.layout.xxx)");
                }
                int layoutid = annotation.value();

                // 获取点击资源数组
                int[] clickids = null;
                List<? extends Element> enclosedElements = enclosingElement.getEnclosedElements();
                for (Element item : enclosedElements) {
                    if(null == item.getAnnotation(com.chenlong.anno.ClickView.class)) continue;
                    clickids = item.getAnnotation(com.chenlong.anno.ClickView.class).value();
                    break;
                }
                ClazzInfo info = new ClazzInfo(simpleName, qualifiedName, packageQualifiedName, layoutid, clickids);
                info.memberMap.put(id, meminfo);
                map.put(qualifiedName, info);
            }
        }

        // 生成view注解工厂类，需要用到运行时反射创建类
        generateFactory();
        // 生成activity的代理类，帮助初始化view
        generateBaseActivityProxy();

        // 生成java文件
        for(Name item : map.keySet()) {
            generateFileByClazzInfo(map.get(item));
        }

        return false;
    }

    private void generateFactory() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("package " + basePackageName + ";\n");
            builder.append("\n");
            builder.append("import android.app.Activity;\n");
            builder.append("\n");
            builder.append("public class InjectFactory {\n");
            builder.append("    public static final String  PROXY = \"$$PROXY\";\n");
            builder.append("    @SuppressWarnings(\"unchecked\")\n");
            builder.append("    public static <T extends Activity> void inject(T t) {\n");
            builder.append("        try {\n");
            builder.append("             String proxyName = t.getClass().getName() + PROXY;\n");
            builder.append("             Class<?> aClass = Class.forName(proxyName);\n");
            builder.append("             BaseActivityProxy proxy = (BaseActivityProxy) aClass.newInstance();\n");
            builder.append("             proxy.injectLayout(t);\n");
            builder.append("             proxy.injectView(t);\n");
            builder.append("             proxy.injectClick(t);\n");
            builder.append("        } catch (Exception e) {\n");
            builder.append("             e.printStackTrace();\n");
            builder.append("        }\n");
            builder.append("    }\n");
            builder.append("}\n");
            String value = builder.toString();
            JavaFileObject jfo = filer.createSourceFile(basePackageName + ".InjectFactory");
            Writer writer = jfo.openWriter();
            writer.write(value);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateBaseActivityProxy() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("package " + basePackageName + ";\n");
            builder.append("\n");
            builder.append("import android.app.Activity;\n");
            builder.append("\n");
            builder.append("public abstract class BaseActivityProxy<T extends  Activity> {\n");
            builder.append("    public abstract void injectView(T t);\n");
            builder.append("    public abstract void injectLayout(T t);\n");
            builder.append("    public abstract void injectClick(T t);\n");
            builder.append("}\n");
            String value = builder.toString();
            JavaFileObject jfo = filer.createSourceFile(basePackageName + ".BaseActivityProxy");
            Writer writer = jfo.openWriter();
            writer.write(value);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateFileByClazzInfo(ClazzInfo clazzInfo) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("package " + clazzInfo.packageName + ";\n");
            builder.append("\n");
            builder.append("import " + basePackageName + ".BaseActivityProxy;\n");
            builder.append("\n");
            builder.append("public class " + clazzInfo.clazzName + PROXY + " extends BaseActivityProxy<" + clazzInfo.clazzName + "> {\n");
            builder.append("    @Override\n");
            builder.append("    public void injectView(" + clazzInfo.clazzName + " activity) {\n");
            for (Map.Entry<Integer, com.chenlong.process.MemberInfo> item : clazzInfo.memberMap.entrySet()) {
                builder.append("        activity." + item.getValue().name + " = (" + item.getValue().type.toString() +") activity.findViewById("+ item.getKey() +");\n");
            }
            builder.append("    }\n");
            builder.append("\n");
            builder.append("    @Override\n");
            builder.append("    public void injectLayout(" + clazzInfo.clazzName + " activity) {\n");
            builder.append("        activity.setContentView(" + clazzInfo.layoutid + ");\n");
            builder.append("    }\n");
            builder.append("\n");
            builder.append("    @Override\n");
            builder.append("    public void injectClick(" + clazzInfo.clazzName + " activity) {\n");
            if(null == clazzInfo.clickIds || clazzInfo.clickIds.length == 0) {
                builder.append("        // NOTE: No View need setClickListener!\n");
            } else {
                for(int item : clazzInfo.clickIds) {
                    com.chenlong.process.MemberInfo info = clazzInfo.memberMap.get(item);
                    if(info == null) continue;
                    builder.append("        activity." + info.name +".setOnClickListener(activity);\n");
                }
            }
            builder.append("    }\n");
            builder.append("}\n");
            String value = builder.toString();
            JavaFileObject jfo = filer.createSourceFile(clazzInfo.packageName + "." + clazzInfo.clazzName + PROXY);
            Writer writer = jfo.openWriter();
            writer.write(value);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}