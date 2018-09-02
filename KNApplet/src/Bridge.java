import tools.KClass;
import tools.Parameter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.KLoader;

/**
 *
 * @author Flav, SeBi
 */
public class Bridge {
	private final KClass groupChat;
        
	public Bridge() throws UnsupportedEncodingException {
            KLoader loader = new KLoader();
            groupChat = new KClass(loader, "Start");
            groupChat.invokeMethod("init2");
            
            byte[] buffer = decompress(compress("t\0".getBytes("UTF-8")));
         //   System.out.println(new String(buffer, "UTF-8").replace("\0", "\\0"));
        }

	private void listen() {
		try {
                    DataInputStream in = new DataInputStream(System.in);
                    DataOutputStream out = new DataOutputStream(System.out);

                    while (true) {
                            byte action = in.readByte();
                            int length = in.readInt();
                            byte[] buffer = new byte[length];
                            in.read(buffer, 0, length);

                            switch (action) {
                                    case 0x01:
                                            buffer = compress(buffer);
                                            break;
                                    case 0x02:
                                            buffer = decompress(buffer);
                                            break;
                            }

                            out.writeInt(buffer.length);
                            out.write(buffer);
                    }
            } catch (IOException e) {
                    e.printStackTrace();
            }
	}

        public byte[] compress(byte[] buffer) {
            return (byte[]) this.groupChat.invokeMethod("compress", buffer);
        }
        public byte[] decompress(byte[] buffer) {
            return (byte[]) this.groupChat.invokeMethod("decompress", buffer);
        }
        
	public static void main(String args[]) throws UnsupportedEncodingException {
		new Bridge().listen();
	}
}
