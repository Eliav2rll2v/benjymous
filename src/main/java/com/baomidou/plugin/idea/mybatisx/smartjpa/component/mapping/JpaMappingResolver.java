package com.baomidou.plugin.idea.mybatisx.smartjpa.component.mapping;

import com.baomidou.plugin.idea.mybatisx.smartjpa.common.appender.JdbcTypeUtils;
import com.baomidou.plugin.idea.mybatisx.smartjpa.component.TxField;
import com.baomidou.plugin.idea.mybatisx.smartjpa.util.FieldUtil;
import com.baomidou.plugin.idea.mybatisx.util.StringUtils;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiConstantEvaluationHelper;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.PsiReferenceParameterList;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Jpa mapping resolver.
 */
public abstract class JpaMappingResolver {


    /**
     * The constant JAVAX_PERSISTENCE_TABLE.
     */
    public static final String JAVAX_PERSISTENCE_TABLE = "javax.persistence.Table";
    /**
     * The constant JAVAX_PERSISTENCE_COLUMN.
     */
    public static final String JAVAX_PERSISTENCE_COLUMN = "javax.persistence.Column";
    /**
     * The constant COLUMN_NAME.
     */
    public static final String COLUMN_NAME = "name";
    /**
     * The constant TABLE_NAME.
     */
    public static final String TABLE_NAME = "name";
    public static final String ID_ANNOTATION = "javax.persistence.Id";

    /**
     * Find entity class by mapper class optional.
     *
     * @param mapperClass the mapper class
     * @return the optional
     */
    public static Optional<PsiClass> findEntityClassByMapperClass(PsiClass mapperClass) {
        JavaPsiFacade instance = JavaPsiFacade.getInstance(mapperClass.getProject());
        PsiReferenceList extendsList = mapperClass.getExtendsList();
        if (extendsList != null) {
            @NotNull PsiJavaCodeReferenceElement[] referenceElements = extendsList.getReferenceElements();
            if (referenceElements.length > 0) {
                for (PsiJavaCodeReferenceElement referenceElement : referenceElements) {
                    PsiReferenceParameterList parameterList = referenceElement.getParameterList();
                    if (parameterList != null) {
                        @NotNull PsiType[] typeArguments = parameterList.getTypeArguments();
                        if (typeArguments != null) {
                            for (PsiType type : typeArguments) {
                                String canonicalText = type.getCanonicalText();
                                // ??????????????????????????????, ??????????????????.  java????????????
                                if (!canonicalText.startsWith("java")
                                    && !StringUtils.isEmpty(canonicalText)) {
                                    PsiClass entityClass = instance.findClass(canonicalText, mapperClass.getResolveScope());
                                    if (entityClass != null) {
                                        PsiAnnotation annotation = entityClass.getAnnotation(JAVAX_PERSISTENCE_TABLE);
                                        if (annotation != null) {
                                            return Optional.of(entityClass);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    protected Optional<String> getTableNameByJpa(PsiClass entityClass) {
        if (entityClass == null) {
            throw new IllegalArgumentException("?????????????????????, ?????????????????????Mapper");
        }
        String tableName = null;
        PsiAnnotation annotation = entityClass.getAnnotation(JAVAX_PERSISTENCE_TABLE);
        if (annotation != null) {
            PsiAnnotationMemberValue originTable = annotation.findAttributeValue(TABLE_NAME);
            PsiLiteralExpression expression = (PsiLiteralExpression) originTable;
            if (expression == null || expression.getValue() == null) {
                return Optional.empty();
            }
            tableName = expression.getValue().toString();
        }
        return Optional.ofNullable(tableName);
    }

    /**
     * Gets column name by jpa or camel.
     *
     * @param field the field
     * @return the column name by jpa or camel
     */
    protected String getColumnNameByJpaOrCamel(PsiField field) {
        String columnName = null;
        // ??????jpa?????????????????????
        PsiAnnotation annotation = field.getAnnotation(JAVAX_PERSISTENCE_COLUMN);
        if (annotation != null) {
            PsiAnnotationMemberValue originFieldAnnotation = annotation.findAttributeValue(COLUMN_NAME);

            PsiConstantEvaluationHelper constantEvaluationHelper = JavaPsiFacade.getInstance(field.getProject()).getConstantEvaluationHelper();
            if (originFieldAnnotation != null) {
                Object value = constantEvaluationHelper.computeConstantExpression(originFieldAnnotation);
                if (value != null) {
                    columnName = value.toString();
                }
            }
        }
        // ??????????????????
        if (columnName == null) {
            columnName = getUnderLineName(field.getName());
        }
        return columnName;
    }

    @NotNull
    private String getUnderLineName(String camelName) {
        String[] strings = org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase(camelName);
        return Arrays.stream(strings).map(StringUtils::lowerCaseFirstChar)
            .collect(Collectors.joining("_"));
    }

    /**
     * Init data by camel list.
     *
     * @param entityClass the entity class
     * @return the list
     */
    protected List<TxField> initDataByCamel(PsiClass entityClass) {
        // ????????? static, transient ???????????????
        List<PsiField> psiFieldList = FieldUtil.getPsiFieldList(entityClass);
        return psiFieldList.stream()
            .filter(this::filterField)
            .map(field -> {
                TxField txField = new TxField();
                txField.setTipName(StringUtils.upperCaseFirstChar(field.getName()));
                txField.setFieldType(field.getType().getCanonicalText());

                String columnName = getColumnNameByJpaOrCamel(field);
                // ?????????????????????
                txField.setFieldName(field.getName());

                // ????????????
                txField.setColumnName(columnName);
                if(field.hasAnnotation(ID_ANNOTATION)){
                    txField.setPrimaryKey(true);
                }

                txField.setClassName(field.getContainingClass().getQualifiedName());
                Optional<String> jdbcTypeByJavaType = JdbcTypeUtils.findJdbcTypeByJavaType(field.getType().getCanonicalText());
                jdbcTypeByJavaType.ifPresent(txField::setJdbcType);
                return txField;
            }).collect(Collectors.toList());
    }

    /**
     * JPA ????????????????????????
     *
     * @param field
     * @return
     */
    protected boolean filterField(PsiField field) {
        return !field.hasModifierProperty(PsiModifier.STATIC);
    }

}
