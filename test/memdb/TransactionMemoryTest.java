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

    assertEquals(3, db.posts.size());
    assertEquals("C", db.posts.get(2).getText());
    String[] newTags = {"A1", "A2", "A3"};
    assertEquals(newTags, db.posts.get(0).getTags());
  }

  @Test
  public void shouldRollbackTransaction() {
    db.update(new Transaction() {
      public void run() {
        db.posts.add(new Post("C"));
        db.posts.get(0).addTag("A3");
        rollback();
      }
    });

    assertEquals(2, db.posts.size());
    String[] tags = {"A1", "A2"};
    assertEquals(tags, db.posts.get(0).getTags());
  }

  @Test
  public void shouldNotCallRollbackTwice() {
    class ShouldNotRollbackTwice extends TransactionalMemory {
      public int count = 0;
      public void run() {Transaction.add(this, "run");}
      public void rollbackRun() {
        count++;
        throw new RuntimeException("can't rollback!");
     }
    }

    final ShouldNotRollbackTwice db = new ShouldNotRollbackTwice();
    Exception exception = null;
    try {
      db.update(new Transaction() {
        public void run() {
          db.run();
          rollback();
        }
      });
    } catch (Exception e) {
      exception = e;
    }

    assertEquals("java.lang.RuntimeException: java.lang.RuntimeException: can't rollback!", exception.getMessage());
    assertEquals(1, db.count);
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
      Transaction.add(this, "addTag", tag).with(tags);
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
      Transaction.add(this, "add", post).with(list.size());
      return list.add(post);
    }

    public void rollbackAdd(Post post, Integer oldSize) {
      if (list.size() > oldSize) list.remove(list.size() - 1);
    }

    public Post get(int i) { return list.get(i); }

    public int size() { return list.size(); }
  }

  class Data extends TransactionalMemory implements Serializable {
    public Posts posts = new Posts();
  }
}