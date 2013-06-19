package ro.catalin.prata.testflightuploader.view;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.jgoodies.common.collect.ArrayListModel;
import ro.catalin.prata.testflightuploader.Model.Team;
import ro.catalin.prata.testflightuploader.controller.KeysManager;
import ro.catalin.prata.testflightuploader.provider.UploadService;
import ro.catalin.prata.testflightuploader.utils.Utils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

/*  Copyright 2013 Catalin Prata

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License. */

/**
 * Description
 *
 * @author Catalin Prata
 *         Date: 6/1/13
 */
public class TFUploader implements ToolWindowFactory, UploadService.UploadServiceDelegate {

    private JButton uploadButton;
    private JPanel mainPanel;
    private JTextArea whatIsNewTextField;
    private JCheckBox notifyTeamCheckBox;
    private JProgressBar progressBar;
    private JList teamList;
    private JButton deleteTeamButton;
    private JButton browseButton;
    private JTextField apkFilePathTextField;
    private JButton setApiKeyButton;
    private JTextArea distributionsListTextArea;
    private JPanel browsePanel;
    private JLabel resultLabel;
    private ToolWindow myToolWindow;


    public TFUploader() {

        // update the list
        updateListOfTeams(KeysManager.instance().getTeamList());

        uploadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (KeysManager.instance().getApiKey() == null) {

                    Messages.showErrorDialog("The Test Flight API token is not set. Please press the 'Set Api Token' button to add your Test Flight API token.",
                            "Invalid Test Flight API Token");

                } else if (teamList.getSelectedIndex() < 1) {

                    Messages.showErrorDialog("Please add/select a team to send the build to.",
                            "Invalid Test Flight Team");

                } else if (apkFilePathTextField.getText().length() < 3) {

                    Messages.showErrorDialog("Please select the apk file to be sent to Test Flight.",
                            "Invalid Test Flight APK File");

                } else if (whatIsNewTextField.getText().length() < 2) {

                    Messages.showErrorDialog("Please add a release note text.",
                            "Invalid Build Release Notes");

                } else {

                    progressBar.setVisible(true);
                    uploadButton.setEnabled(false);
                    uploadButton.setText("Uploading...");

                    // upload the build
                    new UploadService().sendBuild(null, apkFilePathTextField.getText(), KeysManager.instance().getApiKey(),
                            KeysManager.instance().getTeamList().get(teamList.getSelectedIndex()).getToken(),
                            whatIsNewTextField.getText(),
                            KeysManager.instance().getTeamList().get(teamList.getSelectedIndex()).getDistributionList(),
                            notifyTeamCheckBox.isSelected(), TFUploader.this);

                }

            }
        });

        teamList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

                // if the Add new Team item is selected, open a dialog with name and token input fields
                if (teamList.getSelectedIndex() == 0) {

                    AddTeamDialog dialog = new AddTeamDialog(new AddTeamDialog.AddTeamListener() {
                        @Override
                        public void onTeamAdded(Team newTeam) {

                            // add the new team to the list
                            KeysManager.instance().addTeam(newTeam);

                            // update the list
                            updateListOfTeams(KeysManager.instance().getTeamList());

                        }
                    });
                    dialog.pack();
                    dialog.setVisible(true);

                }

            }
        });

        deleteTeamButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (teamList.getSelectedIndex() > 0) {

                    // remove the selected team from the list
                    KeysManager.instance().removeTeamAtIndex(teamList.getSelectedIndex());
                    // update the list
                    updateListOfTeams(KeysManager.instance().getTeamList());

                }

            }
        });

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // create a new file type with the apk extension to be used for file type filtering
                FileType type = FileTypeManager.getInstance().getFileTypeByExtension("apk");

                // create a descriptor for the file chooser
                FileChooserDescriptor descriptor = Utils.createSingleFileDescriptor(type);
                descriptor.setTitle("Android Apk File");
                descriptor.setDescription("Please chose the project Apk file to be uploaded to Test Flight");

                // by default open the first opened project root directory
                VirtualFile fileToSelect = ProjectManager.getInstance().getOpenProjects()[0].getBaseDir();

                // open the file chooser
                FileChooser.chooseFiles(descriptor, null, fileToSelect, new FileChooser.FileChooserConsumer() {
                    @Override
                    public void cancelled() {

                        // do nothing for now...

                    }

                    @Override
                    public void consume(List<VirtualFile> virtualFiles) {

                        String filePath = virtualFiles.get(0).getPath();

                        // the file was selected so add it to the text field
                        apkFilePathTextField.setText(filePath);

                        // save the file path
                        KeysManager.instance().setApkFilePath(filePath);

                    }
                });

            }
        });

        setApiKeyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // open an input dialog for the api key
                String apiKey = Messages.showInputDialog(ProjectManager.getInstance().getOpenProjects()[0],
                        "<HTML>This token gives you access to the upload API. You can get it from <a href=\"https://testflightapp.com/account/#api\">here</a>.</HTML>",
                        "Upload API Token", null, KeysManager.instance().getApiKey(), null);

                // save the api key after a minor validation
                if (apiKey != null && apiKey.length() > 3) {
                    KeysManager.instance().setApiKey(apiKey);
                }

            }
        });

        if (KeysManager.instance().getApkFilePath() != null) {
            // restore the apk file path if was saved
            apkFilePathTextField.setText(KeysManager.instance().getApkFilePath());
        }

        // set the distribution list input disabled by default
        distributionsListTextArea.setEnabled(false);

        teamList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

                distributionsListTextArea.setText("");

                if (teamList.getSelectedIndex() > 0) {

                    distributionsListTextArea.setText(
                            KeysManager.instance().getTeamList().get(teamList.getSelectedIndex()).getDistributionList());
                    distributionsListTextArea.setEnabled(true);

                } else {

                    distributionsListTextArea.setEnabled(false);

                }

            }
        });

        distributionsListTextArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);

                if (teamList.getSelectedIndex() > 0) {

                    KeysManager.instance().getTeamList().get(
                            teamList.getSelectedIndex()).setDistributionList(distributionsListTextArea.getText());

                }

            }
        });
    }

    /**
     * Updates the list of teams
     *
     * @param teams list of teams to be displayed on the screen
     */
    public void updateListOfTeams(ArrayList<Team> teams) {

        // create a new list model
        ArrayListModel<String> model = new ArrayListModel<String>();

        for (Team team : teams) {
            // add all the teams in the list model
            model.add(team.getName());
        }

        // set the new model containing the new team
        teamList.setModel(model);

    }

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        myToolWindow = toolWindow;

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private void createUIComponents() {

    }

    @Override
    public void onUploadFinished(final boolean finishedSuccessful) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                if (!finishedSuccessful) {

                    Messages.showErrorDialog("Build upload failed. Reason: Unknown", "Build Upload Failed");

                }

                progressBar.setVisible(false);
                uploadButton.setEnabled(true);
                uploadButton.setText("Upload");

            }
        });

    }

    @Override
    public void onPackageSizeComputed(final long totalSize) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                progressBar.setMaximum((int) totalSize);

            }
        });

    }

    @Override
    public void onProgressChanged(final long progress) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                progressBar.setValue((int) progress);

            }
        });

    }
}
