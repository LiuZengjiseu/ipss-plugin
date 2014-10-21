package org.interpss.pssl.plugin.cmd;

import org.interpss.pssl.plugin.cmd.json.BaseJSONBean;

public interface BaseDslRunner {
	
	
	public BaseJSONBean loadConfiguraitonBean(String beanFileName);
	
	public boolean runSimulation();
	
	
	

}
