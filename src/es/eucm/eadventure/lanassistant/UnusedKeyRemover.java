package es.eucm.eadventure.lanassistant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UnusedKeyRemover {
	
	public static int unusedKeyRemover(String proyectFolder, LangManager langManager) {
		if (proyectFolder == null)
			return 0;
		return parseFolder(proyectFolder, langManager);
	}
	
	private static int parseFolder(String folder,
			LangManager langManager) {
		List<String> keysToRemove = new ArrayList<String>();
		for (int i = 0; i < langManager.getKeyCount(); i++) {
			String key = langManager.getKey(i);
			while (key.matches("[a-zA-Z.]*[0-9]+") && key.length() > 3) {
				key = key.substring(0, key.length() - 1);
			}
			if (keyUsed(key, folder))
				System.out.println("key " + langManager.getKey(i) + "(" + key + ") used");
			else {
				System.out.println("key " + langManager.getKey(i) + "(" + key + ") NOT used");
//				int option = JOptionPane.showConfirmDialog(null, "Key " + langManager.getKey(i) + " unused", "Unused key", JOptionPane.YES_NO_OPTION);
//				if (option == JOptionPane.YES_OPTION)
					keysToRemove.add(langManager.getKey(i));
			}
		}
		
		new UnusedKeysFrame(keysToRemove, langManager);

//		for (String key : keysToRemove)
//			langManager.removeKey(key);
		return keysToRemove.size();
	}
	
	private static boolean keyUsed(String key, String fileName) {
		File file = new File(fileName);
		
		if (file.isDirectory()) {
			for (File f : file.listFiles())
				if (keyUsed(key, f.getAbsolutePath()))
					return true;
			return false;
		}
		else if (file.getAbsolutePath().endsWith("java"))
			return parseFile(file, key);
		
		return false;
	}
	
	private static boolean parseFile(File file, String key) {
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			isr.getEncoding();
			
			for (String temp = br.readLine(); temp != null; temp = br.readLine()) {
				if (temp.contains(key))
					return true;
			}
			
			br.close();
			isr.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
		
	}
	

}
