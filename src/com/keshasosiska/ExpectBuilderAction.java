package com.keshasosiska;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

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

    public void generate(final PsiJavaFile psiSrcFile, final List<PsiMethod> methods) {
        new WriteCommandAction.Simple(psiSrcFile.getProject()) {
            @Override
            protected void run() throws Throwable {
                createBuilderClass(psiSrcFile, methods);
            }
        }.execute();
    }

    private void createBuilderClass(final PsiJavaFile psiSrcFile, final List<PsiMethod> methods) {
        try {
            PsiJavaFile psiTestFile = createEmptyBuilder(psiSrcFile);
            fillBuilderWithContent(psiSrcFile, psiTestFile, methods);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillBuilderWithContent(final PsiJavaFile psiSrcFile,
                                        final PsiJavaFile psiTestFile,
                                        final List<PsiMethod> methods) {
        PsiClass srcClass = psiSrcFile.getClasses()[0];
        PsiClass testClass = psiTestFile.getClasses()[0];

        Project project = psiSrcFile.getProject();
        PsiElementFactory factory = PsiElementFactory.SERVICE.getInstance(project);
        GlobalSearchScope scope = psiSrcFile.getResolveScope();

        // import
        PsiClass importClass = JavaPsiFacade.getInstance(project).findClass("org.easymock.EasyMock", scope);
        psiTestFile.getImportList().add(factory.createImportStatement(importClass));

        // field
        PsiType srcClassType = PsiType.getTypeByName(srcClass.getName(), project, scope);
        PsiElement field = factory.createField("mock", srcClassType);
        testClass.add(field);

        // Constructor
        PsiMethod constructor = factory.createConstructor();
        PsiCodeBlock constructorBody = constructor.getBody();
        String assignText = String.format("mock = EasyMock.mock(%s.class);", srcClass.getName());
        PsiStatement assignStatement = factory.createStatementFromText(assignText, null);
        constructorBody.add(assignStatement);
        testClass.add(constructor);

        for (PsiMethod method : methods) {
            // TODO: change method contents.
//            testClass.add(method);
        }

        // TODO: Add buildAndReplay method.
        PsiMethod buildAndReplay = factory.createMethod("buildAndReplay", srcClassType);
        PsiCodeBlock buildAndReplayBody = buildAndReplay.getBody();
        buildAndReplayBody.add(factory.createStatementFromText("EasyMock.replay(mock);", null));
        buildAndReplayBody.add(factory.createStatementFromText("return mock;", null));
        testClass.add(buildAndReplay);
    }

    private PsiJavaFile createEmptyBuilder(final PsiJavaFile sourceClass) {
        try {
            return (PsiJavaFile)createFromTemplate(sourceClass);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private VirtualFile getEmptyVirtualFile(final PsiJavaFile sourceClass) throws IOException {
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
        return virtualTestFile.findOrCreateChildData(project, fileName);
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

    public PsiFile createFromTemplate(final PsiJavaFile sourceClass)
            throws IncorrectOperationException, IOException {
        Project project = sourceClass.getProject();

        // TODO: getEmptyVirtualFile creates virtual file. Possible to find PsiDirectory only from sourceClass
        VirtualFile testVirtualFile = getEmptyVirtualFile(sourceClass);
        PsiFile testFile = PsiManager.getInstance(sourceClass.getProject()).findFile(testVirtualFile);

        PsiDirectory directory = testFile.getContainingDirectory();

        FileTemplateManager templateManager = FileTemplateManager.getDefaultInstance();

        Properties properties = new Properties(templateManager.getDefaultProperties());
        JavaTemplateUtil.setPackageNameAttribute(properties, directory);

        String name = testVirtualFile.getName().split(".java")[0];
        properties.setProperty("NAME", name);
        properties.setProperty("lowCaseName", name.substring(0, 1).toLowerCase() + name.substring(1));

        String text;
        try {
            FileTemplate template = templateManager.getInternalTemplate("Class");
            text = template.getText(properties);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load template for "
                    + templateManager.internalTemplateToSubject("Class"),
                    e);
        }

        PsiFileFactory factory = PsiFileFactory.getInstance(project);
        String fileName = testVirtualFile.getName();
        PsiFile file = factory.createFileFromText(fileName, JavaFileType.INSTANCE, text);

        try {
            directory.checkCreateFile(file.getName());
        } catch (IncorrectOperationException e) {
            testFile.delete();
        }

        return (PsiFile) directory.add(file);
    }
}
