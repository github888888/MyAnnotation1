package com.example;

import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MyProcessor extends AbstractProcessor{
    private static final String SUFFIX = "$$LONG";

    private Filer filer;
    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {

        super.init(processingEnv);
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Hello.class);
        for (Element item : set) {
            if (item.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "only support class!");
            }


            TypeElement typeElement = (TypeElement) item;
            messager.printMessage(Diagnostic.Kind.NOTE, typeElement.getQualifiedName());

            List<ExecutableElement> enclosedElements = (List<ExecutableElement>) item.getEnclosedElements();
            for (Element item1 : enclosedElements) {
                messager.printMessage(Diagnostic.Kind.NOTE, item1.getSimpleName());
            }
            PackageElement enclosingElement = (PackageElement) item.getEnclosingElement();
            messager.printMessage(Diagnostic.Kind.NOTE, enclosingElement.getQualifiedName());

            messager.printMessage(Diagnostic.Kind.NOTE, enclosingElement.getQualifiedName() + "." + typeElement.getSimpleName() + SUFFIX);

            try {
                StringBuilder builder = new StringBuilder();
                builder.append("package " + enclosingElement.getQualifiedName() + ";\n\n");
                builder.append("import android.content.Context;\n" +
                        "import android.widget.Toast;\n" +
                        "\n" +
                        "public class MainActivity$$LONG {\n" +
                        "    public void sayHello(Context context) {\n" +
                        "        Toast.makeText(context, \"hello world!\", Toast.LENGTH_LONG).show();\n" +
                        "    }\n" +
                        "}");
                String value = builder.toString();
                JavaFileObject jfo = filer.createSourceFile(enclosingElement.getQualifiedName() + "." + typeElement.getSimpleName() + SUFFIX, typeElement);
                Writer writer = jfo.openWriter();
                writer.write(value);
                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // TODO
            }
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Hello.class.getCanonicalName());
    }

}
