package memdb;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;

public class TransactionMemoryTest implements Serializable {

  private Data db, originalDb;

  @Before
  public void prepareData() {
    db = new Data();
    db.update(new Transaction() {
      public void run() {
        db.posts.add(new Post("A", "A1", "A2"));
        db.posts.add(new Post("B", "B1", "B2"));
      }
    });

    this.originalDb = (Data) TransactionalMemory.deepClone(db);
  }

  @Test
  public void shouldCommitTransaction() {
    db.update(new Transaction() {
      public void run() {
        db.posts.add(new Post("C"));
        db.posts.get(0).addTag("A3");
      }
    });

    assertEquals(db.posts.size(), 3);
    assertEquals(db.posts.get(2).getText(), "C");
    String[] newTags = {"A1", "A2", "A3"};
    assertEquals(db.posts.get(0).getTags(), newTags);
  }

  @Test
  public void shouldRollbackTransaction() {
    db.update(new Transaction() {
      public void run() {
        db.posts.add(new Post("C"));
        db.posts.get(0).addTag("A3");
        this.rollback();
      }
    });

    assertEquals(db.posts.size(), 2);
    String[] tags = {"A1", "A2"};
    assertEquals(db.posts.get(0).getTags(), tags);
  }

  // Data structures.
  class Post implements Serializable {
    private String text;
    private String[] tags;

    public Post(String text, String... tags) {
      this.text = text;
      this.tags = tags;
    }

    public String getText() { return text; }

    public String[] getTags() { return tags; }

    public void addTag(String tag) {
      Transaction.add(this, "add", tag).with(tags);
      String[] newTags = new String[tags.length + 1];
      System.arraycopy(tags, 0, newTags, 0, tags.length);
      newTags[newTags.length - 1] = tag;
      tags = newTags;
    }

    public void rollbackAddTag(String tag, String[] oldTags) { this.tags = oldTags; }
  }

  class Posts implements Serializable {
    private ArrayList<Post> list = new ArrayList<Post>();

    public boolean add(Post post) {
      Transaction.add(this, "add", post);
      return list.add(post);
    }

    public void rollbackAdd(Post post) { if (list.get(list.size()) == post) list.remove(list.size() - 1); }

    public Post get(int i) { return list.get(i); }

    public int size() { return list.size(); }
  }

  class Data extends TransactionalMemory implements Serializable {
    public Posts posts = new Posts();
  }
}