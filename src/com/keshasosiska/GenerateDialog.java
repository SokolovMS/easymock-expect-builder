package com.keshasosiska;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import javax.swing.*;

public class GenerateDialog extends DialogWrapper {
    private CollectionListModel<PsiMethod> methods;
    private final LabeledComponent<JPanel> myComponent;

    public GenerateDialog(PsiClass psiClass, String dialogTitle) {
        super(psiClass.getProject());
        setTitle(dialogTitle);

        methods = new CollectionListModel<>(psiClass.getMethods());
        JList fieldList = new JList(methods);
        fieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        decorator.disableAddAction();
        JPanel panel = decorator.createPanel();
        myComponent = LabeledComponent.create(panel, dialogTitle);

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myComponent;
    }

    public List<PsiMethod> getFields() {
        return methods.getItems();
    }
}
