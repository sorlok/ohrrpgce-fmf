package ohrrpgce.data.loader;

/**
 * Simple Linked List implementation for our program.
 * @author Seth N. Hetu
 */
public class LinkedList {
	private int size;
	private int maxSize;
	private LinkedNode head;
	private LinkedNode tail;
	
	public LinkedList(int capacity) {
		this.maxSize = capacity;
		this.size = 0;
	}
	
	public int getMaxSize() {
		return maxSize;
	}
        
        public int getSize() {
            return size;
        }
	
	public Object getItemNumber(int id) {
		if (id>=maxSize)
			throw new ArrayIndexOutOfBoundsException("Max size is " + maxSize + "; id requested was " + id);
		int count = 0;
		for (LinkedNode test = head; test!=null; test=test.next) {
			if (count==id)
				return test.data;
			
			count++;
		}
		return null;		
	}
	
	/**
	 * Remove an item from the list.
	 * @param o The object to remove
	 * @return The returned object, or null if it wasn't in the list.
	 */
	public Object removeItem(Object o) {
		for (LinkedNode test = head; test!=null; test=test.next) {
			if (test.data.equals(o)) {
				//Remove this item
				Object res = test.data;
				if (test.next!=null)
					test.next.prev = test.prev;
				if (test.prev != null)
					test.prev.next = test.next;
				
				//If header, footer...
				if (test.equals(tail))
					tail = test.prev;
				if (test.equals(head))
					head = test.next;
					
				//Managerial stuff
				test = null;
				size--;
				return res;
			}
		}
		return null;
	}
	
	/**
	 * Add an object to the front of the list.
	 * @param o The data to add
	 * @return The data that was displaced (if any) to add this item.
	 */
	public Object insertIntoFront(Object o) {
		Object res = null;
		if (size==maxSize)
			res = removeFromBack();
		
		LinkedNode insert = new LinkedNode(o, null, head);
		if (head!=null)
			head.prev = insert;
		else
			tail = insert;
		head = insert;
		size++;
		return res;
	}
	
        
	public Object removeFromBack() {
		Object res = tail.data;
		tail = tail.prev;
                if (tail!=null) //If the list is NOT empty
                    tail.next = null;
		size--;
		return res;
	}

        public LinkedNode getLastNode() {
		LinkedNode res = tail;
		return res;
	}
        
	public class LinkedNode {
		public Object data;
		public LinkedNode next;
		public LinkedNode prev;
		
		public LinkedNode(Object item, LinkedNode prev, LinkedNode next) {
			this.data = item;
			this.prev = prev;
			this.next = next;
		}
	
	}
}
