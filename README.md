TFUploader
==========

This is an IntellijIDEA and Android Studio plugin that can be used to upload Android builds to Test Flight in a simple way.
The plugin uses the Upload API to upload builds on Test Flight. See here: https://testflightapp.com/api/doc/ .

V 1.0

The user can:
- save the configuration for the build upload
- add 1 Test Flight api token
- add teams
- add distributions lists for each team
- ability to choose if the server should notify the users from the selected distributions lists
- browse for the project apk file, once found the file will be saved

V 1.5
Added:
- plugin icon
- module support (the apk file path is updated accordingly to the selected module)
- android project version code and name edit
- android.jar dependency so we can get the manifest file of the project

V 2.5
Added:
- bug fixes for Android Studio and gradle projects


TFUploader - simple way to upload Android builds to Test Flight from IntellijIDEA


TFUploader is licensed under Apache V 2.0

/*  
Copyright 2013 Catalin Prata
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
*/
