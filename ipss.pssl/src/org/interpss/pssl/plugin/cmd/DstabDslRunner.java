package org.interpss.pssl.plugin.cmd;

import static com.interpss.common.util.IpssLogger.ipssLogger;

import java.io.IOException;

import org.interpss.pssl.plugin.IpssAdapter.FileImportDSL;
import org.interpss.pssl.plugin.cmd.json.AcscRunConfigBean;
import org.interpss.pssl.plugin.cmd.json.BaseJSONBean;
import org.interpss.pssl.plugin.cmd.json.DstabRunConfigBean;
import org.interpss.pssl.simu.IpssDStab;
import org.interpss.util.FileUtil;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.acsc.AcscNetwork;
import com.interpss.dstab.DStabilityNetwork;
import com.interpss.dstab.algo.DynamicSimuMethod;
import com.interpss.dstab.cache.StateMonitor;
import com.interpss.dstab.common.IDStabSimuOutputHandler;
import com.interpss.dstab.devent.DynamicEvent;

public class DstabDslRunner implements BaseDslRunner{
	
	private DStabilityNetwork net;
	private DstabRunConfigBean dstabBean;
	
	/**
	 * constructor
	 * 
	 * @param net DStabilityNetwork object
	 */
	public DstabDslRunner(DStabilityNetwork net) {
		this.net = net;
	}
	
	public DstabDslRunner() {
		// TODO Auto-generated constructor stub
	}

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

	@Override
	public BaseJSONBean loadConfiguraitonBean(String beanFileName) {
		
		try {
			dstabBean = BaseJSONBean.toBean(beanFileName, DstabRunConfigBean.class);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return dstabBean;
	}

	@Override
	public boolean runSimulation() {
		
		
		  // import the file(s)
		FileImportDSL inDsl =  new FileImportDSL();
		inDsl.setFormat(dstabBean.acscConfigBean.runAclfConfig.format)
			 .setPsseVersion(dstabBean.acscConfigBean.runAclfConfig.version)
		     .load(new String[]{dstabBean.acscConfigBean.runAclfConfig.aclfCaseFileName,
		    		 dstabBean.acscConfigBean.seqFileName,
		    		 dstabBean.dynamicFileName});
		
		// map ODM to InterPSS model object
		try {
			net = inDsl.getImportedObj();
		} catch (InterpssException e) {
			e.printStackTrace();
			return false;
		}	
					
		IDStabSimuOutputHandler outputHdler = runDstab(dstabBean);
		
		//output the result
        
		if(!dstabBean.dstabOutputFileName.equals("")){
			FileUtil.write2File(dstabBean.dstabOutputFileName, outputHdler.toString().getBytes());
			ipssLogger.info("Ouput written to " + dstabBean.dstabOutputFileName);
		}
		

		return true;
	}
	

}
