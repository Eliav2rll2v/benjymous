package com.baomidou.plugin.idea.mybatisx.smartjpa.operate.manager;


import com.baomidou.plugin.idea.mybatisx.smartjpa.common.SyntaxAppender;
import com.baomidou.plugin.idea.mybatisx.smartjpa.common.SyntaxAppenderFactory;
import com.baomidou.plugin.idea.mybatisx.smartjpa.operate.model.AppendTypeEnum;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 符号追加器 工厂管理器
 */
public class StatementBlockFactory {

    private final List<StatementBlock> blockList = new ArrayList<>();


    public StatementBlockFactory() {
    }


    @NotNull
    public LinkedList<SyntaxAppender> splitAppenderByText(String splitParam) {

        List<SyntaxAppenderFactory> syntaxAppenderFactoryList = this.blockList.stream()
            .flatMap(x -> {
                return Arrays.stream(new SyntaxAppenderFactory[]{x.getResultAppenderFactory(),
                    x.getConditionAppenderFactory(),
                    x.getSortAppenderFactory()});
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        SyntaxSplitHelper syntaxSplitHelper = new SyntaxSplitHelper(syntaxAppenderFactoryList);
        return syntaxSplitHelper.splitAppenderByText(splitParam);

    }

    public void registerStatementBlock(final StatementBlock statementBlock) {
        this.blockList.add(statementBlock);

        appenderFactoryMap.put(statementBlock.getTagName(), statementBlock);

    }

    private Map<String, StatementBlock> appenderFactoryMap = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(StatementBlockFactory.class);

    public SyntaxAppenderFactory findAppenderFactoryByJpa(LinkedList<SyntaxAppender> jpaList) {
        SyntaxAppender peek = jpaList.peek();
        String operatorName = peek.getText();
        StatementBlock statementBlock = appenderFactoryMap.get(operatorName);
        if (statementBlock != null) {
            return statementBlock.getAppenderFactoryByJpa(jpaList);
        }
        return null;
    }

    public StatementBlock findStatementBlockByJpa(LinkedList<SyntaxAppender> jpaList) {
        String operatorName = jpaList.peek().getText();
        return appenderFactoryMap.get(operatorName);
    }

    public List<SyntaxAppenderFactory> findAreaListByJpa(LinkedList<SyntaxAppender> jpaList) {
        List<SyntaxAppenderFactory> appenderFactories = new ArrayList<>();
        SyntaxAppender peek = jpaList.peek();
        StatementBlock statementBlock = appenderFactoryMap.get(peek.getText());
        for (SyntaxAppender syntaxAppender : jpaList) {
            if (syntaxAppender.getType() == AppendTypeEnum.AREA) {
                SyntaxAppenderFactory appenderFactoryByJpa = statementBlock.getSyntaxAppenderFactoryByStr(syntaxAppender.getText());
                appenderFactories.add(appenderFactoryByJpa);
            }
        }
        return appenderFactories;
    }

    public Collection<StatementBlock> getAllBlock() {
        return appenderFactoryMap.values();
    }
}
