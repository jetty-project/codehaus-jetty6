/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.servlet;

import java.util.EventListener;

	/** Implementations of this interface receive notifications of
	** changes to the attribute list on the servlet context of a web application. 
	* To receive notification events, the implementation class
	* must be configured in the deployment descriptor for the web application.
	* @see ServletContextAttributeEvent
	 * @since	v 2.3
	*/

public interface ServletContextAttributeListener extends EventListener {
	/** Notification that a new attribute was added to the servlet context. Called after the attribute is added.*/
public void attributeAdded(ServletContextAttributeEvent scab);
	/** Notification that an existing attribute has been removed from the servlet context. Called after the attribute is removed.*/
public void attributeRemoved(ServletContextAttributeEvent scab);
	/** Notification that an attribute on the servlet context has been replaced. Called after the attribute is replaced. */
public void attributeReplaced(ServletContextAttributeEvent scab);
}

