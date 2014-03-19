package fr.unistra.pelican.util;

import java.util.Iterator;


/**
 * Multi-ensemble trié, optimisé. Supporte tous les objets étendants Number
 * Indispensable pour obenir des performances correctes avec la MM soft
 * @author perret
 *
 */
public class SortedBag  implements Iterator<Number>, Iterable<Number> {

	/**
	 * Implémentation sous forme de liste doublement chainée: Tête et queue
	 */
	private Element head=null;
	private Element tail=null;
	
	private int size=0;
	private int ind=0;
	
	public SortedBag(){}
	
	/**
	 * Ajoute l'élement item
	 * @param item élément à ajouter
	 */
	public void add(Number  item)
	{
		add(item,1);
	}
	
	/**
	 * Ajoute plusieurs fois le même élément
	 * @param item élément à ajouter
	 * @param k nombre de fois à ajouter
	 */
	public void add(Number  item, int k)
	{
		size+=k;
		if (head==null)
		{
			head=new Element(item,k,null,null);
			tail=head;
		}
		else {
			Element ge=findGE(item);
			if(ge!=null && Math.abs(ge.val.doubleValue()-item.doubleValue())<0.00001)
				{ge.add(k);}
			else {
				if (ge == null)
				{
					Element nouv=new Element(item,k,null,head);
					head.p=nouv;
					head=nouv;
				}
				else {
				Element nouv=new Element(item,k,ge,ge.n);
				ge.n=nouv;
				
				if (nouv.n==null) tail=nouv;
				else nouv.n.p=nouv;
				}
			}
		}
	}
	
	/**
	 * Récupère l'élément d'indice n
	 * @param n
	 * @return l'élément cherché, null si il n'existe pas
	 */
	public Number getElementAt(int n)
	{
	Element op=head;
	//System.out.println("Accessing item " + n);
	while (op!=null)
	{
		//System.out.println("Accessing item " + n);
		//System.out.println(op.k + " items in this one");
		if (op.k>n)
			{//System.out.println("Ok taking it ");
			return op.val;}
		else {
			n-=op.k;
			op=op.n;
		}
	}
	return null;
	}
	
	/**
	 * Renvoie l'élément numéro n en ordre inverse
	 * @param n
	 * @return 
	 */
	public Number getReverseElementAt(int n)
	{
	Element op=tail;
	while (op!=null)
	{
		if (op.k>n)
			return op.val;
		else {
			n-=op.k;
			op=op.p;
		}
	}
	return null;
	}
	
	
	/**
	 * Renvoie le premier élément strictement plus grand que item
	 * @param item
	 * @return
	 */
	private Element findGE(Number item)
	{
		Element  op=head;
		while(op!=null && (item.doubleValue()<op.val.doubleValue() || (op.n!=null && item.doubleValue()>=op.n.val.doubleValue())))
			op=op.n;			
		return op;
	}


	/**
	 * Initialize l'itérateur sur l'ensemble
	 *
	 */
	private void initializeIterator()
	{
		ind=0;
	}
	
	public static void main (String [] args)
	{
		SortedBag b=new SortedBag();
		b.add(10,3);
		for (Number n:b)
			System.out.println(""+n);
		System.out.println("");
		
		b.add(2,5);
		for (Number n:b)
			System.out.println(""+n);
		System.out.println("");
		b.add(1,1);
		for (Number n:b)
			System.out.println(""+n);
		System.out.println("");
		b.add(22,28);
		for (Number n:b)
			System.out.println(""+n);
		System.out.println("");
		System.out.println("ahah " + b.getElementAt(0));
		System.out.println("ahah " + b.getElementAt(1));
		System.out.println("ahah " + b.getElementAt(5));
		System.out.println("ahah " + b.getElementAt(6));
		System.out.println("ahah " + b.getReverseElementAt(0));
		System.out.println("ahah " + b.getReverseElementAt(27));
		System.out.println("ahah " + b.getReverseElementAt(28));
		System.out.println("ahah " + b.getReverseElementAt(36));
	}

	/**
	 * Récupère le nombre d'élément dans l'ensemble
	 * @return
	 */
	public int size(){ return size;}
	
	
	public boolean hasNext() {
		return (ind<size);
	}

	public Number next() {
		return getElementAt(ind++);
	}

	/**
	 * non implémenté
	 */
	public void remove() {
		return;	
	}

	public Iterator<Number> iterator() {
		initializeIterator();
		return this;
	}

}

/**
 * Un élément d'un multi ensemble: un nombre et un nombre d'occurence
 * @author Ben
 *
 */
class Element {
	
	/**
	 * Valeur
	 */
	Number val;
	/**
	 * Nombre d'occurences
	 */
	int k;
	/**
	 * Element suivant
	 */
	Element n;
	/**
	 * Element précédent
	 */
	Element p;
	
	/**
	 * COnstructeur
	 * @param val valeur
	 * @param k nombre d'occurences
	 * @param p élément précédent
	 * @param n élement suivant
	 */
	public Element(Number  val, int k,Element p, Element n)
	{
		this.val=val;
		this.k=k;
		this.n=n;
		this.p=p;
	}
	
	/**
	 * Ajoute des occurences à l'élément
	 * @param k nombre POSITIF!!!
	 */
	void add(int k)
	{
		this.k+=k;
	}
}
