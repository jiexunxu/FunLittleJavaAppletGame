package bullethell.applet;

class DLNode<E> {
	DLNode<E> prev;
	DLNode<E> next;
	E e;
	
	DLNode(E e){ this.e=e; }
}

