package example;

class A {
    int f;
    int[] arr = new int[10];
}
class Main{

    public static void main(String[] args) {
        A a = new A();
        a.f = 10;
        a.arr[0] = 11;
        System.out.println("Hello test " + a.f + " " + a.arr[0]);
    }
}