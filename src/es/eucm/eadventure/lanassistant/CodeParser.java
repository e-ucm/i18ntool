package es.eucm.eadventure.lanassistant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class CodeParser {
	
	public static void parseCode(String proyectFolder, LangManager langManager) {
		if (proyectFolder == null)
			return;
		parseFolder(proyectFolder, langManager);
	}
	
	private static void parseFolder(String folder,
			LangManager langManager) {
		File proyect = new File(folder);
		for (File file : proyect.listFiles()) {
			if (file.isDirectory())
				parseFolder(file.getAbsolutePath(), langManager);
			else if (file.getAbsolutePath().endsWith("java"))
				parseFile(file, langManager);
		}
	}
	
	private static void parseFile(File file,
			LangManager langManager) {
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String enconding = isr.getEncoding();
			MatchList list = new MatchList();

			int counter = 0;
			
			List<String> lines = new ArrayList<String>();
			boolean changed = false;
			for (String temp = br.readLine(); temp != null; temp = br.readLine()) {
				while (temp.indexOf("\"@@") != -1) {
					int index = temp.indexOf("\"@@");
					int last = temp.indexOf('\"', index + 1) + 1;
					String value = temp.substring(index, last);
					int index1 = value.indexOf("$$");
					int index2 = value.indexOf("$$", index1 + 2);
					if (index1 > -1 && index2 > -1) {
						String key = value.substring(3, index1);
						String es = "";
						if (index2 > index1 + 2)
							es = value.substring(index1 + 2, index2);
						String en = "";
						if (value.length() - 1 > index2 + 2)
							en = value.substring(index2 + 2, value.length() - 1);
						temp = temp.substring(0, index) + "TC.get(\"" + key + "\")" + temp.substring(index + value.length(), temp.length());
						langManager.addParsedKey(key);
						langManager.setKeyValue(key, "es_ES", es);
						langManager.setKeyValue(key, "en_EN", en);
						changed = true;
						list.addMatch(new Match(key, es, en, counter + index));
					}
				}
				counter += temp.length() + 1;//+ 2;
				lines.add(temp);
			}
			br.close();
			isr.close();
			fis.close();
			
			if (changed) {
				fis = new FileInputStream(file);
				isr = new InputStreamReader(fis);
				
				int len = (int) file.length();
				char[] buf = new char[len];
				isr.read(buf, 0, len);

				isr.close();
				fis.close();

				char[] newBuff = new char[len + list.diff];
				

				int temp = 0;
				int diff = 0;
				while (temp < len + list.diff) {
					int newIndex = list.nextIndex(temp, len);
					for (int i = temp; i < newIndex; i++) {
						newBuff[i] = buf[i - diff];
					}
					temp = newIndex;
					Match match = list.getMatch(temp);
					if (match != null) {
						String newString = "TC.get(\"" + match.key + "\")";
						for (int i = newIndex; i < newIndex + newString.length(); i++) {
							newBuff[i] = newString.charAt(i - newIndex);
						}
						diff -= match.english.length() + match.spanish.length() - 2;
						temp += newString.length();
					}
				}
				
				FileOutputStream fos = new FileOutputStream(file);
				OutputStreamWriter fow = new OutputStreamWriter(fos);

				fow.write(newBuff, 0, len + list.diff);
				fow.flush();
				
				fow.close();
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static class Match {
		String key;
		String english;
		String spanish;
		int index;
		
		public Match(String key, String spanish, String english, int index) {
			this.key = key;
			this.spanish = spanish;
			this.english = english;
			this.index = index;
		}
	}
	
	private static class MatchList {
		List<Match> list = new ArrayList<Match>();
		int diff;
		public MatchList() {
			diff = 0;
		}
		
		public void addMatch(Match match) {
			list.add(match);
			match.index = match.index;
			diff -= match.spanish.length() + match.english.length() - 2;
		}
		
		public int nextIndex(int index, int len) {
			int nextIndex = len + diff;
			for (Match match : list) {
				if (match.index > index && match.index < nextIndex)
					nextIndex = match.index;
			}
			return nextIndex;
		}
		
		public Match getMatch(int index) {
			for (Match match : list) {
				if (match.index == index)
					return match;
			}
			return null;
		}
	}

}
