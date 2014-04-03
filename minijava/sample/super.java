class Super {
  public static void main (String[] a) {
    System.out.println(new Main().Start());
  }
}

class SuperClass {
  int code;
  
  public int Init() {
    code = 1;
    return code;
  }
  
  public int DoSomeStuff() {
    code = code + 1;
    return code;
  }
}

class SubClass extends SuperClass {
  public int DoSomeStuff() {
    int temp;
    temp = super.DoSomeStuff();
    code = code + 2;
    return code;
  }
}

class Main {
  public int Start() {
    SuperClass s;
    int temp;
    s = new SubClass();
    
    temp = s.Init();
    System.out.println(s.DoSomeStuff());
    return 0;
  }
}