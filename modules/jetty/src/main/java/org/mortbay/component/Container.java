//========================================================================
//Copyright 2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.component;
import java.util.EventListener;

import org.mortbay.log.Log;
import org.mortbay.util.LazyList;

/* ------------------------------------------------------------ */
/** Container.
 * Static utility for generating containment events from setter methods.
 * The style of usage is: <pre>
 *   public void setFoo(Foo foo)
 *   {
 *       Container.update(this,this.foo,foo,"foo");
 *       this.foo=foo;
 *   }
 *   
 *   public void setBars(Bar[] bars)
 *   {
 *       Container.update(this,this.bars,bars,"bar");
 *       this.bars=bars;
 *   }
 * </pre>
 *   
 * @author gregw
 *
 */
public class Container
{
    private static Object __listeners;
    
    public synchronized static void addEventListener(Container.Listener listener)
    {
        __listeners=LazyList.add(__listeners,listener);
    }
    
    public synchronized static void removeEventListener(Container.Listener listener)
    {
        __listeners=LazyList.remove(__listeners,listener);
    }
    
    /* ------------------------------------------------------------ */
    /** Update single parent to child relationship.
     * @param parent The parent of the child.
     * @param oldChild The previous value of the child.  If this is non null and differs from <code>child</code>, then a remove event is generated.
     * @param child The current child. If this is non null and differs from <code>oldChild</code>, then an add event is generated.
     * @param relationship The name of the relationship
     */
    public synchronized static void update(Object parent, Object oldChild, final Object child, String relationship)
    {
        if (oldChild!=null && !oldChild.equals(child))
            remove(parent,oldChild,relationship);
        if (child!=null && !child.equals(oldChild))
            add(parent,child,relationship);
    }

    /* ------------------------------------------------------------ */
    /** Update multiple parent to child relationship.
     * @param parent The parent of the child.
     * @param oldChildren The previous array of children.  A remove event is generated for any child in this array but not in the  <code>children</code> array.
     * @param children The current array of children. An add event is generated for any child in this array but not in the <code>oldChildren</code> array.
     * @param relationship The name of the relationship
     */
    public synchronized static void update(Object parent, Object[] oldChildren, final Object[] children, String relationship)
    {
        Object[] newChildren = null;
        if (children!=null)
        {
            newChildren = new Object[children.length];
        
            for (int i=children.length;i-->0;)
            {
                boolean new_child=true;
                if (oldChildren!=null)
                {
                    for (int j=oldChildren.length;j-->0;)
                    {
                        if (children[i]!=null && children[i].equals(oldChildren[j]))
                        {
                            oldChildren[j]=null;
                            new_child=false;
                        }
                    }
                }
                if (new_child)
                    newChildren[i]=children[i];
            }
        }
        
        if (oldChildren!=null)
        {
            for (int i=oldChildren.length;i-->0;)
            {
                if (oldChildren[i]!=null)
                    remove(parent,oldChildren[i],relationship);
                oldChildren[i]=null;
            }
        }
        
        if (newChildren!=null)
        {
            for (int i=0;i<newChildren.length;i++)
                if (newChildren[i]!=null)
                    add(parent,newChildren[i],relationship);
        }
    }

    /* ------------------------------------------------------------ */
    /** Add a parent child relationship
     * @param parent
     * @param child
     * @param relationship
     */
    private static void add(Object parent, Object child, String relationship)
    {
        if (Log.isDebugEnabled())
            Log.debug("Container "+parent+" + "+child+" as "+relationship);
        if (__listeners!=null)
        {
            Event event=new Event(parent,child,relationship);
            for (int i=0; i<LazyList.size(__listeners); i++)
                ((Listener)LazyList.get(__listeners, i)).add(event);
        }
    }
    
    /* ------------------------------------------------------------ */
    /** remove a parent child relationship
     * @param parent
     * @param child
     * @param relationship
     */
    private static void remove(Object parent, Object child, String relationship)
    {
        if (Log.isDebugEnabled())
            Log.debug("Container "+parent+" - "+child+" as "+relationship);
        if (__listeners!=null)
        {
            Event event=new Event(parent,child,relationship);
            for (int i=0; i<LazyList.size(__listeners); i++)
                ((Listener)LazyList.get(__listeners, i)).remove(event);
        }
    }
    
    /* ------------------------------------------------------------ */
    /** A Container event.
     * @see Listener
     *
     */
    public static class Event
    {
        private Object _parent;
        private Object _child;
        private String _relationship;
        
        private Event(Object parent,Object child, String relationship)
        {
            _parent=parent;
            _child=child;
            _relationship=relationship;
        }
        
        public Object getChild()
        {
            return _child;
        }
        
        public Object getParent()
        {
            return _parent;
        }
        
        public String getRelationship()
        {
            return _relationship;
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Listener.
     * A listener for Container events.
     */
    public interface Listener extends EventListener
    {
        public void add(Container.Event event);
        public void remove(Container.Event event);
    }
}
