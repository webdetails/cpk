/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cpk.elements.impl;


import pt.webdetails.cpf.Util;
import pt.webdetails.cpk.elements.Element;
import pt.webdetails.cpk.elements.impl.kettleOutputs.IKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleOutputs.KettleOutput;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Constructor;
import java.util.Map;

public abstract class KettleElement extends Element {

  protected static final String DEFAULT_STEP = "OUTPUT";
  protected static final String KETTLEOUTPUT_CLASSES_NAMESPACE = "pt.webdetails.cpk.elements.impl.kettleOutputs";

  protected IKettleOutput inferResult( Map<String, Map<String, Object>> bloatedMap ) {

        /*
         *  There are a few different types of kettle output processing.
         *  They can be infered or specified from a request parameter: kettleOutput
         *
         *  1. ResultOnly - we'll discard the output and print statistics only
         *  2. ResultFiles - Download the files we have as result filenames
         *  3. Json - Json output of the resultset
         *  4. csv - CSV output of the resultset
         *  5. SingleCell - We'll get the first line, first row
         *  6. Infered - Infering
         *
         *  If nothing specified, the behavior will be:
         *  * Jobs and Transformations with result filenames: ResultFiles
         *  * Without filenames:
         *      * Jobs: ResultOnly
         *      * Transformations:
         *          * Just one cell: SingleCell
         *          * Regular resultset: Json
         *
         *  By complexity:
         *      These don't require rowListener:
         *  1. ResultOnly
         *  2. ResultFiles
         *
         *      These do:
         *  3. SingleCell
         *  4. Json
         *  5. CSV
         *  6. Infered
         */

    //These conditions will treat the different types of kettle operations

    // Get info from bloatedmap
    String kettleOutputType = (String) bloatedMap.get( "request" ).get( "kettleOutput" );
    // Are we specifying a stepname?
    String stepName = (String) bloatedMap.get( "request" ).get( "stepName" );
    String downloadStr = (String) bloatedMap.get( "request" ).get( "download" );
    boolean download = Boolean.parseBoolean( downloadStr != null ? downloadStr : "false" );
    HttpServletResponse httpResponse = (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" );

    // Get kettleOutput
    return this.inferResult( kettleOutputType, stepName, download, httpResponse );
  }

  protected IKettleOutput inferResult( String kettleOutputType, String stepName, boolean download
    , HttpServletResponse httpResponse ) {

    if ( kettleOutputType == null || kettleOutputType.isEmpty() ) {
      kettleOutputType = "Infered";
    }

    if ( stepName == null || stepName.isEmpty() ) {
      stepName = DEFAULT_STEP;
    }

    String clazz = kettleOutputType + "KettleOutput";

    IKettleOutput kettleOutput;
    try {
      // Get defined kettleOutput class name
      Constructor constructor = Class.forName( KETTLEOUTPUT_CLASSES_NAMESPACE + "." + clazz )
        .getConstructor( HttpServletResponse.class, boolean.class );
      kettleOutput = (IKettleOutput) constructor.newInstance( httpResponse, download );

    } catch ( Exception ex ) {
      logger.error( "Error initializing Kettle output type " + clazz + ", reverting to KettleOutput: " + Util
        .getExceptionDescription( ex ) );
      kettleOutput = new KettleOutput( httpResponse, download );
    }

    kettleOutput.setOutputStepName( stepName );

    return kettleOutput;
  }

}
