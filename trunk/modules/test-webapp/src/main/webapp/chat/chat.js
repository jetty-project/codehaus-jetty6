



var MembershipHandler = 
{ 
  join: function()
  {
    var name = $('username').value;
    if (name == null || name.length==0 )
    {
      alert("Please enter a username!");
    }
    else
    {
      ajaxEngine.sendRequest('join',"name=" + name);
    }
  },
  
  ajaxUpdate: function(ajaxResponse) 
  {
     if ("left" == ajaxResponse.getAttribute('id'))
     {
       // switch the input form
       $('join').className='';
       $('joined').className='hidden';
       $('username').focus();
     }
     else
     {
       // switch the input form
       $('join').className='hidden';
       $('joined').className='';
       $('phrase').focus();
       
       // start polling for events
       ajaxEngine.sendRequest('getEvents');
     }
     
     Behaviour.apply();
     
  }
};

var EventHandler = 
{
  last: "",
  
  chat: function()
  {
    var text = $('phrase').value;
    if (text != null && text.length>0 )
    {
        text=text.replace('%','%25');
        text=text.replace('&','%26');
	ajaxEngine.sendRequest('chat',"text=" + text); // TODO encode ??
	$('phrase').value="";
    }
  },
  
  ajaxUpdate: function(ajaxResponse) 
  {
     var event=ajaxResponse.childNodes[0];
     document.myevent=event;
     
     var chat=$('chat')
     var from=event.attributes['from'].value;
     var alert=event.attributes['alert'].value;
     var text=event.childNodes[0].data;
     
     if ( alert!="true" && from == this.last )
        from="...";
     else
     {
        this.last=from;
        from+=":";
     }
     
     if (alert!="true")
       chat.innerHTML += "<span class=\"from\">"+from+"&nbsp;</span><span class=\"text\">"+text+"</span><br/>";
     else
       chat.innerHTML += "<span class=\"alert\"><span class=\"from\">"+from+"&nbsp;</span><span class=\"text\">"+text+"</span></span><br/>";
     
     chat.scrollTop = chat.scrollHeight - chat.clientHeight;
     
  }
};

var PollHandler = 
{
  ajaxUpdate: function(ajaxResponse) 
  {
     // Poll again for events
     ajaxEngine.sendRequest('getEvents');
  }
};


function initPage()
{
  ajaxEngine.registerRequest('join', "?ajax=join");
  ajaxEngine.registerRequest('leave', "?ajax=leave");
  ajaxEngine.registerRequest('chat', "?ajax=chat"); 
  ajaxEngine.registerRequest('getEvents', "?ajax=getEvents&id=event");
  
  ajaxEngine.registerAjaxElement('members');
  
  ajaxEngine.registerAjaxObject('joined', MembershipHandler);
  ajaxEngine.registerAjaxObject('left', MembershipHandler);
  ajaxEngine.registerAjaxObject('event', EventHandler);
  ajaxEngine.registerAjaxObject('poll', PollHandler);
  
}


var behaviours = 
{ 

  '#username' : function(element)
  {
    element.setAttribute("autocomplete","OFF"); 
    element.onkeypress = function(event)
    {
        if (event && (event.keyCode==13 || event.keyCode==10))
        {
      	  MembershipHandler.join();
	}
    } 
  },
  
  '#joinB' : function(element)
  {
    element.onclick = function()
    {
      MembershipHandler.join();
    }
  },
  
  '#phrase' : function(element)
  {
    element.setAttribute("autocomplete","OFF");
    element.onkeypress = function(event)
    {
        if (event && (event.keyCode==13 || event.keyCode==10))
        {
          EventHandler.chat();
	  return false;
	}
	return true;
    }
  },
  
  '#sendB' : function(element)
  {
    element.onclick = function()
    {
    	EventHandler.chat();
    }
  },
  
  
  '#leaveB' : function(element)
  {
    element.onclick = function()
    {
      ajaxEngine.sendRequest('leave');
    }
  },
};

Behaviour.register(behaviours);
Behaviour.addLoadEvent(initPage);  

