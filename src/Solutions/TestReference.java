package Solutions;

import java.util.ArrayList;

public class TestReference {
	
	double num;
	double den;
	ArrayList<Integer> x;
	
	public TestReference() {
		num = 100;
		den = 50;
		x = new ArrayList<Integer>();
	}
	
	public void changeNum() {
		subMethod(num);
	}
	
	private void subMethod(double num) {
		num = 25;
	}
	
	public void changeDen() {
		den = 25;
	}
	
	public void changeX() {
		subMethod2(x);
	}
	
	private void subMethod2(ArrayList<Integer> x) {
		x.add(1);
	}
	
	public void print() {
		System.out.println(num + " : " + den);
		System.out.println(x.toString());
	}
	
	public static void main(String[] args) {
		TestReference tr = new TestReference();
		tr.print();
		tr.changeNum();
		tr.print();
		tr.changeDen();
		tr.print();
		tr.changeX();
		tr.print();
	}
}
