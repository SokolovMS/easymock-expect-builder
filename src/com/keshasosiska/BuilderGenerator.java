package com.keshasosiska;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.ide.highlighter.JavaFileType;
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
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class BuilderGenerator {
    private final PsiJavaFile psiSrcFile;
    private final List<PsiMethod> methods;

    private final Project project;
    private final String packageName;
    private final String testFileName;

    public BuilderGenerator(final PsiJavaFile psiSrcFile, final List<PsiMethod> methods) {
        this.psiSrcFile = psiSrcFile;
        this.methods = methods;

        project = psiSrcFile.getProject();
        packageName = psiSrcFile.getPackageName();
        testFileName = psiSrcFile.getName().replace(".java", "MockBuilder.java");
    }

    public void generate() {
        PsiJavaFile psiTestFile;
        try {
            psiTestFile = (PsiJavaFile)createFromTemplate();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        fillBuilderWithContent(psiTestFile, methods);
    }

    private PsiFile createFromTemplate() throws IncorrectOperationException, IOException {
        // TODO: getEmptyVirtualFile creates virtual file. Possible to find PsiDirectory only from sourceClass
        VirtualFile testVirtualFile = getEmptyVirtualFile();
        PsiFile testFile = PsiManager.getInstance(project).findFile(testVirtualFile);

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

    private VirtualFile getEmptyVirtualFile() throws IOException {
        // TODO: I guess test directory is not always "src/test/java"
        PsiDirectory currentDir = psiSrcFile.getContainingDirectory();
        while (!Objects.equals("src", currentDir.getName())) {
            currentDir = currentDir.getParent();
        }
        VirtualFile virtualTestFile = currentDir.getVirtualFile()
                .findOrCreateChildData(project, "test")
                .findOrCreateChildData(project, "java");

        for (String packagePart : packageName.split("\\.")) {
            virtualTestFile = virtualTestFile.findOrCreateChildData(project, packagePart);
        }

        return virtualTestFile.findOrCreateChildData(project, testFileName);
    }

    private void fillBuilderWithContent(final PsiJavaFile psiTestFile,
                                        final List<PsiMethod> methods) {
        PsiClass srcClass = psiSrcFile.getClasses()[0];
        PsiClass testClass = psiTestFile.getClasses()[0];

        PsiElementFactory factory = PsiElementFactory.SERVICE.getInstance(project);
        GlobalSearchScope srcScope = psiSrcFile.getResolveScope();
        GlobalSearchScope testScope = psiTestFile.getResolveScope();

        // import
        PsiClass importClass = JavaPsiFacade.getInstance(project).findClass("org.easymock.EasyMock", testScope);
        if (importClass != null) {
            psiTestFile.getImportList().add(factory.createImportStatement(importClass));
        }

        // field
        PsiType srcClassType = PsiType.getTypeByName(srcClass.getName(), project, srcScope);
        PsiElement field = factory.createField("mock", srcClassType);
        testClass.add(field);

        // constructor
        PsiMethod constructor = factory.createConstructor();
        PsiCodeBlock constructorBody = constructor.getBody();
        String assignText = String.format("mock = EasyMock.mock(%s.class);", srcClass.getName());
        PsiStatement assignStatement = factory.createStatementFromText(assignText, null);
        constructorBody.add(assignStatement);
        testClass.add(constructor);

        for (PsiMethod method : methods) {
            // methods
            addMethod(factory, testClass, method);
        }

        // buildAndReplay() method.
        PsiMethod buildAndReplay = factory.createMethod("buildAndReplay", srcClassType);
        PsiCodeBlock buildAndReplayBody = buildAndReplay.getBody();
        buildAndReplayBody.add(factory.createStatementFromText("EasyMock.replay(mock);", null));
        buildAndReplayBody.add(factory.createStatementFromText("return mock;", null));
        testClass.add(buildAndReplay);
    }

    private void addMethod(final PsiElementFactory factory, final PsiClass testClass, final PsiMethod method) {
        if (method.isConstructor()) {
            return;
        }

        PsiMethod builderMethod = factory.createMethod(method.getName(), factory.createType(testClass));

        PsiParameterList parameters = builderMethod.getParameterList();
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            parameters.add(parameter);
        }

        PsiCodeBlock methodBody = builderMethod.getBody();

        if (Objects.equals(method.getReturnType(), PsiType.VOID)) {
            String expectText = String.format("mock.%s(%s);", method.getName(), getParametersString(method));
            methodBody.add(factory.createStatementFromText(expectText, null));
            methodBody.add(factory.createStatementFromText("EasyMock.expectLastCall().once();", null));
        } else {
            PsiParameter expectedParameter = factory.createParameter("expected", method.getReturnType());
            expectedParameter.getModifierList().setModifierProperty(PsiModifier.FINAL, true);
            parameters.add(expectedParameter);

            String expectText = String.format("EasyMock.expect(mock.%s(%s)).andReturn(expected).once();",
                    method.getName(), getParametersString(method));
            methodBody.add(factory.createStatementFromText(expectText, null));
        }

        methodBody.add(factory.createStatementFromText("return this;", null));

        testClass.add(builderMethod);
    }

    private String getParametersString(final PsiMethod method) {
        PsiParameter[] psiParameters = method.getParameterList().getParameters();
        List<String> parameters = Arrays.stream(psiParameters)
                .map(PsiParameter::getName)
                .collect(Collectors.toList());
        return String.join(", ", parameters);
    }
}
