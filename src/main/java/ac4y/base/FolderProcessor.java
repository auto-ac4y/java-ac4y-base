package ac4y.base;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

public class FolderProcessor {

	public Object processOnFolder(String aPath, Ac4yProcess aAc4yProcess, Object aProcessArgument) throws Ac4yException, ClassNotFoundException, SQLException, IOException, ParseException {

		if (aAc4yProcess == null)
			throw new Ac4yException("process is empty!");

		Object result = null;

		File folder = new File(aPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].isFile()) {

				result =
					aAc4yProcess.process(
						new Ac4yProcessContext(
							aProcessArgument,
							listOfFiles[i].getAbsolutePath()
						)
					);

		      }
		  }

		return result;

	} // processOnFolder

} // FolderProcessor
