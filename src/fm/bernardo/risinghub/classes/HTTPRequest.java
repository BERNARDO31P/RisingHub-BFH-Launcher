package fm.bernardo.risinghub.classes;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;

import static com.jcabi.manifests.Manifests.read;

public final class HTTPRequest
{

    public static String send (final String url, final String getArguments) throws Exception
    {
        final HttpURLConnection con = (HttpURLConnection) new URL(url + getArguments).openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (" + System.getProperty("os.name") + ") RisingHub/" + read("Application-Version") + " REQUESTER");

            final BufferedInputStream in = new BufferedInputStream(con.getInputStream());
            final String message = IOUtils.toString(in);

            in.close();
            return message;
    }

}
