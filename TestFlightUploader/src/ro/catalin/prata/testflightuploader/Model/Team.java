package ro.catalin.prata.testflightuploader.Model;

import java.io.Serializable;

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
 * Encapsulates a Team entity having a name and token, used to store teams and send builds for the selected team
 *
 * @author Catalin Prata
 *         Date: 6/3/13
 */
public class Team implements Serializable {

    /**
     * The team name, given by the user
     */
    public String name;
    /**
     * The team token given by the user but from the TestFlight site
     */
    public String token;
    /**
     * Team distribution list
     */
    public String distributionList;

    /**
     * Default empty constructor
     */
    public Team() {
    }

    /**
     * Returns the team name
     *
     * @return team name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the team name
     *
     * @param name team name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the team token
     *
     * @return team token
     */
    public String getToken() {
        return token;
    }

    /**
     * Set the team token
     *
     * @param token team token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Test Flight distribution list
     *
     * @return distributions list separated by coma
     */
    public String getDistributionList() {
        return distributionList;
    }

    /**
     * Set the distributions list separated by coma
     *
     * @param distributionList distributions list
     */
    public void setDistributionList(String distributionList) {
        this.distributionList = distributionList;
    }
}
