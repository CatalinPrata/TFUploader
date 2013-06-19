package ro.catalin.prata.testflightuploader.provider;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import ro.catalin.prata.testflightuploader.Model.CustomMultiPartEntity;

import java.io.File;

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
 * Manages the upload of the build to Test Flight
 *
 * @author Catalin Prata
 *         Date: 6/1/13
 */
public class UploadService implements CustomMultiPartEntity.ProgressListener {

    /**
     * The test flight api url, see doc page https://testflightapp.com/api/doc/
     */
    public static final String TEST_FLIGHT_API_URL = "http://testflightapp.com/api/builds.json";
    public static final String WS_PARAM_API_TOKEN = "api_token";
    public static final String WS_PARAM_TEAM_TOKEN = "team_token";
    public static final String WS_PARAM_NOTES = "notes";
    public static final String WS_PARAM_NOTIFY = "notify";
    public static final String WS_PARAM_FILE = "file";
    public static final String WS_PARAM_DISTRIBUTION_LISTS = "distribution_lists";
    /**
     * Used to notify the status of the upload action
     */
    private UploadServiceDelegate uploadServiceDelegate;

    /**
     * Sends the build to Test Flight
     *
     * @param url        (optional) path to the TestFlight API, where to upload the build
     * @param filePath   (mandatory) path to the apk file that has to be uploaded
     * @param apiToken   (mandatory) user's api token @see <a href="https://testflightapp.com/account/#api">here</a>
     * @param teamToken  (mandatory) team's api token @see <a href="https://testflightapp.com/dashboard/team/edit/?next=/api/doc/">here</a>
     * @param notes      (mandatory) new build information such as what bugs have been fixed or what features were done in this release
     * @param notifyTeam if this is set to true, it notifies the team about this new release
     * @param delegate   callback method used to notify upload action events
     */
    public void sendBuild(final String url, final String filePath, final String apiToken, final String teamToken, final String notes,
                          final String distributionLists, final boolean notifyTeam, UploadServiceDelegate delegate) {

        uploadServiceDelegate = delegate;

        new Thread(new Runnable() {
            @Override
            public void run() {

                String newUrl = url;
                // check if we have the api url from the user and if not, set the default one
                if (newUrl == null) {
                    newUrl = TEST_FLIGHT_API_URL;
                }

                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost method = new HttpPost(newUrl);

                    // get the apk file
                    File fileToUpload = new File(filePath);

                    CustomMultiPartEntity multipartEntity = new CustomMultiPartEntity(UploadService.this);
                    // set the api token
                    multipartEntity.addPart(WS_PARAM_API_TOKEN, new StringBody(apiToken));
                    // set the team token
                    multipartEntity.addPart(WS_PARAM_TEAM_TOKEN, new StringBody(teamToken));
                    // set the notes for the current build
                    multipartEntity.addPart(WS_PARAM_NOTES, new StringBody(notes));

                    if (distributionLists != null && distributionLists.length() > 1) {
                        // set the distributions list for the current build
                        multipartEntity.addPart(WS_PARAM_DISTRIBUTION_LISTS, new StringBody(distributionLists));
                    }

                    if (notifyTeam) {
                        // notify the team about the new update if needed
                        multipartEntity.addPart(WS_PARAM_NOTIFY, new StringBody(String.valueOf(true)));
                    }

                    // add the file too
                    multipartEntity.addPart(WS_PARAM_FILE, new FileBody(fileToUpload));

                    if (uploadServiceDelegate != null){
                        // send the full package size
                        uploadServiceDelegate.onPackageSizeComputed(multipartEntity.getContentLength());
                    }

                    method.setEntity(multipartEntity);

                    // POST the build
                    HttpResponse response = client.execute(method);

//                    System.out.println("Status code:" + response.getStatusLine().getStatusCode());
//                    System.out.println("Status message:" + response.getStatusLine().getReasonPhrase());

                    if (response.getStatusLine().getStatusCode() == 200) {
                        // if the build was successfully uploaded, inform the View
//                        System.out.println("Response: " + EntityUtils.toString(response.getEntity()));
                        if (uploadServiceDelegate != null) {
                            // send success upload status
                            uploadServiceDelegate.onUploadFinished(true);
                        }

                    } else {

                        if (uploadServiceDelegate != null) {
                            // send failed upload status
                            uploadServiceDelegate.onUploadFinished(false);
                        }

                    }

                } catch (Exception e) {
                    // Ups! error occurred
                    e.printStackTrace();

                    if (uploadServiceDelegate != null) {
                        // send failed upload status
                        uploadServiceDelegate.onUploadFinished(false);
                    }
                }

            }
        }).start();

    }

    @Override
    public void transferred(long num) {

        if (uploadServiceDelegate != null){
            uploadServiceDelegate.onProgressChanged(num);
        }

    }

    /**
     * Upload service callback interface used to notify uploading actions like status or progress
     */
    public interface UploadServiceDelegate {

        /**
         * Called when the upload is done, even if an error occurred
         *
         * @param finishedSuccessful this flag is true if the upload was made successfully, false otherwise
         */
        public void onUploadFinished(boolean finishedSuccessful);

        public void onPackageSizeComputed(long totalSize);

        public void onProgressChanged(long progress);

    }

}
