package example;

class A {
    long f;
    static int g;
    int[] arr = new int[10];
}
class Main{

    public static void main(String[] args) {
        A a = new A();
        a.f = 10;
        A.g = 12;
        a.arr[0] = 11;
        a.arr[1] = 13;
        System.out.println("Hello test " + a.f + " " + a.arr[0] + " " + A.g);
    }
}