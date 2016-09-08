package org.laopopo.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class PersistUtils {
	
	
	public static final void string2File(final String str, final String fileName) throws IOException {
        String tmpFile = fileName + ".tmp";
        string2FileNotSafe(str, tmpFile);

        String bakFile = fileName + ".bak";
        String prevContent = file2String(fileName);
        if (prevContent != null) {
            string2FileNotSafe(prevContent, bakFile);
        }

        File file = new File(fileName);
        file.delete();

        file = new File(tmpFile);
        file.renameTo(new File(fileName));
    }
	
	public static final void string2FileNotSafe(final String str, final String fileName) throws IOException {
        File file = new File(fileName);
        File fileParent = file.getParentFile();
        if (fileParent != null) {
            fileParent.mkdirs();
        }
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(str);
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                }
                catch (IOException e) {
                    throw e;
                }
            }
        }
    }
	
	public static final String file2String(final String fileName) {
		// 读取txt内容为字符串
		StringBuffer txtContent = new StringBuffer();
		// 每次读取的byte数
		byte[] b = new byte[8 * 1024];
		InputStream in = null;
		try {
			// 文件输入流
			in = new FileInputStream(fileName);
			while (in.read(b) != -1) {
				// 字符串拼接
				txtContent.append(new String(b));
			}
			// 关闭流
			in.close();
		} catch (Exception e) {
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return txtContent.toString();
	}

}
