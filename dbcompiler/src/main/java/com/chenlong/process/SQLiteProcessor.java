package com.chenlong.process;

import com.chenlong.anno.Column;
import com.chenlong.anno.DBConfiguration;
import com.chenlong.anno.Format;
import com.chenlong.anno.TableName;
import com.chenlong.base.ClazzInfo;
import com.chenlong.base.MemberInfo;

import java.io.Writer;
import java.util.BitSet;
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
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by Long on 2016/6/29.
 */
@SupportedAnnotationTypes({"com.chenlong.anno.Column", "com.chenlong.anno.DBConfiguration",
        "com.chenlong.anno.Format", "com.chenlong.anno.TableName"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SQLiteProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Name basePackageName = null;
    private static final String PROXY = "$DaoImpl";
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
        Set<? extends Element> elementsWithDBConfiguration = roundEnv.getElementsAnnotatedWith(DBConfiguration.class);
        // 编译时gradle会调用两次，阻止多个配置问题，只需要配置一个即可
        if (elementsWithDBConfiguration == null || elementsWithDBConfiguration.size() != 1)
            return false;
        for (Element item : elementsWithDBConfiguration) {
            TypeElement element = (TypeElement) item;
            // 获取包名
            basePackageName = elementUtils.getPackageOf(element).getQualifiedName();
            // 所有生成的代码放在项目的包名下
            ClazzInfo.packageName = basePackageName;
            // 获取数据库配置信息
            DBConfiguration configuration = element.getAnnotation(DBConfiguration.class);
            // 生成AbstractOpenHelper类
            generateAbastactOpenHelper(configuration);
        }
        generateDaoImplFactory();

        Set<? extends Element> elementsWithTableName = roundEnv.getElementsAnnotatedWith(TableName.class);
        if (elementsWithTableName == null || elementsWithTableName.size() == 0) return false;
        for (Element item : elementsWithTableName) {
            TypeElement element = (TypeElement) item;
            // 获取表名
            String tableName = element.getAnnotation(TableName.class).value();
            // 获取类名称
            Name className = element.getSimpleName();
            // 获取类完全限定名称
            Name qualifiedName = element.getQualifiedName();

            ClazzInfo info = new ClazzInfo(className, qualifiedName, tableName);
            map.put(qualifiedName, info);

            List<? extends Element> enclosedElements = element.getEnclosedElements();
            for (Element item1 : enclosedElements) {
                if (item1 instanceof VariableElement) {
                    VariableElement element1 = (VariableElement) item1;
                    Column annotation = element1.getAnnotation(Column.class);
                    if (null == annotation) continue;
                    String columnName = annotation.value();
                    String simpleName = element1.getSimpleName().toString();
                    TypeMirror typeMirror = element1.asType();
                    MemberInfo memberInfo = new MemberInfo(columnName, simpleName, typeMirror);
                    info.map.put(simpleName.toLowerCase(), memberInfo);
                    // 检查是否有格式化的注解
                    Format format = element1.getAnnotation(Format.class);
                    if (null == format) continue;
                    memberInfo.pattern = format.value();
                    messager.printMessage(Diagnostic.Kind.NOTE, memberInfo.pattern);
                } else if (item1 instanceof ExecutableElement) {
                    ExecutableElement element1 = (ExecutableElement) item1;
                    String simpleName = element1.getSimpleName().toString();

                    if (simpleName.toString().toLowerCase().startsWith("get")) {
                        // 获取get方法
                        String name = simpleName.toString().substring(3).toLowerCase();
                        if (info.map.containsKey(name)) {
                            info.map.get(name).getMethod = simpleName;
                        }
                    } else if (simpleName.toString().toLowerCase().startsWith("is")) {
                        // 获取is方法
                        String name = simpleName.toString().substring(2).toLowerCase();
                        if (info.map.containsKey(name)) {
                            info.map.get(name).getMethod = simpleName;
                        }
                    } else if (simpleName.toString().toLowerCase().startsWith("set")) {
                        // 获取set方法
                        String name = simpleName.toString().substring(3).toLowerCase();
                        if (info.map.containsKey(name)) {
                            info.map.get(name).setMethod = simpleName;
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        Set<Name> names = map.keySet();
        for (Name item : names) {
            generateDaoImpl(map.get(item));
        }
        return true;
    }

    private void generateDaoImpl(ClazzInfo clazzInfo) {
        try {
            StringBuilder builder = new StringBuilder();
            // 包名
            builder.append("package " + basePackageName + ";                                                                                             \n");
            builder.append("                                                                                                                             \n");
            // 需要导入的包
            for (String item : clazzInfo.importList) {
                builder.append(item + "                                                                                                                  \n");
            }
            builder.append("                                                                                                                             \n");
            builder.append("public class " + clazzInfo.clazzName + PROXY + " implements DaoImpl<" + clazzInfo.clazzName + "> {                           \n");
            builder.append("    protected SQLiteDatabase db;                                                                                             \n");
            builder.append("                                                                                                                             \n");
            builder.append("    public " + clazzInfo.clazzName + PROXY + "() {                                                                           \n");
            builder.append("        this.db = DaoImplFactory.getDataBase();                                                                              \n");
            builder.append("    }                                                                                                                        \n");
            builder.append("                                                                                                                             \n");
            // insert
            builder.append("    @Override                                                                                                                \n");
            builder.append("    public long insert(" + clazzInfo.clazzName + " t) {                                                                      \n");
            builder.append("        if (null == t)                                                                                                       \n");
            builder.append("            return -1;                                                                                                       \n");
            builder.append("        ContentValues values = new ContentValues();                                                                          \n");
            Set<String> strings = clazzInfo.map.keySet();
            for (String item : strings) {
                MemberInfo memberInfo = clazzInfo.map.get(item);
                String type_str = memberInfo.type.toString();
                if ("byte".equals(type_str)) {  // byte基本类型
                    builder.append("        values.put(\"" + memberInfo.columnName + "\", CommonUtils.byte2Int(t." + memberInfo.getMethod + "()));       \n");
                } else if ("java.lang.Byte".equals(type_str)) { // Byte包装类型
                    builder.append("        if (null != t." + memberInfo.getMethod + "()) {                                                              \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", CommonUtils.byte2Int(t." + memberInfo.getMethod + "()));   \n");
                    builder.append("        }                                                                                                            \n");
                } else if ("boolean".equals(type_str)) {  // boolean基本类型
                    builder.append("        values.put(\"" + memberInfo.columnName + "\", CommonUtils.boolean2int(t." + memberInfo.getMethod + "()));    \n");
                } else if ("java.lang.Boolean".equals(type_str)) { // Boolean包装类型
                    builder.append("        if (null != t." + memberInfo.getMethod + "()) {                                                              \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", CommonUtils.boolean2int(t." + memberInfo.getMethod + "()));\n");
                    builder.append("        }                                                                                                            \n");
                } else if ("short".equals(type_str) || "int".equals(type_str)
                        || "long".equals(type_str) || "float".equals(type_str)
                        || "double".equals(type_str)) {  // short, int, long, float, double基本类型
                    builder.append("        values.put(\"" + memberInfo.columnName + "\", t." + memberInfo.getMethod + "());                             \n");
                } else if ("java.lang.Short".equals(type_str)
                        || "java.lang.Integer".equals(type_str)
                        || "java.lang.Long".equals(type_str)
                        || "java.lang.Float".equals(type_str)
                        || "java.lang.Double".equals(type_str)) { // Short, Integer, Long, Float, Double包装类型
                    builder.append("        if (null != t." + memberInfo.getMethod + "()) {                                                              \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", t." + memberInfo.getMethod + "());                         \n");
                    builder.append("        }                                                                                                            \n");
                } else if ("java.lang.String".equals(type_str)) { // String类型
                    builder.append("        if (!TextUtils.isEmpty(t." + memberInfo.getMethod + "())) {                                                  \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", t." + memberInfo.getMethod + "());                         \n");
                    builder.append("        }                                                                                                            \n");
                } else if ("char".equals(type_str)) {  // char基本类型
                    builder.append("        values.put(\"" + memberInfo.columnName + "\", CommonUtils.char2int(t." + memberInfo.getMethod + "()));       \n");
                } else if ("java.lang.Character".equals(type_str)) { // Character包装类型
                    builder.append("        if (null != t." + memberInfo.getMethod + "()) {                                                              \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", CommonUtils.char2int(t." + memberInfo.getMethod + "()));   \n");
                    builder.append("        }                                                                                                            \n");
                } else if ("byte[]".equals(type_str)) {
                    builder.append("        if (null != t." + memberInfo.getMethod + "() && t." + memberInfo.getMethod + "().length > 0) {               \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", t." + memberInfo.getMethod + "());                         \n");
                    builder.append("        }                                                                                                            \n");
                } else if ("java.util.Date".equals(type_str)) { // 日期类型
                    String pattern = (memberInfo.pattern == null || memberInfo.pattern == "") ? "yyyy-MM-dd" : memberInfo.pattern;
                    builder.append("        if (null != t." + memberInfo.getMethod + "()) {                                                              \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", CommonUtils.date2string(t." + memberInfo.getMethod + "(), \"" + pattern + "\"));\n");
                    builder.append("        }                                                                                                            \n");
                }
            }
            builder.append("        return db.insert(\"" + clazzInfo.tableName + "\", null, values);                                                    \n");
            builder.append("    }                                                                                                                        \n");
            // insertAll
            builder.append("                                                                                                                             \n");
            builder.append("    @Override                                                                                                                \n");
            builder.append("    public long insertAll(List<" + clazzInfo.clazzName + "> list) {                                                          \n");
            builder.append("        int i = 0;                                                                                                           \n");
            builder.append("        db.beginTransaction();                                                                                               \n");
            builder.append("        try {                                                                                                                \n");
            builder.append("            for (" + clazzInfo.clazzName + " item : list) {                                                                  \n");
            builder.append("                long insert = insert(item);                                                                                  \n");
            builder.append("                item.id = insert;                                                                                            \n");
            builder.append("                i++;                                                                                                         \n");
            builder.append("            }                                                                                                                \n");
            builder.append("            db.setTransactionSuccessful();                                                                                   \n");
            builder.append("            return i;                                                                                                        \n");
            builder.append("        } catch (Exception e) {                                                                                              \n");
            builder.append("            e.printStackTrace();                                                                                             \n");
            builder.append("        } finally {                                                                                                          \n");
            builder.append("            db.endTransaction();                                                                                             \n");
            builder.append("        }                                                                                                                    \n");
            builder.append("        return 0;                                                                                                            \n");
            builder.append("    }                                                                                                                        \n");
            // delete
            builder.append("                                                                                                                             \n");
            builder.append("    @Override                                                                                                                \n");
            builder.append("    public int delete(Serializable id) {                                                                                     \n");
            builder.append("        if (null == id)                                                                                                      \n");
            builder.append("            return -1;                                                                                                       \n");
            builder.append("        return db.delete(\"" + clazzInfo.tableName + "\", \" id = ? \", new String[] { id.toString() });                     \n");
            builder.append("    }                                                                                                                        \n");
            // deleteByCondition
            builder.append("                                                                                                                             \n");
            builder.append("    @Override                                                                                                                \n");
            builder.append("    public int deleteByCondition(String selection, String[] selectionArgs) {                                                 \n");
            builder.append("        return db.delete(\"" + clazzInfo.tableName + "\", selection, selectionArgs);                                         \n");
            builder.append("    }                                                                                                                        \n");
            // update
            builder.append("                                                                                                                             \n");
            builder.append("    @Override                                                                                                                \n");
            builder.append("    public int update(" + clazzInfo.clazzName + " t) {                                                                       \n");
            builder.append("        if (null == t)                                                                                                       \n");
            builder.append("            return -1;                                                                                                       \n");
            builder.append("        ContentValues values = new ContentValues();                                                                          \n");
            strings = clazzInfo.map.keySet();
            for (String item : strings) {
                MemberInfo memberInfo = clazzInfo.map.get(item);
                String type_str = memberInfo.type.toString();
                if ("byte".equals(type_str)) {  // byte基本类型
                    builder.append("        values.put(\"" + memberInfo.columnName + "\", CommonUtils.byte2Int(t." + memberInfo.getMethod + "()));       \n");
                } else if ("java.lang.Byte".equals(type_str)) { // Byte包装类型
                    builder.append("        if (null != t." + memberInfo.getMethod + "()) {                                                              \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", CommonUtils.byte2Int(t." + memberInfo.getMethod + "()));   \n");
                    builder.append("        }                                                                                                            \n");
                } else if ("boolean".equals(type_str)) {  // boolean基本类型
                    builder.append("        values.put(\"" + memberInfo.columnName + "\", CommonUtils.boolean2int(t." + memberInfo.getMethod + "()));    \n");
                } else if ("java.lang.Boolean".equals(type_str)) { // Boolean包装类型
                    builder.append("        if (null != t." + memberInfo.getMethod + "()) {                                                              \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", CommonUtils.boolean2int(t." + memberInfo.getMethod + "()));\n");
                    builder.append("        }                                                                                                            \n");
                } else if ("short".equals(type_str) || "int".equals(type_str)
                        || "long".equals(type_str) || "float".equals(type_str)
                        || "double".equals(type_str)) {  // short, int, long, float, double基本类型
                    builder.append("        values.put(\"" + memberInfo.columnName + "\", t." + memberInfo.getMethod + "());                             \n");
                } else if ("java.lang.Short".equals(type_str)
                        || "java.lang.Integer".equals(type_str)
                        || "java.lang.Long".equals(type_str)
                        || "java.lang.Float".equals(type_str)
                        || "java.lang.Double".equals(type_str)) { // Short, Integer, Long, Float, Double包装类型
                    builder.append("        if (null != t." + memberInfo.getMethod + "()) {                                                              \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", t." + memberInfo.getMethod + "());                         \n");
                    builder.append("        }                                                                                                            \n");
                } else if ("java.lang.String".equals(type_str)) { // String类型
                    builder.append("        if (!TextUtils.isEmpty(t." + memberInfo.getMethod + "())) {                                                  \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", t." + memberInfo.getMethod + "());                         \n");
                    builder.append("        }                                                                                                            \n");
                } else if ("char".equals(type_str)) {  // char基本类型
                    builder.append("        values.put(\"" + memberInfo.columnName + "\", CommonUtils.char2int(t." + memberInfo.getMethod + "()));       \n");
                } else if ("java.lang.Character".equals(type_str)) { // Character包装类型
                    builder.append("        if (null != t." + memberInfo.getMethod + "()) {                                                              \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", CommonUtils.char2int(t." + memberInfo.getMethod + "()));   \n");
                    builder.append("        }                                                                                                            \n");
                } else if ("byte[]".equals(type_str)) {
                    builder.append("        if (null != t." + memberInfo.getMethod + "() && t." + memberInfo.getMethod + "().length > 0) {               \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", t." + memberInfo.getMethod + "());                         \n");
                    builder.append("        }                                                                                                            \n");
                } else if ("java.util.Date".equals(type_str)) { // 日期类型
                    String pattern = (memberInfo.pattern == null || memberInfo.pattern == "") ? "yyyy-MM-dd" : memberInfo.pattern;
                    builder.append("        if (null != t." + memberInfo.getMethod + "()) {                                                              \n");
                    builder.append("            values.put(\"" + memberInfo.columnName + "\", CommonUtils.date2string(t." + memberInfo.getMethod + "(), \"" + pattern + "\"));\n");
                    builder.append("        }                                                                                                            \n");
                }
            }
            builder.append("         return db.update(\"" + clazzInfo.tableName + "\", values, \" id = ? \",                                             \n");
            builder.append("                new String[] { t.id + \"\" });                                                                               \n");
            builder.append("    }                                                                                                                        \n");
            builder.append("                                                                                                                             \n");
            // findAll
            builder.append("    @Override                                                                                                                \n");
            builder.append("    public List<" + clazzInfo.clazzName + "> findAll() {                                                                     \n");
            builder.append("        return findByCondition(null, null, null, null, null);                                                                \n");
            builder.append("    }                                                                                                                        \n");
            builder.append("                                                                                                                             \n");
            // findByCondition 3
            builder.append("    @Override                                                                                                                \n");
            builder.append("    public List<" + clazzInfo.clazzName + "> findByCondition(String selection, String[] selectionArgs, String orderBy) {     \n");
            builder.append("        return findByCondition(selection, selectionArgs, null, null, orderBy);                                               \n");
            builder.append("    }                                                                                                                        \n");
            builder.append("                                                                                                                             \n");
            // findByCondition 5
            builder.append("    @Override                                                                                                                \n");
            builder.append("    public List<" + clazzInfo.clazzName + "> findByCondition(String selection, String[] selectionArgs, String groupBy, String having,          \n");
            builder.append("            String orderBy) {                                                                                         \n");
            builder.append("        List<" + clazzInfo.clazzName + "> result = new ArrayList<" + clazzInfo.clazzName + ">();                                                                    \n");
            builder.append("        Cursor cursor = db.query(\"" + clazzInfo.tableName + "\", null, selection, selectionArgs, groupBy, having, \n");
            builder.append("                orderBy);                                                                                             \n");
            builder.append("        if (cursor != null) {                                                                                         \n");
            builder.append("            while (cursor.moveToNext()) {                                                                             \n");
            builder.append("                " + clazzInfo.clazzName + " item = new " + clazzInfo.clazzName + "();                                                                               \n");
            builder.append("                item.id = cursor.getLong(cursor.getColumnIndex(\"id\"));                                              \n");
            strings = clazzInfo.map.keySet();
            for (String item : strings) {
                MemberInfo memberInfo = clazzInfo.map.get(item);
                String type_str = memberInfo.type.toString();
                if ("byte".equals(type_str)) {  // byte基本类型
                    builder.append("                item." + memberInfo.setMethod + "(CommonUtils.int2Byte(cursor.getInt(cursor.getColumnIndex(\"" + memberInfo.columnName + "\"))));                     \n");
                } else if ("java.lang.Byte".equals(type_str)) { // Byte包装类型
                    builder.append("                item." + memberInfo.setMethod + "(CommonUtils.int2Byte(cursor.getInt(cursor.getColumnIndex(\"" + memberInfo.columnName + "\"))));                     \n");
                } else if ("boolean".equals(type_str)) {  // boolean基本类型
                    builder.append("                item." + memberInfo.setMethod + "(CommonUtils.int2boolean(cursor.getInt(cursor.getColumnIndex(\"" + memberInfo.columnName + "\"))));                     \n");
                } else if ("java.lang.Boolean".equals(type_str)) { // Boolean包装类型
                    builder.append("                item." + memberInfo.setMethod + "(CommonUtils.int2boolean(cursor.getInt(cursor.getColumnIndex(\"" + memberInfo.columnName + "\"))));                     \n");
                } else if ("char".equals(type_str)) {  // char基本类型
                    builder.append("                item." + memberInfo.setMethod + "(CommonUtils.int2char(cursor.getInt(cursor.getColumnIndex(\"" + memberInfo.columnName + "\"))));       \n");
                } else if ("java.lang.Character".equals(type_str)) { // Character包装类型
                    builder.append("                item." + memberInfo.setMethod + "(CommonUtils.int2char(cursor.getInt(cursor.getColumnIndex(\"" + memberInfo.columnName + "\"))));       \n");
                } else if ("short".equals(type_str)) { // short基本类型
                    builder.append("                item." + memberInfo.setMethod + "(cursor.getShort(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")));       \n");
                } else if ("java.lang.Short".equals(type_str)) { // Short包装类型
                    builder.append("                item." + memberInfo.setMethod + "(cursor.getShort(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")));       \n");
                } else if ("int".equals(type_str)) { // int基本类型
                    builder.append("                item." + memberInfo.setMethod + "(cursor.getInt(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")));       \n");
                } else if ("java.lang.Integer".equals(type_str)) { // Integer包装类型
                    builder.append("                item." + memberInfo.setMethod + "(cursor.getInt(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")));       \n");
                } else if ("long".equals(type_str)) { // long基本类型
                    builder.append("                item." + memberInfo.setMethod + "(cursor.getLong(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")));       \n");
                } else if ("java.lang.Long".equals(type_str)) { // Long包装类型
                    builder.append("                item." + memberInfo.setMethod + "(cursor.getLong(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")));       \n");
                } else if ("float".equals(type_str)) { // float基本类型
                    builder.append("                item." + memberInfo.setMethod + "(cursor.getFloat(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")));       \n");
                } else if ("java.lang.Float".equals(type_str)) { // Float包装类型
                    builder.append("                item." + memberInfo.setMethod + "(cursor.getFloat(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")));       \n");
                } else if ("double".equals(type_str)) { // double基本类型
                    builder.append("                item." + memberInfo.setMethod + "(cursor.getDouble(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")));       \n");
                } else if ("java.lang.Double".equals(type_str)) { // Double包装类型
                    builder.append("                item." + memberInfo.setMethod + "(cursor.getDouble(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")));       \n");
                } else if ("java.lang.String".equals(type_str)) { // String类型
                    builder.append("                item." + memberInfo.setMethod + "(cursor.getString(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")));       \n");
                } else if ("byte[]".equals(type_str)) { // byte[]数组类型
                    builder.append("                item." + memberInfo.setMethod + "(cursor.getBlob(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")));       \n");
                } else if ("java.util.Date".equals(type_str)) { // 日期类型
                    String pattern = (memberInfo.pattern == null || memberInfo.pattern == "") ? "yyyy-MM-dd" : memberInfo.pattern;
                    builder.append("                item." + memberInfo.setMethod + "(CommonUtils.string2date(cursor.getString(cursor.getColumnIndex(\"" + memberInfo.columnName + "\")), \"" + pattern + "\"));\n");
                }
            }
            builder.append("                result.add(item);                                                                                     \n");
            builder.append("            }                                                                                                         \n");
            builder.append("            cursor.close();                                                                                           \n");
            builder.append("        }                                                                                                             \n");
            builder.append("        return result;                                                                                                \n");
            builder.append("    }                                                                                                                 \n");

            builder.append("}                                                                                                                            \n");

            String value = builder.toString();
            JavaFileObject jfo = filer.createSourceFile(basePackageName + "." + clazzInfo.clazzName + PROXY);
            Writer writer = jfo.openWriter();
            writer.write(value);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateDaoImplFactory() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("package " + basePackageName + ";                                                   \n");
            builder.append("                                                                                   \n");
            builder.append("import java.util.HashMap;                                                          \n");
            builder.append("import java.util.Map;                                                              \n");
            builder.append("                                                                                   \n");
            builder.append("import android.database.sqlite.SQLiteDatabase;                                     \n");
            builder.append("import android.database.sqlite.SQLiteOpenHelper;                                   \n");
            builder.append("                                                                                   \n");
            builder.append("import com.chenlong.base.BaseBean;                                                 \n");
            builder.append("import com.chenlong.base.DaoImpl;                                                  \n");
            builder.append("                                                                                   \n");
            builder.append("public class DaoImplFactory {                                                      \n");
            builder.append("    private static Map<String, DaoImpl<?>> map = new HashMap<String, DaoImpl<?>>();\n");
            builder.append("    public static SQLiteDatabase db = null;                                        \n");
            builder.append("                                                                                   \n");
            builder.append("    public static void init(SQLiteOpenHelper helper) {                             \n");
            builder.append("        if (helper == null)                                                        \n");
            builder.append("            return;                                                                \n");
            builder.append("        db = helper.getWritableDatabase();                                         \n");
            builder.append("    }                                                                              \n");
            builder.append("                                                                                   \n");
            builder.append("    public static SQLiteDatabase getDataBase() {                                   \n");
            builder.append("        return db;                                                                 \n");
            builder.append("    }                                                                              \n");
            builder.append("                                                                                   \n");
            builder.append("    @SuppressWarnings(\"unchecked\")                                               \n");
            builder.append("    public static <T extends BaseBean> DaoImpl<T> getDaoImpl(Class<T> clazz) {     \n");
            builder.append("        DaoImpl<T> daoImpl = (DaoImpl<T>) map.get(clazz.getCanonicalName());       \n");
            builder.append("        if (null == daoImpl) {                                                     \n");
            builder.append("            String name = clazz.getCanonicalName() + \"" + PROXY + "\";            \n");
            builder.append("            try {                                                                  \n");
            builder.append("                Class<?> forName = Class.forName(name);                            \n");
            builder.append("                DaoImpl<T> newInstance = (DaoImpl<T>) forName.newInstance();       \n");
            builder.append("                map.put(clazz.getCanonicalName(), newInstance);                    \n");
            builder.append("                return newInstance;                                                \n");
            builder.append("            } catch (Exception e) {                                                \n");
            builder.append("                e.printStackTrace();                                               \n");
            builder.append("            }                                                                      \n");
            builder.append("        }                                                                          \n");
            builder.append("        return daoImpl;                                                            \n");
            builder.append("    }                                                                              \n");
            builder.append("}                                                                                  \n");
            String value = builder.toString();
            JavaFileObject jfo = filer.createSourceFile(basePackageName + ".DaoImplFactory");
            Writer writer = jfo.openWriter();
            writer.write(value);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateAbastactOpenHelper(DBConfiguration configuration) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("package " + basePackageName + ";\n");
            builder.append("\n");
            builder.append("import android.content.Context;\n");
            builder.append("import android.database.sqlite.SQLiteDatabase;\n");
            builder.append("import android.database.sqlite.SQLiteOpenHelper;\n");
            builder.append("\n");
            builder.append("public abstract class AbstractOpenHelper extends SQLiteOpenHelper {\n");
            builder.append("    public static final String TABLE_ID = \"id\";\n");
            builder.append("\n");
            builder.append("    public AbstractOpenHelper(Context context) {\n");
            builder.append("        super(context, \"" + configuration.name() + "\", null, " + configuration.currentVersion() + ");\n");
            builder.append("    }\n");
            builder.append("\n");
            builder.append("    /**\n");
            builder.append("     * 数据库第一次初始化\n");
            builder.append("     */\n");
            builder.append("    public abstract void init(SQLiteDatabase db);\n");
            // 数据库有版本更新
            if (configuration.oldVersionSequence().length > 0) {
                for (int item : configuration.oldVersionSequence()) {
                    builder.append("\n");
                    builder.append("    /**\n");
                    builder.append("     * 旧版本为" + item + "需要修改的内容\n");
                    builder.append("     */\n");
                    builder.append("    public abstract void old_version_" + item + "_upgrade(SQLiteDatabase db);\n");
                }
            }
            builder.append("\n");
            builder.append("    @Override\n");
            builder.append("    public void onCreate(SQLiteDatabase db) {\n");
            builder.append("        init(db);\n");
            // 当前版本和开始版本不一致，需要更新数据库
            if (configuration.startVersion() != configuration.currentVersion()) {
                builder.append("        onUpgrade(db, " + configuration.startVersion() + ", " + configuration.currentVersion() + ");\n");
            }
            builder.append("    }\n");
            // 当前版本和开始版本不一致，需要添加更新的逻辑
            if (configuration.startVersion() != configuration.currentVersion()) {
                builder.append("\n");
                builder.append("    @Override\n");
                builder.append("    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {\n");
                builder.append("        switch (oldVersion) {\n");
                // 数据库有版本更新
                if (configuration.oldVersionSequence().length > 0) {
                    for (int item : configuration.oldVersionSequence()) {
                        builder.append("        case " + item + ":\n");
                        builder.append("            old_version_" + item + "_upgrade(db);\n");
                    }
                    builder.append("        default:\n");
                    builder.append("            break;\n");
                    builder.append("        }\n");
                    builder.append("    }\n");
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "请在DBConfiguration.oldVersionSequence添加旧版本序列");
                }
            }
            builder.append("}\n");
            String value = builder.toString();
            JavaFileObject jfo = filer.createSourceFile(basePackageName + ".AbstractOpenHelper");
            Writer writer = jfo.openWriter();
            writer.write(value);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
