package org.interpss.pssl.plugin.cmd;

public class DslRunnerFactory {
	
	
	private static BaseDstabDslRunner dstabDslRunner = null;
	
	public  static AclfDslRunner createAclfDslRunner(){
		return new AclfDslRunner();
	}
	
	public  static AcscDslRunner createAcscDslRunner(){
		return new AcscDslRunner();
	}
	
	
	public static BaseDstabDslRunner createDStabDslRunner(){
		if(dstabDslRunner != null)
			return dstabDslRunner;
		return new BaseDstabDslRunner();
	}
	
	public  static void setDStabDslRunnerCreator(BaseDstabDslRunner newDstabDslRunner){
		dstabDslRunner = newDstabDslRunner;
		
	}

}
