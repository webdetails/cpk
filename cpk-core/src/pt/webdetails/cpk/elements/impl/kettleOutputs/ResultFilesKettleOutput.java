/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk.elements.impl.kettleOutputs;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.vfs.FileSystemException;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.utils.IPluginUtils;

/**
 *
 * @author Pedro Alves<pedro.alves@webdetails.pt>
 */
public class ResultFilesKettleOutput extends KettleOutput {

    public ResultFilesKettleOutput(Map<String, ICommonParameterProvider> parameterProviders,IPluginUtils plug) {
        super(parameterProviders,plug);
    }

    @Override
    public boolean needsRowListener() {
        return false;
    }

    @Override
    public void processResult() {
        try {
            super.processResultFiles();
        } catch (FileSystemException ex) {
            Logger.getLogger(ResultFilesKettleOutput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
