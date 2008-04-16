package org.mortbay.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

/* ------------------------------------------------------------ */
/** Queue backed by circular array.
 * 
 * This partial Queue implementation (also with {@link #pop()} for stack operation)
 * is backed by a growable circular array.
 * 
 * @author gregw
 *
 * @param <E>
 */
public class ArrayQueue<E> extends AbstractList<E> implements Queue<E>
{
    private Object[] _elements;
    private int _nextE;
    private int _nextSlot;
    private int _size;
    private int _growCapacity;
    
    public ArrayQueue()
    {
        _elements=new Object[64];
        _growCapacity=32;
    }
    
    public ArrayQueue(int capacity)
    {
        _elements=new Object[capacity];
        _growCapacity=-1;
    }
    
    public ArrayQueue(int initCapacity,int growBy)
    {
        _elements=new Object[initCapacity];
        _growCapacity=growBy;
    }
    
    
    public synchronized boolean add(E e)
    {
        _size++;
        _elements[_nextSlot++]=e;
        if (_nextSlot==_elements.length)
            _nextSlot=0;
        if (_nextSlot==_nextE)
        {
            if (_growCapacity<=0)
                throw new IllegalStateException("Full");
           
            Object[] elements=new Object[_elements.length+_growCapacity];
                
            int split=_elements.length-_nextE;
            if (split>0)
                System.arraycopy(_elements,_nextE,elements,0,split);
            if (_nextE!=0)
                System.arraycopy(_elements,0,elements,split,_nextSlot);
            
            _elements=elements;
            _nextE=0;
            _nextSlot=_size;
        }
        
        return true;
    }

    public synchronized E element()
    {
        if (_nextSlot==_nextE)
            throw new NoSuchElementException();
        return (E)_elements[_nextE];
    }

    public synchronized boolean offer(E e)
    {
        _size++;
        _elements[_nextSlot++]=e;
        if (_nextSlot==_elements.length)
            _nextSlot=0;
        if (_nextSlot==_nextE)
        {
            if (_growCapacity<=0)
                return false;
            
            Object[] elements=new Object[_elements.length+_growCapacity];
                
            int split=_elements.length-_nextE;
            if (split>0)
                System.arraycopy(_elements,_nextE,elements,0,split);
            if (_nextE!=0)
                System.arraycopy(_elements,0,elements,split,_nextSlot);
            
            _elements=elements;
            _nextE=0;
            _nextSlot=_size;
        }
        
        return true;
    }

    public synchronized E peek()
    {
        if (_nextSlot==_nextE)
            return null;
        return (E)_elements[_nextE];
    }

    public synchronized E poll()
    {
        if (_size==0)
            return null;
        E e = (E)_elements[_nextE];
        _elements[_nextE]=null;
        _size--;
        if (++_nextE==_elements.length)
            _nextE=0;
        return e;
    }
    
    public synchronized E remove()
    {
        if (_nextSlot==_nextE)
            throw new NoSuchElementException();
        E e = (E)_elements[_nextE++];
        if (_nextE==_elements.length)
            _nextE=0;
        return e;
    }

    public synchronized void clear()
    {
        _size=0;
        _nextE=0;
        _nextSlot=0;
    }

    /* ------------------------------------------------------------ */
    public boolean isEmpty()
    {
        return _size==0;
    }

    /* ------------------------------------------------------------ */
    public int size()
    {
        return _size;
    }

    /* ------------------------------------------------------------ */
    public synchronized E get(int index)
    {
        if (index>=_size)
            throw new IndexOutOfBoundsException(index+">="+_size);
        int i = _nextE+index;
        if (i>=_elements.length)
            i-=_elements.length;
        return (E)_elements[i];
    }

}
