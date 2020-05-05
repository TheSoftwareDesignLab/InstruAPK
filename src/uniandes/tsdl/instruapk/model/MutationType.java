package uniandes.tsdl.instruapk.model;

import java.util.HashMap;
import java.util.Map;

public enum MutationType {
	INSTRUMENTER(1,"INSTRUMENTER");
//	INSTRUMENTER(39,"Instrumenter");

	private final int id;
	private final String name;


	private static Map<Integer, MutationType> map = new HashMap<>();
	
	
	static {
        for (MutationType type : MutationType.values()) {
            map.put(type.getId(), type);
        }
    }

	MutationType(int id, String name){
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public static MutationType valueOf(int typeId) {
        return map.get(typeId);
    }
	
	
}
