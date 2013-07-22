package ro.catalin.prata.testflightuploader.controller;

import com.intellij.openapi.compiler.CompilationStatusListener;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import ro.catalin.prata.testflightuploader.Model.Team;
import ro.catalin.prata.testflightuploader.utils.Constants;
import ro.catalin.prata.testflightuploader.view.TFUploader;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

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
 * Manages the teams of the user
 *
 * @author Catalin Prata
 *         Date: 6/3/13
 */
@State(
        name = "KeysManager", storages = {
        @Storage(
                id = "other",
                file = "$APP_CONFIG$/" + Constants.PERSISTENCE_FILE_NAME)
})
public class KeysManager implements PersistentStateComponent<Element>,CompilationStatusListener {

    // xml parsing constant used as a root tag for this class
    public static final String XML_ROOT_NAME_Key_MANAGER = "KeyManager";
    // xml parsing constants used for team
    public static final String XML_ROOT_NAME_TEAM_MANAGER = "TeamManager";
    public static final String XML_TEAM_MANAGER_TEAM = "Team";
    public static final String XML_TEAM_MANAGER_NAME = "name";
    public static final String XML_TEAM_MANAGER_TOKEN = "token";
    public static final String XML_TEAM_MANAGER_DISTRIBUTION_LIST = "distribution";
    public static final String XML_TEAM_MANAGER_COMPONENT = "component";
    // xml parsing constants used for api key
    public static final String XML_ROOT_NAME_API_KEY = "ApiKey";
    // xml parsing constant used for the apk file path
    public static final String XML_ROOT_NAME_APK_FILE_PATH = "ApkFilePath";
    public static final String XML_ROOT_NAME_SELECTED_MODULE_NAME = "SelectedModuleName";
    public static final String XML_ROOT_NAME_SELECTED_PROJECT_NAME = "SelectedProjectName";
    /**
     * Maximum number of milliseconds since the last compile time before the user can send the build to Test Flight (currently 5 minutes)
     */
    public static final int MAX_MILLISECONDS_SINCE_LAST_COMPILE = 1000 * 60 * 5;
    // keeps the last compile time so we can see how much time passed since the last build compile
    private static Calendar lastCompileTime = Calendar.getInstance();
    /**
     * Manager's single instance
     */
    private static KeysManager sInstance = null;
    /**
     * List of teams for this user
     */
    private ArrayList<Team> teamList;
    /**
     * Test flight api key
     */
    private String apiKey;
    /**
     * Project apk file path
     */
    private String apkFilePath;
    /**
     * This is the user selected module name
     */
    private String selectedModuleName;
    /**
     * This is the user selected project name
     */
    private String selectedProjectName;

    public KeysManager() {

        teamList = new ArrayList<Team>();

    }

    public static KeysManager instance() {

        if (sInstance == null) {
            sInstance = ServiceManager.getService(KeysManager.class);

            ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerListener() {
                @Override
                public void projectOpened(Project project) {

                    CompilerManager.getInstance(project)
                            .addCompilationStatusListener(sInstance);

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

                    CompilerManager.getInstance(project)
                            .removeCompilationStatusListener(sInstance);

                }
            });

            CompilerManager.getInstance(ProjectManager.getInstance().getOpenProjects()[0])
                    .addCompilationStatusListener(sInstance);
        }

