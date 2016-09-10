package beeb.rpn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MyClassLoader extends ClassLoader{

	String toLoad;
	// toload is package.classname   no / or .class
    public MyClassLoader(ClassLoader parent,String toLoad) {
        super(parent);
        this.toLoad=toLoad;
    }
    
    public Class loadClass() throws ClassNotFoundException {
    	return this.loadClass(toLoad);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        if(!name.startsWith(toLoad))
                return super.loadClass(name);

        try {
        	String toLoadClass = name.replace('.', '/').concat(".class");
            String url = "file:basic/classes/"+toLoadClass;
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

            while(data != -1){
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            return defineClass(name,
                    classData, 0, classData.length);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean loaded(String clName) {
    	return ( null == this.findLoadedClass(clName) ) ;
    }
}