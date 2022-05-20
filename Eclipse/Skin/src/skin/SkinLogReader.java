package skin;

public class SkinLogReader {

//	char[] fileDataArray;
//	BufferedReader logFile;
//	String logFilePath = "Log_" + month() + "_11/" +"10-31-53.hx";
	
//	void Setup()
//	{
//
//		logFile = createReader(logFilePath);
//
//		List<String> fileData = new ArrayList<String>();
//		try {
//			fileData = Files.readAllLines(Paths.get("D:\\Users\\aLocks\\Desktop\\UT\\MucaRendererReader\\Log_5 _11\\10-31-53.hx"));
//		}
//		catch(IOException e) {
//			println(e);
//		}
//
//		println("Fichier lu");
//
//		String fileAllLines = new String();
//
//		Iterator<String> it = fileData.iterator();
//		while (it.hasNext()) 
//			fileAllLines += it.next() + '\n';
//
//		println("Concatenation des lignes");
//		fileDataArray = fileAllLines.toCharArray();
//		println("Taille des donnees : " + fileData.size());
//	}

//	int indexData = 0;
	//	int[] readRawBuffer() {
	//	  int[] result = null;
	//	  if ( indexData < fileDataArray.length - SKIN_CELLS ) {
	//	    byte[] skinBuffer = new byte[SKIN_CELLS];
	//	    int indexBuffer = 0;
	//	    byte b;
	//	    while ((b = (byte) fileDataArray[indexData++]) != 0x00)
	//	      skinBuffer[indexBuffer++] = b;
	//
	//	    result = new int[SKIN_CELLS];
	//	    for (int i = 0; i < SKIN_CELLS; i++) {
	//	      result[i] = skinBuffer[i] & 0xFF;
	//	    }
	//	  } else
	//	    indexData = 0;
	//	  return result;
	//	}
}
