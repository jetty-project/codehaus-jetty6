//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.jetty.util;

import java.util.AbstractList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/* ------------------------------------------------------------ */
/** Queue backed by a circular array.
 * 
 * This queue is uses  a variant of the two lock queue algorithm to
 * provide an efficient queue or list backed by a growable circular
 * array.  This queue also has a partial implementation of 
 * {@link java.util.concurrent.BlockingQueue}, specifically the {@link #take()} and 
 * {@link #poll(long, TimeUnit)} methods.  
 * Unlike {@link java.util.concurrent.ArrayBlockingQueue}, this class is
 * able to grow and provides a blocking put call.
 * <p>
 * The queue has both a capacity (the size of the array currently allocated)
 * and a limit (the maximum size that may be allocated), which defaults to 
 * {@link Integer#MAX_VALUE}.
 * 
 * @param <E> The element type
 */
public class BlockingArrayQueue<E> extends AbstractList<E> implements Queue<E>
{
    public final int DEFAULT_CAPACITY=64;
    public final int DEFAULT_GROWTH=32;
    protected final int _limit;
    protected final AtomicInteger _size=new AtomicInteger();
    protected final int _growCapacity;
    
    protected Object[] _elements;
    protected int _head;
    protected int _tail;
    
    private final ReentrantLock _takeLock = new ReentrantLock();
    private final Condition _notEmpty = _takeLock.newCondition();
    private final ReentrantLock _offerLock = new ReentrantLock();

    /* ------------------------------------------------------------ */
    /** Create a growing partially blocking Queue
     * 
     */
    public BlockingArrayQueue()
    {
        _elements=new Object[64];
        _growCapacity=32;
        _limit=Integer.MAX_VALUE;
    }

    /* ------------------------------------------------------------ */
    /** Create a fixed size partially blocking Queue
     * @param limit The initial capacity and the limit.
     */
    public BlockingArrayQueue(int limit)
    {
        _elements=new Object[limit];
        _growCapacity=-1;
        _limit=limit;
    }

    /* ------------------------------------------------------------ */
    /** Create a growing partially blocking Queue.
     * @param capacity Initial capacity
     * @param growBy Incremental capacity.
     */
    public BlockingArrayQueue(int capacity,int growBy)
    {
        _elements=new Object[capacity];
        _growCapacity=growBy;
        _limit=Integer.MAX_VALUE;
    }

    /* ------------------------------------------------------------ */
    /** Create a growing limited partially blocking Queue.
     * @param capacity Initial capacity
     * @param growBy Incremental capacity.
     * @param limit maximum capacity.
     */
    public BlockingArrayQueue(int capacity,int growBy,int limit)
    {
        if (capacity>limit)
            throw new IllegalArgumentException();
        
        _elements=new Object[capacity];
        _growCapacity=growBy;
        _limit=limit;
    }

    /* ------------------------------------------------------------ */
    public int getCapacity()
    {
        return _elements.length;
    }

    /* ------------------------------------------------------------ */
    public int getLimit()
    {
        return _limit;
    }
    
    /* ------------------------------------------------------------ */
    public boolean add(E e)
    {
        return offer(e);
    }
    
    /* ------------------------------------------------------------ */
    public E element()
    {
        E e = peek();
        if (e==null)
            throw new NoSuchElementException();
        return e;
    }
    
    /* ------------------------------------------------------------ */
    public E peek()
    {
        if (_size.get() == 0)
            return null;
        
        E e = null;
        _takeLock.lock(); // Size cannot shrink
        try 
        {
            
            if (_size.get() > 0) 
                e = (E)_elements[_head];
        } 
        finally 
        {
            _takeLock.unlock();
        }
        
        return e;
    }

