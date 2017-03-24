package com.keshasosiska;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.List;

public class ExpectBuilderAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        GenerateDialog dlg = new GenerateDialog(psiClass, "Select methods for MockBuilder");
        dlg.show();
        if (dlg.isOK()) {
            PsiJavaFile javaFile = (PsiJavaFile) psiClass.getContext();
            generate(javaFile, dlg.getFields());
        }
    }

    private PsiClass getPsiClassFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
    }

    public void generate(final PsiJavaFile psiSrcFile, final List<PsiMethod> methods) {
        new WriteCommandAction.Simple(psiSrcFile.getProject()) {
            @Override
            protected void run() throws Throwable {
                BuilderGenerator generator = new BuilderGenerator(psiSrcFile, methods);
                generator.generate();
            }
        }.execute();
    }
}
