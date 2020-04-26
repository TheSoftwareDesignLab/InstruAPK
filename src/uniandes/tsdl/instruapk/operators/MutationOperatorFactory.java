package uniandes.tsdl.instruapk.operators;

import java.util.ResourceBundle;

public class MutationOperatorFactory {

	private static MutationOperatorFactory instance = null;
	private static ResourceBundle types = null;
	
	protected  MutationOperatorFactory(){
		types = ResourceBundle.getBundle("uniandes.tsdl.instruapk.operator-types");
	}
	
	public static MutationOperatorFactory getInstance() {
	      if(instance == null) {
	         instance = new MutationOperatorFactory();
	      }
	      return instance;
	}
	
	
	public MutationOperator getOperator(int code){
		
		try{
			return (MutationOperator)Class.forName(types.getString(code+"")).newInstance();
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
		
		
	}

}
