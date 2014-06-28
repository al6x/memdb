package memdb;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class TransactionMemoryTest implements Serializable {

  // Data structures.
  class Post implements Serializable {
    public String text;
    public String[] tags;

    public Post() {}

    public Post(String text, String... tags) {
      this.text = text;
      this.tags = tags;
    }
  }
  class Posts extends ArrayList<Post> implements Serializable {}
  class Data extends HashMap<String, Posts> implements Serializable {}

  private Data data, originalData;

  @Before
  public void prepareData(){
    Posts posts = new Posts();
    posts.add(new Post("A", "A1", "A2"));
    posts.add(new Post("A", "A1", "A2"));
    this.data = new Data();
    data.put("posts", posts);

    this.originalData = (Data) TransactionalMemory.deepClone(data);
  }

  @Test
  public void example(){
    System.out.println(this.data);
  }
}