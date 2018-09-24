package util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilFile {

	private UtilFile() {
	}

	/**
	 * 生成時刻を名前に持つディレクトリを作成します。
	 * @param rootDirPath : ルートディレクトリ
	 * @param suffix : ディレクトリ名のsuffix
	 */
	public static void createTimestampDir(String rootDirPath, String suffix)  {
		SimpleDateFormat fmt = new SimpleDateFormat("yyMMddHHmmss");
		String name = rootDirPath + fmt.format(new Date()) + suffix;

		File dir = new File(name);
		if ( dir.mkdir() ) {
			System.out.println("mkdir Success. name:" + name);
		} else {
			System.out.println("mkdir Failure. name:" + name);
		}
	}

}
