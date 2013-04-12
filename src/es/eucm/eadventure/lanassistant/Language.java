package es.eucm.eadventure.lanassistant;

import java.util.HashMap;

public class Language {

	private HashMap<String, String> values;
	
	private String identifier;
	
	private String comment;
	
	public Language(String identifier, String comment) {
		this.identifier = identifier;
		this.comment = comment;
		values = new HashMap<String, String>();
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getComment() {
		return comment;
	}
	
	public String getValue(String key) {
		return values.get(key);
	}
	
	public void putValue(String key, String value) {
		values.put(key, value);
	}
}
