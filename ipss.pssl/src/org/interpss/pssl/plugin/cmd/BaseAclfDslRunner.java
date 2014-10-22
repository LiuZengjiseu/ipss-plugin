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
import static org.interpss.CorePluginFunction.aclfResultSummary;
import static org.interpss.mapper.odm.ODMUnitHelper.toApparentPowerUnit;
import static org.interpss.pssl.plugin.IpssAdapter.importAclfNet;

import java.io.IOException;

import org.ieee.odm.schema.IpssAclfAlgorithmXmlType;
import org.ieee.odm.schema.LfMethodEnumType;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.pssl.plugin.IpssAdapter.FileImportDSL;
import org.interpss.pssl.plugin.cmd.json.AclfRunConfigBean;
import org.interpss.pssl.plugin.cmd.json.BaseJSONBean;
import org.interpss.pssl.simu.IpssAclf;
import org.interpss.pssl.simu.IpssAclf.LfAlgoDSL;
import org.interpss.util.FileUtil;

import com.interpss.common.exp.InterpssException;
import com.interpss.common.util.StringUtil;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.aclf.BaseAclfNetwork;
import com.interpss.core.algo.AclfMethod;

/**
 * Run aclf DSL using ODM case definition
 * 
 * @author mzhou
 *
 */
public class BaseAclfDslRunner implements BaseDslRunner{
	private BaseAclfNetwork<?,?> net;
	private AclfRunConfigBean aclfBean = null;
	
	/**
	 * constructor
	 * 
	 * @param net AclfNetwork object
	 */
	public BaseAclfDslRunner(BaseAclfNetwork<?,?> net) {
		this.net = net;
	}
	
	public BaseAclfDslRunner() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * run aclf using the ODM case definition
	 * 
	 * @param algoXml
	 * @return
	 * @throws InterpssException 
	 */
	public boolean runAclf(IpssAclfAlgorithmXmlType algoXml) throws InterpssException {
		LfAlgoDSL algoDsl = IpssAclf.createAclfAlgo(net);
		
		algoDsl.lfMethod(algoXml.getLfMethod() == LfMethodEnumType.NR ? AclfMethod.NR
					: (algoXml.getLfMethod() == LfMethodEnumType.PQ ? AclfMethod.PQ
							: (algoXml.getLfMethod() == LfMethodEnumType.CUSTOM ? AclfMethod.CUSTOM 
									: AclfMethod.GS)));
		
		if (algoXml.getMaxIterations() > 0)
			algoDsl.setMaxIterations(algoXml.getMaxIterations());
		
		if (algoXml.getTolerance() != null)
			algoDsl.setTolerance(algoXml.getTolerance().getValue(), 
					toApparentPowerUnit.apply(algoXml.getTolerance().getUnit()));
		
		if (algoXml.isNonDivergent() != null)
			algoDsl.nonDivergent(algoXml.isNonDivergent());

		if (algoXml.isInitBusVoltage() != null)
			algoDsl.initBusVoltage(algoXml.isInitBusVoltage());

		if (algoXml.getAccFactor() != null)
			algoDsl.gsAccFactor(algoXml.getAccFactor());		
		
		return algoDsl.runLoadflow();	
	}
	
	/**
	 * run aclf using the JSON case definition. 
	 * This is also the interface that a customized Aclf DslRunner should override
	 * 
	 * @param aclfConfigBean
	 * @return
	 * @throws InterpssException 
	 */
	public boolean runAclf(AclfRunConfigBean aclfConfigBean) throws InterpssException {
		LfAlgoDSL algoDsl = IpssAclf.createAclfAlgo(net);
		
		// if algoBean is null, run with the default setting
		if(aclfConfigBean !=null){
			algoDsl.lfMethod(aclfConfigBean.lfMethod == LfMethodEnumType.NR ? AclfMethod.NR
						: (aclfConfigBean.lfMethod == LfMethodEnumType.PQ ? AclfMethod.PQ
								: (aclfConfigBean.lfMethod == LfMethodEnumType.CUSTOM ? AclfMethod.CUSTOM 
										: AclfMethod.GS)));
			
			algoDsl.setMaxIterations(aclfConfigBean.maxIteration);
			
			algoDsl.setTolerance(aclfConfigBean.tolerance, UnitType.PU);
			
			algoDsl.nonDivergent(aclfConfigBean.nonDivergent);
	
			algoDsl.initBusVoltage(aclfConfigBean.initBusVoltage);
	
			algoDsl.gsAccFactor(aclfConfigBean.accFactor);
		}
		
		return algoDsl.runLoadflow();	
	}

	@Override
	public BaseJSONBean loadConfiguraitonBean(String beanFileName) {
		try {
			aclfBean = BaseJSONBean.toBean(beanFileName, AclfRunConfigBean.class);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return aclfBean;
	}

	@Override
	public boolean runSimulation() {
		
		// load the study case file
		FileImportDSL inDsl = importAclfNet(aclfBean.aclfCaseFileName)
				.setFormat(aclfBean.format)
				.setPsseVersion(aclfBean.version)
				.load();	

		try {
			
			// map ODM to InterPSS model object
			net = inDsl.getImportedObj();
			
			// run loadflow 
		      runAclf(aclfBean);
		} catch (InterpssException e1) {
		
			e1.printStackTrace();
			return false;
		}	
		
		// output Loadflow result
		FileUtil.write2File(aclfBean.aclfOutputFileName, aclfResultSummary.apply(net).toString().getBytes());
		ipssLogger.info("Ouput written to " + aclfBean.aclfOutputFileName);

	
		return true;
	}
}
