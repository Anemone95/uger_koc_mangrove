
public class TestClass {

	static int intField = 0;

	public static void main(String[] args) {
		int localVar = 5;
		int z;
		if (intField != 0) {
			z = localVar / intField;
		} else {
			z = localVar;
		}
	}

}
