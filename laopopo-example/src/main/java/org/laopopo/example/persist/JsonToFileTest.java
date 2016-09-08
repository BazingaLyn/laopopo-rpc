package org.laopopo.example.persist;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class JsonToFileTest {
	
	private static String storePathRootDir = System.getProperty("user.home") + File.separator + "test" + File.separator + "war3School.json";
	
	public static void main(String[] args) throws IOException {
		
		Map<Teacher,List<Student>> maps = new HashMap<Teacher,List<Student>>();
		
		
		Teacher teacher = new Teacher(1, "梁老师");
		
		Student student1 = new Student(1, "王诩文", 26);
		Student student2 = new Student(2, "朴俊", 27);
		Student student3 = new Student(3, "陆维梁", 28);
		Student student4 = new Student(4, "曾卓", 28);
		List<Student> students = new ArrayList<Student>();
		students.add(student1);
		students.add(student2);
		students.add(student3);
		students.add(student4);
		
		maps.put(teacher, students);
		
		//json to str
		String str = JSON.toJSONString(maps);
		
		//获取到自定义的用户名
		String fileName = storePathRootDir;
		
		//先将要保存的字符串保存到一个临时文件下
		String tmpFile = fileName + ".tmp";
        string2FileNotSafe(str, tmpFile);

        //备份文件夹
        String bakFile = fileName + ".bak";
        //如果该文件已经存在的情况下
        String prevContent = file2String(fileName);
        if (prevContent != null) {
            string2FileNotSafe(prevContent, bakFile);
        }

        File file = new File(fileName);
        file.delete();

        file = new File(tmpFile);
        file.renameTo(new File(fileName));
		
	}
	
	public static final String file2String(final String fileName) {
        File file = new File(fileName);
        return file2String(file);
    }
	
	public static final String file2String(final File file) {
        if (file.exists()) {
            char[] data = new char[(int) file.length()];
            boolean result = false;

            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
                int len = fileReader.read(data);
                result = (len == data.length);
            }
            catch (IOException e) {
                // e.printStackTrace();
            }
            finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (result) {
                String value = new String(data);
                return value;
            }
        }
        return null;
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
	

}
