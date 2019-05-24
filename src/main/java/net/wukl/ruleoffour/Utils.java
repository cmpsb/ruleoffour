package net.wukl.ruleoffour;

import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.util.InheritanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Utils {
    public static boolean isApplicable(final @NotNull PsiElement element) {
        if (!element.isWritable()) {
            return false;
        }

        final PsiClass type = (element instanceof PsiClass)
                ? (PsiClass) element
                : getDeclaredClass(element);

        if (!InheritanceUtil.isInheritor(type, "java.lang.Exception")) {
            return false;
        }

        final String className = type.getName();
        if (className == null) {
            return false;
        }

        return className.endsWith("Exception");
    }

    /**
     * Returns the declared class from a PSI element part of a class declaration.
     *
     * @param element the element to get the declared class from
     *
     * @return the declared class
     */
    @Nullable
    public static PsiClass getDeclaredClass(final @NotNull PsiElement element) {
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
}
