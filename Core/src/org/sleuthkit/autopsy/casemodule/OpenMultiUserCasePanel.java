/*
 * Autopsy Forensic Browser
 *
 * Copyright 2017-2019 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.casemodule;

import org.sleuthkit.autopsy.casemodule.multiusercasesbrowser.MultiUserCasesBrowserPanel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.sleuthkit.autopsy.casemodule.multiusercases.CaseNodeData;
import org.sleuthkit.autopsy.casemodule.multiusercasesbrowser.MultiUserCaseNode;

/**
 * A JPanel that allows a user to open a multi-user case.
 */
@SuppressWarnings("PMD.SingularField")  // Matisse-generated UI widgets cause lots of false positives
final class OpenMultiUserCasePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final JDialog parentDialog;
    private final MultiUserCasesBrowserPanel caseBrowserPanel;

    /**
     * Constructs a JPanel that allows a user to open a multi-user case.
     *
     * @param parentDialog The parent dialog of the panel, may be null. If
     *                     provided, the dialog is hidden when this poanel's
     *                     cancel button is pressed.
     */
    OpenMultiUserCasePanel(JDialog parentDialog) {
        this.parentDialog = parentDialog;
        initComponents(); // Machine generated code 
        caseBrowserPanel = new MultiUserCasesBrowserPanel(new ExplorerManager(), new OpenMultiUserCaseNodeCustomizer());
        caseBrowserScrollPane.add(caseBrowserPanel);
        caseBrowserScrollPane.setViewportView(caseBrowserPanel);
        openSelectedCaseButton.setEnabled(false);
        caseBrowserPanel.addListSelectionListener((ListSelectionEvent event) -> {
            openSelectedCaseButton.setEnabled(caseBrowserPanel.getExplorerManager().getSelectedNodes().length > 0);
        });
    }

    /**
     * Refreshes the child component that displays the multi-user cases known to
     * the coordination service..
     */
    void refreshDisplay() {
        caseBrowserPanel.displayCases();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        openSingleUserCaseButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        searchLabel = new javax.swing.JLabel();
        openSelectedCaseButton = new javax.swing.JButton();
        caseBrowserScrollPane = new javax.swing.JScrollPane();

        setName("Completed Cases"); // NOI18N
        setPreferredSize(new java.awt.Dimension(960, 485));

        org.openide.awt.Mnemonics.setLocalizedText(openSingleUserCaseButton, org.openide.util.NbBundle.getMessage(OpenMultiUserCasePanel.class, "OpenMultiUserCasePanel.openSingleUserCaseButton.text")); // NOI18N
        openSingleUserCaseButton.setMinimumSize(new java.awt.Dimension(156, 23));
        openSingleUserCaseButton.setPreferredSize(new java.awt.Dimension(156, 23));
        openSingleUserCaseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openSingleUserCaseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(OpenMultiUserCasePanel.class, "OpenMultiUserCasePanel.cancelButton.text")); // NOI18N
        cancelButton.setMaximumSize(new java.awt.Dimension(80, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(80, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(80, 23));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(OpenMultiUserCasePanel.class, "OpenMultiUserCasePanel.searchLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(openSelectedCaseButton, org.openide.util.NbBundle.getMessage(OpenMultiUserCasePanel.class, "OpenMultiUserCasePanel.openSelectedCaseButton.text")); // NOI18N
        openSelectedCaseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openSelectedCaseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchLabel)
                .addGap(32, 32, 32)
                .addComponent(openSingleUserCaseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addComponent(openSelectedCaseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(caseBrowserScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 948, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, openSelectedCaseButton, openSingleUserCaseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(462, 462, 462)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openSingleUserCaseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(openSelectedCaseButton))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(caseBrowserScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 445, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(49, Short.MAX_VALUE)))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {cancelButton, openSelectedCaseButton, openSingleUserCaseButton});

    }// </editor-fold>//GEN-END:initComponents

    /**
     * Opens the standard open single user case window.
     *
     * @param evt An ActionEvent, unused.
     */
    private void openSingleUserCaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openSingleUserCaseButtonActionPerformed
        Lookup.getDefault().lookup(CaseOpenAction.class).openCaseSelectionWindow();
    }//GEN-LAST:event_openSingleUserCaseButtonActionPerformed

    /**
     * Closes the parent open multi-user case dialog.
     *
     * @param evt An ActionEvent, unused.
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if (parentDialog != null) {
            parentDialog.setVisible(false);
        }
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Opens the multi-user case selected in the child multi-user case browser
     * panel.
     *
     * @param evt An ActionEvent, unused.
     */
    private void openSelectedCaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openSelectedCaseButtonActionPerformed
        ExplorerManager explorerManager = caseBrowserPanel.getExplorerManager();
        Node[] selectedNodes = explorerManager.getSelectedNodes();
        if (selectedNodes.length > 0 && selectedNodes[0] instanceof MultiUserCaseNode) {
            MultiUserCaseNode caseNode = (MultiUserCaseNode) selectedNodes[0];
            CaseNodeData nodeData = caseNode.getLookup().lookup(CaseNodeData.class);
            new OpenMultiUserCaseAction(nodeData).actionPerformed(evt);
        }
    }//GEN-LAST:event_openSelectedCaseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane caseBrowserScrollPane;
    private javax.swing.JButton openSelectedCaseButton;
    private javax.swing.JButton openSingleUserCaseButton;
    private javax.swing.JLabel searchLabel;
    // End of variables declaration//GEN-END:variables
}
