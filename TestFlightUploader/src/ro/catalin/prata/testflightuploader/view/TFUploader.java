package ro.catalin.prata.testflightuploader.view;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.jgoodies.common.collect.ArrayListModel;
import ro.catalin.prata.testflightuploader.Model.Team;
import ro.catalin.prata.testflightuploader.controller.KeysManager;
import ro.catalin.prata.testflightuploader.controller.ModulesManager;
import ro.catalin.prata.testflightuploader.provider.UploadService;
import ro.catalin.prata.testflightuploader.utils.Utils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Calendar;
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
    private JTextField buildVNameTextField;
    private JTextField buildVCodeTextField;
    private JButton buildVersionHelpBtn;
    private JCheckBox buildVersionCheck;
    private JLabel bVersionCodeLbl;
    private JLabel bVersionNameLbl;
    private JComboBox moduleCombo;
    private JTextPane pleaseNoteThatOnlyTextPane;
    private ToolWindow toolWindow;

    public TFUploader() {

        // update the list
        updateListOfTeams(KeysManager.instance().getTeamList());

        uploadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                performUploadValidation();

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

        buildVersionHelpBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // build version info button was pressed, display the about text...
                Messages.showInfoMessage("This feature let you change the version code/name of the build after it is sent to Test Flight.\n" +
                        "If you change the values of the build version code or name, it will be saved in your main manifest file. \n" +
                        "This can be useful to remind you to increment the build number after sending the apk to TestFlight. \n \n" +
                        "Please note that the change is made after the build is sent to Test Flight.",
                        "Android Build Version Code/Name Update");

            }
        });

        buildVersionCheck.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {

                if (buildVersionCheck.isSelected()) {

                    setBuildFeatureComponentsVisible(true);

                } else {

                    setBuildFeatureComponentsVisible(false);

                }

            }
        });

        moduleCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {

                // if a module is selected, save the module
                KeysManager.instance().setSelectedModuleName((String) moduleCombo.getSelectedItem());

                // update the apk path
                Module module = ModulesManager.instance().getModuleByName((String) moduleCombo.getSelectedItem());
                apkFilePathTextField.setText(ModulesManager.instance().getAndroidApkPath(module));

                // update the build version fields too
                updateBuildVersionFields();

            }
        });

        // setup the previously saved values on the UI or the default ones
        setupValuesOnUI();

    }

    /**
     * Updates the build version(code and name) fields
     */
    public void updateBuildVersionFields() {
        Module module = ModulesManager.instance().getModuleByName((String) moduleCombo.getSelectedItem());
        // update the code and name text fields manifest build version code and name values
        buildVCodeTextField.setText(ModulesManager.instance().getBuildVersionCode(ModulesManager.instance().getManifestForModule(module)));
        buildVNameTextField.setText(ModulesManager.instance().getBuildVersionName(ModulesManager.instance().getManifestForModule(module)));

    }

    /**
     * Performs validation before uploading the build to test flight, if everything is in order, the build is sent
     */
    public void performUploadValidation() {

        if (Calendar.getInstance().getTimeInMillis() - KeysManager.getLastCompileTime().getTimeInMillis() >
                KeysManager.MAX_MILLISECONDS_SINCE_LAST_COMPILE) {

            Messages.showErrorDialog("Please note that the project was not compiled since at least " +
                    (KeysManager.MAX_MILLISECONDS_SINCE_LAST_COMPILE / 1000 / 60) + " minutes ago. " +
                    "If you made changes since then, please rebuild the project to generate the new APK file and upload the latest build to Test Flight.",
                    "Possible Old Build");

            // display this message only once to the user
            KeysManager.setLastCompileTime(Calendar.getInstance());

        } else if (KeysManager.instance().getApiKey() == null) {

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

            if (buildVersionCheck.isSelected()) {

                Module module = ModulesManager.instance().getModuleByName((String) moduleCombo.getSelectedItem());

                ModulesManager.instance().setBuildVersionNameAndCode(ModulesManager.instance().getManifestForModule(module),
                        buildVNameTextField.getText(), buildVCodeTextField.getText(), new ModulesManager.ManifestChangesDelegate() {
                    @Override
                    public void onVersionValueFinishedUpdate() {

                        uploadBuild();

                    }
                });

            } else {

                uploadBuild();

            }

        }

    }

    /**
     * Uploads the build to test flight, it updates also the UI
     */
    public void uploadBuild() {

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

    /**
     * Set the default or previously saved values on the UI components
     */
    public void setupValuesOnUI() {

        Module previouslySelectedModule;

        // if the apk file path was not saved previously by the user, set the saved module apk file path or the best matching module
        previouslySelectedModule = ModulesManager.instance().getModuleByName(KeysManager.instance().getSelectedModuleName());
        if (previouslySelectedModule != null) {

            apkFilePathTextField.setText(ModulesManager.instance().getAndroidApkPath(previouslySelectedModule));

        } else {

            // get the best matching module for this project and set it's file path
            previouslySelectedModule = ModulesManager.instance().getMostImportantModule();
            apkFilePathTextField.setText(ModulesManager.instance().getAndroidApkPath(previouslySelectedModule));

        }

        // set the model of the modules
        moduleCombo.setModel(new DefaultComboBoxModel(ModulesManager.instance().getAllModuleNames()));

        // set the selection
        moduleCombo.setSelectedIndex(ModulesManager.instance().getSelectedModuleIndex(previouslySelectedModule.getName()));

        // set the distribution list input disabled by default
        distributionsListTextArea.setEnabled(false);

        // hide the build version change feature components by default
        setBuildFeatureComponentsVisible(false);

        // update the build version fields
        updateBuildVersionFields();

    }

    /**
     * Changes the components visibility of the build version changing feature
     *
     * @param visible true if the components should be displayed, false otherwise
     */
    public void setBuildFeatureComponentsVisible(boolean visible) {

        bVersionCodeLbl.setVisible(visible);
        bVersionNameLbl.setVisible(visible);
        buildVCodeTextField.setVisible(visible);
        buildVNameTextField.setVisible(visible);

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

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        this.toolWindow = toolWindow;

        ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerListener() {
            @Override
            public void projectOpened(Project project) {

                // get the best matching module for this project and set it's file path
                Module previouslySelectedModule = ModulesManager.instance().getMostImportantModule();
                apkFilePathTextField.setText(ModulesManager.instance().getAndroidApkPath(previouslySelectedModule));

                KeysManager.instance().setSelectedModuleName(previouslySelectedModule.getName());

                // set the model of the modules
                moduleCombo.setModel(new DefaultComboBoxModel(ModulesManager.instance().getAllModuleNames()));

                // set the selection
                moduleCombo.setSelectedIndex(ModulesManager.instance().getSelectedModuleIndex(previouslySelectedModule.getName()));

            }

            @Override
            public boolean canCloseProject(Project project) {
                return true;
            }

            @Override
            public void projectClosed(Project project) {


            }

            @Override
            public void projectClosing(Project project) {

            }
        });

    }

    private void createUIComponents() {

    }

    @Override
    public void onUploadFinished(final boolean finishedSuccessful) {

        // upload is now finished, run some UI updates on the UI thread
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
