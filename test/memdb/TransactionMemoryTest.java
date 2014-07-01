package memdb;

import static memdb.Transaction.atomize;
import static memdb.TransactionalMemory.atomic;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;

public class TransactionMemoryTest implements Serializable {

  private Posts posts, originalDb;

  @Before
  public void prepareData() {
    posts = new Posts();
    atomic(new Transaction() {
      public void run() {
        posts.add(new Post("A", "A1", "A2"));
        posts.add(new Post("B", "B1", "B2"));
      }
    });

    this.originalDb = (Posts) TransactionalMemory.deepClone(posts);
  }

  @Test
  public void shouldCommitTransaction() {
    atomic(new Transaction() {
      public void run() {
        posts.add(new Post("C"));
        posts.get(0).setTags(new String[]{"A3"});
      }
    });

    assertEquals(3, posts.size());
    assertEquals("C", posts.get(2).getText());
    String[] newTags = {"A3"};
    assertEquals(newTags, posts.get(0).getTags());
  }

  @Test
  public void shouldRollbackTransaction() {
    atomic(new Transaction() {
      public void run() {
        posts.add(new Post("C"));
        posts.get(0).setTags(new String[]{"A3"});
        rollback();
      }
    });

    assertEquals(2, posts.size());
    String[] tags = {"A1", "A2"};
    assertEquals(tags, posts.get(0).getTags());
  }

  @Test
  public void shouldNotCallRollbackTwice() {
    class ShouldNotRollbackTwice {
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
      atomic(new Transaction() {
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

    public void setText(String text) {
      Transaction.add(this, "setText", text).with(this.text);
      this.text = text;
    }

    public void rollbackSetText(String text, String oldText) { this.text = oldText; }

    public String[] getTags() { return tags; }

    public void setTags(String[] tags) {
      atomize(this, "setTags", (Object) tags).with((Object) this.tags);
      this.tags = tags;
    }

    public void rollbackSetTags(String[] tags, String[] oldTags) { this.tags = oldTags; }
  }

  class Posts implements Serializable {
    private ArrayList<Post> list = new ArrayList<Post>();

    public boolean add(Post post) {
      atomize(this, "add", post).with(list.size());
      return list.add(post);
    }

    public void rollbackAdd(Post post, Integer oldSize) {
      if (list.size() > oldSize) list.remove(list.size() - 1);
    }

    public Post get(int i) { return list.get(i); }

    public int size() { return list.size(); }
  }
}