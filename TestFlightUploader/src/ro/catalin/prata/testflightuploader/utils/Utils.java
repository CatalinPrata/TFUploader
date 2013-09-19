package ro.catalin.prata.testflightuploader.utils;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;

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
 * Collection of general util methods
 *
 * @author Catalin Prata
 *         Date: 6/18/13
 */
public class Utils {

    /**
     * Checks if the given string is null and if it is, it returns an empty string
     *
     * @param string can be null
     * @return empty string if the string is null, the passed string otherwise
     */
    public static String validateString(String string) {
        if (string == null) {
            return "";
        } else {
            return string;
        }
    }

    /**
     * Used to build a FileChooserDescriptor object with the given FileType and has the following options enabled:
     * - controls whether files can be chosen
     * - controls whether folders can be chosen
     * - controls whether .jar files can be chosen
     * - controls whether .jar files will be returned as files or as folders
     *
     * @param fileType the FileType used for the filtering
     * @return new FileChooserDescriptor
     */
    public static FileChooserDescriptor createSingleFileDescriptor(final FileType fileType) {
        return new FileChooserDescriptor(true, true, true, true, false, false) {
            @Override
            public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
                return file.isDirectory() || file.getFileType() == fileType;
            }

            @Override
            public boolean isFileSelectable(final VirtualFile file) {
                return super.isFileSelectable(file) && file.getFileType() == fileType;
            }
        };
    }

}
