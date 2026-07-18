/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cpk.elements.impl.kettleoutputs;

import pt.webdetails.cpk.elements.impl.KettleResult;

public interface IKettleOutput {

  public void processResult( KettleResult result );

}
