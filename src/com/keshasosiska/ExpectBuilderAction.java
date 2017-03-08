package com.keshasosiska;

import static com.google.common.base.Preconditions.checkArgument;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;

public class ExpectBuilderAction extends AnAction {
    private static final String separator = "/";

    @Override
    public void actionPerformed(AnActionEvent e) {
        VirtualFile sourceFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        String fileName = sourceFile != null ? sourceFile.getName() : null;

        VirtualFile testFile = getTestFile(sourceFile);
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