    /* ------------------------------------------------------------ */
    public boolean offer(E e)
    {
        if (e == null) 
            throw new NullPointerException();
        
        boolean not_empty=false;
        _offerLock.lock();  // size cannot grow... only shrink
        try 
        {
            if (_size.get() >= _limit) 
                return false;
            else
            {
                // should we expand array?
                if (_size.get()==_elements.length)
                {
                    _takeLock.lock();   // Need to grow array
                    try
                    {
                        if (!grow())
                            return false;
                    }
                    finally
                    {
                        _takeLock.unlock();
                    }
                }

                // add the element
                _elements[_tail]=e;
                _tail=(_tail+1)%_elements.length;
                
                not_empty=0==_size.getAndIncrement();
            }
        } 
        finally 
        {
            _offerLock.unlock();
        }
        
        if (not_empty)
        {
            _takeLock.lock();
            try
            {
                _notEmpty.signal();
            }
            finally
            {
                _takeLock.unlock();
            }
        }  

        return true;
    }


    /* ------------------------------------------------------------ */
    public E poll()
    {
        if (_size.get() == 0)
            return null;
        
        E e = null;
        _takeLock.lock(); // Size cannot shrink
        try 
        {
            if (_size.get() > 0) 
            {
                final int head=_head;
                e = (E)_elements[head];
                _head=(head+1)%_elements.length;
                
                if (_size.decrementAndGet()>0)
                    _notEmpty.signal();
            }
        } 
        finally 
        {
            _takeLock.unlock();
        }
        
        return e;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieves and removes the head of this queue, waiting
     * if no elements are present on this queue.
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting.
     */
    public E take() throws InterruptedException
    {
        E e = null;
        _takeLock.lockInterruptibly();  // Size cannot shrink
        try 
        {
            try 
            {
                while (_size.get() == 0)
                {
                    _notEmpty.await();
                }
            } 
            catch (InterruptedException ie) 
            {
                _notEmpty.signal();
                throw ie;
            }

            final int head=_head;
            e = (E)_elements[head];
            _head=(head+1)%_elements.length;

            if (_size.decrementAndGet()>0)
                _notEmpty.signal();
        } 
        finally 
        {
            _takeLock.unlock();
        }
        
        return e;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieves and removes the head of this queue, waiting
     * if necessary up to the specified wait time if no elements are
     * present on this queue.
     * @param timeout how long to wait before giving up, in units of
     * <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the
     * <tt>timeout</tt> parameter
     * @return the head of this queue, or <tt>null</tt> if the
     * specified waiting time elapses before an element is present.
     * @throws InterruptedException if interrupted while waiting.
     */
    public E poll(long time, TimeUnit unit) throws InterruptedException
    {
        
        E e = null;

        long nanos = unit.toNanos(time);
        
        _takeLock.lockInterruptibly(); // Size cannot shrink
        try 
        {    
            try 
            {
                while (_size.get() == 0)
                {
                    if (nanos<=0)
                        return null;
                    nanos = _notEmpty.awaitNanos(nanos);
                }
            } 
            catch (InterruptedException ie) 
            {
                _notEmpty.signal();
                throw ie;
            }

            e = (E)_elements[_head];
            _head=(_head+1)%_elements.length;

            if (_size.decrementAndGet()>0)
                _notEmpty.signal();
        } 
        finally 
        {
            _takeLock.unlock();
        }
        
        return e;
    }

    /* ------------------------------------------------------------ */
    public E remove()
    {
        E e=poll();
        if (e==null)
            throw new NoSuchElementException();
        return e;
    }

    /* ------------------------------------------------------------ */
    public void clear()
    {
        _offerLock.lock();
        try
        {
            _takeLock.lock();
            try
            {
                _head=0;
                _tail=0;
                _size.set(0);
            }
            finally
            {
                _takeLock.unlock();
            }
        }
        finally
        {
            _offerLock.unlock();
        }
    }

    /* ------------------------------------------------------------ */
    public boolean isEmpty()
    {
        return _size.get()==0;
    }

    /* ------------------------------------------------------------ */
    public int size()
    {
        return _size.get();
    }

    /* ------------------------------------------------------------ */
    public E get(int index)
    {
        _offerLock.lock();
        try
        {
            _takeLock.lock();
            try
            {
                if (index<0 || index>=_size.get())
                    throw new IndexOutOfBoundsException("!("+0+"<"+index+"<="+_size+")");
                int i = _head+index;
                if (i>=_elements.length)
                    i-=_elements.length;
                return (E)_elements[i];
            }
            finally
            {
                _takeLock.unlock();
            }
        }
        finally
        {
            _offerLock.unlock();
        }
    }
    
    /* ------------------------------------------------------------ */
    public E remove(int index)
    {
        _offerLock.lock();
        try
        {
            _takeLock.lock();
            try
            {

                if (index<0 || index>=_size.get())
                    throw new IndexOutOfBoundsException("!("+0+"<"+index+"<="+_size+")");

                int i = _head+index;
                if (i>=_elements.length)
                    i-=_elements.length;
                E old=(E)_elements[i];

                if (i<_tail)
                {
                    System.arraycopy(_elements,i+1,_elements,i,_tail-i);
                    _tail--;
                    _size.decrementAndGet();
                }
                else
                {
                    System.arraycopy(_elements,i+1,_elements,i,_elements.length-i-1);
                    if (_tail>0)
                    {
                        _elements[_elements.length]=_elements[0];
                        System.arraycopy(_elements,1,_elements,0,_tail-1);
                        _tail--;
                    }
                    else
                        _tail=_elements.length-1;

                    _size.decrementAndGet();
                }

                return old;
            }
            finally
            {
                _takeLock.unlock();
            }
        }
        finally
        {
            _offerLock.unlock();
        }
    }

    /* ------------------------------------------------------------ */
    public E set(int index, E e)
    {
        if (e == null) 
            throw new NullPointerException();

        _offerLock.lock();
        try
        {
            _takeLock.lock();
            try
            {

                if (index<0 || index>=_size.get())
                    throw new IndexOutOfBoundsException("!("+0+"<"+index+"<="+_size+")");

                int i = _head+index;
                if (i>=_elements.length)
                    i-=_elements.length;
                E old=(E)_elements[i];
                _elements[i]=e;
                return old;
            }
            finally
            {
                _takeLock.unlock();
            }
        }
        finally
        {
            _offerLock.unlock();
        }
    }
    
    /* ------------------------------------------------------------ */
    public void add(int index, E e)
    {
        if (e == null) 
            throw new NullPointerException();

        _offerLock.lock();
        try
        {
            _takeLock.lock();
            try
            {

                if (index<0 || index>_size.get())
                    throw new IndexOutOfBoundsException("!("+0+"<"+index+"<="+_size+")");

                if (index==_size.get())
                {
                    add(e);
                }
                else
                {
                    if (_tail==_head)
                        if (!grow())
                            throw new IllegalStateException("full");

                    int i = _head+index;
                    if (i>=_elements.length)
                        i-=_elements.length;

                    _size.incrementAndGet();
                    _tail=(_tail+1)%_elements.length;


                    if (i<_tail)
                    {
                        System.arraycopy(_elements,i,_elements,i+1,_tail-i);
                        _elements[i]=e;
                    }
                    else
                    {
                        if (_tail>0)
                        {
                            System.arraycopy(_elements,0,_elements,1,_tail);
                            _elements[0]=_elements[_elements.length-1];
                        }

                        System.arraycopy(_elements,i,_elements,i+1,_elements.length-i-1);
                        _elements[i]=e;
                    }
                }
            }
            finally
            {
                _takeLock.unlock();
            }
        }
        finally
        {
            _offerLock.unlock();
        }
    }

    /* ------------------------------------------------------------ */
    private boolean grow()
    {
        if (_growCapacity<=0)
            return false;

        final int s=_size.get();
        final int head=_head;
        final int tail=_tail;

        Object[] elements=new Object[_elements.length+_growCapacity];

        if (head<tail)
            System.arraycopy(_elements,head,elements,0,s);
        else
        {
            System.arraycopy(_elements,head,elements,0,s-head);
            System.arraycopy(_elements,0,elements,s-head,tail);
        }
        _elements=elements;
        _head=0;
        _tail=s;
        
        return true;
    }
}
