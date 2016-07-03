public class Test {
	
	public static enum E { 
		A(0), B(1), C(2), D(3);
		public int val;
		E(int val) { this.val = val; }
		public String toString() { return "" + val; }
		 }

	public static void main(String[] args) {
		Cube cube = new Cube(true);
		System.out.println(cube.toString());

		System.out.println("Rotate white face clockwise");
		cube.rotate(Cube.WHITE, Cube.CW);

		System.out.println(cube.toString());

		System.out.println("Rotate red face double-counterclockwise");
		cube.rotate(Cube.RED, Cube.DBL_CCW);

		System.out.println(cube.toString());

		System.out.println("F, U, L', RR with red as front");
		cube.execute(new String[]{ "F", "U", "L'", "RR" }, Cube.RED);
		System.out.println(cube.toString());
		System.out.println(cube.getLog().toString() + "\n");

		System.out.println("Make 100 more random moves");
		cube.mix(10);
		System.out.println(cube.toString());
		System.out.println(cube.getLog().toString() + "\n");

		cube.getLog().clear();

		Solver s = new Solver(cube);

		if(s.solve()) {
			System.out.println("Solved");
		} else {
			System.out.println("Not solved");
		}

		System.out.println(cube.toString());
		System.out.println(cube.getLog().toString() + "\n");
	}
}
