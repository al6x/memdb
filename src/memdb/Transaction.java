package memdb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public abstract class Transaction {
  static private ThreadLocal<Transaction> threadLocal = new ThreadLocal<Transaction>();
  private boolean rolledBack = false;

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
    if(rolledBack) throw new RuntimeException("can't call rollback twice!");
    rolledBack = true;

    for(int i = 0; i < operations.size(); i++)
      operations.get(i).rollback();
  }

  public boolean rolledBack() { return rolledBack; }

  abstract public void run();

  static private class Operation {
    public Object object;
    public String methodName;
    public Object[] arguments;
    public Object[] data = new Object[0];

    public Operation(Object object, String method, Object[] arguments) {
      this.object = object;
      this.methodName = method;
      this.arguments = arguments;
    }

    private static void callMethod(Object object, String methodName, Object[] arguments) {
      Class[] argumentClasses = new Class[arguments.length];
      for(int j = 0; j < arguments.length; j++)
        argumentClasses[j] = arguments[j].getClass();

      Method method;
      try {
        method = object.getClass().getMethod(methodName, argumentClasses);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }

      try {
        method.invoke(object, arguments);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e.getTargetException());
      }
    }

    public void addData(Object[] data) { this.data = data; }

    public void rollback() {
      String rollbackMethodName = "rollback" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1);

      Object[] argumentsWithData = new Object[arguments.length + data.length];
      System.arraycopy(arguments, 0, argumentsWithData, 0, arguments.length);
      System.arraycopy(data, 0, argumentsWithData, arguments.length, data.length);

      Operation.callMethod(object, rollbackMethodName, argumentsWithData);
    }
  }

  static public class AddDataDsl {
    Operation operation;

    public AddDataDsl(Operation operation) { this.operation = operation; }

    public void with(Object... data) { operation.addData(data); }
  }
}
