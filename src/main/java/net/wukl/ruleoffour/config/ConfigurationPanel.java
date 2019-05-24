package net.wukl.ruleoffour.config;

import com.intellij.openapi.options.ConfigurableEP;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConfigurationPanel implements SearchableConfigurable {
    private @Nullable ConfigurationUi ui;
    private @NotNull Ro4Configuration config;

    public ConfigurationPanel(final @NotNull Project project) {
        this.config = Ro4Configuration.getInstance(project);
    }

    /**
     * Returns the visible name of the configurable component.
     * Note, that this method must return the display name
     * that is equal to the display name declared in XML
     * to avoid unexpected errors.
     *
     * @return the visible name of the configurable component
     */
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Rule of Four";
    }

    /**
     * Creates new Swing form that enables user to configure the settings.
     * Usually this method is called on the EDT, so it should not take a long time.
     *
     * Also this place is designed to allocate resources (subscriptions/listeners etc.)
     *
     * @return new Swing form to show, or {@code null} if it cannot be created
     *
     * @see #disposeUIResources
     */
    @Nullable
    @Override
    public JComponent createComponent() {
        this.ui = new ConfigurationUi();
        return this.ui.getRoot();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disposeUIResources() {
        this.ui = null;
    }

    /**
     * Indicates whether the Swing form was modified or not.
     * This method is called very often, so it should not take a long time.
     *
     * @return {@code true} if the settings were modified, {@code false} otherwise
     */
    @Override
    public boolean isModified() {
        if (this.ui == null) {
            return false;
        }

        return !this.config.equals(this.ui.extractConfig());
    }

    /**
     * Stores the settings from the Swing form to the configurable component.
     * This method is called on EDT upon user's request.
     *
     * @throws ConfigurationException if values cannot be applied
     */
    @Override
    public void apply() throws ConfigurationException {
        if (this.ui == null) {
            return;
        }

        this.config.loadState(this.ui.extractConfig());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        if (this.ui == null) {
            return;
        }

        this.ui.loadState(this.config);
    }

    /**
     * Unique configurable id.
     * Note this id should be THE SAME as the one specified in XML.
     *
     * @see ConfigurableEP#id
     */
    @NotNull
    @Override
    public String getId() {
        return "preferences.ruleoffour";
    }
}
