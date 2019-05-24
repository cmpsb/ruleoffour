package net.wukl.ruleoffour.config;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConfigurationUi {
    private JCheckBox finalParams;
    private JCheckBox nullableParams;
    private JCheckBox exceptionAsCause;
    private JCheckBox javadoc;
    private JCheckBox emptySuper;
    private JCheckBox exactNameInDoc;
    private JPanel root;

    public JPanel getRoot() {
        return this.root;
    }

    public Ro4Configuration extractConfig() {
        final Ro4Configuration config = new Ro4Configuration();

        config.setJavadocEnabled(this.javadoc.isSelected());
        config.setExactNameInDocEnabled(this.exactNameInDoc.isSelected());
        config.setEmptySuperEnabled(this.emptySuper.isSelected());
        config.setFinalParamsEnabled(this.finalParams.isSelected());
        config.setNullableParamsEnabled(this.nullableParams.isSelected());
        config.setExceptionAsCauseEnabled(this.exceptionAsCause.isSelected());

        return config;
    }

    public void loadState(final @NotNull Ro4Configuration config) {
        this.javadoc.setSelected(config.isJavadocEnabled());
        this.exactNameInDoc.setSelected(config.isExceptionAsCauseEnabled());
        this.emptySuper.setSelected(config.isEmptySuperEnabled());
        this.finalParams.setSelected(config.isFinalParamsEnabled());
        this.nullableParams.setSelected(config.isNullableParamsEnabled());
        this.exceptionAsCause.setSelected(config.isExceptionAsCauseEnabled());
    }
}
