# Transactional Memory

Simple in-memory transactional database of native objects with persistent log.

Use `scripts/test` to run tests and `scripts/posts-example` to run examples.

# Example

*For the actual code go to [examples/PostsExample.java](examples/PostsExample.java)*

Define your Domain Models.

``` Java
public static class Post {
  private String text;
  public String getText() {...}
  public void setText(String text) {...}
}

public static class Posts extends TransactionalMemory {
  public Post get(int i) {...}
  public boolean add(Post post) {...}
  public int size() {...}
}
```

And use it as plain native Java Objects, changes will be transactional and persistent.

*Transactional means that either the whole changes will be applied or nothing (if canceled
explicitly or exception thrown).*

``` Java
// Creating collection of Posts.
final Posts posts = new Posts();

// One post will be added.
atomic(new Transaction() { public void run() {
  posts.add(new Post("First post..."));
}});

// Name of the first post will be changed and the second Post will be added,
// transactionally.
atomic(new Transaction() { public void run() {
  posts.get(0).setText("Another name");
  posts.add(new Post("Second post..."));
}});

// Nothing will be changed.
atomic(new Transaction() { public void run() {
  posts.get(0).setText("Yet another name");
  posts.add(new Post("Third post..."));

  // Rolling back changes.
  rollback();
}});
```

To make operation transactional you need to explain how to roll it back, code below
shows how the `setText` operation implemented.

``` Java
public static class Post {
  private String text;
  
  public String getText() { return text; }
  
  public void setText(String text) {
    atomize(this, "setText", text).with(this.text);
    this.text = text;
  }
  public void rollbackSetText(String text, String oldText) { this.text = oldText; }
}
```

# Persistence

Not finished yet...

# Questions

- Variable visibility in inner and anonymous classes.