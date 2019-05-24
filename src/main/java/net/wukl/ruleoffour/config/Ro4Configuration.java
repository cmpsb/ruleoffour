package net.wukl.ruleoffour.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Plugin configuration.
 */
@State(
        name = "Ro4Configuration",
        storages = { @Storage("ruleoffour.xml") }
)
public class Ro4Configuration implements PersistentStateComponent<Ro4Configuration> {
    /**
     * Returns an instance of the configuration.
     *
     * @param project the project the configuration belongs to
     *
     * @return the instance
     */
    public static Ro4Configuration getInstance(final @NotNull Project project) {
        return ServiceManager.getService(project, Ro4Configuration.class);
    }

    /**
     * Iff {@code true}, generate javadoc above the constructors.
     */
    @Property
    private boolean javadoc = true;

    /**
     * Iff {@code true}, generate the exact class name instead of the "human" form.
     *
     * The "human" form is the exact name, lowercase, split by spaces.
     */
    @Property
    private boolean exactNameInDoc = false;

    /**
     * Iff {@code true}, generate an empty super() call in the no-arg constructor.
     */
    @Property
    private boolean emptySuper = true;

    /**
     * Iff {@code true}, marks all parameters as {@code final}.
     */
    @Property
    private boolean finalParams = true;

    /**
     * Iff {@code true}, marks all parameters as {@code @Nullable}
     */
    @Property
    private boolean nullableParams = false;

    /**
     * Iff {@code true}, changes the cause type from {@code Throwable} to {@code Exception} to
     * discourage catching raw throwables.
     */
    @Property
    private boolean exceptionAsCause = false;

    /**
     * @return a component state. All properties, public and annotated fields are serialized. Only
     * values, which differ
     * from the default (i.e., the value of newly instantiated class) are serialized. {@code null}
     * value indicates
     * that the returned state won't be stored, as a result previously stored state will be used.
     *
     * @see XmlSerializer
     */
    @Nullable
    @Override
    public Ro4Configuration getState() {
        return this;
    }

    /**
     * This method is called when new component state is loaded. The method can and will be called
     * several times, if
     * config files were externally changed while IDE was running.
     * <p>
     * State object should be used directly, defensive copying is not required.
     *
     * @param state loaded component state
     *
     * @see XmlSerializerUtil#copyBean(Object, Object)
     */
    @Override
    public void loadState(@NotNull final Ro4Configuration state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public boolean isJavadocEnabled() {
        return javadoc;
    }

    public void setJavadocEnabled(final boolean javadoc) {
        this.javadoc = javadoc;
    }

    public boolean isExactNameInDocEnabled() {
        return exactNameInDoc;
    }

    public void setExactNameInDocEnabled(final boolean exactNameInDoc) {
        this.exactNameInDoc = exactNameInDoc;
    }

    public boolean isEmptySuperEnabled() {
        return emptySuper;
    }

    public void setEmptySuperEnabled(final boolean emptySuper) {
        this.emptySuper = emptySuper;
    }

    public boolean isFinalParamsEnabled() {
        return finalParams;
    }

    public void setFinalParamsEnabled(final boolean finalParams) {
        this.finalParams = finalParams;
    }

    public boolean isNullableParamsEnabled() {
        return nullableParams;
    }

    public void setNullableParamsEnabled(final boolean nullableParams) {
        this.nullableParams = nullableParams;
    }

    public boolean isExceptionAsCauseEnabled() {
        return exceptionAsCause;
    }

    public void setExceptionAsCauseEnabled(final boolean exceptionAsCause) {
        this.exceptionAsCause = exceptionAsCause;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (!(obj instanceof Ro4Configuration)) {
            return false;
        }

        final Ro4Configuration other = (Ro4Configuration) obj;

        return this.javadoc == other.javadoc
                && this.exactNameInDoc == other.exactNameInDoc
                && this.emptySuper == other.emptySuper
                && this.finalParams == other.finalParams
                && this.nullableParams == other.nullableParams
                && this.exceptionAsCause == other.exceptionAsCause;
    }

    @Override
    public final int hashCode() {
        return (this.javadoc ? 1 : 0)
                + (this.exactNameInDoc ? (1 << 1) : 0)
                + (this.emptySuper ? (1 << 2) : 0)
                + (this.finalParams ? (1 << 3) : 0)
                + (this.nullableParams ? (1 << 4) : 0)
                + (this.exceptionAsCause ? (1 << 5) : 0);
    }
}
