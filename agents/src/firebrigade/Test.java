package firebrigade;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FireArea fa = new FireAreaImpl();
		fa.addFire(10);
		fa.addFire(10);
		System.out.println("Count: " + fa.getBuildingsInArea().size());
		fa.removeFire(10);
		System.out.println("Count: " + fa.getBuildingsInArea().size());
	}

}
