package net.wukl.ruleoffour;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

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
        final PsiClass type = this.getDeclaredClass(element);
        if (type == null) {
            throw new IncorrectOperationException("Class does not qualify");
        }

        final String humanName = this.humanizeName(element.getText());

        final PsiMethod[] existingCtors = type.getConstructors();
        final PsiMethod firstExistingCtor;
        if (existingCtors.length > 0) {
            firstExistingCtor = existingCtors[0];
        } else {
            firstExistingCtor = null;
        }

        final PsiElementFactory fact = JavaPsiFacade.getElementFactory(project);
        final PsiParserFacade psi = PsiParserFacade.SERVICE.getInstance(project);

        final PsiParameter msgPar = fact.createParameterFromText("final String message", null);
        final PsiParameter causePar = fact.createParameterFromText("final Throwable cause", null);

        final CodeStyleManager stylist = CodeStyleManager.getInstance(project);

        for (int i = 0; i < 4; ++i) {
            final PsiMethod ctor = fact.createConstructor(element.getText());

            final StringBuilder superBuilder = new StringBuilder("{ super(");
            final StringBuilder commentBuilder = new StringBuilder(
                    "/**\n* Creates a new" + humanName + ".\n"
            );

            if (i > 0) {
                commentBuilder.append("* \n");
            }

            if ((i & 1) == 1) {
                superBuilder.append("message");
                commentBuilder.append(
                    "* @param message the detail message explaining what caused the exception\n"
                );
                ctor.getParameterList().add(msgPar.copy());
            }

            if (i == 3) {
                superBuilder.append(", ");
            }

            if ((i & 2) == 2) {
                superBuilder.append("cause");
                commentBuilder.append("* @param cause the exception that caused this exception\n");
                ctor.getParameterList().add(causePar.copy());
            }

            superBuilder.append("); }");
            commentBuilder.append("*/");

            ctor.getBody().replace(fact.createStatementFromText(superBuilder.toString(), null));
            ctor.addBefore(
                    fact.createDocCommentFromText(commentBuilder.toString()),
                    ctor.getFirstChild()
            );

            final PsiMethod formattedCtor = (PsiMethod) stylist.reformat(ctor);
            formattedCtor.addBefore(psi.createWhiteSpaceFromText(" "), formattedCtor.getBody());

            type.addBefore(formattedCtor, firstExistingCtor);
        }
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
    public boolean isAvailable(@NotNull final Project project, final Editor editor,
                               @NotNull final PsiElement element) {
        if (!element.isWritable()) {
            return false;
        }

        final PsiClass type = this.getDeclaredClass(element);

        if (!InheritanceUtil.isInheritor(type, "java.lang.Exception")) {
            return false;
        }

        return element.getText().endsWith("Exception");
    }

    private PsiClass getDeclaredClass(@NotNull final PsiElement element) {
        if (!(element instanceof PsiJavaToken)) {
            return null;
        }

        final PsiJavaToken token = (PsiJavaToken) element;
        if (token.getTokenType() != JavaTokenType.IDENTIFIER) {
            return null;
        }

        if (!(token.getParent() instanceof PsiClass)) {
            return null;
        }

        return (PsiClass) token.getParent();
    }

    private String humanizeName(final String name) {
        final String splitName = name.replaceAll("([A-Z])", " $1");
        return splitName.toLowerCase();
    }

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
