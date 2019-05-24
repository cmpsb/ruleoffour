package net.wukl.ruleoffour;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import net.wukl.ruleoffour.config.Ro4Configuration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * An inspection highlighting rule of four violations.
 */
public class ViolationInspection extends AbstractBaseJavaLocalInspectionTool {
    private final CreateConstructorsIntention cci = new CreateConstructorsIntention();

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(
            @NotNull final ProblemsHolder holder, final boolean isOnTheFly,
            @NotNull final LocalInspectionToolSession session) {
        final Ro4Configuration config = Ro4Configuration.getInstance(session.getFile().getProject());
        final String causeType = config.isExceptionAsCauseEnabled()
                                         ? CommonClassNames.JAVA_LANG_EXCEPTION
                                         : CommonClassNames.JAVA_LANG_THROWABLE;

        final String simpleCauseType = config.isExceptionAsCauseEnabled() ? "Exception" : "Throwable";

        return new PsiElementVisitor() {
            @Override
            public void visitElement(final PsiElement element) {
                if (!(element instanceof PsiClass) || !Utils.isApplicable(element)) {
                    return;
                }

                final PsiClass type = (PsiClass) element;
                final PsiElement target = type.getNameIdentifier();
                if (target == null) {
                    return;
                }

                if (Arrays.stream(type.getConstructors()).noneMatch(generateExceptionChecker(type, false, false, causeType))) {
                    holder.registerProblem(
                            target,
                            "Exception class violates the Rule of Four: no default constructor",
                            generateLQF(target)
                    );
                }

                if (Arrays.stream(type.getConstructors()).noneMatch(generateExceptionChecker(type, true, false, causeType))) {
                    holder.registerProblem(
                            target,
                            "Exception class violates the Rule of Four: no (String message) constructor",
                            generateLQF(target)
                    );
                }

                if (Arrays.stream(type.getConstructors()).noneMatch(generateExceptionChecker(type, false, true, causeType))) {
                    holder.registerProblem(
                            target,
                            "Exception class violates the Rule of Four: no (" + simpleCauseType + " cause) constructor",
                            generateLQF(target)
                    );
                }

                if (Arrays.stream(type.getConstructors()).noneMatch(generateExceptionChecker(type, true, true, causeType))) {
                    holder.registerProblem(
                            target,
                            "Exception class violates the Rule of Four: no (String message, " + simpleCauseType + " cause) constructor",
                            generateLQF(target)

                    );
                }
            }
        };
    }

    @Contract(pure = true)
    @NotNull
    private Predicate<PsiMethod> generateExceptionChecker(final PsiClass type, final boolean mustHaveMessage, final boolean mustHaveCause, final String causeType) {
        return m -> {
            final PsiParameter[] params =  m.getParameterList().getParameters();

            if (!mustHaveMessage && !mustHaveCause) {
                return params.length == 0;
            } else if (params.length == 0) {
                return false;
            }

            final GlobalSearchScope scope = type.getResolveScope();
            final PsiManager manager = type.getManager();
            final Project project = type.getProject();

            if (mustHaveMessage ^ mustHaveCause && params.length != 1) {
                return false;
            }

            final PsiParameter firstParam =  m.getParameterList().getParameters()[0];

            if (mustHaveMessage && !firstParam.getType().equals(PsiType.getJavaLangString(manager, scope))) {
                return false;
            }

            if (mustHaveMessage && !mustHaveCause) return true;

            final PsiType causePsiType = PsiType.getTypeByName(causeType, project, scope);

            if (!mustHaveMessage) {
                return firstParam.getType().equals(causePsiType);
            }

            if (m.getParameterList().getParametersCount() != 2) {
                return false;
            }

            final PsiParameter secondParam = m.getParameterList().getParameters()[1];
            return secondParam.getType().equals(causePsiType);
        };
    }

    @Contract("_ -> new")
    private LocalQuickFix generateLQF(final @NotNull PsiElement elem) {
        return new LocalQuickFixOnPsiElement(elem) {
            @NotNull
            @Override
            public String getText() {
                return cci.getText();
            }

            @Override
            public void invoke(
                    final @NotNull Project project,
                    final @NotNull PsiFile file,
                    final @NotNull PsiElement startElement, @NotNull final PsiElement endElement) {
                cci.invoke(project, null, elem);
            }

            @Nls(capitalization = Nls.Capitalization.Sentence)
            @NotNull
            @Override
            public String getFamilyName() {
                return cci.getFamilyName();
            }
        };
    }
}
