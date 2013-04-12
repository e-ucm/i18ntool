package es.eucm.eadventure.lanassistant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

public class LangManager {

	private List<String> allKeys;
	
	private List<String> keys;
	
	private List<Language> langs;
	
	private File directory;
	
	private List<String> modifiedKeys;
	
	private List<String> parsedKeys;
	
	public LangManager() {
		this.allKeys = new ArrayList<String>();
		this.keys = allKeys;
		this.langs = new ArrayList<Language>();
		this.modifiedKeys = new ArrayList<String>();
		this.parsedKeys = new ArrayList<String>();
	}
	
	public LangManager(String string) {
		this();
		directory = new File(string);
		for (File file : directory.listFiles()) {
			if (file.getName().endsWith("xml")) {
				String identifier = file.getName().substring(0, file.getName().length() - 4);
				
				Properties prop = new Properties();
				try {
					prop.loadFromXML(new FileInputStream(file));
					//TODO: get comment from XML file
					this.addLanguage(identifier, "comment");
		            for( Object key : prop.keySet( ) ) {
		            	if (!allKeys.contains(key))
		            		allKeys.add((String) key);
		                this.setKeyValue((String) key, identifier, (String) prop.get(key));
		            }
				} catch (InvalidPropertiesFormatException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		modifiedKeys.clear();
		parsedKeys.clear();
		Collections.sort(allKeys);
	}
	
	public void update(String string) {
		directory = new File(string);
		List<String> newKeys = new ArrayList<String>();
		for (File file : directory.listFiles()) {
			if (file.getName().endsWith("xml")) {
				String identifier = file.getName().substring(0, file.getName().length() - 4);
				
				Properties prop = new Properties();
				try {
					prop.loadFromXML(new FileInputStream(file));
					//TODO: get comment from XML file
					this.addLanguage(identifier, "comment");
		            for( Object key : prop.keySet( ) ) {
		            	if (!allKeys.contains(key))
		            		allKeys.add((String) key);
		            	if (!modifiedKeys.contains((String) key)) {
		            		this.setKeyValue((String) key, identifier, (String) prop.get(key));
		            		modifiedKeys.remove(key);
		            		newKeys.add((String) key);
		            	}
		            }
				} catch (InvalidPropertiesFormatException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		for (String key : allKeys) {
			if (!newKeys.contains(key) && !modifiedKeys.contains(key) && !parsedKeys.contains(key))
				allKeys.remove(key);
		}
		Collections.sort(allKeys);
		keys = allKeys;
	}

	public int addKey(String key) {
		this.allKeys.add(key);
		Collections.sort(allKeys);
		this.modifiedKeys.add(key);
		return allKeys.indexOf(key);
	}
	
	public boolean addLanguage(String identifier, String comment) {
		boolean exists = false;
		for (Language lang : langs)
			if (lang.getIdentifier().equals(identifier))
				exists = true;
		if (!exists)
			langs.add(new Language(identifier, comment));
		return !exists;
	}
	
	public void setKeyValue(String key, String identifier, String value) {
		for (Language lang : langs)
			if (lang.getIdentifier().equals(identifier))
				lang.putValue(key, value);
		this.modifiedKeys.add(key);
	}

	public int getKeyCount() {
		return keys.size();
	}

	public String getKey(int index) {
		return keys.get(index);
	}
	
	public String getValue(String key, String identifier) {
		for (Language lang : langs)
			if (lang.getIdentifier().equals(identifier))
				return lang.getValue(key);
		return null;
	}
	
	public String[] getLangIDs() {
		String[] langIDs = new String[langs.size()];
		for(int i = 0; i < langs.size(); i++)
			langIDs[i] = langs.get(i).getIdentifier();
		return langIDs;
	}

	public void saveChanges() {
		for (Language lang : langs) {
			Properties prop = new Properties();
			for (String key : allKeys)
				if (lang.getValue(key) != null)
					prop.setProperty(key, lang.getValue(key));
			try {
				prop.storeToXML(new FileOutputStream(new File(directory.getAbsoluteFile() + File.separator + lang.getIdentifier() + ".xml")), lang.getComment());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void search(String text) {
		if (text == null || text.equals(""))
			keys = allKeys;
		else {
			keys = new ArrayList<String>();
			for (String key : allKeys) {
				if (key.toLowerCase().contains(text.toLowerCase()))
					keys.add(key);
				for (Language lang : langs) {
					if (lang.getValue(key) != null && !keys.contains(keys) && lang.getValue(key).toLowerCase().contains(text.toLowerCase()))
						keys.add(key);
				}
			}
		}
	}

	public boolean isModifiedKey(String key) {
		return modifiedKeys.contains(key);
	}

	public void addParsedKey(String key) {
		allKeys.add(key);
		parsedKeys.add(key);
		Collections.sort(allKeys);
		keys = allKeys;
	}

	public void removeKey(String key) {
		allKeys.remove(key);
	}
}
