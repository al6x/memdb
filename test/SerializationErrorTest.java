import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SerializationErrorTest implements Serializable {
  @Test
  public void run() throws IOException {
    class Post implements Serializable {}
    serialize(new Post());
  }

  public static byte[] serialize(Object obj) throws IOException {
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    ObjectOutputStream o = new ObjectOutputStream(b);
    o.writeObject(obj);
    return b.toByteArray();
  }

}