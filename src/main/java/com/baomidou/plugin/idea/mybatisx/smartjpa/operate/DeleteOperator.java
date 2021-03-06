package com.baomidou.plugin.idea.mybatisx.smartjpa.operate;


import com.baomidou.plugin.idea.mybatisx.setting.config.AbstractStatementGenerator;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.SyntaxAppender;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.appender.AreaSequence;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.appender.CompositeAppender;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.appender.CustomAreaAppender;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.appender.CustomFieldAppender;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.appender.CustomJoinAppender;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.factory.ConditionAppenderFactory;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.factory.ResultAppenderFactory;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.iftest.ConditionFieldWrapper;
import com.baomidou.plugin.idea.mybatisx.smartjpa.component.TxField;
import com.baomidou.plugin.idea.mybatisx.smartjpa.component.TxParameter;
import com.baomidou.plugin.idea.mybatisx.smartjpa.component.TxReturnDescriptor;
import com.baomidou.plugin.idea.mybatisx.smartjpa.operate.generate.Generator;
import com.baomidou.plugin.idea.mybatisx.smartjpa.operate.manager.StatementBlock;
import com.baomidou.plugin.idea.mybatisx.smartjpa.util.SyntaxAppenderWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.LinkedList;
import java.util.List;

/**
 * The type Delete operator.
 */
public class DeleteOperator extends BaseOperatorManager {


    /**
     * Instantiates a new Delete operator.
     *
     * @param mappingField the mapping field
     */
    public DeleteOperator(final List<TxField> mappingField) {
        super.setOperatorNameList(AbstractStatementGenerator.DELETE_GENERATOR.getPatterns());
        this.init(mappingField);
    }


    /**
     * Init.
     *
     * @param mappingField the mapping field
     */
    public void init(final List<TxField> mappingField) {
        for (String areaName : getOperatorNameList()) {
            // ?????????????????????
            final ResultAppenderFactory resultAppenderFactory = new DeleteResultAppenderFactory(areaName);
            ConditionAppenderFactory conditionAppenderFactory = new ConditionAppenderFactory(areaName, mappingField);
            for (TxField field : mappingField) {
                // ???????????? : delete + By + field
                CompositeAppender areaByAppender = new CompositeAppender(
                    CustomAreaAppender.createCustomAreaAppender(areaName, ResultAppenderFactory.RESULT, AreaSequence.AREA, AreaSequence.RESULT, resultAppenderFactory),
                    CustomAreaAppender.createCustomAreaAppender("By", "By", AreaSequence.AREA, AreaSequence.CONDITION, conditionAppenderFactory),
                    new CustomFieldAppender(field, AreaSequence.CONDITION)
                );
                resultAppenderFactory.registerAppender(areaByAppender);

                // ???????????? : delete  By : and + field
                CompositeAppender andAppender = new CompositeAppender(
                    new CustomJoinAppender("And", " AND", AreaSequence.CONDITION),
                    new CustomFieldAppender(field, AreaSequence.CONDITION)
                );
                resultAppenderFactory.registerAppender(andAppender);

                // ???????????? : delete  By : or + field
                CompositeAppender orAppender = new CompositeAppender(
                    new CustomJoinAppender("Or", " OR", AreaSequence.CONDITION),
                    new CustomFieldAppender(field, AreaSequence.CONDITION)
                );
                resultAppenderFactory.registerAppender(orAppender);
            }

            StatementBlock statementBlock = new StatementBlock();
            statementBlock.setTagName(areaName);
            statementBlock.setResultAppenderFactory(resultAppenderFactory);
            statementBlock.setConditionAppenderFactory(conditionAppenderFactory);
            statementBlock.setReturnWrapper(TxReturnDescriptor.createByOrigin(null, "int"));
            this.registerStatementBlock(statementBlock);
        }

    }

    @Override
    public String getTagName() {
        return "delete";
    }

    @Override
    public void generateMapperXml(String id,
                                  LinkedList<SyntaxAppender> jpaList,
                                  PsiClass entityClass,
                                  PsiMethod psiMethod,
                                  String tableName,
                                  Generator mybatisXmlGenerator,
                                  ConditionFieldWrapper conditionFieldWrapper,
                                  List<TxField> resultFields) {
        String mapperXml = super.generateXml(jpaList, entityClass, psiMethod, tableName, conditionFieldWrapper);
        mybatisXmlGenerator.generateDelete(id, mapperXml);
    }

    private class DeleteResultAppenderFactory extends ResultAppenderFactory {

        /**
         * Instantiates a new Delete result appender factory.
         *
         * @param pattern the pattern
         */
        public DeleteResultAppenderFactory(String pattern) {
            super(pattern);
        }

        @Override
        public String getTemplateText(String tableName, PsiClass entityClass,
                                      LinkedList<TxParameter> parameters,
                                      LinkedList<SyntaxAppenderWrapper> collector, ConditionFieldWrapper conditionFieldWrapper) {


            return "delete from " + tableName;
        }
    }
}
