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
import pt.webdetails.cpk.elements.IMetadata;

public class DataSource {

    private DataSourceDefinition definition;
    private IMetadata metadata;

    public DataSource() {
    }

    /**
     * @return the definition
     */
    @JsonProperty("definition")
    public DataSourceDefinition getDefinition() {
        return definition;
    }

    /**
     * @return the metadata
     */
    @JsonProperty("metadata")
    public IMetadata getMetadata() {
        return metadata;
    }

    /**
     * @param definition the definition to set
     */
    public DataSource setDefinition(DataSourceDefinition definition) {
        this.definition = definition;
        return this;
    }

    /**
     * @param metadata the metadata to set
     */
    public DataSource setMetadata(IMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

}
