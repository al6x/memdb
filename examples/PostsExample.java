import memdb.Transaction;
import memdb.TransactionalMemory;

import java.util.ArrayList;

import static memdb.Transaction.atomize;
import static memdb.TransactionalMemory.atomic;

public class PostsExample {

  // Post.
  public static class Post {
    private String text;

    public Post(String text) { this.text = text; }

    public String getText() { return text; }
    public void setText(String text) {
      atomize(this, "setText", text).with(this.text);
      this.text = text;
    }
    public void rollbackSetText(String text, String oldText) { this.text = oldText; }
  }

  // Collection of Posts.
  public static class Posts {
    private ArrayList<Post> list = new ArrayList<Post>();

    public Post get(int i) { return list.get(i); }

    public boolean add(Post post) {
      atomize(this, "add", post).with(list.size());
      return list.add(post);
    }
    public void rollbackAdd(Post post, Integer oldSize) {
      if (list.size() > oldSize) list.remove(list.size() - 1);
    }

    public int size() { return list.size(); }
  }

  // Running the example.
  public static void main(String [] args) {
    // Creating collection of Posts.
    final Posts posts = new Posts();

    // One post will be added.
    atomic(new Transaction() {
      public void run() {
        posts.add(new Post("First post..."));
      }
    });
    System.out.println("Posts size: " + posts.size());

    // Name of the first post will be changed and the second Post will be added,
    // transactionally.
    atomic(new Transaction() {
      public void run() {
        posts.get(0).setText("Another name");
        posts.add(new Post("Second post..."));
      }
    });
    System.out.println();
    System.out.println("Text of the first post: " + posts.get(0).getText());
    System.out.println("Posts size: " + posts.size());

    // Nothing will be changed.
    atomic(new Transaction() {
      public void run() {
        posts.get(0).setText("Yet another name");
        posts.add(new Post("Third post..."));

        // Rolling back changes.
        rollback();
      }
    });
    System.out.println();
    System.out.println("Text of the first post: " + posts.get(0).getText());
    System.out.println("Posts size: " + posts.size());
  }
}
