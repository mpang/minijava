class InstanceOf {
  public static void main(String[] a) {
    System.out.println(new Main().Start());
  }
}

class Car {
  int code;

  public int Init() {
    code = 1;
    return 0;
  }

  public int PrintCode() {
    System.out.println(code);
    return 0;
  }
}

class Suv extends Car {
  public int Init() {
    code = 2;
    return 0;
  }
  
  public int SuvCode() {
    return 20;
  }
}

class Limo extends Car {
  public int dummy() {
    return 0;
  }
}

class Main {
  public int Start() {
    Car car;
    Car suv;
    Car limo;
    Suv realSuv;
    int temp;

    car = new Car();
    suv = new Suv();
    limo = new Limo();

    temp = car.Init();
    temp = suv.Init();

    temp = car.PrintCode();
    temp = suv.PrintCode();

    if (suv instanceof Suv) {
      realSuv = (Suv) suv;
      System.out.println(realSuv.SuvCode());
    } else {
      System.out.println(4);
    }
    
    if (limo instanceof Suv) {
      System.out.println(5);
    } else {
      System.out.println(6);
    }

    return 0;
  }
}
