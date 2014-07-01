# Transactional Memory

Simple in-memory transactional database of native objects with persistent log.

Use `scripts/test` to run tests, also take a look at [examples](examples).

# Example

*For the actual code go to [examples/PostsExample.java](examples/PostsExample.java)*

Define your Domain Model.

``` Java
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
public static class Posts extends TransactionalMemory {
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
```

Use it as plain native Java Objects, changes will be transactional and persistent.

*Transactional means that either the whole changes will be applied or nothing (if canceled
explicitly or exception thrown).*

``` Java
// Creating collection of Posts.
final Posts posts = new Posts();

// One post will be added.
posts.update(new Transaction() { public void run() {
  posts.add(new Post("First post..."));
}});

// Name of the first post will be changed and the second one will be added, transactionally.
posts.update(new Transaction() { public void run() {
  posts.get(0).setText("Another name");
  posts.add(new Post("Second post..."));
}});

// Nothing will be changed.
posts.update(new Transaction() { public void run() {
  posts.get(0).setText("Yet another name");
  posts.add(new Post("Third post..."));
  rollback();
}});
```

# Persistence

Not finished yet...

# Questions

- Variable visibility in inner and anonymous classes.