/*
  * @(#)AclfDslODMRunner.java   
  *
  * Copyright (C) 2006-2011 www.interpss.com
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU LESSER GENERAL PUBLIC LICENSE
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * @Author Mike Zhou
  * @Version 1.0
  * @Date 12/15/2011
  * 
  *   Revision History
  *   ================
  *
  */

package org.interpss.pssl.plugin.cmd;

import static com.interpss.common.util.IpssLogger.ipssLogger;

import java.io.IOException;

import org.apache.commons.math3.complex.Complex;
import org.ieee.odm.schema.AcscBaseFaultXmlType;
import org.ieee.odm.schema.AcscBranchFaultXmlType;
import org.ieee.odm.schema.AcscBusFaultXmlType;
import org.ieee.odm.schema.AcscFaultAnalysisXmlType;
import org.ieee.odm.schema.AcscFaultCategoryEnumType;
import org.ieee.odm.schema.AcscFaultTypeEnumType;
import org.ieee.odm.schema.ComplexXmlType;
import org.interpss.pssl.plugin.IpssAdapter.FileImportDSL;
import org.interpss.pssl.plugin.cmd.json.AclfRunConfigBean;
import org.interpss.pssl.plugin.cmd.json.AcscRunConfigBean;
import org.interpss.pssl.plugin.cmd.json.BaseJSONBean;
import org.interpss.pssl.simu.IpssAcsc;
import org.interpss.pssl.simu.IpssAcsc.FaultAlgoDSL;
import org.interpss.util.FileUtil;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.acsc.AcscNetwork;
import com.interpss.core.acsc.BaseAcscNetwork;
import com.interpss.core.acsc.fault.SimpleFaultCode;
import com.interpss.core.acsc.fault.SimpleFaultType;
import com.interpss.core.datatype.IFaultResult;

/**
 * Acsc Dsl ODM runner implementation
 * 
 * @author mzhou
 *
 */
public class AcscDslRunner implements BaseDslRunner{
	private BaseAcscNetwork<?,?> net;
	private AcscRunConfigBean acscBean;
	
	/**
	 * constructor
	 * 
	 * @param net AcscNetwork object
	 */
	public AcscDslRunner(BaseAcscNetwork<?,?> net) {
		this.net = net;
	}
	
	public AcscDslRunner() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * run the acsc analysis case and return the analysis results
	 * 
	 * @param acscCaseXml
	 * @return
	 */
	public IFaultResult runAcsc(AcscFaultAnalysisXmlType acscCaseXml)  throws InterpssException {
  		FaultAlgoDSL algo = IpssAcsc.createAcscAlgo(this.net);
  		
		AcscBaseFaultXmlType faultXml = acscCaseXml.getAcscFault().getValue();
  		if (faultXml.getFaultType() == AcscFaultTypeEnumType.BUS_FAULT) {
  			AcscBusFaultXmlType busFault = (AcscBusFaultXmlType)faultXml;
  	  		algo.createBusFault(busFault.getRefBus().getBusId())
  	  			.faultCode(mapCode(busFault.getFaultCategory()))
  	  			.zLGFault(toComplex(busFault.getZLG()))
  	  			.zLLFault(toComplex(busFault.getZLL()))
  	  			.calculateFault();
  	  		return algo.getResult();
  		}
  		else if (faultXml.getFaultType() == AcscFaultTypeEnumType.BRANCH_FAULT) {
  			AcscBranchFaultXmlType braFault = (AcscBranchFaultXmlType)faultXml;
  	  		algo.createBranchFault(braFault.getRefBranch().getFromBusId(), braFault.getRefBranch().getToBusId(), braFault.getRefBranch().getCircuitId())
  	  			.faultCode(mapCode(braFault.getFaultCategory()))
  	  			.zLGFault(toComplex(braFault.getZLG()))
  	  			.zLLFault(toComplex(braFault.getZLL()))
  	  			.distance(braFault.getDistance())
  	  			.calculateFault();
  	  		return algo.getResult();
  		}
  		else if (faultXml.getFaultType() == AcscFaultTypeEnumType.BRANCH_OUTAGE) {
  			// TODO
  		}
		return null;
	}
	
	/**
	 * run the acsc analysis case and return the analysis results
	 * 
	 * @param acscConfigBean
	 * @return
	 */
	public IFaultResult runAcsc(AcscRunConfigBean acscBean)  throws InterpssException {

		FaultAlgoDSL algo = IpssAcsc.createAcscAlgo(this.net);

		if (acscBean.type==SimpleFaultType.BUS_FAULT) {
			
	  		algo.createBusFault(acscBean.faultBusId)
	  			.faultCode(acscBean.category)
	  			.zLGFault(acscBean.zLG.toComplex())
	  			.zLLFault(acscBean.zLL.toComplex())
	  			.calculateFault();
	  		return algo.getResult();
		}
		else if (acscBean.type==SimpleFaultType.BRANCH_FAULT) {
			
	  		algo.createBranchFault(acscBean.faultBranchFromId, acscBean.faultBranchToId, acscBean.faultBranchCirId)
	  			.faultCode(acscBean.category)
	  			.zLGFault(acscBean.zLG.toComplex())
	  			.zLLFault(acscBean.zLL.toComplex())
	  			.distance(acscBean.distance)
	  			.calculateFault();
	  		return algo.getResult();
		}
		
	return null;
	}
	
	private SimpleFaultCode mapCode(AcscFaultCategoryEnumType caty) {
		return caty == AcscFaultCategoryEnumType.FAULT_3_PHASE ? SimpleFaultCode.GROUND_3P : 
					(caty == AcscFaultCategoryEnumType.LINE_LINE_TO_GROUND ? SimpleFaultCode.GROUND_LLG :
						(caty == AcscFaultCategoryEnumType.LINE_TO_GROUND ? 
								SimpleFaultCode.GROUND_LG : SimpleFaultCode.GROUND_LL));
		
	}
	
	private Complex toComplex(ComplexXmlType x) {
		return new Complex(x.getRe(), x.getIm());
	}

	@Override
	public BaseJSONBean loadConfiguraitonBean(String beanFileName) {
		try {
			acscBean = BaseJSONBean.toBean(beanFileName, AcscRunConfigBean.class);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return acscBean;
	}

	@Override
	public boolean runSimulation() {
		 // import the file(s)
		FileImportDSL inDsl =  new FileImportDSL();
		inDsl.setFormat(acscBean.runAclfConfig.format)
			 .setPsseVersion(acscBean.runAclfConfig.version)
		     .load(new String[]{acscBean.runAclfConfig.aclfCaseFileName,
				acscBean.seqFileName});
		
		// map ODM to InterPSS model object
		IFaultResult scResults = null;
		try {
			net = inDsl.getImportedObj();
			scResults = runAcsc(acscBean);
		} catch (InterpssException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		// output short circuit result
		
		// require the base votlage of the fault point
		double baseV = acscBean.type == SimpleFaultType.BUS_FAULT? net.getBus(acscBean.faultBusId).getBaseVoltage():
			                                  net.getBus(acscBean.faultBranchFromId).getBaseVoltage();
			
		FileUtil.write2File(acscBean.acscOutputFileName, scResults.toString(baseV).getBytes());
		ipssLogger.info("Ouput written to " + acscBean.acscOutputFileName);
		
		return true;
	}
}
