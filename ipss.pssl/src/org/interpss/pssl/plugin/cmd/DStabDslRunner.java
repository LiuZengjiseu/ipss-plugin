package org.interpss.pssl.plugin.cmd;

import org.interpss.pssl.plugin.cmd.json.DstabRunConfigBean;
import org.interpss.pssl.simu.IpssDStab;

import com.interpss.dstab.DStabilityNetwork;
import com.interpss.dstab.cache.StateMonitor;
import com.interpss.dstab.common.IDStabSimuOutputHandler;

public class DStabDslRunner extends BaseDstabDslRunner {
	
	
	/**
	 * constructor
	 * 
	 * @param net DStabilityNetwork object
	 */
	public DStabDslRunner(DStabilityNetwork net) {
		this.net = net;
	}
	
	public DStabDslRunner() {
		
	}

	/**
	 * A common interface for all DSLRunner. Any DStab DSLRunner should override it.
	 * 
	 * @param dstabConfigBean
	 * @return
	 */
	public IDStabSimuOutputHandler runDstab (DstabRunConfigBean dstabConfigBean){
		
		IpssDStab dstabDSL = new IpssDStab(net);
		
		
		dstabDSL.setTotalSimuTimeSec(dstabConfigBean.totalSimuTimeSec)
		        .setSimuTimeStep(dstabConfigBean.simuTimeStepSec)
		        .setIntegrationMethod(dstabConfigBean.method)
		        .setRefMachine(dstabConfigBean.referenceGeneratorId);
		
		
		StateMonitor sm = new StateMonitor();
		sm.addBusStdMonitor(dstabConfigBean.monitoringBusAry);
		sm.addGeneratorStdMonitor(dstabConfigBean.monitoringGenAry);
		
		// set the output handler
		dstabDSL.setDynSimuOutputHandler(sm)
		        .setSimuOutputPerNSteps(dstabConfigBean.outputPerNSteps);
		
		dstabDSL.addBusFaultEvent(dstabConfigBean.acscConfigBean.faultBusId,  
				                                              dstabConfigBean.acscConfigBean.category, 
											                  dstabConfigBean.eventStartTimeSec,
											                  dstabConfigBean.eventDurationSec, 
											                  dstabConfigBean.acscConfigBean.zLG.toComplex(), 
											                  dstabConfigBean.acscConfigBean.zLL.toComplex()); 
				                   
		
		if(dstabDSL.initialize()){
			if( dstabDSL.runDStab())
				return dstabDSL.getOutputHandler();
		}
		
		return null;
	}

}
