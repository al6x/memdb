package memdb;

import java.util.ArrayList;

public abstract class Transaction {
  static private ThreadLocal<Transaction> threadLocal = new ThreadLocal<Transaction>();

  private ArrayList<Operation> operations = new ArrayList<Operation>();

  public Transaction() { threadLocal.set(this); }

  static public Transaction current() {
    Transaction transaction = threadLocal.get();
    if (transaction == null) throw new RuntimeException("no current transaction!");
    return transaction;
  }

  static public AddDataDsl add(Object object, String method, Object... arguments) {
    return current().addOperation(object, method, arguments);
  }

  public AddDataDsl addOperation(Object object, String method, Object... arguments) {
    Operation operation = new Operation(object, method, arguments);
    operations.add(operation);
    return new AddDataDsl(operation);
  }

  public void rollback() {
  }

  abstract public void run();

  static private class Operation {
    public Object object;
    public String method;
    public Object[] arguments;
    public Object[] data;

    public Operation(Object object, String method, Object[] arguments) {
      this.object = object;
      this.method = method;
      this.arguments = arguments;
    }

    public void addData(Object[] data) { this.data = data; }
  }

  static public class AddDataDsl {
    Operation operation;

    public AddDataDsl(Operation operation) { this.operation = operation; }

    public void with(Object... data) { operation.addData(data); }
  }
}
