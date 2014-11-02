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

public abstract class BaseDstabDslRunner implements BaseDslRunner{
	
	protected DStabilityNetwork net;
	protected DstabRunConfigBean dstabBean;
	


	/**
	 * A common interface for all DSLRunner. Any DStab DSLRunner should override it.
	 * 
	 * @param dstabConfigBean
	 * @return
	 */
	public abstract IDStabSimuOutputHandler runDstab (DstabRunConfigBean dstabConfigBean);

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
