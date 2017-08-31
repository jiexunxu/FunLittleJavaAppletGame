package bullethell.applet;

public class DLList<E> {
	DLNode<E> head;
	int size=0;
	
	void add(E e){
		DLNode<E> n=new DLNode<E>(e);
		if(head==null){
			head=n;
		}else{
			head.prev=n;
			n.next=head;
			head=n;
		}
		size++;
	}
	
	void remove(DLNode<E> n){
		if(n.prev==null){
			head=n.next;
			if(n.next!=null)
				n.next.prev=null;
		}else{
			n.prev.next=n.next;
			if(n.next!=null)
				n.next.prev=n.prev;
		}
		size--;
	}
}