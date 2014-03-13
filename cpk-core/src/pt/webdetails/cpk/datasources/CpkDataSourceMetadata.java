/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cpk.datasources;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CpkDataSourceMetadata extends DataSourceMetadata {

    @JsonProperty("pluginId")
    private String pluginId;

    @JsonProperty("endpoint")
    private String endpointName;

    /**
     * @param pluginId the pluginId to set
     */
    protected void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * @return the pluginId
     */
    public String getPluginId() {
        return pluginId;
    }

    public CpkDataSourceMetadata(String pluginId, String endpointName) {
        setName(String.format("%s Endpoint", endpointName));
        setPluginId(pluginId);
        setEndpointName(endpointName);
    /*
     *  which data type should be declared?
     *  is it plugin implementation dependable?
     */
        setDataType("cpk");
        setGroup(String.format("%s_CPKENDPOINTS", pluginId.toUpperCase()));
        setGroupDescription(String.format("%s Endpoints", pluginId.toUpperCase()));
    }

    /**
     * @param endpointName the endpointName to set
     */
    protected void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

}
