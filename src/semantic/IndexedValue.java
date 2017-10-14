package semantic;

public class IndexedValue<E>
{
	public final int index;
	public final E value;
	
	public IndexedValue(int i, E value)
	{
		this.index = i;
		this.value = value;
	}
}
