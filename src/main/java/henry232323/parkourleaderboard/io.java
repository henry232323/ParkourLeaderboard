package henry232323.parkourleaderboard;

import java.io.*;

public class io {
    public static void save(Object o, File f) {
        try {
            if (!f.exists()) {
                f.createNewFile();
            }

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(o);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Object load(File f) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            Object result = ois.readObject();
            ois.close();
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
}