        return sInstance;
    }

    @Nullable
    @Override
    public Element getState() {

        // create the class root tag
        Element rootTag = new Element(XML_ROOT_NAME_Key_MANAGER);

        // create the team root xml tag
        Element teamRootTag = new Element(XML_ROOT_NAME_TEAM_MANAGER);

        for (Team team : teamList) {

            // create elements with the teams
            Element teamElement = new Element(XML_TEAM_MANAGER_TEAM);
            teamElement.addContent(new Element(XML_TEAM_MANAGER_NAME).setText(team.getName()));
            teamElement.addContent(new Element(XML_TEAM_MANAGER_TOKEN).setText(team.getToken()));
            // set the distributions list if it has one
            if (team.getDistributionList() != null) {
                teamElement.addContent(new Element(XML_TEAM_MANAGER_DISTRIBUTION_LIST).setText(team.getDistributionList()));
            }

            teamRootTag.addContent(teamElement);

        }

        // add the team elements
        rootTag.addContent(teamRootTag);

        if (apiKey != null) {
            // set the api key
            Element apiKeyTag = new Element(XML_ROOT_NAME_API_KEY).setText(apiKey);
            rootTag.addContent(apiKeyTag);
        }

        if (apkFilePath != null) {
            // set the apk file path
            Element filePathTag = new Element(XML_ROOT_NAME_APK_FILE_PATH).setText(apkFilePath);
            rootTag.addContent(filePathTag);
        }

        if (selectedModuleName != null) {
            // set the user selected module
            Element moduleName = new Element(XML_ROOT_NAME_SELECTED_MODULE_NAME).setText(selectedModuleName);
            rootTag.addContent(moduleName);

        }

        if (selectedProjectName != null) {
            // set the user selected project
            Element projectName = new Element(XML_ROOT_NAME_SELECTED_PROJECT_NAME).setText(selectedProjectName);
            rootTag.addContent(projectName);

        }

        return rootTag;
    }

    @Override
    public void loadState(Element componentTag) {

        if (componentTag.getName().equals(XML_TEAM_MANAGER_COMPONENT)) {

            Iterator rootIterator = componentTag.getDescendants();

            // loop through all the root elements and parse them accordingly
            while (rootIterator.hasNext()) {

                Object element = rootIterator.next();

                if (!(element instanceof Element)) {
                    continue;
                }

                Element rootElement = (Element) element;

                if (rootElement.getName().equals(XML_ROOT_NAME_TEAM_MANAGER)) {
                    // parse the team list
                    teamList = parseTeam(rootElement);

                } else if (rootElement.getName().equals(XML_ROOT_NAME_API_KEY)) {
                    // parse the api key
                    apiKey = parseApiKey(rootElement);

                } else if (rootElement.getName().equals(XML_ROOT_NAME_APK_FILE_PATH)) {
                    // parse the apk file path
                    apkFilePath = parseApkFilePath(rootElement);

                } else if (rootElement.getName().equals(XML_ROOT_NAME_SELECTED_MODULE_NAME)) {
                    // parse the user selected module name
                    selectedModuleName = parseUserSelectedModuleName(rootElement);

                } else if (rootElement.getName().equals(XML_ROOT_NAME_SELECTED_PROJECT_NAME)) {
                    // parse the user selected project name
                    selectedProjectName = parseUserSelectedProjectName(rootElement);

                }

            }

        }

    }

    /**
     * Parse the user's selected project name xml element
     *
     * @param element the user's selected project element
     * @return parsed value
     */
    public String parseUserSelectedProjectName(Element element) {

        return element.getText();

    }

    /**
     * Parse the user's selected module name xml element
     *
     * @param element the user's selected module element
     * @return parsed value
     */
    public String parseUserSelectedModuleName(Element element) {

        return element.getText();

    }

    /**
     * Parse the apk file path xml element
     *
     * @param element apk file path element
     * @return parsed file path
     */
    public String parseApkFilePath(Element element) {

        return element.getText();

    }

    /**
     * Parse the api key xml element
     *
     * @param element the root element of the api key
     * @return parsed api key
     */
    public String parseApiKey(Element element) {

        return element.getText();

    }

    /**
     * Parses xml data into an ArrayList of Team objects
     *
     * @param teamRoot xml root (XML_ROOT_NAME_TEAM_MANAGER)
     * @return array list of team objects
     */
    public ArrayList<Team> parseTeam(Element teamRoot) {

        ArrayList<Team> parsedList = new ArrayList<Team>();

        Iterator teamIterator = teamRoot.getDescendants();
        // iterate through the teams elements
        while (teamIterator.hasNext()) {

            Object element = teamIterator.next();

            if (!(element instanceof Element)) {
                continue;
            }

            Element teamElement = (Element) element;

            Team parsedTeam = parseTeamElement(teamElement);
            if (parsedTeam != null) {
                // parse each team object and add them to the list
                parsedList.add(parsedTeam);
            }

        }

        return parsedList;

    }

    /**
     * Parses a team xml element into a team object
     *
     * @param teamElement team element
     * @return parsed team object
     */
    public Team parseTeamElement(Element teamElement) {

        Team team = new Team();

        Iterator teamComponents = teamElement.getDescendants();
        // iterate through the Team components (token/name)
        while (teamComponents.hasNext()) {

            Object elementComponents = teamComponents.next();

            if (!(elementComponents instanceof Element)) {
                continue;
            }

            Element component = (Element) elementComponents;

            if (component.getName().equals(XML_TEAM_MANAGER_NAME)) {
                // if the name is found, set the name of the team
                team.setName(component.getText());

            } else if (component.getName().equals(XML_TEAM_MANAGER_TOKEN)) {
                // if the token is found, set the token of the team
                team.setToken(component.getText());

            } else if (component.getName().equals(XML_TEAM_MANAGER_DISTRIBUTION_LIST)) {
                // if the token is found, set the token of the team
                team.setDistributionList(component.getText());

            }

        }

        if (team.getName() != null) {
            // add the parsed team to the list
            return team;
        }

        return null;

    }

    /**
     * Returns the list of teams
     *
     * @return list of teams
     */
    public ArrayList<Team> getTeamList() {

        if (teamList == null || teamList.size() < 1) {
            // if the list is empty or null, add the first item that will be used to add new team objects
            teamList = new ArrayList<Team>();
            Team firstObject = new Team();
            firstObject.setName("Add new Team");
            firstObject.setToken("");
            teamList.add(firstObject);
        }

        return teamList;
    }

    public void setTeamList(ArrayList<Team> teamList) {
        this.teamList = teamList;
    }

    /**
     * Adds the given team to the list of teams
     *
     * @param newTeam a new team to add to the list
     */
    public void addTeam(Team newTeam) {
        teamList.add(newTeam);
    }

    /**
     * Removes the team from the controller at the given index, it also check if the index is valid or the list is null
     *
     * @param index index of the team in the list
     */
    public void removeTeamAtIndex(int index) {

        if (teamList != null && teamList.size() > index) {
            teamList.remove(index);
        }

    }

    /**
     * Returns the api key for test flight authentication
     *
     * @return api key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Set the api key used for the test flight authentication
     *
     * @param apiKey api key
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Returns the apk file path
     *
     * @return apk file path
     */
    public String getApkFilePath() {
        return apkFilePath;
    }

    /**
     * Set the apk file path
     *
     * @param apkFilePath apk file path
     */
    public void setApkFilePath(String apkFilePath) {
        this.apkFilePath = apkFilePath;
    }

    /**
     * Returns the user's selected module name, null if none was saved so far
     *
     * @return user's selected module name
     */
    public String getSelectedModuleName() {
        return selectedModuleName;
    }

    /**
     * Set the user's selected module name, used to get the apk file path for the module
     *
     * @param selectedModuleName selected module name
     */
    public void setSelectedModuleName(String selectedModuleName) {
        this.selectedModuleName = selectedModuleName;
    }

    /**
     * Returns the selected project name
     *
     * @return user's selected project name
     */
    public String getSelectedProjectName() {
        return selectedProjectName;
    }

    /**
     * Set the selected project name
     *
     * @param selectedProjectName user selected project name
     */
    public void setSelectedProjectName(String selectedProjectName) {
        this.selectedProjectName = selectedProjectName;
    }

    public static Calendar getLastCompileTime() {
        return lastCompileTime;
    }

    public static void setLastCompileTime(Calendar lastCompileTime) {
        KeysManager.lastCompileTime = lastCompileTime;
    }

    @Override
    public void compilationFinished(boolean aborted, int errors, int warnings, CompileContext compileContext) {

        if (errors < 1) {

            // get the current time
            lastCompileTime = Calendar.getInstance();

            StatusBar statusBar = WindowManager.getInstance()
                    .getStatusBar(ProjectManager.getInstance().getOpenProjects()[0]);


            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder("Build ready to be sent to TestFlight, <a href='open'>Click Here</a> to open TestFlightUploader and send it.",
                            MessageType.INFO, new HyperlinkListener() {
                        @Override
                        public void hyperlinkUpdate(HyperlinkEvent e) {

                            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                ToolWindowManager.getInstance(ProjectManager.getInstance().getOpenProjects()[0]).getToolWindow("TF Uploader").show(null);
                            }

                        }
                    })
                    .setFadeoutTime(4000)
                    .createBalloon()
                    .show(RelativePoint.getNorthEastOf(statusBar.getComponent()),
                            Balloon.Position.atRight);

        }

    }

    @Override
    public void fileGenerated(String outputRoot, String relativePath) {

    }
}
