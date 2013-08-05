/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cpk.elements.impl.kettleOutputs;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.row.RowMetaInterface;
import pt.webdetails.cpk.elements.impl.KettleElementType;

/**
 *
 * @author Pedro Alves<pedro.alves@webdetails.pt>
 */
public interface IKettleOutput {

    public void processResult();

    public boolean needsRowListener();

    public void storeRow(Object[] row, RowMetaInterface rowMeta);

    public void setResult(Result r);

    public Result getResult();

    public KettleElementType.KettleType getKettleType();

    public void setKettleType(KettleElementType.KettleType kettleType);
    
    public void setOutputStepName(String stepName);
    
    public String getOutputStepName();
}
