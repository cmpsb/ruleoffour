package net.wukl.ruleoffour;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import net.wukl.ruleoffour.config.Ro4Configuration;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adds an intent that can generate the four standard constructors of an exception.
 */
public class CreateConstructorsIntention extends PsiElementBaseIntentionAction
        implements IntentionAction {
    @NotNull
    @Override
    public String getText() {
        return "Generate exception constructors";
    }

    /**
     * Invokes intention action for the element under caret.
     *
     * @param project the project in which the file is opened.
     * @param editor  the editor for the file.
     * @param element the element under cursor.
     */
    @Override
    public void invoke(@NotNull final Project project, final Editor editor,
                       @NotNull final PsiElement element) throws IncorrectOperationException {
        final PsiClass type = Utils.getDeclaredClass(element);
        if (type == null) {
            throw new IncorrectOperationException("Class does not qualify");
        }

        final Ro4Configuration config = ServiceManager.getService(project, Ro4Configuration.class);

        final String className = element.getText();
        final String docName = config.isExactNameInDocEnabled()
                ? " " + className
                : this.humanizeName(className);

        final PsiMethod[] existingMethods = type.getMethods();
        final PsiMethod firstExistingMethod;
        if (existingMethods.length > 0) {
            firstExistingMethod = existingMethods[0];
        } else {
            firstExistingMethod = null;
        }

        final PsiElementFactory fact = JavaPsiFacade.getElementFactory(project);

        final StringBuilder msgParBuilder = new StringBuilder();
        final StringBuilder causeParBuilder = new StringBuilder();

        if (config.isFinalParamsEnabled()) {
            msgParBuilder.append("final ");
            causeParBuilder.append("final ");
        }

        if (config.isNullableParamsEnabled()) {
            msgParBuilder.append("@Nullable ");
            causeParBuilder.append("@Nullable ");
        }

        msgParBuilder.append("String message");
        causeParBuilder.append(config.isExceptionAsCauseEnabled() ? "Exception" : "Throwable");
        causeParBuilder.append(" cause");

        final PsiParameter msgPar = fact.createParameterFromText(msgParBuilder.toString(), null);
        final PsiParameter causePar = fact.createParameterFromText(causeParBuilder.toString(), null);

        final CodeStyleManager stylist = CodeStyleManager.getInstance(project);

        for (int i = 0; i < 4; ++i) {
            final PsiElement ctor = this.genConstructor(
                    fact,
                    className,
                    docName,
                    (i & 1) == 1 ? msgPar : null,
                    (i & 2) == 2 ? causePar : null,
                    config
            );

            type.addBefore(stylist.reformat(ctor), firstExistingMethod);
        }

        if (config.isNullableParamsEnabled()) {
            final JavaCodeStyleManager javaStylist = JavaCodeStyleManager.getInstance(project);
            javaStylist.optimizeImports(element.getContainingFile());
        }
    }

    /**
     * Generates an exception constnuctor.
     *
     * @param fact the PSI element factory to build new elements with
     * @param className the name of the class to generate constructors for
     * @param docName the name to use in the documentation
     * @param msgPar the element to add as a message parameter, if not {@code null}
     * @param causePar the element to add as a cause parameter, if not {@code null}
     * @param config the plugin configuration instance to get settings from
     *
     * @return the constructor element
     */
    private PsiElement genConstructor(
            final @NotNull PsiElementFactory fact,
            final @NotNull String className,
            final @NotNull String docName,
            final @Nullable PsiParameter msgPar,
            final @Nullable PsiParameter causePar,
            final @NotNull Ro4Configuration config
    ) {
        final PsiMethod ctor = fact.createConstructor(className);

        final StringBuilder superBuilder = new StringBuilder("super(");
        final StringBuilder commentBuilder = new StringBuilder(
                "/**\n* Creates a new" + docName + ".\n"
        );

        final boolean isFirst = msgPar != null || causePar != null;

        if (isFirst) {
            commentBuilder.append("* \n");
        }

        if (msgPar != null) {
            superBuilder.append("message");
            commentBuilder.append(
                    "* @param message the message explaining what caused the exception\n"
            );
            ctor.getParameterList().add(msgPar.copy());
        }

        if (msgPar != null && causePar != null) {
            superBuilder.append(", ");
        }

        if (causePar != null) {
            superBuilder.append("cause");
            commentBuilder.append("* @param cause the exception that caused this exception\n");
            ctor.getParameterList().add(causePar.copy());
        }

        superBuilder.append(");");
        commentBuilder.append("*/");

        PsiElement body = ctor.getBody();
        assert body != null : "Generated constructor has no body";

        if (!isFirst || config.isEmptySuperEnabled()) {
            body.add(fact.createStatementFromText(superBuilder.toString(), null));
        }

        if (config.isJavadocEnabled()) {
            final String doc = commentBuilder.toString();
            ctor.addBefore(fact.createDocCommentFromText(doc), ctor.getFirstChild());
        }

        return ctor;
    }

    /**
     * Checks whether this intention is available at a caret offset in file. If this method returns
     * true, a light bulb for this intention is shown.
     *
     * @param project the project in which the availability is checked.
     * @param editor  the editor in which the intention will be invoked.
     * @param element the element under caret.
     *
     * @return true if the intention is available, false otherwise.
     */
    @Override
    public boolean isAvailable(
            final @Nullable Project project,
            final @Nullable Editor editor,
            final @NotNull PsiElement element
    ) {
        return Utils.isApplicable(element);
    }

    /**
     * Converts a class name to "human" form.
     *
     * "Human" form means that the class name is split at the capitals and converted to lower case.
     *
     * @param name the name to convert
     *
     * @return the humanized name
     */
    @NotNull
    private String humanizeName(final @NotNull String name) {
        final String splitName = name.replaceAll("([A-Z])", " $1");
        return splitName.toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startInWriteAction() {
        return true;
    }

    /**
     * Returns the name of the family of intentions. It is used to externalize "auto-show" state of
     * intentions. When user clicks on a lightbulb in intention list, all intentions with the same
     * family name get enabled/disabled. The name is also shown in settings tree.
     *
     * @return the intention family name.
     */
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }
}
