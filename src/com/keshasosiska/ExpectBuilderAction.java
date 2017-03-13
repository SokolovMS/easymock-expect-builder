package com.keshasosiska;

import static com.google.common.base.Preconditions.checkArgument;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ClassFileViewProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.util.PsiTreeUtil;

import java.io.IOException;
import java.util.List;

public class ExpectBuilderAction extends AnAction {
    private static final String separator = "/";

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

    public void generate(final PsiJavaFile javaClass, final List<PsiMethod> methods) {
        PsiJavaFile builderClass = createBuilderClass(javaClass);

        new WriteCommandAction.Simple(builderClass.getProject(), builderClass.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
//                generateTestClass(builderClass, methods);
            }
        }.execute();
    }

    private PsiJavaFile createBuilderClass(final PsiJavaFile sourceClass) {
        try {
            return createTestBuilderClass(sourceClass);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private PsiJavaFile createTestBuilderClass(final PsiJavaFile sourceClass) throws IOException {
//        String fileName = sourceClass.getName().replace(".java", "MockBuilder.java");
//
//        PsiJavaFile psiJavaFile = (PsiJavaFile) PsiFileFactory.getInstance(sourceClass.getProject())
//                .createFileFromText(fileName, JavaFileType.INSTANCE, "");
//
//        psiJavaFile.setPackageName(sourceClass.getPackageName());
//
//        PsiDirectory directory = sourceClass.getContainingDirectory()
//        PsiElement addedElement = directory.add(psiJavaFile);
//        return psiJavaFile;

        PsiManager psiManager = sourceClass.getManager();
        Project project = sourceClass.getProject();
        String packageName = sourceClass.getPackageName();
        VirtualFile virtualTestFile = project.getBaseDir()
                .findOrCreateChildData(project, "src")
                .findOrCreateChildData(project, "test")
                .findOrCreateChildData(project, "java");

        for (String packagePart : packageName.split("\\.")) {
            virtualTestFile = virtualTestFile.findOrCreateChildData(project, packagePart);
        }

        String fileName = sourceClass.getName().replace(".java", "MockBuilder.java");
        virtualTestFile = virtualTestFile.findOrCreateChildData(project, fileName);

        PsiJavaFile testFile = new PsiJavaFileImpl(new ClassFileViewProvider(psiManager, virtualTestFile));
        testFile.setPackageName(packageName);

        return testFile;
    }

    private void generateTestClass(final PsiClass builderClass, final List<PsiMethod> methods) {
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

    private VirtualFile getTestFile(final VirtualFile sourceFile) {
        VirtualFile testFile;

        String sourcePath = sourceFile.getPath();
        String testPath = makeTestPath(sourcePath);

        return sourceFile;
    }

    private String makeTestPath(final String sourcePath) {
        String regex = separator + "main" + separator;
        String[] parts = sourcePath.split(regex);
        if (parts.length == 1) {
            return makeTestFilename(sourcePath);
        }

        String testPath = parts[0];
        for (int i = 1; i < parts.length - 1; i++) {
            testPath += regex + parts[i];
        }
        testPath += separator + "test" + separator + parts[parts.length - 1];

        return makeTestFilename(testPath);
    }

    private String makeTestFilename(final String testPath) {
        checkArgument(testPath.endsWith(".java"), "Can work only with java files");

        return testPath.replace(".java", "TestBuilder.java");
    }
}
