package ohrrpgce.henceforth;

/**
 * A specialized stack implementation for the Henceforth VM.
 * @author Seth N. Hetu
 */
public class SuperStack {
	
	private int[] stack;
	private int top;
	
	public SuperStack() {
		stack = new int[100];
		top = -1;
	}
	
	public void push(int val) {		
		//Size?
		if (top >= stack.length-1)
			throw new RuntimeException("Henceforth stack overflow on \"push\": " + stack.length);
			
		//Push
		stack[++top] = val;
	}
	
	//Removing
	public int pop() {
		if (top<=-1)
			throw new RuntimeException("Henceforth stack underflow on \"pop\"");
		
		//Pop
		return stack[top--];
	}
	public void remove(int numElements) {		
		//Pop
		top -= numElements;
	}
	
	//General access
	public int top() {
		if (top<=-1)
			throw new RuntimeException("Henceforth stack underflow on \"top\"");
		
		return stack[top];
	}
	public int penultimate() {
		if (top<=0)
			throw new RuntimeException("Henceforth stack underflow on \"penultimate\"");
		
		return stack[top-1];
	}
	public int antepenultimate() {
		if (top<=1)
			throw new RuntimeException("Henceforth stack underflow on \"antepenultimate\"");
		
		return stack[top-2];
	}
	
	//Specialized
	public void swap() {
		if (top<=0)
			throw new RuntimeException("Henceforth stack underflow on \"swap\"");
		
		int temp = stack[top];
		stack[top] = stack[top-1];
		stack[top-1] = temp;
	}
	public void rotate() {
		if (top<=1)
			throw new RuntimeException("Henceforth stack underflow on \"rotate\"");
		
		int temp = stack[top];
		stack[top] = stack[top-2];
		stack[top-2] = temp;
	}
	

}
